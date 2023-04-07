import { Injectable } from '@angular/core';
import { BackgroundLayer } from '@app/components/ol/layers/background-layer';
import { OsmLayer } from '@app/components/ol/layers/osm-layer';
import { MapControls } from '@app/components/ol/layers/map-controls';
import View from 'ol/View';
import { ZoomLevel } from '@app/components/ol/domain/zoom-level';
import { Util } from '@app/components/shared/util';
import { NodeMovedLayer } from '@app/components/ol/layers/node-moved-layer';
import { NodeMoved } from '@api/common/diff/node/node-moved';
import { MapLayerRegistry } from '@app/components/ol/layers/map-layer-registry';
import { OpenlayersMapService } from '@app/components/ol/services/openlayers-map-service';
import Map from 'ol/Map';

@Injectable()
export class NodeMovedMapService extends OpenlayersMapService {
  init(nodeMoved: NodeMoved): void {
    const mapLayers = new MapLayerRegistry();
    mapLayers.register(BackgroundLayer.build(), true);
    mapLayers.register(OsmLayer.build(), false);
    mapLayers.register(NodeMovedLayer.build(nodeMoved), true);

    this.register(mapLayers);

    this.initMap(
      new Map({
        target: this.mapId,
        layers: this.layers,
        controls: MapControls.build(),
        view: new View({
          minZoom: ZoomLevel.minZoom,
          maxZoom: ZoomLevel.maxZoom,
          zoom: 18,
        }),
      })
    );
    const center = Util.latLonToCoordinate(nodeMoved.after);
    this.map.getView().setCenter(center);
  }
}
