import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class TimeService {
	constructor() {}

	public getTime(field, layoutStyle) {
		return `
		<mat-form-field appearance="${layoutStyle}" [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
		[style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' " fxFlex floatLabel="always">
		<mat-label>${field.DISPLAY_LABEL}</mat-label>
    <input matInput [owlDateTimeTrigger]="${field.NAME}" [owlDateTime]="${field.NAME}"
    [(ngModel)]="context.entry.${field.NAME}" autocomplete="off"
		(ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}')"
    [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
    [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']"
		(keypress)="false">
		<owl-date-time [pickerType]="'timer'" #${field.NAME}></owl-date-time>
	<mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"
        class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
    </mat-form-field>
	`;
	}
}
