package kpn.core.tiles.vector.encoder

class VectorTileLayer {

  var features = Seq[VectorTileFeature]()
  private val keyMap = new CodeMap()
  private val valueMap = new CodeMap()

  def key(key: String): Int = keyMap.code(key)

  def value(value: String): Int = valueMap.code(value)

  def keys: Seq[String] = keyMap.keys

  def values: Seq[String] = valueMap.keys

  def addFeature(feature: VectorTileFeature): Unit = {
    features = features :+ feature
  }

}
