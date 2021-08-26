import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { delay } from 'rxjs/operators';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { ModulesService } from 'src/app/modules/modules.service';

@Component({
	selector: 'app-csv-import-dialog',
	templateUrl: './csv-import-dialog.component.html',
	styleUrls: ['./csv-import-dialog.component.scss']
})
export class CsvImportDialogComponent implements OnInit {
	public headers = [];
	public fields = [];
	public selectedFields = [];
	public headerMap = {};
	public rowObject = [];
	public csvImportData = {
		FILE: '',
		FILE_TYPE: '',
		FILE_NAME: '',
		HEADERS: {}
	};
	constructor(
		@Inject(MAT_DIALOG_DATA) public data: any,
		public dialogRef: MatDialogRef<CsvImportDialogComponent>,
		public modulesService: ModulesService,
		public bannerMessageService: BannerMessageService,
		public translateService: TranslateService
	) {}

	public ngOnInit() {
		this.headers = this.data.csvData.HEADERS;
		this.csvImportData = this.data.csvData.CSV_IMPORT_DATA;
		let i = 0;
		const selectedFieldsDropdown = [];
		this.data.csvData.MODULE.FIELDS.forEach(field => {
			if (
				!field.NOT_EDITABLE &&
				field.DATA_TYPE.DISPLAY !== 'Discussion' &&
				field.DATA_TYPE.DISPLAY !== 'Relationship' &&
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
				message: this.translateService.instant('EMPTY_MAPPING')
			});
		} else {
			const moduleId = this.data.csvData.MODULE.MODULE_ID;
			this.csvImportData = this.data.csvData.CSV_IMPORT_DATA;
			this.csvImportData.HEADERS = this.headerMap;
			this.modulesService.ImportCSV(moduleId, this.csvImportData).subscribe(
				(response: any) => {
					this.bannerMessageService.successNotifications.push({
						message: this.translateService.instant('IMPORTED_SUCCESSFULLY')
					});
					this.dialogRef.close({ data: this.data });
				},
				error => {
					console.error(error);
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR
					});
				}
			);
		}
	}
}
