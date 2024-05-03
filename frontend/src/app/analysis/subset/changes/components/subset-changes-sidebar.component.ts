import { inject } from '@angular/core';
import { Component } from '@angular/core';
import { ChangeFilterComponent } from '@app/analysis/components/changes/filter';
import { SidebarComponent } from '@app/components/shared/sidebar';
import { ChangeOption } from '@app/kpn/common';
import { SubsetChangesPageService } from '../subset-changes-page.service';
import { ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'kpn-subset-changes-sidebar',
  changeDetection: ChangeDetectionStrategy.Default,
  template: `
    <kpn-sidebar>
      <kpn-change-filter
        [filterOptions]="filterOptions()"
        (optionSelected)="onOptionSelected($event)"
      />
    </kpn-sidebar>
  `,
  standalone: true,
  imports: [SidebarComponent, ChangeFilterComponent],
})
export class SubsetChangesSidebarComponent {
  private readonly store = inject(SubsetChangesPageService);
  protected readonly filterOptions = this.store.filterOptions;

  onOptionSelected(option: ChangeOption): void {
    this.store.setFilterOption(option);
  }
}
