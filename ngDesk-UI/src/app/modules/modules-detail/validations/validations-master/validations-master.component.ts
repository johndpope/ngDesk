import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../../dialogs/confirm-dialog/confirm-dialog.component';
import { ModulesService } from '../../../modules.service';

@Component({
	selector: 'app-validations-master',
	templateUrl: './validations-master.component.html',
	styleUrls: ['./validations-master.component.scss'],
})
export class ValidationsMasterComponent implements OnInit {
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public validationActions = {
		actions: [
			{
				NAME: '',
				ICON: 'delete',
				PERMISSION_NAME: 'DELETE',
			},
		],
	};
	public showSideNav = true;
	public navigations = [];
	public moduleId: string;
	public validations: any;
	public module: any;
	public isLoading = true;
	public totalRecords;

	constructor(
		private translateService: TranslateService,
		private dialog: MatDialog,
		private router: Router,
		private route: ActivatedRoute,
		public customTableService: CustomTableService,
		private bannerMessageService: BannerMessageService,
		private modulesService: ModulesService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.validationActions[value] = validation => {
				this.deleteWorkflow(validation);
			};
			this.validationActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.modulesService
			.getModuleById(this.moduleId)
			.subscribe((response: any) => {
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
							PATH: ['', 'modules', this.moduleId, 'workflows']
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
							PATH: ['', 'modules', this.moduleId, 'task']
						}
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
							PATH: ['', 'modules', this.moduleId, 'workflows']
						}
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
							PATH: ['', 'modules', this.moduleId, 'workflows']
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
							PATH: ['', 'modules', this.moduleId, 'task']
						}
					];
				}
			});
		this.customTableService.isLoading = true;
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: {
			DISPLAY: string;
			NAME: string;
		}[] = [];
		columnsHeadersObj.push(
			{
				DISPLAY: this.translateService.instant('NAME'),
				NAME: 'NAME',
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
		this.customTableService.sortBy = 'NAME';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: this.translateService.instant('NAME'),
			NAME: 'NAME',
		};

		this.getValidations();
	}

	private getValidations() {
		const moduleId = this.route.snapshot.params['moduleId'];
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;

		this.modulesService
			.getModuleValidations(moduleId, pageSize, page + 1, sortBy, orderBy)
			.subscribe(
				(response: any) => {
					this.customTableService.setTableDataSource(
						response.VALIDATIONS,
						response.TOTAL_RECORDS
					);
				},
				(validationError: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: validationError.error.ERROR,
					});
				}
			);
	}

	public sortData() {
		this.getValidations();
	}

	private deleteWorkflow(validation) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_VALIDATION'
					) +
					validation.NAME +
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
				this.modulesService
					.deleteModuleValidations(this.moduleId, validation.VALIDATION_ID)
					.subscribe(
						(response: any) => {
							this.getValidations();
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

	public newWorkflow(): void {
		this.router.navigate([`modules/${this.moduleId}/validations/new`]);
	}

	public rowClicked(rowData): void {
		this.router.navigate([
			`modules/${this.moduleId}/validations/${rowData.VALIDATION_ID}`,
		]);
	}

	public pageChangeEmit(event) {
		this.getValidations();
	}
}
