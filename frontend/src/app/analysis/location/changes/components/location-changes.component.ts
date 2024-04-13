import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { ChangeNetworkAnalysisSummaryComponent } from '@app/analysis/components/change-set';
import { ChangeLocationAnalysisSummaryComponent } from '@app/analysis/components/change-set';
import { ChangesComponent } from '@app/analysis/components/changes';
import { ItemComponent, ItemsComponent } from '@app/components/shared/items';
import { LocationChangesPageService } from '../location-changes-page.service';
import { LocationChangeComponent } from './location-change.component';
import { LocationChangesPage } from '@api/common/location';

@Component({
  selector: 'kpn-location-changes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="kpn-spacer-above">
      <kpn-changes
        [impact]="service.impact()"
        [pageSize]="service.pageSize()"
        [pageIndex]="service.pageIndex()"
        (impactChange)="onImpactChange($event)"
        (pageSizeChange)="onPageSizeChange($event)"
        (pageIndexChange)="onPageIndexChange($event)"
        [totalCount]="page().summary.changeCount"
        [changeCount]="page().changeSets.length"
      >
        <kpn-items>
          @for (changeSet of page().changeSets; track $index) {
            <kpn-item [index]="changeSet.rowIndex">
              <kpn-location-change [changeSet]="changeSet" />
            </kpn-item>
          }
        </kpn-items>
      </kpn-changes>
    </div>
  `,
  standalone: true,
  imports: [
    ChangeLocationAnalysisSummaryComponent,
    ChangeNetworkAnalysisSummaryComponent,
    ChangesComponent,
    ItemComponent,
    ItemsComponent,
    LocationChangeComponent,
  ],
})
export class LocationChangesComponent {
  protected readonly service = inject(LocationChangesPageService);

  page = input.required<LocationChangesPage>();

  onImpactChange(impact: boolean): void {
    this.service.setImpact(impact);
  }

  onPageSizeChange(pageSize: number): void {
    this.service.setPageSize(pageSize);
  }

  onPageIndexChange(pageIndex: number): void {
    this.service.setPageIndex(pageIndex);
  }
}
