import { Injectable } from '@angular/core';
import { UsersService } from './users/users.service';
import { CacheService } from './cache.service';
import { ModulesService } from './modules/modules.service';
import { AppGlobals } from './app.globals';
import { ConfigService } from '@src/app/config.service';
import { ApplicationSettings } from './ns-local-storage/app-settings-helper';
import { BehaviorSubject } from 'rxjs';
import { ToolbarComponent } from './toolbar/toolbar.component';
import { I } from '@angular/cdk/keycodes';
import { ToolbarService } from './toolbar/toolbar.service';

@Injectable({
	providedIn: 'root',
})
export class WebsocketService {
	public websocket: any;
	public stopPing: boolean = false;
	private pingInterval: any;
	private webSocketUrl;
	public logNotification: BehaviorSubject<string> = new BehaviorSubject('');

	constructor(
		private usersService: UsersService,
		private cacheService: CacheService,
		private modulesService: ModulesService,
		private configService: ConfigService,
		private applicationSetting: ApplicationSettings,
		private toolbarService: ToolbarService,
	) {
		if (!this.applicationSetting.isMobile()) {
			this.webSocketUrl =
				this.configService.getConfig().wss +
				window.location.host +
				'/api/ngdesk-websocket-service-v1/ngdesk-websocket';
		} else {
			this.webSocketUrl =
				'wss://' +
				this.applicationSetting.getSubdomain() +
				'.ngdesk.com/api/ngdesk-websocket-service-v1/ngdesk-websocket';
		}
	}

	public initialize() {
		this.websocket = new WebSocket(
			`${this.webSocketUrl
			}?authentication_token=${this.usersService.getAuthenticationToken()}`
		);
		this.websocket.onopen = (event) => {
			console.log('websocket connection opened!!!');

			this.pingInterval = setInterval(() => {
				console.log('pinging websocket at ' + new Date());
				this.websocket.send(JSON.stringify({ MESSAGE_TYPE: 'ping' }));
			}, 30 * 1000);
		};

		this.websocket.onclose = (event) => {
			console.log(
				' websocket connection closed, Attempting to reconnect in 1 seconds...'
			);
			clearInterval(this.pingInterval);
			setTimeout(() => {
				this.initialize();
			}, 1 * 1000);
		};

		this.websocket.onerror = (event) => {
			console.log('websocket error');
			console.log(event);
			this.websocket.close();
		};

		this.websocket.onmessage = (event) => {
			let message = JSON.parse(event.data);
			if (message.TYPE === 'PROBE_LOG') {
				this.addLogsToApplication(message);
			} else if (message.type === 'CHAT_SETTINGS_UPDATED') {
				this.toolbarService.updateShowAcceptChat();
			} else if (message.type === 'CHAT_STATUS') {
				this.toolbarService.updateChatStatus(message.chatStatus);
			} else {
				this.updateData(message);
			}
		};
	}

	public disconnect() {
		this.websocket.close();
	}

	public addLogsToApplication(message) {
		this.logNotification.next(message);
	}

	public updateData(message) {
		let moduleId = message.MODULE_ID;
		let moduleEntry;
		if (message.TYPE === 'NOTIFICATION') {
			this.cacheService.entryUpdated.next({
				STATUS: true,
				MODULE_ID: moduleId,
				DATA_ID: message.DATA_ID,
				TYPE: 'NOTIFICATION',
			});
		} else if (message.TYPE === 'DISCUSSION') {
			this.cacheService.entryUpdated.next({
				STATUS: true,
				DATA_ID: message.ENTRY_ID,
				MODULE_ID: moduleId,
				TYPE: 'DISCUSSION',
			});
		} else if (message.read === false) {
			this.cacheService.notificationEntry.next({
				READ: message.read,
				DATA_ID: message.dataId,
				MODULE_ID: message.moduleId,
				MESSAGE: message.message,
				NOTIFICATION_ID: message.id,
			});
		}
		// RESET THE NOTIFICATION
		this.cacheService.entryUpdated.next({
			STATUS: false,
			MODULE_ID: null,
			DATA_ID: null,
		});
		this.cacheService.notificationEntry.next({
			READ: true,
			DATA_ID: null,
			MODULE_ID: null,
			MESSAGE: null,
			NOTIFICATION_ID: null,
		});
	}

	public publishMessage(message) {
		this.websocket.send(JSON.stringify(message));
	}

	public publishApproval(payload) {
		this.websocket.send(JSON.stringify(payload));
	}

	public publishDownloadStatus(payload) {
		this.websocket.send(JSON.stringify(payload));
	}

	public publishChatStatus(payload) {
		this.websocket.send(JSON.stringify(payload));

	}

	public publishChatStatusCheck(payload){
		this.websocket.send(JSON.stringify(payload));
	}
}
