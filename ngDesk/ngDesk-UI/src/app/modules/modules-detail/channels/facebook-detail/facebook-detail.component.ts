import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { MatDialog } from '@angular/material/dialog';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ChannelsService } from 'src/app/channels/channels.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CompaniesService } from '../../../../companies/companies.service';

import { AppGlobals } from 'src/app/app.globals';
import { FacebookChannelDialogComponent } from 'src/app/dialogs/facebook-channel-dialog/facebook-channel-dialog.component';
import { UsersService } from 'src/app/users/users.service';

@Component({
	selector: 'app-facebook-detail',
	templateUrl: './facebook-detail.component.html',
	styleUrls: ['./facebook-detail.component.scss']
})
export class FacebookDetailComponent implements OnInit {
	public facebookChannel: any = {};
	public facebookForm: FormGroup;
	private uuid = this.usersService.getAuthenticationToken();
	private verificationTimer: any;
	public pages: any;
	private moduleId: any;
	private dialogRef;

	constructor(
		public channelService: ChannelsService,
		public formBuilder: FormBuilder,
		public route: ActivatedRoute,
		private matIconRegistry: MatIconRegistry,
		public bannerService: BannerMessageService,
		private domSanitizer: DomSanitizer,
		private global: AppGlobals,
		private usersService: UsersService,
		private translateService: TranslateService,
		private dialog: MatDialog,
		private companiesService: CompaniesService
	) {
		this.matIconRegistry.addSvgIcon(
			'facebook_logo',
			this.domSanitizer.bypassSecurityTrustResourceUrl(
				'../../../../../assets/images/facebook_logo.svg'
			)
		);
	}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.facebookForm = this.formBuilder.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: ['']
		});
		this.channelService.getFacebookChannel(this.moduleId).subscribe(
			(response: any) => {
				this.facebookChannel = response;
				this.facebookForm.setValue({
					NAME: response.NAME,
					DESCRIPTION: response.DESCRIPTION
				});
			},
			error => {
				this.bannerService.errorNotifications.push({
					message: error.error.ERROR
				});
			}
		);
	}
	public connectToFacebook() {
		this.dialogRef = this.dialog.open(FacebookChannelDialogComponent, {
			width: '600px',
			disableClose: true,
			autoFocus: true,
			data: {
				facebookChannelId: this.facebookChannel.CHANNEL_ID,
				active: 'paneTwo'
			}
		});
		let verificationCounter = 0;
		const winFacebook = window.open(
			`${this.global.baseRestUrl}/facebook/create_facebook_authorization?channel_id=${this.facebookChannel.CHANNEL_ID}&authentication_token=${this.uuid}`,
			'_blank'
		);

		// reset timer
		clearInterval(this.verificationTimer);
		// timer is set for function to be called every 5 seconds
		// will check if facebook returns facebook channel verification status of true or false
		this.verificationTimer = setInterval(() => {
			// will terminate repeating timer after 1 minute of verification is done
			// and facebook channel is still not verified
			if (++verificationCounter === 120) {
				clearInterval(this.verificationTimer);
			}
			// api checks verification
			this.channelService
				.getFacebookPages(
					this.facebookChannel.CHANNEL_ID,
					this.facebookChannel.MODULE
				)
				.subscribe(
					(facebookChannelResponse: any) => {
						if (
							facebookChannelResponse.hasOwnProperty('PAGES') &&
							facebookChannelResponse.PAGES.length > 0
						) {
							this.dialogRef.close();
							clearInterval(this.verificationTimer);
							winFacebook.close();
							facebookChannelResponse.PAGES.forEach(element => {
								element['IS_ACTIVE'] = true;
							});
							this.pages = facebookChannelResponse;
							this.dialogRef = this.dialog.open(
								FacebookChannelDialogComponent,
								{
									width: '600px',
									disableClose: true,
									autoFocus: true,
									data: {
										facebookChannelId: this.facebookChannel.CHANNEL_ID,
										facebookModuleId: this.facebookChannel.MODULE,
										active: 'paneThree',
										pagesInfo: facebookChannelResponse
									}
								}
							);
							this.dialog.afterAllClosed.subscribe(value => {
								this.channelService.getFacebookChannel(this.moduleId).subscribe(
									(response: any) => {
										this.facebookChannel = response;
										this.facebookForm.setValue({
											NAME: response.NAME,
											DESCRIPTION: response.DESCRIPTION
										});
									},
									error => {
										this.bannerService.errorNotifications.push({
											message: error.error.ERROR
										});
									}
								);
							});
						} else {
							clearInterval(this.verificationTimer);
							winFacebook.close();
							// TO HANDLE ACCESS TOKEN CHANGE
							this.save('');
							this.dialogRef.close();
							// this.bannerService.errorNotifications.push({
							//   message: this.translateService.instant('NO_PAGES_SELECTED')
							// });
						}
					},
					(error: any) => {}
				);
		}, 5000);
	}

	public save(type) {
		this.channelService
			.putFacebookChannel(this.moduleId, this.facebookChannel)
			.subscribe(
				value => {
					this.companiesService.trackEvent(`Updated Channel`, {
						CHANNEL_NAME: this.facebookChannel.CHANNEL_ID,
						MODULE_ID: this.moduleId
					});
					this.pages = undefined;
					this.facebookChannel = value;
					if (type === 'form') {
						this.bannerService.successNotifications.push({
							message: this.translateService.instant('SAVED_SUCCESSFULLY')
						});
					}
				},
				error => {
					this.bannerService.errorNotifications.push({
						message: error.error.ERROR
					});
				}
			);
	}

	public unlinkPage(pageId) {
		this.channelService.unlinkFacebookPage(this.moduleId, pageId).subscribe(
			value => {
				this.facebookChannel = value;
				this.bannerService.successNotifications.push({
					message: this.translateService.instant('UPDATED_SUCCESSFULLY')
				});
			},
			error => {
				this.bannerService.errorNotifications.push({
					message: error.error.ERROR
				});
			}
		);
	}

	public getPageCount(state) {
		let count = 0;
		for (const page of this.facebookChannel.PAGES) {
			if (page.SUBSCRIBED === state) {
				count++;
			}
		}
		return count;
	}
}
