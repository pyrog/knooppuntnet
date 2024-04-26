package kpn.core.tools.location

import kpn.core.tools.config.Dirs
import kpn.server.analyzer.engine.analysis.location.LocationTree
import kpn.server.json.Json
import org.apache.commons.io.FileUtils

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

object LocationBuilderTool {
  def main(args: Array[String]): Unit = {
    new LocationBuilderTool().build()
  }
}

class LocationBuilderTool {

  private val root = s"${Dirs.root}/locations"
  private val boundaryVersion = "osm-boundaries-2021-11-01"
  private val boundaryVersion2 = "osm-boundaries-2023-06-05"

  def build(): Unit = {
    build("be", "belgium", locationsBelgium())
    build("nl", "netherlands", locationsNetherlands())
    build("de", "germany", locationsGermany())
    build("fr", "france", locationsFrance())
    build("at", "austria", locationsAustria())
    build("es", "spain", locationsSpain())
    build("dk", "denmark", locationsDenmark())
  }

  private def build(country: String, countryName: String, locationDatas: Seq[LocationData]): Unit = {
    val tree = buildTree(locationDatas)
    saveLocations(country, locationDatas)
    saveGeometries(country, locationDatas)
    prettyWrite(s"$root/$country/tree.json", tree)
    printTree(locationDatas, tree, countryName)
    if (country == "fr") {
      new LocationImageWriter(locationDatas).printFrance(tree, s"$root/$countryName.png")
    }
    else {
      new LocationImageWriter(locationDatas).printCountry(tree, s"$root/$countryName.png")
    }
  }

  private def locationsBelgium(): Seq[LocationData] = {
    new LocationBuilderBelgium(s"$root/$boundaryVersion").build()
  }

  private def locationsNetherlands(): Seq[LocationData] = {
    new LocationBuilderNetherlands(s"$root/$boundaryVersion").build()
  }

  private def locationsGermany(): Seq[LocationData] = {
    new LocationBuilderGermany(s"$root/$boundaryVersion").build()
  }

  private def locationsFrance(): Seq[LocationData] = {
    new LocationBuilderFrance(s"$root/$boundaryVersion").build()
  }

  private def locationsAustria(): Seq[LocationData] = {
    new LocationBuilderAustria(s"$root/$boundaryVersion").build()
  }

  private def locationsSpain(): Seq[LocationData] = {
    new LocationBuilderSpain(s"$root/$boundaryVersion").build()
  }

  private def locationsDenmark(): Seq[LocationData] = {
    new LocationBuilderDenmark(s"$root/$boundaryVersion2").build()
  }

  private def buildTree(datas: Seq[LocationData]): LocationTree = {
    new NewLocationTreeBuilder().buildTree(datas)
  }

  private def saveLocations(country: String, datas: Seq[LocationData]): Unit = {
    val filename = s"$root/$country/locations.json"
    val definitions = LocationNameDefinitions(datas.map(_.toLocationNameDefinition))
    prettyWrite(filename, definitions)
  }

  private def saveGeometries(country: String, datas: Seq[LocationData]): Unit = {
    datas.foreach { data =>
      val filename = s"$root/$country/geometries/${data.id}.json"
      val geoJson = Json.objectMapper.writeValueAsString(data.geometry.geometry)
      FileUtils.writeStringToFile(new File(filename), geoJson, "UTF-8")
    }
  }

  private def prettyWrite(filename: String, obj: Object): Unit = {
    val json = Json.objectMapper.writerWithDefaultPrettyPrinter()
    FileUtils.writeStringToFile(new File(filename), json.writeValueAsString(obj), "UTF-8")
  }

  private def printTree(locations: Seq[LocationData], tree: LocationTree, countryName: String): Unit = {
    val filename = s"$root/$countryName.md"
    val fw = new FileWriter(new File(filename))
    val out = new PrintWriter(fw)
    try {
      val printer = new LocationTreePrinter(out)
      printer.printTree(locations, tree)
    }
    finally {
      out.close()
    }
  }
}
