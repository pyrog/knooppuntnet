import { Color } from 'ol/color';
import { FeatureLike } from 'ol/Feature';
import Stroke from 'ol/style/Stroke';
import Style from 'ol/style/Style';
import { MapService } from '../services/map.service';
import { surfaceUnknownColor } from './main-style-colors';
import { proposedSurfaceUnknownColor } from './main-style-colors';
import { proposedUnpavedColor } from './main-style-colors';
import { proposedColor } from './main-style-colors';
import { red } from './main-style-colors';
import { orange } from './main-style-colors';
import { gray } from './main-style-colors';
import { yellow } from './main-style-colors';
import { green } from './main-style-colors';
import { RouteStyle } from './route-style';
import { SurveyDateStyle } from './survey-date-style';

export class MainMapRouteStyle {
  private readonly routeStyleBuilder = new RouteStyle();
  private readonly defaultRouteSelectedStyle = this.initRouteSelectedStyle();
  private readonly surveyDateStyle: SurveyDateStyle;

  constructor(private mapService: MapService) {
    this.surveyDateStyle = new SurveyDateStyle(mapService);
  }

  public routeStyle(resolution: number, feature: FeatureLike): Array<Style> {
    const selectedStyle = this.determineRouteSelectedStyle(feature);
    const style = this.determineRouteStyle(feature, resolution);
    return selectedStyle ? [selectedStyle, style] : [style];
  }

  private determineRouteSelectedStyle(feature: FeatureLike): Style {
    const featureId = feature.get('id');
    let style = null;
    if (
      this.mapService.selectedRouteId &&
      featureId &&
      featureId.startsWith(this.mapService.selectedRouteId)
    ) {
      style = this.defaultRouteSelectedStyle;
    }
    return style;
  }

  private determineRouteStyle(feature: FeatureLike, resolution: number): Style {
    const color = this.routeColor(feature);
    const highligthed =
      this.mapService.highlightedRouteId &&
      feature.get('id').startsWith(this.mapService.highlightedRouteId);
    const proposed = feature.get('state') === 'proposed';
    return this.routeStyleBuilder.style(
      color,
      resolution,
      highligthed,
      proposed
    );
  }

  private initRouteSelectedStyle(): Style {
    return new Style({
      stroke: new Stroke({
        color: yellow,
        width: 14,
      }),
    });
  }

  private routeColor(feature: FeatureLike): Color {
    let color = gray;
    if (this.mapService.mapMode === 'surface') {
      color = this.routeColorSurface(feature);
    } else if (this.mapService.mapMode === 'survey') {
      color = this.routeColorSurvey(feature);
    } else if (this.mapService.mapMode === 'analysis') {
      color = this.routeColorAnalysis(feature);
    }
    return color;
  }

  private routeColorSurface(feature: FeatureLike): Color {
    const proposed = feature.get('state') === 'proposed';
    const surface = feature.get('surface');
    let color = green;
    if ('unpaved' === surface) {
      if (proposed) {
        color = proposedUnpavedColor;
      } else {
        color = orange;
      }
    } else if ('unknown' === surface) {
      if (proposed) {
        color = proposedSurfaceUnknownColor;
      } else {
        color = surfaceUnknownColor;
      }
    } else {
      if (proposed) {
        color = proposedColor;
      }
    }
    return color;
  }

  private routeColorSurvey(feature: FeatureLike): Color {
    return this.surveyDateStyle.surveyColor(feature);
  }

  private routeColorAnalysis(feature: FeatureLike): Color {
    const layer = feature.get('layer');
    let color = gray;
    if ('route' === layer) {
      color = green;
    } else if ('incomplete-route' === layer) {
      color = red;
    } else if ('error-route' === layer) {
      color = red;
    }
    return color;
  }
}
