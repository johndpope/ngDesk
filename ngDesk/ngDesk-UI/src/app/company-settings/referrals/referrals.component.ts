import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, OnInit } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { ChannelsService } from 'src/app/channels/channels.service';
import { FormControl, Validators } from '@angular/forms';
import { UsersService } from 'src/app/users/users.service';
import { CompaniesService } from 'src/app/companies/companies.service';
import { ReferralEmailDialogComponent } from 'src/app/dialogs/referral-email-dialog/referral-email-dialog.component';

@Component({
	selector: 'app-referrals',
	templateUrl: './referrals.component.html',
	styleUrls: ['./referrals.component.scss']
})
export class ReferralsComponent implements OnInit {
	public script;
	public allTime;
	public currentMonth;
	public companyUUID;
	public userUUID;
	public userName;
	public invalidEmail = false;
	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private channelsService: ChannelsService,
		private usersService: UsersService,
		private companiesService: CompaniesService,
		private dialog: MatDialog
	) {}

	ngOnInit() {
		this.companyUUID = this.usersService.companyUuid;
		this.userUUID = this.usersService.user.USER_UUID;
		this.userName =
			this.usersService.user.FIRST_NAME +
			' ' +
			this.usersService.user.LAST_NAME;
		this.script =
			'https://signup.ngdesk.com/landing-pages/signup?c_id=' +
			this.companyUUID +
			'&u_id=' +
			this.userUUID;

		this.companiesService.getReferralsCount().subscribe((response: any) => {
			this.allTime = response.ALL_TIME;
			this.currentMonth = response.CURRENT_MONTH;
		});
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
			message: this.translateService.instant('COPIED')
		});
	}

	public enterEmail() {
		const dialogRef = this.dialog.open(ReferralEmailDialogComponent, {});
	}
}
