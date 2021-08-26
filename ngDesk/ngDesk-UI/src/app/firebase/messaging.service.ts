import { Injectable } from '@angular/core';
import { AngularFireMessaging } from '@angular/fire/messaging';
import { NGXLogger } from 'ngx-logger';
import { mergeMap } from 'rxjs/operators';
import { UsersService } from '../users/users.service';
import { AngularFireMessagingHelper } from '../ns-local-storage/angular-fire-messaging-helper';


@Injectable()
export class MessagingService {
	public tokenBody = {
		TYPE: 'WEB',
		TOKEN: ''
	};

	public tokenBodyMobile= {
		TYPE: '',
		TOKEN: '',
		DEVICE_UUID:''
	};

	// public angularFireMessaging:any;

	constructor(
		private userService: UsersService,
		private logger: NGXLogger,
		private angularFireMessagingHelper:AngularFireMessagingHelper
	) {
		// this.angularFireMessaging=this.angularFireMessagingHelper.angularFireMessaging;

		// this.angularFireMessagingHelper.angularFireMessaging.messaging.subscribe(_messaging => {
		// 	_messaging.onMessage = _messaging.onMessage.bind(_messaging);
		// 	_messaging.onTokenRefresh = _messaging.onTokenRefresh.bind(_messaging);
		// });
	}

	public updateTokenMobile(token,deviceUuid,os){
		// const platform = window.navigator.platform;
		// console.log(platform);
		console.log("hit update token")
		console.log('os '+os);
		console.log(os.toUpperCase());
		this.tokenBodyMobile.TYPE = os.toUpperCase();
		this.tokenBodyMobile.TOKEN =token;
		this.tokenBodyMobile.DEVICE_UUID=deviceUuid;
		this.userService.postFirebaseToken(this.tokenBodyMobile).subscribe(
			(status: any) => {
				console.log("posted");
				console.log(status);
				this.logger.trace(status);
			},
			(error: any) => {
				this.logger.trace(error);
			}
		);
	}


	public updateToken(token) {
		this.tokenBody.TOKEN = token;
		this.userService.postFirebaseToken(this.tokenBody).subscribe(
			(status: any) => {
				this.logger.trace(status);
			},
			(error: any) => {
				this.logger.trace(error);
			}
		);
	}

	/**
	 * request permission for notification from firebase cloud messaging
	 */
	public requestPermission(userId) {
		this.angularFireMessagingHelper.angularFireMessaging.requestToken.subscribe(
			token => {
				this.updateToken(token);
			},
			err => {
				console.error('Unable to get permission to notify.', err);
			}
		);
	}

	public receiveMessage() {
		this.angularFireMessagingHelper.angularFireMessaging.messages.subscribe(payload => {
			this.logger.trace('new message received. ', payload);
		});
	}

	public deleteToken() {
		this.angularFireMessagingHelper.angularFireMessaging.getToken
			.pipe(mergeMap(token => this.angularFireMessagingHelper.angularFireMessaging.deleteToken(token)))
			.subscribe(token => {
				this.logger.trace('Token deleted! ', token);
			});
	}
}
