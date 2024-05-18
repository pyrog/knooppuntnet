package kpn.core.doc

case class GeoPolygon(
  `type`: String = "Polygon",
  coordinates: Seq[Seq[Seq[Double]]] = Seq.empty
)
