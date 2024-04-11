// this file is generated, please do not modify

import { LocationChangeSet } from '@api/common';
import { LocationSummary } from './location-summary';

export interface LocationChangesPage {
  readonly summary: LocationSummary;
  readonly changeSets: LocationChangeSet[];
}
