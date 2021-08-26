import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../custom-table/custom-table.service';
import { ModulesService } from '../../modules/modules.service';
import { UsersService } from '../../users/users.service';
import { DataApiService } from '@ngdesk/data-api';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CacheService } from '@src/app/cache.service';
import { MatSidenav } from '@angular/material/sidenav';
import { CompaniesService } from '../../companies/companies.service';
import { RolesService } from '../../company-settings/roles/roles-old.service';
import { Condition } from '../../models/condition';
import { ColumnShow, ListLayout, OrderBy } from '../../models/list-layout';
import { CustomModulesService } from '../../render-layout/render-detail-new/custom-modules.service';
import { FilePreviewOverlayRef } from '../../shared/file-preview-overlay/file-preview-overlay-ref';
import { FilePreviewOverlayService } from '../../shared/file-preview-overlay/file-preview-overlay.service';
import { WalkthroughService } from '../../walkthrough/walkthrough.service';
import { RenderListLayoutService } from './../../render-layout/render-list-layout-new/render-list-layout.service';
import { SortedListLayoutEntryService } from '@src/app/render-layout/render-list-layout-new/sorted-list-layout-entry.service';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
import { result } from 'lodash';

@Component({
	selector: 'app-one-to-many-dialog',
	templateUrl: './one-to-many-dialog.component.html',
	styleUrls: ['./one-to-many-dialog.component.scss'],
})
export class OneToManyDialogComponent implements OnInit {
	private listlayoutName = '';
	public errorMessage: String = '';
	public moduleId = '';
	public module;
	public moduleName;
	public isOpen = false;
	public roleName = '';
	public roles;
	public role;
	public loading = true;
	public discussionExists = false;
	public editAccess = false;
	public viewAccess = false;
	public mergeAccess = false;
	public deleteAccess = false;
	public pageTitle = '';
	public isModuleAllowed = false;
	public recordName: string;
	public layoutButtonShow: boolean;
	public filteredLayouts: ListLayout[] = [];
	public showListLayouts: boolean;
	public name: string;
	private currentListLayout: ListLayout = new ListLayout(
		'',
		'',
		'',
		'',
		new OrderBy('', ''),
		new ColumnShow([]),
		[new Condition('', '', '', '')],
		false
	);
	private navigationSubscription: Subscription;
	private adminSignupSubscription: Subscription;
	public numberOfListLayout: number;
	public postEntrySubscription;
	public dataSort;
	public hideMatTable = false;
	public savedFields: any[] = [];
	private fields;
	private listLayoutData = [];
	public isRelationShipFieldClicked = false;
	public isButtonClicked = false;
	public showMessage = false;
	public sidenav: MatSidenav;
	public fieldMap = [];
	public allModules = [];
	public manyToOneField = '';
	public entriesCount = 0;
	public relationshipFieldId = '';
	private savedSearchFields: any = [];
	constructor(
		public dialogRef: MatDialogRef<OneToManyDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private rolesService: RolesService,
		public customTableService: CustomTableService,
		private cacheService: CacheService,
		private modulesService: ModulesService,
		private router: Router,
		private route: ActivatedRoute,
		private usersService: UsersService,
		public fpos: FilePreviewOverlayService,
		private walkthroughService: WalkthroughService,
		private companiesService: CompaniesService,
		private _snackBar: MatSnackBar,
		private dataApiService: DataApiService,
		private renderListLayoutService: RenderListLayoutService,
		private userService: UsersService,
		private customModulesService: CustomModulesService,
		private sortedListLayoutEntryService: SortedListLayoutEntryService,
		private httpClient: HttpClient,
		private globals: AppGlobals
	) {}

	public ngOnInit() {
		this.customTableService.isLoading = true;
		this.customTableService.dataIds = [];
		const roleId = this.usersService.user.ROLE;
		this.moduleId = this.data.relmoduleId;
		this.customTableService.currentOneToManyDialog = this.data.fieldId;
		this.customTableService.dataIds = [];
		this.modulesService.getModules().subscribe((modulesResponse: any) => {
			this.allModules = modulesResponse.MODULES;

			this.cacheService
				.getModule(this.moduleId)
				.subscribe((modulesResponse) => {
					this.module = modulesResponse;
					this.moduleName = modulesResponse['NAME'];
					this.fields = modulesResponse['FIELDS'];
					this.recordName = modulesResponse.PLURAL_NAME.toLowerCase();
					const defaultListLayout = this.module.LIST_LAYOUTS.find(
						(layout) =>
							layout.ROLE === this.usersService.user.ROLE && layout.IS_DEFAULT
					);
					let searchString = '';
					searchString = this.renderListLayoutService.convertSearchString(
						this.renderListLayoutService.getSearchQuey(this.moduleId)
					);
					if (defaultListLayout) {
						this.pageTitle = defaultListLayout.NAME;
						this.setCustomTableFields(defaultListLayout);
						this.setShowListLayouts();
						this.currentListLayout = defaultListLayout;
						// TODO: Need to add filter
						this.getListLayoutEntries(
							this.moduleId,
							this.currentListLayout,
							searchString,
							true
						);
					}
				});
		});
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

	public close(action): void {
		let dataIds = [];
		let  result = [];
		if (action === 'cancel') {
			this.customTableService.dataIds = [];
			this.dialogRef.close(result);
		} else {
			if(this.customTableService.dataIds.length > 0){
				dataIds = this.customTableService.dataIds;
				dataIds.forEach(dataId => {
					const object = {
						'DATA_ID': dataId
					};
					result.push(object);
				});
				this.customTableService.dataIds = [];
				this.dialogRef.close(result);
			} else {
				this.customTableService.dataIds = [];
				this.dialogRef.close(result);
			}

		}
		
	}

	public setCustomTableFields(listlayout) {
		this.customTableService.columnsHeaders = [];
		this.customTableService.columnsHeadersObj = [];

		if (listlayout) {
			this.listlayoutName = listlayout.NAME;
			listlayout.COLUMN_SHOW.FIELDS.forEach((layoutField) => {
				// specify the sorting column
				if (layoutField === listlayout.ORDER_BY.COLUMN) {
					const field = this.module.FIELDS.find(
						(selectedField) => selectedField.FIELD_ID === layoutField
					);
					this.customTableService.activeSort = {
						ORDER_BY: listlayout.ORDER_BY.ORDER.toLowerCase(),
						SORT_BY: field.DISPLAY_LABEL,
						NAME: field.NAME,
					};

					this.customTableService.sortBy =
						this.customTableService.activeSort.NAME;
					this.customTableService.sortOrder =
						this.customTableService.activeSort.ORDER_BY;
				}
				this.setCustomTableColumnHeaders(layoutField);
			});
		}
		this.customTableService.columnsHeaders.push('Add Entry');
		const otm = {
			DISPLAY: 'Add Entry',
			NAME: 'Add Entry OTM',
			DATA_TYPE: null,
			FIELD_ID: null,
		};
		this.customTableService.columnsHeadersObj.push(otm);
	}

	public setCustomTableColumnHeaders(layoutField) {
		const field = this.module.FIELDS.find(
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

	public getListLayoutEntries(
		moduleId,
		listLayout,
		searchString,
		pageIndexSet
	) {
		this.customTableService.isLoading = true;
		const listLayoutId = listLayout.LAYOUT_ID;
		let fieldsToShow;
		if (typeof window !== 'undefined') {
			fieldsToShow = listLayout.COLUMN_SHOW.FIELDS;
			if (!this.customTableService.sortBy) {
				this.customTableService.sortBy = 'DATE_UPDATED';
				this.customTableService.sortOrder = 'desc';
			}
		} else {
			fieldsToShow = listLayout.FIELDS;
			this.customTableService.sortBy = 'DATE_CREATED';
			this.customTableService.sortOrder = 'desc';
		}
		let fieldsQuery = 'DATA_ID: _id' + '\n';
		fieldsToShow.forEach((fieldId) => {
			const moduleField = this.module.FIELDS.find(
				(field) => field.FIELD_ID === fieldId
			);
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
		const moduleName = this.module.NAME.replace(/\s+/g, '_');
		let query = '';

		// get entries for layout
		// if (pageIndexSet) {
		// 	this.setCustomTableFields(listLayout);
		// 	const moduleName = this.module.NAME.replace(/\s+/g, '_');
		// 	let query = `{
		// 		DATA: get${moduleName} (moduleId: "${moduleId}", layoutId: "${listLayoutId}", pageNumber: ${this.customTableService.pageIndex},
		// 		pageSize: ${this.customTableService.pageSize}, sortBy: "${this.customTableService.sortBy}", orderBy: "${this.customTableService.sortOrder}") {
		// 			${fieldsQuery}
		// 		}
		// 		TOTAL_RECORDS: count(moduleId: "${moduleId}", layoutId: "${listLayoutId}")
		// 	}`;
		if (searchString && searchString !== '' && searchString !== null) {
			query = `{
        DATA: get${moduleName}OneToManyUnmapped (moduleId: "${moduleId}", fieldId: "${this.data.relatedField}", pageNumber: ${this.customTableService.pageIndex}, 
        pageSize: ${this.customTableService.pageSize},search: "${searchString}", sortBy: "${this.customTableService.sortBy}", orderBy: "${this.customTableService.sortOrder}") {
          ${fieldsQuery}
        }
        TOTAL_RECORDS: getOneToManyUnmappedCountDataFetcher(moduleId: "${moduleId}", fieldId: "${this.data.relatedField}", search: "${searchString}")
      }`;
		} else {
			query = `{
      DATA: get${moduleName}OneToManyUnmapped (moduleId: "${moduleId}", fieldId: "${this.data.relatedField}", pageNumber: ${this.customTableService.pageIndex}, 
      pageSize: ${this.customTableService.pageSize}, sortBy: "${this.customTableService.sortBy}", orderBy: "${this.customTableService.sortOrder}") {
        ${fieldsQuery}
      }
      TOTAL_RECORDS: getOneToManyUnmappedCountDataFetcher(moduleId: "${moduleId}", fieldId: "${this.data.relatedField}")
    }`;
		}

		this.makeGraphQLCall(query).subscribe(
			(entriesReponse: any) => {
				this.sortedListLayoutEntryService.sortedEntries = entriesReponse.DATA;
				this.customTableService.isLoading = false;
				this.customTableService.setTableDataSource(
					entriesReponse.DATA,
					entriesReponse.TOTAL_RECORDS
				);
			},
			(error: any) => {
				this.customTableService.isLoading = false;
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);

		// } else {
		// 	this.setCustomTableFields(listLayout);
		// 	const moduleName = this.module.NAME.replace(/\s+/g, '_');
		// 	const query = `{
		// 		DATA: get${moduleName} (moduleId: "${moduleId}", layoutId: "${listLayoutId}", pageNumber: 0,
		// 		pageSize: 20) {
		// 			${fieldsQuery}
		// 		}
		// 		TOTAL_RECORDS: count(moduleId: "${moduleId}", layoutId: "${listLayoutId}")
		// 	}`;
		// 	this.renderListLayoutService.getListLayoutEntries(query).subscribe(
		// 		(entriesReponse: any) => {
		// 			this.customTableService.isLoading = false;
		// 			this.customTableService.pageIndex = 0;
		// 			this.customTableService.pageSize = 20;
		// 			this.sortedListLayoutEntryService.sortedEntries = entriesReponse.DATA;
		// 			this.customTableService.setTableDataSource(
		// 				entriesReponse.DATA,
		// 				entriesReponse.TOTAL_RECORDS
		// 			);

		// 		},
		// 		(error: any) => {
		// 			this.customTableService.isLoading = false;
		// 			this.bannerMessageService.errorNotifications.push({
		// 				message: error.error.ERROR,
		// 			});
		// 		}
		// 	);
		// }
	}

	public setShowListLayouts() {
		const listLayouts = this.module.LIST_LAYOUTS;

		this.filteredLayouts = [];
		for (const listLayout of listLayouts) {
			if (listLayout.ROLE === this.usersService.user.ROLE) {
				this.filteredLayouts.push(listLayout);
			}
		}
		if (this.filteredLayouts.length > 1) {
			this.showListLayouts = true;
		}
	}

	public onPageChange(event) {
		if (
			this.savedSearchFields.length > 0 &&
			this.savedSearchFields[this.savedSearchFields.length - 1]['TYPE'] !==
				'field'
		) {
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

	public makeGraphQLCall(query: string) {
		return this.httpClient.post(`${this.globals.graphqlUrl}`, query);
	}
}
