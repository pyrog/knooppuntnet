package kpn.api.custom

import scala.collection.mutable.ListBuffer

case class Fact(name: String, level: FactLevel) {

  def isError: Boolean = level == FactLevel.ERROR

  def isInfo: Boolean = level == FactLevel.INFO

  def isOther: Boolean = level == FactLevel.OTHER

  override def toString: String = s"Fact.$name"
}

class FactFactory {

  private val facts = ListBuffer[Fact]()

  def info(name: String): Fact = {
    fact(name, FactLevel.INFO)
  }

  def error(name: String): Fact = {
    fact(name, FactLevel.ERROR)
  }

  def other(name: String): Fact = {
    fact(name, FactLevel.OTHER)
  }

  private def fact(name: String, level: FactLevel): Fact = {
    val newFact = Fact(name, level)
    facts += newFact
    newFact
  }

  def all: Seq[Fact] = facts.toSeq
}

object Fact {

  private val f = new FactFactory()

  // new errors
  val RouteUnsupportedNetworkType: Fact = f.error("RouteUnsupportedNetworkType")

  // errors
  val RouteNotContinious: Fact = f.error("RouteNotContinious")
  val RouteNotForward: Fact = f.error("RouteNotForward")
  val RouteNotBackward: Fact = f.error("RouteNotBackward")
  val RouteUnusedSegments: Fact = f.error("RouteUnusedSegments")

  val RouteNodeMissingInWays: Fact = f.error("RouteNodeMissingInWays")
  val RouteRedundantNodes: Fact = f.error("RouteRedundantNodes")
  val RouteWithoutWays: Fact = f.error("RouteWithoutWays")
  val RouteWithoutNodes: Fact = f.error("RouteWithoutNodes")

  val RouteFixmetodo: Fact = f.error("RouteFixmetodo")

  val RouteNameMissing: Fact = f.error("RouteNameMissing")

  val RouteTagMissing: Fact = f.error("RouteTagMissing")
  val RouteTagInvalid: Fact = f.error("RouteTagInvalid")

  val RouteUnexpectedNode: Fact = f.error("RouteUnexpectedNode")
  val RouteUnexpectedRelation: Fact = f.error("RouteUnexpectedRelation")

  val NetworkExtraMemberNode: Fact = f.error("NetworkExtraMemberNode")
  val NetworkExtraMemberWay: Fact = f.error("NetworkExtraMemberWay")
  val NetworkExtraMemberRelation: Fact = f.error("NetworkExtraMemberRelation")
  val NodeMemberMissing: Fact = f.info("NodeMemberMissing")
  val IntegrityCheckFailed: Fact = f.error("IntegrityCheckFailed")
  val UnexpectedIntegrityCheck: Fact = f.error("UnexpectedIntegrityCheck")
  val NameMissing: Fact = f.error("NameMissing")
  val OrphanRoute: Fact = f.other("OrphanRoute")
  val OrphanNode: Fact = f.other("OrphanNode")

  val RouteOverlappingWays: Fact = f.error("RouteOverlappingWays")
  val RouteSuspiciousWays: Fact = f.error("RouteSuspiciousWays")
  val RouteAnalysisFailed: Fact = f.error("RouteAnalysisFailed")

  // informational
  val RouteIncomplete: Fact = f.info("RouteIncomplete")
  val RouteInaccessible: Fact = f.info("RouteInaccessible")
  val RouteInvalidSortingOrder: Fact = f.info("RouteInvalidSortingOrder")

  val RouteNodeNameMismatch: Fact = f.info("RouteNodeNameMismatch")
  val RouteNameDeprecatedNoteTag: Fact = f.info("RouteNameDeprecatedNoteTag")
  val RouteOneWay: Fact = f.info("RouteOneWay")
  val RouteNotOneWay: Fact = f.info("RouteNotOneWay")
  val RouteIncompleteOk: Fact = f.info("RouteIncompleteOk")

  // other
  val RouteBroken: Fact = f.other("RouteBroken")

  val IntegrityCheck: Fact = f.other("IntegrityCheck")

  val Added: Fact = f.other("Added")
  val Deleted: Fact = f.other("Deleted")
  val LostHikingNodeTag: Fact = f.other("LostHikingNodeTag")
  val LostBicycleNodeTag: Fact = f.other("LostBicycleNodeTag")
  val LostRouteTags: Fact = f.other("LostRouteTags")

  val LostHorseNodeTag: Fact = f.other("LostHorseNodeTag")
  val LostMotorboatNodeTag: Fact = f.other("LostMotorboatNodeTag")
  val LostCanoeNodeTag: Fact = f.other("LostCanoeNodeTag")
  val LostInlineSkateNodeTag: Fact = f.other("LostInlineSkateNodeTag")

  val NodeInvalidSurveyDate: Fact = f.error("NodeInvalidSurveyDate")
  val RouteInvalidSurveyDate: Fact = f.error("RouteInvalidSurveyDate")
  val NetworkInvalidSurveyDate: Fact = f.error("NetworkInvalidSurveyDate")

  val all: Seq[Fact] = f.all

  val locationFacts: Seq[Fact] = Seq(
    RouteNotContinious,
    RouteNotForward,
    RouteNotBackward,
    RouteUnusedSegments,
    RouteNodeMissingInWays,
    RouteRedundantNodes,
    RouteFixmetodo,
    RouteWithoutWays,
    RouteNameMissing,
    RouteTagMissing,
    RouteTagInvalid,
    RouteUnexpectedNode,
    RouteUnexpectedRelation,
    RouteSuspiciousWays,
    RouteAnalysisFailed,
    RouteIncomplete,
    RouteInaccessible,
    RouteInvalidSortingOrder, //
    RouteNodeNameMismatch,
    RouteNameDeprecatedNoteTag,
    RouteOneWay,
    RouteNotOneWay,
    RouteIncompleteOk,
    RouteBroken,
    Added,
    Deleted,
    LostHikingNodeTag,
    LostBicycleNodeTag,
    LostRouteTags,
    LostHorseNodeTag,
    LostMotorboatNodeTag,
    LostCanoeNodeTag,
    LostInlineSkateNodeTag,
    UnexpectedIntegrityCheck,
    NodeInvalidSurveyDate,
    RouteInvalidSurveyDate,
  )

  val networkFactsWithElementIds: Seq[Fact] = Seq(Fact.NetworkExtraMemberNode, Fact.NetworkExtraMemberWay, Fact.NetworkExtraMemberRelation)
  val networkFactsWithRefs: Seq[Fact] = Seq(Fact.NodeMemberMissing)

  val routeDetailFacts: Seq[Fact] = all.filterNot(f => f == RouteBroken || f == RouteNotContinious)

  def withName(factName: String): Option[Fact] = {
    all.find(fact => fact.name == factName)
  }

  val errorFacts: Seq[Fact] = all.filter(_.isError).
    filterNot(_ == RouteNotForward).
    filterNot(_ == RouteNotBackward)

  val infoFacts: Seq[Fact] = all.filter(_.isInfo)

  val reportedFacts: Seq[Fact] = errorFacts ++ infoFacts

  val reportedFactNames: Seq[String] = reportedFacts.map(_.name)

  /*
    Determines whether a given fact should be reported during route analysis, depending on
    other facts that have already been discovered. For some facts it does not make sense to
    report it, if another fact has already been discovered.
   */
  def canReport(fact: Fact, otherFacts: Seq[Fact]): Boolean = {

    def reportIfNotIn(mutuallyExclusiveFacts: Fact*): Boolean = {
      !otherFacts.exists(mutuallyExclusiveFacts.contains)
    }

    fact match {
      case RouteUnusedSegments => reportIfNotIn(RouteNodeMissingInWays, RouteWithoutWays, RouteIncomplete, RouteNotForward, RouteNotBackward)
      case RouteNotContinious => reportIfNotIn(RouteNodeMissingInWays, RouteNodeMissingInWays, RouteWithoutWays, RouteIncomplete)
      case RouteNotForward => reportIfNotIn(RouteNodeMissingInWays, RouteWithoutWays)
      case RouteNotBackward => reportIfNotIn(RouteNodeMissingInWays, RouteWithoutWays)
      case RouteNodeMissingInWays => reportIfNotIn(RouteWithoutWays, RouteIncomplete)
      case RouteRedundantNodes => reportIfNotIn(RouteNodeMissingInWays, RouteWithoutWays, RouteIncomplete)
      case _ => true
    }
  }
}
