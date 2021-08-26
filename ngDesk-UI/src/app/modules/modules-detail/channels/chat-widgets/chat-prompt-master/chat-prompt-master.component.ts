import { Component, OnInit, ViewChild } from '@angular/core';
import { Observable } from 'rxjs';
import { FormGroup, FormArray, FormBuilder, Validators } from '@angular/forms';
import { Field, DATATYPE } from 'src/app/models/field';
import { ConditionsComponent } from './../../../../../custom-components/conditions/conditions.component';
import { ConditionsService } from 'src/app/custom-components/conditions/conditions.service';
import { v4 as uuid } from 'uuid';
import { TranslateService } from '@ngx-translate/core';
import { UsersService } from 'src/app/users/users.service';
import { Router, ActivatedRoute } from '@angular/router';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { SchedulesDetailService } from 'src/app/schedules/schedules-detail/schedules-detail.service';
import { MatDialog } from '@angular/material/dialog';
import { Condition } from 'src/app/models/condition';
import {  ChatPromptApiService } from '@ngdesk/module-api'
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '../../../../../app.globals';

@Component({
	selector: 'app-chat-prompt-master',
	templateUrl: './chat-prompt-master.component.html',
	styleUrls: ['./chat-prompt-master.component.scss'],
})


export class ChatPromptMasterComponent implements OnInit {
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public promptData: any;
	public chatBusinessRuleData$: Observable<{}>;
	public subdomain: string;
	public widgetId: string;
	public moduleId: string;
	public companyUuid: string;
	public errorMessage: string;
	public fileExtension: string;
	public fileType: string;
	public fileName: string;
	public fieldew: Field;
	public conditionsService: ConditionsService;
	public customer;
	public hour;
	public fields = [];
	public pageInfo = [];
	public params;
	public autocompleteValuesFiltered: any[] = [];
	public autocompleteValuesInitial: any[] = [];
	public conditions: ConditionsComponent;
	public promptLoaded: boolean;
	public promptForm: FormGroup;
	public STAGES: FormArray;
	public items: FormArray;
	public FIELDS: FormArray;
	public conditionArray = [];
	public enablefaqs: boolean;
	public timeZones: string[];
	public timezone = '';
	private buttonValue = false;
	public chatToggle: boolean;
	public promptId;
	public channelName: '';
	public newValue: FormArray;
	public workflow = {CONDITIONS: [],STAGES: [], TYPE: '',NAME: '', ORDER: 0}
	public prompt = {
		NAME: '',
		DESCRIPTION: '',
		RUN_TRIGGER: '',
		CONDITIONS: [],
		WORKFLOW: this.workflow
	};
	public actions = [];
	public actionNames: {
		ACTION: string;
		TYPE: string;
	}[] = [
		{
			ACTION: 'Show pop up Message',
			TYPE: 'ShowPopUpMessage',
		},
	];

	public runTriggers = [];

	public dateDatatype: DATATYPE = {
		DISPLAY: 'Number',
		BACKEND: 'Integer',
	};

	public pageDatatype: DATATYPE = {
		DISPLAY: 'Text',
		BACKEND: 'String',
	};

	public condition: Field[];

	constructor(
		public translateService: TranslateService,
		private usersService: UsersService,
		private router: Router,
		private route: ActivatedRoute,
		private formBuilder: FormBuilder,
		private bannerMessageService: BannerMessageService,
		private schedulesDetailService: SchedulesDetailService,
		public dialog: MatDialog,
		private chatPromptApiService: ChatPromptApiService,
		private httpClient: HttpClient,
		private appGlobals: AppGlobals
	) {
		this.subdomain = this.usersService.getSubdomain();
		this.companyUuid = this.usersService.companyUuid;
		this.channelName = this.route.snapshot.params.chatName;
	}

	public ngOnInit() {
		this.promptId = this.route.snapshot.params['promptId'];
		const channelName = this.route.snapshot.params.chatName;
		this.moduleId = this.route.snapshot.params.moduleId;
		this.timeZones = this.schedulesDetailService.timeZones;
		this.runTriggers.push('LOADED_CHAT_WIDGET');
		// this.runTriggers.push('REQUESTS_CHAT');
		// this.runTriggers.push('MESSAGE_SENT');
		//  const nodes = this.formBuilder.group({NODES: node});
		this.promptForm = this.formBuilder.group({
			RUN_TRIGGER: ['', Validators.required],
			CONDITIONS: this.formBuilder.array([]),
			TYPE: ['', Validators.required],
		});
		if(this.promptId !== 'new'){
		const query = `{
			getChatPrompt(channelName: "${channelName}", promptId: "${this.promptId}") {
			  NAME: promptName
			  DESCRIPTION: promptdescription
			  PROMPT_ID: promptId
			  RUN_TRIGGER: runTrigger
			  CONDITIONS: conditions {
				REQUIREMENT_TYPE: requirementType
				CONDITION: condition
				OPERATOR: operator
				CONDITION_VALUE: conditionValue
			  }
			  WORKFLOW: workflow {
				NAME: name
				TYPE: type
				DESCRIPTION: description
				ORDER: order
				STAGES: stages {
				  STAGE_ID: id
				  NAME: name
				  NODES: nodes {
					ID: nodeId
					TYPE: type
					NAME: name
					VALUES: value {
						MESSAGE:  message
					}
					CONNECTIONS_TO: connections {
					  TITLE: title
					  FROM: from
					  TO_NODE: toNode
					}
				  }
				}
			  }
			}
		  }
		  
		  `;
			this.makeGraphQLCall(query).subscribe(
				(queryResponse: any) => {
					this.prompt = queryResponse.getChatPrompt;
					// this.prompt = this.setForm(promptResponse);
					this.setValueToForm(this.prompt);
					this.promptLoaded = true;
				},
				(promptsError: any) => {
					this.errorMessage = promptsError.error.error;
				}
			);
		} else {
			this.promptLoaded = true;
		}
		this.params = {
			fields: {
				fields: this.translateService.instant('FIELD'),
			},
			action: {
				field: this.translateService.instant('ACTION'),
			},
		};

		// this.promptForm
		// 	.get('WORKFLOW').get('STAGES')['controls'][0]
		// 	.get('NODES')
		// 	['controls'][0].get('TYPE')
		// 	.setValue('Start');

		this.pushTimeDateFieldsToConditions(
			'HOUR_OF_DAY',
			this.translateService.instant('HOUR_OF_DAY'),
			this.dateDatatype
		);
		this.pushTimeDateFieldsToConditions(
			'DAY_OF_WEEK',
			this.translateService.instant('DAY_OF_WEEK'),
			this.dateDatatype
		);
		this.pushTimeDateFieldsToConditions(
			'STILL_ON_SITE',
			this.translateService.instant('STILL_ON_SITE'),
			this.dateDatatype
		);
		this.pushTimeDateFieldsToConditions(
			'STILL_ON_PAGE',
			this.translateService.instant('STILL_ON_PAGE'),
			this.dateDatatype
		);

		this.pushPageInfoFieldsToConditions(
			'VISITOR_PAGE_URL',
			this.translateService.instant('VISITOR_PAGE_URL'),
			this.pageDatatype
		);
		this.pushPageInfoFieldsToConditions(
			'VISITOR_PAGE_TITLE',
			this.translateService.instant('VISITOR_PAGE_TITLE'),
			this.pageDatatype
		);
		this.fileName = 'No file chosen';
	}

	public createCondition(type): FormGroup {
		return this.formBuilder.group({
			CONDITION: [''],
			CONDITION_VALUE: [],
			OPERATOR: [''],
			REQUIREMENT_TYPE: type,
		});
	}

	public addCondition(type): void {
		this.newValue.push(this.createCondition(type));
	}
	// public setForm(promptsResponse) {
	// 	const nodes: Node[] = [];
	// 	if (promptsResponse.WORKFLOW != null) {
	// 		promptsResponse.WORKFLOW.NODES.forEach((node) => {
	// 			const plugsArray: Plug[] = [];
	// 			const connectionsToArray: ConnectionsTo[] = [];

	// 			node.PLUGS.forEach((plug) => {
	// 				plugsArray.push(new Plug(plug.ORDER, plug.ID, plug.NAME));
	// 			});
	// 			nodes.push(
	// 				new Node(
	// 					node.POSITION_X,
	// 					node.POSITION_Y,
	// 					node.VALUES,
	// 					node.ID,
	// 					node.TYPE,
	// 					connectionsToArray,
	// 					plugsArray,
	// 					node.NAME
	// 				)
	// 			);
	// 		});
	// 	}
	// 	const WORKFLOW = new Promptworkflow(nodes);
	// 	const CONDITIONS: Condition[] = [];
	// 	const RUN_TRIGGER = promptsResponse.RUN_TRIGGER;
	// 	const NAME = promptsResponse.NAME;
	// 	const DESCRIPTION = promptsResponse.DESCRIPTION;

	// 	if (promptsResponse.CONDITIONS != null) {
	// 		promptsResponse.CONDITIONS.forEach((condition) => {
	// 			if (
	// 				condition.CONDITION !== 'STILL_ON_PAGE' ||
	// 				condition.CONDITION !== 'STILL_ON_SITE'
	// 			) {
	// 				CONDITIONS.push(
	// 					new Condition(
	// 						condition.CONDITION,
	// 						condition.CONDITION_VALUE,
	// 						condition.OPERATOR,
	// 						condition.REQUIREMENT_TYPE
	// 					)
	// 				);
	// 			} else {
	// 				CONDITIONS.push(
	// 					new Condition(
	// 						condition.CONDITION,
	// 						condition.CONDITION_VALUE,
	// 						null,
	// 						condition.REQUIREMENT_TYPE
	// 					)
	// 				);
	// 			}
	// 		});
	// 	}
	// 	const newPrompt = {
	// 		NAME,
	// 		DESCRIPTION,
	// 		RUN_TRIGGER,
	// 		CONDITIONS,
	// 		WORKFLOW,
	// 	};
	// 	return newPrompt;
	// }
	public setValueToForm(prompt) {
		this.actions = this.prompt.WORKFLOW.STAGES[0].NODES;
		const runTrigger = this.prompt.RUN_TRIGGER;
		const name = this.prompt.NAME;
		const description = this.prompt.DESCRIPTION;
		this.promptForm.controls['NAME'].setValue(name);
		this.promptForm.controls['DESCRIPTION'].setValue(description);
		this.promptForm.controls['RUN_TRIGGER'].setValue(runTrigger);
		const conditions = this.promptForm.get('CONDITIONS') as FormArray;
		prompt.CONDITIONS.forEach((condition) => {
			condition.CONDITION = this.conditionArray.find(
				(field) => field.FIELD_ID === condition.CONDITION
			);
			if (
				condition.CONDITION.NAME !== 'STILL_ON_PAGE' ||
				condition.CONDITION.NAME !== 'STILL_ON_SITE'
			) {
				condition.OPERATOR = this.getOperators(condition.CONDITION).find(
					(operator) => operator.BACKEND === condition.OPERATOR
				);
			}
			if (
				(condition.CONDITION.NAME !== 'STILL_ON_PAGE' ||
					condition.CONDITION.NAME !== 'STILL_ON_SITE') &&
				condition.CONDITION.NAME !== 'DAY_OF_WEEK'
			) {
				conditions.push(
					this.formBuilder.group({
						CONDITION: [condition.CONDITION, Validators.required],
						OPERATOR: [condition.OPERATOR, Validators.required],
						CONDITION_VALUE: [condition.CONDITION_VALUE],
						REQUIREMENT_TYPE: [condition.REQUIREMENT_TYPE],
					})
				);
			} else if (condition.CONDITION.NAME === 'DAY_OF_WEEK') {
				let DAY: any;
				switch (condition.CONDITION_VALUE) {
					case '1': {
						DAY = 'Monday';
						this.setDay(condition, DAY);
						break;
					}
					case '2': {
						DAY = 'Tuesday';
						this.setDay(condition, DAY);
						break;
					}
					case '3': {
						DAY = 'Wednesday';
						this.setDay(condition, DAY);
						break;
					}
					case '4': {
						DAY = 'Thursday';
						this.setDay(condition, DAY);
						break;
					}
					case '5': {
						DAY = 'Friday';
						this.setDay(condition, DAY);
						break;
					}
					case '6': {
						DAY = 'Saturday';
						this.setDay(condition, DAY);
						break;
					}
					default: {
						DAY = 'Sunday';
						this.setDay(condition, DAY);
						break;
					}
				}
			} else {
				conditions.push(
					this.formBuilder.group({
						CONDITION: [condition.CONDITION, Validators.required],
						OPERATOR: [null, Validators.required],
						CONDITION_VALUE: [condition.CONDITION_VALUE],
						REQUIREMENT_TYPE: [condition.REQUIREMENT_TYPE],
					})
				);
			}
		});

		const actionsArr = this.actions;
		// loop through actions and set values to form
		// for (const action of prompt.WORKFLOW.NODES) {
		// 	if (action.TYPE !== 'Start') {
		// 		const formAction = this.createAction();
		// 		formAction.get('TYPE').setValue(action.TYPE);
		// 		this.changeAction(formAction);
		// 		formAction.get('VALUES').setValue(action.VALUES);
		// 		actionsArr.push(formAction);
		// 	}
		// }
	}

	public setDay(condition, DAY) {
		const conditions = this.promptForm.get('CONDITIONS') as FormArray;
		conditions.push(
			this.formBuilder.group({
				CONDITION: [condition.CONDITION, Validators.required],
				OPERATOR: [condition.OPERATOR, Validators.required],
				CONDITION_VALUE: [DAY],
				REQUIREMENT_TYPE: [condition.REQUIREMENT_TYPE],
			})
		);
	}

	public createField(): FormGroup {
		return this.formBuilder.group({
			FIELD: ['', [Validators.required]],
			VALUE: ['', [Validators.required]],
		});
	}
	private getOperators(field) {
		return [
			{
				DISPLAY: this.translateService.instant('EQUALS_TO'),
				BACKEND: 'EQUALS_TO',
			},
			{
				DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
				BACKEND: 'NOT_EQUALS_TO',
			},
			{
				DISPLAY: this.translateService.instant('GREATER_THAN'),
				BACKEND: 'GREATER_THAN',
			},
			{
				DISPLAY: this.translateService.instant('LESS_THAN'),
				BACKEND: 'LESS_THAN',
			},
			{
				DISPLAY: this.translateService.instant('CONTAINS'),
				BACKEND: 'CONTAINS',
			},
			{
				DISPLAY: this.translateService.instant('DOES_NOT_CONTAIN'),
				BACKEND: 'DOES_NOT_CONTAIN',
			},
			{
				DISPLAY: this.translateService.instant('REGEX'),
				BACKEND: 'REGEX',
			},
			{
				DISPLAY: null,
				BACKEND: null,
			},
		];
	
	}

	public addAction() {
		const nodeId = `node-id-${uuid()}`;
		const type = 'PromptNode'
		const name = type + '-' + nodeId;
		let connectionsTo = [];
		const connectionTo =  { FROM: 'START',TITLE: 'OUT',TO_NODE: nodeId }; 
		connectionsTo.push(connectionTo);
		const node = {
			VALUES: {MESSAGE: ''},
			CONNECTIONS_TO: connectionsTo,
			ID: nodeId,
			TYPE: type,
			NAME: name,
			CONDITIONS: []

		};
		this.actions.push(node);
	}

	public removeActions(actionIndex) {
		this.actions.splice(actionIndex, 1);
	}


	public savePrompt() {
		// transforms conditions to contain field ids for api call
		this.prompt.CONDITIONS = this.conditionsComponent.transformConditions();
		this.prompt.RUN_TRIGGER = this.promptForm.get('RUN_TRIGGER').value;
		this.prompt.NAME = this.promptForm.get('NAME').value;
		this.prompt.DESCRIPTION = this.promptForm.get('DESCRIPTION').value;
		if(this.prompt.WORKFLOW.STAGES.length === 0){
			let stage  = {
				STAGE_ID: uuid(),
				NAME: 'Default Stage',
				CONDITIONS: [],
				NODES: this.actions
			};
			this.prompt.WORKFLOW.STAGES.push(stage);
			this.prompt.WORKFLOW.NAME = 'Default chat workflow';
			this.prompt.WORKFLOW.CONDITIONS = [];
			this.prompt.WORKFLOW.ORDER = 	0;
			this.prompt.WORKFLOW.TYPE = '';
		}else {
			let currentStage = this.prompt.WORKFLOW.STAGES[0];
			currentStage.NODES = this.actions;
			this.prompt.WORKFLOW.STAGES[0] = currentStage;
		}
		if (this.promptId === 'new') {
			this.chatPromptApiService.postChatPrompt(this.channelName, this.prompt)
				.subscribe(
					(response: any) => {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SAVED_SUCCESSFULLY'),
						});
						this.router.navigate([
							`modules/${this.moduleId}/channels/chat-widgets/Chat`,
						]);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		} else {
			this.chatPromptApiService.putChatPrompt(this.channelName, this.route.snapshot.params.promptId, this.prompt)
				.subscribe(
					(response: any) => {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
						});

						this.router.navigate([
							`modules/${this.moduleId}/channels/chat-widgets/Chat`,
						]);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		}
	}
	public pushTimeDateFieldsToConditions(name, displayLabel, datatype) {
		const field = {
			FIELD_ID: name,
			NAME: name,
			DISPLAY_LABEL: displayLabel,
			DATA_TYPE: datatype,
		};
		this.fields.push(field);
		this.conditionArray.push(field);
	}
	public pushPageInfoFieldsToConditions(name, displayLabel, datatype) {
		const field = {
			FIELD_ID: name,
			NAME: name,
			DISPLAY_LABEL: displayLabel,
			DATA_TYPE: datatype,
		};
		this.pageInfo.push(field);
		this.conditionArray.push(field);
	}
	public ngOnDestroy() {
	}

	public makeGraphQLCall(query: string) {
		return this.httpClient.post(`${this.appGlobals.graphqlUrl}`, query);
	}
}
