import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {LegEnd} from "../../../../kpn/api/common/planner/leg-end";
import {PlanNode} from "../../../../kpn/api/common/planner/plan-node";
import {PlannerCommandMoveFirstLegSource} from "../../commands/planner-command-move-first-leg-source";
import {PlannerContext} from "../../context/planner-context";
import {FeatureId} from "../../features/feature-id";
import {PlanFlag} from "../../plan/plan-flag";
import {PlanFlagType} from "../../plan/plan-flag-type";
import {PlanLeg} from "../../plan/plan-leg";
import {PlanUtil} from "../../plan/plan-util";

export class MoveFirstLegSource {

  constructor(private readonly context: PlannerContext) {
  }

  move(newSourceNode: PlanNode): void {

    const oldLeg: PlanLeg = this.context.plan.legs.get(0, null);
    const newSource = PlanUtil.legEndNode(+newSourceNode.nodeId);

    this.buildNewLeg(newSource, oldLeg).subscribe(newLeg => {
      if (newLeg) {
        const oldSourceNode = this.context.plan.sourceNode;
        const oldSourceFlag = this.context.plan.sourceFlag;
        const newSourceFlag = oldSourceFlag.withCoordinate(newSourceNode.coordinate);
        const command = new PlannerCommandMoveFirstLegSource(
          oldLeg.featureId,
          oldSourceNode,
          oldSourceFlag,
          newLeg.featureId,
          newSourceNode,
          newSourceFlag
        );
        this.context.execute(command);
      }
    });
  }

  private buildNewLeg(source: LegEnd, oldLeg: PlanLeg): Observable<PlanLeg> {
    return this.context.legRepository.planLeg(this.context.networkType, source, oldLeg.sink).pipe(
      map(planLegDetail => {
        if (planLegDetail) {
          const lastRoute = planLegDetail.routes.last(null);
          if (lastRoute) {
            const legKey = PlanUtil.key(source, oldLeg.sink);
            const sinkFlag = new PlanFlag(PlanFlagType.End, FeatureId.next(), lastRoute.sinkNode.coordinate);
            const newLeg = new PlanLeg(FeatureId.next(), legKey, source, oldLeg.sink, sinkFlag, oldLeg.viaFlag, planLegDetail.routes);
            this.context.legs.add(newLeg);
            return newLeg;
          }
        }
        return null;
      })
    );
  }

}
