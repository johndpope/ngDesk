import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'numericFormat' })
export class NumericFormatPipe implements PipeTransform {
	transform(value: number, format?: String, prefix?: String, suffix?: String): String {
		if (value === null || value === undefined) {
			return '';
		} else {
			let afterPoint;
			let isFloat = false;
			let valueInString = value.toString();
			if (valueInString.indexOf('.') > 0) {
				afterPoint = valueInString.substring(
					valueInString.indexOf('.'),
					valueInString.length
				);
				value = Math.floor(value);
				valueInString = value.toString();
				isFloat = true;
			}
			if (format === '##,##,###') {
				let lastThree = valueInString.substring(valueInString.length - 3);
				let otherNumbers = valueInString.substring(0, valueInString.length - 3);
				if (otherNumbers != '') lastThree = ',' + lastThree;
				let result =
					otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ',') + lastThree;
				if (isFloat) {
					result = result + afterPoint;
				}
				result = prefix+' ' + result + ' ' + suffix;
				return result;
			} else if (format === '#,###,###') {
				let result = valueInString.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
				if (isFloat) {
					result = result + afterPoint;
				}
				result = prefix+' ' + result + ' ' + suffix;
				return result;
			} else {
				if (isFloat) {
					valueInString = valueInString + afterPoint;
				}
				valueInString = prefix+' ' + valueInString + ' ' + suffix;
				return valueInString;
			}
		}
	}
}
