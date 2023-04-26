import { EventEmitter } from '@angular/core';
import { Output } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component, Input } from '@angular/core';
import { MonitorRouteSubRelation } from '@api/common/monitor';

@Component({
  selector: 'kpn-monitor-sub-relation-menu-option',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a
      (click)="goto()"
      [ngClass]="{ disabled: !routeSubRelation }"
      [title]="routeSubRelation?.name"
    >
      {{ name }}</a
    >
  `,
  styles: [
    `
      .disabled {
        pointer-events: none;
        color: grey;
      }
    `,
  ],
})
export class MonitorRouteSubRelationMenuOptionComponent {
  @Input() routeSubRelation: MonitorRouteSubRelation;
  @Input() name: string;
  @Output() selectSubRelation = new EventEmitter<MonitorRouteSubRelation>();

  goto(): void {
    this.selectSubRelation.emit(this.routeSubRelation);
  }
}
