package kpn.core.changes

import kpn.shared.Timestamp
import kpn.shared.changes.Change

/**
 * Corresponds to the contents of a minute diff file.
 */
case class OsmChange(actions: Seq[Change]) {

  def allChangeSetIds: Set[Long] = actions.flatMap(_.elements.map(_.changeSetId)).toSet

  // this logic assumes there is at least 1 action with at least 1 element
  def timestampFrom: Option[Timestamp] = {
    if(actions.nonEmpty) {
      Some(actions.flatMap(_.elements.map(_.timestamp)).min)
    }
    else {
      None
    }
  }

  def timestampUntil: Option[Timestamp] = {
    if(actions.nonEmpty) {
      Some(actions.flatMap(_.elements.map(_.timestamp)).max)
    }
    else {
      None
    }
  }
}
