import {List} from "immutable";
import {PlannerContext} from "../context/planner-context";
import {Plan} from "../plan/plan";
import {PlanFlag} from "../plan/plan-flag";
import {PlanLeg} from "../plan/plan-leg";
import {PlannerCommand} from "./planner-command";

export class PlannerCommandRemoveViaPoint implements PlannerCommand {

  constructor(private readonly oldLegId1: string,
              private readonly oldLegId2: string,
              private readonly newLegId: string) {
  }

  public do(context: PlannerContext) {

    const oldLeg1 = context.legs.getById(this.oldLegId1);
    const oldLeg2 = context.legs.getById(this.oldLegId2);
    const newLeg = context.legs.getById(this.newLegId);

    context.routeLayer.removeFlag(oldLeg1.sink.featureId);
    context.routeLayer.removeRouteLeg(oldLeg1.featureId);
    context.routeLayer.removeRouteLeg(oldLeg2.featureId);
    context.routeLayer.addRouteLeg(newLeg);

    const newLegs: List<PlanLeg> = context.plan.legs
      .map(leg => leg.featureId === oldLeg1.featureId ? newLeg : leg)
      .filter(leg => leg.featureId !== oldLeg2.featureId);
    const newPlan = Plan.create(context.plan.source, newLegs);
    context.updatePlan(newPlan);
  }

  public undo(context: PlannerContext) {

    const oldLeg1 = context.legs.getById(this.oldLegId1);
    const oldLeg2 = context.legs.getById(this.oldLegId2);
    const newLeg = context.legs.getById(this.newLegId);

    context.routeLayer.addFlag(PlanFlag.fromViaNode(oldLeg1.sink));
    context.routeLayer.addRouteLeg(oldLeg1);
    context.routeLayer.addRouteLeg(oldLeg2);
    context.routeLayer.removeRouteLeg(newLeg.featureId);

    const index = context.plan.legs.findIndex(leg => leg.featureId === newLeg.featureId);
    if (index >= 0) {
      const newLegs1 = context.plan.legs.update(index, () => oldLeg1);
      const newLegs2 = newLegs1.insert(index + 1, oldLeg2);
      const newPlan = Plan.create(context.plan.source, newLegs2);
      context.updatePlan(newPlan);
    }
  }
}
