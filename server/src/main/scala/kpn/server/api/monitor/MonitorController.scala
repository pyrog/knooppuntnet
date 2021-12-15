package kpn.server.api.monitor

import kpn.api.common.monitor.MonitorChangesPage
import kpn.api.common.monitor.MonitorChangesParameters
import kpn.api.common.monitor.MonitorGroupChangesPage
import kpn.api.common.monitor.MonitorGroupPage
import kpn.api.common.monitor.MonitorGroupsPage
import kpn.api.common.monitor.MonitorRouteChangePage
import kpn.api.common.monitor.MonitorRouteChangesPage
import kpn.api.common.monitor.MonitorRouteDetailsPage
import kpn.api.common.monitor.MonitorRouteMapPage
import kpn.api.custom.ApiResponse
import kpn.server.api.CurrentUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Array("/api/monitor"))
class MonitorController(
  facade: MonitorFacade
) {

  @GetMapping(value = Array("groups"))
  def groups(): ApiResponse[MonitorGroupsPage] = {
    facade.groups(CurrentUser.name)
  }

  @GetMapping(value = Array("groups/{groupName}"))
  def group(
    @PathVariable groupName: String
  ): ApiResponse[MonitorGroupPage] = {
    facade.group(CurrentUser.name, groupName)
  }

  @GetMapping(value = Array("routes/{monitorRouteId}"))
  def route(
    @PathVariable monitorRouteId: String
  ): ApiResponse[MonitorRouteDetailsPage] = {
    facade.route(CurrentUser.name, monitorRouteId)
  }

  @GetMapping(value = Array("routes/{monitorRouteId}/map"))
  def routeMap(
    @PathVariable monitorRouteId: String
  ): ApiResponse[MonitorRouteMapPage] = {
    facade.routeMap(CurrentUser.name, monitorRouteId)
  }

  @PostMapping(value = Array("changes"))
  def changes(
    @RequestBody parameters: MonitorChangesParameters
  ): ApiResponse[MonitorChangesPage] = {
    facade.changes(CurrentUser.name, parameters)
  }

  @PostMapping(value = Array("groups/{groupName}/changes"))
  def groupChanges(
    @PathVariable groupName: String,
    @RequestBody parameters: MonitorChangesParameters
  ): ApiResponse[MonitorGroupChangesPage] = {
    facade.groupChanges(CurrentUser.name, groupName, parameters)
  }

  @PostMapping(value = Array("routes/{monitorRouteId}/changes"))
  def routeChanges(
    @PathVariable monitorRouteId: String,
    @RequestBody parameters: MonitorChangesParameters
  ): ApiResponse[MonitorRouteChangesPage] = {
    facade.routeChanges(CurrentUser.name, monitorRouteId, parameters)
  }

  @GetMapping(value = Array("routes/{monitorRouteId}/changes/{changeSetId}/{replicationNumber}"))
  def routeChange(
    @PathVariable monitorRouteId: Long,
    @PathVariable changeSetId: Long,
    @PathVariable replicationNumber: Long
  ): ApiResponse[MonitorRouteChangePage] = {
    facade.routeChange(CurrentUser.name, monitorRouteId, changeSetId, replicationNumber)
  }
}
