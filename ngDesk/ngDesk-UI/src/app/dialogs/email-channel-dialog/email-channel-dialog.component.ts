import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { LoaderService } from '../../custom-components/loader/loader.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ChannelsService } from '../../channels/channels.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { EmailChannel } from '../../models/email-channel';
import { UsersService } from '../../users/users.service';
import { CompaniesService } from './../../../app/companies/companies.service';

@Component({
	selector: 'app-email-channel-dialog',
	templateUrl: './email-channel-dialog.component.html',
	styleUrls: ['./email-channel-dialog.component.scss']
})
export class EmailChannelDialogComponent implements OnInit {
	public emailChannelForm: FormGroup;
	public emailChannel: EmailChannel = new EmailChannel(
		'',
		'email',
		this.data.emailChannelModuleId,
		'',
		'',
		''
	);
	public isVerified = false;
	public isVerifying = false;
	public active = 'paneOne'; // default set to project email channel form
	public forwardingConfirmed = false;
	public verificationTimer;
	public verificationCounter = 0;
	public subdomain: string;
	public errorMessage = '';
	public moduleId;
	public params = {
		emailAddress: {}
	};
	public errorParams = {
		name: {},
		emailAddress: {},
		type: {}
	};

	constructor(
		public dialogRef: MatDialogRef<EmailChannelDialogComponent>,
		private formBuilder: FormBuilder,
		private router: Router,
		private bannerMessageService: BannerMessageService,
		private channelsService: ChannelsService,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private translateService: TranslateService,
		private usersService: UsersService,
		private companiesService: CompaniesService,
		private loaderService: LoaderService
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
	}

	public ngOnInit() {
		// initializes an empty form
		this.emailChannelForm = this.formBuilder.group({
			name: ['', [Validators.required]],
			description: [''],
			emailAddress: ['', [Validators.required]],
			type: ['Internal', [Validators.required]]
		});

		if (this.data.emailId !== 'new') {
			this.channelsService
				.getEmailChannel(this.data.emailChannelModuleId, this.data.emailId)
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
							emailResponse.DATE_UPDATED
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
						this.emailChannelForm.controls.type.setValue(
							this.emailChannel.type
						);
					},
					(emailError: any) => {
						this.errorMessage = emailError.error.ERROR;
					}
				);
		}
	}

	public typeSelected(event) {
		// clears errorMessage if it was invalid type displaying
		if (this.errorMessage.indexOf('type') !== -1) {
			this.errorMessage = '';
		}
		// If Internal email is selected, does not need to validate email
		if (event.value === 'Internal') {
			this.emailChannelForm
				.get('emailAddress')
				.setValidators([Validators.required]);
		} else {
			this.companiesService.trackEvent('Adding external email');
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
			.postEmailVerify(this.data.emailChannelModuleId, this.emailChannel)
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
							.getEmailChannel(
								this.data.emailChannelModuleId,
								this.emailChannel.name
							)
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

	public save() {
		if (this.emailChannelForm.valid) {
			// initializes translation params
			this.params = {
				emailAddress: { value: this.emailChannelForm.value.emailAddress }
			};
			// updating values of request body with values from form
			this.emailChannel.name = this.emailChannelForm.value.name;
			this.emailChannel.description = this.emailChannelForm.value.description;
			this.emailChannel.type = this.emailChannelForm.value.type;
			// if internal, adds the ngdesk email domain at the end of the mailbox name
			this.emailChannel.emailAddress =
				this.emailChannel.type === 'Internal'
					? `${this.emailChannelForm.value.emailAddress}@${this.subdomain}.ngdesk.com`
					: this.emailChannelForm.value.emailAddress;
			if (this.data.emailId !== 'new') {
				this.channelsService
					.putEmailChannel(this.data.emailChannelModuleId, this.emailChannel)
					.subscribe(
						(emailSuccessResponse: any) => {
							this.channelCreateSuccess(emailSuccessResponse);
						},
						(emailError: any) => {
							this.isVerifying = false;
							this.errorMessage = emailError.error.ERROR;
						}
					);
			} else {
				this.channelsService
					.postTicketEmailChannel(
						this.data.emailChannelModuleId,
						this.emailChannel
					)
					.subscribe(
						(emailSuccessResponse: any) => {
							this.loaderService.isLoading = false;
							this.loaderService.isLoading2 = false;
							this.channelCreateSuccess(emailSuccessResponse);
							this.companiesService
								.getUsageType(this.usersService.getSubdomain())
								.subscribe(
									(usageType: any) => {
										if (
											usageType.USAGE_TYPE.TICKETS &&
											!usageType.USAGE_TYPE.CHAT
										) {
											this.companiesService
												.getAllGettingStarted()
												.subscribe((getAll: any) => {
													if (!getAll.GETTING_STARTED[2].COMPLETED) {
														this.companiesService
															.putGettingStarted(getAll.GETTING_STARTED[2])
															.subscribe(
																(put: any) => {
																	this.bannerMessageService.successNotifications.push(
																		{
																			message: 'Successfully Added'
																		}
																	);
																},
																(errorResponse: any) => {
																	console.log(errorResponse);
																}
															);
													}
												});
										} else {
											this.bannerMessageService.successNotifications.push({
												message: 'Successfully Added'
											});
										}
										// location.reload();
									},
									(error: any) => {
										console.log(error);
									}
								);
						},
						(emailError: any) => {
							this.isVerifying = false;
							this.errorMessage = emailError.error.ERROR;
						}
					);
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
		// go to next pane if external email
		// else, modal closes and creates new internal channel
		if (this.emailChannel.type === 'External') {
			this.active = 'paneTwo';
		} else {
			this.loaderService.isLoading = false;
			this.loaderService.isLoading2 = false;
			this.dialogRef.close();
		}
	}
}
