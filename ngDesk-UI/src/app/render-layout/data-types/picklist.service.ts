import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class PicklistService {
	constructor() {}

	public getPicklist(field, layoutStyle) {
		return `
      <mat-form-field 
       [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY ? 'hidden' : 'visible' "
        appearance="${layoutStyle}" floatLabel="always" fxFlex
        [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
        <mat-label>${field.DISPLAY_LABEL}</mat-label>
        <mat-select [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']"
          [required]="${field.REQUIRED}" [(ngModel)]="context.entry['${field.NAME}']"
          (ngModelChange)="context.evaluateConditions('${field.FIELD_ID}');
          context.concatenatedValuesForFormula($event, '${field.FIELD_ID}');
          context.getCalculatedExchangeRate()
          ">
          <mat-option *ngFor="let pickedValue of context.fieldsMap['${field.FIELD_ID}'].PICKLIST_VALUES" [value]="pickedValue">
            {{ pickedValue }}
          </mat-option>
        </mat-select>
        <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary" matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
        <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
      </mat-form-field>
            `;
	}
}
