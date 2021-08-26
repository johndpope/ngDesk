import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class TextService {
	constructor() {}

	public getText(field, layoutStyle) {
		if (field.NAME !== 'ROLE') {
			return `
        <mat-form-field [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY? 'hidden' : 'visible' "
          [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
          appearance="${layoutStyle}" fxFlex ="100" floatLabel="always" >
            <mat-label>${field.DISPLAY_LABEL}</mat-label>
            <input matInput type="text" autocomplete="off"
            [(ngModel)]="context.entry.${field.NAME}"
            (ngModelChange)="context.evaluateConditions('${field.FIELD_ID}');
            context.getCalculatedExchangeRate();
            context.concatenatedValuesForFormula($event, '${field.FIELD_ID}');"
            [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
            [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']">
            <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
          <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
       </mat-form-field>
      `;
		} else {
			return `
        <mat-form-field [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY? 'hidden' : 'visible' "
          [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
          appearance="${layoutStyle}" fxFlex="100" floatLabel="always">
            <mat-label>${field.DISPLAY_LABEL}</mat-label>
            <mat-select [disabled]="!context.editAccess || ${field.NOT_EDITABLE} || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']"
              [(ngModel)]="context.entry['${field.NAME}']"
              (ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}');
              context.getCalculatedExchangeRate()"
              [required]="${field.REQUIRED}">
                <mat-option *ngFor="let role of context.roles" [value]="role.ROLE_ID">
                  {{ role.NAME }}
                </mat-option>
            </mat-select>
            <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
          <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
        </mat-form-field>
        `;
		}
	}
}
