package kpn.core.tools.monitor

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString

import java.io.File
import scala.xml.Elem
import scala.xml.XML

class MonitorRouteGpxReader {

  private val geomFactory = new GeometryFactory

  def readFile(filename: String): LineString = {
    read(XML.loadFile(new File(filename)))
  }

  def readString(xmlString: String): LineString = {
    read(XML.loadString(xmlString))
  }

  def read(xml: Elem): LineString = {

    val tracks = (xml \ "trk").map { trk =>
      (trk \ "trkseg").map { trkseg =>
        val coordinates = (trkseg \ "trkpt").map { trkpt =>
          val lat = (trkpt \ "@lat").text
          val lon = (trkpt \ "@lon").text
          new Coordinate(lon.toDouble, lat.toDouble)
        }
        geomFactory.createLineString(coordinates.toArray)
      }
    }
    if (tracks.size != 1) {
      throw new RuntimeException("Unexpected number of tracks in gpx file: " + tracks.size)
    }
    val track = tracks.head
    if (track.size != 1) {
      throw new RuntimeException("Unexpected number of track segments in gpx file: " + track.size)
    }
    track.head
  }

}
