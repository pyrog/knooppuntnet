import {Component, Input, OnInit} from "@angular/core";
import {NetworkAttributes} from "../../../../kpn/shared/network/network-attributes";
import {InterpretedNetworkAttributes} from "./interpreted-network-attributes";

@Component({
  selector: "kpn-subset-network",
  template: `
    <p class="first-line">
      <kpn-link-network-details [networkId]="network.id" [title]="network.name"></kpn-link-network-details>
      <span>{{interpretedNetwork.percentageOk()}}</span>
      <kpn-subset-network-happy [network]="network"></kpn-subset-network-happy>
    </p>
    <p>{{network.km}} km, {{network.nodeCount}} nodes, {{network.routeCount}} routes</p>
    <p>
      <kpn-osm-link-relation [relationId]="network.id"></kpn-osm-link-relation>
      <kpn-josm-relation [relationId]="network.id"></kpn-josm-relation>
    </p>
  `,
  styles: [`
    .first-line {
      display: flex;
      flex-direction: row;
      align-items: center;

      > :not(:first-child) {
        padding-left: 10px;
      }

    }
  `]
})
export class SubsetNetworkComponent implements OnInit {

  @Input() network: NetworkAttributes;
  interpretedNetwork: InterpretedNetworkAttributes;

  constructor() {
  }

  ngOnInit() {
    this.interpretedNetwork = new InterpretedNetworkAttributes(this.network);
  }
}
