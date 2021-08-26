import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../custom-table/custom-table.service';

import { SchedulesService } from '@src/app/schedules/schedules.service';
import { ConfirmDialogComponent } from '../../dialogs/confirm-dialog/confirm-dialog.component';
import { RolesService } from '../../roles/roles.service';
import { UsersService } from '../../users/users.service';
@Component({
	selector: 'app-schedules-master',
	templateUrl: './schedules-master.component.html',
	styleUrls: ['./schedules-master.component.scss'],
})
export class SchedulesMasterComponent implements OnInit {
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public schedulesActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	public buttonDisabled = false;
	public editAccess = false;

	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private dialog: MatDialog,
		private schedulesService: SchedulesService,
		private companiesService: CompaniesService,
		public customTableService: CustomTableService,
		private router: Router,
		private usersService: UsersService,
		private rolesService: RolesService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.schedulesActions[value] = (schedule) => {
				this.deleteSchedule(schedule);
			};
			this.schedulesActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		const roleId = this.usersService.user.ROLE;
		this.rolesService.getRole(roleId).subscribe(
			(roleResponse: any) => {
				this.editAccess = this.usersService.checkPermission(
					roleResponse,
					'Schedules',
					'EDIT'
				);
				// enable or disable actions depending on role permission
				this.schedulesActions.actions = this.customTableService.checkPermissionsForActions(
					roleResponse,
					this.schedulesActions,
					'Schedules'
				);

				const columnsHeaders: string[] = [
					this.translateService.instant('NAME'),
				];
				const columnsHeadersObj = [
					{ DISPLAY: this.translateService.instant('NAME'), NAME: 'name' },
				];
				// only if there are actions to be shown. Actions are based on permissions
				if (this.schedulesActions.actions.length > 0) {
					columnsHeadersObj.push({
						DISPLAY: this.translateService.instant('ACTION'),
						NAME: 'ACTION',
					});
					columnsHeaders.push(this.translateService.instant('ACTION'));
				}
				this.customTableService.pageIndex = 0;
				this.customTableService.pageSize = 10;
				this.customTableService.sortBy = 'name';
				this.customTableService.sortOrder = 'asc';
				this.customTableService.activeSort = {
					ORDER_BY: 'asc',
					SORT_BY: this.translateService.instant('NAME'),
					NAME: 'NAME',
				};

				this.customTableService.columnsHeaders = columnsHeaders;
				this.customTableService.columnsHeadersObj = columnsHeadersObj;
				this.customTableService.isLoading = true;
				this.getSchedules();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	// Fetching all the schedules based on page numebr and page size.
	public getSchedules() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		const query = `{
			schedules: getSchedules(pageNumber: ${page}, pageSize: ${pageSize}) {
				name
				scheduleId: id
			}
			totalCount: countSchedules
		}`;

		this.schedulesService.getAllSchedules(query).subscribe(
			(schedulesResponse: any) => {
				console.log(schedulesResponse);
				this.customTableService.setTableDataSource(
					schedulesResponse.schedules,
					schedulesResponse.totalCount
				);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	private deleteSchedule(schedule) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_SCHEDULE'
					) +
					schedule.name +
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
				this.companiesService
					.deleteSchedules({ IDS: [schedule.scheduleId] })
					.subscribe(
						(schedulesResponse: any) => {
							this.companiesService.trackEvent(`Deleted Schedule`, {
								SCHEDULE_ID: schedule.scheduleId,
							});
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('DELETED_SUCCESSFULLY'),
							});
							this.getSchedules();
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
		this.getSchedules();
	}

	public pageChangeEmit(event) {
		this.getSchedules();
	}

	public rowClicked(rowData): void {
		this.router.navigate([`schedules/${rowData.scheduleId}`], {
			queryParams: {
			  scheduleName: rowData.name
			},
			queryParamsHandling: 'merge',
		  });
	}

	public newSchedule() {
		this.router.navigate([`schedules/new`]);
	}
}
