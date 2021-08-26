import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';

import { ApiService } from '../../api/api.service';
import { CustomTableService } from '../../custom-table/custom-table.service';
import { ApiKeyDialogComponent } from '../../dialogs/api-key-dialog/api-key-dialog.component';
import { ConfirmDialogComponent } from '../../dialogs/confirm-dialog/confirm-dialog.component';
import { UsersService } from '../../users/users.service';
import { RolesService } from '@src/app/roles/roles.service';

@Component({
	selector: 'app-api-keys',
	templateUrl: './api-keys.component.html',
	styleUrls: ['./api-keys.component.scss'],
})
export class ApiKeysComponent implements OnInit {
	public apiKeysActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};

	constructor(
		private translateService: TranslateService,
		private dialog: MatDialog,
		private apiService: ApiService,
		public customTableService: CustomTableService,
		private usersService: UsersService,
		private rolesService: RolesService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('REVOKE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.apiKeysActions[value] = (schedule) => {
				this.deleteApiKey(schedule);
			};
			this.apiKeysActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.rolesService.getRole(this.usersService.user.ROLE).subscribe(
			(roleResponse: any) => {
				// enable or disable actions depending on role permission
				this.apiKeysActions.actions = this.customTableService.checkPermissionsForActions(
					roleResponse,
					this.apiKeysActions,
					null
				);

				const columnsHeaders: string[] = [
					this.translateService.instant('NAME'),
					this.translateService.instant('USER'),
				];
				const columnsHeadersObj = [
					{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
					{ DISPLAY: this.translateService.instant('USER'), NAME: 'USER' },
				];
				// only if there are actions to be shown. Actions are based on permissions
				if (this.apiKeysActions.actions.length > 0) {
					columnsHeadersObj.push({
						DISPLAY: this.translateService.instant('ACTION'),
						NAME: 'ACTION',
					});
					columnsHeaders.push(this.translateService.instant('ACTION'));
				}
				this.customTableService.pageIndex = 1;
				this.customTableService.pageSize = 10;
				this.customTableService.sortBy = 'NAME';
				this.customTableService.sortOrder = 'asc';
				this.customTableService.activeSort = {
					ORDER_BY: 'asc',
					SORT_BY: this.translateService.instant('NAME'),
					NAME: 'NAME',
				};

				this.customTableService.columnsHeaders = columnsHeaders;
				this.customTableService.columnsHeadersObj = columnsHeadersObj;
				this.customTableService.isLoading = true;
				this.getApiKeys();
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	private getApiKeys() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;

		this.apiService.getAllKeys(sortBy, orderBy, page, pageSize).subscribe(
			(response: any) => {
				this.customTableService.setTableDataSource(
					response.API_KEYS,
					response.TOTAL_RECORDS
				);
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	public pageChangeEmit(event) {
		this.customTableService.pageIndex++;
		this.getApiKeys();
	}

	private deleteApiKey(apiKey) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message: this.translateService.instant(
					'ARE_YOU_SURE_YOU_WANT_TO_DELETE_KEY'
				),
				buttonText: this.translateService.instant('REVOKE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('REVOKE'),
				executebuttonColor: 'warn',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			// TODO: implement  delete call here
			if (result === this.translateService.instant('REVOKE')) {
				this.apiService.deleteAPIKey(apiKey.TOKEN_ID).subscribe(
					(response: any) => {
						this.getApiKeys();
					},
					(error: any) => {
						console.log(error);
					}
				);
			}
		});
	}

	public sortData(event) {
		this.getApiKeys();
	}

	public NewApiKey() {
		const dialogRef = this.dialog.open(ApiKeyDialogComponent, {
			width: '500px',
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === 'close') {
				this.getApiKeys();
			}
		});
	}
}
