import { OnInit } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component, Input } from '@angular/core';
import { Ref } from '@api/common/common';
import { NodeDiffsData } from './node-diffs-data';

@Component({
  selector: 'kpn-node-diffs-updated',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div *ngIf="refs.length > 0" class="kpn-level-2">
      <div class="kpn-line kpn-level-2-header">
        <span i18n="@@node-diffs-updated.title">Updated network nodes</span>
        <span class="kpn-brackets kpn-thin">{{ refs.length }}</span>
      </div>
      <div class="kpn-level-2-body">
        <div *ngFor="let nodeRef of refs" class="kpn-level-3">
          <div class="kpn-line kpn-level-3-header">
            <kpn-link-node-ref-header
              [ref]="nodeRef"
              [knownElements]="data.knownElements"
            />
          </div>
          <div class="kpn-level-3-body">
            <div
              *ngFor="let nodeChangeInfo of data.findNodeChangeInfo(nodeRef)"
            >
              <ng-container
                *ngIf="
                  nodeChangeInfo.before.version === nodeChangeInfo.after.version
                "
                i18n="@@node-diffs-updated.existing-node"
              >
                Existing node v{{ nodeChangeInfo.after.version }}.
              </ng-container>
              <ng-container
                *ngIf="
                  nodeChangeInfo.before.version !== nodeChangeInfo.after.version
                "
                i18n="@@node-diffs-updated.node-changed"
              >
                Node changed to v{{ nodeChangeInfo.after.version }}
              </ng-container>
              <kpn-meta-data [metaData]="nodeChangeInfo.before" />
              <kpn-node-change-detail [nodeChangeInfo]="nodeChangeInfo" />
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class NodeDiffsUpdatedComponent implements OnInit {
  @Input() data: NodeDiffsData;

  refs: Ref[];

  ngOnInit(): void {
    this.refs = this.data.refDiffs.updated;
  }
}
