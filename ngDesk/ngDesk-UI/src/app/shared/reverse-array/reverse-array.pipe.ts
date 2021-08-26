import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'reverse', pure: false })

export class ReversePipe implements PipeTransform {
  public transform(value) {
    if (value) {
      return value.slice().reverse();
    }
    return value;
  }
}
