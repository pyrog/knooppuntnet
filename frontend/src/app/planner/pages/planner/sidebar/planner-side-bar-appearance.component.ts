import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatRadioChange } from '@angular/material/radio';
import { MatRadioModule } from '@angular/material/radio';
import { PreferencesService } from '@app/core';
import { PlannerStateService } from '../planner-state.service';
import { PlannerPageService } from '../planner-page.service';

@Component({
  selector: 'kpn-planner-sidebar-appearance',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-expansion-panel [expanded]="expanded()" (expandedChange)="expandedChanged($event)">
      <mat-expansion-panel-header i18n="@@planner.appearance-options">
        Map appearance options
      </mat-expansion-panel-header>
      <ng-template matExpansionPanelContent>
        <mat-radio-group [value]="mapMode()" (change)="modeChanged($event)">
          <div>
            <mat-radio-button value="surface" class="mode-radio-button" i18n="@@planner.surface">
              Surface
            </mat-radio-button>
          </div>
          <div>
            <mat-radio-button value="survey" class="mode-radio-button" i18n="@@planner.survey">
              Date last survey
            </mat-radio-button>
          </div>
          <div>
            <mat-radio-button value="analysis" class="mode-radio-button" i18n="@@planner.quality">
              Node and route quality status
            </mat-radio-button>
          </div>
        </mat-radio-group>
      </ng-template>
    </mat-expansion-panel>
  `,
  standalone: true,
  imports: [MatExpansionModule, MatRadioModule],
})
export class PlannerSideBarAppearanceComponent {
  private readonly plannerStateService = inject(PlannerStateService);
  private readonly plannerPageService = inject(PlannerPageService);
  private readonly preferencesService = inject(PreferencesService);
  protected readonly mapMode = this.plannerStateService.mapMode;
  protected readonly expanded = this.preferencesService.showAppearanceOptions;

  expandedChanged(expanded: boolean): void {
    this.preferencesService.setShowAppearanceOptions(expanded);
  }

  modeChanged(event: MatRadioChange): void {
    this.plannerPageService.setMapMode(event.value);
  }
}
