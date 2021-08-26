import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AppGlobals } from '@src/app/app.globals';

@Injectable({
	providedIn: 'root',
})
export class DashboardsService {
	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private translateService: TranslateService
	) {}

	public getAllDashboards(query) {
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getAllWidgets() {
		return [
			{
				TITLE: this.translateService.instant('SCORE_CARD'),
				ICON: 'chrome_reader_mode',
				TYPE: 'score',
			},
			{
				TITLE: this.translateService.instant('BAR_CHART'),
				ICON: 'insert_chart',
				TYPE: 'bar-horizontal',
			},
			{
				TITLE: this.translateService.instant('PIE_CHART'),
				ICON: 'pie_chart',
				TYPE: 'pie',
			},
			{
				TITLE: this.translateService.instant('MULTI_SCORE_CARDS'),
				ICON: 'dashboard',
				TYPE: 'multi-score',
			},
			{
				TITLE: this.translateService.instant('ADVANCED_PIE_CHART'),
				ICON: 'pie_chart',
				TYPE: 'advanced-pie',
			}
		];
	}

	public getDashboard(dashboardId) {
		const query = `{
			DATA: getDashboard(dashboardId:"${dashboardId}") {
				name
				dashboardId: dashboardId
				description
				role {
					roleId
					name
				}
                widgets{
                    widgetId
					title
					type
                    moduleId
                    positionX
					positionY
					aggregateType,
					aggregateField,
					width
					height
					multiScorecards{
						widgetId
						title
						type
						moduleId
						positionX
						positionY
						aggregateType,
						aggregateField,
						limit
						field
						limitEntries
						orderBy{
							column
							order
						}
						dashboardconditions{
							condition
							operator
							value
							requirementType
						}
					}
					limit
					field
					limitEntries
					orderBy{
						column
						order
					}
                    dashboardconditions{
                        condition
                        operator
                        value
                        requirementType
                    }
                }
			}
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
