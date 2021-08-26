import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
	providedIn: 'root',
})
export class PhoneService {
	constructor(private translateService: TranslateService) {}

	public getPhone(field, layoutStyle) {
		const enterToSearch = this.translateService.instant('ENTER_TO_SEARCH');
		const noCountryFound = this.translateService.instant('NO_COUNTRY_FOUND');
		return `
		<div *ngIf = "context.entry.${field.NAME}"
		[ngStyle]="{'width': '100%','display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}" fxLayout="column">
        <mat-label [ngStyle]="{'color': 'rgba(0,0,0,.54)', 'font-size': '12px', 'margin-bottom': '-6px',
		'font-family': 'Roboto, Helvetica Neue, sans-serif'}" *ngIf="!context.fieldsMap['${field.FIELD_ID}'].REQUIRED" 
		[ngStyle]="{'width': '100%','display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
		${field.DISPLAY_LABEL}</mat-label>
		 <mat-label [ngStyle]="{'color': 'rgba(0,0,0,.54)', 'font-size': '12px', 'margin-bottom': '-6px',
		'font-family': 'Roboto, Helvetica Neue, sans-serif'}" *ngIf="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
		[ngStyle]="{'width': '100%','display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
		${field.DISPLAY_LABEL} *</mat-label>
		<mat-form-field fxFlex appearance="${layoutStyle}"
		[ngStyle]="{'width': '100%','display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}">
        <section matPrefix [ngStyle]="{'margin-right': '10px'}" >
          <input matInput [ngStyle]="{'display': 'none'}" [disabled]="!context.editAccess || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']" autocomplete="off">
          <input type="image" (click)=select.open()
          src="../../../assets/images/country-flags/{{context.entry.${field.NAME}.COUNTRY_FLAG}}"
              height="25" [disabled]="!context.editAccess || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']">
		  <mat-select #select (selectionChange)="context.updatePhoneInfo($event.value, '${field.NAME}')"
		  class="phone-select-search">
			<mat-option>
      			<ngx-mat-select-search ngModel (ngModelChange)="context.renderDetailDataSerice.searchCountriesForPhoneField($event)"
					   placeholderLabel="${enterToSearch}"
					    noEntriesFoundLabel="${noCountryFound}"></ngx-mat-select-search>
   			 </mat-option>
			<mat-option *ngFor="let country of context.renderDetailDataSerice.filteredCountriesForPhoneField"
				[value]="country" >
				<img [ngStyle]="{'margin-right': '10px'}" aria-hidden [src]="'../../assets/images/country-flags/' + country.COUNTRY_FLAG"
					height="25">
				<span>{{country.COUNTRY_NAME}}</span> | <small>{{country.COUNTRY_DIAL_CODE}}</small>
			</mat-option>
		</mat-select>
        </section>
        <div fxLayout="row" [ngStyle]="{'margin-bottom': '-8px'}">
		<span [ngStyle]="{ 'color' : context.editAccess ? 'rgba(0,0,0)' : 'rgba(0,0,0,.32)', 'margin-right': '5px' }">
		{{context.entry.${field.NAME}.DIAL_CODE}}</span>
        <input type="number" onkeypress="return event.charCode >= 48 && event.charCode <= 57"
		matInput [(ngModel)]="context.entry.${field.NAME}.PHONE_NUMBER"
		(ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}')"
        [disabled]="!context.editAccess || (context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE && !context.createLayout) || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']">
		</div>
		<mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
		class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
        </mat-form-field>
        </div>
       `;
	}

	public getDefaultPhoneValue(moduleField) {
		const defaultValue = JSON.parse(moduleField.DEFAULT_VALUE);
		let result = {};
		if (defaultValue === null) {
			return {
				COUNTRY_CODE: 'us',
				DIAL_CODE: '+1',
				PHONE_NUMBER: '',
				COUNTRY_FLAG: 'us.svg',
			};
		} else {
			result = {
				COUNTRY_CODE: 'us',
				DIAL_CODE: '+1',
				PHONE_NUMBER: '',
				COUNTRY_FLAG: 'us.svg',
			};
			if (defaultValue.COUNTRY_CODE !== '') {
				result['COUNTRY_CODE'] = defaultValue.COUNTRY_CODE;
			}
			if (defaultValue.DIAL_CODE !== '') {
				result['DIAL_CODE'] = defaultValue.DIAL_CODE;
			}
			result['PHONE_NUMBER'] = defaultValue.PHONE_NUMBER;
			if (defaultValue.COUNTRY_FLAG !== '') {
				result['COUNTRY_FLAG'] = defaultValue.COUNTRY_FLAG;
			}
			return result;
		}
	}
}
