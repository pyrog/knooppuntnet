import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { MonitorRouteDetailsPage } from '@api/common/monitor';
import { DistancePipe } from '@app/components/shared/format';
import { SymbolComponent } from '@app/symbol';
import { ActionButtonRelationComponent } from '../../../analysis/components/action/action-button-relation.component';

@Component({
  selector: 'kpn-monitor-route-details-summary',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (!page().relationId) {
      <p i18n="@@monitor.route.details.relation-id-undefined">
        Route relation has not been defined yet
      </p>
    } @else {
      <p class="kpn-space-separated">
        <span>{{ page().wayCount }}</span>
        <span i18n="@@monitor.route.details.ways">ways</span>
      </p>

      <p>{{ page().osmDistance | distance }}</p>

      @if (page().relationCount > 1) {
        <p class="kpn-small-spacer-above" i18n="@@monitor.route.details.relations">
          {{ page().relationCount }} relations in {{ page().relationLevels }} levels
        </p>
      }

      <div class="kpn-align-center">
        <span>{{ page().relationId }}</span>
        <kpn-action-button-relation [relationId]="page().relationId" />
      </div>

      @if (page().symbol) {
        <div class="kpn-small-spacer-above">
          <kpn-symbol [description]="page().symbol" />
        </div>
      }
    }
  `,
  standalone: true,
  imports: [ActionButtonRelationComponent, DistancePipe, SymbolComponent],
})
export class MonitorRouteDetailsSummaryComponent {
  page = input.required<MonitorRouteDetailsPage>();
}
