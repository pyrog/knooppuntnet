import { OnInit } from '@angular/core';
import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { ErrorComponent } from '@app/components/shared/error';
import { PageComponent } from '@app/components/shared/page';
import { RouterService } from '../../../shared/services/router.service';
import { LocationPageHeaderComponent } from '../components/location-page-header.component';
import { LocationResponseComponent } from '../components/location-response.component';
import { LocationRoutesSidebarComponent } from './components/location-routes-sidebar.component';
import { LocationRoutesComponent } from './components/location-routes.component';
import { LocationRoutesPageService } from './location-routes-page.service';

@Component({
  selector: 'kpn-location-routes-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-page>
      <kpn-location-page-header
        pageName="routes"
        pageTitle="Routes"
        i18n-pageTitle="@@location-routes.title"
      />

      <kpn-error />

      @if (service.response(); as response) {
        <div class="kpn-spacer-above">
          <kpn-location-response [response]="response">
            <kpn-location-routes [page]="response.result" />
          </kpn-location-response>
        </div>
      }
      <kpn-location-routes-sidebar sidebar />
    </kpn-page>
  `,
  providers: [LocationRoutesPageService, RouterService],
  standalone: true,
  imports: [
    ErrorComponent,
    LocationPageHeaderComponent,
    LocationResponseComponent,
    LocationRoutesComponent,
    LocationRoutesSidebarComponent,
    PageComponent,
  ],
})
export class LocationRoutesPageComponent implements OnInit {
  protected readonly service = inject(LocationRoutesPageService);

  ngOnInit(): void {
    this.service.onInit();
  }
}
