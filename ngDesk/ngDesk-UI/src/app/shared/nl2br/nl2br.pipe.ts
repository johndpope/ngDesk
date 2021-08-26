import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'nl2br'
})
export class Nl2brPipe implements PipeTransform {

  public transform(value: any, args?: any): any {
    if (!value) { return ''; }
    return value.replace(/(?:\r\n|\r|\n)/g, '<br />');
  }

}
