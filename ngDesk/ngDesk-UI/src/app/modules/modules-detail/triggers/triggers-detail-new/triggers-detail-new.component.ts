import {
	AfterViewInit,
	ChangeDetectorRef,
	Component,
	ElementRef,
	OnDestroy,
	OnInit,
	Renderer2,
	ViewChild,
	ViewEncapsulation,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Stage, Workflow, WorkflowApiService } from '@ngdesk/workflow-api';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { ConditionsDialogComponent } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/conditions-dialog/conditions-dialog.component';
import { NodeCustomizationComponent } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/node-customization/node-customization.component';
import {
	importGraphFromJSON,
	loadStencilShapes,
	zoomToFit,
} from '@src/app/modules/modules-detail/triggers/triggers-detail-new/rappid/actions';
import { SharedEvents } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/rappid/controller';
import { EventBusService } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/services/event-bus.service';
import RappidService from '@src/app/modules/modules-detail/triggers/triggers-detail-new/services/rappid.service';
import { StagesComponent } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/stages/stages.component';
import { STENCIL_WIDTH } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/theme';
import { TriggersDetailService } from './triggers-detail.service';
import { UsersService } from '@src/app/users/users.service';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { LoaderService } from '../../../../custom-components/loader/loader.service';
import { AppGlobals } from '@src/app/app.globals';
import { WorkflowCreateService } from '../workflow-create.service';

@Component({
	selector: 'app-triggers-detail-new',
	templateUrl: './triggers-detail-new.component.html',
	styleUrls: ['./triggers-detail-new.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class TriggersDetailNewComponent
	implements AfterViewInit, OnInit, OnDestroy
{
	@ViewChild('paper') public paper: ElementRef;
	@ViewChild('stencil') public stencil: ElementRef;
	@ViewChild('toolbar') public toolbar: ElementRef;

	private subscriptions = new Subscription();

	public stencilOpened = true;
	public jsonEditorOpened = true;
	public fileJSON: Object;
	public rappid: RappidService;

	private module: any = {};
	private escalations: any = [];
	private htmlTemplates: any = [];
	private emailChannels: any = [];
	private cellData: any[] = [];
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
	private file = {
		cells: [
			{
				type: 'app.FlowchartStart',
				size: { width: 48, height: 48 },
				ports: {
					items: [{ group: 'out', id: 'e4b63033-c7b2-4db8-b680-d18c37f24fe7' }],
				},
				position: { x: 24, y: 32 },
				id: 'a255ccdb-3fc8-4639-be3b-18a04148807c',
				z: 1,
				attrs: { label: { text: 'Start' } },
			},
			{
				type: 'app.FlowchartEnd',
				size: { width: 48, height: 48 },
				ports: {
					items: [{ group: 'in', id: 'a8c6382b-0a22-4ad8-badb-7d72aafea97a' }],
				},
				position: { x: 24, y: 344 },
				id: '1ca7669e-e2fb-48ba-aa4f-8fce388710a9',
				z: 2,
				attrs: { label: { text: 'End' } },
			},
		],
	};

	private moduleWorkflowId = '';
	public params;
	public workflowForm: FormGroup;

	constructor(
		private element: ElementRef,
		private eventBusService: EventBusService,
		private cdr: ChangeDetectorRef,
		private renderer: Renderer2,
		private triggerDetailService: TriggersDetailService,
		private route: ActivatedRoute,
		private dialog: MatDialog,
		private usersService: UsersService,
		private workflowApi: WorkflowApiService,
		private router: Router,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private formBuilder: FormBuilder,
		private loaderService: LoaderService,
		private global: AppGlobals,
		private workflowcreateService: WorkflowCreateService
	) {
		if (!this.workflowcreateService.workflow) {
			const moduleWorkflowId = this.route.snapshot.params['moduleWorkflowId'];
			const moduleId = this.route.snapshot.params['moduleId'];

			if (moduleWorkflowId === 'new') {
				this.router.navigate([`modules/${moduleId}/workflows/create-new`]);
			}
		} else {
			this.workflow = this.workflowcreateService.workflow;
		}
	}

	public ngOnInit() {
		this.workflowForm = this.formBuilder.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
			TYPE: ['', [Validators.required]],
			ORDER: ['', [Validators.required]],
			DISPLAY_ON_ENTRY: [false],
		});
		const moduleWorkflowId = this.route.snapshot.params['moduleWorkflowId'];
		const moduleId = this.route.snapshot.params['moduleId'];
		const { subscriptions, eventBusService } = this;
		subscriptions.add(
			eventBusService.on(SharedEvents.GRAPH_CHANGED, (json: Object) =>
				this.onRappidGraphChange(json)
			)
		);
		subscriptions.add(
			eventBusService.on(SharedEvents.JSON_EDITOR_CHANGED, (json: Object) =>
				this.onJsonEditorChange(json)
			)
		);
		this.params = {
			name: { field: this.translateService.instant('NAME') },
			type: { field: this.translateService.instant('TYPE') },
			order: { field: this.translateService.instant('ORDER') },
		};
		this.triggerDetailService.initializeUsers();
		this.triggerDetailService.initializeTeams();

		this.triggerDetailService
			.getPrerequisiteData(moduleId, moduleWorkflowId)
			.subscribe((response) => {
				this.module = response[0];
				this.escalations = response[1];
				this.emailChannels = response[2];
				if (moduleWorkflowId !== 'new') {
					this.workflow = response[3];
				}
				this.htmlTemplates = response[5];

				if (this.workflow['RAPID_UI_PAYLOAD']) {
					this.openFile(JSON.parse(this.workflow['RAPID_UI_PAYLOAD']));
				}

				if (this.workflow.STAGES) {
					this.cellData = this.triggerDetailService.loadCellValues(
						this.workflow,
						this.module
					);
				}
				if (moduleWorkflowId == 'new') {
					this.openFile(this.file);
				}

				this.emailChannels = this.emailChannels.filter(
					(channel) => channel.IS_VERIFIED
				);
				if (!this.emailChannels || this.emailChannels.length === 0) {
					this.emailChannels.push({
						EMAIL_ADDRESS: `support@${this.usersService.getSubdomain()}.ngdesk.com`,
					});
				}
			});
	}

	public ngAfterViewInit(): void {
		const { element, paper, stencil, toolbar, eventBusService, cdr } = this;
		this.rappid = new RappidService(
			element.nativeElement,
			paper.nativeElement,
			stencil.nativeElement,
			toolbar.nativeElement,
			eventBusService
		);
		this.setStencilContainerSize();
		this.onStart();
		cdr.detectChanges();
	}

	public ngOnDestroy(): void {
		if (this.subscriptions) {
			this.subscriptions.unsubscribe();
		}
		if (this.rappid) {
			this.rappid.destroy();
		}
	}

	public openFile(json: Object): void {
		const { rappid } = this;
		this.fileJSON = json;
		importGraphFromJSON(rappid, json);
		zoomToFit(rappid);
	}

	public toggleJsonEditor(): void {
		this.jsonEditorOpened = !this.jsonEditorOpened;
	}

	private onStart(): void {
		const { rappid } = this;
		loadStencilShapes(rappid);
	}

	private onJsonEditorChange(json: Object): void {
		const { rappid } = this;
		if (rappid) {
			importGraphFromJSON(rappid, json);
		}
	}

	private onRappidGraphChange(json: Object): void {
		this.fileJSON = json;
	}

	private setStencilContainerSize(): void {
		const { renderer, stencil } = this;
		renderer.setStyle(stencil.nativeElement, 'width', `${STENCIL_WIDTH}px`);
	}

	public save() {
		this.workflowForm.get('NAME').markAsTouched();
		this.workflowForm.get('ORDER').markAsTouched();
		this.workflowForm.get('TYPE').markAsTouched();
		if (this.workflowForm.valid && this.fileJSON !== undefined) {
			const [cells, appLinks] = this.triggerDetailService.getCellsAndApplink(
				this.fileJSON
			);
			let countPorts = 0;
			cells.forEach((cell) => {
				const portItems = cell.ports.items;
				portItems.forEach((portItem) => {
					if (portItem.group === 'out') {
						countPorts = countPorts + 1;
					}
				});
			});
			let totallinks = appLinks.length;
			if (totallinks !== countPorts) {
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('NODE_LINK_MISSING'),
				});
				this.loaderService.isLoading = false;
				return;
			}

			this.workflow = this.convertWorkflow(this.fileJSON);
			this.workflow['RAPID_UI_PAYLOAD'] = JSON.stringify(this.fileJSON);

			// TODO: CHECK IF ALL THE NODES ARE INSERTED TO THE STAGES
			if (
				!this.triggerDetailService.validateNodesAddedToStages(
					this.workflow.STAGES,
					this.fileJSON
				)
			) {
				this.loaderService.isLoading = false;
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('NODES_MISSING_STAGE_ID'),
				});
				return;
			}

			if (!this.workflow.WORKFLOW_ID) {
				this.workflowApi
					.postWorkflow(this.module.MODULE_ID, this.workflow)
					.subscribe(
						(success) => {
							this.router.navigate([
								`modules/${this.module.MODULE_ID}/workflows`,
							]);
						},
						(error) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			} else {
				this.workflowApi
					.putWorkflow(this.module.MODULE_ID, this.workflow)
					.subscribe(
						(success) => {
							this.router.navigate([
								`modules/${this.module.MODULE_ID}/workflows`,
							]);
						},
						(error) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			}
		} else {
			this.loaderService.isLoading = false;
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('FILL_REQUIRED_FIELDS'),
			});
		}
	}

	public customizeNode(cell) {
		this.triggerDetailService.loadRelatedFieldsForBody();
		// FIND EXISTING CELL DATA
		const existingCellData = this.triggerDetailService.getSavedValueForCell(
			this.cellData,
			cell.id
		);
		const dialogRef = this.dialog.open(NodeCustomizationComponent, {
			width: '800px',
			data: {
				MODULE: this.module,
				CELL_DATA: existingCellData,
				CELL: cell,
				EMAIL_CHANNELS: this.emailChannels,
				ESCALATIONS: this.escalations,
				PDF_TEMPLATES: this.htmlTemplates,
				BODY_RELATED_FIELDS: this.triggerDetailService.relatedFields,
				BODY_RELATED_NESTED_FIELDS:
					this.triggerDetailService.nestedRelatedFields,
				CELL_NAME: cell.attributes.attrs.label.text,
				STAGES: this.workflow.STAGES,
			},
			disableClose: false,
			maxHeight: '90vh',
		});

		dialogRef.afterClosed().subscribe((result) => {
			if (result) {
				if (result !== 'close') {
					const cellObject = {
						CELL_ID: cell.id,
						VALUE: result[0],
						STAGE_ID: result[1].STAGE_ID,
					};
					const index: number = this.cellData.findIndex(
						(eachcell) => cell.id === eachcell.CELL_ID
					);
					if (index !== -1) {
						// REMOVE THE EXISTING DATA AND REWRITE
						this.cellData.splice(index, 1);
					}
					this.cellData.push(cellObject);
				}
			}
		});
	}

	public openConditionsDialog() {
		const dialogRef = this.dialog.open(ConditionsDialogComponent, {
			width: '800px',
			data: {
				MODULE: this.module,
				CONDITIONS: this.workflow.CONDITIONS,
			},
			disableClose: false,
			maxHeight: '90vh',
		});
		dialogRef.afterClosed().subscribe((result) => {
			if (result !== 'close') {
				this.workflow.CONDITIONS = result;
			}
		});
	}

	public openStagesDialog() {
		const dialogRef = this.dialog.open(StagesComponent, {
			width: '800px',
			data: {
				MODULE: this.module,
				STAGES: this.workflow.STAGES,
				NODE_DATA: this.cellData,
			},
			disableClose: false,
			maxHeight: '90vh',
		});
		dialogRef.afterClosed().subscribe((result) => {
			if (result !== 'close') {
				this.workflow.STAGES = result;
			}
		});
	}

	private convertWorkflow(fileJSON: any) {
		if (!this.workflow.CONDITIONS) {
			this.workflow.CONDITIONS = [];
		}
		const [cells, appLinks] =
			this.triggerDetailService.getCellsAndApplink(fileJSON);
		const allNodes = [];

		// LOOP AND PREPARE THE NODES
		cells.forEach((cell) => {
			const outGroup = cell.ports.items.filter((item) => item.group !== 'in');
			let node = {};
			const savedCellData = this.triggerDetailService.getSavedValueForCell(
				this.cellData,
				cell.id
			);
			// BUILDING DEFAULT PROPERTIES OF A NODE
			node = this.triggerDetailService.buildDefaultNode(
				cell,
				appLinks,
				outGroup,
				savedCellData.VALUE
			);

			switch (cell.type) {
				case 'app.SendEmail':
					node = this.triggerDetailService.buildSendEmailNode(
						savedCellData.VALUE,
						node
					);
					break;
				case 'app.UpdateEntry':
					node = this.triggerDetailService.buildCreateUpdateEntryNode(
						savedCellData.VALUE,
						node,
						this.module.MODULE_ID
					);
					break;
				case 'app.CreateEntry':
					node = this.triggerDetailService.buildCreateUpdateEntryNode(
						savedCellData.VALUE,
						node,
						this.module.MODULE_ID
					);
					break;
				case 'app.StartEscalation':
					node = this.triggerDetailService.buildStartEscalationNode(
						savedCellData.VALUE,
						node
					);
					break;
				case 'app.SendSms':
				case 'app.MakePhoneCall':
					node = this.triggerDetailService.buildMakePhoneCallAndSendSmsNode(
						savedCellData.VALUE,
						node
					);
					break;
				case 'app.Approval':
					node = this.triggerDetailService.buildApprovalNode(
						savedCellData.VALUE,
						node
					);
					break;
				case 'app.GeneratePdf':
					node = this.triggerDetailService.buildPdfNode(
						savedCellData.VALUE,
						node
					);
					break;
				case 'app.MicrosoftTeamsNotification':
					node = this.triggerDetailService.buildMicrosoftTeamsNode(
						savedCellData.VALUE,
						node,
						this.module.MODULE_ID
					);
					break;
				case 'app.SignatureDocument':
					node = this.triggerDetailService.buildSignatureNode(
						savedCellData.VALUE,
						node
					);
					break;
				default:
					break;
			}
			allNodes.push(node);
		});
		// ADD NODES TO THE STAGES
		this.workflow.STAGES = this.triggerDetailService.convertStages(
			allNodes,
			this.workflow.STAGES,
			this.cellData
		);
		return this.workflow;
	}
}
