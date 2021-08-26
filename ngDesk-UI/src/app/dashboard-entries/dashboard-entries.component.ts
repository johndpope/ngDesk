import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
import { ModulesService } from '@src/app/modules/modules.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { UsersService } from '../users/users.service';
import { RenderListLayoutService } from '../render-layout/render-list-layout-new/render-list-layout.service';
import { ColumnShow, ListLayout, OrderBy } from '../models/list-layout';
import { Condition } from '../models/condition';
import { SortedListLayoutEntryService } from '@src/app/render-layout/render-list-layout-new/sorted-list-layout-entry.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { RolesService } from '@src/app/roles/roles.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
	selector: 'app-dashboard-entries',
	templateUrl: './dashboard-entries.component.html',
	styleUrls: ['./dashboard-entries.component.scss'],
})
export class DashboardEntriesComponent implements OnInit {
	public dashboardId: String;
	public widgetId: String;
	public modules = [];
	public dashboard = {};
	public widgets = [];
	public moduleId: String;
	public module;
	public value: String;
	public pageTitle = '';
	private listlayoutName = '';
	public filteredLayouts: ListLayout[] = [];
	public showListLayouts: boolean;
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
	public moduleName: String;
	private editAccess = false;
	public isModuleAllowed = true;
	private viewAccess = true;
	private deleteAccess = true;
	public roleName: String;

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private http: HttpClient,
		private globals: AppGlobals,
		private modulesService: ModulesService,
		public customTableService: CustomTableService,
		private usersService: UsersService,
		private renderListLayoutService: RenderListLayoutService,
		private sortedListLayoutEntryService: SortedListLayoutEntryService,
		private bannerMessageService: BannerMessageService,
		private rolesService: RolesService,
		private translateService: TranslateService
	) {}

	public ngOnInit() {
		this.customTableService.isLoading = true;
		const roleId = this.usersService.user.ROLE;
		this.dashboardId = this.route.snapshot.params['id'];
		this.widgetId = this.route.snapshot.params['widgetId'];
		this.value = this.route.snapshot.params['value'];
		this.modulesService.getModules().subscribe((moduleResponse: any) => {
			this.modules = moduleResponse['MODULES'];
			this.getDashboard(this.dashboardId).subscribe(
				(dashboardResponse: any) => {
					this.widgets = dashboardResponse['DATA'].widgets;
					const widget = this.widgets.find(
						(widgetFound) => widgetFound.widgetId === this.widgetId
					);
					this.moduleId = widget.moduleId;
					this.module = this.modules.find(
						(moduleFound) => moduleFound['MODULE_ID'] === this.moduleId
					);
					this.moduleName = this.module.NAME;
					const defaultListLayout = this.module.LIST_LAYOUTS.find(
						(layout) =>
							layout.ROLE === this.usersService.user.ROLE && layout.IS_DEFAULT
					);
					if (defaultListLayout) {
						this.pageTitle = defaultListLayout.NAME;
						this.setCustomTableFields(defaultListLayout);
						this.setShowListLayouts();
						this.currentListLayout = defaultListLayout;
						this.getDashboardWidgetEntries(
							this.dashboardId,
							this.widgetId,
							this.value,
							this.currentListLayout,
							true
						);
					}
					this.checkPermission();
				}
			);
		});
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
					this.customTableService.sort =
						this.customTableService.activeSort.ORDER_BY;
				}
				this.setCustomTableColumnHeaders(layoutField);
			});
		}
	}
	public setCustomTableColumnHeaders(layoutField) {
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
			field.DATA_TYPE.BACKEND === 'Double'
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

	public getFieldsForModule(module, widget) {
		let showFields = '_id';
		this.customTableService.sortBy = widget.orderBy.column.name;
		this.customTableService.sortOrder = widget.orderBy.order;
		module.FIELDS.forEach((moduleField) => {
			showFields += ' ' + moduleField.NAME;
		});
		return showFields;
	}

	public getDashboardWidgetEntries(
		dashboardId,
		widgetId,
		value,
		listLayout,
		pageIndexSet
	) {
		const listLayoutId = listLayout.LAYOUT_ID;
		let fieldsToShow;
		if (typeof window !== 'undefined') {
			fieldsToShow = listLayout.COLUMN_SHOW.FIELDS;
		} else {
			fieldsToShow = listLayout.FIELDS;
			this.customTableService.sortBy = 'DATE_UPDATED';
			this.customTableService.sortOrder = 'desc';
		}
		let fieldsQuery = 'DATA_ID: _id' + '\n';
		fieldsToShow.forEach((fieldId) => {
			const moduleField = this.module.FIELDS.find(
				(field) => field.FIELD_ID === fieldId
			);
			if (moduleField.DATA_TYPE.DISPLAY === 'Relationship') {
				const relatedModule = this.modules.find(
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
			} else if (moduleField.NAME === 'CHANNEL') {
				fieldsQuery += `CHANNEL {
					name
				}`;
			} else {
				fieldsQuery += moduleField.NAME + '\n';
			}
		});

		if (pageIndexSet) {
			this.setCustomTableFields(listLayout);
			const moduleName = this.module.NAME.replaceAll('\\s+', '_');
			const query = `{
                DATA: get${moduleName}WidgetEntries(dasboardId: "${dashboardId}",widgetId: "${widgetId}",value: "${value}",pageNumber: ${this.customTableService.pageIndex},
                      pageSize: ${this.customTableService.pageSize},sortBy: "${this.customTableService.sortBy}",orderBy: "${this.customTableService.sortOrder}"){
                       ${fieldsQuery}
                
                     }
                 TOTAL_RECORDS: getWidgetEntriesCount(dasboardId: "${dashboardId}",widgetId: "${widgetId}", value: "${value}")
                          
                     }`;
			this.getWidgetEntries(query).subscribe(
				(entriesReponse: any) => {
					this.sortedListLayoutEntryService.sortedEntries = entriesReponse.DATA;
					this.customTableService.isLoading = false;
					this.setCustomTableFields(listLayout);
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
		} else {
			this.setCustomTableFields(listLayout);
			const moduleName = this.module.NAME.replace(/\s+/g, '_');
			const query = `{
                DATA: get${moduleName}WidgetEntries(dasboardId: "${dashboardId}",widgetId: "${widgetId}",value: "${value}"){
                       ${fieldsQuery}
                
                     }
                 TOTAL_RECORDS: getWidgetEntriesCount(dasboardId: "${dashboardId}",widgetId: "${widgetId}", value: "${value}")
                          
                     }`;
			this.renderListLayoutService.getListLayoutEntries(query).subscribe(
				(entriesReponse: any) => {
					this.customTableService.isLoading = false;
					this.customTableService.pageIndex = 0;
					this.customTableService.pageSize = 20;
					this.sortedListLayoutEntryService.sortedEntries = entriesReponse.DATA;
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
		}
	}
	public getDashboard(dashboardId) {
		const query = `{
			DATA: getDashboard(dashboardId:"${dashboardId}") {
				name
				dashboardId: dashboardId
				description
				role {
					roleId
					name
				}
                widgets{
                    widgetId
					title
					type
                    moduleId
                    positionX
					positionY
					aggregateType,
					aggregateField,
					width
					height
					multiScorecards{
						widgetId
						title
						type
						moduleId
						positionX
						positionY
						aggregateType,
						aggregateField,
						limit
						field
						limitEntries
						orderBy{
							column
							order
						}
						dashboardconditions{
							condition
							operator
							value
							requirementType
						}
					}
					limit
					field
					limitEntries
					orderBy{
						column
						order
					}
                    dashboardconditions{
                        condition
                        operator
                        value
                        requirementType
                    }
                }
			}
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getWidgetEntries(query) {
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
	public onPageChange(event) {
		if (event.active) {
			const moduleField = this.module.FIELDS.find(
				(field) => field.DISPLAY_LABEL === event.active
			);
			this.customTableService.activeSort = {
				ORDER_BY: event.direction,
				SORT_BY: moduleField.NAME,
				NAME: event.active,
			};
			this.customTableService.sortBy = moduleField.NAME;
			this.customTableService.sortOrder = event.direction;
		}
		this.getDashboardWidgetEntries(
			this.dashboardId,
			this.widgetId,
			this.value,
			this.currentListLayout,
			true
		);
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

	public rowClicked(entry) {
		if (this.editAccess && this.viewAccess) {
			this.router.navigate([`render/${this.moduleId}/edit/${entry.DATA_ID}`]);
		} else if (this.viewAccess && !this.editAccess) {
			this.router.navigate([`render/${this.moduleId}/detail/${entry.DATA_ID}`]);
		} else if (!this.editAccess && !this.viewAccess) {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('VIEW_NOT_ALLOWED'),
			});
		}
	}
}
