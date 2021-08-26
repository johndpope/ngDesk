export class Category {
  constructor(
    private TITLE: string,
    private BODY: string,
    private IS_DRAFT: boolean,
    private SOURCE_LANGUAGE: string,
    private ROLES: string,
    private TEAMS: any[],
    private SECTION: string,
    private OPEN_FOR_COMMENTS: boolean,
    private AUTHOR: string,
    private LABELS: string,
    private DATE_CREATED?: string,
    private DATE_UPDATED?: string,
    private LAST_UPDATED_BY?: any,
    private CREATED_BY?: string
  ) { }

  public get title() {
    return this.TITLE;
  }

  public set title(title) {
    this.TITLE = title;
  }

  public get body() {
    return this.BODY;
  }

  public set body(body) {
    this.BODY = body;
  }

  public get isDraft() {
    return this.IS_DRAFT;
  }

  public set isDraft(isDraft) {
    this.IS_DRAFT = isDraft;
  }

  public get sourceLanguage() {
    return this.SOURCE_LANGUAGE;
  }

  public set sourceLanguage(sourceLanguage) {
    this.SOURCE_LANGUAGE = sourceLanguage;
  }

  public get roles() {
    return this.ROLES;
  }

  public set roles(roles) {
    this.ROLES = roles;
  }

  public get teams() {
    return this.TEAMS;
  }

  public set teams(teams) {
    this.TEAMS = teams;
  }

  public get section() {
    return this.SECTION;
  }

  public set section(section) {
    this.SECTION = section;
  }

  public get openForComments() {
    return this.OPEN_FOR_COMMENTS;
  }

  public set openForComments(openForComments) {
    this.OPEN_FOR_COMMENTS = openForComments;
  }

  public get author() {
    return this.AUTHOR;
  }

  public set author(author) {
    this.AUTHOR = author;
  }

  public get labels() {
    return this.LABELS;
  }

  public set labels(labels) {
    this.LABELS = labels;
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
