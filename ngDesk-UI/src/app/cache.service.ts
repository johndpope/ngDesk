import { Injectable } from '@angular/core';
import { NGXLogger } from 'ngx-logger';

import { ModulesService } from '@src/app/modules/modules.service';
import { PredefinedTemplateService } from '@src/app/render-layout/render-detail-new/predefined-template.service';
import { RenderListLayoutService } from '@src/app/render-layout/render-list-layout-new/render-list-layout.service';
import { RolesService } from '@src/app/roles/roles.service';
import { UsersService } from '@src/app/users/users.service';

import { Observable, BehaviorSubject, forkJoin, of, Subject } from 'rxjs';
import { catchError, map, mergeMap } from 'rxjs/operators';

import { DataApiService } from '@ngdesk/data-api';
import { CompaniesService } from '@src/app/companies/companies.service';
import { RoleLayoutApiService } from '@ngdesk/role-api';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
import { GraphqlListLayoutService } from './graphql-list-layout.service';

@Injectable({
	providedIn: 'root',
})

// TODO: ERROR HANDLING
export class CacheService {
	// JSON to hold all modules
	public authorizedModules = {};
	public moduleInfo = {};
	public moduleNamesToIds = {};
	public entryUpdated: BehaviorSubject<any> = new BehaviorSubject({
		STATUS: false,
	});
	public notificationEntry: BehaviorSubject<any> = new BehaviorSubject({});
	public companyInfoSubject: BehaviorSubject<boolean> = new BehaviorSubject(
		false
	);
	public companyData = {};
	public roles = [];
	public allRoleLayouts = [];
	public roleLayoutInfo = {};
	public samFileRuleInfo = {};

	constructor(
		private logger: NGXLogger,
		private modulesService: ModulesService,
		private rolesService: RolesService,
		private usersService: UsersService,
		private listLayoutService: RenderListLayoutService,
		private predefinedTemplateService: PredefinedTemplateService,
		private dataService: DataApiService,
		private companiesService: CompaniesService,
		private roleLayoutApiService: RoleLayoutApiService,
		private http: HttpClient,
		private globals: AppGlobals,
		private graphqlListLayoutService: GraphqlListLayoutService
	) {}

	public loadAllData() {
		this.getRequiredData().subscribe((responseList) => {
			this.rolesService.role = responseList[0];
			const modules = responseList[1];

			this.companyData = {
				MODULES: modules,
				COMPANY_THEME: responseList[2],
				COMPANY_ENROLLMENT: responseList[3],
				COMPANY_QUESTION_COUNT: responseList[4],
				GETTING_STARTED: responseList[5],
				ALL_GETTING_STARTED: responseList[6],
				USAGE_TYPE: responseList[7],
			};
			this.companyInfoSubject.next(true);

			modules.forEach((module) => {
				const moduleId = module['MODULE_ID'];
				this.authorizedModules[moduleId] = module;
				this.moduleNamesToIds[module['NAME']] = moduleId;

				this.moduleInfo[moduleId] = {
					PAGE: this.listLayoutService.getPageNumber(module),
					PAGE_SIZE: this.listLayoutService.getPageSize(module),
					LIST_LAYOUT: this.listLayoutService.getListLayoutId(module),
					DATA: [],
					TOTAL_RECORDS: 0,
					SEARCH_STRING: this.listLayoutService.getSearchQuey(module),
					SORT_BY: this.listLayoutService.getSortBy(module),
					ORDER_BY: this.listLayoutService.getOrderBy(module),
					UP_TO_DATE: false,
					PREDEFINED_TEMPLATE: {},
					PREMADE_RESPONSES: [],
				};
			});

			// this.getLayoutData().subscribe((layoutResponseList) => {
			// 	layoutResponseList.forEach((response) => {
			// 		this.moduleInfo[response.MODULE_ID]['DATA'] = response.content;
			// 		this.moduleInfo[response.MODULE_ID]['TOTAL_RECORDS'] =
			// 			response.totalElements;
			// 		this.moduleInfo[response.MODULE_ID]['UP_TO_DATE'] = true;
			// 	});
			// });

			this.getPredefinedTemplates().subscribe((templatesResponseList) => {
				templatesResponseList.forEach((response) => {
					this.moduleInfo[this.moduleNamesToIds[response['MODULE_NAME']]][
						'PREDEFINED_TEMPLATE'
					][response['LAYOUT_TYPE']] = response['HTML_TEMPLATE'];
				});
			});
		});

		this.getAllRolesForDetailPage().subscribe((roles) => {
			this.roles = roles;
		});

		// Store all the role layouts with the data.
		this.getRoleLayoutsForMyActionItemPage().subscribe((roleLayouts) => {
			this.allRoleLayouts = roleLayouts;
			this.allRoleLayouts.forEach((roleLayout) => {
				const roleLayoutId = roleLayout.layoutId;
				roleLayout.tabs.forEach((tab) => {
					this.roleLayoutInfo[tab.module] = {
						PAGE: 0,
						PAGE_SIZE: 20,
						ROLE_LAYOUT_DATA: [],
						TOTAL_RECORDS: 0,
						SORT_BY: '',
						ORDER_BY: '',
						LIST_LAYOUT: roleLayoutId,
						DATA: [],
					};
				});
			});
		});
	}

	private getAllRolesForDetailPage(): Observable<any> {
		return this.rolesService.getRoles().pipe(
			map((rolesResponse) => {
				return rolesResponse['ROLES'].filter(
					(role) =>
						role.NAME !== 'ExternalProbe' &&
						role.NAME !== 'LimitedUser' &&
						role.NAME !== 'Public'
				);
			})
		);
	}

	private getRequiredData(): Observable<any[]> {
		const roleResponse = this.rolesService.getRoleById(
			this.usersService.user.ROLE
		);

		const modulesResponse = this.modulesService.getModules().pipe(
			map((moduleResponse) => {
				return moduleResponse['MODULES'];
			})
		);
		const companyTheme = this.companiesService.getTheme();
		const companyEnrollment = this.companiesService.getEnrollment();
		const gettingStarted = this.companiesService
			.getGettingStarted(this.usersService.getSubdomain())
			.pipe(
				map((response) => {
					return response['GETTING_STARTED'];
				})
			);
		const companyAllGettingStarted = this.companiesService
			.getAllGettingStarted()
			.pipe(
				map((response) => {
					return response['GETTING_STARTED'];
				})
			);

		const usageType = this.companiesService
			.getUsageType(this.usersService.getSubdomain())
			.pipe(
				map((response) => {
					return response['USAGE_TYPE'];
				})
			);

		const companyQuestionCount = this.rolesService
			.getRoleById(this.usersService.user.ROLE)
			.pipe(
				mergeMap((role: any) => {
					if (role.NAME === 'SystemAdmin') {
						return this.companiesService.getQuestionCount();
					} else {
						return of({ COUNT: 4 });
					}
				})
			);

		return forkJoin([
			roleResponse,
			modulesResponse,
			companyTheme,
			companyEnrollment,
			companyQuestionCount,
			gettingStarted,
			companyAllGettingStarted,
			usageType,
		]);
	}

	private getPredefinedTemplates(): Observable<any[]> {
		const templateResponses = [];
		Object.keys(this.moduleInfo).forEach((moduleId) => {
			const module = this.authorizedModules[moduleId];
			const createLayout = this.predefinedTemplateService.getLayout(
				'create',
				module
			);
			if (createLayout && createLayout['PANELS'] === null) {
				if (
					createLayout['PREDEFINED_TEMPLATE'] &&
					createLayout['PREDEFINED_TEMPLATE'] !== null
				) {
					templateResponses.push(
						this.modulesService
							.getPreDefinedLayout(
								this.authorizedModules[moduleId]['NAME'],
								'CREATE_LAYOUTS'
							)
							.pipe(
								map((res) => res),
								catchError((error) => of(undefined))
							)
					);
				}
			}
			const editLayout = this.predefinedTemplateService.getLayout(
				'edit',
				module
			);
			if (editLayout && editLayout['PANELS'] === null) {
				if (
					editLayout['PREDEFINED_TEMPLATE'] &&
					editLayout['PREDEFINED_TEMPLATE'] !== null
				) {
					templateResponses.push(
						this.modulesService
							.getPreDefinedLayout(
								this.authorizedModules[moduleId]['NAME'],
								'EDIT_LAYOUTS'
							)
							.pipe(
								map((res) => res),
								catchError((error) => of(undefined))
							)
					);
				}
			}
		});
		return forkJoin(templateResponses);
	}

	public updateModuleData(message) {
		let moduleId = message.MODULE_ID;
		let module;
		if (this.authorizedModules[moduleId]) {
			module = this.authorizedModules[moduleId];
		}

		this.graphqlListLayoutService
			.getListLayoutData(
				moduleId,
				module,
				this.moduleInfo[moduleId]['LIST_LAYOUT'],
				0,
				10,
				'DATE_UPDATED',
				'desc'
			)
			.subscribe(
				(entriesResponse: any) => {
					this.moduleInfo[moduleId]['DATA'] = entriesResponse['DATA'];
					this.moduleInfo[moduleId]['TOTAL_RECORDS'] =
						entriesResponse['TOTAL_RECORDS'];
					this.entryUpdated.next({
						STATUS: true,
						MODULE_ID: moduleId,
						DATA_ID: message.DATA_ID,
					});
				},
				(error: any) => {
					console.log(error);
				}
			);
	}

	public executeGraphqlQuery(query: String) {
		return this.http.post(this.globals.graphqlUrl, query);
	}

	public updateModule(moduleId) {
		this.modulesService.getModuleById(moduleId).subscribe((data) => {
			this.authorizedModules[moduleId] = data;
		});
	}

	public getPrerequisiteForDetaiLayout(moduleId, entryId): Observable<any[]> {
		let module;
		let moduleEntry;
		if (this.authorizedModules[moduleId]) {
			module = this.authorizedModules[moduleId];
		}
		if (
			module &&
			this.moduleInfo[moduleId] &&
			this.moduleInfo[moduleId]['UP_TO_DATE']
		) {
			if (entryId === 'new') {
				return forkJoin([of(module), of({})]);
			}
			if (this.moduleInfo[moduleId] && this.moduleInfo[moduleId]['DATA']) {
				moduleEntry = this.moduleInfo[moduleId]['DATA'].find(
					(entry) => entry['DATA_ID'] === entryId
				);
			}
			if (moduleEntry) {
				return forkJoin([of(module), of(moduleEntry)]);
			} else {
				if (entryId === 'new') {
					return forkJoin([of(module), of({})]);
				}
				const response = this.buildGraphQLQueryForEntry(module, entryId).pipe(
					mergeMap((graphqlQuery: any) => {
						return this.http.post(
							`${this.globals.graphqlUrl}`,
							graphqlQuery.graphqlQuery
						);
					})
				);
				return forkJoin([of(module), response]);
			}
		} else {
			let moduleResponse;
			if (module) {
				moduleResponse = of(module);
			} else {
				moduleResponse = this.modulesService.getModuleById(moduleId);
			}
			if (entryId === 'new') {
				return forkJoin([moduleResponse, of({})]);
			} else {
				const entryResponse = this.modulesService.getModuleById(moduleId).pipe(
					mergeMap((moduleResponse: any) => {
						return this.buildGraphQLQueryForEntry(moduleResponse, entryId).pipe(
							mergeMap((graphqlQuery: any) => {
								return this.http.post(
									`${this.globals.graphqlUrl}`,
									graphqlQuery.graphqlQuery
								);
							})
						);
					})
				);

				if (entryId === 'new') {
					return forkJoin([of(module), of({})]);
				}

				return forkJoin([moduleResponse, entryResponse]);
			}
		}
	}

	// This function fetch the premade responses based on the module id and store it.
	// Further if the data exists it directly returns the premade responses.
	public getModulePremadeResponses(moduleId): Observable<any> {
		if (
			this.moduleInfo[moduleId] &&
			this.moduleInfo[moduleId]['PREMADE_RESPONSES'] &&
			this.moduleInfo[moduleId]['PREMADE_RESPONSES'].length > 0
		) {
			return of(this.moduleInfo[moduleId]['PREMADE_RESPONSES']);
		} else {
			return this.modulesService
				.getAllPremadeResponsesByModuleId(moduleId)
				.pipe(
					map(
						(premadeResponse) =>
							(this.moduleInfo[moduleId]['PREMADE_RESPONSES'] =
								premadeResponse['DATA'])
					)
				);
		}
	}

	public getModuleForDetailLayout(moduleId): Observable<any[]> {
		let module;
		if (this.authorizedModules[moduleId]) {
			module = this.authorizedModules[moduleId];
		}
		if (module && this.moduleInfo[moduleId]['UP_TO_DATE']) {
			return forkJoin([of(module)]);
		} else {
			const moduleResponse = this.modulesService.getModuleById(moduleId);
			return forkJoin([moduleResponse]);
		}
	}

	public getPredefinedTemplate(moduleId, type, moduleName): Observable<string> {
		let layoutType = 'CREATE_LAYOUTS';
		if (type === 'edit') {
			layoutType = 'EDIT_LAYOUTS';
		}
		if (
			this.moduleInfo[moduleId] &&
			this.moduleInfo[moduleId]['PREDEFINED_TEMPLATE'] &&
			this.moduleInfo[moduleId]['PREDEFINED_TEMPLATE'][layoutType]
		) {
			return of(this.moduleInfo[moduleId]['PREDEFINED_TEMPLATE'][layoutType]);
		} else {
			return this.modulesService
				.getPreDefinedLayout(moduleName, layoutType)
				.pipe(map((templateResponse) => templateResponse['HTML_TEMPLATE']));
		}
	}

	public getModule(moduleId): Observable<any> {
		if (this.authorizedModules[moduleId]) {
			return of(this.authorizedModules[moduleId]);
		} else {
			return this.modulesService.getModuleById(moduleId);
		}
	}

	public getRoles() {
		if (this.roles) {
			return of(this.roles);
		} else {
			return this.getAllRolesForDetailPage();
		}
	}

	// Fetch all the role layouts.
	// If the data already exist in cache service it returns.
	// Else It makes an API call
	public getAllRoleLayouts() {
		if (this.allRoleLayouts && this.roles.length !== 0) {
			return of(this.allRoleLayouts);
		} else {
			this.roleLayoutApiService
				.getRoleBasedRoleLayouts(this.usersService.user.ROLE)
				.subscribe((roleLayoutsResponse: any) => {
					return roleLayoutsResponse.content;
				});
		}
	}

	// MAke api call for role layouts.
	private getRoleLayoutsForMyActionItemPage(): Observable<any> {
		return this.roleLayoutApiService
			.getRoleBasedRoleLayouts(this.usersService.user.ROLE)
			.pipe(
				map((roleLayoutsResponse: any) => {
					return roleLayoutsResponse.content;
				})
			);
	}

	// Fetch the role layouts data based on layout id and module id.
	// If the data already exist in cache service it returns.
	// Else It makes an API call
	public getRoleLayoutData(
		listLayoutId,
		moduleId,
		pageNumber,
		pageSize,
		sort,
		order
	): Observable<any> {
		if (
			this.roleLayoutInfo[moduleId]['LIST_LAYOUT'] === listLayoutId &&
			this.roleLayoutInfo[moduleId]['PAGE'] === pageNumber &&
			this.roleLayoutInfo[moduleId]['PAGE_SIZE'] === pageSize &&
			this.roleLayoutInfo[moduleId]['SORT_BY'] === sort &&
			this.roleLayoutInfo[moduleId]['ORDER_BY'] === order
		) {
			return of({
				DATA: this.roleLayoutInfo[moduleId]['DATA'],
				TOTAL_RECORDS: this.roleLayoutInfo[moduleId]['TOTAL_RECORDS'],
			});
		}
		const sortBy = [sort + ',' + order];
		return this.dataService
			.getRoleLayoutData(listLayoutId, moduleId, pageNumber, pageSize, sortBy)
			.pipe(
				map((layoutResponse) => {
					if (this.roleLayoutInfo[moduleId]) {
						this.roleLayoutInfo[moduleId]['LIST_LAYOUT'] = listLayoutId;
						this.roleLayoutInfo[moduleId]['PAGE'] = pageNumber;
						this.roleLayoutInfo[moduleId]['PAGE_SIZE'] = pageSize;
						this.roleLayoutInfo[moduleId]['SORT_BY'] = sort;
						this.roleLayoutInfo[moduleId]['ORDER_BY'] = order;
						this.roleLayoutInfo[moduleId]['DATA'] = layoutResponse.content;
						this.roleLayoutInfo[moduleId]['TOTAL_RECORDS'] =
							layoutResponse.totalElements;
					}
					return {
						DATA: layoutResponse.content,
						TOTAL_RECORDS: layoutResponse.totalElements,
					};
				})
			);
	}

	public buildGraphQLQueryForEntry(module, entryId): Observable<any> {
		return this.modulesService.getModules().pipe(
			map((modules) => {
				let graphqlQuery = `{
					entry: get${module.NAME.replace(/ /g, '_')}Entry(id: "${entryId}") {
						`;
				const fields = module.FIELDS;
				fields.forEach((field) => {
					if (field.NAME === 'CHANNEL') {
						graphqlQuery += `CHANNEL {
							name
						}`;
					} else {
						if (
							(field.DATA_TYPE.DISPLAY === 'Relationship' &&
								field.RELATIONSHIP_TYPE !== 'One to Many') ||
							(field.DATA_TYPE.DISPLAY !== 'Relationship' &&
								field.DATA_TYPE.BACKEND !== 'Button' &&
								field.DATA_TYPE.DISPLAY !== 'Zoom' &&
								field.DATA_TYPE.DISPLAY !== 'Workflow Stages')
						) {
							graphqlQuery = graphqlQuery + "'" + field.NAME + "'" + '\n';
							if (field.DATA_TYPE.DISPLAY === 'Discussion') {
								graphqlQuery = this.buildDiscussionQuery(
									graphqlQuery,
									field.NAME
								);
							} else if (
								field.DATA_TYPE.DISPLAY === 'Relationship' &&
								field.RELATIONSHIP_TYPE !== 'One to Many'
							) {
								graphqlQuery = this.buildRelationshipQuery(
									graphqlQuery,
									modules['MODULES'],
									field.NAME,
									field.MODULE,
									field.PRIMARY_DISPLAY_FIELD
								);
							} else if (field.DATA_TYPE.DISPLAY === 'Approval') {
								graphqlQuery = this.buildApprovalQuery(
									graphqlQuery,
									field.NAME
								);
							} else if (field.DATA_TYPE.DISPLAY === 'Phone') {
								graphqlQuery = this.buildPhoneQuery(graphqlQuery, field.NAME);
							} else if (field.DATA_TYPE.DISPLAY === 'File Upload') {
								graphqlQuery = this.buildFileUploadQuery(
									graphqlQuery,
									field.NAME
								);
							} else if (field.DATA_TYPE.BACKEND === 'BLOB') {
								graphqlQuery = this.buildBlobQuery(graphqlQuery, field.NAME);
							} else if (field.DATA_TYPE.DISPLAY === 'List Formula') {
								graphqlQuery = this.buildListFormulaQuery(
									graphqlQuery,
									field.NAME
								);
							}
						}
					}
				});
				graphqlQuery =
					graphqlQuery +
					`}
				}`;
				let re = /\'/gi;
				graphqlQuery = graphqlQuery.replace(re, '');
				return {
					graphqlQuery,
				};
			})
		);
	}

	public buildListFormulaQuery(graphqlQuery, fieldName) {
		graphqlQuery = graphqlQuery.replace(
			"'" + fieldName + "'",
			fieldName +
				`{
				FORMULA_NAME
				VALUE
			}`
		);
		return graphqlQuery;
	}

	public buildDiscussionQuery(graphqlQuery, fieldName) {
		graphqlQuery = graphqlQuery.replace(
			"'" + fieldName + "'",
			fieldName +
				`{
            MESSAGE
			DATE_CREATED
            MESSAGE_ID
            SENDER{
                FIRST_NAME
                LAST_NAME
                ROLE{
                    roleId
                }
                USER_UUID
            }
            MESSAGE_TYPE
            ATTACHMENTS{
                FILE_NAME
                FILE_EXTENSION
                HASH
                ATTACHMENT_UUID
            }
        }`
		);
		return graphqlQuery;
	}
	public buildRelationshipQuery(
		graphqlQuery,
		modules,
		fieldName,
		moduleId,
		primaryDisplayFieldId
	) {
		const relatedModule = modules.filter(
			(module) => module['MODULE_ID'] === moduleId
		);
		if (relatedModule[0]) {
			const primaryDisplayField = relatedModule[0].FIELDS.filter(
				(field) => field['FIELD_ID'] === primaryDisplayFieldId
			);
			if (primaryDisplayField[0]) {
				graphqlQuery = graphqlQuery.replace(
					"'" + fieldName + "'",
					fieldName +
						`{
								DATA_ID: _id
								PRIMARY_DISPLAY_FIELD: ${primaryDisplayField[0].NAME}   
							   }`
				);
			}
		} else {
			graphqlQuery = graphqlQuery.replace("'" + fieldName + "'", '');
		}
		return graphqlQuery;
	}
	public buildApprovalQuery(graphqlQuery, fieldName) {
		graphqlQuery = graphqlQuery.replace(
			"'" + fieldName + "'",
			fieldName +
				`{
					STATUS
					DISABLE_ENTRY
				}`
		);
		return graphqlQuery;
	}
	public buildPhoneQuery(graphqlQuery, fieldName) {
		graphqlQuery = graphqlQuery.replace(
			"'" + fieldName + "'",
			fieldName +
				`{
					COUNTRY_CODE 
					DIAL_CODE
					PHONE_NUMBER
					COUNTRY_FLAG
				}`
		);
		return graphqlQuery;
	}

	public buildFileUploadQuery(graphqlQuery, fieldName) {
		graphqlQuery = graphqlQuery.replace(
			"'" + fieldName + "'",
			fieldName +
				`{
					FILE_NAME
					FILE_EXTENSION
					HASH
					ATTACHMENT_UUID
				}`
		);
		return graphqlQuery;
	}

	public buildBlobQuery(graphqlQuery, fieldName) {
		graphqlQuery = graphqlQuery.replace(
			"'" + fieldName + "'",
			fieldName +
				`{
					FILE_NAME
					FILE_EXTENSION
					HASH
					ATTACHMENT_UUID
				}`
		);
		return graphqlQuery;
	}
}
