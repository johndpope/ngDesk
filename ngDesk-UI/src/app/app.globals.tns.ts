import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';


import { ConfigService } from '@src/app/config.service';
import { getString } from '@nativescript/core/application-settings';

@Injectable({
	providedIn: 'root',
})
export class AppGlobals {
	public constructor(private configService: ConfigService) { }

	// public readonly production: boolean = this.configService.getConfig()
	// 	.production;
	public baseRestUrl =
		'https://' + getString('subdomain') + '.ngdesk.com/ngdesk-rest/ngdesk';
	public managerUrl =
		'https://' + getString('subdomain') + '.ngdesk.com/ngdesk-manager/ngdesk';
	// public  websocketUrl = 'ws://localhost:9081/ngdesk';
	public websocketUrl =
		'wss://' + getString('subdomain') + '.ngdesk.com/ngdesk-manager/ngdesk';
	public restWebsocketUrl =
		'wss://' + getString('subdomain') + '.ngdesk.com/ngdesk-rest/ngdesk';
	public readonly graphqlUrl = 'https://' + getString('subdomain') + '.ngdesk.com/api/ngdesk-graphql-service-v1/query';
		
	public s4() {
		return Math.floor((1 + Math.random()) * 0x10000)
			.toString(16)
			.substring(1);
	}

	// generate unique id
	public guid() {
		return (
			this.s4() +
			this.s4() +
			'-' +
			this.s4() +
			'-' +
			this.s4() +
			'-' +
			this.s4() +
			'-' +
			this.s4() +
			this.s4() +
			this.s4()
		);
	}
}
