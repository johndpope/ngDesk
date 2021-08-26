import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ConditionsComponent } from 'src/app/custom-components/conditions/conditions.component';
import { Condition } from 'src/app/models/condition';

@Component({
	selector: 'app-field-filter-dialog',
	templateUrl: './field-filter-dialog.component.html',
	styleUrls: ['./field-filter-dialog.component.scss'],
})
export class FieldFilterDialogComponent implements OnInit {
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public componentLoaded = false;
	public dataFilter = {
		CONDITIONS: [],
	};
	public fields = [];
	public moduleId: any;

	public fieldSettings;
	constructor(
		public dialogRef: MatDialogRef<FieldFilterDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private formbuilder: FormBuilder
	) {}

	public ngOnInit() {
		this.fieldSettings = this.formbuilder.group({
			CONDITIONS: this.formbuilder.array([]),
		});
		this.moduleId = this.data.moduleId;
		if (this.data && this.data.dataFilter && this.data.dataFilter.CONDITIONS) {
			this.data.dataFilter.CONDITIONS.forEach((value) => {
				this.dataFilter.CONDITIONS.push(
					new Condition(
						value.CONDITION,
						value.CONDITION_VALUE,
						value.OPERATOR,
						value.REQUIREMENT_TYPE
					)
				);
			});
		}
		this.componentLoaded = true;
	}
	public save() {
		this.dataFilter.CONDITIONS = this.conditionsComponent.transformConditions();
		this.dialogRef.close(this.dataFilter);
	}
	public cancel() {
		this.dialogRef.close();
	}
}
