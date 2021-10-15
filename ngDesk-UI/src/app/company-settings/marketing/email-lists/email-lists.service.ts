import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AppGlobals } from '@src/app/app.globals';

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
}
