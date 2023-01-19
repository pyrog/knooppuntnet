import { ViewChild } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component, Input } from '@angular/core';
import { MatMenuTrigger } from '@angular/material/menu';
import Map from 'ol/Map';
import { toLonLat } from 'ol/proj';

@Component({
  selector: 'kpn-map-link-menu',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <!-- eslint-disable @angular-eslint/template/i18n -->
    <mat-menu #mapMenu="matMenu">
      <ng-template matMenuContent>
        <div (mouseleave)="mouseLeavesMenu()">
          <button mat-menu-item (click)="goto('iD')">iD</button>
          <button mat-menu-item (click)="goto('openstreetmap')">
            OpenStreetMap
          </button>
          <button mat-menu-item (click)="goto('mapillary')">Mapillary</button>
          <button mat-menu-item (click)="goto('google')">Google</button>
          <button mat-menu-item (click)="goto('google-satellite')">
            Google Satellite
          </button>
        </div>
      </ng-template>
    </mat-menu>

    <div class="map-control map-links-control" (mouseenter)="openPopupMenu()">
      <button
        class="map-control-button"
        [matMenuTriggerFor]="mapMenu"
      >
        <mat-icon svgIcon="external-link"/>
      </button>
    </div>
  `,
  styles: [
    `
      .map-links-control {
        top: 90px;
        right: 10px;
      }
    `,
  ],
})
export class MapLinkMenuComponent {
  @Input() map: Map;

  @ViewChild(MatMenuTrigger) trigger: MatMenuTrigger;

  openPopupMenu(): void {
    this.trigger.openMenu();
  }

  mouseLeavesMenu(): void {
    this.trigger.closeMenu();
  }

  goto(target: string): void {
    const zoom = Math.round(this.map.getView().getZoom());
    const center = toLonLat(this.map.getView().getCenter());
    let url = '';
    if (target === 'openstreetmap') {
      url = `https://www.openstreetmap.org/#map=${zoom}/${center[1]}/${center[0]}`;
    } else if (target === 'mapillary') {
      url = `https://www.mapillary.com/app/?lat=${center[1]}&lng=${center[0]}&z=${zoom}`;
    } else if (target === 'google') {
      url = `https://www.google.com/maps/@?api=1&map_action=map&center=${center[1]},${center[0]}&zoom=${zoom}`;
    } else if (target === 'google-satellite') {
      url = `https://www.google.com/maps/@?api=1&map_action=map&center=${center[1]},${center[0]}&zoom=${zoom}&basemap=satellite`;
    } else if (target === 'iD') {
      url = `https://www.openstreetmap.org/edit?editor=id#map=${zoom}/${center[1]}/${center[0]}`;
    }
    window.open(encodeURI(url), '_blank');
  }
}
