package kpn.server.analyzer.engine.changes.network

import kpn.api.common.changes.details.ChangeType
import kpn.api.common.changes.details.NetworkInfoChange
import kpn.api.common.changes.details.RefChanges
import kpn.api.common.common.Ref
import kpn.api.common.diff.IdDiffs
import kpn.api.common.diff.RefDiffs
import kpn.api.custom.Fact
import kpn.core.mongo.Database
import kpn.core.mongo.actions.nodes.MongoQueryNodeNetworkReferences
import kpn.core.mongo.actions.routes.MongoQueryRouteNetworkReferences
import kpn.core.mongo.doc.NetworkInfoDoc
import kpn.server.analyzer.engine.analysis.network.info.NetworkInfoMasterAnalyzer
import kpn.server.analyzer.engine.changes.ChangeSetContext
import org.springframework.stereotype.Component

@Component
class NetworkInfoChangeProcessorImpl(
  database: Database,
  networkInfoMasterAnalyzer: NetworkInfoMasterAnalyzer
) extends NetworkInfoChangeProcessor {

  def analyze(context: ChangeSetContext): ChangeSetContext = {

    val impactedNetworkIds = analyzeImpact(context)

    val networkChanges = impactedNetworkIds.flatMap { networkId =>
      val beforeOption = database.networkInfos.findById(networkId)
      val afterOption = networkInfoMasterAnalyzer.updateNetwork(networkId)
      beforeOption match {
        case None =>
          afterOption match {
            case None => None // TODO message ?
            case Some(after) =>
              processCreate(context, after, networkId)
          }
        case Some(before) =>
          afterOption match {
            case None => processDelete(context, before, networkId)
            case Some(after) => processUpdate(context, before, after, networkId)
          }
      }
    }

    context.copy(
      changes = context.changes.copy(
        networkInfoChanges = networkChanges
      )
    )
  }

  private def analyzeImpact(context: ChangeSetContext): Seq[Long] = {
    val impactedNetworkIds1 = context.changes.networkChanges.map(_.networkId)
    val impactedRouteIds = context.changes.routeChanges.map(_.id)
    val impactedNetworkIds2 = new MongoQueryRouteNetworkReferences(database).executeRouteIds(impactedRouteIds)
    val impactedNodeIds1 = context.changes.networkChanges.flatMap(_.impactedNodeIds)
    val impactedNodeIds2 = context.changes.routeChanges.flatMap(_.impactedNodeIds)
    val impactedNodeIds3 = context.changes.nodeChanges.map(_.id)
    val impactedNodeIds = (impactedNodeIds1 ++ impactedNodeIds2 ++ impactedNodeIds3).distinct.sorted
    val impactedNetworkIds3 = new MongoQueryNodeNetworkReferences(database).executeNodeIds(impactedNodeIds)
    (impactedNetworkIds1 ++ impactedNetworkIds2 ++ impactedNetworkIds3).distinct.sorted
  }

  private def processCreate(context: ChangeSetContext, after: NetworkInfoDoc, networkId: Long): Option[NetworkInfoChange] = {

    val networkChangeOption = context.changes.networkChanges.find(_.networkId == networkId)

    val oldOrphanRouteRefs = after.routes.map(_.id).flatMap { routeId =>
      context.changes.routeChanges.find(_.id == routeId) match {
        case None => None
        case Some(routeChange) =>
          if (routeChange.facts.contains(Fact.WasOrphan)) {
            Some(routeChange.toRef)
          }
          else {
            None
          }
      }
    }

    val oldOrphanNodeRefs = after.nodes.map(_.id).flatMap { nodeId =>
      context.changes.nodeChanges.find(_.id == nodeId) match {
        case None => None
        case Some(nodeChange) =>
          if (nodeChange.facts.contains(Fact.WasOrphan)) {
            Some(nodeChange.toRef)
          }
          else {
            None
          }
      }
    }

    val key = context.buildChangeKey(networkId)
    Some(
      NetworkInfoChange(
        key.toId,
        key,
        changeType = ChangeType.Create,
        country = after.country,
        networkType = after.scopedNetworkType.networkType,
        networkId = after._id,
        networkName = after.summary.name,
        orphanRoutes = RefChanges(
          oldRefs = oldOrphanRouteRefs
        ),
        orphanNodes = RefChanges(
          oldRefs = oldOrphanNodeRefs
        ),
        networkDataUpdate = None, // TODO Option[NetworkDataUpdate],
        networkNodes = RefDiffs(
          added = after.nodes.map(_.toRef)
        ),
        routes = RefDiffs(
          added = after.routes.map(_.toRef)
        ),
        nodes = networkChangeOption.map(_.nodes).getOrElse(IdDiffs.empty),
        ways = networkChangeOption.map(_.ways).getOrElse(IdDiffs.empty),
        relations = networkChangeOption.map(_.relations).getOrElse(IdDiffs.empty),
        happy = true,
        investigate = false,
        impact = true
      )
    )
  }

  private def processDelete(context: ChangeSetContext, before: NetworkInfoDoc, networkId: Long): Option[NetworkInfoChange] = {

    val networkChangeOption = context.changes.networkChanges.find(_.networkId == networkId)

    val newOrphanRouteRefs = before.routes.map(_.id).flatMap { routeId =>
      context.changes.routeChanges.find(_.id == routeId) match {
        case None => None
        case Some(routeChange) =>
          if (routeChange.facts.contains(Fact.BecomeOrphan)) {
            Some(routeChange.toRef)
          }
          else {
            None
          }
      }
    }

    val newOrphanNodeRefs = before.nodes.map(_.id).flatMap { nodeId =>
      context.changes.nodeChanges.find(_.id == nodeId) match {
        case None => None
        case Some(nodeChange) =>
          if (nodeChange.facts.contains(Fact.BecomeOrphan)) {
            Some(nodeChange.toRef)
          }
          else {
            None
          }
      }
    }

    val key = context.buildChangeKey(networkId)
    Some(
      NetworkInfoChange(
        key.toId,
        key,
        changeType = ChangeType.Delete,
        country = before.country,
        networkType = before.scopedNetworkType.networkType,
        networkId = networkId,
        networkName = before.summary.name,
        orphanRoutes = RefChanges(
          newRefs = newOrphanRouteRefs
        ),
        orphanNodes = RefChanges(
          newRefs = newOrphanNodeRefs
        ),
        networkDataUpdate = None, // TODO Option[NetworkDataUpdate],
        networkNodes = RefDiffs(
          removed = before.nodes.map(_.toRef)
        ),
        routes = RefDiffs(
          removed = before.routes.map(_.toRef)
        ),
        nodes = networkChangeOption.map(_.nodes).getOrElse(IdDiffs.empty),
        ways = networkChangeOption.map(_.ways).getOrElse(IdDiffs.empty),
        relations = networkChangeOption.map(_.relations).getOrElse(IdDiffs.empty),
        happy = false,
        investigate = true,
        impact = true
      )
    )
  }

  private def processUpdate(context: ChangeSetContext, before: NetworkInfoDoc, after: NetworkInfoDoc, networkId: Long): Option[NetworkInfoChange] = {

    if (!after.active) {
      processDelete(context, before, networkId)
    }
    else {
      if (before == after) {
        None
      }
      else {

        // TODO see: NetworkDiffAnalyzer

        val nodeDiffs = analyzeNodeDiffs(before, after)
        val routeDiffs = analyzeRouteDiffs(before, after)

        val nodes = IdDiffs.empty // TODO
        val ways = IdDiffs.empty // TODO
        val relations = IdDiffs.empty // TODO

        val happy = nodeDiffs.added.nonEmpty || routeDiffs.added.nonEmpty

        val investigate = nodeDiffs.removed.nonEmpty ||
          routeDiffs.removed.nonEmpty ||
          nodes.added.nonEmpty ||
          ways.added.nonEmpty ||
          relations.added.nonEmpty

        val impact = happy || investigate

        //        case class NetworkDataUpdate(
        //          before: NetworkData,
        //          after: NetworkData
        //        )

        val key = context.buildChangeKey(networkId)
        Some(
          NetworkInfoChange(
            key.toId,
            key,
            changeType = ChangeType.Update,
            country = after.country,
            networkType = after.scopedNetworkType.networkType,
            networkId = networkId,
            networkName = after.summary.name,
            orphanRoutes = RefChanges(
              // TODO oldRefs = oldOrphanRouteRefs
            ),
            orphanNodes = RefChanges(
              // TODO oldRefs = oldOrphanNodeRefs
            ),
            networkDataUpdate = None, // TODO Option[NetworkDataUpdate],
            networkNodes = nodeDiffs,
            routes = routeDiffs,
            nodes = nodes,
            ways = ways,
            relations = relations,
            happy = happy,
            investigate = investigate,
            impact = impact
          )
        )
      }
    }
  }

  private def analyzeNodeDiffs(before: NetworkInfoDoc, after: NetworkInfoDoc): RefDiffs = {
    val nodeIdsBefore = before.nodes.map(_.id).toSet
    val nodeIdsAfter = after.nodes.map(_.id).toSet
    val nodeIdsAdded = nodeIdsAfter -- nodeIdsBefore
    val nodeIdsRemoved = nodeIdsBefore -- nodeIdsAfter
    val nodeIdsCommon = nodeIdsBefore.intersect(nodeIdsAfter)
    val nodeRefsAdded = after.nodes.filter(node => nodeIdsAdded.contains(node.id)).map(node => Ref(node.id, node.name)).sortBy(_.id)
    val nodeRefsRemoved = before.nodes.filter(node => nodeIdsRemoved.contains(node.id)).map(node => Ref(node.id, node.name)).sortBy(_.id)
    val nodeRefsUpdated = nodeIdsCommon.toSeq.sorted.flatMap { nodeId =>
      before.nodes.find(node => node.id == nodeId).flatMap { nodeBefore =>
        after.nodes.find(node => node.id == nodeId).flatMap { nodeAfter =>
          if (!nodeBefore.isSameAs(nodeAfter)) {
            Some(Ref(nodeId, nodeAfter.name))
          }
          else {
            None
          }
        }
      }
    }
    RefDiffs(
      removed = nodeRefsRemoved,
      added = nodeRefsAdded,
      updated = nodeRefsUpdated
    )
  }

  private def analyzeRouteDiffs(before: NetworkInfoDoc, after: NetworkInfoDoc): RefDiffs = {
    val routeIdsBefore = before.routes.map(_.id).toSet
    val routeIdsAfter = after.routes.map(_.id).toSet
    val routeIdsAdded = routeIdsAfter -- routeIdsBefore
    val routeIdsRemoved = routeIdsBefore -- routeIdsAfter
    val routeIdsCommon = routeIdsBefore.intersect(routeIdsAfter)
    val routeRefsAdded = after.routes.filter(route => routeIdsAdded.contains(route.id)).map(route => Ref(route.id, route.name)).sortBy(_.id)
    val routeRefsRemoved = before.routes.filter(route => routeIdsRemoved.contains(route.id)).map(route => Ref(route.id, route.name)).sortBy(_.id)
    val routeRefsUpdated = routeIdsCommon.toSeq.sorted.flatMap { routeId =>
      before.routes.find(route => route.id == routeId).flatMap { routeBefore =>
        after.routes.find(route => route.id == routeId).flatMap { routeAfter =>
          if (!routeBefore.isSameAs(routeAfter)) {
            Some(Ref(routeId, routeAfter.name))
          }
          else {
            None
          }
        }
      }
    }
    RefDiffs(
      removed = routeRefsRemoved,
      added = routeRefsAdded,
      updated = routeRefsUpdated
    )
  }
}