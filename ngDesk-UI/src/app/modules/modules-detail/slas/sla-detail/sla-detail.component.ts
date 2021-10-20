import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '../../../../companies/companies.service';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { ConditionsComponent } from '../../../../custom-components/conditions/conditions.component';
import { FilterRuleOptionPipe } from '../../../../custom-components/conditions/filter-rule-option/filter-rule-option.pipe';
import { Escalation } from '../../../../models/escalation';
import { Field } from '../../../../models/field';
import { SlaBusinessRulesComponent } from './sla-business-rules/sla-business-rules.component';
import { ChannelsService } from 'src/app/channels/channels.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { WorkflowApiService } from '@ngdesk/workflow-api';
import { SLA, SlaapiService } from '@ngdesk/module-api';
import { SlaService } from '../sla-service';
import { ENTER, COMMA } from '@angular/cdk/keycodes';
import { MatChipInputEvent } from '@angular/material/chips';
import { TriggersDetailService } from '../../triggers/triggers-detail-new/triggers-detail.service';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '../../../../app.globals';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import {FormControl} from '@angular/forms';



@Component({
	selector: 'app-sla-detail',
	templateUrl: './sla-detail.component.html',
	styleUrls: ['./sla-detail.component.scss'],
})
export class SlaDetailComponent implements OnInit {
	
	public workflowCurrentPage = 0;
	public workflowPageSize = 10;
	public recurringFlag: boolean;
	public hasRestrictions: boolean;
	public errorMessage: string;
	public fields: Field[] = [];
	public bodyFields: Field[] = [];
	public escalations: Escalation[] = [];
	public slaLoaded = false;
	private moduleId: string;
	public slaForm: FormGroup;
	public autocompleteConditionsFiltered: Field[] = [];
	public autocompleteConditionsInitial: Field[] = [];
	public autocompleteOperatorsFiltered: {
		DISPLAY: any;
		BACKEND: string;
	}[];
	public autocompleteOperatorsInitial: {
		DISPLAY: any;
		BACKEND: string;
	}[];
	public autocompleteValuesFiltered: any[] = [];
	public autocompleteValuesInitial: any[] = [];
	public variables: Field[];
	public params;
	private variablesInitial: Field[];
	public checked = false;
	public fieldlist: any;
	public fieldId: any;

	public workflows = [];
	public sla: SLA = {
		name: '',
		description: '',
		conditions: [],
		violation: {
			operator: '',
			condition: '',
			conditionValue: '',
		},
		slaExpiry: 0,
		isRecurring: false,
		recurrence: {
			maxRecurrence: 20,
			intervalTime: 1,
		},
		isRestricted: false,
		businessRules: {
			restrictionType: '',
			restrictions: [],
		},
		workflow: '',
	};
	private module: any = {};
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public selectedTeams = [];
	public teams: any[] = [];
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA];
	public filteredWorkflows: Observable<string[]>;
	public workflow = new FormControl();

	constructor(
		private cdr: ChangeDetectorRef,
		private formBuilder: FormBuilder,
		private route: ActivatedRoute,
		private router: Router,
		public dialog: MatDialog,
		private companiesService: CompaniesService,
		private translateService: TranslateService,
		public channelsService: ChannelsService,
		private bannerMessageService: BannerMessageService,
		private loaderService: LoaderService,
		private workflowApiService: WorkflowApiService,
		private slaApiService: SlaapiService,
		private slaService: SlaService,
		private triggersDetailService: TriggersDetailService,
		private httpClient: HttpClient,
		private appGlobals: AppGlobals,
	) {}

	public ngOnInit() {
		this.slaForm = this.formBuilder.group({
			IS_RECURRING: [false],
			RECURRENCE: this.formBuilder.group({
				INTERVAL_TIME: [1],
				MAX_RECCURENCE: [20],
			}),
			CONDITIONS: this.formBuilder.array([]),
			VIOLATIONS: this.formBuilder.group({
				CONDITION: ['', Validators.required],
				OPERATOR: ['', Validators.required],
				CONDITION_VALUE: '',
			}),
			SLA_EXPIRY: ['', Validators.required],
			workflow: this.workflow,
			isRestricted: [false],
		});
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.setWorkflows();

		// set the translated values for is required translation params
		this.params = {
			condition: {
				field: this.translateService.instant('CONDITION'),
			},
			operator: {
				field: this.translateService.instant('OPERATOR'),
			},
			slaExpiry: {
				field: this.translateService.instant('SLA_EXPIRY'),
			},
			intervalTime: {
				field: this.translateService.instant('INTERVAL_TIME'),
			},
			maxReccurence: {
				field: this.translateService.instant('MAX_RECCURENCE'),
			},
		};

		
		this.slaService.getData(this.moduleId).subscribe((response) => {
			response[0].FIELDS.filter(
				(field) =>
					field.NAME !== 'PASSWORD' && field.DATA_TYPE.DISPLAY !== 'Button'
			);
			this.fields = response[0].FIELDS.slice();
			// SEPERATE VARIABLE FOR SHOWING PILLS BELOW BODY

			this.autocompleteConditionsFiltered = response[0].FIELDS.filter(
				(field) => {
					return (
						field.DATA_TYPE.DISPLAY !== 'Auto Number' &&
						field.DATA_TYPE.DISPLAY !== 'Picklist (Multi-Select)' &&
						field.DATA_TYPE.DISPLAY !== 'Button' &&
						field.DATA_TYPE.DISPLAY !== 'Time Window' &&
						field.DATA_TYPE.DISPLAY !== 'Zoom' &&
						field.DATA_TYPE.DISPLAY !== 'Image' &&
						field.DATA_TYPE.DISPLAY !== 'File Preview' &&
						field.DATA_TYPE.DISPLAY !== 'Approval' &&
						field.DATA_TYPE.DISPLAY !== 'File Upload' &&
						field.DATA_TYPE.DISPLAY !== 'PDF'
					);
				}
			);
			this.autocompleteConditionsInitial = response[0].FIELDS.filter(
				(field) => {
					return (
						field.DATA_TYPE.DISPLAY !== 'Auto Number' &&
						field.DATA_TYPE.DISPLAY !== 'Picklist (Multi-Select)' &&
						field.DATA_TYPE.DISPLAY !== 'Button' &&
						field.DATA_TYPE.DISPLAY !== 'Time Window' &&
						field.DATA_TYPE.DISPLAY !== 'Zoom' &&
						field.DATA_TYPE.DISPLAY !== 'Image' &&
						field.DATA_TYPE.DISPLAY !== 'File Preview' &&
						field.DATA_TYPE.DISPLAY !== 'Approval' &&
						field.DATA_TYPE.DISPLAY !== 'File Upload'
					);
				}
			);
			this.variables = this.convertVariables(
				JSON.parse(JSON.stringify(this.fields))
			);
			this.variablesInitial = JSON.parse(JSON.stringify(this.variables));
			this.triggersDetailService.initializeTeams();
			this.triggersDetailService.teamsScrollSubject.next(['', true]);
			const slaId = this.route.snapshot.params['slaId'];
			if (slaId !== 'new') {
				this.slaService.getSla(slaId, this.moduleId).subscribe(
					(slaResponse: any) => {
						this.sla = slaResponse['DATA'];
						console.log('this.sla', this.sla);
						this.setValueToForm(this.sla);
						this.resetVariables();
						this.slaLoaded = true;
					},
					(slaError: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: slaError.error.ERROR,
						});
					}
				);
			} else {
				this.slaLoaded = true;
			}
			this.cdr.detectChanges();
		});
	}

	private setWorkflows() {
		const query = `{ 
			WORKFLOWS: getWorkflows(
			moduleId: "${this.moduleId}"
		  ) {
			WORKFLOW_ID: id
			NAME: name
		  }
		}`
		  this.makeGraphQLCall(query).subscribe(
			(workflowResponse: any) => {
				this.workflows = workflowResponse.WORKFLOWS;
				this.filteredWorkflows = this.workflow.valueChanges
      			.pipe(
		        startWith(''),
        		map(value => typeof value === 'string' ? value : value.WORKFLOW_ID),
        		map(NAME => NAME ? this.filterWorkflow(NAME) : this.workflows.slice()));
			},
			(error: any) => {
			  this.bannerMessageService.errorNotifications.push({
				message: error.error.ERROR
			  });
			}
		  );
	}

	public numberOnly(event): boolean {
		const charCode = event.which ? event.which : event.keyCode;
		if (charCode > 31 && (charCode < 48 || charCode > 57)) {
			return false;
		}
		return true;
	}

	public createNewWorkflow() {
		this.router.navigate([`modules/${this.moduleId}/workflows/create-new`]);
	}

	public onRecurring($event) {
		if ($event.checked === true) {
			this.slaForm
				.get('RECURRENCE')
				.get('INTERVAL_TIME')
				.setValidators([Validators.required, Validators.min(1)]);
			this.slaForm
				.get('RECURRENCE')
				.get('INTERVAL_TIME')
				.updateValueAndValidity();
			this.slaForm
				.get('RECURRENCE')
				.get('MAX_RECCURENCE')
				.setValidators([
					Validators.required,
					Validators.min(1),
					Validators.max(99),
				]);
			this.slaForm
				.get('RECURRENCE')
				.get('MAX_RECCURENCE')
				.updateValueAndValidity();
		} else {
			this.slaForm.get('RECURRENCE').get('INTERVAL_TIME').clearValidators();
			this.slaForm
				.get('RECURRENCE')
				.get('INTERVAL_TIME')
				.updateValueAndValidity();
			this.slaForm.get('RECURRENCE').get('MAX_RECCURENCE').clearValidators();
			this.slaForm
				.get('RECURRENCE')
				.get('MAX_RECCURENCE')
				.updateValueAndValidity();
		}
	}

	public getErrorMessage() {
		return this.slaForm
			.get('RECURRENCE')
			.get('INTERVAL_TIME')
			.hasError('required')
			? this.translateService.instant('MUST_ENTER_VALUE')
			: this.slaForm.get('RECURRENCE').get('INTERVAL_TIME').hasError('min')
			? this.translateService.instant('NOT_LESS_THAN_1')
			: '';
	}

	public ErrorMessage() {
		return this.slaForm
			.get('RECURRENCE')
			.get('MAX_RECCURENCE')
			.hasError('required')
			? this.translateService.instant('MUST_ENTER_VALUE')
			: this.slaForm.get('RECURRENCE').get('MAX_RECCURENCE').hasError('min')
			? this.translateService.instant('NOT_LESS_THAN_1')
			: this.slaForm.get('RECURRENCE').get('MAX_RECCURENCE').hasError('max')
			? this.translateService.instant('LESS_THAN_100')
			: '';
	}

	public filterInputValues(event, condition, filterType) {
		let input = event;
		if (filterType === 'conditions') {
			// if has field ID it means the user slected from the dropdown and didnt
			// type it manually
			if (event.hasOwnProperty('FIELD_ID')) {
				input = event.DISPLAY_LABEL;
			}
			this.autocompleteConditionsFiltered =
				new FilterRuleOptionPipe().transform(
					this.autocompleteConditionsInitial,
					input.toLowerCase(),
					filterType
				);
		} else if (filterType === 'values' || filterType === 'picklistValues') {
			// if has Data ID it means the user slected from the dropdown and didnt
			// type it manually
			if (event.hasOwnProperty('DATA_ID')) {
				input = event[event['PRIMARY_DISPLAY_FIELD']];
			}
			if (this.autocompleteValuesInitial[condition.value.CONDITION.NAME]) {
				this.autocompleteValuesFiltered[condition.value.CONDITION.NAME] =
					new FilterRuleOptionPipe().transform(
						this.autocompleteValuesInitial[condition.value.CONDITION.NAME],
						input.toLowerCase(),
						filterType
					);
			}
		} else if (event.hasOwnProperty('FIELD_ID')) {
			this.autocompleteOperatorsInitial = this.getOperators(
				condition.value.CONDITION
			);

			if (input.DISPLAY === undefined) {
				this.autocompleteOperatorsFiltered =
					new FilterRuleOptionPipe().transform(
						this.autocompleteOperatorsInitial,
						input.toLowerCase(),
						filterType
					);
			} else {
				this.autocompleteOperatorsFiltered =
					new FilterRuleOptionPipe().transform(
						this.autocompleteOperatorsInitial,
						input.DISPLAY.toLowerCase(),
						filterType
					);
			}
		}
	}

	public resetOptions(optionType) {
		//  after options is selected reset the autocomplete values
		if (optionType === 'conditions') {
			this.autocompleteConditionsFiltered = this.autocompleteConditionsInitial;
		} else {
			this.autocompleteOperatorsFiltered = this.autocompleteOperatorsInitial;
		}
	}

	public displayConditionFn(field: any): string | undefined {
		return field ? field.DISPLAY_LABEL : undefined;
	}

	public conditionSelected(selected, condition, operator?) {
		if (typeof selected === 'object') {
			if (selected.DATA_TYPE.DISPLAY === 'Picklist') {
				this.autocompleteValuesInitial[selected.NAME] =
					selected['PICKLIST_VALUES'];
				this.autocompleteValuesFiltered[selected.NAME] =
					selected['PICKLIST_VALUES'];
			}

			// EMPTY THE OPERATOR, SLA EXPIRY AND VALUE ON CONDITION CHANGE
			if (condition.controls.CONDITION.value !== undefined && !operator) {
				if (condition.controls.OPERATOR.value !== '') {
					condition.controls.OPERATOR.touched = false;
					condition.controls.OPERATOR.setValue('');
				}
				if (condition.controls.CONDITION_VALUE.value !== '') {
					condition.controls.CONDITION_VALUE.touched = false;
					condition.controls.CONDITION_VALUE.setValue('');
				}
				this.selectedTeams = [];
			}

			condition = selected;
			// after condition is selected set autocomplete values
			this.autocompleteOperatorsFiltered = this.getOperators(selected);
			this.autocompleteOperatorsInitial = this.getOperators(selected);
		}
	}

	public displayOperator(operator: any): string | undefined {
		return operator ? operator.DISPLAY : undefined;
	}

	public operatorSelected(selected, condition) {
		condition.OPERATOR = selected;
	}

	public displayFieldName(value) {
		if (value) {
			return `${value.DISPLAY_LABEL}`;
		}
	}

	// This function will set value to slaForm
	public setValueToForm(sla) {
		this.slaForm.controls['NAME'].setValue(sla.name);
		this.slaForm.controls['DESCRIPTION'].setValue(sla.description);
		this.slaForm.controls['IS_RECURRING'].setValue(sla.isRecurring);
		this.slaForm.controls['RECURRENCE']['controls']['INTERVAL_TIME'].setValue(
			sla.recurrence.intervalTime
		);
		this.slaForm.controls['isRestricted'].setValue(sla.isRestricted);
		this.slaForm.controls['SLA_EXPIRY'].setValue(sla.slaExpiry);
		this.checked = sla.isRestricted;
		this.slaForm.controls['RECURRENCE']['controls']['MAX_RECCURENCE'].setValue(
			sla.recurrence.maxRecurrence
		);
		if (this.slaForm.get('IS_RECURRING')) {
			this.slaForm
				.get('RECURRENCE')
				.get('INTERVAL_TIME')
				.setValidators([Validators.required, Validators.min(1)]);
			this.slaForm
				.get('RECURRENCE')
				.get('INTERVAL_TIME')
				.updateValueAndValidity();
		}
		if (this.slaForm.get('IS_RECURRING')) {
			this.slaForm
				.get('RECURRENCE')
				.get('MAX_RECCURENCE')
				.setValidators([Validators.required, Validators.min(1)]);
			this.slaForm
				.get('RECURRENCE')
				.get('MAX_RECCURENCE')
				.updateValueAndValidity();
		}

		const conditionValue = this.fields.find(
			(field) => field.FIELD_ID === sla.violation.condition
		);
		const operatorValue = this.getOperators(conditionValue).find(
			(operator) => operator.BACKEND === sla.violation.operator
		);

		this.slaForm.controls['VIOLATIONS']['controls']['CONDITION'].setValue(
			conditionValue
		);
		this.slaForm.controls['VIOLATIONS']['controls']['OPERATOR'].setValue(
			operatorValue
		);
		if (
			conditionValue.DATA_TYPE.DISPLAY === 'Discussion' &&
			sla.violation.conditionValue
		) {
			this.selectedTeams = JSON.parse(sla.violation.conditionValue);
			this.selectedTeams.forEach((element) => {
				this.teams.push(element.DATA_ID);
			});
			this.slaForm.controls['VIOLATIONS']['controls'][
				'CONDITION_VALUE'
			].setValue(this.selectedTeams);
		} else {
			this.slaForm.controls['VIOLATIONS']['controls'][
				'CONDITION_VALUE'
			].setValue(sla.violation.conditionValue);
		}
		if (conditionValue.DATA_TYPE.DISPLAY === 'Picklist') {
			this.autocompleteValuesInitial[conditionValue.NAME] =
				conditionValue.PICKLIST_VALUES;
			this.autocompleteValuesFiltered[conditionValue.NAME] =
				conditionValue.PICKLIST_VALUES;
		}
		if (sla.workflow !== null) {
			const workflowObject = {
				NAME: sla.workflow.name,
				WORKFLOW_ID: sla.workflow.id
			}
			// this.workflow.setValue(sla)
			this.slaForm.controls['workflow'].setValue(workflowObject);
		}
	}

	private convertVariables(fields) {
		fields.forEach((field) => {
			if (field.NAME === 'REQUESTOR' || field.NAME === 'ASSIGNEE') {
				field.NAME = `{{InputMessage.${field.NAME}.EMAIL_ADDRESS}}`;
			} else {
				field.NAME = `{{InputMessage.${field.NAME}}}`;
			}
		});
		return fields;
	}

	public filterVariables(event, filterType) {
		let input = event;
		// if has field ID it means the user slected from the dropdown and didnt
		// type it manually
		if (filterType === 'conditions') {
			if (event.hasOwnProperty('FIELD_ID')) {
				input = event.DISPLAY_LABEL;
			}
			this.variables = new FilterRuleOptionPipe().transform(
				this.variablesInitial,
				input.toLowerCase(),
				'conditions'
			);
		}
	}

	private getOperators(field) {
		if (field.DATA_TYPE.DISPLAY === 'Discussion') {
			return [
				{
					DISPLAY: this.translateService.instant('HAS_NOT_BEEN_REPLIED_BY'),
					BACKEND: 'HAS_NOT_BEEN_REPLIED_BY',
				},
			];
		} else if (
			field.DATA_TYPE.BACKEND === 'Timestamp' ||
			field.DATA_TYPE.DISPLAY === 'Relationship' ||
			field.DATA_TYPE.DISPLAY === 'Email' ||
			field.DATA_TYPE.DISPLAY === 'Number'
		) {
			return [
				{
					DISPLAY: this.translateService.instant('HAS_NOT_CHANGED'),
					BACKEND: 'HAS_NOT_CHANGED',
				},
				{
					DISPLAY: this.translateService.instant('IS_PAST_BY'),
					BACKEND: 'IS_PAST_BY',
				},
				{
					DISPLAY: this.translateService.instant('IS_WITHIN'),
					BACKEND: 'IS_WITHIN',
				},
			];
		} else {
			return [
				{
					DISPLAY: this.translateService.instant('HAS_BEEN'),
					BACKEND: 'HAS_BEEN',
				},
				{
					DISPLAY: this.translateService.instant('HAS_NOT_CHANGED'),
					BACKEND: 'HAS_NOT_CHANGED',
				},
			];
		}
	}

	//  after subject is selected reset the autocomplete values
	public resetVariables() {
		this.variables = this.variablesInitial;
	}

	public save() {
		if (this.slaForm.valid) {
			if (this.selectedTeams.length !== 0) {
				const teamsValue = JSON.stringify(this.selectedTeams);
				this.slaForm.get('VIOLATIONS').value.CONDITION_VALUE = teamsValue;
			}
			const slaObj = {
				name: this.slaForm.value.NAME,
				description: this.slaForm.value.DESCRIPTION,
				conditions: this.convertConditionsToLowercase(
					this.conditionsComponent.transformConditions()
				),
				violation: {
					operator: this.slaForm.get('VIOLATIONS').value.OPERATOR.BACKEND,
					condition: this.slaForm.get('VIOLATIONS').value.CONDITION.FIELD_ID,
					conditionValue: this.slaForm.get('VIOLATIONS').value.CONDITION_VALUE,
				},
				slaExpiry: this.slaForm.value.SLA_EXPIRY,
				isRecurring: this.slaForm.value.IS_RECURRING,
				recurrence: {
					maxRecurrence: this.slaForm.value.RECURRENCE.MAX_RECCURENCE,
					intervalTime: this.slaForm.value.RECURRENCE.INTERVAL_TIME,
				},
				isRestricted: this.sla.isRestricted,
				businessRules: this.sla.businessRules,
				workflow: this.slaForm.value.workflow.WORKFLOW_ID,
			};
			const slaId = this.route.snapshot.params['slaId'];
			console.log('slaObj', slaObj);
			if (slaId !== 'new') {
				slaObj['slaId'] = slaId;
				this.slaApiService.putSla(this.moduleId, slaObj).subscribe(
					(response: any) => {
						this.companiesService.trackEvent(`Updated SLA`, {
							SLA_ID: response.SLA_ID,
							MODULE_ID: this.moduleId,
						});
						this.router.navigate([`modules/${this.moduleId}/slas`]);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			} else {
				this.slaApiService.postSla(this.moduleId, slaObj).subscribe(
					(response: any) => {
						this.companiesService.trackEvent(`Created SLA`, {
							SLA_ID: response.SLA_ID,
							MODULE_ID: this.moduleId,
						});
						this.router.navigate([`modules/${this.moduleId}/slas`]);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			}
		} else {
			this.loaderService.isLoading = false;
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('SLA_REQUIRED_FIELDS'),
			});
		}
	}

	public convertConditionsToLowercase(conditions: any) {
		const newConditions = [];
		conditions.forEach((condition) => {
			newConditions.push({
				requirementType: condition.REQUIREMENT_TYPE,
				operator: condition.OPERATOR,
				condition: condition.CONDITION,
				conditionValue: condition.CONDITION_VALUE,
			});
		});
		return newConditions;
	}

	public toggleRestrictions(event, editModal: boolean) {
		this.sla.isRestricted = this.slaForm.value['isRestricted'];
		if (this.sla.businessRules.restrictionType !== 'Week') {
			this.sla.businessRules.restrictionType = 'Day';
		}
		if (event.checked || editModal) {
			this.checked = true;
			const dialogRef = this.dialog.open(SlaBusinessRulesComponent, {
				width: '600px',
				data: {
					businessRuleValue: this.sla.businessRules,
					isRestrictedValue: this.sla.isRestricted,
				},
				disableClose: true,
			});

			dialogRef.afterClosed().subscribe((result) => {
				if (result) {
					this.sla.businessRules = result.data.businessRuleValue;
					this.sla.isRestricted = result.data.isRestrictedValue;
					this.slaForm.controls['isRestricted'].setValue(this.sla.isRestricted);
					if (!result.data.isRestrictedValue) {
						this.checked = false;
						this.sla.businessRules.restrictions = [];
						this.sla.businessRules.restrictionType = null;
					}
				} else {
					this.checked = false;
					this.sla.isRestricted = false;
					this.sla.businessRules.restrictionType = null;
				}
			});
		} else {
			this.checked = true;
			this.sla.businessRules.restrictions = [];
			this.sla.businessRules.restrictionType = null;
		}
	}

	public removeTeams(element): void {
		const index = this.teams.indexOf(element.DATA_ID);
		if (index >= 0) {
			this.teams.splice(index, 1);
			this.selectedTeams.splice(index, 1);
		}
	}

	public resetInput(event: MatChipInputEvent) {
		if (event.input) {
			event.input.value = '';
		}
	}

	public searchTeam() {
		const teams = this.slaForm.value.VIOLATIONS.CONDITION_VALUE;
		if (typeof teams !== 'object') {
			const searchText = teams;
			this.triggersDetailService.teamsScrollSubject.next([searchText, true]);
		}
	}

	public onTeamsScroll(value) {
		this.triggersDetailService.teamsScrollSubject.next([
			this.slaForm.value.VIOLATIONS.CONDITION_VALUE,
			false,
		]);
	}

	public onTeamSelect(event): void {
		this.selectedTeams.push(event.option.value);
		this.teams.push(event.option.value.DATA_ID);
		this.slaForm.value.VIOLATIONS.CONDITION_VALUE = '';
	}

	public teamAutocompleteClosed() {
		this.triggersDetailService.teamsScrollSubject.next(['', true]);
	}

	public disabledTeamCheck(entry, teams) {
		return teams.indexOf(entry.DATA_ID) !== -1;
	}

	public makeGraphQLCall(query: string) {
		return this.httpClient.post(`${this.appGlobals.graphqlUrl}`, query);
	}

	public navigateToWorkflow(){
		const workflowId = this.slaForm.get('workflow').value.WORKFLOW_ID;
		this.router.navigate([`modules/${this.moduleId}/workflows/${workflowId}`]);
	}

	public filterWorkflow(value: any): any[] {
		const filterValue = value.toLowerCase();
	
		return this.workflows.filter(option => option.NAME.toLowerCase().includes(filterValue));
	  }
	public  displayFn(workflow: any): string {
		return workflow && workflow.NAME ? workflow.NAME : '';
	  }

}
