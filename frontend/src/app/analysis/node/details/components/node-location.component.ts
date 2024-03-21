import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NetworkType } from '@api/custom';
import { Util } from '@app/components/shared';
import { Translations } from '@app/i18n';

@Component({
  selector: 'kpn-node-location',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (!hasLocation()) {
      <p i18n="@@node.location.none">None</p>
    }
    <div class="kpn-comma-list">
      @for (name of locationNames(); track name; let i = $index) {
        <a [routerLink]="locationLink(i)">{{ name }}</a>
      }
    </div>
  `,
  standalone: true,
  imports: [RouterLink],
})
export class NodeLocationComponent {
  networkType = input.required<NetworkType>();
  locations = input.required<string[]>();

  hasLocation() {
    return this.locations() && this.locations().length > 0;
  }

  locationNames(): string[] {
    if (this.locations()) {
      const country = this.locations()![0].toUpperCase();
      const names = [country].concat(this.locations()!.slice(1));
      return names.reverse();
    }
    return [];
  }

  locationLink(index: number): string {
    const country = this.locations()![0].toLowerCase();
    const countryName = Translations.get('country.' + Util.safeGet(() => country));
    const locationParts = [countryName].concat(
      this.locations()!.slice(1, this.locations()!.length - index)
    );
    const location = locationParts.join(':');
    return `/analysis/${this.networkType()}/${country}/${location}/nodes`;
  }
}