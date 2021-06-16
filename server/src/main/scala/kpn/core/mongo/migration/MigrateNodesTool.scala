package kpn.core.mongo.migration

import kpn.core.db.couch.Couch
import kpn.core.mongo.Database
import kpn.core.mongo.migration.MigrateNodesTool.log
import kpn.core.mongo.util.Mongo
import kpn.core.util.Log
import kpn.server.repository.NodeRepositoryImpl

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MigrateNodesTool {

  private val log = Log(classOf[MigrateNodesTool])

  def main(args: Array[String]): Unit = {
    Mongo.executeIn("kpn-test") { database =>
      Couch.executeIn("kpn-database", "analysis") { couchDatabase =>
        new MigrateNodesTool(couchDatabase, database).migrate()
      }
    }
    log.info("Done")
  }
}

class MigrateNodesTool(couchDatabase: kpn.core.database.Database, database: Database) {

  private val nodeRepository = new NodeRepositoryImpl(null, couchDatabase, false)

  def migrate(): Unit = {
    val allNodeIds = findAllNodeIds()
    val batchSize = 100
    allNodeIds.sliding(batchSize, batchSize).zipWithIndex.foreach { case (nodeIds, index) =>
      log.info(s"${index * batchSize}/${allNodeIds.size}")
      migrateNodes(nodeIds)
    }
  }

  private def findAllNodeIds(): Seq[Long] = {
    log.info("find nodeIds")
    log.elapsed {
      val ids = nodeRepository.allNodeIds()
      (s"${ids.size} nodes", ids)
    }
  }

  private def migrateNodes(nodeIds: Seq[Long]): Unit = {
    val nodeInfos = nodeRepository.nodesWithIds(nodeIds)
    val migratedNodeInfos = nodeInfos.map(nodeInfo => nodeInfo.copy(_id = nodeInfo.id))
    val future = database.nodes.tempCollection.insertMany(migratedNodeInfos).toFuture()
    Await.result(future, Duration(1, TimeUnit.MINUTES))
  }
}