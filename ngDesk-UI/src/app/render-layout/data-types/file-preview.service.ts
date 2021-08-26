import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class FilePreviewService {
	constructor(private httpClient: HttpClient) {}

	public getTemplateForGrid(panel, panelIndex, module) {
		const removeDivFrom = panel['FILE_PREVIEW_POSITION']['X_POS'] + 1;
		const removeDivTo = panel['GRIDS'].length - 1;

		panel['TEMPLATE'] = panel['TEMPLATE'].replace(
			new RegExp(
				`<div class='ROW_${removeDivFrom}([\\s\\S]*?)<!--END_ROW_${removeDivTo}-->`
			),
			''
		);

		const rowRegex = new RegExp(
			`<div class='ROW_${panel['FILE_PREVIEW_POSITION']['X_POS']}([\\s\\S]*?)<!--END_ROW_${panel['FILE_PREVIEW_POSITION']['X_POS']}-->`
		);
		panel['TEMPLATE'] = panel['TEMPLATE'].replace(
			rowRegex,
			this.buildTemplateForGridLayout(
				panel,
				panel['FILE_PREVIEW_POSITION']['SIZE'],
				panelIndex
			)
		);

		const xPos = panel['FILE_PREVIEW_POSITION']['X_POS'];
		const yPos = panel['FILE_PREVIEW_POSITION']['Y_POS'];

		const cellRegex = new RegExp(
			`<div class='CELL_${panel['NAME']}_${xPos}_${yPos}([\\s\\S]*?)<!--END_CELL_${panel['NAME']}_${xPos}_${yPos}-->`,
			'g'
		);
		const field = module['FIELDS'].find(
			(moduleField) =>
				moduleField['FIELD_ID'] === panel['GRIDS'][xPos][yPos]['FIELD_ID']
		);
		panel['TEMPLATE'] = panel['TEMPLATE'].replace(
			cellRegex,
			this.getFilePreviewTemplate(panel['NAME'], xPos, yPos, panelIndex)
		);
		return panel;
	}

	private buildTemplateForGridLayout(panel, size, panelIndex) {
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const filePreviewInitialSize = panel['FILE_PREVIEW_POSITION']['SIZE'];
		const xPos = panel['FILE_PREVIEW_POSITION']['X_POS'];
		const yPos = panel['FILE_PREVIEW_POSITION']['Y_POS'];
		const field = panel['GRIDS'][xPos][yPos]['FIELD_ID'];

		if (filePreviewInitialSize === 3) {
			for (let x = xPos; x < panel['GRIDS'].length; x++) {
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

		if (size === 2) {
			panel['FILE_PREVIEW_POSITION']['SIZE'] = 2;
			flex = 50;
		} else if (size === 3) {
			panel['FILE_PREVIEW_POSITION']['SIZE'] = 3;
			flex = 75;
		}

		for (let x = xPos; x < panel['GRIDS'].length; x++) {
			for (let y = yPos; y < yPos + size; y++) {
				panel['GRIDS'][x][y] = {
					IS_EMPTY: false,
					HEIGHT: panel['GRIDS'].length,
					WIDTH: 100,
					FIELD_ID: field,
				};
			}
		}

		let row1 = '';
		let row2 = '';
		for (let x = xPos; x < panel['GRIDS'].length; x++) {
			if (yPos === 1 && size === 2) {
				row1 =
					row1 + this.buildRowForFilePreview(panel, 3, x, yPos, panelIndex);
				row2 = row2 + this.buildRowForFilePreview(panel, 3, x, 0, panelIndex);
			} else {
				rows =
					rows + this.buildRowForFilePreview(panel, size, x, yPos, panelIndex);
			}
		}
		const flexRegex = new RegExp('COLUMN_FLEX');
		const rowsRegex = new RegExp('ADD_ROWS_FOR_THIS_COLUMN');
		let columnWithRows = columnTemplate.replace(rowsRegex, rows);
		columnWithRows = columnWithRows.replace(flexRegex, (100 - flex).toString());

		let columnWithFilePreview = columnTemplate.replace(
			flexRegex,
			flex.toString()
		);
		columnWithFilePreview = columnWithFilePreview.replace(
			rowsRegex,
			this.getTemplateForGrids(
				panel,
				panel['GRIDS'][xPos][yPos],
				xPos,
				yPos,
				panelIndex
			)
		);
		let mainTemplate = `<div class='FILE_PREVIEW_SECTION' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			// FIRST COLUMN FILE_PREVIEW
			mainTemplate = mainTemplate + columnWithFilePreview + columnWithRows;
		} else if (yPos === 1 && size === 2) {
			// FILE_PREVIEW IN MIDDLE
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
				mainTemplate +
				column1WithRows1 +
				columnWithFilePreview +
				column2WithRows2;
		} else {
			// FILE_PREVIEW AT END
			mainTemplate = mainTemplate + columnWithRows + columnWithFilePreview;
		}
		mainTemplate = mainTemplate + `</div><!--END_FILE_PREVIEW_SECTION-->`;
		return mainTemplate;
	}

	private getTemplateForGrids(panel, grid, i, j, panelIndex) {
		if (grid['WIDTH'] !== 0) {
			return `<div class='CELL_${panel.NAME}_${i}_${j}'
        	fxFlex="{{context.panels[${panelIndex}].GRIDS[${i}][${j}].WIDTH}}">
      		</div><!--END_CELL_${panel.NAME}_${i}_${j}-->`;
		} else {
			return `<div class='CELL_${panel.NAME}_${i}_${j}' *ngIf="context.panels[${panelIndex}].GRIDS[${i}][${j}].WIDTH !== 0">
			</div><!--END_CELL_${panel.NAME}_${i}_${j}-->`;
		}
	}

	// ROWS FOR FILE_PREVIEW
	private buildRowForFilePreview(panel, size, i, j, panelIndex) {
		let row = `<div class='ROW_${i}' fxLayoutGap=5px fxLayout=row fxFlex>`;
		for (let y = 0; y < 4; y++) {
			if (size === 2 && j !== 1) {
				if (y !== j && y !== j + 1) {
					panel['GRIDS'][i][y].WIDTH = 50;
					row =
						row +
						this.getTemplateForGrids(
							panel,
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
							panel,
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

	public getFilePreviewTemplate(panelName, xPos, yPos, panelIndex) {
		return ` <div class="CELL_${panelName}_${xPos}_${yPos}" fxLayout="column" fxFlex="{{context.panels[${panelIndex}].
		GRIDS[${xPos}][${yPos}].WIDTH}}" [ngStyle]="{'border-radius': '5px'}" *ngIf="context.attachmentsList.length > 0">
		${this.getFilePreview()}</div>
		<!--END_CELL_${panelName}_${xPos}_${yPos}-->`;
	}

	// This function returns back the preview field builder.
	public getFilePreview() {
		return `<mat-card fxLayout="column" style="max-width: 400px;padding: 0">
			<mat-card-header fxLayoutAlign=" center" style="padding:10px">
				<mat-card-subtitle style="margin:0">{{context.pdfSrc['TITLE']}}</mat-card-subtitle>
			</mat-card-header>
			<mat-card-content style="height: 500px;padding: 0px 10px">
				<pdf-viewer [src]="context.pdf" [autoresize]="true" [original-size]="false" [fit-to-page]="true" 
				[show-borders]="true" [render-text]="true" [page]="context.page" [rotation]="context.rotate" [show-all]="true" 
				[zoom-scale]="page-fit" [zoom]="context.zoom" style="max-width: 700px;"></pdf-viewer>
			</mat-card-content>
			<mat-card-actions fxLayout="row" fxLayoutGap="10px" fxLayoutAlign="center center" style="padding: 5px 0; background: #ebebeb">
				<div>
					<button mat-icon-button>
						<mat-icon style="cursor: pointer" (click)="context.rotate = context.rotate - 90">rotate_left</mat-icon>
					</button>
					<button mat-icon-button>
						<mat-icon style="cursor: pointer" (click)="context.zoom = context.zoom - 0.5">zoom_out</mat-icon>
					</button>
					<button mat-icon-button [disabled]="context.attachmentsList.indexOf(context.pdfSrc) === 0" (click)="context.previousDocument()">
						<mat-icon>chevron_left</mat-icon>
					</button>
					<mat-label>{{ context.attachmentsList.indexOf(context.pdfSrc) + 1 }} &#47; {{ context.attachmentsList.length }}</mat-label>
					<button mat-icon-button [disabled]="context.attachmentsList.indexOf(context.pdfSrc) === context.attachmentsList.length - 1" 
					(click)="context.nextDocument()">
						<mat-icon>chevron_right</mat-icon>
					</button>
					<button mat-icon-button>
						<mat-icon style="cursor: pointer" mat-icon (click)="context.zoom = context.zoom + 0.5">zoom_in</mat-icon>
					</button>
					<button mat-icon-button>
						<mat-icon style="cursor: pointer" (click)="context.rotate = context.rotate + 90">rotate_right</mat-icon>
					</button>
					<button mat-icon-button color="primary" (click)="context.downloadPdf(context.pdfSrc)">
						<mat-icon class="light-grey-color" fontSet="material-icons-outlined">download</mat-icon>
					</button>
				</div>
			</mat-card-actions>
		</mat-card>`;
	}

	// Return back the file as blob
	public getPdf(subdomain, attachmentUuid, moduleId, dataId,filePreviewFieldId) {
		const url = `https://${subdomain}.ngdesk.com/api/ngdesk-data-service-v1/attachments?message_id&module_id=${moduleId}&data_id=${dataId}&attachment_uuid=${attachmentUuid}&field_id=${filePreviewFieldId}`;
		return this.httpClient.get(url, { responseType: 'blob' });
	}
}
