import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from 'src/app/companies/companies.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { ModulesService } from 'src/app/modules/modules.service';

@Component({
	selector: 'app-forms-master',
	templateUrl: './forms-master.component.html',
	styleUrls: ['./forms-master.component.scss'],
})
export class FormsMasterComponent implements OnInit {
	public moduleId;
	public showSideNav = true;
	public navigations = [];
	private module: any;
	public errorMessage: string;
	public isLoading = true;

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private translateService: TranslateService,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		public customTableService: CustomTableService,
		public companiesService: CompaniesService
	) {}

	public ngOnInit() {
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		columnsHeadersObj.push(
			{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
			{
				DISPLAY: this.translateService.instant('DESCRIPTION'),
				NAME: 'DESCRIPTION',
			}
		);
		columnsHeaders.push(
			this.translateService.instant('NAME'),
			this.translateService.instant('DESCRIPTION')
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
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.module = response;
				this.isLoading = false;
				if (response.NAME === 'Tickets') {
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
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels'],
						},
						{
							NAME: 'FORMS',
							PATH: ['', 'modules', this.moduleId, 'forms'],
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
				} else if (response.NAME === 'Chats') {
					this.navigations = [
						{
							NAME: 'MODULE_DETAIL',
							PATH: ['', 'modules', this.moduleId],
						},
						{
							NAME: 'LAYOUTS',
							PATH: ['', 'modules', this.moduleId, 'layouts'],
						},
						{
							NAME: 'WORKFLOWS',
							PATH: ['', 'modules', this.moduleId, 'workflows'],
						},
					];
				} else {
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
				}
				this.getForms();
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);
	}

	private getForms() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		// this.customTableService.setTableDataSource(
		// 	this.module.FORMS,
		// 	this.module.FORMS.length
		// );
		this.modulesService
			.getFormsSorted(this.moduleId, sortBy, orderBy, page + 1, pageSize)
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

	public newFrom() {
		this.router.navigate([`modules/${this.moduleId}/forms/new`]);
	}

	public rowClicked(rowData): void {
		const moduleId = this.route.snapshot.params['moduleId'];
		this.router.navigate([`modules/${moduleId}/forms/${rowData.FORM_ID}`]);
	}

	public sortData() {
		this.getForms();
	}

	public pageChangeEmit(event) {
		this.getForms();
	}
}
