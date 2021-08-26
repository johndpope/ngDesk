import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { RolesService } from '@src/app/roles/roles.service';
import { DashboardsService } from '../dashboards.service';
import { DashboardApiService } from '@ngdesk/sam-api';

@Component({
	selector: 'app-dashboards-master',
	templateUrl: './dashboards-master.component.html',
	styleUrls: ['./dashboards-master.component.scss'],
})
export class DashboardsMasterComponent implements OnInit {
	public dashboardsActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	public roles;
	constructor(
		private translateService: TranslateService,
		private route: ActivatedRoute,
		private router: Router,
        private dialog: MatDialog,
        private customTableService: CustomTableService,
        private dashboardsService: DashboardsService,
		private bannerMessageService: BannerMessageService,
		private rolesService: RolesService,
		private dashboardService: DashboardApiService,
	) {
		this.translateService.get('DELETE').subscribe((value: string) => {
			this.dashboardsActions[value] = (row) => {
				this.deleteDashboard(row);
			};
			this.dashboardsActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.customTableService.isLoading = true;
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];

		columnsHeadersObj.push(
			{ DISPLAY: this.translateService.instant('NAME'), NAME: 'name' },
			{ DISPLAY: this.translateService.instant('ROLE'), NAME: 'roleName' },
			{ DISPLAY: this.translateService.instant('ACTION'), NAME: 'ACTION' }
		);
		columnsHeaders.push(
			this.translateService.instant('NAME'),
			this.translateService.instant('ROLE'),
			this.translateService.instant('ACTION')
		);

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
		this.getDashboards();
	}

	private getDashboards() {
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const query = `{
			dashboards: getDashboards(pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				name
				dashboardId: dashboardId
				role {
					roleId
					name
				}
			}
			totalCount: getDashboardsCount
		}`;

		this.dashboardsService.getAllDashboards(query).subscribe(
			(dashboardResponse: any) => {
				dashboardResponse.dashboards.forEach(dashboard => {
					dashboard['roleName'] = dashboard.role.name;
				});
				this.customTableService.setTableDataSource(
					dashboardResponse.dashboards,
					dashboardResponse.totalCount
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
		// this.router.navigate([`dashboards-new/${rowData.dashboardId}`,]);
	}

	public newDashboard(): void {
		// this.router.navigate([`dashboards-new/new`]);
	}

	private deleteDashboard(dashboard) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_DASHBOARD'
					) +
					dashboard.name +
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
				this.dashboardService.deleteDashboard(dashboard.dashboardId).subscribe((dashboardResponse: any) => {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('DELETED_SUCCESSFULLY'),
						});
						this.getDashboards();
					},	
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
				});
			}
		});
	}

	public sortData() {
		this.getDashboards();
	}

	public pageChangeEmit(event) {
		this.getDashboards();
	}
}