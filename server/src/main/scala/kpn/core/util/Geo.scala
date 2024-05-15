package kpn.core.util

import kpn.core.doc.GeoPoint

object Geo {
  def point(longitude: Double, latitude: Double): GeoPoint = {
    GeoPoint("Point", Seq(longitude, latitude))
  }
}
