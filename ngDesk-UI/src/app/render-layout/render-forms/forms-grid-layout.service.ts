import { Injectable } from '@angular/core';
import { FormsLayoutService } from './forms-layout.service';
import { RenderFormsService } from './render-forms.service';

@Injectable({
	providedIn: 'root',
})
export class FormGridLayoutService {
	constructor(
		private formsLayoutService: FormsLayoutService,
		private renderFormsService: RenderFormsService
	) {}

	public getCustomPanelsForGridLayout(layout) {
		const customPanels = [];
		layout['panels'].forEach((panel) => {
			const customPanel = {
				name: panel['ID'],
				displayName: panel['panelDisplayName'],
				template: '',
				collapse: panel['collapse'],
				grids: JSON.parse(JSON.stringify(panel['grids'])),
				fields: [],
				display: true,
			};
			customPanels.push(customPanel);
		});
		return customPanels;
	}

	public buildTemplates(panels, module, layoutStyle, entry) {
		// LOOP THROUGH EACH PANEL
		panels.forEach((panel, panelIndex) => {
			let layout = `<div fxLayout="column" fxFlex>`;
			// LOOP THROUGH EACH GRID OF THE PANEL
			panel['grids'].forEach((row, i) => {
				layout += `<div class='ROW_${i}' fxLayout="row" fxLayoutGap=10px>`;

				// LOOPING 4 TIMES FOR 4-COLUMNS ON EACH ROW OF PANEL
				row.forEach((grid, j) => {
					if (grid) {
						if (grid['empty']) {
							layout += this.getTemplateForGrids(
								panel['name'],
								grid,
								i,
								j,
								panelIndex
							);
						} else {
							panel['fields'].push(grid['fieldId']);
							layout += this.getTemplateForGrids(
								panel['name'],
								grid,
								i,
								j,
								panelIndex
							);
						}
					}
				});
				layout += `</div> <!--END_ROW_${i}-->`;
			});
			layout += `</div>`;
			panel['template'] = `
				<!--CUSTOM_LAYOUT_START-->
				<!--START_REPLACABLE_LAYOUT-->
				${layout}
				<!--END_REPLACABLE_LAYOUT-->`;

			panel['grids'].forEach((row, i) => {
				row.forEach((grid, j) => {
					if (!grid['empty']) {
						const currentField = module['FIELDS'].find(
							(field) => field['FIELD_ID'] === grid['fieldId']
						);

						this.renderFormsService.loadEntryForEachFieldType(
							currentField,
							entry
						);
						this.renderFormsService.loadDefaultValues(currentField, entry);
						const templateForDataType =
							this.formsLayoutService.getTemplateForField(
								currentField,
								layoutStyle
							);
						const cellRegex = new RegExp(
							`<div class='CELL_${panel['name']}_${i}_${j}([\\s\\S]*?)<!--END_CELL_${panel['name']}_${i}_${j}-->`,
							'g'
						);
						const fieldTemplateForGrid = this.getFieldTemplateForGrid(
							panel['name'],
							i,
							j,
							panelIndex,
							templateForDataType
						);
						panel['template'] = panel['template'].replace(
							cellRegex,
							fieldTemplateForGrid
						);
					}
				});
			});
		});

		return panels;
	}

	private getTemplateForGrids(panelName, grid, i, j, panelIndex) {
		if (grid['width'] !== 0) {
			return `<div class='CELL_${panelName}_${i}_${j}'
        	fxFlex="{{context.panels[${panelIndex}].grids[${i}][${j}].width}}">
      		</div><!--END_CELL_${panelName}_${i}_${j}-->`;
		} else {
			return `<div class='CELL_${panelName}_${i}_${j}' *ngIf="context.panels[${panelIndex}].grids[${i}][${j}].width !== 0">
			</div><!--END_CELL_${panelName}_${i}_${j}-->`;
		}
	}

	private getFieldTemplateForGrid(panelName, i, j, panelIndex, fieldTemplate) {
		return `<div
			class="CELL_${panelName}_${i}_${j}"
			fxLayout="row"
			fxFlex="calc({{context.panels[${panelIndex}].grids[${i}][${j}].width}}% - 10px)"
			fxLayoutAlign="center center"
			[ngStyle]="{'border-radius': '5px','margin':'10px'}"
			fxLayoutGap="15px" >
		${fieldTemplate}
		</div>
		<!--END_CELL_${panelName}_${i}_${j}-->
		`;
	}
}
