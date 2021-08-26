import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { ChannelsService } from '../../channels/channels.service';
import { UsersService } from '../../users/users.service';

import { SMSChannel } from '../../models/sms-channel';

@Component({
	selector: 'app-sms-channel-dialog',
	templateUrl: './sms-channel-dialog.component.html',
	styleUrls: ['./sms-channel-dialog.component.scss'],
})
export class SMSChannelDialogComponent implements OnInit {
	public smsChannelForm: FormGroup;
	private smsChannel: SMSChannel = new SMSChannel('', '', false, false, '');
	public active = 'paneOne';
	public subdomain: string;
	public isSubmitting: boolean;
	public channelId: any;
	public phoneNumbers: any;
	public uuid = this.usersService.getAuthenticationToken();

	public errorMessage = '';
	public errorParams = {
		name: {},
		description: {},
		country: {},
		phoneNumber: {},
	};
	constructor(
		private dialogRef: MatDialogRef<SMSChannelDialogComponent>,
		private formBuilder: FormBuilder,
		private channelsService: ChannelsService,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private translateService: TranslateService,
		private usersService: UsersService
	) {
		this.translateService.get('NAME').subscribe((value: string) => {
			this.errorParams.name = { field: value };
		});
		this.translateService.get('DESCRIPTION').subscribe((value: string) => {
			this.errorParams.description = { field: value };
		});
		this.translateService.get('COUNTRY').subscribe((value: string) => {
			this.errorParams.country = { field: value };
		});
		this.translateService.get('PHONE_NUMBER').subscribe((value: string) => {
			this.errorParams.phoneNumber = { field: value };
		});
		this.subdomain = this.usersService.getSubdomain();
	}

	public ngOnInit() {
		this.smsChannelForm = this.formBuilder.group({
			name: ['', [Validators.required]],
			description: [''],
			country: [
				{ value: this.data.selectedCountry.COUNTRY_NAME, disabled: true },
			],
			phoneNumber: ['', [Validators.required]],
		});

		this.getPhoneNumbers(this.data.selectedCountry);
		if (this.data.smsChannelId !== 'new') {
			this.channelsService
				.getSMSChannel(this.data.smsModuleId, this.data.smsChannelId)
				.subscribe(
					(smsChannelResponse: any) => {
						// cast the api response and sets values of form elements
						this.smsChannel = new SMSChannel(
							smsChannelResponse.NAME,
							smsChannelResponse.DESCRIPTION,
							smsChannelResponse.WHATSAPP_ENABLED,
							smsChannelResponse.WHATSAPP_REQUESTED,
							smsChannelResponse.PHONE_NUMBER,
							smsChannelResponse.VERIFIED,
							smsChannelResponse.CHANNEL_ID,
							smsChannelResponse.MODULE,
							smsChannelResponse.DATE_CREATED,
							smsChannelResponse.LAST_UPDATED_BY,
							smsChannelResponse.CREATED_BY,
							smsChannelResponse.DATE_UPDATED
						);
						this.smsChannelForm.controls.name.setValue(this.smsChannel.name);
						this.smsChannelForm.controls.description.setValue(
							this.smsChannel.description
						);
						this.smsChannelForm.controls.country.setValue(
							this.data.selectedCountry.COUNTRY_NAME
						);
					},
					(emailError: any) => {
						this.errorMessage = emailError.error.ERROR;
					}
				);
		}
	}

	public save() {
		if (this.smsChannelForm.valid) {
			// updating values of request body with values from form
			this.smsChannel.name = this.smsChannelForm.value.name;
			this.smsChannel.description = this.smsChannelForm.value.description;
			this.smsChannel.module = this.data.smsModuleId;
			this.smsChannel.verified = true;
			this.smsChannel.phoneNumber = this.smsChannelForm.value.phoneNumber;
			this.isSubmitting = true;
			this.buyPhoneNumber();
		}
	}
	public getPhoneNumbers(country) {
		this.channelsService
			.getTwilioPhoneNumbers(this.data.smsModuleId, country.COUNTRY_CODE)
			.subscribe(
				(data: any) => {
					this.phoneNumbers = data;
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
	}

	public buyPhoneNumber() {
		this.channelsService
			.postSMSChannel(this.smsChannel.module, this.smsChannel)
			.subscribe(
				(data: any) => {
					this.channelId = data.CHANNEL_ID;
					this.isSubmitting = false;
					this.dialogRef.close();
				},
				(errorResponse: any) => {
					this.errorMessage = errorResponse.error.ERROR;
					this.isSubmitting = false;
				}
			);
	}
}
