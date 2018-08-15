package kpn.core.overpass

import kpn.shared.Subset

case class QuerySubsetNodes(subset: Subset) extends OverpassQuery {

  def name: String = s"nodes-${subset.name}"

  def string: String = {
    s"area['admin_level'='2']['ISO3166-1'='${subset.country.domain.toUpperCase}'];" +
      s"(node['${subset.networkType.nodeTagKey}'](area););" +
      "out meta;"
  }
}
