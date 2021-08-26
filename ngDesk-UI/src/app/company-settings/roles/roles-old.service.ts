import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NGXLogger } from 'ngx-logger';
import { AppGlobals } from '@src/app/app.globals';

@Injectable({
	providedIn: 'root'
})
export class RolesService {
	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private logger: NGXLogger
	) {}

	public getRoles(sortBy?, orderBy?, page?, pageSize?) {
		if (sortBy && orderBy && page && pageSize) {
			this.logger.debug(
				`RolesService.getRoles(sortBy:${sortBy}, orderBy:${orderBy})`
			);
			const httpParams = new HttpParams()
				.set('sort', sortBy)
				.set('order', orderBy)
				.set('page', page)
				.set('page_size', pageSize);
			return this.http.get(`${this.globals.baseRestUrl}/roles`, {
				params: httpParams
			});
		} else {
			return this.http.get(`${this.globals.baseRestUrl}/roles`);
		}
	}

	public getRoleById(roleId) {
		return this.http.get(`${this.globals.baseRestUrl}/roles/${roleId}`);
	}

	public putRoleById(roleId, body) {
		return this.http.put(`${this.globals.baseRestUrl}/roles/${roleId}`, body);
	}

	public postRole(body) {
		return this.http.post(`${this.globals.baseRestUrl}/roles`, body);
	}

	public deleteRole(roleId) {
		return this.http.delete(`${this.globals.baseRestUrl}/roles/${roleId}`);
	}
}
