import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { AppGlobals } from '@src/app/app.globals';

import { Role } from '@src/app/models/role';
import { Observable, of } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class RolesService {
	private userRole: Role;

	get role() {
		return this.userRole;
	}

	set role(role) {
		this.userRole = role;
	}

	constructor(private http: HttpClient, private globals: AppGlobals) {}

	public getRole(role): Observable<any> {
		if (this.userRole) {
			return of(this.userRole);
		} else {
			return this.getRoleById(role);
		}
	}

	public getRoles() {
		return this.http.get(`${this.globals.baseRestUrl}/roles`);
	}
	public getRoleById(roleId) {
		return this.http.get(`${this.globals.baseRestUrl}/roles/${roleId}`);
	}

	public getEditAccess(moduleId) {
		if (this.userRole.NAME === 'SystemAdmin') {
			return true;
		}

		const modulePermission = this.userRole.PERMISSIONS.find(
			(permission) => permission.MODULE === moduleId
		);
		if (
			modulePermission &&
			modulePermission.MODULE_PERMISSIONS.ACCESS === 'Enabled'
		) {
			if (modulePermission.MODULE_PERMISSIONS.EDIT !== 'None') {
				return true;
			}
		}
		return false;
	}

	public getViewAccess(moduleId) {
		if (this.userRole.NAME === 'SystemAdmin') {
			return true;
		}

		const modulePermission = this.userRole.PERMISSIONS.find(
			(permission) => permission.MODULE === moduleId
		);
		if (
			modulePermission &&
			modulePermission.MODULE_PERMISSIONS.ACCESS === 'Enabled'
		) {
			if (modulePermission.MODULE_PERMISSIONS.VIEW !== 'None') {
				return true;
			}
		}
		return false;
	}

	public getDeleteAccess(moduleId) {
		if (this.userRole.NAME === 'SystemAdmin') {
			return true;
		}

		const modulePermission = this.userRole.PERMISSIONS.find(
			(permission) => permission.MODULE === moduleId
		);
		if (
			modulePermission &&
			modulePermission.MODULE_PERMISSIONS.ACCESS === 'Enabled'
		) {
			if (modulePermission.MODULE_PERMISSIONS.DELETE !== 'None') {
				return true;
			}
		}
		return false;
	}
}
