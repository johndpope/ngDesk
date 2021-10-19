import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AppGlobals } from '../../../app.globals';

Injectable({
	providedIn: 'root',
});

@Injectable()
export class EmailListService {
	constructor(private http: HttpClient, private globals: AppGlobals) {}
	public getEmailList(emailListId) {
		const query = `{
				emailList: getEmailList(id: "${emailListId}") {
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
			emailLists: getEmailLists(pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				NAME: name
				EMAIL_LIST_ID: emailListId
			}
			totalCount: getEmailListCount
		}`;

		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getAllEntriesWithConditions(
		moduleId,
		pageNumber,
		pageSize,
		sortBy,
		orderBy,
		filters
	) {
		let query = `{      
			DATA: getEntriesWithConditionForUsers(
			   moduleId: "${moduleId}"
				 pageNumber: ${pageNumber}
				 pageSize: ${pageSize}
				 sortBy: "${sortBy}"
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
		return this.http.post(`${this.globals.graphqlEmailListsUrl}`, payload);
	}
}
