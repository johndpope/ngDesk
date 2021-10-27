import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../../../companies/companies.service';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../../dialogs/confirm-dialog/confirm-dialog.component';
import { ModulesService } from '../../../modules.service';

@Component({
	selector: 'app-layouts-master',
	templateUrl: './layouts-master.component.html',
	styleUrls: ['./layouts-master.component.scss'],
})
export class LayoutsMasterComponent implements OnInit {
	public listLayoutsActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	private layoutType: string;
	public showSideNav = true;
	public navigations = [];
	public moduleId: string;
	public module: any;
	public isLoading = true;
	public buttonDisable = false;
	public moduleName;

	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private customTableService: CustomTableService,
		private route: ActivatedRoute,
		private router: Router,
		private modulesService: ModulesService,
		private dialog: MatDialog,
		private companiesService: CompaniesService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.listLayoutsActions with the name of the translated word
			this.listLayoutsActions[value] = (row) => {
				this.deleteListLayout(row);
			};
			this.listLayoutsActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.isLoading = false;
				this.module = response;
				if (this.module.NAME === 'Chats') {
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
				} else if (this.module.NAME === 'Tickets') {
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
						// {
						// 	NAME: 'FORMS',
						// 	PATH: ['', 'modules', this.moduleId, 'forms'],
						// },
						{
							NAME: 'PDFs',
							PATH: ['', 'modules', this.moduleId, 'pdf'],
						},
						{
							NAME: 'TASK',
							PATH: ['', 'modules', this.moduleId, 'task'],
						},
					];
				} else {
					{
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
							// {
							// 	NAME: 'FORMS',
							// 	PATH: ['', 'modules', this.moduleId, 'forms'],
							// },
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
				}
			},

			(error) => console.log(error)
		);

		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		this.layoutType = this.route.snapshot.params['layoutType'];
		this.modulesService
			.getModuleById(this.moduleId)
			.subscribe((response: any) => {
				this.moduleName = response.NAME;
				if (
					this.moduleName === 'Tickets' &&
					(this.layoutType === 'create_layouts' ||
						this.layoutType === 'detail_layouts' ||
						this.layoutType === 'edit_layouts')
				) {
					columnsHeadersObj.push(
						{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
						{ DISPLAY: this.translateService.instant('ROLE'), NAME: 'ROLE' }
					);
					columnsHeaders.push(
						this.translateService.instant('NAME'),
						this.translateService.instant('ROLE')
					);
				} else {
					columnsHeadersObj.push(
						{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
						{ DISPLAY: this.translateService.instant('ROLE'), NAME: 'ROLE' },
						{ DISPLAY: this.translateService.instant('ACTION'), NAME: 'ACTION' }
					);
					columnsHeaders.push(
						this.translateService.instant('NAME'),
						this.translateService.instant('ROLE'),
						this.translateService.instant('ACTION')
					);
				}
			});
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

		this.getLayouts();
	}

	private getLayouts() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;

		this.customTableService.isLoading = true;
		this.modulesService
			.getLayouts(
				this.moduleId,
				this.layoutType,
				sortBy,
				orderBy,
				page + 1,
				pageSize
			)
			.subscribe(
				(layoutResponse: any) => {
					layoutResponse[this.layoutType.toUpperCase()].filter((role) => {
						if (role.ROLE === 'Customers') {
							role['ROLE'] = 'Customer';
						}
					});
					this.customTableService.setTableDataSource(
						layoutResponse[this.layoutType.toUpperCase()],
						layoutResponse.TOTAL_RECORDS
					);
				},
				(moduleError: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: moduleError.error.ERROR,
					});
				}
			);
	}

	public rowClicked(rowData): void {
		const moduleId = this.route.snapshot.params['moduleId'];
		this.router.navigate([
			`modules/${moduleId}/${this.layoutType}/${rowData.LAYOUT_ID}`,
		]);
	}

	public newLayout(): void {
		const moduleId = this.route.snapshot.params['moduleId'];
		this.router.navigate([`modules/${moduleId}/${this.layoutType}/new`]);
	}

	private deleteListLayout(listLayout) {
		const moduleId = this.route.snapshot.params['moduleId'];
		const layoutType = this.route.snapshot.params['layoutType'];
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_LIST_LAYOUT'
					) +
					listLayout.NAME +
					' ?',
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				layoutType: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn',
			},
		});
		// EVENT AFTER MODAL DIALOG IS CLOSED

		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DELETE')) {
				this.modulesService
					.deleteListLayout(moduleId, listLayout, layoutType)
					.subscribe(
						(listLayoutResponse: any) => {
							this.companiesService.trackEvent(`Deleted Layout`, {
								LAYOUT_TYPE: layoutType,
								LAYOUT_ID: listLayout.LAYOUT_ID,
								MODULE_ID: moduleId,
							});
							this.getLayouts();
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
		this.getLayouts();
	}

	public pageChangeEmit(event) {
		this.getLayouts();
	}
}
