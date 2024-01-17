package kpn.server.analyzer.engine.monitor.structure

import kpn.core.util.UnitTest

class Structure_42_ForwardGapTest extends UnitTest {

  private def setup = new StructureTestSetup() {
    memberWay(11, "forward", 2, 1)
    memberWay(12, "", 3, 2)
  }

  test("reference") {
    setup.reference().shouldMatchTo(
      Seq(
        "1    p     n     loop     fp ■   bp     head ■   tail     d forward",
        //
        "2    p     n     loop     fp     bp     head     tail     d none",
      )
    )
  }

  test("elements") {
    setup.elementGroups().shouldMatchTo(
      Seq(
        Seq(
          "2>1 (Down)",
        ),
        Seq(
          "3>2"
        )
      )
    )
  }

  test("structure") {
    pending
    setup.structure()
  }
}
