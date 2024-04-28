import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { Reference } from '@api/common/common';
import { IconRouteLinkComponent } from '@app/components/shared/link';

@Component({
  selector: 'kpn-node-route-references',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (reference of references(); track reference) {
      <p>
        <kpn-icon-route-link [reference]="reference" [mixedNetworkScopes]="mixedNetworkScopes()" />
      </p>
    } @empty {
      <p i18n="@@node.route-references.none">None</p>
    }
  `,
  standalone: true,
  imports: [IconRouteLinkComponent],
})
export class NodeRouteReferencesComponent {
  references = input.required<Reference[]>();
  mixedNetworkScopes = input.required<boolean>();
}
