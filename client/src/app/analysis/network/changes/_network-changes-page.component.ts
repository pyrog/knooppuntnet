import {ChangeDetectionStrategy} from "@angular/core";
import {Component, OnDestroy, OnInit} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {AppService} from "../../../app.service";
import {ChangesParameters} from "../../../kpn/api/common/changes/filter/changes-parameters";
import {NetworkChangesPage} from "../../../kpn/api/common/network/network-changes-page";
import {ApiResponse} from "../../../kpn/api/custom/api-response";
import {NetworkCacheService} from "../../../services/network-cache.service";
import {UserService} from "../../../services/user.service";
import {Subscriptions} from "../../../util/Subscriptions";
import {ChangeFilterOptions} from "../../components/changes/filter/change-filter-options";
import {NetworkChangesService} from "./network-changes.service";

@Component({
  selector: "kpn-network-changes-page",
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-network-page-header
      [networkId]="networkId"
      pageName="changes"
      pageTitle="Changes"
      i18n-pageTitle="@@network-changes.title">
    </kpn-network-page-header>

    <div class="kpn-spacer-above">
      <div *ngIf="!isLoggedIn()" i18n="@@network-changes.login-required">
        The details of network changes history are available to registered OpenStreetMap contributors only, after
        <kpn-link-login></kpn-link-login>
        .
      </div>

      <div *ngIf="response">
        <div *ngIf="!page" i18n="@@network-changes.network-not-found">
          Network not found
        </div>
        <div *ngIf="page">
          <p>
            <kpn-situation-on [timestamp]="response.situationOn"></kpn-situation-on>
          </p>
          <kpn-changes [(parameters)]="parameters" [totalCount]="page.totalCount" [changeCount]="page.changes.size">
            <kpn-items>
              <kpn-item *ngFor="let networkChangeInfo of page.changes; let i=index" [index]="rowIndex(i)">
                <kpn-network-change-set [networkChangeInfo]="networkChangeInfo"></kpn-network-change-set>
              </kpn-item>
            </kpn-items>
          </kpn-changes>

        </div>
        <kpn-json [object]="response"></kpn-json>
      </div>
    </div>
  `
})
export class NetworkChangesPageComponent implements OnInit, OnDestroy {

  networkId: number;
  response: ApiResponse<NetworkChangesPage>;

  private readonly subscriptions = new Subscriptions();

  constructor(private activatedRoute: ActivatedRoute,
              private appService: AppService,
              private networkChangesService: NetworkChangesService,
              private networkCacheService: NetworkCacheService,
              private userService: UserService) {
    const initialParameters = new ChangesParameters(null, null, null, null, null, null, null, 0, 0, false);
    this._parameters = appService.changesParameters(initialParameters);
  }

  private _parameters: ChangesParameters;

  get parameters(): ChangesParameters {
    return this._parameters;
  }

  set parameters(parameters: ChangesParameters) {
    this.appService.storeChangesParameters(parameters);
    this._parameters = parameters;
    if (this.isLoggedIn()) {
      this.reload();
    } else {
      this.networkChangesService.resetFilterOptions();
    }
  }

  get page(): NetworkChangesPage {
    return this.response.result;
  }

  ngOnInit(): void {
    this.subscriptions.add(
      this.activatedRoute.params.subscribe(params => {
        this.networkId = +params["networkId"];
        this.parameters = {...this.parameters, networkId: this.networkId};
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  isLoggedIn(): boolean {
    return this.userService.isLoggedIn();
  }

  rowIndex(index: number): number {
    return this.parameters.pageIndex * this.parameters.itemsPerPage + index;
  }

  private reload() {
    this.appService.networkChanges(this.networkId, this.parameters).subscribe(response => {
      this.processResponse(response);
    });
  }

  private processResponse(response: ApiResponse<NetworkChangesPage>) {
    this.response = response;
    if (this.page) {
      this.networkCacheService.setNetworkSummary(this.networkId, this.page.network);
      this.networkCacheService.setNetworkName(this.networkId, this.page.network.name);
      this.networkChangesService.filterOptions.next(
        ChangeFilterOptions.from(
          this.parameters,
          this.response.result.filter,
          (parameters: ChangesParameters) => this.parameters = parameters
        )
      );
    }
  }

}
