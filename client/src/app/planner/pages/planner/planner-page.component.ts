import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { AppService } from '@app/app.service';
import { LegHttpErrorDialogComponent } from '@app/components/ol/components/leg-http-error.dialog';
import { LegNotFoundDialogComponent } from '@app/components/ol/components/leg-not-found-dialog';
import { NoRouteDialogComponent } from '@app/components/ol/components/no-route-dialog.component';
import { MapGeocoder } from '@app/components/ol/domain/map-geocoder';
import { MapLayerState } from '@app/components/ol/domain/map-layer-state';
import { ZoomLevel } from '@app/components/ol/domain/zoom-level';
import { MapControls } from '@app/components/ol/layers/map-controls';
import { MapLayerService } from '@app/components/ol/services/map-layer.service';
import { MapZoomService } from '@app/components/ol/services/map-zoom.service';
import { MapService } from '@app/components/ol/services/map.service';
import { PoiTileLayerService } from '@app/components/ol/services/poi-tile-layer.service';
import { PageService } from '@app/components/shared/page.service';
import { Util } from '@app/components/shared/util';
import { PlannerPositionService } from '@app/planner/services/planner-position.service';
import { actionPlannerLoaded } from '@app/planner/store/planner-actions';
import { actionPlannerLayerVisible } from '@app/planner/store/planner-actions';
import { actionPlannerPageInit } from '@app/planner/store/planner-actions';
import { selectPlannerLayerStates } from '@app/planner/store/planner-selectors';
import { PoiService } from '@app/services/poi.service';
import { Subscriptions } from '@app/util/Subscriptions';
import { Store } from '@ngrx/store';
import { Coordinate } from 'ol/coordinate';
import Map from 'ol/Map';
import Overlay from 'ol/Overlay';
import View from 'ol/View';
import { PlannerInteraction } from '../../domain/interaction/planner-interaction';
import { PlannerLayerService } from '../../services/planner-layer.service';
import { PlannerService } from '../../services/planner.service';

@Component({
  selector: 'kpn-planner-page',
  // TODO changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-planner-popup></kpn-planner-popup>
    <div id="main-map" class="map" (mouseleave)="mouseleave()">
      <kpn-route-control (action)="zoomInToRoute()"/>
      <kpn-geolocation-control (action)="geolocation($event)"/>
      <kpn-layer-switcher [layerStates]="layerStates$ | async" (layerStateChange)="layerStateChange($event)">
        <kpn-poi-menu/>
      </kpn-layer-switcher>
      <kpn-map-link-menu [map]="map"/>
    </div>
  `,
  styles: [
    `
      .map {
        position: absolute;
        top: 48px;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: white;
        overflow: hidden;
      }

      .map:-webkit-full-screen {
        top: 0;
      }
    `,
  ],
})
export class PlannerPageComponent implements OnInit, OnDestroy, AfterViewInit {
  readonly layerStates$ = this.store.select(selectPlannerLayerStates);

  private readonly interaction = new PlannerInteraction(
    this.plannerService.engine
  );
  map: Map;
  private overlay: Overlay;
  private planLoaded = false;
  private readonly subscriptions = new Subscriptions();

  constructor(
    private activatedRoute: ActivatedRoute,
    private pageService: PageService,
    private mapService: MapService,
    private mapLayerService: MapLayerService,
    private poiService: PoiService,
    private poiTileLayerService: PoiTileLayerService,
    private plannerService: PlannerService,
    private positionService: PlannerPositionService,
    private mapZoomService: MapZoomService,
    private plannerLayerService: PlannerLayerService,
    private dialog: MatDialog,
    private appService: AppService,
    private store: Store
  ) {
    this.store.dispatch(actionPlannerPageInit());
    this.plannerService.context.error$.subscribe((error) => {
      if (error instanceof HttpErrorResponse) {
        this.dialog.open(LegHttpErrorDialogComponent, {
          autoFocus: false,
          maxWidth: 600,
        });
      } else if ('leg-not-found' === error.message) {
        this.dialog.open(LegNotFoundDialogComponent, {
          autoFocus: false,
          maxWidth: 600,
        });
      }
    });
  }

  ngOnInit(): void {
    // this.subscriptions.add(
    //   this.store
    //     .select(selectRouteParam('networkType'))
    //     .subscribe((networkTypeName) => {
    //       const networkType = NetworkTypes.withName(networkTypeName);
    //       this.mapService.nextNetworkType(networkType);
    //       this.plannerService.context.nextNetworkType(networkType);
    //     })
    // );
    //
    // this.subscriptions.add(
    //   this.mapService.networkType$.subscribe((networkType) => {
    //     this.pageService.nextToolbarBackgroundColor(
    //       'toolbar-style-' + networkType
    //     );
    //   })
    // );
    // this.subscriptions.add(
    //   combineLatest([
    //     this.plannerService.context.networkType$,
    //     this.store.select(selectQueryParam('plan')),
    //     this.store.select(selectFragment),
    //   ]).subscribe(([networkType, planQueryParam, fragment]) => {
    //     let planString: string = null;
    //     if (fragment) {
    //       // support old QR-codes
    //       planString = fragment;
    //     } else {
    //       planString = planQueryParam;
    //     }
    //     if (planString) {
    //       const planParams: PlanParams = {
    //         networkType,
    //         planString,
    //       };
    //       this.appService.plan(planParams).subscribe((response) => {
    //         const plan = PlanBuilder.build(response.result, planString);
    //         const command = new PlannerCommandAddPlan(plan);
    //         this.plannerService.context.execute(command);
    //         this.planLoaded = true;
    //         if (this.map) {
    //           this.zoomInToRoute();
    //         }
    //       });
    //     }
    //   })
    // );
  }

  mouseleave() {
    this.plannerService.engine.handleMouseLeave();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.buildMap(), 0);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.plannerLayerService.mapDestroy(this.map);
    this.pageService.nextToolbarBackgroundColor(null);
    if (this.map) {
      this.map.setTarget(null);
    }
  }

  zoomInToRoute(): void {
    if (this.plannerService.context.plan.legs.isEmpty()) {
      this.dialog.open(NoRouteDialogComponent, {
        autoFocus: false,
        maxWidth: 600,
      });
    } else {
      const bounds = this.plannerService.context.plan.bounds();
      if (bounds !== null) {
        const extent = Util.toExtent(bounds, 0.1);
        this.map.getView().fit(extent);
      }
    }
  }

  geolocation(coordinate: Coordinate): void {
    this.map.getView().setCenter(coordinate);
    let zoomLevel = 15;
    if ('cycling' === this.mapService.networkType()) {
      zoomLevel = 13;
    }
    this.map.getView().setZoom(zoomLevel);
  }

  private buildMap(): void {
    this.overlay = this.buildOverlay();

    const layers = this.plannerLayerService.layers.map((ml) => ml.layer);

    this.map = new Map({
      target: 'main-map',
      layers: layers,
      overlays: [this.overlay],
      controls: MapControls.build(),
      view: new View({
        minZoom: ZoomLevel.minZoom,
        maxZoom: ZoomLevel.vectorTileMaxOverZoom,
      }),
    });

    this.plannerLayerService.mapInit(this.map);

    this.plannerService.init(this.map);
    this.interaction.addToMap(this.map);

    const view = this.map.getView();
    this.positionService.install(view);
    // this.poiService.updateZoomLevel(view.getZoom());
    // this.mapZoomService.install(view);

    MapGeocoder.install(this.map);

    // if (this.planLoaded) {
    //   this.zoomInToRoute();
    // }
    //
    // this.subscriptions.add(
    //   this.pageService.sidebarOpen.subscribe(() => this.updateSize())
    // );
    // this.subscriptions.add(
    //   fromEvent(window, 'webkitfullscreenchange').subscribe(() =>
    //     this.updateSize()
    //   )
    // );

    this.store.dispatch(actionPlannerLoaded());
  }

  private updateSize(): void {
    if (this.map) {
      setTimeout(() => {
        this.map.updateSize();
        this.plannerLayerService.updateSize();
      }, 0);
    }
  }

  layerStateChange(change: MapLayerState): void {
    this.store.dispatch(actionPlannerLayerVisible(change));
  }

  private buildOverlay(): Overlay {
    return new Overlay({
      id: 'popup',
      element: document.getElementById('popup'),
      autoPan: true,
      autoPanAnimation: {
        duration: 250,
      },
    });
  }
}