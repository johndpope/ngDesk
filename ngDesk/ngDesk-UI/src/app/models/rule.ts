import { EscalateTo } from './escalate-to';

export class EscalationRule {
  constructor(private MINS_AFTER: number, private ORDER: number, private ESCALATE_TO: EscalateTo) {
  }

  public get minsAfter() {
    return this.MINS_AFTER;
  }

  public set minsAfter(minsAfter: number) {
    this.MINS_AFTER = minsAfter;
  }

  public get order() {
    return this.ORDER;
  }

  public set order(order: number) {
    this.ORDER = order;
  }

  public get escalateTo() {
    return this.ESCALATE_TO;
  }

  public set escalateTo(escalateTo: EscalateTo) {
    this.ESCALATE_TO = escalateTo;
  }
}
