package kpn.core.doc

case class GeoLineString(
  `type`: String = "LineString",
  coordinates: Seq[Seq[Double]] = Seq.empty
)
