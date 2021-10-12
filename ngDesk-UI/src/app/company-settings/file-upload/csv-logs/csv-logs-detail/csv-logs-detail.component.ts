import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CsvLogsService } from './csv-logs-detail.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { TranslateService } from '@ngx-translate/core';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ModulesService } from '@src/app/modules/modules.service';
@Component({
	selector: 'app-csv-logs-detail',
	templateUrl: './csv-logs-detail.component.html',
	styleUrls: ['./csv-logs-detail.component.scss'],
})
export class CsvLogsDetailComponent implements OnInit {
	public logs = [];
	public fileName = '';
	public table: boolean;
	public failedCount: number;
	public completedCount: number;
	public logId;
	constructor(
		private route: ActivatedRoute,
		private csvLogsService: CsvLogsService,
		private bannerMessageService: BannerMessageService,
		public modulesService: ModulesService,
		private translateService: TranslateService,
		public customTableService: CustomTableService,
		private router: Router
	) {}

	public ngOnInit() {
		this.logId = this.route.snapshot.params['dataId'];
		const columnsHeaders: string[] = [
			this.translateService.instant('lINE_NUMBER'),
			this.translateService.instant('ERROR_MESSAGE'),
		];
		const columnsHeadersObj = [
			{
				DISPLAY: this.translateService.instant('lINE_NUMBER'),
				NAME: 'lineNumber',
			},
			{
				DISPLAY: this.translateService.instant('ERROR_MESSAGE'),
				NAME: 'errorMessage',
			},
		];
		if (!this.customTableService.sortBy) {
			this.customTableService.sortBy = 'lineNumber';
		}
		if (!this.customTableService.sortOrder) {
			this.customTableService.sortOrder = 'asc';
		}
		if (!this.customTableService.pageIndex) {
			this.customTableService.pageIndex = 0;
		}
		if (!this.customTableService.pageSize) {
			this.customTableService.pageSize = 10;
		}
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: this.translateService.instant('lINE_NUMBER'),
			NAME: 'lineNumber',
		};

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.isLoading = true;
		this.getLogs();
		this.csvLogsService.getCsvImport(this.logId).subscribe(
			(response: any) => {
				this.failedCount = response.DATA.failedCount;
				this.completedCount = response.DATA.completedCount;
				console.log('1', response);
				this.fileName = response.DATA.csvImportData.fileName;
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public getLogs() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.csvLogsService
			.getAllImports(this.logId, page, pageSize, sortBy, orderBy)
			.subscribe(
				(csvImportResponse: any) => {
					console.log('csvImportResponse', csvImportResponse);
					this.customTableService.setTableDataSource(
						csvImportResponse.DATA,
						csvImportResponse.TOTAL_RECORDS
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	public sortData(event) {
		this.getLogs();
	}

	public pageChangeEmit(event) {
		this.getLogs();
	}

	public ngOnDestroy() {
		this.customTableService.sortBy = undefined;
		this.customTableService.sortOrder = undefined;
		this.customTableService.pageIndex = undefined;
		this.customTableService.pageSize = undefined;
	}
	public openTable() {
		this.table = true;
	}
}
