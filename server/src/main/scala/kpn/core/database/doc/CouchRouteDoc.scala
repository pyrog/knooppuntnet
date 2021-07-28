package kpn.core.database.doc

import kpn.api.common.route.RouteInfo

case class CouchRouteDoc(_id: String, route: RouteInfo, _rev: Option[String] = None) extends CouchDoc {
  def withRev(_newRev: Option[String]): CouchDoc = this.copy(_rev = _newRev)
}