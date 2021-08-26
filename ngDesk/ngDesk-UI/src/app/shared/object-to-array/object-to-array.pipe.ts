import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'objectToArray' })
export class ObjectToArrayPipe implements PipeTransform {
  public transform(value, args: string[]): any {
    const keys = [];
    for (const key in value) {
      if (key !== 'NAME') {
        keys.push({ key: key, value: value[key] });
      }
    }
    return keys;
  }
}
