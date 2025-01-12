package kpn.server.analyzer.engine.monitor.structure

import kpn.core.util.UnitTest

// reproduces situation in route 5444896 (EV1 Roscoff — Morlaix)
class Structure_72_SplitRoundabout_Test extends UnitTest {

  private def setup = new StructureTestSetupBuilder() {
    // before split:
    memberWay(11, "", 1, 2)
    // down until roundabout:
    memberWay(12, "forward", 2, 5)
    // up from roundabout:
    memberWay(13, "forward", 8, 2)
    memberWay(14, "forward", 4, 8)
    // roundabout:
    memberRoundabout(15, "forward", 3, 4, 5, 6, 7, 3)
    // down until roundabout:
    memberWay(16, "forward", 6, 9)
    memberWay(17, "forward", 9, 10)
    // up from roundabout
    memberWay(18, "forward", 11, 7)
    memberWay(19, "forward", 10, 11)
    // continue:
    memberWay(20, "", 10, 12)
  }.build

  test("reference") {
    setup.reference().shouldMatchTo(
      Seq(
        "1    p     n ■   loop     fp     bp     head     tail     d forward",
        "2    p ■   n ■   loop     fp ■   bp     head ■   tail     d forward",
        "3    p ■   n ■   loop     fp     bp ■   head     tail     d backward",
        "4    p ■   n ■   loop     fp     bp ■   head     tail     d backward",
        "5    p ■   n ■   loop     fp ■   bp     head     tail ■   d roundabout_right",
        "6    p ■   n ■   loop     fp ■   bp     head ■   tail     d forward",
        "7    p ■   n ■   loop     fp ■   bp     head     tail     d forward",
        "8    p ■   n ■   loop     fp     bp ■   head     tail     d backward",
        "9    p ■   n ■   loop     fp     bp ■   head     tail ■   d backward",
        "10    p ■   n     loop     fp     bp     head     tail     d forward"
      )
    )
  }

  test("elements") {
    setup.elementGroups().shouldMatchTo(
      Seq(
        Seq(
          "1>2",
          "2>5 (Forward)",
          "4>8>2 (Backward)",
          "5>6 (Forward)",
          "7>4 (Backward)",
          "6>9>10 (Forward)",
          "10>11>7 (Backward)",
          "10>12",
        ),
      )
    )
  }

  test("structure") {
    val structure = setup.structure()
    structure.shouldMatchTo(
      TestStructure(
        forwardPath = Some(
          TestStructurePath(
            startNodeId = 1,
            endNodeId = 12,
            nodeIds = Seq(1, 2, 5, 6, 9, 10, 12)
          )
        ),
        backwardPath = Some(
          TestStructurePath(
            startNodeId = 12,
            endNodeId = 1,
            nodeIds = Seq(12, 10, 11, 7, 3, 4, 8, 2, 1)
          )
        )
      )
    )
  }
}
