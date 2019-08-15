import {Component} from "@angular/core";

@Component({
  selector: "kpn-fact-orphan-node",
  template: `
    <p i18n="@@fact.description.orphan-node">
      This node does not belong to a network.
      The node was not added as a member to a valid network relation, and also not added
      as a member to valid route relation (that itself was added as a member to a valid
      network relation or is an orphan route).
    </p>
  `
})
export class FactOrphanNodeComponent {
}
