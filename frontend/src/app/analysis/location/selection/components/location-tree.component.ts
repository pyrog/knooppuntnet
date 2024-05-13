import { FlatTreeControl } from '@angular/cdk/tree';
import { NgClass } from '@angular/common';
import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Output } from '@angular/core';
import { input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { MatTreeFlatDataSource } from '@angular/material/tree';
import { MatTreeModule } from '@angular/material/tree';
import { MatTreeFlattener } from '@angular/material/tree';
import { Country } from '@api/custom';
import { NetworkType } from '@api/custom';
import { Subscriptions } from '@app/util';
import { LocationPipe } from '../../../../shared/components/shared/format/location.pipe';
import { LocalLocationNode } from './local-location-node';
import { LocationFlatNode } from './location-flat-node';

@Component({
  selector: 'kpn-location-tree',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="kpn-small-spacer-above kpn-small-spacer-below">
      <button
        mat-stroked-button
        class="location-button"
        (click)="expandAll()"
        i18n="@@location.tree.expand-all"
      >
        Expand all
      </button>
      <button
        mat-stroked-button
        class="location-button"
        (click)="collapseAll()"
        i18n="@@location.tree.collapse-all"
      >
        Collapse all
      </button>
      <mat-radio-group [value]="all" (change)="allChanged()">
        <mat-radio-button [value]="true" class="location-button" i18n="@@location.tree.all">
          All
        </mat-radio-button>
        <mat-radio-button
          [value]="false"
          class="location-button"
          i18n="@@location.tree.in-use-only"
        >
          In use only
        </mat-radio-button>
      </mat-radio-group>
    </div>

    <mat-tree [dataSource]="dataSource" [treeControl]="treeControl">
      <mat-tree-node
        *matTreeNodeDef="let leafNode"
        matTreeNodePadding
        [ngClass]="{ hidden: !all && leafNode.nodeCount === 0 }"
      >
        <a (click)="select(leafNode)">{{ leafNode.name | location }}</a>
        <span class="node-count">{{ leafNode.nodeCount }}</span>
      </mat-tree-node>
      <mat-tree-node
        *matTreeNodeDef="let expandableNode; when: hasChild"
        matTreeNodePadding
        [ngClass]="{ hidden: !all && expandableNode.nodeCount === 0 }"
      >
        <div mat-icon-button matTreeNodeToggle [attr.aria-label]="'toggle ' + expandableNode.name">
          @if (treeControl.isExpanded(expandableNode)) {
            <mat-icon svgIcon="expand" class="expand-collapse-icon" />
          } @else {
            <mat-icon svgIcon="collapse" class="expand-collapse-icon" />
          }
        </div>
        <a (click)="select(expandableNode)">{{ expandableNode.name | location }}</a
        ><span class="node-count">{{ expandableNode.nodeCount }}</span>
      </mat-tree-node>
    </mat-tree>
  `,
  styles: `
    ::ng-deep .expand-collapse-icon > svg {
      width: 12px;
      height: 12px;
      vertical-align: top;
      padding-top: 7px;
    }

    .node-count {
      padding-left: 20px;
      color: grey;
    }

    .location-button {
      margin-right: 10px;
    }

    .hidden {
      display: none;
    }

    mat-tree {
      padding-left: 1em;
    }
  `,
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatRadioModule, MatTreeModule, NgClass, LocationPipe],
})
export class LocationTreeComponent implements OnInit, OnDestroy {
  networkType = input.required<NetworkType>();
  country = input.required<Country>();
  locationNode = input.required<LocalLocationNode>();

  @Output() selection = new EventEmitter<string>();

  private readonly dialog = inject(MatDialog);

  all = false;

  treeControl = new FlatTreeControl<LocationFlatNode>(
    (node) => node.level,
    (node) => node.expandable
  );

  treeFlattener = new MatTreeFlattener(
    this.transformer(),
    (node) => node.level,
    (node) => node.expandable,
    (node) => node.children
  );
  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  private readonly subscriptions = new Subscriptions();

  hasChild = (_: number, node: LocationFlatNode) => node.expandable;

  ngOnInit() {
    this.dataSource.data = [this.locationNode()];
    this.treeControl.expand(this.treeControl.dataNodes[0]);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  select(expandableNode: LocationFlatNode): void {
    const locationName =
      expandableNode.path.length > 0
        ? expandableNode.path + ':' + expandableNode.name
        : expandableNode.name;

    this.selection.emit(locationName);
  }

  expandAll(): void {
    this.treeControl.expandAll();
  }

  collapseAll(): void {
    this.treeControl.collapseAll();
  }

  allChanged(): void {
    this.all = !this.all;
  }

  private transformer() {
    return (node: LocalLocationNode, level: number) => {
      const maxLevel = this.country() === Country.fr ? 2 : 99;
      const hasChildren = !!node.children && node.children.length > 0;
      const expandable = hasChildren && level < maxLevel;
      return new LocationFlatNode(expandable, node.path, node.name, node.nodeCount, level);
    };
  }
}
