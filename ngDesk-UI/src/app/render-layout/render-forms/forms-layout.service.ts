import { Injectable } from '@angular/core';
import { FormsNumberService } from '../forms-datatypes/forms-number.service';
import { FormsCheckboxService } from '../forms-datatypes/forms-checkbox.service';
import { FormsDateTimeService } from '../forms-datatypes/forms-date-time.service';
import { FormsDateService } from '../forms-datatypes/forms-date.service';
import { FormsDiscussionService } from '../forms-datatypes/forms-discussion.service';
import { FormsListTextService } from '../forms-datatypes/forms-list-text.service';
import { FormsPhoneService } from '../forms-datatypes/forms-phone.service';
import { FormsPicklistService } from '../forms-datatypes/forms-picklist.service';
import { FormsRelationshipService } from '../forms-datatypes/forms-relationship.service';
import { FormsTextAreaService } from '../forms-datatypes/forms-text-area.service';
import { FormsTextService } from '../forms-datatypes/forms-text.service';
import { FormsTimeService } from '../forms-datatypes/forms-time.service';

@Injectable({
	providedIn: 'root',
})
export class FormsLayoutService {
	constructor(
		private formsTextService: FormsTextService,
		private formsPicklistService: FormsPicklistService,
		private formsPhoneService: FormsPhoneService,
		private formsRelationshipService: FormsRelationshipService,
		private formsListTextService: FormsListTextService,
		private formsNumberService: FormsNumberService,
		private formsDateTimeService: FormsDateTimeService,
		private formsTimeService: FormsTimeService,
		private formsDiscussionService: FormsDiscussionService,
		private formsTextAreaService: FormsTextAreaService,
		private formsDateService: FormsDateService,
		private formsCheckboxService: FormsCheckboxService
	) {}

	// FIELD SPECIFIC TEMPLATE
	public getTemplateForField(field, layoutStyle) {
		switch (field.DATA_TYPE.DISPLAY) {
			case 'Text':
			case 'Email':
			case 'URL':
			case 'Street 1':
			case 'Street 2':
			case 'City':
			case 'State':
			case 'Country':
			case 'Zipcode':
			case 'Id':
				return this.formsTextService.getText(field, layoutStyle);
			case 'Picklist':
				return this.formsPicklistService.getPicklist(field, layoutStyle);
			case 'Phone':
				return this.formsPhoneService.getPhone(field, layoutStyle);
			case 'Relationship':
				return this.formsRelationshipService.getRelationship(
					field,
					layoutStyle
				);
				break;
			case 'List Text':
				return this.formsListTextService.getListText(field, layoutStyle);
			case 'Number':
				return this.formsNumberService.getNumber(field, layoutStyle);
			case 'Checkbox':
				return this.formsCheckboxService.getCheckbox(field);
			case 'Text Area':
			case 'Text Area Rich':
			case 'Text Area Long':
				return this.formsTextAreaService.getTextArea(field, layoutStyle);
			case 'Discussion':
				return this.formsDiscussionService.getDiscussion(field, layoutStyle);
			case 'Date/Time':
				return this.formsDateTimeService.getDateTime(field, layoutStyle);
			case 'Time':
				return this.formsTimeService.getTime(field, layoutStyle);
			case 'Date':
				return this.formsDateService.getDate(field, layoutStyle);
			default:
				return `<mat-form-field  [style.visibility]="${field.VISIBILITY}  ? 'hidden' : 'visible' " appearance="${layoutStyle}" fxFlex floatLabel="always" >
				<mat-label>${field.DISPLAY_LABEL}</mat-label>
				  <input matInput type="text"
					  [(ngModel)]="context.entry.${field.NAME}"
					  [required]="context.fieldsMap['${field.FIELD_ID}'].REQUIRED"
					  [disabled]="!context.formEnabled">
				  <mat-error>${field.DISPLAY_LABEL} {{'IS_REQUIRED' | translate}}</mat-error>
				  <mat-hint >${field.HELP_TEXT}</mat-hint>
				</mat-form-field>`;
		}
	}
}
