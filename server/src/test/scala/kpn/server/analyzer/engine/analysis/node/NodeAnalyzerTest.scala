package kpn.server.analyzer.engine.analysis.node

import kpn.api.common.NodeName
import kpn.api.custom.NetworkType
import kpn.api.custom.ScopedNetworkType
import kpn.api.custom.Tags
import kpn.core.util.UnitTest

class NodeAnalyzerTest extends UnitTest {

  test("name - single name") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    val tags = Tags.from("rwn_ref" -> "01")
    nodeAnalyzer.name(tags) should equal("01")
  }

  test("name - multiple names") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    val tags = Tags.from("rwn_ref" -> "01", "rcn_ref" -> "02")
    nodeAnalyzer.name(tags) should equal("01 / 02")
  }

  test("name - empty string when no name") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    nodeAnalyzer.name(Tags.empty) should equal("")
  }

  test("names - single name") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    val tags = Tags.from("rwn_ref" -> "01")
    nodeAnalyzer.names(tags) should equal(
      Seq(
        NodeName(ScopedNetworkType.rwn, "01")
      )
    )
  }

  test("names - multiple names") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    val tags = Tags.from("rwn_ref" -> "01", "rcn_ref" -> "02")
    nodeAnalyzer.names(tags) should equal(
      Seq(
        NodeName(ScopedNetworkType.rwn, "01"),
        NodeName(ScopedNetworkType.rcn, "02")
      )
    )
  }

  test("names - empty collection when no names") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    nodeAnalyzer.names(Tags.empty) should equal(Seq.empty)
  }

  test("name - for specific networkType") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    val tags = Tags.from("rwn_ref" -> "01", "rcn_ref" -> "02")
    nodeAnalyzer.name(NetworkType.hiking, tags) should equal("01")
    nodeAnalyzer.name(NetworkType.cycling, tags) should equal("02")
  }

  test("name - when there are multiple names for same NetworkType") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    val tags = Tags.from("lwn_ref" -> "01", "rwn_ref" -> "02")
    nodeAnalyzer.name(NetworkType.hiking, tags) should equal("01 / 02")
  }

  test("name - for specific networkType - empty string when no name") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    nodeAnalyzer.name(NetworkType.hiking, Tags.empty) should equal("")
  }

  test("name - * and .") {
    val nodeAnalyzer = new NodeAnalyzerImpl()
    val tags = Tags.from("rwn_ref" -> "*", "rcn_ref" -> ".")
    nodeAnalyzer.name(NetworkType.hiking, tags) should equal("*")
    nodeAnalyzer.name(NetworkType.cycling, tags) should equal(".")
  }

}