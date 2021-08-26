import { DatePipe } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'reportFilterFields',
  pure: false
})
export class ReportFilterFieldsPipe implements PipeTransform {

  public transform(value: any, field?: any): any {
    if (field.RELATIONSHIP_TYPE === 'Many to One' && field.hasOwnProperty('RELATION_FIELD_VALUE')) {
      // return field.RELATION_FIELD_VALUE.find((entry) => entry.DATA_ID === value)[field.RELATION_FIELD_NAME];
      field.RELATION_FIELD_VALUE.find((entry) => {
        if(entry.DATA_ID === value){
          return entry[field.RELATION_FIELD_NAME]
        }
      })
    } else if (field.DATA_TYPE !== undefined && field.DATA_TYPE.BACKEND === 'Timestamp') {
        const date = new Date(value);  // if orginal type was a string
        let format = 'MMM d, y, h:mm a';
        if (!isNaN(date.getTime())) {
          if (field.DATA_TYPE.DISPLAY === 'Date') {
            format = 'MMM d, y';
          } else if (field.DATA_TYPE.DISPLAY === 'time') {
            format = 'shortTime';
          }
          return new DatePipe('en-US').transform(date, format);
        }
    } else {
      return value;
    }
  }
}
