import { Component } from '@angular/core';
import { input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatStepperModule } from '@angular/material/stepper';
import { ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'kpn-monitor-route-properties-step-6-comment',
  changeDetection: ChangeDetectionStrategy.Default,
  template: `
    <mat-form-field appearance="fill" class="comment">
      <mat-label i18n="@@monitor.route.properties.comment.label"
        >Additional information about the route (optional):
      </mat-label>
      <textarea matInput rows="4" id="comment" [formControl]="comment()"></textarea>
    </mat-form-field>

    <div class="kpn-button-group">
      <button id="step6-back" mat-stroked-button matStepperPrevious i18n="@@action.back">
        Back
      </button>
    </div>
  `,
  styles: `
    .comment {
      width: 50em;
    }
  `,
  standalone: true,
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatStepperModule,
    ReactiveFormsModule,
  ],
})
export class MonitorRoutePropertiesStep6CommentComponent {
  comment = input.required<FormControl<string>>();
}
