package kpn.server.analyzer.engine.analysis.caseStudies

import kpn.api.custom.Fact
import kpn.api.custom.Relation
import kpn.core.data.Data
import kpn.core.data.DataBuilder
import kpn.core.loadOld.Parser
import kpn.core.util.UnitTest
import kpn.server.analyzer.engine.analysis.location.LocationAnalyzerFixed
import kpn.server.analyzer.engine.analysis.route.MasterRouteAnalyzerImpl
import kpn.server.analyzer.engine.analysis.route.analyzers.RouteCountryAnalyzer
import kpn.server.analyzer.engine.analysis.route.analyzers.RouteLocationAnalyzerMock
import kpn.server.analyzer.engine.analysis.route.analyzers.RouteTileAnalyzer
import kpn.server.analyzer.engine.context.AnalysisContext
import kpn.server.analyzer.engine.tile.OldLinesTileCalculatorImpl
import kpn.server.analyzer.engine.tile.OldTileCalculatorImpl
import kpn.server.analyzer.engine.tile.RouteTileCalculatorImpl
import kpn.server.repository.RouteRepository
import org.scalamock.scalatest.MockFactory

import scala.xml.InputSource
import scala.xml.XML

class Issue48_RouteWithSingleNodeWayTest extends UnitTest with MockFactory {

  test("ignore ways with less than 2 nodes in route analysis") {
    val routeRelation = readRoute()
    val analysisContext = new AnalysisContext()
    val locationAnalyzer = new LocationAnalyzerFixed()
    val tileCalculator = new OldTileCalculatorImpl()
    val linesTileCalculator = new OldLinesTileCalculatorImpl(tileCalculator)
    val routeTileCalculator = new RouteTileCalculatorImpl(linesTileCalculator)
    val routeTileAnalyzer = new RouteTileAnalyzer(routeTileCalculator)
    val routeRepository = stub[RouteRepository]
    val routeCountryAnalyzer = new RouteCountryAnalyzer(locationAnalyzer, routeRepository)
    val routeLocationAnalyzer = new RouteLocationAnalyzerMock()
    val routeAnalyzer = new MasterRouteAnalyzerImpl(
      analysisContext,
      routeCountryAnalyzer,
      routeLocationAnalyzer,
      routeTileAnalyzer
    )
    val routeAnalysis = routeAnalyzer.analyze(routeRelation).get
    assert(routeAnalysis.route.facts.contains(Fact.RouteSuspiciousWays))
  }

  private def readRoute(): Relation = {
    val data = readData()
    data.relations(2941800L)
  }

  private def readData(): Data = {
    val stream = getClass.getResourceAsStream("/case-studies/network-2243640.xml")
    val inputSource = new InputSource(stream)
    val xml = XML.load(inputSource)
    val rawData = new Parser(full = false).parse(xml)
    new DataBuilder(rawData).data
  }
}
