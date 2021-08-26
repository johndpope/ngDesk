import { Injectable } from '@angular/core';
import { WorkflowApiService } from '@ngdesk/workflow-api';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
@Injectable({
	providedIn: 'root',
})
export class WorkflowStagesService {
	constructor(
		private workflowApiService: WorkflowApiService,
		private http: HttpClient,
		private globals: AppGlobals
	) {}

	public workflowInstances = new Object();

	public getWorkflow() {
		return `
        <div fxFlex style="margin-bottom: 10px; margin-top: 5px;" [ngStyle]="{display: context.customModulesService.entryWorkflowObject.HAS_WORKFLOW ? 'block' : 'none' }">
		<div *ngIf="context.workflows && context.customModulesService.entryWorkflowObject && context.workflows[context.customModulesService.entryWorkflowObject.WORKFLOW_ID] &&  context.workflows[context.customModulesService.entryWorkflowObject.WORKFLOW_ID].NAME">
		<h3 class="mat-h3" style="margin-left: 30px;margin-bottom: 0px; ">{{context.workflows[context.customModulesService.entryWorkflowObject.WORKFLOW_ID].NAME}}</h3>
		</div>
        <div *ngIf="context.workflows && context.customModulesService.entryWorkflowObject"  fxLayoutAlign="center center" fxLayout="row" style="width: 100%;">
			
            <div fxLayout="row" *ngFor="let key of context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS" style="width: 100%;">
			<div  *ngIf="context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.indexOf(key) === 0" class="gg-pentagon-start" [ngStyle]="{'border-top': context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_STAGE_STATUS.get(key) !== 'executing'  ? '1px solid' : '0px'}"></div>
				<div fxLayoutAlign="center center" fxLayout="row"
                    *ngIf="context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.indexOf(key) + 1 < context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.length && context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_STAGE_STATUS.get(key) === 'executed'"
                    class="gg-pentagon" style="width: 100%;">
                    <div class="tooltip" style="font-weight: bold; color: #000000; font-family: Arial, Helvetica, sans-serif; margin-bottom: 0px; zIndex:2">{{key}}
                    </div>
                    <mat-icon style="color: #7be469; margin-left: 10px;">done</mat-icon>
                </div>
                <div fxLayoutAlign="center center"
                    *ngIf="context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.indexOf(key) + 1 < context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.length && context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_STAGE_STATUS.get(key) === 'pending'"
                    class="gg-pentagon" style="width: 100%;">
                    <div class="tooltip" style="font-weight: bold; color: #000000; font-family: Arial, Helvetica, sans-serif; margin-bottom: 0px; zIndex:2">{{key}}
                    </div>
                </div>
                <div fxLayoutAlign="center center"
                    *ngIf="context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.indexOf(key) + 1 < context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.length && context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_STAGE_STATUS.get(key) === 'executing' "
                    class="gg-pentagon-current" style="width: 100%;">
                    <div class="tooltip" style="font-weight: bold; color: #000000; font-family: Arial, Helvetica, sans-serif; margin-bottom: 0px; zIndex:2; color:white;">{{key}}
                    </div>
                </div>
                <div fxLayoutAlign="center center"
                    *ngIf="context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.indexOf(key) + 1 === context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.length && context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_STAGE_STATUS.get(key) === 'pending'"
                    class="gg-pentagon-end" style="width: 100%;">
                    <div class="tooltip" style="font-weight: bold; color: #000000; font-family: Arial, Helvetica, sans-serif; margin-bottom: 0px; zIndex:2">{{key}}
                    </div>
                </div>
                <div fxLayoutAlign="center center"
                *ngIf="context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.indexOf(key) + 1 === context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.length && context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_STAGE_STATUS.get(key) === 'executing'"
                class="gg-pentagon-end-current" style="width: 100%;">
                <div class="tooltip" style="font-weight: 400; color: #000000; font-family: Arial, Helvetica, sans-serif; margin-bottom: 0px; zIndex:2">{{key}}
                </div>
            </div>
            <div fxLayoutAlign="center center"
            *ngIf="context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.indexOf(key) + 1 === context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS.length && context.customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_STAGE_STATUS.get(key) === 'executed'"
            class="gg-pentagon-end" style="width: 100%;">
            <div class="tooltip" style="font-weight: bold; color: #000000; font-family: Arial, Helvetica, sans-serif; margin-bottom: 0px; zIndex:2">{{key}}
			</div>
			<mat-icon style="color: #7be469; margin-left: 10px;">done</mat-icon>
            </div>
            </div>
        </div>
    </div>`;
	}

	public getWorkflows(entry, module): Observable<any> {
		return this.getWorkflowsUsingGraphql(entry, module['MODULE_ID']).pipe(
			map((response: any) => {
				return response;
			})
		);
	}

	public buildWorkflowStages(
		workflowEntry,
		response,
		entry,
		customModulesService,
		workflowId
	) {
		if (workflowEntry === null && response.STAGES) {
			let object = {};
			response.STAGES.forEach((stage) => {
				object[stage.NAME] = 'executed';
			});
			customModulesService = this.buildNodes(
				entry,
				response,
				customModulesService,
				object
			);
			return customModulesService;
		}
		let object = {};
		let allStagesComplete = false;
		let entryStagesArray = [];
		let allStagesArray = [];
		entryStagesArray.push(workflowEntry.STAGE.ID);
		const workflowStagesArray = response.STAGES;
		workflowStagesArray.forEach((workflow) => {
			allStagesArray.push(workflow.STAGE_ID);
		});
		let differenceArray = allStagesArray.filter(
			(x) => !entryStagesArray.includes(x)
		);
		if (entry.hasOwnProperty('WORKFLOW_STAGES')) {
			customModulesService.entryWorkflowObject.HAS_WORKFLOW = true;
			customModulesService.entryWorkflowObject.WORKFLOW_ID = workflowId;
			if (entry['WORKFLOW_STAGES'] === null) {
				allStagesComplete = true;
			}
			let firstExecutingTraversed = false;
			response.STAGES.forEach((stage) => {
				if (allStagesComplete === true) {
					object[stage.NAME] = 'executed';
				} else {
					if (entryStagesArray.includes(stage.STAGE_ID)) {
						if (
							workflowEntry.WORKFLOW.WORKFLOW_ID === workflowId &&
							stage.STAGE_ID === workflowEntry.STAGE.ID
						) {
							if (workflowEntry.STATUS === 'COMPLETED') {
								object[stage.NAME] = 'executed';
							} else if (
								workflowEntry.STATUS === 'IN_EXECUTION' &&
								firstExecutingTraversed === false
							) {
								object[stage.NAME] = 'executing';
								firstExecutingTraversed = true;
							} else {
								object[stage.NAME] = 'pending';
							}
						}
					} else {
						// TO retain order
						object[stage.NAME] = '';
					}
				}
			});
			if (differenceArray.length > 0) {
				differenceArray.forEach((stageId) => {
					let index = 0;
					for (index; index < response.STAGES.length; index++) {
						if (response.STAGES[index].STAGE_ID === stageId) {
							break;
						}
					}
					if (index + 1 === response.STAGES.length) {
						object[response.STAGES[index].NAME] = 'pending';
					}
					for (let i = index + 1; i < response.STAGES.length; i++) {
						if (object.hasOwnProperty(response.STAGES[i].NAME)) {
							if (
								object[response.STAGES[i].NAME] === 'executed' ||
								object[response.STAGES[i].NAME] === 'executing'
							) {
								object[response.STAGES[index].NAME] = 'executed';
								break;
							} else if (object[response.STAGES[i].NAME] === 'pending') {
								object[response.STAGES[index].NAME] = 'pending';
								break;
							}
						}
					}
				});
			}
			const objectKeys = Object.keys(object);
			objectKeys.forEach((key) => {
				if (object[key] === '') {
					object[key] = 'pending';
				}
			});
			customModulesService = this.buildNodes(
				entry,
				response,
				customModulesService,
				object
			);
		}
		return customModulesService;
	}

	public buildNodes(entry, response, customModulesService, object) {
		if (entry['WORKFLOW_STAGES'] === null) {
			response.STAGES.forEach((stage) => {
				let nodesOfStage = [];
				stage.NODES.forEach((node) => {
					let currentNode = {
						NAME: '',
						STATUS: '',
					};
					currentNode['NAME'] = node.NAME;
					currentNode['STATUS'] = 'done';
					if (node.NAME !== 'Start' && node.NAME !== 'End') {
						nodesOfStage.push(currentNode);
					}
				});
				customModulesService.workflowStageStatus[stage.NAME] = nodesOfStage;
			});
		} else {
			let nodesExecuted = [];
			entry['WORKFLOW_STAGES'].NODES_EXECUTED.forEach((nodes) => {
				nodesExecuted.push(nodes.NODE_ID);
			});
			response.STAGES.forEach((stage) => {
				let nodesOfStage = [];
				stage.NODES.forEach((node) => {
					let currentNode = {
						NAME: '',
						STATUS: '',
					};
					currentNode['NAME'] = node.NAME;
					if (nodesExecuted.includes(node.ID)) {
						currentNode['STATUS'] = 'done';
					} else {
						currentNode['STATUS'] = 'not done';
					}
					if (node.NAME !== 'Start' && node.NAME !== 'End') {
						nodesOfStage.push(currentNode);
					}
				});
				customModulesService.workflowStageStatus[stage.NAME] = nodesOfStage;
			});
		}
		customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_STAGE_STATUS =
			new Map(Object.entries(object));
		customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_KEYS = [
			...customModulesService.entryWorkflowObject.ENTRY_WORKFLOW_STAGE_STATUS.keys(),
		];
		return customModulesService;
	}

	public workflowSwitch(
		entry,
		customModulesService,
		workflowId,
		moduleId,
		workflows,
		workflowInstances
	) {
		let workflow = workflows[workflowId];
		let instance = workflowInstances[workflowId];
		entry['WORKFLOW_STAGES'] = workflowInstances[workflowId];

		this.buildWorkflowStages(
			entry['WORKFLOW_STAGES'],
			workflows[workflowId],
			entry,
			customModulesService,
			workflowId
		);
	}

	public getWorkflowsUsingGraphql(entry, moduleId) {
		const graphqlQuery = `{
			WORKFLOW_INSTANCE: getWorkflowInstance(
				moduleId:"${moduleId}",
				dataId: "${entry['DATA_ID']}"
			  ){  
				WORKFLOW: workflow {
					WORKFLOW_ID: id
					TYPE: type
					NAME: name
					DESCRIPTION: description
					DISPLAY_ON_ENTRY: displayOnEntry
					ORDER: order
					RAPID_UI_PAYLOAD: rapidUiPayload
					MODULE: module {
					  MODULE_ID: moduleId
					}
	                CONDITIONS: conditions {
						REQUIREMENT_TYPE: requirementType
						CONDITION: condition {
						  FIELD_ID: fieldId
						}
						OPERATOR: operator
						CONDITION_VALUE: conditionValue
					  }
					  DATE_CREATED: dateCreated
					  DATE_UPDATED: dateUpdated
	                  STAGES: stages {
						STAGE_ID: id
						NAME: name
						CONDITIONS: conditions {
						  REQUIREMENT_TYPE: requirementType
						  CONDITION: condition {
							FIELD_ID: fieldId
						  }
						  OPERATOR: operator
						  CONDITION_VALUE: conditionValue
						}
						NODES: nodes {
						  ID: nodeId
						  TYPE: type
						  NAME: name
						  CONDITIONS: preConditions {
							REQUIREMENT_TYPE: requirementType
							CONDITION: condition {
							  FIELD_ID: fieldId
							}
							OPERATOR: operator
							CONDITION_VALUE: conditionValue
						  }
						  CONNECTION_TO: connections{
								TO_NODE: toNode{
									nodeId
								}
								FROM: from
								TITLE: title
								ON_ERROR: onError
						  }
						}
					  }
					}
					STAGE: stage {
					  ID: id
					}
					DATE_CREATED: dateCreated
					DATE_UPDATED: dateUpdated
					STATUS: status
					instanceId
					NODE: node {
					  NODE_ID: nodeId
					}
					NODES_EXECUTED: nodesExecuted {
					  NODE_ID: nodeId
					}
				  }
				}`;
		return this.http.post(`${this.globals.graphqlUrl}`, graphqlQuery);
	}
}
