import { Injectable } from '@angular/core';
import { CacheService } from '@src/app/cache.service';
import { UsersService } from '@src/app/users/users.service';
import { map } from 'rxjs/operators';
import { CustomModulesService } from '../render-detail-new/custom-modules.service';
import { RenderDetailDataService } from '../render-detail-new/render-detail-data.service';
import { RenderLayoutService } from '../render-layout.service';

@Injectable({
	providedIn: 'root',
})
export class RenderFormsService {
	constructor(
		private customModulesService: CustomModulesService,
		private cacheService: CacheService,
		private renderDetailDataService: RenderDetailDataService,
		private userService: UsersService,
		public renderLayoutService: RenderLayoutService
	) {}

	public loadEntryForEachFieldType(field, entry) {
		if (field.DATA_TYPE.DISPLAY === 'Phone') {
			if (!entry[field.NAME]) {
				entry[field.NAME] = {};
				entry[field.NAME]['COUNTRY_CODE'] = 'us';
				entry[field.NAME]['DIAL_CODE'] = '+1';
				entry[field.NAME]['PHONE_NUMBER'] = '';
				entry[field.NAME]['COUNTRY_FLAG'] = 'us.svg';
			}
		}
	}

	public loadDefaultValues(moduleField, entry) {
		let result;
		if (moduleField.DEFAULT_VALUE != null && moduleField.DEFAULT_VALUE !== '') {
			if (moduleField.DATA_TYPE.DISPLAY === 'Phone') {
				const defaultValue = JSON.parse(moduleField.DEFAULT_VALUE);
				entry[moduleField.NAME] = {};
				if (defaultValue.COUNTRY_CODE === '') {
					entry[moduleField.NAME]['COUNTRY_CODE'] = 'us';
				} else {
					entry[moduleField.NAME]['COUNTRY_CODE'] = defaultValue.COUNTRY_CODE;
				}
				if (defaultValue.DIAL_CODE === '') {
					entry[moduleField.NAME]['DIAL_CODE'] = '+1';
				} else {
					entry[moduleField.NAME]['DIAL_CODE'] = defaultValue.DIAL_CODE;
				}
				entry[moduleField.NAME]['PHONE_NUMBER'] = defaultValue.PHONE_NUMBER;
				if (defaultValue.COUNTRY_FLAG === '') {
					entry[moduleField.NAME]['COUNTRY_FLAG'] = 'us.svg';
				} else {
					entry[moduleField.NAME]['COUNTRY_FLAG'] = defaultValue.COUNTRY_FLAG;
				}
			} else if (moduleField.DATA_TYPE.DISPLAY === 'Checkbox') {
				entry[moduleField.NAME] =
					moduleField.DEFAULT_VALUE === 'true' ? true : false;
			} else if (moduleField.DATA_TYPE.DISPLAY === 'Relationship') {
				if (moduleField.RELATIONSHIP_TYPE === 'Many to Many') {
					result = [];
					if (moduleField.DEFAULT_VALUE) {
						this.renderDetailDataService
							.getDefaultRelationshipValue(moduleField)
							.subscribe((responseList: any) => {
								const primaryDisplayField = responseList[0]['FIELDS'].find(
									(field) =>
										field.FIELD_ID === moduleField.PRIMARY_DISPLAY_FIELD
								);
								responseList.splice(0, 1);
								responseList.forEach((response) => {
									const primaryDisplayFieldValue =
										response[primaryDisplayField.NAME];
									const realationshipObject = {
										DATA_ID: response.DATA_ID,
										PRIMARY_DISPLAY_FIELD: primaryDisplayFieldValue,
									};
									result.push(realationshipObject);
								});
							});
					}
					entry[moduleField.NAME] = result;
				} else if (moduleField.RELATIONSHIP_TYPE === 'Many to One') {
					if (moduleField.DEFAULT_VALUE) {
						if (
							moduleField.DEFAULT_VALUE.includes('{{') &&
							moduleField.DEFAULT_VALUE.match(new RegExp('{{(.*?)}}'))[1] ===
								'CURRENT_USER'
						) {
							result = this.userService.user.DATA_ID;
						} else if (
							moduleField.DEFAULT_VALUE.includes('{{') &&
							moduleField.DEFAULT_VALUE.match(new RegExp('{{(.*?)}}'))[1] ===
								'CURRENT_CONTACT'
						) {
							this.cacheService
								.getPrerequisiteForDetaiLayout(
									moduleField.MODULE,
									this.userService.user.CONTACT
								)
								.subscribe((entryData) => {
									const fieldControlName =
										moduleField.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
									this.customModulesService.formControls[
										fieldControlName
									].setValue(entryData[1].entry.FULL_NAME);
								});
							result = this.userService.user.CONTACT;
							entry[moduleField.NAME] = result;
						} else {
							if (moduleField.DEFAULT_VALUE) {
								return this.renderDetailDataService
									.getDefaultRelationshipValue(moduleField)
									.pipe(
										map((responseList) => {
											const primaryDisplayField = responseList[0][
												'FIELDS'
											].find(
												(field) =>
													field.FIELD_ID === moduleField.PRIMARY_DISPLAY_FIELD
											);
											responseList.splice(0, 1);
											responseList.forEach((response) => {
												const primaryDisplayFieldValue =
													response[primaryDisplayField.NAME];
												const realationshipObject = {
													DATA_ID: response.DATA_ID,
													PRIMARY_DISPLAY_FIELD: primaryDisplayFieldValue,
												};
												const fieldControlName =
													moduleField.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
												this.customModulesService.formControls[
													fieldControlName
												].setValue(primaryDisplayFieldValue);
												entry[moduleField.NAME] = realationshipObject;
											});
										})
									);
							}
						}
					}
				}
			} else {
				entry[moduleField.NAME] = moduleField.DEFAULT_VALUE;
			}
		}
	}
}
