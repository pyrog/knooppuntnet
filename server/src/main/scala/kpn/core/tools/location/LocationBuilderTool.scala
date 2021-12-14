package kpn.core.tools.location

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

  private val root = "/kpn/locations"
  private val boundaryVersion = "osm-boundaries-2021-11-01"

  def build(): Unit = {
    build("be", "belgium", locationsBelgium())
    build("nl", "netherlands", locationsNetherlands())
    build("de", "germany", locationsGermany())
    // build("fr", "france", locationsFrance())
    // build("at", "austria", locationsAustria())
    // build("es", "spain", locationsSpain())
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

  private def build(country: String, countryName: String, locationDatas: Seq[LocationData]): Unit = {
    val tree = buildTree(locationDatas)
    saveLocations(s"$root/$country", locationDatas)
    saveGeometries(s"$root/$country", locationDatas)
    prettyWrite(s"$root/$country/tree.json", tree)
    printTree(locationDatas.map(_.doc), tree, s"$root/$countryName.md")
    if (country == "fr") {
      new LocationImageWriter(locationDatas).printFrance(tree, s"$root/$countryName.png")
    }
    else {
      new LocationImageWriter(locationDatas).printCountry(tree, s"$root/$countryName.png")
    }
  }

  private def buildTree(datas: Seq[LocationData]): LocationTree = {
    new NewLocationTreeBuilder().buildTree(datas.map(_.doc))
  }

  private def saveLocations(dir: String, datas: Seq[LocationData]): Unit = {
    prettyWrite(s"$dir/locations.json", LocationDocs(datas.map(_.doc)))
  }

  private def saveGeometries(dir: String, datas: Seq[LocationData]): Unit = {
    datas.foreach { data =>
      write(s"$dir/geometries/${data.id}.json", data.geometry.geometry)
    }
  }

  private def write(filename: String, obj: Object): Unit = {
    FileUtils.writeStringToFile(new File(filename), Json.objectMapper.writeValueAsString(obj), "UTF-8")
  }

  private def prettyWrite(filename: String, obj: Object): Unit = {
    val json = Json.objectMapper.writerWithDefaultPrettyPrinter()
    FileUtils.writeStringToFile(new File(filename), json.writeValueAsString(obj), "UTF-8")
  }

  private def printTree(locations: Seq[LocationDoc], tree: LocationTree, filename: String): Unit = {
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
