import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { BannerMessageService } from '../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../dialogs/confirm-dialog/confirm-dialog.component';
import { ModulesService } from '../../../modules/modules.service';
import { UsersService } from '../../../users/users.service';
import { RolesService } from '@src/app/roles/roles.service';

@Component({
	selector: 'app-premade-responses-master',
	templateUrl: './premade-responses-master.component.html',
	styleUrls: ['./premade-responses-master.component.scss'],
})
export class PremadeResponsesMasterComponent implements OnInit {
	public isLoading = true;
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public premadeResponseActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};

	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private dialog: MatDialog,
		public customTableService: CustomTableService,
		private router: Router,
		private usersService: UsersService,
		private rolesService: RolesService,
		private modulesService: ModulesService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.premadeResponseActions[value] = (response) => {
				this.deletePremadeResponse(response);
			};
			this.premadeResponseActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.rolesService.getRole(this.usersService.user.ROLE).subscribe(
			(roleResponse: any) => {
				// enable or disable actions depending on role permission
				this.premadeResponseActions.actions = this.customTableService.checkPermissionsForActions(
					roleResponse,
					this.premadeResponseActions,
					'Premade Responses'
				);
				const columnsHeaders: string[] = [
					this.translateService.instant('NAME'),
				];
				const columnsHeadersObj = [
					{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
				];
				// only if there are actions to be shown. Actions are based on permissions
				if (this.premadeResponseActions.actions.length > 0) {
					columnsHeadersObj.push({
						DISPLAY: this.translateService.instant('ACTION'),
						NAME: 'ACTION',
					});
					columnsHeaders.push(this.translateService.instant('ACTION'));
				}
				this.customTableService.pageIndex = 0;
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
				this.getPremadeResponses();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public getPremadeResponses() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.modulesService
			.getAllPremadeResponses(sortBy, orderBy, page + 1, pageSize)
			.subscribe(
				(premadeResponseSuccess: any) => {
					this.isLoading = false;
					this.customTableService.setTableDataSource(
						premadeResponseSuccess.DATA,
						premadeResponseSuccess.TOTAL_RECORDS
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	private deletePremadeResponse(premadeResponse) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_PREMADE_RESPONSE'
					) +
					premadeResponse.NAME +
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
				this.modulesService
					.deletePremadeResponses(premadeResponse.PREMADE_RESPONSE_ID)
					.subscribe(
						(response: any) => {
							this.getPremadeResponses();
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
	public sortData() {
		this.getPremadeResponses();
	}

	public rowClicked(rowData): void {
		this.router.navigate([
			`company-settings/premade-responses/${rowData.PREMADE_RESPONSE_ID}`,
		]);
	}

	public newPremadeResponse() {
		this.router.navigate([`company-settings/premade-responses/new`]);
	}

	public pageChangeEmit(event) {
		this.getPremadeResponses();
	}
}
