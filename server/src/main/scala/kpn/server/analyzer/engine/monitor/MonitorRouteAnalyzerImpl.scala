package kpn.server.analyzer.engine.monitor

import kpn.api.common.Bounds
import kpn.api.custom.Relation
import kpn.api.custom.Timestamp
import kpn.core.common.Time
import kpn.core.data.DataBuilder
import kpn.core.loadOld.Parser
import kpn.core.overpass.OverpassQueryExecutor
import kpn.core.overpass.QueryRelation
import kpn.core.tools.monitor.MonitorDemoAnalyzer
import kpn.core.tools.monitor.MonitorRouteGpxReader
import kpn.server.api.monitor.domain.MonitorRoute
import kpn.server.api.monitor.domain.MonitorRouteReference
import kpn.server.repository.MonitorRouteRepository
import org.locationtech.jts.geom.Geometry
import org.springframework.stereotype.Component

import scala.xml.Elem
import scala.xml.XML

@Component
class MonitorRouteAnalyzerImpl(
  monitorRouteRepository: MonitorRouteRepository,
  overpassQueryExecutor: OverpassQueryExecutor
) extends MonitorRouteAnalyzer {

  override def processNewReference(user: String, routeDocId: String, filename: String, xml: Elem): Unit = {

    monitorRouteRepository.route(routeDocId) match {
      case None => // TODO throw exception ?
      case Some(monitorRoute) =>
        val now = Time.now

        val geometry = new MonitorRouteGpxReader().read(xml)
        val bounds = geometryBounds(geometry)
        val geoJson = MonitorRouteAnalysisSupport.toGeoJson(geometry)
        val id = s"$routeDocId:${now.key}"

        val reference = MonitorRouteReference(
          id,
          monitorRouteId = routeDocId,
          routeId = monitorRoute.routeId,
          key = now.key,
          created = now,
          user = user,
          bounds = bounds,
          referenceType = "gpx", // "osm" | "gpx"
          referenceTimestamp = Some(now),
          segmentCount = 1, // number of tracks in gpx always 1, multiple track not supported yet
          filename = Some(filename),
          geometry = geoJson
        )

        monitorRouteRepository.saveRouteReference(reference)

        updateRoute(monitorRoute, reference, now)
    }
  }

  private def geometryBounds(geometry: Geometry): Bounds = {
    val envelope = geometry.getEnvelopeInternal
    Bounds(
      envelope.getMinY, // minLat
      envelope.getMinX, // minLon
      envelope.getMaxY, // maxLat
      envelope.getMaxX, // maxLon
    )
  }

  private def updateRoute(route: MonitorRoute, reference: MonitorRouteReference, now: Timestamp): Unit = {
    val routeRelation = readRelation(now, route.routeId)
    val monitorRouteState = new MonitorDemoAnalyzer().analyze(reference, routeRelation, now)
    monitorRouteRepository.saveRouteState(monitorRouteState)
  }

  private def readRelation(now: Timestamp, routeId: Long): Relation = {
    val xmlString = overpassQueryExecutor.executeQuery(Some(now), QueryRelation(routeId))
    val xml = XML.loadString(xmlString)
    val rawData = new Parser().parse(xml.head)
    val data = new DataBuilder(rawData).data
    data.relations(routeId)
  }

}