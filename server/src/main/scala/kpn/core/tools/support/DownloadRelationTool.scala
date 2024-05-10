package kpn.core.tools.support

import kpn.api.custom.Timestamp
import kpn.core.overpass.OverpassQueryExecutor
import kpn.core.overpass.OverpassQueryExecutorRemoteImpl
import kpn.core.overpass.QueryRelation
import org.apache.commons.io.FileUtils

import java.io.File
import java.nio.charset.Charset

object DownloadRelationTool {

  private val routes = Seq(
    "route" -> 17574316,
  )

  def main(args: Array[String]): Unit = {
    val overpassQueryExecutor = new OverpassQueryExecutorRemoteImpl()
    new DownloadRelationTool(overpassQueryExecutor).download()
    println("done")
  }
}

class DownloadRelationTool(overpassQueryExecutor: OverpassQueryExecutor) {

  def download(): Unit = {
    DownloadRelationTool.routes.foreach { case (name, relationId) =>
      println(s"downloading $name")
      val xmlString = overpassQueryExecutor.executeQuery(Some(Timestamp(2024, 5, 8, 20, 0, 0)), QueryRelation(relationId))
      val filename = new File(s"/Users/marc/tmp/xml/$relationId.xml")
      FileUtils.writeStringToFile(filename, xmlString, Charset.forName("UTF-8"))
    }
  }
}
