package kpn.server.analyzer.engine.monitor.changes

import kpn.api.common.changes.ChangeSet
import kpn.core.tools.config.Dirs
import kpn.server.analyzer.engine.context.ElementIds

import java.io.File

class MonitorChangeImpactAnalyzerFileImpl extends MonitorChangeImpactAnalyzer {

  override def hasImpact(changeSet: ChangeSet, routeId: Long, elementIds: ElementIds): Boolean = {
    new File(Dirs.root, s"wrk/${changeSet.id}/$routeId-after.xml").exists()
  }
}
