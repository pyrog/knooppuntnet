import {ChangeDetectionStrategy} from '@angular/core';
import {Component, Input} from '@angular/core';
import {NetworkFact} from '@api/common/network-fact';
import {FactLevel} from '../../fact/fact-level';
import {Facts} from '../../fact/facts';

@Component({
  selector: 'kpn-network-fact-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="kpn-line">
      <span class="kpn-thick">
        <kpn-fact-name [fact]="fact.name"></kpn-fact-name>
      </span>
      <span *ngIf="fact.elements && fact.elements.length > 0">
        ({{fact.elements.length}})
      </span>
      <span *ngIf="fact.elementIds && fact.elementIds.length > 0">
        ({{fact.elementIds.length}})
      </span>
      <span *ngIf="fact.checks && fact.checks.length > 0">
        ({{fact.checks.length}})
      </span>
      <kpn-fact-level [factLevel]="factLevel()" class="level"></kpn-fact-level>
    </div>
    <div class="description">
      <kpn-fact-description [factName]="fact.name"></kpn-fact-description>
    </div>
  `,
  styles: [`
    .description {
      max-width: 60em;
    }
  `]
})
export class NetworkFactHeaderComponent {

  @Input() fact: NetworkFact;

  factLevel(): FactLevel {
    return Facts.factLevels.get(this.fact.name);
  }

}
