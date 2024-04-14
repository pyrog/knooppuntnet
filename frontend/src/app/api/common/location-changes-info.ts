// this file is generated, please do not modify

import { NetworkType } from '@api/custom';
import { ChangeSetElementRefs } from './change-set-element-refs';
import { LocationInfo } from './location-info';

export interface LocationChangesInfo {
  readonly networkType: NetworkType;
  readonly locationInfos: LocationInfo[];
  readonly routeChanges: ChangeSetElementRefs;
  readonly nodeChanges: ChangeSetElementRefs;
  readonly happy: boolean;
  readonly investigate: boolean;
}
