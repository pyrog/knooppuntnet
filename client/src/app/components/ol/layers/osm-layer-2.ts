import {LngLatLike} from "mapbox-gl";
import * as mapboxgl from "mapbox-gl/dist/mapbox-gl-dev";
import {Layer} from "ol/layer";
import {toLonLat} from "ol/proj";
import {I18nService} from "../../../i18n/i18n.service";
import {OsmLibertyStyle} from "../style/osm-liberty-style";
import {MapLayer} from "./map-layer";

export class OsmLayer2 {

  constructor(private i18nService: I18nService) {
  }

  build(): MapLayer {

    const mbMap = new mapboxgl.Map({
      style: OsmLibertyStyle.osmLibertyStyle,
      attributionControl: false,
      boxZoom: false,
      container: "main-map",
      doubleClickZoom: false,
      dragPan: false,
      dragRotate: false,
      interactive: false,
      keyboard: false,
      pitchWithRotate: false,
      scrollZoom: false,
      touchZoomRotate: false
    });

    const layer = new Layer({
      zIndex: 99,
      render: function (frameState) {
        const canvas = mbMap.getCanvas();
        const viewState = frameState.viewState;

        const visible = layer.getVisible();
        canvas.style.display = visible ? "block" : "none";

        canvas.style.opacity = layer.getOpacity().toString();

        // adjust view parameters in mapbox
        const rotation = viewState.rotation;
        if (rotation) {
          mbMap.rotateTo(-rotation * 180 / Math.PI, {
            animate: false
          });
        }

        const c = toLonLat(viewState.center);
        const cc: LngLatLike = {lng: c[0], lat: c[1]};

        mbMap.jumpTo({
          center: cc,
          zoom: viewState.zoom - 1 // ,
          // animate: false
        });

        // cancel the scheduled update & trigger synchronous redraw
        // see https://github.com/mapbox/mapbox-gl-js/issues/7893#issue-408992184
        // NOTE: THIS MIGHT BREAK WHEN UPDATING MAPBOX
        if (mbMap._frame) {
          mbMap._frame.cancel();
          mbMap._frame = null;
        }
        mbMap._render();

        return canvas;
      }
    });

    return new MapLayer("osm-layer", layer);
  }

}