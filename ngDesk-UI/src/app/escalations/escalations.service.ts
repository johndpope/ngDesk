import { Injectable } from '@angular/core';

import { Observable, forkJoin, from } from 'rxjs';
import { map, mergeMap } from 'rxjs/operators';

import { CompaniesService } from '@src/app/companies/companies.service';
import { RolesValidationService } from '@src/app/company-settings/roles/roles-validation.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { UsersService } from '@src/app/users/users.service';
import { CacheService } from '../cache.service';
import { DataApiService } from '@ngdesk/data-api';
import { AppGlobals } from '@src/app/app.globals';
import { HttpClient } from '@angular/common/http';

@Injectable({
	providedIn: 'root',
})
export class EscalationsService {
	constructor(
		private rolesValidationService: RolesValidationService,
		private usersService: UsersService,
		private companiesService: CompaniesService,
		private modulesService: ModulesService,
		private cacheService: CacheService,
		private dataService: DataApiService,
		private globals: AppGlobals,
		private http: HttpClient
	) {}

	public getDataForEscalations(): Observable<any[]> {
		const modules: any[] = this.cacheService.companyData['MODULES'];
		const userModule = modules.find((module) => module.NAME === 'Users');

		const query = `{
				DATA: getUsers(moduleId: "${userModule.MODULE_ID}", pageNumber: 0, pageSize: 5000) {
					DATA_ID: _id
					EMAIL_ADDRESS: EMAIL_ADDRESS

					USER_UUID
				}
			}`;
		const usersResponse = this.http
			.post(`${this.globals.graphqlUrl}`, query)
			.pipe(
				map((usersResponse) => {
					return usersResponse['DATA'];
				})
			);

		const teamsResponse = this.modulesService.getModuleByName('Teams').pipe(
			map((teamsModuleResponse) => {
				const teamsModuleId = JSON.parse(
					JSON.stringify(teamsModuleResponse)
				).MODULE_ID;
				return teamsModuleId;
			}),
			mergeMap((teamsModuleId) => {
				return this.modulesService.getSortEntries(teamsModuleId, 'NAME', 'asc');
			})
		);

		const editAccessResponse = this.rolesValidationService.getEditAccess(
			this.usersService.user.ROLE,
			'Escalations'
		);
		const scheduleResponse = this.companiesService.getSchedules();
		return forkJoin([
			editAccessResponse,
			scheduleResponse,
			usersResponse,
			teamsResponse,
		]);
	}
	public getTeamsData(teamsModuleId, pageNumber, searchValue) {
		let query = '';
		query = `{
					DATA: getTeams(moduleId: "${teamsModuleId}", pageNumber: ${pageNumber}, pageSize: 10, sortBy: "NAME", orderBy: "Asc", search: "${searchValue}") {
						id: _id
						name: NAME
					}
				}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getUsersData(usersModuleId, pageNumber, searchValue) {
		let query = '';
		query = `{
			DATA: getUsers(moduleId: "${usersModuleId}",  pageNumber: ${pageNumber}, pageSize: 10, sortBy: "EMAIL_ADDRESS", orderBy: "Asc", search: "${searchValue}") {
				DATA_ID: _id
				EMAIL_ADDRESS: EMAIL_ADDRESS
				USER_UUID
			}
		}`;

		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Query to fetch single escalation
	public getEscalationById(escalationId) {
		const query = `{
				DATA:getEscalation(id:"${escalationId}") {
				name
				description
      			id
				lastUpdated{
					_id
				}
				rules{
					minsAfter
					order
					escalateTo{
						users{
							DATA_ID:_id
							CONTACT {
									DATA_ID: _id
									PRIMARY_DISPLAY_FIELD: FULL_NAME
								}
						}
						teams{
							DATA_ID:_id
							NAME
						}
						schedules{
							scheduleId: id
                			name
						}
					}
				}
			}
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Query to fetch all the escalations
	public getEscalations(pageNumber, pageSize, sortBy, orderBy) {
		const query = `{
			DATA: getEscalations(pageNumber: ${pageNumber}, pageSize: ${pageSize}, sortBy:"${sortBy}", orderBy: "${orderBy}"){
				NAME:name
				ESCALATION_ID:id
			}
			totalElements: getEscalationsCount
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
