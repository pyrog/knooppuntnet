import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { RouteInfoAnalysis } from '@api/common/route';
import { RouteNodeComponent } from './route-node.component';

@Component({
  selector: 'kpn-route-start-nodes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (node of analysis().map.startNodes; track node) {
      <p>
        <kpn-route-node [node]="node" title="marker-icon-green-small.png" />
      </p>
    } @empty {
      <!-- eslint-disable @angular-eslint/template/i18n -->
      <p>?</p>
    }

    @for (node of analysis().map.startTentacleNodes; track node) {
      <p>
        <kpn-route-node [node]="node" title="marker-icon-orange-small.png" />
      </p>
    }
  `,
  standalone: true,
  imports: [RouteNodeComponent],
})
export class RouteStartNodesComponent {
  analysis = input.required<RouteInfoAnalysis>();
}
