import {
	Component,
	OnInit,
	ViewChild,
	OnDestroy,
	Inject,
	Optional,
} from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { DataApiService } from '@ngdesk/data-api';
import { CacheService } from '@src/app/cache.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ListLayout } from '@src/app/models/list-layout';
import { RolesService } from '@src/app/roles/roles.service';
import { UsersService } from '@src/app/users/users.service';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { RenderDetailDataService } from '@src/app/render-layout/render-detail-new/render-detail-data.service';
import {
	animate,
	query,
	state,
	style,
	transition,
	trigger,
} from '@angular/animations';

// import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';

// import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
// import { EditModuleDialogComponent } from '@src/app/dialogs/edit-module-dialog/edit-module-dialog.component';
// import { InviteUsersDialogComponent } from '@src/app/dialogs/invite-users-dialog/invite-users-dialog.component';
import { RenderListLayoutService } from './render-list-layout.service';
import { SortedListLayoutEntryService } from './sorted-list-layout-entry.service';
import { CustomModulesService } from '../render-detail-new/custom-modules.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { MatDialogHelper } from '../dialog-snackbar-helper/matdialog-helper';
import { RenderListHelper } from '../render-list-helper/render-list-helper.tns';
import {
	LoadOnDemandListViewEventData,
	RadListView,
} from 'nativescript-ui-listview';
import { RenderDetailNewComponent } from '../render-detail-new/render-detail-new.component';
import { RenderDetailHelper } from '../render-detail-helper/render-detail-helper';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { SearchBar } from '@nativescript/core/ui/search-bar';
import { RenderLayoutService } from '../render-layout.service';
@Component({
	selector: 'app-render-list-layout-new',
	templateUrl: './render-list-layout-new.component.html',
	styleUrls: ['./render-list-layout-new.component.css'],
	providers: [RenderListHelper, RenderDetailHelper, MatDialogHelper],
	animations: [
		trigger('openClose', [
			state(
				'open',
				style({
					width: '170px',
					opacity: 1,
				})
			),
			state(
				'closed',
				style({
					width: '0px',
					opacity: 0,
				})
			),
			transition('open => closed', [animate('0.1s')]),
			transition('closed => open', [animate('0.1s')]),
		]),
		trigger('showHideSearchBar', [
			state(
				'open',
				style({
					height: 'auto',
					opacity: 1,
					//display: 'block',
					//marginBottom: '10px',
				})
			),
			state(
				'closed',
				style({
					//display: 'none',
					height: '0px',
					opacity: 0,
					//margin: '0px',
				})
			),
			transition('open => closed', [animate('0.1s')]),
			transition('closed => open', [animate('0.1s')]),
		]),
	],
})
export class RenderListLayoutNewComponent implements OnInit, OnDestroy {
	private navigationSubscription: Subscription;
	public moduleId: string;
	public module: any;
	private filteredLayouts: ListLayout[] = [];
	private showListLayouts = false;
	private editAccess = false;
	public isModuleAllowed = true;
	private viewAccess = true;
	private deleteAccess = true;
	private layoutMenuButtonShow = false;
	private pageTitle: string = '';
	private currentListLayout: any;
	private listlayoutName = '';
	public isOpen = false;
	private roleName: string = '';
	private recordName: string = '';
	private specialDataTypes = [
		'Relationship',
		'Date/Time',
		'Date',
		'Time',
		'Picklist',
		'Aggregate',
		'Phone',
		'Number',
		'Currency',
		'Formula',
		'Derived',
	];
	public listLayoutExists = true;
	private fieldMap = [];
	private savedSearchFields: any = [];
	private fields: any;
	private allModules: any = [];
	public allRoles = [];
	public mobileEntries = [];
	public dialogs: any;
	public isloadingMobileData = true;
	public mobileDisplayTitleField: any;
	public mobileSubTitleField: any;
	public OnLoadDemandPageIndex = 0;
	public showSearchBar = false;
	public includeConditions = true;
	@ViewChild('mergeEntriesSidenav') public mergeEntriesSidenav: MatSidenav;

	private updateDataSubscription: Subscription;

	constructor(
		@Optional() @Inject(MAT_DIALOG_DATA) public modalData: any,
		private route: ActivatedRoute,
		private router: Router,
		private cacheService: CacheService,
		public customTableService: CustomTableService,
		private customModulesService: CustomModulesService,
		private usersService: UsersService,
		private rolesService: RolesService,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private renderDetailDataSerice: RenderDetailDataService,
		// private dialog: MatDialog,
		private dataService: DataApiService,
		private renderListLayoutService: RenderListLayoutService,
		public customModuleService: CustomModulesService,
		private modulesService: ModulesService,
		public dialogHelper: MatDialogHelper,
		public renderListHelper: RenderListHelper,
		private sortedListLayoutEntryService: SortedListLayoutEntryService,
		public renderDetailHelper: RenderDetailHelper,
		private renderLayoutService: RenderLayoutService
	) {
		this.dialogs = this.dialogHelper.dialogs;
	}

	public ngOnInit() {
		this.initializeComponent();
		this.navigationSubscription = this.router.events.subscribe((event) => {
			if (event instanceof NavigationEnd) {
				this.initializeComponent();
			}
		});
		this.reloadDataOnUpdate();
		this.parseMethod();
	}

	public ngOnDestroy() {
		if (this.navigationSubscription) {
			this.navigationSubscription.unsubscribe();
		}

		if (this.updateDataSubscription) {
			this.updateDataSubscription.unsubscribe();
		}
	}

	private reloadDataOnUpdate() {
		this.updateDataSubscription = this.cacheService.entryUpdated.subscribe(
			(updated) => {
				if (
					updated.STATUS &&
					updated.MODULE_ID === this.moduleId &&
					updated.TYPE !== 'DISCUSSION'
				) {
					this.loadDefaultListLayout();
				}
			}
		);
	}

	private initializeComponent() {
		this.listLayoutExists = true;
		this.moduleId = this.route.snapshot.params.moduleId;
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 20;
		this.customTableService.isLoading = true;

		// Fetch all roles to filter the role entry.
		this.cacheService.getRoles().subscribe((role) => {
			role.filter((rol) => {
				if (rol.NAME === 'Customers') {
					rol['NAME'] = 'Customer';
				}
			});
			this.allRoles = role;
		});

		this.modulesService.getModules().subscribe((modulesResponse: any) => {
			this.allModules = modulesResponse.MODULES;
			// check if module is up to date in cache
			// if not, make the api call manually
			this.checkPermission();
			this.loadDefaultListLayout();
		});
	}

	public loadDefaultListLayout() {
		this.cacheService.getModule(this.moduleId).subscribe((modulesResponse) => {
			this.module = modulesResponse;
			this.fields = modulesResponse['FIELDS'];
			this.recordName = modulesResponse.PLURAL_NAME.toLowerCase();
			let defaultListLayout;
			let searchString = '';
			if (typeof window !== 'undefined') {
				defaultListLayout = this.module.LIST_LAYOUTS.find(
					(layout) =>
						layout.ROLE === this.usersService.user.ROLE && layout.IS_DEFAULT
				);
				searchString = this.renderListLayoutService.convertSearchString(
					this.renderListLayoutService.getSearchQuey(this.moduleId)
				);
			} else {
				defaultListLayout = this.renderListHelper.defaultListLayout(
					this.module
				);
				const searchQuey = this.renderListHelper.getSearchQuey(this.moduleId);
				searchString =
					this.renderListLayoutService.convertSearchString(searchQuey);
			}
			if (defaultListLayout) {
				this.pageTitle = defaultListLayout.NAME;
				this.setShowListLayouts();
				this.currentListLayout = defaultListLayout;
				this.getListLayoutEntries(
					this.moduleId,
					this.currentListLayout,
					searchString,
					true
				);
			} else {
				this.listLayoutExists = false;
				this.customTableService.isLoading = false;
				this.isloadingMobileData = false;
			}
		});
	}

	public onPageChange(event) {
		if (
			this.savedSearchFields.length > 0 &&
			this.savedSearchFields[this.savedSearchFields.length - 1]['TYPE'] !==
				'field'
		) {
			if (event.pageIndex !== undefined) {
				this.customTableService.pageIndex = event.pageIndex;
				this.customTableService.pageSize = event.pageSize;
			}
			if (event.active !== undefined) {
				const sortField = this.module.FIELDS.find(
					(field) => field.DISPLAY_LABEL === event.active
				);
				this.customTableService.sortBy = sortField.NAME;
				this.customTableService.sortOrder = event.direction;
			}
			let searchString = this.renderListLayoutService.convertSearchString(
				this.savedSearchFields
			);
			this.getListLayoutEntries(
				this.moduleId,
				this.currentListLayout,
				searchString,
				true
			);
		} else {
			this.getListLayoutEntries(
				this.moduleId,
				this.currentListLayout,
				'',
				true
			);
		}
	}

	public setCustomTableFields(listlayout) {
		this.customTableService.columnsHeaders = [];
		this.customTableService.columnsHeadersObj = [];

		if (listlayout) {
			this.listlayoutName = listlayout.NAME;
			// to view the checkbox column
			this.customTableService.columnsHeaders.push('select');
			let fields;
			if (typeof window !== 'undefined') {
				fields = listlayout.COLUMN_SHOW.FIELDS;
			} else {
				fields = listlayout.FIELDS;
			}

			fields.forEach((layoutField) => {
				// specify the sorting column
				if (
					layoutField === listlayout.ORDER_BY.COLUMN ||
					typeof window !== 'undefined'
				) {
					const field = this.module.FIELDS.find(
						(selectedField) =>
							selectedField.FIELD_ID === listlayout.ORDER_BY.COLUMN
					);
					this.customTableService.activeSort = {
						ORDER_BY: listlayout.ORDER_BY.ORDER.toLowerCase(),
						SORT_BY: field.DISPLAY_LABEL,
						NAME: field.NAME,
					};
					this.customTableService.sort =
						this.customTableService.activeSort.ORDER_BY;
				} else {
					// mobile
					this.mobileDisplayTitleField = this.module.FIELDS.find(
						(selectedField) => selectedField.FIELD_ID === listlayout.FIELDS[0]
					);

					this.mobileSubTitleField = this.module.FIELDS.find(
						(selectedField) => selectedField.FIELD_ID === listlayout.FIELDS[1]
					);

					this.customTableService.pageSize = 20;
					const defaultField = this.module.FIELDS.find(
						(field) => field.FIELD_ID === listlayout.ORDER_BY.COLUMN
					);
					this.customTableService.sortBy = defaultField.NAME;
					this.customTableService.sortOrder =
						listlayout.ORDER_BY.ORDER.toLowerCase();
				}
				this.setCustomTableColumnHeaders(layoutField);
			});
		}
	}

	public onClickRelationshipField(value, fieldName, event) {
		event.stopPropagation();
		const field = this.module.FIELDS.find((field) => field.NAME === fieldName);
		if (field) {
			const relatedModuleId = field.MODULE;
			this.router.navigate([`render/${relatedModuleId}/edit/${value.DATA_ID}`]);
		}
	}

	public setCustomTableColumnHeaders(layoutField) {
		if (layoutField.indexOf('.') !== -1) {
			let headersObject = {
				DISPLAY: '',
				NAME: '',
				DATA_TYPE: 'Derived',
				BACKEND_TYPE: 'String',
			};
			headersObject = this.getNestedFields(
				layoutField,
				this.moduleId,
				headersObject
			);

			this.customTableService.columnsHeaders.push(headersObject.DISPLAY);
			this.customTableService.columnsHeadersObj.push(headersObject);

			return;
		}
		const field = this.module.FIELDS.find(
			(selectedField) => selectedField.FIELD_ID === layoutField
		);
		const headersObject = {
			DISPLAY: field.DISPLAY_LABEL,
			NAME: field.NAME,
			DATA_TYPE: field.DATA_TYPE.DISPLAY,
			BACKEND_TYPE: field.DATA_TYPE.BACKEND,
		};
		if (field.PICKLIST_VALUES) {
			headersObject['PICKLIST_VALUES'] = field.PICKLIST_VALUES;
		}
		if (field.AGGREGATION_FIELD && field.AGGREGATION_RELATED_FIELD) {
			headersObject['AGGREGATION_RELATED_FIELD'] =
				field.AGGREGATION_RELATED_FIELD;
			headersObject['AGGREGATION_FIELD'] = field.AGGREGATION_FIELD;
		}
		if (
			field.DATA_TYPE.BACKEND === 'Integer' ||
			field.DATA_TYPE.BACKEND === 'Float' ||
			field.DATA_TYPE.BACKEND === 'Double' ||
			field.DATA_TYPE.DISPLAY === 'Formula' ||
			field.DATA_TYPE.DISPLAY === 'Aggregate'
		) {
			if (field.hasOwnProperty('SUFFIX') && field.SUFFIX !== null) {
				headersObject['SUFFIX'] = field.SUFFIX;
			} else {
				headersObject['SUFFIX'] = '';
			}
			if (field.hasOwnProperty('PREFIX') && field.PREFIX !== null) {
				headersObject['PREFIX'] = field.PREFIX;
			} else {
				headersObject['PREFIX'] = '';
			}
			if (
				field.hasOwnProperty('NUMERIC_FORMAT') &&
				field.NUMERIC_FORMAT !== null
			) {
				headersObject['NUMERIC_FORMAT'] = field.NUMERIC_FORMAT;
			} else {
				headersObject['NUMERIC_FORMAT'] = 'None';
			}
		}

		this.customTableService.columnsHeaders.push(field.DISPLAY_LABEL);
		this.customTableService.columnsHeadersObj.push(headersObject);
	}

	public getListLayoutEntries(
		moduleId,
		listLayout,
		searchString,
		pageIndexSet
	) {
		const listLayoutId = listLayout ? listLayout.LAYOUT_ID : null;
		let fieldsToShow;
		if (typeof window !== 'undefined') {
			fieldsToShow = listLayout.COLUMN_SHOW.FIELDS;
		} else {
			fieldsToShow = listLayout.FIELDS;
			const defaultField = this.module.FIELDS.find(
				(field) => field.FIELD_ID === listLayout.ORDER_BY.COLUMN
			);
			this.customTableService.sortBy = defaultField.NAME;
			this.customTableService.sortOrder =
				listLayout.ORDER_BY.ORDER.toLowerCase();
		}
		let fieldsQuery = 'DATA_ID: _id' + '\n';
		fieldsToShow.forEach((fieldId) => {
			if (fieldId.indexOf('.') !== -1) {
				let output = { NAME: '', DISPLAY: '' };
				output = this.getNestedFields(fieldId, this.moduleId, output);
				let name = output.NAME.replace(/\./g, '{');
				for (let i = output.NAME.split('.').length; i > 1; i--) {
					name = name + '}';
				}
				fieldsQuery = fieldsQuery + name + '\n';
				return;
			}
			const moduleField = this.module.FIELDS.find(
				(field) => field.FIELD_ID === fieldId
			);
			if (moduleField.NAME === 'CHANNEL') {
				fieldsQuery += `CHANNEL {
					name
				}`;
			} else {
				if (moduleField.DATA_TYPE.DISPLAY === 'Relationship') {
					const relatedModule = this.allModules.find(
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
						`${moduleField.NAME} {
						COUNTRY_CODE 
						DIAL_CODE
						PHONE_NUMBER
						COUNTRY_FLAG
					}` + '\n';
				} else {
					fieldsQuery += moduleField.NAME + '\n';
				}
			}
		});

		// get entries for layout
		if (pageIndexSet) {
			this.setCustomTableFields(listLayout);
			const moduleName = this.module.NAME.replace(/\s+/g, '_');

			if (this.customTableService.sortBy === undefined) {
				const defaultField = this.module.FIELDS.find(
					(field) => field.FIELD_ID === listLayout.ORDER_BY.COLUMN
				);
				this.customTableService.sortBy = defaultField.NAME;
				this.customTableService.sortOrder =
					listLayout.ORDER_BY.ORDER.toLowerCase();
			}
			let query = `{
				DATA: get${moduleName} (moduleId: "${moduleId}", layoutId: "${listLayoutId}", pageNumber: ${this.customTableService.pageIndex}, 
				pageSize: ${this.customTableService.pageSize}, sortBy: "${this.customTableService.sortBy}", orderBy: "${this.customTableService.sortOrder}") {
					${fieldsQuery}
				}
				TOTAL_RECORDS: count(moduleId: "${moduleId}", layoutId: "${listLayoutId}")
			}`;
			if (searchString && searchString !== '' && searchString !== null) {
				query = `{
					DATA: get${moduleName} (moduleId: "${moduleId}", layoutId: "${listLayoutId}", pageNumber: ${this.customTableService.pageIndex}, 
					pageSize: ${this.customTableService.pageSize}, sortBy: "${this.customTableService.sortBy}", orderBy: "${this.customTableService.sortOrder}", search: "${searchString}",includeConditions: ${this.includeConditions}) {
						${fieldsQuery}
					}
					TOTAL_RECORDS: count(moduleId: "${moduleId}", layoutId: "${listLayoutId}", search: "${searchString}",includeConditions: ${this.includeConditions})
				}`;
			}
			this.renderListLayoutService.getListLayoutEntries(query).subscribe(
				(entriesReponse: any) => {
					entriesReponse = this.getRoleName(entriesReponse);
					this.sortedListLayoutEntryService.sortedEntries = entriesReponse.DATA;
					this.customTableService.isLoading = false;
					this.isloadingMobileData = false;
					this.setCustomTableFields(listLayout);
					this.customTableService.setTableDataSource(
						entriesReponse.DATA,
						entriesReponse.TOTAL_RECORDS
					);
					this.mobileEntries = entriesReponse.DATA;
				},
				(error: any) => {
					this.customTableService.isLoading = false;
					this.isloadingMobileData = false;
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		} else {
			this.setCustomTableFields(listLayout);
			const moduleName = this.module.NAME.replace(/\s+/g, '_');
			const query = `{
				DATA: get${moduleName} (moduleId: "${moduleId}", layoutId: "${listLayoutId}", pageNumber: 0, 
				pageSize: 20) {
					${fieldsQuery}
				}
				TOTAL_RECORDS: count(moduleId: "${moduleId}", layoutId: "${listLayoutId}")
			}`;
			this.renderListLayoutService.getListLayoutEntries(query).subscribe(
				(entriesReponse: any) => {
					this.customTableService.isLoading = false;
					this.isloadingMobileData = false;
					this.customTableService.pageIndex = 0;
					this.customTableService.pageSize = 20;
					if (this.customTableService.sortBy === undefined) {
						const defaultField = this.module.FIELDS.find(
							(field) => field.FIELD_ID === listLayout.ORDER_BY.COLUMN
						);
						this.customTableService.sortBy = defaultField.NAME;
						this.customTableService.sortOrder =
							listLayout.ORDER_BY.ORDER.toLowerCase();
					}
					entriesReponse = this.getRoleName(entriesReponse);
					this.sortedListLayoutEntryService.sortedEntries = entriesReponse.DATA;
					this.customTableService.setTableDataSource(
						entriesReponse.DATA,
						entriesReponse.TOTAL_RECORDS
					);
					this.mobileEntries = entriesReponse.DATA;
				},
				(error: any) => {
					this.customTableService.isLoading = false;
					this.isloadingMobileData = false;
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		}
	}

	public includeConditionsChanged(value) {
		this.includeConditions = value;
		if (this.savedSearchFields.length > 0) {
			this.customTableService.pageIndex = 0;
			let searchString = this.renderListLayoutService.convertSearchString(
				this.savedSearchFields
			);
			this.getListLayoutEntries(
				this.moduleId,
				this.currentListLayout,
				searchString,
				true
			);
		}
	}
	public parseMethod() {
		let pair;
		let fieldQuery = '';
		let temp;
		let value0;
		let query;
		const string = 'EMAIL_ADDRESS CONTACT.FULL_NAME CONTACT.FIRST_NAME';
		if (string.length > 0) {
			const values = string.split(' ');
			for (let i = 0; i < values.length; i++) {
				if (values[i] !== undefined) {
					value0 = values[0];
					const value = values[i];
					if (value.indexOf('.') !== -1) {
						pair = value.split('.');
						if (pair !== undefined) {
							fieldQuery += ' ' + pair[1];
						}
					}
					if (pair !== undefined && i === values.length - 1) {
						temp = `${pair[0]}` + '{' + fieldQuery + '}';
					}
				}
			}
			if (temp !== undefined) {
				query = value0 + ' ' + temp;
			} else {
				query = value0;
			}
			console.log(query);
		}
	}

	public formatChronometerFields(entriesReponse) {
		let chronometer = this.module.FIELDS.find(
			(result) => result.DATA_TYPE.DISPLAY === 'Chronometer'
		);
		if (chronometer) {
			for (let i = 0; i < entriesReponse.DATA.length; i++) {
				let fieldName = chronometer['NAME'];
				let fieldValue = entriesReponse.DATA[i][fieldName];

				if (fieldValue !== undefined && fieldValue !== null) {
					entriesReponse.DATA[i][fieldName] =
						this.renderLayoutService.chronometerFormatTransform(fieldValue, '');
				}
			}
		}

		return entriesReponse;
	}
	// Set the role name to the list layout
	public getRoleName(entriesReponse) {
		// this.formatChronometerFields(entriesReponse);
		const entry = entriesReponse;
		for (let i = 0; i < entriesReponse.DATA.length; i++) {
			if (entriesReponse.DATA[i].ROLE !== undefined) {
				const roleId = entriesReponse.DATA[i].ROLE;
				const role = this.allRoles.find((allRole) => {
					return allRole.ROLE_ID === roleId;
				});
				if (role !== undefined) {
					entriesReponse.DATA[i].ROLE = role.NAME;
				}
			}
		}

		return entriesReponse;
	}

	public setShowListLayouts() {
		const listLayouts = this.module.LIST_LAYOUTS;

		this.filteredLayouts = [];
		for (const listLayout of listLayouts) {
			if (listLayout.ROLE === this.usersService.user.ROLE) {
				this.filteredLayouts.push(listLayout);
			}
		}
		if (this.filteredLayouts.length >= 1) {
			this.showListLayouts = true;
			this.layoutMenuButtonShow = true;
		} else {
			this.showListLayouts = false;
			this.layoutMenuButtonShow = true;
		}
	}

	public getEvent(visible) {
		return visible ? 'auto' : 'none';
	}

	public checkPermission() {
		const moduleId = this.moduleId;
		this.rolesService
			.getRole(this.usersService.user.ROLE)
			.subscribe((roleResponse: any) => {
				const role = roleResponse;
				this.roleName = roleResponse.NAME;
				if (role.NAME === 'SystemAdmin') {
					this.isModuleAllowed = true;
					this.editAccess = true;
					this.viewAccess = true;
					this.deleteAccess = true;
				} else {
					const permissions = role.PERMISSIONS;
					permissions.find((modulePermissions) => {
						if (modulePermissions.MODULE === moduleId) {
							if (modulePermissions.MODULE_PERMISSIONS.ACCESS === 'Enabled') {
								this.isModuleAllowed = true;
								if (
									modulePermissions.MODULE_PERMISSIONS.EDIT === 'All' ||
									modulePermissions.MODULE_PERMISSIONS.EDIT === 'Not Set'
								) {
									this.editAccess = true;
								}
								if (
									modulePermissions.MODULE_PERMISSIONS.VIEW === 'All' ||
									modulePermissions.MODULE_PERMISSIONS.VIEW === 'Not Set'
								) {
									this.viewAccess = true;
								}
								if (
									modulePermissions.MODULE_PERMISSIONS.DELETE === 'All' ||
									modulePermissions.MODULE_PERMISSIONS.DELETE === 'Not Set'
								) {
									this.deleteAccess = true;
								}
							} else {
								this.isModuleAllowed = false;
							}
						}
					});
				}
			});
	}

	public setListLayout(layout) {
		this.customTableService.isLoading = true;
		this.isloadingMobileData = true;
		this.currentListLayout = layout;
		this.pageTitle = this.currentListLayout.NAME;
		this.cacheService.getModule(this.moduleId).subscribe(
			(modulesResponse: any) => {
				this.module = modulesResponse;
				this.getListLayoutEntries(
					this.moduleId,
					this.currentListLayout,
					'',
					false
				);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
				this.customTableService.isLoading = false;
				this.isloadingMobileData = false;
			}
		);
	}

	public setListLayoutTableState(state) {
		this.showListLayouts = !state;
	}

	public rowClicked(entry) {
		if (this.editAccess && this.viewAccess) {
			this.router.navigate([
				`render/${this.route.snapshot.params.moduleId}/edit/${entry.DATA_ID}`,
			]);
		} else if (this.viewAccess && !this.editAccess) {
			this.router.navigate([
				`render/${this.route.snapshot.params.moduleId}/detail/${entry.DATA_ID}`,
			]);
		} else if (!this.editAccess && !this.viewAccess) {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('VIEW_NOT_ALLOWED'),
			});
		}
	}

	public updateEntries(selectedEntries) {
		// open dialog
		const body = { ENTRY_IDS: [] };
		body.ENTRY_IDS = selectedEntries.map((entry) => entry.DATA_ID);

		const dialogRef = this.dialogHelper.updateEntries(
			body,
			this.moduleId,
			this.module['FIELDS']
		);
		dialogRef.afterClosed().subscribe((result) => {
			this.getListLayoutEntries(
				this.moduleId,
				this.currentListLayout,
				'',
				false
			);
		});
	}

	public onEntriesMerged() {
		this.mergeEntriesSidenav.toggle();
		this.getListLayoutEntries(this.moduleId, this.currentListLayout, '', false);
	}

	public deleteEntries(selectedEntries) {
		let name = '';
		// if one entry, use translation with 'this' for dialog title
		// else, use translation with 'these' for dialog title
		this.translateService
			.get('ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS', {
				value: this.module['NAME'].toLowerCase(),
			})
			.subscribe((res) => {
				name = res;
			});
		if (selectedEntries.length > 1) {
			this.translateService
				.get('ARE_YOU_SURE_YOU_WANT_TO_DELETE_THESE', {
					values: this.module['NAME'].toLowerCase(),
				})
				.subscribe((res) => {
					name = res;
				});
		}

		const dialogRef = this.dialogHelper.deleteEntries(name);
		// const dialogRef = this.dialog.open(ConfirmDialogComponent, {
		// 	data: {
		// 		message: name,
		// 		buttonText: this.translateService.instant('DELETE'),
		// 		closeDialog: this.translateService.instant('CANCEL'),
		// 		action: this.translateService.instant('DELETE'),
		// 		executebuttonColor: 'warn',
		// 	},
		// });

		// make dataservice call to delete entries after dialog closes
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DELETE')) {
				const entryIds = selectedEntries.map((entry) => entry.DATA_ID);

				this.dataService.deleteData(this.moduleId, entryIds, false).subscribe(
					(entriesResponse: any) => {
						this.getListLayoutEntries(
							this.moduleId,
							this.currentListLayout,
							'',
							false
						);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			}
		});
	}

	public newEntry(): void {
		this.router.navigate([`render/${this.moduleId}/create/new`]);
	}

	public inviteUsers() {
		const dialogRef = this.dialogHelper.inviteUsers();

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			// reload the users table after dialog closes
			this.getListLayoutEntries(
				this.moduleId,
				this.currentListLayout,
				'',
				false
			);
		});
	}

	public cloneEntry(selectedEntries, module) {
		let dialogMessage = '';
		if (selectedEntries.length === 1) {
			this.translateService
				.get('DO_YOU_WANT_TO_CLONE_THIS', {
					value: this.module['NAME'].toLowerCase(),
				})
				.subscribe((res) => {
					dialogMessage = res;
				});
		}

		const dialogRef = this.dialogHelper.cloneEntry(dialogMessage);

		// EVENT AFTER DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('OK')) {
				selectedEntries.forEach((selectedEntry) => {
					const dataId = selectedEntry.DATA_ID;

					let dialogId = `render-detail-dialog_0`;
					if (this.renderDetailHelper.dialog.openDialogs.length > 0) {
						dialogId = `render-detail-dialog_${this.renderDetailHelper.dialog.openDialogs.length}`;
					}
					const dialogs = this.renderDetailHelper.dialog.openDialogs.length;
					const renderDetail = this.renderDetailHelper.dialog.open(
						RenderDetailNewComponent,
						{
							width: '1024px',
							height: '768px',
							id: dialogId,
							data: {
								MODULE_ID: module.MODULE_ID,
								DATA_ID: dataId,
							},
						}
					);
				});
			}
		});
	}

	public updatePicklist(entry, element, value) {
		entry[element.NAME] = value;
		// removing the chronometer and discussion fields
		this.module.FIELDS.forEach((field) => {
			if (
				field.DATA_TYPE.DISPLAY === 'Discussion' ||
				field.DATA_TYPE.DISPLAY === 'Chronometer'
			) {
				const fieldName = field.NAME;
				delete entry[fieldName];
			}
		});
		this.dataService
			.putModuleEntry(this.module['MODULE_ID'], entry, false)
			.subscribe(
				(response) => {},
				(error) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}
	public searchValuesChange(searchParams: any[]) {
		//get entries on search
		this.savedSearchFields = searchParams;
		if (
			(searchParams.length > 0 &&
				searchParams[searchParams.length - 1]['TYPE'] !== 'field') ||
			searchParams.length === 0
		) {
			this.customTableService.pageIndex = 0;
			let searchString = this.renderListLayoutService.convertSearchString(
				this.savedSearchFields
			);
			this.getListLayoutEntries(
				this.moduleId,
				this.currentListLayout,
				searchString,
				true
			);
		}
	}

	public openMobileDropDown() {
		let layoutNames: any = [];
		this.module.LIST_MOBILE_LAYOUTS.forEach((layout) => {
			if (layout.ROLE == this.usersService.user.ROLE) {
				layoutNames.push(layout.NAME);
			}
		});

		this.dialogs
			.action({
				title: 'Layouts',
				cancelButtonText: 'Cancel',
				actions: layoutNames,
			})
			.then((result) => {
				if (result !== 'Cancel') {
					this.pageTitle = result;
					const listlayout = this.module.LIST_MOBILE_LAYOUTS.find(
						(layout) =>
							layout.NAME == result &&
							layout.ROLE === this.usersService.user.ROLE
					);
					this.mobileEntries = [];
					this.isloadingMobileData = true;
					this.currentListLayout = listlayout;
					this.getListLayoutEntries(
						this.moduleId,
						this.currentListLayout,
						'',
						true
					);
				}
			});
	}

	public onMobileLoadMoreItemsRequested(args: LoadOnDemandListViewEventData) {
		const that = this;
		const listView: RadListView = args.object;
		if (this.mobileEntries.length >= 20) {
			this.OnLoadDemandPageIndex = this.OnLoadDemandPageIndex + 1;
			setTimeout(function () {
				const moduleName = that.module.NAME.replace(/\s+/g, '_');
				let fieldsQuery = 'DATA_ID: _id' + '\n';
				let fieldsToShow;

				fieldsToShow = that.currentListLayout.FIELDS;

				fieldsToShow.forEach((fieldId) => {
					const moduleField = that.module.FIELDS.find(
						(field) => field.FIELD_ID === fieldId
					);
					if (moduleField.DATA_TYPE.DISPLAY === 'Relationship') {
						const relatedModule = that.allModules.find(
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
					} else {
						fieldsQuery += moduleField.NAME + '\n';
					}
				});
				let query = `{
					DATA: get${moduleName} (moduleId: "${that.moduleId}", layoutId: "${that.currentListLayout.LAYOUT_ID}", pageNumber: ${that.OnLoadDemandPageIndex}, 
					pageSize: ${that.customTableService.pageSize}, sortBy: "DATE_CREATED", orderBy: "desc") {
						${fieldsQuery}
					}
					TOTAL_RECORDS: count(moduleId: "${that.moduleId}", layoutId: "${that.currentListLayout.LAYOUT_ID}")
				}`;
				that.renderListLayoutService
					.getListLayoutEntries(query)
					.subscribe((entriesReponse: any) => {
						if (entriesReponse.DATA.length > 0) {
							entriesReponse.DATA.forEach((entry) => {
								that.mobileEntries.push(entry);
							});
							args.returnValue = true;
							listView.notifyLoadOnDemandFinished();
						}
					});
			}, 0);
		} else {
			args.returnValue = false;
			listView.notifyLoadOnDemandFinished(true);
		}
	}

	public searchForMobile(args) {
		const searchBar = args.object as SearchBar;
		if (searchBar.text && searchBar.text !== '' && searchBar.text !== null) {
			this.customTableService.pageIndex = 0;
			this.customTableService.pageSize = 20;
			this.getListLayoutEntries(
				this.moduleId,
				this.currentListLayout,
				searchBar.text,
				true
			);
		}
	}

	public onClearForMobile() {
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 20;
		this.getListLayoutEntries(this.moduleId, this.currentListLayout, '', true);
	}

	public getNestedFields(field, moduleId, output): any {
		if (field === null) {
			return output;
		} else if (field.indexOf('.') === -1) {
			const foundModule = this.allModules.find(
				(module) => module.MODULE_ID === moduleId
			);
			const foundField = foundModule.FIELDS.find(
				(moduleField) => moduleField.FIELD_ID === field
			);
			output.DISPLAY = output.DISPLAY + foundField.DISPLAY_LABEL;
			output.NAME = output.NAME + foundField.NAME;
			return this.getNestedFields(null, null, output);
		} else {
			const split = field.split('.');
			const fieldId = split.shift();
			const foundModule = this.allModules.find(
				(module) => module.MODULE_ID === moduleId
			);

			const currentField = foundModule.FIELDS.find(
				(fieldNested) => fieldId === fieldNested.FIELD_ID
			);
			output.DISPLAY = output.DISPLAY + currentField.DISPLAY_LABEL + '.';
			output.NAME = output.NAME + currentField.NAME + '.';
			return this.getNestedFields(split.join('.'), currentField.MODULE, output);
		}
	}

	public getDerivedValue(col, element): String {
		try {
			if (
				eval(`element.${col.NAME}`) === undefined ||
				eval(`element.${col.NAME}`) === null
			) {
				return '-';
			}
			return eval(`element.${col.NAME}`);
		} catch (e) {}
		return '-';
	}
}
