import {
	Component,
	Input,
	OnDestroy,
	OnInit,
	TemplateRef,
	ViewChild,
	ViewEncapsulation,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { Subject, Subscription } from 'rxjs';
import { CacheService } from '../cache.service';
import { CompaniesService } from '../companies/companies.service';
import { BannerMessageService } from '../custom-components/banner-message/banner-message.service';
import { SignupQuestionsDialogComponent } from '../dialogs/signup-questions-dialog/signup-questions-dialog.component';
import { MessagingService } from '../firebase/messaging.service';
import { ModulesService } from '../modules/modules.service';
import { NotificationsService } from '../notifications/notifications.service';
import { RolesService } from '../roles/roles.service';
import { UsersService } from '../users/users.service';
import { WebsocketService } from '../websocket.service';
import { NotificationApiService } from '@ngdesk/notification-api';
import { MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';

import {
	debounceTime,
	distinctUntilChanged,
	map,
	switchMap,
} from 'rxjs/operators';
import { ToolbarService } from './toolbar.service';

@Component({
	selector: 'app-toolbar',
	templateUrl: './toolbar.component.html',
	styleUrls: ['./toolbar.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class ToolbarComponent implements OnDestroy, OnInit {
	public receivedMessages: string[] = [];
	public sub;
	public subStep4;
	public checkGettingStarted;
	public stepFour = false;
	public statusColor: string;
	public statuses: any = [
		{
			NAME: 'ONLINE',
			COLOR: '#7ee67e',
		},
		{
			NAME: 'AWAY',
			COLOR: '#efef01',
		},
		{
			NAME: 'BUSY',
			COLOR: '#d80808',
		},
		{
			NAME: 'OFFLINE',
			COLOR: '#a2a0a0',
		},
	];
	@Input() public templateRef: TemplateRef<any>;
	public authToken: string;
	public enableSignup: boolean;
	public logo;
	private notificationSub: Subscription;
	private onScrollSubscribe: Subscription;
	public isAcceptingChats;
	public value = 0;
	public loaded = false;
	public accept: boolean;
	public roleName: string;
	public displayGettingStarted = false;
	public moduleId;
	public showGettingStarted = false;
	public gettingStarted = false;
	public gettingStartedStatus = false;
	public complete = [];
	public companyDataUpTodate: Subscription;
	public profilePicURL: any = '';
	public unreadNotifications = [];
	public unreadNotificationsLength;
	public notificationScrollSubject = new Subject<any>();
	public notificationLength;
	public filteredUnreadNotifications = [];
	public pageNumber = 0;
	public panelWidth = '300px';
	@ViewChild(MatAutocompleteTrigger, { read: MatAutocompleteTrigger })
	inputAutoComplete: MatAutocompleteTrigger;

	constructor(
		private router: Router,
		public usersService: UsersService,
		private cookieService: CookieService,
		private companiesService: CompaniesService,
		private userService: UsersService,
		public notificationsService: NotificationsService,
		private rolesService: RolesService,
		private bannerMessageService: BannerMessageService,
		private messagingService: MessagingService,
		private dialog: MatDialog,
		private websocketService: WebsocketService,
		private cacheService: CacheService,
		private modulesService: ModulesService,
		public notificationApiService: NotificationApiService,
		private http: HttpClient,
		private globals: AppGlobals,
		private toolbarService: ToolbarService
	) {
		this.authToken = this.usersService.getAuthenticationToken();
	}

	public ngOnInit() {
		this.statusColor = '#a2a0a0';
		this.checkGettingStarted = this.router.url.search('getting-started');
		this.companyDataUpTodate = this.cacheService.companyInfoSubject.subscribe(
			(dataStored) => {
				console.log('loading data');

				if (dataStored) {
					this.logo =
						this.cacheService.companyData['COMPANY_THEME']['SIDEBAR']['FILE'];
					const ticketsModule = this.cacheService.companyData['MODULES'].find(
						(module) => module['NAME'] === 'Tickets'
					);
					// const chatModule = this.cacheService.companyData['MODULES'].find(
					// 	(module) => module['NAME'] === 'Chats'
					// );

					if (!this.authToken) {
						this.enableSignup =
							this.cacheService.companyData['COMPANY_ENROLLMENT'][
								'ENABLE_SIGNUPS'
							];
					} else {
						this.toolbarService.updateShowAcceptChat();
						// if loggedin subscribe to notifications
						this.notificationSubscription();
						// this.getNotifications();
						// this.getAcceptingChats();
						this.initializeNotificationScrollSubject();
						this.reloadNotificationOnPublish();
					}
					if (this.authToken) {
						this.getModuleDetails();
						if (this.rolesService.role.NAME === 'SystemAdmin') {
							this.showGettingStarted = true;
							if (document.getElementById('chat-widget') !== null) {
								document.getElementById('chat-widget').style.display = 'block';
							}
							let questionCount =
								this.cacheService.companyData['COMPANY_QUESTION_COUNT'].COUNT;
							if (questionCount < 4) {
								this.companiesService.setAdminSignup(false);
								this.openQuestionsDialog(questionCount);
							} else {
								this.companiesService.setAdminSignup(true);
							}
						}
						if (this.rolesService.role.NAME !== 'Customers') {
							// FIREBASE REQUEST AND RECEIVE
							this.messagingService.requestPermission(
								this.userService.user.EMAIL_ADDRESS
							);
							this.messagingService.receiveMessage();
						}

						if (!this.cacheService.companyData['GETTING_STARTED']) {
							// if (
							// 	(this.cacheService.companyData['USAGE_TYPE'].TICKETS ||
							// 		this.cacheService.companyData['USAGE_TYPE'].PAGER) &&
							// 	!this.cacheService.companyData['USAGE_TYPE'].CHAT
							// ) {
							this.moduleId = ticketsModule.MODULE_ID;
							this.displayGettingStarted = true;
							let i = 0;
							this.cacheService.companyData['ALL_GETTING_STARTED'].forEach(
								(element) => {
									this.complete[i] = element.COMPLETED;
									i++;
								}
							);

							this.calculateProgress();
							this.publishProgress();
							this.publishEmailStatus();
							this.publishProgressChatIntegration();
							// } else {
							// this.moduleId = chatModule.MODULE_ID;
							// this.displayGettingStarted = true;
							// let i = 0;
							// this.cacheService.companyData['ALL_GETTING_STARTED'].forEach(
							// 	(element) => {
							// 		this.complete[i] = element.COMPLETED;
							// 		i++;
							// 	}
							// );
							// this.calculateProgress();
							// this.publishProgress();
							// this.publishProgressChatIntegration();
							// }
						} else {
							this.gettingStarted = true;
						}
					}
				}
			}
		);
		// });
	}

	public ngOnDestroy() {
		if (this.subStep4) {
			this.subStep4.unsubscribe();
		}
		if (this.sub) {
			this.sub.unsubscribe();
		}
		if (this.onScrollSubscribe) {
			this.onScrollSubscribe.unsubscribe();
		}
		if (this.notificationSub) {
			this.notificationSub.unsubscribe();
		}
		if (this.companyDataUpTodate) {
			this.companyDataUpTodate.unsubscribe();
		}
	}

	public viewProfile() {
		if (this.cacheService.companyData['MODULES']) {
			const userModule = this.cacheService.companyData['MODULES'].find(
				(module) => module['NAME'] === 'Users'
			);
			this.router.navigate([
				`render/${userModule.MODULE_ID}/edit/${this.usersService.user.DATA_ID}`,
			]);
		}
	}

	private openQuestionsDialog(count) {
		if (!this.dialog.openDialogs || !this.dialog.openDialogs.length) {
			const dialogRef = this.dialog.open(SignupQuestionsDialogComponent, {
				disableClose: true,
				width: '600px',
				height: '340px',
				data: {
					questionCount: count,
					action: 'close',
				},
			});

			// EVENT AFTER MODAL DIALOG IS CLOSED
			dialogRef.afterClosed().subscribe((result) => {
				this.companiesService.setAdminSignup(true);
				this.dialog.closeAll();
				if (this.cacheService.companyData['COMPANY_QUESTION_COUNT'].COUNT > 3) {
					this.navigateToGettingStarted();
				}
			});
		}
	}

	public logout() {
		if (document.getElementById('chat-widget') !== null) {
			document.getElementById('chat-widget').style.display = 'none';
		}
		if (this.messagingService.tokenBody.TOKEN) {
			this.messagingService.deleteToken();
		}
		this.usersService.logout().subscribe(
			(logoutReponse: any) => {
				localStorage.clear();
				this.cookieService.delete(
					'authentication_token',
					'/',
					window.location.host
				);
				const currentUrl = window.location.pathname;

				const expiredDate = new Date();
				expiredDate.setDate(expiredDate.getDate() + 45);

				this.cookieService.set(
					'login_redirect',
					currentUrl,
					expiredDate,
					'/',
					window.location.host,
					true,
					'None'
				);
				this.usersService.setAuthenticationToken(null);
				this.websocketService.disconnect();
				if (currentUrl === '/guide') {
					this.router.navigate(
						[
							{
								primary: 'guide',
							},
						],
						{ skipLocationChange: true }
					);
				} else {
					this.router.navigate(['guide']);
				}
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	public playAudio() {
		const audio = new Audio();
		audio.src = '../../assets/sounds/quite-impressed.mp3';
		audio.load();
		audio.play();
	}

	public reloadNotificationOnPublish() {
		this.notificationSub = this.cacheService.notificationEntry.subscribe(
			(notification) => {
				if (notification.READ === false && notification.DATA_ID !== null) {
					this.cacheService.notificationEntry.next({
						READ: true,
						DATA_ID: null,
						MODULE_ID: null,
						MESSAGE: null,
						NOTIFICATION_ID: null,
					});
					if (notification.READ === false) {
						this.playAudio();
					}
					this.notificationSubscription();
				}
			}
		);
	}

	public goToLogin() {
		this.router.navigate(['login']);
	}

	public goToSignup() {
		this.router.navigate(['signup']);
	}

	public statusChange(status) {
		// this.heartbeat(status.toUpperCase());
	}

	// TODO:  HEARTBEAT correct behavior;
	public heartbeat(status) {
		// this.usersService.webHeartBeat(status).subscribe(
		//   (heartbeatResponse: any) => {
		//     console.log(heartbeatResponse);
		//   },
		//   (error: any) => {
		//     console.log(error);
		//   }
		// );
	}

	//Code reated to the chat actions in login menu
	//Uncomment the code if need chat related API calls

	public chatStatus(status) {
		console.log('status', status);
		// if (status) {
		// 	this.companiesService.trackEvent('Clicked set Chat Stats Online');
		// 	if (this.cacheService.companyData['USAGE_TYPE'].CHAT === true) {
		// 		if (
		// 			this.cacheService.companyData['ALL_GETTING_STARTED'][2].COMPLETED ===
		// 			false
		// 		) {
		// 			this.companiesService
		// 				.putGettingStarted(
		// 					this.cacheService.companyData['ALL_GETTING_STARTED'][2]
		// 				)
		// 				.subscribe((put: any) => {});
		// 		}
		// 	}
		// } else {
		// 	this.companiesService.trackEvent('Clicked set Chat Stats Offline');
		// }

		// if (this.isAcceptingChats !== status) {
		this.accept = false;
		const receiptId = require('uuid').v4();
		const chatStatus = {
			userId: this.usersService.user.DATA_ID,
			subdomain: this.usersService.getSubdomain(),
			accepting: status,
		};
		this.websocketService.publishChatStatus(chatStatus);
		//donot uncomment this
		// this._stompService.watchForReceipt(receiptId, (frame) => {
		//   console.log('Receipt: ', frame);
		//   this.getAcceptingChats();
		// });

		// 		// this._stompService._stompManagerService.publish({
		// 		// 	destination: 'ngdesk/chat-status',
		// 		// 	body: JSON.stringify(chatStatus),
		// 		// 	headers: {
		// 		// 		receipt: receiptId
		// 		// 	}
		// 		// });

		// setTimeout(() => {
		// 	this.getAcceptingChats();
		// }, 2000);
		// }
	}

	public notificationSubscription() {
		this.notificationsService
			.getUnreadNotifications(this.pageNumber)
			.subscribe((notifications: any) => {
				this.unreadNotificationsLength = notifications.unreadNotificationLength;
				this.unreadNotifications = notifications.getUnreadNotifications;
				this.filteredUnreadNotifications = this.unreadNotifications;
				this.notificationLength = notifications.getUnreadNotifications.length;
				if (this.unreadNotificationsLength < 1) {
					this.panelWidth = 'auto';
				}
			});
	}

	public openEntry(notification) {
		notification['id'] = notification.notificationId;
		notification.read = true;
		this.notificationApiService
			.updateNotification(notification)
			.subscribe(() => {
				this.notificationSubscription();
				this.router.navigate([
					`render/${notification.moduleId}/edit/${notification.dataId}`,
				]);
			});
	}

	public readAll() {
		this.notificationApiService.markAllNotificationsAsRead().subscribe(() => {
			this.notificationSubscription();
			console.log('Successfully read all notifications');
		});
	}

	public openAutocomplete(event) {
		event.stopPropagation();
		if (!this.inputAutoComplete.panelOpen) {
			this.inputAutoComplete.openPanel();
		} else {
			this.inputAutoComplete.closePanel();
		}
	}

	public onScroll() {
		this.notificationScrollSubject.next(['', false]);
	}

	public initializeNotificationScrollSubject() {
		this.onScrollSubscribe = this.notificationScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let page = 0;
					if (this.filteredUnreadNotifications) {
						page = Math.ceil(this.notificationLength / 5);
					}
					return this.notificationsService.getUnreadNotifications(page).pipe(
						map((results: any) => {
							const newlist = this.notificationsService.filterNewLists(
								this.unreadNotifications,
								results.getUnreadNotifications
							);
							if (newlist.length > 0) {
								this.filteredUnreadNotifications =
									this.unreadNotifications.concat(newlist);
								this.unreadNotifications =
									this.unreadNotifications.concat(newlist);
							}
							this.notificationLength = this.filteredUnreadNotifications.length;
							return results.getUnreadNotifications;
						})
					);
				})
			)
			.subscribe();
	}

	// private getNotifications() {
	// 	this.notificationsService.getNotifications().subscribe(
	// 		(response: any) => {
	// 			this.notificationsService.unreadNotifications = response.NOTIFICATIONS;
	// 		},
	// 		(error: any) => {
	// 			console.log(error.error.ERROR);
	// 		}
	// 	);
	// }

	// private getAcceptingChats() {
	// 	this.usersService.getAcceptingChats().subscribe(
	// 		(response: any) => {
	// 			this.isAcceptingChats = response.ACCEPTING_CHATS;
	// 			this.accept = true;
	// 		},
	// 		(error: any) => {
	// 			console.log(error);
	// 		}
	// 	);
	// }

	private calculateProgress() {
		this.value = 0;
		for (let i = 0; i < this.complete.length; i++) {
			if (this.complete[i] === true && this.complete.length === 4) {
				this.value = this.value + 25;
			} else if (this.complete[i] === true && this.complete.length === 3) {
				this.value = this.value + 33;
				if (this.value === 99) {
					this.value = this.value + 1;
				}
			}
		}
		if (this.value === 99 || this.value === 100) {
			if (!this.gettingStartedStatus) {
				this.companiesService
					.putGettingStartedStatus(this.usersService.getSubdomain())
					.subscribe(
						(putResponse: any) => {
							this.gettingStartedStatus = true;
							this.gettingStarted = true;
							this.bannerMessageService.successNotifications.push({
								message: 'Successfully Completed Getting Started',
							});
						},
						(errorResponse: any) => {
							console.log(errorResponse);
						}
					);
			}
		}
		this.loaded = true;
	}
	private navigateToGettingStarted() {
		this.router.navigate([`getting-started/${this.moduleId}`]);
	}
	private publishProgress() {
		// const notifySubscription$ = this._stompService._stompRestService
		// 	.watch(`rest/getting-started/step/${this.moduleId}`)
		// 	.pipe(shareReplay());
		// this.sub = notifySubscription$.subscribe((notification: any) => {
		// 	this.companiesService
		// 		.getAllGettingStarted()
		// 		.subscribe((response: any) => {
		// 			let index = 0;
		// 			for (index = 0; index < response.GETTING_STARTED.length; index++) {
		// 				this.complete[index] = response.GETTING_STARTED[index].COMPLETED;
		// 			}
		// 			this.calculateProgress();
		// 		});
		// });
	}

	private publishProgressChatIntegration() {
		// const notifySubscription$ = this._stompService._stompRestService
		// 	.watch(`rest/getting-started/step4/${this.moduleId}`)
		// 	.pipe(shareReplay());
		// this.subStep4 = notifySubscription$.subscribe((notification: any) => {
		// 	if (!this.stepFour) {
		// 		this.companiesService
		// 			.getAllGettingStarted()
		// 			.subscribe((response: any) => {
		// 				if (response.GETTING_STARTED[0].COMPLETED === false) {
		// 					this.sub.unsubscribe();
		// 					this.companiesService
		// 						.putGettingStarted(response.GETTING_STARTED[0])
		// 						.subscribe((put: any) => {
		// 							this.complete[0] = true;
		// 							this.calculateProgress();
		// 						});
		// 				}
		// 			});
		// 		this.stepFour = true;
		// 	}
		// });
	}

	private publishEmailStatus() {
		// const notifySubscription$ = this._stompService._stompRestService
		// 	.watch(`rest/getting-started/step/${this.usersService.user.USER_UUID}`)
		// 	.pipe(shareReplay());
		// this.subStep4 = notifySubscription$.subscribe((notification: any) => {
		// 	if (!this.stepFour) {
		// 		this.companiesService
		// 			.getAllGettingStarted()
		// 			.subscribe((response: any) => {
		// 				if (response.GETTING_STARTED[1].COMPLETED === false) {
		// 					this.sub.unsubscribe();
		// 					this.companiesService
		// 						.putGettingStarted(response.GETTING_STARTED[1])
		// 						.subscribe((put: any) => {
		// 							this.complete[1] = true;
		// 							this.calculateProgress();
		// 						});
		// 				}
		// 			});
		// 		this.stepFour = true;
		// 	}
		// });
	}

	public openProfileMenu() {
		document.getElementById('profile').click();
	}

	public getModuleDetails() {
		this.modulesService
			.getModuleByName('Users')
			.subscribe((usersModuleData: any) => {
				let usersModuleId = usersModuleData.MODULE_ID;
				const imageAttachmentField = usersModuleData.FIELDS.find(
					(moduleField) => 'Image' == moduleField.DATA_TYPE.DISPLAY
				);
				this.modulesService
					.getEntry(usersModuleId, this.usersService.user.DATA_ID)
					.subscribe((entryDetails: any) => {
						if (
							entryDetails &&
							imageAttachmentField &&
							entryDetails[imageAttachmentField.NAME] &&
							entryDetails[imageAttachmentField.NAME].length > 0
						) {
							this.createURLForProfileImage(
								entryDetails[imageAttachmentField.NAME][0].ATTACHMENT_UUID,
								imageAttachmentField.FIELD_ID,
								entryDetails.DATA_ID,
								usersModuleId
							);
						} else {
							this.profilePicURL = '';
						}
					});
			});
	}

	public createURLForProfileImage(
		attachment_uuid,
		field_id,
		data_id,
		usersModuleId
	) {
		let subDomain = this.userService.getSubdomain();
		const url = `https://${subDomain}.ngdesk.com/api/ngdesk-data-service-v1/attachments?
		message_id&module_id=${usersModuleId}&data_id=${data_id}&attachment_uuid=${attachment_uuid}&field_id=${field_id}`;
		this.profilePicURL = url;
	}

	public navigateToCatalogue() {
		this.router.navigate([`render/catalogue`]);
	}

	public checkChatStatus() {
		const chatStatusCheck = {
			userId: this.usersService.user.DATA_ID,
			subdomain: this.usersService.getSubdomain(),
			statusCheck: true,
		};
		this.websocketService.publishChatStatusCheck(chatStatusCheck);
	}
}
