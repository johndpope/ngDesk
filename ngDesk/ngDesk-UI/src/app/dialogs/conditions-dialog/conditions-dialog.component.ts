import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ConditionsComponent } from '@src/app/custom-components/conditions/conditions.component';
import { Condition } from '@src/app/models/condition';
import { ModulesService } from '@src/app/modules/modules.service';
import { Field } from '@src/app/models/field';

@Component({
	selector: 'app-conditions-dialog',
	templateUrl: './conditions-dialog.component.html',
	styleUrls: ['./conditions-dialog.component.scss'],
})
export class ConditionsDialogComponent implements OnInit {
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public conditions = [];
	public conditionsForm: FormGroup;
	public moduleId: string = '';
	public moduleFields: Field[] = [];
	public parentComponent = '';
	constructor(
		private dialogRef: MatDialogRef<ConditionsDialogComponent>,
		private formBuilder: FormBuilder,
		public modulesService: ModulesService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {}

	public ngOnInit() {
		this.conditionsForm = this.formBuilder.group({
			CONDITIONS: this.formBuilder.array([]),
		});
		this.parentComponent = this.data.PARENT_COMPONENT
		if (this.data.CONDITIONS) {
			this.conditions = this.getConditions(this.data.CONDITIONS);
		}
		this.moduleId = this.data.MODULE;

		this.modulesService
			.getModuleById(this.moduleId)
			.subscribe((moduleResponse: any) => {
				this.moduleFields = moduleResponse.FIELDS;
			});
	}

	private getConditions(conditions: any[]): Condition[] {
		if(this.parentComponent==='fieldCreator'){
			return conditions.map(
				(condition) =>
					new Condition(
						condition.CONDITION,
						condition.CONDITION_VALUE,
						condition.OPERATOR,
						condition.REQUIREMENT_TYPE === 'All' ? 'All' : 'Any'
					)
			);
		}else {
		return conditions.map(
			(condition) =>
				new Condition(
					condition.condition,
					condition.value,
					condition.operator,
					condition.requirementType === 'All' ? 'All' : 'Any'
				)
		);
		}
	}

	public saveData() {
		this.conditions = this.conditionsComponent.transformConditions();
		if(this.parentComponent==='dashboardsComponent'){
			const allConditions = [];
			this.conditions.forEach((condition) => {
			const newCondition = {
				condition: condition.CONDITION,
				value: condition.CONDITION_VALUE,
				operator: condition.OPERATOR,
				requirementType: condition.REQUIREMENT_TYPE,
			};
			allConditions.push(newCondition);
		});
		this.dialogRef.close(allConditions);
	    } else {
			this.dialogRef.close(this.conditions);
		}
	}
}
