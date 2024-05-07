import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { OsmLayer } from '@app/ol/layers';
import { MapLayerState } from '../domain';
import { MAP_SERVICE_TOKEN } from '../services';
import { OpenlayersMapService } from '../services';

@Component({
  selector: 'kpn-layer-switcher',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-menu #mapMenu="matMenu" class="map-control-menu">
      <ng-template matMenuContent>
        @if (layerStates(); as layerStates) {
          <div (click)="$event.stopPropagation()">
            @for (layerState of layerStates; track layerState) {
              <div [hidden]="!layerState.enabled || layerState.id === 'pois'">
                <mat-checkbox
                  (click)="$event.stopPropagation()"
                  [checked]="layerState.visible"
                  (change)="layerVisibleChanged(layerState, $event)"
                >
                  {{ layerState.name }}
                </mat-checkbox>
                @if (layerState.id === osmLayerId && layerStates.length > 2) {
                  <mat-divider></mat-divider>
                }
              </div>
            }
            <ng-content></ng-content>
          </div>
        }
      </ng-template>
    </mat-menu>

    <div class="ol-control map-control map-layers-control">
      <button
        [matMenuTriggerFor]="mapMenu"
        title="select the layers displayed in the map"
        i18n-title="@@layer-switcher.title"
      >
        <mat-icon svgIcon="layers" />
      </button>
    </div>
  `,
  styles: `
    .map-layers-control {
      top: 90px;
      right: 10px;
    }
  `,
  standalone: true,
  imports: [MatCheckboxModule, MatDividerModule, MatIconModule, MatMenuModule],
})
export class LayerSwitcherComponent {
  private readonly openlayersMapService: OpenlayersMapService = inject(MAP_SERVICE_TOKEN);

  protected readonly layerStates = this.openlayersMapService.layerStates;
  protected readonly osmLayerId = OsmLayer.id;

  layerVisibleChanged(layerState: MapLayerState, event: MatCheckboxChange): void {
    const change: MapLayerState = {
      ...layerState,
      visible: event.checked,
    };
    this.openlayersMapService.layerStateChange(change);
  }
}
