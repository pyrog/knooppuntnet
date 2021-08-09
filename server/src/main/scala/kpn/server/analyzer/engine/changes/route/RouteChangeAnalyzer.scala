package kpn.server.analyzer.engine.changes.route

import kpn.api.common.changes.ChangeAction.ChangeAction
import kpn.api.common.changes.ChangeAction.Create
import kpn.api.common.changes.ChangeAction.Delete
import kpn.api.common.changes.ChangeAction.Modify
import kpn.api.common.changes.ChangeSet
import kpn.api.common.data.raw.RawRelation
import kpn.core.analysis.TagInterpreter
import kpn.server.analyzer.engine.changes.ElementChanges
import kpn.server.analyzer.engine.changes.ElementIdAnalyzer
import kpn.server.analyzer.engine.changes.changes.ChangeSetBuilder
import kpn.server.analyzer.engine.context.AnalysisContext
import kpn.server.repository.BlackListRepository
import org.springframework.stereotype.Component

@Component
class RouteChangeAnalyzer(
  analysisContext: AnalysisContext,
  blackListRepository: BlackListRepository,
  elementIdAnalyzer: ElementIdAnalyzer
) {

  def analyze(changeSet: ChangeSet): ElementChanges = {

    val createdRelationsById = buildRelationMap(changeSet, Create)
    val updatedRelationsById = buildRelationMap(changeSet, Modify)
    val deletedRelationsById = buildRelationMap(changeSet, Delete)

    val createdRouteIds = findRouteRelationIds(createdRelationsById)
    val updatedRouteIds = findRouteRelationIds(updatedRelationsById)

    val routeCreateIds1 = createdRouteIds.filterNot(isKnownRoute)
    val routeCreateIds2 = updatedRouteIds.filterNot(isKnownRoute)

    val routeUpdateIds1 = elementIdAnalyzer.referencedBy(
      analysisContext.data.routes.watched,
      ChangeSetBuilder.elementIdsIn(changeSet)
    )

    val routeUpdateIds2 = updatedRouteIds.filter(isKnownRoute)

    val deletes = deletedRelationsById.keySet.filter { routeId =>
      isKnownRoute(routeId) && !isBlackListed(routeId)
    }

    val updates = routeUpdateIds1 ++ routeUpdateIds2 -- deletes
    val creates = routeCreateIds1 ++ routeCreateIds2 -- updates -- deletes

    val sortedCreates = creates.toList.sorted
    val sortedUpdates = updates.toList.sorted
    val sortedDeletes = deletes.toList.sorted

    ElementChanges(sortedCreates, sortedUpdates, sortedDeletes)
  }

  private def buildRelationMap(changeSet: ChangeSet, action: ChangeAction): Map[Long, RawRelation] = {
    changeSet.changes
      .filter(_.action == action)
      .flatMap(_.elements)
      .collect { case e: RawRelation => e }
      .map(n => n.id -> n)
      .toMap
  }

  private def findRouteRelationIds(relationsById: Map[Long, RawRelation]): Set[Long] = {
    relationsById.values.
      filter(r => TagInterpreter.isRouteRelation(r.tags)).
      filterNot(r => isBlackListed(r.id)).
      map(_.id).
      toSet
  }

  private def isKnownRoute(routeId: Long): Boolean = {
    analysisContext.data.routes.watched.contains(routeId)
  }

  private def isBlackListed(routeId: Long): Boolean = {
    blackListRepository.get.containsRoute(routeId)
  }
}
