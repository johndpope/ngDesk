import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class Street2Service {
	constructor() {}

	public getStreet2(field, layoutStyle) {
		return `
    <mat-form-field [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
    appearance="${layoutStyle}" fxFlex floatLabel="always" >
    <mat-label>${field.DISPLAY_LABEL}</mat-label>
    <input matInput type="text" autocomplete="off"
      [(ngModel)]="context.entry.${field.NAME}"
      (ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}');context.concatenatedValuesForFormula($event, '${field.FIELD_ID}')"
      [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
      [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']">
      <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
          class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
      <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
    </mat-form-field>
    `;
	}
}
