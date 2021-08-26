import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../../dialogs/confirm-dialog/confirm-dialog.component';
import { ModulesService } from '../../../modules.service';
import { SlaapiService } from '@ngdesk/module-api';
import { SlaService } from '../sla-service';
@Component({
	selector: 'app-sla-master',
	templateUrl: './sla-master.component.html',
	styleUrls: ['./sla-master.component.scss'],
})
export class SlaMasterComponent implements OnInit {
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public isLoading = true;
	public slasActions = {
		actions: [
			{
				NAME: '',
				ICON: 'notifications_active',
				PERMISSION_NAME: 'ENABLE',
			},
			{
				NAME: '',
				ICON: 'notifications_off',
				PERMISSION_NAME: 'DISABLE',
			},
		],
	};
	public moduleId;
	public showSideNav = true;
	public navigations = [];
	public allSlas = [];
	private module: any;
	public errorMessage: string;

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private dialog: MatDialog,
		private translateService: TranslateService,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		public customTableService: CustomTableService,
		private slaApiService: SlaapiService,
		private slaService: SlaService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.
		this.translateService.get('ENABLE').subscribe((enableValue: string) => {
			this.translateService.get('DISABLE').subscribe((disableValue: string) => {
				// create a function on this.escalationsActions with the name of the translated word
				this.slasActions[enableValue] = (sla) => {
					this.enableSla(sla);
				};

				this.slasActions[disableValue] = (sla) => {
					this.disableSla(sla);
				};

				this.slasActions.actions[0].NAME = enableValue;
				this.slasActions.actions[1].NAME = disableValue;
			});
		});
	}

	public ngOnInit() {
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
							NAME: 'PDFs',
							PATH: ['', 'modules', this.moduleId, 'pdf'],
						},
						{
							NAME: 'TASK',
							PATH: ['', 'modules', this.moduleId, 'task'],
						},
					];
				} else if (response.NAME === 'Chat') {
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
				// this.moduleForm.controls['SINGULAR_NAME'].disable();
				// this.moduleForm.controls['PLURAL_NAME'].disable();
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);
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
				DISPLAY: this.translateService.instant('ENABLED'),
				NAME: 'deleted',
			},
			{
				DISPLAY: this.translateService.instant('ACTION'),
				NAME: 'ACTION',
			}
		);
		columnsHeaders.push(
			this.translateService.instant('NAME'),
			this.translateService.instant('ENABLED'),
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

		this.getSlas();
	}

	private getSlas() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.slaService
			.getAllSlas(this.moduleId, page, pageSize, sortBy, orderBy)
			.subscribe(
				(response: any) => {
					console.log('response', response);
					this.customTableService.setTableDataSource(
						response.DATA,
						response.TOTAL_RECORDS
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	public pageChangeEmit(event) {
		this.getSlas();
	}

	public sortData() {
		this.getSlas();
	}

	private disableSla(sla) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DISABLE_SLA'
					) +
					' ' +
					sla.name +
					' ?',
				buttonText: this.translateService.instant('DISABLE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DISABLE'),
				executebuttonColor: 'warn',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DISABLE')) {
				this.slaApiService.deleteSla(sla.slaId, this.moduleId).subscribe(
					(response: any) => {
						this.getSlas();
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SLA_DISABLE_SUCCESS'),
						});
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

	private enableSla(sla) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant('ARE_YOU_SURE_YOU_WANT_TO_ENABLE_SLA') +
					' ' +
					sla.name +
					' ?',
				buttonText: this.translateService.instant('ENABLE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('ENABLE'),
				executebuttonColor: 'primary',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('ENABLE')) {
				this.slaApiService.putSlaEnable(sla.slaId, this.moduleId).subscribe(
					(response: any) => {
						this.getSlas();
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SLA_ENABLE_SUCCESS'),
						});
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

	public newSla() {
		this.router.navigate([`modules/${this.moduleId}/slas/new`]);
	}

	public rowClicked(rowData): void {
		this.router.navigate([`modules/${this.moduleId}/slas/${rowData.slaId}`]);
	}
}
