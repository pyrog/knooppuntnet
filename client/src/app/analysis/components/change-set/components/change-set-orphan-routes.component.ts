import { ChangeDetectionStrategy } from '@angular/core';
import { Component, Input } from '@angular/core';
import { ChangeSetSubsetElementRefs } from '@api/common';

@Component({
  selector: 'kpn-change-set-orphan-routes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="kpn-line">
      <span>{{ domain() }}</span>
      <kpn-network-type-icon [networkType]="networkType()" />
      <span i18n="@@change-set.orphan-routes">Free route(s)</span>
    </div>
    <kpn-change-set-element-refs
      [elementType]="'route'"
      [changeSetElementRefs]="subsetElementRefs.elementRefs"
    />
  `,
})
export class ChangesSetOrphanRoutesComponent {
  @Input() subsetElementRefs: ChangeSetSubsetElementRefs;

  domain() {
    if (this.subsetElementRefs.subset.country) {
      return this.subsetElementRefs.subset.country.toUpperCase();
    }
    return '??country??';
  }

  networkType() {
    return this.subsetElementRefs.subset.networkType;
  }
}
