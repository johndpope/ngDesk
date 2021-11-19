import { Injectable } from '@angular/core';
import { DataApiService } from '@ngdesk/data-api';
import { EscalationApiService } from '@ngdesk/escalation-api';
import {
	Node,
	Workflow,
	WorkflowApiService,
	Stage,
	PageWorkflow,
} from '@ngdesk/workflow-api';
import { CacheService } from '@src/app/cache.service';
import { ChannelsService } from '@src/app/channels/channels.service';
import { forkJoin, Observable, of, Subject } from 'rxjs';
import {
	catchError,
	debounceTime,
	distinctUntilChanged,
	map,
	mergeMap,
	switchMap,
} from 'rxjs/operators';
import { HtmlTemplateApiService } from '@ngdesk/module-api';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
import { ModulesService } from '@src/app/modules/modules.service';
import { EscalationsService } from '../../../../escalations/escalations.service';

@Injectable({
	providedIn: 'root',
})
export class TriggersDetailService {
	public escalations: any = [];
	public relatedFields: any = [];
	public nestedRelatedFields: any = [];
	public scrollSubject = new Subject<any>();
	public usersScrollSubject = new Subject<any>();
	public teamsScrollSubject = new Subject<any>();
	public relationshipData: any = {};
	public moduleId = '';
	public teamsLength;
	public usersStore: any = [];
	public usersLength;
	public teamsStore: any = [];

	constructor(
		private cacheService: CacheService,
		private escalationApiService: EscalationApiService,
		private channelService: ChannelsService,
		private dataService: DataApiService,
		private workflowApi: WorkflowApiService,
		private htmlTemplateApiService: HtmlTemplateApiService,
		private http: HttpClient,
		private globals: AppGlobals,
		private modulesService: ModulesService,
		public escalationService: EscalationsService
	) {}

	public initializeUsers() {
		this.usersScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					const moduleId = this.cacheService.moduleNamesToIds['Users'];
					let searchValue = '';
					if (value !== '') {
						searchValue = 'EMAIL_ADDRESS' + '=' + value;
					}
					let page = 0;
					if (this.usersStore && !search) {
						page = Math.ceil(this.usersLength / 10);
					}
					return this.getUsersData(moduleId, page, searchValue).pipe(
						map((results: any) => {
							if (search) {
								this.usersStore = results.DATA;
							} else {
								const newlist = this.filterNewLists('Users', results['DATA']);
								if (newlist.length > 0) {
									this.usersStore = this.usersStore.concat(results.DATA);
								}
							}
							this.usersLength = this.usersStore.length;
							return results.DATA;
						})
					);
				})
			)
			.subscribe();
	}

	public getUsersData(moduleId, pageNumber, searchValue) {
		let query = '';
		query = `{
				DATA: getUsers(moduleId: "${moduleId}", pageNumber: ${pageNumber}, pageSize: 10, sortBy: "EMAIL_ADDRESS", orderBy: "Asc", search: "${searchValue}") {
					DATA_ID: _id
					EMAIL_ADDRESS: EMAIL_ADDRESS
					
				}
			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getContactsData(moduleId, pageNumber, searchValue) {
		let query = '';
		query = `{
				DATA: getContacts(moduleId: "${moduleId}", pageNumber: ${pageNumber}, pageSize: 10, sortBy: "FULL_NAME", orderBy: "Asc", search: "${searchValue}") {
					DATA_ID: _id
					FULL_NAME:FULL_NAME
					PHONE_NUMBER {
						DIAL_CODE
						PHONE_NUMBER
					}
				}
			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public initializeTeams() {
		this.teamsScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					const moduleId = this.cacheService.moduleNamesToIds['Teams'];
					let searchValue = '';
					if (value !== '') {
						searchValue = 'NAME' + '=' + value;
					}
					let page = 0;
					if (this.teamsStore && !search) {
						page = Math.ceil(this.teamsLength / 10);
					}
					return this.getTeamsData(moduleId, page, searchValue).pipe(
						map((results: any) => {
							if (search) {
								this.teamsStore = results.DATA;
							} else {
								const newTeamslist = this.filterTeamsLists(
									'Teams',
									results['DATA']
								);
								if (newTeamslist.length > 0) {
									this.teamsStore = this.teamsStore.concat(results.DATA);
								}
							}
							this.teamsLength = this.teamsStore.length;
							return results.DATA;
						})
					);
				})
			)
			.subscribe();
	}

	public getTeamsData(moduleId, pageNumber, searchValue) {
		let query = '';
		query = `{
				DATA: getTeams(moduleId: "${moduleId}", pageNumber: ${pageNumber}, pageSize: 10, sortBy: "NAME", orderBy: "Asc", search: "${searchValue}") {
					DATA_ID: _id
					NAME: NAME
				}
			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public filterNewLists(type, data) {
		const newArr = [];
		data.forEach((user) => {
			const existingUser = this.usersStore.find(
				(currentUser) => currentUser.DATA_ID === user.DATA_ID
			);
			if (!existingUser) {
				newArr.push(user);
			}
		});
		return newArr;
	}

	public filterTeamsLists(type, data) {
		const newArr = [];
		data.forEach((team) => {
			const existingTeam = this.teamsStore.find(
				(currentTeam) => currentTeam.DATA_ID === team.DATA_ID
			);
			if (!existingTeam) {
				newArr.push(team);
			}
		});
		return newArr;
	}

	public getPrerequisiteData(moduleId, moduleWorkflowId): Observable<any[]> {
		let workflowResponse = of({});

		if (moduleWorkflowId !== 'new') {
			workflowResponse = this.workflowApi.getWorkflowById(
				moduleId,
				moduleWorkflowId
			);
		}

		let workflowOrderResponse: Observable<PageWorkflow> = of({ content: [] });
		if (moduleWorkflowId === 'new') {
			workflowOrderResponse = this.workflowApi.getWorkflows(moduleId, 0, 1, [
				'ORDER,desc',
			]);
		}
		const moduleResponse = this.cacheService.getModule(moduleId);

		const escalationResponse = this.escalationService.getAllEscalations(0, 10, 'name', 'asc').
		pipe(
			map((response) => {
				return response['escalations'];
			}),
			catchError((error) => of([]))
		);
		const channelsResponse = this.channelService
			.getChannels(moduleId, 'email', 20, 0, 'name', 'asc')
			.pipe(
				map((response) => {
					return response['CHANNELS'];
				}),
				catchError((error) => of([]))
			);

		const templatesResponse = this.htmlTemplateApiService
			.getTemplates(moduleId)
			.pipe(
				map((response) => {
					return response['content'];
				}),
				catchError((error) => of([]))
			);
		return forkJoin([
			moduleResponse,
			escalationResponse,
			channelsResponse,
			workflowResponse,
			workflowOrderResponse,
			templatesResponse,
		]);
	}

	public loadRelatedFieldsForBody() {
		// LOAD ALL THE FIELD VALUES TO RELATED FIEDLS
		this.cacheService.companyData['MODULES'].forEach((module) => {
			this.relatedFields[module.MODULE_ID] = module.FIELDS.filter(
				(field) =>
					(field.DATA_TYPE.DISPLAY === 'Relationship' &&
						field.RELATIONSHIP_TYPE !== 'Many to Many' &&
						field.NAME !== 'LAST_UPDATED_BY' &&
						field.NAME !== 'CREATED_BY') ||
					(field.DATA_TYPE.BACKEND === 'String' &&
						field.DATA_TYPE.DISPLAY !== 'Relationship')
			);
			this.nestedRelatedFields[module.MODULE_ID] = module.FIELDS.filter(
				(field) =>
					field.DATA_TYPE.BACKEND === 'String' &&
					field.DATA_TYPE.DISPLAY !== 'Relationship'
			);
			this.relatedFields[module.MODULE_ID].forEach((field) => {
				if (
					field.PRIMARY_DISPLAY_FIELD !== null &&
					field.DISPLAY_LABEL === 'Account'
				) {
					this.cacheService.companyData['MODULES'].filter((relatedModule) => {
						const primaryDisplayField = relatedModule.FIELDS.find(
							(tempField) => tempField.FIELD_ID === field.PRIMARY_DISPLAY_FIELD
						);
						if (primaryDisplayField) {
							field.DISPLAY_LABEL = primaryDisplayField.DISPLAY_LABEL;
						}
					});
				}
			});
		});
	}

	public getSavedValueForCell(cellData: any, cellId: String): any {
		let previousCellValues = cellData.find(
			(previousValue) => previousValue.CELL_ID === cellId
		);
		if (!previousCellValues) {
			previousCellValues = {};
		}
		return previousCellValues;
	}

	public getConnectionsTo(
		nodeId: String,
		links: any[],
		items: any[],
		type: String
	): any[] {
		const connectionsTo = [];
		items.forEach((item) => {
			const link = links.find(
				(nodeLink) =>
					nodeLink.source.port === item.id && nodeLink.source.id === nodeId
			);
			if (link) {
				const connection = {};
				if (type === 'app.Approval') {
					if (item.attrs.portLabel.text === 'Approve') {
						connection['TITLE'] = 'approve';
					} else if (item.attrs.portLabel.text === 'Reject') {
						connection['TITLE'] = 'reject';
					} else {
						connection['TITLE'] = 't1' + Math.random();
					}
				} else {
					connection['TITLE'] = 't1' + Math.random();
				}

				connection['FROM'] = 't2' + Math.random();
				connection['TO_NODE'] = link.target.id;
				connectionsTo.push(connection);
			}
		});
		return connectionsTo;
	}

	public buildCreateUpdateEntryNode(savedData, node, moduleId): any {
		if (node['TYPE'] === 'CreateEntry') {
			node['MODULE'] = savedData.MODULE;
		} else {
			node['MODULE'] = moduleId;
		}
		if (node['TYPE'] !== 'CreateEntry') {
			node['ENTRY_ID'] = '{{InputMessage.DATA_ID}}';
			node['REPLACE'] = savedData.REPLACE;
		}
		let fields: { FIELD: String; VALUE: String[] }[] = [];
		if (savedData && savedData.FIELDS) {
			// PASSING COPY IF PAYLOAD FAILS
			const savedFieldsCopy = JSON.parse(JSON.stringify(savedData.FIELDS));
			if (savedFieldsCopy && savedFieldsCopy.length > 0) {
				fields = this.convertFieldToIds(savedFieldsCopy);
			}
		}
		node['FIELDS'] = fields;
		return node;
	}

	public buildMakePhoneCallAndSendSmsNode(savedData, node): any {
		if (savedData) {
			if (typeof savedData.TO == 'string') {
				node['TO'] = savedData.TO;
			} else {
				node['TO'] = JSON.stringify(savedData.TO);
			}
			node['BODY'] = savedData.BODY;
		}
		return node;
	}

	public buildMicrosoftTeamsNode(savedData, node, moduleId): any {
		node['MODULE'] = moduleId;
		let fields: { FIELD: String }[] = [];
		node['CHANNEL_ID'] = savedData.CHANNEL_ID;
		if (savedData && savedData.FIELDS) {
			fields = savedData.FIELDS.map((x) => x.FIELD.FIELD_ID);
		}
		node['FIELDS'] = fields;
		return node;
	}

	public buildSendEmailNode(savedData, node): any {
		if (savedData) {
			node['TO'] = savedData.TO;
			node['FROM'] = savedData.FROM;
			node['SUBJECT'] = savedData.SUBJECT;
			node['BODY'] = savedData.BODY;
		}
		return node;
	}

	public buildApprovalNode(savedData, node): any {
		if (savedData) {
			node['APPROVERS'] = savedData.APPROVERS;
			node['TEAMS'] = savedData.TEAMS;
			node['APPROVAL_CONDITION'] = savedData.APPROVAL_CONDITION;
			node['NUMBER_OF_APPROVALS_REQUIRED'] =
				savedData.NUMBER_OF_APPROVALS_REQUIRED;
			node['DISABLE_ENTRY'] = savedData.DISABLE_ENTRY;
			node['NOTIFY_USERS_FOR_APPROVAL'] = savedData.NOTIFY_USERS_FOR_APPROVAL;
			node['NOTIFY_USERS_AFTER_APPROVAL'] =
				savedData.NOTIFY_USERS_AFTER_APPROVAL;
		}
		return node;
	}

	public buildPdfNode(savedData, node): any {
		if (savedData) {
			node['PDF_TEMPLATE'] = savedData.PDF_TEMPLATE;
			node['PDF_NAME'] = savedData.PDF_NAME;
		}
		return node;
	}

	public buildStartEscalationNode(savedData, node): any {
		if (savedData) {
			node['ESCALATION_ID'] = savedData.ESCALATION;
			node['BODY'] = savedData.BODY;
			node['SUBJECT'] = savedData.SUBJECT;
		}
		return node;
	}

	public buildSignatureNode(savedData, node): any {
		if (savedData) {
			node['TO'] = savedData.TO;
			node['FROM'] = savedData.FROM;
			node['SUBJECT'] = savedData.SUBJECT;
			node['PDF_TEMPLATE_ID'] = savedData.PDF_TEMPLATE_ID;
			node['FIELD_ID'] = savedData.FIELD_ID;
		}
		return node;
	}

	public buildDefaultNode(cell, appLinks, outGroup, savedData) {
		const connectionsTo = this.getConnectionsTo(
			cell.id,
			appLinks,
			outGroup,
			cell.type
		);
		const node = {};
		const nodeName = cell.type.split('.')[1];
		if (nodeName === 'FlowchartStart') {
			node['TYPE'] = 'Start';
		} else if (nodeName === 'FlowchartEnd') {
			node['TYPE'] = 'End';
		} else {
			node['TYPE'] = nodeName;
		}
		node['CONDITIONS'] = [];
		node['ID'] = cell.id;
		node['CONNECTIONS_TO'] = connectionsTo;
		node['NAME'] = cell.attrs.label.text;

		if (savedData) {
			node['CONDITIONS'] = savedData.CONDITIONS;
		}
		return node;
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

	private convertFieldToIds(fields: any[]): any[] {
		fields.forEach((field) => {
			if (field.FIELD.DATA_TYPE.DISPLAY === 'Relationship') {
				field.VALUE = [JSON.stringify(field.VALUE)];
			} else if (field.FIELD.DATA_TYPE.DISPLAY === 'Picklist (Multi-Select)') {
				field.VALUE = field.VALUE;
			} else {
				field.VALUE = [field.VALUE];
			}
			field.FIELD = field.FIELD.FIELD_ID;
		});
		return fields;
	}

	public loadCellValues(workflow: Workflow, module): any[] {
		const cellData = [];
		const allNodes: Node[] = this.getAllNodes(workflow.STAGES);
		allNodes.forEach((node) => {
			const cell: any = {};
			cell.CELL_ID = node.ID;
			cell.STAGE_ID = node['STAGE_ID'];
			cell['VALUE'] = {};
			cell.VALUE.CONDITIONS = node.CONDITIONS ? node.CONDITIONS : [];
			cell.VALUE.NAME = node['NAME'];
			if (node.TYPE === 'SendEmail') {
				cell.VALUE.TO = node['TO'];
				cell.VALUE.FROM = node['FROM'];
				cell.VALUE.BODY = node['BODY'];
				cell.VALUE.SUBJECT = node['SUBJECT'];
			} else if (node.TYPE === 'SendSms') {
				cell.VALUE.TO = node['TO'];
				cell.VALUE.BODY = node['BODY'];
			} else if (node.TYPE === 'MakePhoneCall') {
				cell.VALUE.TO = node['TO'];
				cell.VALUE.BODY = node['BODY'];
			} else if (node.TYPE === 'CreateEntry') {
				cell.VALUE.MODULE = node['MODULE'];
				this.modulesService
					.getModuleById(node['MODULE'])
					.subscribe((relationModule: any) => {
						cell.VALUE.FIELDS = this.transformCreateUpdateEntry(
							node['FIELDS'],
							relationModule.FIELDS
						);
					});
			} else if (node.TYPE === 'UpdateEntry') {
				cell.VALUE.REPLACE = node['REPLACE'];
				cell.VALUE.FIELDS = this.transformCreateUpdateEntry(
					node['FIELDS'],
					module.FIELDS
				);
			} else if (node.TYPE === 'StartEscalation') {
				cell.VALUE.ESCALATION = node['ESCALATION_ID'];
				cell.VALUE.SUBJECT = node['SUBJECT'];
				cell.VALUE.BODY = node['BODY'];
			} else if (node.TYPE === 'Approval') {
				cell.VALUE.APPROVERS = node['APPROVERS'];
				cell.VALUE.TEAMS = node['TEAMS'];
				cell.VALUE.APPROVAL_CONDITION = node['APPROVAL_CONDITION'];
				cell.VALUE.NUMBER_OF_APPROVALS_REQUIRED =
					node['NUMBER_OF_APPROVALS_REQUIRED'];
				cell.VALUE.DISABLE_ENTRY = node['DISABLE_ENTRY'];
				cell.VALUE.NOTIFY_USERS_FOR_APPROVAL =
					node['NOTIFY_USERS_FOR_APPROVAL'];
				cell.VALUE.NOTIFY_USERS_AFTER_APPROVAL =
					node['NOTIFY_USERS_AFTER_APPROVAL'];
			} else if (node.TYPE === 'GeneratePdf') {
				cell.VALUE.PDF_TEMPLATE = node['PDF_TEMPLATE'];
				cell.VALUE.PDF_NAME = node['PDF_NAME'];
			} else if (node.TYPE === 'MicrosoftTeamsNotification') {
				cell.VALUE.CHANNEL_ID = node['CHANNEL_ID'];
				cell.VALUE.FIELDS = this.transformTeams(node['FIELDS'], module.FIELDS);
			} else if (node.TYPE === 'SignatureDocument') {
				cell.VALUE.TO = node['TO'];
				cell.VALUE.FROM = node['FROM'];
				cell.VALUE.SUBJECT = node['SUBJECT'];
				cell.VALUE.PDF_TEMPLATE_ID = node['PDF_TEMPLATE_ID'];
				cell.VALUE.FIELD_ID = node['FIELD_ID'];
			}
			cellData.push(cell);
		});
		return cellData;
	}

	private getAllNodes(stages: Stage[]): Node[] {
		let allNodes: Node[] = [];
		stages.forEach((stage) => {
			const filteredNodes = stage.NODES.filter((node) => {
				node['STAGE_ID'] = stage.STAGE_ID;
				return node.TYPE !== 'Start' && node.TYPE !== 'End';
			});
			allNodes = allNodes.concat(filteredNodes);
		});
		return allNodes;
	}

	private transformCreateUpdateEntry(fields, moduleFields): any[] {
		const trasnformedFields = [];
		fields.forEach((field) => {
			const transformedField: any = {};
			const fieldObj = moduleFields.find((moduleField) => {
				return moduleField.FIELD_ID === field.FIELD;
			});
			transformedField.FIELD = fieldObj;
			if (fieldObj.DATA_TYPE.DISPLAY === 'Relationship') {
				transformedField.VALUE = JSON.parse(field.VALUE[0]);
			} else if (fieldObj.DATA_TYPE.DISPLAY === 'Picklist (Multi-Select)') {
				transformedField.VALUE = field.VALUE;
			} else {
				transformedField.VALUE = field.VALUE[0];
			}
			trasnformedFields.push(transformedField);
		});
		return trasnformedFields;
	}

	private transformTeams(fields, moduleFields): any[] {
		const trasnformedFields = [];
		fields.forEach((field) => {
			const transformedField: any = {};
			const fieldObj = moduleFields.find((moduleField) => {
				return moduleField.FIELD_ID === field;
			});
			transformedField.FIELD = fieldObj;
			trasnformedFields.push(transformedField);
		});
		return trasnformedFields;
	}

	public validateNodesAddedToStages(stages, fileJSON) {
		const allNodes = this.getAllNodes(stages);
		const [cells] = this.getCellsAndApplink(fileJSON);
		if (allNodes.length !== cells.length - 2) {
			return false;
		}
		return true;
	}

	public convertStages(allNodes, stages, cellData): any[] {
		for (let i = 0; i < stages.length; i++) {
			const stage = stages[i];
			let nodes = [];
			if (i === 0) {
				nodes.push(
					allNodes.find((node) => {
						return node.TYPE === 'Start';
					})
				);
			}
			if (i === stages.length - 1) {
				nodes.push(
					allNodes.find((node) => {
						return node.TYPE === 'End';
					})
				);
			}
			const cellIds: any[] = cellData
				.filter((cell) => cell.STAGE_ID === stage.STAGE_ID)
				.map((cell) => {
					return cell.CELL_ID;
				});

			nodes = nodes.concat(
				allNodes.filter((node) => cellIds.indexOf(node.ID) !== -1)
			);

			stage.NODES = nodes;
		}
		return stages;
	}

	public getCellsAndApplink(fileJSON: any) {
		const cells = fileJSON['cells'].filter((cell) => cell.type !== 'app.Link');
		const appLinks = fileJSON['cells'].filter(
			(cell) => cell.type === 'app.Link'
		);
		return [cells, appLinks];
	}

	public getUsersForApproval(users) {
		const userModuleId = this.cacheService.moduleNamesToIds['Users'];
		const userResultMap = [];
		users.forEach((user) => {
			userResultMap.push(this.dataService.getModuleEntry(userModuleId, user));
		});
		return forkJoin(userResultMap);
	}

	public getTeamsForApproval(teams) {
		const teamModuleId = this.cacheService.moduleNamesToIds['Teams'];
		const teamResultMap = [];
		teams.forEach((team) => {
			teamResultMap.push(this.dataService.getModuleEntry(teamModuleId, team));
		});
		return forkJoin(teamResultMap);
	}
}
