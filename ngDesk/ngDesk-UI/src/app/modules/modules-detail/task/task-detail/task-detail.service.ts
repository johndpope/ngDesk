import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';
import { CacheService } from '@src/app/cache.service';
import { DataApiService } from '@ngdesk/data-api';
import { forkJoin, Observable, of, Subject } from 'rxjs';
import {
	catchError,
	debounceTime,
	distinctUntilChanged,
	map,
	mergeMap,
	switchMap,
} from 'rxjs/operators';
import { ModulesService } from '@src/app/modules/modules.service';
@Injectable({
	providedIn: 'root',
})
export class TaskDetailService {
	public scrollSubject = new Subject<any>();
	public relationshipData: any = {};
	public moduleId = '';

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private modulesService: ModulesService,
		private cacheService: CacheService,
		private dataService: DataApiService
	) {}

	public getPrerequisiteData(moduleId): Observable<any[]> {
		const allModulesResponse = this.modulesService.getModules().pipe(
			map((moduleResponse) => {
				return moduleResponse['MODULES'];
			})
		);
		const moduleResponse = this.cacheService.getModule(moduleId);
		return forkJoin([moduleResponse, allModulesResponse]);
	}

	public initializeSubject(moduleId) {
		this.moduleId = moduleId;
		this.scrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([field, value, search]) => {
					return this.cacheService.getModule(field['MODULE']).pipe(
						map((relatedModule: any) => {
							const primaryDisplayFieldName = relatedModule.FIELDS.find(
								(moduleField) =>
									moduleField.FIELD_ID === field.PRIMARY_DISPLAY_FIELD
							);
							let searchValue = null;
							if (value !== '') {
								searchValue = primaryDisplayFieldName.NAME + '=' + value;
							}
							return searchValue;
						}),
						mergeMap((response) => {
							let page = 0;
							if (this.relationshipData[field.NAME] && !search) {
								page = Math.ceil(this.relationshipData[field.NAME].length / 10);
							}
							return this.dataService
								.getRelationshipData(
									this.moduleId,
									field.FIELD_ID,
									response,
									page,
									10,
									['PRIMARY_DISPLAY_FIELD', 'asc']
								)
								.pipe(
									map((results: any) => {
										if (search) {
											this.relationshipData[field.NAME] = results.content;
										} else if (results.content.length > 0) {
											this.relationshipData[field.NAME] = this.relationshipData[
												field.NAME
											].concat(results.content);
										}
										return results.content;
									})
								);
						})
					);
				})
			)
			.subscribe();
	}

	// Get all call in order to fetch all tasks.
	public getAllTasks(
		moduleId,
		page,
		pageSize,
		sortBy,
		orderBy
	): Observable<any> {
		const query = `{
			DATA: getTasks(moduleId: "${moduleId}", pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				taskName
				taskDescription
				taskId
			}
			TOTAL_RECORDS: getTasksCount(moduleId: "${moduleId}")
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Calling graphql call to fetch a task
	public getTask(taskId, moduleId): Observable<any> {
		const query = `{
			DATA: getTask(taskId:"${taskId}", moduleId:"${moduleId}") {
				companyId,
        		recurrence,
        		moduleId,
       			taskName,
        		stopDate,
				startDate,
				timezone,
				lastExecuted,
        		taskDescription,
        		intervals {
					intervalType,
					intervalValue
				},
        		actions{
					moduleId,
                	type,
                	fields{
                		fieldId,
                    	value
            		}
				}
        	}
   		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
