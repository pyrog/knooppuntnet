package kpn.api.common.planner

case class Plan(
  sourceNode: PlanNode,
  legs: List[PlanLeg]
)