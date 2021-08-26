import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NGXLogger } from 'ngx-logger';
import { AppGlobals } from 'src/app/app.globals';

@Injectable({
	providedIn: 'root',
})
export class AgentService {
	public minimumTiggerOrder = 1;
	public name: any;

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private logger: NGXLogger
	) {}

	public postController(name, data) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/controllers/${name}`,
			data
		);
	}

	public putController(id, data) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/controller/${id}`,
			data
		);
	}

	public getAllControllers() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/controllers`);
	}

	public getControllerById(id) {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/controllers/${id}`
		);
	}

	public putSubApp(controllerId, data) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/controllers/${controllerId}`,
			data
		);
	}

	// public deleteSubApp(subAppId) {
	// 	return this.http.delete(
	// 		`${this.globals.baseRestUrl}/companies/controllers/${subAppId}/subApps`
	// 	);
	// }

	public downloadInstaller(filename: any) {
		const url = window.location.host + '/installers/' + filename;
		return this.http.get(`${url}`);
	}

	public getInstaller(platform) {
		return this.http.get(`${this.globals.baseRestUrl}/sam/installer/download`, {
			params: { platform: platform },
		});
	}

	public updateActionInstaller(id, instruction, name) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/controller/${id}/instructions`,
			instruction,
			{ params: { application_name: name } }
		);
	}
}
