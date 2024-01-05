import { inject } from '@angular/core';
import { OnInit } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { Output } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatOptionModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Country } from '@api/custom';
import { I18nService } from '@app/i18n';
import { Countries } from '@app/kpn/common';
import { Subscriptions } from '@app/util';
import { CountryName } from './country-name';

@Component({
  selector: 'kpn-country-select',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-form-field class="group">
      <mat-label i18n="@@country.selector.label">Country</mat-label>
      <mat-select [formControl]="selectedCountry">
        @for (countryName of countryNames; track countryName) {
          <mat-option [value]="countryName.country">
            {{ countryName.name }}
          </mat-option>
        }
      </mat-select>
    </mat-form-field>
  `,
  standalone: true,
  imports: [MatFormFieldModule, MatOptionModule, MatSelectModule, ReactiveFormsModule],
})
export class CountrySelectComponent implements OnInit, OnDestroy {
  @Output() country = new EventEmitter<Country | null>();

  private readonly i18nService = inject(I18nService);

  protected countryNames: CountryName[];
  private readonly subscriptions = new Subscriptions();
  readonly selectedCountry = new FormControl<Country>(null);

  constructor() {
    this.countryNames = Countries.all.map((country) => {
      const name = this.i18nService.translation(`@@country.${country}`);
      return new CountryName(country, name);
    });
  }

  ngOnInit(): void {
    this.subscriptions.add(
      this.selectedCountry.valueChanges.subscribe((country) => this.country.emit(country))
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
