import { computed } from '@angular/core';
import { inject } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import { input } from '@angular/core';
import { ChangeKey } from '@api/common/changes/details';
import { PageWidthService } from '@app/components/shared';
import { IconHappyComponent } from '@app/components/shared/icon';
import { IconInvestigateComponent } from '@app/components/shared/icon';
import { LinkChangesetComponent } from '@app/components/shared/link';
import { TimestampComponent } from '@app/components/shared/timestamp';

@Component({
  selector: 'kpn-change-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="kpn-line">
      @if (changeKey().changeSetId === 0) {
        <span i18n="@@change-header.start"> Start </span>
      }
      @if (changeKey().changeSetId > 0) {
        <kpn-link-changeset
          [changeSetId]="changeKey().changeSetId"
          [replicationNumber]="changeKey().replicationNumber"
          class="kpn-thick"
        />
      }
      @if (timestampOnSameLine()) {
        <kpn-timestamp [timestamp]="changeKey().timestamp" class="kpn-thin" />
      }
      @if (happy()) {
        <kpn-icon-happy />
      }
      @if (investigate()) {
        <kpn-icon-investigate />
      }
    </div>
    @if (timestampOnSeparateLine()) {
      <div>
        <kpn-timestamp [timestamp]="changeKey().timestamp" class="kpn-thin" />
      </div>
    }

    @if (comment()) {
      <div class="comment">
        {{ comment() }}
      </div>
    }
  `,
  styles: `
    .comment {
      padding-top: 5px;
      padding-bottom: 5px;
      font-style: italic;
    }
  `,
  standalone: true,
  imports: [
    IconHappyComponent,
    IconInvestigateComponent,
    LinkChangesetComponent,
    TimestampComponent,
  ],
})
export class ChangeHeaderComponent {
  changeKey = input.required<ChangeKey>();
  happy = input.required<boolean>();
  investigate = input.required<boolean>();
  comment = input.required<string>();

  private readonly pageWidthService = inject(PageWidthService);
  protected timestampOnSeparateLine = computed(() => this.pageWidthService.isAllSmall());
  protected timestampOnSameLine = computed(() => !this.timestampOnSeparateLine());
}
