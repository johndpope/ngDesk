import { EscalationRule } from './rule';

export class Escalation {
  constructor(
    private NAME: string,
    private DESCRIPTION: string,
    private RULES: EscalationRule[],
    private ESCALATION_ID?: string,
    private DATE_CREATED?: string,
    private DATE_UPDATED?: string,
    private LAST_UPDATED_BY?: string
  ) {}

  public get name() {
    return this.NAME;
  }

  public set name(name: string) {
    this.NAME = name;
  }

  public get description() {
    return this.DESCRIPTION;
  }

  public set description(description: string) {
    this.DESCRIPTION = description;
  }

  public get rules() {
    return this.RULES;
  }

  public set rules(rules: EscalationRule[]) {
    this.RULES = rules;
  }

  public get escalationId() {
    return this.ESCALATION_ID;
  }

  public set escalationId(escalationId: string) {
    this.ESCALATION_ID = escalationId;
  }

  public get dateCreated() {
    return this.DATE_CREATED;
  }

  public set dateCreated(dateCreated: string) {
    this.DATE_CREATED = dateCreated;
  }

  public get dateUpdated() {
    return this.DATE_UPDATED;
  }

  public set dateUpdated(dateUpdated: string) {
    this.DATE_UPDATED = dateUpdated;
  }

  public get lastUpdatedBy() {
    return this.LAST_UPDATED_BY;
  }

  public set lastUpdatedBy(lastUpdatedBy: string) {
    this.LAST_UPDATED_BY = lastUpdatedBy;
  }
}
