import { NetworkType } from '@api/custom/network-type';

export class IntegrityIndicatorData {
  constructor(
    readonly color: string,
    readonly networkType: NetworkType,
    readonly actual: number,
    readonly expected: string
  ) {}
}