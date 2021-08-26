import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SMSChannelDialogComponent } from 'src/app/dialogs/sms-channel-dialog/sms-channel-dialog.component';
import { SMSChannelDisplayDialogComponent } from 'src/app/dialogs/sms-channel-display-dialog/sms-channel-display-dialog.component';
import { TwilioRequestComponent } from 'src/app/dialogs/twilio-request/twilio-request.component';
import { ChannelsService } from '../../../../channels/channels.service';
import { CompaniesService } from '../../../../companies/companies.service';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../../dialogs/confirm-dialog/confirm-dialog.component';
import { EmailChannelDialogComponent } from '../../../../dialogs/email-channel-dialog/email-channel-dialog.component';
import { FacebookChannelDialogComponent } from '../../../../dialogs/facebook-channel-dialog/facebook-channel-dialog.component';
import { ModulesService } from '../../../modules.service';

@Component({
	selector: 'app-channels-list',
	templateUrl: './channels-list.component.html',
	styleUrls: ['./channels-list.component.scss'],
})
export class ChannelsListComponent implements OnInit {
	public dialogRef: any;
	public title = '';
	public channelActions = {
		actions: [{ NAME: '', ICON: 'delete' }],
	};
	public channelType: string;
	public totalRecords: any;
	public isTwilioRequested = false;
	public isLoading = true;
	public showSideNav = true;
	public navigations = [];
	public moduleId: string;
	public moduleName;
	public twilioCountries = [];
	public ticketsId;
	public notMapped = new Map();

	constructor(
		private moduleService: ModulesService,
		private router: Router,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private customTableService: CustomTableService,
		private channelsService: ChannelsService,
		private route: ActivatedRoute,
		private dialog: MatDialog,
		private confirmationDialog: MatDialog,
		private companiesService: CompaniesService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.channelActions with the name of the translated word
			this.channelActions[value] = (row) => {
				this.deleteChannel(row);
			};
			this.channelActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.moduleService.getModuleByName('Tickets').subscribe((response: any) => {
			this.ticketsId = response.MODULE_ID;
		});
		this.customTableService.isLoading = true;
		this.moduleService
			.getModuleById(this.moduleId)
			.subscribe((response: any) => {
				this.moduleName = response.NAME;
			});
		this.channelType = this.route.snapshot.params['channelType'];
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.sortBy = 'NAME';
		this.customTableService.sortOrder = 'asc';
		this.navigations = [
			{
				NAME: 'MODULE_DETAIL',
				PATH: ['', 'modules', this.moduleId],
			},
			{
				NAME: 'FIELDS',
				PATH: ['', 'modules', this.moduleId, 'fields'],
			},
			{
				NAME: 'LAYOUTS',
				PATH: ['', 'modules', this.moduleId, 'layouts'],
			},
			{
				NAME: 'VALIDATIONS',
				PATH: ['', 'modules', this.moduleId, 'validations'],
			},
			{
				NAME: 'WORKFLOWS',
				PATH: ['', 'modules', this.moduleId, 'workflows'],
			},
			{
				NAME: 'SLAS',
				PATH: ['', 'modules', this.moduleId, 'slas'],
			},
			{
				NAME: 'CHANNELS',
				PATH: ['', 'modules', this.moduleId, 'channels'],
			},
			{
				NAME: 'FORMS',
				PATH: ['', 'modules', this.moduleId, 'forms'],
			},
			{
				NAME: 'PDFs',
				PATH: ['', 'modules', this.moduleId, 'pdf'],
			},
			{
				NAME: 'TASK',
				PATH: ['', 'modules', this.moduleId, 'task'],
			},
		];
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		if (this.channelType === 'sms') {
			columnsHeadersObj.push(
				{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
				{
					DISPLAY: this.translateService.instant('IS_VERIFIED'),
					NAME: 'IS_VERIFIED',
				},
				{
					DISPLAY: this.translateService.instant('PHONE_NUMBER'),
					NAME: 'PHONE_NUMBER',
				},
				{ DISPLAY: this.translateService.instant('ACTION'), NAME: 'ACTION' }
			);
			columnsHeaders.push(
				this.translateService.instant('NAME'),
				this.translateService.instant('IS_VERIFIED'),
				this.translateService.instant('PHONE_NUMBER'),
				this.translateService.instant('ACTION')
			);
		} else {
			columnsHeadersObj.push(
				{ DISPLAY: this.translateService.instant('NAME'), NAME: 'NAME' },
				{
					DISPLAY: this.translateService.instant('IS_VERIFIED'),
					NAME: 'IS_VERIFIED',
				},
				{ DISPLAY: this.translateService.instant('ACTION'), NAME: 'ACTION' }
			);
			columnsHeaders.push(
				this.translateService.instant('NAME'),
				this.translateService.instant('IS_VERIFIED'),
				this.translateService.instant('ACTION')
			);
		}

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.getChannelList();
		this.title = this.channelType.toUpperCase();
		if (this.channelType === 'sms') {
			this.channelsService.getTwilioSupportedCountries(this.moduleId).subscribe(
				(data: any) => {
					this.twilioCountries = data.sort((a, b) => {
						if (a.COUNTRY_NAME > b.COUNTRY_NAME) {
							return 1;
						}
						if (a.COUNTRY_NAME < b.COUNTRY_NAME) {
							return -1;
						}
						return 0;
					});
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		}
	}

	public deleteChannel(channel) {
		let deleteMessage: string;
		if (this.channelType === 'email') {
			deleteMessage = 'DELETE_EMAIL_CHANNEL_CONFIRM';
		} else if (this.channelType === 'facebook') {
			deleteMessage = 'DELETE_FACEBOOK_CHANNEL_CONFIRM';
		} else {
			deleteMessage = 'DELETE_SMS_CHANNEL_CONFIRM';
		}
		this.translateService
			.get(deleteMessage, { channelName: channel.NAME })
			.subscribe((res) => {
				this.dialogRef = this.confirmationDialog.open(ConfirmDialogComponent, {
					data: {
						message: res,
						buttonText: this.translateService.instant('DELETE'),
						closeDialog: this.translateService.instant('CANCEL'),
						action: this.translateService.instant('DELETE'),
						executebuttonColor: 'warn',
					},
				});
			});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		this.dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DELETE')) {
				this.channelsService
					.deleteChannel(this.moduleId, this.channelType, channel.CHANNEL_ID)
					.subscribe(
						(channelResponse: any) => {
							this.companiesService.trackEvent(`Deleted Channel`, {
								CHANNEL_ID: channel.CHANNEL_ID,
								MODULE_ID: this.moduleId,
							});
							this.getChannelList();
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			}
		});
	}

	public rowClicked(rowData): void {
		if (this.channelType === 'email' && this.moduleId === this.ticketsId) {
			this.dialogRef = this.dialog.open(EmailChannelDialogComponent, {
				width: '600px',
				disableClose: true,
				autoFocus: true,
				data: { emailId: rowData.NAME, emailChannelModuleId: this.moduleId },
			});
		} else if (
			this.channelType === 'email' &&
			this.moduleId !== this.ticketsId
		) {
			this.router.navigate([
				`modules/${this.moduleId}/channels/email/${rowData.NAME}`,
			]);
		} else if (this.channelType === 'facebook') {
			this.dialogRef = this.dialog.open(FacebookChannelDialogComponent, {
				width: '600px',
				disableClose: true,
				autoFocus: true,
				data: {
					facebookChannelId: rowData.CHANNEL_ID,
					facebookModuleId: this.moduleId,
				},
			});
		} else if (this.channelType === 'sms' && !rowData.VERIFIED) {
			this.dialogRef = this.dialog.open(SMSChannelDialogComponent, {
				width: '600px',
				disableClose: true,
				autoFocus: true,
				data: {
					smsChannelId: rowData.CHANNEL_ID,
					smsModuleId: this.moduleId,
				},
			});
		} else if (this.channelType === 'sms' && rowData.VERIFIED) {
			this.dialogRef = this.dialog.open(SMSChannelDisplayDialogComponent, {
				width: '600px',
				disableClose: true,
				autoFocus: true,
				data: {
					smsChannelId: rowData.CHANNEL_ID,
					smsModuleId: this.moduleId,
				},
			});
		}
		// only if dialog is open
		if (this.dialogRef) {
			this.dialogRef.afterClosed().subscribe((result) => {
				if (result === undefined) {
					let saveMessage: string;
					if (this.channelType === 'sms') {
						saveMessage = 'SMS_CHANNEL_SAVE_SUCCESS';
					}
					this.translateService.get(saveMessage).subscribe((value: string) => {
						this.bannerMessageService.successNotifications.push({
							message: value,
						});
					});
				}
				this.getChannelList();
			});
		}
	}

	public newChannel(selectedCountry?): void {
		if (this.channelType === 'email' && this.moduleId === this.ticketsId) {
			this.dialogRef = this.dialog.open(EmailChannelDialogComponent, {
				width: '600px',
				disableClose: true,
				autoFocus: true,
				data: { emailId: 'new', emailChannelModuleId: this.moduleId },
			});
		} else if (
			this.channelType === 'email' &&
			this.moduleId !== this.ticketsId
		) {
			this.router.navigate([`modules/${this.moduleId}/channels/email/new`]);
		} else if (this.channelType === 'facebook') {
			this.dialogRef = this.dialog.open(FacebookChannelDialogComponent, {
				width: '600px',
				disableClose: true,
				autoFocus: true,
				data: { facebookChannelId: 'new', facebookModuleId: this.moduleId },
			});
		} else if (this.channelType === 'sms') {
			if (
				this.channelsService.twilioSupportedCountries
					.map((d) => d.COUNTRY_NAME)
					.includes(selectedCountry.COUNTRY_NAME)
			) {
				this.dialogRef = this.dialog.open(TwilioRequestComponent, {
					width: '600px',
					disableClose: true,
					autoFocus: true,
					data: {
						smsChannelId: 'new',
						smsModuleId: this.moduleId,
						selectedCountry: selectedCountry,
					},
				});
			} else {
				this.dialogRef = this.dialog.open(SMSChannelDialogComponent, {
					width: '600px',
					disableClose: true,
					autoFocus: true,
					data: {
						smsChannelId: 'new',
						smsModuleId: this.moduleId,
						selectedCountry: selectedCountry,
					},
				});
			}
		}
		if (!(this.channelType === 'email')) {
			this.dialogRef.afterClosed().subscribe((result) => {
				if (result === undefined) {
					let saveMessage: string;
					if (this.channelType === 'email') {
						// saveMessage = 'EMAIL_CHANNEL_SAVE_SUCCESS';
					} else if (this.channelType === 'facebook') {
						saveMessage = 'FACEBOOK_CHANNEL_SAVE_SUCCESS';
					} else if (
						this.channelType === 'sms' &&
						this.channelsService.twilioSupportedCountries
							.map((d) => d.COUNTRY_NAME)
							.includes(selectedCountry.COUNTRY_NAME)
					) {
						saveMessage = 'TWILIO_REQUEST_SUCCESS';
					} else {
						saveMessage = 'SMS_CHANNEL_SAVE_SUCCESS';
					}
					this.translateService.get(saveMessage).subscribe((value: string) => {
						this.bannerMessageService.successNotifications.push({
							message: value,
						});
					});
					this.getChannelList();
				}
			});
		} else {
			if (this.dialogRef === undefined) {
				this.getChannelList();
			} else {
				this.dialogRef.afterClosed().subscribe((result) => {
					this.getChannelList();
				});
			}
		}
	}

	public sortData(event) {
		this.getChannelList();
	}
	public getChannelList() {
		this.isTwilioRequested = false;
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;

		// if (searchString && searchString !== '' && searchString !== null) {
		this.channelsService
			.getChannels(
				this.moduleId,
				this.channelType,
				pageSize,
				page,
				sortBy,
				orderBy
			)
			.subscribe(
				(data: any) => {
					data.CHANNELS.forEach((channel) => {
						if (this.channelType === 'facebook' || this.channelType === 'sms') {
							if (channel.VERIFIED) {
								channel.IS_VERIFIED = 'Yes';
							} else {
								channel.IS_VERIFIED = 'No';
							}
						}
						if (this.channelType === 'sms' && channel.PHONE_NUMBER === '') {
							channel.PHONE_NUMBER = 'Click to Buy';
						} else {
							if (channel.IS_VERIFIED) {
								channel.IS_VERIFIED = 'Yes';
							} else {
								channel.IS_VERIFIED = 'No';
							}
						}
					});
					if (this.channelType === 'sms') {
						this.isLoading = false;
						if (
							data.CHANNELS.filter((channel) => !channel.VERIFIED).length > 0
						) {
							this.isTwilioRequested = true;
						}
						data.CHANNELS = data.CHANNELS.filter((channel) => channel.VERIFIED);
						data.TOTAL_RECORDS = data.CHANNELS.length;
					}
					this.customTableService.setTableDataSource(
						data.CHANNELS,
						data.TOTAL_RECORDS
					);
					this.totalRecords = data.TOTAL_RECORDS;
					const channelsList = data.CHANNELS;
					if (this.ticketsId === undefined) {
						this.moduleService
							.getModuleByName('Tickets')
							.subscribe((response: any) => {
								this.ticketsId = response.MODULE_ID;
								if (this.ticketsId !== this.moduleId) {
									for (let i = 0; i < data.TOTAL_RECORDS; i++) {
										if (
											!data.CHANNELS[i].hasOwnProperty('CREATE_MAPPING') ||
											data.CHANNELS[i].CREATE_MAPPING === null
										) {
											this.notMapped.set(data.CHANNELS[i].NAME, true);
										} else if (
											data.CHANNELS[i].CREATE_MAPPING.SUBJECT === null
										) {
											this.notMapped.set(data.CHANNELS[i].NAME, true);
										} else {
											this.notMapped.set(data.CHANNELS[i].NAME, false);
										}
									}
									this.isLoading = false;
								} else if (this.ticketsId === this.moduleId) {
									this.isLoading = false;
								}
							});
					} else if (this.ticketsId === this.moduleId) {
						this.isLoading = false;
					} else {
						if (this.ticketsId !== this.moduleId) {
							for (let i = 0; i < data.TOTAL_RECORDS; i++) {
								if (
									!data.CHANNELS[i].hasOwnProperty('CREATE_MAPPING') ||
									data.CHANNELS[i].CREATE_MAPPING === null
								) {
									this.notMapped.set(data.CHANNELS[i].NAME, true);
								} else if (data.CHANNELS[i].CREATE_MAPPING.SUBJECT === null) {
									this.notMapped.set(data.CHANNELS[i].NAME, true);
								} else {
									this.notMapped.set(data.CHANNELS[i].NAME, false);
								}
							}
							this.isLoading = false;
						}
					}
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		// }
	}

	// private convertSearchString(searchParams?: any[]): string | null {
	//   let searchString = '';
	//   // reloading table based on params from search
	//   if (searchParams && searchParams.length > 0) {
	//     // build search string for either global search or field search
	//     searchParams.forEach((param, index) => {
	//       if (param['TYPE'] === 'field' && searchParams[index + 1]) {
	//         const field = param['NAME'];
	//         const value = searchParams[index + 1]['VALUE'];
	//         if (searchString === '') {
	//           searchString = `${field}=${value}`;
	//         } else {
	//           searchString += `~~${field}=${value}`;
	//         }
	//       } else if (param['TYPE'] === 'global') {
	//         searchString = param['VALUE'];
	//       }
	//     });
	//     // make get entries call with search param
	//     if (searchString !== '') {
	//       return searchString;
	//     } else {
	//       return null;
	//     }
	//   } else {
	//     // when all params are cleared, return all entries
	//     return null;
	//   }
	// }
}
