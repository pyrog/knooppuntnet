package kpn.core.engine.analysis.route.analyzers

import kpn.core.analysis.RouteMember
import kpn.core.analysis.RouteMemberWay
import kpn.core.engine.analysis.route.RouteAnalysis
import kpn.core.engine.analysis.route.RouteAnalyzerFunctions
import kpn.core.engine.analysis.route.RouteNodeAnalysis
import kpn.core.engine.analysis.route.RouteStructure
import kpn.core.engine.analysis.route.RouteStructureFormatter
import kpn.core.engine.analysis.route.domain.RouteAnalysisContext
import kpn.shared.Fact
import kpn.shared.Fact.RouteBroken
import kpn.shared.RouteSummary
import kpn.shared.Timestamp
import kpn.shared.data.Element
import kpn.shared.data.Way
import kpn.shared.route.RouteInfo
import kpn.shared.route.RouteInfoAnalysis
import kpn.shared.route.RouteMap
import kpn.shared.route.RouteMemberInfo

import scala.collection.mutable.ListBuffer

class RouteAnalysisBuilder(context: RouteAnalysisContext) {

  val facts: ListBuffer[Fact] = ListBuffer[Fact]()
  facts ++= context.facts
  if (facts.exists(_.isError)) {
    facts += RouteBroken
  }

  def build: RouteAnalysis = {

    // TODO ROUTE move to RouteNameAnalyzer ???
    val title: String = context.routeNameAnalysis.get.name match {
      case Some(routeName) => routeName
      case _ => "no-name"
    }

    val display: Boolean = analyzeDisplay(title, context.routeNodeAnalysis.get)

    val route = buildRoute(
      title,
      context.routeMembers.get,
      context.ways.get,
      context.routeMap.get,
      context.unexpectedNodeIds.get,
      context.expectedName.get,
      context.structure.get,
      context.routeNodeAnalysis.get,
      display
    )

    RouteAnalysis(
      context.loadedRoute.relation,
      route = route,
      structure = context.structure.get,
      routeNodes = context.routeNodeAnalysis.get, // TODO ROUTE rename in RouteAnalysis
      routeMembers = context.routeMembers.get,
      ways = context.ways.get,
      startNodes = context.routeMap.get.startNodes,
      endNodes = context.routeMap.get.endNodes,
      startTentacleNodes = context.routeMap.get.startTentacleNodes,
      endTentacleNodes = context.routeMap.get.endTentacleNodes,
      allWayNodes = context.allWayNodes.get,
      bounds = context.routeMap.get.bounds
    )
  }

  private def buildRoute(
    title: String,
    routeMembers: Seq[RouteMember],
    ways: Seq[Way],
    routeMap: RouteMap,
    unexpectedNodeIds: Seq[Long],
    expectedName: String,
    structure: RouteStructure,
    routeNodeAnalysis: RouteNodeAnalysis,
    display: Boolean
  ): RouteInfo = {

    val members: Seq[RouteMemberInfo] = routeMembers.map { member =>

      RouteMemberInfo(
        member.id,
        member.memberType,
        member.memberType == "way",
        member.nodes,
        member.linkName,
        member.from: String,
        member.fromNode.id,
        member.to,
        member.toNode.id,
        member.role.getOrElse(""),
        member.element.timestamp,
        member.accessible, // TODO ROUTE rename
        member.length,
        member.nodeCount,
        member.description,
        RouteAnalyzerFunctions.oneWay(member),
        RouteAnalyzerFunctions.oneWayTags(member)
      )
    }

    val length: Int = ways.map(_.length).sum

    val routeWays: Seq[Way] = {
      routeMembers.flatMap {
        case w: RouteMemberWay => Some(w.way)
        case _ => None
      }
    }

    def routeMemberWays: Seq[RouteMemberWay] = {
      routeMembers.flatMap {
        case w: RouteMemberWay => Some(w)
        case _ => None
      }
    }

    val accessible: Boolean = ways.size == routeMemberWays.count(_.accessible)

    val routeAnalysis = RouteInfoAnalysis(// TODO ROUTE include this information in Route
      routeMap.startNodes, // TODO ROUTE duplication in routeMap member
      routeMap.endNodes, // TODO ROUTE duplication in routeMap member
      routeMap.startTentacleNodes, // TODO ROUTE duplication in routeMap member
      routeMap.endTentacleNodes, // TODO ROUTE duplication in routeMap member
      unexpectedNodeIds,
      members,
      expectedName,
      routeMap,
      new RouteStructureFormatter(structure).strings
    )

    val lastUpdatedElement: Element = {
      val elements: Seq[Element] = Seq(context.loadedRoute.relation) ++ routeWays ++ routeNodeAnalysis.routeNodes.map(rn => rn.node)
      elements.reduceLeft((a, b) => if (a.timestamp > b.timestamp) a else b)
    }

    val lastUpdated: Timestamp = lastUpdatedElement.timestamp

    val nodeNames = routeAnalysis.startNodes.map(_.name) ++ routeAnalysis.endNodes.map(_.name)

    val summary = RouteSummary(
      context.loadedRoute.relation.id,
      context.loadedRoute.country,
      context.interpreter.networkType,
      title,
      length,
      facts.contains(Fact.RouteBroken),
      routeWays.size,
      context.loadedRoute.relation.timestamp,
      nodeNames,
      context.loadedRoute.relation.tags
    )

    RouteInfo(
      summary,
      active = true,
      display = display,
      ignored = false,
      orphan = context.orphan,
      context.loadedRoute.relation.version,
      context.loadedRoute.relation.changeSetId,
      lastUpdated,
      context.loadedRoute.relation.tags,
      facts,
      Some(routeAnalysis)
    )
  }

  /*
    The property 'display' indicates whether the route is to be included in the
    analysis reports on screen.

    Display is false for orphan routes that we want to keep watching, but not show in screens:
    - do not display orphan routes when "no-name"
    - do not display orphan routes when no network nodes at all
   */
  private def analyzeDisplay(title: String, routeNodeAnalysis: RouteNodeAnalysis): Boolean = {
    if (context.orphan) {
      if (title == "no-name") {
        false
      }
      else {
        routeNodeAnalysis.routeNodes.nonEmpty
      }
    }
    else {
      true
    }
  }

}
