import {routerNavigatedAction} from '@ngrx/router-store';
import {createReducer} from '@ngrx/store';
import {on} from '@ngrx/store';
import {Util} from '../../components/shared/util';
import {actionRouteChangesLoaded} from '../analysis/route/route.actions';
import {actionRouteMapLoaded} from '../analysis/route/route.actions';
import {actionRouteDetailsLoaded} from '../analysis/route/route.actions';
import {actionPreferencesImpact} from './preferences.actions';
import {actionPreferencesItemsPerPage} from './preferences.actions';
import {actionPreferencesNetworkType} from './preferences.actions';
import {actionPreferencesInstructions} from './preferences.actions';
import {actionPreferencesExtraLayers} from './preferences.actions';
import {initialState} from './preferences.state';

export const preferencesReducer = createReducer(
  initialState,
  on(
    routerNavigatedAction,
    (state, action) => {
      const params = Util.paramsIn(action.payload.routerState.root);
      const networkType = params.get('networkType');
      if (networkType) {
        return {...state, networkType};
      }
      return state;
    }
  ),
  on(
    actionPreferencesNetworkType,
    (state, action) => ({...state, networkType: action.networkType})
  ),
  on(
    actionPreferencesInstructions,
    (state, action) => ({...state, instructions: action.instructions})
  ),
  on(
    actionPreferencesExtraLayers,
    (state, action) => ({...state, extraLayers: action.extraLayers})
  ),
  on(
    actionPreferencesItemsPerPage,
    (state, action) => ({...state, itemsPerPage: action.itemsPerPage})
  ),
  on(
    actionPreferencesImpact,
    (state, action) => ({...state, impact: action.impact})
  ),
  on(
    actionRouteDetailsLoaded,
    (state, {response}) => {
      const networkType = response?.result.route.summary.networkType.name ?? state.networkType;
      return {
        ...state,
        networkType
      };
    }
  ),
  on(
    actionRouteMapLoaded,
    (state, {response}) => {
      const networkType = response?.result.route.summary.networkType.name ?? state.networkType;
      return {
        ...state,
        networkType
      };
    }
  ),
  on(
    actionRouteChangesLoaded,
    (state, {response}) => {
      const networkType = response?.result.route.summary.networkType.name ?? state.networkType;
      return {
        ...state,
        networkType
      };
    }
  )
);