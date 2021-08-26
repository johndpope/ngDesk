import { Injectable } from '@angular/core';
import { RolesService } from '@src/app/roles/roles.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
@Injectable({
	providedIn: 'root',
})
export class RolesValidationService {
	constructor(
		private rolesService: RolesService,
	) {}

	public getEditAccess(roleId, moduleName): Observable<boolean> {
		return this.rolesService.getRole(roleId).pipe(
			map((response) => {
				const roleResponse = JSON.parse(JSON.stringify(response));
				if (roleResponse.NAME !== 'SystemAdmin') {
					for (const permission of roleResponse.PERMISSIONS) {
						if (permission.MODULE === moduleName) {
							if (permission.MODULE_PERMISSIONS.EDIT === 'None') {
								return false;
							} else {
								return true;
							}
						}
					}
				} else {
					return true;
				}
			})
		);
	}
}
