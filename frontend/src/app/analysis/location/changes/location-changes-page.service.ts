import { computed } from '@angular/core';
import { signal } from '@angular/core';
import { inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { Params } from '@angular/router';
import { ChangesParameters } from '@api/common/changes/filter';
import { LocationChangesPage } from '@api/common/location';
import { ApiResponse } from '@api/custom';
import { Util } from '@app/components/shared';
import { AnalysisStrategy } from '@app/core';
import { PreferencesService } from '@app/core';
import { ChangeOption } from '@app/kpn/common';
import { ApiService } from '@app/services';
import { PageParams } from '@app/shared/base';
import { RouterService } from '../../../shared/services/router.service';
import { UserService } from '../../../shared/user';
import { LocationService } from '../location.service';

export class LocationChangesPageService {
  private readonly apiService = inject(ApiService);
  private readonly locationService = inject(LocationService);
  private readonly routerService = inject(RouterService);
  private readonly preferencesService = inject(PreferencesService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  private readonly _response = signal<ApiResponse<LocationChangesPage> | null>(null);
  private readonly _changesParameters = signal<ChangesParameters>(null);

  readonly loggedIn = this.userService.loggedIn;
  readonly impact = computed(() => this.changesParameters().impact);
  readonly pageSize = computed(() => this.changesParameters().pageSize);
  readonly pageIndex = computed(() => this.changesParameters().pageIndex);
  readonly filterOptions = computed(() => this.response()?.result?.filterOptions);

  readonly response = this._response.asReadonly();
  readonly changesParameters = this._changesParameters.asReadonly();

  onInit() {
    this.locationService.initPage(this.routerService);
    const uniqueQueryParams = Util.uniqueParams(this.routerService.queryParams());
    const pageParams = new PageParams(this.routerService.params(), uniqueQueryParams);
    const strategy = pageParams.strategy(this.preferencesService.strategy());
    const changesParameters = pageParams.changesParameters(
      this.preferencesService.impact(),
      this.preferencesService.pageSize()
    );
    this._changesParameters.set(changesParameters);
    this.load();
  }

  setPageSize(pageSize: number): void {
    this.preferencesService.setPageSize(pageSize);
    this.setChangeParameters({
      ...this.changesParameters(),
      pageIndex: 0,
      pageSize,
    });
  }

  setImpact(impact: boolean): void {
    this.setChangeParameters({
      ...this.changesParameters(),
      pageIndex: 0,
      impact,
    });
  }

  setPageIndex(pageIndex: number): void {
    this.setChangeParameters({
      ...this.changesParameters(),
      pageIndex,
    });
  }

  setFilterOption(option: ChangeOption): void {
    this.setChangeParameters({
      ...this.changesParameters(),
      year: option.year,
      month: option.month,
      day: option.day,
      impact: option.impact,
      pageIndex: 0,
    });
  }

  private setChangeParameters(changeParameters: ChangesParameters): void {
    this._changesParameters.set(changeParameters);
    this.load();
  }

  private load() {
    const promise = this.navigate(this.preferencesService.strategy(), this.changesParameters());
    promise.then(() => {
      this.apiService
        .locationChanges(this.locationService.key(), this.changesParameters())
        .subscribe((response) => {
          if (response.result) {
            this.locationService.setSummary(response.result.summary);
          }
          this._response.set(response);
        });
    });
  }

  private navigate(
    strategy: AnalysisStrategy,
    changesParameters: ChangesParameters
  ): Promise<boolean> {
    const queryParams: Params = {
      strategy,
      ...changesParameters,
    };
    return this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams,
    });
  }
}
