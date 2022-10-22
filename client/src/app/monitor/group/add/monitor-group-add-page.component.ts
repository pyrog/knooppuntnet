import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { FormControl } from '@angular/forms';
import { Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { AppState } from '../../../core/core.state';
import { MonitorService } from '../../monitor.service';
import { actionMonitorGroupAdd } from '../../store/monitor.actions';
import { urlFragmentValidator } from '../../validator/url-fragment-validator';

@Component({
  selector: 'kpn-monitor-group-add-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <kpn-monitor-group-breadcrumb></kpn-monitor-group-breadcrumb>

    <h1 i18n="@@monitor.group.add.title">Monitor - add group</h1>

    <div class="kpn-comment">
      <p i18n="@@monitor.group.add.comment.1">
        Create a new group containing routes to be monitored.
      </p>
      <p i18n="@@monitor.group.add.comment.2">
        Provide a short name (that will be used in the browser address), and a
        title for the group (probably describing who will be maintaining the
        route group).
      </p>
    </div>

    <form [formGroup]="form" class="kpn-form" #ngForm="ngForm">
      <kpn-monitor-group-name [ngForm]="ngForm" [name]="name">
      </kpn-monitor-group-name>
      <kpn-monitor-group-description
        [ngForm]="ngForm"
        [description]="description"
      ></kpn-monitor-group-description>
      <div class="kpn-form-buttons">
        <button
          mat-stroked-button
          (click)="add()"
          i18n="@@monitor.group.add.action"
        >
          Add group
        </button>
        <a routerLink="/monitor" i18n="@@action.cancel">Cancel</a>
      </div>
    </form>
  `,
})
export class MonitorGroupAddPageComponent {
  readonly name = new FormControl<string>('', {
    validators: [
      Validators.required,
      urlFragmentValidator,
      Validators.maxLength(15),
    ],
    asyncValidators: this.monitorService.asyncGroupNameUniqueValidator(
      () => ''
    ),
    // updateOn: 'blur',
  });

  readonly description = new FormControl<string>('', [
    Validators.required,
    Validators.maxLength(100),
  ]);

  readonly form = new FormGroup({
    name: this.name,
    description: this.description,
  });

  constructor(
    private monitorService: MonitorService,
    private store: Store<AppState>
  ) {}

  add(): void {
    if (this.form.valid) {
      this.store.dispatch(actionMonitorGroupAdd(this.form.value));
    }
  }
}