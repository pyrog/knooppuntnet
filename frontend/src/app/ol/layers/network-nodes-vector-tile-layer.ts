import { NetworkType } from '@api/custom';
import { Translations } from '@app/i18n';
import { MVT } from 'ol/format';
import VectorTileLayer from 'ol/layer/VectorTile';
import VectorTile from 'ol/source/VectorTile';
import { ZoomLevel } from '../domain';
import { NetworkNodesMapStyle } from '../style';
import { Layers } from './layers';
import { MapLayer } from './map-layer';

export class NetworkNodesVectorTileLayer {
  static build(networkType: NetworkType, nodeIds: number[], routeIds: number[]): MapLayer {
    const source = new VectorTile({
      tileSize: 512,
      minZoom: ZoomLevel.vectorTileMinZoom,
      maxZoom: ZoomLevel.vectorTileMaxZoom,
      format: new MVT(),
      url: `/tiles/${networkType}/{z}/{x}/{y}.mvt`,
    });

    const layer = new VectorTileLayer({
      zIndex: Layers.zIndexNetworkLayer,
      className: 'network-layer',
      source,
      renderMode: 'vector',
    });

    const nodeMapStyle = new NetworkNodesMapStyle(nodeIds, routeIds).styleFunction();
    layer.setStyle(nodeMapStyle);

    const name = Translations.get(`network-type.${networkType}`);
    return new MapLayer(
      `network-nodes-${networkType}-layer`,
      name,
      ZoomLevel.vectorTileMinZoom,
      ZoomLevel.vectorTileMaxOverZoom,
      'vector',
      layer,
      networkType,
      null
    );
  }
}
