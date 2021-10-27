import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../../../companies/companies.service';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../../custom-table/custom-table.service';
import { ModulesService } from '../../../modules.service';

@Component({
	selector: 'app-field-master',
	templateUrl: './field-master.component.html',
	styleUrls: ['./field-master.component.scss'],
})
export class FieldMasterComponent implements OnInit {
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
			{ DISPLAY: this.translateService.instant('SYSTEM_LABEL'), NAME: 'NAME' },
			{
				DISPLAY: this.translateService.instant('DISPLAY_LABEL'),
				NAME: 'DISPLAY_LABEL',
			},
			{ DISPLAY: this.translateService.instant('DATA_TYPE'), NAME: 'DISPLAY' }
		);
		columnsHeaders.push(
			this.translateService.instant('DISPLAY_LABEL'),
			this.translateService.instant('SYSTEM_LABEL'),
			this.translateService.instant('DATA_TYPE')
		);

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.sortBy = 'DISPLAY_LABEL';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: this.translateService.instant('DISPLAY_LABEL'),
			NAME: 'DISPLAY_LABEL',
		};
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.module = response;
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
							NAME: 'PDFs',
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
							NAME: 'FIELDS',
							PATH: ['', 'modules', this.moduleId, 'fields'],
						},
						{
							NAME: 'LAYOUTS',
							PATH: ['', 'modules', this.moduleId, 'layouts'],
						},
						{
							NAME: 'WORKFLOWS',
							PATH: ['', 'modules', this.moduleId, 'workflows'],
						},
						{
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels'],
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
							NAME: 'PDFs',
							PATH: ['', 'modules', this.moduleId, 'pdf'],
						},
						{
							NAME: 'TASK',
							PATH: ['', 'modules', this.moduleId, 'task'],
						},
					];
				}
				this.getFields();
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
				this.isLoading = false;
				this.bannerMessageService.errorNotifications.push({
					message: this.errorMessage,
				});
			}
		);
	}

	private getFields() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.modulesService
			.getFieldsSorted(this.moduleId, sortBy, orderBy, page + 1, pageSize)
			.subscribe(
				(fieldsResponse: any) => {
					this.isLoading = false;
					this.customTableService.setTableDataSource(
						fieldsResponse.FIELDS,
						fieldsResponse.TOTAL_RECORDS
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
		this.getFields();
	}

	public pageChangeEmit(event) {
		this.getFields();
	}

	public newField() {
		this.router.navigate([`modules/${this.moduleId}/field/field-creator`]);
	}

	public rowClicked(rowData): void {
		this.router.navigate([
			`modules/${this.moduleId}/field/${rowData.FIELD_ID}`,
		]);
	}
}
