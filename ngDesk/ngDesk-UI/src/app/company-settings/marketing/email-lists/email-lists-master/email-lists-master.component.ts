import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../../../companies/companies.service';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../../dialogs/confirm-dialog/confirm-dialog.component';
import { UsersService } from '../../../../users/users.service';
import { RolesService } from '@src/app/roles/roles.service';

@Component({
	selector: 'app-email-lists-master',
	templateUrl: './email-lists-master.component.html',
	styleUrls: ['./email-lists-master.component.scss'],
})
export class EmailListsMasterComponent implements OnInit {
	public emailListsActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};

	constructor(
		private translateService: TranslateService,
		private dialog: MatDialog,
		public customTableService: CustomTableService,
		private usersService: UsersService,
		private rolesService: RolesService,
		private companiesService: CompaniesService,
		private router: Router,
		private bannerMessageService: BannerMessageService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.emailListsActions[value] = (emailList) => {
				this.deleteEmailList(emailList);
			};
			this.emailListsActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.rolesService.getRole(this.usersService.user.ROLE).subscribe(
			(roleResponse: any) => {
				// enable or disable actions depending on role permission
				this.emailListsActions.actions = this.customTableService.checkPermissionsForActions(
					roleResponse,
					this.emailListsActions,
					null
				);

				const columnsHeaders: string[] = [
					this.translateService.instant('NAME'),
				];
				const columnsHeadersObj = [
					{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
					// { DISPLAY: this.translateService.instant('USER'), NAME: 'USER' }
				];
				// only if there are actions to be shown. Actions are based on permissions
				if (this.emailListsActions.actions.length > 0) {
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
				this.getEmailLists();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	private getEmailLists() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;

		this.companiesService
			.getAllEmailLists(sortBy, orderBy, page + 1, pageSize)
			.subscribe(
				(response: any) => {
					this.customTableService.setTableDataSource(
						response.EMAIL_LISTS,
						response.TOTAL_RECORDS
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	public pageChangeEmit(event) {
		this.getEmailLists();
	}

	private deleteEmailList(emailList) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message: this.translateService.instant(
					'ARE_YOU_SURE_YOU_WANT_TO_DELETE_EMAIL_LIST'
				),
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			// TODO: implement  delete call here
			if (result === this.translateService.instant('DELETE')) {
				this.companiesService
					.deleteEmailList(emailList.EMAIL_LIST_ID)
					.subscribe(
						(response: any) => {
							this.getEmailLists();
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

	public rowClicked(rowData): void {
		this.router.navigate([
			`company-settings/marketing/email-lists/${rowData.EMAIL_LIST_ID}`,
		]);
	}

	public sortData() {
		this.getEmailLists();
	}

	public NewEmailList() {
		this.router.navigate([`company-settings/marketing/email-lists/new`]);
	}
}
