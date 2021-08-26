import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';

@Component({
	selector: 'app-file-rule-master',
	templateUrl: './file-rule-master.component.html',
	styleUrls: ['./file-rule-master.component.scss'],
})
export class FileRuleMasterComponent implements OnInit {
	constructor(
		private translateService: TranslateService,
		public customTableService: CustomTableService,
		private router: Router,
		private http: HttpClient,
		private globals: AppGlobals,
	) {}

	public ngOnInit() {
		// Initialize table headers.
		this.initializeHeaders();

		// Initialize the sam rules.

		// Get all the file rules.
		this.getFileRules();
	}

	// This function initializes the table headers.
	public initializeHeaders() {
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('FILE_NAME'),
			NAME: 'FILE_NAME',
		});
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('FILE_PATH'),
			NAME: 'FILE_PATH',
		});
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('DATE_CREATED'),
			NAME: 'DATE_CREATED',
		});
		columnsHeaders.push(this.translateService.instant('FILE_NAME'));
		columnsHeaders.push(this.translateService.instant('FILE_PATH'));
		columnsHeaders.push(this.translateService.instant('DATE_CREATED'));
		this.customTableService.sortBy = 'fileName';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
	}

	// Get list of rules to show all in the table
	public getFileRules() {
		let query = `{
			DATA: getSamFileRules(pageNumber: ${this.customTableService.pageIndex}, pageSize: ${this.customTableService.pageSize}, sortBy: "${this.customTableService.sortBy}", orderBy: "${this.customTableService.sortOrder}") {
				FILE_NAME: fileName
				FILE_PATH: filePath
				DATE_CREATED: dateCreated
				DATA_ID: id
			}
			TOTAL_RECORDS: getSamFileRulesCount
		}`;
		this.graphqlQuery(query).subscribe((response: any)=>{
			this.customTableService.isLoading = false;
			this.customTableService.setTableDataSource(response.DATA, response.TOTAL_RECORDS);
		});
		
	}

	// Navigate to detail page to create an entry.
	public createNewRule() {
		this.router.navigate([`company-settings/file-rules/new`]);
	}

	// Executed when a row is clicked.
	public rowClicked(event) {
		this.router.navigate([`company-settings/file-rules/${event.DATA_ID}`]);
	}

	// Executed when sort button is clicked.
	public sortData() {
		switch (this.customTableService.sortBy) {
			case 'FILE_NAME':
				this.customTableService.sortBy = 'fileName'
				break;
			case 'FILE_PATH':
				this.customTableService.sortBy = 'filePath'
				break;
			case 'DATE_CREATED':
				this.customTableService.sortBy = 'dateCreated'
				break;		
		}
		this.getFileRules();
	}

	// Called when click on the pagination button.
	public pageChangeEmit(event) {
		this.getFileRules();
	}

	public graphqlQuery(query: string) {
		return this.http.post(
			`${this.globals.graphqlUrl}`,
			query
		);
	}
}
