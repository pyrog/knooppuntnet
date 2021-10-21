package kpn.database.actions.networks

import kpn.database.actions.networks.MongoQueryNetworkChangeCounts.log
import kpn.database.actions.networks.MongoQueryNetworkChangeCounts.pipelineDaysString
import kpn.database.actions.networks.MongoQueryNetworkChangeCounts.pipelineMonthsString
import kpn.database.actions.networks.MongoQueryNetworkChangeCounts.pipelineYearsString
import kpn.database.actions.statistics.ChangeSetCounts
import kpn.database.base.Database
import kpn.database.base.MongoQuery
import kpn.database.util.Mongo
import kpn.core.util.Log
import org.mongodb.scala.model.Aggregates.facet
import org.mongodb.scala.model.Aggregates.filter
import org.mongodb.scala.model.Facet
import org.mongodb.scala.model.Filters.equal

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MongoQueryNetworkChangeCounts extends MongoQuery {
  private val log = Log(classOf[MongoQueryNetworkChangeCounts])
  private val pipelineYearsString = readPipelineString("years")
  private val pipelineMonthsString = readPipelineString("months")
  private val pipelineDaysString = readPipelineString("days")
}

class MongoQueryNetworkChangeCounts(database: Database) extends MongoQuery {

  def execute(networkId: Long, year: Int, monthOption: Option[Int]): ChangeSetCounts = {

    val pipelineYears = toPipeline(pipelineYearsString.replace("@networkId", s"$networkId"))

    val pipelineMonths = toPipeline(
      pipelineMonthsString.
        replace("@networkId", s"$networkId").
        replace("@year", s"$year")
    )

    val pipeline = monthOption match {
      case None =>
        Seq(
          filter(equal("networkId", networkId)),
          facet(
            Facet("years", pipelineYears: _*),
            Facet("months", pipelineMonths: _*),
          )
        )

      case Some(month) =>
        val pipelineDays = toPipeline(
          pipelineDaysString.
            replace("@networkId", s"$networkId").
            replace("@year", s"$year").
            replace("@month", s"$month")
        )
        Seq(
          filter(equal("networkId", networkId)),
          facet(
            Facet("years", pipelineYears: _*),
            Facet("months", pipelineMonths: _*),
            Facet("days", pipelineDays: _*),
          )
        )
    }

    if (log.isTraceEnabled) {
      log.trace(Mongo.pipelineString(pipeline))
    }

    log.debugElapsed {
      val collection = database.getCollection("network-changes")
      val future = collection.aggregate[ChangeSetCounts](pipeline).first().toFuture()
      val counts = Await.result(future, Duration(60, TimeUnit.SECONDS))
      val result = s"year: $year, month: ${monthOption.getOrElse('-')}, results: years: ${counts.years.size}, months: ${counts.months.size}, days: ${counts.days.size}"
      (result, counts)
    }
  }
}