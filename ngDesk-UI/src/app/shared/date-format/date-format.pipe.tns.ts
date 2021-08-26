import { DatePipe } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';
import * as _moment from 'moment';
import { CompaniesService } from '@src/app/companies/companies.service';

@Pipe({ name: 'dateFormat' })
export class DateFormatPipe implements PipeTransform {
	constructor(private session: CompaniesService) {}
	// adding a default value in case you don't want to pass the format then 'yyyy-MM-dd' will be used
	public transform(date, format: 'long'): string {
		// if it's valid date then format it
		if (_moment(date, _moment.ISO_8601, true).isValid()) {
			return new DatePipe(this.session.locale).transform(date, format);
		} else {
			return date;
		}
	}
}
