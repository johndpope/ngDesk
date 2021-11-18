import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {
	CsvHeaders,
	CsvImport,
	CsvImportApiService,
	CsvImportData,
} from '@ngdesk/data-api';
import { TranslateService } from '@ngx-translate/core';
import { CsvFormat } from 'ngdesk-swagger/data-api';
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
		csvFormat: {},
	};

	public csvFormat: CsvFormat = {
		separator: '',
		dateFormat: '',
		timeFormat: '',
		dateTimeFormat: '',
	};
	public selectedSeparator;
	public separators = ['-', 'Blank space'];
	public selectedDateTimeFormat;
	public dateTimeFormat = [
		'dd/mm/yyyy hh:mm:ss',
		'dd-mm-yyyy hh:mm:ss',
		'mm/dd/yyyy hh:mm:ss',
		'mm-dd-yyyy hh:mm:ss',
	];
	public selectedDate;
	public dateFormat = [
		'dd/mm/yyyy',
		'mm/dd/yyyy',
		'dd-mm-yyyy',
		'mm-dd-yyyy',
		'dd mmm yyyy',
		'dd mmmm yyyy',
		'mmm dd, yyyy',
		'mmmm dd, yyyy',
	];
	public selectedTimeFormat;
	public timeFormat = ['h:mm', 'h:mm:ss'];
	public dateField: boolean;
	public phoneField: boolean;
	public dateTimeField: boolean;
	public timeField: boolean;
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
				field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
				field.DATA_TYPE.DISPLAY !== 'Approval' &&
				field.DATA_TYPE.DISPLAY !== 'Button' &&
				field.DATA_TYPE.DISPLAY !== 'Checkbox' &&
				field.DATA_TYPE.DISPLAY !== 'Currency Exchange' &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'File Preview' &&
				field.DATA_TYPE.DISPLAY !== 'Image' &&
				field.DATA_TYPE.DISPLAY !== 'Password' &&
				field.DATA_TYPE.DISPLAY !== 'Receipt Capture' &&
				field.DATA_TYPE.DISPLAY !== 'Zoom' &&
				field.NAME !== 'ROLE'
			) {
				this.fields.push(field);
				if (field.DATA_TYPE.DISPLAY === 'Date') {
					this.dateField = true;
				} else if (field.DATA_TYPE.DISPLAY === 'Phone') {
					this.phoneField = true;
				} else if (field.DATA_TYPE.DISPLAY === 'Date/Time') {
					this.dateTimeField = true;
				} else if (field.DATA_TYPE.DISPLAY === 'Time') {
					this.timeField = true;
				}
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
			this.csvFormat = {
				separator: this.selectedSeparator,
				dateFormat: this.selectedDate,
				timeFormat: this.selectedTimeFormat,
				dateTimeFormat: this.selectedDateTimeFormat,
			};
			this.CsvImport.csvFormat = this.csvFormat;
			this.csvImportApiService
				.importFromCsv(moduleId, this.CsvImport)
				.subscribe(
					(response: any) => {
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
