import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { PlanRoute } from '@api/common/planner';
import { Plan } from '../../../domain/plan/plan';
import { PlanRouteColourUtil } from '../../../util/plan-route-colour-util';

@Component({
  selector: 'kpn-plan-compact',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (plan().sourceNode !== null) {
      <span class="node">
        {{ plan().sourceNode.nodeName }}
      </span>
    }
    @for (leg of plan().legs; track leg) {
      @for (legRoute of leg.routes; track legRoute; let i = $index) {
        @if (hasColour(legRoute)) {
          <span class="colour">
            {{ colours(legRoute) }}
          </span>
        }
        <span class="node" [class.visited-node]="i < leg.routes.size - 1">
          {{ legRoute.sinkNode.nodeName }}
        </span>
      }
    }
  `,
  styles: `
    .node {
      padding-right: 5px;
      font-weight: bold;
    }

    .visited-node {
      font-weight: normal;
    }

    .colour {
      padding-right: 5px;
      color: rgba(0, 0, 0, 0.75);
    }
  `,
  standalone: true,
  imports: [],
})
export class PlanCompactComponent {
  plan = input.required<Plan>();

  hasColour(planRoute: PlanRoute): boolean {
    return PlanRouteColourUtil.hasColour(planRoute);
  }

  colours(planRoute: PlanRoute): string {
    return PlanRouteColourUtil.colours(planRoute);
  }
}
