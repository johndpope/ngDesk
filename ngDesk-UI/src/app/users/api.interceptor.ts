import {
	HttpErrorResponse,
	HttpHandler,
	HttpInterceptor,
	HttpRequest
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, tap } from 'rxjs/operators';

import { LoaderService } from '../custom-components/loader/loader.service';
import { UsersService } from './users.service';
import { UiFailLogApiService} from '@ngdesk/company-api'
import { UIFailLog} from '@ngdesk/company-api'

@Injectable({
	providedIn: 'root'
})
export class ApiInterceptor implements HttpInterceptor { 
	constructor(
		public userService: UsersService,
		public router: Router,
		public loaderService: LoaderService,
		public uiFailLogApiService: UiFailLogApiService
	) {}

	public uiFailLog : UIFailLog = {};
	public errorStatus;

	public intercept(request: HttpRequest<any>, next: HttpHandler) {
		// TODO: make this request path based
		if (
			this.userService.getAuthenticationToken() !== '' &&
			this.userService.getAuthenticationToken() !== null
		) {
			request = request.clone({
				setHeaders: {
					authentication_token: this.userService.getAuthenticationToken()
				}
				// setParams: {
				// 	authentication_token: this.userService.getAuthenticationToken()
				// }
			});
		}

		return next.handle(request).pipe(
			tap(
				event => {
					// TODO LOGS
				},
				error => {
					console.log('api interceptor');
					console.log(error);
					this.errorStatus = error.status;
					if (error instanceof HttpErrorResponse) {
						if (error.status === 401) {
							console.log('got 401 redirect to login');
							this.router.navigate(['login']);
						} 
					}
				}
			),
			finalize(() => {
				this.loaderService.isLoading = false;
				// TODO push to loggin service
				// Post logs for failed 500s
				if(this.errorStatus === 500 || this.errorStatus === '500'){
					this.uiFailLog.TYPE = request.method;
					this.uiFailLog.URL = request.url;
					this.uiFailLog.AUTHENTICATION_TOKEN =  this.userService.getAuthenticationToken();
					this.uiFailLog.BODY = request.body;
					this.uiFailLog.COMPANY_SUBDOMAIN = this.userService.getSubdomain();
					this.errorStatus = undefined;
					this.uiFailLogApiService.putAccountLevelAccess1(this.uiFailLog).subscribe((response: any) => {
						this.uiFailLog = {};
					});
				}
			})
		);
	}
}
