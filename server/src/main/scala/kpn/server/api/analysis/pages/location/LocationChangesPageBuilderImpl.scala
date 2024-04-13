package kpn.server.api.analysis.pages.location

import kpn.api.common.Language
import kpn.api.common.LocationChangeSetInfo
import kpn.api.common.LocationChangesInfo
import kpn.api.common.changes.filter.ChangesParameters
import kpn.api.common.location.LocationChangesPage
import kpn.api.custom.Country
import kpn.api.custom.LocationKey
import kpn.api.custom.NetworkType
import kpn.server.analyzer.engine.analysis.location.LocationService
import kpn.server.repository.ChangeSetInfoRepository
import kpn.server.repository.LocationRepository
import org.springframework.stereotype.Component

@Component
class LocationChangesPageBuilderImpl(
  locationRepository: LocationRepository,
  locationService: LocationService,
  changeSetInfoRepository: ChangeSetInfoRepository
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
    val changeSetIds = changeSets.map(_.key.changeSetId)
    val changeSetInfos = changeSetInfoRepository.all(changeSetIds)
    val locationChangeSetInfos = changeSets.zipWithIndex.map { case (changeSet, index) =>
      val rowIndex = parameters.pageSize * parameters.pageIndex + index
      val comment = changeSetInfos.find(s => s.id == changeSet.key.changeSetId).flatMap(_.tags("comment"))
      val locationChangeInfos = changeSet.locationChanges.map { change =>
        val locationNames = change.locationNames.dropWhile(_ != locationKey.name).drop(1).map(locationName => locationService.name(language, locationName))
        LocationChangesInfo(
          change.networkType,
          locationNames,
          change.routeChanges,
          change.nodeChanges,
          change.happy,
          change.investigate
        )
      }

      val happy = changeSet.locationChanges.forall(_.happy)
      val investigate = changeSet.locationChanges.exists(_.investigate)

      LocationChangeSetInfo(
        rowIndex,
        changeSet.key,
        comment,
        happy,
        investigate,
        locationChangeInfos
      )
    }

    Some(
      LocationChangesPage(summary, locationChangeSetInfos)
    )
  }
}
