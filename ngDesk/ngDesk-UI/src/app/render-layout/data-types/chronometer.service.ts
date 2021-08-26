import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class ChronometerService {
	constructor() {}
	public getChronometer(field, layoutStyle) {
		return `
            <div [style.visibility]="context.fieldSettingsMap.get('${field.FIELD_ID}') ? 'hidden' : 'visible'" 
            [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' " fxLayout="column">
            <span fxLayoutAlign="center">
            <mat-form-field appearance="${layoutStyle}" floatLabel="always" fxFlex=100>
            <mat-label>${field.DISPLAY_LABEL}</mat-label>
            <input [disabled]=true matInput type="text" placeholder="No Time Added"
            [(ngModel)] = "context.customModulesService.chronometerValues['${field.NAME}']" autocomplete="off"/>
            </mat-form-field>
            </span>
            <span fxLayoutAlign="center">
            <mat-form-field appearance="${layoutStyle}" fxFlex=100>
            <mat-label>{{'ADD_TIME_TO'|translate}}${field.DISPLAY_LABEL}</mat-label>
            <input type="text" matInput placeholder="0mo 0w 0d 0h 0m"
            [(ngModel)]="context.entry.${field.NAME}"
            [disabled]="(context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']"   
            (ngModelChange)="context.evaluateConditions('${field.FIELD_ID}');context.calculatedValuesForFormula($event, '${field.FIELD_ID}')"
            [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED" autocomplete="off">
            <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
            </mat-form-field></span>
            </div>
            `;
	}
}
