package kpn.server.repository

import kpn.api.common.poi.LocationPoiInfo
import kpn.api.common.poi.LocationPoiLayerCount
import kpn.api.common.poi.LocationPoiParameters
import kpn.api.common.poi.Poi
import kpn.core.poi.PoiInfo
import kpn.server.analyzer.engine.poi.PoiRef

trait PoiRepository {

  def save(poi: Poi): Unit

  def nodeIds(): Seq[Long]

  def wayIds(): Seq[Long]

  def relationIds(): Seq[Long]

  def get(poiRef: PoiRef): Option[Poi]

  def delete(poiRef: PoiRef): Unit

  def allTiles(): Seq[String]

  def tilePoiInfos(tileName: String): Seq[PoiInfo]

  def locationPois(locationName: String, parameters: LocationPoiParameters, layers: Seq[String]): Seq[LocationPoiInfo]

  def locationPoiCount(locationName: String, layers: Seq[String]): Long

  def locationPoiLayerCounts(locationName: String): Seq[LocationPoiLayerCount]
}
