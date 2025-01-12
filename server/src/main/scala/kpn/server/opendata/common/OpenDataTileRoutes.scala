package kpn.server.opendata.common

import kpn.server.analyzer.engine.tiles.domain.OldTile

case class OpenDataTileRoutes(
  tile: OldTile,
  routes: Seq[OpenDataRoute]
)
