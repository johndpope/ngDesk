import { Component, OnInit, OnDestroy } from '@angular/core';
import { CacheService } from '@src/app/cache.service';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { HtmlTemplateApiService } from '@ngdesk/module-api';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

@Component({
	selector: 'app-pdf-master',
	templateUrl: './pdf-master.component.html',
	styleUrls: ['./pdf-master.component.scss'],
})
export class PdfMasterComponent implements OnInit, OnDestroy {
	private companyInfoSubscription: Subscription;
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public moduleId = '';
	private module: any;
	public showSideNav = true;
	public pdfActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	public isLoading = true;
	public navigations = [];
	private modules: any[];
	constructor(
		private cacheService: CacheService,
		private route: ActivatedRoute,
		private dialog: MatDialog,
		private router: Router,
		private translateService: TranslateService,
		public customTableService: CustomTableService,
		private bannerMessageService: BannerMessageService,
		private htmlTemplatenApiService: HtmlTemplateApiService
	) {
		this.translateService.get('DELETE').subscribe((value: string) => {
			this.pdfActions[value] = (row) => {
				this.deletePdf(row);
			};
			this.pdfActions.actions[0].NAME = value;
		});
	}
	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		// use modules saved in cache
		this.companyInfoSubscription =
			this.cacheService.companyInfoSubject.subscribe(
				(dataStored) => {
					if (dataStored) {
						this.modules = this.cacheService.companyData['MODULES'];
						const response = this.modules.find(
							(data) => data.MODULE_ID === this.moduleId
						);
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
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: {
			DISPLAY: string;
			NAME: string;
		}[] = [];
		columnsHeadersObj.push(
			{
				DISPLAY: this.translateService.instant('TITLE'),
				NAME: 'TITLE',
			},
			{
				DISPLAY: this.translateService.instant('ACTION'),
				NAME: 'ACTION',
			}
		);
		columnsHeaders.push(
			this.translateService.instant('TITLE'),
			this.translateService.instant('ACTION')
		);

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.sortBy = 'TITLE';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: this.translateService.instant('TITLE'),
			NAME: 'TITLE',
		};

		this.getPdf();
	}

	private getPdf() {
		const sort = [
			this.customTableService.sortBy + ',' + this.customTableService.sortOrder,
		];
		this.htmlTemplatenApiService
			.getTemplates(
				this.moduleId,
				this.customTableService.pageIndex,
				this.customTableService.pageSize,
				sort
			)
			.subscribe(
				(data: any) => {
					this.customTableService.setTableDataSource(
						data.content,
						data.totalElements
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	private deletePdf(pdf) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant('ARE_YOU_SURE_YOU_WANT_TO_DELETE_PDF') +
					pdf.TITLE +
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
				this.htmlTemplatenApiService
					.deleteTemplate(pdf.TEMPLATE_ID, this.moduleId)
					.subscribe(
						(response: any) => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('DELETED_SUCCESSFULLY'),
							});
							this.getPdf();
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

	public ngOnDestroy() {
		if (this.companyInfoSubscription) {
			this.companyInfoSubscription.unsubscribe();
		}
	}
	public pageChangeEmit(event) {
		this.getPdf();
	}

	public sortData() {
		this.getPdf();
	}

	public newPdf(): void {
		// clicking on new pdf button will redirect to pdf detail
		this.router.navigate([`modules/${this.moduleId}/pdf/new`]);
	}

	public rowClicked(rowData): void {
		// clicking on table row will redirect to pdf detail
		this.router.navigate([
			`modules/${this.moduleId}/pdf/${rowData.TEMPLATE_ID}`,
		]);
	}
}
