import { effect } from '@angular/core';
import { viewChild } from '@angular/core';
import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatTableModule } from '@angular/material/table';
import { OrphanNodeInfo } from '@api/common';
import { EditAndPaginatorComponent } from '@app/analysis/components/edit';
import { EditService } from '@app/components/shared';
import { Util } from '@app/components/shared';
import { DayComponent } from '@app/components/shared/day';
import { LinkNodeComponent } from '@app/components/shared/link';
import { ActionButtonNodeComponent } from '../../../components/action/action-button-node.component';
import { SubsetOrphanNodesPageService } from '../subset-orphan-nodes-page.service';

@Component({
  selector: 'kpn-subset-orphan-nodes-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-edit-and-paginator
      (edit)="edit()"
      i18n-editLinkTitle="@@subset-orphan-nodes.edit.title"
      editLinkTitle="Load the nodes in this page in JOSM"
      [pageSize]="service.pageSize()"
      (pageSizeChange)="onPageSizeChange($event)"
      [pageIndex]="service.pageIndex()"
      [length]="dataSource.data.length"
      [showPageSizeSelection]="true"
      [showFirstLastButtons]="true"
    />

    <table mat-table [dataSource]="dataSource">
      <ng-container matColumnDef="nr">
        <th *matHeaderCellDef mat-header-cell i18n="@@subset-orphan-nodes.table.nr">Nr</th>
        <td mat-cell *matCellDef="let i = index">{{ rowNumber(i) }}</td>
      </ng-container>

      <ng-container matColumnDef="node">
        <th *matHeaderCellDef mat-header-cell i18n="@@subset-orphan-nodes.table.node">Node</th>
        <td mat-cell *matCellDef="let node" class="kpn-align-center action-button-table-cell">
          <kpn-action-button-node [nodeId]="node.id" />
          <kpn-link-node [nodeId]="node.id" [nodeName]="node.name" />
        </td>
      </ng-container>

      <ng-container matColumnDef="name">
        <th *matHeaderCellDef mat-header-cell i18n="@@subset-orphan-nodes.table.name">Name</th>
        <td mat-cell *matCellDef="let node">
          {{ node.longName }}
        </td>
      </ng-container>

      <ng-container matColumnDef="last-survey">
        <th *matHeaderCellDef mat-header-cell i18n="@@subset-orphan-nodes.table.last-survey">
          Survey
        </th>
        <td mat-cell *matCellDef="let node">
          {{ node.lastSurvey }}
        </td>
      </ng-container>

      <ng-container matColumnDef="last-edit">
        <th *matHeaderCellDef mat-header-cell i18n="@@subset-orphan-nodes.table.last-edit">
          Last edit
        </th>
        <td mat-cell *matCellDef="let node" class="kpn-separated">
          <kpn-day [timestamp]="node.lastUpdated" />
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let node; columns: displayedColumns"></tr>
    </table>
  `,
  styles: `
    .mat-column-nr {
      width: 3rem;
    }
  `,
  standalone: true,
  imports: [
    ActionButtonNodeComponent,
    DayComponent,
    EditAndPaginatorComponent,
    LinkNodeComponent,
    MatTableModule,
  ],
})
export class SubsetOrphanNodesTableComponent implements OnInit {
  private readonly editAndPaginator = viewChild(EditAndPaginatorComponent);

  private readonly editService = inject(EditService);
  protected readonly service = inject(SubsetOrphanNodesPageService);

  protected readonly dataSource = new MatTableDataSource<OrphanNodeInfo>();

  protected displayedColumns = ['nr', 'node', 'name', 'last-survey', 'last-edit'];

  constructor() {
    effect(() => {
      this.dataSource.data = this.service.filteredNodes();
    });
  }

  ngOnInit(): void {
    this.dataSource.paginator = this.editAndPaginator().paginator().matPaginator();
  }

  rowNumber(index: number): number {
    return this.editAndPaginator().paginator().rowNumber(index);
  }

  onPageSizeChange(pageSize: number) {
    this.service.setPageSize(pageSize);
  }

  edit(): void {
    const nodeIds = Util.currentPageItems(this.dataSource).map((node) => node.id);
    this.editService.edit({
      nodeIds,
    });
  }
}
