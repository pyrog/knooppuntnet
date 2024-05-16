package kpn.core.util

import kpn.core.doc.GeoLineString
import kpn.core.doc.GeoPoint

object Geo {
  def point(longitude: Double, latitude: Double): GeoPoint = {
    GeoPoint(coordinates = Seq(longitude, latitude))
  }

  def lineString(coordinates: Seq[Seq[Double]]): GeoLineString = {
    GeoLineString(coordinates = coordinates)
  }
}
