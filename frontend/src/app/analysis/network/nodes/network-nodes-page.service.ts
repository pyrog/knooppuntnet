import { signal } from '@angular/core';
import { inject } from '@angular/core';
import { NetworkNodesPage } from '@api/common/network';
import { ApiResponse } from '@api/custom';
import { PreferencesService } from '@app/core';
import { ApiService } from '@app/services';
import { RouterService } from '../../../shared/services/router.service';
import { NetworkService } from '../network.service';

export class NetworkNodesPageService {
  private readonly apiService = inject(ApiService);
  private readonly networkService = inject(NetworkService);
  private readonly preferencesService = inject(PreferencesService);
  private readonly routerService = inject(RouterService);

  private readonly _response = signal<ApiResponse<NetworkNodesPage>>(null);
  readonly response = this._response.asReadonly();

  readonly pageSize = this.preferencesService.pageSize;

  onInit(): void {
    this.networkService.initPage(this.routerService);
    this.load();
  }

  setPageSize(pageSize: number): void {
    this.preferencesService.setPageSize(pageSize);
    this.load();
  }

  private load(): void {
    this.apiService.networkNodes(this.networkService.networkId()).subscribe((response) => {
      if (response.result) {
        this.networkService.setSummary(response.result.summary);
      }
      this._response.set(response);
    });
  }
}
