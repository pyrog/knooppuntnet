import {Component, OnDestroy, OnInit} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {AppService} from "../../../../app.service";
import {PageService} from "../../../../components/shared/page.service";
import {ApiResponse} from "../../../../kpn/shared/api-response";
import {UserService} from "../../../../services/user.service";
import {Subscriptions} from "../../../../util/Subscriptions";
import {NodeChangesPage} from "../../../../kpn/shared/node/node-changes-page";
import {flatMap, map, tap} from "rxjs/operators";
import {Util} from "../../../../components/shared/util";

@Component({
  selector: "kpn-node-changes-page",
  template: `

    <kpn-node-page-header [nodeId]="nodeId" [nodeName]="nodeName"></kpn-node-page-header>

    <div *ngIf="!isLoggedIn()">
      <span i18n="@@node.login-required">The node history is available to registered OpenStreetMap contributors only, after</span>
      <kpn-link-login></kpn-link-login>
      .
    </div>

    <div *ngIf="response?.result">
      <div *ngIf="!response.result" i18n="@@node.node-not-found">
        Node not found
      </div>
      <div *ngIf="response.result">

        <div *ngIf="response.result.changes.isEmpty()" i18n="@@node.no-history">
          No history
        </div>

        <div *ngIf="!response.result.changes.isEmpty()">

          <kpn-items>
            <kpn-item *ngFor="let nodeChangeInfo of response.result.changes; let i=index" index="{{i}}">
              <kpn-node-change [nodeChangeInfo]="nodeChangeInfo"></kpn-node-change>
            </kpn-item>
          </kpn-items>

          <div *ngIf="response.result.incompleteWarning">
            <kpn-history-incomplete-warning></kpn-history-incomplete-warning>
          </div>

        </div>

        <kpn-json [object]="response"></kpn-json>
      </div>
    </div>
  `
})
export class NodeChangesPageComponent implements OnInit, OnDestroy {

  private readonly subscriptions = new Subscriptions();

  nodeId: number;
  nodeName: string;
  response: ApiResponse<NodeChangesPage>;

  constructor(private activatedRoute: ActivatedRoute,
              private appService: AppService,
              private pageService: PageService,
              private userService: UserService) {
  }

  isLoggedIn(): boolean {
    return this.userService.isLoggedIn();
  }

  ngOnInit(): void {
    this.nodeName = history.state.nodeName;
    this.pageService.defaultMenu();
    this.subscriptions.add(
      this.activatedRoute.params.pipe(
        map(params => params["nodeId"]),
        tap(nodeId => this.nodeId = +nodeId),
        flatMap(nodeId => this.appService.nodeChanges(nodeId))
      ).subscribe(response => this.processResponse(response))
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private processResponse(response: ApiResponse<NodeChangesPage>) {
    this.response = response;
    this.nodeName = Util.safeGet(() => response.result.nodeInfo.name);
  }

}
