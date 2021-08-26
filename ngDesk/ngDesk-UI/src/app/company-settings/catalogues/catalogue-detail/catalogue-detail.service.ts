import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';
import { CacheService } from '@src/app/cache.service';
import { Observable, of, Subject } from 'rxjs';
@Injectable({
	providedIn: 'root',
})
export class CatalogueDetailService {
	public moduleId = '';
	public scrollSubject = new Subject<any>();
	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private cacheService: CacheService
	) {}

	//Get all call in order to fetch all forms.

	public getAllForms(
		moduleId,
		page,
		pageSize,
		sortBy,
		orderBy
	): Observable<any> {
		const query = `{
			DATA: getForms(moduleId: "${moduleId}",pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				formId
				name
        		description
			}
			TOTAL_RECORDS: getFormsCount(moduleId: "${moduleId}")
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getForm(moduleId, formId): Observable<any> {
		const query = `{
			DATA: getForm(moduleId:"${moduleId}",formId:"${formId}") {
				
            name,
			formId,
			moduleId
			}
        
   		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Get all call in order to fetch all catalogues.
	public getAllCatalogues(page, pageSize, sortBy, orderBy): Observable<any> {
		const query = `{
			DATA: getCatalogues(pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				name
				description
        		catalogueId
			}
			TOTAL_RECORDS: getCatalogueCount
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getCatalogue(catalogueId): Observable<any> {
		const query = `{
			DATA: getCatalogue(catalogueId:"${catalogueId}") {
				
            name,
        	description,
			visibleTo {
				_id,
				NAME
			}
            displayImage
        	  catalogueForms {
              moduleId
              formId{
				 name
				  formId
			  }
				    }
        	}
   		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
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
