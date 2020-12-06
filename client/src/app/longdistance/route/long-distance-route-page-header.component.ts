import {ChangeDetectionStrategy} from '@angular/core';
import {Component, Input} from '@angular/core';
import {Store} from '@ngrx/store';
import {AppState} from '../../core/core.state';
import {selectLongDistanceRouteName} from '../../core/longdistance/long-distance.selectors';

@Component({
  selector: 'kpn-long-distance-route-page-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="breadcrumb">
      <li><a routerLink="/" i18n="@@breadcrumb.home">Home</a></li>
      <li><a routerLink="/long-distance/routes">Long distance routes</a></li>
      <li>Route</li>
    </ul>

    <h1 class="title">
      {{routeName$ | async}}
    </h1>

    <kpn-page-menu>
      <kpn-page-menu-option
        [link]="'/long-distance/routes/' + routeId"
        [active]="pageName === 'details'">
        Details
      </kpn-page-menu-option>

      <kpn-page-menu-option
        [link]="'/long-distance/routes/' + routeId + '/map'"
        [active]="pageName === 'map'">
        Map
      </kpn-page-menu-option>

      <kpn-page-menu-option
        [link]="'/long-distance/routes/' + routeId + '/changes'"
        [active]="pageName === 'changes'">
        Changes
      </kpn-page-menu-option>

    </kpn-page-menu>
  `,
  styles: [`
    .title {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  `]
})
export class LongDistanceRoutePageHeaderComponent {

  @Input() pageName: string;
  @Input() routeId: number;
  @Input() pageTitle: string;

  routeName$ = this.store.select(selectLongDistanceRouteName);

  constructor(private store: Store<AppState>) {
  }

}
