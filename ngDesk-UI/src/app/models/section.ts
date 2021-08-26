export class Section {
  constructor(
    private NAME: string,
    private SOURCE_LANGUAGE: string,
    private CATEGORY: string,
    private SORT_BY: string,
    private ORDER: number,
    private DESCRIPTION?: string,
    private SECTION_ID?: string,
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

  public get category() {
    return this.CATEGORY;
  }

  public set category(category) {
    this.CATEGORY = category;
  }

  public get sortBy() {
    return this.SORT_BY;
  }

  public set sortBy(sortBy) {
    this.SORT_BY = sortBy;
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

  public get sectionId() {
    return this.SECTION_ID;
  }

  public set sectionId(sectionId) {
    this.SECTION_ID = sectionId;
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

// export class SortBy {
//   constructor(private FIELD: string, private ORDER_BY: string) { }
//
//   public get field() {
//     return this.FIELD;
//   }
//
//   public set field(field: string) {
//     this.FIELD = field;
//   }
//
//   public get orderBy() {
//     return this.ORDER_BY;
//   }
//
//   public set orderBy(orderBy: string) {
//     this.ORDER_BY = orderBy;
//   }
// }
