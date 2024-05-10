package kpn.server.analyzer.engine.analysis.caseStudies

import kpn.api.custom.Fact
import kpn.core.util.UnitTest

class Issue379_RedundantNodes extends UnitTest {

  test("analyze route with redundant nodes") {
    val route = CaseStudy.routeAnalysis("17574316").route
    route.facts should equal(
      Seq(
        Fact.RouteRedundantNodes,
        Fact.RouteUnusedSegments,
        Fact.RouteBroken
      )
    )
  }
}
