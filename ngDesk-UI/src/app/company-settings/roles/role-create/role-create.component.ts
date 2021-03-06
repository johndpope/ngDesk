import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { Role } from 'src/app/models/role';
import { ModulesService } from 'src/app/modules/modules.service';
import { RolesService } from '../roles-old.service';
import { RolesValidationService } from '../roles-validation.service';

@Component({
	selector: 'app-role-create',
	templateUrl: './role-create.component.html',
	styleUrls: ['./role-create.component.scss'],
})
export class RoleCreateComponent implements OnInit {
	public roleForm: FormGroup;
	public accessOptions = ['Disabled', 'Enabled', 'Not Set'];
	public accessTypeOptions = ['Admin', 'Normal', 'Not Set'];
	public modifyOptions = ['All', 'None', 'Not Set'];
	public buttonText: string;
	public pageTitle: string;
	public isSystemAdmin: boolean;
	public displayfieldPermissionButton: boolean
	public rolePermissions = [];
	public modulePermissions = [];
	public modules: any;
	public roleId;
	public role: Role = {
		PERMISSIONS: [],
		DESCRIPTION: '',
		ROLE_ID: '',
		USERS: [],
		NAME: '',
	};

	constructor(
		private formBuilder: FormBuilder,
		private customTableService: CustomTableService,
		private rolesService: RolesService,
		private bannerMessageService: BannerMessageService,
		private router: Router,
		private route: ActivatedRoute,
		private modulesService: ModulesService,
		private translateService: TranslateService,

	) {}

	public ngOnInit() {
		this.roleId = this.route.snapshot.params['dataId'];
		this.buttonText = '';
		this.pageTitle = '';
		this.isSystemAdmin = false;
		this.roleForm = this.formBuilder.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
		});
		this.customTableService.columnsHeaders = [
			'Modules',
			'Access',
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
			{ DISPLAY: 'View', NAME: 'VIEW' }
		];

		if (this.roleId !== 'new') {
			this.displayfieldPermissionButton = true;
			this.role.DESCRIPTION = this.roleForm.value['DESCRIPTION'];
			this.rolesService.getRoleById(this.roleId).subscribe(
				(roleResponse: Role) => {
					this.roleForm.controls['NAME'].setValue(roleResponse.NAME);
					this.roleForm.controls['DESCRIPTION'].setValue(
						roleResponse.DESCRIPTION
					);
					this.pageTitle = roleResponse.NAME;
					if (roleResponse.NAME === 'SystemAdmin') {
						this.isSystemAdmin = true;
					} else {
						this.buttonText = 'Save';
					}
					this.modulesService.getModules().subscribe(
						(modulesResponse: any) => {
							this.modules = modulesResponse.MODULES;
							let rolePermissions = [];
							this.role = roleResponse;
							this.role.PERMISSIONS.forEach((permission) => {
								const module = modulesResponse.MODULES.find(
									(currentModule) =>
										currentModule.MODULE_ID === permission.MODULE
								);
								if (module !== undefined) {
									permission.MODULE_PERMISSIONS.MODULE_NAME = module.NAME;
								} else {
									permission.MODULE_PERMISSIONS.MODULE_NAME = permission.MODULE;
								}
								rolePermissions.push(permission.MODULE_PERMISSIONS);
							});
							this.customTableService.isLoading = true;
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
		} else {
			this.modulesService.getModules().subscribe((modulesResponse: any) => {
				this.modules = modulesResponse.MODULES;
				this.rolePermissions = [];
				this.modules.forEach((module) => {
					const fieldPermissions = [];
					const modulePermission = {
						DELETE: 'Not Set',
						ACCESS_TYPE: 'Not Set',
						EDIT: 'Not Set',
						VIEW: 'Not Set',
						ACCESS: 'Not Set',
						MODULE_NAME: module.NAME,
					};

					module.FIELDS.forEach((field) => {
						fieldPermissions.push({
							PERMISSION: 'Not Set',
							FIELD: field.FIELD_ID,
						});
					});

					const rolePermission = {
						MODULE_PERMISSIONS: modulePermission,
						MODULE: module.MODULE_ID,
						FIELD_PERMISSIONS: fieldPermissions,
					};

					this.rolePermissions.push(rolePermission);
					this.modulePermissions.push(modulePermission);
				});
				const scheduleModulePermission = {
					DELETE: 'Not Set',
					ACCESS_TYPE: 'Not Set',
					EDIT: 'Not Set',
					VIEW: 'Not Set',
					ACCESS: 'Not Set',
					MODULE_NAME: 'Schedules',
				};

				const escalationModulePermission = {
					DELETE: 'Not Set',
					ACCESS_TYPE: 'Not Set',
					EDIT: 'Not Set',
					VIEW: 'Not Set',
					ACCESS: 'Not Set',
					MODULE_NAME: 'Escalations',
				};

				const scheduleRolePermission = {
					MODULE_PERMISSIONS: scheduleModulePermission,
					MODULE: 'Schedules',
					FIELD_PERMISSIONS: [],
				};

				const escalationRolePermission = {
					MODULE_PERMISSIONS: scheduleModulePermission,
					MODULE: 'Escalations',
					FIELD_PERMISSIONS: [],
				};
				this.rolePermissions.push(scheduleRolePermission);
				this.rolePermissions.push(escalationRolePermission);
				this.modulePermissions.push(scheduleModulePermission);
				this.modulePermissions.push(escalationModulePermission);
				this.customTableService.isLoading = true;
				this.customTableService.setTableDataSource(
					this.modulePermissions,
					this.modulePermissions.length
				);
			});
		}
	}

	public saveRole() {
		if (this.roleForm.valid) {
			this.role.NAME = this.roleForm.controls.NAME.value;
			this.role.DESCRIPTION = this.roleForm.value['DESCRIPTION'];
			if (this.roleId !== 'new') {
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
			} else {
				this.role.PERMISSIONS = this.rolePermissions;
				this.rolesService.postRole(this.role).subscribe(
					(putResponse: any) => {
						this.router.navigate(['company-settings/roles']);
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SAVED_SUCCESSFULLY'),
						});
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			}
		}
	}

	public fieldPermission(entry) {
		if(entry.MODULE_NAME !== 'Schedules' && entry.MODULE_NAME !== 'Escalations') {
			const module = this.modules.find(
				(currentModule) => currentModule.NAME === entry.MODULE_NAME
			);
			this.router.navigate([`company-settings/roles/${this.roleId}/${module.MODULE_ID}/field-permisssion`]);
		}

	}
}
