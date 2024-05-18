package kpn.core.util

import kpn.core.doc.GeoLineString
import kpn.core.doc.GeoPoint
import kpn.core.doc.GeoPolygon

object Geo {
  def point(longitude: Double, latitude: Double): GeoPoint = {
    GeoPoint(coordinates = Seq(longitude, latitude))
  }

  def lineString(coordinates: Seq[Seq[Double]]): GeoLineString = {
    GeoLineString(coordinates = coordinates)
  }

  def polygon(coordinates: Seq[Seq[Seq[Double]]]): GeoPolygon = {
    GeoPolygon(coordinates = coordinates)
  }
}
