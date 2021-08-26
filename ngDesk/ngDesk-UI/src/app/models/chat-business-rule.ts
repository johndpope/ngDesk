import { LayerRestriction } from './layer-restriction';

export class ChatBusinessRule {
  constructor(
    private TIMEZONE: string,
    private ACTIVE: boolean,
    private RESTRICTION_TYPE: string,
    private RESTRICTIONS: LayerRestriction[]
  ) {}
  public get timezone() {
    return this.TIMEZONE;
  }

  public set timezone(timezone: string) {
    this.TIMEZONE = timezone;
  }
  public get hasRestriction() {
    return this.ACTIVE;
  }

  public set hasRestriction(hasRestriction: boolean) {
    this.ACTIVE = hasRestriction;
  }

  public get restrictionType() {
    return this.RESTRICTION_TYPE;
  }

  public set restrictionType(restrictionType: string) {
    this.RESTRICTION_TYPE = restrictionType;
  }

  public get chatRestrictions() {
    return this.RESTRICTIONS;
  }

  public set chatRestrictions(chatRestrictions: LayerRestriction[]) {
    this.RESTRICTIONS = chatRestrictions;
  }
}
