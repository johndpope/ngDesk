import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
	CompactType,
	DisplayGrid,
	GridsterConfig,
	GridsterItem,
	GridType
} from 'angular-gridster2';
import { AppGlobals } from '../app.globals';

export interface IComponent {
	id: string;
	componentRef: string;
	name: string;
	layout: string;
	field: string;
	data: any;
	representedIn: string;
	chartData: { labels: []; data: [] };
	setModule: string;
}

@Injectable({
	providedIn: 'root'
})
export class StoryboardService {
	public components: IComponent[] = [];
	public layout: GridsterItem[] = [];
	public defaultLayout: string;
	public defaultField: string;
	public moduleId: string;
	public options: GridsterConfig = {
		gridType: GridType.Fixed,
		compactType: CompactType.None,
		margin: 10,
		outerMargin: true,
		outerMarginTop: 10,
		outerMarginRight: 10,
		outerMarginBottom: 10,
		outerMarginLeft: 10,
		useTransformPositioning: true,
		mobileBreakpoint: 640,
		minCols: 1,
		maxCols: 24,
		minRows: 1,
		maxRows: 11,
		maxItemCols: 7,
		minItemCols: 1,
		maxItemRows: 7,
		minItemRows: 1,
		maxItemArea: 250,
		minItemArea: 1,
		defaultItemCols: 24,
		defaultItemRows: 11,
		fixedColWidth: 50,
		fixedRowHeight: 50,
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
			enabled: false
		},
		resizable: {
			enabled: false
		},
		swap: true,
		pushItems: false,
		disablePushOnDrag: true,
		disablePushOnResize: false,
		pushDirections: { north: true, east: true, south: true, west: true },
		pushResizeItems: false,
		displayGrid: DisplayGrid.Always,
		disableWindowResize: false,
		disableWarnings: false,
		scrollToNewItems: false
	};

	constructor(private http: HttpClient, private globals: AppGlobals) {}

	public addItem(id, cols, rows, label): void {
		this.layout.push({
			cols,
			id,
			rows,
			x: 0,
			y: 0
		});
		const { components } = this;
		const comp: IComponent = components.find(c => c.id === id);
		const updateIdx: number = comp
			? components.indexOf(comp)
			: components.length;
		let componentItem: IComponent;
		if (label === 'score') {
			componentItem = {
				id: id,
				componentRef: label,
				name: 'Widget title',
				layout: this.defaultLayout,
				field: null,
				data: {},
				representedIn: 'null',
				chartData: null,
				setModule: 'Tickets'
			};
		} else {
			componentItem = {
				id: id,
				componentRef: label,
				name: 'Widget title',
				layout: this.defaultLayout,
				field: this.defaultField,
				data: {},
				representedIn: 'COUNT',
				chartData: null,
				setModule: 'Tickets'
			};
		}

		// this.components = Object.assign([], components, {
		// 	[updateIdx]: componentItem
		// });
		this.components.push(componentItem);
	}

	public deleteItem(id: string): void {
		const item = this.layout.find(d => d.id === id);
		this.layout.splice(this.layout.indexOf(item), 1);
		const comp = this.components.find(c => c.id === id);
		this.components.splice(this.components.indexOf(comp), 1);
	}

	public getComponentRef(id: string): string {
		const comp = this.components.find(c => c.id === id);
		return comp ? comp.componentRef : null;
	}

	public getWidgetId(compRef, layoutId, fieldId, represntedBy) {
		const httpParams = new HttpParams()
			.set('componentRef', compRef)
			.set('layoutId', layoutId)
			.set('fieldId', fieldId)
			.set('represntedBy', represntedBy);
		return this.http.get(`${this.globals.baseRestUrl}/widget/data/toCreate`, {
			params: httpParams
		});
	}

	public postWidgetData(data) {
		return this.http.post(`${this.globals.baseRestUrl}/dashboards/data`, data);
	}
}
