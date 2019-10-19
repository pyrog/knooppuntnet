package kpn.core.db.views

import kpn.core.db.TestDocBuilder
import kpn.core.db.couch.Couch
import kpn.core.db.views.FactView.FactViewKey
import kpn.core.test.TestSupport.withOldDatabase
import kpn.shared.Country
import kpn.shared.Fact
import kpn.shared.NetworkType
import kpn.shared.Subset
import kpn.shared.data.Tags
import org.scalatest.FunSuite
import org.scalatest.Matchers

class FactViewTest extends FunSuite with Matchers {

  test("rows") {

    withOldDatabase { database =>

      val networkId = 5L

      new TestDocBuilder(database) {
        val detail = Some(
          networkInfoDetail(
            nodes = Seq(
              newNetworkNodeInfo2(
                1001L,
                "01",
                facts = Seq(
                  Fact.NodeMemberMissing
                )
              )
            ),
            routes = Seq(
              networkRouteInfo(
                10L,
                facts = Seq(
                  Fact.RouteBroken,
                  Fact.RouteNameMissing
                )
              )
            )
          )
        )
        network(
          networkId,
          Subset.nlHiking,
          "network-name",
          facts = Seq(
            Fact.NameMissing,
            Fact.NetworkExtraMemberNode
          ),
          detail = detail
        )
      }

      val rows = database.query(AnalyzerDesign, FactView, Couch.uiTimeout, stale = false)().map(FactView.convert)

      rows should equal(
        Seq(
          FactViewKey("nl", "rwn", "NameMissing", "network-name", networkId),
          FactViewKey("nl", "rwn", "NetworkExtraMemberNode", "network-name", networkId),
          FactViewKey("nl", "rwn", "NodeMemberMissing", "network-name", networkId),
          FactViewKey("nl", "rwn", "RouteBroken", "network-name", networkId),
          FactViewKey("nl", "rwn", "RouteNameMissing", "network-name", networkId)
        )
      )
    }
  }

  test("orphan route") {

    withOldDatabase { database =>

      new TestDocBuilder(database) {
        route(
          11,
          Subset.nlHiking,
          orphan = true,
          facts = Seq(Fact.RouteBroken)
        )
      }

      val rows = database.query(AnalyzerDesign, FactView, Couch.uiTimeout, stale = false)().map(FactView.convert)

      rows should equal(
        Seq(
          FactViewKey("nl", "rwn", "RouteBroken", "OrphanRoutes", 0)
        )
      )
    }
  }

  test("orphan node rcn") {
    orphanNodeTest(NetworkType.bicycle)
    orphanNodeTest(NetworkType.hiking)
    orphanNodeTest(NetworkType.horseRiding)
    orphanNodeTest(NetworkType.motorboat)
    orphanNodeTest(NetworkType.canoe)
    orphanNodeTest(NetworkType.inlineSkates)
  }

  private def orphanNodeTest(networkType: NetworkType): Unit = {

    withOldDatabase { database =>

      new TestDocBuilder(database) {
        node(
          11,
          Country.nl,
          tags = Tags.from(networkType.nodeTagKey -> "01"),
          orphan = true,
          facts = Seq(Fact.IntegrityCheck)
        )
      }

      val rows = database.query(AnalyzerDesign, FactView, Couch.uiTimeout, stale = false)().map(FactView.convert)

      rows should equal(
        Seq(
          FactViewKey("nl", networkType.name, "IntegrityCheck", "OrphanNodes", 0)
        )
      )
    }
  }
}
