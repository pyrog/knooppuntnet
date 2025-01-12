package kpn.server.analyzer.engine.analysis.location

import kpn.api.common.RouteLocationAnalysis
import kpn.api.common.common.TrackSegment
import kpn.api.common.location.LocationCandidate
import kpn.api.common.route.RouteMap
import kpn.core.util.Haversine
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.MultiLineString
import org.springframework.stereotype.Component

@Component
class RouteLocatorImpl(locationAnalyzer: LocationAnalyzer) extends RouteLocator {

  private val geometryFactory = new GeometryFactory

  def locate(routeMap: RouteMap): RouteLocationAnalysis = {

    val routeGeometries: Seq[Geometry] = toGeometries(routeMap)

    val candidates: Seq[LocationSelector] = locationAnalyzer.locateGeometries(routeGeometries)

    val locationSelectorCandidates = candidates.map { candidate =>
      val distance = calculateDistance(routeGeometries, candidate)
      LocationSelectorCandidate(candidate, distance)
    }

    if (locationSelectorCandidates.isEmpty) {
      RouteLocationAnalysis(None, Seq.empty, Seq.empty)
    }
    else {
      val sorted = locationSelectorCandidates.sortBy(_.distance).reverse

      val totalDistance = locationSelectorCandidates.map(_.distance).sum

      val locationCandidates = sorted.map { locationSelectorCandidate =>
        val percentage = Math.round(100d * locationSelectorCandidate.distance / totalDistance).toInt
        LocationCandidate(locationSelectorCandidate.selector.toLocation, percentage)
      }

      val locationNames = locationCandidates.flatMap(_.location.names).sorted.distinct

      RouteLocationAnalysis(
        Some(sorted.head.selector.toLocation),
        locationCandidates,
        locationNames
      )
    }
  }

  private def toGeometries(routeMap: RouteMap): Seq[Geometry] = {
    toSegments(routeMap).flatMap { segment: TrackSegment =>
      val coordinates = (segment.source +: segment.fragments.map(_.trackPoint)).map { trackPoint =>
        val lat = trackPoint.lat.toDouble
        val lon = trackPoint.lon.toDouble
        new Coordinate(lon, lat)
      }
      if (coordinates.size > 1) {
        Some(geometryFactory.createLineString(coordinates.toArray))
      }
      else {
        None
      }
    }
  }

  private def lineLength(geometry: Geometry): Double = {
    geometry match {
      case lineString: LineString => Haversine.meters(lineString)
      case multiLineString: MultiLineString =>
        0.until(multiLineString.getNumGeometries).map { index =>
          Haversine.meters(multiLineString.getGeometryN(index).asInstanceOf[LineString])
        }.sum

      case _ => 0d
    }
  }

  private def toSegments(routeMap: RouteMap): Seq[TrackSegment] = {
    Seq(
      routeMap.forwardPath.toSeq.flatMap(_.segments),
      routeMap.backwardPath.toSeq.flatMap(_.segments),
      routeMap.unusedSegments,
      routeMap.startTentaclePaths.flatMap(_.segments),
      routeMap.endTentaclePaths.flatMap(_.segments),
      routeMap.freePaths.flatMap(_.segments)
    ).flatten
  }

  private def calculateDistance(routeGeometries: Seq[Geometry], location: LocationSelector): Double = {
    routeGeometries.map(_.intersection(location.leaf.geometry.geometry)).map(lineLength).sum
  }
}
