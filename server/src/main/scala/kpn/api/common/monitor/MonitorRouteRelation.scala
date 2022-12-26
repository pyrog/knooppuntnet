package kpn.api.common.monitor

import kpn.api.custom.Day
import kpn.api.custom.Relation
import kpn.core.util.Haversine
import kpn.server.analyzer.engine.analysis.common.SurveyDateAnalyzer

import scala.util.Failure
import scala.util.Success

object MonitorRouteRelation {

  def from(relation: Relation, role: Option[String]): MonitorRouteRelation = {

    val name = relation.tags("name")
    val from = relation.tags("from")
    val to = relation.tags("to")
    val survey = SurveyDateAnalyzer.analyze(relation.tags) match {
      case Success(surveyDate) => surveyDate
      case Failure(_) => None
    }
    val osmWayCount = relation.wayMembers.size
    val osmDistance = relation.wayMembers.map(w => Haversine.meters(w.way.nodes)).sum

    val relations = relation.relationMembers.filterNot(_.role.contains("place_of_worship")).map { member =>
      MonitorRouteRelation.from(member.relation, member.role)
    }

    val subRelationsDistance = relations.map(_.osmDistance).sum

    MonitorRouteRelation(
      relationId = relation.id,
      name = name,
      from = from,
      to = to,
      role = role,
      survey = survey,
      deviationDistance = 0,
      deviationCount = 0,
      osmWayCount = osmWayCount,
      osmDistance = osmDistance + subRelationsDistance,
      osmSegmentCount = 0,
      happy = false,
      relations = relations
    )
  }
}

case class MonitorRouteRelation(
  relationId: Long,
  name: Option[String],
  from: Option[String],
  to: Option[String],
  role: Option[String],
  survey: Option[Day],
  deviationDistance: Long,
  deviationCount: Long,
  osmWayCount: Long,
  osmDistance: Long,
  osmSegmentCount: Long,
  happy: Boolean,
  relations: Seq[MonitorRouteRelation]
)