import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class ListFormulaService {
	constructor() {}

	public getListFormulaField(field) {
		return `<div fxLayout="column" fxFlex class="mat-form-field-wrapper mat-form-field-appearance-outline mat-form-field-wrapper"
    style="border: 1px solid #ccc; border-radius: 5px;">
    <span class="mat-caption pad10">${field.DISPLAY_LABEL}</span>
    <mat-divider></mat-divider>
    <mat-selection-list style="overflow:auto" #${field.NAME} (selectionChange)="context.onChangeSelectionList($event, context.fieldsMap['${field.FIELD_ID}'])" [compareWith]="context.compareFn" [(ngModel)]="context.entry['${field.NAME}']">
    <mat-list-option [matTooltip]="formula.FORMULA_LABEL  + ' ('+ context.getFormulaListValue('${field.NAME}',formula.FORMULA_NAME) + ')'" checkboxPosition="before" *ngFor="let formula of context.fieldsMap['${field.FIELD_ID}'].LIST_FORMULA" [value]="{FORMULA_NAME:formula.FORMULA_NAME}">
    <span fxLayoutAlign="space-between center"><span>{{formula.FORMULA_LABEL}}</span><span>{{context.getFormulaListValue('${field.NAME}',formula.FORMULA_NAME) | truncate : 10}}</span></span>
   
    </mat-list-option>    
    </mat-selection-list>
  </div>
  `;
	}

	public getLayoutForListFormula(
		panel,
		xpossition,
		ypossition,
		currentField,
		panelIndex,
		size,
		layoutStyle,
		layoutType
	) {
		const removeDivFrom = xpossition + 1;
		const removeDivTo = removeDivFrom + 3;
		let fieldWidth = size;

		panel['TEMPLATE'] = panel['TEMPLATE'].replace(
			new RegExp(
				`<div class='ROW_${removeDivFrom}([\\s\\S]*?)<!--END_ROW_${removeDivTo}-->`
			),
			''
		);

		const rowRegex = new RegExp(
			`<div class='ROW_${xpossition}([\\s\\S]*?)<!--END_ROW_${xpossition}-->`
		);

		panel['TEMPLATE'] = panel['TEMPLATE'].replace(
			rowRegex,
			this.buildTemplateForGridLayout(
				panel,
				panelIndex,
				xpossition,
				ypossition,
				fieldWidth
			)
		);

		const xPos = xpossition;
		const yPos = ypossition;

		const cellRegex = new RegExp(
			`<div class='CELL_${panel['NAME']}_${xPos}_${yPos}([\\s\\S]*?)<!--END_CELL_${panel['NAME']}_${xPos}_${yPos}-->`,
			'g'
		);

		panel['TEMPLATE'] = panel['TEMPLATE'].replace(
			cellRegex,
			this.loadReceiptUploaderView(currentField)
		);
		return panel;
	}

	buildTemplateForGridLayout(panel, panelIndex, xpossition, ypossition, size) {
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const fieldSize = size;
		const xPos = xpossition;
		const yPos = ypossition;
		const field = panel['GRIDS'][xPos][yPos]['FIELD_ID'];
		if (fieldSize === 3) {
			for (let x = xPos; x < xPos + 4; x++) {
				for (let y = yPos; y < yPos + 3; y++) {
					panel['GRIDS'][x][y] = {
						IS_EMPTY: true,
						HEIGHT: panel['GRIDS'].length,
						WIDTH: 100,
						FIELD_ID: '',
					};
				}
			}
		}

		if (fieldSize === 2) {
			flex = 50;
		} else if (fieldSize === 3) {
			flex = 75;
		} else if (fieldSize === 4) {
			flex = 100;
		} else {
			flex = 25;
		}

		for (let x = xPos; x < xPos + 4; x++) {
			for (let y = yPos; y < yPos + size; y++) {
				panel['GRIDS'][x][y] = {
					IS_EMPTY: panel['GRIDS'][xPos][yPos].IS_EMPTY,
					HEIGHT: panel['GRIDS'].length,
					WIDTH: panel['GRIDS'][xPos][yPos].WIDTH,
					FIELD_ID: field,
				};
			}
		}

		let row1 = '';
		let row2 = '';
		for (let x = xPos; x < xPos + 4; x++) {
			if (yPos === 1 && size === 2) {
				row1 = row1 + this.buildRowForReceipt(panel, 3, x, yPos, panelIndex);
				row2 = row2 + this.buildRowForReceipt(panel, 3, x, 0, panelIndex);
			} else {
				rows = rows + this.buildRowForReceipt(panel, size, x, yPos, panelIndex);
			}
		}

		const flexRegex = new RegExp('COLUMN_FLEX');
		const rowsRegex = new RegExp('ADD_ROWS_FOR_THIS_COLUMN');
		let columnWithRows = columnTemplate.replace(rowsRegex, rows);
		columnWithRows = columnWithRows.replace(flexRegex, (100 - flex).toString());

		let columnWithreceipt = columnTemplate.replace(flexRegex, flex.toString());
		columnWithreceipt = columnWithreceipt.replace(
			rowsRegex,
			this.getTemplateForGrids(
				panel['NAME'],
				panel['GRIDS'][xPos][yPos],
				xPos,
				yPos,
				panelIndex
			)
		);

		let mainTemplate = `<div class='LIST_FORMULA' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			mainTemplate = mainTemplate + columnWithreceipt + columnWithRows;
		} else if (yPos === 1 && size === 2) {
			let column1WithRows1 = columnTemplate.replace(rowsRegex, row1);
			column1WithRows1 = column1WithRows1.replace(
				flexRegex,
				(flex / 2).toString()
			);
			let column2WithRows2 = columnTemplate.replace(rowsRegex, row2);
			column2WithRows2 = column2WithRows2.replace(
				flexRegex,
				(flex / 2).toString()
			);
			mainTemplate =
				mainTemplate + column1WithRows1 + columnWithreceipt + column2WithRows2;
		} else {
			mainTemplate = mainTemplate + columnWithRows + columnWithreceipt;
		}
		mainTemplate = mainTemplate + `</div><!--END_LIST_FORMULA-->`;
		return mainTemplate;
	}

	buildRowForReceipt(panel, size, i, j, panelIndex) {
		let row = `<div class='ROW_${i}' fxLayoutGap=5px fxLayout=row fxFlex>`;
		for (let y = 0; y < 4; y++) {
			if (size === 2 && j !== 1) {
				if (y !== j && y !== j + 1) {
					panel['GRIDS'][i][y].WIDTH = 50;
					row =
						row +
						this.getTemplateForGrids(
							panel['NAME'],
							panel['GRIDS'][i][y],
							i,
							y,
							panelIndex
						);
				}
			} else if (size === 3) {
				if (y === (j - 1 < 0 ? 3 : j - 1)) {
					panel['GRIDS'][i][y].WIDTH = 100;
					row =
						row +
						this.getTemplateForGrids(
							panel['NAME'],
							panel['GRIDS'][i][y],
							i,
							y,
							panelIndex
						);
				}
			}
		}
		row = row + `</div> <!--END_ROW_${i}-->`;
		return row;
	}

	private getTemplateForGrids(panelName, grid, i, j, panelIndex) {
		if (grid['WIDTH'] !== 0) {
			return `<div class='CELL_${panelName}_${i}_${j}'
            fxFlex="{{context.panels[${panelIndex}].GRIDS[${i}][${j}].WIDTH}}">
            </div><!--END_CELL_${panelName}_${i}_${j}-->`;
		} else {
			return `<div class='CELL_${panelName}_${i}_${j}' *ngIf="context.panels[${panelIndex}].GRIDS[${i}][${j}].WIDTH !== 0">
        </div><!--END_CELL_${panelName}_${i}_${j}-->`;
		}
	}

	loadReceiptUploaderView(field) {
		return this.getListFormulaField(field);
	}
}
