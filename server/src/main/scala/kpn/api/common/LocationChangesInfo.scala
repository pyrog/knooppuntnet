package kpn.api.common

import kpn.api.custom.NetworkType

case class LocationChangesInfo(
  networkType: NetworkType,
  locationInfos: Seq[LocationInfo],
  routeChanges: ChangeSetElementRefs,
  nodeChanges: ChangeSetElementRefs,
  happy: Boolean,
  investigate: Boolean
)
