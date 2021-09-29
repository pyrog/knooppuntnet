// this file is generated, please do not modify

import { Day } from '../../custom/day';
import { Fact } from '../../custom/fact';
import { RouteInfoAnalysis } from './route-info-analysis';
import { RouteSummary } from '../route-summary';
import { Tags } from '../../custom/tags';
import { Timestamp } from '../../custom/timestamp';

export interface RouteDetailsPageData {
  readonly id: number;
  readonly active: boolean;
  readonly summary: RouteSummary;
  readonly proposed: boolean;
  readonly version: number;
  readonly changeSetId: number;
  readonly lastUpdated: Timestamp;
  readonly lastSurvey: Day;
  readonly tags: Tags;
  readonly facts: Fact[];
  readonly analysis: RouteInfoAnalysis;
  readonly tiles: string[];
  readonly nodeRefs: number[];
}