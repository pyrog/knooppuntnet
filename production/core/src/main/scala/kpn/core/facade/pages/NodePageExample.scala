package kpn.core.facade.pages

import kpn.shared.Country
import kpn.shared.Fact
import kpn.shared.LatLonImpl
import kpn.shared.NetworkType
import kpn.shared.NodeInfo
import kpn.shared.Timestamp
import kpn.shared.changes.details.ChangeKey
import kpn.shared.changes.details.RefBooleanChange
import kpn.shared.common.Ref
import kpn.shared.data.Tags
import kpn.shared.diff.TagDetail
import kpn.shared.diff.TagDetailType
import kpn.shared.diff.TagDiffs
import kpn.shared.diff.common.FactDiffs
import kpn.shared.diff.node.NodeMoved
import kpn.shared.node.NodeChangeInfo
import kpn.shared.node.NodeChangeInfos
import kpn.shared.node.NodeNetworkIntegrityCheck
import kpn.shared.node.NodeNetworkReference
import kpn.shared.node.NodeNetworkRouteReference
import kpn.shared.node.NodeOrphanRouteReference
import kpn.shared.node.NodePage
import kpn.shared.node.NodeReferences

object NodePageExample {

  val page: NodePage = {
    NodePage(
      NodeInfo(
        id = 1,
        active = false,
        ignored = true,
        display = true,
        orphan = true,
        country = Some(Country.nl),
        name = "01 / 02",
        rcnName = "01",
        rwnName = "02",
        rhnName = "",
        rmnName = "",
        rpnName = "",
        rinName = "",
        latitude = "51.5291600",
        longitude = "4.297800",
        lastUpdated = Timestamp(2020, 10, 11, 12, 34, 56),
        tags = Tags.from(
          "rwn_ref" -> "01",
          "rcn_ref" -> "02",
          "note" -> "this is a test network node for trying out the node page"
        ),
        facts = Seq(
          Fact.WasOrphan,
          Fact.Deleted
        )
      ),
      NodeReferences(
        networkReferences = Seq(
          NodeNetworkReference(
            networkType = NetworkType.hiking,
            networkId = 1,
            networkName = "network one",
            nodeDefinedInRelation = true,
            nodeConnection = true,
            nodeIntegrityCheck = Some(NodeNetworkIntegrityCheck(failed = true, 3, 2)),
            routes = Seq(
              NodeNetworkRouteReference(
                routeId = 11,
                routeName = "01-02",
                routeRole = Some("connection")
              )
            )
          ),
          NodeNetworkReference(
            networkType = NetworkType.hiking,
            networkId = 2,
            networkName = "network two",
            nodeDefinedInRelation = false,
            nodeConnection = false,
            nodeIntegrityCheck = Some(NodeNetworkIntegrityCheck(failed = false, 3, 3)),
            routes = Seq(
              NodeNetworkRouteReference(
                routeId = 11,
                routeName = "01-02",
                routeRole = Some("connection")
              ),
              NodeNetworkRouteReference(
                routeId = 12,
                routeName = "01-03",
                routeRole = Some("connection")
              ),
              NodeNetworkRouteReference(
                routeId = 13,
                routeName = "01-04",
                routeRole = Some("connection")
              )
            )
          )
        ),
        routeReferences = Seq(
          NodeOrphanRouteReference(NetworkType.bicycle, 1, "01-02"),
          NodeOrphanRouteReference(NetworkType.hiking, 2, "01-03")
        )
      ),
      NodeChangeInfos(
        changes = Seq(
          NodeChangeInfo(
            1,
            Some(1),
            ChangeKey(
              replicationNumber = 1,
              timestamp = Timestamp(2020, 10, 11, 12, 34, 56),
              changeSetId = 1,
              elementId = 1
            ),
            changeTags = Tags.from(
              "created_by" -> "JOSM",
              "source" -> "survey"
            ),
            comment = Some("this is the comment"),
            None, // TODO CHANGE provide some value
            None, // TODO CHANGE provide some value
            connectionChanges = Seq(
              RefBooleanChange(
                ref = Ref(1, "network one"),
                after = false
              )
            ),
            definedInNetworkChanges = Seq(
              RefBooleanChange(
                ref = Ref(2, "network two"),
                after = true
              )
            ),
            tagDiffs = Some(
              TagDiffs(
                mainTags = Seq(
                  TagDetail(TagDetailType.Update, "rwn_ref", Some("01"), Some("02"))
                )
              )
            ),
            nodeMoved = Some(
              NodeMoved(
                before = LatLonImpl("51.5291500", "4.297700"),
                after = LatLonImpl("51.5291600", "4.297800"),
                distance = 7
              )
            ),
            addedToRoute = Seq(
              Ref(1, "route one"),
              Ref(1, "route two")
            ),
            removedFromRoute = Seq(
              Ref(1, "route three"),
              Ref(1, "route four")
            ),
            addedToNetwork = Seq(
              Ref(1, "network one"),
              Ref(1, "network two")
            ),
            removedFromNetwork = Seq(
              Ref(1, "network three"),
              Ref(1, "network four")
            ),
            factDiffs = FactDiffs(
              resolved = Set(Fact.IgnoreForeignCountry),
              introduced = Set(Fact.IntegrityCheckFailed),
              remaining = Set(Fact.IntegrityCheck)
            ),
            facts = Seq(
              Fact.WasOrphan,
              Fact.WasIgnored
            ),
            happy = true,
            investigate = true
          )
        ),
        incompleteWarning = true,
        more = true
      )
    )
  }
}
