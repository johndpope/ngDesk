import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AppGlobals } from '../../../app.globals';
import { forkJoin } from 'rxjs';

Injectable({
	providedIn: 'root',
});

@Injectable()
export class EmailListService {
	constructor(private http: HttpClient, private globals: AppGlobals) {}
	public getEmailList(emailListId) {
		const query = `{
			EMAIL_LIST: getEmailList(id: "${emailListId}") {
 					EMAIL_LIST_ID:emailListId
					NAME:name
 					DESCRIPTION:description
					CONDITIONS:conditions{
						CONDITION:condition
						OPERATOR:operator
						CONDITION_VALUE:conditionValue
						REQUIREMENT_TYPE:requirementType       					
    				}
				}
			}`;

		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getAllEmailLists(page, pageSize, sortBy, orderBy) {
		const query = `{
			EMAIL_LISTS: getEmailLists(pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				NAME: name
				EMAIL_LIST_ID: emailListId
			}
			TOTAL_RECORDS: getEmailListCount
		}`;

		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getAllEntriesWithConditions(
		usersModule,
		pageNumber,
		pageSize,
		sortBy,
		orderBy,
		filters
	) {
		let query = `{      
			DATA: get${usersModule.NAME}(
			   moduleId: "${usersModule.MODULE_ID}"
				 pageNumber: ${pageNumber}
				 pageSize: ${pageSize}
				 sortBy: "DATE_CREATED"
				 orderBy: "${orderBy}"
			   ) {
				 DATA_ID: _id
				 EMAIL_ADDRESS: EMAIL_ADDRESS
				 CONTACT {
					 FIRST_NAME
					 LAST_NAME
				 }
			 }
				 }`;
		let payload: any = {
			query: query,
			conditions: filters,
		};
		const url = this.globals.graphqlEmailListsUrl;
		const emailListData = this.makeGraphQLCall(url, payload);
		const emailListCount = this.getAllEntriesCountWithConditions(
			usersModule.MODULE_ID,
			filters
		);
		return forkJoin([emailListData, emailListCount]);
	}

	public getAllEntriesCountWithConditions(moduleId, filters) {
		let query = `{
      TOTAL_RECORDS: count(moduleId: "${moduleId}")
    }`;

		let payload: any = {
			query: query,
			conditions: filters,
		};
		const url = this.globals.graphqlEmailListsUrl;
		return this.makeGraphQLCall(url, payload);
	}

	public makeGraphQLCall(url, query: string) {
		return this.http.post(`${url}`, query);
	}
}
