package kpn.server.repository

import kpn.api.common.common.Reference
import kpn.core.mongo.Database
import kpn.core.mongo.actions.nodes.MongoQueryKnownNodeIds
import kpn.core.mongo.actions.nodes.MongoQueryNodeIds
import kpn.core.mongo.actions.nodes.MongoQueryNodeNetworkReferences
import kpn.core.mongo.doc.NodeDoc
import kpn.core.util.Log
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Aggregates.filter
import org.mongodb.scala.model.Aggregates.project
import org.mongodb.scala.model.Aggregates.sort
import org.mongodb.scala.model.Filters.and
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Projections.computed
import org.mongodb.scala.model.Projections.excludeId
import org.mongodb.scala.model.Projections.fields
import org.mongodb.scala.model.Sorts.ascending
import org.mongodb.scala.model.Sorts.orderBy
import org.springframework.stereotype.Component

@Component
class NodeRepositoryImpl(database: Database) extends NodeRepository {

  private val log = Log(classOf[NodeRepositoryImpl])

  override def allNodeIds(): Seq[Long] = {
    database.nodes.ids(log)
  }

  override def activeNodeIds(): Seq[Long] = {
    new MongoQueryNodeIds(database).execute()
  }

  override def save(nodeDoc: NodeDoc): Unit = {
    database.nodes.save(nodeDoc)
  }

  override def bulkSave(nodeDocs: NodeDoc*): Unit = {
    database.nodes.bulkSave(nodeDocs)
  }

  override def delete(nodeId: Long): Unit = {
    database.nodes.delete(nodeId, log)
  }

  override def nodeWithId(nodeId: Long): Option[NodeDoc] = {
    database.nodes.findById(nodeId, log)
  }

  override def nodesWithIds(nodeIds: Seq[Long]): Seq[NodeDoc] = {
    database.nodes.findByIds(nodeIds, log)
  }

  override def nodeNetworkReferences(nodeId: Long): Seq[Reference] = {
    new MongoQueryNodeNetworkReferences(database).execute(nodeId)
  }

  override def nodeRouteReferences(nodeId: Long): Seq[Reference] = {
    database.routes.aggregate[Reference](routeReferencesPipeline(nodeId))
  }

  override def filterKnown(nodeIds: Set[Long]): Set[Long] = {
    new MongoQueryKnownNodeIds(database).execute(nodeIds.toSeq).toSet
  }

  private def routeReferencesPipeline(nodeId: Long): Seq[Bson] = {
    Seq(
      filter(
        and(
          equal("active", true),
          equal("nodeRefs", nodeId)
        )
      ),
      project(
        fields(
          excludeId(),
          computed("networkType", "$summary.networkType"),
          computed("networkScope", "$summary.networkScope"),
          computed("id", "$summary.id"),
          computed("name", "$summary.name")
        )
      ),
      sort(orderBy(ascending("networkType", "networkScope", "routeName")))
    )
  }
}
