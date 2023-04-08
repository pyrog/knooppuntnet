import { AfterViewInit } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Output } from '@angular/core';
import { Bounds } from '@api/common/bounds';
import { NetworkType } from '@api/custom/network-type';
import { FrisoNode } from '@app/friso/friso/friso-node';
import { OsmLayer } from '@app/components/ol/layers/osm-layer';
import { MapService } from '@app/components/ol/services/map.service';
import { MainMapStyle } from '@app/components/ol/style/main-map-style';
import { MainMapStyleParameters } from '@app/components/ol/style/main-map-style-parameters';
import { selectFrisoMode } from '@app/friso/store/friso.selectors';
import { Subscriptions } from '@app/util/Subscriptions';
import { Store } from '@ngrx/store';
import { List } from 'immutable';
import { MapBrowserEvent } from 'ol';
import { FeatureLike } from 'ol/Feature';
import Interaction from 'ol/interaction/Interaction';
import MapBrowserEventType from 'ol/MapBrowserEventType';
import View from 'ol/View';
import { Observable } from 'rxjs';
import { Util } from '@app/components/shared/util';
import { ZoomLevel } from '@app/components/ol/domain/zoom-level';
import { MapControls } from '@app/components/ol/layers/map-controls';
import { OldMapLayers } from '@app/components/ol/layers/old-map-layers';
import { MapLayerService } from '@app/components/ol/services/map-layer.service';
import { BackgroundLayer } from '@app/components/ol/layers/background-layer';
import { FrisoLayer } from '@app/components/ol/layers/friso-layer';
import { MapLayer } from '@app/components/ol/layers/map-layer';
import { NetworkBitmapTileLayer } from '@app/components/ol/layers/network-bitmap-tile-layer';
import { NetworkVectorTileLayer } from '@app/components/ol/layers/network-vector-tile-layer';
import { OpenLayersMap } from '@app/components/ol/domain/open-layers-map';
import { NewMapService } from '@app/components/ol/services/new-map.service';

@Component({
  selector: 'kpn-friso-map',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div [id]="mapId" class="kpn-map">
      <kpn-old-layer-switcher [mapLayers]="switcherLayers" />
      <kpn-old-map-link-menu [map]="map" />
    </div>
  `,
})
export class FrisoMapComponent implements AfterViewInit, OnDestroy {
  bounds: Bounds = {
    minLat: 50.92176250622567,
    minLon: 3.5257314989690713,
    maxLat: 53.28321294207922,
    maxLon: 6.729255052272727,
  };
  @Output() nodeClicked = new EventEmitter<FrisoNode>();

  protected switcherLayers: OldMapLayers;
  protected layers: OldMapLayers;
  protected map: OpenLayersMap;
  protected readonly mapId = 'friso-map';

  private readonly subscriptions = new Subscriptions();

  constructor(
    private newMapService: NewMapService,
    private mapService: MapService,
    private mapLayerService: MapLayerService,
    private store: Store
  ) {}

  ngAfterViewInit(): void {
    this.mapService.nextNetworkType(NetworkType.hiking);
    this.layers = this.buildLayers();

    this.switcherLayers = new OldMapLayers(
      this.layers.layers.filter(
        (mapLayer) =>
          mapLayer.name === 'osm-layer' ||
          mapLayer.name === 'background-layer' ||
          mapLayer.name === 'network-hiking-layer'
      )
    );

    setTimeout(
      () => this.mapLayerService.restoreMapLayerStates(this.layers),
      0
    );
    this.map = this.newMapService.build({
      target: this.mapId,
      layers: this.layers.toArray(),
      controls: MapControls.build(),
      view: new View({
        minZoom: ZoomLevel.minZoom,
        maxZoom: ZoomLevel.maxZoom,
      }),
    });

    const view = this.map.map.getView();
    view.fit(Util.toExtent(this.bounds, 0.1));

    this.map.map.addInteraction(this.buildInteraction());

    this.subscriptions.add(
      this.store.select(selectFrisoMode).subscribe((mode) => {
        const layerName = `friso-${mode}-layer`;
        this.layers.layers.forEach((layer) => {
          if (layer.name.startsWith('friso-')) {
            const visible = layer.name === layerName;
            layer.layer.setVisible(visible);
          }
        });
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.map.destroy();
  }

  private buildLayers(): OldMapLayers {
    throw new Error('TODO implement options$');
    const parameters$: Observable<MainMapStyleParameters> = null;
    const mainMapStyle = new MainMapStyle(parameters$);

    return new OldMapLayers(
      List([
        OsmLayer.build(),
        BackgroundLayer.build(),
        NetworkVectorTileLayer.oldBuild(
          NetworkType.hiking,
          mainMapStyle.styleFunction()
        ),
        NetworkBitmapTileLayer.build(NetworkType.hiking, 'analysis'),
        this.frisoLayer('rename'), //'Renamed_ext.geojson'
        this.frisoLayer('minor-rename'), // Minor rename_ext.geojson
        this.frisoLayer('removed'), // Removed_osm.geojson
        this.frisoLayer('added'), // Added_ext.geojson
        this.frisoLayer('no-change'), // No change_ext.geojson
        this.frisoLayer('moved-short-distance'), // Moved short distance_ext.geojson
        this.frisoLayer('moved-long-distance'), // Moved long distance_ext.geojson
        this.frisoLayer('other'), // other.geojson
      ])
    );
  }

  private frisoLayer(name: string): MapLayer {
    return new FrisoLayer(name, `$name.geojson`).build();
  }

  private buildInteraction(): Interaction {
    return new Interaction({
      handleEvent: (event: MapBrowserEvent<MouseEvent>) => {
        if (MapBrowserEventType.SINGLECLICK === event.type) {
          return this.handleSingleClickEvent(event);
        }
        if (MapBrowserEventType.POINTERMOVE === event.type) {
          return this.handleMoveEvent(event);
        }
        return true; // propagate event
      },
    });
  }

  private handleSingleClickEvent(evt: MapBrowserEvent<MouseEvent>): boolean {
    const features: FeatureLike[] = evt.map.getFeaturesAtPixel(evt.pixel, {
      hitTolerance: 10,
    });
    if (features) {
      const index = features.findIndex(
        (feature) => !!feature.get('distance closest node')
      );
      if (index >= 0) {
        const name = features[index].get('rwn_ref');
        const distanceClosestNode = features[index].get(
          'distance closest node'
        );
        const node = new FrisoNode(name, Math.round(+distanceClosestNode));
        this.nodeClicked.emit(node);
        return false; // do not propagate event
      }
    }
    return true; // propagate event
  }

  private handleMoveEvent(evt: MapBrowserEvent<MouseEvent>): boolean {
    const features: FeatureLike[] = evt.map.getFeaturesAtPixel(evt.pixel, {
      hitTolerance: 10,
    });
    if (features && features.length > 0) {
      const index = features.findIndex((feature) => !!feature.get('rwn_ref'));
      evt.map.getTargetElement().style.cursor =
        index >= 0 ? 'pointer' : 'default';
    }
    return true; // propagate event
  }
}