import { ChangeDetectionStrategy } from '@angular/core';
import { Component, Input, OnInit } from '@angular/core';
import { ChangeSetNetwork } from '@api/common/change-set-network';
import { ChangeSetSummaryInfo } from '@api/common/change-set-summary-info';
import { ChangeSetNetworkAction } from './components/change-set-network.component';

@Component({
  selector: 'kpn-change-network-analysis-summary',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="change-set">
      <kpn-change-header
        [changeKey]="changeSet.key"
        [happy]="changeSet.happy"
        [investigate]="changeSet.investigate"
        [comment]="changeSet.comment"
      />

      <div *ngFor="let action of networkActions">
        <kpn-change-set-network [changeSetNetworkAction]="action" />
      </div>

      <div
        *ngFor="let orphanNodeChanges of changeSet.network.orphanNodeChanges"
      >
        <kpn-change-set-orphan-nodes [subsetElementRefs]="orphanNodeChanges" />
      </div>

      <div
        *ngFor="let orphanRouteChanges of changeSet.network.orphanRouteChanges"
      >
        <kpn-change-set-orphan-routes [subsetElementRefs]="orphanRouteChanges" />
      </div>
    </div>
  `,
  styles: [
    `
      .change-set {
        margin-top: 5px;
        margin-bottom: 5px;
      }
    `,
  ],
})
export class ChangeNetworkAnalysisSummaryComponent implements OnInit {
  @Input() changeSet: ChangeSetSummaryInfo;
  networkActions: ChangeSetNetworkAction[];

  ngOnInit(): void {
    this.networkActions = this.buildNetworkActions();
  }

  private buildNetworkActions(): ChangeSetNetworkAction[] {
    const networkChanges = this.changeSet.network.networkChanges;
    const creates = this.toNetworkActions('C', networkChanges.creates);
    const updates = this.toNetworkActions('U', networkChanges.updates);
    const deletes = this.toNetworkActions('D', networkChanges.deletes);
    return creates.concat(updates).concat(deletes);
  }

  private toNetworkActions(action: string, networks: ChangeSetNetwork[]) {
    return networks.map(
      (nc) => new ChangeSetNetworkAction(this.changeSet.key, action, nc)
    );
  }
}
