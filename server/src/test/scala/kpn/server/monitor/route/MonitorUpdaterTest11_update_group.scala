package kpn.server.monitor.route

import kpn.api.common.SharedTestObjects
import kpn.api.common.monitor.MonitorRouteUpdate
import kpn.api.common.monitor.MonitorRouteUpdateStatus
import kpn.api.common.monitor.MonitorRouteUpdateStep
import kpn.api.custom.Timestamp
import kpn.core.common.Time
import kpn.core.test.TestSupport.withDatabase
import kpn.core.util.UnitTest
import org.scalatest.BeforeAndAfterEach

class MonitorUpdaterTest11_update_group extends UnitTest with BeforeAndAfterEach with SharedTestObjects {

  override def afterEach(): Unit = {
    Time.clear()
  }

  test("route update - change group") {

    withDatabase() { database =>

      val configuration = MonitorUpdaterTestSupport.configuration(database)

      val group1 = newMonitorGroup("group1")
      val group2 = newMonitorGroup("group2")
      val route = newMonitorRoute(
        group1._id,
        name = "route",
        relationId = Some(1),
        referenceType = "osm",
        referenceTimestamp = Some(Timestamp(2022, 8, 11)),
        referenceFilename = None,
      )
      val reference = newMonitorRouteReference(
        routeId = route._id,
        relationId = Some(1),
        referenceType = "osm",
        referenceTimestamp = Timestamp(2022, 8, 11),
      )

      configuration.monitorGroupRepository.saveGroup(group1)
      configuration.monitorGroupRepository.saveGroup(group2)
      configuration.monitorRouteRepository.saveRoute(route)
      configuration.monitorRouteRepository.saveRouteReference(reference)

      val update = MonitorRouteUpdate(
        action = "update",
        groupName = group1.name,
        newGroupName = Some(group2.name), // <-- changed
        routeName = "route",
        referenceType = "osm",
        description = Some(""),
        relationId = Some(1),
        referenceTimestamp = Some(Timestamp(2022, 8, 11)),
      )

      Time.set(Timestamp(2023, 1, 1))
      val reporter = new MonitorUpdateReporterMock()
      configuration.monitorUpdater.update("user", update, reporter)

      reporter.statusses.shouldMatchTo(
        Seq(
          MonitorRouteUpdateStatus(
            steps = Seq(
              MonitorRouteUpdateStep("definition", "busy")
            )
          )
        )
      )

      val updatedRoute = configuration.monitorRouteRepository.routeByName(group2._id, "route").get
      val updatedReference = configuration.monitorRouteRepository.routeRelationReference(route._id, 1).get
      // TODO val updatedState = config.monitorRouteRepository.routeState(route._id, 1).get

      updatedRoute.groupId should equal(group2._id)
      updatedReference should equal(reference)
      // TODO assert state not updated
    }
  }
}
