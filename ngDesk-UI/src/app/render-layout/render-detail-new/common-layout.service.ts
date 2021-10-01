import { Injectable } from '@angular/core';

import { AutoNumberService } from '@src/app/render-layout/data-types/auto-number.service';
import { ButtonService } from '@src/app/render-layout/data-types/button.service';
import { PasswordService } from '@src/app/render-layout/data-types/password.service';
import { CheckboxService } from '@src/app/render-layout/data-types/checkbox.service';
import { ChronometerService } from '@src/app/render-layout/data-types/chronometer.service';
import { CityService } from '@src/app/render-layout/data-types/city.service';
import { CountryService } from '@src/app/render-layout/data-types/country.service';
import { CurrencyService } from '@src/app/render-layout/data-types/currency.service';
import { DateTimeService } from '@src/app/render-layout/data-types/date-time.service';
import { DateService } from '@src/app/render-layout/data-types/date.service';
import { FileUploadService } from '@src/app/render-layout/data-types/file-upload.service';
import { FormulaService } from '@src/app/render-layout/data-types/formula.service';
import { ListTextService } from '@src/app/render-layout/data-types/list-text.service';
import { NumberService } from '@src/app/render-layout/data-types/number.service';
import { PhoneService } from '@src/app/render-layout/data-types/phone.service';
import { PicklistService } from '@src/app/render-layout/data-types/picklist.service';
import { PicklistMultiselectService } from '@src/app/render-layout/data-types/picklistMultiselect.service';
import { RelationshipService } from '@src/app/render-layout/data-types/relationship.service';
import { Street1Service } from '@src/app/render-layout/data-types/street1.service';
import { Street2Service } from '@src/app/render-layout/data-types/street2.service';
import { TextAreaService } from '@src/app/render-layout/data-types/text-area.service';
import { TextService } from '@src/app/render-layout/data-types/text.service';
import { TimeService } from '@src/app/render-layout/data-types/time.service';
import { PdfsService } from '../data-types/pdfs.service';
import { WorkflowStagesService } from './../data-types/workflowStages.service';
import { ZoomService } from '@src/app/render-layout/data-types/zoom.service';
import { ConditionService } from '../data-types/condition.service';

@Injectable({
	providedIn: 'root',
})
export class CommonLayoutService {
	constructor(
		private street1Service: Street1Service,
		private street2Service: Street2Service,
		private cityService: CityService,
		private countryService: CountryService,
		private autoNumberService: AutoNumberService,
		private buttonService: ButtonService,
		private passwordService: PasswordService,
		private checkboxService: CheckboxService,
		private chronometerService: ChronometerService,
		private currencyService: CurrencyService,
		private dateTimeService: DateTimeService,
		private dateService: DateService,
		private fileUploadService: FileUploadService,
		private pdfsService: PdfsService,
		private formulaService: FormulaService,
		private listTextService: ListTextService,
		private numberService: NumberService,
		private phoneService: PhoneService,
		private picklistService: PicklistService,
		private picklistMultiselectService: PicklistMultiselectService,
		private relationshipService: RelationshipService,
		private textAreaService: TextAreaService,
		private textService: TextService,
		private timeService: TimeService,
		private workflowStagesService: WorkflowStagesService,
		private zoomService: ZoomService,
		private conditionService: ConditionService
	) {}

	public getLayoutStyle(layout) {
		if (layout['LAYOUT_STYLE'] && layout['LAYOUT_STYLE'] !== null) {
			return layout['LAYOUT_STYLE'];
		}
		return 'standard';
	}

	public getTemplateForField(field, layoutStyle, layoutType) {
		switch (field.DATA_TYPE.DISPLAY) {
			case 'Street 1':
				return this.street1Service.getStreet1(field, layoutStyle);
			case 'Street 2':
				return this.street2Service.getStreet2(field, layoutStyle);
			case 'City':
			case 'State':
				return this.cityService.getCity(field, layoutStyle);
			case 'Country':
			case 'Zipcode':
				return this.countryService.getCountry(field, layoutStyle);
			case 'Text':
			case 'Email':
			case 'URL':
			case 'Id':
				return this.textService.getText(field, layoutStyle);
			case 'Picklist':
				return this.picklistService.getPicklist(field, layoutStyle);
			case 'Picklist (Multi-Select)':
				return this.picklistMultiselectService.getPicklistMultiselect(
					field,
					layoutStyle
				);
			case 'Phone':
				return this.phoneService.getPhone(field, layoutStyle);
			case 'Relationship':
				return this.relationshipService.getRelationship(field, layoutStyle);
				break;
			case 'List Text':
				return this.listTextService.getListText(field, layoutStyle);
			case 'Number':
				return this.numberService.getNumber(field, layoutStyle, layoutType);
			case 'Currency':
				return this.currencyService.getCurrency(field, layoutStyle);
			case 'Auto Number':
				return this.autoNumberService.getAutoNumber(field, layoutType);
				break;
			case 'Formula':
				return this.formulaService.getFormula(field, layoutStyle, layoutType);
			case 'Chronometer':
				return this.chronometerService.getChronometer(field, layoutStyle);
			case 'Checkbox':
				return this.checkboxService.getCheckbox(field);
			case 'Text Area':
			case 'Text Area Rich':
			case 'Text Area Long':
				return this.textAreaService.getTextArea(field, layoutStyle);
			case 'Date/Time':
				return this.dateTimeService.getDateTime(field, layoutStyle, layoutType);
			case 'Time':
				return this.timeService.getTime(field, layoutStyle);
			case 'Date':
				return this.dateService.getDate(field, layoutStyle, layoutType);
			case 'Button':
				return this.buttonService.getButton(field);
			case 'Password':
				return this.passwordService.getPassword(field, layoutStyle);
			case 'File Upload':
				return this.fileUploadService.getFileUpload(field, layoutType);
			case 'PDF':
				return this.pdfsService.getPdfs(field);
			case 'Zoom':
				return this.zoomService.getZoom(field);
			case 'Image':
				return this.fileUploadService.getImageUpload(field);
			case 'Condition':
				return this.conditionService.getLayoutForConditionField(
					field,
					layoutStyle,
					layoutType
				);

			default:
				return `<mat-form-field appearance="${layoutStyle}" 
				[ngStyle]="{'display': !context.fieldSettingsMap.get('${field.FIELD_ID}')? 'none':'block'}"
                [style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' " fxFlex floatLabel="always">
                <mat-label>${field.DISPLAY_LABEL}</mat-label>
                  <input matInput type="text" autocomplete="off"
                      [(ngModel)]="context.entry.${field.NAME}"
                      (ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}')"
                      [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
                      [disabled]="!context.editAccess || context.fieldsMap['${field.FIELD_ID}'].NOT_EDITABLE">
                      <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
                      <mat-hint>${field.HELP_TEXT}</mat-hint>
                </mat-form-field>`;
		}
	}
}
