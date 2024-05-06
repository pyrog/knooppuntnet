package kpn.api.common.location

import kpn.api.common.LocationChangeSetInfo

case class LocationChangesPage(
  summary: LocationSummary,
  changeSets: Seq[LocationChangeSetInfo],
  changesCount: Long
)
