import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomTableService } from '../../../custom-table/custom-table.service';
import { Role } from '../../../models/role';
import { ModulesService } from '../../../modules/modules.service';
import { RolesService } from '../roles-old.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { RolesValidationService } from '../roles-validation.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
	selector: 'app-role-detail',
	templateUrl: './role-detail.component.html',
	styleUrls: ['./role-detail.component.scss'],
})
export class RoleDetailComponent implements OnInit {
	public modules: any;
	public roleId;
	public data: any;
	public pageTitle: string;
	public buttonText: string;
	private role: Role;
	public isSystemAdmin: boolean;
	public accessOptions = ['Disabled', 'Enabled', 'Not Set'];
	public accessTypeOptions = ['Admin', 'Normal', 'Not Set'];
	public modifyOptions = ['All', 'None', 'Not Set'];

	constructor(
		private rolesService: RolesService,
		private modulesService: ModulesService,
		private customTableService: CustomTableService,
		private router: Router,
		private route: ActivatedRoute,
		private bannerMessageService: BannerMessageService,
		private rolesValidationService: RolesValidationService,
		private translateService: TranslateService,
	) {}

	public ngOnInit() {
		this.roleId = this.route.snapshot.params['roleId'];
		this.buttonText = '';
		this.pageTitle = '';
		this.isSystemAdmin = false;
		this.customTableService.columnsHeaders = [
			'Modules',
			'Access',
			// 'Access Type', commented for now may be used later
			'Delete',
			'Create/Edit',
			'View'
		];
		this.customTableService.columnsHeadersObj = [
			{ DISPLAY: 'Modules', NAME: 'MODULE_NAME' },
			{ DISPLAY: 'Access', NAME: 'ACCESS' },
			// { DISPLAY: 'Access Type', NAME: 'ACCESS_TYPE' }, commented for now may be used later
			{ DISPLAY: 'Delete', NAME: 'DELETE' },
			{ DISPLAY: 'Create/Edit', NAME: 'EDIT' },
			{ DISPLAY: 'View', NAME: 'VIEW' },
		];

		this.rolesService.getRoleById(this.roleId).subscribe(
			(roleResponse: Role) => {
				if (roleResponse.NAME === 'Customers') {
					this.pageTitle  = 'Customer'; 
				} else {
					this.pageTitle = roleResponse.NAME;
				}
				if (roleResponse.NAME === 'SystemAdmin') {
					this.isSystemAdmin = true;
				} else {
					this.buttonText = 'Save';
				}
				this.modulesService.getModules().subscribe(
					(modulesResponse: any) => {
						this.modules = modulesResponse.MODULES;
						console.log(this.modules);
						const rolePermissions = [];
						this.role = roleResponse;
						this.role.PERMISSIONS.forEach((permission) => {
							const module = modulesResponse.MODULES.find(
								(currentModule) => currentModule.MODULE_ID === permission.MODULE
							);
							if (module !== undefined) {
								permission.MODULE_PERMISSIONS.MODULE_NAME = module.NAME;
							} else {
								permission.MODULE_PERMISSIONS.MODULE_NAME = permission.MODULE;
							}
							rolePermissions.push(permission.MODULE_PERMISSIONS);
						});
						this.data = {
							DATA: rolePermissions,
							TOTAL_RECORDS: rolePermissions.length,
						};
						this.customTableService.showPaginator = false;
						this.customTableService.setTableDataSource(
							rolePermissions,
							rolePermissions.length
						);
					},
					(error: any) => {
						console.log(error);
					}
				);
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	public saveRole() {
		// this.removeModuleNameFromPermissions();
		this.rolesService.putRoleById(this.role.ROLE_ID, this.role).subscribe(
			(putResponse: any) => {
				this.router.navigate(['company-settings/roles']);
				this.bannerMessageService.successNotifications.push({
					message: this.translateService.instant('ROLE_UPDATED_SUCCESSFULLY'),
				});
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public sortData(event) {
		let index = this.customTableService.columnsHeaders.indexOf(event.active);
		let name = this.customTableService.columnsHeadersObj[index].NAME;
		if (event.direction === 'asc') {
			this.data.DATA.sort((a, b) =>
				a[name] > b[name] ? 1 : b[name] > a[name] ? -1 : 0
			);
		} else {
			this.data.DATA.sort((a, b) =>
				a[name] > b[name] ? -1 : b[name] > a[name] ? 1 : 0
			);
		}
		this.customTableService.setTableDataSource(
			this.data.DATA,
			this.data.TOTAL_RECORDS
		);
	}

	public fieldPermission(entry) {
		if(entry.MODULE_NAME !== 'Schedules' && entry.MODULE_NAME !== 'Escalations') {
			const module = this.modules.find(
				(currentModule) => currentModule.NAME === entry.MODULE_NAME
			);
			this.router.navigate([`company-settings/roles/${this.roleId}/${module.MODULE_ID}/field-permisssion`]);
		}

	}
	// 	private removeModuleNameFromPermissions() {
	// 		this.role.PERMISSIONS.forEach(permission => {
	// 			delete permission.MODULE_PERMISSIONS.MODULE_NAME;
	// 		});
	// 	}
}
