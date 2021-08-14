package kpn.server.repository

import kpn.api.common.common.Reference
import kpn.api.common.route.RouteInfo
import kpn.api.common.route.RouteMapInfo
import kpn.api.common.route.RouteNameInfo
import kpn.server.analyzer.engine.changes.changes.ReferencedElementIds

trait RouteRepository {

  def allRouteIds(): Seq[Long]

  def activeRouteIds(): Seq[Long]

  def activeRouteElementIds(): Seq[ReferencedElementIds]

  def save(route: RouteInfo): Unit

  def bulkSave(routes: Seq[RouteInfo]): Unit

  def delete(routeId: Long): Unit

  def findById(routeId: Long): Option[RouteInfo]

  def mapInfo(routeId: Long): Option[RouteMapInfo]

  def nameInfo(routeId: Long): Option[RouteNameInfo]

  def networkReferences(routeId: Long): Seq[Reference]

  def filterKnown(routeIds: Set[Long]): Set[Long]
}
