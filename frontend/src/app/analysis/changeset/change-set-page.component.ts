import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { PageComponent } from '@app/components/shared/page';
import { SidebarComponent } from '@app/components/shared/sidebar';
import { RouterService } from '../../shared/services/router.service';
import { ChangeSetPageService } from './change-set-page.service';
import { ChangeSetHeaderComponent } from './components/change-set-header.component';
import { ChangeSetLocationChangesComponent } from './components/change-set-location-changes.component';
import { ChangeSetNetworkChangesComponent } from './components/change-set-network-changes.component';
import { ChangeSetOrphanNodeChangesComponent } from './components/change-set-orphan-node-changes.component';
import { ChangeSetOrphanRouteChangesComponent } from './components/change-set-orphan-route-changes.component';

@Component({
  selector: 'kpn-change-set-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-page>
      <h1>
        <ng-container i18n="@@change-set.title">Changeset</ng-container>
        {{ service.changeSetTitle }}
      </h1>

      @if (service.response(); as response) {
        @if (!response.result) {
          <div i18n="@@changeset.not-found">Changeset not found</div>
        } @else {
          <kpn-change-set-header [page]="response.result" />
          <kpn-change-set-location-changes [changess]="response.result.summary.locationChanges" />
          <kpn-change-set-network-changes [page]="response.result" />
          <kpn-change-set-orphan-node-changes [page]="response.result" />
          <kpn-change-set-orphan-route-changes [page]="response.result" />
        }
      }
      <kpn-sidebar sidebar />
    </kpn-page>
  `,
  providers: [ChangeSetPageService, RouterService],
  standalone: true,
  imports: [
    ChangeSetHeaderComponent,
    ChangeSetLocationChangesComponent,
    ChangeSetNetworkChangesComponent,
    ChangeSetOrphanNodeChangesComponent,
    ChangeSetOrphanRouteChangesComponent,
    PageComponent,
    SidebarComponent,
  ],
})
export class ChangeSetPageComponent implements OnInit {
  protected readonly service = inject(ChangeSetPageService);

  ngOnInit(): void {
    this.service.onInit();
  }
}
