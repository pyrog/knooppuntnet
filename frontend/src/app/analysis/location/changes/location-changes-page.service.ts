import { computed } from "@angular/core";
import { signal } from "@angular/core";
import { inject } from "@angular/core";
import { ChangesParameters } from "@api/common/changes/filter";
import { LocationChangesPage } from "@api/common/location";
import { ApiResponse } from "@api/custom";
import { PreferencesService } from "@app/core";
import { ApiService } from "@app/services";
import { RouterService } from "../../../shared/services/router.service";
import { UserService } from "../../../shared/user";
import { LocationService } from "../location.service";

export class LocationChangesPageService {
  private readonly apiService = inject(ApiService);
  private readonly locationService = inject(LocationService);
  private readonly routerService = inject(RouterService);
  private readonly preferencesService = inject(PreferencesService);
  private readonly userService = inject(UserService);

  private readonly _response = signal<ApiResponse<LocationChangesPage> | null>(null);
  private readonly _changesParameters = signal<ChangesParameters>(null);

  readonly loggedIn = this.userService.loggedIn;
  readonly impact = computed(() => this.changesParameters().impact);
  readonly pageSize = computed(() => this.changesParameters().pageSize);
  readonly pageIndex = computed(() => this.changesParameters().pageIndex);
  // TODO readonly filterOptions = computed(() => this.response()?.result?.filterOptions);

  readonly response = this._response.asReadonly();
  readonly changesParameters = this._changesParameters.asReadonly();

  onInit() {
    this.locationService.initPage(this.routerService);
    const parameters: ChangesParameters = {
      year: null,
      month: null,
      day: null,
      pageSize: 25,
      pageIndex: 0,
      impact: false,
    };
    this._changesParameters.set(parameters);
    this.load();
  }

  private load() {
    this.apiService
      .locationChanges(this.locationService.key(), this.changesParameters())
      .subscribe((response) => {
        if (response.result) {
          this.locationService.setSummary(response.result.summary);
        }
        this._response.set(response);
      });
  }
}
