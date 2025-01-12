import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Translations } from '@app/i18n';

@Component({
  selector: 'kpn-page-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="footer links">
      <li>
        <a
          href="https://www.openstreetmap.org/message/new/vmarc"
          class="external"
          target="_blank"
          rel="nofollow noreferrer"
          i18n="@@footer.contact"
        >
          contact
        </a>
      </li>
      <li>
        <a
          href="https://wiki.openstreetmap.org/wiki/Knooppuntnet_issues"
          i18n-href="@@wiki.issues"
          class="external"
          target="_blank"
          rel="nofollow noreferrer"
          i18n="@@footer.issues"
        >
          issues
        </a>
      </li>
      @if (settings()) {
        <li>
          <a routerLink="/settings()" i18n="@@footer.settings">settings</a>
        </li>
      }
    </ul>
  `,
  styles: `
    .footer {
      padding: 15px;
      border-top-width: 1px;
      border-top-style: solid;
      border-top-color: lightgray;
      text-align: center;
    }
  `,
  standalone: true,
  imports: [RouterLink],
})
export class PageFooterComponent {
  settings = input(false);

  issues(): string {
    const languageSpecificSubject = Translations.get(`@@wiki.issues`);
    return `https://wiki.openstreetmap.org/wiki/${languageSpecificSubject}`;
  }
}
