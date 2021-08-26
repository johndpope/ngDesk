import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { ModulesService } from '../../../modules/modules.service';

@Component({
	selector: 'app-currencies-master',
	templateUrl: './currencies-master.component.html',
	styleUrls: ['./currencies-master.component.scss']
})
export class CurrenciesMasterComponent implements OnInit {
	public isLoading = true;
	constructor(
		private router: Router,
		public customTableService: CustomTableService,
		private translateService: TranslateService,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService
	) {}

	public ngOnInit() {
		this.customTableService.isLoading = true;
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: {
			DISPLAY: string;
			NAME: string;
		}[] = [];
		columnsHeadersObj.push(
			{
				DISPLAY: this.translateService.instant('CURRENCY_NAME'),
				NAME: 'CURRENCY_NAME'
			},
			{
				DISPLAY: this.translateService.instant('ISO_4217_CODE'),
				NAME: 'ISO_CODE'
			},
			{
				DISPLAY: this.translateService.instant('CURRENCY_SYMBOL'),
				NAME: 'CURRENCY_SYMBOL'
			},
			{
				DISPLAY: this.translateService.instant('CONVERTION_RATE'),
				NAME: 'CONVERTION_RATE'
			},
			{
				DISPLAY: this.translateService.instant('STATUS'),
				NAME: 'STATUS'
			}
		);
		columnsHeaders.push(
			this.translateService.instant('CURRENCY_NAME'),
			this.translateService.instant('ISO_4217_CODE'),
			this.translateService.instant('CURRENCY_SYMBOL'),
			this.translateService.instant('CONVERTION_RATE'),
			this.translateService.instant('STATUS')
		);

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.sortBy = 'CURRENCY_NAME';
		this.customTableService.sortOrder = 'asc';
		this.getCurrencies();
	}

	public getCurrencies() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.modulesService
			.getAllCurrencies(sortBy, orderBy, page + 1, pageSize)
			.subscribe(
				(currenciesSuccess: any) => {
					this.isLoading = false;
					this.customTableService.setTableDataSource(
						currenciesSuccess.CURRENCIES,
						currenciesSuccess.TOTAL_RECORDS
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR
					});
				}
			);
	}

	public sortData() {
		this.getCurrencies();
	}

	public newCurrency(): void {
		this.router.navigate([`company-settings/currencies/new`]);
	}

	public rowClicked(rowData): void {
		this.router.navigate([
			`company-settings/currencies/${rowData.CURRENCY_ID}`
		]);
	}

	public pageChangeEmit(event) {
		this.getCurrencies();
	}
}
