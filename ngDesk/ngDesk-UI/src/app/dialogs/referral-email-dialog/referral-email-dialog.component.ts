import { Component, OnInit, Inject } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';
import {
	MatDialogRef,
	MAT_DIALOG_DATA,
	MatDialog,
} from '@angular/material/dialog';
import { Validators, FormControl } from '@angular/forms';
import { ENTER, COMMA } from '@angular/cdk/keycodes';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { TranslateService } from '@ngx-translate/core';
import { ChannelsService } from 'src/app/channels/channels.service';
import { UsersService } from 'src/app/users/users.service';
import { CompaniesService } from 'src/app/companies/companies.service';
import { InviteUsersDialogComponent } from '../invite-users-dialog/invite-users-dialog.component';

@Component({
	selector: 'app-referral-email-dialog',
	templateUrl: './referral-email-dialog.component.html',
	styleUrls: ['./referral-email-dialog.component.scss'],
})
export class ReferralEmailDialogComponent implements OnInit {
	public EMAIL_REGEX = /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:.[a-zA-Z0-9-]+)*$/;
	public emailFormControl = new FormControl('', [
		Validators.required,
		Validators.pattern(this.EMAIL_REGEX),
	]);
	public script;
	public allTime;
	public currentMonth;
	public companyUUID;
	public userUUID;
	public userName;
	public referralEmails: string[] = [];
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA];
	public invalidEmail = false;
	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private channelsService: ChannelsService,
		public dialogRef: MatDialogRef<InviteUsersDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {}

	ngOnInit() {}

	public add(event: MatChipInputEvent): void {
		const input = event.input;
		const value = event.value;
		console.log('input ' + input);
		console.log('value ' + value);

		if (this.emailFormControl.hasError('pattern')) {
			this.invalidEmail = true;
			this.remove(value);
		} else {
			this.invalidEmail = false;
			if ((value || '').trim()) {
				// Add our email
				this.referralEmails.push(value.trim());
			}
			console.log(event);
			// Reset the input value
			if (input) {
				input.value = '';
			}
		}
	}

	public remove(email): void {
		const index = this.referralEmails.indexOf(email);
		if (index >= 0) {
			this.referralEmails.splice(index, 1);
		}
	}

	public sendEmailToDevelopers() {
		this.channelsService.referralEmail(this.referralEmails).subscribe(
			(data: any) => {
				this.bannerMessageService.successNotifications.push({
					message: this.translateService.instant('EMAIL_SENT_SUCCESSFULLY'),
				});
				this.referralEmails = [];
				this.dialogRef.close();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}
}
