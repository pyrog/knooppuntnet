import { inject } from '@angular/core';
import { input } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { MatIconButton } from '@angular/material/button';
import { MatDivider } from '@angular/material/divider';
import { MatIcon } from '@angular/material/icon';
import { MatMenuTrigger } from '@angular/material/menu';
import { MatMenuItem } from '@angular/material/menu';
import { MatMenu } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActionService } from './action.service';

@Component({
  selector: 'kpn-action-button-relation',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button
      mat-icon-button
      matTooltip="Open relation action menu"
      [matMenuTriggerFor]="menu"
      (click)="$event.stopPropagation()"
    >
      <mat-icon svgIcon="open-in-new" class="action-button-icon" />
    </button>
    <mat-menu #menu="matMenu" class="menu-fit-width">
      <button mat-menu-item (click)="josmLoad()">JOSM load relation</button>
      <button mat-menu-item (click)="josmLoadFull()">JOSM load relation and members</button>
      <button mat-menu-item (click)="josmZoom()">JOSM zoom/pan to relation</button>
      <mat-divider />
      <button mat-menu-item (click)="id()">Open in iD</button>
      <button mat-menu-item (click)="osm()">Open in openstreetmap.org</button>
      <button mat-menu-item (click)="deepHistory()">Open in OSM Deep History</button>
    </mat-menu>
  `,
  standalone: true,
  imports: [
    MatIconButton,
    MatIcon,
    MatTooltipModule,
    MatMenu,
    MatMenuItem,
    MatMenuTrigger,
    MatDivider,
  ],
})
export class ActionButtonRelationComponent {
  relationId = input.required<number>();

  private readonly actionService = inject(ActionService);

  josmLoad(): void {
    this.actionService.josmLoadRelation(this.relationId());
  }

  josmLoadFull(): void {
    this.actionService.josmLoadRelationAndMembers(this.relationId());
  }

  josmZoom(): void {
    this.actionService.josmZoomRelation(this.relationId());
  }

  id(): void {
    this.actionService.idRelation(this.relationId());
  }

  osm(): void {
    this.actionService.osmRelation(this.relationId());
  }

  deepHistory(): void {
    this.actionService.deepHistoryRelation(this.relationId());
  }
}
