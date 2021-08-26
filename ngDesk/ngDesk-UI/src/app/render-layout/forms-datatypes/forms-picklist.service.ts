import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class FormsPicklistService {
	constructor() {}

	public getPicklist(field, layoutStyle) {
		return `
    <mat-form-field [style.visibility]="${field.VISIBILITY}  ? 'hidden' : 'visible' " appearance="${layoutStyle}" fxFlex floatLabel="always" fxFlex >
    <mat-label>${field.DISPLAY_LABEL}</mat-label>
<mat-select [disabled]="!context.formEnabled"
   [required]="${field.REQUIRED}" [(ngModel)]="context.entry['${field.NAME}']">
  <mat-option *ngFor="let pickedValue of context.fieldsMap['${field.FIELD_ID}'].PICKLIST_VALUES" [value]="pickedValue">
  {{ pickedValue }}
  </mat-option>
</mat-select>
<mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
<mat-error>${field.DISPLAY_LABEL} {{'IS_REQUIRED' | translate}}</mat-error>
</mat-form-field>
            `;
	}
}
