import { Component, OnInit } from '@angular/core';

import { MatDialog } from '@angular/material/dialog';
import { CompaniesService } from '../companies/companies.service';
import { BannerMessageService } from '../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../custom-table/custom-table.service';
import { InviteUsersDialogComponent } from '../dialogs/invite-users-dialog/invite-users-dialog.component';
import { UserApiService } from '@ngdesk/data-api';

@Component({
	selector: 'app-manage-invites',
	templateUrl: './manage-invites.component.html',
	styleUrls: ['./manage-invites.component.scss']
})
export class ManageInvitesComponent implements OnInit {
	public manageActions = {
		resend: row => {
			this.performAction(row);
		},
		actions: [{ NAME: 'Resend', ICON: 'send', PERMISSION_NAME: 'RESEND' }]
	};

	constructor(
		private bannerMessageService: BannerMessageService,
		private dialog: MatDialog,
		public customTableService: CustomTableService,
		private companiesService: CompaniesService,
		private userApiService: UserApiService
	) {}

	public ngOnInit() {
		this.customTableService.isLoading = true;
		const columnsHeaders: string[] = ['First Name', 'Last Name', 'Email'];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [
			{ DISPLAY: 'First Name', NAME: 'FIRST_NAME' },
			{ DISPLAY: 'Last Name', NAME: 'LAST_NAME' },
			{ DISPLAY: 'Email', NAME: 'EMAIL_ADDRESS' }
		];

		// only if there are actions to be shown. Actions are based on permissions
		if (this.manageActions.actions.length > 0) {
			columnsHeadersObj.push({ DISPLAY: 'Action', NAME: 'ACTION' });
			columnsHeaders.push('Action');
		}

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;

		this.customTableService.sortBy = 'FIRST_NAME';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: 'First Name',
			NAME: 'FIRST_NAME'
		};
		this.getInvites();
	}

	private performAction(row) {
		const invite = {
			USERS: [
				{
					EMAIL_ADDRESS: row.EMAIL_ADDRESS,
					FIRST_NAME: row.FIRST_NAME,
					LAST_NAME: row.LAST_NAME,
					ROLE: row.ROLE
				}
			]
		};
		this.companiesService.postResendInvites(invite).subscribe(
			(response: any) => {
				this.bannerMessageService.successNotifications.push({
					message: 'Invite sent successfully'
				});
				this.getInvites();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR
				});
			}
		);
	}

	public getInvites() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;

		this.userApiService
			.pendingInvitesAPI(page, pageSize, ["result_ad." + sortBy + "," + orderBy])
			.subscribe(
				(response: any) => {
					this.customTableService.setTableDataSource(
						response.content,
						response.totalElements
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR
					});
				}
			);
	}

	public sortData(event) {
		this.getInvites();
	}

	public pageChangeEmit(event) {
		this.getInvites();
	}

	public inviteUsers() {
		const dialogRef = this.dialog.open(InviteUsersDialogComponent, {});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe(result => {
			// reload the users table after dialog closes
			this.getInvites();
		});
	}
}
