import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class CheckboxService {
	constructor() {}

	public getCheckbox(field) {
		return `
		<div [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' "
		[ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}" fxFlex fxLayout="row">
            <mat-checkbox fxFlex [(ngModel)]="context.entry.${field.NAME}"
						(ngModelChange)="context.evaluateConditions('${field.FIELD_ID}');context.fieldsMapping('${field.NAME}')"
            [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
            [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']">
					   ${field.DISPLAY_LABEL} <span *ngIf="context.fieldsMap['${field.FIELD_ID}'].REQUIRED">*</span>
					   </mat-checkbox>
			<mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
					   </div>
					   `;
	}
}
