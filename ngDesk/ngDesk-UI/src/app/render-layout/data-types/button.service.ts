import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class ButtonService {
	constructor() {}

	public getButton(field) {
		return `<div  fxFlex fxLayout="row" fxLayoutAlign="space-between center">
        <button fxFlex [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' "
        color="primary" mat-raised-button
        [disabled]="!context.editAccess || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']" (click)="context.triggerWorkflow('${field.FIELD_ID}')">
        ${field.DISPLAY_LABEL}</button>
        <mat-icon *ngIf="context.helpTextMap.get('${field.FIELD_ID}')" style="padding-left:8px;"  
                  class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>  
        </div>
        `;
	}
}
