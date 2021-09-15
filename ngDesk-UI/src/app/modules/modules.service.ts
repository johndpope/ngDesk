import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NGXLogger } from 'ngx-logger';

import { AppGlobals } from '@src/app/app.globals';

@Injectable({
	providedIn: 'root',
})
export class ModulesService {
	public minimumTiggerOrder = 1;
	public name: any;
	// public url: string;

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private logger: NGXLogger
	) {}

	public settemplate(templateName) {
		this.name = templateName;
	}

	public postCurrency(currencies) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/currency`,
			currencies
		);
	}

	public postDashboard(moduleId, data) {
		return this.http.post(`${this.globals.baseRestUrl}/dashboards`, data);
	}

	public putDashboard(moduleId, dashboardId, data) {
		return this.http.put(
			`${this.globals.baseRestUrl}/dashboards/${dashboardId}`,
			data
		);
	}

	public deleteDashboard(moduleId, dashboardId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/dashboards/${dashboardId}`
		);
	}

	public getAllDashboard() {
		return this.http.get(`${this.globals.baseRestUrl}/dashboards`);
	}

	public getAllCurrencies(sortBy, orderBy, page, pageSize) {
		this.logger.debug(
			`ModulesService.getAllCurrencies(sortBy:${sortBy}, orderBy:${orderBy})`
		);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(`${this.globals.baseRestUrl}/companies/currencies`, {
			params: httpParams,
		});
	}

	public getCurrencyById(currencyId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/currency/${currencyId}`
		);
	}

	public putCurrencyById(currencyId, currencies) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/currency/${currencyId}`,
			currencies
		);
	}

	public getModules() {
		return this.http.get(`${this.globals.baseRestUrl}/modules`);
	}

	public getModuleByName(moduleName) {
		return this.http.get(`${this.globals.baseRestUrl}/modules/${moduleName}`);
	}

	// getting modules from GET all modules call
	public getAllModules() {
		return this.http.get(`${this.globals.baseRestUrl}/modules/names/all`);
	}

	public getModuleById(moduleId) {
		return this.http.get(`${this.globals.baseRestUrl}/modules/id/${moduleId}`);
	}

	public getEntries(moduleId) {
		this.logger.debug(`ModulesService.getEntries(moduleId:${moduleId})`);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/data`
		);
	}

	public getSelectedEntries(moduleId, ids: any[]) {
		const body: any = {
			IDS: ids,
		};
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/data/selected_entries`,
			body
		);
	}

	public getFieldFilteredPaginatedSearchEntries(
		moduleId,
		search,
		sortBy,
		orderBy,
		page,
		pageSize,
		filterFieldId
	) {
		this.logger.debug(`ModulesService.getEntries(moduleId:${moduleId})`);
		const httpParams = new HttpParams()
			.set('field_id', filterFieldId)
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize)
			.set('search', search);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/relationship`,
			{ params: httpParams }
		);
	}

	public getSortEntries(moduleId, sortBy, orderBy) {
		this.logger.debug(`ModulesService.getEntries(moduleId:${moduleId})`);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/data`,
			{ params: httpParams }
		);
	}

	public getSearchEntries(moduleId, search, sortBy, orderBy, page, pageSize) {
		this.logger.debug(
			`ModulesService.getEntries(moduleId:${moduleId},search:${search})`
		);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize)
			.set('search', search);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/data`,
			{ params: httpParams }
		);
	}

	public getEntriesSorted(moduleId, sortBy, orderBy, page, pageSize) {
		this.logger.debug(`ModulesService.getEntriesByLayoutId(moduleId:${moduleId},
    sortBy:${sortBy}, orderBy:${orderBy})`);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/data`,
			{ params: httpParams }
		);
	}

	public getEntriesByLayoutId(
		moduleId,
		layoutId,
		sortBy,
		orderBy,
		page,
		pageSize
	) {
		this.logger
			.debug(`ModulesService.getEntriesByLayoutId(moduleId:${moduleId}, layoutId:${layoutId},
      sortBy:${sortBy}, orderBy:${orderBy})`);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		console.log(
			`${this.globals.baseRestUrl}/modules/${moduleId}/layouts/${layoutId}`
		);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/layouts/${layoutId}`,
			{ params: httpParams }
		);
	}

	public getEntriesByLayoutIdOneToMany(
		moduleId,
		layoutId,
		sortBy,
		orderBy,
		page,
		pageSize,
		fieldName,
		fieldValue,
		filterType
	) {
		this.logger
			.debug(`ModulesService.getEntriesByLayoutId(moduleId:${moduleId}, layoutId:${layoutId},
      sortBy:${sortBy}, orderBy:${orderBy})`);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize)
			.set('field_name', fieldName)
			.set('field_value', fieldValue)
			.set('filter_type', filterType);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/layouts/${layoutId}`,
			{ params: httpParams }
		);
	}

	public getEntry(moduleId, entryId) {
		this.logger.debug(
			`ModulesService.getEntries(moduleId:${moduleId}, entryId:${entryId})`
		);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/data/${entryId}`
		);
	}

	public postEntry(moduleId, entry) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/data`,
			entry
		);
	}

	public putEntry(moduleId, entryId, entry) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/data/${entryId}`,
			entry
		);
	}

	public putEntryOneToMany(moduleId, entryId, entry) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/data/${entryId}/one_to_many/`,
			entry
		);
	}

	public deleteEntries(moduleId, entries) {
		return this.http.request(
			'delete',
			`${this.globals.baseRestUrl}/modules/${moduleId}/data`,
			{ body: entries }
		);
	}

	public putEntries(moduleId, entries) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/bulk`,
			entries
		);
	}

	public postMergeEntries(moduleId: string, data: any) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/merge`,
			data
		);
	}

	public getWorkflows(moduleId, pageSize, page, sortBy, orderBy) {
		this.logger.debug(
			`ModulesService.getWorkflows(sortBy:${sortBy}, orderBy:${orderBy})`
		);
		const httpParams = new HttpParams()
			.set('page', page)
			.set('page_size', pageSize)
			.set('sort', sortBy)
			.set('order', orderBy);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/workflows`,
			{ params: httpParams }
		);
	}

	public getWorkflow(moduleId, workflowId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/workflows/${workflowId}`
		);
	}

	public postWorkflow(moduleId, workflow) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/workflows/${workflow.NAME}`,
			workflow
		);
	}

	public putWorkflow(moduleId, workflow) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/workflows/${workflow.WORKFLOW_ID}`,
			workflow
		);
	}

	public deleteWorkflow(moduleId, workflow) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/${moduleId}/workflows/${workflow.WORKFLOW_ID}`
		);
	}

	public getFields(moduleId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/fields`
		);
	}

	public getFieldById(moduleId, fieldId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/fields/id/${fieldId}`
		);
	}

	public getListLayouts(moduleId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_layouts`
		);
	}

	public getMobileListLayouts(moduleId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_mobile_layouts`
		);
	}

	public getListLayout(moduleId, listLayoutId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_layouts/${listLayoutId}`
		);
	}

	public getMobileListLayout(moduleId, listLayoutId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_mobile_layouts/${listLayoutId}`
		);
	}

	public postListLayouts(moduleId, listLayout) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_layouts/${listLayout.NAME}`,
			listLayout
		);
	}

	public postMobileListLayouts(moduleId, listLayout) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_mobile_layouts/${listLayout.NAME}`,
			listLayout
		);
	}

	public transferChat(moduleId, dataId) {
		return this.http.get(
			`${this.globals.managerUrl}/modules/${moduleId}/data/${dataId}/transfer`
		);
	}
	public putListLayouts(moduleId, listLayout) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_layouts/${listLayout.LAYOUT_ID}`,
			listLayout
		);
	}

	public putMobileListLayouts(moduleId, listLayout) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_mobile_layouts/${listLayout.LAYOUT_ID}`,
			listLayout
		);
	}

	public deleteListLayout(moduleId, listLayout, layoutType) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/${moduleId}/${layoutType}/${listLayout.LAYOUT_ID}`
		);
	}

	public deleteMobileListLayout(moduleId, listLayout) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_mobile_layouts/${listLayout.LAYOUT_ID}`
		);
	}

	public putEditLayout(moduleId, editLayout) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/edit_layouts/${editLayout.LAYOUT_ID}`,
			editLayout
		);
	}

	public putMobileLayout(moduleId, editLayout, type) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/${type}/${editLayout.LAYOUT_ID}`,
			editLayout
		);
	}

	public putCreateLayout(moduleId, createLayout) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/create_layouts/${createLayout.LAYOUT_ID}`,
			createLayout
		);
	}

	public postMobileLayout(moduleId, layout, type) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/${type}`,
			layout
		);
	}

	public getEditLayouts(moduleId, sortBy, orderBy, page, pageSize) {
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/edit_layouts`,
			{ params: httpParams }
		);
	}

	public postField(field, moduleId) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/fields/${field.NAME}`,
			field
		);
	}

	public postRelationshipField(field, moduleId, relationshipField) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/fields/${field.NAME}
      /relationship?required=${relationshipField.REQUIRED}&display_name=${relationshipField.DISPLAY_NAME}
      &name=${relationshipField.NAME}&primary_display_field=${relationshipField.PRIMARY_DISPLAY_FIELD}`,
			field
		);
	}

	public getFieldsSorted(moduleId, sortBy, orderBy, page, pageSize) {
		this.logger.debug(`ModulesService.getFieldsSorted(moduleId:${moduleId},
      sortBy:${sortBy}, orderBy:${orderBy})`);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/fields/master`,
			{ params: httpParams }
		);
	}

	public deleteField(moduleId, field) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/${moduleId}/fields/${field.NAME}`
		);
	}

	public putField(moduleId, field) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/fields/${field.NAME}`,
			field
		);
	}

	public getListLayoutsSorted(moduleId, sortBy, orderBy, page, pageSize) {
		this.logger.debug(`ModulesService.getListLayoutsSorted(moduleId:${moduleId},
    sortBy:${sortBy}, orderBy:${orderBy})`);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/list_layouts`,
			{ params: httpParams }
		);
	}

	public getSortedModules(sortBy, orderBy, page, pageSize, location) {
		this.logger.debug(
			`ModulesService.getSortedModules(sortBy:${sortBy}, orderBy:${orderBy})`
		);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize)
			.set('location', location);
		return this.http.get(`${this.globals.baseRestUrl}/modules`, {
			params: httpParams,
		});
	}

	public postModule(moduleObj) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleObj.NAME}`,
			moduleObj
		);
	}

	public putModule(moduleObj) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleObj.MODULE_ID}`,
			moduleObj
		);
	}

	public getLayouts(moduleId, layoutType, sortBy, orderBy, page, pageSize) {
		this.logger.debug(`ModulesService.getListLayoutsSorted(moduleId:${moduleId},
    sortBy:${sortBy}, orderBy:${orderBy})`);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/${layoutType}`,
			{ params: httpParams }
		);
	}

	public getLayoutById(moduleId, layoutType, layoutId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/${layoutType}/${layoutId}`
		);
	}

	public putLayout(moduleId, layoutType, layoutId, layoutObj) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/${layoutType}/${layoutId}`,
			layoutObj
		);
	}

	public postSla(moduleId, sla) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/sla`,
			sla
		);
	}

	public getSlas(moduleId, pageSize, page, sortBy, orderBy) {
		this.logger.debug(
			`ModulesService.getSlas(sortBy:${sortBy}, orderBy:${orderBy})`
		);
		const httpParams = new HttpParams()
			.set('page', page)
			.set('page_size', pageSize)
			.set('sort', sortBy)
			.set('order', orderBy);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/sla`,
			{ params: httpParams }
		);
	}

	public getSla(moduleId, slaId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/sla/${slaId}`
		);
	}

	public putSla(moduleId, slaObj) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/sla/${slaObj.SLA_ID}`,
			slaObj
		);
	}

	public disableSla(moduleId, slaId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/${moduleId}/sla/${slaId}`
		);
	}

	public enableSla(moduleId, slaId) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/sla/${slaId}`,
			{}
		);
	}

	// public getAllPremadeResponses() {
	// 	return this.http.get(
	// 		`${this.globals.baseRestUrl}/modules/premade_responses`
	// 	);
	// }

	public getAllPremadeResponses(sortBy, orderBy, page, pageSize) {
		this.logger.debug(
			`ModulesService.getAllPremadeResponses(sortBy:${sortBy}, orderBy:${orderBy})`
		);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/premade_responses`,
			{
				params: httpParams,
			}
		);
	}

	public getAllPremadeResponsesByModuleId(moduleId) {
		const httpParams = new HttpParams().set('module_id', moduleId);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/premade_responses`,
			{ params: httpParams }
		);
	}

	public getPremadeResponse(responseId, moduleId, dataId) {
		if (moduleId && dataId) {
			const httpParams = new HttpParams()
				.set('module_id', moduleId)
				.set('data_id', dataId);
			return this.http.get(
				`${this.globals.baseRestUrl}/modules/premade_responses/${responseId}`,
				{ params: httpParams }
			);
		} else {
			return this.http.get(
				`${this.globals.baseRestUrl}/modules/premade_responses/${responseId}`
			);
		}
	}

	public postPremadeResponse(premadeResponse) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/premade_responses`,
			premadeResponse
		);
	}

	public putPremadeResponse(premadeResponse) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/premade_responses`,
			premadeResponse
		);
	}

	public deletePremadeResponses(responseId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/premade_responses/${responseId}`
		);
	}

	public getModuleValidations(moduleId, pageSize, page, sortBy, orderBy) {
		this.logger.debug(
			`ModulesService.getModuleValidations(sortBy:${sortBy}, orderBy:${orderBy})`
		);
		const httpParams = new HttpParams()
			.set('page', page)
			.set('page_size', pageSize)
			.set('sort', sortBy)
			.set('order', orderBy);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/validations`,
			{ params: httpParams }
		);
	}

	public getModuleValidation(moduleId, validationId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/validations/${validationId}`
		);
	}

	public postModuleValidations(moduleId, validations) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/validations`,
			validations
		);
	}

	public putModuleValidations(moduleId, validations) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/validations`,
			validations
		);
	}

	public deleteModuleValidations(moduleId, validationId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/${moduleId}/validations/${validationId}`
		);
	}

	public postLayouts(moduleId, layoutType, layout) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/${layoutType}`,
			layout
		);
	}
	public getChatBots(moduleId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/chatbots`
		);
	}

	public getChatBotsTemplates(moduleId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/chatbots/templates`
		);
	}

	public getSortedChatBots(moduleId, sortBy, orderBy) {
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/chatbots`,
			{ params: httpParams }
		);
	}

	public getChatBot(moduleId, chatBotId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/chatbots/${chatBotId}`
		);
	}

	public getChatBotTemplate(moduleId) {
		const httpParams = new HttpParams().set('chatbotName', this.name);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/chatbots/template`,
			{ params: httpParams }
		);
	}

	public postChatBot(moduleId, chatBot) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/chatbots`,
			chatBot
		);
	}

	public putChatBot(moduleId, chatBotId, chatBot) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/chatbots/${chatBotId}`,
			chatBot
		);
	}

	public deleteChatBot(moduleId, chatBotId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/${moduleId}/chatbots/${chatBotId}`
		);
	}

	public getPreDefinedLayout(moduleName, layoutType) {
		return this.http.get(
			`${this.globals.baseRestUrl}/layouts/${moduleName}/${layoutType}`
		);
	}

	public ImportCSV(moduleId, body) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/csv`,
			body
		);
	}

	public getForm(moduleId, formId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/forms/${formId}`
		);
	}

	public postForm(moduleId, body) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/forms`,
			body
		);
	}

	public putForm(moduleId, body, formId) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/forms/${formId}`,
			body
		);
	}
	public getOneToManyData(
		primaryModuleId,
		relatedModuleId,
		fieldId,
		entryId,
		pageSize,
		page,
		sort,
		order
	) {
		const httpParams = new HttpParams()
			.set('module_1', primaryModuleId)
			.set('module_2', relatedModuleId)
			.set('field_id', fieldId)
			.set('page_size', pageSize)
			.set('page', page)
			.set('sort', sort)
			.set('order', order)
			.set('value', entryId);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/relationship/many/data`,
			{ params: httpParams }
		);
	}

	public getFormsSorted(moduleId, sortBy, orderBy, page, pageSize) {
		this.logger.debug(`ModulesService.getFormsSorted(moduleId:${moduleId},
      sortBy:${sortBy}, orderBy:${orderBy})`);
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/forms`,
			{ params: httpParams }
		);
	}

	public getCsvHeaders(moduleId, body) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/csvheaders`,
			body
		);
	}

	public getCsvLogs(sortBy, orderBy, page, pageSize) {
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(`${this.globals.baseRestUrl}/import/csv`, {
			params: httpParams,
		});
	}

	public getCsvLog(dataId) {
		return this.http.get(`${this.globals.baseRestUrl}/import/csv/${dataId}`);
	}

	public getModulesFromGraphql() {
		const query = `{
			MODULES:getModules(pageNumber:0, pageSize:50) {
				  MODULE_ID:moduleId
				  NAME:name
				  FIELDS:fields{
					  FIELD_ID:fieldId
					  NAME: name
					  MODULE: module
					  DISPLAY_LABEL:displayLabel
						  DATA_TYPE:dataType{
								  DISPLAY:display
								  BACKEND:backend
							  }
							  RELATIONSHIP_TYPE:relationshipType
				  }
				
			  }
		  }`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
