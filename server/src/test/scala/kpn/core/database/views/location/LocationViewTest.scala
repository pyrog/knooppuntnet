package kpn.core.database.views.location

import kpn.api.common.NodeName
import kpn.api.common.RouteLocationAnalysis
import kpn.api.common.SharedTestObjects
import kpn.api.common.common.Ref
import kpn.api.common.location.Location
import kpn.api.common.location.LocationCandidate
import kpn.api.custom.NetworkScope
import kpn.api.custom.NetworkType
import kpn.core.test.TestSupport.withCouchDatabase
import kpn.core.util.UnitTest
import kpn.server.repository.NodeRepositoryImpl
import kpn.server.repository.RouteRepositoryImpl

class LocationViewTest extends UnitTest with SharedTestObjects {

  test("node location") {

    pending

    withCouchDatabase { database =>
      val repo = new NodeRepositoryImpl(null)
      repo.save(
        newNodeDoc(
          id = 1001,
          names = Seq(
            NodeName(
              NetworkType.cycling,
              NetworkScope.regional,
              "01",
              None,
              proposed = false
            ),
          ),
          locations = Seq(
            "country",
            "province",
            "municipality"
          )
        )
      )

      LocationView.query(database, "node", NetworkType.cycling, "country") should matchTo(
        Seq(
          Ref(1001, "01")
        )
      )

      LocationView.query(database, "node", NetworkType.cycling, "province") should matchTo(
        Seq(
          Ref(1001, "01")
        )
      )

      LocationView.query(database, "node", NetworkType.cycling, "municipality") should matchTo(
        Seq(
          Ref(1001, "01")
        )
      )
    }
  }

  test("route location") {
    withCouchDatabase { database =>

      val route1 = newRoute(
        id = 11,
        name = "01-02",
        analysis = newRouteInfoAnalysis(
          locationAnalysis = RouteLocationAnalysis(
            location = Some(Location(Seq("country", "province2", "municipality3"))),
            candidates = Seq(
              LocationCandidate(Location(Seq("country", "province1", "municipality1")), 20),
              LocationCandidate(Location(Seq("country", "province2", "municipality2")), 30),
              LocationCandidate(Location(Seq("country", "province2", "municipality3")), 50),
            ),
            locationNames = Seq(
              "country",
              "province1",
              "province2",
              "municipality1",
              "municipality2",
              "municipality3"
            )
          )
        )
      )

      val route2 = newRoute(
        id = 12,
        name = "02-03",
        analysis = newRouteInfoAnalysis(
          locationAnalysis = RouteLocationAnalysis(
            location = Some(Location(Seq("country", "province1", "municipality1"))),
            candidates = Seq(
              LocationCandidate(Location(Seq("country", "province1", "municipality1")), 100)
            ),
            locationNames = Seq("country", "province1", "municipality1")
          )
        )
      )

      val routeRepository = new RouteRepositoryImpl(null)

      routeRepository.save(route1)
      routeRepository.save(route2)

      def query(locationName: String): Seq[Ref] = {
        LocationView.query(database, "route", NetworkType.hiking, locationName)
      }

      query("country") should matchTo(
        Seq(
          Ref(11, "01-02"),
          Ref(12, "02-03")
        )
      )

      query("province1") should matchTo(
        Seq(
          Ref(11, "01-02"),
          Ref(12, "02-03")
        )
      )

      query("province2") should matchTo(
        Seq(
          Ref(11, "01-02")
        )
      )

      query("municipality1") should matchTo(
        Seq(
          Ref(11, "01-02"),
          Ref(12, "02-03")
        )
      )

      query("municipality2") should matchTo(
        Seq(
          Ref(11, "01-02")
        )
      )

      query("municipality3") should matchTo(
        Seq(
          Ref(11, "01-02")
        )
      )
    }
  }
}
