import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatChipInputEvent } from '@angular/material/chips';
import { ActivatedRoute, Router } from '@angular/router';
import { OWL_DATE_TIME_FORMATS } from '@danielmoncada/angular-datetime-picker';
import { TranslateService } from '@ngx-translate/core';

import { CookieService } from 'ngx-cookie-service';
import { RenderLayoutService } from 'src/app/render-layout/render-layout.service';
import { UsersService } from 'src/app/users/users.service';
import { DATATYPE, Field } from '../../../../models/field';
import { NewField } from '../../../../models/new-field';
import { ModulesService } from '../../../modules.service';

import { MatDialog } from '@angular/material/dialog';
import { WorkflowApiService } from '@ngdesk/workflow-api';
import { CompaniesService } from '@src/app/companies/companies.service';
import { OWL_DATE_FORMATS } from '@src/app/render-layout/data-types/date-time.service';
import { WebsocketService } from '@src/app/websocket.service';
import { FieldFilterDialogComponent } from 'src/app/dialogs/field-filter-dialog/field-filter-dialog.component';
import { ConditionsDialogComponent } from '../../../../dialogs/conditions-dialog/conditions-dialog.component';
import {
	FieldApiService,
	ModuleApiService,
	RelationshipField,
} from '@ngdesk/module-api';
import * as moment from 'moment';

@Component({
	selector: 'app-field-creator',
	templateUrl: './field-creator.component.html',
	styleUrls: ['./field-creator.component.scss'],
	providers: [{ provide: OWL_DATE_TIME_FORMATS, useValue: OWL_DATE_FORMATS }],
})
export class FieldCreatorComponent implements OnInit {
	public fieldNameFormGroup: FormGroup;
	public fieldFilePreviewNameFormGroup: FormGroup;
	public fieldSelectFormGroup: FormGroup;
	public inheritanceFormGroup: FormGroup;
	public fieldMappingFormGroup: FormGroup;
	public fieldTypes: DATATYPE[] = [
		{ DISPLAY: 'Address', BACKEND: 'String' },
		{ DISPLAY: 'Aggregate', BACKEND: 'Aggregate' },
		{ DISPLAY: 'Approval', BACKEND: 'Approval' },
		{ DISPLAY: 'Auto Number', BACKEND: 'Integer' },
		{ DISPLAY: 'Button', BACKEND: 'Button' },
		{ DISPLAY: 'Checkbox', BACKEND: 'Boolean' },
		{ DISPLAY: 'Chronometer', BACKEND: 'Integer' },
		{ DISPLAY: 'Currency', BACKEND: 'Float' },
		{ DISPLAY: 'Currency Exchange', BACKEND: 'Float' },
		{ DISPLAY: 'Date', BACKEND: 'Date' },
		{ DISPLAY: 'Date/Time', BACKEND: 'Date' },
		{ DISPLAY: 'Discussion', BACKEND: 'String' },
		{ DISPLAY: 'Email', BACKEND: 'String' },
		{ DISPLAY: 'File Upload', BACKEND: 'BLOB' },
		{ DISPLAY: 'File Preview', BACKEND: 'BLOB' },
		{ DISPLAY: 'Formula', BACKEND: 'String' },
		{ DISPLAY: 'Image', BACKEND: 'BLOB' },
		{ DISPLAY: 'List Text', BACKEND: 'Array' },
		{ DISPLAY: 'Number', BACKEND: 'Integer' },
		{ DISPLAY: 'Password', BACKEND: 'String' },
		{ DISPLAY: 'Phone', BACKEND: 'String' },
		{ DISPLAY: 'Picklist', BACKEND: 'String' },
		{ DISPLAY: 'Picklist (Multi-Select)', BACKEND: 'Array' },
		{ DISPLAY: 'Receipt Capture', BACKEND: 'BLOB' },
		{ DISPLAY: 'Relationship', BACKEND: 'String' },
		{ DISPLAY: 'Text', BACKEND: 'String' },
		{ DISPLAY: 'Text Area', BACKEND: 'String' },
		{ DISPLAY: 'Time', BACKEND: 'Date' },
		{ DISPLAY: 'URL', BACKEND: 'String' },
		{ DISPLAY: 'Zoom', BACKEND: 'Zoom' },
	];
	public check = false;
	public isPhoneDatatype = false;
	public defaultCountry = {
		COUNTRY_CODE: '',
		COUNTRY_FLAG: '',
		COUNTRY_DIAL_CODE: '',
		PHONE_NUMBER: '',
	};
	public defaultPhoneNumber = '';
	public autoNumber: any = [];
	public approval: any = [];
	public chronometer: any = [];
	public currency: any = [];
	public discussion: any = [];
	public image: any = [];
	public email: any = [];
	public number: any = [];
	public phone: any = [];
	public picklist: any = [];
	public relationship: any = [];
	public text: any = [];
	public textArea: any = [];
	public url: any = [];
	public selectedField: any = {};
	public field1;
	public field2;
	public dataFilter: any;
	public oneToManyFields = [];
	public parentFields: any;
	public parentFielsForFieldMapping = [];
	public childFielsForFieldMapping = [];
	public childFields: any;
	public childModuleName: any;
	public parentModuleName: any;
	public parentModuleNameForFieldMapping: any;
	public showInheritance: any;
	public showFieldMapping: any;
	public aggregationType = ['sum'];
	public aggregationRelatedFields = [];
	public aggregationObject = {};
	public originalModuleFields = [];
	public datatype;
	public fieldId;
	public stepName = 'Field name and type';
	public field;
	public workflows = [];
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA];
	public modules: { MODULE_ID: string; NAME: string }[];
	public selectedModuleFields: Field[] = [];
	public relationshipTypes: {
		DISPLAY: string;
		TYPE: string;
		DATA_TYPE: string;
	}[];
	public parentModuleFields: Field[] = [];
	public relationshipObj: any = {
		REQUIRED: false,
		DISPLAY_LABEL: '',
		NAME: '',
		PRIMARY_DISPLAY_FIELD: '',
		MODULE: {},
	};
	public showPreviewOfFiles = false;
	public successMessage: string;
	public errorMessage: string;
	public overrideSysLabel = true;
	public emailPattern = /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$/;
	private valid = true;
	private moduleId: string;
	private moduleName: string;
	public originalPicklist = [];
	public selectConcatenate: any = [];
	public selectFields: any = [];
	public fieldInteger: any = [];
	public fieldString: any = [];
	public originalModuleFieldsMapping = [];
	private windowReference;
	private filePreviewField;
	public setIntervalForAPICall;
	public numericFormats = ['#,###,###', '##,##,###', 'None'];
	public formulaFields: any[] = [];
	public relatedFields = {};
	public currencyExchangefields: any[] = [];
	public dateFields: any[] = [];
	public formulaOperators: any[] = [
		{
			DISPLAY_LABEL: '+',
			DATA_TYPE: {
				DISPLAY: 'Text',
			},
		},
		{
			DISPLAY_LABEL: '-',
			DATA_TYPE: {
				DISPLAY: 'Text',
			},
		},
		{
			DISPLAY_LABEL: '*',
			DATA_TYPE: {
				DISPLAY: 'Text',
			},
		},
		{
			DISPLAY_LABEL: '/',
			DATA_TYPE: {
				DISPLAY: 'Text',
			},
		},
		{
			DISPLAY_LABEL: '(',
			DATA_TYPE: {
				DISPLAY: 'Text',
			},
		},
		{
			DISPLAY_LABEL: ')',
			DATA_TYPE: {
				DISPLAY: 'Text',
			},
		},
		{
			DISPLAY_LABEL: '( )',
			DATA_TYPE: {
				DISPLAY: 'Text',
			},
		},
	];
	public formulaSpecialOperators: any[] = [
		{
			DISPLAY_LABEL: 'BLANK SPACE',
			DATA_TYPE: {
				DISPLAY: 'Text',
			},
			VALUE: ' {{inputMessage.BLANK_SPACE}}',
		},
	];
	cursorPossition: any = {};
	isCursorIsActive: boolean = false;
	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private _formBuilder: FormBuilder,
		private modulesService: ModulesService,
		private translateService: TranslateService,
		private companiesService: CompaniesService,
		private renderLayoutService: RenderLayoutService,
		private cookieService: CookieService,
		private usersService: UsersService,
		public dialog: MatDialog,
		private websocketService: WebsocketService,
		private workflowApiService: WorkflowApiService,
		private fieldApiService: FieldApiService
	) {}

	public ngOnInit() {
		moment.tz.setDefault('UTC');
		this.selectConcatenate.push({
			value: 'Concatenate',
			viewValue: 'Concatenate',
		});

		this.moduleId = this.route.snapshot.params['moduleId'];
		this.modulesService
			.getModuleById(this.moduleId)
			.subscribe((response: any) => {
				this.childModuleName = response.NAME;
				this.parentModuleNameForFieldMapping = response.NAME;
			});
		this.modulesService.getFields(this.moduleId).subscribe((response: any) => {
			this.childFields = response.FIELDS;
			this.setFieldsForFormula(response.FIELDS);
			this.setRelatedFieldsForFormula(response.FIELDS);
			this.originalModuleFields[0] = this.childFields;
			response.FIELDS.forEach((element) => {
				if (element.DATA_TYPE.DISPLAY !== 'Relationship') {
					this.parentFielsForFieldMapping.push(element);
					this.originalModuleFieldsMapping.push(element);
					this.childFielsForFieldMapping.push(element);
				}
				if (
					element.DATA_TYPE.BACKEND === 'Integer' ||
					element.DATA_TYPE.DISPLAY === 'Currency' ||
					element.DATA_TYPE.BACKEND === 'String'
				) {
					this.selectFields.push({
						Id: element.FIELD_ID,
						display: element.DATA_TYPE.DISPLAY,
						backend: element.DATA_TYPE.BACKEND,
						viewValue: element.DISPLAY_LABEL,
					});
				}
				if (
					element.DATA_TYPE.DISPLAY === 'Picklist' ||
					element.DATA_TYPE.DISPLAY === 'Text'
				) {
					this.currencyExchangefields.push(element);
				}
				if (element.DATA_TYPE.DISPLAY === 'Date') {
					this.dateFields.push(element);
				}
				switch (element.DATA_TYPE.DISPLAY) {
					case 'Auto Number': {
						this.autoNumber.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Chronometer': {
						this.chronometer.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Currency': {
						this.currency.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Discussion': {
						this.discussion.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Image': {
						this.image.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Email': {
						this.email.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Number': {
						this.number.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Phone': {
						this.phone.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Picklist': {
						this.picklist.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Relationship': {
						this.relationship.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Text': {
						this.text.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Text Area': {
						this.textArea.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'URL': {
						this.url.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Approval': {
						this.approval.push({
							Id: element.FIELD_ID,
							display: element.DATA_TYPE.DISPLAY,
							backend: element.DATA_TYPE.BACKEND,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
				}
			});
		});
		this.fieldNameFormGroup = this._formBuilder.group({
			displayLabel: ['', Validators.required],
			systemLabel: [
				'',
				[Validators.required, Validators.pattern('^[a-zA-Z0-9_]*$')],
			],
			dataType: ['', Validators.required],
		});
		const storedDataType = this.route.snapshot.params['dataType'];
		if (storedDataType && storedDataType !== null && storedDataType !== '') {
			const toSelect = this.fieldTypes.find(
				(f) => f.DISPLAY === storedDataType
			);
			this.fieldNameFormGroup.controls['dataType'].setValue(toSelect, [
				Validators.required,
			]);
		}

		// New Form group for the file preview field creator
		this.fieldFilePreviewNameFormGroup = this._formBuilder.group({
			displayLabel: ['', Validators.required],
			systemLabel: [
				'',
				[Validators.required, Validators.pattern('^[a-zA-Z0-9_]*$')],
			],
			dataType: [
				{ DISPLAY: 'File Preview', BACKEND: 'BLOB' },
				Validators.required,
			],
		});

		this.fieldSelectFormGroup = this._formBuilder.group({
			formulaString: ['', Validators.required],
		});

		this.inheritanceFormGroup = this._formBuilder.group({
			inheritances: this._formBuilder.array([]),
		});

		this.fieldMappingFormGroup = this._formBuilder.group({
			fieldMapping: this._formBuilder.array([]),
		});

		this.cursorPossition.startPossition = 0;
		this.cursorPossition.endPossition = 0;
	}

	public createInheritanceItem(): FormGroup {
		return this._formBuilder.group({
			FIELD_FROM_PARENT_MODULE: [''],
			FIELD_FROM_CHILD_MODULE: [''],
		});
	}

	public createFieldMappingItem(): FormGroup {
		return this._formBuilder.group({
			FIELD_FROM_PARENT_MODULE: [''],
			FIELD_FROM_CHILD_MODULE: [''],
		});
	}

	public addInheritanceOption() {
		const inheritance = this.inheritanceFormGroup.get(
			'inheritances'
		) as FormArray;
		inheritance.push(this.createInheritanceItem());
		this.originalModuleFields[inheritance.value.length - 1] = this.childFields;
	}

	public addFieldMappingOption() {
		const fieldMapping = this.fieldMappingFormGroup.get(
			'fieldMapping'
		) as FormArray;
		fieldMapping.push(this.createFieldMappingItem());
		this.originalModuleFieldsMapping[fieldMapping.value.length - 1] =
			this.childFielsForFieldMapping;
	}

	public removeInheritanceOption(inheritanceIndex) {
		const inheritance = this.inheritanceFormGroup.get(
			'inheritances'
		) as FormArray;
		if (inheritance.value.length >= 1) {
			inheritance.removeAt(inheritanceIndex);
		}
	}

	public removeFieldMappingOption(fieldMappingIndex) {
		const fieldMapping = this.fieldMappingFormGroup.get(
			'fieldMapping'
		) as FormArray;
		if (fieldMapping.value.length >= 1) {
			fieldMapping.removeAt(fieldMappingIndex);
		}
	}

	public enableInheritance(value) {
		if (value === true) {
			this.showInheritance = true;
		} else {
			this.showInheritance = false;
		}
	}

	public enableFieldMapping(value) {
		if (value === true) {
			this.showFieldMapping = true;
		} else {
			this.showFieldMapping = false;
		}
	}

	public filterChildModuleFields(index, field) {
		const selectedParentField = this.parentFields.find(
			(f) => f.FIELD_ID === field.value
		);
		this.originalModuleFields[index] = this.childFields.filter(
			(temp) => temp.DATA_TYPE.BACKEND === selectedParentField.DATA_TYPE.BACKEND
		);
	}

	public filterChildModuleFieldsForFieldMapping(index, field) {
		const selectedParentField = this.parentFielsForFieldMapping.find(
			(f) => f.FIELD_ID === field.value
		);
		this.originalModuleFieldsMapping[index] =
			this.childFielsForFieldMapping.filter(
				(temp) =>
					temp.DATA_TYPE.DISPLAY === selectedParentField.DATA_TYPE.DISPLAY &&
					temp !== selectedParentField
			);
	}

	public updateSystemLabel() {
		if (this.overrideSysLabel) {
			this.fieldNameFormGroup.patchValue({
				systemLabel: this.fieldNameFormGroup.value.displayLabel
					.toUpperCase()
					.replace(/ /g, '_'),
			});
		}
	}

	// Update system label on edit of diplaylabel for file preview field.
	public updateFilePreviewSystemLabel() {
		this.fieldFilePreviewNameFormGroup.patchValue({
			systemLabel: this.fieldFilePreviewNameFormGroup.value.displayLabel
				.toUpperCase()
				.replace(/ /g, '_'),
		});
	}

	public Separator() {
		this.check = this.check === true ? false : true;
	}

	public nextStep() {
		this.errorMessage = '';
		if (
			this.fieldNameFormGroup.value.dataType.DISPLAY === 'Discussion' ||
			this.fieldNameFormGroup.value.dataType.DISPLAY === 'Image' ||
			this.fieldNameFormGroup.value.dataType.DISPLAY === 'Approval'
		) {
		} else {
			if (this.fieldNameFormGroup.valid) {
				this.stepName = 'Field details';
				this.field = new NewField(
					null,
					null,
					0,
					null,
					0,
					0,
					this.fieldNameFormGroup.value.dataType,
					null,
					this.fieldNameFormGroup.value.displayLabel,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					this.fieldNameFormGroup.value.systemLabel,
					null,
					null,
					[],
					null,
					null,
					false,
					false,
					true,
					0,
					null,
					null,
					false,
					false,
					null,
					null,
					null,
					0,
					0,
					false,
					false,
					null,
					false
				);
				if (
					this.fieldNameFormGroup.value.dataType.DISPLAY === 'Number' ||
					this.fieldNameFormGroup.value.dataType.DISPLAY === 'Currency' ||
					this.fieldNameFormGroup.value.dataType.DISPLAY === 'Formula'
				) {
					this.field.NUMERIC_FORMAT = '';
					this.field.PREFIX = '';
					this.field.SUFFIX = '';
				}
				if (this.fieldNameFormGroup.value.dataType.DISPLAY === 'Button') {
					this.setWorkflows();
				}
				if (this.fieldNameFormGroup.value.dataType.DISPLAY === 'Aggregate') {
					this.field.aggregationType = 'sum';
					this.field.notEditable = true;
					this.modulesService.getFields(this.moduleId).subscribe(
						(fieldresponse: any) => {
							fieldresponse.FIELDS.forEach((field) => {
								if (
									field.DATA_TYPE.DISPLAY === 'Relationship' &&
									field.RELATIONSHIP_TYPE === 'One to Many'
								) {
									this.oneToManyFields.push(field);
								}
							});
						},
						(error: any) => {
							this.errorMessage = error.error.ERROR;
						}
					);
				}
				// Get module for relationship type field
				this.modulesService.getAllModules().subscribe(
					(response: any) => {
						this.modules = response.MODULES;
						this.moduleName = response.MODULES.find(
							(moduleObj) => moduleObj.MODULE_ID === this.moduleId
						).NAME;
						if (
							this.fieldNameFormGroup.value.dataType.DISPLAY === 'Relationship'
						) {
							// getting fields of module in which we are creating this field.
							this.modulesService.getFields(this.moduleId).subscribe(
								(fieldresponse: any) => {
									fieldresponse.FIELDS.forEach((field) => {
										if (
											field.DATA_TYPE.DISPLAY !== 'Relationship' &&
											field.DATA_TYPE.DISPLAY !== 'Phone'
										) {
											this.parentModuleFields.push(field);
										}
									});
								},
								(error: any) => {
									this.errorMessage = error.error.ERROR;
								}
							);
						}
					},
					(error: any) => {
						this.errorMessage = error.error.ERROR;
					}
				);
			}
		}
	}

	private setWorkflows() {
		this.workflowApiService.getWorkflows(this.moduleId).subscribe(
			(workflowResponse: any) => {
				this.workflows = workflowResponse.content;
				console.log(this.workflows);
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);
	}

	// private setWorkflows() {
	// 	this.setIntervalForAPICall = setInterval(() => {
	// 		this.workflowApiService.getWorkflows(this.moduleId).subscribe(
	// 			(workflowResponse: any) => {
	// 				this.workflows = workflowResponse.content;
	// 				if (this.workflows.length > 0) {
	// 					clearInterval(this.setIntervalForAPICall);
	// 					if (this.windowReference) {
	// 						this.windowReference.close();
	// 					}
	// 				}
	// 			},
	// 			(error: any) => {
	// 				this.errorMessage = error.error.ERROR;
	// 			}
	// 		);
	// 	}, 5000);
	// }

	// Adding picklist options to array
	public addPicklistOption(event: MatChipInputEvent): void {
		if ((event.value || '').trim()) {
			this.field.picklistValues.push(event.value.trim());
			this.originalPicklist = JSON.parse(
				JSON.stringify(this.field.picklistValues)
			);
			this.optionAlphabetizeChange();
		}
		// Reset the input value
		if (event.input) {
			event.input.value = '';
		}
	}

	public optionAlphabetizeChange() {
		if (this.field.picklistDisplayAlphabetically) {
			this.field.picklistValues.sort((a: string, b: string) =>
				a.toLowerCase() > b.toLowerCase() ? 1 : -1
			);
		} else {
			this.field.picklistValues = JSON.parse(
				JSON.stringify(this.originalPicklist)
			);
		}
		this.firstValueDefaultChange();
	}

	public firstValueDefaultChange() {
		if (this.field.picklistUseFirstValue) {
			this.field.defaultValue = this.field.picklistValues[0];
		} else {
			this.field.defaultValue = null;
		}
	}

	public removeItem(i: number) {
		this.field.picklistValues.splice(i, 1);
		this.originalPicklist.splice(i, 1);
		this.firstValueDefaultChange();
	}
	public getFieldsForModule() {
		const relationField = this.oneToManyFields.find(
			(f) => f.FIELD_ID === this.field['AGGREGATION_FIELD']
		);
		this.modulesService.getFields(relationField.MODULE).subscribe(
			(response: any) => {
				this.aggregationRelatedFields = response.FIELDS.filter(
					(field) =>
						field.DATA_TYPE.BACKEND === 'Integer' ||
						field.DATA_TYPE.BACKEND === 'Float' ||
						field.DATA_TYPE.BACKEND === 'Double' ||
						field.DATA_TYPE.DISPLAY === 'Formula'
				);
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);
	}

	public moduleChange() {
		this.field.module = this.relationshipObj.MODULE.MODULE_ID;
		this.modulesService
			.getModuleById(this.relationshipObj.MODULE.MODULE_ID)
			.subscribe((response: any) => {
				this.parentModuleName = response.NAME;
			});
		// getting fields of referencing module
		this.modulesService
			.getFields(this.relationshipObj.MODULE.MODULE_ID)
			.subscribe(
				(response: any) => {
					this.parentFields = response.FIELDS;
					response.FIELDS.forEach((field) => {
						if (
							field.DATA_TYPE.DISPLAY !== 'Relationship' &&
							field.DATA_TYPE.DISPLAY !== 'Phone'
						) {
							this.selectedModuleFields.push(field);
						}
					});
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
		this.relationshipTypes = [
			{
				DISPLAY: `One ${this.moduleName} has one ${this.relationshipObj.MODULE.NAME}`,
				TYPE: 'One to One',
				DATA_TYPE: 'String',
			},
			{
				DISPLAY: `One ${this.moduleName} has many ${this.relationshipObj.MODULE.NAME}`,
				TYPE: 'One to Many',
				DATA_TYPE: 'Array',
			},
			{
				DISPLAY: `Many ${this.moduleName} has many ${this.relationshipObj.MODULE.NAME}`,
				TYPE: 'Many to Many',
				DATA_TYPE: 'Array',
			},
		];
	}

	public changeType() {
		this.field.dataType.BACKEND = this.relationshipTypes.filter(
			(type) => type.TYPE === this.field.relationshipType
		)[0].DATA_TYPE;
	}

	public updateRelationFieldName() {
		this.relationshipObj.NAME =
			this.relationshipObj.DISPLAY_LABEL.toUpperCase().replace(/ /g, '_');
	}

	/** restricting the space  */

	public restrictSpaceBar(event):boolean{
		const charCode = event.which ? event.which : event.keyCode;
		 if (charCode == 32 ) {
		 	return false;
		 }
		 return true;
	}
	

	public save() {
		this.errorMessage = '';
		if (
			this.fieldNameFormGroup.value.dataType.DISPLAY === 'Discussion' ||
			this.fieldNameFormGroup.value.dataType.DISPLAY === 'Approval'
		) {
			if (this.fieldNameFormGroup.valid) {
				this.field = new NewField(
					null,
					null,
					0,
					null,
					0,
					0,
					this.fieldNameFormGroup.value.dataType,
					null,
					this.fieldNameFormGroup.value.displayLabel,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					this.fieldNameFormGroup.value.systemLabel,
					null,
					null,
					[],
					null,
					null,
					false,
					false,
					true,
					0,
					null,
					null,
					false,
					false,
					null,
					null,
					null,
					0,
					0,
					false,
					false,
					null,
					false
				);
			}
		}
		if (this.fieldNameFormGroup.value.dataType.DISPLAY === 'Image') {
			if (this.fieldNameFormGroup.valid) {
				this.field = new NewField(
					null,
					null,
					0,
					null,
					0,
					0,
					this.fieldNameFormGroup.value.dataType,
					null,
					this.fieldNameFormGroup.value.displayLabel,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					this.fieldNameFormGroup.value.systemLabel,
					null,
					null,
					[],
					null,
					null,
					false,
					false,
					true,
					0,
					null,
					null,
					false,
					false,
					null,
					null,
					null,
					0,
					0,
					false,
					false,
					null,
					false
				);
			}
		}
		if (this.check === false) {
			this.fieldSelectFormGroup.value.separator = null;
		}
		if (this.fieldNameFormGroup.value.dataType.DISPLAY === 'Phone') {
			this.field.defaultValue = `{
			"COUNTRY_CODE": "${this.defaultCountry.COUNTRY_CODE}",
			"DIAL_CODE": "${this.defaultCountry.COUNTRY_DIAL_CODE}",
			"PHONE_NUMBER": "${this.defaultPhoneNumber}",
			"COUNTRY_FLAG": "${this.defaultCountry.COUNTRY_FLAG}"
		}`;
		}
		if (this.fieldNameFormGroup.value.dataType.DISPLAY === 'Formula') {
			if (this.fieldNameFormGroup.valid) {
				this.stepName = 'Field details';
				this.field = new NewField(
					null,
					null,
					0,
					null,
					0,
					0,
					this.fieldNameFormGroup.value.dataType,
					null,
					this.fieldNameFormGroup.value.displayLabel,
					null,
					this.fieldSelectFormGroup.value.formulaString,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					this.fieldNameFormGroup.value.systemLabel,
					null,
					null,
					[],
					null,
					null,
					false,
					false,
					true,
					0,
					null,
					null,
					false,
					false,
					null,
					null,
					null,
					0,
					0,
					false,
					false,
					null,
					false,
					null,
					null,
					null,
					null,
					null,
					null,
					this.field.NUMERIC_FORMAT,
					this.field.PREFIX,
					this.field.SUFFIX,
					null
				);
			}
		}
		if (this.field.dataType.DISPLAY === 'Checkbox') {
			if (this.fieldMappingFormGroup.value.fieldMapping.length !== 0) {
				let fieldMappingObj = {};
				this.fieldMappingFormGroup.value.fieldMapping.forEach((element) => {
					fieldMappingObj[element.FIELD_FROM_PARENT_MODULE] =
						element.FIELD_FROM_CHILD_MODULE;
				});
				this.field.FIELDS_MAPPING = fieldMappingObj;
			}
		}
		if (this.field.dataType.DISPLAY === 'Relationship') {
			if (this.inheritanceFormGroup.value.inheritances.length !== 0) {
				let inheritaceMappingObj = {};
				this.inheritanceFormGroup.value.inheritances.forEach((element) => {
					inheritaceMappingObj[element.FIELD_FROM_PARENT_MODULE] =
						element.FIELD_FROM_CHILD_MODULE;
				});
				this.field.INHERITANCE_MAPPING = inheritaceMappingObj;
			}
			if (this.dataFilter) {
				this.relationshipObj['DATA_FILTER'] = this.dataFilter;
			}

			let relationshipField: RelationshipField = {
				FIELD: this.field,
				RELATED_FIELD: JSON.parse(JSON.stringify(this.relationshipObj)),
			};
			relationshipField.RELATED_FIELD.MODULE = this.moduleId;

			relationshipField['RELATED_FIELD']['DATA_TYPE'] = {
				DISPLAY: 'Relationship',
				BACKEND: 'String',
			};
			this.fieldApiService
				.postRelationshipField(this.moduleId, relationshipField)
				.subscribe(
					(response: any) => {
						this.companiesService.trackEvent(`Created Field`, {
							FIELD_ID: response.FIELD_ID,
							MODULE_ID: this.moduleId,
						});
						this.successMessage =
							this.translateService.instant('SAVED_SUCCESSFULLY');
						const emailChannelUrl = this.cookieService.get(
							'email_channel_redirect'
						);
						this.router.navigate([`modules/${this.moduleId}/fields`]);
						this.cookieService.set('FIELD_DATA_TYPE', '');
					},
					(error: any) => {
						this.errorMessage = error.error.ERROR;
					}
				);

			// this.modulesService
			// 	.postRelationshipField(this.field, this.moduleId, this.relationshipObj)
			// 	.subscribe(
			// 		(response: any) => {
			// 			this.companiesService.trackEvent(`Created Field`, {
			// 				FIELD_ID: response.FIELD_ID,
			// 				MODULE_ID: this.moduleId,
			// 			});
			// 			this.successMessage = this.translateService.instant(
			// 				'SAVED_SUCCESSFULLY'
			// 			);
			// 			const emailChannelUrl = this.cookieService.get(
			// 				'email_channel_redirect'
			// 			);
			// 			this.router.navigate([`modules/${this.moduleId}/fields`]);
			// 			this.cookieService.set('FIELD_DATA_TYPE', '');
			// 		},
			// 		(error: any) => {
			// 			this.errorMessage = error.error.ERROR;
			// 		}
			// 	);
		} else {
			this.validate();
			if (this.valid) {
				if (
					this.field.hasOwnProperty('NUMERIC_FORMAT') &&
					this.field['NUMERIC_FORMAT'] === 'None'
				) {
					this.field.NUMERIC_FORMAT = '';
				}
				if (
					this.field.VISIBILITY &&
					this.field.DEFAULT_VALUE === null &&
					this.field.dataType.DISPLAY !== 'Auto Number'
				) {
					this.errorMessage = this.translateService.instant(
						'NO_DEFAULT_FOR_HIDDEN'
					);
				} else {
					if (
						this.field.HELP_TEXT === null ||
						this.field.HELP_TEXT === undefined
					) {
						this.field.HELP_TEXT = '';
					}
					if (this.isPhoneDatatype) {
						if (this.defaultPhoneNumber === '') {
							this.errorMessage = this.translateService.instant(
								'PHONE_DEFAULT_VALUE_REQUIRED'
							);
						} else {
							this.errorMessage = this.translateService.instant(
								'COUNTRY_DEFAULT_VALUE_REQUIRED'
							);
						}
					}
					this.fieldApiService.postField(this.moduleId, this.field).subscribe(
						(response: any) => {
							this.companiesService.trackEvent(`Created Field`, {
								FIELD_ID: response.FIELD_ID,
								MODULE_ID: this.moduleId,
							});
							this.successMessage =
								this.translateService.instant('SAVED_SUCCESSFULLY');
							const emailChannelUrl = this.cookieService.get(
								'email_channel_redirect'
							);
							this.cookieService.set('FIELD_DATA_TYPE', '');
							this.router.navigate([`modules/${this.moduleId}/fields`]);
						},
						(error: any) => {
							this.errorMessage = this.translateService.instant(
								error.error.ERROR
							);
						}
					);

					// this.modulesService.postField(this.field, this.moduleId).subscribe(
					// 	(response: any) => {
					// 		this.companiesService.trackEvent(`Created Field`, {
					// 			FIELD_ID: response.FIELD_ID,
					// 			MODULE_ID: this.moduleId,
					// 		});
					// 		this.successMessage = this.translateService.instant(
					// 			'SAVED_SUCCESSFULLY'
					// 		);
					// 		const emailChannelUrl = this.cookieService.get(
					// 			'email_channel_redirect'
					// 		);
					// 		this.cookieService.set('FIELD_DATA_TYPE', '');
					// 		this.router.navigate([`modules/${this.moduleId}/fields`]);
					// 	},
					// 	(error: any) => {
					// 		this.errorMessage = this.translateService.instant(
					// 			error.error.ERROR
					// 		);
					// 	}
					// );
				}
			}
			// Post file preview field
			if (
				this.fieldNameFormGroup.value.dataType.DISPLAY === 'File Upload' &&
				this.fieldFilePreviewNameFormGroup.valid
			) {
				this.filePreviewField = new NewField(
					null,
					null,
					0,
					null,
					0,
					0,
					this.fieldFilePreviewNameFormGroup.value.dataType,
					null,
					this.fieldFilePreviewNameFormGroup.value.displayLabel,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					this.fieldFilePreviewNameFormGroup.value.systemLabel,
					null,
					null,
					[],
					null,
					null,
					false,
					false,
					true,
					0,
					null,
					null,
					false,
					false,
					null,
					null,
					null,
					0,
					0,
					false,
					false,
					null,
					true
				);
				this.modulesService
					.postField(this.filePreviewField, this.moduleId)
					.subscribe(
						(response: any) => {
							this.companiesService.trackEvent(`Created Field`, {
								FIELD_ID: response.FIELD_ID,
								MODULE_ID: this.moduleId,
							});
							this.successMessage =
								this.translateService.instant('SAVED_SUCCESSFULLY');
							const emailChannelUrl = this.cookieService.get(
								'email_channel_redirect'
							);
							this.cookieService.set('FIELD_DATA_TYPE', '');
							this.router.navigate([`modules/${this.moduleId}/fields`]);
						},
						(error: any) => {
							this.errorMessage = this.translateService.instant(
								error.error.ERROR
							);
						}
					);
			}
		}
	}

	public navigateToTriggers() {
		const origin = window.location.origin;
		this.windowReference = window.open(
			`${origin}/modules/${this.moduleId}/workflows/create-new`,
			'_blank'
		);
	}

	private validate() {
		if (
			this.field.dataType.DISPLAY === 'Email' &&
			this.field.defaultValue !== null
		) {
			this.valid = this.emailPattern.test(this.field.defaultValue);
		} else if (
			this.isPhoneDatatype &&
			(this.defaultPhoneNumber === '' ||
				this.defaultCountry.COUNTRY_CODE === '')
		) {
			this.valid = false;
		} else if (
			this.field.dataType.DISPLAY !== 'Auto Number' &&
			this.field.dataType.DISPLAY !== 'Chronometer' &&
			(this.field.dataType.BACKEND === 'Integer' ||
				this.field.dataType.BACKEND === 'Float' ||
				this.field.dataType.BACKEND === 'Double')
		) {
			if (
				this.field.PREFIX !== undefined &&
				this.field.PREFIX !== '' &&
				this.field.PREFIX.length > 3
			) {
				this.errorMessage = this.translateService.instant(
					'PREFIX_LENGTH_LIMIT'
				);
				this.valid = false;
			} else if (
				this.field.PREFIX !== undefined &&
				this.field.SUFFIX !== '' &&
				this.field.SUFFIX.length > 3
			) {
				this.errorMessage = this.translateService.instant(
					'SUFFIX_LENGTH_LIMIT'
				);
				this.valid = false;
			} else {
				this.valid = true;
			}
		} else {
			this.valid = true;
		}
	}

	public back() {
		this.errorMessage = '';
		this.stepName = 'Field name and type';
	}

	public compareFn(option1, option2) {
		return option1.DISPLAY === option2.DISPLAY;
	}

	public openSettings() {
		if (this.relationshipObj.MODULE.MODULE_ID) {
			const dialogRef = this.dialog.open(FieldFilterDialogComponent, {
				data: {
					buttonText: this.translateService.instant('SAVE'),
					closeDialog: this.translateService.instant('CANCEL'),
					moduleId: this.relationshipObj.MODULE.MODULE_ID,
					dataFilter: this.dataFilter,
				},
			});
			// EVENT AFTER MODAL DIALOG IS CLOSED
			dialogRef.afterClosed().subscribe((result) => {
				if (result) {
					this.dataFilter = result;
				}
			});
		}
	}

	get inheritanceFormData() {
		return <FormArray>this.inheritanceFormGroup.get('inheritances');
	}

	get fieldMappingFormData() {
		return <FormArray>this.fieldMappingFormGroup.get('fieldMapping');
	}

	createnew() {
		this.router.navigate([`modules/${this.moduleId}/workflows`]);
	}

	public isDefaultField(field): boolean {
		const defaultFields = [
			'ASSIGNEE',
			'CREATED_BY',
			'LAST_UPDATED_BY',
			'ACCOUNT',
		];
		if (defaultFields.indexOf(field.NAME) !== -1) {
			return true;
		}
		return false;
	}

	/** filters fields for current module  */
	setFieldsForFormula(fields) {
		fields.forEach((field) => {
			if (this.validateFieldForFormula(field)) {
				this.formulaFields.push(field);
			}
		});
	}

	public validateFieldForFormula(field) {
		if (
			field.DATA_TYPE.DISPLAY == 'Street 1' ||
			field.DATA_TYPE.DISPLAY == 'Street 2' ||
			field.DATA_TYPE.DISPLAY == 'State' ||
			field.DATA_TYPE.DISPLAY == 'Country' ||
			field.DATA_TYPE.DISPLAY == 'City' ||
			field.DATA_TYPE.DISPLAY == 'Auto Number' ||
			field.DATA_TYPE.DISPLAY == 'Chronometer' ||
			field.DATA_TYPE.DISPLAY == 'Currency' ||
			field.DATA_TYPE.DISPLAY == 'Currency Exchange' ||
			field.DATA_TYPE.DISPLAY == 'Number' ||
			field.DATA_TYPE.DISPLAY == 'Email' ||
			field.DATA_TYPE.DISPLAY == 'Picklist' ||
			field.DATA_TYPE.DISPLAY == 'Zipcode' ||
			field.DATA_TYPE.DISPLAY == 'Formula' ||
			field.DATA_TYPE.DISPLAY == 'Aggregate' ||
			(field.DATA_TYPE.DISPLAY == 'Text' &&
				field.NAME !== 'CHANNEL' &&
				field.NAME !== 'DATA_ID' &&
				field.NAME !== 'PASSWORD') ||
			(field.DATA_TYPE.DISPLAY == 'Relationship' &&
				(field.RELATIONSHIP_TYPE === 'One to One' ||
					field.RELATIONSHIP_TYPE === 'Many to One'))
		) {
			return true;
		} else {
			return false;
		}
	}

	/** To set related module fields*/

	public setRelatedFieldsForFormula(fields) {
		const relationshipFields = fields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY === 'Relationship' &&
				(field.RELATIONSHIP_TYPE === 'One to One' ||
					field.RELATIONSHIP_TYPE === 'Many to One')
		);

		for (const relationshipField of relationshipFields) {
			if (!this.relatedFields.hasOwnProperty(relationshipField.MODULE)) {
				this.modulesService.getFields(relationshipField.MODULE).subscribe(
					(relatedFieldsResponse: any) => {
						this.relatedFields[relationshipField.MODULE] = [];
						relatedFieldsResponse.FIELDS.forEach((field) => {
							if (
								this.validateFieldForFormula(field) &&
								field.DATA_TYPE.DISPLAY !== 'Formula'
							) {
								this.relatedFields[relationshipField.MODULE].push(field);
							}
						});
						this.setRelatedFieldsForFormula(relatedFieldsResponse.FIELDS);
					},
					(relatedFieldsError: any) => {
						console.log(relatedFieldsError);
					}
				);
			}
		}
	}

	/** to stop key down events for Formula creation  */

	public validateKeyEvents(key) {
		const events = [
			'Backspace',
			'ArrowRight',
			'ArrowLeft',
			'ArrowDown',
			'ArrowUp',
		];
		if (events.indexOf(key) !== -1) {
			return true;
		}
		return false;
	}

	onkeydownOfFormula(event) {
		const e = <KeyboardEvent>event;
		const key = e.key;
		if (e.type === 'keydown' && !this.validateKeyEvents(key)) {
			return false;
		}
		return true;
	}

	public concatenateVariables(field, mainItem?, subItem?, subSubItem?) {
		let concatVariable = mainItem.NAME;
		if (subItem) {
			concatVariable += `.${subItem.NAME}`;
			if (subSubItem) {
				concatVariable += `.${subSubItem.NAME}`;
			}
		}
		this.insertInToBody(field, concatVariable);
	}

	onClickOfTextArea(myTextArea) {
		if (
			myTextArea.selectionStart !== this.cursorPossition.startPossition ||
			this.cursorPossition.startPossition == 0
		) {
			this.isCursorIsActive = true;
			this.cursorPossition.startPossition = myTextArea.selectionStart;
			this.cursorPossition.endPossition = myTextArea.selectionEnd;
		} else {
			this.isCursorIsActive = false;
		}
	}

	/** creating formula string  */

	public insertInToBody(field, relatedField?) {
		let formula = this.fieldSelectFormGroup.value.formulaString;
		if (!field.NAME && !relatedField) {
			this.injectOperatorInToBody(field, formula);
		} else {
			let fieldVar;
			fieldVar = field.NAME;
			if (relatedField) {
				fieldVar += `.${relatedField}`;
			}
			if (!formula) {
				formula = this.setValuesForDefaults(fieldVar);
			} else {
				formula = this.insertValuesByCursorValues(
					this.setValuesForDefaults(fieldVar),
					formula
				);
			}
			this.fieldSelectFormGroup.controls['formulaString'].patchValue(formula);
		}
	}

	/* To add operators +,-,*,/,(,), SPACE**/

	public injectOperatorInToBody(field, formula) {
		if (
			(field.DISPLAY_LABEL === '(' || field.DISPLAY_LABEL === '( )') &&
			!formula
		) {
			formula = field.DISPLAY_LABEL;
		} else if (formula && field.DISPLAY_LABEL === 'BLANK SPACE') {
			formula = this.insertValuesByCursorValues(
				'+' + field.VALUE + '+',
				formula
			);
		} else if (formula) {
			formula = this.insertValuesByCursorValues(field.DISPLAY_LABEL, formula);
		}
		this.fieldSelectFormGroup.controls['formulaString'].patchValue(formula);
	}

	/** to insert values as sub string */

	insertValuesByCursorValues(newValue, oldString) {
		if (this.isCursorIsActive) {
			let newString =
				oldString.substring(0, this.cursorPossition.startPossition) +
				newValue +
				oldString.substring(
					this.cursorPossition.endPossition,
					oldString.length
				);
			this.cursorPossition.startPossition += newValue.length;
			this.cursorPossition.endPossition += newValue.length;
			return newString;
		} else {
			return (oldString += newValue);
		}
	}
	/** Inserting hard coded values  */

	setValuesForDefaults(fieldVar) {
		let body;
		switch (fieldVar) {
			case 'ASSIGNEE': {
				body = `{{inputMessage.${fieldVar}.EMAIL_ADDRESS}}`;
				break;
			}
			case 'CREATED_BY': {
				body = `{{inputMessage.${fieldVar}.EMAIL_ADDRESS}}`;
				break;
			}
			case 'LAST_UPDATED_BY': {
				body = `{{inputMessage.${fieldVar}.EMAIL_ADDRESS}}`;
				break;
			}
			case 'ACCOUNT': {
				body = `{{inputMessage.${fieldVar}.ACCOUNT_NAME}}`;
				break;
			}
			default: {
				body = `{{InputMessage.${fieldVar}}}`;
				break;
			}
		}
		return body;
	}

	public openConditionsDialog() {
		const relatedField = this.oneToManyFields.find(
			(field) => field.FIELD_ID === this.field.AGGREGATION_FIELD
		);
		const dialogRef = this.dialog.open(ConditionsDialogComponent, {
			width: '800px',
			data: {
				MODULE: relatedField.MODULE,
				CONDITIONS: this.field.CONDITIONS,
				PARENT_COMPONENT: 'fieldCreator',
			},
			disableClose: false,
			maxHeight: '90vh',
		});
		dialogRef.afterClosed().subscribe((result) => {
			if (result !== 'close') {
				this.field.CONDITIONS = result;
			}
		});
	}
}
