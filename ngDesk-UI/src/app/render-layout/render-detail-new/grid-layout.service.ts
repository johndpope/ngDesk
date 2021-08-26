import { Injectable } from '@angular/core';

import { CommonLayoutService } from '@src/app/render-layout/render-detail-new/common-layout.service';
import { DiscussionService } from '@src/app/render-layout/data-types/discussion.service';
import { FilePreviewService } from '../data-types/file-preview.service';
import { ImageviewerService } from '../data-types/imageviewer.service';
import { ReceiptCaptureService } from '../data-types/receipt-capture.service';

@Injectable({
	providedIn: 'root',
})
export class GridLayoutService {
	constructor(
		private discussionService: DiscussionService,
		private filePreviewService: FilePreviewService,
		private commonLayoutService: CommonLayoutService,
		private imageViewer: ImageviewerService,
		private receiptViewer: ReceiptCaptureService
	) {}

	imageViewerWidth = 0;
	imgpossition: any = {};
	hasImage: boolean = false;
	imageFieldAdded: boolean = false;

	receiptWidth = 0;
	receiptPossition: any = {};
	hasReceipt: boolean = false;
	receiptFieldAdded: boolean = false;

	public getCustomPanelsForGridLayout(layout) {
		const customPanels = [];
		layout['PANELS'].forEach((panel) => {
			const customPanel = {
				DISPLAY_TYPE: panel['DISPLAY_TYPE'],
				NAME: panel['ID'],
				DISPLAY_NAME: panel['PANEL_NAME'],
				TEMPLATE: '',
				COLLAPSE: panel['SETTINGS']['COLLAPSABLE'],
				GRIDS: JSON.parse(JSON.stringify(panel['GRIDS'])),
				FIELDS: [],
				HAS_DISCUSSION: false,
				DISCUSSION_POSITION: {
					X_POS: null,
					Y_POS: null,
					SIZE: 0,
				},
				HAS_FILE_PREVIEW: false,
				FILE_PREVIEW_POSITION: {
					X_POS: null,
					Y_POS: null,
					SIZE: 0,
				},
				SETTINGS: {
					ACTION: panel['SETTINGS']['ACTION'],
					CONDITIONS: panel['SETTINGS']['CONDITIONS'],
				},
				DISPLAY: true,
			};
			customPanels.push(customPanel);
		});
		return customPanels;
	}

	public showTabs(layout) {
		const tabbedLayout = layout['PANELS'].find(
			(panel) => panel['DISPLAY_TYPE'] === 'Tab'
		);
		if (tabbedLayout) {
			return true;
		}
		return false;
	}

	public buildTemplates(panels, module, layoutType, editLayout) {
		const layoutStyle = this.commonLayoutService.getLayoutStyle(editLayout);
		// LOOP THROUGH EACH PANEL
		panels.forEach((panel, panelIndex) => {
			let layout = `<div fxLayout="column" fxFlex>`;
			// LOOP THROUGH EACH GRID OF THE PANEL
			panel['GRIDS'].forEach((row, i) => {
				layout += `<div class='ROW_${i}' fxLayout="row" fxLayoutGap=10px>`;

				// LOOPING 4 TIMES FOR 4-COLUMNS ON EACH ROW OF PANEL
				row.forEach((grid, j) => {
					if (grid) {
						if (grid['IS_EMPTY']) {
							layout += this.getTemplateForGrids(
								panel['NAME'],
								grid,
								i,
								j,
								panelIndex
							);
						} else {
							panel['FIELDS'].push(grid['FIELD_ID']);
							const moduleField = module['FIELDS'].find(
								(field) => field['FIELD_ID'] === grid['FIELD_ID']
							);
							if (
								moduleField &&
								!panel['HAS_DISCUSSION'] &&
								moduleField['DATA_TYPE']['DISPLAY'] === 'Discussion'
							) {
								panel['HAS_DISCUSSION'] = true;
								panel['DISCUSSION_POSITION']['X_POS'] = i;
								panel['DISCUSSION_POSITION']['Y_POS'] = j;
							} else if (
								moduleField &&
								!panel['HAS_FILE_PREVIEW'] &&
								moduleField['DATA_TYPE']['DISPLAY'] === 'File Preview'
							) {
								panel['HAS_FILE_PREVIEW'] = true;
								panel['FILE_PREVIEW_POSITION']['X_POS'] = i;
								panel['FILE_PREVIEW_POSITION']['Y_POS'] = j;
							} else if (
								moduleField &&
								!this.hasImage &&
								moduleField['DATA_TYPE']['DISPLAY'] === 'Image'
							) {
								this.hasImage = true;
							} else if (
								moduleField &&
								!this.hasReceipt &&
								moduleField['DATA_TYPE']['DISPLAY'] === 'Receipt Capture'
							) {
								this.hasReceipt = true;
							}

							layout += this.getTemplateForGrids(
								panel['NAME'],
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
			panel['TEMPLATE'] = `
				<!--CUSTOM_LAYOUT_START-->
				<!--START_REPLACABLE_LAYOUT-->
				${layout}
				<!--END_REPLACABLE_LAYOUT-->`;

			if (panel['HAS_DISCUSSION']) {
				let discussionWidth = 0;
				const gridRow = panel['GRIDS'][panel['DISCUSSION_POSITION']['X_POS']];
				gridRow.forEach((grid) => {
					if (!grid['IS_EMPTY']) {
						const moduleField = module['FIELDS'].find(
							(field) => field['FIELD_ID'] === grid['FIELD_ID']
						);
						if (moduleField['DATA_TYPE']['DISPLAY'] === 'Discussion') {
							discussionWidth += 1;
						}
					}
				});
				panel['DISCUSSION_POSITION']['SIZE'] = discussionWidth;
				panel = this.discussionService.getTemplateForGrid(
					panel,
					panelIndex,
					module
				);
			} else if (panel['HAS_FILE_PREVIEW']) {
				let filePreviewWidth = 0;
				const gridRow = panel['GRIDS'][panel['FILE_PREVIEW_POSITION']['X_POS']];
				gridRow.forEach((grid) => {
					if (!grid['IS_EMPTY']) {
						const moduleField = module['FIELDS'].find(
							(field) => field['FIELD_ID'] === grid['FIELD_ID']
						);
						if (moduleField['DATA_TYPE']['DISPLAY'] === 'File Preview') {
							filePreviewWidth += 1;
						}
					}
				});
				panel['FILE_PREVIEW_POSITION']['SIZE'] = filePreviewWidth;
				panel = this.filePreviewService.getTemplateForGrid(
					panel,
					panelIndex,
					module
				);
			}

			//To call Image viewer
			//to get grid

			panel['GRIDS'].forEach((row, i) => {
				//to get col
				row.forEach((grid, j) => {
					const currentField = module['FIELDS'].find(
						(field) => field['FIELD_ID'] === grid['FIELD_ID']
					);
					if (
						!grid['IS_EMPTY'] &&
						currentField['DATA_TYPE']['DISPLAY'] === 'Image'
					) {
						this.imageViewerWidth = 0;
						const gridRow = panel['GRIDS'][i];
						//to get fields
						gridRow.forEach((clum) => {
							if (
								!clum['IS_EMPTY'] &&
								clum['FIELD_ID'] &&
								clum['FIELD_ID'] != '' &&
								clum['FIELD_ID'] == currentField['FIELD_ID']
							) {
								this.imageViewerWidth = this.imageViewerWidth + 1;
								if (this.imageFieldAdded == false) {
									this.imgpossition = {
										xPossition: i,
										yPossition: j,
										currentField: currentField,
									};
								}
								this.imageFieldAdded = true;
							}
						});
					} else if (
						!grid['IS_EMPTY'] &&
						currentField['DATA_TYPE']['DISPLAY'] === 'Receipt Capture'
					) {
						this.receiptWidth = 0;
						const gridRow = panel['GRIDS'][i];
						//to get fields
						gridRow.forEach((clum) => {
							if (
								!clum['IS_EMPTY'] &&
								clum['FIELD_ID'] &&
								clum['FIELD_ID'] != '' &&
								clum['FIELD_ID'] == currentField['FIELD_ID']
							) {
								this.receiptWidth = this.receiptWidth + 1;
								if (this.receiptFieldAdded == false) {
									this.receiptPossition = {
										xPossition: i,
										yPossition: j,
										currentField: currentField,
									};
								}
								this.receiptFieldAdded = true;
							}
						});
					}
				});
			});

			// method call
			if (this.hasImage == true) {
				panel = this.imageViewer.getLayoutForImageViewer(
					panel,
					this.imgpossition.xPossition,
					this.imgpossition.yPossition,
					this.imgpossition.currentField,
					panelIndex,
					this.imageViewerWidth,
					layoutStyle,
					layoutType
				);
				this.hasImage = false;
			}
			if (this.hasReceipt == true) {
				panel = this.receiptViewer.getLayoutForReceipt(
					panel,
					this.receiptPossition.xPossition,
					this.receiptPossition.yPossition,
					this.receiptPossition.currentField,
					panelIndex,
					this.receiptWidth,
					layoutStyle,
					layoutType
				);
				this.hasReceipt = false;
			}

			panel['GRIDS'].forEach((row, i) => {
				row.forEach((grid, j) => {
					if (!grid['IS_EMPTY']) {
						const currentField = module['FIELDS'].find(
							(field) => field['FIELD_ID'] === grid['FIELD_ID']
						);
						if (
							currentField['DATA_TYPE']['DISPLAY'] !== 'Discussion' &&
							currentField['DATA_TYPE']['DISPLAY'] !== 'File Preview' &&
							currentField['DATA_TYPE']['DISPLAY'] !== 'Image' &&
							currentField['DATA_TYPE']['DISPLAY'] !== 'Receipt Capture'
						) {
							const templateForDataType =
								this.commonLayoutService.getTemplateForField(
									currentField,
									layoutStyle,
									layoutType
								);
							const cellRegex = new RegExp(
								`<div class='CELL_${panel['NAME']}_${i}_${j}([\\s\\S]*?)<!--END_CELL_${panel['NAME']}_${i}_${j}-->`,
								'g'
							);
							const fieldTemplateForGrid = this.getFieldTemplateForGrid(
								panel['NAME'],
								i,
								j,
								panelIndex,
								templateForDataType
							);
							panel['TEMPLATE'] = panel['TEMPLATE'].replace(
								cellRegex,
								fieldTemplateForGrid
							);
						}
					}
				});
			});
		});

		return panels;
	}

	public getTitleBarTemplate(module, layout, layoutType) {
		const layoutStyle = this.commonLayoutService.getLayoutStyle(layout);
		let titleBarTemplate = '<div fxLayoutGap=10px>';
		layout.TITLE_BAR.forEach((grid) => {
			const currentField = module['FIELDS'].find(
				(field) => field['FIELD_ID'] === grid['FIELD_ID']
			);

			titleBarTemplate =
				titleBarTemplate +
				this.commonLayoutService.getTemplateForField(
					currentField,
					layoutStyle,
					layoutType
				);
		});
		return titleBarTemplate + '</div>';
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

	private getFieldTemplateForGrid(panelName, i, j, panelIndex, fieldTemplate) {
		return `<div
			class="CELL_${panelName}_${i}_${j}"
			fxLayout="row"
			fxFlex="calc({{context.panels[${panelIndex}].GRIDS[${i}][${j}].WIDTH}}% - 10px)"
			fxLayoutAlign="center center"
			[ngStyle]="{'border-radius': '5px','margin':'10px'}"
			fxLayoutGap="15px" >
		${fieldTemplate}
		</div>
		<!--END_CELL_${panelName}_${i}_${j}-->
		`;
	}
}
