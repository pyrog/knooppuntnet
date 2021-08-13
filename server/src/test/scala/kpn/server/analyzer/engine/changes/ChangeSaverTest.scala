package kpn.server.analyzer.engine.changes

import kpn.api.common.ChangeSetElementRef
import kpn.api.common.ChangeSetElementRefs
import kpn.api.common.ChangeSetSubsetAnalysis
import kpn.api.common.ChangeSetSubsetElementRefs
import kpn.api.common.ChangeSetSummary
import kpn.api.common.NetworkChanges
import kpn.api.common.ReplicationId
import kpn.api.common.SharedTestObjects
import kpn.api.common.changes.details.NetworkInfoChange
import kpn.api.common.changes.details.NodeChange
import kpn.api.common.changes.details.RouteChange
import kpn.api.custom.Country
import kpn.api.custom.Fact
import kpn.api.custom.NetworkType
import kpn.api.custom.Subset
import kpn.core.util.UnitTest
import kpn.server.analyzer.engine.changes.changes.ElementIds
import kpn.server.analyzer.engine.changes.data.ChangeSetChanges
import kpn.server.repository.ChangeSetRepository
import org.scalamock.scalatest.MockFactory

class ChangeSaverTest extends UnitTest with MockFactory with SharedTestObjects {

  test("nothing to save") {

    val changeSetRepository = stub[ChangeSetRepository]

    val context = ChangeSetContext(
      ReplicationId(1, 2, 3),
      newChangeSet(),
      ElementIds(),
      ChangeSetChanges()
    )

    new ChangeSaverImpl(changeSetRepository).save(context)

    (changeSetRepository.saveNetworkInfoChange _).verify(*).never()
    (changeSetRepository.saveRouteChange _).verify(*).never()
    (changeSetRepository.saveNodeChange _).verify(*).never()
    (changeSetRepository.saveChangeSetSummary _).verify(*).never()
  }

  test("save network changes") {

    val networkChange = newNetworkInfoChange(newChangeKey(elementId = 1))

    val changeSetChanges = ChangeSetChanges(
      networkInfoChanges = Seq(networkChange)
    )

    val changeSetRepository = stub[ChangeSetRepository]

    save(changeSetRepository, changeSetChanges)

    (changeSetRepository.saveRouteChange _).verify(*).never()
    (changeSetRepository.saveNodeChange _).verify(*).never()

    (changeSetRepository.saveNetworkInfoChange _).verify(
      where { savedNetworkChange: NetworkInfoChange =>
        savedNetworkChange should matchTo(networkChange)
        true
      }
    ).once()

    (changeSetRepository.saveChangeSetSummary _).verify(
      where { changeSetSummary: ChangeSetSummary =>
        changeSetSummary should matchTo(
          newChangeSetSummary(
            networkChanges = NetworkChanges(
              updates = Seq(
                newChangeSetNetwork()
              )
            )
          )
        )
        true
      }
    ).once()
  }

  test("save route changes") {

    val routeChange = newRouteChange(
      newChangeKey(elementId = 10),
      after = Some(
        newRouteData(
          country = Some(Country.nl),
          networkType = NetworkType.hiking
        )
      ),
      facts = Seq(Fact.OrphanRoute)
    )

    val changeSetChanges = ChangeSetChanges(
      routeChanges = Seq(routeChange)
    )

    val changeSetRepository = stub[ChangeSetRepository]

    save(changeSetRepository, changeSetChanges)

    (changeSetRepository.saveNetworkInfoChange _).verify(*).never()
    (changeSetRepository.saveNodeChange _).verify(*).never()

    (changeSetRepository.saveRouteChange _).verify(
      where { savedRouteChange: RouteChange =>
        savedRouteChange should matchTo(routeChange)
        true
      }
    ).once()

    (changeSetRepository.saveChangeSetSummary _).verify(
      where { changeSetSummary: ChangeSetSummary =>
        changeSetSummary should matchTo(
          newChangeSetSummary(
            subsets = Seq(Subset.nlHiking),
            routeChanges = Seq(
              ChangeSetSubsetElementRefs(
                Subset.nlHiking,
                ChangeSetElementRefs(
                  added = Seq(
                    ChangeSetElementRef(10, "", happy = false, investigate = false)
                  )
                )
              )
            ),
            subsetAnalyses = Seq(
              ChangeSetSubsetAnalysis(Subset.nlHiking)
            )
          )
        )
        true
      }
    ).once()
  }

  test("save node changes") {

    val nodeChange = newNodeChange(
      newChangeKey(elementId = 1001),
      subsets = Seq(Subset.nlHiking),
      name = "01",
      facts = Seq(Fact.OrphanNode)
    )

    val changeSetChanges = ChangeSetChanges(
      nodeChanges = Seq(nodeChange)
    )

    val changeSetRepository = stub[ChangeSetRepository]

    save(changeSetRepository, changeSetChanges)

    (changeSetRepository.saveNetworkInfoChange _).verify(*).never()
    (changeSetRepository.saveRouteChange _).verify(*).never()

    (changeSetRepository.saveNodeChange _).verify(
      where { savedNodeChange: NodeChange =>
        savedNodeChange should matchTo(nodeChange)
        true
      }
    ).once()

    (changeSetRepository.saveChangeSetSummary _).verify(
      where { changeSetSummary: ChangeSetSummary =>
        changeSetSummary should matchTo(
          newChangeSetSummary(
            subsets = Seq(Subset.nlHiking),
            nodeChanges = Seq(
              ChangeSetSubsetElementRefs(
                Subset.nlHiking,
                ChangeSetElementRefs(
                  updated = Seq(
                    ChangeSetElementRef(1001, "01", happy = false, investigate = false)
                  )
                )
              )
            ),
            subsetAnalyses = Seq(
              ChangeSetSubsetAnalysis(Subset.nlHiking)
            )
          )
        )
        true
      }
    ).once()
  }

  private def save(changeSetRepository: ChangeSetRepository, changeSetChanges: ChangeSetChanges): Unit = {
    val context = ChangeSetContext(
      ReplicationId(0, 0, 1),
      newChangeSet(),
      ElementIds(),
      changeSetChanges
    )
    new ChangeSaverImpl(changeSetRepository).save(context)
  }
}
