package kpn.server.api.analysis.pages.route

import kpn.api.common.Language
import kpn.api.common.location.LocationCandidateInfo
import kpn.api.common.route.RouteDetailsPage
import kpn.api.common.route.RouteDetailsPageData
import kpn.core.doc.Label
import kpn.server.analyzer.engine.analysis.location.LocationService
import kpn.server.repository.ChangeSetRepository
import kpn.server.repository.RouteRepository
import org.springframework.stereotype.Component

@Component
class RouteDetailsPageBuilderImpl(
  routeRepository: RouteRepository,
  changeSetRepository: ChangeSetRepository,
  locationService: LocationService
) extends RouteDetailsPageBuilder {
  override def build(language: Language, routeId: Long): Option[RouteDetailsPage] = {
    if (routeId == 1) {
      Some(RouteDetailsPageExample.page)
    }
    else {
      doBuildDetailsPage(language, routeId)
    }
  }

  private def doBuildDetailsPage(language: Language, routeId: Long): Option[RouteDetailsPage] = {
    routeRepository.findById(routeId).map { route =>
      val changeCount = changeSetRepository.routeChangesCount(routeId)
      val networkReferences = routeRepository.networkReferences(routeId)
      val locationCandidateInfos = {
        route.analysis.locationAnalysis.candidates.map { candidate =>
          val locationNames = candidate.location.names
          val locationInfos = locationService.toInfos(language, locationNames, locationNames)
          LocationCandidateInfo(locationInfos, candidate.percentage)
        }
      }
      val data = RouteDetailsPageData(
        route._id,
        route.labels.contains(Label.active),
        route.summary,
        route.proposed,
        route.version,
        route.changeSetId,
        route.lastUpdated,
        route.lastSurvey,
        route.tags,
        route.facts,
        locationCandidateInfos,
        route.analysis,
        route.tiles,
        route.nodeRefs
      )
      RouteDetailsPage(data, networkReferences, changeCount)
    }
  }
}
