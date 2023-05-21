import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { MonitorChangesParameters } from '@api/common/monitor';
import { selectRouteParam } from '@app/core';
import { selectPreferencesPageSize } from '@app/core';
import { selectPreferencesImpact } from '@app/core';
import { concatLatestFrom } from '@ngrx/effects';
import { Actions } from '@ngrx/effects';
import { createEffect } from '@ngrx/effects';
import { ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { map } from 'rxjs/operators';
import { mergeMap } from 'rxjs/operators';
import { MonitorService } from '../monitor.service';
import { actionMonitorChangesPageIndex } from './monitor.actions';
import { actionMonitorRouteChangesPageIndex } from './monitor.actions';
import { actionMonitorGroupChangesPageIndex } from './monitor.actions';
import { actionMonitorChangesPageLoaded } from './monitor.actions';
import { actionMonitorChangesPageInit } from './monitor.actions';
import { actionMonitorGroupChangesPageLoaded } from './monitor.actions';
import { actionMonitorGroupChangesPageInit } from './monitor.actions';
import { actionMonitorRouteChangesPageInit } from './monitor.actions';
import { actionMonitorRouteChangePageInit } from './monitor.actions';
import { actionMonitorRouteChangePageLoaded } from './monitor.actions';
import { actionMonitorRouteChangesPageLoaded } from './monitor.actions';
import { selectMonitorChangesPageIndex } from './monitor.selectors';
import { selectMonitorGroupChangesPageIndex } from './monitor.selectors';

@Injectable()
export class MonitorEffects {
  // noinspection JSUnusedGlobalSymbols
  monitorGroupChangesPageInit = createEffect(() => {
    return this.actions$.pipe(
      ofType(
        actionMonitorGroupChangesPageInit,
        actionMonitorGroupChangesPageIndex
      ),
      concatLatestFrom(() => [
        this.store.select(selectRouteParam('groupName')),
        this.store.select(selectPreferencesPageSize),
        this.store.select(selectMonitorGroupChangesPageIndex),
        this.store.select(selectPreferencesImpact),
      ]),
      mergeMap(([_, groupName, pageSize, pageIndex, impact]) => {
        const parameters: MonitorChangesParameters = {
          pageSize,
          pageIndex,
          impact,
        };
        return this.monitorService
          .groupChanges(groupName, parameters)
          .pipe(
            map((response) => actionMonitorGroupChangesPageLoaded(response))
          );
      })
    );
  });

  // noinspection JSUnusedGlobalSymbols
  monitorRouteChangesPageInit = createEffect(() => {
    return this.actions$.pipe(
      ofType(
        actionMonitorRouteChangesPageInit,
        actionMonitorRouteChangesPageIndex
      ),
      concatLatestFrom(() => [
        this.store.select(selectRouteParam('monitorRouteId')),
        this.store.select(selectPreferencesPageSize),
        this.store.select(selectMonitorGroupChangesPageIndex),
        this.store.select(selectPreferencesImpact),
      ]),
      mergeMap(([_, monitorRouteId, pageSize, pageIndex, impact]) => {
        const parameters: MonitorChangesParameters = {
          pageSize,
          pageIndex,
          impact,
        };
        return this.monitorService
          .routeChanges(monitorRouteId, parameters)
          .pipe(
            map((response) => actionMonitorRouteChangesPageLoaded(response))
          );
      })
    );
  });

  // noinspection JSUnusedGlobalSymbols
  monitorRouteChangePageInit = createEffect(() => {
    return this.actions$.pipe(
      ofType(actionMonitorRouteChangePageInit),
      concatLatestFrom(() => [
        this.store.select(selectRouteParam('monitorRouteId')),
        this.store.select(selectRouteParam('changeSetId')),
        this.store.select(selectRouteParam('replicationNumber')),
      ]),
      mergeMap(([_, monitorRouteId, changeSetId, replicationNumber]) =>
        this.monitorService
          .routeChange(monitorRouteId, changeSetId, replicationNumber)
          .pipe(map((response) => actionMonitorRouteChangePageLoaded(response)))
      )
    );
  });

  // noinspection JSUnusedGlobalSymbols
  monitorChangesPageInit = createEffect(() => {
    return this.actions$.pipe(
      ofType(actionMonitorChangesPageInit, actionMonitorChangesPageIndex),
      concatLatestFrom(() => [
        this.store.select(selectPreferencesPageSize),
        this.store.select(selectMonitorChangesPageIndex),
        this.store.select(selectPreferencesImpact),
      ]),
      mergeMap(([_, pageSize, pageIndex, impact]) => {
        const parameters: MonitorChangesParameters = {
          pageSize,
          pageIndex,
          impact,
        };
        return this.monitorService
          .changes(parameters)
          .pipe(map((response) => actionMonitorChangesPageLoaded(response)));
      })
    );
  });

  constructor(
    private actions$: Actions,
    private store: Store,
    private router: Router,
    private monitorService: MonitorService
  ) {}
}