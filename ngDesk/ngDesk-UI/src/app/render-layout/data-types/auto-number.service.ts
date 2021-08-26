import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class AutoNumberService {
	constructor() {}

	public getAutoNumber(field, layoutType) {
		if (layoutType === 'detail' || layoutType === 'edit') {
			return `
      <mat-form-field [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
    appearance="outline" fxFlex floatLabel="always">
    <mat-label>${field.DISPLAY_LABEL}</mat-label>
    <input matInput type="text" autocomplete="off"
      [(ngModel)]="context.entry.${field.NAME}" 
      (ngModelChange)="context.evaluateConditions('${field.FIELD_ID}');context.concatenatedValuesForFormula($event, '${field.FIELD_ID}')"
      [disabled]="true">
    </mat-form-field>
      `;
		}
	}
}
