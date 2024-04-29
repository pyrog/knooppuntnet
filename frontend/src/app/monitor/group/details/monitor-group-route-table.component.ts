import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { MonitorRouteDetail } from '@api/common/monitor';
import { TimestampPipe } from '@app/components/shared/format';
import { TimestampDayPipe } from '@app/components/shared/format';
import { DayPipe } from '@app/components/shared/format';
import { DistancePipe } from '@app/components/shared/format';
import { OsmLinkRelationComponent } from '@app/components/shared/link';
import { SymbolComponent } from '@app/symbol';
import { ActionButtonRelationComponent } from '../../../analysis/components/action/action-button-relation.component';

@Component({
  selector: 'kpn-monitor-group-route-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <table mat-table [dataSource]="routes()">
      <ng-container matColumnDef="nr">
        <th mat-header-cell *matHeaderCellDef class="nr" i18n="@@monitor.group.route-table.nr">
          Nr
        </th>
        <td mat-cell *matCellDef="let route">
          {{ route.rowIndex + 1 }}
        </td>
      </ng-container>

      <ng-container matColumnDef="name">
        <th
          mat-header-cell
          *matHeaderCellDef
          class="id"
          [colSpan]="2"
          i18n="@@monitor.group.route-table.name"
        >
          Name
        </th>
        <td mat-cell *matCellDef="let route" class="action-button-table-cell">
          <div class="kpn-align-center">
            <kpn-action-button-relation [relationId]="route.relationId" />
            <a [routerLink]="routeLink(route)" [state]="route">{{ route.name }}</a>
          </div>
        </td>
      </ng-container>

      <ng-container matColumnDef="happy">
        <th mat-header-cell *matHeaderCellDef></th>
        <td mat-cell *matCellDef="let route">
          @if (route.happy) {
            <mat-icon svgIcon="happy" />
          }
        </td>
      </ng-container>

      <ng-container matColumnDef="map">
        <th mat-header-cell *matHeaderCellDef i18n="@@monitor.group.route-table.map">Map</th>
        <td mat-cell *matCellDef="let route">
          <a
            [routerLink]="routeMapLink(route)"
            [state]="route"
            i18n="@@monitor.group.route-table.map-link"
          >
            map
          </a>
        </td>
      </ng-container>

      <ng-container matColumnDef="relationId">
        <th mat-header-cell *matHeaderCellDef i18n="@@monitor.group.route-table.relation">
          Relation
        </th>
        <td mat-cell *matCellDef="let route">
          @if (route.relationId) {
            {{ route.relationId }}
          }
        </td>
      </ng-container>

      <ng-container matColumnDef="symbol">
        <th mat-header-cell *matHeaderCellDef i18n="@@monitor.group.route-table.symbol">Symbol</th>
        <td mat-cell *matCellDef="let route" class="symbol">
          @if (route.symbol) {
            <kpn-symbol [description]="route.symbol" [width]="25" [height]="25" />
          }
        </td>
      </ng-container>

      <ng-container matColumnDef="description">
        <th mat-header-cell *matHeaderCellDef i18n="@@monitor.group.route-table.description">
          Description
        </th>
        <td mat-cell *matCellDef="let route">
          {{ route.description }}
        </td>
      </ng-container>

      <ng-container matColumnDef="reference-type">
        <th
          mat-header-cell
          [colSpan]="3"
          *matHeaderCellDef
          i18n="@@monitor.group.route-table.reference"
        >
          Reference
        </th>
        <td mat-cell *matCellDef="let route">
          {{ route.referenceType }}
        </td>
      </ng-container>

      <ng-container matColumnDef="reference-day">
        <th mat-header-cell *matHeaderCellDef></th>
        <td
          mat-cell
          *matCellDef="let route"
          [matTooltip]="route.referenceTimestamp | yyyymmddhhmm"
          matTooltipPosition="after"
        >
          {{ route.referenceTimestamp | yyyymmdd }}
        </td>
      </ng-container>

      <ng-container matColumnDef="reference-distance">
        <th mat-header-cell *matHeaderCellDef></th>
        <td mat-cell *matCellDef="let route">
          @if (route.referenceType) {
            <span>
              {{ route.referenceDistance | distance }}
            </span>
          }
        </td>
      </ng-container>

      <ng-container matColumnDef="deviation-count">
        <th
          mat-header-cell
          *matHeaderCellDef
          [colSpan]="2"
          i18n="@@monitor.group.route-table.deviations"
        >
          Deviations
        </th>
        <td mat-cell *matCellDef="let route">
          @if (route.referenceType && route.relationId) {
            <span>
              {{ route.deviationCount }}
            </span>
          }
        </td>
      </ng-container>

      <ng-container matColumnDef="deviation-distance">
        <th mat-header-cell *matHeaderCellDef></th>
        <td mat-cell *matCellDef="let route">
          @if (route.referenceType && route.deviationCount > 0) {
            <span>
              {{ route.deviationDistance | distance }}
            </span>
          }
          @if (route.referenceType && route.relationId && route.deviationCount === 0) {
            <span> - </span>
          }
        </td>
      </ng-container>

      <ng-container matColumnDef="osm-segment-count">
        <th mat-header-cell *matHeaderCellDef i18n="@@monitor.group.route-table.osm-segment-count">
          Segments
        </th>
        <td mat-cell *matCellDef="let route">
          @if (route.relationId) {
            <span>
              {{ route.osmSegmentCount }}
            </span>
          }
        </td>
      </ng-container>

      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef i18n="@@monitor.group.route-table.actions">
          Actions
        </th>
        <td mat-cell *matCellDef="let route" class="kpn-action-cell">
          <button
            mat-icon-button
            [routerLink]="routeUpdateLink(route)"
            [state]="route"
            title="Update"
            i18n-title="@@action.update"
            class="kpn-action-button kpn-link"
          >
            <mat-icon svgIcon="pencil" />
          </button>
          <button
            mat-icon-button
            [routerLink]="routeDeleteLink(route)"
            [state]="route"
            title="delete"
            i18n-title="@@action.delete"
            class="kpn-action-button kpn-warning"
          >
            <mat-icon svgIcon="garbage" />
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedHeaders(admin())"></tr>
      <tr mat-row *matRowDef="let group; columns: displayedColumns(admin())"></tr>
    </table>
  `,
  styles: `
    .id {
      width: 12em;
    }

    .mat-column-name {
      white-space: nowrap;
    }

    .mat-column-description {
      min-width: 12em;
    }

    .mat-column-reference-day {
      white-space: nowrap;
    }

    .mat-column-reference-distance {
      text-align: right !important;
      white-space: nowrap;
    }

    .mat-column-deviation-count {
      text-align: right !important;
      white-space: nowrap;
    }

    .mat-column-deviation-distance {
      text-align: right !important;
      white-space: nowrap;
    }

    .mat-column-osm-segment-count {
      text-align: right !important;
      white-space: nowrap;
    }

    .symbol {
      vertical-align: middle;
    }
  `,
  standalone: true,
  imports: [
    DayPipe,
    DistancePipe,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatTooltipModule,
    OsmLinkRelationComponent,
    RouterLink,
    SymbolComponent,
    TimestampDayPipe,
    TimestampPipe,
    ActionButtonRelationComponent,
  ],
})
export class MonitorGroupRouteTableComponent {
  admin = input.required<boolean>();
  groupName = input.required<string>();
  routes = input.required<MonitorRouteDetail[]>();

  private readonly columns = [
    'nr',
    'name',
    'happy',
    'map',
    'relationId',
    'symbol',
    'description',
    'reference-type',
    'reference-day',
    'reference-distance',
    'deviation-count',
    'deviation-distance',
    'osm-segment-count',
  ];

  private readonly columnsWithoutHeader = [
    'happy',
    'reference-day',
    'reference-distance',
    'deviation-distance',
  ];

  displayedColumns(admin: boolean): string[] {
    if (admin) {
      return [...this.columns, 'actions'];
    }
    return this.columns;
  }

  displayedHeaders(admin: boolean): string[] {
    return this.displayedColumns(admin).filter((name) => !this.columnsWithoutHeader.includes(name));
  }

  routeLink(route: MonitorRouteDetail): string {
    return `/monitor/groups/${this.groupName()}/routes/${route.name}`;
  }

  routeMapLink(route: MonitorRouteDetail): string {
    return `/monitor/groups/${this.groupName()}/routes/${route.name}/map`;
  }

  routeUpdateLink(route: MonitorRouteDetail): string {
    return `/monitor/admin/groups/${this.groupName()}/routes/${route.name}`;
  }

  routeDeleteLink(route: MonitorRouteDetail): string {
    return `/monitor/admin/groups/${this.groupName()}/routes/${route.name}/delete`;
  }
}
