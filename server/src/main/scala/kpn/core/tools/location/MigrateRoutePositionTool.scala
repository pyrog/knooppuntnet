package kpn.core.tools.location

import kpn.api.common.common.TrackPath
import kpn.api.common.common.TrackPoint
import kpn.api.common.common.TrackSegment
import kpn.core.doc.GeoLineString
import kpn.core.doc.RouteDoc
import kpn.core.util.Geo
import kpn.database.base.Database
import kpn.database.util.Mongo

object MigrateRoutePositionTool {
  def main(args: Array[String]): Unit = {
    Mongo.executeIn("kpn-next") { database =>
      new MigrateRoutePositionTool(database).migrate()
    }
  }
}

class MigrateRoutePositionTool(database: Database) {
  def migrate(): Unit = {
    val routeIds = database.routes.ids()
    println(s"${routeIds.size} routes")
    routeIds.zipWithIndex.foreach { case (routeId, index) =>
      if (index % 100 == 0) {
        println(s"$index/${routeIds.size}")
      }
      database.routes.findById(routeId).foreach { routeDoc =>
        database.routes.save(
          migrateRoute(routeDoc)
        )
      }
    }
  }

  private def migrateRoute(route: RouteDoc): RouteDoc = {
    val map = route.analysis.map

    val geoFreePaths = if (map.freePaths.nonEmpty) {
      Some(
        map.freePaths.map(trackPathToLineString)
      )
    } else {
      None
    }

    val geoForwardPath = map.forwardPath.map(trackPathToLineString)

    val geoBackwardPath = map.backwardPath.map(trackPathToLineString)

    val geoUnusedSegments = if (map.unusedSegments.nonEmpty) {

      Some(
        map.unusedSegments.map(segmentToLineString)
      )
    }
    else {
      None
    }

    val geoStartTentaclePaths = if (map.startTentaclePaths.nonEmpty) {
      Some(
        map.startTentaclePaths.map(trackPathToLineString)
      )
    } else {
      None
    }

    val geoEndTentaclePaths = if (map.endTentaclePaths.nonEmpty) {
      Some(
        map.endTentaclePaths.map(trackPathToLineString)
      )
    } else {
      None
    }

    route.copy(
      geoFreePaths = geoFreePaths,
      geoForwardPath = geoForwardPath,
      geoBackwardPath = geoBackwardPath,
      geoUnusedSegments = geoUnusedSegments,
      geoStartTentaclePaths = geoStartTentaclePaths,
      geoEndTentaclePaths = geoEndTentaclePaths,
    )
  }

  private def trackPathToLineString(path: TrackPath): GeoLineString = {
    trackPointsToLineString(path.trackPoints)
  }

  private def segmentToLineString(segment: TrackSegment): GeoLineString = {
    trackPointsToLineString(segment.trackPoints)
  }

  private def trackPointsToLineString(trackPoints: Seq[TrackPoint]): GeoLineString = {
    val coordinates = trackPoints.map(tp => Seq(tp.lon.toDouble, tp.lat.toDouble))
    Geo.lineString(coordinates)
  }
}
