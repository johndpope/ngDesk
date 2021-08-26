import { formatNumber } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';
import { CompaniesService } from '@src/app/companies/companies.service';

@Pipe({
	name: 'localNumber'
})
export class LocalNumberPipe implements PipeTransform {
	constructor(private session: CompaniesService) {}

	public transform(value: any, format) {
		if (value == null) {
			return '';
		} // !value would also react to zeros.
		if (isNaN(value)) {
			return value;
		}
		return formatNumber(value, this.session.locale, format);
	}
}
