import {
	ChangeDetectorRef,
	Component,
	OnDestroy,
	OnInit,
	ViewChild,
} from '@angular/core';
import {
	FormArray,
	FormBuilder,
	FormControl,
	FormGroup,
	Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { v4 as uuid } from 'uuid';

import { Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { ChannelsService } from '../../../../channels/channels.service';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { CompaniesService } from '../../../../companies/companies.service';
import { ConditionsComponent } from '../../../../custom-components/conditions/conditions.component';
import { FilterRuleOptionPipe } from '../../../../custom-components/conditions/filter-rule-option/filter-rule-option.pipe';
import { Condition } from '../../../../models/condition';
import { Escalation } from '../../../../models/escalation';
import { AdditionalFields } from '../../../../models/additional-field';
import { Field } from '../../../../models/field';
import {
	ConnectionsTo,
	ModuleWorkflow,
	Node,
	Plug,
	Values,
	Workflow,
} from '../../../../models/module-workflow';
import { User } from '../../../../models/role';
import { UsersService } from '../../../../users/users.service';
import { ModulesService } from '../../../modules.service';

import { EscalationApiService } from '@ngdesk/escalation-api';

@Component({
	selector: 'app-triggers-detail',
	templateUrl: './triggers-detail.component.html',
	styleUrls: ['./triggers-detail.component.scss'],
})
export class TriggersDetailComponent implements OnInit, OnDestroy {
	public errorMessage = '';
	public escalations: Escalation[] = [];
	public triggerLoaded = false;
	public fields: Field[];
	public bodyFields: Field[];
	public users: User[] = [];
	public smsUsers: any[] = [];
	public variables: Field[];
	public params;
	private variablesInitial: Field[];
	public relationshipObj: { module: string; data: any[] }[] = [];
	private _destroyed$ = new Subject();
	public trigger: ModuleWorkflow = new ModuleWorkflow(
		'',
		'',
		[],
		{
			DISPLAY: '',
			BACKEND: '',
		},
		new Workflow([
			new Node(
				'100px',
				'100px',
				new Values(),
				`node-id-${uuid()}`,
				'Start',
				[],
				[new Plug(0, `output-plug-${uuid()}`, 'OUT')],
				'Start'
			),
		])
	);
	public actionNames: {
		ACTION: string;
		TYPE: string;
	}[] = [
		{
			ACTION: 'Make Phone Call',
			TYPE: 'MakePhoneCall',
		},
		{
			ACTION: 'Send Email',
			TYPE: 'SendEmail',
		},
		{
			ACTION: 'Send SMS',
			TYPE: 'SendSms',
		},
		{
			ACTION: 'Start Escalation',
			TYPE: 'StartEscalation',
		},
		{
			ACTION: 'Stop Escalation',
			TYPE: 'StopEscalation',
		},
		{
			ACTION: 'Update Entry',
			TYPE: 'UpdateEntry',
		},
		{
			ACTION: 'Delete Entry',
			TYPE: 'DeleteEntry',
		}
	];
	public triggerTypes = [
		{
			DISPLAY: this.translateService.instant('CREATE'),
			BACKEND: 'CREATE',
		},
		{
			DISPLAY: this.translateService.instant('UPDATE'),
			BACKEND: 'UPDATE',
		},
		{
			DISPLAY: this.translateService.instant('CREATE_OR_UPDATE'),
			BACKEND: 'CREATE_OR_UPDATE',
		},
		{
			DISPLAY: this.translateService.instant('BUTTON'),
			BACKEND: 'BUTTON',
		},
		{
			DISPLAY: this.translateService.instant('FORM_OR_CATALOGUE'),
			BACKEND: 'FORM_OR_CATALOGUE',
		},
	];
	public triggerForm: FormGroup;
	public items: FormArray;
	public additionalFields = [];
	public FIELDS: FormArray;
	public channellist: any;
	public channels: any = [];
	public emailChannels: any = [];
	public fieldlist: any;
	public fieldId: any;
	public relatedFields = {};
	public moduleId: String;
	public moduleMap = new Map<String, Object>();
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;

	constructor(
		private cdr: ChangeDetectorRef,
		private formBuilder: FormBuilder,
		private route: ActivatedRoute,
		private router: Router,
		private modulesService: ModulesService,
		public channelsService: ChannelsService,
		private companiesService: CompaniesService,
		private usersService: UsersService,
		private translateService: TranslateService,
		private bannerMessageService: BannerMessageService,
		private escalationApiService: EscalationApiService
	) {}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		const moduleWorkflowId = this.route.snapshot.params['moduleWorkflowId'];
		const relationshipModules = [];
		const lowestOrder = this.modulesService.minimumTiggerOrder;
		this.additionalFields.push(
			new AdditionalFields(
				'TRIGGER_TYPE',
				'TYPE',
				'list',
				'BACKEND',
				'TYPE',
				this.triggerTypes,
				'DISPLAY',
				'triggerType'
			),
			new AdditionalFields(
				'ORDER',
				'ORDER',
				'number',
				null,
				'ORDER',
				null,
				null,
				'MINIMUM_ORDER'
			)
		);
		this.params = {
			action: { field: this.translateService.instant('ACTION') },
			triggerType: { field: this.translateService.instant('TRIGGER_TYPE') },
			escalation: { field: this.translateService.instant('ESCALATION') },
			to: { field: this.translateService.instant('TO') },
			subject: { field: this.translateService.instant('SUBJECT') },
			body: { field: this.translateService.instant('BODY') },
			field: { field: this.translateService.instant('FIELD') },
			value: { field: this.translateService.instant('VALUE') },
			order: { field: this.translateService.instant('ORDER') },
		};
		this.modulesService.getModules().subscribe((response: any) => {
			response.MODULES.forEach((allModule) => {
				this.moduleMap.set(allModule.MODULE_ID, allModule);
				this.relatedFields[allModule.MODULE_ID] = allModule.FIELDS.filter(
					(field) =>
						field.DATA_TYPE.DISPLAY === 'Relationship' &&
						field.RELATIONSHIP_TYPE !== 'Many to Many' &&
						field.NAME !== 'LAST_UPDATED_BY' &&
						field.NAME !== 'CREATED_BY'
				);
				this.relatedFields[allModule.MODULE_ID].forEach((field) => {
					if (
						field.PRIMARY_DISPLAY_FIELD !== null &&
						field.DISPLAY_LABEL === 'Account'
					) {
						this.modulesService
							.getModuleById(field.MODULE)
							.subscribe((relationModule: any) => {
								const primaryDisplayField = relationModule.FIELDS.find(
									(tempField) =>
										tempField.FIELD_ID === field.PRIMARY_DISPLAY_FIELD
								);
								field.DISPLAY_LABEL = primaryDisplayField.DISPLAY_LABEL;
							});
					}
				});
			});
			const module = this.moduleMap.get(this.route.snapshot.params['moduleId']);
			this.fieldlist = module['FIELDS'];
			this.fieldlist.forEach((element) => {
				if (element.NAME === 'CHANNEL') {
					this.fieldId = element.FIELD_ID;
				}
			});
			const modules = this.moduleMap.get(this.moduleId);
			modules['FIELDS'].forEach((field) => {
				if (field.DATA_TYPE.DISPLAY.toLocaleLowerCase() === 'relationship') {
					if (!relationshipModules.includes(field.MODULE)) {
						relationshipModules.push(field.MODULE);
					}
				}
			});

			relationshipModules.forEach((relModule) => {
				this.modulesService
					.getEntries(relModule)
					.pipe(takeUntil(this._destroyed$))
					.subscribe(
						(entries: any) => {
							/* Checking USER_UUID here to identify users module*/
							if (entries.DATA[0].hasOwnProperty('USER_UUID')) {
								entries.DATA.forEach((entry) => {
									entry.NAME = `${entry.FIRST_NAME} ${entry.LAST_NAME}`;
									this.users = entries.DATA;
									this.smsUsers = JSON.parse(JSON.stringify(this.users));
									this.smsUsers.push({
										NAME: 'Requestor',
										FIRST_NAME: 'Requestor',
										LAST_NAME: '',
										DATA_ID: '{{InputMessage.REQUESTOR.PHONE_NUMBER}}',
									});
								});
							}
							this.relationshipObj.push({
								module: relModule,
								data: entries.DATA,
							});
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.error,
							});
						}
					);
			});
			/********************/
			const modulesResponse = this.moduleMap.get(this.moduleId);
			this.fields = modulesResponse['FIELDS'];
			this.variables = this.convertVariables(
				JSON.parse(JSON.stringify(this.fields))
			);
			this.variablesInitial = JSON.parse(JSON.stringify(this.variables));
			this.bodyFields = modulesResponse['FIELDS'].slice();
			let discussionFieldLatest;
			this.bodyFields.forEach((element, index) => {
				if (element.DATA_TYPE.DISPLAY === 'Discussion') {
					discussionFieldLatest = JSON.parse(
						JSON.stringify(this.bodyFields.slice(index, index + 1)[0])
					);
				}
			});
			if (discussionFieldLatest !== undefined) {
				const label = discussionFieldLatest.DISPLAY_LABEL;
				discussionFieldLatest.DISPLAY_LABEL = label + ' - Latest';
				const name = discussionFieldLatest.NAME;
				discussionFieldLatest.NAME = name + '.LATEST';
				this.bodyFields.push(discussionFieldLatest);
			}

			if (moduleWorkflowId !== 'new') {
				this.modulesService
					.getWorkflow(this.moduleId, moduleWorkflowId)
					.pipe(take(1), takeUntil(this._destroyed$))
					.subscribe(
						(workflowResponse: any) => {
							this.trigger = this.convertTrigger(workflowResponse);
							this.trigger.workflow.nodes.forEach((node, nodeIndex) => {
								if (node.type === 'MakePhoneCall' || node.type === 'SendSms') {
									// transform user ids to user objects for front end display purposes
									this.trigger.workflow.nodes[nodeIndex].values['TO'] =
										this.transformObjects(
											this.trigger.workflow.nodes[nodeIndex].values['TO'],
											// this.usersInitial,
											this.smsUsers,
											'DATA_ID'
										);
								}
								if (node.type === 'UpdateEntry') {
									this.trigger.workflow.nodes[nodeIndex].values[
										'FIELDS'
									].forEach((fieldId) => {
										const fieldObj = this.fields.find(
											(field) => field.FIELD_ID === fieldId.FIELD
										);
										fieldId.FIELD = fieldObj;
										const valueArr = fieldId.VALUE;
										const value = valueArr[0];
										if (fieldId.FIELD.DATA_TYPE.DISPLAY === 'Relationship') {
											const relationshipField = this.relationshipObj.find(
												(v) => fieldId.FIELD.MODULE === v.module
											).data;

											// get primary display field name from related module
											const relatedModuleFields = this.moduleMap.get(
												fieldId.FIELD.MODULE
											)['FIELDS'];
											const relatedField = relatedModuleFields.find(
												(fieldFound) =>
													fieldFound.FIELD_ID ===
													fieldId.FIELD.PRIMARY_DISPLAY_FIELD
											);
											fieldId.FIELD['PRIMARY_DISPLAY_FIELD_NAME'] =
												relatedField.NAME;

											if (fieldId.FIELD.RELATIONSHIP_TYPE === 'Many to Many') {
												fieldId.VALUE = [];
												for (const val of relationshipField) {
													if (valueArr.indexOf(val.DATA_ID) !== -1) {
														fieldId.VALUE.push(val);
													}
												}
											} else {
												const fieldValue = this.transformObjects(
													value,
													relationshipField,
													'DATA_ID'
												);
												// associate primary display field with entry in order to display properly
												fieldId.VALUE = {
													DATA: fieldValue,
													ITEM: fieldId.FIELD,
												};
											}
										} else {
											fieldId.VALUE = value;
										}
									});
								}
							});
							this.trigger.workflow.nodes.forEach((element) => {
								if (element.type === 'SendEmail') {
									if (element.values != null) {
										// let body = element.values.BODY ;
										if (element.values.body) {
											const re = /<br\/>/gi;
											let body = element.values.body;
											body = body.replace(re, '\n');
											element.values.body = body;
										}
									}
								}
							});
							this.setValueToForm(this.trigger, moduleWorkflowId);
							this.resetOptions();
							this.triggerLoaded = true;
						},
						(workflowError: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: workflowError.error.error,
							});
						}
					);
			} else {
				this.triggerLoaded = true;
			}

			this.cdr.detectChanges();

			/**********************/
		});
		this.channelsService
			.getAllChannels(this.route.snapshot.params['moduleId'])
			.subscribe(
				(response: any) => {
					this.channellist = response.CHANNELS;
					response.CHANNELS.forEach((element) => {
						this.channels.push({
							value: element.NAME,
							viewValue: element.NAME,
						});
					});
				},
				(error) => {
					console.log(error);
				}
			);
		this.channelsService
			.getFromEmail(this.route.snapshot.params['moduleId'])
			.subscribe(
				(response: any) => {
					if (response.TOTAL_RECORDS === 0) {
						this.emailChannels.push({
							EMAIL_ADDRESS: `support@${this.usersService.getSubdomain()}.ngdesk.com`,
						});
					} else {
						this.emailChannels = response.CHANNELS.filter(
							(channel) => channel.IS_VERIFIED === true
						);
					}
				},
				(error) => {
					console.log(error);
				}
			);

		this.triggerForm = this.formBuilder.group({
			CONDITIONS: this.formBuilder.array([]),
			TYPE: ['', Validators.required],
			ORDER: [
				moduleWorkflowId === 'new' ? lowestOrder : null,
				Validators.min(0),
			],
			WORKFLOW: this.formBuilder.group({
				NODES: this.formBuilder.array([this.createAction()]),
			}),
		});

		// initialize the first node type to Start
		this.triggerForm
			.get('WORKFLOW')
			.get('NODES')
			['controls'][0].get('TYPE')
			.setValue('Start');

		// gets list of escalation policies to select from
		this.escalationApiService.getEscalations().subscribe(
			(escalationsResponse: any) => {
				this.escalations = escalationsResponse.content;
			},
			(escalationsError: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: escalationsError.error.error,
				});
			}
		);
	}

	// replace Ids -> Objects
	private transformObjects(id, initialArray, key) {
		for (const obj of initialArray) {
			if (obj[key] === id) {
				return obj;
			}
		}
	}

	// This function will add actions
	public addActions() {
		const nodeId = `node-id-${uuid()}`;
		const newPos = 100 + 50 * this.trigger.workflow.nodes.length;
		this.trigger.workflow.nodes[
			this.trigger.workflow.nodes.length - 1
		].connectionsTo[0] = new ConnectionsTo('START', 'OUT', nodeId);

		this.trigger.workflow.nodes.push(
			new Node(
				`${newPos}px`,
				`${newPos}px`,
				new Values(),
				nodeId,
				'',
				[],
				[
					new Plug(0, `input-plug-${uuid()}`, 'INPUT'),
					new Plug(1, `output-plug-${uuid()}`, 'OUT'),
				],
				''
			)
		);

		// Adding the workflow to trigger form
		this.items = this.triggerForm.get('WORKFLOW').get('NODES') as FormArray;

		// Action are added at the beginning of List
		this.items.insert(1, this.createAction());
	}

	private createAction() {
		return this.formBuilder.group({
			NAME: '',
			VALUES: this.formBuilder.group({}),
			TYPE: ['', Validators.required],
		});
	}

	private isUnique(arr, name, length) {
		if (arr.find((node) => node.NAME === `${name} ${length}`)) {
			return false;
		}
		return true;
	}

	private createUniqueName(nodes, name) {
		let length = nodes.length;
		while (!this.isUnique(nodes, name, length)) {
			length++;
		}
		return `${name} ${length}`;
	}

	private validateEmail(c: FormControl) {
		// tslint:disable-next-line:max-line-length
		const EMAIL_REGEXP =
			/^(?=.{1,254}$)(?=.{1,64}@)[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+(\.[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$/;

		return EMAIL_REGEXP.test(c.value) ||
			c.value.toLowerCase().indexOf('inputmessage') !== -1
			? null
			: {
					invalidEmail: true,
			  };
	}
	public removeAllControls(formGroup: FormGroup) {
		const formControlNames = Object.keys(formGroup.controls);
		formControlNames.forEach((name) => {
			formGroup.removeControl(name);
		});
	}

	// Creates dynamic form elements depending on which action selected
	public changeAction(action) {
		const values = action.get('VALUES') as FormGroup;
		switch (action.get('TYPE').value) {
			case 'MakePhoneCall': {
				this.actionNames;
				const phoneCallNodes = this.triggerForm.value.WORKFLOW.NODES.filter(
					function (node) {
						return node.TYPE === 'MakePhoneCall';
					}
				);
				const nodeName = this.createUniqueName(phoneCallNodes, 'MakePhoneCall');
				action.get('NAME').setValue(nodeName);
				this.removeAllControls(values);
				values.addControl('TO', new FormControl('', Validators.required));
				values.get('TO').valueChanges.subscribe((selectedValue) => {
					if (selectedValue) {
						this.filterVariables(selectedValue, 'users');
					}
				});
				values.addControl('BODY', new FormControl('', Validators.required));
				break;
			}

			case 'SendEmail': {
				const emailNodes = this.triggerForm.value.WORKFLOW.NODES.filter(
					function (node) {
						return node.TYPE === 'SendEmail';
					}
				);
				const nodeName = this.createUniqueName(emailNodes, 'SendEmail');
				action.get('NAME').setValue(nodeName);
				this.removeAllControls(values);
				values.addControl(
					'FROM',
					new FormControl(
						`support@${this.usersService.getSubdomain()}.ngdesk.com`,
						Validators.required
					)
				);
				values.addControl(
					'TO',
					new FormControl('', [Validators.required, this.validateEmail])
				);
				values.get('TO').valueChanges.subscribe((selectedValue) => {
					if (selectedValue) {
						this.filterVariables(selectedValue, 'conditions');
					}
				});
				values.addControl('SUBJECT', new FormControl('', Validators.required));
				values.get('SUBJECT').valueChanges.subscribe((selectedValue) => {
					if (selectedValue) {
						this.filterVariables(selectedValue, 'conditions');
					}
				});
				values.addControl('BODY', new FormControl('', Validators.required));
				break;
			}

			case 'SendSms': {
				const smsNodes = this.triggerForm.value.WORKFLOW.NODES.filter(function (
					node
				) {
					return node.TYPE === 'SendSms';
				});
				this.removeAllControls(values);
				const nodeName = this.createUniqueName(smsNodes, 'SendSms');
				action.get('NAME').setValue(nodeName);
				values.addControl('TO', new FormControl('', Validators.required));
				values.get('TO').valueChanges.subscribe((selectedValue) => {
					if (selectedValue) {
						this.filterVariables(selectedValue, 'users');
					}
				});
				values.addControl('BODY', new FormControl('', Validators.required));
				break;
			}

			case 'StartEscalation': {
				const startEscalationNodes =
					this.triggerForm.value.WORKFLOW.NODES.filter(function (node) {
						return node.TYPE === 'StartEscalation';
					});
				const nodeName = this.createUniqueName(
					startEscalationNodes,
					'StartEscalation'
				);
				action.get('NAME').setValue(nodeName);
				this.removeAllControls(values);
				values.addControl(
					'ESCALATION',
					new FormControl('', Validators.required)
				);
				values.addControl('SUBJECT', new FormControl('', Validators.required));
				values.get('SUBJECT').valueChanges.subscribe((selectedValue) => {
					if (selectedValue) {
						this.filterVariables(selectedValue, 'conditions');
					}
				});
				values.addControl('BODY', new FormControl('', Validators.required));
				// TODO: need to add the start escalation node id
				break;
			}

			case 'StopEscalation': {
				const stopEscalationNodes =
					this.triggerForm.value.WORKFLOW.NODES.filter(function (node) {
						return node.TYPE === 'StopEscalation';
					});
				const nodeName = this.createUniqueName(
					stopEscalationNodes,
					'StopEscalation'
				);
				action.get('NAME').setValue(nodeName);
				// TODO: need to add the stop escalation node id
				break;
			}

			case 'UpdateEntry': {
				const updateEntryNodes = this.triggerForm.value.WORKFLOW.NODES.filter(
					function (node) {
						return node.TYPE === 'UpdateEntry';
					}
				);
				this.removeAllControls(values);
				// ADDING FIELDS
				values.addControl('FIELDS', this.formBuilder.array([]));
				// DATA_ID
				const dataId = '{{inputMessage.DATA_ID}}';
				const nodeName = this.createUniqueName(updateEntryNodes, 'UpdateEntry');
				action.get('NAME').setValue(nodeName);
				// REQUIRED FIELDS FOR UPDATE ENTRY
				values.addControl(
					'MODULE',
					new FormControl(this.route.snapshot.params['moduleId'])
				);
				values.addControl('ENTRY_ID', new FormControl(dataId));
				values.get('MODULE').disable();
				values.get('ENTRY_ID').disable();
				this.FIELDS = values.get('FIELDS') as FormArray;
				break;
			}

			case 'DeleteEntry': {
				const deleteEntryNodes = this.triggerForm.value.WORKFLOW.NODES.filter(
					function (node) {
						return node.TYPE === 'DeleteEntry';
					}
				);
				this.removeAllControls(values);
				const nodeName = this.createUniqueName(deleteEntryNodes, 'DeleteEntry');
				action.get('NAME').setValue(nodeName);
				break;
			}
		}
	}

	// This function will remove actions
	public removeActions(actionIndex) {
		// if not last node reconnect node before with node after
		// if last node, clear previous node's connections to

		if (
			this.trigger.workflow.nodes.length > 2 &&
			actionIndex !== this.trigger.workflow.nodes.length - 1
		) {
			const prevNode = this.trigger.workflow.nodes[actionIndex - 1];
			const nextNode = this.trigger.workflow.nodes[actionIndex + 1];

			prevNode.connectionsTo[0].toNode = nextNode.id;
		} else if (actionIndex === this.trigger.workflow.nodes.length - 1) {
			this.trigger.workflow.nodes[actionIndex - 1].connectionsTo = [];
		}
		this.trigger.workflow.nodes.splice(actionIndex, 1);
		const actionsArr = this.triggerForm
			.get('WORKFLOW')
			.get('NODES') as FormArray;
		actionsArr.removeAt(actionIndex);
	}

	public removeField(field, fieldIndex) {
		field.removeAt(fieldIndex);
	}

	// cast api response to custom module workflow data type
	public convertTrigger(trigger: any): ModuleWorkflow {
		const nodes: Node[] = [];
		trigger.WORKFLOW.NODES.forEach((node) => {
			const plugsArray: Plug[] = [];
			const connectionsToArray: ConnectionsTo[] = [];
			node.CONNECTIONS_TO.forEach((connectionsTo) => {
				connectionsToArray.push(
					new ConnectionsTo(
						connectionsTo.TITLE,
						connectionsTo.FROM,
						connectionsTo.TO_NODE
					)
				);
			});
			node.PLUGS.forEach((plug) => {
				plugsArray.push(new Plug(plug.ORDER, plug.ID, plug.NAME));
			});
			nodes.push(
				new Node(
					node.POSITION_X,
					node.POSITION_Y,
					node.VALUES,
					node.ID,
					node.TYPE,
					connectionsToArray,
					plugsArray,
					node.NAME
				)
			);
		});
		const workflow = new Workflow(
			nodes,
			trigger.WORKFLOW.LAST_UPDATED_BY,
			trigger.WORKFLOW.DATE_UPDATED
		);

		const conditions: Condition[] = [];
		trigger.CONDITIONS.forEach((condition) => {
			if (condition.CONDITION === this.fieldId) {
				this.channellist.forEach((element) => {
					if (element.ID === condition.CONDITION_VALUE) {
						conditions.push(
							new Condition(
								condition.CONDITION,
								element.NAME,
								condition.OPERATOR,
								condition.REQUIREMENT_TYPE
							)
						);
					}
				});
			} else {
				conditions.push(
					new Condition(
						condition.CONDITION,
						condition.CONDITION_VALUE,
						condition.OPERATOR,
						condition.REQUIREMENT_TYPE
					)
				);
			}
		});

		const triggerType = this.triggerTypes.find(
			(type) => type.BACKEND === trigger.TYPE
		);

		const newTrigger = new ModuleWorkflow(
			trigger.NAME,
			trigger.DESCRIPTION,
			conditions,
			triggerType,
			workflow,
			trigger.WORKFLOW_ID,
			trigger.DATE_CREATED,
			trigger.LAST_UPDATED_BY,
			trigger.DATE_UPDATED,
			trigger.ORDER
		);
		return newTrigger;
	}

	// This function will set value to triggerForm
	public setValueToForm(trigger, moduleWorkflowId) {
		this.triggerForm.controls['NAME'].setValue(trigger.NAME);
		this.triggerForm.controls['DESCRIPTION'].setValue(trigger.DESCRIPTION);
		this.triggerForm.controls['TYPE'].setValue(trigger.TYPE.DISPLAY);
		this.triggerForm.controls['ORDER'].setValue(trigger.ORDER);
		this.modulesService
			.getWorkflow(this.moduleId, moduleWorkflowId)
			.subscribe((data: any) => {
				this.triggerForm.controls.TYPE.setValue(data.TYPE);
			});
		const actionsArr = this.triggerForm
			.get('WORKFLOW')
			.get('NODES') as FormArray;
		// loop through actions and set values to form
		for (const action of trigger.WORKFLOW.NODES) {
			if (action.TYPE !== 'Start') {
				const formAction = this.createAction();
				formAction.get('TYPE').setValue(action.TYPE);
				this.changeAction(formAction);
				formAction.get('NAME').setValue(action.NAME);
				if (action.TYPE === 'UpdateEntry') {
					action.VALUES.FIELDS.forEach((element) => {
						this.FIELDS.controls.push(this.createField());
						delete element.OPERATOR;
						delete element.ATTACHMENTS;
					});
				}
				formAction.get('VALUES').setValue(action.VALUES);
				actionsArr.push(formAction);
			}
		}
	}

	private convertVariables(fields) {
		fields.forEach((field) => {
			if (
				// tslint:disable-next-line: prefer-switch
				field.NAME === 'REQUESTOR' ||
				field.NAME === 'ASSIGNEE' ||
				field.NAME === 'CREATED_BY' ||
				field.NAME === 'LAST_UPDATED_BY'
			) {
				field.NAME = `{{InputMessage.${field.NAME}.EMAIL_ADDRESS}}`;
			} else {
				field.NAME = `{{InputMessage.${field.NAME}}}`;
			}
		});
		return fields;
	}

	public filterVariables(event, filterType, values?) {
		let input = event;
		// if has field ID it means the user slected from the dropdown and didnt
		// type it manually
		switch (filterType) {
			case 'conditions':
				if (event.hasOwnProperty('FIELD_ID')) {
					input = event.DISPLAY_LABEL;
				}
				this.variables = new FilterRuleOptionPipe().transform(
					this.variablesInitial,
					input.toLowerCase(),
					'conditions'
				);
				break;
			case 'picklistValues':
				values = new FilterRuleOptionPipe().transform(
					values,
					input.toLowerCase(),
					'picklistValues'
				);
				break;
		}
	}

	public displayFullName(value) {
		if (value) {
			if (value.FIRST_NAME === 'Requestor') {
				return `${value.DATA_ID}`;
			}
			return `${value.FIRST_NAME} ${value.LAST_NAME}`;
		}
	}
	public displayFieldName(value) {
		if (value) {
			return `${value.DISPLAY_LABEL}`;
		}
	}

	public displayName(value) {
		if (value.DATA) {
			return value.DATA[value.ITEM.PRIMARY_DISPLAY_FIELD_NAME];
		}
	}

	public concatenateVariables(action, field, mainItem?, subItem?, subSubItem?) {
		let concatVariable = mainItem.NAME;
		if (subItem) {
			concatVariable += `.${subItem.NAME}`;
			if (subSubItem) {
				concatVariable += `.${subSubItem.NAME}`;
			}
		}
		this.insertBodyVariable(action, field, concatVariable);
	}

	// BODYFORFIELD USED FOR UPDATE ENTRY NODE
	public insertBodyVariable(action, field, relatedVars?, bodyForField?) {
		let body;
		if (!bodyForField) {
			body = action.get('VALUES').get('BODY') as FormControl;
		} else {
			body = action.get('VALUE') as FormControl;
		}
		let fieldVar = field.NAME;
		if (relatedVars) {
			fieldVar += `.${relatedVars}`;
		}
		if (body === null) {
			body.setValue(` {{InputMessage.${fieldVar}}} `);
		} else {
			body.setValue(`${body.value} {{InputMessage.${fieldVar}}} `);
		}
	}

	// after subject is selected reset the autocomplete values
	public resetOptions() {
		this.variables = this.variablesInitial;
	}

	public save() {
		if (this.triggerForm.valid) {
			// transforms conditions to contain field ids for api call
			this.trigger.conditions = this.conditionsComponent.transformConditions();
			this.trigger.name = this.triggerForm.value['NAME'];
			this.trigger.description = this.triggerForm.value['DESCRIPTION'];
			this.trigger.type = this.triggerForm.value['TYPE'];
			this.trigger.order = this.triggerForm.value['ORDER'];
			const actionsArr = this.triggerForm
				.get('WORKFLOW')
				.get('NODES') as FormArray;

			for (let i = 1; i < this.trigger.workflow.nodes.length; i++) {
				this.trigger.workflow.nodes[i].name =
					this.triggerForm.value['WORKFLOW']['NODES'][i]['NAME'];
				this.trigger.workflow.nodes[i].type =
					this.triggerForm.value['WORKFLOW']['NODES'][i]['TYPE'];
				const actionValues = actionsArr.at(i).get('VALUES') as FormGroup;
				this.trigger.workflow.nodes[i].values = actionValues.getRawValue();
			}
			const moduleWorkflowId = this.route.snapshot.params['moduleWorkflowId'];
			this.trigger.workflow.nodes.forEach((element) => {
				if (element.type === 'SendEmail') {
					if (element.values != null) {
						// let body = element.values.BODY ;
						if (element.values.body) {
							const re = /\n/gi;
							let body = element.values.body;
							body = body.replace(re, '<br/>');
							element.values.body = body;
						}
					}
				}
			});
			const triggerObj = JSON.parse(JSON.stringify(this.trigger)); // doing this so if we get backend error fields will not be blank
			this.trigger.workflow.nodes.forEach((node, nodeIndex) => {
				if (node.type === 'MakePhoneCall' || node.type === 'SendSms') {
					// tranform user objects to data id for api call
					triggerObj.WORKFLOW.NODES[nodeIndex].VALUES.TO =
						this.trigger.workflow.nodes[nodeIndex].values['TO']['DATA_ID'];
				}
				if (node.type === 'UpdateEntry') {
					triggerObj.WORKFLOW.NODES[nodeIndex].VALUES =
						this.convertRelationshipValues(
							triggerObj.WORKFLOW.NODES[nodeIndex].VALUES
						);
					triggerObj.WORKFLOW.NODES[nodeIndex].VALUES.moduleId = this.moduleId;
					triggerObj.WORKFLOW.NODES[nodeIndex].VALUES.entryId =
						'{{inputMessage.VALUES.ENTRY_ID}}';
					triggerObj.WORKFLOW.NODES[nodeIndex].VALUES.FIELDS.forEach(
						(field) => {
							const fieldId = field.FIELD.FIELD_ID;
							const valueArr = [];
							if (field.VALUE.hasOwnProperty('DATA_ID')) {
								const value = field.VALUE;
								valueArr.push(value.DATA_ID);
							} else if (Array.isArray(field.VALUE)) {
								for (const val of field.VALUE) {
									if (val.hasOwnProperty('DATA_ID')) {
										valueArr.push(val.DATA_ID);
									}
								}
							} else {
								valueArr.push(field.VALUE);
							}
							field.VALUE = valueArr;
							field.FIELD = fieldId;
						}
					);
				}
			});
			if (moduleWorkflowId !== 'new') {
				this.modulesService
					.putWorkflow(this.moduleId, triggerObj)
					.pipe(take(1), takeUntil(this._destroyed$))
					.subscribe(
						() => {
							this.companiesService.trackEvent(`Updated Trigger`, {
								TRIGGER_TYPE: triggerObj.TYPE,
								MODULE_ID: this.moduleId,
							});
							this.router.navigate([`modules/${this.moduleId}/triggers`]);
						},
						(postWorkflowError: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: postWorkflowError.error.ERROR,
							});
						}
					);
			} else {
				this.modulesService
					.postWorkflow(this.moduleId, triggerObj)
					.pipe(take(1), takeUntil(this._destroyed$))
					.subscribe(
						() => {
							this.companiesService.trackEvent(`Created Trigger`, {
								TRIGGER_TYPE: triggerObj.TYPE,
								MODULE_ID: this.moduleId,
							});
							this.router.navigate([`modules/${this.moduleId}/triggers`]);
						},
						(putWorkflowError: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: putWorkflowError.error.ERROR,
							});
						}
					);
			}
		} else {
			for (
				let i = 1;
				i < this.triggerForm.value['WORKFLOW']['NODES'].length;
				i++
			) {
				if (
					this.triggerForm.value['WORKFLOW']['NODES'][i]['TYPE'] ===
						'StartEscalation' &&
					this.escalations.length === 0
				) {
					this.bannerMessageService.errorNotifications.push({
						message: this.translateService.instant('MISSING_ESCALATION_ERROR'),
					});
				} else {
					this.errorMessage = '';
				}
			}
		}
	}
	// CREATE FIELDS
	public createField(): FormGroup {
		return this.formBuilder.group({
			FIELD: ['', [Validators.required]],
			VALUE: ['', [Validators.required]],
		});
	}

	public addField(field): void {
		field.push(this.createField());
		field.controls.forEach((form) => {
			form
				.get('FIELD')
				.valueChanges.pipe(takeUntil(this._destroyed$))
				.subscribe((selectedValue) => {
					if (selectedValue) {
						// for relationship field type
						// add primary display field name to variables for display purposes
						if (
							selectedValue.RELATIONSHIP_FIELD !== null &&
							selectedValue.RELATIONSHIP_FIELD !== ''
						) {
							const relatedModule = this.moduleMap.get(selectedValue.MODULE);
							const primaryDisplayField = relatedModule['FIELDS'].find(
								(fieldFound) =>
									fieldFound['FIELD_ID'] ===
									selectedValue['PRIMARY_DISPLAY_FIELD']
							);
							selectedValue['PRIMARY_DISPLAY_FIELD_NAME'] =
								primaryDisplayField['NAME'];
						}
						this.filterVariables(selectedValue, 'conditions');
					}
				});
		});
	}

	private convertRelationshipValues(fields) {
		fields.FIELDS.forEach((element) => {
			if (element.VALUE.hasOwnProperty('DATA')) {
				element.VALUE = element.VALUE.DATA;
			}
		});
		return fields;
	}

	public ngOnDestroy(): void {
		this._destroyed$.next();
		this._destroyed$.complete();
	}
}
