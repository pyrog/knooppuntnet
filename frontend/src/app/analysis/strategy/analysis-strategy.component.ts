import { AsyncPipe } from '@angular/common';
import { inject } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { Output } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { MatRadioChange } from '@angular/material/radio';
import { MatRadioModule } from '@angular/material/radio';
import { selectPreferencesAnalysisStrategy } from '@app/core';
import { AnalysisStrategy } from '@app/core';
import { Store } from '@ngrx/store';

@Component({
  selector: 'kpn-analysis-strategy',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="sidebar-section">
      <div class="sidebar-section-title" i18n="@@analysis.strategy.title">Analysis strategy</div>

      <mat-radio-group [value]="strategy()" (change)="onStrategyChange($event)">
        <div>
          <mat-radio-button value="location" title="Location" i18n="@@analysis.by-location">
            Explore by location
          </mat-radio-button>
        </div>
        <div>
          <mat-radio-button value="network" title="Network" i18n="@@analysis.by-network">
            Explore by network
          </mat-radio-button>
        </div>
      </mat-radio-group>
    </div>
  `,
  styleUrl: '../../shared/components/shared/sidebar/sidebar.scss',
  standalone: true,
  imports: [MatRadioModule, AsyncPipe],
})
export class AnalysisStrategyComponent {
  @Output() strategyChange = new EventEmitter<AnalysisStrategy>();

  private readonly store = inject(Store);
  protected readonly strategy = this.store.selectSignal(selectPreferencesAnalysisStrategy);

  onStrategyChange(event: MatRadioChange) {
    this.strategyChange.emit(event.value);
  }
}
