package kpn.core.history

import kpn.api.common.diff.TagDetail
import kpn.api.common.diff.TagDetailType
import kpn.api.common.diff.TagDiffs
import kpn.api.common.diff.common.FactDiffs
import kpn.api.common.diff.route.RouteNameDiff
import kpn.api.custom.Fact
import kpn.api.custom.Tags
import kpn.core.test.TestData
import kpn.core.util.UnitTest
import org.scalamock.scalatest.MockFactory

class RouteDiffAnalyzerTest extends UnitTest with MockFactory {

  test("removed way") {

    val before = new TestData() {
      node(1001)
      node(1002)
      node(1003)

      way(101, 1001, 1002)
      way(102, 1002, 1003)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101),
          newMember("way", 102)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    val after = new TestData() {
      node(1001)
      node(1002)
      node(1003)

      way(101, 1001, 1002)
      way(102, 1002, 1003)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    //    val analysis = new RouteDiffAnalyzer(snapshot(before), snapshot(after), 11).analysis
    //    analysis.removedWays.map(_.id) should equal(Seq(102))
  }

  test("added way") {

    val before = new TestData() {
      node(1001)
      node(1002)
      node(1003)

      way(101, 1001, 1002)
      way(102, 1002, 1003)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    val after = new TestData() {
      node(1001)
      node(1002)
      node(1003)

      way(101, 1001, 1002)
      way(102, 1002, 1003)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101),
          newMember("way", 102)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    //    val analysis = new NetworkRouteDiffAnalyzer(snapshot(before), snapshot(after), 11).analysis
    //    analysis.get.addedWays.map(_.id) should equal(Seq(102))
  }

  test("updated way") {

    val before = new TestData() {
      node(1001)
      node(1002)

      way(101, 1001, 1002)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    val after = new TestData() {
      node(1001)
      node(1002)

      way(101, 1002, 1001) // direction reversed

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    //    val analysis = new NetworkRouteDiffAnalyzer(snapshot(before), snapshot(after), 11).analysis
    //    analysis.get.updatedWays.map(_.id) should equal(Seq(101))
  }

  test("name diff") {

    val before = new TestData() {
      node(1001)
      node(1002)

      way(101, 1001, 1002)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    val after = new TestData() {
      node(1001)
      node(1002)

      way(101, 1001, 1002)

      route(
        11,
        "02-01",
        Seq(
          newMember("way", 101)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }


    val expectedNameDiff = Some(RouteNameDiff("01-02", "02-01"))

    val expectedTagDiff = Some(
      TagDiffs(
        Seq(
          TagDetail(TagDetailType.Update, "note", Some("01-02"), Some("02-01")),
          TagDetail(TagDetailType.Same, "network", Some("rwn"), Some("rwn")),
          TagDetail(TagDetailType.Same, "type", Some("route"), Some("route")),
          TagDetail(TagDetailType.Same, "route", Some("foot"), Some("foot")),
          TagDetail(TagDetailType.Same, "network:type", Some("node_network"), Some("node_network"))
        ),
        Seq.empty
      )
    )

    //    val analysis = new NetworkRouteDiffAnalyzer(snapshot(before), snapshot(after), 11).analysis
    //    analysis.get.diffs.shouldMatchTo(RouteDiff(nameDiff = expectedNameDiff, tagDiffs = expectedTagDiff))
  }

  test("role diff") {

    val before = new TestData() {
      node(1001)
      node(1002)

      way(101, 1001, 1002)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11, "role")))
    }

    val after = new TestData() {
      node(1001)
      node(1002)

      way(101, 1001, 1002)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11, "connection")))
    }

    //    val analysis = new NetworkRouteDiffAnalyzer(snapshot(before), snapshot(after), 11).analysis
    //    analysis.get.diffs.shouldMatchTo(RouteDiff(roleDiff = Some(RouteRoleDiff(Some("role"), Some("connection")))))
  }

  test("fact diff") {

    val before = new TestData() {
      networkNode(1001, "01")
      networkNode(1002, "02")

      way(101, 1001, 1002)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    val after = new TestData() {
      networkNode(1001, "01")
      networkNode(1002, "02")

      way(101, 1001, 1002)

      relation(12) // extra relation that does not belong in a route relation

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101),
          newMember("relation", 12)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    val expectedDiff = FactDiffs(
      introduced = Seq(
        Fact.RouteUnexpectedRelation,
        Fact.RouteBroken
      )
    )

    //    val analysis = new NetworkRouteDiffAnalyzer(snapshot(before), snapshot(after), 11).analysis
    //    analysis.get.diffs.shouldMatchTo(RouteDiff(factDiffs = Some(expectedDiff)))
  }

  test("member diffs") {
    val before = new TestData() {
      networkNode(1001, "01")
      node(1002)
      node(1003)
      networkNode(1004, "04")

      way(101, 1001, 1002)
      way(102, 1002, 1003)
      way(103, 1003, 1004)

      route(
        11,
        "01-04",
        Seq(
          newMember("way", 101),
          newMember("way", 102),
          newMember("way", 103)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    val after = new TestData() {
      networkNode(1001, "01")
      node(1002)
      node(1003)
      networkNode(1004, "04")

      way(101, 1001, 1002)
      way(102, 1002, 1003)
      way(103, 1003, 1004)

      route(
        11,
        "01-04",
        Seq(
          newMember("way", 101),
          newMember("way", 103),
          newMember("way", 102)
        )
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    //    val analysis = new NetworkRouteDiffAnalyzer(snapshot(before), snapshot(after), 11).analysis
    //    analysis.get.diffs.shouldMatchTo(RouteDiff(factDiffs = None, memberOrderChanged = true))
  }

  test("tags diff") {

    val before = new TestData() {
      networkNode(1001, "01")
      networkNode(1002, "02")

      way(101, 1001, 1002)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        ),
        Tags.from("a" -> "1")
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    val after = new TestData() {
      networkNode(1001, "01")
      networkNode(1002, "02")

      way(101, 1001, 1002)

      route(
        11,
        "01-02",
        Seq(
          newMember("way", 101)
        ),
        Tags.from("a" -> "2")
      )

      networkRelation(1, "name", Seq(newMember("relation", 11)))
    }

    //    val analysis = new NetworkRouteDiffAnalyzer(snapshot(before), snapshot(after), 11).analysis

    val expectedTagDiff = TagDiffs(
      Seq(
        TagDetail(TagDetailType.Same, "note", Some("01-02"), Some("01-02")),
        TagDetail(TagDetailType.Same, "network", Some("rwn"), Some("rwn")),
        TagDetail(TagDetailType.Same, "type", Some("route"), Some("route")),
        TagDetail(TagDetailType.Same, "route", Some("foot"), Some("foot")),
        TagDetail(TagDetailType.Same, "network:type", Some("node_network"), Some("node_network"))
      ),
      Seq(
        TagDetail(TagDetailType.Update, "a", Some("1"), Some("2"))
      )
    )

    //    analysis.get.diffs.shouldMatchTo(RouteDiff(tagDiffs = Some(expectedTagDiff)))
  }
}
