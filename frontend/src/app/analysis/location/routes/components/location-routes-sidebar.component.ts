import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { MatRadioChange } from '@angular/material/radio';
import { MatRadioModule } from '@angular/material/radio';
import { LocationRoutesType } from '@api/custom';
import { SidebarComponent } from '@app/components/shared/sidebar';
import { LocationRoutesPageService } from '../location-routes-page.service';

@Component({
  selector: 'kpn-location-routes-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-sidebar>
      @if (store.response(); as response) {
        <div class="filter">
          <div class="title" i18n="@@location-routes-sidebar.filter.title">Filter</div>
          <mat-radio-group
            [value]="locationRoutesType.all"
            (change)="locationRoutesTypeChanged($event)"
          >
            <div>
              <mat-radio-button [value]="locationRoutesType.all">
                <span i18n="@@location-routes-sidebar.filter.all">All</span
                ><span class="kpn-brackets">{{ response.result.allRouteCount }}</span>
              </mat-radio-button>
            </div>
            <div>
              <mat-radio-button [value]="locationRoutesType.facts">
                <span i18n="@@location-routes-sidebar.filter.facts">Facts</span
                ><span class="kpn-brackets">{{ response.result.factsRouteCount }}</span>
              </mat-radio-button>
            </div>
            <div>
              <mat-radio-button [value]="locationRoutesType.inaccessible">
                <span i18n="@@location-routes-sidebar.filter.inaccessible">Inaccessible</span
                ><span class="kpn-brackets">{{ response.result.inaccessibleRouteCount }}</span>
              </mat-radio-button>
            </div>
            <div>
              <mat-radio-button [value]="locationRoutesType.survey">
                <span i18n="@@location-routes-sidebar.filter.survey">Survey</span
                ><span class="kpn-brackets">{{ response.result.surveyRouteCount }}</span>
              </mat-radio-button>
            </div>
          </mat-radio-group>
        </div>
      }
    </kpn-sidebar>
  `,
  styles: `
    .filter {
      padding: 25px 15px 25px 25px;
    }

    .title {
      padding-bottom: 10px;
    }
  `,
  standalone: true,
  imports: [SidebarComponent, MatRadioModule],
})
export class LocationRoutesSidebarComponent {
  protected readonly store = inject(LocationRoutesPageService);
  protected readonly locationRoutesType = LocationRoutesType;

  locationRoutesTypeChanged(change: MatRadioChange): void {
    const locationRoutesType = change.value as LocationRoutesType;
    this.store.setPageType(locationRoutesType);
  }
}
