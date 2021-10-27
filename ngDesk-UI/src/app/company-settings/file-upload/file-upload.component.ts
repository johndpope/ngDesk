import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { CsvImportDialogComponent } from 'src/app/dialogs/csv-import-dialog/csv-import-dialog.component';
import { ModulesService } from 'src/app/modules/modules.service';
import { read, utils } from 'xlsx';
import { Router } from '@angular/router';

@Component({
	selector: 'app-file-upload',
	templateUrl: './file-upload.component.html',
	styleUrls: ['./file-upload.component.scss'],
})
export class FileUploadComponent implements OnInit {
	public fileUploadQueue;
	public modules: any = [];
	public selectedModule;
	public errorMessage: string;
	public matchedFields: any = [];
	public fieldsForDropdown: any = [];
	public unmatchedFields: any = [];
	public rowObject: [];
	public headers: [];
	public csvImportData = {
		FILE: '',
		FILE_TYPE: '',
		FILE_NAME: '',
		HEADERS: {},
	};

	constructor(
		private modulesService: ModulesService,
		private translateService: TranslateService,
		private dialog: MatDialog,
		public dialogRef: MatDialogRef<FileUploadComponent>,
		private router: Router
	) {}

	public ngOnInit() {
		this.modulesService.getModules().subscribe((moduleResponse: any) => {
			this.modules = moduleResponse.MODULES.sort((a, b) =>
				a.NAME.localeCompare(b.NAME)
			);
			// REMOVING TEAMS MODULE
			this.modules = this.modules.filter(
				(module) => module.NAME !== 'Teams' && module.NAME !== 'Chats'
			);
		});
	}

	public fileChangeListener(event: any) {
		this.fileUploadQueue = event.target.files[0];
		const fileType = this.fileUploadQueue.name.split('.')[1];
		const fileNAme = this.fileUploadQueue.name.split('.')[0];
		const reader = new FileReader();
		const [file] = event.target.files;
		if (fileType === 'csv' || fileType === 'xlsx' || fileType === 'xls') {
			reader.readAsDataURL(file);
			reader.onload = () => {
				const data: any = reader.result;
				this.csvImportData = {
					FILE: data.split('base64,')[1],
					FILE_TYPE: fileType,
					FILE_NAME: fileNAme,
					HEADERS: {},
				};
			};
		} else {
			this.errorMessage =
				'This file is not supported, Please try to upload a csv file';
		}
	}

	public uploadCSV() {
		this.errorMessage = '';
		if (
			!this.selectedModule ||
			this.selectedModule == null ||
			this.selectedModule === ''
		) {
			this.errorMessage = 'Please Select a Module';
		} else if (
			!this.fileUploadQueue ||
			this.fileUploadQueue == null ||
			this.fileUploadQueue === ''
		) {
			this.errorMessage = 'Please Select a file to upload';
		} else {
			const moduleId = this.selectedModule.MODULE_ID;
			this.modulesService
				.getCsvHeaders(moduleId, this.csvImportData)
				.subscribe((response: any) => {
					this.headers = response;
					const csvData = {
						HEADERS: this.headers,
						MODULE: this.selectedModule,
						CSV_IMPORT_DATA: this.csvImportData,
					};
					const dialogRef = this.dialog.open(CsvImportDialogComponent, {
						data: {
							csvData: csvData,
							dialogTitle: this.translateService.instant('IMPORT_FROM_CSV'),
							buttonText: this.translateService.instant('IMPORT'),
							action: this.translateService.instant('IMPORT'),
							closeDialog: this.translateService.instant('CANCEL'),
							executebuttonColor: 'warn',
						},
					});
					dialogRef.afterClosed().subscribe((result) => {
						this.onNoClick();
						this.dialogRef.close({ data: result });
					});
				});
		}
	}

	public openLogs() {
		this.router.navigate([`company-settings/file-upload/csv-logs`]);
	}

	public onNoClick(): void {
		this.dialog.closeAll();
	}
}
