import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {
	CsvHeaders,
	CsvImport,
	CsvImportApiService,
	CsvImportData,
} from '@ngdesk/data-api';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { ModulesService } from 'src/app/modules/modules.service';
@Component({
	selector: 'app-csv-import-dialog',
	templateUrl: './csv-import-dialog.component.html',
	styleUrls: ['./csv-import-dialog.component.scss'],
})
export class CsvImportDialogComponent implements OnInit {
	public headers = [];
	public fields = [];
	public selectedFields = [];
	public headerMap = {};
	public rowObject = [];
	public csvheaders: CsvHeaders = {
		fieldId: '',
		headerName: '',
	};
	public csvImportData: CsvImportData = {
		file: '',
		fileType: '',
		fileName: '',
		headers: [],
	};
	public CsvImport: CsvImport = {
		csvImportData: {},
		separator: '',
	};

	public selectedSeparator;
	public separators = ['-', 'Blank space'];
	public selectedDateTimeFormat;
	public dateTimeFormat = [
		'dd/MM/yyyy hh:mm:ss',
		'dd-MM-yyyy hh:mm:ss',
		'MM/dd/yyyy hh:mm:ss',
		'MM-dd-yyyy hh:mm:ss',
	];
	public selectedDate;
	public dateFormat = [
		'dd/MM/yyyy',
		'MM/dd/yyyy',
		'dd-MM-yyyy',
		'MM-dd-yyyy',
		'dd MMM yyyy',
		'dd MMMM yyyy',
		'MMM dd, yyyy',
		'MMMM dd, yyyy',
	];
	public selectedTimeFormat;
	public timeFormat = ['h:mm', 'h:mm:ss'];

	constructor(
		@Inject(MAT_DIALOG_DATA) public data: any,
		public dialogRef: MatDialogRef<CsvImportDialogComponent>,
		public modulesService: ModulesService,
		public bannerMessageService: BannerMessageService,
		public translateService: TranslateService,
		private csvImportApiService: CsvImportApiService
	) {}

	public ngOnInit() {
		this.headers = this.data.csvData.HEADERS;
		this.csvImportData = this.data.csvData.CSV_IMPORT_DATA;
		let i = 0;
		const selectedFieldsDropdown = [];
		this.data.csvData.MODULE.FIELDS.forEach((field) => {
			if (
				!field.NOT_EDITABLE &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'Checkbox' &&
				field.NAME !== 'ROLE'
			) {
				this.fields.push(field);
				if (this.headers && this.headers.includes(field.DISPLAY_LABEL)) {
					selectedFieldsDropdown[i] = field.DISPLAY_LABEL;
				}
				i++;
			}
		});
		this.selectedFields = selectedFieldsDropdown;
	}

	public onImportCsv(): void {
		if (Object.keys(this.headerMap).length === 0) {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('EMPTY_MAPPING'),
			});
		} else {
			for (let [key, value] of Object.entries(this.headerMap)) {
				if (`${value}` != undefined) {
					this.csvheaders = {
						fieldId: `${key}`,
						headerName: `${value}`,
					};
					this.csvImportData.headers.push(this.csvheaders);
				}
			}
			const moduleId = this.data.csvData.MODULE.MODULE_ID;
			this.csvImportData = this.data.csvData.CSV_IMPORT_DATA;
			this.CsvImport.csvImportData = this.csvImportData;
			this.CsvImport.separator = this.selectedSeparator;
			this.csvImportApiService
				.importFromCsv(moduleId, this.CsvImport)
				.subscribe(
					(response: any) => {
						console.log('response', response);
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('IMPORTED_SUCCESSFULLY'),
						});
						this.dialogRef.close({ data: this.data });
					},
					(error) => {
						console.error(error);
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		}
	}
}
