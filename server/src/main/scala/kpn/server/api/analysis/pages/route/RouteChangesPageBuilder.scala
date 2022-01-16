package kpn.server.api.analysis.pages.route

import kpn.api.common.changes.filter.ChangesParameters
import kpn.api.common.route.RouteChangesPage

trait RouteChangesPageBuilder {

  def build(user: Option[String], routeId: Long, parameters: ChangesParameters): Option[RouteChangesPage]

}