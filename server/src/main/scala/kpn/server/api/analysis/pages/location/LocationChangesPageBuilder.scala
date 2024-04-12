package kpn.server.api.analysis.pages.location

import kpn.api.common.Language
import kpn.api.common.changes.filter.ChangesParameters
import kpn.api.common.location.LocationChangesPage
import kpn.api.custom.LocationKey

trait LocationChangesPageBuilder {
  def build(language: Language, locationKey: LocationKey, parameters: ChangesParameters): Option[LocationChangesPage]
}
