import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class PasswordService {
	constructor() {}

	public getPassword(field, layoutStyle) {
		return `<mat-form-field class="style-suffix" [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY? 'hidden' : 'visible' "
        [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
        appearance="${layoutStyle}" fxFlex ="100" floatLabel="always" >
          <mat-label>${field.DISPLAY_LABEL}</mat-label>
          <input matInput maxlength="128" [type]="context.passwordFieldMap['${field.FIELD_ID}'] ? 'text' : 'password'" autocomplete="new-password"
          [(ngModel)]="context.entry.${field.NAME}"
          (ngModelChange)="context.evaluateConditions('${field.FIELD_ID}');"
          [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
          [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']">
          <mat-icon style="cursor: pointer;" matSuffix class="color-primary" [matTooltip]="context.passwordFieldMap['${field.FIELD_ID}'] ? 'Hide' : 'Show'" (click)="context.showHide('${field.FIELD_ID}', '${field.NAME}')">{{context.passwordFieldMap['${field.FIELD_ID}'] ? 'visibility' : 'visibility_off'}}</mat-icon>
          <mat-icon style="cursor: pointer;" matSuffix class="color-primary" (click)="context.copyPassword('${field.NAME}')" matTooltip="{{'COPY_TO_CLIPBOARD' | translate}}">content_copy</mat-icon>
        <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
     </mat-form-field>
        `;
	}
}
