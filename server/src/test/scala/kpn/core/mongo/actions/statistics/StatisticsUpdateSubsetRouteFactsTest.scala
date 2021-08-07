package kpn.core.mongo.actions.statistics

import kpn.api.common.SharedTestObjects
import kpn.api.common.statistics.StatisticValue
import kpn.api.common.statistics.StatisticValues
import kpn.api.custom.Country
import kpn.api.custom.Country.de
import kpn.api.custom.Country.nl
import kpn.api.custom.Fact
import kpn.api.custom.Fact.RouteBroken
import kpn.api.custom.Fact.RouteFixmetodo
import kpn.api.custom.Fact.RouteUnaccessible
import kpn.api.custom.NetworkType
import kpn.api.custom.NetworkType.cycling
import kpn.api.custom.NetworkType.hiking
import kpn.core.mongo.Database
import kpn.core.test.TestSupport.withDatabase
import kpn.core.util.UnitTest

class StatisticsUpdateSubsetRouteFactsTest extends UnitTest with SharedTestObjects {

  test("execute") {
    withDatabase { database =>

      buildRouteInfo(database, 11L, nl, hiking, Seq(RouteBroken, RouteUnaccessible))
      buildRouteInfo(database, 12L, nl, hiking, Seq(RouteBroken, RouteFixmetodo))
      buildRouteInfo(database, 13L, nl, cycling, Seq(RouteBroken))
      buildRouteInfo(database, 14L, de, hiking, Seq(RouteBroken))
      buildRouteInfo(database, 15L, de, hiking, Seq(RouteBroken))
      buildRouteInfo(database, 16L, de, cycling, Seq(RouteBroken))
      buildRouteInfo(database, 17L, de, cycling, Seq(RouteBroken), active = false)

      new StatisticsUpdateSubsetRouteFacts(database).execute()
      val counts = new MongoQueryStatistics(database).execute()

      counts should equal(
        Seq(
          StatisticValues(
            "RouteBrokenCount",
            Seq(
              StatisticValue(de, cycling, 1),
              StatisticValue(de, hiking, 2),
              StatisticValue(nl, cycling, 1),
              StatisticValue(nl, hiking, 2)
            )
          ),
          StatisticValues(
            "RouteFixmetodoCount",
            Seq(
              StatisticValue(nl, hiking, 1)
            )
          ),
          StatisticValues(
            "RouteUnaccessibleCount",
            Seq(
              StatisticValue(nl, hiking, 1)
            )
          )
        )
      )
    }
  }

  private def buildRouteInfo(database: Database, routeId: Long, country: Country, networkType: NetworkType, facts: Seq[Fact], active: Boolean = true): Unit = {
    database.routes.save(
      newRouteInfo(
        newRouteSummary(
          routeId,
          Some(country),
          networkType,
        ),
        labels = if (active) {
          Seq("active")
        }
        else {
          Seq.empty
        },
        active,
        facts = facts
      )
    )
  }
}
