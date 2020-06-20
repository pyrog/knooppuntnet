package kpn.server.api.planner

import kpn.api.common.PoiPage
import kpn.api.common.node.MapNodeDetail
import kpn.api.common.planner.RouteLeg
import kpn.api.common.route.MapRouteDetail
import kpn.api.common.tiles.ClientPoiConfiguration
import kpn.api.custom.ApiResponse
import kpn.api.custom.NetworkType
import kpn.server.analyzer.engine.poi.PoiRef
import kpn.server.api.planner.leg.LegBuildParams

trait PlannerFacade {

  def mapNodeDetail(user: Option[String], networkType: NetworkType, nodeId: Long): ApiResponse[MapNodeDetail]

  def mapRouteDetail(user: Option[String], routeId: Long): ApiResponse[MapRouteDetail]

  def poiConfiguration(user: Option[String]): ApiResponse[ClientPoiConfiguration]

  def poi(user: Option[String], poiRef: PoiRef): ApiResponse[PoiPage]

  def leg(user: Option[String], params: LegBuildParams): ApiResponse[RouteLeg]

}
