import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ConditionsComponent } from '@src/app/custom-components/conditions/conditions.component';
import { Condition } from '@src/app/models/condition';
import {
	catchError,
	debounceTime,
	distinctUntilChanged,
	map,
	mergeMap,
	switchMap,
} from 'rxjs/operators';
import { Subject, of } from 'rxjs';
import { TriggersDetailService } from '../triggers-detail.service';
import { ENTER, COMMA } from '@angular/cdk/keycodes';
import { MatChipInputEvent } from '@angular/material/chips';
import { TranslateService } from '@ngx-translate/core';
import { MicrosoftTeamsApiService } from '@ngdesk/integration-api';
import { ModulesService } from '@src/app/modules/modules.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
	selector: 'app-node-customization',
	templateUrl: './node-customization.component.html',
	styleUrls: ['./node-customization.component.scss'],
})
export class NodeCustomizationComponent implements OnInit {
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public conditions: Condition[] = [];
	public conditionsForm: FormGroup;
	public moduleFields: any[] = [];
	public moduleId: string;
	public variables: any = [];
	public to: String = '';
	public from: String = '';
	public subject: String = '';
	public node: any = {};
	public emailChannels: any = [];
	public toAddressFields: any = [];
	public toAddressFieldsInitials: any = [];
	public subjectFields: any = [];
	public subjectFieldsInitials: any = [];
	public body: String = '';
	public pdfName: String = '';
	public bodyFields: any = [];
	public escalations: any = [];
	public htmlTemplates: any = [];
	public escalation: String = '';
	public htmlTemplate: String = '';
	public signTemplate: String = '';
	public storeTemplate: String = '';
	public relatedFields: any = [];
	public nestedRelatedFields: any = [];
	public updateFields: any = [];
	public specialDataTypes: any = [
		'Picklist',
		'Date/Time',
		'Date',
		'Time',
		'Relationship',
		'Picklist (Multi-Select)',
	];
	public users: any[] = [];
	public teams: any[] = [];
	public stages: any[] = [];
	public channels: any[] = [];
	public stageId = '';
	public channelId = '';
	public tempUserInput = '';
	public tempContactInput = '';
	public tempTeamInput = '';
	public approvalCondition = '';
	public numberOfApprovalsRequired = '';
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA];
	public selectedUsers = [];
	public selectedTeams = [];
	public updateEntryFields: any[] = [];
	public updateEntryFieldsInitials: any[] = [];
	public errors;
	public approverConditionValue = [
		'All Approvers',
		'Any Approver',
		'Minimum No. of Approvals',
	];
	public disableEntry: boolean;
	public notifyUsersForApproval: boolean;
	public notifyUsersAfterApproval: boolean;
	public teamNotificationFields: any[] = [];
	public teamNotificationFieldsInitials: any[] = [];
	public fieldTypes = [];
	public fieldNameFormGroup: FormGroup;
	public replace: boolean;
	public modules = [];
	public createEntryModuleId = '';

	public pdfFields: any = [];
	public contactsScrollSubject = new Subject<any>();
	public contactsStore: any = [];
	public sampleStore: any = [];
	public contactsLength;
	public contactModuleId;

	constructor(
		private dialogRef: MatDialogRef<NodeCustomizationComponent>,
		private formBuilder: FormBuilder,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private triggerDetailService: TriggersDetailService,
		private translateService: TranslateService,
		private microsoftTeamsApiService: MicrosoftTeamsApiService,
		private modulesService: ModulesService,
		private route: ActivatedRoute,
		private router: Router
	) {}

	public ngOnInit() {
		this.errors = {
			subject: { field: this.translateService.instant('SUBJECT') },
			escalation: { field: this.translateService.instant('ESCALATION') },
			from: { field: this.translateService.instant('FROM') },
			to: { field: this.translateService.instant('TO') },
			body: { field: this.translateService.instant('BODY') },
		};
		this.modulesService.getModules().subscribe((moduleResponse: any) => {
			this.modules = moduleResponse.MODULES.sort((a, b) =>
				a.NAME.localeCompare(b.NAME)
			);

			// REMOVING TEAMS MODULE
			this.modules = this.modules.filter(
				(module) =>
					module.NAME !== 'Teams' &&
					module.NAME !== 'Contacts' &&
					module.NAME !== 'Users' &&
					module.NAME !== 'Accounts'
			);
		});
		this.microsoftTeamsApiService
			.getMicrosoftTeams()
			.subscribe((response: any) => {
				this.channels = response.content;
			});
		this.moduleFields = this.data.MODULE.FIELDS;
		this.moduleId = this.data.MODULE.MODULE_ID;
		this.node = this.data.CELL;
		this.stages = this.data.STAGES;
		this.loadDataToVariables();
		this.updateEntryFilterFields();
		this.teamsNotificationFilterFields();
		let page = 0;
		let searchValue = '';
		this.modulesService
			.getModuleByName('Contacts')
			.pipe(
				map((response: any) => {
					this.contactModuleId = response.MODULE_ID;
				}),
				mergeMap((contacts) =>
					this.triggerDetailService.getContactsData(
						this.contactModuleId,
						page,
						searchValue
					)
				)
			)
			.subscribe((response: any) => {
				this.contactsStore = response.DATA;
				this.initializeContacts();
			});

		if (this.updateFields) {
			if (this.updateFields.length === 0) {
				this.addField();
			}
		}
		const storedDataType = this.route.snapshot.params['dataType'];
		if (storedDataType && storedDataType !== null && storedDataType !== '') {
			const toSelect = this.fieldTypes.find(
				(f) => f.DISPLAY === storedDataType
			);
			this.fieldNameFormGroup.controls['dataType'].setValue(toSelect, [
				Validators.required,
			]);
		}
		this.fieldTypes = this.moduleFields.filter(
			(field) => field.DATA_TYPE.DISPLAY === 'File Upload'
		);
	}

	public updateEntryFilterFields() {
		this.updateEntryFields = this.moduleFields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
				field.DATA_TYPE.DISPLAY !== 'Approval' &&
				field.DATA_TYPE.DISPLAY !== 'Auto Number' &&
				field.DATA_TYPE.DISPLAY !== 'Button' &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'File Preview' &&
				field.DATA_TYPE.DISPLAY !== 'Formula' &&
				field.DATA_TYPE.DISPLAY !== 'Image' &&
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.DATA_TYPE.DISPLAY !== 'Time Window' &&
				field.DATA_TYPE.DISPLAY !== 'Zoom' &&
				field.NAME !== 'EFFECTIVE_TO' &&
				field.NAME !== 'DATE_UPDATED' &&
				field.NAME !== 'LAST_UPDATED_BY' &&
				field.NAME !== 'DATE_CREATED' &&
				field.NAME !== 'POST_ID' &&
				field.NAME !== 'SOURCE_TYPE' &&
				field.NAME !== 'CHANNEL' &&
				field.NAME !== 'CREATED_BY' &&
				field.NAME !== 'REQUESTOR' &&
				field.NAME !== 'DATA_ID' &&
				field.NAME !== 'EFFECTIVE_FROM'
		);

		this.updateEntryFieldsInitials = this.moduleFields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
				field.DATA_TYPE.DISPLAY !== 'Approval' &&
				field.DATA_TYPE.DISPLAY !== 'Auto Number' &&
				field.DATA_TYPE.DISPLAY !== 'Button' &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'File Preview' &&
				field.DATA_TYPE.DISPLAY !== 'Formula' &&
				field.DATA_TYPE.DISPLAY !== 'Image' &&
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.DATA_TYPE.DISPLAY !== 'Time Window' &&
				field.DATA_TYPE.DISPLAY !== 'Zoom' &&
				field.NAME !== 'EFFECTIVE_TO' &&
				field.NAME !== 'DATE_UPDATED' &&
				field.NAME !== 'LAST_UPDATED_BY' &&
				field.NAME !== 'DATE_CREATED' &&
				field.NAME !== 'POST_ID' &&
				field.NAME !== 'SOURCE_TYPE' &&
				field.NAME !== 'CHANNEL' &&
				field.NAME !== 'CREATED_BY' &&
				field.NAME !== 'REQUESTOR' &&
				field.NAME !== 'DATA_ID' &&
				field.NAME !== 'EFFECTIVE_FROM'
		);
	}

	public teamsNotificationFilterFields() {
		this.teamNotificationFields = this.moduleFields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
				field.DATA_TYPE.DISPLAY !== 'Approval' &&
				field.DATA_TYPE.DISPLAY !== 'Button' &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'File Preview' &&
				field.DATA_TYPE.DISPLAY !== 'Formula' &&
				field.DATA_TYPE.DISPLAY !== 'Image' &&
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.DATA_TYPE.DISPLAY !== 'Time Window' &&
				field.DATA_TYPE.DISPLAY !== 'Workflow Stages' &&
				field.DATA_TYPE.DISPLAY !== 'Zoom' &&
				field.NAME !== 'EFFECTIVE_TO' &&
				field.NAME !== 'DATE_UPDATED' &&
				field.NAME !== 'LAST_UPDATED_BY' &&
				field.NAME !== 'DATE_CREATED' &&
				field.NAME !== 'POST_ID' &&
				field.NAME !== 'SOURCE_TYPE' &&
				field.NAME !== 'CHANNEL' &&
				field.NAME !== 'CREATED_BY' &&
				field.NAME !== 'DATA_ID' &&
				field.NAME !== 'EFFECTIVE_FROM'
		);

		this.teamNotificationFieldsInitials = this.moduleFields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
				field.DATA_TYPE.DISPLAY !== 'Approval' &&
				field.DATA_TYPE.DISPLAY !== 'Button' &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'File Preview' &&
				field.DATA_TYPE.DISPLAY !== 'Formula' &&
				field.DATA_TYPE.DISPLAY !== 'Image' &&
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.DATA_TYPE.DISPLAY !== 'Time Window' &&
				field.DATA_TYPE.DISPLAY !== 'Workflow Stages' &&
				field.DATA_TYPE.DISPLAY !== 'Zoom' &&
				field.NAME !== 'EFFECTIVE_TO' &&
				field.NAME !== 'DATE_UPDATED' &&
				field.NAME !== 'LAST_UPDATED_BY' &&
				field.NAME !== 'DATE_CREATED' &&
				field.NAME !== 'POST_ID' &&
				field.NAME !== 'SOURCE_TYPE' &&
				field.NAME !== 'CHANNEL' &&
				field.NAME !== 'CREATED_BY' &&
				field.NAME !== 'DATA_ID' &&
				field.NAME !== 'EFFECTIVE_FROM'
		);
	}

	public changeModule(moduleId) {
		this.modules.forEach((element) => {
			if (element.MODULE_ID === moduleId) {
				this.moduleFields = element.FIELDS;
			}
		});
		this.createEntryModuleId = moduleId;
		if (this.node.attributes.type === 'app.CreateEntry') {
			this.triggerDetailService.initializeSubject(this.createEntryModuleId);
		} else {
			this.triggerDetailService.initializeSubject(this.moduleId);
		}
		this.updateEntryFilterFields();
	}

	private loadDataToVariables() {
		if (this.node.attributes.type === 'app.Approval') {
			this.triggerDetailService.usersScrollSubject.next(['', true]);
			this.triggerDetailService.teamsScrollSubject.next(['', true]);
		}
		if (
			this.node.attributes.type === 'app.SendSms' ||
			this.node.attributes.type === 'app.MakePhoneCall'
		) {
			this.contactsScrollSubject.next(['', true]);
		}
		this.conditionsForm = this.formBuilder.group({
			CONDITIONS: this.formBuilder.array([]),
		});

		if (this.data.CELL_DATA.VALUE) {
			this.conditions = this.getConditions(
				this.data.CELL_DATA.VALUE.CONDITIONS
			);
			this.stageId = this.data.CELL_DATA.STAGE_ID;
			this.to = this.data.CELL_DATA.VALUE.TO;
			this.from = this.data.CELL_DATA.VALUE.FROM;
			this.subject = this.data.CELL_DATA.VALUE.SUBJECT;
			this.body = this.data.CELL_DATA.VALUE.BODY;
			this.pdfName = this.data.CELL_DATA.VALUE.PDF_NAME;
			this.escalation = this.data.CELL_DATA.VALUE.ESCALATION;
			this.htmlTemplate = this.data.CELL_DATA.VALUE.PDF_TEMPLATE;
			this.signTemplate = this.data.CELL_DATA.VALUE.PDF_TEMPLATE_ID;
			this.storeTemplate = this.data.CELL_DATA.VALUE.FIELD_ID;
			this.updateFields = this.data.CELL_DATA.VALUE.FIELDS;
			this.channelId = this.data.CELL_DATA.VALUE.CHANNEL_ID;
			this.createEntryModuleId = this.data.CELL_DATA.VALUE.MODULE;
			this.users = this.data.CELL_DATA.VALUE.APPROVERS;
			this.teams = this.data.CELL_DATA.VALUE.TEAMS;
			this.approvalCondition = this.data.CELL_DATA.VALUE.APPROVAL_CONDITION;
			this.numberOfApprovalsRequired =
				this.data.CELL_DATA.VALUE.NUMBER_OF_APPROVALS_REQUIRED;
			this.disableEntry = this.data.CELL_DATA.VALUE.DISABLE_ENTRY;
			this.replace = this.data.CELL_DATA.VALUE.REPLACE;
			this.notifyUsersForApproval =
				this.data.CELL_DATA.VALUE.NOTIFY_USERS_FOR_APPROVAL;
			this.notifyUsersAfterApproval =
				this.data.CELL_DATA.VALUE.NOTIFY_USERS_AFTER_APPROVAL;
			if (this.users && this.users.length > 0) {
				this.triggerDetailService
					.getUsersForApproval(this.users)
					.subscribe((result) => {
						this.selectedUsers = result;
					});
			}
			if (this.teams && this.teams.length > 0) {
				this.triggerDetailService
					.getTeamsForApproval(this.teams)
					.subscribe((result) => {
						this.selectedTeams = result;
					});
			}
		}

		// DEFAULT VALUES COMING FROM THE TRIGGER DETAIL
		this.emailChannels = this.data.EMAIL_CHANNELS;
		this.relatedFields = this.data.BODY_RELATED_FIELDS;
		this.nestedRelatedFields = this.data.BODY_RELATED_NESTED_FIELDS;
		this.escalations = this.data.ESCALATIONS;
		this.htmlTemplates = this.data.PDF_TEMPLATES;

		// PASSING FIELDS AS A COPY
		this.loadToAddressFields(JSON.parse(JSON.stringify(this.moduleFields)));
		this.loadSubjectFields(JSON.parse(JSON.stringify(this.moduleFields)));
		this.loadBodyFields(JSON.parse(JSON.stringify(this.moduleFields)));
		this.loadFieldsForPDF(JSON.parse(JSON.stringify(this.moduleFields)));
	}

	private getConditions(conditions: any[]): Condition[] {
		return conditions.map(
			(condition) =>
				new Condition(
					condition.CONDITION,
					condition.CONDITION_VALUE,
					condition.OPERATOR,
					condition.REQUIREMENT_TYPE
				)
		);
	}

	createnew() {
		this.router.navigate([`modules/${this.moduleId}/field/field-creator`]);
		this.dialogRef.close();
	}

	createnewPdf() {
		this.router.navigate([`modules/${this.moduleId}/pdf/new`]);
		this.dialogRef.close();
	}

	public addField() {
		this.updateFields.push({ FIELD: '', VALUE: '' });
		this.teamsNotificationFilterFields();
		this.changeModule(this.createEntryModuleId);
		this.updateEntryFilterFields();
	}

	public removeField(i: number) {
		this.updateFields.splice(i, 1);
	}

	public displayFieldName(value) {
		if (value) {
			return `${value.DISPLAY_LABEL}`;
		}
	}

	public displayPrimaryDisplayField(value) {
		if (value) {
			return value.PRIMARY_DISPLAY_FIELD;
		}
	}

	public contactName(to) {
		if (to && to.FULL_NAME != undefined) {
			if (to.PHONE_NUMBER && to.PHONE_NUMBER.PHONE_NUMBER != null) {
				return (
					to.FULL_NAME +
					'<' +
					to.PHONE_NUMBER.DIAL_CODE +
					' ' +
					to.PHONE_NUMBER.PHONE_NUMBER +
					'>'
				);
			} else {
				return to.FULL_NAME;
			}
		} else if (typeof to == 'string' && to.match('DATA_ID')) {
			const contact = JSON.parse(to);
			if (contact.PHONE_NUMBER && contact.PHONE_NUMBER.PHONE_NUMBER != null) {
				return (
					contact.FULL_NAME +
					'<' +
					contact.PHONE_NUMBER.DIAL_CODE +
					' ' +
					contact.PHONE_NUMBER.PHONE_NUMBER +
					'>'
				);
			} else {
				return contact.FULL_NAME;
			}
		} else {
			return JSON.parse(JSON.stringify(to));
		}
	}

	public toolTips(data) {
		if (data.PHONE_NUMBER && data.PHONE_NUMBER.PHONE_NUMBER != null) {
			return (
				data.FULL_NAME +
				'<' +
				data.PHONE_NUMBER.DIAL_CODE +
				' ' +
				data.PHONE_NUMBER.PHONE_NUMBER +
				'>'
			);
		} else {
			return data.FULL_NAME;
		}
	}

	// RESET VALUE for UPDATE ENTRY
	public resetValue(event, item, filterType) {
		let input = event;
		if (event && filterType === 'UpdateEntry') {
			// if has field ID it means the user slected from the dropdown and didnt
			// type it manually
			if (event.hasOwnProperty('FIELD_ID')) {
				input = event.DISPLAY_LABEL;
			}
			this.updateEntryFields = this.filterFields(
				this.updateEntryFieldsInitials,
				input.toLowerCase()
			);
		}
		if (event && filterType === 'microsoftTeamsNotification') {
			// if has field ID it means the user slected from the dropdown and didnt
			// type it manually
			if (event.hasOwnProperty('FIELD_ID')) {
				input = event.DISPLAY_LABEL;
			}
			this.teamNotificationFields = this.filterFields(
				this.teamNotificationFieldsInitials,
				input.toLowerCase()
			);
		}
		item.VALUE = '';
		if (item.FIELD.DATA_TYPE === undefined) {
			item.FIELD = '';
		} else if (item.FIELD.DATA_TYPE.DISPLAY === 'Relationship') {
			this.triggerDetailService.scrollSubject.next([item.FIELD, '', true]);
		}
	}

	public numberOnly(event): boolean {
		const charCode = event.which ? event.which : event.keyCode;
		if (charCode > 31 && (charCode < 48 || charCode > 57)) {
			return false;
		}
		return true;
	}

	private loadToAddressFields(fields: any) {
		this.toAddressFields = fields;
		this.toAddressFieldsInitials = fields;
		const dataTypesToIgnore = [
			'Auto Number',
			'Number',
			'Currency',
			'Geolocation',
			'Percent',
			'Phone',
			'Formula',
			'Time',
			'URL',
			'File Upload',
			'Discussion',
			'Checkbox',
			'Date',
			'Date/Time',
			'List Text',
			'Chronometer',
			'Button',
			'Aggregate',
		];

		this.toAddressFields = this.toAddressFields.filter(
			(field) => dataTypesToIgnore.indexOf(field.DATA_TYPE.DISPLAY) === -1
		);

		this.toAddressFieldsInitials = this.toAddressFieldsInitials.filter(
			(field) => dataTypesToIgnore.indexOf(field.DATA_TYPE.DISPLAY) === -1
		);

		this.toAddressFields.forEach((field) => {
			switch (field.NAME) {
				case 'ASSIGNEE':
				case 'CREATED_BY':
				case 'LAST_UPDATED_BY':
					field.NAME = `{{InputMessage.${field.NAME}.EMAIL_ADDRESS}}`;
					break;
				case 'REQUESTOR':
					field.NAME = `{{InputMessage.${field.NAME}.USER.EMAIL_ADDRESS}}`;
					break;
				default:
					field.NAME = `{{InputMessage.${field.NAME}}}`;
					break;
			}
		});
	}

	// RESET VALUE for UPDATE ENTRY
	public searchField(event, filterType) {
		let input = event;
		if (event && filterType === 'To') {
			// if has field ID it means the user slected from the dropdown and didnt
			// type it manually
			if (event.hasOwnProperty('FIELD_ID')) {
				input = event.DISPLAY_LABEL;
			}
			this.toAddressFields = this.filterFields(
				this.toAddressFieldsInitials,
				input.toLowerCase()
			);
		} else if (event && filterType === 'subject') {
			// if has field ID it means the user slected from the dropdown and didnt
			// type it manually
			if (event.hasOwnProperty('FIELD_ID')) {
				input = event.DISPLAY_LABEL;
			}
			this.subjectFields = this.filterFields(
				this.subjectFieldsInitials,
				input.toLowerCase()
			);
		}
	}

	private loadSubjectFields(fields: any) {
		this.subjectFields = fields;
		this.subjectFieldsInitials = fields;
		const dataTypesToIgnore = [
			'Geolocation',
			'Percent',
			'Formula',
			'File Upload',
			'Discussion',
			'Checkbox',
			'Button',
		];

		this.subjectFields = this.subjectFields.filter(
			(field) => dataTypesToIgnore.indexOf(field.DATA_TYPE.DISPLAY) === -1
		);
		this.subjectFieldsInitials = this.subjectFieldsInitials.filter(
			(field) => dataTypesToIgnore.indexOf(field.DATA_TYPE.DISPLAY) === -1
		);

		this.subjectFields.forEach((field) => {
			switch (field.NAME) {
				case 'ASSIGNEE':
				case 'CREATED_BY':
				case 'LAST_UPDATED_BY':
					field.NAME = `{{InputMessage.${field.NAME}.EMAIL_ADDRESS}}`;
					break;
				case 'REQUESTOR':
					field.NAME = `{{InputMessage.${field.NAME}.USER.EMAIL_ADDRESS}}`;
					break;
				default:
					field.NAME = `{{InputMessage.${field.NAME}}}`;
					break;
			}
		});
	}

	private loadBodyFields(fields: any) {
		this.bodyFields = fields;
		const dataTypesToIgnore = [
			'Geolocation',
			'Percent',
			'Formula',
			'File Upload',
			'Checkbox',
			'Button',
		];

		this.bodyFields = this.bodyFields.filter(
			(field) => dataTypesToIgnore.indexOf(field.DATA_TYPE.DISPLAY) === -1
		);
		let discussionFieldLatest = null;
		this.bodyFields.forEach((element, index) => {
			if (element.DATA_TYPE.DISPLAY === 'Discussion') {
				discussionFieldLatest = JSON.parse(
					JSON.stringify(this.bodyFields.slice(index, index + 1)[0])
				);
			}
		});
		if (discussionFieldLatest !== null) {
			const label = discussionFieldLatest.DISPLAY_LABEL;
			discussionFieldLatest.DISPLAY_LABEL = label + ' - Latest';
			const name = discussionFieldLatest.NAME;
			discussionFieldLatest.NAME = name + '.LATEST';
			this.bodyFields.push(discussionFieldLatest);
		}
	}

	private loadFieldsForPDF(fields: any) {
		this.pdfFields = fields;
		const dataTypesToIgnore = [
			'Geolocation',
			'Percent',
			'Formula',
			'File Upload',
			'Checkbox',
			'Button',
		];

		this.pdfFields = this.pdfFields.filter(
			(field) => field.DATA_TYPE.DISPLAY === 'Text'
		);
	}

	public insertBodyVariable(field, relatedVars?) {
		let fieldVar = field.NAME;
		if (relatedVars) {
			fieldVar += `.${relatedVars}`;
		}
		if (this.body === '') {
			this.body = ` {{InputMessage.${fieldVar}}} `;
		} else {
			this.body = this.body + `{{InputMessage.${fieldVar}}}`;
		}
	}

	public insertPDFVariable(field, relatedVars?) {
		let fieldVar = field.NAME;
		if (relatedVars) {
			fieldVar += `.${relatedVars}`;
		}
		if (this.pdfName === '') {
			this.pdfName = ` {{InputMessage.${fieldVar}}} `;
		} else {
			this.pdfName = this.pdfName + `{{InputMessage.${fieldVar}}}`;
		}
	}

	public concatenateVariables(field, mainItem?, subItem?, subSubItem?) {
		let concatVariable = mainItem.NAME;
		if (subItem) {
			concatVariable += `.${subItem.NAME}`;
			if (subSubItem) {
				concatVariable += `.${subSubItem.NAME}`;
			}
		}
		this.insertBodyVariable(field, concatVariable);
	}

	// SEND THE DATA TO TRIGGER DETAIL
	public saveData() {
		this.conditions = this.conditionsComponent.transformConditions();
		let dataToStore = {
			TO: this.to,
			FROM: this.from,
			SUBJECT: this.subject,
			CONDITIONS: this.conditions,
			BODY: this.body,
			ESCALATION: this.escalation,
			PDF_TEMPLATE: this.htmlTemplate,
			FIELDS: this.updateFields,
			NAME: this.data.CELL_NAME,
			STAGE_ID: this.stageId,
			CHANNEL_ID: this.channelId,
			MODULE: this.createEntryModuleId,
			APPROVERS: this.users,
			TEAMS: this.teams,
			PDF_TEMPLATE_ID: this.signTemplate,
			FIELD_ID: this.storeTemplate,
			APPROVAL_CONDITION: this.approvalCondition,
			NUMBER_OF_APPROVALS_REQUIRED: this.numberOfApprovalsRequired,
			DISABLE_ENTRY: this.disableEntry,
			NOTIFY_USERS_FOR_APPROVAL: this.notifyUsersForApproval,
			NOTIFY_USERS_AFTER_APPROVAL: this.notifyUsersAfterApproval,
			REPLACE: this.replace,
			PDF_NAME: this.pdfName,
		};
		this.dialogRef.close([dataToStore, { STAGE_ID: this.stageId }]);
	}

	// FUNCTIONS FOR RELATIONSHIP DATA SEARCH AND SCROLL
	public search(field, value) {
		this.triggerDetailService.scrollSubject.next([field, value, true]);
	}

	public onScroll(field, value) {
		this.triggerDetailService.scrollSubject.next([field, value, false]);
	}

	public autocompleteClosed(field) {
		this.triggerDetailService.scrollSubject.next([field, '', true]);
	}

	public searchUser() {
		this.triggerDetailService.usersScrollSubject.next([
			this.tempUserInput,
			true,
		]);
	}

	public searchContact() {
		this.contactsScrollSubject.next([this.to, true]);
	}

	public searchTeam() {
		this.triggerDetailService.teamsScrollSubject.next([
			this.tempTeamInput,
			true,
		]);
	}

	public userAutocompleteClosed() {
		this.triggerDetailService.usersScrollSubject.next(['', true]);
	}
	public contactAutocompleteClosed() {
		this.contactsScrollSubject.next(['', true]);
	}

	public teamAutocompleteClosed() {
		this.triggerDetailService.teamsScrollSubject.next(['', true]);
	}

	public onUsersScroll(value) {
		this.triggerDetailService.usersScrollSubject.next([
			this.tempUserInput,
			false,
		]);
	}
	public onContactsScroll() {
		this.contactsScrollSubject.next(['', false]);
	}

	public onTeamsScroll(value) {
		this.triggerDetailService.teamsScrollSubject.next([
			this.tempTeamInput,
			false,
		]);
	}

	public onSelect(event): void {
		this.selectedUsers.push(event.option.value);
		this.users.push(event.option.value.DATA_ID);
		this.tempUserInput = '';
	}

	public onTeamSelect(event): void {
		this.selectedTeams.push(event.option.value);
		this.teams.push(event.option.value.DATA_ID);
		this.tempTeamInput = '';
	}

	public resetInput(event: MatChipInputEvent) {
		if (event.input) {
			event.input.value = '';
		}
	}

	public disabledCheck(entry, users) {
		return users.indexOf(entry.DATA_ID) !== -1;
	}

	public disabledTeamCheck(entry, teams) {
		return teams.indexOf(entry.DATA_ID) !== -1;
	}

	public remove(element): void {
		const index = this.users.indexOf(element.DATA_ID);
		if (index >= 0) {
			this.users.splice(index, 1);
			this.selectedUsers.splice(index, 1);
		}
	}

	public removeTeams(element): void {
		const index = this.teams.indexOf(element.DATA_ID);
		if (index >= 0) {
			this.teams.splice(index, 1);
			this.selectedTeams.splice(index, 1);
		}
	}

	public initializeContacts() {
		this.contactsScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let searchValue = '';
					if (value !== '') {
						searchValue = 'FULL_NAME' + '=' + value;
					}
					let page = 0;
					if (this.contactsStore && !search) {
						page = Math.ceil(this.contactsStore.length / 10);
					}
					return this.triggerDetailService
						.getContactsData(this.contactModuleId, page, searchValue)
						.pipe(
							map((results: any) => {
								if (search) {
									this.contactsStore = results.DATA;
								} else {
									const newlist = this.filterContactsLists(
										'Contacts',
										results['DATA']
									);
									if (newlist.length > 0) {
										this.contactsStore = this.contactsStore.concat(
											results.DATA
										);
									}
								}
								this.contactsLength = this.contactsStore.length;
								return results.DATA;
							})
						);
				})
			)
			.subscribe();
	}

	public filterContactsLists(type, data) {
		const newArr = [];
		data.forEach((contact) => {
			const existingContact = this.contactsStore.find(
				(currentContact) => currentContact.DATA_ID === contact.DATA_ID
			);
			if (!existingContact) {
				newArr.push(contact);
			}
		});
		return newArr;
	}
	public filterFields(items: any, input: string) {
		const filteredItems = [];
		items.forEach((item) => {
			if (item.DISPLAY_LABEL.toLowerCase().indexOf(input) !== -1) {
				filteredItems.push(item);
			}
		});
		return filteredItems;
	}
	//  after subject is selected reset the autocomplete values
	public resetVariables() {
		this.toAddressFields = this.toAddressFieldsInitials;
		this.subjectFields = this.subjectFieldsInitials;
	}

	public createEsclation() {
		this.router.navigate([`escalations/new`]);
		this.dialogRef.close();
	}
}
