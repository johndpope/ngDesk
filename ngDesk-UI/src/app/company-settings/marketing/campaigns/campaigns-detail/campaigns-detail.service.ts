import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';

@Injectable({
	providedIn: 'root',
})
export class CampaignsDetailService {
	constructor(private http: HttpClient, private globals: AppGlobals) {}

	public getUsersData(moduleId, pageNumber, searchValue) {
		let query = '';
		query = `{
				DATA: getUsers(moduleId: "${moduleId}", pageNumber: ${pageNumber}, pageSize: 10, sortBy: "EMAIL_ADDRESS", orderBy: "Asc", search: "${searchValue}") {
					DATA_ID: _id
					EMAIL_ADDRESS: EMAIL_ADDRESS
				}
			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
