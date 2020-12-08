package kpn.core.tools.longdistance

import kpn.api.common.ReplicationId
import kpn.api.common.data.raw.RawNode
import kpn.api.common.data.raw.RawWay
import kpn.api.custom.Relation
import kpn.api.custom.Timestamp
import kpn.core.data.DataBuilder
import kpn.core.loadOld.Parser
import kpn.core.overpass.OverpassQueryExecutor
import kpn.core.overpass.OverpassQueryExecutorImpl
import kpn.core.overpass.QueryRelation
import kpn.core.tools.longdistance.LongDistanceRouteAnalyzer.toRouteSegments
import kpn.core.util.Log
import kpn.server.analyzer.engine.changes.ChangeSetContext
import kpn.server.analyzer.engine.changes.OsmChangeRepository
import kpn.server.analyzer.engine.changes.changes.ChangeSetBuilder
import org.apache.commons.io.FileUtils

import java.io.File
import java.nio.charset.Charset
import scala.xml.XML

object LongDistanceRouteAnalyzerTool {
  def main(args: Array[String]): Unit = {
    val executor = new OverpassQueryExecutorImpl()
    //    new LongDistanceRouteAnalyzerTool(executor).collectFiles()
    new LongDistanceRouteAnalyzerTool(executor).analyze()
  }
}

class LongDistanceRouteAnalyzerTool(overpassQueryExecutor: OverpassQueryExecutor) {

  private val routeId = 3121667L
  private val log = Log(classOf[LongDistanceRouteAnalyzerTool])
  private val osmChangeRepository = new OsmChangeRepository(new File("/kpn/replicate"))

  var wayIds: Set[Long] = Set()
  var nodeIds: Set[Long] = Set()

  def collectFiles(): Unit = {

    val begin = ReplicationId(4, 131, 462)
    val end = ReplicationId(4, 320, 1)

    val timestamp = osmChangeRepository.timestamp(begin)
    val xmlString = writeXml(s"/kpn/wrk/begin/$routeId.xml", timestamp)
    updateNodeAndWayIds(xmlString)

    ReplicationId.range(begin, end) foreach { replicationId =>
      Log.context(s"${replicationId.name}") {
        val osmChange = osmChangeRepository.get(replicationId)
        val timestamp = osmChangeRepository.timestamp(replicationId)
        log.info(timestamp.yyyymmddhhmmss)

        val changeSets = ChangeSetBuilder.from(timestamp, osmChange)
        changeSets.foreach { changeSet =>
          val impacted = changeSet.changes.exists { change =>
            change.elements.exists {
              case node: RawNode => nodeIds.contains(node.id)
              case way: RawWay => wayIds.contains(way.id)
              case _ => false
            }
          }
          if (impacted) {
            log.info("Writing route files")
            val context = ChangeSetContext(replicationId, changeSet)
            val dir = s"/kpn/wrk/${changeSet.id}"
            new File(dir).mkdir()
            writeXml(s"$dir/$routeId-before.xml", context.timestampBefore)
            val xmlString = writeXml(s"$dir/$routeId-after.xml", context.timestampAfter)
            updateNodeAndWayIds(xmlString)
          }
        }
      }
    }
    log.info("Done")
  }

  def analyze(): Unit = {

    val referenceRelation = readRelation(s"/kpn/wrk/begin/$routeId.xml")
    println(s"BEGIN ${toRouteSegments(referenceRelation).size}")

    val files = new File("/kpn/wrk").list().filterNot(_ == "begin").toSeq.sorted
    files.zipWithIndex.foreach { case (changeSetId, index) =>
      //      val beforeRelation = readRelation(s"/kpn/wrk/$changeSetId/$routeId-before.xml")

      println(s"${index + 1} $changeSetId")

      val afterRelation = readRelation(s"/kpn/wrk/$changeSetId/$routeId-after.xml")
      println(s"${index + 1} ${toRouteSegments(afterRelation).size}")
    }
  }

  private def writeXml(filename: String, timestamp: Timestamp): String = {
    val xml = overpassQueryExecutor.executeQuery(Some(timestamp), QueryRelation(routeId))
    FileUtils.writeStringToFile(new File(filename), xml, Charset.forName("UTF-8"))
    xml
  }

  private def updateNodeAndWayIds(xmlString: String): Unit = {
    val xml = XML.loadString(xmlString)
    val rawData = new Parser().parse(xml.head)
    val relation = new DataBuilder(rawData).data.relations(routeId)
    wayIds = relation.wayMembers.map(_.way.id).toSet
    nodeIds = relation.wayMembers.flatMap(_.way.nodes.map(_.id)).toSet
  }

  private def readRelation(filename: String): Relation = {
    val xmlString = FileUtils.readFileToString(new File(filename), Charset.forName("UTF-8"))
    val xml = XML.loadString(xmlString)
    val rawData = new Parser().parse(xml.head)
    new DataBuilder(rawData).data.relations(routeId)
  }

}
