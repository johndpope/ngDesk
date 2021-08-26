import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { ModulesService } from './modules/modules.service';
import { UsersService } from './users/users.service';

@Injectable({
	providedIn: 'root',
})
export class GuideGuard implements CanActivate {
	constructor(
		private usersService: UsersService,
		private modulesService: ModulesService,
		private router: Router,
		private cookieService: CookieService
	) {}

	public canActivate() {

		if (this.cookieService.get('authentication_token') !== '') {
			this.usersService.validate().subscribe(
				(validateSuccess: any) => {
					this.modulesService.getModuleByName('Tickets').subscribe(
						(response: any) => {
							this.router.navigate([`render/${response.MODULE_ID}`]);

						},
						(error: any) => {
							console.log(error);
							return true;
						}
					);
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
