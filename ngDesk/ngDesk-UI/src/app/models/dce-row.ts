import { DceField } from './dce-field';

export class DceRow {

  constructor( private HEIGHT: number, private FIELDS: DceField[]) {  }

  get height() {
    return this.HEIGHT;
  }

  set height(value) {
    this.HEIGHT = value;
  }

  get fields() {
    return this.FIELDS;
  }

  set fields(value) {
    this.FIELDS = value;
  }
}
