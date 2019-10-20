package kpn.core.tools

import kpn.core.db.couch.Couch
import kpn.core.db.couch.OldDatabase
import kpn.core.db.couch.DesignDoc
import kpn.core.db.couch.ViewDoc
import kpn.core.db.json.JsonFormats.designDocFormat
import kpn.core.db.views.AnalyzerDesign
import kpn.core.db.views.ChangesDesign
import kpn.core.db.views.Design
import kpn.core.db.views.LocationDesign
import kpn.core.db.views.PlannerDesign
import kpn.core.db.views.PoiDesign
import kpn.core.util.Util

/*
 * Saves our couchdb view definitions in the database.
 */
object DatabaseViewTool {

  def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      println("Usage: DatabaseViewTool host masterDbName changesDbName poisDbName")
      System.exit(-1)
    }
    val host = args(0)
    val masterDbName = args(1)
    val changesDbName = args(2)
    val poisDbName = args(3)

    Couch.oldExecuteIn(host, masterDbName) { database =>
      updateView(database, AnalyzerDesign)
      updateView(database, PlannerDesign)
      updateView(database, LocationDesign)
    }

    Couch.oldExecuteIn(host, changesDbName) { database =>
      updateView(database, ChangesDesign)
    }

    Couch.oldExecuteIn(host, poisDbName) { database =>
      updateView(database, PoiDesign)
    }
    println("Ready")
  }

  private def updateView(database: OldDatabase, design: Design): Unit = {
    val views = design.views.map(v => v.name -> ViewDoc(v.map, v.reduce)).toMap
    val id = "_design/" + Util.classNameOf(design)
    val rev = database.currentRevision(id, Couch.batchTimeout)
    val designDoc = DesignDoc(id, rev, "javascript", views)
    database.authorizedSsave(id, designDocFormat.write(designDoc))
  }
}
