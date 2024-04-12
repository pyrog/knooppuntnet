package kpn.server.api.analysis.pages.location

import kpn.api.common.Language
import kpn.api.common.changes.filter.ChangesParameters
import kpn.api.common.location.LocationChangesPage
import kpn.api.custom.Country
import kpn.api.custom.LocationKey
import kpn.api.custom.NetworkType
import kpn.server.analyzer.engine.analysis.location.LocationService
import kpn.server.repository.LocationRepository
import org.springframework.stereotype.Component

@Component
class LocationChangesPageBuilderImpl(
  locationRepository: LocationRepository,
  locationService: LocationService
) extends LocationChangesPageBuilder {

  override def build(language: Language, locationKey: LocationKey, parameters: ChangesParameters): Option[LocationChangesPage] = {
    if (locationKey == LocationKey(NetworkType.cycling, Country.nl, "example")) {
      Some(LocationChangesPageExample.page)
    }
    else {
      buildPage(language, locationKey, parameters)
    }
  }

  private def buildPage(language: Language, locationKeyParam: LocationKey, parameters: ChangesParameters): Option[LocationChangesPage] = {
    val locationKey = locationService.toIdBased(language, locationKeyParam)
    val summary = locationRepository.summary(locationKey)
    val changeSets = locationRepository.changes(locationKey, parameters)
    Some(
      LocationChangesPage(summary, changeSets)
    )
  }
}
