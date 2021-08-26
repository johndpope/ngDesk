import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
  name: 'disableToEnable'
})
export class DisableToEnablePipe implements PipeTransform {
  private translatedVals = {
    yes: '',
    no: ''
  };

  constructor(private translateService: TranslateService) {
    this.translateService.get('YES').subscribe(val => {
      this.translatedVals.yes = val;
    });
    this.translateService.get('NO').subscribe(val => {
      this.translatedVals.no = val;
    });
  }

  public transform(value: any, args?: any): any {
    if (value === true) {
      return this.translatedVals.no;
    } else if (value === false) {
      return this.translatedVals.yes;
    }
    return value;
  }
}
