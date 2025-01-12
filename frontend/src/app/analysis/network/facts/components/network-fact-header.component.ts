import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { NetworkFact } from '@api/common';
import { EditParameters } from '@app/analysis/components/edit';
import { FactInfo } from '@app/analysis/fact';
import { FactLevel } from '@app/analysis/fact';
import { Facts } from '@app/analysis/fact';
import { FactLevelComponent } from '@app/analysis/fact';
import { FactNameComponent } from '@app/analysis/fact';
import { EditService } from '@app/components/shared';

@Component({
  selector: 'kpn-network-fact-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="kpn-line">
      <span class="kpn-thick"><kpn-fact-name [fact]="fact().name" /></span>
      <span class="kpn-brackets">{{ factCount() }}</span>
      <kpn-fact-level [factLevel]="factLevel()" class="level" />
    </div>
  `,
  standalone: true,
  imports: [FactNameComponent, FactLevelComponent],
})
export class NetworkFactHeaderComponent {
  fact = input.required<NetworkFact>();

  private readonly editService = inject(EditService);

  factLevel(): FactLevel {
    return Facts.factLevel(this.fact().name);
  }

  factCount(): number {
    if (this.fact().elements && this.fact().elements.length > 0) {
      return this.fact().elements.length;
    }
    if (this.fact().elementIds && this.fact().elementIds.length > 0) {
      return this.fact().elementIds.length;
    }
    if (this.fact().checks) {
      return this.fact().checks.length;
    }
    return 0;
  }

  edit(networkFact: NetworkFact): void {
    let editParameters: EditParameters = null;

    if (
      networkFact.elementType === 'node' &&
      networkFact.elementIds &&
      networkFact.elementIds.length > 0
    ) {
      editParameters = {
        nodeIds: networkFact.elementIds,
      };
    } else if (
      networkFact.elementType === 'node' &&
      networkFact.elements &&
      networkFact.elements.length > 0
    ) {
      editParameters = {
        nodeIds: networkFact.elements.map((ref) => ref.id),
      };
    } else if (
      networkFact.elementType === 'route' &&
      networkFact.elementIds &&
      networkFact.elementIds.length > 0
    ) {
      editParameters = {
        relationIds: networkFact.elementIds,
        fullRelation: true,
      };
    } else if (networkFact.checks && networkFact.checks.length > 0) {
      editParameters = {
        nodeIds: networkFact.checks.map((check) => check.nodeId),
      };
    }
    if (editParameters !== null) {
      this.editService.edit(editParameters);
    }
  }

  factInfo(networkFact: NetworkFact): FactInfo {
    return new FactInfo(networkFact.name);
  }
}
