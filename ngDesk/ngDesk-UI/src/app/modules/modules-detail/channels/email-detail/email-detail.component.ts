import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTabGroup } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ChannelsService } from '../../../../channels/channels.service';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { EmailChannel } from '../../../../models/email-channel';
import { ModuleMapping } from '../../../../models/moduleMapping';
import { UsersService } from '../../../../users/users.service';
import { ModulesService } from '../../../modules.service';
@Component({
	selector: 'app-email-detail',
	templateUrl: './email-detail.component.html',
	styleUrls: ['./email-detail.component.scss'],
})
export class EmailDetailComponent implements OnInit, OnDestroy {
	public notificationSub;
	public emailChannelForm: FormGroup;
	public emailChannel: EmailChannel = new EmailChannel(
		'',
		'email',
		'moduleId',
		'',
		'',
		''
	);

	@ViewChild('chatSettingsTabs')
	public chatSettingsTabs: MatTabGroup;

	public createMapping: ModuleMapping = new ModuleMapping(
		null,
		null,
		null,
		null,
		null,
		null
	);
	public updateMapping: ModuleMapping = new ModuleMapping(
		null,
		null,
		null,
		null,
		null,
		null
	);
	public channelSet = false;
	public isVerified = false;
	public isVerifying = false;
	public active = 'paneOne'; // default set to project email channel form
	public forwardingConfirmed = false;
	public verificationTimer;
	public verificationCounter = 0;
	public channelType = '';
	public subdomain: string;
	public emailId;
	public moduleId;
	public step = 1;
	public selectedTab = 0;
	public errorMessage = '';
	public params = {
		emailAddress: {},
	};
	public errorParams = {
		name: {},
		emailAddress: {},
		type: {},
	};
	public currentTab = 1;
	public dialogRef: any;
	public module: any;
	public emailChannelFields = [
		{ NAME: 'SUBJECT', DISPLAY: 'Subject' },
		{ NAME: 'BODY', DISPLAY: 'Body' },
		{ NAME: 'CC_EMAILS', DISPLAY: 'CC Emails' },
		{ NAME: 'TEAMS', DISPLAY: 'Teams' },
		{ NAME: 'REQUESTOR', DISPLAY: 'From' },
		{ NAME: 'FROM_EMAIL', DISPLAY: 'From Email' },
	];
	public moduleFields: any;
	public subjectField = null;
	public bodyField = null;
	public ccemailField = null;
	public teamsField = null;
	public requestorField = null;
	public fromemailField = null;

	// Fields for UpdateMapping

	// public subjectFieldUpdate = null;
	// public bodyFieldUpdate = null;
	// public ccemailFieldUpdate = null;
	// public teamsFieldUpdate = null;
	// public requestorFieldUpdate = null;
	// public fromemailFieldUpdate = null;
	public loading = true;
	public subjectFields = [];
	public bodyFields = [];
	public ccemailFields = [];
	public subjectFieldsUpdate = [];
	public bodyFieldsUpdate = [];
	public ccemailFieldsUpdate = [];
	public teamsFields = [];
	public requestorFields = [];
	public fromemailFields = [];
	public isForwardEmail = false;
	public enableSaveButton = true;
	private missingFieldName = '';
	private windowReference;
	private usersModuleId;
	constructor(
		private modulesService: ModulesService,
		private router: Router,
		private route: ActivatedRoute,
		private formBuilder: FormBuilder,
		private bannerMessageService: BannerMessageService,
		private channelsService: ChannelsService,
		// 	@Inject(MAT_DIALOG_DATA) public data: any,
		private translateService: TranslateService,
		private usersService: UsersService
	) {
		this.emailId = this.route.snapshot.params['emailId'];
		this.moduleId = this.route.snapshot.params['moduleId'];

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
	}

	public ngOnInit() {
		// initializes an empty form
		this.emailChannelForm = this.formBuilder.group({
			name: ['', [Validators.required]],
			description: [''],
			emailAddress: ['', [Validators.required, Validators.email]],
		});
		this.emailChannel.MODULE = this.moduleId;
		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.module = response;
				this.moduleFields = this.module.FIELDS;
				this.loading = false;
				this.emailChannelFields.forEach((element) => {
					if (element.NAME === 'SUBJECT') {
						this.moduleFields.forEach((field) => {
							if (
								field.DATA_TYPE.DISPLAY === 'Text' &&
								field.NAME !== 'CHANNEL'
							) {
								this.subjectFields.push(field);
								this.subjectFieldsUpdate.push(field);
							}
						});
					} else if (element.NAME === 'BODY') {
						this.moduleFields.forEach((field) => {
							if (field.DATA_TYPE.DISPLAY === 'Discussion') {
								this.bodyFields.push(field);
								this.bodyFieldsUpdate.push(field);
							}
						});
					} else if (element.NAME === 'CC_EMAILS') {
						this.moduleFields.forEach((field) => {
							if (field.DATA_TYPE.DISPLAY === 'List Text') {
								this.ccemailFields.push(field);
								this.ccemailFieldsUpdate.push(field);
							}
						});
					} else if (element.NAME === 'TEAMS') {
						this.moduleFields.forEach((field) => {
							if (field.NAME === 'TEAMS') {
								this.teamsField = field.FIELD_ID;
							}
						});
					} else if (element.NAME === 'REQUESTOR') {
						this.moduleFields.forEach((field) => {
							if (
								field.DATA_TYPE.DISPLAY === 'Relationship' &&
								field.RELATIONSHIP_TYPE === 'Many to One' &&
								field.NAME !== 'CREATED_BY' &&
								field.NAME !== 'LAST_UPDATED_BY'
							) {
								this.modulesService
									.getModuleByName('Users')
									.subscribe((moduleResponse: any) => {
										this.usersModuleId = moduleResponse.MODULE_ID;
										if (moduleResponse.MODULE_ID === field.MODULE) {
											this.requestorFields.push(field);
										}
									});
							}
						});
					} else if (element.NAME === 'FROM_EMAIL') {
						this.moduleFields.forEach((field) => {
							if (
								(field.DATA_TYPE.DISPLAY === 'Text' ||
									field.DATA_TYPE.DISPLAY === 'Email') &&
								field.NAME !== 'CHANNEL'
							) {
								this.fromemailFields.push(field);
							}
						});
					}
				});
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
				this.bannerMessageService.errorNotifications.push({
					message: this.errorMessage,
				});
			}
		);

		if (this.emailId !== 'new') {
			this.channelsService
				.getEmailChannel(this.moduleId, this.emailId)
				.subscribe(
					(emailResponse: any) => {
						// cast the api response and sets values of form elements
						this.emailChannel = new EmailChannel(
							emailResponse.NAME,
							emailResponse.SOURCE_TYPE,
							emailResponse.MODULE,
							emailResponse.DESCRIPTION,
							emailResponse.EMAIL_ADDRESS,
							emailResponse.TYPE,
							emailResponse.CHANNEL_ID,
							emailResponse.IS_VERIFIED,
							emailResponse.WORKFLOW,
							emailResponse.DATE_CREATED,
							emailResponse.LAST_UPDATED_BY,
							emailResponse.DATE_UPDATED,
							emailResponse.CREATE_MAPPING,
							emailResponse.UPDATE_MAPPING
						);
						if (this.emailChannel.type === 'Internal') {
							this.emailChannelForm
								.get('emailAddress')
								.setValidators([Validators.required]);
							this.emailChannel.emailAddress = this.emailChannel.emailAddress.split(
								'@'
							)[0];
						}
						this.emailChannelForm.controls.name.setValue(
							this.emailChannel.name
						);
						this.emailChannelForm.controls.description.setValue(
							this.emailChannel.description
						);
						this.emailChannelForm.controls.emailAddress.setValue(
							this.emailChannel.emailAddress
						);
						this.channelType = this.emailChannel.type;
						this.subjectField = this.emailChannel.CREATE_MAPPING.SUBJECT;
						this.bodyField = this.emailChannel.CREATE_MAPPING.BODY;
						this.ccemailField = this.emailChannel.CREATE_MAPPING.CC_EMAILS;
						//this.teamsField = this.emailChannel.CREATE_MAPPING.TEAMS;
						this.fromemailField = this.emailChannel.CREATE_MAPPING.FROM_EMAIL;
						this.requestorField = this.emailChannel.CREATE_MAPPING.REQUESTOR;

						// Binding Update Mapping values

						// this.subjectFieldUpdate = this.emailChannel.UPDATE_MAPPING.SUBJECT;
						// this.bodyFieldUpdate = this.emailChannel.UPDATE_MAPPING.BODY;
						// this.ccemailFieldUpdate = this.emailChannel.UPDATE_MAPPING.CC_EMAILS;
						// this.teamsFieldUpdate = this.emailChannel.UPDATE_MAPPING.TEAMS;
						// this.fromemailFieldUpdate = this.emailChannel.UPDATE_MAPPING.FROM_EMAIL;
						// this.requestorFieldUpdate = this.emailChannel.UPDATE_MAPPING.REQUESTOR;
					},
					(emailError: any) => {
						this.errorMessage = emailError.error.ERROR;
						this.bannerMessageService.errorNotifications.push({
							message: this.errorMessage,
						});
					}
				);
		}

		// const notifySubscription$ = this._stompService._stompRestService
		// 	.watch(`rest/${this.moduleId}/channels/email/mapping/field-created`)
		// 	.pipe(shareReplay());
		// this.notificationSub = notifySubscription$.subscribe(
		// 	(notification: any) => {
		// 		this.initializeMissingFields();
		// 	}
		// );
	}

	public typeSelected(event) {
		// clears errorMessage if it was invalid type displaying
		if (this.errorMessage.indexOf('type') !== -1) {
			this.errorMessage = '';
			this.bannerMessageService.errorNotifications.push({
				message: this.errorMessage,
			});
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

	// This is to check email verification
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
					this.bannerMessageService.errorNotifications.push({
						message: this.errorMessage,
					});
				}
			);
	}

	public save() {
		if (this.channelSet && this.selectedTab === 1) {
			if (
				this.emailChannelForm.valid &&
				this.bodyField !== null &&
				this.subjectField !== null &&
				this.teamsField !== null
			) {
				if (this.emailChannelForm.get('emailAddress').hasError('required')) {
					this.bannerMessageService.errorNotifications.push({
						message: this.errorParams.emailAddress,
					});
				}
				if (this.emailChannelForm.get('emailAddress').hasError('email')) {
					this.bannerMessageService.errorNotifications.push({
						message: 'Email must be valid',
					});
				}
				// initializes translation params
				let fieldIds = [];
				fieldIds.push(
					this.bodyField,
					this.subjectField,
					this.teamsField,
					this.ccemailField,
					this.requestorField,
					this.fromemailField
				);
				let found = false;
				for (let i = 0; i < fieldIds.length; i++) {
					for (let j = i; j < fieldIds.length; j++) {
						if (
							fieldIds[i] === fieldIds[j] &&
							i !== j &&
							fieldIds[i] !== null &&
							fieldIds[i] !== undefined &&
							fieldIds[j] !== null &&
							fieldIds[j] !== undefined
						) {
							this.bannerMessageService.errorNotifications.push({
								message:
									'Two or more email channel fields have been mapped to same field of the module',
							});
							found = true;
							break;
						}
					}
					if (found === true) {
						return;
					}
				}

				// Binding UpdateMapping Values

				// this.teamsFieldUpdate = this.teamsField;
				// this.fromemailFieldUpdate = this.fromemailField;
				// this.requestorFieldUpdate = this.requestorField;
				this.params = {
					emailAddress: { value: this.emailChannelForm.value.emailAddress },
				};
				// updating values of request body with values from form
				this.emailChannel.name = this.emailChannelForm.value.name;
				this.emailChannel.description = this.emailChannelForm.value.description;
				this.emailChannel.type = this.channelType;
				// if internal, adds the ngdesk email domain at the end of the mailbox name
				this.emailChannel.emailAddress =
					this.emailChannel.type === 'Internal'
						? `${this.emailChannelForm.value.emailAddress}@${this.subdomain}.ngdesk.com`
						: this.emailChannelForm.value.emailAddress;
				if (this.emailId !== 'new') {
					this.createMapping.BODY = this.bodyField;
					this.createMapping.CC_EMAILS = this.ccemailField;
					this.createMapping.FROM_EMAIL = this.fromemailField;
					this.createMapping.REQUESTOR = this.requestorField;
					this.createMapping.SUBJECT = this.subjectField;
					this.createMapping.TEAMS = this.teamsField;

					// Binding Values for UpdateMapping

					// this.updateMapping.BODY = this.bodyFieldUpdate;
					// this.updateMapping.CC_EMAILS = this.ccemailFieldUpdate;
					// this.updateMapping.SUBJECT = this.subjectFieldUpdate;
					// this.updateMapping.FROM_EMAIL = this.fromemailFieldUpdate;
					// this.updateMapping.REQUESTOR = this.requestorFieldUpdate;
					// this.updateMapping.TEAMS = this.teamsFieldUpdate;

					this.emailChannel.CREATE_MAPPING = this.createMapping;
					// TODO remove below line after update Mapping is added
					this.updateMapping = this.createMapping;
					this.emailChannel.UPDATE_MAPPING = this.updateMapping;
					this.channelsService
						.putEmailChannel(this.moduleId, this.emailChannel)
						.subscribe(
							(emailSuccessResponse: any) => {
								this.channelCreateSuccess(emailSuccessResponse);
								if (this.emailChannel.type === 'Internal') {
									this.isForwardEmail = false;
									this.router.navigate([
										`modules/${this.moduleId}/channels/email`,
									]);
								} else {
									this.isForwardEmail = true;
									if (emailSuccessResponse.IS_VERIFIED === false) {
										this.emailChannel = emailSuccessResponse;
										this.selectedTab = this.selectedTab + 1;
										this.enableSaveButton = false;
										this.setStep(3);
										this.active = 'paneTwo';
									} else {
										this.bannerMessageService.successNotifications.push({
											message: 'Successfully Added',
										});
										this.router.navigate([
											`modules/${this.moduleId}/channels/email`,
										]);
									}
								}
							},
							(emailError: any) => {
								this.isVerifying = false;
								this.errorMessage = emailError.error.ERROR;
								this.bannerMessageService.errorNotifications.push({
									message: this.errorMessage,
								});
							}
						);
				} else {
					this.createMapping.BODY = this.bodyField;
					this.createMapping.CC_EMAILS = this.ccemailField;
					this.createMapping.FROM_EMAIL = this.fromemailField;
					this.createMapping.REQUESTOR = this.requestorField;
					this.createMapping.SUBJECT = this.subjectField;
					this.createMapping.TEAMS = this.teamsField;

					// Binding values for Update Mapping
					// this.updateMapping.FROM_EMAIL = this.fromemailFieldUpdate;
					// this.updateMapping.REQUESTOR = this.requestorFieldUpdate;
					// this.updateMapping.TEAMS = this.teamsFieldUpdate;
					// this.updateMapping.BODY = this.bodyFieldUpdate;
					// this.updateMapping.CC_EMAILS = this.ccemailFieldUpdate;
					// this.updateMapping.SUBJECT = this.subjectFieldUpdate;

					// this.emailChannel.UPDATE_MAPPING.BODY = this.fieldMap['BODY'];
					// this.emailChannel.UPDATE_MAPPING.SUBJECT = this.fieldMap['SUBJECT'];
					// this.emailChannel.UPDATE_MAPPING.REQUESTOR = this.fieldMap['REQUESTOR'];
					// this.emailChannel.UPDATE_MAPPING.FROM_EMAIL = this.fieldMap[
					// 	'FROM_EMAIL'
					// ];
					// this.emailChannel.UPDATE_MAPPING.CC_EMAILS = this.fieldMap['CC_EMAILS'];
					// this.emailChannel.UPDATE_MAPPING.TEAMS = this.fieldMap['TEAMS'];
					this.emailChannel.CREATE_MAPPING = this.createMapping;

					// TODO remove below line after update Mapping is added
					this.updateMapping = this.createMapping;
					this.emailChannel.UPDATE_MAPPING = this.updateMapping;
					this.channelsService
						.postTicketEmailChannel(this.moduleId, this.emailChannel)
						.subscribe(
							(emailSuccessResponse: any) => {
								this.bannerMessageService.successNotifications.push({
									message: 'Successfully Added',
								});
								if (this.emailChannel.type === 'Internal') {
									this.isForwardEmail = false;
									this.router.navigate([
										`modules/${this.moduleId}/channels/email`,
									]);
								} else {
									this.isForwardEmail = true;
									this.emailChannel = emailSuccessResponse;
									this.selectedTab = this.selectedTab + 1;
									this.enableSaveButton = false;
									this.setStep(3);
									this.active = 'paneTwo';
								}
							},
							(emailError: any) => {
								this.isVerifying = false;
								this.errorMessage = emailError.error.ERROR;
								this.bannerMessageService.errorNotifications.push({
									message: this.errorMessage,
								});
							}
						);
				}
			} else {
				return this.bannerMessageService.errorNotifications.push({
					message: 'Fill all required fields',
				});
			}
		} else {
			if (this.channelType === '') {
				return this.bannerMessageService.errorNotifications.push({
					message: 'Select type of Channel',
				});
			} else if (
				this.emailChannelForm.value.name === '' ||
				this.emailChannelForm.value.emailAddress === ''
			) {
				return this.bannerMessageService.errorNotifications.push({
					message: 'Fill all required fields',
				});
			} else {
				this.selectedTab = 1;
				this.channelSet = true;
			}
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
	}

	public done() {
		this.router.navigate([`modules/${this.moduleId}/channels/email`]);
	}

	public fieldCreator(fieldName) {
		// this.router.navigate([
		// 	{
		// 		outlets: {
		// 			primary: 'dashboard',
		// 			main: `modules/${this.moduleId}/field/field-creator`
		// 		}
		// 	}
		// ]);
		this.missingFieldName = fieldName;
		let dataType = '';
		if (this.missingFieldName === 'SUBJECT') {
			dataType = 'Text';
		} else if (this.missingFieldName === 'BODY') {
			dataType = 'Discussion';
		} else if (this.missingFieldName === 'CC_EMAILS') {
			dataType = 'List Text';
		} else if (this.missingFieldName === 'REQUESTOR') {
			dataType = 'Relationship';
		} else if (this.missingFieldName === 'FROM_EMAIL') {
			dataType = 'Text';
		}
		if (dataType && dataType !== '' && dataType !== 'Relationship') {
			const origin = window.location.origin;
			this.windowReference = window.open(
				`${origin}/modules/${this.moduleId}/field/field-creator/${dataType}`,
				'_blank'
			);
		} else if (dataType && dataType !== '' && dataType === 'Relationship') {
			this.modulesService
				.getModuleByName('Users')
				.subscribe((moduleResponse: any) => {
					this.usersModuleId = moduleResponse.MODULE_ID;
					const origin = window.location.origin;
					this.windowReference = window.open(
						`${origin}/modules/${this.usersModuleId}/field/field-creator/${dataType}`,
						'_blank'
					);
				});
		}
	}
	public setStep(index: number) {
		this.step = index;
	}

	public onSelectTabs(event) {
		if (event.index === 0) {
			this.enableSaveButton = false;
			this.selectedTab = 0;
		} else {
			this.selectedTab = event.index;
			this.enableSaveButton = true;
		}
	}

	public initializeMissingFields() {
		if (this.moduleId) {
			this.modulesService
				.getFields(this.moduleId)
				.subscribe((response: any) => {
					const allFields = response.FIELDS;
					if (this.missingFieldName === 'SUBJECT') {
						allFields.forEach((field) => {
							if (
								field.DATA_TYPE.DISPLAY === 'Text' &&
								field.NAME !== 'CHANNEL'
							) {
								this.subjectFields.push(field);
								this.subjectFieldsUpdate.push(field);
								this.missingFieldName = '';
								this.windowReference.close();
							}
						});
					} else if (this.missingFieldName === 'BODY') {
						allFields.forEach((field) => {
							if (field.DATA_TYPE.DISPLAY === 'Discussion') {
								this.bodyFields.push(field);
								this.bodyFieldsUpdate.push(field);
								this.missingFieldName = '';
								this.windowReference.close();
							}
						});
					} else if (this.missingFieldName === 'CC_EMAILS') {
						allFields.forEach((field) => {
							if (field.DATA_TYPE.DISPLAY === 'List Text') {
								this.ccemailFields.push(field);
								this.ccemailFieldsUpdate.push(field);
								this.missingFieldName = '';
								this.windowReference.close();
							}
						});
					} else if (this.missingFieldName === 'REQUESTOR') {
						allFields.forEach((field) => {
							if (
								field.DATA_TYPE.DISPLAY === 'Relationship' &&
								field.RELATIONSHIP_TYPE === 'Many to One' &&
								field.NAME !== 'CREATED_BY' &&
								field.NAME !== 'LAST_UPDATED_BY'
							) {
								this.modulesService
									.getModuleByName('Users')
									.subscribe((moduleResponse: any) => {
										if (moduleResponse.MODULE_ID === field.MODULE) {
											this.requestorFields.push(field);
											this.missingFieldName = '';
											this.windowReference.close();
										}
									});
							}
						});
					} else if (this.missingFieldName === 'FROM_EMAIL') {
						allFields.forEach((field) => {
							if (
								(field.DATA_TYPE.DISPLAY === 'Text' ||
									field.DATA_TYPE.DISPLAY === 'Email') &&
								field.NAME !== 'CHANNEL'
							) {
								this.fromemailFields.push(field);
								this.missingFieldName = '';
								this.windowReference.close();
							}
						});
					}
				});
		}
	}

	private back() {
		if (this.selectedTab === 1 && this.channelSet) {
			this.selectedTab = 0;
		} else {
			this.channelType = '';
		}
	}

	public ngOnDestroy() {
		if (this.notificationSub) {
			this.notificationSub.unsubscribe();
		}
	}
}
