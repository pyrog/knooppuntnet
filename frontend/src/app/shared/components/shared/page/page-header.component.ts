import { viewChild } from '@angular/core';
import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { Component } from '@angular/core';
import { ElementRef } from '@angular/core';
import { OnChanges } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { input } from '@angular/core';
import { DocLinkComponent } from '../link/doc-link.component';
import { PageService } from '../page.service';

@Component({
  selector: 'kpn-page-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="header">
      <h1 #title class="kpn-ellipsis">
        <ng-content></ng-content>
      </h1>
      @if (subject()) {
        <kpn-doc-link [subject]="subject()" />
      }
    </div>
  `,
  styles: `
    .header {
      display: flex;
      vertical-align: middle;
    }

    .header h1 {
      display: inline-block;
      flex: 1;
    }
  `,
  standalone: true,
  imports: [DocLinkComponent],
})
export class PageHeaderComponent implements AfterViewInit, OnChanges {
  subject = input<string>();
  pageTitle = input<string>();

  private readonly pageService = inject(PageService);

  private readonly renderedTitle = viewChild<ElementRef>('title');

  ngOnChanges(changes: SimpleChanges) {
    if (changes['pageTitle']) {
      this.updatePageTitle();
    }
  }

  ngAfterViewInit(): void {
    this.updatePageTitle();
  }

  private updatePageTitle(): void {
    if (this.pageTitle() || this.pageTitle() === null) {
      this.pageService.setTitle(this.pageTitle());
    } else {
      const titleFromPage = this.renderedTitle().nativeElement.textContent.trim();
      this.pageService.setTitle(titleFromPage);
    }
  }
}
