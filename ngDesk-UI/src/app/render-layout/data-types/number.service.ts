import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class NumberService {
	constructor() {}

	public getNumber(field, layoutStyle, layoutType) {
		if (layoutType === 'detail') {
			return `
      <mat-form-field [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' "
    appearance="${layoutStyle}" fxFlex floatLabel="always" [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
    <mat-label>${field.DISPLAY_LABEL}</mat-label>
    <input type="text" matInput autocomplete="off"
    [ngModel]="context.entry.${field.NAME} | localNumber"
    (ngModelChange)="context.evaluateConditions('${field.FIELD_ID}');context.entry.${field.NAME}=$event;context.calculatedValuesForFormula($event, '${field.FIELD_ID}')"
    [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
    [disabled]="!context.editAccess || context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']">
    <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
    <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
    </mat-form-field>
    `;
		} else {
			return `
      <mat-form-field [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
    appearance="${layoutStyle}" fxFlex floatLabel="always"  [style.visibility]="context.fieldSettingsMap.get('${field.FIELD_ID}')">
    <mat-label>${field.DISPLAY_LABEL}</mat-label>
    <input matInput autocomplete="off"
    [(ngModel)]="context.entry.${field.NAME} "
    (ngModelChange)="context.evaluateConditions('${field.FIELD_ID}'); context.calculatedValuesForFormula($event, '${field.FIELD_ID}');"
    [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
    [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']">
    <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
    <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
    </mat-form-field>
    `;
		}
	}
}
