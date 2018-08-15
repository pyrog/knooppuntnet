package kpn.core.facade.pages

import kpn.shared.changes.filter.ChangesParameters
import kpn.shared.network.NetworkChangesPage

trait NetworkChangesPageBuilder {
  def build(user: Option[String], parameters: ChangesParameters): Option[NetworkChangesPage]
}
