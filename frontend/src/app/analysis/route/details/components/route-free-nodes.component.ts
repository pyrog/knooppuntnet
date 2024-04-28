import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { RouteInfoAnalysis } from '@api/common/route';
import { RouteNodeComponent } from './route-node.component';

@Component({
  selector: 'kpn-route-free-nodes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (node of analysis().map.freeNodes; track node) {
      <p>
        <kpn-route-node [node]="node" title="marker-icon-blue-small.png" />
      </p>
    } @empty {
      <!-- eslint-disable @angular-eslint/template/i18n -->
      <p>?</p>
    }
  `,
  standalone: true,
  imports: [RouteNodeComponent],
})
export class RouteFreeNodesComponent {
  analysis = input.required<RouteInfoAnalysis>();
}
