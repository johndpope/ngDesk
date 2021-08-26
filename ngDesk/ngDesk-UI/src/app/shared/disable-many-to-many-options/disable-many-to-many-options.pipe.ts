import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'disableManyToManyOption' })
export class DisableManyToManyPipe implements PipeTransform {
	constructor() {}
	// adding a default value in case you don't want to pass the format then 'yyyy-MM-dd' will be used
	public transform(entry: any, fieldName: any, entryInLoop: any): boolean {
		if (entry[fieldName]) {
			const item = entry[fieldName].find(
				(val) => val.DATA_ID === entryInLoop.DATA_ID
			);
			if (item) {
				return true;
			}
		}
		return false;
	}
}
