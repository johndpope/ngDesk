import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { EnterpriseSearchApiService } from '@ngdesk/sam-api';
import { EnterpriseSearchService } from '@src/app/company-settings/software-asset-management/enterprise-search/enterprise-search.service';

@Component({
	selector: 'app-enterprise-search-master',
	templateUrl: './enterprise-search-master.component.html',
	styleUrls: ['./enterprise-search-master.component.scss'],
})
export class EnterpriseSearchMasterComponent implements OnInit {
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public enterpriseSearchActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	public buttonDisabled = false;
	public editAccess = false;
	// public savedFields: any[] = [];

	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private dialog: MatDialog,
		public customTableService: CustomTableService,
		private router: Router,
		private enterpriseSearchService: EnterpriseSearchService,
		private enterpriseSearchApiService: EnterpriseSearchApiService,
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.enterpriseSearchActions[value] = (enterpriseSearch) => {
				this.deleteEnterpriseSearch(enterpriseSearch);
			};
			this.enterpriseSearchActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		columnsHeadersObj.push(
			{ DISPLAY: this.translateService.instant('NAME'), NAME: 'name' },
			{ DISPLAY: this.translateService.instant('DESCRIPTION'), NAME: 'description' },
			{ DISPLAY: this.translateService.instant('ACTION'), NAME: 'ACTION' }
		);
		columnsHeaders.push(
			this.translateService.instant('NAME'),
			this.translateService.instant('DESCRIPTION'),
			this.translateService.instant('ACTION')
		);
		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.sortBy = 'NAME';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: this.translateService.instant('NAME'),
			NAME: 'NAME',
		};

		this.getEnterpriseSearch();
		
	}

	public getEnterpriseSearch() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.enterpriseSearchService
		.getAllEnterpriseSearch(page, pageSize, sortBy, orderBy)
		.subscribe(
			(enterpriseSearchResponse: any) => {
				this.customTableService.setTableDataSource(
					enterpriseSearchResponse.enterpriseSearch,
					enterpriseSearchResponse.totalCount
				);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	// public searchValuesChange(searchParams: any[]) {
	// 	this.savedFields = searchParams;
	// 	if (
	// 		(searchParams.length > 0 &&
	// 			searchParams[searchParams.length - 1]['TYPE'] !== 'field') ||
	// 		searchParams.length === 0
	// 	) {
	// 		this.customTableService.pageIndex = 0;
	// 		this.searchPiiDiscoveryMap();
	// 	}
	// }

	// public searchPiiDiscoveryMap() {
	// 	const searchString = this.convertSearchString(this.savedFields);

	// 	const sort = [
	// 		this.customTableService.sortBy + ',' + this.customTableService.sortOrder,
	// 	];
	// 	if (searchString && searchString !== '' && searchString !== null) {

	// 	} else {

	// 	}
	// }

	// private convertSearchString(searchParams?: any[]): string | null {
	// 	let searchString = '';
	// 	if (searchParams && searchParams.length > 0) {
	// 		searchParams.forEach((param, index) => {
	// 			if (param['TYPE'] === 'field' && searchParams[index + 1]) {
	// 				const field = param['NAME'];
	// 				const value = searchParams[index + 1]['VALUE'];
	// 				if (searchString === '') {
	// 					searchString = `${field}=${value}`;
	// 				} else {
	// 					searchString += `~~${field}=${value}`;
	// 				}
	// 			} else if (param['TYPE'] === 'global') {
	// 				searchString = param['VALUE'];
	// 			}
	// 		});
	// 		if (searchString !== '') {
	// 			return searchString;
	// 		} else {
	// 			return null;
	// 		}
	// 	} else {
	// 		return null;
	// 	}
	// }

	private deleteEnterpriseSearch(enterpriseSearch) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_ENTERPRISE_SEARCH'
					) +
					enterpriseSearch.name +
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
				this.enterpriseSearchApiService.deleteEnterpriseSearch(enterpriseSearch.enterpriseSearchId).subscribe(
					(enterpriseSearchResponse: any) => {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('DELETED_SUCCESSFULLY'),
						});
						this.getEnterpriseSearch();
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
		this.getEnterpriseSearch();
	}

	public pageChangeEmit(event) {
		this.getEnterpriseSearch();
	}

	public rowClicked(rowData): void {
        this.router.navigate([`company-settings/enterprise-search/${rowData.enterpriseSearchId}`]);
	}

	public newEnterpriseSearch() {
		this.router.navigate([`company-settings/enterprise-search/new`]);
	}
}
