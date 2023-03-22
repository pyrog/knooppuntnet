import { Injectable } from '@angular/core';
import { PoiService } from '@app/services/poi.service';
import { StyleFunction } from 'ol/style/Style';
import { ZoomLevel } from '../domain/zoom-level';
import { MapLayer } from '../layers/map-layer';
import { PoiStyleMap } from '../style/poi-style-map';
import { MapLayerService } from './map-layer.service';

@Injectable()
export class PoiTileLayerService {
  poiStyleMap: PoiStyleMap;

  constructor(
    private mapLayerService: MapLayerService,
    private poiService: PoiService
  ) {
    poiService.poiConfiguration.subscribe((configuration) => {
      if (configuration !== null) {
        this.poiStyleMap = new PoiStyleMap(configuration);
      }
    });
  }

  public buildLayer(): MapLayer {
    const layer = this.mapLayerService.poiTileLayer();
    layer.setStyle(this.poiStyleFunction());
    this.poiService.changed.subscribe(() => layer.changed());
    return new MapLayer(
      'poi-tile-layer',
      'poi-tile-layer',
      ZoomLevel.poiTileMinZoom,
      ZoomLevel.poiTileMaxZoom,
      layer,
      null,
      null
    );
  }

  private poiStyleFunction(): StyleFunction {
    return (feature, resolution) => {
      if (this.poiStyleMap) {
        const layer = feature.get('layer');
        if (layer != null) {
          if (this.poiService.isPoiActive(layer)) {
            const style = this.poiStyleMap.get(layer);
            if (style != null) {
              return [style];
            }
          }
        }
      }
      return null;
    };
  }
}
