import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class FilterPipe implements PipeTransform {

  public transform(value: any, arg: any): any {
    if (arg === undefined || arg === null || arg === '' || typeof(arg) === 'object') {
      return value;
    }
    return value.filter(v => v.NAME.toLocaleLowerCase().indexOf(arg.toLocaleLowerCase()) !== -1);
  }

}
