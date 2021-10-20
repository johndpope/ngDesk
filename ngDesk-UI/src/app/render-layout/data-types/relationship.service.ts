import { Injectable } from '@angular/core';
import { UsersService } from '@src/app/users/users.service';
@Injectable({
	providedIn: 'root',
})
export class RelationshipService {
	constructor(private usersService: UsersService) {}

	public getRelationship(field, layoutStyle) {
		const fieldControlName = field.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
		if (
			field.RELATIONSHIP_TYPE === 'Many to One' ||
			field.RELATIONSHIP_TYPE === 'One to One'
		) {
			return `
            <mat-form-field id = "${field.NAME.toLowerCase()}-dropdown" appearance="${layoutStyle}"
				[style.visibility]="context.fieldsMap['${
					field.FIELD_ID
				}'].VISIBILITY ? 'hidden' : 'visible' "
				floatLabel="always" fxFlex [ngStyle]="{'display': !context.fieldSettingsMap.get('${
					field.FIELD_ID
				}')? 'none':'block'}">
			<mat-label>${field.DISPLAY_LABEL}</mat-label>
            <input #${field.NAME}Input type="text"
           matInput [formControl]="context.customModulesService.formControls['${fieldControlName}']"
           [matAutocomplete]="${
							field.NAME
						}_auto" [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
            (blur)="context.clearInput(${field.NAME}Input, '${
				field.NAME
			}')" (ngModelChange)="context.calculatedValuesForFormula($event, '${
				field.FIELD_ID
			}')">
		  <mat-autocomplete (closed)="context.customModulesService.closeAutoComplete(context.module, context.fieldsMap['${
				field.FIELD_ID
			}'])"
		   (appAutocompleteScroll)="context.customModulesService.onScroll(context.fieldsMap['${
					field.FIELD_ID
				}'])"
		  #${field.NAME}_auto="matAutocomplete"
          (optionSelected)="context.addDataForRelationshipField(context.fieldsMap['${
						field.FIELD_ID
					}'],$event,'${fieldControlName}');">
            <mat-option [disabled]="context.customModulesService.fieldsDisableMap['${
							field.FIELD_ID
						}']" *ngFor="let entry of context.customModulesService.relationFieldFilteredEntries['${
				field.NAME
			}']"
             [value]="entry">{{entry['PRIMARY_DISPLAY_FIELD']}}
            </mat-option>
          </mat-autocomplete>
          <mat-icon  *ngIf = "context.entry['${
						field.NAME
					}'] && context.customModulesService.layoutType ==='edit'" matSuffix style="cursor: pointer;display: inline-block;"
(click)="context.navigateTorelationEntry('${field.MODULE}', 
context.fieldsMap['${field.FIELD_ID}']) ;" color="primary">link</mat-icon>
            <button matSuffix style="cursor: pointer;display: inline-block;padding: 0px;border: none;background: none;"
          (click)="context.openOneToOneCreateLayoutDialog('${
						field.MODULE
					}',  context.fieldsMap['${
				field.FIELD_ID
			}']) ;$event.stopPropagation();">
          <mat-icon matTooltip="Create entry for ${
						field.DISPLAY_LABEL
					}">add_circle_outline
          </mat-icon>
           </button>

            <mat-icon matSuffix style="display: inline-block;"  *ngIf="context.helpTextMap.get('${
							field.FIELD_ID
						}')"
                            class="color-primary"  matTooltip="${
															field.HELP_TEXT
														}">help_outline</mat-icon>
          <mat-error>${
						field.DISPLAY_LABEL
					} {{ "IS_REQUIRED" | translate }}.</mat-error>
          </mat-form-field>
          `;
		} else if (field.RELATIONSHIP_TYPE === 'Many to Many') {
			return `
            <mat-form-field appearance="${layoutStyle}"
					[style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY ? 'hidden' : 'visible' "
					 floatLabel="always" fxFlex [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
					<mat-label>${field.DISPLAY_LABEL}</mat-label>
			<mat-chip-list  [disabled]="!context.editAccess || context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']" #${field.NAME}ChipList
			[required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED">
              <mat-chip *ngFor="let entry of context.entry['${field.NAME}']" [selectable]='true'
                    [removable]=true (removed)="context.remove(entry, '${field.NAME}', trigautoTipDoc)" >
                    {{entry.PRIMARY_DISPLAY_FIELD}}
                    <mat-icon matChipRemove>cancel</mat-icon>
              </mat-chip>
              <input #${field.NAME}Input [matAutocomplete]="${field.NAME}_auto" #trigautoTipDoc ="matAutocompleteTrigger"
                    [matChipInputFor]="${field.NAME}ChipList" [matChipInputSeparatorKeyCodes]="context.separatorKeysCodes"
                    [matChipInputAddOnBlur]="true" (matChipInputTokenEnd)="context.customModulesService.resetInput($event)"
                    [formControl]="context.customModulesService.formControls['${fieldControlName}']">
              </mat-chip-list>
			  <mat-autocomplete (closed)="context.customModulesService.closeAutoComplete(context.module, context.fieldsMap['${field.FIELD_ID}'])"
			  (appAutocompleteScroll)="context.customModulesService.onScroll(context.fieldsMap['${field.FIELD_ID}'])"
			   #${field.NAME}_auto="matAutocomplete"
                (optionSelected)="context.addDataForRelationshipField(context.fieldsMap['${field.FIELD_ID}'],$event,'${fieldControlName}')">
                <mat-option *ngFor="let entry of context.customModulesService.relationFieldFilteredEntries['${field.NAME}']" [value]="entry"
                     [disabled]="context.entry | disableManyToManyOption: '${field.NAME}' : entry"> 
                        {{entry['PRIMARY_DISPLAY_FIELD']}}
                </mat-option>
              </mat-autocomplete>
              <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
              <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
        </mat-form-field>
        `;
		} else if (field.RELATIONSHIP_TYPE === 'One to Many') {
			return `<div  style="padding-bottom: 15px;" fxFlex="100" [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY ? 'hidden' : 'visible' " fxLayout="column"
			fxLayoutGap="10px" [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
			<div [style.visibility]="context.customModulesService.isSavingOneToMany ? 'hidden' : 'visible'">
				<mat-expansion-panel [expanded]="context.customModulesService.oneToManyControls['FIELD_IN_FOCUS'] === '${field.FIELD_ID}'" (click)="context.customModulesService.oneToManyControls['FIELD_IN_FOCUS'] = '${field.FIELD_ID}';context.customModulesService.setupOneToManyTable(context.module, '${field.MODULE}', context.fieldsMap['${field.FIELD_ID}'], context.entry);">
				   <mat-expansion-panel-header style="padding-left: 10px;">
					 <mat-panel-title fxFlex="95">
							<div fxLayout="row" fxLayoutAlign="space-between center">
								<label class="mat-h4" style="font-weight:500; margin: 0px"> ${field.DISPLAY_LABEL}
								</label>
							</div>
						</mat-panel-title>
                        <mat-panel-description *ngIf="context.customModulesService.layoutType !== 'create'" fxLayoutAlign="center center">
                            <button style="margin-right:'20px" mat-button [disabled]="!context.editAccess || context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']"
								(click)="context.openOneToManyMapDialog(context.fieldsMap['${field.FIELD_ID}']) ; $event.stopPropagation();">
								<mat-icon matTooltip="Map ${field.DISPLAY_LABEL} to this {{context.module.NAME}}">link</mat-icon>
							</button>
                            <button mat-button [disabled]="!context.editAccess || context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']"
                                (click)="context.openOneToManyCreateLayoutDialog('${field.MODULE}', context.fieldsMap['${field.FIELD_ID}'] ) ;$event.stopPropagation();">
								<mat-icon matTooltip="Create and map ${field.DISPLAY_LABEL} to this {{context.module.NAME}}">add_circle_outline
								</mat-icon>
							</button>
						</mat-panel-description>
					</mat-expansion-panel-header>
					          <div style="width: 100%;">
                <div (click)="$event.stopPropagation();" fxLayout="column" class="mat-elevation-z8" [ngStyle]="context.customModulesService.customTableService.totalRecords> 0 && !context.customModulesService.customTableService.isLoading?{'display': 'block'} : {'display': 'none'}">
                    <table mat-table
                        *ngIf="!context.customModulesService.customTableService.isLoading  && context.customModulesService.customTableService.totalRecords > 0"
                        [dataSource]="context.customModulesService.customTableService.customTableDataSource" matSort
                        (matSortChange)="context.customModulesService.onPageChange($event, '${field.MODULE}', context.fieldsMap['${field.FIELD_ID}']); context.customModulesService.oneToManyonPageChange = true;$event.stopPropagation();"
                        [matSortActive]="context.customModulesService.customTableService.activeSort.SORT_BY"
                        [matSortDirection]="context.customModulesService.customTableService.activeSort.ORDER_BY" matSortDisableClear="true"
                        [ngStyle]="context.customModulesService.customTableService.totalRecords > 0 && !context.customModulesService.customTableService.isLoading?{'width':'100%'}:{'width':'0%'}">
                        <!-- Checkbox Column -->
                        <ng-container matColumnDef="select">
                            <th mat-header-cell *matHeaderCellDef>
                                <mat-checkbox (change)="$event ? context.customModulesService.customTableService.masterToggle() : null"
                                    [checked]="context.customModulesService.customTableService.selection.hasValue() && context.customModulesService.customTableService.isAllSelected()"
                                    [indeterminate]="context.customModulesService.customTableService.selection.hasValue() && !context.customModulesService.customTableService.isAllSelected()">
                                </mat-checkbox>
                            </th>
                            <td mat-cell *matCellDef="let row" class="pointer">
                                <mat-checkbox (click)="$event.stopPropagation()"
                                    (change)="$event ? context.customModulesService.customTableService.selection.toggle(row) : null"
                                    [checked]="context.customModulesService.customTableService.selection.isSelected(row)">
                                </mat-checkbox>
                            </td>
                        </ng-container>
                        <ng-container matColumnDef="{{col.DISPLAY}}"
                            *ngFor="let col of context.customModulesService.customTableService.columnsHeadersObj ">
                            <th mat-header-cell *matHeaderCellDef mat-sort-header class="custom-theme-icon-color"> {{col.DISPLAY}}
                            </th>
                            <td mat-cell *matCellDef="let element" class="pointer" [matTooltip]="context.customModulesService.customTableService.showTooltip(element,col)" matTooltipClass="custom-tooltip">
                                <!--this container has the data to populate the rows of each column  -->
                                <span *ngIf="col.DATA_TYPE === 'Date/Time'">{{element[col.NAME] | dateFormat: 'medium'}}</span>
                                <span *ngIf="col.DATA_TYPE === 'Relationship' && element[col.NAME]">{{element[col.NAME].PRIMARY_DISPLAY_FIELD}}</span>
                                <span *ngIf="col.DATA_TYPE ==='Button'"><button mat-raised-button color="primary"
                                        (click)="customButtonClick(col, element)">{{col.DISPLAY}}</button></span>
                                <span *ngIf="col.DATA_TYPE === 'Date'">{{element[col.NAME] | dateFormat: 'mediumDate'}}</span>
                                <span *ngIf="col.DATA_TYPE === 'Time'">{{element[col.NAME] | dateFormat: 'h:mm a'}}</span>
                                <span *ngIf="col.DATA_TYPE === 'Number'">{{element[col.NAME] | localNumber}}</span>
                                <span *ngIf="col.DATA_TYPE === 'Phone'">{{element[col.NAME].DIAL_CODE}} {{element[col.NAME].PHONE_NUMBER}}</span>
                                <span
                                    *ngIf="col.DATA_TYPE!== 'Phone' && col.DATA_TYPE !== 'Button' && col.DATA_TYPE !== 'Date/Time' && col.DATA_TYPE !== 'Relationship' && col.DATA_TYPE !== 'Date' &&  col.DATA_TYPE !== 'Time'&& col.DATA_TYPE !== 'Number' && col.DATA_TYPE !=='Text' && col.DATA_TYPE !=='Text Area'">{{element[col.NAME]}}</span>
                                <span *ngIf="col.DATA_TYPE =='Text' || col.DATA_TYPE =='Text Area'">
                                   
                                        {{element[col.NAME] | truncate : 32}}
                                </span>
                                <div *ngIf="col.NAME ==='Remove Entry'">
                                <button 
                                (click)="$event.stopPropagation(); context.removeOneToManyEntry(element, context.fieldsMap['${field.FIELD_ID}']);" style="color: #F50057;" mat-button>
                                <mat-icon matTooltip="Remove">remove_circle_outline</mat-icon>
                            </button>
                            </div>
                                <!--this container the buttons for when table has action column  -->
                                <ng-container *ngTemplateOutlet="menuTemplate; context:{col:col,row:element}"></ng-container>
                            </td>
                        </ng-container>
                        <tr mat-header-row *matHeaderRowDef="context.customModulesService.customTableService.columnsHeaders;sticky:true;"
                            class="custom-theme-primary-color">
                        </tr>
                        <tr mat-row *matRowDef="let row; columns: context.customModulesService.customTableService.columnsHeaders"
                        (click)="context.navigateToManyToOneEntry(row, context.fieldsMap['${field.FIELD_ID}'])"></tr>
                    </table>
                    <mat-paginator [ngStyle]="context.customModulesService.customTableService.totalRecords> 0 && !context.customModulesService.customTableService.isLoading?{'display': 'block'} : {'display': 'none'}"  (page)="context.customModulesService.onPageChange($event, '${field.MODULE}', context.fieldsMap['${field.FIELD_ID}']); context.customModulesService.oneToManyonPageChange = true;$event.stopPropagation();"
                        [length]="context.customModulesService.customTableService.totalRecords"
                        [pageSize]="context.customModulesService.customTableService.pageSize"
                        [pageIndex]="context.customModulesService.customTableService.pageIndex" [pageSizeOptions]="[5, 10, 20]">
                    </mat-paginator>
                </div>
                <div flxLayout="row" fxLayoutAlign="center" *ngIf="context.customModulesService.customTableService.isLoading"
                    style="margin-top:30px">
                    <mat-label>No records found</mat-label>
                </div>
                <div *ngIf="!context.customModulesService.customTableService.isLoading  && !context.customModulesService.customTableService.totalRecords > 0"
                    fxLayoutAlign="center center">
                    <mat-label>No records found</mat-label>
                </div>
            </div>
				</mat-expansion-panel>
			</div>
            <div *ngIf="context.customModulesService.isSavingOneToMany">
                <mat-progress-spinner color="primary" mode="indeterminate">
                </mat-progress-spinner>
            </div>
		</div>
`;
		}
	}
}
