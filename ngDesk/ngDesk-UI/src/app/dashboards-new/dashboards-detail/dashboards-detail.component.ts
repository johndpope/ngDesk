import {
	ChangeDetectionStrategy,
	Component,
	OnInit,
	ViewEncapsulation,
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DashboardsService } from '../dashboards.service';
import {
	CompactType,
	DisplayGrid,
	Draggable,
	GridsterConfig,
	GridsterItem,
	GridType,
} from 'angular-gridster2';
import { Dashboard, Widget, DashboardApiService } from '@ngdesk/sam-api';
import { ModulesService } from '@src/app/modules/modules.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ConditionsDialogComponent } from '../../dialogs/conditions-dialog/conditions-dialog.component';
import { UUID } from 'angular2-uuid';
import { RolesService } from '@src/app/roles/roles.service';
import { AdditionalFields } from '@src/app/models/additional-field';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { find } from 'lodash';

interface Safe extends GridsterConfig {
	draggable: Draggable;
}

@Component({
	selector: 'app-dashboards-detail',
	templateUrl: './dashboards-detail.component.html',
	styleUrls: ['./dashboards-detail.component.scss'],
	changeDetection: ChangeDetectionStrategy.OnPush,
	encapsulation: ViewEncapsulation.None,
})
export class DashboardsDetailComponent implements OnInit {
	public dashboardForm: FormGroup;
	public allWidgets = [];
	public options: Safe;
	public subOptions: Safe;
	public selectedWidget;
	public widgetAdded = false;
	private dashboardId;
	public modules = [];
	public fields = [];
	public aggregationFields = [];
	public widget: Widget = {};
	public selectedIndex = 0;
	public availableScoreCards = new Map<String, any[]>();
	public dashboard: Dashboard = {
		name: '',
		description: '',
		widgets: [],
	} as Dashboard;
	public moduleId;
	public dashboards = new Array<GridsterItem>();
	public roles = [];
	public additionalFields = [];
	public widgets = [];
	public orderBy = { column: '', order: '' };
	public flexSize = 85;
	public orderByfields = [];
	public widgetMap = new Map<String, String>();
	public filterdFields = [];
	public multiScorecardWidgetIndex = 0;
	public validTimeWindowConditions = true;
	public scoreCardsAddedToMulti = new Map<String, any[]>();
	public advanceCharts = [
		{
			TITLE: this.translateService.instant('MULTI_SCORE_CARDS'),
			ICON: 'score',
			TYPE: 'multiscore',
		},
		{
			TITLE: this.translateService.instant('ADVANCED_PIE_CHART'),
			ICON: 'pie_chart',
			TYPE: 'advanced_pie_chart',
		},
	];
	public aggregateTypes = ['sum', 'count', 'max', 'average', 'min'];

	constructor(
		private formBuilder: FormBuilder,
		private dashboardService: DashboardsService,
		private dashboardApiService: DashboardApiService,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private router: Router,
		private route: ActivatedRoute,
		private dialog: MatDialog,
		private rolesService: RolesService,
		private loaderService: LoaderService
	) {}

	public ngOnInit() {
		this.initializeForms();
		this.allWidgets = this.dashboardService.getAllWidgets();
		this.initializeGridster();
		this.getAllModules();
		this.dashboardId = this.route.snapshot.params['id'];
		this.getRoles();
		if (this.dashboardId !== 'new') {
			this.getDashboard(this.dashboardId);
		}
	}

	// Initialize gridster component
	public initializeGridster() {
		this.options = {
			gridType: GridType.Fit,
			compactType: CompactType.None,
			margin: 0,
			outerMargin: true,
			outerMarginTop: null,
			outerMarginRight: null,
			outerMarginBottom: null,
			outerMarginLeft: null,
			useTransformPositioning: true,
			mobileBreakpoint: 640,
			minCols: 8,
			maxCols: 10,
			minRows: 8,
			maxRows: 100,
			maxItemCols: 100,
			minItemCols: 1,
			maxItemRows: 100,
			minItemRows: 1,
			maxItemArea: 2500,
			minItemArea: 1,
			defaultItemCols: 1,
			defaultItemRows: 1,
			fixedColWidth: 105,
			fixedRowHeight: 105,
			keepFixedHeightInMobile: false,
			keepFixedWidthInMobile: false,
			scrollSensitivity: 10,
			scrollSpeed: 20,
			enableEmptyCellClick: false,
			enableEmptyCellContextMenu: false,
			enableEmptyCellDrop: false,
			enableEmptyCellDrag: false,
			enableOccupiedCellDrop: false,
			emptyCellDragMaxCols: 50,
			emptyCellDragMaxRows: 50,
			ignoreMarginInRow: false,
			draggable: {
				enabled: true,
			},
			resizable: {
				enabled: true,
			},
			swap: false,
			pushItems: true,
			disablePushOnDrag: false,
			disablePushOnResize: false,
			pushDirections: { north: true, east: true, south: true, west: true },
			pushResizeItems: false,
			displayGrid: DisplayGrid.Always,
			disableWindowResize: false,
			disableWarnings: false,
			scrollToNewItems: false,
			itemResizeCallback: this.itemResize,
		};
	}

	// Get all the roles.
	public getRoles() {
		this.rolesService.getRoles().subscribe((rolesResponse: any) => {
			rolesResponse['ROLES'].filter((role) => {
				if (role.NAME === 'Customers') {
					role['NAME'] = 'Customer';
				}
			});
			this.roles = rolesResponse.ROLES.filter(
				(role) =>
					role.NAME !== 'Public' &&
					role.NAME !== 'ExternalProbe' &&
					role.NAME !== 'LimitedUser'
			);
			this.roles = this.roles.sort((a, b) => a.NAME.localeCompare(b.NAME));
			this.additionalFields.push(
				new AdditionalFields(
					'ROLE',
					'ROLE',
					'list',
					'ROLE_ID',
					'ROLE',
					this.roles,
					'NAME',
					'role'
				)
			);
		});
	}

	// Get dashboard entry based on id
	public getDashboard(id) {
		this.dashboardService.getDashboard(id).subscribe((dashboardResponse) => {
			this.dashboardForm.controls['NAME'].setValue(
				dashboardResponse['DATA'].name
			);
			this.dashboardForm.controls['DESCRIPTION'].setValue(
				dashboardResponse['DATA'].description
			);
			this.dashboardForm.controls['ROLE'].setValue(
				dashboardResponse['DATA'].role.roleId
			);
			this.dashboard.name = dashboardResponse['DATA'].name;
			this.dashboard.description = dashboardResponse['DATA'].description;
			this.widgets = dashboardResponse['DATA'].widgets;
			this.widgets.forEach((widget) => {
				const selectedWidet = dashboardResponse['DATA'].widgets.find(
					(wdgt) => widget.widgetId === wdgt.widgetId
				);
				if (!selectedWidet['orderBy']) {
					widget['orderBy'] = { column: '', order: '' };
				} else {
					widget['orderBy'] = {
						column: selectedWidet.orderBy.column,
						order: selectedWidet.orderBy.order,
					};
				}
			});
			let availableScoreCards = [];

			this.widgets.forEach((widget) => {
				if (widget.type === 'score') {
					availableScoreCards.push(widget);
				}
			});

			let widgetScoreCards = [];
			this.widgets.forEach((widget) => {
				availableScoreCards.forEach((element) => {
					widgetScoreCards.push(element);
				});
				if (widget.type === 'multi-score') {
					if (widget.multiScorecards.length > 0) {
						widget.multiScorecards.forEach((element) => {
							widgetScoreCards.push(element);
						});
					}
					this.scoreCardsAddedToMulti.set(widget.widgetId, []);
					this.availableScoreCards.set(widget.widgetId, widgetScoreCards);
					widgetScoreCards = [];
				}
			});
			this.initializeDashboardWidgets(this.widgets);
		});
	}

	// Initialize the gridster items while editing the dashboard.
	public initializeDashboardWidgets(widgets) {
		widgets.forEach((widget) => {
			const gridDimensions = this.calculatepositionsAndSizePxToGrid(widget);
			if (widget.type === 'score') {
				this.dashboards.push({
					x: gridDimensions.positionX,
					y: gridDimensions.positionY,
					cols: gridDimensions.width,
					rows: gridDimensions.height,
					id: widget.widgetId,
					minItemRows: 1,
					minItemCols: 1,
					maxItemRows: 1,
					maxItemCols: 1,
					label: widget.title,
					type: widget.type,
				});
			} else if (widget.type === 'multi-score') {
				this.dashboards.push({
					x: gridDimensions.positionX,
					y: gridDimensions.positionY,
					cols: gridDimensions.width,
					rows: gridDimensions.height,
					id: widget.widgetId,
					minItemRows: 2,
					minItemCols: 3,
					label: widget.title,
					type: widget.type,
					multiScorecards: widget.multiScorecards,
				});
			} else {
				this.dashboards.push({
					x: gridDimensions.positionX,
					y: gridDimensions.positionY,
					cols: gridDimensions.width,
					rows: gridDimensions.height,
					id: widget.widgetId,
					minItemRows: 2,
					minItemCols: 3,
					label: widget.title,
					type: widget.type,
				});
			}
			this.widgetMap.set(widget['widgetId'], widget['title']);
		});
		this.selectedWidget = widgets[widgets.length - 1];
	}

	// When resizing the elements
	public itemResize(item): void {
		if (this.widgets) {
			this.selectedWidget = this.widgets.find(
				(dashboardWidget) => dashboardWidget.widgetId === item.widgetId
			);
			this.selectedIndex = this.widgets.indexOf(this.selectedWidget);
			this.widgets[this.selectedIndex].x = item.x;
			this.widgets[this.selectedIndex].y = item.y;
		}
	}

	// Fetch all modules for modules dropdown.
	public getAllModules() {
		this.modulesService.getModules().subscribe((modulesResponse) => {
			if (modulesResponse) {
				this.modules = modulesResponse['MODULES'].sort((a, b) =>
					a.NAME.localeCompare(b.NAME)
				);
			}
		});
	}

	// On select of module set the fields.
	public onSelectModule(event) {
		this.moduleId = event.value;
		const existingModule = this.modules.find(
			(module) => module.MODULE_ID === event.value
		);
		if (this.modules.length > 0 && existingModule) {
			this.moduleId = existingModule['MODULE_ID'];
			this.fields = existingModule.FIELDS;
			this.fields = this.fields.sort((a, b) =>
				a.DATA_TYPE.DISPLAY.localeCompare(b.DATA_TYPE.DISPLAY)
			);

			this.fields = this.fields.filter(
				(field) => field.DATA_TYPE.DISPLAY !== 'PDF'
			);
			this.aggregationFields = this.fields;
			this.aggregationFields = this.aggregationFields.filter(
				(field) =>
					field.DATA_TYPE.BACKEND === 'Integer' ||
					field.DATA_TYPE.BACKEND === 'Float' ||
					field.DATA_TYPE.BACKEND === 'Double'
			);
			// Filtered fields to not show Many to Many or One to Many fields
			this.orderByfields = this.fields.filter(
				(field) =>
					field.DATA_TYPE.DISPLAY !== 'Relationship' &&
					field.DATA_TYPE.DISPLAY !== 'Discussion' &&
					field.DATA_TYPE.DISPLAY !== 'File Upload' &&
					field.DATA_TYPE.DISPLAY !== 'Image' &&
					field.DATA_TYPE.DISPLAY !== 'Picklist' &&
					field.DATA_TYPE.DISPLAY !== 'Checkbox' &&
					field.DATA_TYPE.DISPLAY !== 'Email'
			);
			this.filterdFields = this.fields.filter(
				(field) =>
					field.DATA_TYPE.DISPLAY !== 'Relationship' &&
					field.DATA_TYPE.DISPLAY !== 'Discussion' &&
					field.DATA_TYPE.DISPLAY !== 'File Upload' &&
					field.DATA_TYPE.DISPLAY !== 'Image' &&
					field.DATA_TYPE.DISPLAY !== 'Picklist' &&
					field.DATA_TYPE.DISPLAY !== 'Checkbox' &&
					field.DATA_TYPE.DISPLAY !== 'Email'
			);
			if (this.widgets[this.selectedIndex]) {
				this.widgets[this.selectedIndex].moduleId = this.moduleId;
			}
		}
	}

	// Initializes the dashboard form
	public initializeForms() {
		this.dashboardForm = this.formBuilder.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
			ROLE: ['', Validators.required],
		});
	}

	public changedOptions(): void {
		if (this.options.api && this.options.api.optionsChanged) {
			this.options.api.optionsChanged();
		}
	}

	// Add widget chart
	public addItem(item): void {
		if (item.TYPE === 'score') {
			this.dashboards.push({
				x: 0,
				y: 0,
				cols: 1,
				rows: 1,
				id: UUID.UUID(),
				minItemRows: 1,
				minItemCols: 1,
				maxItemRows: 1,
				maxItemCols: 1,
				label: item.TYPE,
				type: item.TYPE,
			});
			const index = this.dashboards.length - 1;
			const currDashboard = this.dashboards[index];
			this.initializeWidget(item, currDashboard);
		} else if (item.TYPE === 'multi-score') {
			this.dashboards.push({
				x: 0,
				y: 0,
				cols: 3,
				rows: 2,
				id: UUID.UUID(),
				minItemRows: 2,
				minItemCols: 3,
				label: item.TYPE,
				type: item.TYPE,
				multiScorecards: [],
			});
			const index = this.dashboards.length - 1;
			const currDashboard = this.dashboards[index];
			this.initializeWidget(item, currDashboard);
		} else {
			this.dashboards.push({
				x: 0,
				y: 0,
				cols: 3,
				rows: 2,
				id: UUID.UUID(),
				minItemRows: 2,
				minItemCols: 3,
				label: item.TYPE,
				type: item.TYPE,
			});
			const index = this.dashboards.length - 1;
			const currDashboard = this.dashboards[index];
			this.initializeWidget(item, currDashboard);
		}
	}

	// Widget object initialization.
	public initializeWidget(item, dashboard) {
		let widget = {};
		if (item.TYPE === 'score') {
			widget = {
				widgetId: dashboard.id,
				title: item.TYPE,
				moduleId: this.moduleId,
				type: item.TYPE,
				positionX: dashboard.x,
				positionY: dashboard.y,
				aggregateType: 'count',
				dashboardconditions: [],
				limit: 0,
				limitEntries: false,
				orderBy: { column: '', order: '' },
				width: 0,
				height: 0,
			};
			let availableScoreCardsKeys = Array.from(this.availableScoreCards.keys());
			availableScoreCardsKeys.forEach((availableScoreCardKey) => {
				let availableScoreCard = this.availableScoreCards.get(
					availableScoreCardKey
				);
				availableScoreCard.push(widget);
				this.availableScoreCards.set(availableScoreCardKey, availableScoreCard);
			});
		} else if (item.TYPE === 'multi-score') {
			widget = {
				widgetId: dashboard.id,
				title: item.TYPE,
				type: item.TYPE,
				moduleId: '',
				orderBy: { column: '', order: '' },
				positionX: dashboard.x,
				dashboardconditions: [],
				positionY: dashboard.y,
				width: 0,
				height: 0,
				multiScorecards: [],
			};
			this.scoreCardsAddedToMulti.set(dashboard.id, []);
			let availableScoreCards = [];
			this.widgets.forEach((element) => {
				if (element.type === 'score') {
					availableScoreCards.push(element);
				}
			});
			this.availableScoreCards.set(dashboard.id, availableScoreCards);
		} else {
			widget = {
				widgetId: dashboard.id,
				title: item.TYPE,
				moduleId: this.moduleId,
				type: item.TYPE,
				positionX: dashboard.x,
				positionY: dashboard.y,
				aggregateType: 'count',
				dashboardconditions: [],
				limit: 0,
				limitEntries: false,
				orderBy: { column: '', order: '' },
				field: '',
				width: 0,
				height: 0,
			};
		}
		this.widgets.push(widget);
		this.widgetMap.set(widget['widgetId'], widget['title']);
		this.selectedWidget = this.widgets.find(
			(dashboardWidget) => dashboardWidget.widgetId === widget['widgetId']
		);
		this.selectedIndex = this.widgets.indexOf(this.selectedWidget);
	}

	public onChnageTitle(widgetId, event) {
		if (event) {
			this.widgetMap.set(widgetId, event);
		} else {
			this.widgetMap.set(widgetId, '');
		}
	}

	// Calculate the widget height and width.
	public calculatePositionAndWidthGridToPx(widgets) {
		let positionx = 15;
		let positionY = 15;
		widgets.forEach((widget) => {
			const dashboard = this.dashboards.find(
				(dashboard) => dashboard.id === widget.widgetId
			);
			if (widget.type === 'score') {
				widget.width = 200;
				widget.height = 100;
				if (dashboard.x === 0) {
					widget.positionX = positionx;
				} else {
					widget.positionX = positionx + dashboard.x * 200 + 10 * dashboard.x;
				}
				if (dashboard.y === 0) {
					widget.positionY = positionY;
				} else {
					widget.positionY = positionY + dashboard.y * 100 + 10 * dashboard.y;
				}
			} else {
				widget.width = 200 * dashboard.cols;
				widget.height = 100 * dashboard.rows;
				if (dashboard.x === 0) {
					widget.positionX = positionx;
				} else {
					widget.positionX = positionx + dashboard.x * 200 + 10 * dashboard.x;
				}
				if (dashboard.y === 0) {
					widget.positionY = positionY;
				} else {
					widget.positionY = positionY + dashboard.y * 100 + 10 * dashboard.y;
				}
			}
		});
	}

	// Remove the widgets on click of remove button.
	public removeItem(event, item) {
		event.preventDefault();
		event.stopPropagation();
		this.dashboards.splice(this.dashboards.indexOf(item), 1);
		this.selectedWidget = this.widgets.find(
			(dashboardWidget) => dashboardWidget.widgetId === item.id
		);
		this.widgets.splice(this.widgets.indexOf(this.selectedWidget), 1);
		let widgetIds = Array.from(this.availableScoreCards.keys());
		widgetIds.forEach((widgetId) => {
			let availableScoreCards = this.availableScoreCards.get(widgetId);
			availableScoreCards.splice(this.widgets.indexOf(this.selectedWidget), 1);
			this.availableScoreCards.set(widgetId, availableScoreCards);
		});

		//this.selectedIndex = this.widgets.length - 1;
		this.widgetAdded = false;
	}

	// Responsible for the save and update of dashboard.
	public save() {
		this.widgets.forEach((element) => {
			if (element.type === 'multi-score') {
				element.moduleId = '606be303a46ed03f35c03f56';
				element.orderBy.column = '9d3c5936-21d8-44cd-b523-6732c9386650';
				element.orderBy.order = 'Asc';
				element.aggregateType = 'count';
				element.aggregateField = '';
			}
			if (element.aggregateType === null) {
				element.aggregateType = 'count';
			}

			if (element.aggregateType === 'count') {
				element.aggregateField = null;
			}
		});
		const dashboard = {
			name: this.dashboardForm.value['NAME'],
			role: this.dashboardForm.value['ROLE'],
			description: this.dashboardForm.value['DESCRIPTION'],
			widgets: this.widgets,
		};
		this.validateTimeWindowConditions(dashboard.widgets);
		if (this.validTimeWindowConditions) {
			this.calculatePositionAndWidthGridToPx(dashboard.widgets);
			let payload;
			this.loaderService.isLoading = true;
			if (this.dashboardId === 'new') {
				payload = JSON.parse(JSON.stringify(dashboard));
				this.dashboardApiService.postDashboard(payload).subscribe(
					(dashboardResponse) => {
						if (dashboardResponse) {
							this.router.navigate([`dashboards`]);
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SAVED_SUCCESSFULLY'),
							});
						}
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			} else {
				// Update call for existing dashboard.
				dashboard['dashboardId'] = this.dashboardId;
				payload = JSON.parse(JSON.stringify(dashboard));
				this.dashboardApiService.putDashboard(payload).subscribe(
					(dashboardResponse) => {
						if (dashboardResponse) {
							this.router.navigate([`dashboards`]);
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
							});
						}
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			}
		}
		this.loaderService.isLoading = false;
	}

	// set dashboard x and y axis values.
	public updateWidgetPositions() {
		this.dashboards.forEach((dashboard) => {
			const widget = this.widgets.find(
				(widgt) => dashboard.id === widgt.widgetId
			);
			const index = this.widgets.indexOf(widget);
			this.widgets[index].positionX = dashboard.x;
			this.widgets[index].positionY = dashboard.y;
		});
	}

	// On click of conditions button open condition dialog.
	public openConditionsDialog() {
		const dialogRef = this.dialog.open(ConditionsDialogComponent, {
			width: '800px',
			data: {
				PARENT_COMPONENT: 'dashboardsComponent',
				MODULE: this.modules.find(
					(module) => module.MODULE_ID === this.moduleId
				).MODULE_ID,
				CONDITIONS: this.widgets[this.selectedIndex].dashboardconditions
					? this.widgets[this.selectedIndex].dashboardconditions
					: [],
			},
			disableClose: false,
			maxHeight: '90vh',
		});
		dialogRef.afterClosed().subscribe((result) => {
			if (result !== 'close') {
				this.widgets[this.selectedIndex].dashboardconditions = result;
			}
		});
	}

	// calls on click of widget.
	public editWidget(item) {
		this.widgetAdded = true;
		this.selectedWidget = this.widgets.find(
			(dashboardWidget) => dashboardWidget.widgetId === item.id
		);
		this.selectedIndex = this.widgets.indexOf(this.selectedWidget);
		this.moduleId = this.selectedWidget.moduleId;
		const event = { value: this.moduleId };
		this.onSelectModule(event);
		if (this.widgetAdded) {
			this.flexSize = 65;
		} else {
			this.flexSize = 85;
		}
	}

	public validateTimeWindowConditions(widgets) {
		const timeWindow = this.fields.find(
			(field) => field.NAME === 'TIME_WINDOW'
		);

		widgets.forEach((widget) => {
			if (widget.dashboardconditions.length > 0) {
				widget.dashboardconditions.forEach((dashboardCondition) => {
					if (
						timeWindow &&
						dashboardCondition.condition === timeWindow.FIELD_ID
					) {
						let value = new String();
						value = dashboardCondition.value;
						const period = value.substring(0, value.indexOf('('));
						const firstOperand = value.substring(
							value.indexOf('(') + 1,
							value.indexOf('-')
						);
						const secondOperand = value.substring(
							value.indexOf('-') + 1,
							value.indexOf(')')
						);
						if (period !== 'days' && period !== 'months') {
							this.bannerMessageService.errorNotifications.push({
								message: 'Invalid period, please type days | months',
							});
							this.validTimeWindowConditions = false;
						}
						if (firstOperand !== 'current_date') {
							this.bannerMessageService.errorNotifications.push({
								message:
									'Invalid operand on the time window, please type current_date',
							});
							this.validTimeWindowConditions = false;
						}
						if (isNaN(parseInt(secondOperand))) {
							this.bannerMessageService.errorNotifications.push({
								message:
									'Invalid operand please enter a number instead of ' +
									secondOperand,
							});
							this.validTimeWindowConditions = false;
						}
					}
				});
			}
		});
	}
	public multiScoreOnChange(event, widget) {
		let rowOrColumnAdded = false;
		widget.multiScorecards.forEach((element) => {
			const widgetPresent = this.widgets.find(
				(dashboardWidget) => dashboardWidget.widgetId === element.widgetId
			);
			if (widgetPresent) {
				let multiScoreDashboard = this.dashboards.find(
					(dashboardWidget) => dashboardWidget.id === widget.widgetId
				);
				const indexOfDashboard = this.dashboards.indexOf(multiScoreDashboard);
				if (!rowOrColumnAdded) {
					rowOrColumnAdded = true;

					if (
						widget.multiScorecards.length >
						multiScoreDashboard.cols * multiScoreDashboard.rows
					) {
						if (multiScoreDashboard.rows > multiScoreDashboard.cols) {
							multiScoreDashboard.cols = multiScoreDashboard.cols + 1;
						} else {
							multiScoreDashboard.rows = multiScoreDashboard.rows + 1;
						}
					}
					this.dashboards[indexOfDashboard] = multiScoreDashboard;
				}

				const dashboard = this.dashboards.find(
					(dashboardWidget) => dashboardWidget.id === element.widgetId
				);
				this.dashboards.splice(this.dashboards.indexOf(dashboard), 1);

				this.widgets.splice(this.widgets.indexOf(element), 1);
				this.widgetMap.delete(element['widgetId']);
				let availableScoreCardsKeys = Array.from(
					this.availableScoreCards.keys()
				);
				availableScoreCardsKeys.forEach((widgetId) => {
					if (widgetId !== widget.widgetId) {
						event.forEach((element) => {
							let widgetsPresent = this.availableScoreCards
								.get(widgetId)
								.find(
									(dashboardWidget) =>
										element.widgetId === dashboardWidget.widgetId
								);
							if (widgetsPresent !== undefined) {
								let widgetsArray = this.availableScoreCards.get(widgetId);

								widgetsArray.splice(widgetsArray.indexOf(element), 1);

								this.availableScoreCards.set(widgetId, widgetsArray), 1;
							}
						});
					}
				});

				let tempscoreCardsAddedToMulti = [];
				tempscoreCardsAddedToMulti = this.scoreCardsAddedToMulti.get(
					widget.widgetId
				);
				tempscoreCardsAddedToMulti.push(element);
				this.scoreCardsAddedToMulti.set(
					widget.widgetId,
					tempscoreCardsAddedToMulti
				);
				this.selectedIndex = this.widgets.indexOf(widget);
			}
		});
		if (this.scoreCardsAddedToMulti.has(widget.widgetId)) {
			let tempscoreCardsAddedToMulti = [];
			tempscoreCardsAddedToMulti = this.scoreCardsAddedToMulti.get(
				widget.widgetId
			);

			this.scoreCardsAddedToMulti.get(widget.widgetId).forEach((element) => {
				const widgetPresent = widget.multiScorecards.find(
					(dashboardWidget) => dashboardWidget.widgetId === element['widgetId']
				);
				if (widgetPresent === null || widgetPresent === undefined) {
					this.dashboards.push({
						x: 0,
						y: 0,
						cols: 1,
						rows: 1,
						id: element['widgetId'],
						minItemRows: 1,
						minItemCols: 1,
						maxItemRows: 1,
						maxItemCols: 1,
						label: element['title'],
						type: element['type'],
					});
					this.widgetMap.set(element['widgetId'], element['title']);
					tempscoreCardsAddedToMulti.splice(
						tempscoreCardsAddedToMulti.indexOf(element),
						1
					);
					this.widgets.push(element);
					// this.availableScoreCards.push(element);
					this.selectedIndex = this.widgets.indexOf(widget);
				}
			});
			this.scoreCardsAddedToMulti.set(
				widget.widgetId,
				tempscoreCardsAddedToMulti
			);
		}
	}

	public unlinkScoreFromMultiScore(multiScorewidget, scoreWidget) {
		const indexOfScore = multiScorewidget.multiScorecards.indexOf(scoreWidget);
		const indexOfMulti = this.widgets.indexOf(multiScorewidget);
		this.widgets[indexOfMulti].multiScorecards.splice(indexOfScore, 1);
		let scoreCardsTemp = [];
		scoreCardsTemp = this.scoreCardsAddedToMulti.get(multiScorewidget.widgetId);
		const widgetPresent = scoreCardsTemp.find(
			(dashboardWidget) => dashboardWidget.widgetId === scoreWidget['widgetId']
		);
		if (widgetPresent) {
			scoreCardsTemp.splice(scoreCardsTemp.indexOf(scoreWidget), 1);
			this.scoreCardsAddedToMulti.set(
				multiScorewidget.widgetId,
				scoreCardsTemp
			);
		}
		this.dashboards.push({
			x: 0,
			y: 0,
			cols: 1,
			rows: 1,
			id: scoreWidget['widgetId'],
			minItemRows: 1,
			minItemCols: 1,
			maxItemRows: 1,
			maxItemCols: 1,
			label: scoreWidget['title'],
			type: scoreWidget['type'],
		});
		this.widgetMap.set(scoreWidget['widgetId'], scoreWidget['title']);
		this.widgets.push(scoreWidget);
		let availableScoreCardsKeys = Array.from(this.availableScoreCards.keys());
		availableScoreCardsKeys.forEach((widgetId) => {
			let availableScoreCards = this.availableScoreCards.get(widgetId);
			const widgetPresentInAvaialable = availableScoreCards.find(
				(dashboardWidget) =>
					dashboardWidget.widgetId === scoreWidget['widgetId']
			);
			if (
				widgetPresentInAvaialable === undefined ||
				widgetPresentInAvaialable === null
			) {
				availableScoreCards.push(scoreWidget);
				this.availableScoreCards.set(widgetId, availableScoreCards);
			}
		});
		this.widgetAdded = false;
	}

	public deleteScoreFromMultiScore(multiScorewidget, scoreWidget) {
		const indexOfScore = multiScorewidget.multiScorecards.indexOf(scoreWidget);
		const indexOfMulti = this.widgets.indexOf(multiScorewidget);
		this.widgets[indexOfMulti].multiScorecards.splice(indexOfScore, 1);
		let availableScoreCards = this.availableScoreCards.get(
			multiScorewidget.widgetId
		);
		availableScoreCards.splice(availableScoreCards.indexOf(scoreWidget), 1);
		this.availableScoreCards.set(multiScorewidget, availableScoreCards);
		this.widgetAdded = false;
	}

	public calculatepositionsAndSizePxToGrid(widget) {
		const positionX = widget.positionX;
		const positionY = widget.positionY;
		const width = widget.width;
		const height = widget.height;
		let gridPositionX = 0;
		let gridPositionY = 0;
		let gridWidth = 0;
		let gridHeight = 0;

		if (positionX === 15) {
			gridPositionX = 0;
		} else {
			gridPositionX = Math.round(positionX / 210);
		}

		if (positionY === 15) {
			gridPositionY = 0;
		} else {
			gridPositionY = Math.round(positionY / 110);
		}

		gridWidth = Math.round(width / 200);
		gridHeight = Math.round(height / 100);

		if (widget.type !== 'score') {
			if (gridWidth < 3) {
				gridWidth = 3;
			}
			if (gridHeight < 2) {
				gridHeight = 2;
			}
		}
		let positionAndSizeObject = {
			positionX: gridPositionX,
			positionY: gridPositionY,
			width: gridWidth,
			height: gridHeight,
		};

		return positionAndSizeObject;

		// 		if (widget.type === 'score') {
		// 			widget.width = 200;
		// 			widget.height = 100;
		// 			if (dashboard.x === 0) {
		// 				widget.positionX = positionx;
		// 			} else {
		// 				widget.positionX = positionx + dashboard.x * 200 + 10 * dashboard.x;
		// 			}
		// 			if (dashboard.y === 0) {
		// 				widget.positionY = positionY;
		// 			} else {
		// 				widget.positionY = positionY + dashboard.y * 200 + 10 * dashboard.y;
		// 			}
		// 		} else {
		// 			widget.width = 200 * dashboard.cols;
		// 			widget.height = 100 * dashboard.rows;
		// 			if (dashboard.x === 0) {
		// 				widget.positionX = positionx;
		// 			} else {
		// 				widget.positionX = positionx + dashboard.x * 200 + 10 * dashboard.x;
		// 			}
		// 			if (dashboard.y === 0) {
		// 				widget.positionY = positionY;
		// 			} else {
		// 				widget.positionY = positionY + dashboard.y * 200 + 10 * dashboard.y;
		// 			}
		// 		}
		// }
	}
}
