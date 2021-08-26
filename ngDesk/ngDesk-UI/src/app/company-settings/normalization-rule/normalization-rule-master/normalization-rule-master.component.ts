import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { NormalizationRulesService } from '@src/app/company-settings/normalization-rule/normalization-rules.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { NormalizationRuleApiService } from '@ngdesk/sam-api';

@Component({
	selector: 'app-normalization-rule-master',
	templateUrl: './normalization-rule-master.component.html',
	styleUrls: ['./normalization-rule-master.component.scss'],
})
export class NormalizationRuleMasterComponent implements OnInit {
	public normalizationRuleActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	constructor(
		private router: Router,
		public customTableService: CustomTableService,
		private translateService: TranslateService,
		private normalizationRulesService: NormalizationRulesService,
		private bannerMessageService: BannerMessageService,
		private dialog: MatDialog,
		private normalizationApiService: NormalizationRuleApiService,
	) {
		this.translateService.get('DELETE').subscribe((value: string) => {
			this.normalizationRuleActions[value] = (normalizationRule) => {
				this.deleteNormalizationRule(normalizationRule);
			};
			this.normalizationRuleActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.initializeHeaders();
		this.getAllNormalizationRules();
	}

	// This function initializes the table headers.
	public initializeHeaders() {
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('NAME'),
			NAME: 'name',
		});

		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('DATE_CREATED'),
			NAME: 'dateCreated',
		});

		columnsHeadersObj.push({ 
			DISPLAY: this.translateService.instant('ACTION'), 
			NAME: 'ACTION' 
		});
		columnsHeaders.push(this.translateService.instant('NAME'));
		columnsHeaders.push(this.translateService.instant('DATE_CREATED'));
		columnsHeaders.push(this.translateService.instant('ACTION'));
		this.customTableService.sortBy = 'name';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
	}

	// Get all normalization rules.
	public getAllNormalizationRules() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.normalizationRulesService
			.getAllNormalizationRules(page, pageSize, sortBy, orderBy)
			.subscribe(
				(normalizedRuleResponse: any) => {
					this.customTableService.setTableDataSource(
						normalizedRuleResponse.normalizationRules,
						normalizedRuleResponse.totalCount
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	private deleteNormalizationRule(normalizationRule) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_NORMALIZATION_RULE'
					) +
					normalizationRule.name +
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
				this.normalizationApiService
					.deleteNormalizationRule(normalizationRule.normalizationRuleId)
					.subscribe(
						(normalizationRuleResponse: any) => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('DELETED_SUCCESSFULLY'),
							});
							this.getAllNormalizationRules();
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

	// Navigate to detail page to show create normalization rule.
	public createNewNormalizationRule() {
		this.router.navigate([`company-settings/normalization-rules/new`]);
	}

	// Navigate to detail page to show edit normalization rule.
	public rowClicked(event) {
		this.router.navigate([
			`company-settings/normalization-rules/${event.normalizationRuleId}`,
		]);
	}

	// On sort of data.
	public sortData() {
		this.getAllNormalizationRules();
	}

	// On use of pagination
	public pageChangeEmit(event) {
		this.getAllNormalizationRules();
	}
}
