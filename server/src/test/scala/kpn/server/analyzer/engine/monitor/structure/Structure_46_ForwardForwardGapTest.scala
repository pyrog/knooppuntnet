package kpn.server.analyzer.engine.monitor.structure

import kpn.core.util.UnitTest

class Structure_46_ForwardForwardGapTest extends UnitTest {

  private def setup = new StructureTestSetupBuilder() {
    memberWay(11, "", 1, 2)
    memberWay(12, "forward", 2, 3)
    memberWay(13, "forward", 3, 8)
    memberWay(14, "forward", 7, 2)
    memberWay(15, "forward", 8, 7)
    memberWay(16, "", 8, 9)
    //
    memberWay(17, "", 10, 11)
    memberWay(18, "", 11, 12)
  }.build

  test("reference") {
    setup.reference().shouldMatchTo(
      Seq(
        "1    p     n ■   loop     fp     bp     head     tail     d forward",
        "2    p ■   n ■   loop     fp ■   bp     head ■   tail     d forward",
        "3    p ■   n ■   loop     fp ■   bp     head     tail     d forward",
        "4    p ■   n ■   loop     fp     bp ■   head     tail     d backward",
        "5    p ■   n ■   loop     fp     bp ■   head     tail ■   d backward",
        "6    p ■   n     loop     fp     bp     head     tail     d forward",
        //
        "7    p     n ■   loop     fp     bp     head     tail     d forward",
        "8    p ■   n     loop     fp     bp     head     tail     d forward",
      )
    )
  }

  test("elements") {
    setup.elementGroups().shouldMatchTo(
      Seq(
        Seq(
          "1>2",
          "2>3>8 (Forward)",
          "8>7>2 (Backward)",
          "8>9",
        ),
        Seq(
          "10>11>12",
        )
      )
    )
  }

  test("structure") {
    val structure = setup.structure()
    structure.shouldMatchTo(
      TestStructure(
        forwardPath = None,
        backwardPath = None
      )
    )
  }
}
