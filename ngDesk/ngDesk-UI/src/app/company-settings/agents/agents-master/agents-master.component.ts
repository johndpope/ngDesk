import {
	animate,
	state,
	style,
	transition,
	trigger,
} from '@angular/animations';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ControllersApiService } from '@ngdesk/sam-api';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { WebsocketService } from '@src/app/websocket.service';

@Component({
	selector: 'app-agents-master',
	templateUrl: './agents-master.component.html',
	styleUrls: ['./agents-master.component.scss'],
	animations: [
		trigger('openClose', [
			state(
				'open',
				style({
					width: '200px',
					opacity: 1,
				})
			),
			state(
				'closed',
				style({
					width: '0px',
					opacity: 0,
				})
			),
			transition('open => closed', [animate('0.1s')]),
			transition('closed => open', [animate('0.1s')]),
		]),
		trigger('showHideSearchBar', [
			state(
				'open',
				style({
					height: 'auto',
					opacity: 1,
					// display: 'block',
					// marginBottom: '10px',
				})
			),
			state(
				'closed',
				style({
					// display: 'none',
					height: '0px',
					opacity: 0,
					// margin: '0px',
				})
			),
			transition('open => closed', [animate('0.1s')]),
			transition('closed => open', [animate('0.1s')]),
		]),
	],
})
export class AgentsMasterComponent implements OnInit {
	public panelOpenState = true;
	public step;
	public controllerActions = {
		actions: [
			{
				NAME: this.translateService.instant('RESTART'),
				ICON: 'update',
				ACTION: 'RESTART',
			},
		],
	};
	public controller: {
		HOST_NAME: '';
		CONTROLLER_ID: '';
		SUB_APPS: [];
	};
	public controllers = [];
	public controllerName: any;
	public controllerId: any;
	public isController = true;
	public savedFields: any[] = [];
	public isOpen = false;
	constructor(
		private router: Router,
		private translateService: TranslateService,
		public customTableService: CustomTableService,
		private bannerMessageService: BannerMessageService,
		private controllerAPIservice: ControllersApiService,
		private websocketService: WebsocketService
	) {
		this.translateService.get('RESTART').subscribe((value: string) => {
			this.controllerActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.isController = true;
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('HOST_NAME'),
			NAME: 'HOST_NAME',
		});
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('STATUS'),
			NAME: 'STATUS',
		});
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('LAST_SEEN'),
			NAME: 'LAST_SEEN',
		});
		columnsHeaders.push(this.translateService.instant('HOST_NAME'));
		columnsHeaders.push(this.translateService.instant('STATUS'));
		columnsHeaders.push(this.translateService.instant('LAST_SEEN'));
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('ACTION'),
			NAME: 'ACTION',
		});
		this.customTableService.sortBy = 'HOST_NAME';
		this.customTableService.sortOrder = 'asc';
		columnsHeaders.push(this.translateService.instant('ACTION'));
		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.getControllers();
	}

	public rowClicked(rowData): void {
		this.router.navigate([
			`company-settings/controller/${rowData.CONTROLLER_ID}`,
		]);
	}

	// public menuItemClicked(action, row) {
	// 	row['COMMAND'] = action.NAME;

	// 	this.stompService._stompRestService.publish({
	// 		destination: `rest/ngdesk-controller-updater/${row.CONTROLLER_ID}`,
	// 		body: JSON.stringify(row),
	// 	});
	// }
	// This is the function which fetch all the controllers to show in the ui as a list.
	public getControllers() {
		const sort = [
			this.customTableService.sortBy + ',' + this.customTableService.sortOrder,
		];
		this.controllerAPIservice
			.getControllers(
				'',
				this.customTableService.pageIndex,
				this.customTableService.pageSize,
				sort
			)
			.subscribe(
				(data: any) => {
					this.customTableService.setTableDataSource(
						data.content,
						data.totalElements
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	// This function helps user to search controllers based onsearch parameters.
	public searchValuesChange(searchParams: any[]) {
		this.savedFields = searchParams;
		if (
			(searchParams.length > 0 &&
				searchParams[searchParams.length - 1]['TYPE'] !== 'field') ||
			searchParams.length === 0
		) {
			this.customTableService.pageIndex = 0;
			this.searchControllers();
		}
	}

	// Makes the search API call in-order to fetch the records based on search parameter.
	public searchControllers() {
		// Convert the search parameter to search string.
		const searchString = this.convertSearchString(this.savedFields);
		// ToDo: Need to call the new get all API call.

		const sort = [
			this.customTableService.sortBy + ',' + this.customTableService.sortOrder,
		];
		if (searchString && searchString !== '' && searchString !== null) {
			this.controllerAPIservice
				.getControllers(
					searchString,
					this.customTableService.pageIndex,
					this.customTableService.pageSize,
					sort
				)
				.subscribe(
					(data: any) => {
						this.customTableService.setTableDataSource(
							data.content,
							data.totalElements
						);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		} else {
			this.controllerAPIservice
				.getControllers(
					'',
					this.customTableService.pageIndex,
					this.customTableService.pageSize,
					sort
				)
				.subscribe(
					(data: any) => {
						this.customTableService.setTableDataSource(
							data.content,
							data.totalElements
						);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		}
	}

	// This function converts search parameter to search string.
	private convertSearchString(searchParams?: any[]): string | null {
		let searchString = '';
		// reloading table based on params from search
		if (searchParams && searchParams.length > 0) {
			// build search string for either global search or field search
			searchParams.forEach((param, index) => {
				if (param['TYPE'] === 'field' && searchParams[index + 1]) {
					const field = param['NAME'];
					const value = searchParams[index + 1]['VALUE'];
					if (searchString === '') {
						searchString = `${field}=${value}`;
					} else {
						searchString += `~~${field}=${value}`;
					}
				} else if (param['TYPE'] === 'global') {
					searchString = param['VALUE'];
				}
			});
			// make get entries call with search param
			if (searchString !== '') {
				return searchString;
			} else {
				return null;
			}
		} else {
			// when all params are cleared, return all entries
			return null;
		}
	}

	public pageChangeEmit(event) {
		this.getControllers();
	}

	public sortData() {
		this.getControllers();
	}

	public restartAgent(row) {
		const instruction = {
			APPLICATION_NAME: 'ngDesk-Controller',
			ACTION: 'STOP',
			LOG_LEVEL: null,
		};
		const controllerInstruction = {
			CONTROLLER_ID: row.CONTROLLER_ID,
			INSTRUCTION: instruction,
		};
		this.websocketService.publishMessage(controllerInstruction);
	}
}
