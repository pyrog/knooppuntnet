package kpn.core.replicate

import kpn.api.common.ReplicationId
import kpn.api.common.status.ActionTimestamp
import kpn.api.custom.Timestamp
import kpn.database.base.MetricsDatabaseImpl
import kpn.core.metrics.UpdateAction
import kpn.database.util.Mongo.client
import kpn.database.util.Mongo.codecRegistry
import kpn.core.tools.config.Dirs
import kpn.core.tools.status.StatusRepository
import kpn.core.tools.status.StatusRepositoryImpl
import kpn.core.util.Log
import kpn.server.analyzer.engine.changes.MinuteDiffReader
import kpn.server.analyzer.engine.changes.ReplicationStateReader
import kpn.server.repository.MetricsRepository
import kpn.server.repository.MetricsRepositoryImpl
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.ThreadContext

import java.io.File
import scala.annotation.tailrec

/*
  Updates the Overpass API database from the minute diff files.

    --rootDir=/kpn
    --actionsDatabase=actions

    -Dlog4j.configurationFile=file:///kpn/conf/updater-log4j2.xml
    -Dcom.sun.management.jmxremote.port=5555
    -Dcom.sun.management.jmxremote.authenticate=false
    -Dcom.sun.management.jmxremote.ssl=false
*/
object UpdaterTool {

  private val LOG = Log(classOf[UpdaterTool])

  // maximum number of minute diff files to process in one go
  private val BATCH_SIZE = 10 // 60 * 24

  // number of seconds to wait before attempting to make new request to osm API once in sync
  private val WAIT = 30

  // milliseconds between poll of shutdown flag during sleep
  private val SLEEP_SHUTDOWN_POLL_INTERVAL = 250L

  def main(args: Array[String]): Unit = {

    val exit = try {
      UpdaterToolOptions.parse(args) match {
        case Some(options) =>

          val mongoClient = client
          try {
            val dirs = Dirs()
            val statusRepository = new StatusRepositoryImpl(dirs)
            val replicationStateRepository = new ReplicationStateRepositoryImpl(dirs.replicate)
            val database = new MetricsDatabaseImpl(mongoClient.getDatabase("kpn-metrics").withCodecRegistry(codecRegistry))
            val metricsRepository = new MetricsRepositoryImpl(database)
            val updater = new UpdaterTool(options, statusRepository, metricsRepository, replicationStateRepository)
            updater.launch()
          }
          finally {
            mongoClient.close()
          }
          0

        case None =>
          // arguments are bad, error message will have been displayed
          -1
      }
    }
    catch {
      case e: Exception =>
        LOG.fatal("Unexpected exception", e)
        -1
    }

    System.exit(exit)
  }
}

class UpdaterTool(
  options: UpdaterToolOptions,
  statusRepository: StatusRepository,
  metricsRepository: MetricsRepository,
  replicationStateRepository: ReplicationStateRepository
) {

  import kpn.core.replicate.UpdaterTool._

  private val oper = new Oper()

  def launch(): Unit = {

    assertExists(options.rootDir)

    statusRepository.updaterStatus match {
      case None =>
        LOG.info("Cannot find initial update status")
      case Some(initialReplicationId) =>
        LOG.info("Start processing minute diff files after " + initialReplicationId.name)
        processBatchLoop(initialReplicationId)
    }

    LOG.info("End")
  }

  private def assertExists(file: File): Unit = {
    require(file.exists, s"${file.getAbsolutePath} not found")
  }

  @tailrec
  private def processBatchLoop(previousReplicationId: ReplicationId): Unit = {

    statusRepository.replicatorStatus match {
      case None =>

        LOG.info("Cannot find replicator status")
        sleep(WAIT)

      case Some(maxReplicationId) =>

        FileUtils.cleanDirectory(options.tmpDir)
        val (lastReplicationId, timestampOption) = readBatchAndWriteTempFiles(previousReplicationId, maxReplicationId, 0, None)
        if (previousReplicationId == lastReplicationId) {
          // all files processed, sleep for a while
          sleep(lastReplicationId)
          if (oper.isActive) {
            processBatchLoop(lastReplicationId)
          }
        }
        else {
          if (oper.isActive) {
            val batchSize = lastReplicationId.number - previousReplicationId.next.number + 1
            LOG.info(s"Processing batch ${previousReplicationId.next.name} to ${lastReplicationId.name} [$batchSize]")
            LOG.infoElapsed {
              new OverpassUpdate(options.overpassUpdate, options.tmpDir).update(timestampOption.get)
              (s"${timestampOption.get.yyyymmddhhmmss}", ())
            }
            statusRepository.writeUpdateStatus(lastReplicationId)
            log(previousReplicationId, lastReplicationId)
            if (oper.isActive) {
              processBatchLoop(lastReplicationId)
            }
          }
        }
    }
  }

  private def log(previousReplicationId: ReplicationId, lastReplicationId: ReplicationId): Unit = {
    (previousReplicationId.next.number to lastReplicationId.number) foreach { id =>
      val replicationId = ReplicationId(id)
      val timestamp = replicationStateRepository.read(replicationId)
      val minuteDiffInfo = ActionTimestamp.minuteDiffInfo(id, timestamp)
      metricsRepository.saveUpdateAction(UpdateAction(minuteDiffInfo))
    }
  }

  @tailrec
  private def readBatchAndWriteTempFiles(previousReplicationId: ReplicationId, maxReplicationId: ReplicationId, filesInBatchCount: Int, timestamp: Option[Timestamp]): (ReplicationId, Option[Timestamp]) = {
    val replicationId = previousReplicationId.next
    if (replicationId.number <= maxReplicationId.number && filesInBatchCount < UpdaterTool.BATCH_SIZE) {
      ThreadContext.push(replicationId.name)
      minuteDiff(replicationId) match {
        case Some(diff) =>
          FileUtils.writeStringToFile(new File(options.tmpDir, s"${diff.replicationId.number}.xml"), diff.xml, "UTF-8")
          ThreadContext.pop()
          readBatchAndWriteTempFiles(replicationId, maxReplicationId, filesInBatchCount + 1, Some(diff.timestamp))
        case None =>
          ThreadContext.pop()
          (previousReplicationId, timestamp)
      }
    }
    else {
      (previousReplicationId, timestamp)
    }
  }

  private def sleep(replicationId: ReplicationId): Option[ReplicationId] = {
    if (oper.isActive) {
      sleep(WAIT)
      if (oper.isActive) {
        Some(replicationId)
      }
      else {
        None
      }
    }
    else {
      None
    }
  }

  private def sleep(seconds: Int): Unit = {
    LOG.info(s"Waiting ${seconds}s")
    val end = System.currentTimeMillis() + (seconds * 1000)
    while (oper.isActive && System.currentTimeMillis() < end) {
      Thread.sleep(SLEEP_SHUTDOWN_POLL_INTERVAL)
    }
    LOG.info(s"End waiting ${seconds}s")
  }

  private def minuteDiff(replicationId: ReplicationId): Option[MinuteDiff] = {
    new ReplicationStateReader(options.replicateDir).readTimestamp(replicationId) match {
      case Some(timestamp) =>
        new MinuteDiffReader(options.replicateDir).read(replicationId) match {
          case Some(xml) => Some(MinuteDiff(replicationId, timestamp, xml))
          case None => None
        }
      case None => None
    }
  }
}
