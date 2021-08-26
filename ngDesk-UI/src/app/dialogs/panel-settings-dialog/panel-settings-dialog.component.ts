import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { ChannelsService } from 'src/app/channels/channels.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { ConditionsComponent } from 'src/app/custom-components/conditions/conditions.component';
import { Condition } from 'src/app/models/condition';
import { ModulesService } from '../../modules/modules.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
	selector: 'app-panel-settings-dialog',
	templateUrl: './panel-settings-dialog.component.html',
	styleUrls: ['./panel-settings-dialog.component.scss'],
})
export class PanelSettingsDialogComponent implements OnInit {
	public actionForm: FormGroup;
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public settings = {
		action: '',
		conditions: [],
	};
	public fields;
	public channellist: any;
	public channels: any = [];
	public fieldlist: any;
	public fieldId: any;
	public moduleId: any;
	public panelSettingsDialogLoaded = false;

	constructor(
		private bannerMessageService: BannerMessageService,
		public dialogRef: MatDialogRef<PanelSettingsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private formbuilder: FormBuilder,
		private modulesService: ModulesService,
		private channelsService: ChannelsService,
		private route: ActivatedRoute,
		private translateService: TranslateService
	) {}

	public ngOnInit() {
		this.moduleId = this.data.moduleId;
		const { fields, customLayout } = this.data;
		this.actionForm = this.formbuilder.group({
			ACTION: ['SHOW', Validators.required],
			CONDITIONS: this.formbuilder.array([]),
		});

		this.channelsService.getAllChannels(this.moduleId).subscribe(
			(response: any) => {
				this.channellist = response.CHANNELS;
				response.CHANNELS.forEach((element) => {
					this.channels.push({
						value: element.NAME,
						viewValue: element.NAME,
					});
				});
				this.modulesService
					.getFields(this.moduleId)
					.subscribe((response: any) => {
						this.fieldlist = response.FIELDS;
						this.fieldlist.forEach((element) => {
							if (element.NAME === 'CHANNEL') {
								this.fieldId = element.FIELD_ID;
							}
						});

						if (
							customLayout.hasOwnProperty('settings') &&
							customLayout.settings !== null &&
							customLayout.settings.hasOwnProperty('action')
						) {
							const { action, conditions } = customLayout.settings;
							this.actionForm.controls['ACTION'].setValue(action);
							this.settings.action = action;

							conditions.forEach((value) => {
								if (value.CONDITION === this.fieldId) {
									this.channellist.forEach((element) => {
										if (element.ID === value.CONDITION_VALUE) {
											this.settings.conditions.push(
												new Condition(
													value.CONDITION,
													element.NAME,
													value.OPERATOR,
													value.REQUIREMENT_TYPE
												)
											);
										}
									});
								} else {
									this.settings.conditions.push(
										new Condition(
											value.CONDITION,
											value.CONDITION_VALUE,
											value.OPERATOR,
											value.REQUIREMENT_TYPE
										)
									);
								}
							});
						}
						this.panelSettingsDialogLoaded = true;
					});
			},
			(error) => {
				console.log(error);
			}
		);
	}

	public save() {
		this.settings.action = this.actionForm.value['ACTION'];
		this.settings.conditions = this.conditionsComponent.transformConditions();
		if (this.settings.conditions.length >= 1) {
			this.dialogRef.close(this.settings);
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant(
					'CONDITIONS_ARE_REQUIRED_TO_SAVE'
				),
			});
		}
	}

	public cancel() {
		this.dialogRef.close();
	}
}
