import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { forkJoin } from 'rxjs';
import { AppGlobals } from '../app.globals';
import { WebsocketService } from '../websocket.service';

@Injectable({
	providedIn: 'root',
})
export class ReportService {
	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private webSocketService: WebsocketService
	) { }

	public getAllReports(sortBy, orderBy, page, pageSize) {
		return this.http.get(`${this.globals.baseRestUrl}/reports?
sort=${sortBy}&order=${orderBy}&page=${page}&page_size=${pageSize}`);
	}

	public getReport(reportName) {
		return this.http.get(`${this.globals.baseRestUrl}/reports/${reportName}`);
	}

	public postReport(reportName, body) {
		return this.http.post(
			`${this.globals.baseRestUrl}/reports/${reportName}`,
			body
		);
	}
	public putReport(reportName, body) {
		return this.http.put(
			`${this.globals.baseRestUrl}/reports/${reportName}`,
			body
		);
	}

	public deleteReport(reportName) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/reports/${reportName}`
		);
	}

	public generateReport(reportName, body) {
		return this.http.post(
			`${this.globals.baseRestUrl}/reports/${reportName}/generate`,
			body
		);
	}

	public postReportData(reportName, body, page, pageSize) {
		return this.http.post(
			`${this.globals.baseRestUrl}/reports/${reportName}/data?page=${page}&page_size=${pageSize}`,
			body
		);
	}

	/** -------  GraphQl Call to post report -------------  */

	public postReportForField(
		module: any,
		filtres: any[],
		fieldsInTable: any[],
		pageNumber: number,
		pagesize: number,
		allModules,
		sortBy,
		orderBy,
		oneToManyFields,
		customisationData
	) {
		if (fieldsInTable && fieldsInTable.length > 0) {
			let moduleName = module.NAME.replace(/\s+/g, '_');
			let moduleId = module.MODULE_ID;
			let query = `{
        
       DATA: getReportsFor${moduleName}(
          moduleId: "${moduleId}"
            pageNumber: ${pageNumber}
            pageSize: ${pagesize}
            sortBy:"${sortBy}"
            orderBy:"${orderBy}"
          ) {
            ${this.buildQuerry(
				module,
				fieldsInTable,
				allModules,
				oneToManyFields,
				customisationData
			)}
        }
			}`;
			let payload: any = {
				query: query,
				conditions: filtres,
			};

			const url = this.globals.graphqlReportsUrl;
			const reportData = this.makeGraphQLCall(url, payload);
			const reportCount = this.buildQueryToGetReportCount(module, filtres);
			return forkJoin([reportData, reportCount]);
		}
	}

	/** -------  GraphQl Call to get report entries count -------------  */

	buildQueryToGetReportCount(module, filtres) {
		let moduleId = module.MODULE_ID;
		let query = `{
      COUNT: getCountForReportsData(moduleId: "${moduleId}")
    }`;

		let payload: any = {
			query: query,
			conditions: filtres,
		};
		const url = this.globals.graphqlReportsUrl;
		return this.makeGraphQLCall(url, payload);
	}

	/** -------  GraphQl Call to generate report (Download)  -------------  */

	generateCSVForReport(
		module,
		filters,
		fieldsInTable,
		allModules,
		fileName,
		oneToManyFields,
		fieldNames,
		customisation
	) {
		let moduleName = module.NAME.replace(/\s+/g, '_');
		let moduleId = module.MODULE_ID;

		let query = `{
        CSV:getCsvFor${moduleName}(moduleId:"${moduleId}") {
          ${this.buildQuerry(
			module,
			fieldsInTable,
			allModules,
			oneToManyFields,
			customisation
		)}
        }
      }`;
		let payload: any = {
			query: query,
			conditions: filters,
			fileName: fileName,
			fieldNames: fieldNames,
		};
		this.webSocketService.publishDownloadStatus(payload);

		// const url = this.globals.graphqlReportsgenerateurl;
		// return this.makeGraphQLCall(url, payload);
	}

	public buildQuerry(
		module,
		fieldsInTable,
		allModules,
		oneToManyFields?,
		customisationData?
	) {
		let aggregateQuery: any = {};
		let reportQuery = 'DATA_ID: _id' + '\n';
		fieldsInTable.forEach((tableField) => {
			const moduleField = module.FIELDS.find(
				(field) => field.FIELD_ID === tableField.fieldId
			);
			if (moduleField && moduleField.NAME === 'CHANNEL') {
				reportQuery += `CHANNEL {
					name
				}`;
			} else if (tableField && tableField.paentFieldId) {
				if (tableField?.DATA_TYPE?.DISPLAY === 'Aggregate') {
					if (!aggregateQuery.hasOwnProperty(tableField.parentFieldName)) {
						aggregateQuery[tableField.parentFieldName] = [
							`
						${tableField.NAME}(moduleId:"${tableField.parentModuleId}")	`,
						];
					} else {
						aggregateQuery[tableField.parentFieldName].push(`
						${tableField.NAME}(moduleId:"${tableField.parentModuleId}")	`);
					}
				} else {
					if (!aggregateQuery.hasOwnProperty(tableField.parentFieldName)) {
						aggregateQuery[tableField.parentFieldName] = [tableField.NAME];
					} else {
						aggregateQuery[tableField.parentFieldName].push(tableField.NAME);
					}
				}
			} else {
				if (moduleField && moduleField.DATA_TYPE.DISPLAY === 'Relationship') {
					const relatedModule = allModules.find(
						(module) => module.MODULE_ID === moduleField.MODULE
					);
					const primaryDisplayField = relatedModule.FIELDS.find(
						(field) => field.FIELD_ID === moduleField.PRIMARY_DISPLAY_FIELD
					);
					const relationshipQuery = `${moduleField.NAME} {
						DATA_ID: _id
						PRIMARY_DISPLAY_FIELD: ${primaryDisplayField.NAME}
					}`;
					reportQuery += relationshipQuery + '\n';
				} else if (moduleField && moduleField.DATA_TYPE.DISPLAY === 'Phone') {
					reportQuery +=
						`${moduleField.NAME} {
						COUNTRY_CODE 
						DIAL_CODE
						PHONE_NUMBER
						COUNTRY_FLAG
					}` + '\n';
				} else {
					reportQuery += moduleField?.NAME + '\n';
					if (moduleField && moduleField.DATA_TYPE.DISPLAY == 'Discussion') {
						reportQuery = reportQuery.replace(
							moduleField.NAME,
							moduleField.NAME +
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
					}
				}
			}
		});

		/**  --------------------------  building Aggregation Field Query for Related Fields --------------------- */

		oneToManyFields?.forEach((field) => {
			if (
				aggregateQuery.hasOwnProperty(field.NAME) &&
				aggregateQuery[field.NAME].length > 0
			) {
				let queryString = '';
				aggregateQuery[field.NAME].forEach((query) => {
					if (queryString == '') {
						queryString = query;
					} else {
						queryString += ' ' + query;
					}
				});

				reportQuery += this.buildRelatedFieldQuery(
					queryString,
					field.NAME,
					module,
					customisationData
				);
			}
		});

		return reportQuery;
	}

	/** ---------- Build Query for One To Many  fields -------------  */

	public buildRelatedFieldQuery(queryString, key, module, customisationData) {
		let sortBy = 'DATE_CREATED';
		let orderBy = 'dsc';
		let pageSize = 1;
		let pageNumber = 0;
		if (customisationData && customisationData.customizeFor == key) {
			sortBy = customisationData.sortBy;
			orderBy = customisationData.orederBy;
			pageSize = customisationData.pageSize;
			pageNumber = customisationData.pageIndex;
		}
		let moduleId = module.MODULE_ID;
		let relationQuery = `
		${key}(pageNumber:${pageNumber} pageSize: ${pageSize} moduleId: "${moduleId}" sortBy:"${sortBy}"
		orderBy:"${orderBy}"){
			${queryString}
		}
		`;
		return relationQuery;
	}

	public buildQuireyToGetAggregationCount(moduleID, dataId, currentFieldId) {
		let query = `{
			TOTAL_RECORDS: getOneToManyCountDataFetcher(moduleId: "${moduleID.MODULE_ID}", fieldId: "${currentFieldId}", dataId: "${dataId}")
		}`;
		const url = this.globals.graphqlUrl;
		return this.makeGraphQLCall(url, query);
	}
	public makeGraphQLCall(url, query: string) {
		return this.http.post(`${url}`, query);
	}
}
