import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ServicecatalogueApiService } from '@ngdesk/module-api';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { CatalogueDetailService } from '../catalogue-detail/catalogue-detail.service';

@Component({
	selector: 'app-catalogue-master',
	templateUrl: './catalogue-master.component.html',
	styleUrls: ['./catalogue-master.component.scss']
})
export class CatalogueMasterComponent implements OnInit {

	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public catalogueActions = {
		actions: [
			{
				NAME: '',
				ICON: 'delete',
				PERMISSION_NAME: 'DELETE',
			},
		],
	};

	public isLoading = true;
	public totalRecords = 1;

	constructor(
		private translateService: TranslateService,
		private dialog: MatDialog,
		private router: Router,
		public customTableService: CustomTableService,
		private bannerMessageService: BannerMessageService,
		private CatalogueApiService: ServicecatalogueApiService,
		private catalogueDetailService: CatalogueDetailService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.catalogueActions with the name of the translated word
			this.catalogueActions[value] = (catalogue) => {
				this.deleteCatalogue(catalogue);
			};
			this.catalogueActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.customTableService.isLoading = true;
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: {
			DISPLAY: string;
			NAME: string;
		}[] = [];
		columnsHeadersObj.push(
			{
				DISPLAY: this.translateService.instant('NAME'),
				NAME: 'name',
			},
			{
				DISPLAY: this.translateService.instant('ACTION'),
				NAME: 'ACTION',
			}
		);
		columnsHeaders.push(
			this.translateService.instant('NAME'),
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
			NAME: 'name',
		};

		this.getCatalogue();
	}

	private getCatalogue() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.catalogueDetailService
			.getAllCatalogues(page, pageSize, sortBy, orderBy)
			.subscribe(
				(catalogueResponse: any) => {
					this.customTableService.setTableDataSource(
						catalogueResponse.DATA,
						catalogueResponse.TOTAL_RECORDS
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	public sortData() {
		this.getCatalogue();
	}

	public pageChangeEmit(event) {
		this.getCatalogue();
	}

	private deleteCatalogue(catalogue) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_CATALOGUE'
					) +
					catalogue.name +
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
				this.CatalogueApiService
					.deleteCatalogue(catalogue.catalogueId)
					.subscribe(
						(catalogueResponse: any) => {
							this.getCatalogue();
						},
						(triggersError: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: triggersError.error.ERROR,
							});
						}
					);
			}
		});
	}

	public rowClicked(rowData): void {
		this.router.navigate([
			`company-settings/catalogues/${rowData.catalogueId}`,
		]);
	}

	public newCatalogue(): void {
		this.router.navigate([`company-settings/catalogues/new`]);
	}

}
