// this file is generated, please do not modify

import { LocationChangeSetInfo } from '@api/common';
import { LocationSummary } from './location-summary';

export interface LocationChangesPage {
  readonly summary: LocationSummary;
  readonly changeSets: LocationChangeSetInfo[];
}
