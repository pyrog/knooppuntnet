import { Input } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { MonitorRouteDetailsPage } from '@api/common/monitor/monitor-route-details-page';

@Component({
  selector: 'kpn-monitor-route-details-analysis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p *ngIf="page.happy" class="kpn-line">
      <span i18n="@@monitor.route.details.analysis.ok">All ok</span>
      <kpn-icon-happy></kpn-icon-happy>
    </p>
    <div *ngIf="!page.happy">
      <p>
        <span>{{ page.deviationCount + ' ' }}</span>
        <span i18n="@@monitor.route.details.analysis.deviations"
          >deviations</span
        >
        <span class="kpn-brackets">
          <span>{{ page.deviationDistance | distance }}</span>
        </span>
      </p>
      <p>
        <span>{{ page.osmSegmentCount + ' ' }}</span>
        <span i18n="@@monitor.route.details.analysis.osm-segments"
          >OSM segment(s)</span
        >
      </p>
    </div>
  `,
})
export class MonitorRouteDetailsAnalysisComponent {
  @Input() page: MonitorRouteDetailsPage;
}