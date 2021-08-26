import {
	CdkDragDrop,
	moveItemInArray,
	transferArrayItem,
} from '@angular/cdk/drag-drop';
import { Component, Input, OnInit } from '@angular/core';

@Component({
	selector: 'app-drag-drop-fields',
	templateUrl: './drag-drop-fields.component.html',
	styleUrls: ['./drag-drop-fields.component.scss'],
})
export class DragDropFieldsComponent implements OnInit {
	@Input() public availableFields = [];
	@Input() public shownColumns = [];

	constructor() {}

	public ngOnInit() {}

	// Used for drag and drop of columns in each category
	public drop(event: CdkDragDrop<any[]>) {
		if (event.previousContainer === event.container) {
			moveItemInArray(
				event.container.data,
				event.previousIndex,
				event.currentIndex
			);
		} else {
			transferArrayItem(
				event.previousContainer.data,
				event.container.data,
				event.previousIndex,
				event.currentIndex
			);
		}
	}
}
