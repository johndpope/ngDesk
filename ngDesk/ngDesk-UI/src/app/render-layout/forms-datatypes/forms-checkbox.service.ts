import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class FormsCheckboxService {
	constructor() {}

	public getCheckbox(field) {
		return `
		<div  [style.visibility]="${field.VISIBILITY}  ? 'hidden' : 'visible' " fxFlex fxLayout="column">
  					 <mat-checkbox fxFlex
  						 [(ngModel)]="context.entry.${field.NAME}"
  						 [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
  						 [disabled]="!context.formEnabled">
  					   ${field.DISPLAY_LABEL} <span *ngIf="context.fieldsMap['${field.FIELD_ID}'].REQUIRED">*</span>
               </mat-checkbox>
               <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
        class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
               </div>
					   `;
	}
}
