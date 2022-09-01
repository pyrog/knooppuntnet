import { OnInit } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { Store } from '@ngrx/store';
import { map } from 'rxjs';
import { selectRouteParam } from '../../../core/core.state';
import { selectDefined } from '../../../core/core.state';
import { AppState } from '../../../core/core.state';
import { MonitorService } from '../../monitor.service';
import { actionMonitorRouteUpdatePageInit } from '../../store/monitor.actions';
import { selectMonitorRouteDescription } from '../../store/monitor.selectors';
import { selectMonitorRouteName } from '../../store/monitor.selectors';
import { selectMonitorGroupName } from '../../store/monitor.selectors';
import { selectMonitorRouteUpdatePage } from '../../store/monitor.selectors';

@Component({
  selector: 'kpn-monitor-route-update-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="breadcrumb">
      <li><a routerLink="/" i18n="@@breadcrumb.home">Home</a></li>
      <li><a routerLink="/monitor">Monitor</a></li>
      <li>
        <a [routerLink]="groupLink$ | async">{{ groupName$ | async }}</a>
      </li>
      <li>Route</li>
    </ul>

    <h1>
      <span>{{ routeName$ | async }}</span>
      <span *ngIf="routeDescription$ | async as routeDescription"
        >: {{ routeDescription }}</span
      >
    </h1>

    <h2>Update route</h2>

    <div *ngIf="response$ | async as response">
      <kpn-monitor-route-properties
        mode="update"
        [groupName]="groupName$ | async"
        [initialProperties]="response.result.properties"
        [routeGroups]="response.result.groups"
      >
      </kpn-monitor-route-properties>
    </div>
  `,
})
export class MonitorRouteUpdatePageComponent implements OnInit {
  readonly response$ = selectDefined(this.store, selectMonitorRouteUpdatePage);
  readonly groupName$ = this.store.select(selectMonitorGroupName);
  readonly routeName$ = this.store.select(selectMonitorRouteName);
  readonly routeDescription$ = this.store.select(selectMonitorRouteDescription);
  readonly groupLink$ = this.groupName$.pipe(
    map((groupName) => `/monitor/groups/${groupName}`)
  );

  constructor(
    private monitorService: MonitorService,
    private store: Store<AppState>
  ) {}

  ngOnInit(): void {
    this.store.dispatch(actionMonitorRouteUpdatePageInit());
  }
}
