import { NgIf } from '@angular/common';
import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { StatisticValues } from '@api/common/statistics';
import { ApiResponse } from '@api/custom';
import { ErrorComponent } from '@app/components/shared/error';
import { PageHeaderComponent } from '@app/components/shared/page';
import { SituationOnComponent } from '@app/components/shared/timestamp';
import { ApiService } from '@app/services';
import { Observable } from 'rxjs';
import { OverviewService } from '../overview.service';
import { OverviewListComponent } from './overview-list.component';
import { OverviewTableComponent } from './overview-table.component';

@Component({
  selector: 'kpn-overview-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="breadcrumb">
      <li><a routerLink="/" i18n="@@breadcrumb.home">Home</a></li>
      <li>
        <a routerLink="/analysis" i18n="@@breadcrumb.analysis">Analysis</a>
      </li>
      <li i18n="@@breadcrumb.overview">Overview</li>
    </ul>

    <kpn-page-header
      subject="overview-in-numbers-page"
      i18n="@@overview-page.title"
      >Overview
    </kpn-page-header>

    <kpn-error></kpn-error>

    <div *ngIf="response$ | async as response" class="kpn-spacer-above">
      <div *ngIf="response.result">
        <div class="situation-on">
          <kpn-situation-on [timestamp]="response.situationOn" />
        </div>
        <ng-content
          *ngIf="tableFormat$ | async; then table; else list"
        ></ng-content>
        <ng-template #table>
          <kpn-overview-table [statistics]="response.result" />
        </ng-template>
        <ng-template #list>
          <kpn-overview-list [statistics]="response.result" />
        </ng-template>
      </div>
    </div>
  `,
  styles: [
    `
      .situation-on {
        padding-bottom: 15px;
      }
    `,
  ],
  standalone: true,
  imports: [
    RouterLink,
    PageHeaderComponent,
    ErrorComponent,
    NgIf,
    SituationOnComponent,
    OverviewTableComponent,
    OverviewListComponent,
    AsyncPipe,
  ],
})
export class OverviewPageComponent implements OnInit {
  readonly tableFormat$ = this.overviewService.tableFormat$;
  response$: Observable<ApiResponse<StatisticValues[]>>;

  constructor(
    private apiService: ApiService,
    private overviewService: OverviewService
  ) {}

  ngOnInit(): void {
    this.response$ = this.apiService.overview();
  }
}