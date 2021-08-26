import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';

import { RolesService } from '@src/app/roles/roles.service';
import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../dialogs/confirm-dialog/confirm-dialog.component';
import { SpfRecordDialogComponent } from '../../dialogs/spf-record-dialog/spf-record-dialog.component';
import { UsersService } from '../../users/users.service';

@Component({
	selector: 'app-spf-records',
	templateUrl: './spf-records.component.html',
	styleUrls: ['./spf-records.component.scss'],
})
export class SpfRecordsComponent implements OnInit {
	public spfRecordsActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};

	constructor(
		private bannerMessageService: BannerMessageService,
		public customTableService: CustomTableService,
		private companiesService: CompaniesService,
		private translateService: TranslateService,
		private rolesService: RolesService,
		private usersService: UsersService,
		private dialog: MatDialog
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.spfRecordsActions with the name of the translated word
			this.spfRecordsActions[value] = (response) => {
				this.deleteSpfRecord(response);
			};
			this.spfRecordsActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.rolesService.getRole(this.usersService.user.ROLE).subscribe(
			(roleResponse: any) => {
				// enable or disable actions depending on role permission
				this.spfRecordsActions.actions = this.customTableService.checkPermissionsForActions(
					roleResponse,
					this.spfRecordsActions,
					'Spf Records'
				);

				const columnsHeaders: string[] = [
					this.translateService.instant('DOMAIN'),
				];
				const columnsHeadersObj = [
					{ DISPLAY: this.translateService.instant('DOMAIN'), NAME: 'DOMAIN' },
				];
				// only if there are actions to be shown. Actions are based on permissions
				if (this.spfRecordsActions.actions.length > 0) {
					columnsHeadersObj.push({
						DISPLAY: this.translateService.instant('ACTION'),
						NAME: 'ACTION',
					});
					columnsHeaders.push(this.translateService.instant('ACTION'));
				}
				this.customTableService.pageIndex = 0;
				this.customTableService.pageSize = 10;
				this.customTableService.sortBy = 'DOMAIN';
				this.customTableService.sortOrder = 'asc';
				this.customTableService.activeSort = {
					ORDER_BY: 'asc',
					SORT_BY: this.translateService.instant('DOMAIN'),
					NAME: 'DOMAIN',
				};

				this.customTableService.columnsHeaders = columnsHeaders;
				this.customTableService.columnsHeadersObj = columnsHeadersObj;
				this.customTableService.isLoading = true;
				this.getSpfRecords();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	private getSpfRecords() {
		this.companiesService.getSpfRecords().subscribe(
			(spfRecordsSuccess: any) => {
				this.customTableService.setTableDataSource(
					spfRecordsSuccess.SPF_RECORDS,
					spfRecordsSuccess.TOTAL_RECORDS
				);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	private deleteSpfRecord(spfRecord) {
		console.log(spfRecord);
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_SPF_RECORD'
					) +
					spfRecord.DOMAIN +
					'?',
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DELETE')) {
				this.companiesService.deleteSpfRecord(spfRecord.SPF_ID).subscribe(
					(response: any) => {
						this.getSpfRecords();
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
		this.getSpfRecords();
	}

	public pageChangeEmit(event) {
		this.getSpfRecords();
	}

	public openDialog(rowData?) {
		let dialogRef;
		if (rowData !== undefined) {
			dialogRef = this.dialog.open(SpfRecordDialogComponent, {
				// width: '400px',
				disableClose: true,
				data: { spfId: rowData.SPF_ID, domain: rowData.DOMAIN },
			});
		} else {
			dialogRef = this.dialog.open(SpfRecordDialogComponent, {
				// width: '400px',
				disableClose: true,
				autoFocus: true,
				data: { spfId: 'new', domain: null },
			});
		}

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === undefined) {
				this.translateService
					.get('SPF_RECORD_SAVE_SUCCESS')
					.subscribe((value: string) => {
						this.bannerMessageService.successNotifications.push({
							message: value,
						});
					});
			}
			this.getSpfRecords();
		});
	}
}
