import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'moduleidToName'
})
export class ModuleidToNamePipe implements PipeTransform {

  public transform(module: any): any {
    if (module.hasOwnProperty('NAME')) {
      return module.NAME;
    } else {
      return module;
    }
  }

}
