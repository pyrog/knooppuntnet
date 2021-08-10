package kpn.server.analyzer.engine.changes.builder

import kpn.api.common.changes.details.ChangeType
import kpn.api.common.changes.details.NodeChange
import kpn.api.common.changes.details.RefBooleanChange
import kpn.api.common.common.Ref
import kpn.api.common.data.raw.RawNode
import kpn.api.common.diff.common.FactDiffs
import kpn.api.custom.Country
import kpn.api.custom.Fact
import kpn.api.custom.Subset
import kpn.core.analysis.Network
import kpn.core.analysis.NetworkNodeInfo
import kpn.core.history.NodeMovedAnalyzer
import kpn.core.history.NodeTagDiffAnalyzer
import kpn.core.util.Log
import kpn.server.analyzer.engine.analysis.node.NodeAnalyzer
import kpn.server.analyzer.engine.analysis.node.domain.NodeAnalysis
import kpn.server.analyzer.engine.changes.node.NodeChangeFactAnalyzer
import kpn.server.analyzer.engine.changes.node.NodeChangeStateAnalyzer
import kpn.server.analyzer.engine.context.AnalysisContext
import kpn.server.analyzer.load.NodeLoader
import kpn.server.repository.NodeRepository
import org.springframework.stereotype.Component

@Component
class NodeChangeBuilderImpl(
  analysisContext: AnalysisContext,
  nodeRepository: NodeRepository,
  nodeLoader: NodeLoader,
  nodeAnalyzer: NodeAnalyzer
) extends NodeChangeBuilder {

  private val log = Log(classOf[NodeChangeBuilderImpl])

  override def build(context: ChangeBuilderContext): Seq[NodeChange] = {

    val nodeIdsBefore = nodeIdsIn(context.networkBefore)
    val nodeIdsAfter = nodeIdsIn(context.networkAfter)

    val addedNodeIds = nodeIdsAfter -- nodeIdsBefore
    val removedNodeIds = nodeIdsBefore -- nodeIdsAfter
    val commonNodeIds = nodeIdsBefore intersect nodeIdsAfter

    nodeChangesAdded(context, addedNodeIds) ++
      nodeChangesRemoved(context, removedNodeIds) ++
      nodeChangesUpdated(context, commonNodeIds)
  }

  private def nodeChangesAdded(context: ChangeBuilderContext, nodeIds: Set[Long]): Seq[NodeChange] = {

    // TODO CHANGE
    val nodesBefore1: Seq[NetworkNodeInfo] = Seq.empty // networkNodeInfosIn(context.networkBefore, nodeIds)
    val nodesBefore2 = {
      val missingNodeIds = nodeIds -- nodesBefore1.map(_.id).toSet
      nodeLoader.oldLoadNodes(context.changeSetContext.timestampBefore, missingNodeIds.toSeq)
    }
    val nodesAfter = networkNodeInfosIn(context.networkAfter, nodeIds)

    nodesAfter.map { nodeAfter =>

      val nodeId = nodeAfter.id

      val extraFacts = Seq(
        if (analysisContext.data.nodes.watched.contains(nodeId)) {
          analysisContext.data.nodes.watched.delete(nodeId)
          Some(Fact.WasOrphan)
        } else {
          None
        }
      ).flatten


      val addedToRoute = determineAddedToRoutes(context, nodeId)

      nodesBefore2.find(_.id == nodeId) match {

        case None =>

          //noinspection SideEffectsInMonadicTransformation
          assertNodeVersion1(nodeAfter.networkNode.node.raw)

          val subsets: Seq[Subset] = context.networkAfter.flatMap { networkAfter =>
            nodeAfter.networkNode.country.flatMap(c => Subset.of(c, networkAfter.networkType))
          }.toSeq

          val key = context.changeSetContext.buildChangeKey(nodeAfter.id)
          analyzed(
            NodeChange(
              _id = key.toId,
              key = key,
              changeType = ChangeType.Create,
              subsets = subsets,
              locations = nodeAfter.networkNode.locations,
              name = nodeAfter.networkNode.name,
              before = None,
              after = Some(nodeAfter.networkNode.node.raw),
              connectionChanges = Seq.empty,
              roleConnectionChanges = Seq.empty,
              definedInNetworkChanges = Seq.empty,
              tagDiffs = None,
              nodeMoved = None,
              addedToRoute = addedToRoute,
              removedFromRoute = Seq.empty,
              addedToNetwork = context.networkRef.toSeq,
              removedFromNetwork = Seq.empty,
              factDiffs = FactDiffs(),
              facts = extraFacts
            )
          )

        case Some(nodeBefore) =>

          val before = nodeBefore.node.raw
          val after = nodeAfter.networkNode.node.raw
          val facts = new NodeChangeFactAnalyzer(analysisContext).facts(before, after)
          val tagDiffs = new NodeTagDiffAnalyzer(before, after).diffs
          val nodeMoved = new NodeMovedAnalyzer(before, after).analysis

          val subsets: Seq[Subset] = {
            val countries: Seq[Country] = (nodeBefore.country ++ nodeAfter.networkNode.country).toSeq
            context.networkAfter.toSeq.flatMap(networkAfter => countries.flatMap(c => Subset.of(c, networkAfter.networkType))).distinct.sorted
          }

          val key = context.changeSetContext.buildChangeKey(nodeAfter.id)
          analyzed(
            NodeChange(
              _id = key.toId,
              key = key,
              changeType = ChangeType.Update,
              subsets = subsets,
              locations = nodeAfter.networkNode.locations,
              name = nodeAfter.networkNode.name,
              before = Some(nodeBefore.node.raw),
              after = Some(nodeAfter.networkNode.node.raw),
              connectionChanges = Seq.empty,
              roleConnectionChanges = Seq.empty,
              definedInNetworkChanges = Seq.empty,
              tagDiffs = tagDiffs,
              nodeMoved = nodeMoved,
              addedToRoute = addedToRoute,
              removedFromRoute = Seq.empty,
              addedToNetwork = context.networkRef.toSeq,
              removedFromNetwork = Seq.empty,
              factDiffs = FactDiffs(),
              facts = extraFacts ++ facts
            )
          )
      }
    }
  }

  private def nodeChangesRemoved(context: ChangeBuilderContext, nodeIds: Set[Long]): Seq[NodeChange] = {

    val nodesBefore = networkNodeInfosIn(context.networkBefore, nodeIds)
    val nodesAfter = nodeLoader.oldLoadNodes(context.changeSetContext.timestampAfter, nodeIds.toSeq)

    nodesBefore.flatMap { nodeBefore =>

      val nodeId = nodeBefore.id
      val removedFromRoute = determineRemovedFromRoutes(context, nodeId)

      nodesAfter.find(_.id == nodeBefore.id) match {

        case None =>

          // the node was really deleted from the database

          val nodeAnalysisOption = nodeAnalyzer.analyze(
            NodeAnalysis(
              nodeBefore.networkNode.node.raw,
              active = false,
              facts = Seq(Fact.Deleted)
            )
          )

          nodeAnalysisOption match {
            case None => None
            case Some(nodeAnalysis) =>

              val nodeDoc = nodeAnalysis.toNodeDoc.copy(lastUpdated = context.changeSetContext.changeSet.timestamp)
              nodeRepository.save(nodeDoc)

              val subsets: Seq[Subset] = context.networkBefore.flatMap { networkBefore =>
                nodeBefore.networkNode.country.flatMap(c => Subset.of(c, networkBefore.networkType))
              }.toSeq

              val key = context.changeSetContext.buildChangeKey(nodeId)
              Some(
                analyzed(
                  NodeChange(
                    _id = key.toId,
                    key = key,
                    changeType = ChangeType.Delete,
                    subsets = subsets,
                    locations = nodeAnalysis.locations,
                    name = nodeAnalysis.name,
                    before = Some(nodeAnalysis.node),
                    after = None,
                    connectionChanges = Seq.empty,
                    roleConnectionChanges = Seq.empty,
                    definedInNetworkChanges = Seq.empty,
                    tagDiffs = None,
                    nodeMoved = None,
                    addedToRoute = Seq.empty,
                    removedFromRoute = removedFromRoute,
                    addedToNetwork = Seq.empty,
                    removedFromNetwork = context.networkRef.toSeq,
                    factDiffs = FactDiffs(),
                    facts = Seq(Fact.Deleted)
                  )
                )
              )
          }

        case Some(nodeAfter) =>

          // the node is still available in the database

          val subsets: Seq[Subset] = {
            val countries: Seq[Country] = (nodeBefore.networkNode.country ++ nodeAfter.country).toSeq
            val nodeSubsets = context.networkAfter.toSeq.flatMap(networkAfter => countries.flatMap(c => Subset.of(c, networkAfter.networkType))).distinct.sorted
            if (nodeSubsets.nonEmpty) {
              nodeSubsets
            }
            else {
              (context.networkBefore.flatMap(_.subset).toSeq ++ context.networkAfter.flatMap(_.subset).toSeq).distinct.sorted
            }
          }

          val before = nodeBefore.networkNode.node.raw
          val after = nodeAfter.node.raw
          val nodeFacts = new NodeChangeFactAnalyzer(analysisContext).facts(before, after)
          val tagDiffs = new NodeTagDiffAnalyzer(before, after).diffs
          val nodeMoved = new NodeMovedAnalyzer(before, after).analysis

          if (nodeFacts.contains(Fact.LostHikingNodeTag) || nodeFacts.contains(Fact.LostBicycleNodeTag)) {

            val active = nodeAfter.networkTypes.nonEmpty

            val name = if (active) {
              nodeAfter.name
            }
            else {
              nodeBefore.networkNode.name
            }

            val nodeAfterAnalysisOption = nodeAnalyzer.analyze(
              NodeAnalysis(
                nodeAfter.node.raw,
                active = active
              )
            )

            nodeAfterAnalysisOption match {
              case None => None
              case Some(nodeAfterAnalysis) =>
                nodeRepository.save(nodeAfterAnalysis.toNodeDoc)
                val key = context.changeSetContext.buildChangeKey(nodeId)
                Some(
                  analyzed(
                    NodeChange(
                      _id = key.toId,
                      key = key,
                      changeType = ChangeType.Update,
                      subsets = subsets,
                      locations = nodeAfterAnalysis.locations,
                      name = name,
                      before = Some(before),
                      after = Some(after),
                      connectionChanges = Seq.empty,
                      roleConnectionChanges = Seq.empty,
                      definedInNetworkChanges = Seq.empty,
                      tagDiffs = tagDiffs,
                      nodeMoved = nodeMoved,
                      addedToRoute = Seq.empty,
                      removedFromRoute = removedFromRoute,
                      addedToNetwork = Seq.empty,
                      removedFromNetwork = context.networkRef.toSeq,
                      factDiffs = FactDiffs(),
                      facts = nodeFacts
                    )
                  )
                )
            }
          }
          else {

            val extraFacts: Seq[Fact] = if (isReferencedNode(after.id)) {
              Seq.empty
            }
            else {
              // the node is not referenced anymore by any network or route
              val nodeAfterAnalysisOption = nodeAnalyzer.analyze(
                NodeAnalysis(
                  nodeAfter.node.raw,
                  orphan = true
                )
              )
              nodeAfterAnalysisOption match {
                case None => Seq.empty
                case Some(nodeAfterAnalysis) =>
                  nodeRepository.save(nodeAfterAnalysis.toNodeDoc)
                  analysisContext.data.nodes.watched.add(after.id)
                  Seq(Fact.BecomeOrphan)
              }
            }

            val key = context.changeSetContext.buildChangeKey(nodeId)
            Some(
              analyzed(
                NodeChange(
                  _id = key.toId,
                  key = key,
                  changeType = ChangeType.Update,
                  subsets = subsets,
                  locations = nodeBefore.networkNode.locations,
                  name = nodeBefore.networkNode.name,
                  before = Some(before),
                  after = Some(after),
                  connectionChanges = Seq.empty,
                  roleConnectionChanges = Seq.empty,
                  definedInNetworkChanges = Seq.empty,
                  tagDiffs = tagDiffs,
                  nodeMoved = nodeMoved,
                  addedToRoute = Seq.empty,
                  removedFromRoute = removedFromRoute,
                  addedToNetwork = Seq.empty,
                  removedFromNetwork = context.networkRef.toSeq,
                  factDiffs = FactDiffs(),
                  facts = extraFacts ++ nodeFacts
                )
              )
            )
          }
      }
    }
  }

  private def nodeChangesUpdated(context: ChangeBuilderContext, nodeIds: Set[Long]): Seq[NodeChange] = {

    val nodesBefore = networkNodeInfosIn(context.networkBefore, nodeIds)
    val nodesAfter = networkNodeInfosIn(context.networkAfter, nodeIds)

    nodesBefore.flatMap { nodeBefore =>

      val nodeId = nodeBefore.id

      nodesAfter.find(_.id == nodeId).flatMap { nodeAfter =>

        val subsets: Seq[Subset] = {
          val countries: Seq[Country] = (nodeBefore.networkNode.country ++ nodeAfter.networkNode.country).toSeq
          context.networkAfter.toSeq.flatMap(networkAfter => countries.flatMap(c => Subset.of(c, networkAfter.networkType))).distinct.sorted
        }

        val addedToRoute = determineAddedToRoutes(context, nodeId)
        val removedFromRoute = determineRemovedFromRoutes(context, nodeId)
        val before = nodeBefore.networkNode.node.raw
        val after = nodeAfter.networkNode.node.raw
        val facts = new NodeChangeFactAnalyzer(analysisContext).facts(before, after)
        val tagDiffs = new NodeTagDiffAnalyzer(before, after).diffs
        val nodeMoved = new NodeMovedAnalyzer(before, after).analysis

        val connectionChanges = if (nodeBefore.connection == nodeAfter.connection) {
          Seq.empty
        }
        else {
          context.networkAfter match {
            case None => Seq.empty
            case Some(network) =>
              Seq(
                RefBooleanChange(Ref(network.id, network.name), nodeAfter.connection)
              )
          }
        }

        val roleConnectionChanges = if (nodeBefore.roleConnection == nodeAfter.roleConnection) {
          Seq.empty
        }
        else {
          context.networkAfter match {
            case None => Seq.empty
            case Some(network) =>
              Seq(
                RefBooleanChange(Ref(network.id, network.name), nodeAfter.roleConnection)
              )
          }
        }

        val definedInNetworkChanges = if (nodeBefore.definedInRelation == nodeAfter.definedInRelation) {
          Seq.empty
        }
        else {
          context.networkAfter match {
            case None => Seq.empty
            case Some(network) =>
              Seq(
                RefBooleanChange(Ref(network.id, network.name), nodeAfter.definedInRelation)
              )
          }
        }

        val extraFacts = Seq(
          if (analysisContext.data.nodes.watched.contains(nodeId)) {
            analysisContext.data.nodes.watched.delete(nodeId)
            Some(Fact.WasOrphan)
          } else {
            None
          }
        ).flatten

        val key = context.changeSetContext.buildChangeKey(nodeId)
        val nodeChange = NodeChange(
          _id = key.toId,
          key = key,
          changeType = ChangeType.Update,
          subsets = subsets,
          locations = nodeAfter.networkNode.locations,
          name = nodeAfter.networkNode.name,
          before = Some(before),
          after = Some(after),
          connectionChanges = connectionChanges,
          roleConnectionChanges = roleConnectionChanges,
          definedInNetworkChanges = definedInNetworkChanges,
          tagDiffs = tagDiffs,
          nodeMoved = nodeMoved,
          addedToRoute = addedToRoute,
          removedFromRoute = removedFromRoute,
          addedToNetwork = Seq.empty,
          removedFromNetwork = Seq.empty,
          factDiffs = FactDiffs(),
          facts = extraFacts ++ facts
        )

        if (nodeChange.isEmpty) {
          None
        }
        else {
          Some(analyzed(nodeChange))
        }
      }
    }
  }

  private def determineAddedToRoutes(context: ChangeBuilderContext, nodeId: Long): Seq[Ref] = {
    val referencedInRoutesAfter = context.networkAfter.toSeq.flatMap { networkAfter =>
      networkAfter.routes.filter(r => r.routeAnalysis.containsNode(nodeId))
    }
    referencedInRoutesAfter.filter { routeAfter =>
      context.routeAnalysesBefore.find(_.id == routeAfter.id) match {
        case Some(routeAnalysisBefore) => !routeAnalysisBefore.containsNode(nodeId)
        case None => true
      }
    }.map(_.toRef)
  }

  private def determineRemovedFromRoutes(context: ChangeBuilderContext, nodeId: Long): Seq[Ref] = {
    val referencedInRoutesBefore = context.networkBefore.toSeq.flatMap { networkBefore =>
      networkBefore.routes.filter(r => r.routeAnalysis.containsNode(nodeId))
    }
    referencedInRoutesBefore.filter { routeBefore =>
      context.routeAnalysesAfter.find(_.id == routeBefore.id) match {
        case Some(routeAnalysisAfter) => !routeAnalysisAfter.containsNode(nodeId)
        case None => true
      }
    }.map(_.toRef)
  }

  private def assertNodeVersion1(node: RawNode): Unit = {
    if (node.version != 1) {
      val message = s"Node '${node.id}' was not found in the database at the time before the changeset; " +
        s"we would expect the version of the node to be 1, but we found ${node.version}. " +
        "Continued processing anyway."
      log.warn(message)
    }
  }

  private def isReferencedNode(nodeId: Long): Boolean = {
    analysisContext.data.networks.isReferencingNode(nodeId) || analysisContext.data.routes.watched.isReferencingNode(nodeId)
  }

  private def nodeIdsIn(network: Option[Network]): Set[Long] = {
    network.toSeq.flatMap(_.nodes.map(_.id)).toSet
  }

  private def networkNodeInfosIn(network: Option[Network], nodeIds: Set[Long]): Seq[NetworkNodeInfo] = {
    network.toSeq.flatMap(_.nodes.filter(n => nodeIds.contains(n.id)))
  }

  private def analyzed(nodeChange: NodeChange): NodeChange = {
    new NodeChangeStateAnalyzer(nodeChange).analyzed()
  }
}
