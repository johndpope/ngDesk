import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ConditionsComponent } from '@src/app/custom-components/conditions/conditions.component';
import { Condition } from '@src/app/models/condition';
import { ModulesService } from '@src/app/modules/modules.service';
import { Field } from '@src/app/models/field';
import { ChannelsService } from '@src/app/channels/channels.service';
import { ActivatedRoute } from '@angular/router';

@Component({
	selector: 'app-conditions-dialog',
	templateUrl: './conditions-dialog.component.html',
	styleUrls: ['./conditions-dialog.component.scss'],
})
export class ConditionsDialogComponent implements OnInit {
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public conditions: Condition[] = [];
	public conditionsForm: FormGroup;
	public moduleId: string = '';
	public moduleFields: Field[] = [];
	public channellist: any;
	public channels: any = [];
	public fieldlist: any;
	public fieldId: any;
	constructor(
		private dialogRef: MatDialogRef<ConditionsDialogComponent>,
		private formBuilder: FormBuilder,
		public modulesService: ModulesService,
		private channelsService: ChannelsService,
		private route: ActivatedRoute,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {}

	public ngOnInit() {
		this.moduleId = this.data.MODULE.MODULE_ID;
		this.channelsService.getAllChannels(this.moduleId).subscribe(
			(response: any) => {
				this.channellist = response.CHANNELS;
				response.CHANNELS.forEach((element) => {
					this.channels.push({
						value: element.NAME,
						viewValue: element.NAME,
					});
				});
			},
			(error) => {
				console.log(error);
			}
		);

		this.conditionsForm = this.formBuilder.group({
			CONDITIONS: this.formBuilder.array([]),
		});
		if (this.data) {
			this.getConditions(this.data);
		}
	}

	private getConditions(conditions: any) {
		this.modulesService.getFields(this.moduleId).subscribe((response: any) => {
			this.fieldlist = response.FIELDS;
			this.fieldlist.forEach((element) => {
				if (element.NAME === 'CHANNEL') {
					this.fieldId = element.FIELD_ID;
				}
			});
			this.conditions = this.conditionsComponent.transformConditions();
			for (const condition of conditions.CONDITIONS) {
				if (condition.CONDITION === this.fieldId) {
					this.channellist.forEach((element) => {
						if (element.ID === condition.CONDITION_VALUE) {
							this.conditions.push(
								new Condition(
									condition.CONDITION,
									element.NAME,
									condition.OPERATOR,
									condition.REQUIREMENT_TYPE
								)
							);
						}
					});
				} else {
					this.conditions.push(
						new Condition(
							condition.CONDITION,
							condition.CONDITION_VALUE,
							condition.OPERATOR,
							condition.REQUIREMENT_TYPE
						)
					);
				}
			}
		});
	}

	public saveData() {
		this.conditions = this.conditionsComponent.transformConditions();
		this.dialogRef.close(this.conditions);
	}
}
