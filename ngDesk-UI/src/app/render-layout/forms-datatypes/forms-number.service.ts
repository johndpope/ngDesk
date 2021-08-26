import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class FormsNumberService {
	constructor() {}

	public getNumber(field, layoutStyle) {
		return `
      <mat-form-field  [style.visibility]="${field.VISIBILITY}  ? 'hidden' : 'visible' " appearance="${layoutStyle}" fxFlex floatLabel="always" >
  		<mat-label>${field.DISPLAY_LABEL}</mat-label>
  		<input matInput type="number"
  			[(ngModel)]="context.entry.${field.NAME}"
  			[required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
  			[disabled]="!context.formEnabled">
			  <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
        class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
        <mat-error>${field.DISPLAY_LABEL} {{'IS_REQUIRED' | translate}}</mat-error>
  	  </mat-form-field>
        `;
	}
}
