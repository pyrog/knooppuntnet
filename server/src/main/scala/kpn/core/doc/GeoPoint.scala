package kpn.core.doc

case class GeoPoint(
  `type`: String = "Point",
  coordinates: Seq[Double] = Seq.empty
)
