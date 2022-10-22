import { OnInit } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { Country } from '@api/custom/country';
import { LocationNode } from '@app/kpn/api/common/location/location-node';
import { Store } from '@ngrx/store';
import { AppState } from '../../core/core.state';
import { actionLocationPoiSummaryPageInit } from '../store/poi.actions';
import { selectLocationPoiSummaryPage } from '../store/poi.selectors';

@Component({
  selector: 'kpn-location-pois-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-sidebar>
      <div class="location-selector">
        <kpn-country-selector
          (country)="countryChanged($event)"
        ></kpn-country-selector>
        <kpn-location-selector
          [country]="country"
          [locationNode]="locationNode"
          (selection)="locationSelectionChanged($event)"
        ></kpn-location-selector>
      </div>

      <div *ngIf="response$ | async as response" class="filter">
        <div *ngIf="response.result as page">
          <div *ngFor="let group of page.groups">
            <div>
              <mat-checkbox>
                <span class="poi-group-name">{{ group.name }}</span>
              </mat-checkbox>
            </div>
            <div class="poi-group-body">
              <div *ngFor="let poiCount of group.poiCounts">
                <mat-checkbox>
                  <span class="poi-name">{{ poiCount.name }}</span>
                  <span class="poi-count">{{ poiCount.count }}</span>
                </mat-checkbox>
              </div>
            </div>
          </div>
        </div>
      </div>
    </kpn-sidebar>
  `,
  styles: [
    `
      .location-selector {
        padding: 15px;
      }

      .filter {
        padding: 25px 15px 25px 25px;
      }

      .poi-group-name {
        display: inline-block;
        font-weight: bold;
      }

      .poi-group-body {
        padding-top: 5px;
        padding-left: 25px;
        padding-bottom: 20px;
      }

      .poi-name {
        display: inline-block;
        width: 160px;
        max-width: 160px;
        word-wrap: break-word;
      }

      .poi-count {
        display: inline-flex;
        width: 50px;
        justify-content: right;
      }
    `,
  ],
})
export class LocationPoisSidebarComponent implements OnInit {
  readonly response$ = this.store.select(selectLocationPoiSummaryPage);

  country = Country.be;
  locationNode: LocationNode = null;

  constructor(private store: Store<AppState>) {}

  ngOnInit(): void {
    this.store.dispatch(actionLocationPoiSummaryPageInit());
  }

  countryChanged(country: Country): void {
    console.log('country changed: ' + country);
  }

  locationSelectionChanged(location: string): void {
    console.log('locationSelectionChanged: ' + location);
  }
}