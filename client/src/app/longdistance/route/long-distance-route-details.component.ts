import {ChangeDetectionStrategy} from '@angular/core';
import {Component} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Store} from '@ngrx/store';
import {AppState} from '../../core/core.state';
import {selectLongDistanceRouteId} from '../../core/longdistance/long-distance.selectors';
import {selectLongDistanceRouteDetails} from '../../core/longdistance/long-distance.selectors';

@Component({
  selector: 'kpn-long-distance-route-details',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-long-distance-route-page-header pageName="details" [routeId]="routeId$ | async"></kpn-long-distance-route-page-header>

    <div *ngIf="response$ | async as response" class="kpn-spacer-above">

      <div *ngIf="!response.result">
        Route not found
      </div>

      <div *ngIf="response.result as route">

        <kpn-data title="Summary">
          <p *ngIf="route.ref">{{route.ref}}</p>
          <p>{{route.name}}</p>
          <p class="kpn-separated">
            <kpn-osm-link-relation [relationId]="route.id"></kpn-osm-link-relation>
            <kpn-josm-relation [relationId]="route.id"></kpn-josm-relation>
          </p>
          <p *ngIf="route.website">
            <a href="{{route.website}}" target="_blank" rel="nofollow noreferrer" class="external">website</a>
          </p>
        </kpn-data>

        <kpn-data title="Operator">
          {{route.operator}}
        </kpn-data>

        <kpn-data title="OSM">
          <p>
            {{route.wayCount}} ways
          </p>
          <p>
            {{route.osmDistance}}km
          </p>
        </kpn-data>

        <kpn-data title="GPX">
          <p>
            {{route.gpxFilename}}
          </p>
          <p>
            {{route.gpxDistance}}km
          </p>
        </kpn-data>

        <kpn-data title="Analysis">
          <p *ngIf="route.happy">
            <span>All ok</span>
          </p>
          <div *ngIf="!route.gpxFilename && route.osmSegmentCount == 1 && route.gpxNokSegmentCount == 0">
            <p>No GPX, so no known deviations.</p>
            <p>The OSM route looks ok: a GPX trace can be created from it.</p>
          </div>
          <div *ngIf="route.osmSegmentCount > 1" class="kpn-line warning-line">
            <div>
              <mat-icon svgIcon="warning" class="warning-icon"></mat-icon>
            </div>
            <span>The OSM route relation contains {{route.osmSegmentCount}} segments. It will not be possible to create a GPX trace from it.</span>
          </div>
          <div *ngIf="route.gpxNokSegmentCount > 0" class="kpn-line warning-line">
            <div>
              <mat-icon svgIcon="warning" class="warning-icon"></mat-icon>
            </div>
            <span>There are {{route.gpxNokSegmentCount}} segments in the GPX trace where the distance to the closest OSM way is more than 10 meters.</span>
          </div>
        </kpn-data>

        <div class="buttons">
          <button mat-raised-button color="primary" (click)="gpxDownload()">Download GPX file</button>
          <button mat-raised-button (click)="gpxUpload()">Upload GPX file</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .buttons {
      padding-top: 2em;
      display: flex;
    }

    .buttons :not(:last-child) {
      margin-right: 1em;
    }

    .warning-line {
      padding-bottom: 1em;
    }

    .warning-icon {
      width: 2em;
      height: 2em;
    }
  `]
})
export class LongDistanceRouteDetailsComponent {

  routeId$ = this.store.select(selectLongDistanceRouteId);
  response$ = this.store.select(selectLongDistanceRouteDetails);

  constructor(private snackBar: MatSnackBar,
              private store: Store<AppState>) {
  }

  gpxUpload(): void {
    this.snackBar.open(
      'Sorry, GPX file upload not implemented yet',
      'close',
      {panelClass: ['mat-toolbar', 'mat-primary']}
    );
  }

  gpxDownload(): void {
    this.snackBar.open(
      'Sorry, GPX file download not implemented yet',
      'close',
      {panelClass: ['mat-toolbar', 'mat-primary']}
    );
  }

}