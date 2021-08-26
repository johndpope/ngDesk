import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';

import { Router } from '@angular/router';
import { ChannelsService } from '../../channels/channels.service';
import { CompaniesService } from '../../companies/companies.service';
import { ModulesService } from '../../modules/modules.service';

@Component({
	selector: 'app-spf-record-dialog',
	templateUrl: './spf-record-dialog.component.html',
	styleUrls: ['./spf-record-dialog.component.scss']
})
export class SpfRecordDialogComponent implements OnInit {
	public spfRecordForm: FormGroup;
	public errorMessage = '';
	public spferrorMessage: String;
	public spfErrorMessage = false;
	public spfErrorMessageForEmptyString = false;
	public showLoader = false;
	public errorParams = {
		domain: {}
	};
	public modules = [];
	public emailChannels = [];
	public noVerifiedEmailChannels = false;
	private existingSpfRecords = [];

	constructor(
		public dialogRef: MatDialogRef<SpfRecordDialogComponent>,
		private formBuilder: FormBuilder,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private translateService: TranslateService,
		private companiesService: CompaniesService,
		private modulesService: ModulesService,
		private router: Router,
		private channelsService: ChannelsService
	) {
		// get translations for dynamic error messages
		this.translateService.get('DOMAIN').subscribe((value: string) => {
			this.errorParams.domain = { field: value };
		});

		// get a list of all the modules and respective ids
		this.modulesService.getAllModules().subscribe(
			(modulesResponse: any) => {
				this.modules = modulesResponse.MODULES;
			},
			(modulesError: any) => {
				this.errorMessage = modulesError.error.ERROR;
			}
		);

		// get all existing spf records
		// used to cross reference the email channels
		this.companiesService.getSpfRecords().subscribe(
			(spfResponse: any) => {
				this.existingSpfRecords = spfResponse.SPF_RECORDS;
			},
			(spfError: any) => {
				this.errorMessage = spfError.error.ERROR;
			}
		);
	}

	public ngOnInit() {
		// initalizes an empty form
		this.spfRecordForm = this.formBuilder.group({
			DOMAIN: ['', [Validators.required]]
		});

		// loads existing spf record if not new
		// email address dropdown stays disabled until loaded
		if (this.data.spfId !== 'new') {
			this.spfRecordForm.controls.DOMAIN.disable();
			this.showLoader = true;
			this.companiesService.getSpfRecordById(this.data.spfId).subscribe(
				(spfResponse: any) => {
					// TODO: replace this logic with spf records returning module id
					this.spfRecordForm.controls.DOMAIN.enable();
					this.spfRecordForm.controls.DOMAIN.setValue(spfResponse.DOMAIN);
					this.showLoader = false;
				},
				(spfError: any) => {
					this.errorMessage = spfError.error.ERROR;
					this.showLoader = false;
				}
			);
		}
	}

	// attempts to save spf record
	// returns any errors on save
	// shows and stops loader icon
	// will close the dialog if successful

	public saveSpf() {
		if (this.spfRecordForm.valid) {
			this.showLoader = true;
			if (this.data.spfId === 'new') {
				this.companiesService
					.postSpfRecord(this.spfRecordForm.value.DOMAIN)
					.subscribe(
						(spfSuccess: any) => {
							this.showLoader = false;
							this.dialogRef.close();
						},
						(spfError: any) => {
							this.showLoader = false;
							//this.errorMessage = spfError.error.ERROR;
							this.spferrorMessage = spfError.error.ERROR;
							if (spfError.error.ERROR === '') {
								this.spfErrorMessage = false;
								this.spfErrorMessageForEmptyString = true;
							} else if (spfError.error.ERROR !== '') {
								this.spfErrorMessageForEmptyString = false;
								this.spfErrorMessage = true;
							}
						}
					);
			} else {
				this.companiesService
					.putSpfRecord(this.data.spfId, this.spfRecordForm.value.DOMAIN)
					.subscribe(
						(spfSuccess: any) => {
							this.showLoader = false;
							this.dialogRef.close();
						},
						(spfError: any) => {
							this.showLoader = false;
							this.errorMessage = spfError.error.ERROR;
						}
					);
			}
		}
	}

	// action for when a module is selected
	// queries all the email channels for selected module
	// filters only external and verified channels
	public moduleSelected(event) {
		let moduleId = event.value;
		if (!event.value) {
			moduleId = event;
		}
		this.errorMessage = '';
		this.emailChannels = [];
		// this.channelsService.getEmailChannelsByModule(moduleId).subscribe(
		// 	(channelsResponse: any) => {
		// 		this.emailChannels = channelsResponse.CHANNELS.filter(
		// 			email => email.TYPE === 'External' && email.IS_VERIFIED
		// 		);
		// 		if (this.emailChannels.length > 0) {
		// 			this.spfRecordForm.controls.DOMAIN.enable();
		// 		} else {
		// 			this.spfRecordForm.controls.DOMAIN.disable();
		// 			this.translateService
		// 				.get('NO_VERIFIED_EXTERNAL_EMAIL_CHANNEL_FOUND')
		// 				.subscribe((value: string) => {
		// 					this.errorMessage = value;
		// 				});
		// 		}
		// 		this.showLoader = false;
		// 	},
		// 	(channelsError: any) => {
		// 		this.errorMessage = channelsError.error.ERROR;
		// 		this.showLoader = false;
		// 	}
		// );
	}

	// checks if spf record exists for email channels
	// public isSpfExisting(emailChannel): boolean {
	// 	return this.existingSpfRecords.find(
	// 		record => record['DOMAIN'] === emailChannel['DOMAIN']
	// 	)
	// 		? true
	// 		: false;
	// }
}
