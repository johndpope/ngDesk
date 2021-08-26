import { Condition } from './condition';

export class MobileListLayout {
  constructor(
    private NAME: string,
    private DESCRIPTION: string,
    private ID: string,
    private ROLE: string,
    private ORDER_BY: OrderBy,
    private FIELDS: String[],
    private CONDITIONS: Condition[],
    private IS_DEFAULT: boolean,
    private LAYOUT_ID?: string,
    private DATE_CREATED?: string,
    private DATE_UPDATED?: string,
    private LAST_UPDATED_BY?: string,
    private CREATED_BY?: string
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

  public get id() {
    return this.ID;
  }

  public set id(id: string) {
    this.ID = id;
  }

  public get role() {
    return this.ROLE;
  }

  public set role(role: string) {
    this.ROLE = role;
  }

  public get orderBy() {
    return this.ORDER_BY;
  }

  public set orderBy(orderBy: OrderBy) {
    this.ORDER_BY = orderBy;
  }

  public get fields() {
    return this.FIELDS;
  }

  public set fields(fields: String[]) {
    this.FIELDS = fields;
  }

  public get conditions() {
    return this.CONDITIONS;
  }

  public set conditions(conditions: Condition[]) {
    this.CONDITIONS = conditions;
  }

  public get isDefault() {
    return this.IS_DEFAULT;
  }

  public set isDefault(isDefault: boolean) {
    this.IS_DEFAULT = isDefault;
  }

  public get layoutId() {
    return this.LAYOUT_ID;
  }

  public set layoutId(layoutId: string) {
    this.LAYOUT_ID = layoutId;
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

  public get createdBy() {
    return this.CREATED_BY;
  }

  public set createdBy(createdBy: string) {
    this.CREATED_BY = createdBy;
  }
}

export class OrderBy {
  constructor(private COLUMN: string, private ORDER: string) {}

  public get column() {
    return this.COLUMN;
  }

  public set column(column: string) {
    this.COLUMN = column;
  }

  public get order() {
    return this.ORDER;
  }

  public set order(order: string) {
    this.ORDER = order;
  }
}

export class MobileDetailLayout {
  constructor(
    private NAME: string,
    private DESCRIPTION: string,
    private ID: string,
    private ROLE: string,
    private FIELDS: String[],
    private LAYOUT_ID?: string,
    private DATE_CREATED?: string,
    private DATE_UPDATED?: string,
    private LAST_UPDATED_BY?: string,
    private CREATED_BY?: string
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

  public get id() {
    return this.ID;
  }

  public set id(id: string) {
    this.ID = id;
  }

  public get role() {
    return this.ROLE;
  }

  public set role(role: string) {
    this.ROLE = role;
  }

  public get fields() {
    return this.FIELDS;
  }

  public set fields(fields: String[]) {
    this.FIELDS = fields;
  }

  public get layoutId() {
    return this.LAYOUT_ID;
  }

  public set layoutId(layoutId: string) {
    this.LAYOUT_ID = layoutId;
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

  public get createdBy() {
    return this.CREATED_BY;
  }

  public set createdBy(createdBy: string) {
    this.CREATED_BY = createdBy;
  }
}
