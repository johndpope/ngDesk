import { DceRow } from './dce-row';

export class DceColumn {

  constructor(private ORDER: number, private SIZE: number, private ROWS: DceRow[]) {  }

  get order() {
    return this.ORDER;
  }

  set order(value) {
    this.ORDER = value;
  }

  get size() {
    return this.SIZE;
  }

  set size(value) {
    this.SIZE = value;
  }

  get rows() {
    return this.ROWS;
  }

  set rows(value) {
    this.ROWS = value;
  }
}
