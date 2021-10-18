import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { ModulesService } from 'src/app/modules/modules.service';
import { FileUploadComponent } from './../file-upload.component';
import { CsvLogsService } from './csv-logs-detail/csv-logs-detail.service';

@Component({
	selector: 'app-csv-logs',
	templateUrl: './csv-logs.component.html',
	styleUrls: ['./csv-logs.component.scss'],
})
export class CsvLogsComponent implements OnInit, OnDestroy {
	public importFailed: boolean;
	public fileName = '';
	constructor(
		public modulesService: ModulesService,
		private dialog: MatDialog,
		private translateService: TranslateService,
		public customTableService: CustomTableService,
		private router: Router,
		private csvLogsService: CsvLogsService,
		private bannerMessageService: BannerMessageService
	) {}
	public dialogRef: MatDialogRef<FileUploadComponent>;
	public ngOnInit() {
		const columnsHeaders: string[] = [
			this.translateService.instant('NAME'),
			this.translateService.instant('STATUS'),
			this.translateService.instant('DATE_CREATED'),
		];
		const columnsHeadersObj = [
			{
				DISPLAY: this.translateService.instant('NAME'),
				NAME: 'name',
			},
			{
				DISPLAY: this.translateService.instant('STATUS'),
				NAME: 'status',
			},
			{
				DISPLAY: this.translateService.instant('DATE_CREATED'),
				NAME: 'dateCreated',
			},
		];
		if (!this.customTableService.sortBy) {
			this.customTableService.sortBy = 'name';
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
			SORT_BY: this.translateService.instant('NAME'),
			NAME: 'name',
		};

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.isLoading = true;
		this.getCsvLogs();
	}

	public getCsvLogs() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.csvLogsService
			.getAllCsvImports(page, pageSize, sortBy, orderBy)
			.subscribe(
				(csvImportResponse: any) => {
					this.customTableService.setTableDataSource(
						csvImportResponse.DATA,
						csvImportResponse.TOTAL_RECORDS
					);
					if (csvImportResponse.TOTAL_RECORDS === 0) {
						this.import();
					}
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	public rowClicked(event) {
		if (event.status === 'FAILED') {
			this.router.navigate([
				`company-settings/file-upload/csv-logs/${event.csvImportId}`,
			]);
		}
	}

	public import() {
		this.dialogRef = this.dialog.open(FileUploadComponent, {
			data: {},
			height: '320',
			width: '520px',
		});
		this.dialogRef.afterClosed().subscribe((result) => {
			if (result && result.data !== 'Cancel') {
				this.ngOnInit();
			}
		});
	}

	public sortData(event) {
		this.getCsvLogs();
	}

	public pageChangeEmit(event) {
		this.getCsvLogs();
	}

	public ngOnDestroy() {
		this.customTableService.sortBy = undefined;
		this.customTableService.sortOrder = undefined;
		this.customTableService.pageIndex = undefined;
		this.customTableService.pageSize = undefined;
	}
}
