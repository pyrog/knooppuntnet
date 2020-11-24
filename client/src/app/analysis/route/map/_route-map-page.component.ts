import {ChangeDetectionStrategy} from '@angular/core';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Subject} from 'rxjs';
import {Observable} from 'rxjs';
import {map, mergeMap, tap} from 'rxjs/operators';
import {AppService} from '../../../app.service';
import {PageService} from '../../../components/shared/page.service';
import {RouteMapPage} from '../../../kpn/api/common/route/route-map-page';
import {ApiResponse} from '../../../kpn/api/custom/api-response';
import {Store} from '@ngrx/store';
import {AppState} from '../../../core/core.state';
import * as PreferencesActions from '../../../core/preferences/preferences.actions';

@Component({
  selector: 'kpn-route-changes-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="breadcrumb">
      <li><a routerLink="/" i18n="@@breadcrumb.home">Home</a></li>
      <li><a routerLink="/analysis" i18n="@@breadcrumb.analysis">Analysis</a></li>
      <li i18n="@@breadcrumb.route-map">Route map</li>
    </ul>

    <kpn-route-page-header
      pageName="map"
      [routeId]="routeId$ | async"
      [routeName]="routeName$ | async"
      [changeCount]="changeCount$ | async">
    </kpn-route-page-header>

    <div *ngIf="response$ | async as response">
      <div *ngIf="!response.result" class="kpn-spacer-above" i18n="@@route.route-not-found">
        Route not found
      </div>
      <div *ngIf="response.result">
        <kpn-route-map [routeInfo]="response.result.route"></kpn-route-map>
      </div>
    </div>
  `
})
export class RouteMapPageComponent implements OnInit, OnDestroy {

  response$: Observable<ApiResponse<RouteMapPage>>;

  routeId$ = new Subject<string>();
  routeName$ = new Subject<string>();
  changeCount$ = new Subject<number>();

  constructor(private activatedRoute: ActivatedRoute,
              private appService: AppService,
              private pageService: PageService,
              private store: Store<AppState>) {
    this.pageService.showFooter = false;
  }

  ngOnInit(): void {
    this.routeName$.next(history.state.routeName);
    this.changeCount$.next(history.state.changeCount);
    this.response$ = this.activatedRoute.params.pipe(
      map(params => params['routeId']),
      tap(routeId => this.routeId$.next(routeId)),
      mergeMap(routeId => this.appService.routeMap(routeId).pipe(
        tap(response => {
          if (response.result) {
            this.routeName$.next(response.result.route.summary.name);
            this.store.dispatch(PreferencesActions.networkType({networkType: response.result.route.summary.networkType.name}));
            this.changeCount$.next(response.result.changeCount);
          }
        })
      ))
    );
  }

  ngOnDestroy(): void {
    this.pageService.showFooter = true;
  }
}
