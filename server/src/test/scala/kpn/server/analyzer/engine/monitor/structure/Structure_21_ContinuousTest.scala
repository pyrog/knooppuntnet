package kpn.server.analyzer.engine.monitor.structure

import kpn.core.util.UnitTest

class Structure_21_ContinuousTest extends UnitTest {

  private def setup = new StructureTestSetup() {
    memberWay(11, "", 1, 2, 3)
    memberWay(12, "", 3, 4, 5)
    memberWay(13, "", 5, 6, 7)
  }

  test("reference") {
    setup.reference().shouldMatchTo(
      Seq(
        "1    p     n ■   loop     fp     bp     head     tail     d forward",
        "2    p ■   n ■   loop     fp     bp     head     tail     d forward",
        "3    p ■   n     loop     fp     bp     head     tail     d forward",
      )
    )
  }

  test("elements") {
    setup.elementGroups().shouldMatchTo(
      Seq(
        Seq(
          "1>3>5>7"
        )
      )
    )
  }

  test("structure") {
    pending
    setup.structure()
  }
}
