import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSidenav } from '@angular/material/sidenav';
import { RolesService } from '@src/app/roles/roles.service';
import { UsersService } from '@src/app/users/users.service';
import { DashboardApiService } from '@ngdesk/sam-api';
@Component({
	selector: 'app-dashboards-render',
	templateUrl: './dashboards-render.component.html',
	styleUrls: ['./dashboards-render.component.scss'],
})
export class DashboardsRenderComponent implements OnInit {
	public dashboard;
	public dashboards = [];
	public widgetValues = Object();
	public dashboardId;
	public currentRole;
	public currentRoleId;
	public isLoading = true;
	@ViewChild('sidenav')
	public sidenav: MatSidenav;
	public colorPallete = [];
	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private rolesService: RolesService,
		private usersService: UsersService,
		private dashboardService: DashboardApiService,
		private route: ActivatedRoute,
		private router: Router
	) {}

	public ngOnInit(dashboardId?) {
		this.isLoading = true;
		let isSet = false;
		if (dashboardId) {
			this.dashboardId = dashboardId;
			isSet = true;
		}
		this.colorPallete = [
			'#59c1c7',
			'#5555d3',
			'#e8872a',
			'#db528f',
			'#9089fa',
			'#7ae370',
			'#2680eb',
			'#714ab1',
			'#dfc01f',
			'#cb7024',
			'#468d6c',
			'#9aeb53',
			'#ABD2FA',
			'#84a59d',
			'#577590',
			'#0047ab',
			'#0496ff',
		];
		const getAllDashboardsquery = `{dashboards:getDashboards(sortBy:"name",orderBy:"asc"){dashboardId name role{roleId name}description dateCreated widgets{title widgetId type moduleId positionX positionY width height multiScorecards{title widgetId type moduleId positionX positionY width height dashboardconditions{condition operator requirementType value}field orderBy{order column}limit}dashboardconditions{condition operator requirementType value}field orderBy{order column}limit}dateCreated}totalCount:getDashboardsCount}`;
		this.rolesService
			.getRole(this.usersService.user.ROLE)
			.subscribe((role: any) => {
				this.currentRole = role.NAME;
				this.currentRoleId = role.ROLE_ID;
				this.graphqlQuery(getAllDashboardsquery).subscribe((response: any) => {
					let query = '{';
					this.dashboards = response.dashboards;
					this.filterDashboardsForRoleAndSetDashboard(isSet);
					this.dashboard.widgets.forEach((widget) => {
						const responsePlaceHolder = this.transalater(widget.widgetId);
						if (widget.type === 'score') {
							let scoreCardQuery = `${responsePlaceHolder} : getScoreCardValue(dashboardId:"${this.dashboardId}", widgetId: "${widget.widgetId}")	`;
							query = query + scoreCardQuery;
						} else if (widget.type === 'bar-horizontal') {
							let barChartQuery = `${responsePlaceHolder} : getBarChartValue(dashboardId:"${this.dashboardId}", widgetId: "${widget.widgetId}"){
					name
					value
					id }`;
							query = query + barChartQuery;
						} else if (widget.type === 'pie') {
							let pieChartQuery = `${responsePlaceHolder} : getPieChartValue(dashboardId:"${this.dashboardId}", widgetId: "${widget.widgetId}"){
					name
					value
					id }`;
							query = query + pieChartQuery;
						} else if (widget.type === 'advanced-pie') {
							let advancedpieChartQuery = `${responsePlaceHolder} : getAdvancePieChartValue(dashboardId:"${this.dashboardId}", widgetId: "${widget.widgetId}"){
					name
					value: count
				}`;
							query = query + advancedpieChartQuery;
						} else if (widget.type === 'multi-score') {
							let multiScoreCardQuery = `${responsePlaceHolder} : getMultiScoreCardValue(dashboardId:"${this.dashboardId}", widgetId: "${widget.widgetId}"){
					name
					value }`;
							query = query + multiScoreCardQuery;
						}
					});
					query = query + '}';
					this.graphqlQuery(query).subscribe((response: any) => {
						this.isLoading = false;
						this.dashboard.widgets.forEach((widget) => {
							const widgetId = this.transalater(widget.widgetId);
							if (widget.type === 'score') {
								const value = {
									value: response[widgetId],
								};
								this.widgetValues[widget.widgetId] = value;
							} else if (widget.type === 'bar-horizontal') {
								let colorPallete = this.randomColorPicker(
									response[widgetId].length
								);
								const value = {
									value: response[widgetId],
									colorScheme: {
										domain: colorPallete,
									},
								};
								this.widgetValues[widget.widgetId] = value;
							} else if (
								widget.type === 'pie' ||
								widget.type === 'advanced-pie'
							) {
								let colorPallete = this.randomColorPicker(
									response[widgetId].length
								);
								const value = {
									value: response[widgetId],
									colorScheme: {
										domain: colorPallete,
									},
								};
								this.widgetValues[widget.widgetId] = value;
							} else if (widget.type === 'multi-score') {
								response[widgetId].forEach((element) => {
									this.widgetValues[element.name] = element.value;
								});
							}
						});
					});
				});
			});
	}

	public graphqlQuery(query: string) {
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public randomColorPicker(size) {
		let colorPalleteForChart = [];
		const colorPallete = this.colorPallete;
		let tempPalette = colorPallete;
		for (let i = 0; i < size; i++) {
			let color = tempPalette[Math.floor(Math.random() * tempPalette.length)];
			// let index = tempPalette.indexOf(color);
			// tempPalette.splice(index, 1);
			colorPalleteForChart.push(color);
		}

		return colorPalleteForChart;
	}

	public selectDashboard(id: string) {
		this.ngOnInit(id);
	}

	public editDashboard() {
		this.router.navigate([`dashboards/${this.dashboardId}`]);
	}

	public deleteDashboard() {
		if (this.dashboardId !== 'new') {
			this.dashboardService
				.deleteDashboard(this.dashboardId)
				.subscribe((dashboardResponse: any) => {
					let defaultDashboard;
					this.dashboards.forEach((dashboard) => {
						if (dashboard.name === 'Default (Admin)') {
							defaultDashboard = dashboard.dashboardId;
						}
					});
					this.ngOnInit(this.dashboardId);
				});
		}
	}

	public newDashboard() {
		this.router.navigate([`dashboards/new`]);
	}

	public filterDashboardsForRoleAndSetDashboard(isSet) {
		let newDashboardsList = [];
		if (this.currentRole !== 'SystemAdmin') {
			this.dashboards.forEach((dashboard) => {
				if (dashboard.role.roleId === this.currentRoleId) {
					newDashboardsList.push(dashboard);
					
						if (
							(this.dashboard === undefined || this.dashboard === null) &&
							!isSet
						) {
							this.dashboardId = dashboard.dashboardId;
							this.dashboard = dashboard;
						}
					
				}
			});
			this.dashboards = newDashboardsList;
		} else {
			this.dashboards.forEach((dashboard) => {
				if (
					(this.dashboard === undefined || this.dashboard === null) &&
					!isSet
				) {
					this.dashboardId = dashboard.dashboardId;
					this.dashboard = dashboard;
				}
			});
		}
		if (isSet) {
			this.dashboard = this.dashboards.find(
				(f) => f.dashboardId === this.dashboardId
			);
		}
	}

	public transalater(string) {
		let translatedString = '';
		if (string.includes('-')) {
			translatedString = string.replace(new RegExp('-', 'g'), '_');
			translatedString = 'widgetId' + translatedString;
		}
		return translatedString;
	}

	public onSelect(event, widgetId) {
		const bar = this.widgetValues[widgetId].value.find(
			(f) => f.name === event.name
		);
		if (!bar.id || bar.id === 'no_id') {
			this.router.navigate([
				`dashboards/entries/${this.dashboardId}/${widgetId}/${event.name}`,
			]);
		} else {
			this.router.navigate([
				`dashboards/entries/${this.dashboardId}/${widgetId}/${bar.id}`,
			]);
		}
	}

	public onClick(event, widgetId) {
		this.router.navigate([
			`dashboards/entries/${this.dashboardId}/${widgetId}/${event.name}`,
		]);
	}
}
