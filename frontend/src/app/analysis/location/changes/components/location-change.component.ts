import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LocationChangeSet } from '@api/common';
import { NetworkType } from '@api/custom';
import { ChangeHeaderComponent } from '@app/analysis/components/change-set';
import { ChangesSetElementRefsComponent } from '@app/analysis/components/change-set/components';
import { Util } from '@app/components/shared';
import { NetworkTypeIconComponent } from '@app/components/shared';
import { Translations } from '@app/i18n';

@Component({
  selector: 'kpn-location-change',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="change-set">
      <!--      <kpn-change-header-->
      <!--        [changeKey]="changeSet().key"-->
      <!--        [happy]="changeSet().happy"-->
      <!--        [investigate]="changeSet().investigate"-->
      <!--        [comment]="changeSet().comment"-->
      <!--      />-->
      <kpn-change-header
        [changeKey]="changeSet().key"
        [happy]="true"
        [investigate]="true"
        [comment]="'TODO'"
      />

      @for (locationChanges of changeSet().locationChanges; track locationChanges) {
        <div>
          <div class="kpn-line">
            <kpn-network-type-icon [networkType]="locationChanges.networkType" />
            <div class="location-names">
              @for (
                locationName of locationChanges.locationNames;
                track locationName;
                let i = $index
              ) {
                <div class="location-name">
                  <a
                    [routerLink]="
                      locationLink(locationChanges.networkType, locationChanges.locationNames, i)
                    "
                    >{{ locationName }}</a
                  >
                </div>
              }
            </div>
          </div>
          <kpn-change-set-element-refs
            [elementType]="'node'"
            [changeSetElementRefs]="locationChanges.nodeChanges"
          />
          <kpn-change-set-element-refs
            [elementType]="'route'"
            [changeSetElementRefs]="locationChanges.routeChanges"
          />
        </div>
      }
    </div>
  `,
  styles: `
    .change-set {
      margin-top: 5px;
      margin-bottom: 5px;
    }

    .location-names {
      display: inline;
    }

    .location-name {
      display: inline;
    }

    .location-names :not(:last-child):after {
      content: ' \\2192 \\0020 ';
    }
  `,
  standalone: true,
  imports: [
    ChangeHeaderComponent,
    ChangesSetElementRefsComponent,
    NetworkTypeIconComponent,
    RouterLink,
  ],
})
export class LocationChangeComponent {
  changeSet = input.required<LocationChangeSet>();

  locationLink(networkType: NetworkType, locationNames: string[], index: number): string {
    const country = locationNames[0].toLowerCase();
    const countryName = Translations.get('country.' + Util.safeGet(() => country));
    const locationParts = [countryName].concat(locationNames.slice(1, index + 1));
    const location = locationParts.join(':');
    return `/analysis/${networkType}/${country}/${location}/nodes`;
  }
}
