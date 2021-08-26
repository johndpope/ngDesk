import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class PicklistMultiselectService {
	constructor() {}

	public getPicklistMultiselect(field, layoutStyle) {
		const fieldControlName = field.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
		if (field.NAME === 'DISCOVERY_MAP') {
			return `
            <div fxFlex *ngIf="context.customModulesService.noDiscoveryMaps;else elseBlock" >
            <mat-form-field appearance="${layoutStyle}"
            [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY ? 'hidden' : 'visible' "
             floatLabel="always" fxFlex [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
            <mat-label>${field.DISPLAY_LABEL}</mat-label>
        <mat-chip-list  [disabled]="!context.editAccess || context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']" #${field.NAME}ChipList
        [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED">
                <mat-chip *ngFor="let entry of context.entry['${field.NAME}']" [selectable]='true'
                      [removable]=true (removed)="context.remove(entry, '${field.NAME}')" >
                      {{context.customModulesService.displayDiscoveryMapName.get(entry)}}
                      <mat-icon matChipRemove>cancel</mat-icon>
                </mat-chip>
                <input #${field.NAME}Input [(ngModel)]="context.customModulesService.selectedValue" [matAutocomplete]="${field.NAME}_auto"
                      [matChipInputFor]="${field.NAME}ChipList" [matChipInputSeparatorKeyCodes]="context.separatorKeysCodes"
                      [matChipInputAddOnBlur]="true" (matChipInputTokenEnd)="context.customModulesService.resetInput($event)"
                      (keyup)="context.customModulesService.onSearchValue()">
                </mat-chip-list>
          <mat-autocomplete (closed)="context.customModulesService.closeAutoCompleteForDiscoveryMap()" 
          #${field.NAME}_auto="matAutocomplete" (appAutocompleteScroll)="context.customModulesService.onScrollDiscoveryMaps();"
                  (optionSelected)="context.addDataForDiscoveryField(context.fieldsMap['${field.FIELD_ID}'],$event)">
                  <mat-option *ngFor="let entry of context.customModulesService.discoveryMaps" [value]="entry" 
                  [disabled]="context.entry | disableDiscoveryMapOption: '${field.NAME}' : entry"> 
                          {{entry.name}}
                  </mat-option>
                </mat-autocomplete>
                <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
          </mat-form-field>  
          </div>
          <ng-template #elseBlock>
           {{ "NO_DISCOVERY_MAP_PRESENT" | translate }}
          </ng-template>
            `;
		} else {
			return `
      <mat-form-field 
       [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY ? 'hidden' : 'visible' "
        appearance="${layoutStyle}" fxFlex floatLabel="always"
        [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
        <mat-label>${field.DISPLAY_LABEL}</mat-label>
        <mat-chip-list [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']" [required]="${field.REQUIRED}" #${field.NAME}ChipList>
            <mat-chip *ngFor="let value of context.entry['${field.NAME}']" [selectable]='true' [removable]=true
            (removed)="context.remove(value, '${field.NAME}')">
            {{value}}
                <mat-icon matChipRemove>cancel</mat-icon>
            </mat-chip>
            <input #${field.NAME}Input [matAutocomplete]="${field.NAME}_auto" [matChipInputFor]="${field.NAME}ChipList"
            [matChipInputAddOnBlur]="true" (matChipInputTokenEnd)="context.customModulesService.resetInput($event)">
        </mat-chip-list>

        <mat-autocomplete #${field.NAME}_auto="matAutocomplete" 
        (optionSelected)="context.addPicklistMultiselectValue(context.fieldsMap['${field.FIELD_ID}'],$event)" >
            <mat-option *ngFor="let pickedValue of context.fieldsMap['${field.FIELD_ID}'].PICKLIST_VALUES" [value]="pickedValue" 
            [disabled]="context.disableOption('${field.NAME}',pickedValue)">
            {{pickedValue}}
            </mat-option>
        </mat-autocomplete>

        <mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
            class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
        <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
      </mat-form-field>
      `;
		}
	}
}
