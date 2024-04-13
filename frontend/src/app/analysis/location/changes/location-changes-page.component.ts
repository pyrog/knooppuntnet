import { inject } from '@angular/core';
import { OnInit } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { ErrorComponent } from '@app/components/shared/error';
import { PageComponent } from '@app/components/shared/page';
import { RouterService } from '../../../shared/services/router.service';
import { LocationPageHeaderComponent } from '../components/location-page-header.component';
import { LocationResponseComponent } from '../components/location-response.component';
import { LocationSidebarComponent } from '../location-sidebar.component';
import { LocationChangesComponent } from './components/location-changes.component';
import { LocationChangesPageService } from './location-changes-page.service';

@Component({
  selector: 'kpn-location-changes-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-page>
      <kpn-location-page-header
        pageName="changes"
        pageTitle="Changes"
        i18n-pageTitle="@@location-changes.title"
      />

      <kpn-error />

      @if (service.response(); as response) {
        <div class="kpn-spacer-above">
          <kpn-location-response [response]="response">
            <kpn-location-changes [page]="response.result" />
          </kpn-location-response>
        </div>
      }
      <kpn-location-sidebar sidebar />
    </kpn-page>
  `,
  providers: [LocationChangesPageService, RouterService],
  standalone: true,
  imports: [
    ErrorComponent,
    LocationChangesComponent,
    LocationPageHeaderComponent,
    LocationResponseComponent,
    LocationSidebarComponent,
    PageComponent,
  ],
})
export class LocationChangesPageComponent implements OnInit {
  protected readonly service = inject(LocationChangesPageService);

  ngOnInit(): void {
    this.service.onInit();
  }
}
