package kpn.core.tools.support

import kpn.api.custom.Country
import kpn.api.custom.ScopedNetworkType
import kpn.api.custom.Tags
import kpn.core.doc.NodeDoc
import kpn.database.base.Database
import kpn.database.util.Mongo

object FindSuspiciousExpectedRouteRelationsTags {
  def main(args: Array[String]): Unit = {
    Mongo.executeIn("kpn-laptop") { database =>
      new FindSuspiciousExpectedRouteRelationsTags(database).explore()
    }
  }
}

class FindSuspiciousExpectedRouteRelationsTags(database: Database) {

  private val all = ScopedNetworkType.all
  private val nodeNameTags = all.map(_.nodeRefTagKey) ++ all.map(_.nodeNameTagKey)
  private val proposedNameTags = all.map(_.proposedNodeRefTagKey) ++ all.map(_.proposedNodeNameTagKey)
  private val expectedTags = all.map(_.expectedRouteRelationsTag)

  def explore(): Unit = {
    println("loading nodes")
    val nodes = database.nodes.findAll()
    println(s"analyzing ${nodes.size} nodes")

    val suspiciousNodes = nodes.filter(isSuspicious)

    report(suspiciousNodes)
    println(s"\n${suspiciousNodes.size} nodes")
  }

  private def isSuspicious(node: NodeDoc): Boolean = {
    val a = networkTypesInNodeNames(node.tags)
    val b = networkTypesInExpectedTags(node.tags)
    (b -- a).nonEmpty
  }

  private def report(nodes: Seq[NodeDoc]): Unit = {
    Country.all.foreach { country =>
      println(s"** ${country.domain} ***")
      val countryNodes = nodes.filter(_.country.contains(country))
      countryNodes.zipWithIndex.foreach { case (node, index) =>
        println(s"${country.domain} ${index + 1}/${countryNodes.size} [${node.name}](http://localhost:4000/analysis/node/${node._id})")
        reportNode(node)
      }
    }
  }

  private def reportNode(node: NodeDoc): Unit = {
    println()
    println("|key|value|")
    println("|---|-----|")
    node.tags.tags.foreach { tag =>
      println(s"|${tag.key}|${tag.value}|")
    }
    println()
  }

  private def networkTypesInNodeNames(tags: Tags): Set[String] = {
    val from = "proposed:".length
    (networkTypesInTags(nodeNameTags, 0, 3, tags) ++
      networkTypesInTags(proposedNameTags, from, from + 3, tags)).toSet
  }

  private def networkTypesInExpectedTags(tags: Tags): Set[String] = {
    val from = "expected_".length
    networkTypesInTags(expectedTags, from, from + 3, tags).toSet
  }

  private def networkTypesInTags(tagKeys: Seq[String], from: Int, until: Int, tags: Tags) = {
    tagKeys.filter(tag => tags.has(tag)).map(tag => tag.slice(from, until))
  }
}
