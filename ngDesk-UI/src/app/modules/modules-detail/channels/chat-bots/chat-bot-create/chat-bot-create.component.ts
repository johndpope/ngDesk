import { Component, OnInit } from '@angular/core';

import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../../../../companies/companies.service';
import { BannerMessageService } from '../../../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../../../dialogs/confirm-dialog/confirm-dialog.component';
import { ModulesService } from '../../../../modules.service';

@Component({
	selector: 'app-chat-bots-create',
	templateUrl: './chat-bot-create.component.html',
	styleUrls: ['./chat-bot-create.component.scss'],
})
export class ChatBotCreateComponent implements OnInit {
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public chatBotActions = {
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
	public allChatBots;
	public module: any;
	public isLoading = true;
	public totalRecords = 1;
	public chatTemplate: any;

	constructor(
		private translateService: TranslateService,
		private dialog: MatDialog,
		private router: Router,
		private route: ActivatedRoute,
		public customTableService: CustomTableService,
		private bannerMessageService: BannerMessageService,
		private modulesService: ModulesService,
		private companiesService: CompaniesService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.chatBotActions[value] = (chatBot) => {
				this.deleteChatBot(chatBot);
			};
			this.chatBotActions.actions[0].NAME = value;
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
						{
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels'],
						},
						{
							NAME: 'CHAT_BOTS',
							PATH: ['', 'modules', this.moduleId, 'chatbots'],
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
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels'],
						},
					];
				}
			});
		this.customTableService.isLoading = true;
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: {
			DISPLAY: string;
			NAME: string;
		}[] = [];

		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('NAME'),
			NAME: 'NAME',
		});
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('ACTION'),
			NAME: 'ACTION',
		});
		columnsHeaders.push(this.translateService.instant('NAME'));
		columnsHeaders.push(this.translateService.instant('ACTION'));
		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;

		this.getChatBots();
	}

	private getChatBots() {
		let sortBy = this.customTableService.sortBy;
		let orderBy = this.customTableService.sortOrder;
		const moduleId = this.route.snapshot.params['moduleId'];
		if (!sortBy) {
			sortBy = 'NAME';
		}
		if (!orderBy) {
			orderBy = 'NAME';
		}
		this.modulesService.getSortedChatBots(moduleId, sortBy, orderBy).subscribe(
			(chatBotsResponce: any) => {
				this.allChatBots = chatBotsResponce.CHAT_BOTS;
				this.setDatasource(0, 10);
			},
			(chatBotError: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: chatBotError.error.ERROR,
				});
			}
		);
	}

	private deleteChatBot(chatBot) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_CHAT_BOT'
					) +
					chatBot.NAME +
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
					.deleteChatBot(this.moduleId, chatBot.CHAT_BOT_ID)
					.subscribe(
						(chatBotsResonse: any) => {
							this.companiesService.trackEvent(`Deleted ChatBot`, {
								CHAT_BOT_ID: chatBot.CHAT_BOT_ID,
								MODULE_ID: this.moduleId,
							});
							this.getChatBots();
						},
						(chatBotsError: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: chatBotsError.error.ERROR,
							});
						}
					);
			}
		});
	}

	public newChatBot(): void {
		this.router.navigate([`modules/${this.moduleId}/chatbots/create`]);
	}
	public rowClicked(rowData): void {
		this.router.navigate([
			`modules/${this.moduleId}/chatbots/${rowData.CHAT_BOT_ID}`,
		]);
	}

	public pageChangeEmit(event) {
		this.setDatasource(event.pageIndex, event.pageSize);
	}

	// setting datasource on change pagination
	private setDatasource(pageIndex, pageSize) {
		const dataSource = this.allChatBots.slice(
			pageIndex * pageSize,
			pageIndex * pageSize + pageSize
		);
		this.customTableService.setTableDataSource(
			dataSource,
			this.allChatBots.length
		);
	}

	public sortData(event) {
		this.getChatBots();
	}
}
