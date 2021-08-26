import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class DiscussionService {
	constructor() {}

	public getTemplateForGrid(panel, panelIndex, module) {
		const removeDivFrom = panel['DISCUSSION_POSITION']['X_POS'] + 1;
		const removeDivTo = panel['GRIDS'].length - 1;

		panel['TEMPLATE'] = panel['TEMPLATE'].replace(
			new RegExp(
				`<div class='ROW_${removeDivFrom}([\\s\\S]*?)<!--END_ROW_${removeDivTo}-->`
			),
			''
		);

		const rowRegex = new RegExp(
			`<div class='ROW_${panel['DISCUSSION_POSITION']['X_POS']}([\\s\\S]*?)<!--END_ROW_${panel['DISCUSSION_POSITION']['X_POS']}-->`
		);
		panel['TEMPLATE'] = panel['TEMPLATE'].replace(
			rowRegex,
			this.buildTemplateForGridLayout(
				panel,
				panel['DISCUSSION_POSITION']['SIZE'],
				panelIndex
			)
		);

		const xPos = panel['DISCUSSION_POSITION']['X_POS'];
		const yPos = panel['DISCUSSION_POSITION']['Y_POS'];

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
			this.getDiscussionTemplate(panel['NAME'], xPos, yPos, field, panelIndex)
		);
		return panel;
	}

	// TODO: REVIEW LOGIC HERE
	private buildTemplateForGridLayout(panel, size, panelIndex) {
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const discussionInitialSize = panel['DISCUSSION_POSITION']['SIZE'];
		const xPos = panel['DISCUSSION_POSITION']['X_POS'];
		const yPos = panel['DISCUSSION_POSITION']['Y_POS'];
		const field = panel['GRIDS'][xPos][yPos]['FIELD_ID'];

		if (discussionInitialSize === 3) {
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
			panel['DISCUSSION_POSITION']['SIZE'] = 2;
			flex = 50;
		} else if (size === 3) {
			panel['DISCUSSION_POSITION']['SIZE'] = 3;
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
				row1 = row1 + this.buildRowForDiscussion(panel, 3, x, yPos, panelIndex);
				row2 = row2 + this.buildRowForDiscussion(panel, 3, x, 0, panelIndex);
			} else {
				rows =
					rows + this.buildRowForDiscussion(panel, size, x, yPos, panelIndex);
			}
		}
		const flexRegex = new RegExp('COLUMN_FLEX');
		const rowsRegex = new RegExp('ADD_ROWS_FOR_THIS_COLUMN');
		let columnWithRows = columnTemplate.replace(rowsRegex, rows);
		columnWithRows = columnWithRows.replace(flexRegex, (100 - flex).toString());

		let columnWithDiscussion = columnTemplate.replace(
			flexRegex,
			flex.toString()
		);
		columnWithDiscussion = columnWithDiscussion.replace(
			rowsRegex,
			this.getTemplateForGrids(
				panel,
				panel['GRIDS'][xPos][yPos],
				xPos,
				yPos,
				panelIndex
			)
		);
		let mainTemplate = `<div class='DISCUSSION_SECTION' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			// FIRST COLUMN DISCUSSION
			mainTemplate = mainTemplate + columnWithDiscussion + columnWithRows;
		} else if (yPos === 1 && size === 2) {
			// DISCUSSION IN MIDDLE
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
				columnWithDiscussion +
				column2WithRows2;
		} else {
			// DISCUSSION AT END
			mainTemplate = mainTemplate + columnWithRows + columnWithDiscussion;
		}
		mainTemplate = mainTemplate + `</div><!--END_DISCUSSION_SECTION-->`;
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

	// ROWS FOR DISCUSSION
	private buildRowForDiscussion(panel, size, i, j, panelIndex) {
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

	private getDiscussionTemplate(panelName, xPos, yPos, field, panelIndex) {
		return `
        <div class="CELL_${panelName}_${xPos}_${yPos}" fxLayout="column" fxFlex="{{context.panels[${panelIndex}].GRIDS[${xPos}][${yPos}].WIDTH}}" [ngStyle]="{'border-radius': '5px'}">
            <div fxLayout="row" fxLayoutGap="20px" [ngStyle]="{'padding': '15px', 'min-height': '45vh'}">
                <div fxLayout="row" fxLayoutAlign="center center" [ngStyle]="{'border-radius': '50%', 'border': '1px solid #68737D','width': '40px', 'height': '40px'}">
                    <label class="mat-body-2">
                        {{context.userService.user.FIRST_NAME | firstLetter}}{{context.userService.user.LAST_NAME | firstLetter}}
                    </label>
                </div>
                <div fxLayout="column" fxFlex>
                    <div fxLayout="row">
                        <div fxFill class="text-no-bottom-padding no-hover-effect" fxLayout="column" fxFlex="100">
                            <div fxFlex>
                                <tinymce placeholder="" #editor [config]="context.config" [(ngModel)]="context.customModulesService.discussionControls['MESSAGE']" (ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}')"></tinymce>
                            </div>
                            <div fxLayout="row" fxLayoutAlign="space-between center" [ngStyle]="{'border': '1px solid #ccc','border-top':'0px'}">
                                <div fxLayout="row" fxLayoutAlign="start center" fxLayoutGap="10px" [ngStyle]="{'font-size': '20px', 'padding': '10px'}">
                                    <mat-icon inline class="pointer" (click)="fileInput.click()">attach_file</mat-icon>
                                    <input hidden type="file" #fileInput (change)="context.onFileChange($event)" />
                                    <ng-container *ngFor="let attachment of context.attachments; index as i">
                                        <label class="mat-body-strong">{{attachment.FILE_NAME}}</label>
                                        <mat-icon class="pointer" (click)="context.attachments.splice(i,1)">close</mat-icon>
                                    </ng-container>
									<div fxLayout="row" *ngIf="!context.createLayout">
                                        <div [ngStyle]="{'height': '22px', 'color': '#2F3941'}" class="pointer" matRipple [matMenuTriggerFor]="premadeResponses" fxLayout="row">
                                            <mat-icon>chat_bubble_outline</mat-icon>
                                            <div class="mat-small" [ngStyle]="{'margin-left': '2px'}">Pre-made responses</div>
                                        </div>
        
                                        <mat-menu #premadeResponses="matMenu">
                                            <div *ngIf="context.premadeResponses.length > 0">
                                                <div mat-menu-item class="pointer" *ngFor="let premadeResponse of context.premadeResponses" fxLayout="row" (click)="context.addPremadeResponse(premadeResponse)">
                                                    <span>{{premadeResponse.NAME}}</span>
                                                </div>
                                            </div>
                                            <div *ngIf="context.premadeResponses.length===0 && context.rolesService.role.NAME === 'SystemAdmin'">
                                                <button mat-menu-item (click)="context.newPremadeResponse()"><mat-icon>add</mat-icon>{{'NEW_PREMADE_RESPONSE' | translate}}</button>
                                            </div>
                                            <div *ngIf="context.premadeResponses.length===0 && context.rolesService.role.NAME !== 'SystemAdmin'">
                                                <span mat-menu-item>{{'NO_PREMADE_RESPONSES_FOUND' | translate}}</span>
                                            </div>
										</mat-menu>
                                    </div>
								</div>
								<div fxLayoutAlign="end center" *ngIf="!context.createLayout">
									<button mat-button (click)="context.publishDiscussion(true)">
										<mat-icon>send</mat-icon>
						  			</button>
								</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <mat-divider></mat-divider>
			<div class="DISCUSSION_SECTION_REPLACE" style="word-break: break-word;" fxFlex>
			<mat-tab-group *ngIf="context.customModulesService.layoutType !== 'create'"[selectedIndex]="0" (selectedTabChange)="context.customModulesService.onSelectDiscussionTabs($event)">
					<mat-tab label="context.customModulesService.discussionControls['SHOW_TYPE']">
						<ng-template mat-tab-label>
							<button mat-button [matMenuTriggerFor]="menu" style="color: black;">{{context.customModulesService.discussionControls['DISCUSSION_TYPE']}}
								<i class="material-icons">keyboard_arrow_down</i>
							</button>
							<mat-menu #menu="matMenu">
								<button mat-menu-item (click)="context.customModulesService.discussionControls['SHOW_TYPE']='MESSAGES';context.customModulesService.discussionControls['DISCUSSION_TYPE']='Messages'">Messages</button>
								<button *ngIf="context.rolesService.role.NAME !== 'Customers'" mat-menu-item (click)="context.customModulesService.discussionControls['SHOW_TYPE']='INTERNAL_COMMENTS';context.customModulesService.discussionControls['DISCUSSION_TYPE']='Internal Comments'">Internal Comments</button>
								<button mat-menu-item (click)="context.customModulesService.discussionControls['SHOW_TYPE']='EVENTS';context.customModulesService.discussionControls['DISCUSSION_TYPE']='Events'">Events</button>
							</mat-menu>
						</ng-template>
					</mat-tab>
					<mat-tab label="All"></mat-tab>
				</mat-tab-group>
				<div fxLayout="column" *ngFor="let message of context.entry['${field.NAME}'] | reverse">
				<div fxLayout="row" fxLayoutGap="20px" [ngStyle]="{'padding': '15px'}" *ngIf="(message.MESSAGE_TYPE==='META_DATA' &&
				 context.customModulesService.discussionControls['SHOW_TYPE']=='EVENTS'&&context.customModulesService.discussionControls['DISCUSSION_TYPE']=='Events')||
				 (context.customModulesService.discussionControls['SHOW_TYPE'] === 'ALL' )" fxLayout="column" fxFlex="100">
				<div *ngIf = "message.MESSAGE_TYPE==='META_DATA'"fxLayout="row" fxLayoutAlign=" center" [ngStyle]="{'color': '#68737D'}">
					<label class="mat-caption">Event occurred at {{message.DATE_CREATED | date: 'MMM d, y h:mm a'}}</label>
				</div>
				<p *ngIf = "message.MESSAGE_TYPE==='META_DATA'" class="mat-body" [ngStyle]="{'margin-bottom': '0px'}" [innerHtml]="message.MESSAGE | allowStyles"></p>
			</div>
					<div fxLayout="row" fxLayoutGap="20px" [ngStyle]="{'padding': '15px'}" *ngIf="(message.MESSAGE_TYPE === 'MESSAGE' || message.MESSAGE_TYPE === 'INTERNAL_COMMENT') && 
					(context.customModulesService.discussionControls['SHOW_TYPE'] === 'ALL' || context.customModulesService.discussionControls['SHOW_TYPE'] === 'MESSAGES')">
                        <div fxLayout="row" fxLayoutAlign="center center" [ngStyle]="{'border-radius': '50%', 'border': '1px solid #68737D', 'width': '40px', 'height': '40px'}">
                            <label class="mat-body-2"> {{message.SENDER.FIRST_NAME | firstLetter}}{{message.SENDER.LAST_NAME | firstLetter}}</label>
                        </div>
                        <div fxLayout="column" fxFlex="100" fxLayoutGap="10px">
                            <div fxLayout="row" fxLayoutAlign=" center" fxLayoutGap="10px">
                                <div fxLayout="column">
                                    <label class="mat-body-strong" [ngStyle]="{'margin-bottom': '0px'}"> {{message.SENDER.FIRST_NAME}} {{message.SENDER.LAST_NAME}} </label>
                                </div>
                                <div fxLayout="column" [ngStyle]="{'color': '#68737D'}">
                                    <label class="mat-caption">{{message.DATE_CREATED | date: 'MMM d, y h:mm a'}}</label>
                                </div>
                            </div>
                            <div>
                                <p class="mat-body" [ngStyle]="{'margin-bottom': '0px'}" [innerHtml]="message.MESSAGE | allowStyles"></p>
                            </div>
                            <div fxLayout="row" fxLayoutGap="10px" class="pointer">
                                <div *ngFor="let attachment of message.ATTACHMENTS" fxLayoutAlign="start center" [ngStyle]="{'color': '#1f73b7', 'border-radius': '5px'}">
                                    <a
                                        [ngStyle]="{'color': '#1f73b7', 'text-decoration': 'none'}"
                                        class="mat-body"
                                        fxLayout="row"
                                        fxLayoutAlign="center center"
                                        [attr.href]="context.downloadAttachment(attachment.ATTACHMENT_UUID, message.MESSAGE_ID)"
                                        target="blank"
                                        download="attachment.FILE_NAME"
                                    >
                                        <mat-icon>attach_file</mat-icon>{{attachment.FILE_NAME}}
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <mat-divider></mat-divider>
                </div>
            </div>
        </div>
        <!--END_CELL_${panelName}_${xPos}_${yPos}-->
        `;
	}

	getEventsSectionTeplate() {}
}
