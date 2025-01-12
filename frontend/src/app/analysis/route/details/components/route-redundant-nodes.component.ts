import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { RouteInfoAnalysis } from '@api/common/route';
import { RouteNodeComponent } from './route-node.component';

@Component({
  selector: 'kpn-route-redundant-nodes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (node of analysis().map.redundantNodes; track node) {
      <p>
        <kpn-route-node [node]="node" title="marker-icon-yellow-small.png" />
      </p>
    }
  `,
  standalone: true,
  imports: [RouteNodeComponent],
})
export class RouteRedundantNodesComponent {
  analysis = input.required<RouteInfoAnalysis>();
}
