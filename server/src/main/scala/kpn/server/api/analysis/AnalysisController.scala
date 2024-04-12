package kpn.server.api.analysis

import kpn.api.common.AnalysisStrategy
import kpn.api.common.ChangesPage
import kpn.api.common.EN
import kpn.api.common.LOCATION
import kpn.api.common.Language
import kpn.api.common.Languages
import kpn.api.common.ReplicationId
import kpn.api.common.SurveyDateInfo
import kpn.api.common.changes.ChangeSetPage
import kpn.api.common.changes.filter.ChangesParameters
import kpn.api.common.location.LocationChangesPage
import kpn.api.common.location.LocationEditPage
import kpn.api.common.location.LocationFactsPage
import kpn.api.common.location.LocationMapPage
import kpn.api.common.location.LocationNodesPage
import kpn.api.common.location.LocationNodesParameters
import kpn.api.common.location.LocationRoutesPage
import kpn.api.common.location.LocationRoutesParameters
import kpn.api.common.location.LocationsPage
import kpn.api.common.network.NetworkChangesPage
import kpn.api.common.network.NetworkDetailsPage
import kpn.api.common.network.NetworkFactsPage
import kpn.api.common.network.NetworkMapPage
import kpn.api.common.network.NetworkNodesPage
import kpn.api.common.network.NetworkRoutesPage
import kpn.api.common.node.NodeChangesPage
import kpn.api.common.node.NodeDetailsPage
import kpn.api.common.node.NodeMapPage
import kpn.api.common.route.RouteChangesPage
import kpn.api.common.route.RouteDetailsPage
import kpn.api.common.route.RouteMapPage
import kpn.api.common.statistics.StatisticValues
import kpn.api.common.subset.SubsetChangesPage
import kpn.api.common.subset.SubsetFactDetailsPage
import kpn.api.common.subset.SubsetFactRefs
import kpn.api.common.subset.SubsetFactsPage
import kpn.api.common.subset.SubsetMapPage
import kpn.api.common.subset.SubsetNetworksPage
import kpn.api.common.subset.SubsetOrphanNodesPage
import kpn.api.common.subset.SubsetOrphanRoutesPage
import kpn.api.custom.ApiResponse
import kpn.api.custom.Country
import kpn.api.custom.Fact
import kpn.api.custom.LocationKey
import kpn.api.custom.NetworkType
import kpn.api.custom.Subset
import kpn.server.api.analysis.pages.SurveyDateInfoBuilder
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class AnalysisController(analysisFacade: AnalysisFacade) {

  @GetMapping(value = Array("/api/overview"))
  def overview(@RequestParam language: String): ApiResponse[Seq[StatisticValues]] = {
    analysisFacade.overview(toLanguage(language))
  }

  @GetMapping(value = Array("/api/{country:be|de|fr|nl|at|es|dk}/{networkType:cycling|hiking|horse-riding|motorboat|canoe|inline-skating}/networks"))
  def subsetNetworks(
    @PathVariable country: Country,
    @PathVariable networkType: NetworkType
  ): ApiResponse[SubsetNetworksPage] = {
    Subset.of(country, networkType) match {
      case Some(subset) => analysisFacade.subsetNetworks(subset)
      case None => notFound()
    }
  }

  @GetMapping(value = Array("/api/{country:be|de|fr|nl|at|es|dk}/{networkType:cycling|hiking|horse-riding|motorboat|canoe|inline-skating}/facts"))
  def subsetFacts(
    @PathVariable country: Country,
    @PathVariable networkType: NetworkType
  ): ApiResponse[SubsetFactsPage] = {
    Subset.of(country, networkType) match {
      case Some(subset) => analysisFacade.subsetFacts(subset)
      case None => notFound()
    }
  }

  @GetMapping(value = Array("/api/{country:be|de|fr|nl|at|es|dk}/{networkType:cycling|hiking|horse-riding|motorboat|canoe|inline-skating}/{fact}/refs"))
  def subsetFactRefs(
    @PathVariable country: Country,
    @PathVariable networkType: NetworkType,
    @PathVariable fact: String
  ): ApiResponse[SubsetFactRefs] = {
    Subset.of(country, networkType) match {
      case Some(subset) =>
        Fact.withName(fact) match {
          case Some(f) => analysisFacade.subsetFactRefs(subset, f)
          case None => notFound()
        }
      case None => notFound()
    }
  }

  @GetMapping(value = Array("/api/{country:be|de|fr|nl|at|es|dk}/{networkType:cycling|hiking|horse-riding|motorboat|canoe|inline-skating}/{fact}"))
  def subsetFactDetails(
    @PathVariable country: Country,
    @PathVariable networkType: NetworkType,
    @PathVariable fact: String
  ): ApiResponse[SubsetFactDetailsPage] = {
    Subset.of(country, networkType) match {
      case Some(subset) =>
        Fact.withName(fact) match {
          case Some(f) => analysisFacade.subsetFactDetails(subset, f)
          case None => notFound()
        }
      case None => notFound()
    }
  }

  @GetMapping(value = Array("/api/{country:be|de|fr|nl|at|es|dk}/{networkType:cycling|hiking|horse-riding|motorboat|canoe|inline-skating}/orphan-nodes"))
  def subsetOrphanNodes(
    @PathVariable country: Country,
    @PathVariable networkType: NetworkType
  ): ApiResponse[SubsetOrphanNodesPage] = {
    Subset.of(country, networkType) match {
      case Some(subset) => analysisFacade.subsetOrphanNodes(subset)
      case None => notFound()
    }
  }

  @GetMapping(value = Array("/api/{country:be|de|fr|nl|at|es|dk}/{networkType:cycling|hiking|horse-riding|motorboat|canoe|inline-skating}/orphan-routes"))
  def subsetOrphanRoutes(
    @PathVariable country: Country,
    @PathVariable networkType: NetworkType
  ): ApiResponse[SubsetOrphanRoutesPage] = {
    Subset.of(country, networkType) match {
      case Some(subset) => analysisFacade.subsetOrphanRoutes(subset)
      case None => notFound()
    }
  }

  @GetMapping(value = Array("/api/{country:be|de|fr|nl|at|es|dk}/{networkType:cycling|hiking|horse-riding|motorboat|canoe|inline-skating}/map"))
  def subsetMap(
    @PathVariable country: Country,
    @PathVariable networkType: NetworkType
  ): ApiResponse[SubsetMapPage] = {
    Subset.of(country, networkType) match {
      case Some(subset) => analysisFacade.subsetMap(subset)
      case None => notFound()
    }
  }

  @PostMapping(value = Array("/api/{country:be|de|fr|nl|at|es|dk}/{networkType:cycling|hiking|horse-riding|motorboat|canoe|inline-skating}/changes"))
  def subsetChanges(
    @PathVariable country: Country,
    @PathVariable networkType: NetworkType,
    @RequestBody parameters: ChangesParameters
  ): ApiResponse[SubsetChangesPage] = {
    Subset.of(country, networkType) match {
      case None => notFound()
      case Some(subset) =>
        analysisFacade.subsetChanges(subset, parameters)
    }
  }

  @GetMapping(value = Array("/api/network/{networkId}"))
  def networkDetails(@PathVariable networkId: Long): ApiResponse[NetworkDetailsPage] = {
    analysisFacade.networkDetails(networkId)
  }

  @GetMapping(value = Array("/api/network/{networkId}/map"))
  def networkMap(@PathVariable networkId: Long): ApiResponse[NetworkMapPage] = {
    analysisFacade.networkMap(networkId)
  }

  @GetMapping(value = Array("/api/network/{networkId}/facts"))
  def networkFacts(@PathVariable networkId: Long): ApiResponse[NetworkFactsPage] = {
    analysisFacade.networkFacts(networkId)
  }

  @GetMapping(value = Array("/api/network/{networkId}/nodes"))
  def networkNodes(@PathVariable networkId: Long): ApiResponse[NetworkNodesPage] = {
    analysisFacade.networkNodes(networkId)
  }

  @GetMapping(value = Array("/api/network/{networkId}/routes"))
  def networkRoutes(@PathVariable networkId: Long): ApiResponse[NetworkRoutesPage] = {
    analysisFacade.networkRoutes(networkId)
  }

  @PostMapping(value = Array("/api/network/{networkId}/changes"))
  def networkChanges(
    @PathVariable networkId: Long,
    @RequestBody parameters: ChangesParameters
  ): ApiResponse[NetworkChangesPage] = {
    analysisFacade.networkChanges(networkId, parameters)
  }

  @GetMapping(value = Array("/api/node/{nodeId}"))
  def node(
    @RequestParam language: String,
    @PathVariable nodeId: Long
  ): ApiResponse[NodeDetailsPage] = {
    analysisFacade.nodeDetails(toLanguage(language), nodeId)
  }

  @GetMapping(value = Array("/api/node/{nodeId}/map"))
  def nodeMap(@PathVariable nodeId: Long): ApiResponse[NodeMapPage] = {
    analysisFacade.nodeMap(nodeId)
  }

  @PostMapping(value = Array("/api/node/{nodeId}/changes"))
  def nodeChanges(
    @PathVariable nodeId: Long,
    @RequestBody parameters: ChangesParameters
  ): ApiResponse[NodeChangesPage] = {
    analysisFacade.nodeChanges(nodeId, parameters)
  }

  @GetMapping(value = Array("/api/route/{routeId}"))
  def route(
    @RequestParam language: String,
    @PathVariable routeId: Long
  ): ApiResponse[RouteDetailsPage] = {
    analysisFacade.routeDetails(toLanguage(language), routeId)
  }

  @GetMapping(value = Array("/api/route/{routeId}/map"))
  def routeMap(@PathVariable routeId: Long): ApiResponse[RouteMapPage] = {
    analysisFacade.routeMap(routeId)
  }

  @PostMapping(value = Array("/api/route/{routeId}/changes"))
  def routeChanges(
    @PathVariable routeId: Long,
    @RequestBody parameters: ChangesParameters
  ): ApiResponse[RouteChangesPage] = {
    analysisFacade.routeChanges(routeId, parameters)
  }

  @PostMapping(value = Array("/api/changes"))
  def changes(
    @RequestParam language: String,
    @RequestParam strategy: String,
    @RequestBody parameters: ChangesParameters
  ): ApiResponse[ChangesPage] = {
    analysisFacade.changes(
      toLanguage(language),
      toAnalysisStrategy(strategy),
      parameters
    )
  }

  @GetMapping(value = Array("/api/changeset/{changeSetId}/{replicationNumber}"))
  def changeSet(
    @RequestParam language: String,
    @PathVariable changeSetId: Long,
    @PathVariable replicationNumber: Int
  ): ApiResponse[ChangeSetPage] = {
    val replicationId = ReplicationId(replicationNumber)
    analysisFacade.changeSet(toLanguage(language), changeSetId, Some(replicationId))
  }

  @GetMapping(value = Array("/api/replication/{changeSetId}"))
  def replication(
    @RequestParam language: String,
    @PathVariable changeSetId: Long
  ): ApiResponse[Long] = {
    analysisFacade.replication(toLanguage(language), changeSetId)
  }

  @GetMapping(value = Array("/api/survey-date-info"))
  def surveyDateInfo(): ApiResponse[SurveyDateInfo] = {
    ApiResponse(None, 1, Some(SurveyDateInfoBuilder.dateInfo))
  }

  @GetMapping(value = Array("/api/locations/{language}/{networkType}/{country}"))
  def locations(
    @PathVariable language: String,
    @PathVariable networkType: NetworkType,
    @PathVariable country: Country
  ): ApiResponse[LocationsPage] = {
    analysisFacade.locations(toLanguage(language), networkType, country)
  }

  @PostMapping(value = Array("/api/{networkType}/{country}/{location}/nodes"))
  def locationNodes(
    @RequestParam language: String,
    @PathVariable networkType: NetworkType,
    @PathVariable country: Country,
    @PathVariable location: String,
    @RequestBody parameters: LocationNodesParameters
  ): ApiResponse[LocationNodesPage] = {
    val locationKey = LocationKey(networkType, country, location)
    analysisFacade.locationNodes(toLanguage(language), locationKey, parameters)
  }

  @PostMapping(value = Array("/api/{networkType}/{country}/{location}/routes"))
  def locationRoutes(
    @RequestParam language: String,
    @PathVariable networkType: NetworkType,
    @PathVariable country: Country,
    @PathVariable location: String,
    @RequestBody parameters: LocationRoutesParameters
  ): ApiResponse[LocationRoutesPage] = {
    val locationKey = LocationKey(networkType, country, location)
    analysisFacade.locationRoutes(toLanguage(language), locationKey, parameters)
  }

  @GetMapping(value = Array("/api/{networkType}/{country}/{location}/facts"))
  def locationFacts(
    @RequestParam language: String,
    @PathVariable networkType: NetworkType,
    @PathVariable country: Country,
    @PathVariable location: String
  ): ApiResponse[LocationFactsPage] = {
    val locationKey = LocationKey(networkType, country, location)
    analysisFacade.locationFacts(toLanguage(language), locationKey)
  }

  @GetMapping(value = Array("/api/{networkType}/{country}/{location}/map"))
  def locationMap(
    @RequestParam language: String,
    @PathVariable networkType: NetworkType,
    @PathVariable country: Country,
    @PathVariable location: String
  ): ApiResponse[LocationMapPage] = {
    val locationKey = LocationKey(networkType, country, location)
    analysisFacade.locationMap(toLanguage(language), locationKey)
  }

  @PostMapping(value = Array("/api/{networkType:cycling|hiking|horse-riding|motorboat|canoe|inline-skating}/{country:be|de|fr|nl|at|es|dk}/{location}/changes"))
  def locationChanges(
    @RequestParam language: String,
    @PathVariable networkType: NetworkType,
    @PathVariable country: Country,
    @PathVariable location: String,
    @RequestBody parameters: ChangesParameters
  ): ApiResponse[LocationChangesPage] = {
    val locationKey = LocationKey(networkType, country, location)
    analysisFacade.locationChanges(toLanguage(language), locationKey, parameters)
  }

  @PostMapping(value = Array("/api/{networkType}/{country}/{location}/edit"))
  def locationEdit(
    @RequestParam language: String,
    @PathVariable networkType: NetworkType,
    @PathVariable country: Country,
    @PathVariable location: String
  ): ApiResponse[LocationEditPage] = {
    val locationKey = LocationKey(networkType, country, location)
    analysisFacade.locationEdit(toLanguage(language), locationKey)
  }

  private def toLanguage(language: String): Language = {
    Languages.all.find(_.toString.toLowerCase == language).getOrElse(EN)
  }

  private def toAnalysisStrategy(analysisStrategy: String): AnalysisStrategy = {
    AnalysisStrategy.all.find(_.toString.toLowerCase == analysisStrategy).getOrElse(LOCATION)
  }

  private def notFound[T](): ApiResponse[T] = {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND)
  }
}
