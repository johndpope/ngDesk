import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { CompaniesService } from '../../../../companies/companies.service';
import { ConfirmDialogComponent } from '../../../../dialogs/confirm-dialog/confirm-dialog.component';
import { UsersService } from '../../../../users/users.service';
import { RolesService } from '@src/app/roles/roles.service';

@Component({
	selector: 'app-campaigns-master',
	templateUrl: './campaigns-master.component.html',
	styleUrls: ['./campaigns-master.component.scss'],
})
export class CampaignsMasterComponent implements OnInit {
	public allCampaigns = [];
	public isLoading = true;
	public campaignActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	constructor(
		private router: Router,
		public customTableService: CustomTableService,
		private translateService: TranslateService,
		private companiesService: CompaniesService,
		private bannerMessageService: BannerMessageService,
		private dialog: MatDialog,
		private usersService: UsersService,
		private rolesService: RolesService
	) {
		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.campaignActions[value] = (campaign) => {
				this.deleteCampaign(campaign);
			};
			this.campaignActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.rolesService.getRole(this.usersService.user.ROLE).subscribe(
			(roleResponse: any) => {
				// enable or disable actions depending on role permission
				this.campaignActions.actions = this.customTableService.checkPermissionsForActions(
					roleResponse,
					this.campaignActions,
					null
				);

				const columnsHeaders: string[] = [
					this.translateService.instant('NAME'),
					this.translateService.instant('DESCRIPTION'),
					this.translateService.instant('STATUS'),
				];
				const columnsHeadersObj = [
					{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
					{
						DISPLAY: this.translateService.instant('DESCRIPTION'),
						NAME: 'DESCRIPTION',
					},
					{ DISPLAY: this.translateService.instant('STATUS'), NAME: 'STATUS' },
				];
				// only if there are actions to be shown. Actions are based on permissions
				if (this.campaignActions.actions.length > 0) {
					columnsHeadersObj.push({
						DISPLAY: this.translateService.instant('ACTION'),
						NAME: 'ACTION',
					});
					columnsHeaders.push(this.translateService.instant('ACTION'));
				}
				this.customTableService.pageIndex = 0;
				this.customTableService.pageSize = 10;
				this.customTableService.sortBy = 'NAME';
				this.customTableService.sortOrder = 'asc';
				this.customTableService.activeSort = {
					ORDER_BY: 'asc',
					SORT_BY: this.translateService.instant('NAME'),
					NAME: 'NAME',
				};

				this.customTableService.columnsHeaders = columnsHeaders;
				this.customTableService.columnsHeadersObj = columnsHeadersObj;
				this.customTableService.isLoading = true;
				this.getCampaigns();
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	public getCampaigns() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;

		this.companiesService
			.getAllCampaigns(sortBy, orderBy, page + 1, pageSize)
			.subscribe(
				(campaignsSuccess: any) => {
					this.isLoading = false;
					this.allCampaigns = campaignsSuccess.CAMPAIGNS;
					this.customTableService.setTableDataSource(
						campaignsSuccess.CAMPAIGNS,
						campaignsSuccess.TOTAL_RECORDS
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	private deleteCampaign(campaign) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message: this.translateService.instant(
					'ARE_YOU_SURE_YOU_WANT_TO_DELETE_CAMPAIGN'
				),
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			// TODO: implement  delete call here
			if (result === this.translateService.instant('DELETE')) {
				this.companiesService.deleteCampaign(campaign.CAMPAIGN_ID).subscribe(
					(response: any) => {
						this.getCampaigns();
					},
					(error: any) => {
						console.log(error);
					}
				);
			}
		});
	}

	public newCampaign(campaignType: String): void {
		this.router.navigate([`company-settings/marketing/campaigns/type`]);
	}

	public rowClicked(rowData): void {
		this.router.navigate([
			`company-settings/marketing/campaigns/${rowData.CAMPAIGN_TYPE}/${rowData.CAMPAIGN_ID}`,
		]);
	}

	public pageChangeEmit(event) {
		this.getCampaigns();
	}

	public sortData() {
		this.getCampaigns();
	}
}
