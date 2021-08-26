import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { ServiceCatalogueService } from '../service-catalogue-detail/service-catalogue.service';

@Component({
	selector: 'app-service-catalogue',
	templateUrl: './service-catalogue.component.html',
	styleUrls: ['./service-catalogue.component.scss'],
})
export class ServiceCatalogueComponent implements OnInit {
	public moduleId;
	public showSideNav = true;
	public navigations = [];
	public module: any;
	public errorMessage: string;
	public isLoading = true;

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private translateService: TranslateService,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		public customTableService: CustomTableService,
		private serviceCatalogueForms: ServiceCatalogueService
	) {}

	ngOnInit(): void {
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		columnsHeadersObj.push(
			{ DISPLAY: this.translateService.instant('NAME'), NAME: 'name' },
			{
				DISPLAY: this.translateService.instant('DESCRIPTION'),
				NAME: 'description',
			}
		);
		columnsHeaders.push(
			this.translateService.instant('NAME'),
			this.translateService.instant('DESCRIPTION')
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
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.module = response;
				this.isLoading = false;
				this.navigations = [
					{
						NAME: 'MODULE_DETAIL',
						PATH: ['', 'modules', this.moduleId],
					},
					{
						NAME: 'FIELDS',
						PATH: ['', 'modules', this.moduleId, 'fields'],
					},
					{
						NAME: 'LAYOUTS',
						PATH: ['', 'modules', this.moduleId, 'layouts'],
					},
					{
						NAME: 'VALIDATIONS',
						PATH: ['', 'modules', this.moduleId, 'validations'],
					},
					{
						NAME: 'WORKFLOWS',
						PATH: ['', 'modules', this.moduleId, 'workflows'],
					},
					{
						NAME: 'SLAS',
						PATH: ['', 'modules', this.moduleId, 'slas'],
					},
					{
						NAME: 'FORMS',
						PATH: ['', 'modules', this.moduleId, 'forms'],
					},
					{
						NAME: 'CHANNELS',
						PATH: ['', 'modules', this.moduleId, 'channels'],
					},
					{
						NAME: 'PDF',
						PATH: ['', 'modules', this.moduleId, 'pdf'],
					},
					{
						NAME: 'TASK',
						PATH: ['', 'modules', this.moduleId, 'task'],
					},
				];
				this.getServiceCatalogueForms();
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);
	}

	public newFrom() {
		this.router.navigate([
			'',
			'modules',
			this.moduleId,
			'service-catalogue',
			'new',
		]);
	}

	public rowClicked(rowData): void {
		const moduleId = this.route.snapshot.params['moduleId'];
		this.router.navigate([
			`modules/${moduleId}/service-catalogue/${rowData.formId}`,
		]);
	}

	public sortData() {
		this.getServiceCatalogueForms();
	}

	public pageChangeEmit(event) {
		this.getServiceCatalogueForms();
	}

	private getServiceCatalogueForms() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.serviceCatalogueForms
			.getForms(this.moduleId, page, pageSize, sortBy, orderBy)
			.subscribe(
				(formsResponse: any) => {
					this.customTableService.setTableDataSource(
						formsResponse.FORMS,
						formsResponse.TOTAL_RECORDS
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}
}
