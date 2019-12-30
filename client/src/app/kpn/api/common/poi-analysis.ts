// this class is generated, please do not modify

import {List} from "immutable";
import {Tags} from "../custom/tags";

export class PoiAnalysis {

  constructor(readonly layers: List<string>,
              readonly mainTags: Tags,
              readonly extraTags: Tags,
              readonly name: string,
              readonly subject: string,
              readonly description: string,
              readonly addressLine1: string,
              readonly addressLine2: string,
              readonly phone: string,
              readonly email: string,
              readonly website: string,
              readonly wikidata: string,
              readonly wikipedia: string,
              readonly molenDatabase: string,
              readonly hollandscheMolenDatabase: string,
              readonly image: string,
              readonly mapillary: string,
              readonly wheelchair: string) {
  }

  public static fromJSON(jsonObject): PoiAnalysis {
    if (!jsonObject) {
      return undefined;
    }
    return new PoiAnalysis(
      jsonObject.layers ? List(jsonObject.layers) : List(),
      Tags.fromJSON(jsonObject.mainTags),
      Tags.fromJSON(jsonObject.extraTags),
      jsonObject.name,
      jsonObject.subject,
      jsonObject.description,
      jsonObject.addressLine1,
      jsonObject.addressLine2,
      jsonObject.phone,
      jsonObject.email,
      jsonObject.website,
      jsonObject.wikidata,
      jsonObject.wikipedia,
      jsonObject.molenDatabase,
      jsonObject.hollandscheMolenDatabase,
      jsonObject.image,
      jsonObject.mapillary,
      jsonObject.wheelchair
    );
  }
}
