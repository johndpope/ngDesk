import { Injectable } from '@angular/core';

@Injectable()
export class AppGlobals {
	constructor() {}

	public readonly baseRestUrl: string = '/ngdesk-rest/ngdesk';
	public readonly managerUrl: string = '/ngdesk-manager/ngdesk';
	public readonly graphqlUrl =
		'https://' +
		window.location.hostname +
		'/api/ngdesk-graphql-service-v1/query';
	public readonly graphqlReportsUrl =
		'https://' +
		window.location.hostname +
		'/api/ngdesk-graphql-service-v1/reports/data';

	public readonly graphqlReportsgenerateurl =
		'https://' +
		window.location.hostname +
		'/api/ngdesk-graphql-service-v1/reports/generate';

	public readonly graphqlKBUrl =
		'https://' +
		window.location.hostname +
		'/api/ngdesk-graphql-service-v1/noauth/query';

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
