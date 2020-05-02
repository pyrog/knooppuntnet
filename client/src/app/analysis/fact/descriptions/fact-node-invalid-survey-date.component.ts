import {ChangeDetectionStrategy} from "@angular/core";
import {Component} from "@angular/core";

@Component({
  selector: "kpn-fact-node-invalid-survey-date",
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <markdown i18n="@@fact.description.node-invalid-survey-date">
      The node survey date has an unexpected format (valid format: YYYY-MM-DD or YYYY-MM).
      The survey date is defined in the _"survey:date"_ tag, or the _"source:date"_ tag if _"source=survey"_.
    </markdown>
  `
})
export class FactNodeInvalidSurveyDateComponent {
}
