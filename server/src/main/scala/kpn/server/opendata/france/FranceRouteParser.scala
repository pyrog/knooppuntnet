package kpn.server.opendata.france

import kpn.server.opendata.common.OpenDataRoute
import mil.nga.geopackage.GeoPackageManager
import mil.nga.geopackage.features.user.FeatureRow
import mil.nga.sf.GeometryType
import mil.nga.sf.LineString

import java.io.File
import scala.jdk.CollectionConverters.IterableHasAsScala

class FranceRouteParser {
  def read(): Seq[OpenDataRoute] = {
    read(new File("/Users/marc/kpn/opendata/france/itineraires-rando-parc-du-vercors.gpkg"))
  }

  private def read(geopackageFile: File): Seq[OpenDataRoute] = {
    val rows = readRows(geopackageFile)
    rows.filterNot(horsReseau).flatMap { row =>
      val fid = row.getValue("fid")
      val geometry = row.getGeometry.getGeometry
      if (geometry != null && geometry.getGeometryType == GeometryType.LINESTRING) {
        val lineString = geometry.asInstanceOf[LineString]
        val points = lineString.getPoints.asScala.toSeq
        val coordinates = points.map { point =>
          FranceUtil.lambertToLatLon(point.getX, point.getY)
        }
        Some(OpenDataRoute(fid.toString, coordinates))
      }
      else {
        None
      }
    }
  }

  private def readRows(geopackageFile: File): Seq[FeatureRow] = {
    val geoPackage = GeoPackageManager.open(geopackageFile)
    val featureTable = geoPackage.getFeatureTables.get(0)
    val featureDao = geoPackage.getFeatureDao(featureTable)
    val featureResultSet = featureDao.query(Array("fid", "iti_nom", "geom"))
    try {
      featureResultSet.asScala.toSeq
    } finally {
      featureResultSet.close()
    }
  }

  private def horsReseau(row: FeatureRow): Boolean = {
    FranceUtil.routeNames(row) != Seq("hr")
  }
}
