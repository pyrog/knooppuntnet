import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { concatLatestFrom } from '@ngrx/effects';
import { Actions } from '@ngrx/effects';
import { createEffect } from '@ngrx/effects';
import { ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { map } from 'rxjs/operators';
import { mergeMap } from 'rxjs/operators';
import { selectQueryParam } from '../../core/core.state';
import { AppState } from '../../core/core.state';
import { selectRouteParam } from '../../core/core.state';
import { actionPreferencesPageSize } from '../../core/preferences/preferences.actions';
import { selectPreferencesPageSize } from '../../core/preferences/preferences.selectors';
import { PoiService } from '../poi.service';
import { actionLocationPoiSummaryPageLoaded } from './poi.actions';
import { actionLocationPoiSummaryPageInit } from './poi.actions';
import { actionLocationPoisPageIndex } from './poi.actions';
import { actionLocationPoisPageLoaded } from './poi.actions';
import { actionLocationPoisPageInit } from './poi.actions';
import { selectLocationPoisPageIndex } from './poi.selectors';

@Injectable()
export class PoiEffects {
  // noinspection JSUnusedGlobalSymbols
  locationPoisPageInit = createEffect(() =>
    this.actions$.pipe(
      ofType(
        actionLocationPoisPageInit,
        actionPreferencesPageSize,
        actionLocationPoisPageIndex
      ),
      concatLatestFrom(() => [
        this.store.select(selectRouteParam('country')),
        this.store.select(selectRouteParam('location')),
        this.store.select(selectQueryParam('layers')),
        this.store.select(selectPreferencesPageSize),
        this.store.select(selectLocationPoisPageIndex),
      ]),
      mergeMap(([{}, country, location, layers, pageSize, pageIndex]) =>
        this.poiService
          .locationPois(country, location, layers, pageSize, pageIndex)
          .pipe(map((response) => actionLocationPoisPageLoaded(response)))
      )
    )
  );

  // noinspection JSUnusedGlobalSymbols
  locationPoiSummaryPageInit = createEffect(() =>
    this.actions$.pipe(
      ofType(actionLocationPoiSummaryPageInit),
      concatLatestFrom(() => [
        this.store.select(selectRouteParam('country')),
        this.store.select(selectRouteParam('location')),
      ]),
      mergeMap(([{}, country, location]) =>
        this.poiService
          .locationPoiSummary(country, location)
          .pipe(map((response) => actionLocationPoiSummaryPageLoaded(response)))
      )
    )
  );

  constructor(
    private actions$: Actions,
    private store: Store<AppState>,
    private router: Router,
    private poiService: PoiService
  ) {}
}