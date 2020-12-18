// this file is generated, please do not modify

import {BoundsI} from '../bounds-i';
import {MonitorRouteNokSegment} from './monitor-route-nok-segment';
import {MonitorRouteSegment} from './monitor-route-segment';

export interface MonitorRoute {
  readonly id: number;
  readonly ref: string;
  readonly name: string;
  readonly nameNl: string;
  readonly nameEn: string;
  readonly nameDe: string;
  readonly nameFr: string;
  readonly description: string;
  readonly operator: string;
  readonly website: string;
  readonly wayCount: number;
  readonly osmDistance: number;
  readonly gpxDistance: number;
  readonly bounds: BoundsI;
  readonly gpxFilename: string;
  readonly osmSegments: MonitorRouteSegment[];
  readonly gpxGeometry: string;
  readonly okGeometry: string;
  readonly nokSegments: MonitorRouteNokSegment[];
}
