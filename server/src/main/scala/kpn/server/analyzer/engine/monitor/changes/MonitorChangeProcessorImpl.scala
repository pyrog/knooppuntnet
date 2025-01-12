package kpn.server.analyzer.engine.monitor.changes

import kpn.api.base.ObjectId
import kpn.api.common.Bounds
import kpn.api.common.LatLonImpl
import kpn.api.custom.Relation
import kpn.api.custom.Timestamp
import kpn.core.util.Log
import kpn.server.analyzer.engine.changes.ChangeSetContext
import kpn.server.analyzer.engine.changes.changes.RelationAnalyzerHelper
import kpn.server.analyzer.engine.context.ElementIdMap
import kpn.server.analyzer.engine.monitor.domain.MonitorRouteAnalysis
import kpn.server.analyzer.engine.monitor.domain.MonitorRouteSegmentData
import kpn.server.monitor.domain.MonitorRouteChange
import kpn.server.monitor.domain.MonitorRouteChangeGeometry
import kpn.server.monitor.domain.MonitorRouteReference
import kpn.server.monitor.domain.MonitorRouteState
import kpn.server.monitor.repository.MonitorRouteRepository
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Component

@Component
class MonitorChangeProcessorImpl(
  monitorRouteRepository: MonitorRouteRepository,
  monitorRouteLoader: MonitorRouteLoader,
  monitorChangeImpactAnalyzer: MonitorChangeImpactAnalyzer
) extends MonitorChangeProcessor {

  private val log = Log(classOf[MonitorChangeProcessorImpl])
  private val elementIdMap = ElementIdMap()
  private val geometryFactory = new GeometryFactory
  private val sampleDistanceMeters = 10
  private val toleranceMeters = 10

  // TODO call from AnalyzerEngineImpl.load
  override def load(timestamp: Timestamp): Unit = {
    Log.context(timestamp.yyyymmddhhmmss) {
      Log.context("load") {
        log.info("Start loading monitor routes")
        log.infoElapsed {
          monitorRouteRepository.allRouteIds.foreach { routeId =>
            monitorRouteLoader.loadInitial(timestamp, routeId) match {
              case Some(routeRelation) =>
                val elementIds = RelationAnalyzerHelper.toElementIds(routeRelation)
                elementIdMap.add(routeId, elementIds)
              case None =>
                log.warn(s"Could not load route $routeId")
            }
          }
          (s"Loaded monitor routes", ())
        }
      }
    }
  }

  override def process(changeSetContext: ChangeSetContext): Unit = {
    elementIdMap.foreach { (routeId, elementIds) =>
      if (monitorChangeImpactAnalyzer.hasImpact(changeSetContext.changeSet, routeId, elementIds)) {
        Log.context(routeId.toString) {
          log.infoElapsed {
            processRoute(changeSetContext, routeId)
            ("process route", ())
          }
        }
      }
    }
  }

  private def processRoute(changeSetContext: ChangeSetContext, routeId: Long): Unit = {
    monitorRouteRepository.routeReferenceKey("TODO KEY" + routeId) match {
      case None => log.warn(s"$routeId TODO routeReferenceKey not available ")
      case Some(referenceKey) =>

        val referenceOption = monitorRouteRepository.routeReference(ObjectId("TODO MON") /*, routeId, referenceKey*/ , None)
        monitorRouteLoader.loadBefore(changeSetContext.changeSet.id, changeSetContext.changeSet.timestampBefore, routeId) match {
          case None => log.warn(s"$routeId TODO route did not exist before --> create change ???")
          case Some(beforeRelation) =>

            monitorRouteLoader.loadAfter(changeSetContext.changeSet.id, changeSetContext.changeSet.timestampAfter, routeId) match {
              case None => log.warn(s"$routeId TODO route did not exist anymore after --> delete change ???")
              case Some(afterRelation) =>

                referenceOption match {
                  case None => log.warn(s"$routeId TODO geen reference --> alleen andere changes loggen ???")
                  case Some(reference) =>
                    log.infoElapsed {
                      analyze(
                        changeSetContext,
                        routeId,
                        beforeRelation,
                        afterRelation,
                        reference
                      )
                      ("analyze", ())
                    }
                }
            }
        }
    }
  }

  private def analyze(
    context: ChangeSetContext,
    routeId: Long,
    beforeRelation: Relation,
    afterRelation: Relation,
    reference: MonitorRouteReference
  ): Unit = {

    val beforeRouteSegments = log.infoElapsed {
      ("toRouteSegments before", Seq.empty /* MonitorRouteAnalysisSupport.toRouteSegments(beforeRelation) */ )
    }
    val beforeRouteAnalysis = log.infoElapsed {
      ("analyze change before", analyzeChange(reference, beforeRelation, beforeRouteSegments))
    }

    val afterRouteSegments = log.infoElapsed {
      ("toRouteSegments after", Seq.empty /* MonitorRouteAnalysisSupport.toRouteSegments(afterRelation) */ )
    }
    val afterRouteAnalysis = log.infoElapsed {
      ("analyze change after", analyzeChange(reference, afterRelation, afterRouteSegments))
    }

    val wayIdsBefore = beforeRelation.wayMembers.map(_.way.id).toSet
    val wayIdsAfter = afterRelation.wayMembers.map(_.way.id).toSet

    val wayIdsAdded = (wayIdsAfter -- wayIdsBefore).size
    val wayIdsRemoved = (wayIdsBefore -- wayIdsAfter).size

    val wayIdsUpdated = wayIdsAfter.intersect(wayIdsBefore).count { wayId =>
      val wayBefore = beforeRelation.wayMembers.filter(_.way.id == wayId).head.way
      val wayAfter = afterRelation.wayMembers.filter(_.way.id == wayId).head.way
      val latLonsBefore = wayBefore.nodes.map(node => LatLonImpl(node.latitude, node.longitude))
      val latLonsAfter = wayAfter.nodes.map(node => LatLonImpl(node.latitude, node.longitude))
      !latLonsBefore.equals(latLonsAfter)
    }

    if ((wayIdsAdded + wayIdsRemoved + wayIdsUpdated) == 0) {
      log.info("No geometry changes, no further analysis")
    }
    else {
      val beforeGeoJons = beforeRouteAnalysis.deviations.map(_.geoJson)
      val afterGeoJons = afterRouteAnalysis.deviations.map(_.geoJson)

      val newSegments = afterRouteAnalysis.deviations.filterNot(nokSegment => beforeGeoJons.contains(nokSegment.geoJson))
      val resolvedSegments = beforeRouteAnalysis.deviations.filterNot(nokSegment => afterGeoJons.contains(nokSegment.geoJson))

      val message = s"ways=${afterRouteAnalysis.wayCount} $wayIdsAdded/$wayIdsRemoved/$wayIdsUpdated," ++
        s" osm=${afterRouteAnalysis.osmDistance}," ++
        s" gpx=${afterRouteAnalysis.gpxDistance}," ++
        s" osmSegments=${afterRouteAnalysis.osmSegments.size}," ++
        s" nokSegments=${afterRouteAnalysis.deviations.size}," ++
        s" new=${newSegments.size}," ++
        s" resolved=${resolvedSegments.size}"

      val key = context.buildChangeKey(routeId)

      val routeSegments = if (newSegments.nonEmpty || resolvedSegments.nonEmpty) {
        afterRouteAnalysis.osmSegments
      }
      else {
        Seq.empty
      }

      val change = MonitorRouteChange(
        ObjectId(),
        ObjectId("TODO"), // key.toId,
        key,
        afterRouteAnalysis.wayCount,
        wayIdsAdded,
        wayIdsRemoved,
        wayIdsUpdated,
        afterRouteAnalysis.osmDistance,
        afterRouteAnalysis.osmSegments.size,
        afterRouteAnalysis.deviations.size,
        resolvedSegments.size,
        happy = resolvedSegments.nonEmpty,
        investigate = newSegments.nonEmpty
      )

      monitorRouteRepository.saveRouteChange(change)

      val routeChangeGeometry = MonitorRouteChangeGeometry(
        ObjectId(),
        ObjectId("TODO"), // key.toId,
        key,
        routeSegments,
        newSegments,
        resolvedSegments,
      )
      monitorRouteRepository.saveRouteChangeGeometry(routeChangeGeometry)

      val happy = false
      val routeState = MonitorRouteState(
        ObjectId(),
        null, // TODO routeId,
        1L, // TODO relationId
        afterRouteAnalysis.relation.timestamp,
        afterRouteAnalysis.wayCount,
        afterRouteAnalysis.startNodeId,
        afterRouteAnalysis.endNodeId,
        afterRouteAnalysis.osmDistance,
        afterRouteAnalysis.bounds,
        afterRouteAnalysis.osmSegments,
        afterRouteAnalysis.matchesGeometry,
        afterRouteAnalysis.deviations,
        happy,
      )

      monitorRouteRepository.saveRouteState(routeState)

      log.info(message)
    }
  }

  private def analyzeChange(reference: MonitorRouteReference, routeRelation: Relation, osmRouteSegments: Seq[MonitorRouteSegmentData]): MonitorRouteAnalysis = {
    MonitorRouteAnalysis(
      routeRelation,
      routeRelation.wayMembers.size,
      None, // TODO
      None, // TODO
      0,
      0,
      Bounds(),
      osmRouteSegments.map(_.segment),
      None,
      None,
      Seq.empty,
      Seq.empty
    )
  }
}
