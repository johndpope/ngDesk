import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
@Pipe({
  name: 'allowStyles'
})
export class AllowStylesPipe implements PipeTransform {
  constructor( private sanitized: DomSanitizer ) { }

  public transform(value: any, args?: any): any {
    if (!value) { return ''; }
    return this.sanitized.bypassSecurityTrustHtml(value);
  }

}
