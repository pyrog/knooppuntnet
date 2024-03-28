import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { PreferencesService } from '@app/core';
import { PlannerStateService } from '../planner-state.service';
import { LegendIconComponent } from './legend-icon.component';

@Component({
  selector: 'kpn-planner-sidebar-legend',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-expansion-panel [expanded]="expanded()" (expandedChange)="expandedChanged($event)">
      <mat-expansion-panel-header i18n="@@planner.legend">Legend</mat-expansion-panel-header>

      @if (mapMode(); as mapMode) {
        @if (mapMode === 'surface') {
          <div class="legend">
            <div>
              <kpn-legend-icon color="rgb(0, 200, 0)" />
              <span i18n="@@planner.legend.paved">Paved</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(255, 165, 0)" />
              <span i18n="@@planner.legend.unpaved">Unpaved</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(0, 0, 200)" />
              <span i18n="@@planner.legend.surface-unknown">Surface unknown</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(0, 200, 0)" [proposed]="true" />
              <span i18n="@@planner.legend.proposed">Proposed</span>
            </div>
          </div>
        }
        @if (mapMode === 'survey') {
          <div class="legend">
            <div>
              <kpn-legend-icon color="rgb(0, 255, 0)" />
              <span i18n="@@planner.legend.survey.last-month">Last month</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(0, 200, 0)" />
              <span i18n="@@planner.legend.survey.last-half-month">Last half year</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(0, 150, 0)" />
              <span i18n="@@planner.legend.survey.last-year">Last year</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(0, 90, 0)" />
              <span i18n="@@planner.legend.survey.last-two-years">Last two years</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(150, 0, 0)" />
              <span i18n="@@planner.legend.survey.more-than-tow-years-ago"
                >More than two years ago</span
              >
            </div>
            <div>
              <kpn-legend-icon color="rgb(255, 255, 0)" circleColor="rgb(225, 225, 0)" />
              <span i18n="@@planner.legend.survey.unknown">Unknown</span>
            </div>
          </div>
        }
        @if (mapMode === 'analysis') {
          <div class="legend">
            <div>
              <kpn-legend-icon color="rgb(0, 200, 0)" />
              <span i18n="@@planner.legend.analysis.ok">OK</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(0, 150, 0)" />
              <span i18n="@@planner.legend.survey.ok-orphan">OK Orphan</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(255, 0, 0)" />
              <span i18n="@@planner.legend.survey.review">Review</span>
            </div>
            <div>
              <kpn-legend-icon color="rgb(187, 0, 0)" />
              <span i18n="@@planner.legend.survey.review-orphan">Review Orphan</span>
            </div>
          </div>
        }
        <div class="legend">
          <div>
            <div class="legend-icon">
              <img
                src="/assets/images/marker-icon-blue.png"
                class="image"
                alt="Start node icon"
                i18n-alt="@@planner.legend.marker.icon.start-node"
              />
            </div>
            <span i18n="@@planner.legend.marker.start-node">Start node</span>
          </div>
          <div>
            <div class="legend-icon">
              <img
                src="/assets/images/marker-icon-green.png"
                class="image"
                alt="End node icon"
                i18n-alt="@@planner.legend.marker.icon.end-node"
              />
            </div>
            <span i18n="@@planner.legend.marker.end-node">End node</span>
          </div>
          <div>
            <div class="legend-icon">
              <img
                src="/assets/images/marker-icon-orange.png"
                class="image"
                alt="Via node icon"
                i18n-alt="@@planner.legend.marker.icon.via-node"
              />
            </div>
            <span i18n="@@planner.legend.marker.via-node">Via node</span>
          </div>
        </div>
      }
    </mat-expansion-panel>
  `,
  styles: `
    .legend > div {
      display: flex;
      align-items: center;
    }

    .legend-icon {
      width: 60px;
      padding-right: 10px;
      text-align: center;
    }
  `,
  standalone: true,
  imports: [MatExpansionModule, LegendIconComponent],
})
export class PlannerSideBarLegendComponent {
  private readonly plannerStateService = inject(PlannerStateService);
  private readonly preferencesService = inject(PreferencesService);
  protected readonly expanded = this.preferencesService.showLegend;
  protected readonly mapMode = this.plannerStateService.mapMode;

  expandedChanged(expanded: boolean): void {
    this.preferencesService.setShowLegend(expanded);
  }
}
