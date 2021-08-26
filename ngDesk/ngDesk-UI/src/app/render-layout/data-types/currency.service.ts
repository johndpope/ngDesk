import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class CurrencyService {
	constructor() {}

	public getCurrency(field, layoutStyle) {
		return `
    <mat-form-field [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}" 
    [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' "
    appearance="${layoutStyle}" fxFlex floatLabel="always" >
    <mat-label>${field.DISPLAY_LABEL}</mat-label>
    <input type="number" matInput autocomplete="off"
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
