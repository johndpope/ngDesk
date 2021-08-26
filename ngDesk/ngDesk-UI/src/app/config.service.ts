import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
@Injectable({
	providedIn: 'root',
})
export class ConfigService {
	private appConfig: any;

	constructor(private httpClient: HttpClient) {}

	public loadConfig() {
		return this.httpClient
			.get(`/environment`)
			.toPromise()
			.then((response) => {
				this.appConfig = response;
			})
			.catch((error) => {
				this.appConfig = {
					production: false,
					https: 'https://',
					wss: 'wss://',
					applicationVersionCheckDelay: 60 * 1000,
					stripePublicKey: 'pk_test_l0eZEHdCES2lcPnXjOAHckNT',
				};
			});
	}

	public getConfig() {
		return this.appConfig;
	}
}
