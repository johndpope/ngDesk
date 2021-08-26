import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../custom-table/custom-table.service';
import { ModulesService } from '../modules.service';

@Component({
	selector: 'app-modules-master',
	templateUrl: './modules-master.component.html',
	styleUrls: ['./modules-master.component.scss']
})
export class ModulesMasterComponent implements OnInit {
	constructor(
		private router: Router,
		private translateService: TranslateService,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		public customTableService: CustomTableService,
		public companiesService: CompaniesService
	) {}

	public ngOnInit() {
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('NAME'),
			NAME: 'PLURAL_NAME'
		});
		columnsHeaders.push(this.translateService.instant('NAME'));

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.sortBy = 'NAME';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: this.translateService.instant('NAME'),
			NAME: 'NAME'
		};
		this.getModules();
	}

	private getModules() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		const location = 'module_master';
		this.modulesService
			.getSortedModules(sortBy, orderBy, page + 1, pageSize, location)
			.subscribe(
				(response: any) => {
					// TODO: add it back when we support all the modules
					this.customTableService.setTableDataSource(
						response.MODULES,
						response.TOTAL_RECORDS
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
		this.getModules();
	}

	public pageChangeEmit(event) {
		this.getModules();
	}

	public newModule() {
		this.router.navigate([`modules/new`]);
	}

	public rowClicked(rowData): void {
		this.router.navigate([`modules/${rowData.MODULE_ID}`]);
	}
}
