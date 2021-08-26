import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppGlobals } from '@src/app/app.globals';
import { CacheService } from '@src/app/cache.service';

@Injectable({
	providedIn: 'root',
})
export class ServiceCatalogueService {
	constructor(
		public http: HttpClient,
		private globals: AppGlobals,
		private cacheService: CacheService
	) {}

	public getForms(
		moduleId: String,
		pageNumber,
		pageSize,
		sortBy,
		orderBy
	): Observable<any> {
		let query = `
    {
      FORMS:getForms(moduleId: "${moduleId}", pageNumber: ${pageNumber}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
        formId
        name
        description
      }
      TOTAL_RECORDS:getFormsCount(moduleId:"${moduleId}")
    }`;
		return this.http.post(this.globals.graphqlUrl, query);
	}

	public getForm(moduleId, formId): Observable<any> {
		let query = `
    {
  	  FORM: getForm(formId: "${formId}", moduleId: "${moduleId}") {
        formId
        name
		workflow {
			WORKFLOW_ID: id
			NAME: name
		}
        description
        panels {
          grids {
            empty
            height
            width
            fieldId
          }
          ID
          collapse
          panelDisplayName
        }
    	layoutStyle
    	moduleId
    	displayImage
		visibleTo {
			_id,
			NAME
		}
      }
    }`;
		return this.http.post(this.globals.graphqlUrl, query);
	}

	// Get all the teams.
	public getTeamsData(pageNumber, searchValue, teamsModule) {
		let query = '';
		query = `{
				DATA: getTeams(moduleId: "${teamsModule.MODULE_ID}", pageNumber: ${pageNumber}, pageSize: 10, sortBy: "NAME", orderBy: "Asc", search: "${searchValue}") {
					id: _id
					name: NAME
				}
			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
