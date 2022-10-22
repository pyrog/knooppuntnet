import { LocationPoiSummaryPage } from '@api/common/poi/location-poi-summary-page';
import { LocationPoisPage } from '@api/common/poi/location-pois-page';
import { ApiResponse } from '@api/custom/api-response';
import { createAction } from '@ngrx/store';
import { props } from '@ngrx/store';

export const actionLocationPoisPageInit = createAction(
  '[LocationPoisPage] Init'
);

export const actionLocationPoisPageLoaded = createAction(
  '[LocationPoisPage] Loaded',
  props<ApiResponse<LocationPoisPage>>()
);

export const actionLocationPoisPageIndex = createAction(
  '[LocationPoisPage] Page index',
  props<{ pageIndex: number }>()
);

export const actionLocationPoiSummaryPageInit = createAction(
  '[LocationPoiSummaryPage] Init'
);

export const actionLocationPoiSummaryPageLoaded = createAction(
  '[LocationPoiSummaryPage] Loaded',
  props<ApiResponse<LocationPoiSummaryPage>>()
);