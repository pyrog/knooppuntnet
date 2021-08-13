package kpn.core.database.views.changes

import kpn.api.common.SharedTestObjects
import kpn.core.test.TestSupport.withCouchDatabase
import kpn.core.util.UnitTest
import kpn.server.repository.ChangeSetRepositoryImpl

class ChangeDocumentViewTest extends UnitTest with SharedTestObjects {

  test("view") {

    withCouchDatabase { database =>
      val repo = new ChangeSetRepositoryImpl(null, database, false)

      repo.saveChangeSetSummary(newChangeSetSummary(newChangeKey()))

      repo.saveNetworkInfoChange(newNetworkInfoChange(newChangeKey(elementId = 1)))
      repo.saveNetworkInfoChange(newNetworkInfoChange(newChangeKey(elementId = 2)))

      repo.saveRouteChange(newRouteChange(newChangeKey(elementId = 11)))
      repo.saveRouteChange(newRouteChange(newChangeKey(elementId = 12)))

      repo.saveNodeChange(newNodeChange(newChangeKey(elementId = 1001)))
      repo.saveNodeChange(newNodeChange(newChangeKey(elementId = 1002)))

      ChangeDocumentView.allNetworkIds(database) should equal(Seq(1L, 2L))
      ChangeDocumentView.allRouteIds(database) should equal(Seq(11L, 12L))
      ChangeDocumentView.allNodeIds(database) should equal(Seq(1001L, 1002L))
    }
  }
}
