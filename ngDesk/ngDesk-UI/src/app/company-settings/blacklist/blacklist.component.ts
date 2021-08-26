import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, OnInit } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';

@Component({
	selector: 'app-blacklist',
	templateUrl: './blacklist.component.html',
	styleUrls: ['./blacklist.component.scss']
})
export class BlacklistComponent implements OnInit {
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA];
	public EMAIL_LIST = {
		BLACK_LIST_INCOMING: [],
		BLACK_LIST_OUTGOING: [],
		WHITE_LIST_INCOMING: [],
		WHITE_LIST_OUTGOING: []
	};

	constructor(
		private translateService: TranslateService,
		private companiesService: CompaniesService,
		private bannerMessageService: BannerMessageService
	) {}

	public ngOnInit() {
		this.companiesService.getBlacklistWhitelist().subscribe(
			(response: any) => {
				this.EMAIL_LIST.BLACK_LIST_INCOMING =
					response.DATA.BLACK_LISTED_INCOMING;
				this.EMAIL_LIST.BLACK_LIST_OUTGOING =
					response.DATA.BLACK_LISTED_OUTGOING;
				this.EMAIL_LIST.WHITE_LIST_INCOMING =
					response.DATA.WHITE_LISTED_INCOMING;
				this.EMAIL_LIST.WHITE_LIST_OUTGOING =
					response.DATA.WHITE_LISTED_OUTGOING;
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR
				});
			}
		);
	}

	public add(event: MatChipInputEvent, list): void {
		const input = event.input;
		const value = event.value;

		// Add our email
		if ((value || '').trim()) {
			this.EMAIL_LIST[list].push(value.trim());
		}

		// Reset the input value
		if (input) {
			input.value = '';
		}
	}

	public remove(email, list): void {
		const index = this.EMAIL_LIST[list].indexOf(email);
		if (index >= 0) {
			this.EMAIL_LIST[list].splice(index, 1);
		}
	}

	public save() {
		this.companiesService.putBlacklistWhitelist(this.EMAIL_LIST).subscribe(
			(response: any) => {
				this.bannerMessageService.successNotifications.push({
					message: this.translateService.instant('SAVED_SUCCESSFULLY')
				});
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR
				});
			}
		);
	}
}
