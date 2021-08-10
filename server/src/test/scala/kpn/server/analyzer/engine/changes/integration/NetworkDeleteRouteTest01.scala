package kpn.server.analyzer.engine.changes.integration

import kpn.api.common.ChangeSetSubsetAnalysis
import kpn.api.common.NetworkChanges
import kpn.api.common.changes.ChangeAction
import kpn.api.common.changes.details.ChangeType
import kpn.api.common.changes.details.RefChanges
import kpn.api.common.common.Ref
import kpn.api.common.data.raw.RawMember
import kpn.api.common.network.NetworkInfo
import kpn.api.custom.Country
import kpn.api.custom.Fact
import kpn.api.custom.NetworkType
import kpn.api.custom.Subset
import kpn.api.custom.Tags
import kpn.core.test.OverpassData
import kpn.core.test.TestSupport.withDatabase
import kpn.server.analyzer.engine.changes.changes.RelationAnalyzer

class NetworkDeleteRouteTest01 extends AbstractIntegrationTest {

  test("network delete - route becomes orphan") {

    pending

    withDatabase { database =>

      val dataBefore = OverpassData()
        .networkNode(1001, "01")
        .networkNode(1002, "02")
        .way(101, 1001, 1002)
        .route(11, "01-02", Seq(newMember("way", 101)))
        .networkRelation(1, "network", Seq(newMember("relation", 11)))

      val dataAfter = OverpassData()
        .networkNode(1001, "01")
        .networkNode(1002, "02")
        .way(101, 1001, 1002)
        .route(11, "01-02", Seq(newMember("way", 101)))

      val tc = new IntegrationTestContext(database, dataBefore, dataAfter)

      tc.analysisContext.data.networks.watched.add(1, RelationAnalyzer.toElementIds(tc.beforeRelationWithId(1)))

      tc.process(ChangeAction.Delete, newRawRelation(1))

      assert(!tc.analysisContext.data.networks.watched.contains(1))
      assert(tc.analysisContext.data.routes.watched.contains(11))

      (tc.networkRepository.oldSaveNetworkInfo _).verify(
        where { networkInfo: NetworkInfo =>
          networkInfo should matchTo(
            newNetworkInfo(
              newNetworkAttributes(
                1,
                Some(Country.nl),
                NetworkType.hiking,
                name = "network",
                lastUpdated = timestampAfterValue,
                relationLastUpdated = timestampAfterValue
              ),
              active = false // <--- !!!
            )
          )
          true
        }
      )

      tc.findChangeSetSummaryById("123:1") should matchTo(
        newChangeSetSummary(
          subsets = Seq(Subset.nlHiking),
          networkChanges = NetworkChanges(
            deletes = Seq(
              newChangeSetNetwork(
                Some(Country.nl),
                NetworkType.hiking,
                1,
                "network",
                investigate = true
              )
            )
          ),
          subsetAnalyses = Seq(
            ChangeSetSubsetAnalysis(Subset.nlHiking, investigate = true)
          ),
          investigate = true
        )
      )

      tc.findNetworkChangeById("123:1:1") should matchTo(
        newNetworkChange(
          newChangeKey(elementId = 1),
          ChangeType.Delete,
          Some(Country.nl),
          NetworkType.hiking,
          1,
          "network",
          orphanRoutes = RefChanges(newRefs = Seq(Ref(11, "01-02"))),
          investigate = true
        )
      )

      val routeData = newRouteData(
        Some(Country.nl),
        NetworkType.hiking,
        relation = newRawRelation(
          11,
          members = Seq(
            RawMember("way", 101, None)
          ),
          tags = newRouteTags("01-02")
        ),
        name = "01-02",
        networkNodes = Seq(
          newRawNodeWithName(1001, "01"),
          newRawNodeWithName(1002, "02")
        ),
        nodes = Seq(
          newRawNodeWithName(1001, "01"),
          newRawNodeWithName(1002, "02")
        ),
        ways = Seq(
          newRawWay(
            101,
            nodeIds = Seq(1001, 1002),
            tags = Tags.from("highway" -> "unclassified")
          )
        )
      )

      tc.findRouteChangeById("123:1:11") should matchTo(
        newRouteChange(
          newChangeKey(elementId = 11),
          ChangeType.Update,
          "01-02",
          removedFromNetwork = Seq(Ref(1, "network")),
          before = Some(routeData),
          after = Some(routeData),
          facts = Seq(Fact.BecomeOrphan),
          investigate = true,
          impact = true
        )
      )

      tc.findNodeChangeById("123:1:1001") should matchTo(
        newNodeChange(
          key = newChangeKey(elementId = 1001),
          changeType = ChangeType.Update,
          subsets = Seq(Subset.nlHiking),
          name = "01",
          before = Some(
            newRawNodeWithName(1001, "01")
          ),
          after = Some(
            newRawNodeWithName(1001, "01")
          ),
          removedFromNetwork = Seq(
            Ref(1, "network")
          ),
          investigate = true,
          impact = true
        )
      )

      tc.findNodeChangeById("123:1:1002") should matchTo(
        newNodeChange(
          key = newChangeKey(elementId = 1002),
          changeType = ChangeType.Update,
          subsets = Seq(Subset.nlHiking),
          name = "02",
          before = Some(
            newRawNodeWithName(1002, "02")
          ),
          after = Some(
            newRawNodeWithName(1002, "02")
          ),
          removedFromNetwork = Seq(
            Ref(1, "network")
          ),
          investigate = true,
          impact = true
        )
      )
    }
  }
}
