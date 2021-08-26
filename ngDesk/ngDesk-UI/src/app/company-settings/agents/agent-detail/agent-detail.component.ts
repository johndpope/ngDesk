import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';

import {
	Controller,
	ControllersApiService,
	LogsApiService,
	SubApp,
} from '@ngdesk/sam-api';
import { WebsocketService } from '@src/app/websocket.service';
import { Subject, Subscription } from 'rxjs';

@Component({
	selector: 'app-agent-detail',
	templateUrl: './agent-detail.component.html',
	styleUrls: ['./agent-detail.component.scss'],
})
export class AgentDetailComponent implements OnInit, OnDestroy {
	public controller: Controller = {
		HOST_NAME: '',
		SUB_APPS: [],
		STATUS: '',
		UPDATER_STATUS: '',
		LOGS: [],
		COMPANY_ID: '',
		INSTRUCTIONS: [],
	} as Controller;

	public limitSelected = 100;
	public logMessages: any = [];
	public sortBy: string;
	public sortOrder: string;
	public applications = [{ NAME: 'ngDesk-Controller', LOG_LEVEL: '' }];
	public applicationName: string;
	public displayedColumns: string[] = [
		// To-do: Need to add when it's needed.
		// 'select',
		'NAME',
		'LAST_SEEN',
		'STATUS',
		'ACTION',
	];
	public logs = ['ALL', 'INFO', 'SEVERE', 'WARN', 'FINE', 'OFF'];
	public selectedApplication = { NAME: 'ngDesk-Controller', LOG_LEVEL: 'ALL' };
	public errorMessage: string;
	public showSubApps = true;
	public isLoading = true;
	public limits = [100, 200, 300, 400, 500];
	public logIntervalTime;
	private controllerId: string;
	public tabType;
	public controllerTabIndex = 0;
	public dataSource = new MatTableDataSource<SubApp>(this.controller.SUB_APPS);
	public selection = new SelectionModel<SubApp>(true, []);
	public streamLogs = true;
	private updateLogsSubscription: Subscription;
	private _destroyed$ = new Subject();

	public actions = [
		{
			NAME: this.translateService.instant('VIEW_LOGS'),
			ACTION: 'VIEW_LOGS',
		},
		{
			NAME: this.translateService.instant('START'),
			ACTION: 'START',
		},
		{
			NAME: this.translateService.instant('STOP'),
			ACTION: 'STOP',
		},
	];
	@ViewChild(MatSort, { static: false }) public sort: MatSort;
	@ViewChild(MatPaginator, { static: false }) public paginator: MatPaginator;

	constructor(
		private controllersApiService: ControllersApiService,
		private logsApiService: LogsApiService,
		private route: ActivatedRoute,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private websocketService: WebsocketService
	) {}

	public ngOnInit() {
		this.isLoading = true;
		this.controllerId = this.route.snapshot.params['controllerId'];
		this.tabType = 'Applications';

		// Intialize the sort by and sor order for the sub apps.
		this.sortBy = 'NAME';
		this.sortOrder = 'asc';

		// fetching the controller.
		this.getController();

		this.getLogMessages();
		this.updateLogs();
	}

	// Get all the sub apps from the controller and set to the data table.
	public setSubAppsToDataTable(controller) {
		if (controller) {
			// Get all sub apps from the controller and pushing all to application list.
			controller.SUB_APPS.forEach((element) => {
				this.applications.push(element);
			});

			// If the sub apps list is zero then don't show the table.
			if (this.controller.SUB_APPS.length < 1) {
				this.showSubApps = false;
			} else {
				// Set the sub apps list to the data table.
				this.dataSource = new MatTableDataSource<SubApp>(
					this.controller.SUB_APPS
				);
			}
		}
	}

	public ngOnDestroy() {
		this._destroyed$.next();
		this._destroyed$.complete();
		if (this.updateLogsSubscription && this.updateLogsSubscription !== null) {
			this.updateLogsSubscription.unsubscribe();
		}
	}

	private updateLogs() {
		this.updateLogsSubscription = this.websocketService.logNotification.subscribe(
			(log) => {
				if (
					this.streamLogs &&
					this.selectedApplication.NAME === log['APPLICATION']
				) {
					if (this.logMessages.length > 0) {
						if (this.logMessages.length >= this.limitSelected) {
							this.logMessages.shift();
						}
						this.logMessages.push(log);
					}
				}
			}
		);
	}

	// Whether the number of selected elements matches the total number of rows.
	public isAllSelected() {
		const numSelected = this.selection.selected.length;
		const numRows = this.dataSource.data.length;
		return numSelected === numRows;
	}

	// Selects all rows if they are not all selected; otherwise clear selection.
	public masterToggle() {
		this.isAllSelected()
			? this.selection.clear()
			: this.dataSource.data.forEach((row) => this.selection.select(row));
	}

	// The label for the checkbox on the passed row
	public checkboxLabel(row?: SubApp): string {
		if (!row) {
			return `${this.isAllSelected() ? 'select' : 'deselect'} all`;
		}
		return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${
			row.NAME + 1
		}`;
	}

	// on selection of application Name and limit displaying logMessage
	public getLogMessages() {
		this.applicationName = this.selectedApplication.NAME;
		this.logsApiService
			.getLogs(this.controllerId, this.applicationName, this.limitSelected)
			.subscribe(
				(controllerLogResponse) => {
					this.logMessages = controllerLogResponse['content'];
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
	}

	// This function will update the agent on click of update button.
	public updateAgent() {
		this.postInstruction(
			this.controllerId,
			'ngDesk-Controller',
			null,
			'UPDATE'
		);
	}

	// This function will reatart the agent on click of restart button.
	public restartAgent() {
		// First it will stop the agent and on success of it it will start the agent.
		// TODO:Remove the hardcoded values for instruction object
		const instruction = {
			APPLICATION_NAME: 'ngDesk-Controller',
			ACTION: 'STOP',
			LOG_LEVEL: null,
		};
		const controllerInstruction = {
			CONTROLLER_ID: this.controllerId,
			INSTRUCTION: instruction,
		};
		this.websocketService.publishMessage(controllerInstruction);
	}

	// This function will be called when the menu items in the sub app tables will be clicked.
	public menuItemClicked(action, row) {
		let successMessage = '';
		// There should be instruction post call other than View Log action.
		if (action.ACTION !== 'VIEW_LOGS') {
			if (action.NAME === 'Start' || action.NAME === 'Stop') {
				this.translateService
					.get('APP_ACTION_QUEUED', {
						application: row.NAME,
						action: this.translateService.instant(action.ACTION),
					})
					.subscribe((res) => {
						successMessage = res;
					});
			} else {
				successMessage = '';
			}
			this.postInstruction(this.controllerId, row.NAME, null, action.ACTION);
		} else {
			// SELECT THE VIEW LOGS TAB IF VIEW LOGS BUTTON IS CLICKED
			this.tabType = 'Logs';
			const tabCount = 2;
			this.controllerTabIndex = (this.controllerTabIndex + 1) % tabCount;
			this.selectedApplication = this.applications.find(
				(app) => app.NAME === row.NAME
			);
			this.applicationChanged({ value: this.selectedApplication.NAME });
			this.getLogMessages();
		}
	}

	// This function will post instruction for the Agent.
	public postInstruction(controllerId, applicationName, logLevel, action) {
		const instruction = {
			APPLICATION_NAME: applicationName,
			ACTION: action,
			LOG_LEVEL: logLevel,
		};
		const controllerInstruction = {
			CONTROLLER_ID: controllerId,
			INSTRUCTION: instruction,
		};
		this.websocketService.publishMessage(controllerInstruction);
	}

	// get controller from api and render in UI
	private getController() {
		this.controllersApiService.getControllerById(this.controllerId).subscribe(
			(controllerResponse: Controller) => {
				this.isLoading = false;
				this.controller = controllerResponse;
				// this.controller.STATUS = 'Online';
				this.setSubAppsToDataTable(this.controller);
				this.applicationChanged({ value: this.selectedApplication.NAME });

				// SETTING PAGINATOR DATA
				setTimeout(() => (this.dataSource.paginator = this.paginator));
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);
	}

	// Set the tab name on select of tabs
	public onSelectTabs($event) {
		this.tabType = $event.tab.textLabel;
		// This function will start fetching logs in every 30 seconds.
		this.getLogMessages();
	}

	// used to post instruction for change of log level
	public changeLogLevel(event) {
		this.postInstruction(
			this.controllerId,
			this.selectedApplication.NAME,
			event.value,
			'LOG_UPDATE'
		);
	}

	// whne application is changed for viewing logs
	public applicationChanged(event) {
		this.logMessages = [];
		// reload agent logs
		this.getLogMessages();
	}
}
