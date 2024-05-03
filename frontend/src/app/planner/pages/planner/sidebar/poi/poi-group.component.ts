import { inject } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { PoiService } from '@app/services';
import { ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'kpn-poi-group',
  // TODO changeDetection: ChangeDetectionStrategy.OnPush,
  changeDetection: ChangeDetectionStrategy.Default,
  template: `
    <mat-expansion-panel>
      <mat-expansion-panel-header>
        <mat-panel-title>
          <mat-checkbox
            (click)="$event.stopPropagation()"
            [checked]="isEnabled()"
            (change)="groupEnabledChanged($event)"
          />
          <span class="title">{{ title() }}</span>
          <span class="kpn-thin">(10/10)</span>
        </mat-panel-title>
      </mat-expansion-panel-header>
      <ng-template matExpansionPanelContent>
        <div></div>

        <div>
          <button mat-stroked-button (click)="showAllClicked()" i18n="@@planner.pois.show-all">
            Show all
          </button>
          <button mat-stroked-button (click)="hideAllClicked()" i18n="@@planner.pois.hide-all">
            Hide all
          </button>
          <button mat-stroked-button (click)="defaultClicked()" i18n="@@planner.pois.default">
            Default
          </button>
        </div>

        <div>
          <!-- eslint-disable @angular-eslint/template/i18n -->
          <div class="col-spacer"></div>
          <div class="col-level-0">NO</div>
          <div class="col-level-11">11</div>
          <div class="col-level-12">12</div>
          <div class="col-level-13">13</div>
          <div class="col-level-14">14</div>
          <div class="col-level-15">15</div>
          <!-- eslint-enable @angular-eslint/template/i18n -->
        </div>
        <ng-content />
      </ng-template>
    </mat-expansion-panel>
  `,
  styles: `
    .title {
      padding-left: 10px;
      padding-right: 20px;
    }
  `,
  standalone: true,
  imports: [MatExpansionModule, MatCheckboxModule, MatButtonModule],
})
export class PoiGroupComponent {
  name = input.required<string>();
  title = input.required<string>();

  private readonly poiService = inject(PoiService);

  isEnabled(): boolean {
    return this.poiService.isGroupEnabled(this.name());
  }

  groupEnabledChanged(event: MatCheckboxChange) {
    this.poiService.updateGroupEnabled(this.name(), event.checked);
  }

  showAllClicked() {
    this.poiService.updateGroupShowAll(this.name());
  }

  hideAllClicked() {
    this.poiService.updateGroupHideAll(this.name());
  }

  defaultClicked() {
    this.poiService.updateGroupDefault(this.name());
  }
}
