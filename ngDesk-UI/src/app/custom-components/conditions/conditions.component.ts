import { Component, Input, OnInit } from '@angular/core';
import {
	ControlContainer,
	FormArray,
	FormBuilder,
	FormGroup,
	FormGroupDirective,
	Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { ChannelsService } from '@src/app/channels/channels.service';
import { ConditionsService } from '@src/app/custom-components/conditions/conditions.service';
import { FilterRuleOptionPipe } from '@src/app/custom-components/conditions/filter-rule-option/filter-rule-option.pipe';
import { Condition } from '@src/app/models/condition';
import { Field } from '@src/app/models/field';
import { ModulesService } from '@src/app/modules/modules.service';
import { RolesService } from '@src/app/roles/roles.service';
import { combineLatest, Subject } from 'rxjs';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	switchMap,
} from 'rxjs/operators';
import { RenderLayoutService } from '../../render-layout/render-layout.service';

export const _filter = (opt: Field[], value: string): Field[] => {
	const filterValue = value.toLowerCase();
	return opt.filter(
		(item) => item.DISPLAY_LABEL.toLowerCase().indexOf(filterValue) !== -1
	);
};
export interface Day {
	option: number;
	viewValue: string;
}

export interface Channel {
	value: string;
	viewValue: string;
}
@Component({
	selector: 'app-conditions',
	templateUrl: './conditions.component.html',
	styleUrls: ['./conditions.component.scss'],
	viewProviders: [
		{ provide: ControlContainer, useExisting: FormGroupDirective },
	],
})
export class ConditionsComponent implements OnInit {
	@Input() public fields: Field[];
	@Input() public pageInfo: Field[];
	@Input() public conditions: Condition[];
	@Input() public parentName: string;
	@Input() public passedInModule: string;
	public CONDITIONS: FormArray;
	public autocompleteConditionsFiltered: {
		LABEL: string;
		FIELDS: Field[];
	}[] = [];
	public autocompleteConditionsInitial: {
		LABEL: string;
		FIELDS: Field[];
	}[] = [];
	public autocompleteOperatorsFiltered: {
		DISPLAY: any;
		BACKEND: string;
	}[] = [];
	public autocompleteOperatorsInitial: { DISPLAY: any; BACKEND: string }[] = [];
	public autocompleteValuesFiltered: any[] = [];
	public autocompleteValuesInitial: any[] = [];
	// FOR CUSTOM VALUES (USERS)
	public autoCompleteCustomValuesFiltered: any[] = [];
	public autoCompleteCustomValuesInitial: any[] = [];
	public customField: Field;
	public params;
	public selectedValue: number;
	public days: Day[] = [];
	public channels: Channel[] = [];
	public channellist: any;
	public fieldlist: any;
	public fieldId: any;
	public moduleId: any;
	public roles: any[] = [];
	public approvalValues: ['Required', 'Approved', 'Rejected'];
	public relationshipDataScrollSubject = new Subject<any>();
	public relationshipScrollAndSearchData = {
		RELATED_MODULE: {},
		SELECTED_CONDITION: {},
	};

	constructor(
		private modulesService: ModulesService,
		private channelsService: ChannelsService,
		private conditionsService: ConditionsService,
		private formBuilder: FormBuilder,
		private translateService: TranslateService,
		public fgd: FormGroupDirective,
		private route: ActivatedRoute,
		private renderLayoutService: RenderLayoutService,
		private rolesService: RolesService
	) {}

	public ngOnInit() {
		this.autocompleteValuesFiltered = [];
		this.autocompleteValuesInitial = [];
		this.autoCompleteCustomValuesFiltered = [];
		this.autoCompleteCustomValuesInitial = [];
		this.approvalValues = ['Required', 'Approved', 'Rejected'];
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.initializeScheduleDataScrollSubject();
		if (this.passedInModule) {
			this.moduleId = this.passedInModule;
		}
		// set the translated values for is required translation params
		this.params = {
			condition: { field: this.translateService.instant('CONDITION') },
			operator: { field: this.translateService.instant('OPERATOR') },
		};

		this.modulesService.getFields(this.moduleId).subscribe((response: any) => {
			this.fieldlist = response.FIELDS;
			this.fieldlist.forEach((element) => {
				if (element.NAME === 'CHANNEL') {
					this.fieldId = element.FIELD_ID;
				}
			});
		});
	
		this.rolesService.getRoles().subscribe(
			(rolesResponse: any) => {
				rolesResponse['ROLES'].filter((role) => {
					if (role.NAME === 'Customers') {
						role['NAME'] = 'Customer';
					}
				});
				this.roles = rolesResponse.ROLES.filter(
					(role) => role.NAME !== 'Public'
				);
			},
			(error: any) => {
				console.log(error);
			}
		);

		this.channelsService
			.getAllChannels(this.moduleId)
			.subscribe((response: any) => {
				this.channellist = response.CHANNELS;
				response.CHANNELS.forEach((element) => {
					this.channels.push({ value: element.ID, viewValue: element.NAME });
				});

				// set conditions from formgroup directive
				this.CONDITIONS = this.fgd.control.get('CONDITIONS') as FormArray;
				// TODO: remove get Modules and getModuleById  call and pass the fields from the parent
				// component using this child component
				// this is here for now. fields really should pass to this component as a an input

				if (this.parentName === 'promptsComponent') {
					this.days.push({
						option: 1,
						viewValue: 'Monday',
					});
					this.days.push({
						option: 2,
						viewValue: 'Tuesday',
					});
					this.days.push({
						option: 3,
						viewValue: 'Wednesday',
					});
					this.days.push({
						option: 4,
						viewValue: 'Thursday',
					});
					this.days.push({
						option: 5,
						viewValue: 'Friday',
					});
					this.days.push({
						option: 6,
						viewValue: 'Saturday',
					});
					this.days.push({
						option: 0,
						viewValue: 'Sunday',
					});
					this.autocompleteConditionsFiltered.push({
						LABEL: 'Time/Date',
						FIELDS: this.fields,
					});

					this.autocompleteConditionsInitial.push({
						LABEL: 'Time/Date',
						FIELDS: this.fields,
					});
					this.autocompleteConditionsFiltered.push({
						LABEL: 'Page Information',
						FIELDS: this.pageInfo,
					});

					this.autocompleteConditionsInitial.push({
						LABEL: 'Page Information',
						FIELDS: this.pageInfo,
					});
				} else {
					// this.modulesService.getModuleById(modulesResponse.MODULE_ID).subscribe(
					if (this.moduleId) {
						combineLatest([
							this.modulesService.getModuleByName('Users'),
							this.modulesService.getModuleById(this.moduleId),
						]).subscribe(
							([userResponse, moduleResponse]: any) => {
								this.initialiseContitions(moduleResponse.FIELDS, userResponse);
							},
							(error) => console.log(error)
						);
					} else {
						this.modulesService.getModuleByName('Users').subscribe(
							(response) => this.initialiseContitions(this.fields, response),
							(error) => console.log(error)
						);
					}
				}
			});
	}

	private initialiseContitions(fields, usersModule) {
		// DO NOT SHOW THESE FIELDS IN DROPDOWN
		fields = fields.filter(
			(field) =>
				field.NAME !== 'PASSWORD' &&
				field.DATA_TYPE.DISPLAY !== 'Button' &&
				field.DATA_TYPE.DISPLAY !== 'Zoom' &&
				field.DATA_TYPE.DISPLAY !== 'Image' &&
				field.DATA_TYPE.DISPLAY !== 'File Preview' &&
				// field.DATA_TYPE.DISPLAY !== 'Approval' &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.DATA_TYPE.DISPLAY !== 'Password' &&
				field.DATA_TYPE.DISPLAY !== 'List Formula'
		);
		if (this.parentName === 'dashboardsComponent') {
			fields = fields.filter(
				(field) =>
					field.DATA_TYPE.DISPLAY === 'Picklist' ||
					field.DATA_TYPE.DISPLAY === 'Approval' ||
					field.DATA_TYPE.DISPLAY === 'Text' ||
					field.NAME === 'TIME_WINDOW' ||
					(field.DATA_TYPE.DISPLAY === 'Relationship' &&
						field.RELATIONSHIP_TYPE === 'Many to One') ||
					(field.DATA_TYPE.DISPLAY === 'Relationship' &&
						field.RELATIONSHIP_TYPE === 'Many to Many')
			);
		}
		if (this.parentName !== 'dashboardsComponent') {
			fields = fields.filter((field) => field.NAME !== 'TIME_WINDOW');
		}
		if (
			this.parentName === 'validationComponent ' ||
			this.parentName === 'taskComponent'
		) {
			fields = fields.filter((field) => {
				return (
					field.RELATIONSHIP_TYPE !== 'One to Many' &&
					field.RELATIONSHIP_TYPE !== 'Many to Many'
				);
			});
		}
		let customField;
		const emailAddress: Field = usersModule.FIELDS.filter(
			(element) => element.NAME === 'EMAIL_ADDRESS'
		)[0];
		this.fields = fields;
		this.autocompleteConditionsFiltered.push({
			LABEL: 'FIELDS',
			FIELDS: fields,
		});

		this.autocompleteConditionsInitial.push({
			LABEL: 'FIELDS',
			FIELDS: fields,
		});

		if (this.parentName === 'triggersComponent') {
			const discussionFieldsFound = fields.filter(
				(item) => item.DATA_TYPE.DISPLAY === 'Discussion'
			);
			if (discussionFieldsFound.length > 0) {
				customField = JSON.parse(
					JSON.stringify(
						fields.filter((item) => item.DATA_TYPE.DISPLAY === 'Discussion')[0]
					)
				);
				const discussionFields: Field[] = [];
				discussionFields.push(customField);
				const displayLabel = discussionFields[0].DISPLAY_LABEL;
				discussionFields[0].DISPLAY_LABEL = 'Last Replied By';
				discussionFields[0].FIELD_ID =
					'{{InputMessage.' + discussionFields[0].NAME + '.LATEST.SENDER}}';

				discussionFields[0].DATA_TYPE.DISPLAY = 'Custom';
				discussionFields[0].DATA_TYPE.BACKEND = 'Custom';
				discussionFields[0].MODULE = usersModule.MODULE_ID;
				discussionFields[0].PRIMARY_DISPLAY_FIELD = emailAddress.FIELD_ID;
				this.autocompleteConditionsFiltered.push({
					LABEL: displayLabel,
					FIELDS: discussionFields,
				});
				this.autocompleteConditionsInitial.push({
					LABEL: displayLabel,
					FIELDS: discussionFields,
				});
			}
		}
		let discussionFieldName;
		for (let i = 0; i < this.conditions.length; i++) {
			for (const field of this.fields) {
				if (field.DATA_TYPE.DISPLAY === 'Discussion') {
					discussionFieldName = field.NAME;
				}
				if (this.conditions[i].condition === field.FIELD_ID) {
					this.CONDITIONS.push(
						this.createCondition(this.conditions[i].requirementType)
					);
					if (field.DATA_TYPE.DISPLAY === 'Relationship') {
						this.getRelationshipValues(field, 'setValue', i);
					} else {
						this.CONDITIONS['controls'][i]['controls'][
							'CONDITION_VALUE'
						].setValue(this.conditions[i].conditionValue);
					}
					this.CONDITIONS['controls'][i]['controls']['CONDITION'].setValue(
						field
					);
					let operatorsArr = this.conditionsService.setOperators(field);
					if (field.DATA_TYPE.DISPLAY === 'Chronometer') {
						const chronValue =
							this.renderLayoutService.chronometerFormatTransform(
								Number(this.conditions[i].conditionValue),
								''
							);
						this.CONDITIONS['controls'][i]['controls'][
							'CONDITION_VALUE'
						].setValue(chronValue);
					}
					// Displaying the CHANNEL Name because it was converted in the transform conditions.
					if (field.NAME === 'CHANNEL') {
						const channel = this.channels.find(
							(channels) =>
								this.conditions[i].conditionValue === channels['value']
						);
						if (channel && channel.viewValue) {
							this.CONDITIONS['controls'][i]['controls'][
								'CONDITION_VALUE'
							].setValue(channel.viewValue);
						}
					}
					if (field.DISPLAY_LABEL === 'Role') {
						this.rolesService.getRoles().subscribe((rolesResponse: any) => {
							this.roles = rolesResponse.ROLES.filter(
								(role) => role.NAME !== 'Public'
							);
							const role = this.roles.find(
								(foundRole) =>
									this.conditions[i].conditionValue === foundRole['ROLE_ID']
							);
							this.autocompleteValuesInitial['ROLE'] = this.roles;
							this.autocompleteValuesFiltered['ROLE'] = this.roles;
							this.CONDITIONS['controls'][i]['controls'][
								'CONDITION_VALUE'
							].setValue(role);
						});
					}
					if (
						this.parentName === 'triggersComponent' &&
						field.DATA_TYPE.DISPLAY !== 'Custom'
					) {
						operatorsArr.push({
							DISPLAY: this.translateService.instant('CHANGED'),
							BACKEND: 'CHANGED',
						});
					}
					if (
						this.parentName === 'slaComponent' ||
						this.parentName === 'validationComponent' ||
						this.parentName === 'taskComponent'
					) {
						operatorsArr.push({
							DISPLAY: this.translateService.instant('IS_SET'),
							BACKEND: 'EXISTS',
						});
						operatorsArr.push({
							DISPLAY: this.translateService.instant('NOT_SET'),
							BACKEND: 'DOES_NOT_EXIST',
						});
					}
					if (this.parentName === 'listLayoutComponent') {
						operatorsArr.push({
							DISPLAY: this.translateService.instant('EXISTS'),
							BACKEND: 'EXISTS',
						});
					}
					operatorsArr = operatorsArr.sort((a, b) =>
						a.DISPLAY.localeCompare(b.DISPLAY)
					);
					const operatorsObj = operatorsArr.find(
						(operator) => operator.BACKEND === this.conditions[i].operator
					);
					this.CONDITIONS['controls'][i]['controls']['OPERATOR'].setValue(
						operatorsObj
					);
				}
			}
			if (
				this.conditions[i].condition ===
				'{{InputMessage.' + discussionFieldName + '.LATEST.SENDER}}'
			) {
				this.CONDITIONS.push(
					this.createCondition(this.conditions[i].requirementType)
				);
				this.CONDITIONS['controls'][i]['controls']['CONDITION'].setValue(
					customField
				);
				const operatorsArr = this.conditionsService.setOperators(customField);
				const operatorsObj = operatorsArr.find(
					(operator) => operator.BACKEND === this.conditions[i].operator
				);
				this.CONDITIONS['controls'][i]['controls']['OPERATOR'].setValue(
					operatorsObj
				);
				this.getRelationshipValues(customField, 'setValue', i);
			}
		}
	}

	private _filterGroup(value: string): { LABEL: string; FIELDS: Field[] }[] {
		if (!value) {
			value = '';
		}
		return this.autocompleteConditionsInitial
			.map((group) => ({
				LABEL: group.LABEL,
				FIELDS: _filter(group.FIELDS, value),
			}))
			.filter((group) => group.FIELDS.length > 0);
	}
	public createCondition(type): FormGroup {
		return this.formBuilder.group({
			CONDITION: ['', [Validators.required]],
			CONDITION_VALUE: [''],
			OPERATOR: ['', [Validators.required]],
			REQUIREMENT_TYPE: type,
		});
	}

	public addCondition(type): void {
		this.CONDITIONS.push(this.createCondition(type));
	}

	public resetOptions(optionType) {
		//  after options is selected reset the autocomplete values
		if (optionType === 'conditions') {
			this.autocompleteConditionsFiltered = this.autocompleteConditionsInitial;
		} else {
			this.autocompleteOperatorsFiltered = this.autocompleteOperatorsInitial;
		}
	}

	public filterInputValues(event, condition, filterType, index) {
		let input = event;
		// tslint:disable-next-line: prefer-switch
		if (event && filterType === 'conditions') {
			// if has field ID it means the user slected from the dropdown and didnt
			// type it manually
			if (event.hasOwnProperty('FIELD_ID')) {
				condition['controls']['OPERATOR'].setValue('');
				condition['controls']['CONDITION_VALUE'].setValue('');
			} else {
				this.autocompleteConditionsFiltered = this._filterGroup(
					input.toString().toLowerCase()
				);
			}
		} else if (filterType === 'conditions') {
			//On changing condition or setting condition as blank, setting ope  rator and value as blank
			condition['controls']['OPERATOR'].setValue('');
			condition['controls']['CONDITION_VALUE'].setValue('');
		} else if (
			(event && filterType === 'values') ||
			(event && filterType === 'picklistValues')
		) {
			// if has Data ID it means the user slected from the dropdown and didnt
			// type it manually
			if (condition.value.CONDITION.DATA_TYPE.DISPLAY === 'Relationship') {
				if (event.hasOwnProperty('DATA_ID')) {
					input = event[event['PRIMARY_DISPLAY_FIELD']];
				} else {
					this.modulesService
						.getModuleById(condition.value.CONDITION.MODULE)
						.subscribe((module: any) => {
							this.relationshipScrollAndSearchData.RELATED_MODULE = module;
							this.relationshipDataScrollSubject.next([event, true, index]);
						});
				}
			}
			if (
				input &&
				condition['controls']['OPERATOR'].value !== '' &&
				this.autocompleteValuesInitial[condition.value.CONDITION.NAME]
			) {
				this.autocompleteValuesFiltered[condition.value.CONDITION.NAME] =
					new FilterRuleOptionPipe().transform(
						this.autocompleteValuesInitial[condition.value.CONDITION.NAME],
						input.toString().toLowerCase(),
						filterType
					);
			}
		} else if (event && filterType === 'roles') {
			// if has Data ID it means the user slected from the dropdown and didnt
			// type it manually
			if (event.hasOwnProperty('NAME')) {
				input = event['NAME'];
			}
			if (
				input &&
				this.autocompleteValuesInitial['ROLE'] &&
				this.autocompleteValuesInitial['ROLE']
			) {
				this.autocompleteValuesFiltered['ROLE'] =
					new FilterRuleOptionPipe().transform(
						this.autocompleteValuesInitial['ROLE'],
						input,
						filterType
					);
			}
		} else {
			this.autocompleteOperatorsInitial = this.conditionsService.setOperators(
				condition.value.CONDITION
			);

			if (
				this.parentName === 'triggersComponent' &&
				event.hasOwnProperty('FIELD_ID') &&
				condition.value.CONDITION.DATA_TYPE.DISPLAY !== 'Custom'
			) {
				this.autocompleteOperatorsInitial.push({
					DISPLAY: this.translateService.instant('CHANGED'),
					BACKEND: 'CHANGED',
				});
				this.autocompleteOperatorsInitial =
					this.autocompleteOperatorsInitial.sort((a, b) =>
						a.DISPLAY.localeCompare(b.DISPLAY)
					);
			}
			if (
				this.parentName === 'slaComponent' ||
				this.parentName === 'validationComponent' ||
				this.parentName === 'taskComponent'
			) {
				this.autocompleteOperatorsInitial.push({
					DISPLAY: this.translateService.instant('IS_SET'),
					BACKEND: 'EXISTS',
				});
				this.autocompleteOperatorsInitial.push({
					DISPLAY: this.translateService.instant('NOT_SET'),
					BACKEND: 'DOES_NOT_EXIST',
				});
				this.autocompleteOperatorsInitial =
					this.autocompleteOperatorsInitial.sort((a, b) =>
						a.DISPLAY.localeCompare(b.DISPLAY)
					);
			}
			if (input && input !== '') {
				if (input.DISPLAY === undefined && this.autocompleteOperatorsInitial) {
					this.autocompleteOperatorsFiltered =
						new FilterRuleOptionPipe().transform(
							this.autocompleteOperatorsInitial,
							input.toLowerCase(),
							filterType
						);
				} else {
					if (this.autocompleteOperatorsInitial) {
						this.autocompleteOperatorsFiltered =
							new FilterRuleOptionPipe().transform(
								this.autocompleteOperatorsInitial,
								input.DISPLAY.toLowerCase(),
								filterType
							);
					}
				}
			}
		}
	}
	public conditionSelected(selected, condition) {
		if (selected.DATA_TYPE.DISPLAY === 'Relationship') {
			this.getRelationshipEntries(selected);
		} else if (selected.DATA_TYPE.DISPLAY === 'Picklist') {
			this.autocompleteValuesInitial[selected.NAME] =
				selected['PICKLIST_VALUES'];
			this.autocompleteValuesFiltered[selected.NAME] =
				selected['PICKLIST_VALUES'];
		} else if (selected.DATA_TYPE.DISPLAY === 'Approval') {
			this.autocompleteValuesInitial[selected.NAME] = this.approvalValues;
			this.autocompleteValuesFiltered[selected.NAME] = this.approvalValues;
		} else if (selected.DATA_TYPE.DISPLAY === 'Custom') {
			this.getRelationshipValues(selected);
		} else if (selected.NAME === 'ROLE') {
			this.autocompleteValuesInitial[selected.NAME] = this.roles;
			this.autocompleteValuesFiltered[selected.NAME] = this.roles;
		}
		condition.CONDITION = selected;
		// after condition is selected set autocomplete values
		this.autocompleteOperatorsFiltered =
			this.conditionsService.setOperators(selected);
		this.autocompleteOperatorsInitial =
			this.conditionsService.setOperators(selected);
		if (
			this.parentName === 'triggersComponent' &&
			selected.DATA_TYPE.DISPLAY !== 'Custom'
		) {
			this.autocompleteOperatorsFiltered.push({
				DISPLAY: this.translateService.instant('CHANGED'),
				BACKEND: 'CHANGED',
			});
			this.autocompleteOperatorsInitial.push({
				DISPLAY: this.translateService.instant('CHANGED'),
				BACKEND: 'CHANGED',
			});
			this.autocompleteOperatorsFiltered =
				this.autocompleteOperatorsFiltered.sort((a, b) =>
					a.DISPLAY.localeCompare(b.DISPLAY)
				);
			this.autocompleteOperatorsInitial =
				this.autocompleteOperatorsInitial.sort((a, b) =>
					a.DISPLAY.localeCompare(b.DISPLAY)
				);
		}
		if (
			this.parentName === 'slaComponent' ||
			this.parentName === 'validationComponent' ||
			this.parentName === 'taskComponent'
		) {
			this.autocompleteOperatorsFiltered.push({
				DISPLAY: this.translateService.instant('IS_SET'),
				BACKEND: 'EXISTS',
			});
			this.autocompleteOperatorsInitial.push({
				DISPLAY: this.translateService.instant('IS_SET'),
				BACKEND: 'EXISTS',
			});

			this.autocompleteOperatorsFiltered.push({
				DISPLAY: this.translateService.instant('NOT_SET'),
				BACKEND: 'DOES_NOT_EXIST',
			});
			this.autocompleteOperatorsInitial.push({
				DISPLAY: this.translateService.instant('NOT_SET'),
				BACKEND: 'DOES_NOT_EXIST',
			});
			this.autocompleteOperatorsFiltered =
				this.autocompleteOperatorsFiltered.sort((a, b) =>
					a.DISPLAY.localeCompare(b.DISPLAY)
				);
			this.autocompleteOperatorsInitial =
				this.autocompleteOperatorsInitial.sort((a, b) =>
					a.DISPLAY.localeCompare(b.DISPLAY)
				);
		}

		if (this.parentName === 'listLayoutComponent') {
			this.autocompleteOperatorsFiltered.push({
				DISPLAY: this.translateService.instant('EXISTS'),
				BACKEND: 'EXISTS',
			});
			this.autocompleteOperatorsInitial.push({
				DISPLAY: this.translateService.instant('EXISTS'),
				BACKEND: 'EXISTS',
			});
			this.autocompleteOperatorsFiltered =
				this.autocompleteOperatorsFiltered.sort((a, b) =>
					a.DISPLAY.localeCompare(b.DISPLAY)
				);
			this.autocompleteOperatorsInitial =
				this.autocompleteOperatorsInitial.sort((a, b) =>
					a.DISPLAY.localeCompare(b.DISPLAY)
				);
		}
	}

	public displayOperator(operator: any): string | undefined {
		return operator ? operator.DISPLAY : undefined;
	}

	public operatorSelected(selected, condition) {
		condition.OPERATOR = selected;
	}

	public removeCondition(conditionIndex) {
		this.CONDITIONS.removeAt(conditionIndex);
	}

	public displayConditionFn(field: any): string | undefined {
		return field ? field.DISPLAY_LABEL : undefined;
	}

	public displayValueFn(value: any): string | undefined {
		return value ? value['PRIMARY_DISPLAY_FIELD'] : undefined;
	}

	public displayRoleValueFn(role: any): string | undefined {
		return role ? role['NAME'] : undefined;
	}

	private getRelationshipValues(field, type?, index?) {
		// Temporary fix
		// TODO: need to figure out where the conditions are changed.
		const currentConditions = this.conditions;
		this.modulesService
			.getFieldById(field.MODULE, field.PRIMARY_DISPLAY_FIELD)
			.subscribe(
				(relationModule: any) => {
					field['RELATION_FIELD_NAME'] = relationModule.NAME;
					this.modulesService.getEntries(field.MODULE).subscribe(
						(entriesResponse: any) => {
							// TODO: remove this when we stop using {{CURRENT_USER}} as condotion value for relationship fields
							this.modulesService
								.getModuleByName('Users')
								.subscribe((userResponse: any) => {
									this.modulesService
										.getModuleByName('Contacts')
										.subscribe((contactResponse: any) => {
											if (
												(this.parentName === 'listLayoutComponent' ||
													this.parentName === 'roleLayoutComponent' ||
													this.parentName === 'dashboardsComponent') &&
												(userResponse.MODULE_ID === field.MODULE ||
													contactResponse.MODULE_ID === field.MODULE)
											) {
												entriesResponse.DATA.push({
													[relationModule.NAME]: '{{CURRENT_USER}}',
													DATA_ID: '{{CURRENT_USER}}',
												});
											}

											if (field.DATA_TYPE.DISPLAY === 'Custom') {
												entriesResponse.DATA.push({
													[relationModule.NAME]: '{{REQUESTOR}}',
													DATA_ID: '{{REQUESTOR}}',
												});
											}

											entriesResponse.DATA.forEach((entry) => {
												entry['PRIMARY_DISPLAY_FIELD'] =
													entry[relationModule.NAME];
											});
											if (
												type === 'setValue' &&
												field.DATA_TYPE.DISPLAY === 'Relationship'
											) {
												this.CONDITIONS['controls'][index]['controls'][
													'CONDITION_VALUE'
												].setValue(
													entriesResponse.DATA.find(
														(entry) =>
															entry.DATA_ID ===
															currentConditions[index].conditionValue
													)
												);
												this.getRelationshipEntries(field);
											} else {
												this.autocompleteValuesInitial[field.NAME] =
													entriesResponse.DATA;
												this.autocompleteValuesFiltered[field.NAME] =
													entriesResponse.DATA;
											}
										});
								});
						},
						(error) => {
							console.log(error);
						}
					);
				},
				(error) => {
					console.log(error);
				}
			);
	}

	public transformConditions(conditions?) {
		let conditionsWithIds = JSON.parse(
			JSON.stringify(this.fgd.control.get('CONDITIONS').value)
		);

		if (conditions) {
			conditionsWithIds = conditions;
		}

		for (const condition of conditionsWithIds) {
			if (typeof condition.CONDITION === 'object') {
				if (
					condition.CONDITION.DATA_TYPE &&
					(condition.CONDITION.DATA_TYPE.DISPLAY === 'Custom' ||
						condition.CONDITION.DATA_TYPE.DISPLAY === 'Relationship') &&
					condition.CONDITION_VALUE &&
					condition.CONDITION_VALUE.hasOwnProperty('DATA_ID')
				) {
					condition.CONDITION_VALUE = condition.CONDITION_VALUE['DATA_ID'];
				} else if (condition.CONDITION.NAME === 'ROLE') {
					condition.CONDITION_VALUE = condition.CONDITION_VALUE['ROLE_ID'];
				} else if (condition.CONDITION.NAME === 'CHANNEL') {
					const channel = this.channels.find(
						(channels) => condition.CONDITION_VALUE === channels['viewValue']
					);
					condition.CONDITION_VALUE = channel.value;
				} else if (
					// if value is changed from relationship type to other datatype
					condition.CONDITION.DATA_TYPE.DISPLAY !== 'Relationship' &&
					condition.CONDITION_VALUE.hasOwnProperty('DATA_ID')
				) {
					condition.CONDITION_VALUE = condition.CONDITION_VALUE['DATA_ID'];
				}
				condition.CONDITION = condition.CONDITION.FIELD_ID;
				// tslint:disable-next-line: triple-equals
				if (
					condition.CONDITION === 'STILL_ON_PAGE' ||
					condition.CONDITION === 'STILL_ON_SITE'
				) {
					condition.OPERATOR = null;
				} else {
					condition.OPERATOR = condition.OPERATOR.BACKEND;
				}
				if (condition.CONDITION === 'DAY_OF_WEEK') {
					switch (condition.CONDITION_VALUE) {
						case 'Monday': {
							condition.CONDITION_VALUE = 1;
							break;
						}
						case 'Tuesday': {
							condition.CONDITION_VALUE = 2;
							break;
						}
						case 'Wednesday': {
							condition.CONDITION_VALUE = 3;
							break;
						}
						case 'Thursday': {
							condition.CONDITION_VALUE = 4;
							break;
						}
						case 'Friday': {
							condition.CONDITION_VALUE = 5;
							break;
						}
						case 'Saturday': {
							condition.CONDITION_VALUE = 6;
							break;
						}
						default: {
							condition.CONDITION_VALUE = 0;
							break;
						}
					}
				}

				if (condition.CONDITION === this.fieldId) {
					this.channellist.forEach((element) => {
						if (element.NAME === condition.CONDITION_VALUE) {
							condition.CONDITION_VALUE = element.ID;
						}
					});
				}
			} else if (
				condition.OPERATOR !== '' &&
				typeof condition.OPERATOR === 'object'
			) {
				condition.OPERATOR = condition.OPERATOR.BACKEND;
			}
		}
		return conditionsWithIds;
	}
	public numberOnly(event): boolean {
		const charCode = event.which ? event.which : event.keyCode;
		if (charCode > 31 && (charCode < 46 || charCode > 57)) {
			return false;
		}
		return true;
	}

	public addCurrentUser(selectedCondition) {
		this.modulesService
			.getModuleByName('Users')
			.subscribe((userResponse: any) => {
				this.modulesService
					.getModuleByName('Contacts')
					.subscribe((contactResponse: any) => {
						if (
							(this.parentName === 'listLayoutComponent' ||
								this.parentName === 'roleLayoutComponent' ||
								this.parentName === 'dashboardsComponent') &&
							(userResponse.MODULE_ID === selectedCondition['MODULE'] ||
								contactResponse.MODULE_ID === selectedCondition['MODULE'])
						) {
							const currentUserExist = this.autocompleteValuesFiltered[
								selectedCondition['NAME']
							].find(
								(currentData) => currentData.DATA_ID === '{{CURRENT_USER}}'
							);
							if (!currentUserExist) {
								const currentUser = {
									PRIMARY_DISPLAY_FIELD: '{{CURRENT_USER}}',
									DATA_ID: '{{CURRENT_USER}}',
								};
								this.autocompleteValuesFiltered[selectedCondition['NAME']] =
									this.autocompleteValuesFiltered[
										selectedCondition['NAME']
									].concat(currentUser);
							}
						}
					});
			});
	}

	public getRelationshipEntries(selected) {
		this.relationshipScrollAndSearchData.SELECTED_CONDITION = selected;
		this.modulesService
			.getModuleById(selected.MODULE)
			.subscribe((module: any) => {
				this.relationshipScrollAndSearchData.RELATED_MODULE = module;
				const primaryDisplayField = module.FIELDS.find(
					(field) => field.FIELD_ID === selected.PRIMARY_DISPLAY_FIELD
				);
				if (primaryDisplayField) {
					this.conditionsService
						.buildQueryToGetRelationshipData(module, 0, primaryDisplayField, '')
						.subscribe((fieldValues: any) => {
							this.autocompleteValuesInitial[selected.NAME] = fieldValues.DATA;
							this.autocompleteValuesFiltered[selected.NAME] = fieldValues.DATA;
							if (this.autocompleteValuesFiltered[selected.NAME].length < 10) {
								this.addCurrentUser(selected);
							}
						});
				}
			});
	}

	public onRelationshipScroll(field) {
		this.relationshipScrollAndSearchData.SELECTED_CONDITION = field;
		this.modulesService.getModuleById(field.MODULE).subscribe((module: any) => {
			this.relationshipScrollAndSearchData.RELATED_MODULE = module;
			this.relationshipDataScrollSubject.next(['', false]);
		});
	}

	public setRelationshipValues(index) {
		if (this.CONDITIONS.value[index].CONDITION_VALUE === '') {
			this.getRelationshipEntries(this.CONDITIONS.value[index].CONDITION);
		}
	}

	public filterNewLists(existingData, newData) {
		const newArr = [];
		newData.forEach((data) => {
			const existingValue = existingData.find(
				(currentData) => currentData.DATA_ID === data.DATA_ID
			);
			if (!existingValue) {
				newArr.push(data);
			}
		});
		return newArr;
	}

	public initializeScheduleDataScrollSubject() {
		this.relationshipDataScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search, index]) => {
					let searchValue = '';
					let page = 0;
					let primaryDisplayField;
					if (!search) {
						primaryDisplayField =
							this.relationshipScrollAndSearchData.RELATED_MODULE[
								'FIELDS'
							].find(
								(field) =>
									field.FIELD_ID ===
									this.relationshipScrollAndSearchData.SELECTED_CONDITION[
										'PRIMARY_DISPLAY_FIELD'
									]
							);
					}
					if (value !== '') {
						this.relationshipScrollAndSearchData.SELECTED_CONDITION =
							this.CONDITIONS.value[index].CONDITION;
						const searchPrimaryDisplayField =
							this.relationshipScrollAndSearchData.RELATED_MODULE[
								'FIELDS'
							].find(
								(field) =>
									field.FIELD_ID ===
									this.relationshipScrollAndSearchData.SELECTED_CONDITION[
										'PRIMARY_DISPLAY_FIELD'
									]
							);
						primaryDisplayField = searchPrimaryDisplayField;
						searchValue = searchPrimaryDisplayField.NAME + '=' + value;
					}
					if (
						!search &&
						this.autocompleteValuesFiltered[
							this.relationshipScrollAndSearchData.SELECTED_CONDITION['NAME']
						]
					) {
						page = Math.ceil(
							this.autocompleteValuesFiltered[
								this.relationshipScrollAndSearchData.SELECTED_CONDITION['NAME']
							].length / 10
						);
					}
					return this.conditionsService
						.buildQueryToGetRelationshipData(
							this.relationshipScrollAndSearchData.RELATED_MODULE,
							page,
							primaryDisplayField,
							searchValue
						)
						.pipe(
							map((results: any) => {
								if (search) {
									this.autocompleteValuesFiltered[
										this.relationshipScrollAndSearchData.SELECTED_CONDITION[
											'NAME'
										]
									] = results['DATA'];
								} else {
									const newlist = this.filterNewLists(
										this.autocompleteValuesFiltered[
											this.relationshipScrollAndSearchData.SELECTED_CONDITION[
												'NAME'
											]
										],
										results['DATA']
									);
									if (newlist.length > 0) {
										this.autocompleteValuesFiltered[
											this.relationshipScrollAndSearchData.SELECTED_CONDITION[
												'NAME'
											]
										] =
											this.autocompleteValuesFiltered[
												this.relationshipScrollAndSearchData.SELECTED_CONDITION[
													'NAME'
												]
											].concat(newlist);
									}
									if (
										results['DATA'].length < 10 ||
										results['DATA'].length === 0
									) {
										this.addCurrentUser(
											this.relationshipScrollAndSearchData.SELECTED_CONDITION
										);
									}
								}
								return results;
							})
						);
				})
			)
			.subscribe();
	}
}
