import { AsyncPipe } from '@angular/common';
import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ChangesComponent } from '@app/analysis/components/changes';
import { ErrorComponent } from '@app/components/shared/error';
import { ItemComponent } from '@app/components/shared/items';
import { ItemsComponent } from '@app/components/shared/items';
import { PageComponent } from '@app/components/shared/page';
import { SituationOnComponent } from '@app/components/shared/timestamp';
import { RouterService } from '../../../shared/services/router.service';
import { UserLinkLoginComponent } from '../../../shared/user';
import { UserStore } from '../../../shared/user/user.store';
import { NodePageHeaderComponent } from '../components/node-page-header.component';
import { NodeStore } from '../node.store';
import { NodeChangeComponent } from './components/node-change.component';
import { NodeChangesSidebarComponent } from './components/node-changes-sidebar.component';
import { NodeChangesStore } from './node-changes.store';

@Component({
  selector: 'kpn-node-changes-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-page>
      <ul class="breadcrumb">
        <li><a [routerLink]="'/'" i18n="@@breadcrumb.home">Home</a></li>
        <li>
          <a [routerLink]="'/analysis'" i18n="@@breadcrumb.analysis">Analysis</a>
        </li>
        <li i18n="@@breadcrumb.node-changes">Node changes</li>
      </ul>

      <kpn-node-page-header
        pageName="changes"
        [nodeId]="nodeId()"
        [nodeName]="nodeName()"
        [changeCount]="changeCount()"
      />

      <kpn-error />

      @if (store.response(); as response) {
        <div class="kpn-spacer-above">
          @if (!response.result) {
            <p i18n="@@node.node-not-found">Node not found</p>
          } @else {
            @if (loggedIn() === false) {
              <div>
                <p i18n="@@node.login-required">
                  The details of the node changes history is available to logged in OpenStreetMap
                  contributors only.
                </p>
                <p>
                  <kpn-user-link-login />
                </p>
              </div>
            } @else {
              @if (response.result; as page) {
                <div>
                  <p>
                    <kpn-situation-on [timestamp]="response.situationOn"></kpn-situation-on>
                  </p>
                  <kpn-changes
                    [impact]="impact()"
                    [pageSize]="pageSize()"
                    [pageIndex]="pageIndex()"
                    (impactChange)="onImpactChange($event)"
                    (pageSizeChange)="onPageSizeChange($event)"
                    (pageIndexChange)="onPageIndexChange($event)"
                    [totalCount]="page.totalCount"
                    [changeCount]="page.changes.length"
                  >
                    <kpn-items>
                      @for (nodeChangeInfo of page.changes; track nodeChangeInfo) {
                        <kpn-item [index]="nodeChangeInfo.rowIndex">
                          <kpn-node-change [nodeChangeInfo]="nodeChangeInfo" />
                        </kpn-item>
                      }
                    </kpn-items>
                  </kpn-changes>
                </div>
              }
            }
            <ng-template #changes>
              @if (response.result; as page) {
                <div>
                  <p>
                    <kpn-situation-on [timestamp]="response.situationOn"></kpn-situation-on>
                  </p>
                  <kpn-changes
                    [impact]="impact()"
                    [pageSize]="pageSize()"
                    [pageIndex]="pageIndex()"
                    (impactChange)="onImpactChange($event)"
                    (pageSizeChange)="onPageSizeChange($event)"
                    (pageIndexChange)="onPageIndexChange($event)"
                    [totalCount]="page.totalCount"
                    [changeCount]="page.changes.length"
                  >
                    <kpn-items>
                      @for (nodeChangeInfo of page.changes; track nodeChangeInfo) {
                        <kpn-item [index]="nodeChangeInfo.rowIndex">
                          <kpn-node-change [nodeChangeInfo]="nodeChangeInfo" />
                        </kpn-item>
                      }
                    </kpn-items>
                  </kpn-changes>
                </div>
              }
            </ng-template>
          }
        </div>
      }
      <kpn-node-changes-sidebar sidebar />
    </kpn-page>
  `,
  providers: [NodeChangesStore, RouterService],
  standalone: true,
  imports: [
    AsyncPipe,
    ChangesComponent,
    ErrorComponent,
    ItemComponent,
    ItemsComponent,
    NodeChangeComponent,
    NodeChangesSidebarComponent,
    NodePageHeaderComponent,
    PageComponent,
    RouterLink,
    SituationOnComponent,
    UserLinkLoginComponent,
  ],
})
export class NodeChangesPageComponent {
  private readonly userStore = inject(UserStore);
  protected readonly loggedIn = this.userStore.loggedIn;

  protected readonly store = inject(NodeChangesStore);
  protected readonly nodeStore = inject(NodeStore);
  protected readonly nodeId = this.nodeStore.nodeId;
  protected readonly nodeName = this.nodeStore.nodeName;
  protected readonly changeCount = this.nodeStore.changeCount;
  protected readonly impact = this.store.impact();
  protected readonly pageSize = this.store.pageSize();
  protected readonly pageIndex = this.store.pageIndex;

  onImpactChange(impact: boolean): void {
    this.store.updateImpact(impact);
  }

  onPageSizeChange(pageSize: number): void {
    this.store.updatePageSize(pageSize);
  }

  onPageIndexChange(pageIndex: number): void {
    this.store.updatePageIndex(pageIndex);
  }
}
