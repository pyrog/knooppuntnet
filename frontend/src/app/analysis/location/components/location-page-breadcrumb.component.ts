import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LocationKey } from '@api/custom';
import { CountryNameComponent } from '@app/components/shared';
import { NetworkTypeNameComponent } from '@app/components/shared';
import { LocationPipe } from '../../../shared/components/shared/format/location.pipe';

@Component({
  selector: 'kpn-location-page-breadcrumb',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="breadcrumb">
      <li><a routerLink="/" i18n="@@breadcrumb.home">Home</a></li>
      <li>
        <a routerLink="/analysis" i18n="@@breadcrumb.analysis">Analysis</a>
      </li>
      <li>
        <a [routerLink]="networkTypeLink()">
          <kpn-network-type-name [networkType]="locationKey().networkType" />
        </a>
      </li>
      <li>
        <a [routerLink]="countryLink()">
          <kpn-country-name [country]="locationKey().country" />
        </a>
      </li>
      <li>{{ locationName() | location }}</li>
    </ul>
  `,
  standalone: true,
  imports: [RouterLink, NetworkTypeNameComponent, CountryNameComponent, LocationPipe],
})
export class LocationPageBreadcrumbComponent {
  locationKey = input.required<LocationKey>();

  networkTypeLink(): string {
    return `/analysis/${this.locationKey().networkType}`;
  }

  countryLink(): string {
    return `/analysis/${this.locationKey().networkType}/${this.locationKey().country}`;
  }

  locationName(): string {
    const nameParts = this.locationKey().name.split(':');
    return nameParts[nameParts.length - 1];
  }
}
