package kpn.api.common.network

import kpn.api.base.WithId
import kpn.api.common.NetworkFacts
import kpn.api.custom.Fact
import kpn.api.custom.Tags
import kpn.core.mongo.doc.NetworkDoc
import kpn.core.mongo.doc.NetworkNodeRef
import kpn.core.mongo.doc.NetworkRouteRef

case class NetworkInfo(
  _id: Long,
  attributes: NetworkAttributes,
  active: Boolean,
  nodeRefs: Seq[Long],
  routeRefs: Seq[Long],
  networkRefs: Seq[Long],
  facts: Seq[Fact] = Seq.empty,
  tags: Tags,
  detail: Option[NetworkInfoDetail] = None
) extends WithId {

  def id: Long = attributes.id

  def hasFacts: Boolean = {
    facts.nonEmpty || hasNodeFacts || hasRouteFacts
  }

  def hasNodesWithFact(fact: Fact): Boolean = {
    detail match {
      case Some(d) => d.nodes.exists(_.facts.contains(fact))
      case None => false
    }
  }

  def hasRoutesWithFact(fact: Fact): Boolean = {
    detail match {
      case Some(d) => d.routes.exists(_.facts.contains(fact))
      case None => false
    }
  }

  def nodesWithFact(fact: Fact): Seq[NetworkInfoNode] = {
    detail match {
      case Some(d) => d.nodes.filter(_.facts.contains(fact))
      case None => Seq.empty
    }
  }

  def routesWithFact(fact: Fact): Seq[NetworkInfoRoute] = {
    detail match {
      case Some(d) => d.routes.filter(_.facts.contains(fact))
      case None => Seq.empty
    }
  }

  def factCount: Int = {
    facts.size + networkFactCount + nodeFactCount + routeFactCount
  }

  private def hasNodeFacts: Boolean = {
    detail match {
      case Some(d) => d.nodes.exists(_.facts.nonEmpty)
      case None => false
    }
  }

  private def hasRouteFacts: Boolean = {
    detail match {
      case Some(d) => d.routes.exists(_.facts.nonEmpty)
      case None => false
    }
  }

  private def networkFactCount: Int = {
    detail match {
      case Some(d) => d.networkFacts.factCount
      case None => 0
    }
  }

  private def nodeFactCount: Int = {
    detail match {
      case Some(d) => d.nodes.map(_.facts.count(Fact.reportedFacts.contains)).sum
      case None => 0
    }
  }

  private def routeFactCount: Int = {
    detail match {
      case Some(d) => d.routes.map(_.facts.count(Fact.reportedFacts.contains)).sum
      case None => 0
    }
  }

  def toNetworkDoc: NetworkDoc = {
    val nodeRefs: Seq[NetworkNodeRef] = detail match {
      case None => Seq.empty
      case Some(d) =>
        d.nodes.map { networkInfoNode =>
          NetworkNodeRef(
            networkInfoNode.id,
            if (networkInfoNode.roleConnection) Some("connection") else None
          )
        }
    }

    val routeRefs: Seq[NetworkRouteRef] = detail match {
      case None => Seq.empty
      case Some(d) =>
        d.routes.map { networkInfoRoute =>
          NetworkRouteRef(
            networkInfoRoute.id,
            networkInfoRoute.role
          )
        }
    }

    NetworkDoc(
      _id,
      // labels: Seq[String], TODO MONGO include country, networkType, networkScope?
      attributes.country,
      attributes.networkType,
      attributes.networkScope,
      attributes.name,
      attributes.lastUpdated,
      attributes.relationLastUpdated,
      nodeRefs,
      routeRefs,
      detail.map(_.networkFacts).getOrElse(NetworkFacts()),
      Seq.empty, // TODO MONGO not needed?
      tags
    )
  }
}
