import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import * as _moment from 'moment';
import { OWL_DATE_TIME_FORMATS } from '@danielmoncada/angular-datetime-picker';
import { ImportService } from '../import.service';

import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { OWL_DATE_FORMATS } from '@src/app/render-layout/data-types/date-time.service';

@Component({
	selector: 'app-zendesk-import',
	templateUrl: './zendesk-import.component.html',
	styleUrls: ['./zendesk-import.component.scss'],
	providers: [{ provide: OWL_DATE_TIME_FORMATS, useValue: OWL_DATE_FORMATS }],
})
export class ZendeskImportComponent implements OnInit {
	public migrationForm: FormGroup;
	public settingsForm: FormGroup;
	public currentDate = new Date();
	public emailErrorMessages: { type: string; message: string }[] = [];
	public errorParams = {
		zendeskDomain: {},
		apiKey: {},
		ngdeskDomain: {},
		importTicketsFrom: {},
		emailAddress: {},
	};

	constructor(
		private formBuilder: FormBuilder,
		private translateService: TranslateService,
		private importService: ImportService,
		private bannerMessageService: BannerMessageService
	) {
		this.translateService.get('ZENDESK_DOMAIN').subscribe((value: string) => {
			this.errorParams.zendeskDomain = { field: value };
		});

		this.translateService.get('API_KEY').subscribe((value: string) => {
			this.errorParams.apiKey = { field: value };
		});

		this.translateService.get('NGDESK_DOMAIN').subscribe((value: string) => {
			this.errorParams.ngdeskDomain = { field: value };
		});

		this.translateService.get('EMAIL_ADDRESS').subscribe((value: string) => {
			this.errorParams.emailAddress = { field: value };
		});

		this.translateService
			.get('IMPORT_TICKETS_FROM_DATE')
			.subscribe((value: string) => {
				this.errorParams.importTicketsFrom = { field: value };
			});

		this.translateService
			.get('EMAIL_MUST_BE_VALID')
			.subscribe((value: string) => {
				this.emailErrorMessages.push({ type: 'email', message: value });
			});

		this.translateService.get('EMAIL_ADDRESS').subscribe((value: string) => {
			this.translateService
				.get('FIELD_REQUIRED', { field: value })
				.subscribe((requValue: string) => {
					this.emailErrorMessages.push({
						type: 'required',
						message: requValue,
					});
				});
		});
	}

	public ngOnInit() {
		this.migrationForm = this.formBuilder.group({
			IMPORT_TICKETS_FROM: [''],
			API_TOKEN: ['', Validators.required],
			EMAIL_ADDRESS: ['', [Validators.required, Validators.email]],
			SUBDOMAIN: ['', Validators.required],
		});

		// to set import ticket date from one month prior
		const d = new Date();
		const m = d.getMonth();
		d.setMonth(d.getMonth() - 1);

		// If still in same month, set date to last day of
		// previous month
		if (d.getMonth() === m) {
			d.setDate(0);
		}
		d.setHours(0, 0, 0);
		d.setMilliseconds(0);

		this.settingsForm = this.formBuilder.group({
			// IMPORT_USERS_GROUPS_COMPANIES: [false, Validators.required],
			IMPORT_TICKETS: [true, Validators.required],
			IMPORT_TICKET_OPTION: ['IMPORT_ALL_TICKETS', Validators.required],
			IMPORT_TICKETS_FROM_DATE: d,
			IMPORT_TICKET_ATTACHMENTS: [true, Validators.required],
			// MIGRATE_KNOWLEDGE_BASE: [false, Validators.required]
		});
	}

	public connectToZendesk() {
		if (this.migrationForm.valid) {
			const zendeskUserObj = JSON.parse(
				JSON.stringify(this.migrationForm.value)
			);
			if (
				this.settingsForm.get('IMPORT_TICKET_OPTION').value ===
					'IMPORT_TICKETS_FROM_DATE' &&
				this.settingsForm.get('IMPORT_TICKETS').value
			) {
				console.log('entered');
				zendeskUserObj['IMPORT_TICKETS_FROM'] =
					JSON.stringify(
						new Date(this.settingsForm.get('IMPORT_TICKETS_FROM_DATE').value)
					)
						.split('.')[0]
						.substring(1) + 'Z';
			}

			this.importService
				.postImportZendesk(
					zendeskUserObj,
					this.settingsForm.value.IMPORT_TICKETS,
					this.settingsForm.value.IMPORT_TICKET_ATTACHMENTS
				)
				.subscribe(
					(importSuccess: any) => {
						this.bannerMessageService.successNotifications.push({
							message: 'Imported Successfully',
						});
					},
					(importError: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: importError.error.ERROR,
						});
					}
				);
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('ZENDESK_FORM_INVALID'),
			});
		}
	}

	public saveSettings(option?: string) {
		this.connectToZendesk();
	}
}
