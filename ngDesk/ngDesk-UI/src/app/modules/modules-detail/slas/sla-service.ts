import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';
import { forkJoin, Observable, Subject } from 'rxjs';
import { ModulesService } from '../../modules.service';
@Injectable({
	providedIn: 'root',
})
export class SlaService {
	public scrollSubject = new Subject<any>();
	public relationshipData: any = {};
	public moduleId = '';

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private modulesService: ModulesService
	) {}

	// Get all call in order to fetch all slas.
	public getAllSlas(
		moduleId,
		page,
		pageSize,
		sortBy,
		orderBy
	): Observable<any> {
		const query = `{
			DATA: getSlas( 
				  moduleId: "${moduleId}",
				  pageNumber: ${page},
				  pageSize: ${pageSize},
				  sortBy: "${sortBy}", 
				  orderBy: "${orderBy}"
				) {
				  name
				  description
				  slaId
				  deleted
				}
			TOTAL_RECORDS:	getSlasCount(moduleId: "${moduleId}")  
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Calling graphql call to fetch a task
	public getSla(slaId, moduleId): Observable<any> {
		const query = `{
			DATA: getSla(
				slaId: "${slaId}"
				moduleId: "${moduleId}"
			  ) {
				companyId
				moduleId
				name
				description
				conditions {
				 requirementType
				 condition
				 operator
				 conditionValue
				 }
				violation {
				  condition
				  operator
				  conditionValue
				}
				slaExpiry
				isRecurring
				recurrence {
				  maxRecurrence
				  intervalTime
				}
				isRestricted
				businessRules {
				  restrictionType
				  restrictions {
					startTime
					endTime
					startDay
					endDay
				  }
				}
				workflow {
				  id
				  name
				  type
				  stages {
					name
				  }
				}
			  }
   		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getData(moduleId): Observable<any[]> {
		const moduleResponse = this.modulesService.getModuleById(moduleId);
		return forkJoin([moduleResponse]);
	}
}
