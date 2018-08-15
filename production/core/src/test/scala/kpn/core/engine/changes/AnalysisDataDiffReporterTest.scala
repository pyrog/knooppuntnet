package kpn.core.engine.changes

import kpn.core.changes.ElementIds
import kpn.core.engine.changes.data.AnalysisData
import org.scalatest.FunSuite
import org.scalatest.Matchers

class AnalysisDataDiffReporterTest extends FunSuite with Matchers {

  test("No differences") {
    val left = AnalysisData()
    val right = AnalysisData()
    val report = new AnalysisDataDiffReporter().report(left: AnalysisData, right: AnalysisData).mkString("\n")
    report should equal("No differences")
  }

  test("Network differences") {

    pending

    val left = AnalysisData()
    val right = AnalysisData()

    val leftElementIds = ElementIds(
      nodeIds = Set(1011, 1012),
      wayIds = Set(1021, 1022),
      relationIds = Set(1031, 1032)
    )

    val rightElementIds = ElementIds(
      nodeIds = Set(1011, 1013),
      wayIds = Set(1021, 1023),
      relationIds = Set(1031, 1033)
    )

    left.networks.watched.add(10, leftElementIds)
    left.networks.watched.add(11, ElementIds())
    left.networks.watched.add(12, ElementIds())
    right.networks.watched.add(10, rightElementIds)
    right.networks.watched.add(11, ElementIds())
    right.networks.watched.add(13, ElementIds())

    left.networks.ignored.add(21, ElementIds())
    left.networks.ignored.add(22, ElementIds())
    left.networks.ignored.add(23, ElementIds())
    left.networks.ignored.add(24, ElementIds())
    right.networks.ignored.add(21, ElementIds())
    right.networks.ignored.add(22, ElementIds())
    right.networks.ignored.add(25, ElementIds())
    right.networks.ignored.add(26, ElementIds())

    val report = new AnalysisDataDiffReporter().report(left: AnalysisData, right: AnalysisData).mkString("\n")

    val expected =
      """|Network differences:
         |  watched:
         |    leftOnlyNetworks = 12
         |    rightOnlyNetworks = 13
         |    network 10
         |      nodeIdsLeftOnly = 1012
         |      nodeIdsRightOnly = 1013
         |      wayIdsLeftOnly = 1022
         |      wayIdsRightOnly = 1023
         |      relationIdsLeftOnly = 1032
         |      relationIdsRightOnly = 1033
         |  ignored:
         |    leftOnlyNetworks = 23, 24
         |    rightOnlyNetworks = 25, 26""".stripMargin

    report should equal(expected)
  }

  test("Orphan route differences") {

    pending


    val left = AnalysisData()
    val right = AnalysisData()

    val leftElementIds = ElementIds(
      nodeIds = Set(1011, 1012),
      wayIds = Set(1021, 1022),
      relationIds = Set(1031, 1032)
    )

    val rightElementIds = ElementIds(
      nodeIds = Set(1011, 1013),
      wayIds = Set(1021, 1023),
      relationIds = Set(1031, 1033)
    )

    left.orphanRoutes.watched.add(10, leftElementIds)
    left.orphanRoutes.watched.add(11, ElementIds())
    left.orphanRoutes.watched.add(12, ElementIds())
    left.orphanRoutes.watched.add(13, ElementIds())

    right.orphanRoutes.watched.add(10, rightElementIds)
    right.orphanRoutes.watched.add(11, ElementIds())
    right.orphanRoutes.watched.add(14, ElementIds())
    right.orphanRoutes.watched.add(15, ElementIds())

    left.orphanRoutes.ignored.add(20, ElementIds())
    left.orphanRoutes.ignored.add(21, ElementIds())
    left.orphanRoutes.ignored.add(22, ElementIds())
    left.orphanRoutes.ignored.add(23, ElementIds())

    right.orphanRoutes.ignored.add(20, ElementIds())
    right.orphanRoutes.ignored.add(21, ElementIds())
    right.orphanRoutes.ignored.add(24, ElementIds())
    right.orphanRoutes.ignored.add(25, ElementIds())

    val report = new AnalysisDataDiffReporter().report(left: AnalysisData, right: AnalysisData).mkString("\n")

    val expected =
      """|Orphan route differences:
         |  watched:
         |    leftOnly = 12, 13
         |    rightOnly = 14, 15
         |    route 10
         |      left = RouteSummary(10,Some(Country(nl,The Netherlands)),rwn,A,0,false,0,Timestamp(2016,8,11,0,0,0),user,List(),,false)
         |      right = RouteSummary(10,Some(Country(nl,The Netherlands)),rwn,B,0,false,0,Timestamp(2016,8,11,0,0,0),user,List(),,false)
         |      nodeIdsLeftOnly = 1012
         |      nodeIdsRightOnly = 1013
         |      wayIdsLeftOnly = 1022
         |      wayIdsRightOnly = 1023
         |      relationIdsLeftOnly = 1032
         |      relationIdsRightOnly = 1033
         |  ignored:
         |    leftOnly = 22, 23
         |    rightOnly = 24, 25
         |    route 20
         |      left = RouteSummary(20,Some(Country(nl,The Netherlands)),rwn,A,0,false,0,Timestamp(2016,8,11,0,0,0),user,List(),,false)
         |      right = RouteSummary(20,Some(Country(nl,The Netherlands)),rwn,B,0,false,0,Timestamp(2016,8,11,0,0,0),user,List(),,false)""".stripMargin

    report should equal(expected)
  }

  test("Orphan node differences") {

    pending

    val left = AnalysisData()
    val right = AnalysisData()

    left.orphanNodes.watched.add(10)
    left.orphanNodes.watched.add(11)
    left.orphanNodes.watched.add(12)
    left.orphanNodes.watched.add(13)

    right.orphanNodes.watched.add(10)
    right.orphanNodes.watched.add(11)
    right.orphanNodes.watched.add(14)
    right.orphanNodes.watched.add(15)

    val report = new AnalysisDataDiffReporter().report(left: AnalysisData, right: AnalysisData).mkString("\n")

    val expected =
      """|Orphan node differences:
         |  watched:
         |    leftOnly = 12, 13
         |    rightOnly = 14, 15
         |    orphan node 10
         |      left = LoadedNode(10,Subset(Country(nl,The Netherlands),rwn),A,Node(RawNode(10,,,0,Timestamp(2016,11,8,0,0,0),1,1,user,Tags())))
         |      right = LoadedNode(10,Subset(Country(nl,The Netherlands),rwn),B,Node(RawNode(10,,,0,Timestamp(2016,11,8,0,0,0),1,1,user,Tags())))""".stripMargin

    report should equal(expected)
  }

  test("Network collection differences") {

    val left = AnalysisData()
    val right = AnalysisData()

    left.networkCollections.add(10)
    left.networkCollections.add(11)
    left.networkCollections.add(12)
    left.networkCollections.add(13)

    right.networkCollections.add(10)
    right.networkCollections.add(11)
    right.networkCollections.add(14)
    right.networkCollections.add(15)

    val report = new AnalysisDataDiffReporter().report(left: AnalysisData, right: AnalysisData).mkString("\n")

    val expected =
      """|Network collection differences:
         |  leftOnly = 12, 13
         |  rightOnly = 14, 15""".stripMargin

    report should equal(expected)
  }
}
