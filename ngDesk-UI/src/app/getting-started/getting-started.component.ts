import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatChipInputEvent } from '@angular/material/chips';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ConditionsComponent } from '@src/app/custom-components/conditions/conditions.component';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ChatBusinessRuleDialogComponent } from '@src/app/dialogs/chat-business-rule-dialog/chat-business-rule-dialog.component';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { ChatBusinessRule } from '@src/app/models/chat-business-rule';
import { LayerRestriction } from '@src/app/models/layer-restriction';
import { SchedulesDetailService } from '@src/app/schedules/schedules-detail/schedules-detail.service';
import { FilePreviewOverlayService } from '@src/app/shared/file-preview-overlay/file-preview-overlay.service';
import { Observable, Subscription } from 'rxjs';
import { shareReplay } from 'rxjs/operators';
import { ConditionsService } from 'src/app/custom-components/conditions/conditions.service';
import { DATATYPE, Field } from 'src/app/models/field';
import { ChannelsService } from '../channels/channels.service';
import { RolesService } from '../company-settings/roles/roles-old.service';
import { BannerMessageService } from '../custom-components/banner-message/banner-message.service';
import { EmailChannel } from '../models/email-channel';
import { Role } from '../models/role';
import { CompaniesService } from './../companies/companies.service';
import { ModulesService } from './../modules/modules.service';
import { ToolbarComponent } from './../toolbar/toolbar.component';
import { UsersService } from './../users/users.service';
@Component({
	selector: 'app-getting-started',
	templateUrl: './getting-started.component.html',
	styleUrls: ['./getting-started.component.scss'],
	providers: [ToolbarComponent],
})
export class GettingStartedComponent implements OnInit, OnDestroy {
	public ticketsSrc: SafeResourceUrl;
	public predefinedSrc: SafeResourceUrl;
	public searchTicketsSrc: SafeResourceUrl;
	public knowwledgebaseSrc: SafeResourceUrl;
	public color = 'primary';
	public mode = 'determinate';
	public bufferValue = 100;
	public value = 0;
	public complete = [false, false, false, false];
	public step = 0;
	public isVerified = false;
	public moduleId;
	public active = 'paneOne';
	public isVerifying = false;
	public emailChannelForm: FormGroup;
	public channelName = 'new';
	public chatForm: FormGroup;
	public timezone = '';
	private buttonValue = false;
	public emailChannel: EmailChannel = new EmailChannel(
		'',
		'email',
		'', // email moduleID
		'',
		'',
		''
	);
	public completedSteps = 0;
	public totalSteps = 0;
	public firstStep = 0;
	public forwardingConfirmed = false;
	public verificationTimer;
	public verificationCounter = 0;
	public subdomain: string;
	private chatChannel: any;
	private chatChannelSubscription: Subscription;
	public script;
	public enablefaqs: boolean;
	public chatBots = [];
	public disableOn = false;
	public isChat = false;
	public chatBusinessRule = new ChatBusinessRule('', false, '', [
		new LayerRestriction('', '', '', ''),
	]);
	public chatChannelChanges$: Observable<{}>;
	public errorMessage = '';
	public inviteUsersParams;
	public inviting = false;
	public params = {
		emailAddress: { value: this.translateService.instant('EMAIL_ADDRESS') },
	};
	public errorParams = {
		name: { field: this.translateService.instant('NAME') },
		emailAddress: { field: this.translateService.instant('EMAIL_ADDRESS') },
		type: { field: this.translateService.instant('TYPE') },
	};
	public roles: Role[];
	private notificationSub;
	private notificationSubEntry;
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public promptData: any;
	public chatBusinessRuleData$: Observable<{}>;
	public widgetId: string;
	public companyUuid: string;
	public fileExtension: string;
	public fileType: string;
	public fileName: string;
	public fieldew: Field;
	public selectedIndex = 0;
	public conditionsService: ConditionsService;
	public customer;
	public hour;
	public email;
	public fields = [];
	public finish = false;
	public pageInfo = [];
	public conditions: ConditionsComponent;
	public FIELDS: FormArray;
	public conditionArray = [];
	public allPrompts = [];
	public timeZones: string[];
	public channelType = '';
	public chatToggle: boolean;
	public checkGettingStarted;
	public enableChatBot = false;
	public chatBotSelected = '';
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
	private gettingStarted = [];
	public chatBotsExist = false;
	public loaded = false;
	private showResources = false;
	private stepIcon = [
		'chevron_right',
		'chevron_right',
		'chevron_right',
		'chevron_right',
	];

	@ViewChild('chatSettingsTabs')
	public chatSettingsTabs: MatTabGroup;

	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public plugins = [
		{
			NAME: 'Wordpress',
			LINK: 'https://wordpress.org/plugins/ngdesk',
		},
	];
	public selectedPlugin: any;
	public developerEmails: string[] = [];
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA];
	public inviteUsersForm: FormGroup;
	private currentStep = 0;
	public resourceSelected = 'none';
	public showTicketsGuide = false;
	constructor(
		private toolbarComp: ToolbarComponent,
		private rolesService: RolesService,
		private userService: UsersService,
		private sanitizer: DomSanitizer,
		private router: Router,
		private formBuilder: FormBuilder,
		private modulesService: ModulesService,
		private companiesService: CompaniesService,
		private dialog: MatDialog,
		private route: ActivatedRoute,
		private schedulesDetailService: SchedulesDetailService,
		// private walkthroughService: WalkthroughService,
		public fpos: FilePreviewOverlayService,
		private channelsService: ChannelsService,
		private usersService: UsersService,
		public customTableService: CustomTableService,
		public translateService: TranslateService,
		private bannerMessageService: BannerMessageService
	) {
		this.translateService.get('NAME').subscribe((value: string) => {
			this.errorParams.name = { field: value };
		});

		this.translateService.get('EMAIL_ADDRESS').subscribe((value: string) => {
			this.errorParams.emailAddress = { field: value };
		});

		this.translateService.get('EMAIL_TYPE').subscribe((value: string) => {
			this.errorParams.type = { field: value };
		});
		this.subdomain = this.usersService.getSubdomain();
		this.subdomain = this.usersService.getSubdomain();
		this.email = this.userService.user.EMAIL_ADDRESS;
		this.companyUuid = this.usersService.companyUuid;
		this.checkGettingStarted = this.router.url.search('main:getting-started');
		let channelName;
		if (this.checkGettingStarted) {
			channelName = 'Chats';
		} else {
			channelName = this.route.snapshot.params.chatName;
		}
		// this.chatChannelChanges$ = this.channelsService
		// 	.getChatChannel(channelName)
		// 	.pipe(shareReplay());
		this.chatForm = this.formBuilder.group({});
	}
	public ngOnInit() {
		this.selectedPlugin = this.plugins[0];
		this.subdomain = this.userService.getSubdomain();
		this.companiesService
			.putFirstSignin(this.usersService.getSubdomain())
			.subscribe((put: any) => {});
		this.moduleId = this.route.snapshot.params['moduleId'];
		const channelName = 'Chats';
		this.timeZones = this.schedulesDetailService.timeZones;

		// this.chatChannelSubscription = this.chatChannelChanges$.subscribe(
		// 	(val: any) => {
		// 		this.chatChannel = val;
		// 		this.script =
		// 			'<script> var script = document.createElement("script");script.type = "text/javascript";script.src = "https://' +
		// 			this.subdomain +
		// 			'.ngdesk.com/widgets/chat/' +
		// 			this.chatChannel.CHANNEL_ID +
		// 			'/chat_widget.js";document.getElementsByTagName("head")[0].appendChild(script);</script>';
		// 		const layerRestrictions: LayerRestriction[] = [];
		// 		if (this.chatChannel.SETTINGS.ENABLE_FAQS) {
		// 			this.enablefaqs = this.chatChannel.SETTINGS.ENABLE_FAQS;
		// 		}

		// 		if (this.chatChannel.SETTINGS.BUSINESS_RULES) {
		// 			this.timezone = this.chatChannel.SETTINGS.BUSINESS_RULES.TIMEZONE;
		// 			this.chatBusinessRule.restrictionType = this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTION_TYPE;
		// 			this.chatBusinessRule.hasRestriction = this.chatChannel.SETTINGS.BUSINESS_RULES.ACTIVE;
		// 			if (this.chatBusinessRule.hasRestriction === true) {
		// 				this.disableOn = true;
		// 				this.buttonValue = true;
		// 			}
		// 			this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS.forEach(
		// 				(restriction) => {
		// 					layerRestrictions.push(
		// 						new LayerRestriction(
		// 							restriction.START_TIME,
		// 							restriction.END_TIME,
		// 							restriction.START_DAY,
		// 							restriction.END_DAY
		// 						)
		// 					);
		// 				}
		// 			);
		// 			this.chatBusinessRule.chatRestrictions = layerRestrictions;
		// 			if (
		// 				this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS.length > 0 &&
		// 				this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS[0]
		// 					.START_DAY === null &&
		// 				this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS[0].END_DAY ===
		// 					null &&
		// 				this.chatChannel.SETTINGS.BUSINESS_RULES.ACTIVE === true
		// 			) {
		// 				this.chatBusinessRule.restrictionType = 'Day';
		// 			} else if (
		// 				this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS.length > 0 &&
		// 				this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS[0]
		// 					.START_DAY !== null &&
		// 				this.chatChannel.SETTINGS.BUSINESS_RULES.RESTRICTIONS[0].END_DAY !==
		// 					null &&
		// 				this.chatChannel.SETTINGS.BUSINESS_RULES.ACTIVE === true
		// 			) {
		// 				this.chatBusinessRule.restrictionType = 'Week';
		// 			}
		// 		} else {
		// 			this.chatBusinessRule.hasRestriction = false;
		// 			this.chatBusinessRule.restrictionType = null;
		// 		}
		// 		this.chatForm = this.formBuilder.group({
		// 			NAME: [val.NAME, Validators.required],
		// 			DESCRIPTION: val.DESCRIPTION,
		// 		});
		// 		this.modulesService.getChatBots(this.chatChannel.MODULE).subscribe(
		// 			(chatBotResponse: any) => {
		// 				this.chatBots = chatBotResponse.CHAT_BOTS;
		// 			},
		// 			(error: any) => {
		// 				console.log(error);
		// 			}
		// 		);
		// 	}
		// );

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

		if (!this.checkGettingStarted) {
			this.getPrompts();
		}
		this.fileName = 'No file chosen';
		this.companiesService.getAllGettingStarted().subscribe(
			(response: any) => {
				let i = 0;
				response.GETTING_STARTED.forEach((element) => {
					this.gettingStarted.push(element);
					this.complete[i] = element.COMPLETED;
					i++;
				});
				this.publishChatStatus(response.GETTING_STARTED[0].STEP_ID);
				this.companiesService
					.getUsageType(this.usersService.getSubdomain())
					.subscribe((usage: any) => {
						if (usage.USAGE_TYPE.CHAT) {
							this.modulesService
								.getEntries(this.moduleId)
								.subscribe((module: any) => {
									if (
										module.TOTAL_RECORDS >= 2 &&
										this.gettingStarted[0].COMPLETED === false
									) {
										this.currentStep = 4;
										this.companiesService
											.putGettingStarted(this.gettingStarted[0])
											.subscribe((put: any) => {
												this.companiesService
													.putGettingStarted(this.gettingStarted[3])
													.subscribe((puta: any) => {
														this.loaded = true;
														this.publishChatStatus(
															response.GETTING_STARTED[0].STEP_ID
														);
													});
											});
									} else {
										this.loadExisting(response.GETTING_STARTED[0].STEP_ID);
										this.loaded = true;
										this.publishChatStatus(response.GETTING_STARTED[0].STEP_ID);
									}
								});
							this.modulesService
								.getChatBots(this.moduleId)
								.subscribe((chatbots: any) => {
									if (chatbots.TOTAL_RECORDS > 0) {
										this.chatBotsExist = true;
									}
								});
						} else {
							if (!this.complete[1] && this.usersService.user.EMAIL_VERIFIED) {
								this.companiesService
									.putGettingStarted(this.gettingStarted[1])
									.subscribe((putResponse: any) => {
										this.loadExisting(response.GETTING_STARTED[0].STEP_ID);
										this.loaded = true;
										this.publishChatStatus(response.GETTING_STARTED[0].STEP_ID);
									});
							} else {
								this.loadExisting(response.GETTING_STARTED[0].STEP_ID);
								this.loaded = true;
								this.publishChatStatus(response.GETTING_STARTED[0].STEP_ID);
							}
						}
					});
			},
			(errorResponse: any) => {
				console.log(errorResponse);
			}
		);

		this.emailChannel.MODULE = this.moduleId;
		// initializes an empty form
		this.emailChannelForm = this.formBuilder.group({
			name: ['', [Validators.required]],
			description: [''],
			emailAddress: ['', [Validators.required, Validators.email]],
		});

		this.inviteUsersParams = {
			firstName: { field: this.translateService.instant('FIRST_NAME') },
			lastName: { field: this.translateService.instant('LAST_NAME') },
			emailAddress: { field: this.translateService.instant('EMAIL_ADDRESS') },
			role: { field: this.translateService.instant('ROLE') },
		};

		this.rolesService.getRoles().subscribe(
			(rolesResponse: any) => {
				rolesResponse['ROLES'].filter((role) => {
					if (role.NAME === 'Customers') {
						role['NAME'] = 'Customer';
					}
				});
				this.roles = rolesResponse.ROLES.filter(
					(role) => role.NAME !== 'Public'
				);
				this.roles = this.roles.sort((a, b) => a.NAME.localeCompare(b.NAME));
			},
			(error: any) => {
				console.log(error);
			}
		);

		this.inviteUsersForm = this.formBuilder.group({
			users: this.formBuilder.array([this.createFormItem()]),
		});
	}
	public publishChatStatus(stepId) {
		// const notifySubscription$ = this._stompService._stompRestService
		// 	.watch(`rest/getting-started/step/${this.moduleId}`)
		// 	.pipe(shareReplay());
		// this.notificationSub = notifySubscription$.subscribe(
		// 	(notification: any) => {
		// 		this.companiesService
		// 			.getAllGettingStarted()
		// 			.subscribe((response: any) => {
		// 				let index = 0;
		// 				for (index = 0; index < this.gettingStarted.length; index++) {
		// 					this.gettingStarted[index] = response.GETTING_STARTED[index];
		// 					this.complete[index] = response.GETTING_STARTED[index].COMPLETED;
		// 				}
		// 				this.loadExisting(stepId);
		// 			});
		// 	}
		// );
	}
	public nextStep() {
		if (this.currentStep < 3 && !this.finish) {
			this.stepIcon[this.currentStep] = 'check_circle_outline';
			this.complete[this.currentStep] = true;
			this.companiesService
				.putGettingStarted(this.gettingStarted[this.currentStep])
				.subscribe(
					(response: any) => {
						this.currentStep = this.currentStep + 1;
					},
					(errorResponse: any) => {
						console.log(errorResponse);
					}
				);
			if (this.value < 100) {
				this.value = this.value + 25;
			}
		} else if (this.currentStep === 3) {
			this.stepIcon[this.currentStep] = 'check_circle_outline';
			this.complete[this.currentStep] = true;
			this.bannerMessageService.successNotifications.push({
				message: this.translateService.instant(
					'SUCCESSFULLY_COMPLETED_GETTING_STARTED'
				),
			});
			this.modulesService.getModuleByName('Tickets').subscribe(
				(moduleResponse: any) => {
					this.router.navigate([`render/${moduleResponse.MODULE_ID}`]);
				},
				(error: any) => {
					console.log(error);
				}
			);
		} else if (this.currentStep === 4 && !this.finish) {
			this.chatSettingsTabs.selectedIndex = 0;
			this.currentStep = this.currentStep + 1;
		} else if (this.currentStep === 5 && !this.finish) {
			this.chatSettingsTabs.selectedIndex = 1;
			this.currentStep = this.currentStep + 1;
		} else if (this.currentStep === 6 && !this.finish) {
			this.currentStep = this.currentStep + 1;
		} else if (this.currentStep === 7 || this.finish) {
			this.value = 100;

			this.bannerMessageService.successNotifications.push({
				message: this.translateService.instant(
					'SUCCESSFULLY_COMPLETED_GETTING_STARTED'
				),
			});
			this.modulesService.getModuleByName('Tickets').subscribe(
				(moduleResponse: any) => {
					this.router.navigate([`render/${moduleResponse.MODULE_ID}`]);
				},
				(error: any) => {
					console.log(error);
				}
			);
		}
	}
	// fix layout for invite

	public createFormItem(): FormGroup {
		return this.formBuilder.group({
			FIRST_NAME: ['', Validators.required],
			LAST_NAME: [''],
			EMAIL_ADDRESS: ['', [Validators.required, Validators.email]],
			ROLE: ['', Validators.required],
		});
	}
	public typeSelected(event) {
		// clears errorMessage if it was invalid type displaying
		if (this.errorMessage.indexOf('type') !== -1) {
			this.errorMessage = '';
		}
		// If Internal email is selected, does not need to validate email
		if (event === 'Internal') {
			this.channelType = 'Internal';
			this.emailChannelForm
				.get('emailAddress')
				.setValidators([Validators.required]);
		} else {
			this.channelType = 'External';
			this.emailChannelForm
				.get('emailAddress')
				.setValidators([Validators.required, Validators.email]);
		}
		this.emailChannelForm.get('emailAddress').updateValueAndValidity();
	}
	public verifyEmail() {
		// will project the third pane which is the email vericication
		this.active = 'paneThree';
		this.isVerifying = true;
		// call the verify on click of test forwarding
		this.channelsService
			.postEmailVerify(this.moduleId, this.emailChannel)
			.subscribe(
				(verifyResponse: any) => {
					let verificationCounter = 0;
					// reset timer
					clearInterval(this.verificationTimer);
					// timer is set for function to be called every 5 seconds
					// will check if email returns email channel verification status of true or false
					this.verificationTimer = setInterval(() => {
						// will terminate repeating timer after 1 minute of verification is done
						// and email channel is still not verified
						if (++verificationCounter === 12) {
							this.isVerified = false;
							this.isVerifying = false;
							clearInterval(this.verificationTimer);
						}

						// api checks the email channel verification status
						this.channelsService
							.getEmailChannel(this.moduleId, this.emailChannel.NAME)
							.subscribe(
								(response: any) => {
									// will terminate timer if the channel returns as verified
									if (response.IS_VERIFIED === true) {
										this.isVerified = true;
										this.isVerifying = false;
										clearInterval(this.verificationTimer);
									}
								},
								(error: any) => {
									this.isVerified = false;
									this.isVerifying = false;
									this.errorMessage = error.error.ERROR;
								}
							);
					}, 5000);
				},
				(verifyError: any) => {
					this.isVerified = false;
					this.isVerifying = false;
					this.errorMessage = verifyError.error.ERROR;
				}
			);
	}
	public activateBusinessRule(value, editModal: boolean) {
		if (this.buttonValue === null) {
			return;
		}
		if (this.buttonValue || editModal) {
			this.chatBusinessRule.hasRestriction = true;
			const dialogRef = this.dialog.open(ChatBusinessRuleDialogComponent, {
				width: '600px',
				data: {
					buisnessRule: this.chatBusinessRule.restrictionType
						? this.chatBusinessRule
						: null,
				},
				disableClose: true,
			});
			dialogRef.afterClosed().subscribe((result) => {
				if (result) {
					const { TIMEZONE, ACTIVE, RESTRICTION_TYPE, RESTRICTIONS } = result;
					this.chatBusinessRule = new ChatBusinessRule(
						TIMEZONE,
						ACTIVE,
						RESTRICTION_TYPE,
						RESTRICTIONS
					);
					this.disableOn = true;
					this.buttonValue = true;
				}
			});
		} else {
			this.buttonValue = false;
			this.disableOn = false;
			this.chatBusinessRule.chatRestrictions = [];
			this.chatBusinessRule.restrictionType = null;
			this.chatBusinessRule.hasRestriction = false;
		}
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

	public chatBusinessToggled() {
		if (this.buttonValue) {
			this.activateBusinessRule(this.buttonValue, undefined);
		} else {
			this.buttonValue = false;
			this.disableOn = false;
			this.chatBusinessRule.chatRestrictions = [];
			this.chatBusinessRule.restrictionType = null;
			this.chatBusinessRule.hasRestriction = false;
		}
		this.buttonValue = !this.buttonValue;
	}

	// This is to check email verification

	public save() {
		if (this.emailChannelForm.valid) {
			// initializes translation params
			this.params = {
				emailAddress: { value: this.emailChannelForm.value.emailAddress },
			};
			// updating values of request body with values from form
			this.emailChannel.name = this.emailChannelForm.value.name;
			this.emailChannel.MODULE = this.moduleId;
			this.emailChannel.description = this.emailChannelForm.value.description;
			this.emailChannel.type = this.channelType;
			// if internal, adds the ngdesk email domain at the end of the mailbox name
			this.emailChannel.emailAddress =
				this.emailChannel.type === 'Internal'
					? `${this.emailChannelForm.value.emailAddress}@${this.subdomain}.ngdesk.com`
					: this.emailChannelForm.value.emailAddress;
			this.channelsService
				.postTicketEmailChannel(this.moduleId, this.emailChannel)
				.subscribe(
					(emailSuccessResponse: any) => {
						this.channelCreateSuccess(emailSuccessResponse);
					},
					(emailError: any) => {
						this.isVerifying = false;
						this.bannerMessageService.errorNotifications.push({
							message: emailError.error.ERROR,
						});
					}
				);
		}
	}

	private channelCreateSuccess(emailSuccessResponse) {
		// cast api response as custom email channel data type
		this.emailChannel = new EmailChannel(
			emailSuccessResponse.NAME,
			emailSuccessResponse.SOURCE_TYPE,
			emailSuccessResponse.MODULE,
			emailSuccessResponse.DESCRIPTION,
			emailSuccessResponse.EMAIL_ADDRESS,
			emailSuccessResponse.TYPE,
			emailSuccessResponse.CHANNEL_ID,
			emailSuccessResponse.IS_VERIFIED,
			emailSuccessResponse.WORKFLOW,
			emailSuccessResponse.DATE_CREATED,
			emailSuccessResponse.LAST_UPDATED_BY,
			emailSuccessResponse.DATE_UPDATED
		);
		// go to next pane if external email
		// else, modal closes and creates new internal channel
		if (this.emailChannel.type === 'External') {
			this.active = 'paneTwo';
			if (!this.complete[2]) {
				this.companiesService
					.putGettingStarted(this.gettingStarted[2])
					.subscribe(
						(put: any) => {
							this.loadExisting('0');
						},
						(errorResponse: any) => {
							console.log(errorResponse);
						}
					);
			}
		} else {
			this.bannerMessageService.successNotifications.push({
				message: this.translateService.instant('SUCCESSFUL'),
			});
			if (!this.complete[2]) {
				this.companiesService
					.putGettingStarted(this.gettingStarted[2])
					.subscribe(
						(put: any) => {
							this.loadExisting('0');
						},
						(errorResponse: any) => {
							console.log(errorResponse);
						}
					);
			}

			this.currentStep = 3;
		}
	}

	public loadExisting(firstStep) {
		this.completedSteps = 0;
		this.value = 0;
		// typecasting to a number
		firstStep = firstStep - 0;
		this.currentStep = firstStep - 0;
		if (this.currentStep === 4) {
			this.isChat = true;
		}
		if (this.isChat) {
			for (let i = firstStep; i < this.complete.length + firstStep; i++) {
				if (this.complete[i - firstStep] === false && this.isChat) {
					// typecasting to a number
					this.currentStep = i - 0;
					for (let j = i - 0; j < this.complete.length; j++) {
						this.complete[j] = false;
					}
				} else {
					this.completedSteps = this.completedSteps + 1;
					this.currentStep = i;
					this.stepIcon[this.currentStep - firstStep] = 'check_circle_outline';
					this.value = this.value + 33;
					if (this.value === 99) {
						this.value = this.value + 1;
					}
				}
			}
			let index = 0;
			for (index; index < this.complete.length; index++) {
				if (this.complete[index] === false && this.isChat) {
					this.currentStep = index + 4;
					break;
				}
			}
			if (index === 3) {
				this.finish = true;
			}
		} else {
			let count = 0;
			for (let i = firstStep; i < this.complete.length + firstStep; i++) {
				if (this.complete[i - firstStep] === false) {
					// typecasting to a number
					count++;
					this.currentStep = i - 0;
				} else {
					this.completedSteps = this.completedSteps + 1;
					if (count === 0 && i === 3) {
						this.currentStep = 0;
						this.stepIcon[3 - firstStep] = 'check_circle_outline';
					} else {
						this.currentStep = i;
						this.stepIcon[this.currentStep - firstStep] =
							'check_circle_outline';
					}
					this.value = this.value + 25;
				}
			}
			let index = 0;
			for (index; index < this.complete.length; index++) {
				if (this.complete[index] === false) {
					this.currentStep = index;
					break;
				}
			}
			if (index === 4) {
				this.finish = true;
			}
		}
		if (this.active === 'paneTwo' || this.active === 'paneThree') {
			// this.currentStep = 2;
			this.panelClicked(2);
		}
		this.firstStep = firstStep;
		this.loaded = true;
		if (this.isChat) {
			this.totalSteps = 3;
		} else {
			this.totalSteps = 4;
		}
	}

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
		if (!this.chatBusinessRule.hasRestriction) {
			this.chatBusinessRule.restrictionType = null;
		}
		this.chatChannel.SETTINGS.BUSINESS_RULES = this.chatBusinessRule;
		this.chatChannel.SETTINGS.BUSINESS_RULES.TIMEZONE = this.timezone;
		this.channelsService.putChatChannel(this.chatChannel).subscribe(
			(response: any) => {
				if (this.gettingStarted[1].COMPLETED === false) {
					this.companiesService
						.putGettingStarted(this.gettingStarted[1])
						.subscribe(
							(put: any) => {
								this.complete[1] = true;
								this.stepIcon[1] = 'check_circle_outline';
								this.value = this.value + 33;
								this.bannerMessageService.successNotifications.push({
									message: this.translateService.instant(
										'SAVE_CHAT_CHANNEL_CUSTOMIZATION'
									),
								});
							},
							(errorResponse: any) => {}
						);
				} else {
					this.bannerMessageService.successNotifications.push({
						message: this.translateService.instant(
							'SAVE_CHAT_CHANNEL_CUSTOMIZATION'
						),
					});
				}
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	private getPrompts() {
		this.channelsService
			.getPrompts(this.route.snapshot.params.chatName)
			.subscribe(
				(response: any) => {
					this.allPrompts = response.CHAT_PROMPTS;
					this.setDatasource(0, 10);
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

	private deletePrompt(prompt) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_PROMPT'
					) +
					prompt.NAME +
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
				this.channelsService
					.deletePrompt(prompt.PROMPT_ID, this.route.snapshot.params.chatName)
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
		this.channelsService
			.emailToDevelopers(this.chatChannel, this.developerEmails)
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

	public stepClicked(index, item) {
		if (
			item.COMPLETED === false ||
			item.STEP_ID === '0' ||
			item.STEP_ID === '5'
		) {
			this.resourceSelected = 'none';
			this.showResources = false;
			if (this.isChat) {
				this.currentStep = index + 4;
				this.nextStep();
			} else {
				this.currentStep = index;
			}
			if (index === 2 && this.isChat) {
				this.status();
			}
			for (let i = 0; i < this.complete.length; i++) {
				const otherSteps = document.getElementById(i + '');
				if (this.complete[i]) {
					otherSteps.style.backgroundColor = '#81C784';
					otherSteps.style.color = '#FAFAFA';
				} else {
					if (otherSteps !== null) {
						otherSteps.style.backgroundColor = '#FAFAFA';
						otherSteps.style.color = '#000000';
					}
				}
			}
			const selected = document.getElementById(index);
			selected.style.backgroundColor = 'var(--primaryColor)';
			selected.style.color = '#FAFAFA';
		}
	}

	public panelClicked(index) {
		this.resourceSelected = 'none';
		this.showResources = false;
		if (this.isChat) {
			this.currentStep = index + 4;
			this.nextStep();
		} else {
			this.currentStep = index;
		}
		if (index === 2 && this.isChat) {
			this.status();
		}
		for (let i = 0; i < this.complete.length; i++) {
			const otherSteps = document.getElementById(i + '');
			if (this.complete[i]) {
				otherSteps.style.backgroundColor = '#81C784';
				otherSteps.style.color = '#FAFAFA';
			} else {
				if (otherSteps !== null) {
					otherSteps.style.backgroundColor = '#FAFAFA';
					otherSteps.style.color = '#000000';
				}
			}
		}
		const selected = document.getElementById(index);
		selected.style.backgroundColor = 'var(--primaryColor)';
		selected.style.color = '#FAFAFA';
	}

	public activateEmail() {
		this.companiesService
			.postGettingStartedResendEmail(this.userService.user, this.subdomain)
			.subscribe((response: any) => {
				this.bannerMessageService.successNotifications.push({
					message: 'Successfully resent Activation Email',
				});
			});
	}
	public tabClicked(tab) {
		this.panelClicked(tab.index);
	}
	public ngOnDestroy() {
		if (this.chatChannelSubscription) {
			this.chatChannelSubscription.unsubscribe();
		}
		if (this.notificationSub) {
			this.notificationSub.unsubscribe();
		}
		if (this.notificationSubEntry) {
			this.notificationSubEntry.unsubscribe();
		}
	}

	public status() {
		this.toolbarComp.openProfileMenu();
	}
	public addInvite() {
		const users = this.inviteUsersForm.get('users') as FormArray;
		users.push(this.createFormItem());
	}
	public removeInvite(userIndex) {
		const users = this.inviteUsersForm.get('users') as FormArray;
		if (users.value.length > 1) {
			users.removeAt(userIndex);
		}
	}

	public sendInvite() {
		this.inviting = true;
		this.errorMessage = '';
		this.companiesService
			.postUserInvite({ USERS: this.inviteUsersForm.value.users })
			.subscribe(
				(inviteResponse: any) => {
					this.companiesService.trackEvent('Invited users', {
						USERS: this.inviteUsersForm.value.users,
					});
					this.companiesService
						.getUsageType(this.usersService.getSubdomain())
						.subscribe(
							(usageType: any) => {
								if (
									(usageType.USAGE_TYPE.TICKETS &&
										!usageType.USAGE_TYPE.CHAT) ||
									usageType.USAGE_TYPE.PAGER
								) {
									this.companiesService
										.getAllGettingStarted()
										.subscribe((getAll: any) => {
											if (!getAll.GETTING_STARTED[3].COMPLETED) {
												this.companiesService
													.putGettingStarted(getAll.GETTING_STARTED[3])
													.subscribe(
														(put: any) => {
															this.bannerMessageService.successNotifications.push(
																{
																	message: this.translateService.instant(
																		'SUCCESSFULLY_INVITED'
																	),
																}
															);
															this.loadExisting('0');
															this.inviting = false;
														},
														(errorResponse: any) => {
															this.inviting = false;
															console.log(errorResponse);
														}
													);
											} else {
												this.bannerMessageService.successNotifications.push({
													message: this.translateService.instant(
														'SUCCESSFULLY_INVITED'
													),
												});
												this.inviting = false;
											}
										});
								} else {
									this.bannerMessageService.successNotifications.push({
										message: this.translateService.instant(
											'SUCCESSFULLY_INVITED'
										),
									});
									this.inviting = false;
								}
								// location.reload();
							},
							(error: any) => {
								console.log(error);
								this.inviting = false;
							}
						);
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
					this.inviting = false;
				}
			);
	}

	public goToInvites() {
		this.router.navigate([`manage-invites`]);
	}

	// FormArray class contains the controls property.
	get formData() {
		return <FormArray>this.inviteUsersForm.get('users');
	}
	public getValue(val) {
		return this.translateService.instant(val);
	}

	public createBot() {
		this.saveCustomization();
		this.router.navigate([`modules/${this.moduleId}/chatbots/create`]);
	}

	public clickedResources() {
		this.showResources = !this.showResources;
	}

	public resourcesMenu(menu) {
		if (menu === 'knowledgebase') {
			this.resourceSelected = 'knowledgebase';
			this.knowwledgebaseSrc = this.sanitizer.bypassSecurityTrustResourceUrl(
				'https://www.youtube.com/embed/tGfDNxi4EOM'
			);
		} else if (menu === 'tickets') {
			this.showTicketsGuide = !this.showTicketsGuide;
			this.ticketsSrc = this.sanitizer.bypassSecurityTrustResourceUrl(
				'https://www.youtube.com/embed/O7RFgvAeG5M'
			);
			this.resourceSelected = 'createTickets';
		} else if (menu === 'createTickets') {
			this.ticketsSrc = this.sanitizer.bypassSecurityTrustResourceUrl(
				'https://www.youtube.com/embed/O7RFgvAeG5M'
			);
			this.resourceSelected = 'createTickets';
		} else if (menu === 'premade') {
			this.predefinedSrc = this.sanitizer.bypassSecurityTrustResourceUrl(
				'https://www.youtube.com/embed/67Y3dJQGN1U'
			);
			this.resourceSelected = 'premade';
		} else if (menu === 'search') {
			this.searchTicketsSrc = this.sanitizer.bypassSecurityTrustResourceUrl(
				'https://www.youtube.com/embed/XqdUeUFhPqc'
			);
			this.resourceSelected = 'search';
		}
	}
	public learnMore(url) {
		window.open(url);
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
}
