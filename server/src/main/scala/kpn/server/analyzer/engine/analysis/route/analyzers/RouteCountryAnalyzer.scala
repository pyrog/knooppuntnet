package kpn.server.analyzer.engine.analysis.route.analyzers

import kpn.server.analyzer.engine.analysis.location.LocationAnalyzer
import kpn.server.analyzer.engine.analysis.route.domain.RouteAnalysisContext
import kpn.server.repository.RouteRepository
import org.springframework.stereotype.Component

@Component
class RouteCountryAnalyzer(locationAnalyzer: LocationAnalyzer, routeRepository: RouteRepository) extends RouteAnalyzer {

  def analyze(context: RouteAnalysisContext): RouteAnalysisContext = {
    val countryOption = locationAnalyzer.relationCountry(context.relation) match {
      case Some(country) => Some(country)
      case None => routeRepository.routeCountry(context.relation.id)
    }
    context.copy(
      country = countryOption,
      abort = countryOption.isEmpty
    )
  }
}
