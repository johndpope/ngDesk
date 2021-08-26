import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'firstLetter'
})
export class FirstLetterPipe implements PipeTransform {

  public transform(value: string, args: any[]): string {
    if (!value) { return ''; }
    return value.charAt(0).toUpperCase(); 
  }

}
