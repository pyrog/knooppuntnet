import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { BarChart } from '@api/common/status';
import { ActionBarChartComponent } from './action-bar-chart.component';

@Component({
  selector: 'kpn-replication-delay-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <!-- English only-->
    <!-- eslint-disable @angular-eslint/template/i18n -->
    <h2>Replication average delay</h2>
    <div class="chart">
      <kpn-action-bar-chart
        [barChart]="barChart()"
        [xAxisLabel]="xAxisLabel()"
        yAxisLabel="Average delay"
      />
    </div>
  `,
  standalone: true,
  imports: [ActionBarChartComponent],
})
export class ReplicationDelayChartComponent {
  barChart = input.required<BarChart>();
  xAxisLabel = input.required<string>();
}
