package kpn.server.api.monitor.domain

import kpn.api.common.Bounds

case class MonitorRouteSuperSegmentElement(
  relationId: Long,
  segmentId: Long,
  meters: Long,
  bounds: Bounds,
  reversed: Boolean
)
