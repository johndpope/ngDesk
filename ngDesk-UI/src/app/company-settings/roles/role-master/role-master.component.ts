import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { ConfirmDialogComponent } from 'src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { UsersService } from 'src/app/users/users.service';
import { RolesService } from '../roles-old.service';
import { RoleLayout } from '@ngdesk/role-api';
import { RoleApiService } from '@ngdesk/role-api';

@Component({
	selector: 'app-role-master',
	templateUrl: './role-master.component.html',
	styleUrls: ['./role-master.component.scss'],
})
export class RoleMasterComponent implements OnInit {
	public rolesActions = {
		actions: [{ NAME: 'DELETE', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	public buttonDisabled = false;

	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private router: Router,
		private dialog: MatDialog,
		public customTableService: CustomTableService,
		private rolesService: RolesService,
		private usersService: UsersService,
		private roleApiService: RoleApiService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.
		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.rolesActions[value] = (role) => {
				this.deleteRole(role);
			};
			this.rolesActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		const roleId = this.usersService.user.ROLE;

		this.rolesService.getRole(roleId).subscribe(
			(roleResponse: any) => {
				// enable or disable actions depending on role permission
				this.customTableService.isLoading = true;
				const columnsHeaders: string[] = [];
				const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];

				// only if there are actions to be shown. Actions are based on permissions
				columnsHeadersObj.push(
					{
						DISPLAY: this.translateService.instant('NAME'),
						NAME: 'name',
					},
					{
						DISPLAY: this.translateService.instant('DESCRIPTION'),
						NAME: 'description',
					}
				);
				columnsHeaders.push(this.translateService.instant('NAME'));
				columnsHeaders.push(this.translateService.instant('DESCRIPTION'));
				this.rolesActions.actions =
					this.customTableService.checkPermissionsForActions(
						roleResponse.DATA,
						this.rolesActions,
						'Roles'
					);

				// only if there are actions to be shown. Actions are based on permissions
				if (this.rolesActions.actions.length > 0) {
					columnsHeadersObj.push({
						DISPLAY: this.translateService.instant('ACTION'),
						NAME: 'ACTION',
					});
					columnsHeaders.push(this.translateService.instant('ACTION'));
				}

				this.customTableService.columnsHeaders = columnsHeaders;
				this.customTableService.columnsHeadersObj = columnsHeadersObj;
				this.customTableService.sortBy = 'name';
				this.customTableService.sortOrder = 'asc';
				this.customTableService.pageIndex = 0;
				this.customTableService.pageSize = 10;
				this.customTableService.activeSort = {
					ORDER_BY: 'asc',
					SORT_BY: this.translateService.instant('NAME'),
					NAME: 'NAME',
				};
				this.getRoles();
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	// private getRoles() {
	// 	const sortBy = this.customTableService.sortBy;
	// 	const orderBy = this.customTableService.sortOrder;
	// 	const page = this.customTableService.pageIndex;
	// 	const pageSize = this.customTableService.pageSize;

	// 	this.rolesService.getRoles(sortBy, orderBy, page + 1, pageSize).subscribe(
	// 		(data: any) => {
	// 			data['ROLES'].filter(
	// 				(role) => {
	// 					if (role.NAME === 'Customers')
	// 					{
	// 					  role['NAME'] = 'Customer';
	// 					}
	// 				});
	// 			this.customTableService.setTableDataSource(
	// 				data.ROLES,
	// 				data.TOTAL_RECORDS
	// 			);
	// 		},
	// 		(error: any) => {
	// 			this.bannerMessageService.errorNotifications.push({
	// 				message: error.error.ERROR,
	// 			});
	// 		}
	// 	);
	// }

	// Fetching all the roles based on page numebr and page size.
	public getRoles() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		const query = `{
			roles: getRoles(pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				name
				description
				roleId
			}
			totalCount: getRolesCount
		}`;

		this.rolesService.getAllRoles(query).subscribe(
			(rolesResponse: any) => {
				this.customTableService.setTableDataSource(
					rolesResponse.roles,
					rolesResponse.totalCount
				);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public rowClicked(rowData): void {
		// clicking on table row will redirect to escalation detail
		if (
			rowData.NAME === 'SystemAdmin' ||
			rowData.NAME === 'Agent' ||
			rowData.NAME === 'Customer'
		) {
			this.router.navigate([`company-settings/roles/${rowData.roleId}`]);
		} else {
			this.router.navigate([`company-settings/roles/edit/${rowData.roleId}`]);
		}
	}

	public sortData() {
		this.getRoles();
	}

	public pageChangeEmit(event) {
		this.getRoles();
	}

	public createNewRole() {
		this.router.navigate([`company-settings/roles/create/new`]);
	}

	private deleteRole(role) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_ROLE'
					) +
					role.name +
					' ?',
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DELETE')) {
				this.roleApiService.deleteRole(role.roleId).subscribe(
					(roleResponse: any) => {
						this.getRoles();
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('DELETED_SUCCESSFULLY'),
						});
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
}
