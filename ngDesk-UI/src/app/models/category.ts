export class Category {
  constructor(
    private NAME: string,
    private SOURCE_LANGUAGE: string,
    private IS_DRAFT: boolean,
    private ORDER: number,
    private DESCRIPTION?: string,
    private CATEGORY_ID?: string,
    private DATE_CREATED?: string,
    private DATE_UPDATED?: string,
    private LAST_UPDATED_BY?: any,
    private CREATED_BY?: string
  ) { }

  public get name() {
    return this.NAME;
  }

  public set name(name) {
    this.NAME = name;
  }

  public get sourceLanguage() {
    return this.SOURCE_LANGUAGE;
  }

  public set sourceLanguage(sourceLanguage) {
    this.SOURCE_LANGUAGE = sourceLanguage;
  }

  public get isDraft() {
    return this.IS_DRAFT;
  }

  public set isDraft(isDraft) {
    this.IS_DRAFT = isDraft;
  }

  public get order() {
    return this.ORDER;
  }

  public set order(order) {
    this.ORDER = order;
  }

  public get description() {
    return this.DESCRIPTION;
  }

  public set description(description) {
    this.DESCRIPTION = description;
  }

  public get categoryId() {
    return this.CATEGORY_ID;
  }

  public set categoryId(categoryId) {
    this.CATEGORY_ID = categoryId;
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
