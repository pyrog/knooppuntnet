package kpn.database.actions.locations

import kpn.api.common.SurveyDateInfo
import kpn.api.common.changes.filter.ServerFilterGroup
import kpn.api.common.location.LocationRouteInfo
import kpn.api.custom.Day
import kpn.api.custom.Fact
import kpn.api.custom.LocationRoutesType
import kpn.api.custom.NetworkType
import kpn.api.custom.Tags
import kpn.api.custom.Timestamp
import kpn.core.doc.Label
import kpn.core.util.Log
import kpn.core.util.RouteSymbol
import kpn.database.actions.locations.MongoQueryLocationRoutes.log
import kpn.database.base.Database
import kpn.database.util.Mongo
import kpn.server.api.analysis.pages.SurveyDateInfoBuilder
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Accumulators.push
import org.mongodb.scala.model.Accumulators.sum
import org.mongodb.scala.model.Aggregates.filter
import org.mongodb.scala.model.Aggregates.group
import org.mongodb.scala.model.Aggregates.limit
import org.mongodb.scala.model.Aggregates.project
import org.mongodb.scala.model.Aggregates.skip
import org.mongodb.scala.model.Aggregates.sort
import org.mongodb.scala.model.Aggregates.unwind
import org.mongodb.scala.model.Filters.and
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Projections.computed
import org.mongodb.scala.model.Projections.excludeId
import org.mongodb.scala.model.Projections.fields
import org.mongodb.scala.model.Projections.include
import org.mongodb.scala.model.Sorts.ascending
import org.mongodb.scala.model.Sorts.orderBy

case class LocationRouteInfoData(
  id: Long,
  name: String,
  meters: Long,
  lastUpdated: Timestamp,
  lastSurvey: Option[Day],
  tags: Tags,
  broken: Boolean,
  inaccessible: Boolean
)

object MongoQueryLocationRoutes {
  private val log = Log(classOf[MongoQueryLocationRoutes])

  def main(args: Array[String]): Unit = {
    Mongo.executeIn("kpn-laptop") { database =>
      new MongoQueryLocationRoutes(database, SurveyDateInfoBuilder.dateInfo).exploreSurvey()
    }
  }
}

class MongoQueryLocationRoutes(database: Database, surveyDateInfo: SurveyDateInfo) {

  def optionsGroups(): Seq[ServerFilterGroup] = {
    //  Seq(
    //    facet(
    //      Facet("years", pipelineYears: _*),
    //      Facet("months", pipelineMonths: _*),
    //    )
    //  )
    Seq.empty
  }

  def exploreSurvey(): Seq[Bson] = {
    val surveyValue =
      s"""
         |{
         |  survey: {
         |    $$switch: {
         |      branches: [
         |        {
         |          case: {$$not: ["$$lastSurvey"]},
         |          then: "0-unknown"
         |        },
         |        {
         |          case: {$$gte: ["$$lastSurvey", "${surveyDateInfo.lastMonthStart.yyyymmdd}"]},
         |          then: "1-last-month"
         |        },
         |        {
         |          case: {$$gte: ["$$lastSurvey", "${surveyDateInfo.lastHalfYearStart.yyyymmdd}"]},
         |          then: "2-last-half-year"
         |        },
         |        {
         |          case: {$$gte: ["$$lastSurvey", "${surveyDateInfo.lastYearStart.yyyymmdd}"]},
         |          then: "3-last-year"
         |        },
         |        {
         |          case: {$$gte: ["$$lastSurvey", "${surveyDateInfo.lastTwoYearsStart.yyyymmdd}"]},
         |          then: "4-last-two-years"
         |        },
         |      ],
         |      default: "5-older"
         |    }
         |  }
         |}
         |""".stripMargin

    Seq(
      filter(buildFilter(NetworkType.hiking, "be", LocationRoutesType.survey /* !!! */)),
      project(
        BsonDocument(surveyValue),
      ),
      group(
        "$survey",
        sum("count", 1)
      ),
      sort(orderBy(ascending("_id"))),
      project(
        fields(
          excludeId(),
          computed("_id", BsonDocument("""{$substr: ["$_id", 2, 99]}""")),
          include("count")
        )
      )
    ) ++ optionGroupPipeline("survey")
  }

  def optionGroupProposedPipeline(): Seq[Bson] = {
    Seq(
      filter(buildFilter(NetworkType.hiking, "be", LocationRoutesType.all)),
      project(
        fields(
          excludeId(),
          computed("proposed", BsonDocument("""{ $cond: [ "$proposed", "yes", "no" ]}"""))
        )
      ),
      group(
        "$proposed",
        sum("count", 1)
      ),
    ) ++ optionGroupPipeline("proposed")
  }

  def optionGroupFactsPipeline(): Seq[Bson] = {
    Seq(
      filter(buildFilter(NetworkType.hiking, "be", LocationRoutesType.all)),
      unwind("$labels"),
      filter(
        BsonDocument("""{labels: {$regex: "fact-.*"}}""")
      ),
      project(
        fields(
          BsonDocument("""{name: {$substr: ["$labels", 5, 99]}}""")
        )
      ),
      group(
        "$name",
        sum("count", 1)
      ),
    ) ++ optionGroupPipeline("facts")
  }

  private def optionGroupPipeline(groupName: String): Seq[Bson] = {
    Seq(
      project(
        fields(
          excludeId(),
          computed(
            "options",
            fields(
              computed("name", "$_id"),
              computed("count", "$count"),
            )
          )
        )
      ),
      sort(orderBy(ascending("name"))),
      group(
        groupName,
        push("options", "$options")
      ),
      project(
        fields(
          excludeId(),
          computed("name", "$_id"),
          include("options")
        )
      )
    )
  }

  def countDocuments(networkType: NetworkType, location: String, locationRoutesType: LocationRoutesType): Long = {
    val filter = buildFilter(networkType, location, locationRoutesType)
    database.routes.countDocuments(filter, log)
  }

  def find(
    networkType: NetworkType,
    location: String,
    locationRoutesType: LocationRoutesType,
    pageSize: Int,
    pageIndex: Int
  ): Seq[LocationRouteInfo] = {

    val pipeline = Seq(
      filter(buildFilter(networkType, location, locationRoutesType)),
      sort(orderBy(ascending("summary.name", "summary.id"))),
      skip(pageSize * pageIndex),
      limit(pageSize),
      project(
        fields(
          excludeId(),
          computed("id", "$summary.id"),
          computed("name", "$summary.name"),
          computed("meters", "$summary.meters"),
          include("lastUpdated"),
          include("lastSurvey"),
          computed("tags", "$summary.tags"),
          computed("broken", "$summary.broken"),
          computed("inaccessible", "$summary.inaccessible")
        )
      )
    )

    log.debugElapsed {
      val docs = database.routes.aggregate[LocationRouteInfoData](pipeline).zipWithIndex.map { case (doc, index) =>
        val rowIndex = pageSize * pageIndex + index
        val symbol = RouteSymbol.from(doc.tags)
        LocationRouteInfo(
          rowIndex = rowIndex,
          id = doc.id,
          name = doc.name,
          meters = doc.meters,
          lastUpdated = doc.lastUpdated,
          lastSurvey = doc.lastSurvey,
          symbol = symbol,
          broken = doc.broken,
          inaccessible = doc.inaccessible
        )
      }
      (s"location routes: ${docs.size}", docs)
    }
  }

  private def buildFilter(networkType: NetworkType, location: String, locationRoutesType: LocationRoutesType): Bson = {
    val filters = Seq(
      Some(equal("labels", Label.active)),
      Some(equal("labels", Label.networkType(networkType))),
      Some(equal("labels", Label.location(location))),
      locationRoutesType match {
        case LocationRoutesType.inaccessible => Some(equal("labels", Label.fact(Fact.RouteInaccessible)))
        case LocationRoutesType.facts => Some(equal("labels", Label.facts))
        case LocationRoutesType.survey => Some(equal("labels", Label.survey))
        case _ => None
      }
    ).flatten
    and(filters: _*)
  }
}
