package kpn.core.util

class Report {
  var indentLevel = 0

  def indent(block: => Unit): Unit = {
    indentLevel = indentLevel + 1
    block
    indentLevel = indentLevel - 1
  }

  def print(string: String): Unit = {
    0.until (indentLevel).foreach { level =>
      System.out.print(" ")
    }
    println(string)
  }
}
