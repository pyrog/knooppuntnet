package kpn.api.common.monitor

import kpn.api.common.Bounds

case class MonitorRouteMapPage(
  routeId: String,
  relationId: Option[Long],
  routeName: String,
  routeDescription: String,
  groupName: String,
  groupDescription: String,
  bounds: Option[Bounds],
  nextSubRelation: Option[MonitorRouteSubRelation],
  prevSubRelation: Option [MonitorRouteSubRelation],

  osmSegments: Seq[MonitorRouteSegment],
  matchesGeometry: Option[String],
  deviations: Seq[MonitorRouteDeviation],
  reference: Option[MonitorRouteReferenceInfo],

  subRelations: Seq[MonitorRouteSubRelation]
)
