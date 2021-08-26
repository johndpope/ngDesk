import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class FormsListTextService {
	constructor() {}

	public getListText(field, layoutStyle) {
		return `
    <mat-form-field  [style.visibility]="${field.VISIBILITY}  ? 'hidden' : 'visible' " appearance="${layoutStyle}" floatLabel="always" fxFlex >
    <mat-label>${field.DISPLAY_LABEL}</mat-label>
      <mat-chip-list #${field.NAME}ChipList  [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED">
      <mat-chip *ngFor="let entry of context.entry['${field.NAME}']" [selectable]="true" [removable]="true"
       (removed)="context.removeItem(entry, '${field.NAME}')">{{entry}}
        <mat-icon matChipRemove>cancel</mat-icon>
      </mat-chip>
      <input  [matChipInputFor]="${field.NAME}ChipList"
      [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
       [matChipInputSeparatorKeyCodes]="context.separatorKeysCodes"
       [matChipInputAddOnBlur]="true" (matChipInputTokenEnd)="context.addItem($event, '${field.NAME}')"
       >
       </mat-chip-list>
       <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
        class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
       <mat-error>${field.DISPLAY_LABEL} {{'IS_REQUIRED' | translate}}</mat-error>
    </mat-form-field>
          `;
	}
}
