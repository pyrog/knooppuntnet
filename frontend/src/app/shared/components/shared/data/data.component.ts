import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';

@Component({
  selector: 'kpn-data',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="data">
      <div class="title">
        {{ title() }}
      </div>
      <div class="body">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styleUrl: './data.component.scss',
  standalone: true,
})
export class DataComponent {
  title = input.required<string>();
}
