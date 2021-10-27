import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSidenav } from '@angular/material/sidenav';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { RolesService } from '@src/app/roles/roles.service';
import {
	IComponent,
	StoryboardService,
} from '@src/app/storyboard/storyboard.service';
import { UsersService } from '@src/app/users/users.service';
import { GridsterConfig, GridsterItem } from 'angular-gridster2';
import { UUID } from 'angular2-uuid';
import { Subscription } from 'rxjs';

export interface Widget {
	MODULE: string;
	TYPE: string;
	LIST_LAYOUT: string;
	CATEGORISED_BY: string;
	REPRESENTED_IN: string;
}

@Component({
	selector: 'app-storyboard',
	templateUrl: './storyboard.component.html',
	styleUrls: ['./storyboard.component.scss'],
})
export class StoryboardComponent implements OnInit, OnDestroy {
	public scoreForm = false;
	public barForm = false;
	public listLayouts = [];
	public fields = [];
	private moduleId: string;
	public entryData: any;
	public allowedModules = ['Tickets', 'Chats'];
	public dashboardForm: FormGroup;
	public teams = [];
	public editEnabled = false;
	public flexSize = '100%';
	public dashboardId = 'new';
	public dashboards = [];
	public currentRole;
	public dashboard: any = {};
	@ViewChild('sidenav')
	public sidenav: MatSidenav;
	public selectedWidget: string;
	public isDefault = false;
	public isLoading = true;
	public widgetData: any = [];
	public modules: any = [];
	public selectedModuleName: 'Tickets';
	public selectedModuleId: string;
	public dashBoardData: any = {};
	private companyInfoSubscription: Subscription;

	constructor(
		private rolesService: RolesService,
		private translateService: TranslateService,
		private layoutService: StoryboardService,
		private modulesService: ModulesService,
		private usersService: UsersService,
		private formBuilder: FormBuilder,
		private cacheService: CacheService,
		private bannerMessageService: BannerMessageService
	) {}

	public ngOnInit() {
		this.dashboardForm = this.formBuilder.group({
			NAME: [{ value: '', disabled: true }, Validators.required],
			DESCRIPTION: [{ value: '', disabled: true }],
			TEAMS: [[], Validators.required],
		});
		// use modules saved in cache
		this.companyInfoSubscription =
			this.cacheService.companyInfoSubject.subscribe((dataStored) => {
				if (dataStored) {
					this.modules = this.cacheService.companyData['MODULES'];
					const ticketsModule = this.cacheService.companyData['MODULES'].find(
						(module) => module['NAME'] === 'Tickets'
					);
					this.fields = ticketsModule.FIELDS.filter(
						(f) => f.DATA_TYPE.DISPLAY === 'Picklist'
					);
					this.listLayouts = ticketsModule.LIST_LAYOUTS.filter(
						(layout) => layout.ROLE === this.usersService.user.ROLE
					);
					const teamsModule = this.cacheService.companyData['MODULES'].find(
						(module) => module['NAME'] === 'Teams'
					);
					this.modulesService.getEntries(teamsModule.MODULE_ID).subscribe(
						(entryResponse: any) => {
							this.teams = entryResponse.DATA;
						},
						(error) => {
							this.isLoading = false;
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
				}
			});

		this.modulesService.getAllDashboard().subscribe((response: any) => {
			this.rolesService
				.getRole(this.usersService.user.ROLE)
				.subscribe((role: any) => {
					this.currentRole = role.NAME;
					if (response.DASHBOARDS.length > 0) {
						this.dashboards = response.DASHBOARDS;
						this.dashboards.forEach((element: any) => {
							element.WIDGETS.forEach((widget: any) => {
								this.layoutService.defaultLayout = widget.LIST_LAYOUT;
								this.layoutService.defaultField = widget.FIELD_ID;
								this.moduleId = widget.MODULE;
								this.layoutService.moduleId = this.moduleId;
								this.selectedModuleId = this.moduleId;
							});
						});
						this.setDefaultBoard();
					}
				});
		});
	}

	public setModuleEntries(event, moduleData, id) {
		if (event.isUserInput) {
			// ignore on deselection of the previous option
			const comp = this.components.find((f) => f.id === id);
			this.selectedModuleName = moduleData.NAME;
			this.selectedModuleId = moduleData.MODULE_ID;
			const selectedModule = this.modules.find(
				(module) => module['NAME'] === this.selectedModuleName
			);
			this.fields = selectedModule.FIELDS.filter(
				(f) => f.DATA_TYPE.DISPLAY === 'Picklist'
			);
			this.listLayouts = selectedModule.LIST_LAYOUTS.filter(
				(layout) => layout.ROLE === this.usersService.user.ROLE
			);
			this.layoutService.defaultLayout = this.listLayouts[0].LAYOUT_ID;
			this.layoutService.defaultField = this.fields[0].FIELD_ID;
			this.moduleId = selectedModule.MODULE_ID;
			this.layoutService.moduleId = selectedModule.MODULE_ID;
			comp.layout = this.layoutService.defaultLayout;
			comp.setModule = this.selectedModuleName;
			if (comp.componentRef !== 'score') {
				comp.field = this.layoutService.defaultField;
			}
			let setWidget: Widget[];
			// this.components.forEach((component: any) => {
			let moduleId = this.modules.find(
				(M) => M.NAME === comp.setModule
			).MODULE_ID;
			setWidget = [
				{
					MODULE: moduleId,
					TYPE: comp.componentRef,
					CATEGORISED_BY: comp.field,
					REPRESENTED_IN: comp.representedIn,
					LIST_LAYOUT: comp.layout,
				},
			];
			// setWidget.push(eachWidget);
			// });

			this.dashBoardData = {
				NAME: this.dashboardForm.value.NAME,
				DESCRIPTION: this.dashboardForm.value.DESCRIPTION,
				TEAMS: this.dashboardForm.value.TEAMS,
				WIDGETS: setWidget,
			};
			this.layoutService.postWidgetData(this.dashBoardData).subscribe(
				(response: any) => {
					const widgetEntries = response.WIDGETS;
					widgetEntries.forEach((widget: any) => {
						this.setData(comp, this.entryData, widget);
					});
				},
				(error) => {
					this.isLoading = false;
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		}
	}
	get options(): GridsterConfig {
		return this.layoutService.options;
	}

	get layout(): GridsterItem[] {
		return this.layoutService.layout;
	}
	get components(): IComponent[] {
		return this.layoutService.components;
	}

	public setDefaultBoard() {
		if (this.dashboardId === 'new') {
			const defaultId = this.dashboards.find((f) => f.DEFAULT).DASHBOARD_ID;
			this.setDashboard(defaultId);
		} else {
			this.setDashboard(this.dashboardId);
		}
	}

	public newDashboard() {
		// this.editEnabled = false;
		this.scoreForm = false;
		this.barForm = false;
		this.dashboard.NAME = 'New DashBoard';
		this.dashboard.DESCRIPTION = null;
		this.selectedWidget = undefined;
		this.layoutService.components = [];
		this.layoutService.layout = [];
		this.dashboardForm.reset();
		this.dashboardId = 'new';
		this.editDashboard(true);
		this.sidenav.toggle();
	}

	public editDashboard(val: boolean) {
		this.editEnabled = val;
		this.layoutService.options.draggable.enabled = val;
		if (val) {
			this.dashboardForm.controls['NAME'].enable();
			this.dashboardForm.controls['DESCRIPTION'].enable();
			this.flexSize = '75%';
		} else {
			this.dashboardForm.controls['NAME'].disable();
			this.dashboardForm.controls['DESCRIPTION'].disable();
			this.flexSize = '100%';
			this.selectedWidget = undefined;
			this.isLoading = true;
		}
		if (this.options.api) {
			this.options.api.optionsChanged();
		}
	}

	public deleteDashboard() {
		if (this.dashboardId !== 'new') {
			this.modulesService
				.deleteDashboard(this.moduleId, this.dashboardId)
				.subscribe(
					(response) => {
						this.dashboards = this.dashboards.filter(
							(f) => f.DASHBOARD_ID !== this.dashboardId
						);
						this.setDashboard(this.dashboards[0].DASHBOARD_ID);
					},
					(error) => {
						this.isLoading = false;
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		}
	}

	public selectDashboard(id: string) {
		const dashboard = this.dashboards.find((f) => f.DASHBOARD_ID === id);
		this.flexSize = '100%';
		if (dashboard.DEFAULT) {
			this.editEnabled = false;
			this.dashboardForm.controls['NAME'].disable();
			this.dashboardForm.controls['DESCRIPTION'].disable();
			this.selectedWidget = undefined;
			this.layoutService.options.draggable.enabled = false;
		} else {
			this.dashboardForm.controls['NAME'].enable();
			this.dashboardForm.controls['DESCRIPTION'].enable();
		}
		this.isLoading = true;
		this.dashboards.forEach((element: any) => {
			element.WIDGETS.forEach((widget: any) => {
				this.layoutService.defaultLayout = widget.LIST_LAYOUT;
				this.layoutService.defaultField = widget.FIELD_ID;
				this.moduleId = widget.MODULE;
				this.layoutService.moduleId = this.moduleId;
				this.selectedModuleId = this.moduleId;
			});
		});
		this.setDashboard(id);
	}

	public setDashboard(id: string) {
		let allWidgets: any = [];
		let component: IComponent;
		this.layoutService.components = [];
		this.layoutService.layout = [];
		const dashboard = this.dashboards.find((f) => f.DASHBOARD_ID === id);
		this.dashboardId = dashboard.DASHBOARD_ID;
		this.dashboard.NAME = dashboard.NAME;
		this.dashboard.DESCRIPTION = dashboard.DESCRIPTION;
		this.isDefault = !!dashboard.DEFAULT;
		this.dashboardForm.controls.NAME.setValue(dashboard.NAME);
		this.dashboardForm.controls.DESCRIPTION.setValue(dashboard.DESCRIPTION);
		this.dashboardForm.controls.TEAMS.setValue(dashboard.TEAMS);
		dashboard.WIDGETS.forEach((widget: any) => {
			component = {
				chartData: null,
				componentRef: widget.TYPE,
				data: {},
				field: widget.CATEGORISED_BY,
				id: widget.WIDGET_ID,
				layout: widget.LIST_LAYOUT,
				name: widget.TITLE,
				representedIn: widget.REPRESENTED_IN,
				setModule: this.moduleId,
			};
			const { POSITION } = widget;
			const layout: GridsterItem = {
				cols: POSITION.COL,
				rows: POSITION.ROW,
				id: widget.WIDGET_ID,
				x: POSITION.X_AXIS,
				y: POSITION.Y_AXIS,
			};
			// let setWidget: Widget[];
			let setWidget = {
				MODULE: widget.MODULE,
				TYPE: widget.TYPE,
				CATEGORISED_BY: widget.CATEGORISED_BY,
				REPRESENTED_IN: widget.REPRESENTED_IN,
				LIST_LAYOUT: widget.LIST_LAYOUT,
			};
			allWidgets.push(setWidget);

			this.layoutService.components.push(component);
			this.layoutService.layout.push(layout);
		});
		this.dashBoardData = {
			NAME: dashboard.NAME,
			DESCRIPTION: dashboard.DESCRIPTION,
			TEAMS: dashboard.TEAMS,
			WIDGETS: allWidgets,
		};

		this.layoutService.postWidgetData(this.dashBoardData).subscribe(
			(response: any) => {
				this.isLoading = false;
				const widgetEntries = response.WIDGETS;
				widgetEntries.forEach((widget: any) => {
					const comp =
						this.layoutService.components[widgetEntries.indexOf(widget)];
					this.setData(comp, this.entryData, widget);
				});
			},
			(error) => {
				this.isLoading = false;
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public addChart(type: string, col: number, row: number) {
		this.layoutService.defaultField = this.fields[0].FIELD_ID;
		const uuid = UUID.UUID();
		let widgetData: any = {};
		// this.selectedModuleId = this.modules.find(
		// 	M => M.NAME === 'Tickets'
		// ).MODULE_ID;
		this.layoutService.addItem(uuid, col, row, type);
		if (type === 'score') {
			this.scoreForm = true;
			widgetData = {
				MODULE: this.selectedModuleId,
				TYPE: type,
				LIST_LAYOUT: this.layoutService.defaultLayout,
				CATEGORISED_BY: null,
				REPRESENTED_IN: null,
			};
		} else if (type === 'bar') {
			this.barForm = true;
			widgetData = {
				MODULE: this.selectedModuleId,
				TYPE: type,
				LIST_LAYOUT: this.layoutService.defaultLayout,
				CATEGORISED_BY: this.layoutService.defaultField,
				REPRESENTED_IN: 'COUNT',
			};
		}
		this.setWidgetData(
			this.layoutService.components.find((f) => f.id === uuid),
			widgetData
		);

		this.selectedWidget = uuid;
	}

	public changeTitle(event, id) {
		this.components.find((f) => f.id === id).name = event;
	}

	public changeLayout(event, id) {
		const { value } = event;
		const comp = this.components.find((f) => f.id === id);
		comp.layout = value;
		if (comp.componentRef === 'score') {
			this.widgetData = {
				MODULE: this.selectedModuleId,
				TYPE: comp.componentRef,
				LIST_LAYOUT: value,
				CATEGORISED_BY: null,
				REPRESENTED_IN: null,
			};
		} else {
			this.widgetData = {
				MODULE: this.selectedModuleId,
				TYPE: comp.componentRef,
				LIST_LAYOUT: value,
				CATEGORISED_BY: comp.field,
				REPRESENTED_IN: comp.representedIn,
			};
		}
		this.setWidgetData(comp, this.widgetData);
	}

	public changeCategory(event, id) {
		const comp = this.components.find((f) => f.id === id);
		comp.field = event.value;

		if (comp.componentRef === 'score') {
			this.widgetData = {
				MODULE: this.selectedModuleId,
				TYPE: comp.componentRef,
				LIST_LAYOUT: comp.layout,
				CATEGORISED_BY: null,
				REPRESENTED_IN: null,
			};
		} else {
			this.widgetData = {
				MODULE: this.selectedModuleId,
				TYPE: comp.componentRef,
				LIST_LAYOUT: comp.layout,
				CATEGORISED_BY: event.value,
				REPRESENTED_IN: comp.representedIn,
			};
		}
		this.setWidgetData(comp, this.widgetData);
	}

	public changeType(event, id) {
		const comp = this.components.find((f) => f.id === id);
		comp.representedIn = event;
		// let widgetEntries: any;
		this.widgetData = {
			MODULE: this.selectedModuleId,
			TYPE: comp.componentRef,
			LIST_LAYOUT: comp.layout,
			CATEGORISED_BY: comp.field,
			REPRESENTED_IN: event,
		};
		this.setWidgetData(comp, this.widgetData);
	}

	public setWidgetData(comp, widgetData) {
		let widgetEntries: any;
		const allWidgets: any = [];
		this.components.forEach((component: any) => {
			let eachWidget: any = {};
			let moduleId;
			if (this.components.indexOf(component) < this.components.length - 1) {
				if (this.modules.find((M) => M.NAME === component.setModule)) {
					moduleId = this.modules.find(
						(M) => M.NAME === component.setModule
					).MODULE_ID;
				} else {
					moduleId = component.setModule;
				}
				// let moduleId;
				// this.modules.forEach(moduleEach => {
				// 	if (moduleEach.MODULE_ID === component.setModule) {
				// 		moduleId = moduleEach.MODULE_ID;
				// 	}
				// });
				if (component.componentRef === 'score') {
					eachWidget = {
						MODULE: moduleId,
						TYPE: component.componentRef,
						CATEGORISED_BY: null,
						REPRESENTED_IN: null,
						LIST_LAYOUT: component.layout,
					};
				} else {
					eachWidget = {
						MODULE: moduleId,
						TYPE: component.componentRef,
						CATEGORISED_BY: component.field,
						REPRESENTED_IN: component.representedIn,
						LIST_LAYOUT: component.layout,
					};
				}

				allWidgets.push(eachWidget);
			}
		});
		allWidgets.push(widgetData);
		this.dashBoardData = {
			NAME: this.dashboardForm.value.NAME,
			DESCRIPTION: this.dashboardForm.value.DESCRIPTION,
			TEAMS: this.dashboardForm.value.TEAMS,
			WIDGETS: allWidgets,
		};

		this.layoutService.postWidgetData(this.dashBoardData).subscribe(
			(response: any) => {
				widgetEntries = response.WIDGETS;
				widgetEntries.forEach((widget) => {
					this.setData(comp, this.entryData, widget);
				});
			},
			(error) => {
				this.isLoading = false;
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}
	public editItem(id) {
		if (this.editEnabled) {
			this.selectedWidget = id;
			const comp = this.components.find((f) => f.id === id);
			const index = this.components.findIndex((f) => f.id === id);
			this.components.push(this.components.splice(index, 1)[0]);

			if (comp.componentRef === 'score') {
				this.barForm = false;
				this.scoreForm = true;
			} else {
				this.scoreForm = false;
				this.barForm = true;
			}
		}
	}

	public setData(comp, response, widget) {
		// const { DATA, TOTAL_RECORDS } = response;
		if (comp.componentRef === 'bar') {
			comp.data = {};
			// const fieldToMap = this.fields.find(f => f.FIELD_ID === comp.field).NAME;
			// DATA.forEach((val: any) => {
			// 	if (val.hasOwnProperty(fieldToMap)) {
			// 		comp.data[val[fieldToMap]] = comp.data[val[fieldToMap]]
			// 			? comp.data[val[fieldToMap]] + 1
			// 			: 1;
			// 	}
			// 	console.log(comp.data[val[fieldToMap]]);
			// });

			if (comp.representedIn === 'COUNT') {
				comp.chartData = {};
				if (widget.DATA == null) {
					comp.chartData = {};
				} else {
					const keys = Object.keys(widget.DATA);
					// const values = Object.values(widgetEntries.DATA);
					const keysSet: any = [];
					const valuesSet: any = [];
					keys.forEach((key) => {
						if (widget.DATA[key] > 0) {
							keysSet.push(key);
							valuesSet.push(widget.DATA[key]);
						}
					});
					if (keysSet.length < 5) {
						const keyLength = 5 - keysSet.length;
						for (let i = 0; i < keyLength; i++) {
							keysSet.push('');
						}
					}
					comp.chartData = {
						labels: keysSet,
						data: [
							{
								barThickness: 15,
								maxBarThickness: 250,
								minBarLength: 25,
								data: valuesSet,
								stacked: true,
							},
						],
					};
				}
			} else {
				comp.chartData = {};
				if (widget.DATA == null) {
					comp.chartData = {};
				} else {
					const percentData = [];
					const keys = Object.keys(widget.DATA);
					const values = Object.values(widget.DATA);
					const keysSet: any = [];
					const valuesSet: any = [];
					let total = 0;
					keys.forEach((key) => {
						if (widget.DATA[key] > 0) {
							keysSet.push(key);
							valuesSet.push(widget.DATA[key]);
							total = total + widget.DATA[key];
						}
					});
					if (keysSet.length < 5) {
						const keyLength = 5 - keysSet.length;
						for (let i = 0; i < keyLength; i++) {
							keysSet.push('');
						}
					}
					valuesSet.forEach((val) => {
						percentData.push(
							Math.round((Number(val) / total) * 100 * 100) / 100
						);
					});
					comp.chartData = {
						labels: keysSet,
						data: [
							{
								barThickness: 15,
								maxBarThickness: 250,
								minBarLength: 25,
								data: percentData,
								stacked: true,
							},
						],
					};
					for (let i = 0; i < valuesSet.length; i++) {
						comp.chartData.labels[i] =
							comp.chartData.labels[i] + ' ' + percentData[i] + '(%)';
					}
				}
			}
		} else {
			if (widget.DATA == null) {
				comp.data['score'] = 'No List';
			} else {
				comp.data['score'] = widget.DATA.COUNT;
			}
		}
	}

	public save() {
		this.scoreForm = false;
		this.barForm = false;
		this.selectedWidget = undefined;
		if (this.dashboardForm.valid) {
			const data = [];
			this.layoutService.components.map((component) => {
				const layout = this.layoutService.layout.find(
					(val) => val.id === component.id
				);
				let moduleValue;
				if (this.moduleId === component.setModule) {
					moduleValue = this.modules.find(
						(f) => f.MODULE_ID === component.setModule
					);
				} else {
					moduleValue = this.modules.find(
						(f) => f.NAME === component.setModule
					);
				}

				if (component.componentRef === 'score') {
					data.push({
						WIDGET_ID: component.id,
						MODULE: moduleValue.MODULE_ID,
						TITLE: component.name,
						TYPE: component.componentRef,
						LIST_LAYOUT: component.layout,
						CATEGORISED_BY: null,
						REPRESENTED_IN: null,
						POSITION: {
							COL: layout.cols,
							ROW: layout.rows,
							X_AXIS: layout.x,
							Y_AXIS: layout.y,
						},
					});
				} else {
					data.push({
						WIDGET_ID: component.id,
						MODULE: moduleValue.MODULE_ID,
						TITLE: component.name,
						TYPE: component.componentRef,
						LIST_LAYOUT: component.layout,
						CATEGORISED_BY: component.field,
						REPRESENTED_IN: component.representedIn,
						POSITION: {
							COL: layout.cols,
							ROW: layout.rows,
							X_AXIS: layout.x,
							Y_AXIS: layout.y,
						},
					});
				}
			});
			const dashboard = {
				NAME: this.dashboardForm.value.NAME,
				DESCRIPTION: this.dashboardForm.value.DESCRIPTION,
				TEAMS: this.dashboardForm.value.TEAMS,
				WIDGETS: data,
			};

			if (this.dashboardId !== 'new') {
				this.modulesService
					.putDashboard(this.moduleId, this.dashboardId, dashboard)
					.subscribe(
						(response) => {
							this.modulesService
								.getAllDashboard()
								.subscribe((dashboardResponse: any) => {
									this.rolesService
										.getRole(this.usersService.user.ROLE)
										.subscribe((role: any) => {
											this.currentRole = role.NAME;
											if (dashboardResponse.DASHBOARDS.length > 0) {
												this.dashboards = dashboardResponse.DASHBOARDS;
												this.dashboards.forEach((element: any) => {
													element.WIDGETS.forEach((widget: any) => {
														this.layoutService.defaultLayout =
															widget.LIST_LAYOUT;
														this.layoutService.defaultField = widget.FIELD_ID;
														this.moduleId = widget.MODULE;
														this.layoutService.moduleId = this.moduleId;
														this.selectedModuleId = this.moduleId;
													});
												});
											}
										});
									this.bannerMessageService.successNotifications.push({
										message: this.translateService.instant(
											'SUCCESSFULLY_EDITED_DASHBOARD'
										),
									});
									this.editEnabled = false;
									this.isDefault = false;
									this.editDashboard(false);
									this.isLoading = false;
								});
						},
						(error) => {
							this.isLoading = false;
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			} else {
				this.modulesService.postDashboard(this.moduleId, dashboard).subscribe(
					(response: any) => {
						this.dashboard = response;
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant(
								'SUCCESSFULLY_POSTED_DASHBOARD'
							),
						});
						this.editEnabled = false;
						this.isDefault = false;
						this.dashboardId = response.DASHBOARD_ID;
						this.dashboards.push(response);
						this.editDashboard(false);
						this.isLoading = false;
					},
					(error) => {
						this.isLoading = false;
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			}
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: 'Please fill out all the required fields',
			});
		}
	}

	public ngOnDestroy() {
		if (this.companyInfoSubscription) {
			this.companyInfoSubscription.unsubscribe();
		}
	}
}
