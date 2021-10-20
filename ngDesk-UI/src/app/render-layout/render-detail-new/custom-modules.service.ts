import { Injectable } from '@angular/core';

import { CacheService } from '@src/app/cache.service';

import { MatChipInputEvent } from '@angular/material/chips';

import { Router } from '@angular/router';

import { forkJoin, of, Subject } from 'rxjs';
import {
	catchError,
	debounceTime,
	distinctUntilChanged,
	map,
	mergeMap,
	startWith,
	switchMap,
} from 'rxjs/operators';

import { HttpClient } from '@angular/common/http';
import { FormControl } from '@angular/forms';
import { DataApiService, ConversionApiService } from '@ngdesk/data-api';
import { CustomTableService } from './../../custom-table/custom-table.service';
import { UsersService } from '@src/app/users/users.service';
import { Condition } from '../../models/condition';
import { ColumnShow, ListLayout, OrderBy } from '../../models/list-layout';
import { RenderLayoutService } from '../render-layout.service';
import { SortedListLayoutEntryService } from '../render-list-layout-new/sorted-list-layout-entry.service';
import { DiscoveryMapsService } from '@src/app/company-settings/software-asset-management/discovery-maps/discovery-maps.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { TranslateService } from '@ngx-translate/core';
import { COSMIC_THEME } from '@nebular/theme';
import { PhoneService } from './../data-types/phone.service';
import { AppGlobals } from '@src/app/app.globals';
import { result } from 'lodash';

@Injectable({
	providedIn: 'root',
})
export class CustomModulesService {
	public customModuleVariables: any = {};
	public discussionControls = {};
	public noDiscoveryMaps: boolean;
	public layoutType;
	public formControls = {};
	public relationFieldFilteredEntries = {};
	public discoveryMaps = [];
	public scrollSubject = new Subject<any>();
	public chronometerValues = {};
	public oneToManyControls = {};
	public existingDataIdsOneToMany = [];
	public oneToManyFieldTable = {
		FIELD: {},
		LAYOUT_ID: '',
		DATA_ID: '',
	};
	public teamsAdded = [];
	public newUserDetails = {};
	public fieldsConditionsMap: Map<String, any> = new Map<String, any>();
	public fieldsInfluenceMap: Map<String, any> = new Map<String, []>();
	public displayDiscoveryMapName: Map<String, String> = new Map<
		String,
		String
	>();
	public entryWorkflowObject = {
		HAS_WORKFLOW: false,
		ENTRY_WORKFLOW_STAGE_STATUS: new Map<String, String>(),
		ENTRY_WORKFLOW_KEYS: [],
	};
	public currentOneToManyListLayout: ListLayout = new ListLayout(
		'',
		'',
		'',
		'',
		new OrderBy('', ''),
		new ColumnShow([]),
		[new Condition('', '', '', '')],
		false
	);
	public workflowStageStatus = {};
	public discoveryMapScrollSubject = new Subject<any>();
	public selectedValue = '';
	public displayApprovalButton = false;
	public approvalStatusObject = {};
	public fieldsDisableMap: Map<String, boolean> = new Map<String, any>();
	public oneToManyonPageChange = false;
	public isSavingOneToMany = false;
	public oneToManyFields = {};

	public oneToManyRelationshipData: any = {
		VALUE: {},
		FIELD_NAME: '',
		PARRENT_MADULE_ID: '',
		PARRENT_ENTRY: {},
		CURRENT_FIELD: {},
	};

	private customSubject = new Subject<any>();
	customModuleServiceObservable = this.customSubject.asObservable();

	constructor(
		private cacheService: CacheService,
		private router: Router,
		private dataService: DataApiService,
		private currencyConversionApi: ConversionApiService,
		private usersService: UsersService,
		private customTableService: CustomTableService,
		private renderLayoutService: RenderLayoutService,
		private httpClient: HttpClient,
		private translateService: TranslateService,
		private bannerMessageService: BannerMessageService,
		private discoveryMapService: DiscoveryMapsService,
		private sortedListLayoutEntryService: SortedListLayoutEntryService,
		private phoneService: PhoneService,
		private globals: AppGlobals
	) {}

	public clearVariables() {
		this.customModuleVariables = {};
		this.discussionControls = {};
		this.relationFieldFilteredEntries = {};
		this.discoveryMaps = [];
		this.noDiscoveryMaps = false;
		this.oneToManyControls = {};
		// this.formControls = {};
		this.chronometerValues = {};
		this.teamsAdded = [];
		this.scrollSubject = new Subject<any>();
		this.oneToManyFields = {};
		this.existingDataIdsOneToMany = [];
		this.oneToManyFieldTable = {
			FIELD: {},
			LAYOUT_ID: '',
			DATA_ID: '',
		};
		this.fieldsConditionsMap = new Map<String, any>();
		this.fieldsInfluenceMap = new Map<String, []>();
		this.newUserDetails = {
			EMAIL_ADDRESS: '',
			PHONE_NUMBER: {
				COUNTRY_CODE: 'in',
				DIAL_CODE: '+91',
				PHONE_NUMBER: '',
				COUNTRY_FLAG: 'in.svg',
			},
			FIRST_NAME: '',
			LAST_NAME: '',
			ROLE: '',
		};
		this.entryWorkflowObject = {
			HAS_WORKFLOW: false,
			ENTRY_WORKFLOW_STAGE_STATUS: new Map<String, String>(),
			ENTRY_WORKFLOW_KEYS: [],
		};
		this.workflowStageStatus = {};
		this.approvalStatusObject = {};
		this.displayApprovalButton = false;
	}

	public loadVariablesForModule(module, entry, createLayout?) {
		const moduleId = module['MODULE_ID'];
		let entryId = 'new';
		if (entry['DATA_ID']) {
			entryId = entry['DATA_ID'];
		}

		const discussionField = module['FIELDS'].find(
			(field) => field.DATA_TYPE.DISPLAY === 'Discussion'
		);
		if (discussionField) {
			this.discussionControls = {
				MESSAGE: '',
				SHOW_TYPE: 'MESSAGES',
				DISCUSSION_TYPE: 'Messages',
				MESSAGE_TYPE: 'MESSAGE',
			};
		}
		const oneToManyRelationshipFields = module['FIELDS'].filter(
			(field) =>
				field.DATA_TYPE.DISPLAY === 'Relationship' &&
				field.RELATIONSHIP_TYPE === 'One to Many'
		);
		if (oneToManyRelationshipFields.length > 0) {
			this.oneToManyControls['MODULE_IN_FOCUS'] = '';
			this.oneToManyControls['FIELD_IN_FOCUS'] = '';
		}
		this.initializeScrollSubject(moduleId);
		this.loadFormControls(module, entry, createLayout);

		switch (module['NAME']) {
			case 'Tickets':
				this.customModuleVariables['SHOW_NEXT_BUTTON'] = false;
				this.customModuleVariables['SHOW_BACK_BUTTON'] = false;
				if (entryId !== 'new') {
					const index = this.getIndexOfEntryOnCurrentLayout(moduleId, entryId);
					if (index !== -1) {
						if (
							index <
							this.sortedListLayoutEntryService.sortedEntries.length - 1
						) {
							this.customModuleVariables['SHOW_NEXT_BUTTON'] = true;
						}
						if (index !== 0) {
							this.customModuleVariables['SHOW_BACK_BUTTON'] = true;
						}
					}
				}
				break;
			case 'Users':
				this.customModuleVariables['RIGHT_SIDEBAR'] = {
					OPEN: true,
				};
				this.customModuleVariables['SHOW_CALL_ICON'] = false;
				if (this.usersService.getSubdomain() === 'haloocom') {
					this.customModuleVariables['SHOW_CALL_ICON'] = true;
				}
				break;
		}
	}

	// START DISCUSSION FUNCTIONS
	private onSelectDiscussionTabs($event) {
		const tabValue = $event.tab.textLabel;
		if (tabValue === 'All') {
			this.discussionControls['SHOW_TYPE'] = 'ALL';
			this.discussionControls['DISCUSSION_TYPE'] = 'Messages';
		} else {
			this.discussionControls['SHOW_TYPE'] = 'MESSAGES';
		}
	}
	// END DISCUSSION FUNCTIONS

	// START RELATIONSHIP FUNCTIONS
	private initializeScrollSubject(moduleId) {
		this.scrollSubject
			.pipe(
				switchMap((moduleField: any) => {
					return this.cacheService.getModule(moduleField['MODULE']).pipe(
						map((relatedModule) => {
							const primaryDisplayField = relatedModule['FIELDS'].find(
								(field) =>
									field.FIELD_ID === moduleField['PRIMARY_DISPLAY_FIELD']
							);
							let primaryDisplayFieldName = 'DATE_CREATED';
							if (primaryDisplayField) {
								primaryDisplayFieldName = primaryDisplayField['NAME'];
							}

							const fieldControlName =
								moduleField.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
							const fieldSearch =
								primaryDisplayFieldName +
								'=' +
								(this.formControls[fieldControlName] &&
								this.formControls[fieldControlName].value
									? this.formControls[fieldControlName].value
									: '*');
							const sort = ['PRIMARY_DISPLAY_FIELD', 'asc'];

							return {
								SORT: sort,
								FIELD_SEARCH: fieldSearch,
							};
						}),
						mergeMap((response) => {
							return this.dataService
								.getRelationshipData(
									moduleId,
									moduleField.FIELD_ID,
									response['FIELD_SEARCH'],
									Math.ceil(
										this.relationFieldFilteredEntries[moduleField.NAME].length /
											10
									),
									10,
									response['SORT']
								)
								.pipe(
									map((results: any) => {
										if (results.content.length > 0) {
											this.relationFieldFilteredEntries[moduleField.NAME] =
												this.relationFieldFilteredEntries[
													moduleField.NAME
												].concat(results.content);
											if (
												this.oneToManyRelationshipData.PARRENT_MADULE_ID != ''
											) {
												this.setOneToManyFieldValues(
													this.oneToManyRelationshipData.PARRENT_MADULE_ID,
													this.oneToManyRelationshipData.CURRENT_FIELD,
													this.oneToManyRelationshipData.PARRENT_ENTRY,
													this.relationFieldFilteredEntries
												);
											}
										}
										return results.content;
									})
								);
						})
					);
				})
			)
			.subscribe();
	}

	public onScroll(moduleField) {
		this.scrollSubject.next(moduleField);
	}

	public disableFieldBasedOnFieldPermission(fieldPermissions) {
		if (
			fieldPermissions.getFieldPermissions !== null &&
			fieldPermissions.getFieldPermissions.length > 0
		) {
			fieldPermissions.getFieldPermissions.forEach((element) => {
				this.fieldsDisableMap[element.fieldId] = element.notEditable;
			});
		}
	}

	private loadFormControls(module, entry, createLayout) {
		let relationshipFields = module['FIELDS'].filter(
			(field) => field['DATA_TYPE']['DISPLAY'] === 'Relationship'
		);
		relationshipFields = relationshipFields.filter(
			(field) => field['RELATIONSHIP_TYPE'] !== 'One to Many'
		);
		relationshipFields.forEach((moduleField) => {
			const fieldControlName = moduleField.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
			this.formControls[fieldControlName] = new FormControl();
			if (
				moduleField.NOT_EDITABLE ||
				this.fieldsDisableMap[moduleField.FIELD_ID]
			) {
				this.formControls[fieldControlName].disable();
			}
			this.relationFieldFilteredEntries[moduleField['NAME']] = [];
			this.cacheService
				.getModule(moduleField['MODULE'])
				.subscribe((relatedModule) => {
					const primaryDisplayField = relatedModule['FIELDS'].find(
						(field) => field.FIELD_ID === moduleField['PRIMARY_DISPLAY_FIELD']
					);
					let primaryDisplayFieldName = 'DATE_CREATED';
					if (primaryDisplayField) {
						primaryDisplayFieldName = primaryDisplayField['NAME'];
					}

					this.formControls[fieldControlName].valueChanges
						.pipe(
							startWith(''),
							debounceTime(400),
							distinctUntilChanged(),
							switchMap((value: any) => {
								const fieldSearch =
									primaryDisplayFieldName +
									'=' +
									(value && value !== '' ? value : '*');
								const sort = ['PRIMARY_DISPLAY_FIELD', 'asc'];
								return this.dataService
									.getRelationshipData(
										module['MODULE_ID'],
										moduleField.FIELD_ID,
										fieldSearch,
										0,
										10,
										sort
									)
									.pipe(
										map((results: any) => {
											this.relationFieldFilteredEntries[moduleField.NAME] =
												results.content;
											if (
												this.oneToManyRelationshipData.PARRENT_MADULE_ID != ''
											) {
												this.setOneToManyFieldValues(
													this.oneToManyRelationshipData.PARRENT_MADULE_ID,
													this.oneToManyRelationshipData.CURRENT_FIELD,
													this.oneToManyRelationshipData.PARRENT_ENTRY,
													this.relationFieldFilteredEntries
												);
											}
											return results.content;
										}),
										catchError((error) => of([]))
									);
							})
						)
						.subscribe();

					if (
						moduleField['RELATIONSHIP_TYPE'] === 'Many to One' ||
						moduleField['RELATIONSHIP_TYPE'] === 'One to One'
					) {
						if (
							entry[moduleField['NAME']] &&
							entry[moduleField['NAME']] !== null
						) {
							if (entry[moduleField['NAME']]['PRIMARY_DISPLAY_FIELD']) {
								this.formControls[fieldControlName].setValue(
									entry[moduleField['NAME']]['PRIMARY_DISPLAY_FIELD']
								);
							}
						}
					}
				});
		});
	}

	public closeAutoComplete(module, moduleField) {
		this.cacheService
			.getModule(moduleField['MODULE'])
			.subscribe((relatedModule) => {
				const primaryDisplayField = relatedModule['FIELDS'].find(
					(field) => field.FIELD_ID === moduleField['PRIMARY_DISPLAY_FIELD']
				);
				let primaryDisplayFieldName = 'DATE_CREATED';
				if (primaryDisplayField) {
					primaryDisplayFieldName = primaryDisplayField['NAME'];
				}

				const fieldControlName =
					moduleField.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
				of(moduleField)
					.pipe(
						startWith(''),
						debounceTime(400),
						distinctUntilChanged(),
						switchMap((value: any) => {
							const fieldSearch =
								primaryDisplayFieldName +
								'=' +
								(this.formControls[fieldControlName] &&
								this.formControls[fieldControlName].value
									? this.formControls[fieldControlName].value
									: '*');
							const sort = ['PRIMARY_DISPLAY_FIELD', 'asc'];

							return this.dataService
								.getRelationshipData(
									module['MODULE_ID'],
									moduleField.FIELD_ID,
									fieldSearch,
									0,
									10,
									sort
								)
								.pipe(
									map((results: any) => {
										if (results.content.length > 0) {
											this.relationFieldFilteredEntries[moduleField.NAME] =
												results.content;
											if (
												this.oneToManyRelationshipData.PARRENT_MADULE_ID != ''
											) {
												this.setOneToManyFieldValues(
													this.oneToManyRelationshipData.PARRENT_MADULE_ID,
													this.oneToManyRelationshipData.CURRENT_FIELD,
													this.oneToManyRelationshipData.PARRENT_ENTRY,
													this.relationFieldFilteredEntries
												);
											}
										}
										return results.content;
									})
								);
						})
					)
					.subscribe();
			});
	}

	public closeAutoCompleteForDiscoveryMap() {
		this.discoveryMapService
			.getAllDiscoveryMaps(0, 10, 'NAME', 'Asc')
			.subscribe((discoveryMapResponse: any) => {
				if (discoveryMapResponse['discoveryMaps'].length > 0) {
					this.discoveryMaps = discoveryMapResponse['discoveryMaps'];
					this.initialiseDiscoveryMapScrollSubject();
					this.discoveryMaps.forEach((element) => {
						this.displayDiscoveryMapName.set(element.id, element.name);
					});
				} else {
					this.bannerMessageService.errorNotifications.push({
						message: this.translateService.instant(
							'DISCOVERY_MAP_ENTRY_DOESNT_EXIST'
						),
					});
				}
			});
	}

	// Initialize scroll subject for scrolling.
	public initialiseDiscoveryMapScrollSubject() {
		this.discoveryMapScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let page = 0;
					if (this.discoveryMaps && !search) {
						page = Math.ceil(this.discoveryMaps.length / 10);
					}
					return this.discoveryMapService
						.searchDiscoveryMaps(page, 10, 'NAME', 'Asc', value)
						.pipe(
							map((results: any) => {
								if (search) {
									this.discoveryMaps = results['DATA'];
								} else if (results['DATA'].length > 0) {
									this.discoveryMaps = this.discoveryMaps.concat(
										results['DATA']
									);
								}
								return results['DATA'];
							})
						);
				})
			)
			.subscribe();
	}

	// When scrolling the dropdown.
	public onScrollDiscoveryMaps() {
		this.discoveryMapScrollSubject.next(['', false]);
	}

	// While entering any text to the input start searching.
	public onSearchValue() {
		const searchValue = this.selectedValue;
		if (typeof searchValue !== 'object') {
			const searchText = searchValue;
			this.discoveryMapScrollSubject.next([searchText, true]);
		}
	}

	public resetInput(event: MatChipInputEvent): void {
		const input = event.input;
		if (input) {
			input.value = '';
		}
	}

	// END RELATIONSHIP FUNCTIONS

	// START LIST TEXT FUNCTIONS
	public addItemToListText(entry, value, fieldName, module) {
		if (!entry[fieldName]) {
			entry[fieldName] = [];
		}
		const field = module['FIELDS'].find((field) => field.NAME === fieldName);
		if (field['DATA_TYPE']['DISPLAY'] === 'Email') {
			if ((value || '').trim() && /\S+@\S+\.\S+/.test((value || '').trim())) {
				entry[fieldName].push(value.trim());
			}
		} else {
			if ((value || '').trim() && /\S+@\S+\.\S+/.test((value || '').trim())) {
				entry[fieldName].push(value.trim());
			}
		}
		return entry;
	}

	public removeItemFromListText(entry, value, fieldName) {
		const index = entry[fieldName].indexOf(value);
		entry[fieldName].splice(index, 1);
		return entry;
	}

	// END LIST TEXT FUNCTIONS

	// START TICKET MODULE FUNCTIONS
	public nextTicket(moduleId, entryId) {
		const index = this.getIndexOfEntryOnCurrentLayout(moduleId, entryId);
		const size = this.sortedListLayoutEntryService.sortedEntries.length;
		if (index === -1 || index === size) {
			// REDIRECT TO HOME OF MODULE
			this.router.navigate([`render/${moduleId}`]);
		} else {
			// REDIRECT TO NEXT TICKET
			this.router.navigate([
				`render/${moduleId}/edit/${
					this.sortedListLayoutEntryService.sortedEntries[index + 1].DATA_ID
				}`,
			]);
		}
	}

	public previousTicket(moduleId, entryId) {
		const index = this.getIndexOfEntryOnCurrentLayout(moduleId, entryId);
		const size = this.sortedListLayoutEntryService.sortedEntries.length;
		if (index === -1 || index === size) {
			// REDIRECT TO HOME OF MODULE
			this.router.navigate([`render/${moduleId}`]);
		} else {
			// REDIRECT TO PREVIOUS TICKET
			this.router.navigate([
				`render/${moduleId}/edit/${
					this.sortedListLayoutEntryService.sortedEntries[index - 1].DATA_ID
				}`,
			]);
		}
	}

	private getIndexOfEntryOnCurrentLayout(moduleId, entryId) {
		if (!this.sortedListLayoutEntryService.sortedEntries) {
			return -1;
		}
		return this.sortedListLayoutEntryService.sortedEntries.findIndex(
			(entry) => entry['DATA_ID'] === entryId
		);
	}
	// END TICKET MODULE FUNCTIONS

	public setupOneToManyTable(module, relatedModuleId, field, entry) {
		if (!this.oneToManyonPageChange) {
			this.customTableService.setTableDataSource([], 0);
			this.cacheService
				.getModule(relatedModuleId)
				.subscribe((modulesResponse) => {
					const relatedModule = modulesResponse;
					const fields = modulesResponse['FIELDS'];
					const fieldRelated = relatedModule.FIELDS.find(
						(relatedfield) =>
							field.PRIMARY_DISPLAY_FIELD === relatedfield.FIELD_ID
					);
					const defaultListLayout = relatedModule.LIST_LAYOUTS.find(
						(layout) =>
							layout.ROLE === this.usersService.user.ROLE && layout.IS_DEFAULT
					);
					if (defaultListLayout) {
						this.setCustomTableFields(defaultListLayout, relatedModule);
						// this.setShowListLayouts(relatedModule);
						this.currentOneToManyListLayout = defaultListLayout;

						this.oneToManyFieldTable.DATA_ID = entry.DATA_ID;
						this.oneToManyFieldTable.FIELD = field;
						this.oneToManyFieldTable.LAYOUT_ID =
							this.currentOneToManyListLayout['LAYOUT_ID'];
						let fields = this.OneToManyLayoutField(
							defaultListLayout,
							relatedModule,
							entry.DATA_ID
						);
						this.getOneToManyEntries(
							module.MODULE_ID,
							field.FIELD_ID,
							entry.DATA_ID,
							relatedModule
						);
					}
				});
		} else {
			this.oneToManyonPageChange = false;
		}
	}

	public setCustomTableFields(listlayout, module) {
		this.customTableService.columnsHeaders = [];
		this.customTableService.columnsHeadersObj = [];
		if (listlayout) {
			const field = module.FIELDS.find(
				(selectedField) => selectedField.FIELD_ID === listlayout.ORDER_BY.COLUMN
			);
			this.customTableService.activeSort = {
				ORDER_BY: listlayout.ORDER_BY.ORDER.toLowerCase(),
				SORT_BY: field.DISPLAY_LABEL,
				NAME: field.NAME,
			};

			this.customTableService.sortBy =
				this.customTableService.activeSort.SORT_BY;
			this.customTableService.sortOrder =
				this.customTableService.activeSort.ORDER_BY;
			listlayout.COLUMN_SHOW.FIELDS.forEach((layoutField) => {
				this.setCustomTableColumnHeaders(layoutField, module);
			});
			this.customTableService.columnsHeaders.push('Remove');
			const remove = {
				DISPLAY: 'Remove',
				NAME: 'Remove Entry',
				DATA_TYPE: null,
				FIELD_ID: null,
			};
			this.customTableService.columnsHeadersObj.push(remove);
		}
	}

	public setCustomTableColumnHeaders(layoutField, module) {
		const field = module.FIELDS.find(
			(selectedField) => selectedField.FIELD_ID === layoutField
		);
		const headersObject = {
			DISPLAY: field.DISPLAY_LABEL,
			NAME: field.NAME,
			DATA_TYPE: field.DATA_TYPE.DISPLAY,
		};

		this.customTableService.columnsHeaders.push(field.DISPLAY_LABEL);
		// 	this.customTableService.columnsHeaders.push('Add Entry');
		this.customTableService.columnsHeadersObj.push(headersObject);
	}

	public OneToManyLayoutField(listlayout, module, entryId) {
		if (listlayout) {
			listlayout.COLUMN_SHOW.FIELDS.forEach((layoutField) => {});
		}
	}
	public getOneToManyEntries(moduleId, fieldId, dataId, module) {
		this.customTableService.isLoading = true;
		const allModules = this.cacheService.companyData['MODULES'];
		if (!this.customTableService.sortBy) {
			this.customTableService.sortBy = 'DATE_UPDATED';
			this.customTableService.sortOrder = 'desc';
		}
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const moduleName = module.NAME.replace(/\s+/g, '_');

		const fieldsToShow =
			this.currentOneToManyListLayout['COLUMN_SHOW']['FIELDS'];
		let fieldsQuery = 'DATA_ID: _id' + '\n';

		fieldsToShow.forEach((fieldId) => {
			const moduleField = module.FIELDS.find(
				(field) => field.FIELD_ID === fieldId
			);
			if (moduleField.DATA_TYPE.DISPLAY === 'Relationship') {
				const relatedModule = allModules.find(
					(module) => module.MODULE_ID === moduleField.MODULE
				);

				const primaryDisplayField = relatedModule.FIELDS.find(
					(field) => field.FIELD_ID === moduleField.PRIMARY_DISPLAY_FIELD
				);
				const relationshipQuery = `${moduleField.NAME} {
					DATA_ID: _id
					PRIMARY_DISPLAY_FIELD: ${primaryDisplayField.NAME}
				}`;
				fieldsQuery += relationshipQuery + '\n';
			} else if (moduleField.DATA_TYPE.DISPLAY === 'Phone') {
				fieldsQuery +=
					`PHONE_NUMBER{
					COUNTRY_CODE 
					DIAL_CODE
					PHONE_NUMBER
					COUNTRY_FLAG
				}` + '\n';
			} else {
				fieldsQuery += moduleField.NAME + '\n';
			}
		});

		const query = `{
				DATA: get${moduleName}OneToMany (moduleId: "${moduleId}", fieldId: "${fieldId}", dataId: "${dataId}", pageNumber: ${page}, 
				pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
					${fieldsQuery}
				}
				TOTAL_RECORDS: getOneToManyCountDataFetcher(moduleId: "${moduleId}", fieldId: "${fieldId}", dataId: "${dataId}")
			}`;

		this.makeGraphQLCall(query).subscribe(
			(entriesReponse: any) => {
				this.sortedListLayoutEntryService.sortedEntries = entriesReponse.DATA;
				this.customTableService.setTableDataSource(
					entriesReponse.DATA,
					entriesReponse.TOTAL_RECORDS
				);
				this.customTableService.isLoading = false;
			},
			(error: any) => {
				this.customTableService.isLoading = false;
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public onPageChange(event, moduleId, field) {
		const allModules = this.cacheService.companyData['MODULES'];
		const relatedModule = allModules.find(
			(module) => module.MODULE_ID === field.MODULE
		);
		const relatedField = relatedModule.FIELDS.find(
			(moduleField) => moduleField.FIELD_ID === field.RELATIONSHIP_FIELD
		);
		if (event && event.hasOwnProperty('pageIndex')) {
			this.customTableService.pageIndex = event.pageIndex;
			this.customTableService.pageSize = event.pageSize;
		}
		if (event.active) {
			const moduleField = relatedModule.FIELDS.find(
				(field) => field.DISPLAY_LABEL === event.active
			);
			this.customTableService.activeSort = {
				ORDER_BY: event.direction,
				SORT_BY: event.active,
				NAME: moduleField.NAME,
			};
			this.customTableService.sortBy = moduleField.NAME;
			this.customTableService.sortOrder = event.direction;
		}
		this.customTableService.setTableDataSource([], 0);
		this.getOneToManyEntries(
			relatedField.MODULE,
			field.FIELD_ID,
			this.oneToManyFieldTable.DATA_ID,
			relatedModule
		);
	}

	// FORMULA FIELD FUNCTION

	public calculateFormula(module, entry: any): any {
		return this.dataService.generateFormulaFieldValue(module.MODULE_ID, entry);
	}

	// CURRENCY EXCHANGE FIELD FUNCTION
	public calculateExchangeRate(moduleId, fieldId, entry: any): any {
		return this.currencyConversionApi.postCurrencyConversion(
			moduleId,
			fieldId,
			entry
		);
	}

	public convertAggregateFields(aggregateField, module, value) {
		const fieldInCurrentModule = module.FIELDS.find(
			(field) => field.FIELD_ID === aggregateField.AGGREGATION_FIELD
		);
		return this.cacheService.getModule(fieldInCurrentModule.MODULE).pipe(
			map((res) => {
				const fieldInRelatedModule = res['FIELDS'].find(
					(field) => field.FIELD_ID === aggregateField.AGGREGATION_RELATED_FIELD
				);
				if (!value) {
					value = 0;
				}
				if (fieldInRelatedModule.DATA_TYPE.DISPLAY === 'Chronometer') {
					return this.renderLayoutService.chronometerFormatTransform(value, '');
				} else {
					return this.transformNumbersField(
						value,
						aggregateField.NUMERIC_FORMAT,
						aggregateField.PREFIX,
						aggregateField.SUFFIX
					);
				}
			})
		);
	}

	// START HALOOCOM API
	public makePhoneCall(entry) {
		// MAKE API CALL WITH GIVEN URL

		if (!entry.PHONE_NUMBER) {
			this.dataService
				.getModuleEntry('5c93698bb2039a0001872e82', entry.DATA_ID)
				.subscribe(
					(userResponse: any) => {
						const url = `https://lnt.haloocom.in/Contact_customer/click2call.php
				?UserName=${userResponse.EMAIL_ADDRESS}&customer_number=${userResponse.PHONE_NUMBER.PHONE_NUMBER}`;
						this.httpClient.get(url).subscribe((response) => {
							console.log(response);
						});
					},
					(error: any) => {
						console.log(error);
					}
				);
		} else {
			const url = `https://lnt.haloocom.in/Contact_customer/click2call.php
			?UserName=${entry.EMAIL_ADDRESS}&customer_number=${entry.PHONE_NUMBER.PHONE_NUMBER}`;
			this.httpClient.get(url).subscribe((response) => {
				console.log(response);
			});
		}
	}

	public loadUserDetails(phoneNumber) {
		this.newUserDetails = {
			EMAIL_ADDRESS: '',
			PHONE_NUMBER: {
				COUNTRY_CODE: 'in',
				DIAL_CODE: '+91',
				PHONE_NUMBER: phoneNumber,
				COUNTRY_FLAG: 'in.svg',
			},
			FIRST_NAME: '',
			LAST_NAME: '',
			ROLE: '',
		};
	}

	public hangupCall(entry) {
		// MAKE API CALL WITH GIVEN URL
		const url = `https://lnt.haloocom.in/Call_hangup/call_hangup.php?
		UserName=x${entry.EMAIL_ADDRESS}&disposition=${entry.CALL_STATUS}`;
		this.httpClient.get(url).subscribe((response) => {
			console.log(response);
		});
	}
	// END HALOOCOM API

	public fieldConditions(panels) {
		panels.forEach((panel) => {
			const grids = panel.GRIDS;
			grids.forEach((rows) => {
				rows.forEach((pill) => {
					if (!pill.IS_EMPTY) {
						if (
							pill.SETTINGS &&
							pill.SETTINGS.ACTION !== '' &&
							pill.SETTINGS.CONDITIONS.length > 0
						) {
							this.fieldsConditionsMap.set(pill.FIELD_ID, pill.SETTINGS);
							const conditions = pill.SETTINGS.CONDITIONS;
							conditions.forEach((condition) => {
								let conditionInfluence = [];
								if (this.fieldsInfluenceMap.has(condition.CONDITION)) {
									conditionInfluence = this.fieldsInfluenceMap.get(
										condition.CONDITION
									);
								}
								if (!conditionInfluence.includes(pill.FIELD_ID)) {
									conditionInfluence.push(pill.FIELD_ID);
								}
								this.fieldsInfluenceMap.set(
									condition.CONDITION,
									conditionInfluence
								);
							});
						}
					}
				});
			});
		});
	}

	public fieldConditionEvaluation(condition, field, entry) {
		let result = false;
		switch (condition.OPERATOR) {
			case 'EQUALS_TO':
				if (field.DATA_TYPE.DISPLAY === 'Checkbox') {
					if (entry[field.NAME]) {
						if (
							entry[field.NAME].toString() ===
							condition.CONDITION_VALUE.toString()
						) {
							result = true;
						}
					} else {
						if (condition.CONDITION_VALUE.toString() === 'false') {
							result = true;
						}
					}
				} else {
					if (entry[field.NAME]) {
						if (entry[field.NAME] === condition.CONDITION_VALUE) {
							result = true;
						}
					}
				}
				break;
			case 'NOT_EQUALS_TO':
				if (field.DATA_TYPE.DISPLAY === 'Checkbox') {
					if (entry[field.NAME]) {
						if (
							entry[field.NAME].toString() !==
							condition.CONDITION_VALUE.toString()
						) {
							result = true;
						}
					} else {
						if (condition.CONDITION_VALUE.toString() !== 'false') {
							result = true;
						}
					}
				} else {
					if (entry[field.NAME]) {
						if (entry[field.NAME] !== condition.CONDITION_VALUE) {
							result = true;
						}
					}
				}
				break;
			case 'IS_SET':
				if (
					entry[field.NAME] &&
					entry.hasOwnProperty(field.NAME) &&
					entry[field.NAME]
				) {
					result = true;
				}
				break;
			case 'NOT_SET':
				if (
					entry[field.NAME] &&
					(!entry.hasOwnProperty(field.NAME) || entry[field.NAME] === null)
				) {
					result = true;
				}
				break;
			case 'LESS_THAN':
				if (
					entry[field.NAME] &&
					entry[field.NAME] < condition.CONDITION_VALUE
				) {
					result = true;
				}
				break;
			case 'GREATER_THAN':
				if (
					entry[field.NAME] &&
					entry[field.NAME] > condition.CONDITION_VALUE
				) {
					result = true;
				}
				break;
			case 'CONTAINS':
				if (
					entry[field.NAME] &&
					entry[field.NAME].includes(condition.CONDITION_VALUE)
				) {
					result = true;
				}
				break;
			case 'DOES_NOT_CONTAIN':
				if (
					entry[field.NAME] &&
					!entry[field.NAME].includes(condition.CONDITION_VALUE)
				) {
					result = true;
				}
				break;
			case 'LENGTH_IS_GREATER_THAN':
				if (
					entry[field.NAME] &&
					entry[field.NAME].length > condition.CONDITION_VALUE
				) {
					result = true;
				}
				break;
			case 'LENGTH_IS_LESS_THAN':
				if (
					entry[field.NAME] &&
					entry[field.NAME].length < condition.CONDITION_VALUE
				) {
					result = true;
				}
				break;
		}
		return result;
	}

	public evaluateConditionResult(restrictionValues) {
		if (
			restrictionValues.All.length > 0 &&
			restrictionValues.All.includes(false)
		) {
			return false;
		} else {
			if (
				(restrictionValues.Any.length > 0 &&
					restrictionValues.Any.includes(true)) ||
				restrictionValues.All.length > 0
			) {
				return true;
			}
		}
	}

	public setToolBarButtons(entry, fieldName, moduleId) {
		let approvalStatus = entry[fieldName]['STATUS'];
		if (approvalStatus === 'APPROVED') {
			this.approvalStatusObject = { status: 'APPROVED' };
			this.displayApprovalButton = false;
		} else {
			if (approvalStatus === 'REQUIRED') {
				const query = `{
		result: getApprovalOngoingData(id: "${entry.DATA_ID}", moduleId: "${moduleId}"){
			displayButton
			status
			deniedBy{
				deniedUser{
					EMAIL_ADDRESS
					CONTACT{FULL_NAME}
					}
			  deniedComments
			}
		}
	}`;
				this.makeGraphQLCall(query).subscribe((entriesReponse: any) => {
					if (entriesReponse.result !== null) {
						this.approvalStatusObject = entriesReponse.result;
						this.displayApprovalButton = entriesReponse.result.displayButton;
					} else {
						this.displayApprovalButton = false;
						this.approvalStatusObject = {};
					}
				});
			} else if (approvalStatus === 'REJECTED') {
				const query = `{
					result: getApprovalDeniedData(id: "${entry.DATA_ID}", moduleId: "${moduleId}"){
						status
						deniedBy{
							deniedUser{
								EMAIL_ADDRESS
								CONTACT{FULL_NAME}
								}
						  deniedComments
						}
					}
				}`;
				this.makeGraphQLCall(query).subscribe((entriesReponse: any) => {
					if (entriesReponse.result !== null) {
						this.approvalStatusObject = entriesReponse.result;
						this.displayApprovalButton = false;
					} else {
						this.displayApprovalButton = false;
						this.approvalStatusObject = {};
					}
				});
			}
		}
	}

	public getApprovalField(module: any) {
		const approvalField = module['FIELDS'].find((field) => {
			return field['DATA_TYPE']['DISPLAY'] === 'Approval';
		});
		return approvalField;
	}

	public getDiscoveryMapField(module: any) {
		const discoveryMapField = module['FIELDS'].find((field) => {
			return field['NAME'] === 'DISCOVERY_MAP';
		});
		return discoveryMapField;
	}

	public getDiscoveryMaps() {
		this.discoveryMaps = [];
		this.discoveryMapService
			.getAllDiscoveryMaps(0, 10, 'NAME', 'Asc')
			.subscribe((discoveryMapResponse: any) => {
				if (discoveryMapResponse['discoveryMaps'].length > 0) {
					this.noDiscoveryMaps = true;
					this.discoveryMaps = discoveryMapResponse['discoveryMaps'];
					this.initialiseDiscoveryMapScrollSubject();
					this.discoveryMaps.forEach((element) => {
						this.displayDiscoveryMapName.set(element.id, element.name);
					});
				} else {
					this.noDiscoveryMaps = false;
				}
			});
	}
	//ADD DEFAULT PHONE FIELD

	public setDefaultPhoneEntry(entry, phoneField) {
		const phone = this.phoneService.getDefaultPhoneValue(phoneField);
		if (entry[phoneField.NAME] && entry[phoneField.NAME]['PHONE_NUMBER']) {
			entry[phoneField.NAME];
		} else {
			entry[phoneField.NAME] = phone;
		}
	}
	public makeGraphQLCall(query: string) {
		return this.httpClient.post(`${this.globals.graphqlUrl}`, query);
	}

	public setOneToManyFieldValues(
		parrentModuleId,
		field,
		entry,
		relationshipValues
	) {
		const allModules = this.cacheService.companyData['MODULES'];
		const parrentModule = allModules.find(
			(module) => module.MODULE_ID === parrentModuleId
		);

		const relatedModule = allModules.find(
			(module) => module.MODULE_ID === field.MODULE
		);
		const relatedField = relatedModule.FIELDS.find(
			(moduleField) => moduleField.FIELD_ID === field.RELATIONSHIP_FIELD
		);
		const primaryField = parrentModule.FIELDS.find(
			(moduleField) =>
				moduleField.FIELD_ID === relatedField.PRIMARY_DISPLAY_FIELD
		);
		let value = entry[primaryField.NAME];
		let currentCurrenEntry;
		if (
			relationshipValues[relatedField.NAME] &&
			relationshipValues[relatedField.NAME].length > 0
		) {
			currentCurrenEntry = relationshipValues[relatedField.NAME].find(
				(item) => (item.PRIMARY_DISPLAY_FIELD = value)
			);
		}

		if (currentCurrenEntry && currentCurrenEntry.DATA_ID && value) {
			const fieldControlName =
				relatedField.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
			this.formControls[fieldControlName].setValue(value);
			value = entry;
			this.oneToManyRelationshipData.VALUE = entry;
			this.oneToManyRelationshipData.FIELD_NAME = relatedField.NAME;
		}
		this.inheritValues(
			relatedField,
			currentCurrenEntry.DATA_ID,
			relatedModule,
			entry
		);
	}

	public inheritValues(field, entryId, module, currentEntry) {
		const parentFieldsMap = new Map<any, any>();
		const fieldsMap = new Map<any, any>();
		const resultMap = new Map<any, any>();
		const map = new Map<any, any>();

		const childModuleId = field.MODULE;
		// Get the child module.
		const childModule = this.cacheService.companyData['MODULES'].find(
			(module) => module.MODULE_ID === childModuleId
		);
		module.FIELDS.forEach((field) => {
			fieldsMap.set(field.FIELD_ID, field);
		});
		childModule.FIELDS.forEach((field) => {
			parentFieldsMap.set(field.FIELD_ID, field);
		});
		// Get the ineritance map
		if (
			field.INHERITANCE_MAPPING !== undefined &&
			field.INHERITANCE_MAPPING !== null
		) {
			const inheritanceMap: Map<string, any> = new Map(
				Object.entries(field.INHERITANCE_MAPPING)
			);
			// Fetch the child module entry using entryId

			const entry = currentEntry;
			const keysList = Array.from(inheritanceMap.keys());
			// Assign the parent field's inerited values
			keysList.forEach((key) => {
				const parentField = parentFieldsMap.get(key);
				if (parentField) {
					map.set(parentFieldsMap.get(key).FIELD_ID, entry[parentField.NAME]);
				}

				resultMap.set(inheritanceMap.get(key), map.get(key));
				const field = fieldsMap.get(inheritanceMap.get(key));
				// this.entry[field.NAME] = resultMap.get(inheritanceMap.get(key));
				const entryResult = {
					fieldName: field.NAME,
					value: resultMap.get(inheritanceMap.get(key)),
				};
				this.customSubject.next(entryResult);

				// if inherited values are relationship
				// needs to set data for form control
				if (
					field.DATA_TYPE.DISPLAY === 'Relationship' &&
					(field.RELATIONSHIP_TYPE === 'One to One' ||
						field.RELATIONSHIP_TYPE === 'Many to One')
				) {
					const event = {
						option: {
							value: resultMap.get(inheritanceMap.get(key)),
						},
					};

					const fieldControlName = field.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
				}
			});
		}
	}

	public transformNumbersField(
		value: number,
		format?: String,
		prefix?: String,
		suffix?: String
	): String {
		if (value === null || value === undefined) {
			return '';
		} else {
			let afterPoint;
			let isFloat = false;
			let valueInString = value.toString();
			if (valueInString.indexOf('.') > 0) {
				afterPoint = valueInString.substring(
					valueInString.indexOf('.'),
					valueInString.length
				);
				value = Math.floor(value);
				valueInString = value.toString();
				isFloat = true;
			}
			if (format === '##,##,###') {
				let lastThree = valueInString.substring(valueInString.length - 3);
				let otherNumbers = valueInString.substring(0, valueInString.length - 3);
				if (otherNumbers != '') lastThree = ',' + lastThree;
				let result =
					otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ',') + lastThree;
				if (isFloat) {
					result = result + afterPoint;
				}
				result = prefix + ' ' + result + ' ' + suffix;
				return result;
			} else if (format === '#,###,###') {
				let result = valueInString.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
				if (isFloat) {
					result = result + afterPoint;
				}
				result = prefix + ' ' + result + ' ' + suffix;
				return result;
			} else {
				if (isFloat) {
					valueInString = valueInString + afterPoint;
				}
				if (prefix === null) {
					prefix = '';
				}
				if (suffix === null) {
					suffix = '';
				}
				valueInString = prefix + ' ' + valueInString + ' ' + suffix;
				return valueInString;
			}
		}
	}
}
