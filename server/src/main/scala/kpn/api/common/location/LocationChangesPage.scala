package kpn.api.common.location

import kpn.api.common.LocationChangeSet

case class LocationChangesPage(
  summary: LocationSummary,
  changeSets: Seq[LocationChangeSet]
)
