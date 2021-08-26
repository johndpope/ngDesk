import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ChannelsService } from 'src/app/channels/channels.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { LearnMoreDialogComponent } from 'src/app/dialogs/learn-more-dialog/learn-more-dialog.component';
import { FilePreviewOverlayRef } from 'src/app/shared/file-preview-overlay/file-preview-overlay-ref';
import { FilePreviewOverlayService } from 'src/app/shared/file-preview-overlay/file-preview-overlay.service';
import { WalkthroughService } from 'src/app/walkthrough/walkthrough.service';

@Component({
	selector: 'app-chat-channels-master',
	templateUrl: './chat-channels-master.component.html',
	styleUrls: ['./chat-channels-master.component.scss'],
})
export class ChatChannelsMasterComponent implements OnInit {
	public dialogRef: FilePreviewOverlayRef;
	private previewRef;
	@ViewChild('chatWidgetsTable', { read: ElementRef, static: true })
	private chatWidgetsTable: ElementRef;

	constructor(
		private router: Router,
		private translateService: TranslateService,
		public customTableService: CustomTableService,
		private channelsService: ChannelsService,
		private bannerMessageService: BannerMessageService,
		private walkthroughService: WalkthroughService,
		public fpos: FilePreviewOverlayService,
		private dialog: MatDialog
	) {}

	public ngOnInit() {
		const columnsHeaders: string[] = [
			this.translateService.instant('NAME'),
			this.translateService.instant('DESCRIPTION'),
		];
		const columnsHeadersObj = [
			{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
			{
				DISPLAY: this.translateService.instant('DESCRIPTION'),
				NAME: 'DESCRIPTION',
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
		// const pageSize = this.custmTableService.pageSize;
		this.channelsService.getChatChannels().subscribe(
			(chatChannelsResponse: any) => {
				this.customTableService.setTableDataSource(
					chatChannelsResponse.CHANNELS,
					chatChannelsResponse.TOTAL_RECORDS
				);

				// check if company has completed tickets list walkthrough yet
				this.walkthroughService
					.getWalkthrough()
					.subscribe((walkthroughSuccess: any) => {
						if (!walkthroughSuccess.hasOwnProperty('CHAT_WIDGETS_LIST')) {
							this.showLearnMore();
						}
					});
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public rowClicked(rowData): void {
		this.router.navigate(['company-settings', 'chat-widgets', rowData.NAME]);
		this.dialogRef.close();
		this.fpos.hostElement = '';
	}

	public sortData() {
		this.getChats();
	}
	public pageChangeEmit(event) {
		this.getChats();
	}

	private showLearnMore() {
		const learnMoreDialogRef = this.dialog.open(LearnMoreDialogComponent, {
			data: {
				title: this.translateService.instant(
					'LEARN_MORE_CHAT_WIDGETS_LIST_TITLE'
				),
				description: this.translateService.instant(
					'LEARN_MORE_CHAT_WIDGETS_LIST_DESCRIPTION'
				),
				buttonText: this.translateService.instant('LEARN_MORE'),
				linkText: this.translateService.instant('DISMISS'),
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		learnMoreDialogRef.afterClosed().subscribe((result) => {
			let chatWidgetsListWalkthrough = false;
			// if user selected the learn more button and the layouts button is avialable
			if (result === this.translateService.instant('LEARN_MORE')) {
				// will show walkthrough popup
				this.previewRef = this.fpos.open(
					this.fpos.getWalkthroughData('Chat', 'chat-widgets-table'),
					this.chatWidgetsTable
				);
				this.dialogRef = this.previewRef.dialogRef;
				chatWidgetsListWalkthrough = true;
			}

			// post walkthrough key with true value indicating walkthrough complete
			this.walkthroughService
				.postWalkthrough('CHAT_WIDGETS_LIST', chatWidgetsListWalkthrough)
				.subscribe((walkthroughSuccess: any) => {});
		});
	}
}
