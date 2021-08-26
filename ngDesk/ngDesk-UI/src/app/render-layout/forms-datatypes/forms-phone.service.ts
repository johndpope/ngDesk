import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
	providedIn: 'root',
})
export class FormsPhoneService {
	constructor(private translateService: TranslateService) {}

	public getPhone(field, layoutStyle) {
		const selectPhone = 'selectPhone_' + field.NAME;
		const enterToSearch = this.translateService.instant('ENTER_TO_SEARCH');
		const noCountryFound = this.translateService.instant('NO_COUNTRY_FOUND');
		return `
		<div [style.visibility]="${field.VISIBILITY}  ? 'hidden' : 'visible' " fxLayout="column" [ngStyle]="{'width': '100%'}">
		<mat-label [ngStyle]="{'color': 'rgba(0,0,0,.54)', 'font-size': '12px', 'margin-bottom': '-6px',
	'font-family': 'Roboto, Helvetica Neue, sans-serif'}" *ngIf="!context.fieldsMap['${field.FIELD_ID}'].REQUIRED">
	${field.DISPLAY_LABEL}</mat-label>
	<mat-label [ngStyle]="{'color': 'rgba(0,0,0,.54)', 'font-size': '12px', 'margin-bottom': '-6px',
	  'font-family': 'Roboto, Helvetica Neue, sans-serif'}" *ngIf="context.fieldsMap['${field.FIELD_ID}'].REQUIRED">
	  ${field.DISPLAY_LABEL} *</mat-label>
		<mat-form-field fxFlex appearance="${layoutStyle}" >
		<section matPrefix [ngStyle]="{'margin-right': '10px'}" >
		  <input matInput [ngStyle]="{'display': 'none'}" [disabled]="!context.formEnabled" autocomplete="off">
		  <input type="image" (click)=${selectPhone}.open()
	  src="../../../assets/images/country-flags/{{context.entry.${field.NAME}.COUNTRY_FLAG}}" height="25" 
	  [disabled]="!context.formEnabled">
	  <mat-select #${selectPhone} (selectionChange)="context.updatePhoneInfo($event.value, '${field.NAME}')"
	class="phone-select-search">
	<mat-option>
				<ngx-mat-select-search ngModel (ngModelChange)="context.searchCountries($event)"
					 placeholderLabel="${enterToSearch}"
					  noEntriesFoundLabel="${noCountryFound}"></ngx-mat-select-search>
			  </mat-option>
			<mat-option *ngFor="let country of context.filteredCountries"
				[value]="country" >
			  <img [ngStyle]="{'margin-right': '10px'}" aria-hidden [src]="'../../assets/images/country-flags/' + country.COUNTRY_FLAG"
				  height="25">
			  <span>{{country.COUNTRY_NAME}}</span> | <small>{{country.COUNTRY_DIAL_CODE}}</small>
			</mat-option>
	  </mat-select>
		</section>
		<div fxLayout="row" [ngStyle]="{'margin-bottom': '-8px'}">
		<span [ngStyle]="{ 'color' : context.formEnabled ? 'rgba(0,0,0)' : 'rgba(0,0,0,.32)' }">
	  {{context.entry.${field.NAME}.DIAL_CODE}}</span>
		<input type="number" matInput [(ngModel)]="context.entry.${field.NAME}.PHONE_NUMBER"
		[disabled]="!context.formEnabled">
	</div>
	<mat-icon matSuffix *ngIf="context.helpTextMap.get('${field.FIELD_ID}')"  
        class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
		</mat-form-field>
		</div>
       `;
	}
}
