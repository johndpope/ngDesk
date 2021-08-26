import {
	Component,
	ElementRef,
	OnDestroy,
	OnInit,
	ViewChild,
} from '@angular/core';

import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';

import { MatChipInputEvent } from '@angular/material/chips';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';

import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subscription } from 'rxjs';
import { shareReplay } from 'rxjs/operators';

import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { ConditionsComponent } from '@src/app/custom-components/conditions/conditions.component';
import { ConditionsService } from 'src/app/custom-components/conditions/conditions.service';
import { ChatBusinessRuleDialogComponent } from '@src/app/dialogs/chat-business-rule-dialog/chat-business-rule-dialog.component';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { ChatBusinessRule } from './../../../../../models/chat-business-rule';
import { DATATYPE, Field } from './../../../../../models/field';
import { ModulesService } from '@src/app/modules/modules.service';
import { CompaniesService } from '../../../../../companies/companies.service';

import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { SchedulesDetailService } from '@src/app/schedules/schedules-detail/schedules-detail.service';
import { FilePreviewOverlayRef } from 'src/app/shared/file-preview-overlay/file-preview-overlay-ref';
import { FilePreviewOverlayService } from '@src/app/shared/file-preview-overlay/file-preview-overlay.service';
import { UsersService } from '@src/app/users/users.service';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '../../../../../app.globals';
import {ChatChannelApiService, ChatChannel, BusinessRules, Restriction, BotSettings, ChatPromptApiService} from '@ngdesk/module-api'

@Component({
	selector: 'app-chat-getting-started',
	templateUrl: './chat-getting-started.component.html',
	styleUrls: ['./chat-getting-started.component.scss'],
})
export class ChatGettingStartedComponent implements OnDestroy, OnInit {
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public promptData: any;
	public chatBusinessRuleData$: Observable<{}>;
	public chatChannel: ChatChannel;
	private chatChannelSubscription: Subscription;
	public subdomain: string;
	public widgetId: string;
	public moduleId: string;
	public chatForm: FormGroup;
	public companyUuid: string;
	public errorMessage: string;
	public fileExtension: string;
	public fileType: string;
	public fileName: string;
	public fieldew: Field;
	public conditionsService: ConditionsService;
	public customer;
	public headerTemplateRef;
	public hour;
	public fields = [];
	public pageInfo = [];
	public params;
	public conditions: ConditionsComponent;
	public FIELDS: FormArray;
	public conditionArray = [];
	public allPrompts = [];
	public enablefaqs: boolean;
	public timeZones: string[];
	public timezone = '';
	private buttonValue = false;
	public chatToggle: boolean;
	public senderTextColor: string;
	public receiverTextColor: string;
	public chatBusinessRule: BusinessRules;
	public disableOn = false;
	public enableChatBot = false;
	public chatBotSelected = '';
	public chatBots = [];
	public script;
	public chatBotsExist = false;
	public showMore = false;
	public promptsActions = {
		actions: [
			{
				NAME: '',
				ICON: 'delete',
				PERMISSION_NAME: 'DELETE',
			},
		],
	};

	public dateDatatype: DATATYPE = {
		DISPLAY: 'Number',
		BACKEND: 'Integer',
	};

	public pageDatatype: DATATYPE = {
		DISPLAY: 'Text',
		BACKEND: 'String',
	};

	@ViewChild('chatScript', {
		read: ElementRef,
	})
	private chatScript: ElementRef;

	@ViewChild('chatCustomizeDetails', {
		read: ElementRef,
	})
	private chatCustomizeDetails: ElementRef;

	@ViewChild('chatPreviewArea', {
		read: ElementRef,
	})
	private chatPreviewArea: ElementRef;

	@ViewChild('chatPlugin', {
		read: ElementRef,
	})
	private chatPlugin: ElementRef;

	@ViewChild('chatEmailDevelopers', {
		read: ElementRef,
	})
	private chatEmailDevelopers: ElementRef;

	@ViewChild('chatPrechatSurvey', {
		read: ElementRef,
	})
	private chatPrechatSurvey: ElementRef;

	@ViewChild('chatBusinessRules', {
		read: ElementRef,
	})
	private chatBusinessRules: ElementRef;

	@ViewChild('chatPromptsDetails', {
		read: ElementRef,
	})
	private chatPromptsDetails: ElementRef;

	@ViewChild('chatOperatingHoursSwitch', {
		read: ElementRef,
	})
	private chatOperatingHoursSwitch: ElementRef;

	@ViewChild('chatbotSettings', {
		read: ElementRef,
	})
	private chatbotSettings: ElementRef;

	@ViewChild('chatSettingsTabs')
	public chatSettingsTabs: MatTabGroup;

	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public fposDialogRef: FilePreviewOverlayRef;
	private previewRef;
	public plugins = [
		{
			NAME: 'Wordpress',
			LINK: 'https://wordpress.org/plugins/ngdesk',
		},
	];
	public selectedPlugin: any;
	public developerEmails: string[] = [];
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	constructor(
		public companiesService: CompaniesService,
		public translateService: TranslateService,
		private usersService: UsersService,
		private router: Router,
		private route: ActivatedRoute,
		private formBuilder: FormBuilder,
		private bannerMessageService: BannerMessageService,
		private schedulesDetailService: SchedulesDetailService,
		public dialog: MatDialog,
		public customTableService: CustomTableService,
		public modulesService: ModulesService,
		public fpos: FilePreviewOverlayService,
		private httpClient: HttpClient,
		private appGlobals: AppGlobals,
		private chatChannelApiService: ChatChannelApiService,
		private chatPromptApiService: ChatPromptApiService
	) {
		this.subdomain = this.usersService.getSubdomain();
		this.companyUuid = this.usersService.companyUuid;
		const channelName = this.route.snapshot.params.chatName;
		this.chatForm = this.formBuilder.group({});

		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.promptsActions[value] = (prompt) => {
				this.deletePrompt(prompt);
			};
			this.promptsActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		const channelName = this.route.snapshot.params.chatName;
		// this.modulesService
		// 	.getChatBots(this.moduleId)
		// 	.subscribe((chatbots: any) => {
		// 		if (chatbots.TOTAL_RECORDS > 0) {
		// 			this.chatBotsExist = true;
		// 		}
		// 	});
		this.selectedPlugin = this.plugins[0];
		const query = `{
			getChatChannel(name:"${channelName}"){
				NAME:name
				DESCRIPTION: description
				SOURCE_TYPE: sourceType
				TITLE: title
				SUBTITLE: subTitle
				FILE: file
				HEADER_COLOR: color
				HEADER_TEXT_COLOR: textColor
				SENDER_BUBBLE_COLOR: senderBubbleColor
				RECEIVER_BUBBLE_COLOR: receiverBubbleColor
				SENDER_TEXT_COLOR: senderTextColor
				RECEIVER_TEXT_COLOR: receiverTextColor
				SETTINGS: settings{
					ENABLE_FAQS: enable
					PRECHAT_SURVEY: preSurveyRequired
					BUSINESS_RULES: businessRules{
						ACTIVE: active
						TIMEZONE: timezone
						RESTRICTION_TYPE: restrictionType
						RESTRICTIONS: restrictions{
							START_TIME: startTime
							END_TIME: endTime
							START_DAY: startDay
							END_DAY: endDay
						}
					}
				}
				CHANNEL_ID: channelId
				CHAT_PROMPTS: chatPrompt{
					PROMPT_NAME: promptName
					PROMPT_ID: promptId
				} 
			}          		   
		}`;
		this.makeGraphQLCall(query).subscribe(
			(queryResponse: any) => {
				this.allPrompts = queryResponse.getChatChannel.CHAT_PROMPTS;
				this.setDatasource(0, 10);
				this.chatChannel = queryResponse.getChatChannel;
				this.senderTextColor = this.chatChannel.SENDER_TEXT_COLOR;
				this.receiverTextColor = this.chatChannel.RECEIVER_TEXT_COLOR;
				this.timeZones = this.schedulesDetailService.timeZones;
				this.script =
					'<script> var script = document.createElement("script");script.type = "text/javascript";script.src = "https://' +
					this.subdomain +
					'.ngdesk.com/widgets/chat/' +
					this.chatChannel.CHANNEL_ID +
					'/chat_widget.js";document.getElementsByTagName("head")[0].appendChild(script);</script>';
				const layerRestrictions: Restriction[] = [];
				if (this.chatChannel.SETTINGS.ENABLE_FAQS) {
					this.enablefaqs = this.chatChannel.SETTINGS.ENABLE_FAQS;
				}
				this.chatBusinessRule = this.chatChannel.SETTINGS.BUSINESS_RULES;

				if (this.chatChannel.SETTINGS.BUSINESS_RULES && this.chatChannel.SETTINGS.BUSINESS_RULES.ACTIVE) {
					this.timezone = this.chatChannel.SETTINGS.BUSINESS_RULES.TIMEZONE;
					this.chatBusinessRule.RESTRICTION_TYPE =
						this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTION_TYPE;
					this.chatBusinessRule.ACTIVE =
						this.chatChannel.SETTINGS.BUSINESS_RULES.ACTIVE;
					if (this.chatBusinessRule.ACTIVE === true) {
						this.disableOn = true;
						this.buttonValue = true;
					}
					this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS.forEach(
						(restriction) => {
							let newRestriction: Restriction = {};
							newRestriction.END_DAY = restriction.END_DAY;
							newRestriction.START_TIME = restriction.START_TIME;
							newRestriction.END_TIME = restriction.END_TIME;
							newRestriction.START_DAY = restriction.START_DAY;
							layerRestrictions.push(
								newRestriction
							);
						}
					);
					this.chatBusinessRule.RESTRICTIONS = layerRestrictions;
					if (
						this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS.length > 0 &&
						this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS[0]
							.START_DAY === null &&
						this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS[0].END_DAY ===
							null &&
						this.chatChannel.SETTINGS.BUSINESS_RULES.ACTIVE === true
					) {
						this.chatBusinessRule.RESTRICTION_TYPE = 'Day';
					} else if (
						this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS.length > 0 &&
						this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS[0]
							.START_DAY !== null &&
						this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS[0].END_DAY !==
							null &&
						this.chatChannel.SETTINGS.BUSINESS_RULES.ACTIVE === true
					) {
						this.chatBusinessRule.RESTRICTION_TYPE = 'Week';
					}
				} else {
					this.chatBusinessRule.ACTIVE = false;
				}
				this.chatForm = this.formBuilder.group({
					NAME: [this.chatChannel.NAME, Validators.required],
					DESCRIPTION: this.chatChannel.DESCRIPTION,
				});
				this.modulesService.getChatBots(this.chatChannel.MODULE).subscribe(
					(chatBotResponse: any) => {
						this.chatBots = chatBotResponse.CHAT_BOTS;
					},
					(error: any) => {
						console.log(error);
					}
				);
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
				DISPLAY: this.translateService.instant('PROMPT_NAME'),
				NAME: 'PROMPT_NAME',
			},
			{
				DISPLAY: this.translateService.instant('ACTION'),
				NAME: 'ACTION',
			}
		);
		columnsHeaders.push(
			this.translateService.instant('PROMPT_NAME'),
			this.translateService.instant('ACTION')
		);

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.sortBy = 'PROMPT_NAME';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;

		this.fileName = 'No file chosen';
	}

	public activateBusinessRule(value, editModal: boolean) {
		if (this.buttonValue === null) {
			return;
		}
		if (this.buttonValue || editModal) {
			this.chatBusinessRule.ACTIVE = true;
			const dialogRef = this.dialog.open(ChatBusinessRuleDialogComponent, {
				width: '600px',
				data: {
					buisnessRule: this.chatBusinessRule.RESTRICTION_TYPE
						? this.chatBusinessRule
						: null,
				},
				disableClose: true,
			});
			dialogRef.afterClosed().subscribe((result) => {
				if (result) {
					const { TIMEZONE, ACTIVE, RESTRICTION_TYPE, RESTRICTIONS } = result;
					this.chatBusinessRule.TIMEZONE = TIMEZONE;
					this.chatBusinessRule.ACTIVE = ACTIVE;
					this.chatBusinessRule.RESTRICTION_TYPE = RESTRICTION_TYPE;
					this.chatBusinessRule.RESTRICTIONS = RESTRICTIONS
					this.disableOn = true;
					this.buttonValue = true;
				}
			});
		} else {
			this.buttonValue = false;
			this.disableOn = false;
			this.chatBusinessRule.RESTRICTIONS = [];
			this.chatBusinessRule.RESTRICTION_TYPE = null;
			this.chatBusinessRule.ACTIVE = false;
		}
		// if (
		// 	this.chatBusinessRule.chatRestrictions.length > 0 &&
		// 	this.chatBusinessRule.chatRestrictions[0].startTime !== ''
		// ) {
		// 	this.disableOn = true;
		// 	this.buttonValue = null;
		// } else {
		// 	this.buttonValue = false;
		// 	this.disableOn = false;
		// }
	}

	public newPrompt() {
		this.router.navigate([
			`modules/${this.moduleId}/channels/chat-widgets/Chat/prompt/new`,
		]);
	}

	public chatBotToggled() {
		if (!this.chatChannel.SETTINGS.BOT_SETTINGS.BOT_ENABLED) {
			this.chatChannel.SETTINGS.BOT_SETTINGS.CHAT_BOT = '';
		}
	}

	// public saveChatChannel() {
	// 	if (this.chatForm.valid) {
	// 		this.chatChannel.NAME = this.chatForm.get('NAME').value;
	// 		this.chatChannel.DESCRIPTION = this.chatForm.get('DESCRIPTION').value;

	// 		this.channelsService.putChatChannel(this.chatChannel).subscribe(
	// 			(response: any) => {
	// 				this.router.navigate([{ outlets: { main: `chat-widgets` } }]);
	// 			},
	// 			(error: any) => {
	// 				this.bannerMessageService.errorNotifications.push({
	// 					message: error.error.ERROR
	// 				});
	// 			}
	// 		);
	// 	}
	// }

	public onFileChanged(event) {
		const files = event.target.files;
		const file = files[0];
		this.fileType = file.type.split('/', 2)[0];
		this.fileExtension = file.type.split('/', 2)[1];
		if (this.fileType === 'image') {
			this.fileName = file.name;
			if (files && file) {
				const reader = new FileReader();
				reader.onload = this.handleReaderLoaded.bind(this);
				reader.readAsBinaryString(file);
			}
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('INVALID_FILE_TYPE'),
			});
		}
	}
	public handleReaderLoaded(readerEvt) {
		const binaryString = readerEvt.target.result;
		this.chatChannel.FILE =
			'data:image/' + this.fileExtension + ';base64,' + btoa(binaryString);
	}
	public saveCustomization() {
		if (!this.chatBusinessRule.ACTIVE) {
			this.chatBusinessRule.RESTRICTION_TYPE = null;
			this.chatChannel.SETTINGS.BUSINESS_RULES.TIMEZONE = null;
		}else {
			this.chatChannel.SETTINGS.BUSINESS_RULES.TIMEZONE = this.timezone;
		}
		this.chatChannel.SETTINGS.BUSINESS_RULES = this.chatBusinessRule;
		const botSettings: BotSettings = {BOT_ENABLED: false,
		CHAT_BOT: null };
		this.chatChannel.SETTINGS.BOT_SETTINGS = botSettings;
		this.chatChannelApiService.updateChatChannel(this.chatChannel.NAME, this.chatChannel).subscribe(
			(response: any) => {
				this.companiesService
					.getAllGettingStarted()
					.subscribe((gettingStarted: any) => {
						this.companiesService
							.getUsageType(this.usersService.getSubdomain())
							.subscribe((usage: any) => {
								if (
									gettingStarted.GETTING_STARTED[1].COMPLETED === false &&
									usage.USAGE_TYPE.CHAT
								) {
									this.companiesService
										.putGettingStarted(gettingStarted.GETTING_STARTED[1])
										.subscribe(
											(put: any) => {},
											(errorResponse: any) => {
												console.log(errorResponse);
											}
										);

									this.bannerMessageService.successNotifications.push({
										message: this.translateService.instant(
											'SAVE_CHAT_CHANNEL_CUSTOMIZATION'
										),
									});
								} else {
									this.bannerMessageService.successNotifications.push({
										message: this.translateService.instant(
											'SAVE_CHAT_CHANNEL_CUSTOMIZATION'
										),
									});
								}
							});
					});
				this.companiesService.trackEvent(`Updated Channel`, {
					CHANNEL_ID: response.CHANNEL_ID,
					MODULE_ID: response.MODULE,
				});
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public pageChangeEmit(event) {
		this.setDatasource(event.pageIndex, event.pageSize);
	}

	// setting datasource on change pagination
	private setDatasource(pageIndex, pageSize) {
		const dataSource = this.allPrompts.slice(
			pageIndex * pageSize,
			pageIndex * pageSize + pageSize
		);
		this.customTableService.setTableDataSource(
			dataSource,
			this.allPrompts.length
		);
	}

	public chatBusinessToggled() {
		if (this.buttonValue) {
			this.activateBusinessRule(this.buttonValue, undefined);
		} else {
			this.buttonValue = false;
			this.disableOn = false;
			this.chatBusinessRule.RESTRICTIONS = [];
			this.chatBusinessRule.RESTRICTION_TYPE = null;
			this.chatBusinessRule.ACTIVE = false;
		}
		this.buttonValue = !this.buttonValue;
	}

	private deletePrompt(prompt) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_PROMPT'
					) +
					prompt.PROMPT_NAME +
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
				this.chatPromptApiService.deleteChatPrompt(prompt.PROMPT_ID, this.route.snapshot.params.chatName)
					.subscribe(
						(triggersResponse: any) => {
							this.getPrompts();
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('DELETED_SUCCESSFULLY'),
							});
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

	public rowClicked(rowData): void {
		this.router.navigate([
			`modules/${this.moduleId}/channels/chat-widgets/${this.chatChannel.NAME}/prompt/${rowData.PROMPT_ID}`,
		]);
	}

	public downloadPlugin() {
		window.open(this.selectedPlugin.LINK);
	}

	public guideLink() {
		window.open('https://www.youtube.com/watch?v=1MxHlYDf8oM');
	}

	public getPrompts(){
		const query = `{
			getChatChannel(name:"${this.route.snapshot.params.chatName}"){
				CHAT_PROMPTS: chatPrompt{
					PROMPT_NAME: promptName
					PROMPT_ID: promptId
				} 
			}          		   
		}`;
		this.makeGraphQLCall(query).subscribe(
			(queryResponse: any) => {
				this.allPrompts = queryResponse.getChatChannel.CHAT_PROMPTS;
				this.setDatasource(0, 10);
			});
	}

	public add(event: MatChipInputEvent): void {
		const input = event.input;
		const value = event.value;

		// Add our email
		if ((value || '').trim()) {
			this.developerEmails.push(value.trim());
		}

		// Reset the input value
		if (input) {
			input.value = '';
		}
	}

	public remove(email): void {
		const index = this.developerEmails.indexOf(email);
		if (index >= 0) {
			this.developerEmails.splice(index, 1);
		}
	}

	public sendEmailToDevelopers() {
		this.chatChannelApiService.emailToDevelopers(this.chatChannel.NAME, this.developerEmails)
			.subscribe(
				(data: any) => {
					this.bannerMessageService.successNotifications.push({
						message: this.translateService.instant('EMAIL_SENT_SUCCESSFULLY'),
					});
					this.developerEmails = [];
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	public copyToClipboard() {
		const selBox = document.createElement('textarea');
		selBox.style.position = 'fixed';
		selBox.style.left = '0';
		selBox.style.top = '0';
		selBox.style.opacity = '0';
		selBox.value = this.script;
		document.body.appendChild(selBox);
		selBox.focus();
		selBox.select();
		document.execCommand('copy');
		document.body.removeChild(selBox);
		this.bannerMessageService.successNotifications.push({
			message: this.translateService.instant('COPIED'),
		});
	}

	public ngOnDestroy() {
		if (this.chatChannelSubscription) {
			this.chatChannelSubscription.unsubscribe();
		}
	}

	public createBot() {
		this.router.navigate([`modules/${this.moduleId}/chatbots/create`]);
	}

	public setTextColor(value, type, id) {
		const button = document.getElementById(id);

		if (type === 'sender') {
			this.chatChannel.SENDER_TEXT_COLOR = value;
			button.style.borderColor = '#000';
			if (id === 'senderblackS') {
				const button2 = document.getElementById('senderwhiteS');
				button2.style.borderColor = '#ddd';
			} else {
				const button2 = document.getElementById('senderblackS');
				button2.style.borderColor = '#ddd';
			}
		}
		if (type === 'receiver') {
			this.chatChannel.RECEIVER_TEXT_COLOR = value;
			button.style.borderColor = '#000';
			if (id === 'receiverblackS') {
				const button2 = document.getElementById('receiverwhiteS');
				button2.style.borderColor = '#ddd';
			} else {
				const button2 = document.getElementById('receiverblackS');
				button2.style.borderColor = '#ddd';
			}
		}
	}
	public showMoreToggle(value) {
		this.showMore = value;
		const more = document.getElementById('moreSettings');
		if (this.showMore) {
			more.style.display = 'block';
		} else {
			more.style.display = 'none';
		}
	}

	public makeGraphQLCall(query: string) {
		return this.httpClient.post(`${this.appGlobals.graphqlUrl}`, query);
	}
}
