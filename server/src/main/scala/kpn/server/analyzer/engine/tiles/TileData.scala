package kpn.server.analyzer.engine.tiles

import kpn.api.custom.NetworkType
import kpn.server.analyzer.engine.tiles.domain.OldTile
import kpn.server.analyzer.engine.tiles.domain.TileDataNode
import kpn.server.analyzer.engine.tiles.domain.TileDataRoute

case class TileData(
  networkType: NetworkType,
  tile: OldTile,
  nodes: Seq[TileDataNode],
  routes: Seq[TileDataRoute]
) {
  def isEmpty: Boolean = {
    nodes.isEmpty && routes.isEmpty
  }
}
