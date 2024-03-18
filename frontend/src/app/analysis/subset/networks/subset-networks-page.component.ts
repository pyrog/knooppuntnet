import { AsyncPipe } from '@angular/common';
import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { PageWidthService } from '@app/components/shared';
import { ErrorComponent } from '@app/components/shared/error';
import { IntegerFormatPipe } from '@app/components/shared/format';
import { PageComponent } from '@app/components/shared/page';
import { SituationOnComponent } from '@app/components/shared/timestamp';
import { MarkdownModule } from 'ngx-markdown';
import { map } from 'rxjs/operators';
import { RouterService } from '../../../shared/services/router.service';
import { SubsetPageHeaderBlockComponent } from '../components/subset-page-header-block.component';
import { SubsetSidebarComponent } from '../subset-sidebar.component';
import { SubsetNetworkListComponent } from './components/subset-network-list.component';
import { SubsetNetworkTableComponent } from './components/subset-network-table.component';
import { SubsetNetworksStore } from './subset-networks.store';

@Component({
  selector: 'kpn-subset-networks-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-page>
      <kpn-subset-page-header-block
        pageName="networks"
        pageTitle="Networks"
        i18n-pageTitle="@@subset-networks.title"
      />

      <kpn-error />

      @if (store.response(); as response) {
        <div class="kpn-spacer-above">
          @if (response.result.networks.length === 0) {
            <div i18n="@@subset-networks.no-networks">No networks</div>
          } @else {
            <div>
              <p>
                <kpn-situation-on [timestamp]="response.situationOn"></kpn-situation-on>
              </p>
              <markdown i18n="@@subset-networks.summary">
                _There are __{{ response.result.networkCount | integer }}__ networks, with a total
                of __{{ response.result.nodeCount | integer }}__ nodes and __{{
                  response.result.routeCount | integer
                }}__ routes with an overall length of __{{ response.result.km | integer }}__ km._
              </markdown>
              @if (large$ | async) {
                <kpn-subset-network-table [networks]="response.result.networks" />
              } @else {
                <kpn-subset-network-list [networks]="response.result.networks" />
              }
            </div>
          }
        </div>
      }
      <kpn-subset-sidebar sidebar />
    </kpn-page>
  `,
  providers: [SubsetNetworksStore, RouterService],
  standalone: true,
  imports: [
    AsyncPipe,
    ErrorComponent,
    IntegerFormatPipe,
    MarkdownModule,
    PageComponent,
    SituationOnComponent,
    SubsetNetworkListComponent,
    SubsetNetworkTableComponent,
    SubsetPageHeaderBlockComponent,
    SubsetSidebarComponent,
  ],
})
export class SubsetNetworksPageComponent {
  protected readonly store = inject(SubsetNetworksStore);
  private readonly pageWidthService = inject(PageWidthService);

  protected readonly large$ = this.pageWidthService.current$.pipe(
    map(() => this.pageWidthService.isVeryLarge())
  );
}
