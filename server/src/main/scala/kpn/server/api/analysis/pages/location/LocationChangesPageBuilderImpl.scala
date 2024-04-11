package kpn.server.api.analysis.pages.location

import kpn.api.common.Language
import kpn.api.common.changes.filter.ChangesParameters
import kpn.api.common.location.LocationChangesPage
import kpn.api.common.location.LocationChangesParameters
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

  override def build(language: Language, locationKey: LocationKey, parameters: LocationChangesParameters): Option[LocationChangesPage] = {
    if (locationKey == LocationKey(NetworkType.cycling, Country.nl, "example")) {
      Some(LocationChangesPageExample.page)
    }
    else {
      buildPage(language, locationKey, parameters)
    }
  }

  private def buildPage(language: Language, locationKeyParam: LocationKey, parameters: LocationChangesParameters): Option[LocationChangesPage] = {
    val locationKey = locationService.toIdBased(language, locationKeyParam)
    val summary = locationRepository.summary(locationKey)
    val changesParameters = ChangesParameters(pageSize = parameters.pageSize, pageIndex = parameters.pageIndex)
    val changeSets = locationRepository.changes(locationKey, changesParameters)
    Some(
      LocationChangesPage(summary, changeSets)
    )
  }
}
