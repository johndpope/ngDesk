import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { UsersService } from './users/users.service';
import * as jwt_decode from 'jwt-decode';

import { AppGlobals } from './app.globals.tns';
import { getBoolean, getString } from '@nativescript/core/application-settings';
@Injectable({
	providedIn: 'root'
})
export class GuideGuard implements CanActivate {

	public onMessageCallBack=false;
	constructor(
		private usersService: UsersService,
		private router: Router,
		private cookieService: CookieService
	) {}

	public canActivate() {
		
		getBoolean("onMessageCallBack");
	
		if (getString("subdomain") !== undefined && getString('authentication_token') !== undefined ) {
			this.cookieService.set('authentication_token',getString('authentication_token'));
			this.usersService.setAuthenticationToken(getString('authentication_token'));
			const decode = jwt_decode(getString('authentication_token'));
			this.usersService.setUserDetails(JSON.parse(decode.USER));
			this.usersService.setCompanyUuid(decode.COMPANY_UUID);
			this.usersService.setSubdomain(decode.SUBDOMAIN);
			
			this.usersService.validate().subscribe(
				(validateSuccess: any) => {
					
						if(!getBoolean("onMessageCallBack")){
							this.router.navigate([`sidebar`]);
						}else{
						
							let dataId= getString("dataId");
							let moduleId=getString("moduleId");
							let foreground= getBoolean("foreground");
							if (!foreground){
									  if(getString("subdomain") !== undefined && getString('authentication_token') !== undefined){
										console.log("render");
										  this.router.navigate([`render/${moduleId}/detail/${dataId}`],{ queryParams: { previousUrl: '/sidebar' }});
									  }else{
										  console.log("subdomain");
										  this.router.navigate([`login`]);
									  }
							}
								
						}
				},
				(error: any) => {
					return true;
				}
			);
			return false;
		} else {
			
			return true;
		}
}
}
