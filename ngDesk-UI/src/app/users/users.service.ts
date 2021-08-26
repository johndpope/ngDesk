import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { NGXLogger } from 'ngx-logger';

import { AppGlobals } from '@src/app/app.globals';

@Injectable({
	providedIn: 'root'
})
export class UsersService {
	private _user: any = {};
	private _companyUuid = '';
	private authenticationToken = '';
	private _subdomain: string;
	private _sidebarLogo: string;
	private _sidebarTitle: string;
	// public url:string;

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private cookieService: CookieService,
		private logger: NGXLogger
	) {
		this.setAuthenticationToken(this.cookieService.get('authentication_token'));
	}



	public login(creds: {
		EMAIL_ADDRESS: string;
		PASSWORD: string;
		SUBDOMAIN: string;
	}) {
		this.logger.debug(`UsersService.login(creds:${JSON.stringify(creds)})`);
		return this.http.post(`${this.globals.baseRestUrl}/users/login`, creds, {
			headers: new HttpHeaders({ 'Device-Type': 'web' })
		});
	}

	public validate() {
		return this.http.post(
			`${this.globals.baseRestUrl}/users/login/validate`,
			{}
		);
	}

	public getAuthenticationToken() {
		return this.authenticationToken;
	}

	public setAuthenticationToken(authenticationToken: string) {
		this.authenticationToken = authenticationToken;
	}

	public logout() {
		return this.http.post(`${this.globals.baseRestUrl}/users/logout`, {});
	}

	public webHeartBeat(status) {
		return this.http.post(
			`${this.globals.managerUrl}/users/heartbeat`,
			{},
			{ params: new HttpParams().set('status', status) }
		);
	}

	public verifyEmail(email, uuid) {
		return this.http.post(
			`${this.globals.baseRestUrl}/users/email/verify`,
			{},
			{ params: new HttpParams().set('email', email).set('uuid', uuid) }
		);
	}

	public get user() {
		return this._user;
	}

	public setUserDetails(user) {
		this._user = user;
	}

	public get companyUuid() {
		return this._companyUuid;
	}

	public setCompanyUuid(companyUuid) {
		this._companyUuid = companyUuid;
	}

	public getSubdomain() {
		return this._subdomain;
	}

	public setSubdomain(subdomain) {
		this._subdomain = subdomain;
	}

	public get sidebarLogo() {
		return this._sidebarLogo;
	}

	public set sidebarLogo(sidebarLogo) {
		this._sidebarLogo = sidebarLogo;
	}

	public get sidebarTitle() {
		return this._sidebarTitle;
	}

	public set sidebarTitle(sidebarTitle) {
		this._sidebarTitle = sidebarTitle;
	}

	// public getAcceptingChats() {
	// 	return this.http.get(`${this.globals.managerUrl}/users`);
	// }

	public postFirebaseToken(tokenBody: any) {
		return this.http.post(
			`${this.globals.baseRestUrl}/users/tokens`,
			tokenBody
		);
	}

	public postChangePassword(newPassword: any) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/users/change_password`,
			newPassword
		);
	}

	public postUnsubscriptionToMarketingEmail(email, uuid) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/users/marketing/emails/unsubscribe`,
			{},
			{ params: new HttpParams().set('email', email).set('uuid', uuid) }
		);
	}

	public checkPermission(role, moduleId, type) {
		console.log(role, moduleId, type);
		let editAccess = false;
		if (role.NAME === 'SystemAdmin') {
			editAccess = true;
		} else {
			const permissions = role.PERMISSIONS;
			permissions.find(modulePermissions => {
				if (modulePermissions.MODULE === moduleId) {
					if (modulePermissions.MODULE_PERMISSIONS.ACCESS === 'Enabled') {
						if (
							type === 'EDIT' &&
							(modulePermissions.MODULE_PERMISSIONS.EDIT === 'All' ||
								modulePermissions.MODULE_PERMISSIONS.EDIT === 'Not Set')
						) {
							editAccess = true;
						}
						if (
							type === 'VIEW' &&
							(modulePermissions.MODULE_PERMISSIONS.VIEW === 'All' ||
								modulePermissions.MODULE_PERMISSIONS.VIEW === 'Not Set')
						) {
							editAccess = true;
						}
						if (
							type === 'DELETE' &&
							(modulePermissions.MODULE_PERMISSIONS.DELETE === 'All' ||
								modulePermissions.MODULE_PERMISSIONS.DELETE === 'Not Set')
						) {
							editAccess = true;
						}
					} else {
						editAccess = false;
					}
				}
			});
		}
		return editAccess;
	}
}
