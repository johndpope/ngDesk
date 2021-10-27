import {
	ChangeDetectionStrategy,
	ChangeDetectorRef,
	Component,
	OnInit,
	ViewChild,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { ModulesService } from '../../../modules.service';
import { TaskDetailService } from './task-detail.service';
import { TimezoneService } from './timezone.service';
import { ConditionsComponent } from '@src/app/custom-components/conditions/conditions.component';
import { Condition } from '@src/app/models/condition';
import { TaskApiService, Task } from '@ngdesk/module-api';
import { FilterRuleOptionPipe } from '../../../../custom-components/conditions/filter-rule-option/filter-rule-option.pipe';
import { MatChipInputEvent } from '@angular/material/chips';
import { ENTER, COMMA } from '@angular/cdk/keycodes';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import * as moment from 'moment-timezone/builds/moment-timezone-with-data-2012-2022.min';
import { OWL_DATE_FORMATS } from '@src/app/render-layout/data-types/date-time.service';
import { OWL_DATE_TIME_FORMATS } from '@danielmoncada/angular-datetime-picker';
@Component({
	selector: 'app-task-detail',
	templateUrl: './task-detail.component.html',
	styleUrls: ['./task-detail.component.scss'],
	providers: [{ provide: OWL_DATE_TIME_FORMATS, useValue: OWL_DATE_FORMATS }],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TaskDetailComponent implements OnInit {
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public moduleId: string;
	public timezones: string[];
	public taskId: string;
	public selectIntervalType = [
		'Hour',
		'Day',
		'Week',
		'Month',
		'Quarter',
		'Half Year',
		'Year',
	];
	public taskLoaded = false;
	public modules: any = [];
	public conditions: Condition[] = [];
	public taskForm: FormGroup;
	public actionNames: {
		ACTION: string;
		TYPE: string;
	}[] = [
		{
			ACTION: 'Create Entry',
			TYPE: 'CreateEntry',
		},
	];
	public specialDataTypes: any = [
		'Picklist',
		'Date/Time',
		'Date',
		'Time',
		'Relationship',
		'Picklist (Multi-Select)',
	];
	public createEntryFields: any[] = [];
	public createEntryFieldsInitials: any[] = [];
	public moduleFields: any[] = [];
	public actions: any[] = [];
	public actionName = 'CreateEntry';
	public params;
	private module: any = {};
	public task;
	public selectedTeams = [];
	public tempTeamInput = '';
	public teams: any[] = [];
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	constructor(
		public cdRef: ChangeDetectorRef,
		private translateService: TranslateService,
		private route: ActivatedRoute,
		private formBuilder: FormBuilder,
		private router: Router,
		private bannerMessageService: BannerMessageService,
		private modulesService: ModulesService,
		private loaderService: LoaderService,
		public taskDetailService: TaskDetailService,
		private taskApiService: TaskApiService,
		public timezoneService: TimezoneService
	) {}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.taskId = this.route.snapshot.params['taskId'];
		this.taskForm = this.formBuilder.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
			START_DATE: ['', [Validators.required]],
			STOP_DATE: [''],
			TIMEZONE: ['', [Validators.required]],
			LAST_EXECUTED: [''],
			RECURRENCE: [false],
			INTERVALS: this.formBuilder.group({
				INTERVAL_TYPE: [''],
				INTERVAL_VALUE: [''],
			}),
			// CONDITIONS: this.formBuilder.array([]),formAction
			actions: this.formBuilder.array([]),
		});

		this.timezones = this.timezoneService.timezones;

		this.params = {
			name: { field: this.translateService.instant('NAME') },
			fieldId: { field: this.translateService.instant('FIELD') },
			value: { field: this.translateService.instant('value') },
		};

		this.modulesService.getModules().subscribe((moduleResponse: any) => {
			this.modules = moduleResponse.MODULES.sort((a, b) =>
				a.NAME.localeCompare(b.NAME)
			);
			// REMOVING TEAMS MODULE
			this.modules = this.modules.filter(
				(module) =>
					module.NAME !== 'Teams' &&
					module.NAME !== 'Chats' &&
					module.NAME !== 'Users'
			);
		});
		this.params = {
			name: { field: this.translateService.instant('NAME') },
			fieldId: { field: this.translateService.instant('FIELD') },
			value: { field: this.translateService.instant('value') },
		};

		this.taskDetailService
			.getPrerequisiteData(this.moduleId)
			.subscribe((response) => {
				this.module = response[0];
				this.modules = response[1];

				// REMOVING TEAMS MODULE
				this.modules = this.modules.filter(
					(module) =>
						module.NAME !== 'Teams' &&
						module.NAME !== 'Chats' &&
						module.NAME !== 'Users' &&
						module.NAME !== 'Accounts' &&
						module.NAME !== 'Contacts'
				);

				if (this.taskId !== 'new') {
					this.taskDetailService.getTask(this.taskId, this.moduleId).subscribe(
						(taskResponse: any) => {
							this.task = taskResponse.DATA;
							this.setValueToForm(this.task);
							if (this.task.actions.length !== 0) {
								this.task.actions.forEach((element) => {
									if (element) {
										this.actionName = element.type;
										const module = this.modules.find(
											(moduleFound) =>
												moduleFound['MODULE_ID'] === element.moduleId
										);
										if (element.fields.length !== 0) {
											element.fields.forEach((field) => {
												const fieldObj = module.FIELDS.find((moduleField) => {
													return moduleField.FIELD_ID === field.fieldId;
												});
												if (fieldObj.DATA_TYPE.DISPLAY === 'Relationship') {
													if (fieldObj.RELATIONSHIP_TYPE === 'Many to Many') {
														this.selectedTeams = JSON.parse(field.value);
														//disable the selected field Value
														this.selectedTeams.forEach((element) => {
															this.teams.push(element.DATA_ID);
														});
													} else {
														field.value = JSON.parse(field.value);
													}
												} else {
													field.value = field.value;
												}
												field.fieldId = fieldObj;
												// many to many relationship type displays drop down
												if (
													field.fieldId.RELATIONSHIP_TYPE === 'Many to Many'
												) {
													this.taskDetailService.initializeSubject(
														element.moduleId
													);
													this.taskDetailService.scrollSubject.next([
														field.fieldId,
														'',
														true,
													]);
												}
											});
										}
									}
								});
								const actionsArr = this.taskForm.get('actions') as FormArray;
								for (const action of this.task.actions) {
									if (action.type === 'CreateEntry') {
										const formAction = this.createActions;
										formAction.get('type').setValue(action.type);
										if (action.moduleId) {
											formAction.get('moduleId').setValue(action.moduleId);
											this.changeModule(action.moduleId);
										}
										const fieldsArr = formAction.get('fields') as FormArray;
										for (const fieldVal of action.fields) {
											const formFields = this.fields;
											formFields.get('fieldId').setValue(fieldVal.fieldId);
											formFields.get('value').setValue(fieldVal.value);
											fieldsArr.push(formFields);
										}
										actionsArr.push(formAction);
									}
								}
							}
						},
						(error) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
				} else {
					this.changeAction(this.actionName);
					this.cdRef.detectChanges();
				}
			});
	}

	get createActions(): FormGroup {
		return this.formBuilder.group({
			moduleId: '',
			type: '',
			fields: this.formBuilder.array([]),
		});
	}

	get fields(): FormGroup {
		return this.formBuilder.group({
			fieldId: ['', [Validators.required]],
			value: ['', [Validators.required]],
		});
	}

	public clearFields(event) {
		if (event.checked) {
			this.taskForm.controls['STOP_DATE'].setValue('');
			this.taskForm.controls['INTERVALS']['controls']['INTERVAL_TYPE'].setValue(
				''
			);
			this.taskForm.controls['INTERVALS']['controls'][
				'INTERVAL_VALUE'
			].setValue('');
		}
	}

	public getLocalDateFromUtc(date, timezone) {
		return new Date(moment(date).tz(timezone).format('YYYY-MM-DD HH:mm:ss'));
	}

	public setStartDate() {
		if (
			this.taskId !== 'new' &&
			this.task.startDate === this.taskForm.get('START_DATE').value
		) {
			const startDateTime = this.getLocalDateFromUtc(
				this.task.startDate,
				this.task.timezone
			);
			this.taskForm.controls['START_DATE'].setValue(startDateTime);
		} else {
			const startDate = this.getLocalDateFromUtc(
				new Date().toUTCString(),
				this.taskForm.get('TIMEZONE').value
			);
			this.taskForm.controls['START_DATE'].setValue(startDate);
		}
	}

	addField(action) {
		const field = action.get('fields') as FormArray;
		field.push(this.fields);
		this.createEntryFilterFields();
	}

	deleteField(action, index) {
		action.get('fields').removeAt(index);
	}

	public changeModule(moduleId) {
		this.modules.forEach((element) => {
			if (element.MODULE_ID === moduleId) {
				this.moduleFields = element.FIELDS;
			}
		});
		this.createEntryFilterFields();
		this.taskDetailService.initializeSubject(moduleId);
	}

	public getStartAndStopTimezoneOffset(date) {
		const timezoneOffset = new Date(moment(date)).getTimezoneOffset();
		if (timezoneOffset) {
			const mins = timezoneOffset.toString().substring(1);
			return parseInt(mins);
		} else {
			return 0;
		}
	}

	public subStartAndStopDateTime(dateTime) {
		const formattedDateTime = new Date(
			moment(dateTime) -
				this.getStartAndStopTimezoneOffset(dateTime) * 1000 * 60
		).toISOString();
		return formattedDateTime;
	}

	public addStartAndStopDateTime(dateTime) {
		const formattedDateTime = new Date(
			moment(dateTime) +
				this.getStartAndStopTimezoneOffset(dateTime) * 1000 * 60
		).toISOString();
		return formattedDateTime;
	}

	// Set values from api response to task layout form
	private setValueToForm(taskObj: any) {
		let stopDateTime;
		if (taskObj.stopDate) {
			if (moment.tz.guess() !== taskObj.timezone) {
				stopDateTime = this.subStartAndStopDateTime(taskObj.stopDate);
			} else {
				stopDateTime = taskObj.stopDate;
			}
		}
		let lastExecutedDateTime = null;
		if (taskObj.lastExecuted) {
			if (moment.tz.guess() !== taskObj.timezone) {
				lastExecutedDateTime = this.subStartAndStopDateTime(
					taskObj.lastExecuted
				);
			} else {
				lastExecutedDateTime = taskObj.lastExecuted;
			}
		}
		this.taskForm.controls['NAME'].setValue(taskObj.taskName);
		this.taskForm.controls['DESCRIPTION'].setValue(taskObj.taskDescription);
		this.taskForm.controls['STOP_DATE'].setValue(stopDateTime);
		this.taskForm.controls['START_DATE'].setValue(taskObj.startDate);
		this.taskForm.controls['TIMEZONE'].setValue(taskObj.timezone);
		this.taskForm.controls['LAST_EXECUTED'].setValue(lastExecutedDateTime);
		this.taskForm.controls['RECURRENCE'].setValue(taskObj.recurrence);
		this.taskForm.controls['INTERVALS']['controls']['INTERVAL_TYPE'].setValue(
			taskObj.intervals.intervalType
		);
		this.taskForm.controls['INTERVALS']['controls']['INTERVAL_VALUE'].setValue(
			taskObj.intervals.intervalValue
		);
	}

	private getConditions(conditions: any[]): Condition[] {
		return conditions.map(
			(conditionObj) =>
				new Condition(
					conditionObj.condition,
					conditionObj.conditionValue,
					conditionObj.opearator,
					conditionObj.requirementType
				)
		);
	}

	public changeAction(type) {
		if (type === 'CreateEntry') {
			const actionsArr = this.taskForm.get('actions') as FormArray;
			const formAction = this.createActions;
			const fieldsArr = formAction.get('fields') as FormArray;
			const formFields = this.fields;
			fieldsArr.push(formFields);
			actionsArr.push(formAction);
		}
	}

	// RESET VALUE for CREATE ENTRY
	public resetValue(event, item, filterType) {
		let input = event;
		item.value.value = '';
		if (event && filterType === 'conditions') {
			// if has field ID it means the user slected from the dropdown and didnt
			// type it manually
			if (event.hasOwnProperty('FIELD_ID')) {
				input = event.DISPLAY_LABEL;
			}
			this.createEntryFields = new FilterRuleOptionPipe().transform(
				this.createEntryFieldsInitials,
				input.toLowerCase(),
				filterType
			);
		} else if (filterType === 'conditions') {
			item['controls']['value'].setValue('');
		}
		if (item.value.fieldId.DATA_TYPE === undefined) {
			item.value.fieldId = '';
		} else if (item.value.fieldId.DATA_TYPE.DISPLAY === 'Relationship') {
			this.taskDetailService.scrollSubject.next([item.value.fieldId, '', true]);
		}
	}

	public displayFieldName(value) {
		if (value) {
			return `${value.DISPLAY_LABEL}`;
		}
	}

	public displayPrimaryDisplayField(value) {
		if (value) {
			return value.PRIMARY_DISPLAY_FIELD;
		}
	}

	// FUNCTIONS FOR RELATIONSHIP DATA SEARCH AND SCROLL
	public search(field, value) {
		this.taskDetailService.scrollSubject.next([field, value, true]);
	}

	public onScroll(field, value) {
		this.taskDetailService.scrollSubject.next([field, value, false]);
	}

	public autocompleteClosed(field) {
		this.taskDetailService.scrollSubject.next([field, '', true]);
	}

	public createEntryFilterFields() {
		this.createEntryFields = this.moduleFields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
				field.DATA_TYPE.DISPLAY !== 'Approval' &&
				field.DATA_TYPE.DISPLAY !== 'Button' &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'File Preview' &&
				field.DATA_TYPE.DISPLAY !== 'Formula' &&
				field.DATA_TYPE.DISPLAY !== 'Image' &&
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.DATA_TYPE.DISPLAY !== 'Time Window' &&
				field.DATA_TYPE.DISPLAY !== 'Workflow Stages' &&
				field.DATA_TYPE.DISPLAY !== 'Zoom' &&
				field.DISPLAY_LABEL !== 'Effective to' &&
				field.DISPLAY_LABEL !== 'Date Updated' &&
				field.DISPLAY_LABEL !== 'Created by' &&
				field.DISPLAY_LABEL !== 'Last Updated by' &&
				field.DISPLAY_LABEL !== 'Date Created' &&
				field.DISPLAY_LABEL !== 'Post Id' &&
				field.DISPLAY_LABEL !== 'Source Type' &&
				field.DISPLAY_LABEL !== 'Channel' &&
				field.DISPLAY_LABEL !== 'Created By' &&
				field.DISPLAY_LABEL !== 'Data ID' &&
				field.DISPLAY_LABEL !== 'Effective from'
		);

		this.createEntryFieldsInitials = this.moduleFields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
				field.DATA_TYPE.DISPLAY !== 'Approval' &&
				field.DATA_TYPE.DISPLAY !== 'Button' &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'File Preview' &&
				field.DATA_TYPE.DISPLAY !== 'Formula' &&
				field.DATA_TYPE.DISPLAY !== 'Image' &&
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.DATA_TYPE.DISPLAY !== 'Time Window' &&
				field.DATA_TYPE.DISPLAY !== 'Workflow Stages' &&
				field.DATA_TYPE.DISPLAY !== 'Zoom' &&
				field.DISPLAY_LABEL !== 'Effective to' &&
				field.DISPLAY_LABEL !== 'Date Updated' &&
				field.DISPLAY_LABEL !== 'Created by' &&
				field.DISPLAY_LABEL !== 'Last Updated by' &&
				field.DISPLAY_LABEL !== 'Date Created' &&
				field.DISPLAY_LABEL !== 'Post Id' &&
				field.DISPLAY_LABEL !== 'Source Type' &&
				field.DISPLAY_LABEL !== 'Channel' &&
				field.DISPLAY_LABEL !== 'Created By' &&
				field.DISPLAY_LABEL !== 'Data ID' &&
				field.DISPLAY_LABEL !== 'Effective from'
		);
	}

	public resetOptions(optionType) {
		//  after options is selected reset the autocomplete values
		if (optionType === 'conditions') {
			this.createEntryFields = this.createEntryFieldsInitials;
		}
	}

	public onTeamSelect(event): void {
		this.selectedTeams.push(event.option.value);
		this.teams.push(event.option.value.DATA_ID);
	}

	public removeTeams(element): void {
		const index = this.teams.indexOf(element.DATA_ID);
		if (index >= 0) {
			this.teams.splice(index, 1);
			this.selectedTeams.splice(index, 1);
		}
	}

	public disabledTeamCheck(entry, teams) {
		return teams.indexOf(entry.DATA_ID) !== -1;
	}

	public resetInput(event: MatChipInputEvent) {
		if (event.input) {
			event.input.value = '';
		}
	}

	public save() {
		this.taskForm.get('NAME').markAsTouched();
		if (this.taskForm.valid) {
			let acctionArry;
			acctionArry = this.taskForm.value.actions;
			acctionArry = JSON.parse(JSON.stringify(acctionArry));
			acctionArry.forEach((element) => {
				if (element.fields) {
					element.fields.forEach((field) => {
						if (field.fieldId.DATA_TYPE !== undefined) {
							if (field.fieldId.DATA_TYPE.DISPLAY === 'Relationship') {
								if (field.fieldId.RELATIONSHIP_TYPE === 'Many to Many') {
									field.value = JSON.stringify(this.selectedTeams);
								} else {
									field.value = JSON.stringify(field.value);
								}
							} else {
								field.value = field.value;
							}
							field.fieldId = field.fieldId.FIELD_ID;
						}
					});
				}
			});
			let startDateTime;
			let stopDateTime;
			if (moment.tz.guess() !== this.taskForm.value.TIMEZONE) {
				startDateTime = this.addStartAndStopDateTime(
					this.taskForm.value.START_DATE
				);
				startDateTime = moment(startDateTime).utc();
				startDateTime.tz(this.taskForm.value.TIMEZONE, true);
				startDateTime = new Date(startDateTime);
			} else {
				startDateTime = this.taskForm.value.START_DATE;
			}
			if (this.taskForm.value.STOP_DATE) {
				if (moment.tz.guess() !== this.taskForm.value.TIMEZONE) {
					stopDateTime = this.addStartAndStopDateTime(
						this.taskForm.value.STOP_DATE
					);
				} else {
					stopDateTime = this.taskForm.value.STOP_DATE;
				}
			}
			const taskPayload = {
				moduleId: this.moduleId,
				taskName: this.taskForm.value.NAME,
				taskDescription: this.taskForm.value.DESCRIPTION,
				recurrence: this.taskForm.value.RECURRENCE,
				intervals: {
					intervalType: this.taskForm.value.INTERVALS.INTERVAL_TYPE,
					intervalValue: this.taskForm.value.INTERVALS.INTERVAL_VALUE,
				},
				startDate: startDateTime,
				stopDate: stopDateTime,
				timezone: this.taskForm.value.TIMEZONE,
				action: acctionArry,
			};
			if (this.taskId === 'new') {
				this.taskApiService.addTask(this.moduleId, taskPayload).subscribe(
					(response) => {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SAVED_SUCCESSFULLY'),
						});
						this.router.navigate([`modules/${this.moduleId}/task`]);
					},
					(error) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
				this.loaderService.isLoading = false;
			} else {
				taskPayload['taskId'] = this.taskId;
				this.taskApiService.updateTask(this.moduleId, taskPayload).subscribe(
					(response) => {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
						});
						this.router.navigate([`modules/${this.moduleId}/task`]);
					},
					(error) => {
						this.loaderService.isLoading = false;
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
				this.loaderService.isLoading = false;
			}
		} else {
			this.loaderService.isLoading = false;
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('ENTER_THE_REQUIRED_FIELDS'),
			});
		}
	}
}
