import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { debounce } from 'lodash';
import { Subject } from 'rxjs';
import {
	debounceTime,
	distinctUntilChanged,
	switchMap,
	map,
} from 'rxjs/operators';
import { AppGlobals } from '../../../app/app.globals';

@Injectable({
	providedIn: 'root',
})
export class ConditionService {
	valuesStote: any = [];
	// valuesSubject = new Subject();
	moduleId: any;
	fieldId: any;

	constructor(private http: HttpClient, private globals: AppGlobals) {}

	getLayoutForConditionField(field, layoutStyle, layoutType) {
		let fieldTobedisplayed;
		if (field.DISPLAY_LABEL) {
			fieldTobedisplayed = field.DISPLAY_LABEL.split(' ')[0];
		}
		return `<div style ="padding-right:20px; padding-bottom:20px;" fxFlex="100" fxLayoutGap="10px" fxLayout="row" fxLayoutAlign="start center">
    
   <div  fxLayout="column" fxFlex = "33.33">
      <mat-form-field id = "field-dropdown" appearance="${layoutStyle}"[style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY ? 'hidden' : 'visible' "
          floatLabel="always" fxFlex [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
            <mat-label> Condition </mat-label>
            <input id = field_Input type="text" matInput [(ngModel)] = "context.selectedFieldName" [matAutocomplete]="field_auto" [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED">
          <mat-autocomplete #field_auto="matAutocomplete"(optionSelected)="context.addFieldDataForConditionField($event, context.fieldsMap['${field.FIELD_ID}']);">
          <mat-option *ngFor="let entry of context.fieldsListForCondition"[value]="entry.DISPLAY_LABEL" [disabled] = "entry.DISPLAY_LABEL != '${fieldTobedisplayed}'" >{{entry.DISPLAY_LABEL}}</mat-option>
        </mat-autocomplete>
  </mat-form-field>
    </div>


    <div  fxLayout="column" fxFlex = "33.33">
      <mat-form-field id = "operator-dropdown" appearance="${layoutStyle}" [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY ? 'hidden' : 'visible' "
        floatLabel="always" fxFlex [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
          <mat-label>Operator</mat-label>
            <input #Operator_Input type="text" matInput [(ngModel)] = "context.conditionFieldData.OPERATOR" [matAutocomplete]="operator_auto" [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED">
                    <mat-autocomplete #operator_auto="matAutocomplete"(optionSelected)="context.addOptionDataForConditionField($event);">
                      <mat-option  value="Is">Is</mat-option>
                      <mat-option value ="Is Anything">Is Anything</mat-option>
                      <mat-option  value ="Starts With">Starts With</mat-option>
                    </mat-autocomplete>
      </mat-form-field>
    </div>

    <div  fxLayout="column" fxFlex = "33.33" *ngIf = "context.conditionFieldData && context.conditionFieldData.OPERATOR !='Is Anything'">
    <mat-form-field id = "value-dropdown" appearance="${layoutStyle}" [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY ? 'hidden' : 'visible' "
      floatLabel="always" fxFlex [ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
        <mat-label>Value</mat-label>
          <input #value_Input type="text" matInput  [(ngModel)] = "context.conditionFieldData.VALUE" (ngModelChange)= "context.onValueChange($event)" [matAutocomplete]="value_auto" [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED">
                  <mat-autocomplete #value_auto="matAutocomplete"(optionSelected)="context.addValueDataForConditionField($event);">
                  <mat-option *ngFor="let entry of context.filteredValuesArray"[value]="entry">{{entry}}</mat-option>
                  </mat-autocomplete>
    </mat-form-field>
  </div>
    </div>`;
	}

	public getValuesForCondition(moduleId: any, fieldId: any) {
		const query = `{
                DATA: getDistinctValues(moduleId: "${moduleId}", fieldId:"${fieldId}", pageNumber: 0, pageSize: 10)
                }`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
