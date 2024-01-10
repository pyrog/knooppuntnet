package kpn.server.analyzer.engine.monitor

import kpn.api.common.data.RelationMember
import kpn.api.common.data.WayMember
import kpn.api.custom.Relation
import kpn.server.analyzer.engine.analysis.route.RouteWay

import scala.collection.mutable

class MonitorRouteStructureAnalyzer {

  private val connectionAnalyzer = new MonitorRouteConnectionAnalyzer
  private val nextRouteAnalyzer = new MonitorRouteNextRouteAnalyzer

  private var startWayMember: Option[WayMember] = None
  private var processedStartWayMember = false

  val segments = mutable.Buffer[Seq[RouteWay]]()
  val currentSegment = mutable.Buffer[RouteWay]()
  var loop: Option[Seq[RouteWay]] = None
  var endNodeId: Long = 0

  def analyzeRoute(relation: Relation): Seq[Seq[RouteWay]] = {
    relation.members.foreach { member =>
      member match {
        case wayMember: WayMember => processWayMember(wayMember)
        case relationMember: RelationMember => processRelationMember(relationMember)
        case _ => None
      }
    }

    startWayMember match {
      case None => // nothing more to do here
      case Some(wayMember) =>
        if (processedStartWayMember == false) {
          currentSegment.addOne(RouteWay(wayMember.way))
        }
    }

    if (currentSegment.nonEmpty) {
      segments.addOne(currentSegment.toSeq)
    }
    segments.toSeq
  }

  private def processRelationMember(relationMember: RelationMember): Unit = {
    // still to be implemented
  }

  private def processWayMember(currentWayMember: WayMember): Unit = {
    if (currentWayMember.way.nodes.length >= 2) {
      if (!processedStartWayMember) {
        startWayMember match {
          case None =>
            startWayMember = Some(currentWayMember)
          case Some(wayMember) =>
            processStart(wayMember, currentWayMember)
        }
      }
      else {
        processNextWayMember(currentWayMember)
      }
    }
  }

  private def processStart(wayMember1: WayMember, wayMember2: WayMember): Unit = {
    val routeWays = connectionAnalyzer.analyze(wayMember1, wayMember2)
    if (routeWays.isEmpty) {
      // false start, the first 2 ways do not connect
      //   make way1 a separate segment
      //   make way2 the first way
      segments.addOne(Seq(RouteWay(wayMember1.way)))
      startWayMember = Some(wayMember2)
    }
    else {
      currentSegment.addAll(routeWays)
      startWayMember = None
      processedStartWayMember = true
      endNodeId = routeWays.last.endNode.id
    }
  }

  private def processNextWayMember(wayMember: WayMember): Unit = {
    loop match {
      case Some(loopRouteWays) => processLoop(loopRouteWays, wayMember)
      case None => processNextWayMemberAnalyze(wayMember)
    }
  }

  private def processNextWayMemberAnalyze(wayMember: WayMember): Unit = {
    nextRouteAnalyzer.analyze(endNodeId, wayMember) match {
      case Some(routeWay) =>
        endNodeId = routeWay.endNode.id

        currentSegment.addOne(routeWay)

        val startNodeIds = currentSegment.map(_.startNode.id)

        if (startNodeIds.contains(endNodeId)) {
          // loop, split currentSegment
          val index = currentSegment.lastIndexWhere(_.startNode.id == endNodeId)
          val segmentBeforeLoop = currentSegment.take(index)
          loop = Some(currentSegment.drop(index).toSeq)
          segments.addOne(segmentBeforeLoop.toSeq)
          currentSegment.clear()
        }

      case None =>
        if (currentSegment.nonEmpty) {
          segments.addOne(currentSegment.toSeq)
          currentSegment.clear()
        }
        startWayMember = Some(wayMember)
        processedStartWayMember = false
    }
  }

  private def processLoop(loopRouteWays: Seq[RouteWay], wayMember: WayMember): Unit = {
    val startNodeId = wayMember.way.nodes.head.id
    val endNodeId = wayMember.way.nodes.last.id

    val index = loopRouteWays.indexWhere { routeWay =>
      routeWay.endNode.id == startNodeId
    }
    if (index >= 0) {
      val routeWays1 = loopRouteWays.take(index + 1)
      val routeWays2 = loopRouteWays.drop(index + 1)
      segments.addOne(routeWays1)
      segments.addOne(routeWays2)
    }
    else {
      segments.addOne(loopRouteWays)
    }

    startWayMember = Some(wayMember)
    processedStartWayMember = false
  }

  private def debug(message: String): Unit = {
    println(message)
  }
}
