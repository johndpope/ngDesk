import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { ChannelsService } from 'src/app/channels/channels.service';
import { UsersService } from 'src/app/users/users.service';
import { TwilioRequest } from '../../models/twilio-request';

@Component({
	selector: 'app-twilio-request',
	templateUrl: './twilio-request.component.html',
	styleUrls: ['./twilio-request.component.scss'],
})
export class TwilioRequestComponent implements OnInit {
	public twilioRequestForm: FormGroup;
	private twilioRequest: TwilioRequest = new TwilioRequest(
		'',
		'',
		'',
		'',
		'',
		{}
	);
	public active = 'paneOne';
	public selectedCountry: any;
	public isSubmitting: boolean;
	private user = this.usersService.user;
	private subdomain = this.usersService.getSubdomain();
	public errorMessage = '';
	public errorParams = {
		firstName: {},
		lastName: {},
		emailAddress: {},
		phoneNumber: {},
		country: {},
	};

	constructor(
		private dialogRef: MatDialogRef<TwilioRequestComponent>,
		private formBuilder: FormBuilder,
		private channelsService: ChannelsService,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private translateService: TranslateService,
		private usersService: UsersService
	) {
		this.translateService.get('FIRST_NAME').subscribe((value: string) => {
			this.errorParams.firstName = { field: value };
		});
		this.translateService.get('LAST_NAME').subscribe((value: string) => {
			this.errorParams.lastName = { field: value };
		});
		this.translateService.get('EMAIL_ADDRESS').subscribe((value: string) => {
			this.errorParams.emailAddress = { field: value };
		});
		this.translateService.get('PHONE_NUMBER').subscribe((value: string) => {
			this.errorParams.phoneNumber = { field: value };
		});
		this.translateService.get('COUNTRY').subscribe((value: string) => {
			this.errorParams.country = { field: value };
		});
	}

	public ngOnInit() {
		this.twilioRequestForm = this.formBuilder.group({
			firstName: [this.user.FIRST_NAME, [Validators.required]],
			lastName: [this.user.LAST_NAME, [Validators.required]],
			emailAddress: [this.user.EMAIL_ADDRESS, [Validators.required]],
			phoneNumber: [this.user.PHONE_NUMBER.PHONE_NUMBER, [Validators.required]],
			country: [
				{ value: this.data.selectedCountry.COUNTRY_NAME, disabled: true },
			],
		});
	}
	public request() {
		this.isSubmitting = true;
		this.twilioRequest = new TwilioRequest(
			this.subdomain,
			this.twilioRequestForm.value.emailAddress,
			this.twilioRequestForm.value.firstName,
			this.twilioRequestForm.value.lastName,
			this.twilioRequestForm.value.phoneNumber,
			this.channelsService.twilioSupportedCountries[
				this.channelsService.twilioSupportedCountries
					.map((a) => a.COUNTRY_NAME)
					.indexOf(this.data.selectedCountry.COUNTRY_NAME)
			]
		);
		this.channelsService
			.sendEmailForTwilioRequest(this.data.smsModuleId, this.twilioRequest)
			.subscribe(
				() => {
					this.dialogRef.close();
					this.isSubmitting = false;
				},
				(error: any) => {
					console.log(error);
					this.errorMessage = error.error.ERROR;
				}
			);
	}
}
