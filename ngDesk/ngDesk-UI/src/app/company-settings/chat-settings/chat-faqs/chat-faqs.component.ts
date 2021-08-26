import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ChannelsService } from '../../../channels/channels.service';
import { BannerMessageService } from '../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../dialogs/confirm-dialog/confirm-dialog.component';

@Component({
	selector: 'app-chat-faqs',
	templateUrl: './chat-faqs.component.html',
	styleUrls: ['./chat-faqs.component.scss'],
})
export class ChatFaqsComponent implements OnInit {
	public faqId: string;
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public triggersActions = {
		actions: [
			{
				NAME: '',
				ICON: 'delete',
				PERMISSION_NAME: 'DELETE',
			},
		],
	};
	constructor(
		private router: Router,
		private dialog: MatDialog,
		private translateService: TranslateService,
		public customTableService: CustomTableService,
		private channelsService: ChannelsService,
		private bannerMessageService: BannerMessageService
	) {
		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.triggersActions[value] = (trigger) => {
				this.deleteTrigger(trigger);
			};
			this.triggersActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		const columnsHeaders: string[] = [
			this.translateService.instant('NAME'),
			this.translateService.instant('DESCRIPTION'),
			this.translateService.instant('ACTION'),
		];

		const columnsHeadersObj = [
			{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
			{
				DISPLAY: this.translateService.instant('DESCRIPTION'),
				NAME: 'DESCRIPTION',
			},
			{
				DISPLAY: this.translateService.instant('ACTION'),
				NAME: 'ACTION',
			},
		];
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
		this.getChats();
	}

	private getChats() {
		// const sortBy = this.customTableService.sortBy;
		// const orderBy = this.customTableService.sortOrder;
		// const page = this.customTableService.pageIndex;
		// const pageSize = this.customTableService.pageSize;
		this.channelsService.getFaqs().subscribe(
			(chatChannelsResponse: any) => {
				this.customTableService.setTableDataSource(
					chatChannelsResponse.DATA,
					chatChannelsResponse.TOTAL_RECORDS
				);
				this.faqId = chatChannelsResponse.DATA.FAQ_ID;
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public rowClicked(rowData): void {
		this.faqId = rowData.FAQ_ID;
		this.router.navigate(['company-settings', 'chat-faqs', rowData.FAQ_ID]);
	}
	public newFaq(): void {
		// clicking on new escalation button will redirect to Faq detail
		this.router.navigate(['company-settings', 'chat-faqs', 'new']);
	}
	public sortData() {
		this.getChats();
	}
	public pageChangeEmit(event) {
		this.getChats();
	}
	private deleteTrigger(trigger) {
		this.faqId = trigger.FAQ_ID;
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant('ARE_YOU_SURE_YOU_WANT_TO_DELETE_FAQ') +
					trigger.NAME +
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
				this.channelsService.deleteFaqs(this.faqId).subscribe(
					(triggersResponse: any) => {
						this.getChats();
					},
					(triggersError: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: triggersError.error.ERROR,
						});
					}
				);
			}
		});
	}
}
