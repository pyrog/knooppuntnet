import {List} from "immutable";
import {Plan} from "../../../kpn/api/common/planner/plan";
import {PlanLeg} from "../../../kpn/api/common/planner/plan-leg";
import {PlanNode} from "../../../kpn/api/common/planner/plan-node";

class PrintPlanNode {

  private constructor(readonly featureId: string,
                      readonly nodeName: string) {
  }

  static from(planNode: PlanNode): PrintPlanNode {
    if (planNode === null) {
      return null;
    }
    return new PrintPlanNode(planNode.featureId, planNode.nodeName);
  }
}

class PrintPlanLeg {

  constructor(readonly featureId: string,
              readonly source: PrintPlanNode,
              readonly sink: PrintPlanNode) {
  }

  static from(leg: PlanLeg): PrintPlanLeg {
    return new PrintPlanLeg(
      leg.featureId,
      PrintPlanNode.from(leg.sourceNode),
      PrintPlanNode.from(leg.sinkNode)
    );
  }
}

export class PrintPlan {

  constructor(readonly  source: PrintPlanNode,
              readonly  legs: List<PrintPlanLeg>) {
  }

  static from(plan: Plan): PrintPlan {
    return new PrintPlan(
      PrintPlanNode.from(plan.sourceNode),
      plan.legs.map(leg => PrintPlanLeg.from(leg))
    );
  }
}