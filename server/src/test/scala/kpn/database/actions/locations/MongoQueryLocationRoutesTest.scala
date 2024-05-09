package kpn.database.actions.locations

import kpn.api.common.SharedTestObjects
import kpn.api.common.changes.filter.ServerFilterGroup
import kpn.api.common.changes.filter.ServerFilterOption
import kpn.api.common.location.LocationRouteInfo
import kpn.api.custom.Day
import kpn.api.custom.LocationRoutesType
import kpn.api.custom.NetworkType
import kpn.api.custom.Tags
import kpn.core.test.TestSupport.withDatabase
import kpn.core.util.UnitTest
import kpn.database.base.Database
import kpn.database.util.Mongo

class MongoQueryLocationRoutesTest extends UnitTest with SharedTestObjects {

  test("count documents") {

    withDatabase { database =>
      val setup = new MongoQueryLocationRoutesTestSetup(database)
      val query = new MongoQueryLocationRoutes(database, setup.surveyDateInfo)

      route(database, 11L, "active", "network-type-hiking", "location-essen")
      route(database, 12L, "active", "network-type-hiking", "location-essen", "facts", "fact-RouteInaccessible")
      route(database, 13L, "active", "network-type-hiking", "location-essen", "survey", "facts")
      route(database, 14L, "active", "network-type-hiking", "location-essen", "survey")
      route(database, 15L, "active", "network-type-hiking", "location-essen", "survey")
      route(database, 16L, "active", "network-type-hiking", "location-essen")

      // non-active is not counted
      route(database, 17L, "network-type-hiking", "location-essen")
      // non-hiking is not counted
      route(database, 18L, "active", "network-type-cycling", "location-essen")
      // location other that "essen" not counted
      route(database, 19L, "active", "network-type-cycling", "location-kalmthout")

      countDocuments(query, LocationRoutesType.all) should equal(6)
      countDocuments(query, LocationRoutesType.facts) should equal(2)
      countDocuments(query, LocationRoutesType.survey) should equal(3)
      countDocuments(query, LocationRoutesType.inaccessible) should equal(1)
    }
  }

  test("find") {

    withDatabase { database =>
      val setup = new MongoQueryLocationRoutesTestSetup(database)

      database.routes.save(
        newRouteDoc(
          newRouteSummary(
            10L,
            name = "bbb",
            meters = 100,
            tags = Tags.from("osmc:symbol" -> "red:white:red_lower")
          ),
          labels = Seq(
            "active",
            "network-type-hiking",
            "location-essen",
            "survey"
          ),
          lastSurvey = Some(Day(2020, 8))
        )
      )

      database.routes.save(
        newRouteDoc(
          newRouteSummary(
            20L,
            name = "aaa",
            meters = 200,
            broken = true
          ),
          labels = Seq(
            "active",
            "network-type-hiking",
            "location-essen",
            "facts",
          )
        )
      )

      database.routes.save(
        newRouteDoc(
          newRouteSummary(
            30L,
            name = "ccc",
            meters = 300,
            broken = true,
            inaccessible = true
          ),
          labels = Seq(
            "active",
            "network-type-hiking",
            "location-essen",
            "facts",
            "fact-RouteInaccessible",
          )
        )
      )

      val query = new MongoQueryLocationRoutes(database, setup.surveyDateInfo)
      val locationRouteInfos = query.find(NetworkType.hiking, "essen", LocationRoutesType.all, 10, 0)

      locationRouteInfos.shouldMatchTo(
        Seq(
          LocationRouteInfo(
            0L,
            20L,
            "aaa",
            200,
            defaultTimestamp,
            None,
            None,
            broken = true,
            inaccessible = false
          ),
          LocationRouteInfo(
            1L,
            10L,
            "bbb",
            100,
            defaultTimestamp,
            Some(Day(2020, 8)),
            Some("red:white:red_lower"),
            broken = false,
            inaccessible = false
          ),
          LocationRouteInfo(
            2L,
            30L,
            "ccc",
            300,
            defaultTimestamp,
            None,
            None,
            broken = true,
            inaccessible = true
          )
        )
      )
    }
  }

  test("filter option group 'facts'") {

    withDatabase { database =>
      val setup = new MongoQueryLocationRoutesTestSetup(database)

      database.routes.save(
        newRouteDoc(
          newRouteSummary(10L),
          labels = Seq(
            "active",
            "network-type-hiking",
            "location-be",
            "fact-RouteIncomplete",
            "fact-RouteNotForward"
          ),
        )
      )

      database.routes.save(
        newRouteDoc(
          newRouteSummary(20L),
          labels = Seq(
            "active",
            "network-type-hiking",
            "location-be",
            "fact-RouteIncomplete",
          ),
        )
      )

      database.routes.save(
        newRouteDoc(
          newRouteSummary(30L),
          labels = Seq(
            "active",
            "network-type-hiking",
            "location-be",
          ),
        )
      )

      val pipeline = new MongoQueryLocationRoutes(database, setup.surveyDateInfo).optionGroupFactsPipeline()

      val groups = database.routes.aggregate[ServerFilterGroup](pipeline)

      groups.filter(_.name == "facts").shouldMatchTo(
        Seq(
          ServerFilterGroup(
            "facts",
            Seq(
              ServerFilterOption("RouteIncomplete", 2),
              ServerFilterOption("RouteNotForward", 1)
            )
          )
        )
      )
    }
  }

  test("filter option group 'survey'") {

    withDatabase { database =>

      val setup = new MongoQueryLocationRoutesTestSetup(database)

      // last month
      setup.buildSurveyRoute(10, Some(Day(2023, 12, 15)))

      // last half year
      setup.buildSurveyRoute(20, Some(Day(2023, 11)))
      setup.buildSurveyRoute(30, Some(Day(2023, 10)))

      // last year
      setup.buildSurveyRoute(40, Some(Day(2023, 5)))
      setup.buildSurveyRoute(50, Some(Day(2023, 4)))
      setup.buildSurveyRoute(60, Some(Day(2023, 3)))

      // last two years
      setup.buildSurveyRoute(70, Some(Day(2022, 5)))
      setup.buildSurveyRoute(80, Some(Day(2022, 4)))
      setup.buildSurveyRoute(90, Some(Day(2022, 3)))
      setup.buildSurveyRoute(100, Some(Day(2022, 2)))

      // older
      setup.buildSurveyRoute(110, Some(Day(2021, 6)))
      setup.buildSurveyRoute(120, Some(Day(2021, 5)))
      setup.buildSurveyRoute(130, Some(Day(2021, 4)))
      setup.buildSurveyRoute(140, Some(Day(2021, 3)))
      setup.buildSurveyRoute(150, Some(Day(2021, 2)))

      // unknown
      setup.buildSurveyRoute(160, None)

      val pipeline = new MongoQueryLocationRoutes(database, setup.surveyDateInfo).exploreSurvey()

      println(Mongo.pipelineString(pipeline))

      val groups = database.routes.aggregate[ServerFilterGroup](pipeline)

      groups.filter(_.name == "survey").shouldMatchTo(
        Seq(
          ServerFilterGroup(
            "survey",
            Seq(
              ServerFilterOption("unknown", 1),
              ServerFilterOption("last-month", 1),
              ServerFilterOption("last-half-year", 2),
              ServerFilterOption("last-year", 3),
              ServerFilterOption("last-two-years", 4),
              ServerFilterOption("older", 5),
            )
          )
        )
      )
    }
  }

  test("filter option group 'proposed'") {

    withDatabase { database =>

      val setup = new MongoQueryLocationRoutesTestSetup(database)

      setup.buildPropsedRoute(10, proposed = false)
      setup.buildPropsedRoute(20, proposed = false)
      setup.buildPropsedRoute(30, proposed = true)

      val pipeline = new MongoQueryLocationRoutes(database, setup.surveyDateInfo).optionGroupProposedPipeline()
      val groups = database.routes.aggregate[ServerFilterGroup](pipeline)

      groups.filter(_.name == "proposed").shouldMatchTo(
        Seq(
          ServerFilterGroup(
            "proposed",
            Seq(
              ServerFilterOption("no", 2),
              ServerFilterOption("yes", 1),
            )
          )
        )
      )
    }
  }

  private def route(database: Database, id: Long, labels: String*): Unit = {
    routeWithTags(database, id, Tags.empty, labels: _*)
  }

  private def routeWithTags(database: Database, id: Long, tags: Tags, labels: String*): Unit = {
    database.routes.save(
      newRouteDoc(
        newRouteSummary(id),
        labels = labels,
        tags = tags
      )
    )
  }

  private def countDocuments(query: MongoQueryLocationRoutes, locationRoutesType: LocationRoutesType): Long = {
    query.countDocuments(NetworkType.hiking, "essen", locationRoutesType)
  }
}
