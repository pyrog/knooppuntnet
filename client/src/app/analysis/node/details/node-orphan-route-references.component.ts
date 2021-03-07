import {ChangeDetectionStrategy} from '@angular/core';
import {Component, Input} from '@angular/core';
import {Reference} from '@api/common/common/reference';
import {NodeOrphanRouteReference} from '@api/common/node/node-orphan-route-reference';

@Component({
  selector: 'kpn-node-orphan-route-references',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p *ngIf="references.length === 0" i18n="@@node.orphan-route-references.none">None</p>
    <p *ngFor="let reference of references">
      <kpn-icon-route-link [reference]="toReference(reference)"></kpn-icon-route-link>
    </p>
  `
})
export class NodeOrphanRouteReferencesComponent {

  @Input() references: NodeOrphanRouteReference[];

  toReference(ref: NodeOrphanRouteReference): Reference {
    return new Reference(ref.routeId, ref.routeName, ref.networkType, false);
  }

}