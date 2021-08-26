import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { HttpClient } from '@angular/common/http';
import { CompaniesService } from '../../companies/companies.service';
import { RolesService } from '@src/app/roles/roles.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../custom-table/custom-table.service';
import { UsersService } from '../../users/users.service';
import { AppGlobals } from '@src/app/app.globals';
import { ReportApiService } from '@ngdesk/report-api';

@Component({
	selector: 'app-report-master',
	templateUrl: './report-master.component.html',
	styleUrls: ['./report-master.component.scss'],
})
export class ReportMasterComponent implements OnInit {
	public reportsAction = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		public customTableService: CustomTableService,
		private rolesService: RolesService,
		private usersService: UsersService,
		private router: Router,
		private reportApiService: ReportApiService,
		private http: HttpClient,
		private globals: AppGlobals,
		private companiesService: CompaniesService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.reportsAction[value] = (report) => {
				this.deleteReport(report);
			};
			this.reportsAction.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.rolesService.getRole(this.usersService.user.ROLE).subscribe(
			(roleResponse: any) => {
				const columnsHeaders: string[] = [
					this.translateService.instant('NAME'),
				];
				const columnsHeadersObj = [
					{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
				];

				this.reportsAction.actions = this.customTableService.checkPermissionsForActions(
					roleResponse,
					this.reportsAction,
					'Reports',
					true
				);

				if (this.reportsAction.actions.length > 0) {
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
				this.getReports();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	private getReports() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		let query = `{
			DATA: getReports(pageNumber: ${this.customTableService.pageIndex}, 
			pageSize: ${this.customTableService.pageSize}, sortBy: "${this.customTableService.sortBy}", orderBy: "${this.customTableService.sortOrder}") {
				REPORT_ID: reportId
				NAME: reportName
			}
			COUNT: getReportsCount
		}`;
		this.reportsQuery(query).subscribe(
			(reportsResponse: any) => {
				console.log(reportsResponse);
				this.customTableService.setTableDataSource(
					reportsResponse.DATA,
					reportsResponse.COUNT
				);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);	
	}

	private deleteReport(report) {
		this.reportApiService.deleteReport(report.REPORT_ID).subscribe(
			(reportsResponse: any) => {
				this.companiesService.trackEvent(`Deleted Report`, {
					REPORT_ID: report.REPORT_ID,
				});
				this.getReports();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public newReport() {
		this.router.navigate([`reports/new`]);
	}

	public rowClicked(rowData): void {
		this.router.navigate([`reports/${rowData.REPORT_ID}`]);
	}

	public sortData() {
		this.getReports();
	}

	public pageChangeEmit(event) {
		this.getReports();
	}

	public reportsQuery(query: string) {
		return this.http.post(
			`${this.globals.graphqlUrl}`,
			query
		);
	}
}
