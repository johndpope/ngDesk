import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomTableService } from '../../../custom-table/custom-table.service';
import { Role } from '../../../models/role';
import { ModulesService } from '../../../modules/modules.service';
import { RolesService } from '../roles-old.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { TranslateService } from '@ngx-translate/core';
@Component({
	selector: 'app-field-permission',
	templateUrl: './field-permission.component.html',
	styleUrls: ['./field-permission.component.scss'],
})
export class FieldPermissionComponent implements OnInit {
	public roleId;
	public moduleId;
	public modules: any;new
	public fields = [];
	public data: any;
	private role: Role;
	public fieldPermission;
	public fieldsMap = {};
	public fieldPermissions = [];
	public nonEditableFields = [];
	public accessOptions = ['Read', 'Write Only Creator', 'Not Editable', 'Read/Write', 'Not Set', 'Write by team'];

	constructor(
		private rolesService: RolesService,
		private modulesService: ModulesService,
		private customTableService: CustomTableService,
		private router: Router,
		private route: ActivatedRoute,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,

		) {}

	public ngOnInit() {
		this.roleId = this.route.snapshot.params['roleId'];
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.customTableService.columnsHeaders = [
			'Fields',
			'Permission',
		];
		this.customTableService.columnsHeadersObj = [
			{ DISPLAY: 'Fields', NAME: 'FIELD_NAME' },
			{ DISPLAY: 'Permission', NAME: 'PERMISSION' },
		];

		this.rolesService.getRoleById(this.roleId).subscribe(
			(roleResponse: Role) => {

				this.modulesService.getModules().subscribe(
					(modulesResponse: any) => {
						this.modules = modulesResponse.MODULES;
						this.modules.forEach(element => {
							if(element.MODULE_ID === this.moduleId){
								this.fields = element.FIELDS;
							}
						});
						for (let i = 0; i < this.fields.length; i++) {
							this.fieldsMap[this.fields[i].FIELD_ID] =
								this.fields[i];
						}
						this.role = roleResponse;
						this.role.PERMISSIONS.forEach((permission) => {
							if(permission.MODULE === this.moduleId) {
								permission.FIELD_PERMISSIONS.forEach(permissionField => {
									const fieldPermissionObject = this.fieldsMap[permissionField.FIELD];
									if(fieldPermissionObject) {
										if(	fieldPermissionObject.NOT_EDITABLE === false &&
											fieldPermissionObject.DATA_TYPE.DISPLAY !== 'Workflow Stages' &&
											fieldPermissionObject.DATA_TYPE.DISPLAY !== 'Auto Number' &&
											fieldPermissionObject.DATA_TYPE.DISPLAY !== 'Zoom' 
										){
											const fieldPermissionsObject = {
												PERMISSION : permissionField.PERMISSION,
												FIELD : permissionField.FIELD ,
												FIELD_NAME : fieldPermissionObject.DISPLAY_LABEL
											}
											this.fieldPermissions.push(fieldPermissionsObject);
											permission.FIELD_PERMISSIONS = this.fieldPermissions;

										} else 
											if(fieldPermissionObject.NOT_EDITABLE === true ||
												fieldPermissionObject.DATA_TYPE.DISPLAY === 'Workflow Stages' ||
												fieldPermissionObject.DATA_TYPE.DISPLAY === 'Auto Number' ||
												fieldPermissionObject.DATA_TYPE.DISPLAY === 'Zoom' ) {
													const nonEditableFieldObject = {
														PERMISSION : permissionField.PERMISSION,
														FIELD : permissionField.FIELD ,
														FIELD_NAME : fieldPermissionObject.DISPLAY_LABEL
													}
												this.nonEditableFields.push(nonEditableFieldObject);
											}

										}
									}
								);
								
							}
						});
						this.data = {
							DATA: this.fieldPermissions,
							TOTAL_RECORDS: this.fieldPermissions.length,
						};
						this.customTableService.showPaginator = true;
						this.customTableService.setTableDataSource(
							this.fieldPermissions,
							this.fieldPermissions.length
						);
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SAVE_ROLE_TO_CHANGE_FIELD_PERMISSION'),
						});
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

	public saveField() {
		this.role.PERMISSIONS.forEach((permission) => {
			if(permission.MODULE === this.moduleId) {
				this.nonEditableFields.forEach((nonEditableField: any) => {
					this.fieldPermissions.push(nonEditableField);
				});
				permission.FIELD_PERMISSIONS = 	this.fieldPermissions;
			}
		});
		this.rolesService.putRoleById(this.role.ROLE_ID, this.role).subscribe(
			(putResponse: any) => {
				this.router.navigate([`company-settings/roles/${this.roleId}`]);
				this.bannerMessageService.successNotifications.push({
					message: this.translateService.instant('FIELD_PERMISSION_UPDATED_SUCCESSFULLY'),
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
 