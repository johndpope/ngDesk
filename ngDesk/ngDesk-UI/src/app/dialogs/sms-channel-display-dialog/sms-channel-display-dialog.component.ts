import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { ChannelsService } from '../../channels/channels.service';
import { SMSChannel } from '../../models/sms-channel';

@Component({
	selector: 'app-sms-channel-display-dialog',
	templateUrl: './sms-channel-display-dialog.component.html',
	styleUrls: ['./sms-channel-display-dialog.component.scss'],
})
export class SMSChannelDisplayDialogComponent implements OnInit {
	public smsChannelForm: FormGroup;
	public smsChannel: SMSChannel = new SMSChannel('', '', false, false, '');
	public errorMessage = '';
	public errorParams = {
		name: {},
		description: {},
		phoneNumber: {},
	};

	constructor(
		private dialogRef: MatDialogRef<SMSChannelDisplayDialogComponent>,
		private formBuilder: FormBuilder,
		private channelsService: ChannelsService,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private translateService: TranslateService
	) {
		this.translateService.get('NAME').subscribe((value: string) => {
			this.errorParams.name = { field: value };
		});
		this.translateService.get('DESCRIPTION').subscribe((value: string) => {
			this.errorParams.description = { field: value };
		});
		this.translateService.get('PHONE_NUMBER').subscribe((value: string) => {
			this.errorParams.phoneNumber = { field: value };
		});
	}

	public ngOnInit() {
		this.smsChannelForm = this.formBuilder.group({
			name: ['', [Validators.required]],
			description: [''],
			phoneNumber: [{ value: '', disabled: true }],
		});

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
						this.smsChannelForm.controls.phoneNumber.setValue(
							this.smsChannel.phoneNumber
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
			this.smsChannel.name = this.smsChannelForm.value.name;
			this.smsChannel.description = this.smsChannelForm.value.description;
			this.smsChannel.module = this.data.smsModuleId;
			if (this.data.smsChannelId !== 'new') {
				this.channelsService
					.putSMSChannel(this.smsChannel.module, this.smsChannel)
					.subscribe(
						(smsSuccessResponse: any) => {
							this.dialogRef.close();
						},
						(smsError: any) => {
							this.errorMessage = smsError.error.ERROR;
						}
					);
			}
		}
	}

	public enableWhatsapp() {
		this.channelsService
			.postWhatsappRequest(this.smsChannel.module, this.smsChannel.channelId)
			.subscribe(
				(data: any) => {
					this.dialogRef.close();
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
	}
}
