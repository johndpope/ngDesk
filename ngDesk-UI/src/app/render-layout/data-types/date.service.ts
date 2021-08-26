import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class DateService {
	constructor() {}

	public getDate(field, layoutStyle, layoutType) {
		if (layoutType === 'detail') {
			return `
            <mat-form-field appearance="${layoutStyle}"
            [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
        [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' "  fxFlex floatLabel="always">
        <mat-label>${field.DISPLAY_LABEL}</mat-label>
        <input type="text" matInput  autocomplete="off"
        [ngModel]="context.entry.${field.NAME} | dateFormat: 'mediumDate'"
        (ngModelChange)="context.evaluateConditions('${field.FIELD_ID}');
        context.getCalculatedExchangeRate();
        context.entry.${field.NAME}=$event"
        [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
        [disabled]="!context.editAccess || context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']"
				(keypress)="false">
        <owl-date-time [pickerType]="'calendar'" #${field.NAME}></owl-date-time>
        <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
        </mat-form-field>
        `;
		} else {
			return `
            <mat-form-field appearance="${layoutStyle}"
            [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
        [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' " fxFlex floatLabel="always">
        <mat-label>${field.DISPLAY_LABEL}</mat-label>
        <input matInput [owlDateTimeTrigger]="${field.NAME}" [owlDateTime]="${field.NAME}"
        [(ngModel)]="context.entry.${field.NAME}" autocomplete="off"
        (ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}');
        context.getCalculatedExchangeRate()"
        [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
        [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']"
				(keypress)="false">
        <owl-date-time [pickerType]="'calendar'" #${field.NAME}></owl-date-time>
        <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
        </mat-form-field>
        `;
		}
	}
}
