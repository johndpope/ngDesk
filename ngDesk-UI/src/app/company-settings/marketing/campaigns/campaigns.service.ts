import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

Injectable({
  providedIn: 'root'
})

export interface Template {
	TYPE: string;
	TEXT: string;
}

export interface Column {
	WIDTH: number;
	TYPE: string;
}

export interface Layout {
	COLUMNS: Column[];
	DISPLAY: string;
	ICON: string;
}

@Injectable()
export class CampaignsService {

	private templates: Template[] = [];
	private layouts: Layout[] = [
		{ COLUMNS: [{WIDTH: 3, TYPE: ''}], DISPLAY: '1', ICON: 'subject' },
		{ COLUMNS: [{WIDTH: 1.5, TYPE: ''}, {WIDTH: 1.5, TYPE: ''}], DISPLAY: '2', ICON: 'subject' },
		{ COLUMNS: [{WIDTH: 1, TYPE: ''}, {WIDTH: 1, TYPE: ''}, {WIDTH: 1, TYPE: ''}], DISPLAY: '3', ICON: 'subject' },
		{ COLUMNS: [{WIDTH: .3333, TYPE: ''}, {WIDTH: .6667, TYPE: ''}], DISPLAY: '1/3 : 2/3', ICON: 'subject' },
		{ COLUMNS: [{WIDTH: .6667, TYPE: ''}, {WIDTH: .3333, TYPE: ''}], DISPLAY: '2/3 : 1/3', ICON: 'subject' }
	];

	constructor(private translateService: TranslateService) {
		this.translateService.get('CAMPAIGN_PLAIN_TEMPLATE').subscribe((plainValue: string) => {
			this.translateService.get('CAMPAIGN_SIMPLE_TEMPLATE').subscribe((simpleValue: string) => {
				this.translateService.get('CAMPAIGN_WELCOME_TEMPLATE').subscribe((welcomeValue: string) => {
					this.templates = [
						{
							TYPE: 'Plain',
							TEXT: plainValue
						},
						{
							TYPE: 'Simple',
							TEXT: simpleValue
						},
						{
							TYPE: 'Welcome',
							TEXT: welcomeValue
						}
					];
				});
			});
		});
	}

	public getTemplate(type: string): string {
		return this.templates.find(template => template.TYPE === type)['TEXT'];
	}

	public getLayouts(): Layout[] {
		return this.layouts;
	}

	public getLayoutByType(type: string): Layout {
		return this.layouts.find(layoutFound => layoutFound.DISPLAY === type);
	}
}

