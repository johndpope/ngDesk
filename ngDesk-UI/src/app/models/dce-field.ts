export class DceField {
  constructor(
    private FIELD: string,
    private SIZE: number,
    private DATA_TYPE?: string,
    private DISPLAY_LABEL?: string,
    private VALUE?: any,
    private HELP_TEXT?: string,
    private NAME?: string,
    private PICKLIST_VALUES?: string[],
    private PRIMARY_DISPLAY_FIELD_NAME?: string,
    private RELATIONSHIP_TYPE?: string,
    private NOT_EDITABLE?: boolean,
    private REQUIRED?: boolean
  ) {}

  get field() {
    return this.FIELD;
  }

  set field(value) {
    this.FIELD = value;
  }

  get size() {
    return this.SIZE;
  }

  set size(value) {
    this.SIZE = value;
  }

  get dataType() {
    return this.DATA_TYPE;
  }

  set dataType(value) {
    this.DATA_TYPE = value;
  }

  get dispayLabel() {
    return this.DISPLAY_LABEL;
  }

  set dispayLabel(value) {
    this.DISPLAY_LABEL = value;
  }

  get value() {
    return this.VALUE;
  }

  set value(value) {
    this.VALUE = value;
  }

  get helpText() {
    return this.HELP_TEXT;
  }

  set helpText(value) {
    this.HELP_TEXT = value;
  }

  get name() {
    return this.NAME;
  }

  set name(value) {
    this.NAME = value;
  }

  get picklistValues() {
    return this.PICKLIST_VALUES;
  }

  set picklistValues(value) {
    this.PICKLIST_VALUES = value;
  }

  get primaryDisplayField() {
    return this.PRIMARY_DISPLAY_FIELD_NAME;
  }

  set primaryDisplayField(value) {
    this.PRIMARY_DISPLAY_FIELD_NAME = value;
  }

  get relationshipType() {
    return this.RELATIONSHIP_TYPE;
  }

  set relationshipType(value) {
    this.RELATIONSHIP_TYPE = value;
  }

  get notEditable() {
    return this.NOT_EDITABLE;
  }

  set notEditable(value) {
    this.NOT_EDITABLE = value;
  }

  get required() {
    return this.REQUIRED;
  }

  set required(value) {
    this.REQUIRED = value;
  }
}
