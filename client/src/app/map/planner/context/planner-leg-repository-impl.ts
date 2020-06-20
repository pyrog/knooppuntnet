import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {AppService} from "../../../app.service";
import {PlanLeg} from "../plan/plan-leg";
import {PlanLegBuilder} from "../plan/plan-leg-builder";
import {PlanNode} from "../plan/plan-node";
import {ViaRoute} from "../plan/via-route";
import {PlannerLegRepository} from "./planner-leg-repository";

export class PlannerLegRepositoryImpl implements PlannerLegRepository {

  constructor(private appService: AppService) {
  }

  planLeg(networkType: string, legId: string, source: PlanNode, sink: PlanNode): Observable<PlanLeg> {
    return this.appService.routeLeg(networkType, legId, source.nodeId, sink.nodeId).pipe(
      map(response => PlanLegBuilder.toPlanLeg(source, sink, response.result))
    );
  }

  planLegViaRoute(networkType: string, legId: string, source: PlanNode, sink: PlanNode, viaRoute: ViaRoute): Observable<PlanLeg> {
    return this.appService.routeLegViaRoute(networkType, legId, source.nodeId, sink.nodeId, viaRoute).pipe(
      map(response => PlanLegBuilder.toPlanLeg(source, sink, response.result))
    );
  }

}
