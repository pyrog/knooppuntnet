import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {Observable} from "rxjs";
import {ChangeFilterOptions} from "../../components/changes/filter/change-filter-options";

@Injectable()
export class SubsetChangesService {

  readonly filterOptions$: Observable<ChangeFilterOptions>;
  private readonly _filterOptions$ = new BehaviorSubject<ChangeFilterOptions>(ChangeFilterOptions.empty());

  constructor() {
    this.filterOptions$ = this._filterOptions$.asObservable();
  }

  setFilterOptions(options: ChangeFilterOptions): void {
    this._filterOptions$.next(options);
  }

  resetFilterOptions() {
    this.setFilterOptions(ChangeFilterOptions.empty());
  }
}
