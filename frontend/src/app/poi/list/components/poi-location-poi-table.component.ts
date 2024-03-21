import { AsyncPipe } from '@angular/common';
import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { OnChanges } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { ViewChild } from '@angular/core';
import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { input } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { LocationPoiInfo } from '@api/common/poi';
import { PageWidthService } from '@app/components/shared';
import { PaginatorComponent } from '@app/components/shared/paginator';
import { map } from 'rxjs/operators';
import { PoiLocationPoisPageService } from '../poi-location-pois-page.service';

@Component({
  selector: 'kpn-poi-location-poi-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-paginator
      [pageIndex]="service.pageIndex()"
      (pageIndexChange)="onPageIndexChange($event)"
      [pageSize]="service.pageSize()"
      (pageSizeChange)="onPageSizeChange($event)"
      [length]="poiCount()"
      [showFirstLastButtons]="false"
      [showPageSizeSelection]="true"
    />

    <table mat-table [dataSource]="dataSource">
      <ng-container matColumnDef="nr">
        <th mat-header-cell *matHeaderCellDef i18n="@@location-pois.table.nr">Nr</th>
        <td mat-cell *matCellDef="let poi">{{ poi.rowIndex + 1 }}</td>
      </ng-container>

      <ng-container matColumnDef="layer">
        <th mat-header-cell *matHeaderCellDef i18n="@@location-pois.table.layer">Layer</th>
        <td mat-cell *matCellDef="let poi">
          @for (layer of poi.layers; track layer) {
            <span>
              {{ layer }}
            </span>
          }
        </td>
      </ng-container>

      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef i18n="@@location-pois.table.id">Id</th>
        <td mat-cell *matCellDef="let poi">
          <a [routerLink]="'/poi/' + poi.elementType + '/' + poi.elementId">{{ poi._id }}</a>
        </td>
      </ng-container>

      <ng-container matColumnDef="description">
        <th mat-header-cell *matHeaderCellDef i18n="@@location-pois.table.description">
          Description
        </th>
        <td mat-cell *matCellDef="let poi">
          {{ poi.description }}
        </td>
      </ng-container>

      <ng-container matColumnDef="address">
        <th mat-header-cell *matHeaderCellDef i18n="@@location-pois.table.address">Address</th>
        <td mat-cell *matCellDef="let poi">
          {{ poi.address }}
        </td>
      </ng-container>

      <ng-container matColumnDef="link">
        <th mat-header-cell *matHeaderCellDef i18n="@@location-pois.table.link">Link</th>
        <td mat-cell *matCellDef="let poi">
          {{ poi.link ? 'yes' : '' }}
        </td>
      </ng-container>

      <ng-container matColumnDef="image">
        <th mat-header-cell *matHeaderCellDef i18n="@@location-pois.table.image">Image</th>
        <td mat-cell *matCellDef="let poi">
          {{ poi.image ? 'yes' : '' }}
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns$ | async"></tr>
      <tr mat-row *matRowDef="let node; columns: displayedColumns$ | async"></tr>
    </table>

    <kpn-paginator
      [pageIndex]="service.pageIndex()"
      (pageIndexChange)="onPageIndexChange($event)"
      [pageSize]="service.pageSize()"
      (pageSizeChange)="onPageSizeChange($event)"
      [length]="poiCount()"
    />
  `,
  styles: `
    .mat-column-nr {
      flex: 0 0 4em;
    }
  `,
  standalone: true,
  imports: [PaginatorComponent, MatTableModule, RouterLink, AsyncPipe],
})
export class PoiLocationPoiTableComponent implements OnInit, OnChanges {
  pois = input.required<LocationPoiInfo[]>();
  poiCount = input.required<number>();

  @ViewChild(PaginatorComponent, { static: true }) paginator: PaginatorComponent;

  private readonly pageWidthService = inject(PageWidthService);
  protected readonly service = inject(PoiLocationPoisPageService);

  protected readonly dataSource = new MatTableDataSource<LocationPoiInfo>();
  protected readonly displayedColumns$ = this.pageWidthService.current$.pipe(
    map(() => this.displayedColumns())
  );

  ngOnInit(): void {
    this.dataSource.data = this.pois();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pois']) {
      this.dataSource.data = this.pois();
    }
  }

  onPageSizeChange(pageSize: number) {
    this.service.setPageSize(pageSize);
  }

  onPageIndexChange(pageIndex: number) {
    window.scroll(0, 0);
    this.service.setPageIndex(pageIndex);
  }

  private displayedColumns() {
    if (this.pageWidthService.isVeryLarge()) {
      return ['nr', 'layer', 'id', 'description', 'address', 'link', 'image'];
    }

    if (this.pageWidthService.isLarge()) {
      return ['nr', 'layer', 'id', 'description', 'address', 'link', 'image'];
    }

    return ['nr', 'layer', 'id', 'description', 'address', 'link', 'image'];
  }
}