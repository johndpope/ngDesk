import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class TextAreaService {
	constructor() {}

	public getTextArea(field, layoutStyle) {
		return `
		<mat-form-field [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
		[style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' "
		 appearance="${layoutStyle}" fxFlex floatLabel="always">
				<mat-label>${field.DISPLAY_LABEL}</mat-label>
       <textarea matInput [(ngModel)]="context.entry.${field.NAME}"
				(ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}');context.concatenatedValuesForFormula($event, '${field.FIELD_ID}')"
        [ngModelOptions]="{standalone: true}"
        [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
        [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']"></textarea>
		<mat-icon matSuffix style="position: relative; bottom: 10px;" *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
		<mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
        </mat-form-field>
		`;
	}
}
