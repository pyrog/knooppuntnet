// this file is generated, please do not modify

import { LocationChangeSetInfo } from '@api/common';
import { ChangesFilterOption } from '@api/common/changes/filter';
import { LocationSummary } from './location-summary';

export interface LocationChangesPage {
  readonly summary: LocationSummary;
  readonly changeSets: LocationChangeSetInfo[];
  readonly changesCount: number;
  readonly filterOptions: ChangesFilterOption[];
}
