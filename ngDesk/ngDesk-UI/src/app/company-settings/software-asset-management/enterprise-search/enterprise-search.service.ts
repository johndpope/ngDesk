import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class EnterpriseSearchService {
	constructor(
		private http: HttpClient,
		private globals: AppGlobals
	) {}

	public getAllEnterpriseSearch(page, pageSize, sortBy, orderBy): Observable<any> {
		const query = `{
		enterpriseSearch: getEnterpriseSearches(pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
			name
			description
            enterpriseSearchId
			}
			totalCount: getEnterpriseSearchCount
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getEnterpriseSearch(id): Observable<any> {
		const query = ` {
			enterpriseSearch: getEnterpriseSearch(enterpriseSearchId: "${id}") {
				name
                description
                enterpriseSearchId
				tags
				filePath
				regex
				}
   			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
