package kpn.api.common

import kpn.api.custom.NetworkType

case class LocationChangesInfo(
  networkType: NetworkType,
  locationNames: Seq[String],
  routeChanges: ChangeSetElementRefs,
  nodeChanges: ChangeSetElementRefs,
  happy: Boolean,
  investigate: Boolean
)
