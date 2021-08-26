import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class MyActionItemsService {

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
	) {}

	public getRoleLayoutData(layoutId, tabId, module, fields, page, pageSize, sortBy, orderBy): Observable<any> {
		const query = `{
        get${module.replace(/ /g, '_')}RoleLayout(layoutId: "${layoutId}",tabId: "${tabId}" ,pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
			${fields}
			}
		getRoleLayoutValuesCount(layoutId: "${layoutId}", tabId: "${tabId}")
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
    }
    
    public getAllRoleLayouts(): Observable<any> {
		const query = `{
			getRoleLayouts(pageNumber: 0, pageSize: 20){
				layoutId
				name
				companyId
				defaultLayout
				description
				role{
					name
				}
				tabs
					{
						tabId
						module{
							moduleId
							name
						}
						columnsShow{
							fieldId
						}
						 orderBy{
							   order
							   column{
								   fieldId
								   name
								   helpText
								   displayLabel
							   }
						   }
						   conditions{
							   requirementType
							   operator
							   condition{
								   name
							   }
						   }
					}			   
			}
		  
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
