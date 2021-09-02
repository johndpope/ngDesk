import { Component, OnInit } from '@angular/core';
import {
	AbstractControl,
	FormBuilder,
	FormGroup,
	Validators,
} from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import * as _moment from 'moment';

import { ScheduleReportsDialogComponent } from 'src/app/dialogs/schedule-reports-dialog/schedule-reports-dialog.component';
import { RolesService } from 'src/app/roles/roles.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { ConditionsService } from '../../custom-components/conditions/conditions.service';
import { ReportsDialogComponent } from '../../dialogs/reports-dialog/reports-dialog.component';
import { Role } from '../../models/role';
import { ModulesService } from '../../modules/modules.service';
import { LoaderService } from './../../custom-components/loader/loader.service';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
import { CompaniesService } from '../../companies/companies.service';
import { Report, ReportApiService } from '@ngdesk/report-api';
import { ReportService } from '../report.service';
import { mergeMap, map } from 'rxjs/operators';
import { MatAccordion } from '@angular/material/expansion';
import {
	animate,
	state,
	style,
	transition,
	trigger,
} from '@angular/animations';

import {
	CdkDragDrop,
	moveItemInArray,
	transferArrayItem,
} from '@angular/cdk/drag-drop';
import { ResizeEvent } from 'angular-resizable-element';
@Component({
	selector: 'app-report-detail',
	templateUrl: './report-detail.component.html',
	styleUrls: ['./report-detail.component.scss'],
	animations: [
		trigger('detailExpand', [
			state('collapsed', style({ height: '0px', minHeight: '0' })),
			state('expanded', style({ height: '*' })),
			transition(
				'expanded <=> collapsed',
				animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')
			),
		]),
	],
})
export class ReportDetailComponent implements OnInit {
	public actioncFunctions = {
		delete: (colIndex, action, col) => {
			this.removeColumn(colIndex, col);
		},
		sort: (colIndex, action, col) => {
			this.sortBy = colIndex;
			this.orderBy = action === 'SORT_ASCENDING' ? 'asc' : 'desc';
			this.postReportData();
		},
		move: (colIndex, action, col) => {
			this.moveColumn(colIndex, action, col);
		},
	};
	public moduleId: string;
	private reportId: string;
	public fieldsInTable = [];
	public relationFieldsInTable: any = {};
	public menuItems = [];
	public reportForm: FormGroup;
	public displayedColumns: string[] = [];
	public displayedColumnsObj = [];
	public dataSource = new MatTableDataSource<any>();
	// public fieldsDataSource = new MatTableDataSource<any>();
	public relationshipFieldsSource = new MatTableDataSource<any>();
	public fields: any;
	private sortBy = 0;
	private orderBy = 'asc';
	private source = [];
	private page = 0;
	private pageSize = 10;
	public totalRecords: number;
	public operators = [];
	public filters = [];
	public roles: Role[];
	// public picklistValues: string[];
	public filterField = {
		FIELD: {},
		OPERATOR: 'equals to',
		VALUE: '',
		REQUIREMENT_TYPE: 'All',
	};
	public allFields: any;
	public allFieldsForFilter: any;
	public editField = null;
	public dialogRef: any;
	private schedules: { CRON: string; EMAILS: string[] };
	public filterRequirementType = 'All';
	public isLoadingTable = false;
	public allModules;
	public fieldsIds: any[] = [];
	public relationFieldIds: any = {};
	public relatedFields = {}; // contains all the related fields
	public parentField: any = {}; // currently clicked aggregation field
	public chaildFields: any[] = []; //displayed  in aggregation field dropdown
	public oneToManyFields: any[] = []; // one to many fields displayed in aggregation section
	public relatedModuleFields: any[] = []; // validated other module fields
	public reportInfo: any = {}; // save API Report info
	// public aggregationData;
	// public expandedElement;
	// public expandedElementIndex;
	// public sortByFieldsList: any[] = [];
	// public selectedColName: any = ' ';
	// public selectedColData: any = {};
	// public aggregationDataSource = new MatTableDataSource<any>();
	// public aggregationTableHeaders: any[] = [];
	// public aggregationColumns = [];
	public childPagination: any = {};
	public childTableSorting: any = {};
	public customisation: any = {};
	public duplicateTableFields = [];

	/** ----  Newly added Fields ------ */

	public dropList: any = [];
	public relationDropList: any = [];
	public fieldsInList: any[] = [];
	public currentIndex = 0;
	public childTableLength = {};

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private dialog: MatDialog,
		private translateService: TranslateService,
		private modulesService: ModulesService,
		private formBuilder: FormBuilder,
		private bannerMessageService: BannerMessageService,
		private conditionsService: ConditionsService,
		private rolesService: RolesService,
		private http: HttpClient,
		private globals: AppGlobals,
		private schedulesDialog: MatDialog,
		private companiesService: CompaniesService,
		private reportApiService: ReportApiService,
		private loaderService: LoaderService,
		private reportService: ReportService
	) {}

	public ngOnInit() {
		this.reportId = this.route.snapshot.params['reportId'];
		this.dropList.push('field1');
		this.relationDropList.push('field1');
		this.menuItems.push(
			{ NAME: 'SORT_ASCENDING', ICON: 'arrow_upward', ACTION: 'sort' },
			{ NAME: 'SORT_DECENDING', ICON: 'arrow_downward', ACTION: 'sort' },
			{ NAME: 'MOVE_RIGHT', ICON: 'arrow_forward', ACTION: 'move' },
			{ NAME: 'MOVE_LEFT', ICON: 'arrow_backward', ACTION: 'move' },
			{ NAME: 'REMOVE_COLUMN', ICON: 'delete', ACTION: 'delete' }
		);
		this.reportForm = this.formBuilder.group({
			NAME: ['', Validators.required],
			DESCRIPTION: [''],
			MODULE: ['', [Validators.required, this.checkModuleExist]],
		});
		if (this.reportId === 'new') {
			setTimeout(() => {
				this.openNewReportDialog();
			}, 200);

			this.modulesService.getModules().subscribe((moduleResponse: any) => {
				this.allModules = moduleResponse.MODULES;
			});
		} else {
			let query = `{
				DATA: getReport(id: "${this.reportId}"){
				    reportId
					reportName
                    reportDescription
                    module
                    fields {
                        fieldId
                        data
                    }
                    filters {
                        FIELD: field {
                           FIELD_ID: fieldId
                           DATA: data
                        }
                        OPERATOR: operator
                        VALUE: value
						REQUIREMENT_TYPE: requirementType
                    }
                    sortBy {
                        fieldId
                        data
                    }
                    type
                    order
                    schedules {
                        CRON: cron
                        EMAILS: emails
                    }
				}
			}`;
			this.isLoadingTable = true;

			this.modulesService
				.getModules()
				.pipe(
					mergeMap((modulesResponse: any) => {
						return this.reportsQuery(query).pipe(
							mergeMap((reportResponse: any) => {
								return this.modulesService
									.getModuleById(reportResponse.DATA.module)
									.pipe(
										map((moduleResponse: any) => {
											return {
												allModules: modulesResponse,
												reportResponse: reportResponse,
												module: moduleResponse,
											};
										})
									);
							})
						);
					})
				)
				.subscribe(
					(reportData) => {
						this.allModules = reportData.allModules.MODULES;
						if (reportData.reportResponse.DATA.filterRequirementType !== null) {
							this.filterRequirementType =
								reportData.reportResponse.DATA.filterRequirementType;
						}
						this.filters = reportData.reportResponse.DATA.filters;

						this.moduleId = reportData?.reportResponse.DATA.module;
						this.reportId = reportData?.reportResponse.DATA.reportId;
						this.reportForm
							.get('NAME')
							.setValue(reportData?.reportResponse.DATA.reportName);
						this.reportForm
							.get('DESCRIPTION')
							.setValue(reportData?.reportResponse.DATA.reportDescription);
						this.reportForm.get('MODULE').setValue(reportData?.module);
						this.orderBy = reportData?.reportResponse.DATA.order;
						this.schedules = reportData?.reportResponse.DATA.schedules;

						this.getModuleFields(reportData?.module.FIELDS);
						this.loadRelationDropdownFields(this.oneToManyFields, reportData);
						this.isLoadingTable = false;
					},
					(error: any) => {
						this.isLoadingTable = false;
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		}

		this.getUserRoles();
	}

	// Every time when we sort or add a column to report we will make this call and get the data
	private postReportData(isSorted?) {
		this.isLoadingTable = true;
		let filters = [];
		filters = this.createConditionsForQuery(filters);
		let sortBy = this.allFields.find(
			(field) => field.FIELD_ID === this.fieldsInTable[this.sortBy]?.fieldId
		);

		if (!sortBy) {
			let relatedTableFields = this.getRelatedFieldsInTable();
			sortBy = this.allFields.find(
				(field) => field.FIELD_ID === relatedTableFields[this.sortBy]?.fieldId
			);
		}
		//to set oreder for entries
		this.setOrderForFieldsInTable();
		let allTableFields = [];
		allTableFields = this.fieldsInTable.concat(this.getRelatedFieldsInTable());
		this.reportService
			.postReportForField(
				this.reportForm.value['MODULE'],
				filters,
				allTableFields,
				this.page,
				this.pageSize,
				this.allModules,
				sortBy?.NAME,
				this.orderBy,
				this.oneToManyFields,
				this.customisation
			)
			.subscribe(
				(reportDataResponse: any) => {
					this.source = [];
					this.displayedColumns = [];
					this.displayedColumnsObj = [];
					this.fieldsInTable = [];
					this.relationFieldsInTable = {};
					this.relatedModuleFields = [];

					let reportInfo = reportDataResponse[0]?.DATA;
					let isParentFiedPresent: boolean = false;
					if (reportInfo) {
						this.reportInfo = reportInfo;
						this.allFields.forEach((field) => {
							if (this.fieldsIds.includes(field.FIELD_ID)) {
								let fieldData: any = {};
								fieldData.data = [];
								fieldData.fieldId = field.FIELD_ID;
								this.fieldsInTable.push(fieldData);
							}
						});

						// fiter relationship Fields
						this.oneToManyFields.forEach((field) => {
							if (this.relatedFields.hasOwnProperty(field.MODULE)) {
								let relatedFieds: any[] = this.relatedFields[field.MODULE];
								relatedFieds.forEach((relationField) => {
									if (
										this.relationFieldIds[field.MODULE] &&
										this.relationFieldIds[field.MODULE].length > 0 &&
										this.relationFieldIds[field.MODULE].includes(
											relationField.FIELD_ID
										)
									) {
										let fieldData: any = {};
										fieldData.data = [];
										(fieldData.DATA = []),
											(fieldData.parentFieldName = field.NAME);
										fieldData.parentModuleId = field.MODULE;
										fieldData.fieldId = relationField.FIELD_ID;
										(fieldData.DATA_TYPE = field.DATA_TYPE),
											(fieldData.NAME = relationField.NAME),
											(fieldData.isRelated = true);
										fieldData.paentFieldId = field.FIELD_ID;
										if (
											this.relationFieldsInTable.hasOwnProperty(field.MODULE)
										) {
											this.relationFieldsInTable[field.MODULE].push(fieldData);
										} else {
											this.relationFieldsInTable[field.MODULE] = [fieldData];
										}
										this.relatedModuleFields.push(relationField);
									}
								});
							}
						});

						this.oneToManyFields.forEach((field) => {
							this.fieldsInTable.forEach((item) => {
								if (item.NAME == field.NAME) {
									isParentFiedPresent = true;
								}
							});

							if (
								!isParentFiedPresent &&
								this.relationFieldIds.hasOwnProperty(field.MODULE) &&
								this.relationFieldIds[field.MODULE].length > 0
							) {
								let fieldData = {
									fieldId: field.FIELD_ID,
									DATA: [],
									DISPLAY_LABEL: field.DISPLAY_LABEL,
									DATA_TYPE: field.DATA_TYPE,
									NAME: field.NAME,
									isParentField: true,
									parentFieldName: field.NAME,
									data: [],
									moduleId: field.MODULE,
								};
								this.fieldsInTable.push(fieldData);
							}
						});

						this.convertIdsToFields();
						this.getTotalRecordsLength();
						// setting data to Fields
						this.changeFormatForTableData(reportInfo);
						this.createDataSource();
						this.addEmptyDataToRows();
						this.loadEmptyTable();
						if (!isSorted) {
							this.setPaginationForChild();
						}

						this.totalRecords = reportDataResponse[1]?.COUNT;
					}
					this.isLoadingTable = false;
				},
				(error: any) => {
					this.isLoadingTable = false;
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	public validateTableFields(FieldID) {
		let isValid;
		let allTableFields = [];
		allTableFields = this.fieldsInTable.concat(this.getRelatedFieldsInTable());
		if (allTableFields.length == 0) {
			isValid = true;
		} else {
			allTableFields.forEach((field) => {
				if (field.fieldId === FieldID) {
					isValid = false;
				} else {
					isValid = true;
				}
			});
		}
		return isValid;
	}
	public onFieldDrop(field, isRelationField?) {
		this.chaildFields = [];
		this.currentIndex = 0;
		if (isRelationField) {
			let isParentFiedPresent: boolean = false;
			let tempField = {
				fieldId: field.FIELD_ID,
				DATA: [],
				DATA_TYPE: field.DATA_TYPE,
				NAME: field.NAME,
				isRelated: isRelationField,
			};
			field = tempField;
			if (this.relationFieldIds.hasOwnProperty(this.parentField.MODULE)) {
				this.relationFieldIds[this.parentField.MODULE].push(field.fieldId);
			} else {
				this.relationFieldIds[this.parentField.MODULE] = [field.fieldId];
			}
			// console.log(field);
			field = this.getParentByChaildId(field);
			if (this.relationFieldsInTable.hasOwnProperty(this.parentField.MODULE)) {
				this.relationFieldsInTable[this.parentField.MODULE].push(field);
			} else {
				this.relationFieldsInTable[this.parentField.MODULE] = [field];
			}

			this.fieldsInTable.forEach((item) => {
				if (item.NAME == this.parentField.NAME) {
					isParentFiedPresent = true;
				}
			});

			if (!isParentFiedPresent) {
				let fieldData = {
					fieldId: this.parentField.FIELD_ID,
					DATA: [],
					DISPLAY_LABEL: this.parentField.DISPLAY_LABEL,
					DATA_TYPE: this.parentField.DATA_TYPE,
					NAME: this.parentField.NAME,
					isParentField: true,
				};

				this.fieldsInTable.push(fieldData);
			}

			this.postReportData();
		} else {
			this.fieldsInList = [];
			this.removeDropHereData();
			let insertedFieldIndex;

			this.fields.forEach((currentField, index) => {
				if (currentField === field) {
					field.fieldId = field.FIELD_ID;
					insertedFieldIndex = index;
					field.DATA = [];
					this.fieldsInTable.push(field);
				}
			});
			this.fieldsIds.push(field.fieldId);
			this.allFields.forEach((field) => {
				let isPresent = false;
				this.fieldsIds.forEach((ids) => {
					if (ids === field.FIELD_ID) {
						isPresent = true;
					}
				});

				if (!isPresent && field.RELATIONSHIP_TYPE !== 'One to Many') {
					this.fieldsInList.push(field);
				}
			});
			this.postReportData();
		}
	}

	private createDataSource() {
		let messageRecords = this.totalRecords;
		if (messageRecords > this.pageSize) {
			messageRecords = this.pageSize;
		}
		const fieldsInTable = this.fieldsInTable.slice();
		fieldsInTable.forEach((field, fIndex) => {
			let flag = true;
			field.data.forEach((data, dIndex) => {
				if (fIndex === 0) {
					if (field.DATA_TYPE.DISPLAY === 'Discussion') {
						if (flag) {
							for (let i = 0; i < messageRecords; i++) {
								this.source.push({ [field.NAME]: 'Download To View' });
							}
							flag = false;
						} else if (
							field?.DATA_TYPE?.DISPLAY === 'Relationship' &&
							field.RELATIONSHIP_TYPE == 'One to Many'
						) {
							this.source[dIndex][field.NAME] = data;
						}
					} else {
						this.source.push({ [field.NAME]: data });
					}
				} else {
					if (field?.DATA_TYPE?.DISPLAY === 'Discussion') {
						if (flag) {
							for (let i = 0; i < this.source.length; i++) {
								this.source[i][field.NAME] = 'Download To View';
							}
							flag = false;
						}
					} else if (
						field?.DATA_TYPE?.DISPLAY === 'Relationship' &&
						field.RELATIONSHIP_TYPE == 'One to Many'
					) {
						this.source[dIndex][field.NAME] = data;
					} else {
						if (this.source[dIndex]) {
							this.source[dIndex][field.NAME] = data;
						}
					}
				}
			});
		});
		this.dataSource = new MatTableDataSource<any>(this.source);
	}

	public pageChanged(event) {
		this.page = event.pageIndex;
		this.pageSize = event.pageSize;
		this.postReportData();
	}
	public generateReport() {
		let relatedIdsList = [];
		let fieldNames = [];
		this.addDataToField();
		let filters = [];
		this.filters.forEach((filter) => {
			let newFilter = {
				condition: filter.FIELD.FIELD_ID,
				operator: filter.OPERATOR,
				conditionValue: filter.VALUE,
				requirementType: filter.REQUIREMENT_TYPE,
			};
			filters.push(newFilter);
		});

		if (this.oneToManyFields.length > 0) {
			this.oneToManyFields.forEach((item) => {
				if (
					this.relationFieldIds.hasOwnProperty(item.MODULE) &&
					this.relationFieldIds[item.MODULE].length > 0
				) {
					relatedIdsList = relatedIdsList.concat(
						this.relationFieldIds[item.MODULE]
					);
				}
			});
			this.fieldsIds = this.fieldsIds.concat(relatedIdsList);
		}

		this.fieldsIds.forEach((Id) => {
			if (this.getFieldNamesByIDs(Id)) {
				fieldNames.push(this.getFieldNamesByIDs(Id));
			}
		});
		let allTableFields = [];
		allTableFields = this.fieldsInTable.concat(this.getRelatedFieldsInTable());
		allTableFields = allTableFields.filter((item) => !item.isParentField);
		if (allTableFields && allTableFields.length > 0) {
			this.reportService.generateCSVForReport(
				this.reportForm.value['MODULE'],
				filters,
				allTableFields,
				this.allModules,
				this.reportForm.value['NAME'],
				this.oneToManyFields,
				fieldNames,
				this.customisation
			);

			setTimeout(() => {
				this.bannerMessageService.successNotifications.push({
					message: 'The report link will be sent to registered Email Address',
				});
			}, 1000);
		}
	}

	private getModuleFields(fields) {
		let oneToManyFields = [];
		this.fields = fields.filter(
			(field) => field.DATA_TYPE.DISPLAY !== 'PDF' && field.NAME !== 'DATA_ID'
		);
		this.cleanFields();
		this.sortFieldsAlphabetically();
		this.allFields.forEach((field) => {
			if (field.RELATIONSHIP_TYPE == 'One to Many') {
				oneToManyFields.push(field);
			}
		});
		this.oneToManyFields = oneToManyFields;
		this.relationshipFieldsSource = new MatTableDataSource<any>(
			this.oneToManyFields
		);
	}

	private removeColumn(colIndex, col) {
		this.fieldsInList = [];
		if (this.fieldsInTable?.length > 0) {
			this.duplicateTableFields = this.fieldsInTable.slice(0);
			// check if field is in table
			this.fieldsInTable.map((field, index) => {
				if (field.NAME === col.NAME) {
					if (field.isParentField) {
						this.relationFieldsInTable[field.moduleId] = [];
						this.relationFieldIds[field.moduleId] = [];
						this.displayedColumnsObj.splice(colIndex, 1);
						this.displayedColumns.splice(colIndex, 1);
					} else {
						this.fieldsIds.splice(index, 1);
						this.displayedColumnsObj.splice(colIndex, 1);
						this.displayedColumns.splice(colIndex, 1);
						this.fieldsInTable.splice(index, 1);
					}
					this.source.forEach((record) => {
						delete record[col.NAME];
					});
					this.dataSource = new MatTableDataSource<any>(this.source);
					this.duplicateTableFields.splice(index, 1);

					this.sortFieldsAlphabetically();

					this.allFields.forEach((field) => {
						let isPresent = false;
						this.fieldsIds.forEach((ids) => {
							if (ids === field.FIELD_ID) {
								isPresent = true;
							}
						});
						if (!isPresent && field?.RELATIONSHIP_TYPE !== 'One to Many') {
							this.fieldsInList.push(field);
						}
					});
				}
			});
		} else {
			if (
				this.relationFieldsInTable.hasOwnProperty(col.parentModuleId) &&
				this.relationFieldsInTable[col.parentModuleId].length > 0
			) {
				this.relationFieldsInTable[col.parentModuleId] = [];
			}
		}

		this.fieldsInTable = this.duplicateTableFields;
	}

	private addEmptyDataToRows() {
		this.fieldsInTable.forEach((field) => {
			this.source.forEach((entry) => {
				if (!entry[field.NAME]) {
					entry[field.NAME] = 'No Data';
				}
			});
		});
	}

	private addDataToField() {
		this.fieldsInTable.forEach((field) => {
			field.DATA = [];
			this.source.forEach((record) => {
				field.DATA.push(record[field.NAME]);
			});
		});
	}

	public saveReport() {
		let allTableFields = [];
		let tableFields = this.fieldsInTable.filter(
			(field) => !field.isParentField
		);
		this.fieldsInTable = tableFields;

		allTableFields = this.fieldsInTable.concat(
			this.removeParentId(this.getRelatedFieldsInTable())
		);
		if (allTableFields.length > 0) {
			this.addDataToField();
			let filters = [];
			let validTimeWindowConditions = true;
			this.filters.forEach((filter) => {
				if (filter.FIELD.NAME === 'TIME_WINDOW') {
					let value = new String();
					value = filter.VALUE;
					const period = value.substring(0, value.indexOf('('));
					const firstOperand = value.substring(
						value.indexOf('(') + 1,
						value.indexOf('-')
					);
					const secondOperand = value.substring(
						value.indexOf('-') + 1,
						value.indexOf(')')
					);
					if (period !== 'days' && period !== 'months') {
						this.bannerMessageService.errorNotifications.push({
							message: 'Invalid period, please type days | months',
						});
						validTimeWindowConditions = false;
					} else if (firstOperand !== 'current_date') {
						this.bannerMessageService.errorNotifications.push({
							message:
								'Invalid operand on the time window, please type current_date',
						});
						validTimeWindowConditions = false;
					} else if (isNaN(parseInt(secondOperand))) {
						this.bannerMessageService.errorNotifications.push({
							message:
								'Invalid operand please enter a number instead of ' +
								secondOperand,
						});
						validTimeWindowConditions = false;
					}
				}

				let newFilter = {
					field: {
						dataType: {
							display: filter.FIELD.DATA_TYPE.DISPLAY,
							backend: filter.FIELD.DATA_TYPE.BACKEND,
						},
						displayLabel: filter.FIELD.DISPLAY_LABEL,
						fieldId: filter.FIELD.FIELD_ID,
						name: filter.FIELD.NAME,
					},
					operator: filter.OPERATOR,
					value: filter.VALUE,
					requirementType: filter.REQUIREMENT_TYPE,
				};
				filters.push(newFilter);
			});
			if (validTimeWindowConditions) {
				let report: Report = {
					reportId: this.reportId,
					reportName: this.reportForm.value['NAME'],
					reportDescription: this.reportForm.value['DESCRIPTION'],
					type: 'list',
					fields: allTableFields,
					filters: filters,
					module: this.reportForm.value['MODULE'].MODULE_ID,
					sortBy: allTableFields[this.sortBy],
					order: this.orderBy,
				};
				report = this.setDataOfReportToEmpty(report);
				if (this.schedules) {
					report.schedules = {
						cron: this.schedules.CRON,
						emails: this.schedules.EMAILS,
					};
				}

				if (!this.reportForm.controls.NAME.hasError('required')) {
					if (this.route.snapshot.params['reportId'] === 'new') {
						this.reportApiService.postReport(report).subscribe(
							(postReportReponse: any) => {
								this.companiesService.trackEvent(`Created Report`, {
									REPORT_ID: postReportReponse.REPORT_ID,
									MODULE_ID: this.moduleId,
								});
								this.router.navigate([`reports`]);
							},
							(error: any) => {
								this.bannerMessageService.errorNotifications.push({
									message: error.error.ERROR,
								});
							}
						);
					} else {
						report.reportId = this.reportId;
						this.reportApiService.putReport(report).subscribe(
							(postReportReponse: any) => {
								this.companiesService.trackEvent(`Updated Report`, {
									REPORT_ID: postReportReponse.REPORT_ID,
									MODULE_ID: this.moduleId,
								});
								this.router.navigate([`reports`]);
							},
							(error: any) => {
								this.bannerMessageService.errorNotifications.push({
									message: error.error.ERROR,
								});
							}
						);
					}
				}
			} else {
				this.loaderService.isLoading = false;
			}
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant(
					'ATLEAST_ONE_FIELD_HAS_TO_BE_ADDED'
				),
			});
		}
	}

	public removeParentId(relatedFields) {
		let relatedFieldsInTable = relatedFields;
		relatedFieldsInTable.forEach((field) => {
			field.fieldId = field.paentFieldId + '.' + field.fieldId;
			delete field.paentFieldId;
			delete field.parentFieldName;
			delete field.parentModuleId;
		});
		return relatedFieldsInTable;
	}

	private moveColumn(colIndex, action, col) {
		if (action === 'MOVE_LEFT') {
			let temp = this.displayedColumns[colIndex - 1];
			this.displayedColumns[colIndex - 1] = this.displayedColumns[colIndex];
			this.displayedColumns[colIndex] = temp;
			temp = this.displayedColumnsObj[colIndex - 1];
			this.displayedColumnsObj[colIndex - 1] =
				this.displayedColumnsObj[colIndex];
			this.displayedColumnsObj[colIndex] = temp;

			temp = this.fieldsInTable[colIndex - 1];
			this.fieldsInTable[colIndex - 1] = this.fieldsInTable[colIndex];
			this.fieldsInTable[colIndex] = temp;
		} else {
			let temp = this.displayedColumns[colIndex + 1];
			this.displayedColumns[colIndex + 1] = this.displayedColumns[colIndex];
			this.displayedColumns[colIndex] = temp;
			temp = this.displayedColumnsObj[colIndex + 1];
			this.displayedColumnsObj[colIndex + 1] =
				this.displayedColumnsObj[colIndex];
			this.displayedColumnsObj[colIndex] = temp;

			temp = this.fieldsInTable[colIndex + 1];
			this.fieldsInTable[colIndex + 1] = this.fieldsInTable[colIndex];
			this.fieldsInTable[colIndex] = temp;
		}
	}

	private openNewReportDialog() {
		const dialogRef = this.dialog.open(ReportsDialogComponent, {
			disableClose: true,
			width: '500px',
			height: '400px',
			data: {
				reportForm: this.reportForm,
				dialogTitle: this.translateService.instant('NEW_REPORT'),
				buttonText: this.translateService.instant('NEXT'),
				closeDialog: this.translateService.instant('CANCEL'),
				executebuttonColor: 'warn',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('CANCEL')) {
				this.router.navigate([`reports`]);
			} else {
				this.moduleId = result.reportForm.value.MODULE.MODULE_ID;
				this.getModuleFields(result.reportForm.value.MODULE.FIELDS);
				this.loadRelationDropdownFields(this.oneToManyFields);
				this.getTableFieldbyID();
				this.loadEmptyTable();
			}
		});
	}

	private cleanFields() {
		// remove below fields
		for (let i = this.fields.length - 1; i >= 0; i--) {
			if (
				this.fields[i].RELATIONSHIP_TYPE === 'Many to Many' ||
				// this.fields[i].RELATIONSHIP_TYPE === 'One to Many' ||
				this.fields[i].DATA_TYPE.DISPLAY === 'File Upload' ||
				this.fields[i].DATA_TYPE.DISPLAY === 'List Text' ||
				this.fields[i].DATA_TYPE.DISPLAY === 'Button' ||
				this.fields[i].DATA_TYPE.DISPLAY === 'File Preview' ||
				this.fields[i].DATA_TYPE.DISPLAY === 'Zoom' ||
				this.fields[i].DATA_TYPE.DISPLAY === 'Image' ||
				this.fields[i].DATA_TYPE.DISPLAY === 'Approval'
			) {
				this.fields.splice(i, 1);
			}
		}
		this.allFields = this.fields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.NAME !== 'TIME_WINDOW' &&
				field.NAME !== 'COPY_BILLING_TO_SHIPPING'
		);

		this.fieldsInList = this.allFields;

		this.allFieldsForFilter = this.fields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY !== 'Discussion' &&
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
				// field.NAME !== 'TIME_WINDOW' &&
				field.NAME !== 'COPY_BILLING_TO_SHIPPING' &&
				field.RELATIONSHIP_TYPE !== 'One to One' &&
				field.RELATIONSHIP_TYPE !== 'One to Many' &&
				field.NAME !== 'COPY_BILLING_TO_SHIPPING'
		);
	}

	private sortFieldsAlphabetically() {
		this.fields.sort(function (a, b) {
			const textA = a.NAME.toUpperCase();
			const textB = b.NAME.toUpperCase();
			return textA < textB ? -1 : textA > textB ? 1 : 0;
		});

		this.allFields.sort(function (a, b) {
			const textA = a.NAME.toUpperCase();
			const textB = b.NAME.toUpperCase();
			return textA < textB ? -1 : textA > textB ? 1 : 0;
		});
	}

	public checkModuleExist(control: AbstractControl) {
		if (!control.value.hasOwnProperty('MODULE_ID')) {
			return { moduleError: true };
		} else {
			return null;
		}
	}

	public getOperators(field) {
		if (field?.parentDataType?.DISPLAY == 'Relationship') {
			this.menuItems = [
				{ NAME: 'MOVE_RIGHT', ICON: 'arrow_forward', ACTION: 'move' },
				{ NAME: 'MOVE_LEFT', ICON: 'arrow_backward', ACTION: 'move' },
				{ NAME: 'REMOVE_COLUMN', ICON: 'delete', ACTION: 'delete' },
			];
		} else {
			this.menuItems = [
				{ NAME: 'SORT_ASCENDING', ICON: 'arrow_upward', ACTION: 'sort' },
				{ NAME: 'SORT_DECENDING', ICON: 'arrow_downward', ACTION: 'sort' },
				{ NAME: 'MOVE_RIGHT', ICON: 'arrow_forward', ACTION: 'move' },
				{ NAME: 'MOVE_LEFT', ICON: 'arrow_backward', ACTION: 'move' },
				{ NAME: 'REMOVE_COLUMN', ICON: 'delete', ACTION: 'delete' },
			];
		}
		this.operators = this.conditionsService.setOperators(field);
	}

	public applyFilter() {
		const filterField = {
			FIELD: {
				DATA: this.filterField.FIELD['DATA'],
				DATA_TYPE: this.filterField.FIELD['DATA_TYPE'],
				DISPLAY_LABEL: this.filterField.FIELD['DISPLAY_LABEL'],
				FIELD_ID: this.filterField.FIELD['FIELD_ID'],
				NAME: this.filterField.FIELD['NAME'],
				PICKLIST_VALUES: this.filterField.FIELD['PICKLIST_VALUES'],
			},
			OPERATOR: this.filterField.OPERATOR,
			VALUE: this.filterField.VALUE,
			REQUIREMENT_TYPE: this.filterField.REQUIREMENT_TYPE,
		};
		if (this.editField === null) {
			this.filters.push(filterField);
		} else {
			this.filters.forEach((fField, fIndex) => {
				if (fField.FIELD.NAME === filterField.FIELD['NAME']) {
					this.filters[fIndex] = filterField;
				}
			});
		}
		this.postReportData();
		this.resetFilterField();
	}

	public removeFilter(index) {
		this.filters.splice(index, 1);
		this.postReportData();
	}

	public filterFieldSelection(field) {
		this.filterField = {
			FIELD: field,
			OPERATOR: 'equals to',
			VALUE: '',
			REQUIREMENT_TYPE: 'All',
		};
		this.editField = null;
		// if (field.DATA_TYPE.DISPLAY === 'Picklist') {
		// 	this.picklistValues = field.PICKLIST_VALUES;
		// }
		if (field.RELATIONSHIP_TYPE === 'Many to One') {
			this.getRelationshipValues(field);
		}
		this.getOperators(field);
	}

	public editFilter(index) {
		this.editField = index;
		this.filterField = Object.assign({}, this.filters[index]);
		this.getOperators(this.filterField.FIELD);
		this.filterField.FIELD = this.allFieldsForFilter.find(
			(field) => field.FIELD_ID === this.filterField.FIELD['FIELD_ID']
		);
	}

	public resetFilterField() {
		this.filterField = {
			FIELD: {},
			OPERATOR: 'equals to',
			VALUE: '',
			REQUIREMENT_TYPE: 'All',
		};
		this.editField = null;
	}

	private convertIdsToFields() {
		this.fieldsInTable.forEach((field, fIndex) => {
			if (
				field.parentFieldName &&
				field.parentModuleId &&
				this.relatedFields.hasOwnProperty(field.parentModuleId)
			) {
				this.relatedFields[field.parentModuleId].forEach((afld, afldIndex) => {
					if (field.fieldId === afld.FIELD_ID) {
						this.fieldsInTable[fIndex].NAME =
							this.relatedFields[field.parentModuleId][afldIndex].NAME;
						this.fieldsInTable[fIndex].DATA_TYPE =
							this.relatedFields[field.parentModuleId][afldIndex].DATA_TYPE;
						this.fieldsInTable[fIndex].DISPLAY_LABEL =
							this.relatedFields[field.parentModuleId][afldIndex].DISPLAY_LABEL;
					}
				});
			} else {
				this.allFields.forEach((afld, afldIndex) => {
					if (field.fieldId === afld.FIELD_ID) {
						this.fieldsInTable[fIndex].NAME = this.allFields[afldIndex].NAME;
						this.fieldsInTable[fIndex].DATA_TYPE =
							this.allFields[afldIndex].DATA_TYPE;
						this.fieldsInTable[fIndex].DISPLAY_LABEL =
							this.allFields[afldIndex].DISPLAY_LABEL;
						if (!this.displayedColumns.includes(field.DISPLAY_LABEL)) {
							this.displayedColumns.push(field.DISPLAY_LABEL);
							if (field.isParentField) {
								this.displayedColumnsObj.push({
									NAME: field.NAME,
									DISPLAY: field.DISPLAY_LABEL,
									DATA_TYPE: field.DATA_TYPE,
									parentDataType: field.DATA_TYPE,
									parentModuleId: field.moduleId,
									isParentField: true,
									parentFieldId: field.fieldId,
								});
							} else {
								this.displayedColumnsObj.push({
									NAME: field.NAME,
									DISPLAY: field.DISPLAY_LABEL,
									DATA_TYPE: field.DATA_TYPE,
								});
							}
						}
					}
				});
			}
		});

		this.filters.forEach((field, fIndex) => {
			this.allFieldsForFilter.forEach((afld, afldIndex) => {
				if (field.FIELD.FIELD_ID === afld.FIELD_ID) {
					this.filters[fIndex].FIELD.NAME = afld.NAME;
					this.filters[fIndex].FIELD.DATA_TYPE = afld.DATA_TYPE;
					this.filters[fIndex].FIELD.DISPLAY_LABEL = afld.DISPLAY_LABEL;
					this.filters[fIndex].FIELD.RELATIONSHIP_TYPE = afld.RELATIONSHIP_TYPE;
					this.filters[fIndex].FIELD.MODULE = afld.MODULE;
					this.filters[fIndex].FIELD.PRIMARY_DISPLAY_FIELD =
						afld.PRIMARY_DISPLAY_FIELD;
					this.filters[fIndex].FIELD.PICKLIST_VALUES = afld.PICKLIST_VALUES;
				}
			});
			if (this.filters[fIndex].FIELD.RELATIONSHIP_TYPE === 'Many to One') {
				this.getRelationshipValues(this.filters[fIndex].FIELD);
			}
		});
	}

	private getRelationshipValues(field) {
		this.modulesService.getModuleById(field.MODULE).subscribe(
			(relationModule: any) => {
				field['RELATION_FIELD_NAME'] = relationModule.FIELDS.find(
					(tempField) => tempField.FIELD_ID === field.PRIMARY_DISPLAY_FIELD
				).NAME;
				this.modulesService.getEntries(field.MODULE).subscribe(
					(entriesResponse: any) => {
						entriesResponse.DATA.push({
							[field['RELATION_FIELD_NAME']]: '{{CURRENT_USER}}',
							DATA_ID: '{{CURRENT_USER}}',
						});
						field['RELATION_FIELD_VALUE'] = entriesResponse.DATA;
					},
					(error) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			},
			(error) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public scheduleReport(): void {
		this.dialogRef = this.schedulesDialog.open(ScheduleReportsDialogComponent, {
			width: '650px',
			disableClose: true,
			autoFocus: true,
			data: {
				CRON: this.schedules ? this.schedules.CRON : '',
				EMAILS: this.schedules ? this.schedules.EMAILS : [],
			},
		});

		this.dialogRef.afterClosed().subscribe((result: any) => {
			if (result !== undefined) {
				if (result.EMAILS.length === 0) {
					this.schedules = null;
				} else {
					this.schedules = result;
				}
			}
		});
	}

	public reportsQuery(query: string) {
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public setDataOfReportToEmpty(report: Report) {
		report.fields.forEach((field) => {
			field.data = [];
		});

		if (report?.sortBy?.data) {
			report.sortBy.data = [];
		} else {
			report.sortBy.data = [];
		}

		return report;
	}

	setOrderForFieldsInTable() {
		let entryFields = [];
		this.fieldsInTable.forEach((field) => {
			if (!field.paentFieldId) {
				this.fieldsIds.forEach((Id) => {
					if (Id === field.fieldId) {
						entryFields.push(field);
					}
				});
			} else {
				this.relationFieldIds[field.parentModuleId].forEach((Id) => {
					if (Id === field.fieldId) {
						entryFields.push(field);
					}
				});
			}
		});
		this.fieldsInTable = entryFields;
	}

	public createConditionsForQuery(filters) {
		this.filters.forEach((filter) => {
			let newFilter = {
				condition: filter.FIELD.FIELD_ID,
				operator: filter.OPERATOR,
				conditionValue: filter.VALUE,
				requirementType: filter.REQUIREMENT_TYPE,
			};
			filters.push(newFilter);
		});
		return filters;
	}

	public getUserRoles() {
		this.rolesService.getRoles().subscribe(
			(rolesResponse: any) => {
				this.roles = rolesResponse.ROLES;
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public setRoleName(field, data) {
		const roleId = data[field.NAME];
		let role;
		if (roleId) {
			role = this.roles.find((allRole) => {
				return allRole.ROLE_ID === roleId;
			});
		}
		return role;
	}

	public setPhoneNumber(field, data) {
		let phoneNumber = null;
		if (
			data &&
			data[field.NAME] &&
			data[field.NAME].DIAL_CODE &&
			data[field.NAME].PHONE_NUMBER
		) {
			phoneNumber =
				data[field.NAME].DIAL_CODE + ' ' + data[field.NAME].PHONE_NUMBER;
		}
		return phoneNumber;
	}

	public loadRelationDropdownFields(relationshipFields, reportData?) {
		let apiCallCount: number = 0;
		for (const relationshipField of relationshipFields) {
			if (!this.relatedFields.hasOwnProperty(relationshipField.MODULE)) {
				this.modulesService.getFields(relationshipField.MODULE).subscribe(
					(relatedFieldsResponse: any) => {
						apiCallCount = apiCallCount + 1;
						this.relatedFields[relationshipField.MODULE] = [];
						relatedFieldsResponse.FIELDS.forEach((field) => {
							if (this.validateFieldForRelationship(field)) {
								this.relatedFields[relationshipField.MODULE].push(field);
							}
						});
						if (apiCallCount === relationshipFields.length && reportData) {
							this.getTableFieldbyID(reportData);
						}
					},
					(relatedFieldsError: any) => {
						apiCallCount = apiCallCount + 1;
						if (apiCallCount === relationshipFields.length && reportData) {
							this.getTableFieldbyID(reportData);
						}
						console.log(relatedFieldsError);
					}
				);
			}
		}
		if (relationshipFields.length == 0) {
			this.getTableFieldbyID(reportData);
		}
	}

	public getTableFieldbyID(reportData?) {
		this.fieldsInTable = [];
		this.fieldsIds = [];
		this.relationFieldIds = {};
		reportData?.reportResponse.DATA.fields.forEach((field: any, index) => {
			let fieldId: string = field?.fieldId;
			if (fieldId.includes('.')) {
				let ids = fieldId.split('.');
				if (ids.length > 1) {
					field.fieldId = ids[1];
					field.isRelated = true;
				}
				if (this.oneToManyFields && this.oneToManyFields.length > 0) {
					this.oneToManyFields.forEach((relatedField) => {
						if (ids[0] === relatedField.FIELD_ID) {
							if (this.relationFieldIds.hasOwnProperty(relatedField.MODULE)) {
								this.relationFieldIds[relatedField.MODULE].push(ids[1]);
							} else {
								this.relationFieldIds[relatedField.MODULE] = [ids[1]];
							}
							if (
								this.relationFieldsInTable.hasOwnProperty(relatedField.MODULE)
							) {
								this.relationFieldsInTable[relatedField.MODULE].push(
									this.getParentByChaildId(field)
								);
							} else {
								this.relationFieldsInTable[relatedField.MODULE] = [
									this.getParentByChaildId(field),
								];
							}
						}
					});
				}
			} else {
				let currentField = this.allFields.find(
					(element) => element.FIELD_ID == field.fieldId
				);
				if (currentField) {
					field.DATA_TYPE = currentField.DATA_TYPE;
					field.NAME = currentField.NAME;
					field.DISPLAY_LABEL = currentField.DISPLAY_LABEL;

					this.fieldsInTable.push(field);
				}
				this.fieldsIds.push(field.fieldId);
			}
			if (field.fieldId === reportData?.reportResponse.DATA.sortBy.fieldId) {
				this.sortBy = index;
			}
		});

		this.setTableFields();
		this.removeFieldsFromList();
		if (this.fieldsInTable.length > 0 || this.relationFieldIds.length > 0) {
			this.postReportData();
		}
	}

	public setTableFields() {
		let fieldsDataSource = [];
		this.allFields.forEach((field) => {
			let isPresent = false;
			this.fieldsIds.forEach((id) => {
				if (id === field.FIELD_ID) {
					isPresent = true;
				}
			});
			if (!isPresent) {
				if (field.RELATIONSHIP_TYPE !== 'One to Many') {
					fieldsDataSource.push(field);
				}
			}
		});
		// this.fieldsDataSource = new MatTableDataSource<any>(fieldsDataSource);
		this.fieldsInList = fieldsDataSource;
	}

	public removeFieldsFromList() {
		//remove fields if used in table
		for (let i = this.fields.length - 1; i >= 0; i--) {
			// remove field from available columns area if field is in use by table.
			if (this.fieldsInTable.length > 0) {
				for (let j = 0; j < this.fieldsInTable.length; j++) {
					if (this.fields[i].FIELD_ID === this.fieldsInTable[j].FIELD_ID) {
						this.fields.splice(i, 1);
						break;
					}
				}
			}
		}
	}

	public validateFieldForRelationship(field) {
		if (
			field.DATA_TYPE.DISPLAY === 'Number' ||
			field.DATA_TYPE.DISPLAY === 'Formula' ||
			field.DATA_TYPE.DISPLAY === 'Aggregate' ||
			field.DATA_TYPE.DISPLAY === 'Date/Time' ||
			(field.DATA_TYPE.DISPLAY === 'Text' && field.NAME !== 'CHANNEL') ||
			field.DATA_TYPE.DISPLAY === 'Text Area'
		) {
			return true;
		} else {
			return false;
		}
	}

	// On click of Aggregation section  field

	public onRelationFieldClick(relationshipField) {
		this.parentField = relationshipField;
		this.chaildFields = [];
		if (this.relatedFields.hasOwnProperty(relationshipField.MODULE)) {
			this.chaildFields = this.relatedFields[relationshipField.MODULE];
		}
	}

	// to set Data to DATA:[] for field inthe table
	// Aggregation data is not setting into the table

	public changeFormatForTableData(reportInfo) {
		this.fieldsInTable.forEach((field) => {
			reportInfo?.forEach((data) => {
				if (
					data.hasOwnProperty(field.NAME) &&
					!field.parentFieldName &&
					!field.isParentField
				) {
					if (field.DATA_TYPE.DISPLAY === 'Relationship') {
						let relationValue = data[field.NAME];
						if (relationValue) {
							relationValue = relationValue?.PRIMARY_DISPLAY_FIELD;
						}
						field.data.push(relationValue);
					} else if (field.NAME === 'ROLE') {
						const role = this.setRoleName(field, data);
						if (role) {
							field.data.push(role.NAME);
						}
					} else if (field.DATA_TYPE.DISPLAY === 'Phone') {
						const phone = this.setPhoneNumber(field, data);
						field.data.push(phone);
					} else {
						field.data.push(data[field.NAME]);
					}
				} else if (field.isParentField) {
					if (
						data[field.parentFieldName] &&
						data[field.parentFieldName].length > 0
					) {
						field.data.push(data[field.parentFieldName]);
					} else if (
						data[field.parentFieldName] &&
						data[field.parentFieldName].length == 0
					) {
						field.data = [];
					} else if (
						data.hasOwnProperty(field.parentFieldName) &&
						data[field.parentFieldName] == null
					) {
						field.data = [];
					}
				}
			});
		});
	}
	// to get parent field (Relationship)

	public getParentByChaildId(field) {
		this.oneToManyFields.forEach((relField) => {
			let fieldsList = this.relatedFields[relField.MODULE];
			if (fieldsList && fieldsList.length > 0) {
				fieldsList.forEach((item) => {
					if (field.fieldId === item.FIELD_ID) {
						if (
							this.relationFieldIds[relField.MODULE] &&
							this.relationFieldIds[relField.MODULE].includes(field.fieldId) &&
							field.isRelated == true
						) {
							field.paentFieldId = relField.FIELD_ID;
							field.parentFieldName = relField.NAME;
							field.parentModuleId = relField.MODULE;
							field.DATA_TYPE = item.DATA_TYPE;
							field.NAME = item.NAME;
							field.DISPLAY_LABEL = relField.DISPLAY_LABEL;
							return;
						}
					}
				});
			}
		});
		return field;
	}
	// To get Field Dissplay lable from NAME

	public getFieldName(raw_field) {
		let field_name = raw_field;

		this.oneToManyFields.forEach((field) => {
			this.relatedFields[field.MODULE]?.find((item) => {
				if (item.NAME === raw_field) {
					field_name = item.DISPLAY_LABEL;
					return;
				}
			});
		});

		if (raw_field === field_name) {
			this.oneToManyFields.forEach((field) => {
				if (field_name === field.NAME) {
					field_name = field.DISPLAY_LABEL;
				}
			});
		}

		return field_name;
	}

	// to Create Parent.Chaild Names For Generate API call
	public getFieldNamesByIDs(fieldId: string) {
		let chaildName;
		let parentName;
		this.fields.find((field) => {
			if (field.FIELD_ID === fieldId) {
				chaildName = field.NAME;
				return;
			}
		});

		if (!chaildName) {
			this.oneToManyFields.forEach((field) => {
				this.relatedFields[field.MODULE]?.forEach((relField) => {
					if (relField.FIELD_ID === fieldId) {
						parentName = field.NAME;
						chaildName = relField.NAME;
					}
				});
			});
		}

		if (chaildName && !parentName) {
			return chaildName;
		} else if (chaildName && parentName) {
			return parentName + '.' + chaildName;
		}
	}

	public onPageChange(event, rowIndex, colIndex, colName, moduleId) {
		if (this.childPagination['table_' + rowIndex + '_' + colIndex]) {
			this.childPagination['table_' + rowIndex + '_' + colIndex].pageSize =
				event.pageSize;
			this.childPagination['table_' + rowIndex + '_' + colIndex].pageIndex =
				event.pageIndex;

			if (
				this.childTableSorting['table_' + rowIndex + '_' + colIndex].active ==
				''
			) {
				this.childTableSorting['table_' + rowIndex + '_' + colIndex]['active'] =
					this.relationFieldsInTable[moduleId][0].NAME;
			}
			this.customisation = {
				customizeFor: colName,
				sortBy:
					this.childTableSorting['table_' + rowIndex + '_' + colIndex].active,
				orederBy:
					this.childTableSorting['table_' + rowIndex + '_' + colIndex]
						.direction,
				pageSize:
					this.childPagination['table_' + rowIndex + '_' + colIndex].pageSize,
				pageIndex: (this.childPagination[
					'table_' + rowIndex + '_' + colIndex
				].pageIndex = event.pageIndex),
			};
			this.postReportData(true);
		}
	}

	public sortData(event, rowIndex, colIndex, colName, moduleId) {
		if (event.direction == '') {
			this.childTableSorting['table_' + rowIndex + '_' + colIndex].direction =
				this.childTableSorting['table_' + rowIndex + '_' + colIndex]
					.direction === 'asc'
					? 'dsc'
					: 'asc';
		} else {
			this.childTableSorting['table_' + rowIndex + '_' + colIndex].direction =
				event.direction;
		}

		if (event.active == '') {
			this.childTableSorting['table_' + rowIndex + '_' + colIndex]['active'] =
				this.relationFieldsInTable[moduleId][0].NAME;
		} else {
			this.childTableSorting['table_' + rowIndex + '_' + colIndex].active =
				event.active;
		}

		this.customisation = {
			customizeFor: colName,
			sortBy:
				this.childTableSorting['table_' + rowIndex + '_' + colIndex].active,
			orederBy:
				this.childTableSorting['table_' + rowIndex + '_' + colIndex].direction,
			pageSize:
				this.childPagination['table_' + rowIndex + '_' + colIndex].pageSize,
			pageIndex:
				this.childPagination['table_' + rowIndex + '_' + colIndex].pageIndex,
		};
		this.postReportData(true);
	}

	public getReatedFieldsOfAllmodules(moduleId?) {
		let fieldsSet = [];
		if (moduleId) {
			if (
				this.relatedFields.hasOwnProperty(moduleId) &&
				this.relatedFields[moduleId] > 0
			) {
				fieldsSet = this.relatedFields[moduleId];
			}
		} else {
			this.oneToManyFields.forEach((field) => {
				if (this.relatedFields.hasOwnProperty(field.MODULE)) {
					this.relatedFields[field.MODULE].forEach((element) => {
						fieldsSet.push(element);
					});
				}
			});
		}

		return fieldsSet;
	}

	public getRelatedFieldsInTable(moduleId?) {
		let fieldsSet = [];
		if (moduleId) {
			if (
				this.relationFieldsInTable.hasOwnProperty(moduleId) &&
				this.relationFieldsInTable[moduleId] > 0
			) {
				fieldsSet = this.relationFieldsInTable[moduleId];
			}
		} else {
			this.oneToManyFields.forEach((field) => {
				if (this.relationFieldsInTable.hasOwnProperty(field.MODULE)) {
					this.relationFieldsInTable[field.MODULE].forEach((element) => {
						//  element.paentFieldId = field.FIELD_ID
						fieldsSet.push(element);
					});
				}
			});
		}

		return fieldsSet;
	}

	public loadEmptyTable() {
		let field1 = {
			NAME: 'Drop_Here',
			DISPLAY_LABLE: 'Drop Here',
			DATA_TYPE: { DISPLAY: 'DROP_HERE' },
			DISPLAY: 'Drop Here',
		};
		this.displayedColumnsObj.push(field1);
		this.displayedColumns.push('Drop Here');
		this.dataSource = new MatTableDataSource<any>(this.source);
	}

	public dropField(event: CdkDragDrop<string[]>) {
		let currentField: any = this.getFieldByFieldId(
			event.item.element.nativeElement.id
		);
		if (this.currentIndex == 0) {
			this.onFieldDrop(currentField);
		} else {
			this.onFieldDrop(currentField, true);
		}
	}

	public getFieldByFieldId(id) {
		let field;
		field = this.allFields.find((field) => field.FIELD_ID === id);
		if (!field) {
			field = this.chaildFields.find((field) => field.FIELD_ID === id);
		}
		return field;
	}

	public removeDropHereData() {
		let colNameLastindx = this.displayedColumns.length - 1;
		let colObjLastindx = this.displayedColumnsObj.length - 1;
		// let sourceLastIndex = this.source.length - 1;

		this.displayedColumns.splice(colNameLastindx, 1);
		this.displayedColumnsObj.splice(colObjLastindx, 1);
		// this.source.splice(sourceLastIndex, 1);

		this.dataSource = new MatTableDataSource<any>(this.source);
	}

	public onResizeEnd(event: ResizeEvent, col) {
		if (event.edges) {
			const cssValue = event.rectangle.width + 'px';
			const columnElts = document.getElementsByClassName(
				'col-resSize-' + col.NAME
			);
			for (let i = 0; i < columnElts.length; i++) {
				const currentEl = columnElts[i] as HTMLDivElement;
				currentEl.style.width = cssValue;
			}
		}
	}

	public setDropList(event) {
		this.chaildFields = [];
		if (event && event.index == 1) {
			this.currentIndex = 1;
		} else if (event && event.index == 0) {
			this.currentIndex = 0;
		}
	}

	clearChaildList() {
		this.chaildFields = [];
	}

	public createDynamicDataSource(source) {
		if (source.length > 0) {
			let datasource = new MatTableDataSource<any>(source);
			return datasource;
		}
	}

	public getChaildTableColNames(source) {
		if (source.length > 0) {
			let fieldKeys = Object.keys(source[0]);
			let display = [];
			if (fieldKeys?.length > 0) {
				fieldKeys.forEach((key) => {
					display.push(this.getFieldName(key));
				});
			}
			return display;
		}
	}

	public getChildTableColObj(source, col) {
		if (source.length > 0) {
			let fieldKeys = Object.keys(source[0]);
			let colData = [];
			let relatedFields = this.relatedFields[col.parentModuleId];
			if (fieldKeys?.length > 0) {
				fieldKeys.forEach((name) => {
					let fieldData: any = {};
					if (relatedFields.length > 0) {
						relatedFields.find((field) => {
							if (field.NAME == name) {
								fieldData.NAME = name;
								fieldData.DISPLAY = this.getFieldName(name);
								fieldData.DATA_TYPE = field.DATA_TYPE;
								colData.push(fieldData);
							}
						});
					}
				});
				return colData;
			}
		}
	}

	// to get total records length for every child table
	public getTotalRecordsLength() {
		let totatalCounts = {};
		this.displayedColumnsObj.forEach((colObj) => {
			if (colObj.isParentField == true) {
				this.reportInfo.forEach((data, index) => {
					let dataId = data['DATA_ID'];
					let fieldId = colObj.parentFieldId;
					this.reportService
						.buildQuireyToGetAggregationCount(
							this.reportForm.value['MODULE'],
							dataId,
							fieldId
						)
						.subscribe((totalCount: any) => {
							if (totatalCounts.hasOwnProperty('row' + index)) {
								totatalCounts['row' + index].push({
									NAME: colObj.NAME,
									length: totalCount.TOTAL_RECORDS,
								});
							} else {
								totatalCounts['row' + index] = [
									{
										NAME: colObj.NAME,
										length: totalCount.TOTAL_RECORDS,
									},
								];
							}

							this.childTableLength = totatalCounts;
						});
				});
			}
		});
	}

	public displayTableLength(rowIndex, colName) {
		let currentEntry: any = {};
		let length;
		currentEntry = this.childTableLength['row' + rowIndex];
		if (currentEntry) {
			currentEntry.forEach((element) => {
				if (element?.NAME && element?.NAME == colName) {
					length = element?.length;
				}
			});
		}
		return length ? length : 0;
	}

	public setPaginationForChild() {
		this.displayedColumnsObj.forEach((col, colIndex) => {
			if (col.isParentField == true) {
				this.reportInfo.forEach((row, rowIndex) => {
					this.childPagination['table_' + rowIndex + '_' + colIndex] = {
						pageSize: 1,
						pageIndex: 0,
						totalRecords: 0,
					};

					this.childTableSorting['table_' + rowIndex + '_' + colIndex] = {
						direction: 'asc',
						active: '',
					};
				});
			}
		});
	}

	public getPageDetails(rowIndex, colIndex, param) {
		if (this.childPagination['table_' + rowIndex + '_' + colIndex]) {
			return this.childPagination['table_' + rowIndex + '_' + colIndex][param];
		}
	}

	public getSortDetails(rowIndex, colIndex, param) {
		if (this.childTableSorting['table_' + rowIndex + '_' + colIndex]) {
			return this.childTableSorting['table_' + rowIndex + '_' + colIndex][
				param
			];
		}
	}
}
