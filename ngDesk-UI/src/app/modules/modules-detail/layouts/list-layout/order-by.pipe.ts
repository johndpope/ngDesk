import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
	name: 'orderBy',
	pure: false,
})
export class OrderByPipe implements PipeTransform {
	transform(value: any[]): any[] {
		return value.filter((field) => field.FIELD_ID.indexOf('.') === -1);
	}
}
