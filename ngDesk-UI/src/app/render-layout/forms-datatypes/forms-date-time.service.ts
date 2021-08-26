import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class FormsDateTimeService {
	constructor() {}

	public getDateTime(field, layoutStyle) {
		return `
			<mat-form-field  [style.visibility]="${field.VISIBILITY}  ? 'hidden' : 'visible' " appearance="${layoutStyle}" fxFlex floatLabel="always">
  				<mat-label>${field.DISPLAY_LABEL}</mat-label>
  		<input matInput [owlDateTimeTrigger]="${field.NAME}" [owlDateTime]="${field.NAME}"
  			 [(ngModel)]="context.entry.${field.NAME}"
  			[required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
  			[disabled]="!context.formEnabled">
        <owl-date-time #${field.NAME}></owl-date-time>
        <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
        class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
  	  </mat-form-field>
	  `;
	}
}
