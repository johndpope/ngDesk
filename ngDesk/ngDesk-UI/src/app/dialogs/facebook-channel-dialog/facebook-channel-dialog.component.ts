import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { Router } from '@angular/router';

import { ChannelsService } from '../../channels/channels.service';
import { FacebookChannel } from '../../models/facebook-channel';

@Component({
	selector: 'app-facebook-channel-dialog',
	templateUrl: './facebook-channel-dialog.component.html',
	styleUrls: ['./facebook-channel-dialog.component.scss'],
})
export class FacebookChannelDialogComponent implements OnInit {
	public facebookChannelForm: FormGroup;
	private facebookChannel: FacebookChannel = new FacebookChannel('', '', '');
	public pages: any;
	public active = 'paneOne'; // default set to project email channel form
	public verificationCounter = 0;
	public errorMessage = '';
	public entry: any;
	public facebookUserName = {
		FIRST_NAME: '',
		LAST_NAME: '',
	};

	public errorParams = {
		name: {},
		description: {},
	};

	constructor(
		private dialogRef: MatDialogRef<FacebookChannelDialogComponent>,
		private formBuilder: FormBuilder,
		private channelsService: ChannelsService,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private matIconRegistry: MatIconRegistry,
		private domSanitizer: DomSanitizer,
		private router: Router
	) {
		this.matIconRegistry.addSvgIcon(
			'facebook_logo',
			this.domSanitizer.bypassSecurityTrustResourceUrl(
				'../../../assets/images/facebook_logo.svg'
			)
		);
	}

	public ngOnInit() {
		this.facebookChannelForm = this.formBuilder.group({
			name: ['', [Validators.required]],
			description: [''],
		});

		if (this.data.facebookChannelId !== 'new') {
			this.active = this.data.active;
			if (this.data.active === 'paneThree') {
				this.facebookUserName.FIRST_NAME = this.data.pagesInfo.FIRST_NAME;
				this.facebookUserName.LAST_NAME = this.data.pagesInfo.LAST_NAME;
				this.channelsService
					.getFacebookChannel(this.data.facebookModuleId)
					.subscribe(
						(response: any) => {
							this.pages = this.data.pagesInfo.PAGES;
							this.entry = response;
						},
						(error) => {
							this.errorMessage = error.error.ERROR;
						}
					);
			}
		}
	}

	public save() {
		if (this.facebookChannelForm.valid) {
			// updating values of request body with values from form
			this.facebookChannel.name = this.facebookChannelForm.value.name;
			this.facebookChannel.description = this.facebookChannelForm.value.description;
			this.facebookChannel.module = this.data.facebookModuleId;
			this.channelsService
				.postFacebookChannel(this.facebookChannel.module, this.facebookChannel)
				.subscribe(
					(facebookSuccessResponse: any) => {
						this.dialogRef.close();
						this.router.navigate([
							`modules/${this.data.facebookModuleId}/channels/facebook/facebook-detail`,
						]);
					},
					(facebookError: any) => {
						this.errorMessage = facebookError.error.ERROR;
					}
				);
		}
	}

	public savePages() {
		this.pages.forEach((element) => {
			if (element.IS_ACTIVE) {
				this.entry.PAGES.push(element);
			}
		});
		this.channelsService
			.putFacebookChannel(this.entry.MODULE, this.entry)
			.subscribe(
				(value) => {
					this.dialogRef.close();
					this.data.entry = value;
					this.pages = undefined;
				},
				(error) => {
					this.errorMessage = error.error.ERROR;
				}
			);
	}
	public cancel() {
		this.dialogRef.close();
	}
}
