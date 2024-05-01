import { Routes } from '@angular/router';

export const appRoutes: Routes = [
  {
    path: 'tryout-filter',
    loadComponent: () =>
      import('./tryout/filter/tryout-filter-page.component').then(
        (m) => m.TryoutFilterPageComponent
      ),
  },
  {
    path: 'tryout-tabs',
    loadComponent: () =>
      import('./tryout/tabs/tryout-tabs-page.component').then((m) => m.TryoutTabsPageComponent),
  },
  {
    path: 'analysis',
    loadChildren: () => import('@app/analysis/analysis').then((m) => m.analysisRoutes),
  },
  {
    path: 'map',
    loadChildren: () => import('@app/planner').then((m) => m.plannerRoutes),
  },
  {
    path: 'status',
    loadChildren: () => import('@app/status').then((m) => m.statusRoutes),
  },
  {
    path: 'settings',
    loadChildren: () => import('@app/settings').then((m) => m.settingsRoutes),
  },
  {
    path: 'poi',
    loadChildren: () => import('@app/poi').then((m) => m.poiRoutes),
  },
  {
    path: 'demo',
    loadChildren: () => import('@app/demo').then((m) => m.demoRoutes),
  },
  {
    path: 'monitor',
    loadChildren: () => import('@app/monitor').then((m) => m.monitorRoutes),
  },
  {
    path: 'symbols',
    loadChildren: () => import('@app/symbol').then((m) => m.symbolRoutes),
  },
  {
    path: '',
    loadChildren: () => import('@app/shared/base').then((m) => m.baseRoutes),
  },
];
