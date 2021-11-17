import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { Escalation, EscalationApiService } from '@ngdesk/escalation-api';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '@src/app/companies/companies.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { RolesService } from '@src/app/roles/roles.service';
import { UsersService } from '@src/app/users/users.service';
import { EscalationsService } from '../escalations.service';

@Component({
	selector: 'app-escalations-master',
	templateUrl: './escalations-master.component.html',
	styleUrls: ['./escalations-master.component.scss']
})
export class EscalationsMasterComponent implements OnInit {
	public escalations: Escalation[] = [];
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public escalationsActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }]
	};
	public buttonDisabled = false;
	public editAccess = false;
	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private dialog: MatDialog,
		private router: Router,
		private companiesService: CompaniesService,
		public customTableService: CustomTableService,
		private rolesService: RolesService,
		private usersService: UsersService,
		private escalationApiService: EscalationApiService,
		public escalationService: EscalationsService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.escalationsActions[value] = row => {
				this.deleteEscalation(row);
			};
			this.escalationsActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		const roleId = this.usersService.user.ROLE;
		this.rolesService.getRole(roleId).subscribe(
			(roleResponse: any) => {
				this.editAccess = this.usersService.checkPermission(
					roleResponse,
					'Escalations',
					'EDIT'
				);
				// enable or disable actions depending on role permission
				this.escalationsActions.actions = this.customTableService.checkPermissionsForActions(
					roleResponse,
					this.escalationsActions,
					'Escalations'
				);

				this.customTableService.isLoading = true;
				const columnsHeaders: string[] = [];
				const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];

				// only if there are actions to be shown. Actions are based on permissions
				columnsHeadersObj.push({
					DISPLAY: this.translateService.instant('NAME'),
					NAME: 'name'
				});
				columnsHeaders.push(this.translateService.instant('NAME'));
				if (this.escalationsActions.actions.length > 0) {
					columnsHeadersObj.push({
						DISPLAY: this.translateService.instant('ACTION'),
						NAME: 'ACTION'
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
					NAME: 'NAME'
				};
				this.getEscalations();
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	// Fetching all the schedules based on page numebr and page size.
	public getEscalations() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		const query = `{
			escalations: getEscalations(pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				name
				escalationId
			}
			totalCount: getEscalationsCount
		}`;

		this.escalationService.getAllEscalations(query).subscribe(
			(escalationsResponse: any) => {
				this.customTableService.setTableDataSource(
					escalationsResponse.escalations,
					escalationsResponse.totalCount
				);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	private deleteEscalation(escalation) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_ESCALATION'
					) +
					escalation.NAME +
					' ?',
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn'
			}
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe(result => {
			if (result === this.translateService.instant('DELETE')) {
				this.escalationApiService
					.deleteEscalation(escalation.escalationId)
					.subscribe(
						(escalationsResponse: any) => {
							this.companiesService.trackEvent(`Deleted Escalation`, {
								ESCALATION_ID: escalation.escalationId
							});
							this.getEscalations();
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR
							});
						}
					);
			}
		});
	}

	public rowClicked(rowData): void {
		// clicking on table row will redirect to escalation detail
		this.router.navigate([`escalations/${rowData.escalationId}`]);
	}

	public newEscalation(): void {
		// clicking on new escalation button will redirect to escalation detail
		this.router.navigate([`escalations/new`]);
	}

	public sortData() {
		this.getEscalations();
	}

	public pageChangeEmit(event) {
		this.getEscalations();
	}
}
