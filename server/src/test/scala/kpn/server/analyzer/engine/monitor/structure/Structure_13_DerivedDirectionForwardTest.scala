package kpn.server.analyzer.engine.monitor.structure

import kpn.core.util.UnitTest

class Structure_13_DerivedDirectionForwardTest extends UnitTest {

  // direction of first way derived from second way - second way forward
  private def setup = new StructureTestSetup() {
    memberWay(11, "", 3, 2, 1)
    memberWay(12, "forward", 3, 4, 5)
  }

  test("reference") {
    setup.reference().shouldMatchTo(
      Seq(
        "1    p     n ■   loop     fp     bp     head     tail     d backward",
        "2    p ■   n     loop     fp ■   bp     head ■   tail     d forward",
      )
    )
  }

  test("elements") {
    setup.elementGroups().shouldMatchTo(
      Seq(
        Seq(
          "1>3",
          "3>5 (Down)",
        )
      )
    )
  }

  test("structure") {
    pending
    setup.structure()
  }
}
