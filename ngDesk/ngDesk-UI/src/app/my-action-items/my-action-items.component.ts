import {
	animate,
	state,
	style,
	transition,
	trigger,
} from '@angular/animations';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { DataApiService } from '@ngdesk/data-api';
import { RoleLayoutApiService } from '@ngdesk/role-api';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { Role } from '@src/app/models/role';
import { RolesService } from '@src/app/roles/roles.service';
import { indexOf } from 'lodash';
import { Subscription } from 'rxjs';
import { ModulesService } from '../modules/modules.service';
import { UsersService } from '../users/users.service';
import { MyActionItemsService } from './my-action-items.service';

@Component({
	selector: 'app-my-action-items',
	templateUrl: './my-action-items.component.html',
	styleUrls: ['./my-action-items.component.scss'],
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
	],
})
export class MyActionItemsComponent implements OnInit, OnDestroy {
	private navigationSubscription: Subscription;
	public showListLayouts = false;
	private allModules: any = [];
	private modules = [];
	private modulePermissions = [];
	public selectModuleTabIndex = 0;
	public companyDataUpTodate: Subscription;
	public roleLayouts: any[];
	public selectedRoleLayout: any;
	public pageTitle = '';
	public roleLayoutsExist = true;
	public allowNewEntry = false;
	public role: Role;
	public specialDataTypes = ['Relationship', 'Date/Time', 'Date', 'Time'];

	constructor(
		private modulesService: ModulesService,
		private translateService: TranslateService,
		private dataService: DataApiService,
		public customTableService: CustomTableService,
		private cacheService: CacheService,
		private router: Router,
		private roleApi: RoleLayoutApiService,
		private rolesService: RolesService,
		private usersService: UsersService,
		private bannerMessageService: BannerMessageService,
		private myActionItemsService: MyActionItemsService
	) {}

	public ngOnInit() {
		this.initializeComponent();
		this.navigationSubscription = this.router.events.subscribe((event) => {
			if (event instanceof NavigationEnd) {
				this.initializeComponent();
			}
		});
	}

	public ngOnDestroy() {
		if (this.companyDataUpTodate) {
			this.companyDataUpTodate.unsubscribe();
		}
	}

	private initializeComponent() {
		this.companyDataUpTodate = this.cacheService.companyInfoSubject.subscribe(
			(dataStored) => {
				if (dataStored) {
					this.modules = this.cacheService.companyData['MODULES'];
					// Get role layouts stored in cache service.
					this.myActionItemsService
						.getAllRoleLayouts()
						.subscribe((roleLayouts: any) => {
							if (roleLayouts.getRoleLayouts !== null) {
								this.roleLayouts = roleLayouts.getRoleLayouts;
								// get role stored in cache service
								this.cacheService.getRoles().subscribe((roles) => {
									this.role = roles.find((role) => {
										return role.ROLE_ID === this.usersService.user.ROLE;
									});
									const layouts = [];
									this.roleLayouts.forEach((layout) => {
										if (layout.role.name === this.role.NAME) {
											layouts.push(layout);
										}
									});
									this.roleLayouts = layouts;
									this.checkPermissions(this.role);
									const defaultRoleLayout = this.roleLayouts.find(
										(roleLayout) => roleLayout.defaultLayout === true
									);
									if (defaultRoleLayout) {
										this.setListLayout(defaultRoleLayout);
									} else {
										this.roleLayoutsExist = false;
									}
								});
							} else {
								this.roleLayouts = [];
								this.roleLayoutsExist = false;
								this.cacheService.getRoles().subscribe((roles) => {
									this.role = roles.find((role) => {
										return role.ROLE_ID === this.usersService.user.ROLE;
									});
								});
							}
						});
				}
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	private transformModules(roleLayoutObj: any) {
		roleLayoutObj.tabs.forEach((moduleFound, moduleIndex) => {
			const module = this.modules.find(
				(moduleLooped) => moduleLooped.MODULE_ID === moduleFound.module
			);
			if (typeof moduleFound.module === 'string') {
				moduleFound.module = { moduleId: module.MODULE_ID, name: module.NAME };
			}
		});
		return roleLayoutObj;
	}

	public newEntry() {
		const moduleId =
			this.selectedRoleLayout.tabs[this.selectModuleTabIndex].module.moduleId;
		this.router.navigate(['render', moduleId, 'create', 'new']);
	}

	public newLayout() {
		this.router.navigate(['company-settings', 'role-layouts', 'new']);
	}

	public setCustomTableFields(listlayout, moduleIndex) {
		this.customTableService.columnsHeaders = [];
		this.customTableService.columnsHeadersObj = [];
		let moduleField: any;
		if (listlayout) {
			const selectedModule = listlayout.tabs[moduleIndex].module.moduleId;
			const moduleFields = this.modules.find(
				(module) => module.MODULE_ID === selectedModule
			).FIELDS;
			listlayout.tabs[moduleIndex].columnsShow.forEach((field) => {
				// specify the sorting column
				if (field === listlayout.tabs[moduleIndex].orderBy.column) {
					moduleField = moduleFields.find(
						(eachField) => eachField.FIELD_ID === field
					);
					this.customTableService.activeSort = {
						ORDER_BY: listlayout.tabs[moduleIndex].orderBy.order.toLowerCase(),
						SORT_BY: moduleField.DISPLAY_LABEL,
						NAME: moduleField.NAME,
					};

					this.customTableService.sortBy =
						this.customTableService.activeSort.NAME;
					this.customTableService.sortOrder =
						this.customTableService.activeSort.ORDER_BY;
				}
				this.setCustomTableColumnHeaders(moduleFields, field);
			});
		}
	}

	public checkPermissions(roleObj) {
		const role = roleObj;
		const permissions = role.PERMISSIONS;
		this.roleLayouts.forEach((roleLayout) => {
			roleLayout.tabs.forEach((moduleTab) => {
				const permissionFound = this.modulePermissions.find(
					(permission) => permission.MODULE === moduleTab.module.moduleId
				);
				if (!permissionFound) {
					const moduleId = moduleTab.module.moduleId;
					const modulePermission = {
						MODULE: moduleId,
						EDIT_ACCESS: false,
						VIEW_ACCESS: true,
						DELETE_ACCESS: false,
						MODULE_ALLOWED: true,
					};

					if (role.NAME === 'SystemAdmin') {
						modulePermission.MODULE_ALLOWED = true;
						modulePermission.EDIT_ACCESS = true;
						modulePermission.VIEW_ACCESS = true;
						modulePermission.DELETE_ACCESS = true;
					} else {
						const rolePermission = permissions.find(
							(permission) => permission.MODULE === moduleId
						);
						if (rolePermission) {
							if (rolePermission.MODULE_PERMISSIONS.ACCESS === 'Enabled') {
								modulePermission.MODULE_ALLOWED = true;
								if (
									rolePermission.MODULE_PERMISSIONS.EDIT === 'All' ||
									rolePermission.MODULE_PERMISSIONS.EDIT === 'Not Set'
								) {
									modulePermission.EDIT_ACCESS = true;
								}
								if (
									rolePermission.MODULE_PERMISSIONS.VIEW === 'All' ||
									rolePermission.MODULE_PERMISSIONS.VIEW === 'Not Set'
								) {
									modulePermission.VIEW_ACCESS = true;
								}
								if (
									rolePermission.MODULE_PERMISSIONS.DELETE === 'All' ||
									rolePermission.MODULE_PERMISSIONS.DELETE === 'Not Set'
								) {
									modulePermission.DELETE_ACCESS = true;
								}
							} else {
								modulePermission.MODULE_ALLOWED = false;
							}
						}
					}
					this.modulePermissions.push(modulePermission);
				}
			});
		});
	}

	private setCustomTableColumnHeaders(moduleFields, field) {
		const moduleField = moduleFields.find(
			(eachField) => eachField.FIELD_ID === field.fieldId
		);
		const headersObject = {
			DISPLAY: moduleField.DISPLAY_LABEL,
			NAME: moduleField.NAME,
			DATA_TYPE: moduleField.DATA_TYPE.DISPLAY,
		};
		this.customTableService.columnsHeaders.push(moduleField.DISPLAY_LABEL);
		this.customTableService.columnsHeadersObj.push(headersObject);
		this.customTableService.isLoading = false;
	}

	public setListLayoutTableState(currentState) {
		this.showListLayouts = !currentState;
	}

	public setListLayout(layout, moduleIndex?) {
		this.selectedRoleLayout = this.transformModules(layout);
		this.pageTitle = layout.name;
		if (!moduleIndex) {
			moduleIndex = 0;
		}
		this.checkModuleNewEntryPermission(moduleIndex);
		this.setCustomTableFields(this.selectedRoleLayout, moduleIndex);
		const fields = this.getFieldsForListlayout(layout, moduleIndex);
		this.getListLayoutEntries(
			this.selectedRoleLayout.tabs[moduleIndex],
			fields
		);
		this.roleLayoutsExist = true;
	}

	private checkModuleNewEntryPermission(moduleIndex) {
		const moduleId = this.selectedRoleLayout.tabs[moduleIndex].module.moduleId;
		const modulePermission = this.modulePermissions.find(
			(permission) => permission.MODULE === moduleId
		);
		if (modulePermission.EDIT_ACCESS) {
			this.allowNewEntry = true;
		} else {
			this.allowNewEntry = false;
		}
	}

	public rowClicked(entry, moduleId) {
		const modulePermission = this.modulePermissions.find(
			(permission) => permission.MODULE === moduleId
		);
		if (modulePermission.EDIT_ACCESS && modulePermission.VIEW_ACCESS) {
			this.router.navigate([`render/${moduleId}/edit/${entry._id}`]);
		} else if (modulePermission.VIEW_ACCESS && !modulePermission.EDIT_ACCESS) {
			this.router.navigate([`render/${moduleId}/detail/${entry._id}`]);
		} else if (!modulePermission.EDIT_ACCESS && !modulePermission.VIEW_ACCESS) {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('VIEW_NOT_ALLOWED'),
			});
		}
	}

	public onPageChange(event, orderBy, tab, type) {
		this.customTableService.isLoading = true;
		let sort;
		let order;
		let index;
		this.selectedRoleLayout.tabs.forEach((layout) => {
			if (layout === tab) {
				index = indexOf(this.selectedRoleLayout.tabs, layout);
			}
		});
		if (type === 'pageChange') {
			sort = orderBy.column.name;
			order = orderBy.order;
		} else {
			sort = event.active.toUpperCase().replace(/ /g, '_');
			order = event.direction;
		}
		const fields = this.getFieldsForListlayout(this.selectedRoleLayout, index);
		this.myActionItemsService
			.getRoleLayoutData(
				this.selectedRoleLayout.layoutId,
				tab.tabId,
				tab.module.name,
				fields,
				this.customTableService.pageIndex,
				this.customTableService.pageSize,
				sort,
				order
			)
			.subscribe(
				(roleData: any) => {
					this.customTableService.setTableDataSource(
						roleData[`get${tab.module.name.replace(/ /g, '_')}RoleLayout`],
						roleData['getRoleLayoutValuesCount']
					);
					this.customTableService.isLoading = false;
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	// It returns all the role layouts data.
	public getListLayoutEntries(tab, fields) {
		this.customTableService.isLoading = true;
		this.myActionItemsService
			.getRoleLayoutData(
				this.selectedRoleLayout.layoutId,
				tab.tabId,
				tab.module.name,
				fields,
				0,
				10,
				this.customTableService.sortBy,
				this.customTableService.sortOrder
			)
			.subscribe(
				(roleData: any) => {
					this.customTableService.setTableDataSource(
						roleData[`get${tab.module.name.replace(/ /g, '_')}RoleLayout`],
						roleData[`getRoleLayoutValuesCount`]
					);
					this.customTableService.isLoading = false;
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	public onSelectModuleTabs(event) {
		this.checkModuleNewEntryPermission(event.index);
		this.setCustomTableFields(this.selectedRoleLayout, event.index);
		const fields = this.getFieldsForListlayout(
			this.selectedRoleLayout,
			event.index
		);
		this.getListLayoutEntries(
			this.selectedRoleLayout.tabs[event.index],
			fields
		);
	}

	public getFieldsForListlayout(layout, index) {
		const selectedModule = layout.tabs[index].module.moduleId;
		const moduleFields = this.modules.find(
			(module) => module.MODULE_ID === selectedModule
		).FIELDS;
		let showFields = '_id' + '\n';
		this.customTableService.sortBy = layout.tabs[index].orderBy.column.name;
		this.customTableService.sortOrder = layout.tabs[index].orderBy.order;
		layout.tabs[index].columnsShow.forEach((field) => {
			
			const moduleField = moduleFields.find(
				(eachField) => eachField.FIELD_ID === field.fieldId
			);
			if (moduleField.DATA_TYPE.DISPLAY === 'Relationship') {
				this.modulesService.getModules().subscribe((modulesResponse: any) => {
					this.allModules = modulesResponse.MODULES;
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
					showFields += relationshipQuery + '\n';
				});
			} else {
				showFields += moduleField.NAME + '\n';
			}
		});
		return showFields;
	}
}
