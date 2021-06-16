package kpn.core.mongo

import kpn.api.common.NodeInfo

case class NodeDoc2(
  _id: String,
  node: NodeInfo,
  routeRefs: Seq[NodeRouteRef],
  test1: String,
  test2: String,
  test3: String
)