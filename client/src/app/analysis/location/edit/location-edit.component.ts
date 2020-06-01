import {Input} from "@angular/core";
import {ChangeDetectionStrategy, Component} from "@angular/core";
import {List, Range} from "immutable";
import {Subscription} from "rxjs";
import {TimeoutError} from "rxjs";
import {BehaviorSubject} from "rxjs";
import {concat} from "rxjs";
import {Observable} from "rxjs";
import {delay} from "rxjs/operators";
import {tap} from "rxjs/operators";
import {AppService} from "../../../app.service";
import {LocationEditPage} from "../../../kpn/api/common/location/location-edit-page";

@Component({
  selector: "kpn-location-edit",
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p>
      {{page.summary.nodeCount}} <span i18n="@@location-edit.nodes">nodes, </span>
      {{page.summary.routeCount}} <span i18n="@@location-edit.routes">routes</span>
    </p>
    <p>
      <i i18n="@@location-edit.time-warning">
        We estimate that it will take perhaps about {{seconds}} seconds to load all nodes and routes in the editor.
      </i>
    </p>
    <p>
      <a
        rel="nofollow"
        (click)="edit()"
        title="Open in editor (like JOSM)"
        i18n-title="@@links.edit.tooltip"
        i18n="@@links.edit">
        edit
      </a>
    </p>
    <p *ngIf="showProgress$ | async">
      <mat-progress-bar [value]="progress$ | async"></mat-progress-bar>
    </p>
    <p *ngIf="ready$ | async" i18n="@@location-edit.ready">
      Ready
    </p>
    <p *ngIf="error$ | async" i18n="@@location-edit.error">
      Error
    </p>
    <p *ngIf="errorName$ | async as errorName">
      {{errorName}}
    </p>
    <p *ngIf="errorMessage$ | async as errorMessage">
      {{errorMessage}}
    </p>
    <p *ngIf="timeout$ | async" class="timeout" i18n="@@location-edit.timeout">
      Timeout: editor not started, or editor remote control not enabled?
    </p>
    <p *ngIf="showProgress$ | async" i18n="@@location-edit.cancel">
      <button mat-raised-button (click)="cancel()">Cancel</button>
    </p>
  `,
  styles: [`
    mat-progress-bar {
      width: 80%;
    }

    .timeout {
      color: red;
    }
  `]
})
export class LocationEditComponent {

  @Input() page: LocationEditPage;

  progressCount = 0;
  progressSteps = 0;
  seconds = 0;

  subscription: Subscription;

  progress$ = new BehaviorSubject<number>(0);
  showProgress$ = new BehaviorSubject<boolean>(false);
  ready$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<boolean>(false);
  errorName$ = new BehaviorSubject<string>("");
  errorMessage$ = new BehaviorSubject<string>("");
  timeout$ = new BehaviorSubject<boolean>(false);

  private readonly chunkSize = 50;
  private readonly requestDelay = 200;
  private readonly josmUrl = "http://localhost:8111/";
  private readonly apiUrl = this.josmUrl + "import?url=https://api.openstreetmap.org/api/0.6";

  constructor(private appService: AppService) {
  }

  ngOnInit(): void {
    const nodeStepCount = (this.page.nodeIds.size / this.chunkSize) + 1;
    const routeStepCount = this.page.routeIds.size;
    const stepCount = nodeStepCount + routeStepCount;
    this.seconds = Math.round(stepCount * (this.requestDelay + 200) / 1000);
  }

  edit(): void {

    this.error$.next(false);
    this.timeout$.next(false);
    this.ready$.next(false);

    const nodeEdits = this.buildNodeEdits();
    const routeEdits = this.buildRouteEdits();
    const setBounds = this.buildSetBounds();
    const steps = setBounds === null ? nodeEdits.concat(routeEdits) : nodeEdits.concat(routeEdits).push(setBounds);

    this.progressSteps = steps.size;
    this.showProgress$.next(true);

    this.subscription = concat(...steps.toArray()).subscribe(
      result => {
      },
      err => {
        if (err instanceof TimeoutError) {
          this.timeout$.next(true);
          this.showProgress$.next(false);
        } else {
          this.showProgress$.next(false);
          this.error$.next(true);
          this.errorName$.next(err.name);
          this.errorMessage$.next(err.message);
        }
      },
      () => {
        this.showProgress$.next(false);
        this.progress$.next(0);
        this.progressCount = 0;
        this.progressSteps = 0;
        this.ready$.next(true);
        this.subscription.unsubscribe();
      }
    );
  }

  cancel(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.showProgress$.next(false);
      this.progress$.next(0);
      this.progressCount = 0;
      this.progressSteps = 0;
    }
  }

  buildSetBounds(): Observable<Object> {
    if (!this.page.nodeIds.isEmpty()) {
      const zoomUrl = this.josmUrl + `zoom?left=${this.page.bounds.minLon}&right=${this.page.bounds.maxLon}&top=${this.page.bounds.maxLat}&bottom=${this.page.bounds.minLat}`;
      return this.appService.edit(zoomUrl).pipe(
        tap(() => this.updateProgress())
      );
    }
    return null;
  }

  private buildNodeEdits(): List<Observable<Object>> {
    const nodeBatches = Range(0, this.page.nodeIds.count(), this.chunkSize)
      .map(chunkStart => this.page.nodeIds.slice(chunkStart, chunkStart + this.chunkSize))
      .toList();
    return nodeBatches.map(nodeIds => {
      const nodeIdString = nodeIds.join(",");
      const url = `${this.apiUrl}/nodes?nodes=${nodeIdString}`;
      return this.appService.edit(url).pipe(
        tap(() => this.updateProgress()),
        delay(this.requestDelay)
      );
    });
  }

  private buildRouteEdits(): List<Observable<Object>> {
    return this.page.routeIds.map(routeId => {
      const url = `${this.apiUrl}/relation/${routeId}/full`;
      return this.appService.edit(url).pipe(
        tap(() => this.updateProgress()),
        delay(this.requestDelay)
      );
    });
  }

  private updateProgress() {
    this.progressCount = this.progressCount + 1;
    let progress = 0;
    if (this.progressSteps > 0) {
      progress = Math.round(100 * this.progressCount / this.progressSteps);
    }
    this.progress$.next(progress);
  }
}