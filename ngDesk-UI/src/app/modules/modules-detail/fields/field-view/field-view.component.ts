import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, OnInit, ViewChild } from '@angular/core';
import {
	FormArray,
	FormBuilder,
	FormGroup,
	Validators,
	FormControl,
} from '@angular/forms';
import { MatChipInputEvent } from '@angular/material/chips';
import { ActivatedRoute, Router } from '@angular/router';
import { OWL_DATE_TIME_FORMATS } from '@danielmoncada/angular-datetime-picker';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from 'src/app/companies/companies.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';

import { MatDialog } from '@angular/material/dialog';
import { OWL_DATE_FORMATS } from '@src/app/render-layout/data-types/date-time.service';
import { LoaderService } from 'src/app/custom-components/loader/loader.service';
import { FieldFilterDialogComponent } from 'src/app/dialogs/field-filter-dialog/field-filter-dialog.component';
import { NewField } from 'src/app/models/new-field';
import { RenderLayoutService } from 'src/app/render-layout/render-layout.service';
import { DATATYPE } from '../../../../models/field';
import { ModulesService } from '../../../modules.service';
import { CacheService } from '@src/app/cache.service';
import { WorkflowApiService } from '@ngdesk/workflow-api';
import { ConditionsDialogComponent } from '../../../../dialogs/conditions-dialog/conditions-dialog.component';
import {
	MatAutocomplete,
	MatAutocompleteSelectedEvent,
} from '@angular/material/autocomplete';
import { FieldApiService, ModuleField } from '@ngdesk/module-api';
import * as moment from 'moment';
@Component({
	selector: 'app-field-view',
	templateUrl: './field-view.component.html',
	styleUrls: ['./field-view.component.scss'],
	providers: [{ provide: OWL_DATE_TIME_FORMATS, useValue: OWL_DATE_FORMATS }],
})
export class FieldViewComponent implements OnInit {
	@ViewChild('auto') matAutocomplete: MatAutocomplete;
	public fieldForm: FormGroup;
	public fieldFilePreviewNameFormGroup: FormGroup;
	public phoneNumberForm: FormGroup;
	public fieldSelectFormGroup: FormGroup;
	public inheritanceFormGroup: FormGroup;
	public currencyExchangeFormGroup: FormGroup;
	textAreaCursorPositions: any = {};
	public fieldTypes: DATATYPE[] = [
		{ DISPLAY: 'Auto Number', BACKEND: 'Integer' },
		{ DISPLAY: 'Aggregate', BACKEND: 'Aggregate' },
		{ DISPLAY: 'Approval', BACKEND: 'Approval' },
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
		{ DISPLAY: 'Formula', BACKEND: 'Formula' },
		{ DISPLAY: 'Formula', BACKEND: 'Integer' },
		{ DISPLAY: 'Formula', BACKEND: 'Float' },
		{ DISPLAY: 'Formula', BACKEND: 'String' },
		{ DISPLAY: 'Formula', BACKEND: 'Double' },
		{ DISPLAY: 'List Formula', BACKEND: 'String' },
		{ DISPLAY: 'List Text', BACKEND: 'Array' },
		{ DISPLAY: 'Number', BACKEND: 'Integer' },
		{ DISPLAY: 'Phone', BACKEND: 'String' },
		{ DISPLAY: 'Password', BACKEND: 'String' },
		{ DISPLAY: 'Picklist', BACKEND: 'String' },
		{ DISPLAY: 'Picklist (Multi-Select)', BACKEND: 'Array' },
		{ DISPLAY: 'Receipt Capture', BACKEND: 'BLOB' },
		{ DISPLAY: 'Relationship', BACKEND: 'String' },
		{ DISPLAY: 'Relationship', BACKEND: 'Array' },
		{ DISPLAY: 'Text', BACKEND: 'String' },
		{ DISPLAY: 'Text Area', BACKEND: 'String' },
		{ DISPLAY: 'Time', BACKEND: 'Date' },
		{ DISPLAY: 'URL', BACKEND: 'String' },
		{ DISPLAY: 'Street 1', BACKEND: 'String' },
		{ DISPLAY: 'Street 2', BACKEND: 'String' },
		{ DISPLAY: 'City', BACKEND: 'String' },
		{ DISPLAY: 'Country', BACKEND: 'String' },
		{ DISPLAY: 'State', BACKEND: 'String' },
		{ DISPLAY: 'Zipcode', BACKEND: 'String' },
		{ DISPLAY: 'Zoom', BACKEND: 'Zoom' },
		{ DISPLAY: 'Image', BACKEND: 'BLOB' },
		{ DISPLAY: 'PDF', BACKEND: 'BLOB' },
		{ DISPLAY: 'Time Window', BACKEND: 'Timestamp' },
		{ DISPLAY: 'Date/Time', BACKEND: 'Timestamp' },
	];
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA];
	private moduleId: string;
	public fieldId: string;
	public formulaFieldSet = false;
	public overrideSysLabel = true;
	public successMessage: string;
	public errorMessage: string;
	public modules: { MODULE_ID: string; NAME: string }[];
	public relationshipModule = {};
	public disableFields: boolean = false;
	public listFormulaFormArray: FormArray;
	public relationshipTypes: {
		DISPLAY: string;
		TYPE: string;
		DATA_TYPE: string;
	}[];
	public selectedModuleFields = [];
	private moduleName: string;
	private originalPicklist = [];
	// public selectConcatenate: any = [];
	public selectFields: any = [];
	public fieldInteger: any = [];
	public fieldString: any = [];
	public selectedField: any = {};
	public check = false;
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
	public fieldLhs: any = [];
	public fieldRhs: any = [];
	public originalModuleFields = [];
	public originalModuleFieldsMapping = [];
	public fieldArray: any;
	public childFields: any;
	public childModuleName: any;
	public showInheritance: any;
	public showFieldMapping: any;
	public operatorSelected: any;
	public valid = true;
	public workflows = [];
	private dataFilter: any;
	public oneToManyFields = [];
	public aggregationType = ['sum'];
	public aggregationRelatedFields = [];
	public fieldMappingFormGroup: FormGroup;
	public selectedParentFielsForFieldMapping = [];
	public selectedChildFielsForFieldMapping = [];
	public parentModuleNameForFieldMapping: any;
	public numericFormats = ['#,###,###', '##,##,###', 'None'];
	setIntervalForAPICall: NodeJS.Timeout;
	private windowReference;
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
		private bannerMessageService: BannerMessageService,
		private modulesService: ModulesService,
		private translateService: TranslateService,
		private companiesService: CompaniesService,
		private renderLayoutService: RenderLayoutService,
		private loaderService: LoaderService,
		private dialog: MatDialog,
		private cacheService: CacheService,
		private fieldService: FieldApiService,
		private workflowApiService: WorkflowApiService,
		private fieldApiService: FieldApiService
	) {}

	public ngOnInit() {
		moment.tz.setDefault('UTC');
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.fieldId = this.route.snapshot.params['fieldId'];
		// this.selectConcatenate.push({
		// 	value: 'Concatenate',
		// 	viewValue: 'Concatenate',
		// });

		this.phoneNumberForm = this._formBuilder.group({
			COUNTRY: [{}],
			PHONE_NUMBER: [''],
		});
		this.fieldSelectFormGroup = this._formBuilder.group({
			formulaString: ['', Validators.required],
		});
		this.currencyExchangeFormGroup = this._formBuilder.group({
			toCurrency: ['', Validators.required],
			fromCurrency: ['', Validators.required],
			dateIncurred: ['', Validators.required],
		});
		this.listFormulaFormArray = this._formBuilder.array([]);

		this.modulesService
			.getModuleById(this.moduleId)
			.subscribe((response: any) => {
				this.childModuleName = response.NAME;
				this.parentModuleNameForFieldMapping = response.NAME;
			});
		this.modulesService.getFields(this.moduleId).subscribe((response: any) => {
			// this.selectedParentFielsForFieldMapping = response.FIELDS;
			this.childFields = response.FIELDS;
			this.setFieldsForFormula(response.FIELDS);
			this.setRelatedFieldsForFormula(response.FIELDS);
			this.originalModuleFields[0] = this.childFields;
			response.FIELDS.forEach((element) => {
				if (element.DATA_TYPE.DISPLAY !== 'Relationship') {
					this.selectedParentFielsForFieldMapping.push(element);
					this.originalModuleFieldsMapping.push(element);
					this.selectedChildFielsForFieldMapping.push(element);
				}
				if (
					element.DATA_TYPE.BACKEND === 'Integer' ||
					element.DATA_TYPE.DISPLAY === 'Currency' ||
					element.DATA_TYPE.BACKEND === 'String'
				) {
					this.selectFields.push({
						value: element.FIELD_ID,
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
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Chronometer': {
						this.chronometer.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Currency': {
						this.currency.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Discussion': {
						this.discussion.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Image': {
						this.image.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Email': {
						this.email.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Number': {
						this.number.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Phone': {
						this.phone.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Picklist': {
						this.picklist.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Relationship': {
						this.relationship.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Text': {
						this.text.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Text Area': {
						this.textArea.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'URL': {
						this.url.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
					case 'Approval': {
						this.approval.push({
							value: element.FIELD_ID,
							viewValue: element.DISPLAY_LABEL,
						});
						break;
					}
				}
			});
		});

		this.modulesService.getFieldById(this.moduleId, this.fieldId).subscribe(
			(response: any) => {
				const picklistArray = response.PICKLIST_VALUES;
				delete response.PICKLIST_VALUES;
				const conditions = response.CONDITIONS;
				delete response.CONDITIONS;
				this.fieldForm = this._formBuilder.group(response);
				if (
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Relationship' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Checkbox' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Text Area' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Text' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'List Text' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Email' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Currency' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Picklist' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Phone' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Number' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Approval' ||
					this.fieldForm.value.DATA_TYPE.DISPLAY === 'Date/Time'
				) {
					this.disableFields = true;
				} else {
					this.disableFields = false;
				}

				this.fieldForm.controls['NAME'].reset(
					{ value: response.NAME, disabled: true },
					Validators.required
				);
				if (response.DATA_FILTER) {
					this.dataFilter = response.DATA_FILTER;
				}
				this.fieldForm.controls['DISPLAY_LABEL'].setValidators([
					Validators.required,
				]);
				const toSelect = this.fieldTypes.find(
					(f) =>
						f.DISPLAY === response.DATA_TYPE.DISPLAY &&
						f.BACKEND === response.DATA_TYPE.BACKEND
				);

				this.fieldForm.controls['DATA_TYPE'].setValue(toSelect, [
					Validators.required,
				]);
				this.fieldForm.addControl(
					'PICKLIST_VALUES',
					this._formBuilder.array([])
				);
				// add email validator for email field default value
				if (response.DATA_TYPE.DISPLAY === 'Email') {
					this.fieldForm.controls['DEFAULT_VALUE'].setValidators([
						Validators.email,
					]);
				}
				if (
					response.DATA_TYPE.DISPLAY === 'Picklist' ||
					response.DATA_TYPE.DISPLAY === 'Picklist (Multi-Select)'
				) {
					this.addPicklistValues(picklistArray);
				}
				if (response.DATA_TYPE.DISPLAY === 'Relationship') {
					this.setRelationField();
					if (response.INHERITANCE_MAPPING) {
						this.showInheritance = true;
						this.addInheritance(response);
					}
				}
				if (response.DATA_TYPE.DISPLAY === 'Checkbox') {
					if (response.FIELDS_MAPPING) {
						this.showFieldMapping = true;
						this.addFieldMapping(response);
					}
				}
				if (response.DATA_TYPE.DISPLAY === 'Aggregate') {
					this.fieldForm.addControl('CONDITIONS', this._formBuilder.array([]));
					this.addConditionsValues(conditions);
					this.modulesService.getFields(this.moduleId).subscribe(
						(fieldresponse: any) => {
							this.oneToManyFields = fieldresponse.FIELDS.filter(
								(f) =>
									f.DATA_TYPE.DISPLAY === 'Relationship' &&
									f.RELATIONSHIP_TYPE === 'One to Many'
							);
							this.getFieldsForModule();
						},
						(error: any) => {
							this.errorMessage = error.error.ERROR;
						}
					);
				}

				if (response.DATA_TYPE.DISPLAY === 'Formula') {
					this.setFormulaField(response);
				}

				if (response.DATA_TYPE.DISPLAY === 'List Formula') {
					this.setListFormulaField(response);
				}

				if (response.DATA_TYPE.DISPLAY === 'Phone') {
					if (response.DEFAULT_VALUE && response.DEFAULT_VALUE !== null) {
						const defaultValue = JSON.parse(response.DEFAULT_VALUE);
						const countryCode = defaultValue.COUNTRY_CODE;
						const country = this.renderLayoutService.countries.find(
							(element) => element.COUNTRY_CODE === countryCode
						);
						this.phoneNumberForm.controls['COUNTRY'].setValue(country);
						this.phoneNumberForm.controls['PHONE_NUMBER'].setValue(
							defaultValue.PHONE_NUMBER
						);
					}
				}
				if (response.DATA_TYPE.DISPLAY === 'Button') {
					this.fieldForm.controls['WORKFLOW'].setValidators([
						Validators.required,
					]);
					this.fieldForm.controls['WORKFLOW'].setValue(response.WORKFLOW);
					this.setWorkflows();
				}
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);

		this.inheritanceFormGroup = this._formBuilder.group({
			inheritances: this._formBuilder.array([]),
		});

		this.fieldMappingFormGroup = this._formBuilder.group({
			fieldMapping: this._formBuilder.array([]),
		});
		this.cursorPossition.startPossition = 0;
		this.cursorPossition.endPossition = 0;
	}
	private setWorkflows() {
		this.workflowApiService.getWorkflows(this.moduleId).subscribe(
			(workflowResponse: any) => {
				this.workflows = workflowResponse.content;
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);
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
	public removeInheritanceOption(inheritanceIndex) {
		const inheritance = this.inheritanceFormGroup.get(
			'inheritances'
		) as FormArray;
		if (inheritance.value.length > 1) {
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

	public addFieldMappingOption() {
		const fieldMapping = this.fieldMappingFormGroup.get(
			'fieldMapping'
		) as FormArray;
		fieldMapping.push(this.createFieldMappingItem());
		this.originalModuleFieldsMapping[fieldMapping.value.length - 1] =
			this.selectedChildFielsForFieldMapping;
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
		const selectedParentField = this.selectedModuleFields.find(
			(f) => f.FIELD_ID === field.value
		);
		this.originalModuleFields[index] = this.childFields.filter(
			(temp) => temp.DATA_TYPE.BACKEND === selectedParentField.DATA_TYPE.BACKEND
		);
	}

	public filterChildModuleFieldsForFieldMapping(index, field) {
		const selectedParentField = this.selectedParentFielsForFieldMapping.find(
			(f) => f.FIELD_ID === field.value
		);
		this.originalModuleFieldsMapping[index] =
			this.selectedChildFielsForFieldMapping.filter(
				(temp) =>
					temp.DATA_TYPE.DISPLAY === selectedParentField.DATA_TYPE.DISPLAY &&
					temp !== selectedParentField
			);
	}

	public getFieldsForModule() {
		const relationField = this.oneToManyFields.find(
			(f) => f.FIELD_ID === this.fieldForm.controls['AGGREGATION_FIELD'].value
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

	public Separator() {
		this.check = this.check === true ? false : true;
	}

	public setFormulaField(response) {
		this.fieldSelectFormGroup.controls['formulaString'].setValue(
			response.FORMULA
		);
		this.textAreaCursorPositions['myTextArea'] = {
			isCursorActive: false,
			startPosition: 0,
			endPosition: response.FORMULA.length,
		};
		this.formulaFieldSet = true;
	}

	public setListFormulaField(response) {
		response.LIST_FORMULA.forEach((element: any, index) => {
			const textArea: string = 'myTextArea' + this.listFormulaFormArray.length;
			this.textAreaCursorPositions[textArea] = {
				isCursorActive: false,
				startPosition: 0,
				endPosition: element.FORMULA.length,
			};
			this.listFormulaFormArray.push(
				this._formBuilder.group({
					FORMULA_NAME: [{ value: element.FORMULA_NAME, disabled: true }],
					FORMULA_LABEL: [element.FORMULA_LABEL],
					formulaString: [element.FORMULA],
				})
			);
		});
	}

	public onChange(value) {
		const fieldRh: any = [];
		this.fieldArray.forEach((element) => {
			if (element.FIELD_ID === value) {
				if (
					element.DATA_TYPE.BACKEND === 'Integer' ||
					element.DATA_TYPE.BACKEND === ' Currency'
				) {
					this.check = false;
				}
				fieldRh.push({
					Id: value,
					display: element.DATA_TYPE.DISPLAY,
					backend: element.DATA_TYPE.BACKEND,
					viewValue: element.DISPLAY_LABEL,
				});
			}
		});

		this.selectedField = fieldRh[0];
	}

	public openSettings() {
		if (this.fieldForm.get('MODULE')) {
			const dialogRef = this.dialog.open(FieldFilterDialogComponent, {
				data: {
					buttonText: this.translateService.instant('SAVE'),
					closeDialog: this.translateService.instant('CANCEL'),
					moduleId: this.fieldForm.get('MODULE').value,
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

	private setRelationField() {
		// Get module for relationship type field
		this.modulesService.getAllModules().subscribe(
			(moduleResponse: any) => {
				this.modules = moduleResponse.MODULES;
				this.moduleName = moduleResponse.MODULES.find(
					(moduleObj) => moduleObj.MODULE_ID === this.moduleId
				).NAME;
				this.relationshipModule = this.modules.find(
					(module) => module.MODULE_ID === this.fieldForm.get('MODULE').value
				);
				// getting fields of referencing module
				this.modulesService
					.getFields(this.fieldForm.get('MODULE').value)
					.subscribe(
						(referencingModuleFieldsResponse: any) => {
							this.selectedModuleFields =
								referencingModuleFieldsResponse.FIELDS;
							let firstModule = this.moduleName;
							let secondModule = this.relationshipModule['NAME'];
							if (
								this.fieldForm.get('RELATIONSHIP_TYPE').value === 'Many to One'
							) {
								firstModule = this.relationshipModule['NAME'];
								secondModule = this.moduleName;
							}
							this.relationshipTypes = [
								{
									DISPLAY: `One ${firstModule} has one ${secondModule}`,
									TYPE: 'One to One',
									DATA_TYPE: 'String',
								},
								{
									DISPLAY: `One ${firstModule} has many ${secondModule}`,
									TYPE: 'One to Many',
									DATA_TYPE: 'Array',
								},
								{
									DISPLAY: `Many ${firstModule} has many ${secondModule}`,
									TYPE: 'Many to Many',
									DATA_TYPE: 'Array',
								},
							];
						},
						(error: any) => {
							this.errorMessage = error.error.ERROR;
						}
					);
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);
	}

	private addPicklistValues(picklistArray) {
		this.originalPicklist = picklistArray;
		const picklistValues = this.fieldForm.get('PICKLIST_VALUES') as FormArray;
		picklistArray.forEach((value) => {
			picklistValues.push(this._formBuilder.control(value));
		});
	}

	private addConditionsValues(conditions) {
		if (conditions !== null) {
			const conditionsValue = this.fieldForm.get('CONDITIONS') as FormArray;
			conditions.forEach((value) => {
				conditionsValue.push(this._formBuilder.control(value));
			});
		}
	}

	private addInheritance(field) {
		let obj = [];
		for (var key in field.INHERITANCE_MAPPING) {
			const childFieldId = field.INHERITANCE_MAPPING[key];
			const inheritance = {
				FIELD_FROM_PARENT_MODULE: key,
				FIELD_FROM_CHILD_MODULE: childFieldId,
			};
			obj.push(inheritance);
		}
		for (let i = 0; i <= obj.length - 1; i++) {
			this.addInheritanceOption();
		}
		this.inheritanceFormGroup.controls.inheritances.setValue(obj);
	}

	private addFieldMapping(field) {
		let obj = [];
		for (var key in field.FIELDS_MAPPING) {
			const childFieldId = field.FIELDS_MAPPING[key];
			const fieldsMapping = {
				FIELD_FROM_PARENT_MODULE: key,
				FIELD_FROM_CHILD_MODULE: childFieldId,
			};
			obj.push(fieldsMapping);
		}
		for (let i = 0; i < obj.length; i++) {
			this.addFieldMappingOption();
		}
		this.fieldMappingFormGroup.controls.fieldMapping.setValue(obj);
	}

	public optionAlphabetizeChange() {
		if (this.fieldForm.get('PICKLIST_DISPLAY_ALPHABETICALLY').value === true) {
			const picklistValues = this.fieldForm.get('PICKLIST_VALUES').value;
			picklistValues.sort((a: string, b: string) =>
				a.toLowerCase() > b.toLowerCase() ? 1 : -1
			);
			this.fieldForm.get('PICKLIST_VALUES').patchValue(picklistValues);
		} else {
			this.fieldForm.get('PICKLIST_VALUES').setValue(this.originalPicklist);
		}
		this.firstValueDefaultChange();
	}

	public firstValueDefaultChange() {
		if (this.fieldForm.get('PICKLIST_USE_FIRST_VALUE').value) {
			this.fieldForm
				.get('DEFAULT_VALUE')
				.setValue(this.fieldForm.get('PICKLIST_VALUES').value[0]);
		} else {
			this.fieldForm.get('DEFAULT_VALUE').setValue(null);
		}
	}

	public removeItem(index: number): void {
		const picklistValues = this.fieldForm.get('PICKLIST_VALUES') as FormArray;
		if (index >= 0) {
			picklistValues.removeAt(index);
			this.originalPicklist.splice(index, 1);
		}
		this.firstValueDefaultChange();
	}

	// Ading picklist options to array
	public addPicklistOption(event: MatChipInputEvent): void {
		if ((event.value || '').trim()) {
			const picklistValues = this.fieldForm.get('PICKLIST_VALUES') as FormArray;
			picklistValues.push(this._formBuilder.control(event.value.trim()));
			this.originalPicklist = picklistValues.value;
			this.optionAlphabetizeChange();
		}
		// Reset the input value
		if (event.input) {
			event.input.value = '';
		}
	}

	public update() {
		this.loaderService.isLoading = true;
		let field: ModuleField;
		const datatype = this.fieldForm.get('DATA_TYPE').value;
		const displayLabel = this.fieldForm.get('DISPLAY_LABEL').value;
		const systemLabel = this.fieldForm.get('NAME').value;

		if (datatype.DISPLAY === 'Phone') {
			if (!this.phoneNumberForm.valid) {
				this.valid = false;
				this.loaderService.isLoading = false;
			}
			this.fieldForm.value.DEFAULT_VALUE = `{
				"COUNTRY_CODE": "${
					this.phoneNumberForm.value.COUNTRY
						? this.phoneNumberForm.value.COUNTRY.COUNTRY_CODE
						: ''
				}",
				"DIAL_CODE": "${
					this.phoneNumberForm.value.COUNTRY
						? this.phoneNumberForm.value.COUNTRY.COUNTRY_DIAL_CODE
						: ''
				}",
				"PHONE_NUMBER": "${this.phoneNumberForm.value.PHONE_NUMBER}",
				"COUNTRY_FLAG": "${
					this.phoneNumberForm.value.COUNTRY
						? this.phoneNumberForm.value.COUNTRY.COUNTRY_FLAG
						: ''
				}"
			}`;
		}
		if (datatype.DISPLAY === 'Formula') {
			field = {
				NAME: systemLabel,
				DATA_TYPE: datatype,
				DISPLAY_LABEL: displayLabel,
				FORMULA: this.fieldSelectFormGroup.value.formulaString,
				FIELD_ID: this.fieldId,
				VISIBILITY: false,
				REQUIRED: false,
				INTERNAL: false,
				NOT_EDITABLE: false,
				PREFIX: this.fieldForm.value.PREFIX,
				SUFFIX: this.fieldForm.value.SUFFIX,
				NUMERIC_FORMAT: this.fieldForm.value.NUMERIC_FORMAT,
			};
		} else {
			field = this.fieldForm.getRawValue();
			if (datatype.DISPLAY === 'Phone') {
				field['DEFAULT_VALUE'] = this.fieldForm.value.DEFAULT_VALUE;
			}
			if (field.DATA_TYPE.DISPLAY === 'List Formula') {
				field.LIST_FORMULA = this.listFormulaFormArray.getRawValue();
				const formulaList: any[] = field.LIST_FORMULA;
				for (let formula of formulaList) {
					formula['FORMULA'] = formula.formulaString;
				}
			}
		}

		if (
			field.DATA_TYPE.DISPLAY !== 'Auto Number' &&
			(field.DATA_TYPE.BACKEND === 'Integer' ||
				field.DATA_TYPE.BACKEND === 'Float' ||
				field.DATA_TYPE.BACKEND === 'Double' ||
				field.DATA_TYPE.DISPLAY === 'Formula')
		) {
			if (!field.hasOwnProperty('PREFIX') || field['PREFIX'] === null) {
				field['PREFIX'] = '';
			}
			if (!field.hasOwnProperty('SUFFIX') || field['SUFFIX'] === null) {
				field['SUFFIX'] = '';
			}
			if (field['PREFIX'] !== '' && field['PREFIX'].length > 3) {
				this.errorMessage = this.translateService.instant(
					'PREFIX_LENGTH_LIMIT'
				);
				this.loaderService.isLoading = false;
				this.valid = false;
			} else if (field['SUFFIX'] !== '' && field['SUFFIX'].length > 3) {
				this.errorMessage = this.translateService.instant(
					'SUFFIX_LENGTH_LIMIT'
				);
				this.valid = false;
				this.loaderService.isLoading = false;
			} else {
				this.valid = true;
			}
		}
		if (this.dataFilter) {
			field['DATA_FILTER'] = this.dataFilter;
		}
		if (this.valid) {
			if (
				field.hasOwnProperty('NUMERIC_FORMAT') &&
				field['NUMERIC_FORMAT'] === 'None'
			) {
				field['NUMERIC_FORMAT'] = '';
			}
			if (
				field['VISIBILITY'] &&
				field['DEFAULT_VALUE'] === null &&
				datatype.DISPLAY !== 'Auto Number'
			) {
				this.loaderService.isLoading = false;
				this.errorMessage = this.translateService.instant(
					'NO_DEFAULT_FOR_HIDDEN'
				);
			} else {
				if (field['HELP_TEXT'] === null || field['HELP_TEXT'] === undefined) {
					field['HELP_TEXT'] = '';
				}
				if (this.inheritanceFormGroup.value.inheritances.length !== 0) {
					let inheritaceMappingObj = {};
					this.inheritanceFormGroup.value.inheritances.forEach((element) => {
						inheritaceMappingObj[element.FIELD_FROM_PARENT_MODULE] =
							element.FIELD_FROM_CHILD_MODULE;
					});
					field['INHERITANCE_MAPPING'] = inheritaceMappingObj;
				}
				if (this.fieldMappingFormGroup.value.fieldMapping.length !== 0) {
					let fieldMappingObj = {};
					this.fieldMappingFormGroup.value.fieldMapping.forEach((element) => {
						fieldMappingObj[element.FIELD_FROM_PARENT_MODULE] =
							element.FIELD_FROM_CHILD_MODULE;
					});
					field['FIELDS_MAPPING'] = fieldMappingObj;
				}
				this.fieldApiService.updateField(this.moduleId, field).subscribe(
					(response: any) => {
						this.companiesService.trackEvent(`Updated Field`, {
							FIELD_ID: response.FIELD_ID,
							MODULE_ID: this.moduleId,
						});
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SAVED_SUCCESSFULLY'),
						});
						this.router.navigate([`modules/${this.moduleId}/fields`]);
					},
					(error: any) => {
						// this.loaderService.isLoading = false;
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
				// this.fieldService.updateField(this.moduleId, field).subscribe(
				// 	(response: any) => {
				// 		this.companiesService.trackEvent(`Updated Field`, {
				// 			FIELD_ID: response.FIELD_ID,
				// 			MODULE_ID: this.moduleId,
				// 		});
				// 		this.bannerMessageService.successNotifications.push({
				// 			message: this.translateService.instant('SAVED_SUCCESSFULLY'),
				// 		});
				// 		this.router.navigate([`modules/${this.moduleId}/fields`]);
				// 	},
				// 	(error: any) => {
				// 		this.loaderService.isLoading = false;
				// 		this.errorMessage = this.translateService.instant(
				// 			error.error.ERROR
				// 		);
				// 	}
				// );
			}
		}
	}

	get inheritanceFormData() {
		return <FormArray>this.inheritanceFormGroup.get('inheritances');
	}

	get fieldMappingFormData() {
		return <FormArray>this.fieldMappingFormGroup.get('fieldMapping');
	}

	public isDefaultField(field): boolean {
		const defaultFields = ['ASSIGNEE', 'ACCOUNT'];
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
			field.DATA_TYPE.DISPLAY == 'List Formula' ||
			(field.DATA_TYPE.DISPLAY == 'Text' &&
				field.NAME !== 'CHANNEL' &&
				field.NAME !== 'DATA_ID' &&
				field.NAME !== 'PASSWORD') ||
			(field.DATA_TYPE.DISPLAY == 'Relationship' &&
				(field.RELATIONSHIP_TYPE === 'One to One' ||
					(field.RELATIONSHIP_TYPE === 'Many to One' &&
						field.NAME !== 'CREATED_BY' &&
						field.NAME !== 'LAST_UPDATED_BY')))
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

	public concatenateVariables(
		field,
		fieldSelectFormGroup?,
		index?,
		mainItem?,
		subItem?,
		subSubItem?
	) {
		let concatVariable = mainItem.NAME;
		if (subItem) {
			concatVariable += `.${subItem.NAME}`;
			if (subSubItem) {
				concatVariable += `.${subSubItem.NAME}`;
			}
		}
		this.insertInToBody(field, fieldSelectFormGroup, index, concatVariable);
	}

	onClickOfTextArea(myTextArea) {
		if (
			myTextArea.selectionStart !==
				this.textAreaCursorPositions[myTextArea.id].startPosition ||
			this.textAreaCursorPositions[myTextArea.id].startPosition == 0
		) {
			this.textAreaCursorPositions[myTextArea.id] = {
				isCursorActive: true,
				startPosition: myTextArea.selectionStart,
				endPosition: myTextArea.selectionEnd,
			};
		} else {
			this.textAreaCursorPositions[myTextArea.id].isCursorActive = false;
		}
	}

	/** creating formula string  */

	public insertInToBody(field, fieldSelectFormGroup?, index?, relatedField?) {
		if (!fieldSelectFormGroup) {
			fieldSelectFormGroup = this.fieldSelectFormGroup;
		}
		let formula = fieldSelectFormGroup.value.formulaString;
		if (!field.NAME && !relatedField) {
			this.injectOperatorInToBody(field, formula, fieldSelectFormGroup, index);
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
					formula,
					index
				);
			}
			fieldSelectFormGroup.controls['formulaString'].patchValue(formula);
		}
	}

	/* To add operators +,-,*,/,(,), SPACE**/

	public injectOperatorInToBody(field, formula, fieldSelectFormGroup?, index?) {
		if (!fieldSelectFormGroup) {
			fieldSelectFormGroup = this.fieldSelectFormGroup;
		}
		if (
			(field.DISPLAY_LABEL === '(' || field.DISPLAY_LABEL === '( )') &&
			!formula
		) {
			formula = field.DISPLAY_LABEL;
		} else if (formula && field.DISPLAY_LABEL === 'BLANK SPACE') {
			formula = this.insertValuesByCursorValues(
				'+' + field.VALUE + '+',
				formula,
				index
			);
		} else if (formula) {
			formula = this.insertValuesByCursorValues(
				field.DISPLAY_LABEL,
				formula,
				index
			);
		}
		fieldSelectFormGroup.controls['formulaString'].patchValue(formula);
	}

	/** to insert values as sub string */

	insertValuesByCursorValues(newValue, oldString, index?) {
		var textAreaId = 'myTextArea';
		if (index !== undefined) {
			textAreaId = textAreaId + index;
		}
		if (this.textAreaCursorPositions[textAreaId].isCursorActive) {
			let newString =
				oldString.substring(
					0,
					this.textAreaCursorPositions[textAreaId].startPosition
				) +
				newValue +
				oldString.substring(
					this.textAreaCursorPositions[textAreaId].endPosition,
					oldString.length
				);
			this.textAreaCursorPositions[textAreaId].startPosition += newValue.length;
			this.textAreaCursorPositions[textAreaId].endPosition += newValue.length;
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
			(field) => field.FIELD_ID === this.fieldForm.value.AGGREGATION_FIELD
		);
		const dialogRef = this.dialog.open(ConditionsDialogComponent, {
			width: '800px',
			data: {
				MODULE: relatedField.MODULE,
				CONDITIONS: this.fieldForm.value.CONDITIONS,
				PARENT_COMPONENT: 'fieldCreator',
			},
			disableClose: false,
			maxHeight: '90vh',
		});
		dialogRef.afterClosed().subscribe((result) => {
			if (result !== 'close') {
				const conditions = this.fieldForm.get('CONDITIONS') as FormArray;
				while (conditions.length) {
					conditions.removeAt(0);
				}
				result.forEach((value) => {
					conditions.push(this._formBuilder.control(value));
				});
			}
		});
	}
	public addFormulaToList() {
		const textArea: string = 'myTextArea' + this.listFormulaFormArray.length;
		this.textAreaCursorPositions[textArea] = {
			isCursorActive: false,
			startPosition: 0,
			endPosition: 0,
		};
		this.listFormulaFormArray.push(
			this._formBuilder.group({
				formulaString: ['', Validators.required],
				FORMULA_NAME: ['NEW_FORMULA', Validators.required],
				FORMULA_LABEL: ['New Formula', Validators.required],
			})
		);
		event.preventDefault();
	}

	public updateFormulaName(formulaGroup) {
		formulaGroup.patchValue({
			FORMULA_NAME: formulaGroup.value.FORMULA_LABEL.toUpperCase().replace(
				/\s+/g,
				'_'
			),
		});
	}
}
