import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Subscription} from "rxjs";
import {AppService} from "../../../app.service";
import {ApiResponse} from "../../../kpn/shared/api-response";
import {NetworkFactsPage} from "../../../kpn/shared/network/network-facts-page";
import {Subset} from "../../../kpn/shared/subset";
import {Country} from "../../../kpn/shared/country";
import {NetworkType} from "../../../kpn/shared/network-type";
import {PageService} from "../../../shared/page.service";

@Component({
  selector: 'kpn-network-facts-page',
  template: `
    <h1>
      Network facts
    </h1>
    <div *ngIf="response">
      <json [object]="response"></json>
    </div>
  `
})
export class NetworkFactsPageComponent implements OnInit, OnDestroy {

  subset: Subset;
  networkId: string;

  response: ApiResponse<NetworkFactsPage>;
  paramsSubscription: Subscription;

  constructor(private activatedRoute: ActivatedRoute,
              private appService: AppService,
              private pageService: PageService) {
  }

  ngOnInit() {
    this.pageService.initNetworkPage();
    this.paramsSubscription = this.activatedRoute.params.subscribe(params => {
      this.networkId = params['networkId'];
      this.pageService.networkId = this.networkId;
      // TODO this.subset = response.result.network.attributes.country + networkType
      this.subset = new Subset(new Country("nl"), new NetworkType("rwn"));
      this.pageService.subset = this.subset;
      this.appService.networkFacts(this.networkId).subscribe(response => {
        this.response = response;
      });
    });
  }

  ngOnDestroy() {
    this.paramsSubscription.unsubscribe();
  }

}
