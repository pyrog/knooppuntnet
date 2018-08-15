package kpn.core

import kpn.core.engine.changes.ChangeSetContext
import kpn.shared.ReplicationId
import kpn.shared.SharedTestObjects

trait TestObjects extends SharedTestObjects {

  def newChangeSetContext(): ChangeSetContext = {
    ChangeSetContext(ReplicationId(1), newChangeSet())
  }
}
