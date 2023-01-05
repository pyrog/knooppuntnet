package kpn.server.repository

import kpn.api.base.ObjectId
import kpn.api.common.changes.details.ChangeKey
import kpn.api.common.monitor.MonitorChangesParameters
import kpn.api.common.monitor.MonitorRouteSegmentInfo
import kpn.server.api.monitor.domain.MonitorGroupRouteCount
import kpn.server.api.monitor.domain.MonitorRoute
import kpn.server.api.monitor.domain.MonitorRouteChange
import kpn.server.api.monitor.domain.MonitorRouteChangeGeometry
import kpn.server.api.monitor.domain.MonitorRouteReference
import kpn.server.api.monitor.domain.MonitorRouteState
import kpn.server.api.monitor.domain.OldMonitorRoute
import kpn.server.api.monitor.domain.OldMonitorRouteReference
import kpn.server.api.monitor.domain.OldMonitorRouteState

trait MonitorRouteRepository {

  def allRouteIds: Seq[Long]

  def saveRoute(route: MonitorRoute): Unit

  def deleteRoute(routeId: ObjectId): Unit

  def deleteRouteReferences(routeId: ObjectId): Unit

  def saveRouteState(routeState: MonitorRouteState): Unit

  def saveRouteReference(routeReference: MonitorRouteReference): Unit

  def saveRouteChange(routeChange: MonitorRouteChange): Unit

  def saveRouteChangeGeometry(routeChangeGeometry: MonitorRouteChangeGeometry): Unit

  def routeById(routeId: ObjectId): Option[MonitorRoute]

  def routeByName(groupId: ObjectId, routeName: String): Option[MonitorRoute]

  def oldRouteByName(groupId: ObjectId, routeName: String): Option[OldMonitorRoute]

  def routeState(routeId: ObjectId, relationId: Long): Option[MonitorRouteState]

  def oldRouteState(routeId: ObjectId): Option[OldMonitorRouteState]

  def routeStates(routeId: ObjectId): Seq[MonitorRouteState]

  def routeStateCount(routeId: ObjectId): Long

  def routeStateSize(routeId: ObjectId): Long

  def routeStateInfos(routeId: ObjectId): Seq[MonitorRouteStateInfo]

  def routeStateSegments(routeId: ObjectId): Seq[MonitorRouteSegmentInfo]

  def routeReferenceRouteWithId(routeId: ObjectId): Option[MonitorRouteReference]

  def routeRelationReference(routeId: ObjectId, relationId: Long): Option[MonitorRouteReference]

  def routeReferences(routeId: ObjectId): Seq[MonitorRouteReference]

  def oldRouteReferenceRouteWithId(routeId: ObjectId): Option[OldMonitorRouteReference]

  def routeReference(referenceId: ObjectId): Option[MonitorRouteReference]

  def oldRouteReference(referenceId: ObjectId): Option[OldMonitorRouteReference]

  def superRouteReferenceSummary(routeId: ObjectId): Option[Long]

  def superRouteStateSummary(routeId: ObjectId): Option[MonitorRouteStateSummary]

  def routeChange(monitorRouteId: String, changeSetId: Long, replicationNumber: Long): Option[MonitorRouteChange]

  def routeChangeGeometry(monitorRouteId: String, changeSetId: Long, replicationNumber: Long): Option[MonitorRouteChangeGeometry]

  def changesCount(parameters: MonitorChangesParameters): Long

  def changes(parameters: MonitorChangesParameters): Seq[MonitorRouteChange]

  def groupRouteCounts(): Seq[MonitorGroupRouteCount]

  def groupChangesCount(groupName: String, parameters: MonitorChangesParameters): Long

  def groupChanges(groupName: String, parameters: MonitorChangesParameters): Seq[MonitorRouteChange]

  def routeChangesCount(monitorRouteId: String, parameters: MonitorChangesParameters): Long

  def routeChanges(monitorRouteId: String, parameters: MonitorChangesParameters): Seq[MonitorRouteChange]

  def routes(): Seq[MonitorRoute]

  def routeChange(changeKey: ChangeKey): Option[MonitorRouteChange]

  def routeChangeGeometry(changeKey: ChangeKey): Option[MonitorRouteChangeGeometry]

  def routeReferenceKey(monitorRouteId: String): Option[String]

  def routeNames(groupId: ObjectId): Seq[String]
}
