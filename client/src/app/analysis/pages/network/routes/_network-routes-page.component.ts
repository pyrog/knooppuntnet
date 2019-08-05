import {Component, OnDestroy, OnInit} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {AppService} from "../../../../app.service";
import {PageService} from "../../../../components/shared/page.service";
import {ApiResponse} from "../../../../kpn/shared/api-response";
import {NetworkRoutesPage} from "../../../../kpn/shared/network/network-routes-page";
import {NetworkCacheService} from "../../../../services/network-cache.service";
import {Subscriptions} from "../../../../util/Subscriptions";
import {flatMap, map, tap} from "rxjs/operators";

@Component({
  selector: "kpn-network-routes-page",
  template: `

    <kpn-network-page-header
      [networkId]="networkId"
      pageTitle="Routes"
      i18n-pageTitle="@@network-routes.title">
    </kpn-network-page-header>

    <div *ngIf="response">
      <div *ngIf="response.result.routes.isEmpty()" i18n="@@network-routes.no-routes">
        No network routes in network
      </div>
      <kpn-network-route-table
        *ngIf="!response.result.routes.isEmpty()"
        [networkType]="response.result.networkType"
        [routes]="response.result.routes">
      </kpn-network-route-table>
      <kpn-json [object]="response"></kpn-json>
    </div>
  `
})
export class NetworkRoutesPageComponent implements OnInit, OnDestroy {

  private readonly subscriptions = new Subscriptions();

  networkId: string;
  response: ApiResponse<NetworkRoutesPage>;

  constructor(private activatedRoute: ActivatedRoute,
              private appService: AppService,
              private pageService: PageService,
              private networkCacheService: NetworkCacheService) {
  }

  ngOnInit() {
    this.pageService.initNetworkPage();
    this.subscriptions.add(
      this.activatedRoute.params.pipe(
        map(params => params["networkId"]),
        tap(networkId => this.processNetworkId(networkId)),
        flatMap(networkId => this.appService.networkRoutes(networkId))
      ).subscribe(response => this.processResponse(response))
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private processNetworkId(networkId: string) {
    this.networkId = networkId;
    this.pageService.networkId = networkId;
  }

  private processResponse(response: ApiResponse<NetworkRoutesPage>) {
    this.response = response;
    this.networkCacheService.setNetworkSummary(this.networkId, response.result.networkSummary);
    const networkName = response.result.networkSummary.name;
    this.networkCacheService.setNetworkName(this.networkId, networkName);
  }

}
