import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';

import { ActivatedRoute, Router } from '@angular/router';
import { Stage, Workflow, WorkflowApiService } from '@ngdesk/workflow-api';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { TriggersDetailService } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/triggers-detail.service';
import { TranslateService } from '@ngx-translate/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { LoaderService } from '../../../../custom-components/loader/loader.service';
import { AppGlobals } from '@src/app/app.globals';
import { ConditionsComponent } from '@src/app/custom-components/conditions/conditions.component';
import { Condition } from '@src/app/models/condition';
import { WorkflowCreateService } from '../workflow-create.service';
import { Field } from '@src/app/models/field';
import { ModulesService } from '@src/app/modules/modules.service';

@Component({
	selector: 'app-workflow-create',
	templateUrl: './workflow-create.component.html',
	styleUrls: ['./workflow-create.component.scss'],
})
export class WorkflowCreateComponent implements OnInit {
	@ViewChild('toolbar') public toolbar: ElementRef;
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public conditions: Condition[] = [];
	public fileJSON: Object;
	private module: any = {};
	private cellData: any[] = [];
	public moduleFields: Field[] = [];
	public workflow: Workflow = {
		NAME: '',
		CONDITIONS: [],
		ORDER: 0,
		STAGES: [],
		TYPE: 'CREATE_OR_UPDATE',
		DISPLAY_ON_ENTRY: false,
	};
	public types: string[] = [
		'CREATE',
		'UPDATE',
		'CREATE_OR_UPDATE',
		'BUTTON',
		'SLA',
		'FORM_OR_CATALOGUE',
	];

	private moduleWorkflowId = 'new';
	public params;
	public workflowForm: FormGroup;
	public conditionsForm: FormGroup;
	public moduleId: string;

	constructor(
		private triggerDetailService: TriggersDetailService,
		private route: ActivatedRoute,
		private workflowApi: WorkflowApiService,
		private router: Router,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private formBuilder: FormBuilder,
		private loaderService: LoaderService,
		private global: AppGlobals,
		private workflowcreateService: WorkflowCreateService,
		public modulesService: ModulesService
	) {}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.conditionsForm = this.formBuilder.group({
			CONDITIONS: this.formBuilder.array([]),
		});
		this.workflowForm = this.formBuilder.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
			TYPE: ['', [Validators.required]],
			ORDER: ['', [Validators.required]],
			DISPLAY_ON_ENTRY: [false],
		});

		this.params = {
			name: { field: this.translateService.instant('NAME') },
			type: { field: this.translateService.instant('TYPE') },
			order: { field: this.translateService.instant('ORDER') },
		};

		this.modulesService
			.getModuleById(this.moduleId)
			.subscribe((moduleResponse: any) => {
				this.moduleFields = moduleResponse.FIELDS;
			});

		this.triggerDetailService
			.getPrerequisiteData(this.moduleId, this.moduleWorkflowId)
			.subscribe((response) => {
				this.module = response[0];

				if (this.moduleWorkflowId == 'new') {
					const stage: Stage = {
						CONDITIONS: [],
						NAME: 'Default Stage',
						NODES: [],
						STAGE_ID: this.global.guid(),
					};
					this.workflow.STAGES.push(stage);
				}

				const triggerOrderResponse = response[4];
				if (triggerOrderResponse.content.length > 0) {
					this.workflow.ORDER = triggerOrderResponse.content[0].ORDER + 1;
				}

				if (this.workflow.STAGES) {
					this.cellData = this.triggerDetailService.loadCellValues(
						this.workflow,
						this.module
					);
				}
			});
	}

	public save() {
		this.workflowForm.get('NAME').markAsTouched();
		this.workflowForm.get('ORDER').markAsTouched();
		this.workflowForm.get('TYPE').markAsTouched();

		if (this.workflowForm.valid) {
			this.workflow['RAPID_UI_PAYLOAD'] = JSON.stringify(this.fileJSON);
			this.workflow.CONDITIONS = this.conditionsComponent.transformConditions();
			this.workflowcreateService.workflow = {
				NAME: this.workflow.NAME,
				DESCRIPTION: this.workflow.DESCRIPTION,
				ORDER: this.workflow.ORDER,
				TYPE: this.workflow.TYPE,
				DISPLAY_ON_ENTRY: this.workflow.DISPLAY_ON_ENTRY,
				STAGES: this.workflow.STAGES,
				CONDITIONS: this.workflow.CONDITIONS,
			};

			this.router.navigate([`modules/${this.moduleId}/workflows/new`]);
		}
	}

	public addStages() {
		const stage: Stage = {
			CONDITIONS: [],
			NAME: 'Stage' + this.workflow.STAGES.length + 1,
			NODES: [],
			STAGE_ID: this.global.guid(),
		};
		this.workflow.STAGES.push(stage);
	}
}
