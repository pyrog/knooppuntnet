package kpn.core.mongo.actions.statistics

import kpn.api.common.SharedTestObjects
import kpn.api.custom.Country
import kpn.api.custom.Country.de
import kpn.api.custom.Country.nl
import kpn.api.custom.NetworkType
import kpn.api.custom.NetworkType.cycling
import kpn.api.custom.NetworkType.hiking
import kpn.core.mongo.Database
import kpn.core.test.TestSupport.withDatabase
import kpn.core.util.UnitTest

class StatisticsUpdateSubsetRouteCountTest extends UnitTest with SharedTestObjects {

  test("execute") {
    withDatabase { database =>

      buildRouteInfo(database, 11L, nl, hiking)
      buildRouteInfo(database, 12L, nl, hiking)
      buildRouteInfo(database, 13L, nl, cycling)
      buildRouteInfo(database, 14L, de, hiking)
      buildRouteInfo(database, 15L, de, hiking)
      buildRouteInfo(database, 16L, de, cycling)
      buildRouteInfo(database, 17L, de, cycling, active = false)

      new StatisticsUpdateSubsetRouteCount(database).execute()

      val counts = new MongoQueryStatistics(database).execute()

      counts.size should equal(4)
      counts should contain(StatisticValue(nl, hiking, "RouteCount", 2))
      counts should contain(StatisticValue(nl, cycling, "RouteCount", 1))
      counts should contain(StatisticValue(de, hiking, "RouteCount", 2))
      counts should contain(StatisticValue(de, cycling, "RouteCount", 1))
    }
  }

  private def buildRouteInfo(database: Database, routeId: Long, country: Country, networkType: NetworkType, active: Boolean = true): Unit = {
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
        active
      )
    )
  }
}