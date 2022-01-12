package kpn.server.analyzer.engine.changes.node

import kpn.api.common.SharedTestObjects
import kpn.api.common.common.Ref
import kpn.api.common.data.MetaData
import kpn.api.common.data.raw.RawNode
import kpn.api.custom.Subset
import kpn.core.util.UnitTest

class NodeChangeMergerTest extends UnitTest with SharedTestObjects {

  test("mergedSubsets") {
    def assertMerged(left: Seq[Subset], right: Seq[Subset], expected: Seq[Subset]): Unit = {
      new NodeChangeMerger(
        newNodeChange(subsets = left),
        newNodeChange(subsets = right)
      ).merged.subsets.shouldMatchTo(expected)
    }

    assertMerged(Seq.empty, Seq.empty, Seq.empty)
    assertMerged(Seq(Subset.beHiking), Seq.empty, Seq(Subset.beHiking))
    assertMerged(Seq.empty, Seq(Subset.nlBicycle), Seq(Subset.nlBicycle))
    assertMerged(Seq(Subset.beHiking), Seq(Subset.nlBicycle), Seq(Subset.beHiking, Subset.nlBicycle))
  }

  test("mergedName") {
    pending
  }

  test("mergedBefore") {
    def assertMerged(left: Option[MetaData], right: Option[MetaData], expected: Option[MetaData]): Unit = {
      new NodeChangeMerger(
        newNodeChange(before = left),
        newNodeChange(before = right)
      ).merged.before.shouldMatchTo(expected)
    }

    assertMerged(None, None, None)
    assertMerged(Some(newMetaData()), None, Some(newMetaData()))
    assertMerged(None, Some(newMetaData()), Some(newMetaData()))
    assertMerged(Some(newMetaData()), Some(newMetaData()), Some(newMetaData()))
  }

  test("mergedAfter") {
    def assertMerged(left: Option[MetaData], right: Option[MetaData], expected: Option[MetaData]): Unit = {
      new NodeChangeMerger(
        newNodeChange(after = left),
        newNodeChange(after = right)
      ).merged.after.shouldMatchTo(expected)
    }

    assertMerged(None, None, None)
    assertMerged(Some(newMetaData()), None, Some(newMetaData()))
    assertMerged(None, Some(newMetaData()), Some(newMetaData()))
    assertMerged(Some(newMetaData()), Some(newMetaData()), Some(newMetaData()))
  }

  test("mergedTagDiffs") {
    pending
  }

  test("mergedNodeMoved") {
    pending
  }

  test("mergedAddedToNetwork") {
    def assertMerged(left: Seq[Ref], right: Seq[Ref], expected: Seq[Ref]): Unit = {
      new NodeChangeMerger(
        newNodeChange(addedToNetwork = left),
        newNodeChange(addedToNetwork = right)
      ).merged.addedToNetwork.shouldMatchTo(expected)
    }

    assertMerged(Seq.empty, Seq.empty, Seq.empty)
    assertMerged(Seq(Ref(2, "2"), Ref(3, "3")), Seq.empty, Seq(Ref(2, "2"), Ref(3, "3")))
    assertMerged(Seq.empty, Seq(Ref(1, "1"), Ref(2, "2")), Seq(Ref(1, "1"), Ref(2, "2")))
    assertMerged(Seq(Ref(2, "2"), Ref(3, "3")), Seq(Ref(1, "1"), Ref(2, "2")), Seq(Ref(1, "1"), Ref(2, "2"), Ref(3, "3")))
  }

  test("mergedRemovedFromNetwork") {
    def assertMerged(left: Seq[Ref], right: Seq[Ref], expected: Seq[Ref]): Unit = {
      new NodeChangeMerger(
        newNodeChange(removedFromNetwork = left),
        newNodeChange(removedFromNetwork = right)
      ).merged.removedFromNetwork.shouldMatchTo(expected)
    }

    assertMerged(Seq.empty, Seq.empty, Seq.empty)
    assertMerged(Seq(Ref(2, "2"), Ref(3, "3")), Seq.empty, Seq(Ref(2, "2"), Ref(3, "3")))
    assertMerged(Seq.empty, Seq(Ref(1, "1"), Ref(2, "2")), Seq(Ref(1, "1"), Ref(2, "2")))
    assertMerged(Seq(Ref(2, "2"), Ref(3, "3")), Seq(Ref(1, "1"), Ref(2, "2")), Seq(Ref(1, "1"), Ref(2, "2"), Ref(3, "3")))
  }

  test("mergedFactDiffs") {
    pending
  }

  test("mergedFacts") {
    pending
  }
}
