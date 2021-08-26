import { Injectable } from '@angular/core';
import { CustomModulesService } from './../render-detail-new/custom-modules.service';
import { RenderLayoutService } from '@src/app/render-layout/render-layout.service';
import { UsersService } from '@src/app/users/users.service';
import { AppGlobals } from '@src/app/app.globals';
import { AttachmentsService } from '@src/app/attachments/attachments.service';
import { of, Observable, observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { RelationshipService } from './../data-types/relationship.service';
import { PhoneService } from './../data-types/phone.service';
import { CacheService } from './../../cache.service';
import { DataApiService } from '@ngdesk/data-api';
import { HttpClient } from '@angular/common/http';

@Injectable({
	providedIn: 'root',
})
export class RenderDetailDataService {
	public filteredCountriesForPhoneField: any = [];
	public countryCodeForPhoneField: any = [];
	public countryNameForPhoneField: any = [];
	constructor(
		private renderLayoutService: RenderLayoutService,
		private userService: UsersService,
		private globals: AppGlobals,
		private attachmentsService: AttachmentsService,
		private customModulesService: CustomModulesService,
		private relationshipService: RelationshipService,
		private phoneService: PhoneService,
		private cacheService: CacheService,
		private dataService: DataApiService,
		private http: HttpClient
	) {}

	public loadMissingFields(layout, module, entry, layoutType) {
		if (
			layout.PREDEFINED_TEMPLATE &&
			layout.PREDEFINED_TEMPLATE !== null &&
			layoutType !== 'create'
		) {
			// PREDEFINED TEMPLATE
			layout['PREDEFINED_TEMPLATE'].forEach((section) => {
				section['FIELDS'].forEach((sectionField) => {
					const moduleField = module['FIELDS'].find(
						(field) => field.FIELD_ID === sectionField.FIELD_ID
					);
					const fieldName = moduleField.NAME;
					if (!entry[fieldName] || entry[fieldName] === null) {
						entry[fieldName] = this.getValueForField(moduleField);
					} else {
						if (moduleField.DATA_TYPE.DISPLAY === 'Chronometer') {
							this.customModulesService.chronometerValues[fieldName] =
								this.renderLayoutService.chronometerFormatTransform(
									entry[fieldName],
									''
								);
							entry[fieldName] = '';
						}
					}
				});
			});
		} else if (
			layout.PREDEFINED_TEMPLATE &&
			layout.PREDEFINED_TEMPLATE !== null
		) {
			entry = this.getDefaultValuesForCreateLayout(
				module,
				layout,
				entry,
				'PREDEFINED'
			);
		}

		return entry;
	}

	public getValueForField(moduleField) {
		switch (moduleField['DATA_TYPE']['DISPLAY']) {
			case 'Phone':
				this.loadCountriesForPhoneField();
				return {
					COUNTRY_CODE: 'us',
					DIAL_CODE: '+1',
					PHONE_NUMBER: '',
					COUNTRY_FLAG: 'us.svg',
				};
			case 'Discussion':
			case 'List Text':
				return [];
			case 'Relationship':
				switch (moduleField['RELATIONSHIP_TYPE']) {
					case 'Many to Many':
						return [];
					case 'One to Many':
						return [];
				}
		}
	}

	public formatChronometerFieldsOnGet(entry, module) {
		const chronometerFields = module['FIELDS'].filter(
			(field) => field.DATA_TYPE.DISPLAY === 'Chronometer'
		);
		chronometerFields.forEach((field) => {
			if (entry[field.NAME] !== undefined && entry[field.NAME] !== null) {
				this.customModulesService.chronometerValues[field.NAME] =
					this.renderLayoutService.chronometerFormatTransform(
						entry[field.NAME],
						''
					);
				entry[field.NAME] = '';
			}
		});
		return entry;
	}

	public canPublish(attachments) {
		if (
			attachments.length > 0 ||
			this.customModulesService.discussionControls['MESSAGE'].trim().length > 0
		) {
			return true;
		}
		return false;
	}

	public removeInlineBaseImages(attachments) {
		let message = this.customModulesService.discussionControls['MESSAGE'];

		if (message.includes('base64')) {
			const base64Images = message.match(/<\s*img[^>]*(.*?)\s*\s*>/g);

			base64Images.forEach((image) => {
				if (image.includes('base64')) {
					const extensionRegex = /(image.*?)\/(.*)(?=;)/g;
					const extensionMatch = extensionRegex.exec(image);
					let extension = 'png';
					if (extensionMatch !== null) {
						extension = extensionMatch[2];
					}

					const regex = /<img.*?src=\"(.*?)\".*?>/gs;
					const fileMatch = regex.exec(image);
					if (fileMatch !== null) {
						const file = fileMatch[1];
						if (file && file !== null) {
							const base64Regex = /data:image.*?;base64,(.*)/gs;
							const base64 = base64Regex.exec(file);
							if (base64 && base64 !== null) {
								const fileName = 'File-' + this.globals.guid();
								message = message.replace(file, fileName + '.' + extension);
								attachments.push({
									FILE_NAME: fileName + '.' + extension,
									FILE: base64[1],
									FILE_EXTENSION: extension,
								});
							}
						}
					}
				}
			});
		}
		this.customModulesService.discussionControls['MESSAGE'] = message;
		return attachments;
	}

	public postAttachments(attachments): Observable<any> {
		if (!attachments || attachments.length === 0) {
			return of(attachments);
		}

		return this.attachmentsService
			.postAttachments({ ATTACHMENTS: attachments })
			.pipe(
				map((attachmentResponse: any) => attachmentResponse['ATTACHMENTS'])
			);
	}

	public replaceInlineMessageWithUrl(
		attachments,
		entryId,
		moduleId,
		messageId
	) {
		let message = this.customModulesService.discussionControls['MESSAGE'];
		attachments.forEach((attachment) => {
			const url =
				window.location.protocol +
				'//' +
				window.location.host +
				this.globals.baseRestUrl +
				'/attachments?attachment_uuid=' +
				attachment['ATTACHMENT_UUID'] +
				'&message_id=' +
				messageId +
				'&entry_id=' +
				entryId +
				'&module_id=' +
				moduleId;
			message = message.replace(attachment.FILE_NAME, url);
		});
		this.customModulesService.discussionControls['MESSAGE'] = message;
	}

	public buildDiscussionPayload(
		messageId,
		attachments,
		moduleId,
		entryId,
		publish
	) {
		const messagePayload = {
			MESSAGE: this.customModulesService.discussionControls['MESSAGE'],
			ATTACHMENTS: JSON.parse(JSON.stringify(attachments)),
			COMPANY_SUBDOMAIN: this.userService.getSubdomain(),
			SENDER: {
				FIRST_NAME: this.userService.user.FIRST_NAME,
				LAST_NAME: this.userService.user.LAST_NAME,
				USER_UUID: this.userService.user.USER_UUID,
				ROLE: this.userService.user.ROLE,
			},
			MESSAGE_TYPE:
				this.customModulesService.discussionControls['MESSAGE_TYPE'],
		};
		if (!publish) {
			return messagePayload;
		}

		messagePayload['MODULE_ID'] = moduleId;
		messagePayload['ENTRY_ID'] = entryId;
		messagePayload['MESSAGE_ID'] = messageId;
		return messagePayload;
	}

	public formatDiscussion(entry, module, attachments) {
		const messageId = this.globals.guid();
		const discussionField = module['FIELDS'].find(
			(field) => field.DATA_TYPE.DISPLAY === 'Discussion'
		);

		if (discussionField) {
			const discussionFieldName = discussionField['NAME'];
			entry[discussionFieldName] = [];
			if (
				attachments.length > 0 ||
				(this.customModulesService.discussionControls.hasOwnProperty(
					'MESSAGE'
				) &&
					this.customModulesService.discussionControls['MESSAGE'].trim()
						.length > 0)
			) {
				const messagePayload = this.buildDiscussionPayload(
					messageId,
					attachments,
					module['MODULE_ID'],
					entry['DATA_ID'],
					false
				);
				entry[discussionFieldName].push(messagePayload);
				return entry;
			}
			delete entry[discussionFieldName];
		}

		return entry;
	}

	public initializeFormulaFields(module, entry, formulaFields) {
		if (!entry) {
			return entry;
		} else {
			entry = this.customModulesService
				.calculateFormula(module, entry)
				.subscribe((results: any) => {
					if (results) {
						formulaFields.forEach((field) => {
							if (results[field.NAME] && Number(results[field.NAME])) {
								const value = +(Math.round(results[field.NAME] * 100) / 100);
								entry[field.NAME] = this.customModulesService.transformNumbersField(value, field.NUMERIC_FORMAT, field.PREFIX, field.SUFFIX);
							} else if (results[field.NAME] && !Number(results[field.NAME])) {
								const value = results[field.NAME];
								entry[field.NAME] = this.customModulesService.transformNumbersField(value, field.NUMERIC_FORMAT, field.PREFIX, field.SUFFIX);
							}
						});
					}
					return entry;
				});
		}
	}

	public getDefaultValuesForCreateLayout(module, layout, entry, layoutType) {
		if (layoutType === 'PREDEFINED') {
			layout['PREDEFINED_TEMPLATE'].forEach((section) => {
				section['FIELDS'].forEach((sectionField) => {
					const moduleField = module['FIELDS'].find(
						(field) => field.FIELD_ID === sectionField.FIELD_ID
					);
					const fieldName = moduleField.NAME;
					if (!entry[fieldName] || entry[fieldName] === null) {
						this.getValueForFieldOnCreateLayout(moduleField).subscribe(
							(response: any) => {
								entry[moduleField.NAME] = response;
							}
						);
					} else {
						if (moduleField.DATA_TYPE.DISPLAY === 'Chronometer') {
							this.customModulesService.chronometerValues[fieldName] =
								this.renderLayoutService.chronometerFormatTransform(
									entry[fieldName],
									''
								);
							entry[fieldName] = '';
						}
					}
				});
			});
			return entry;
		} else if (layoutType === 'GRID') {
			layout['PANELS'].forEach((panel) => {
				const grid = panel['GRIDS'];
				grid.forEach((row) => {
					row.forEach((pill) => {
						if (pill.IS_EMPTY === false && pill.FIELD_ID) {
							const moduleField = module['FIELDS'].find(
								(field) => field.FIELD_ID === pill.FIELD_ID
							);
							this.getValueForFieldOnCreateLayout(moduleField).subscribe(
								(response: any) => {
									entry[moduleField.NAME] = response;
								}
							);
						}
					});
				});
			});
			return entry;
		}
	}

	public getValueForFieldOnCreateLayout(moduleField) {
		let result;
		if (moduleField) {
			switch (moduleField['DATA_TYPE']['DISPLAY']) {
				case 'Relationship':
					switch (moduleField['RELATIONSHIP_TYPE']) {
						case 'Many to Many':
							result = [];
							if (moduleField.DEFAULT_VALUE) {
								this.getDefaultRelationshipValue(moduleField).subscribe(
									(responseList: any) => {
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
									}
								);
							}
							break;
						case 'Many to One':
							if (moduleField.DEFAULT_VALUE) {
								if (
									moduleField.DEFAULT_VALUE.includes('{{') &&
									moduleField.DEFAULT_VALUE.match(
										new RegExp('{{(.*?)}}')
									)[1] === 'CURRENT_USER'
								) {
									result = this.userService.user.DATA_ID;
								} else if (
									moduleField.DEFAULT_VALUE.includes('{{') &&
									moduleField.DEFAULT_VALUE.match(
										new RegExp('{{(.*?)}}')
									)[1] === 'CURRENT_CONTACT'
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
								} else {
									if (moduleField.DEFAULT_VALUE) {
										return this.getDefaultRelationshipValue(moduleField).pipe(
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
													return realationshipObject;
												});
											})
										);
									}
								}
							}
							break;
						case 'One to Many':
							result = [];
							break;
						default:
							result = '';
					}
					break;
				case 'Checkbox':
					result = moduleField.DEFAULT_VALUE === 'true' ? true : false;
					break;
				case 'Picklist (Multi-Select)':
					result = [];
					if (
						moduleField.DEFAULT_VALUE &&
						moduleField.DEFAULT_VALUE.trim() !== ''
					) {
						result.push(moduleField.DEFAULT_VALUE);
					}
					break;
				case 'Phone':
					this.loadCountriesForPhoneField();
					result = this.phoneService.getDefaultPhoneValue(moduleField);
					break;
				default:
					if (
						moduleField.DEFAULT_VALUE &&
						moduleField.DEFAULT_VALUE.trim() !== ''
					) {
						result = moduleField.DEFAULT_VALUE;
					}
			}
		}
		return of(result);
	}

	public getDefaultRelationshipValue(moduleField) {
		const obersvables: Observable<any>[] = [];
		obersvables.push(this.cacheService.getModule(moduleField.MODULE));
		if (moduleField.DEFAULT_VALUE.search(',') === -1) {
			obersvables.push(
				this.dataService.getModuleEntry(
					moduleField.MODULE,
					moduleField.DEFAULT_VALUE
				)
			);
		} else {
			let defaultValues = [];
			defaultValues = moduleField.DEFAULT_VALUE.split(',');
			defaultValues.forEach((defaultValue) => {
				obersvables.push(
					this.dataService.getModuleEntry(moduleField.MODULE, defaultValue)
				);
			});
		}
		return forkJoin(obersvables);
	}

	public loadCountriesForPhoneField() {
		this.filteredCountriesForPhoneField = this.renderLayoutService.countries;

		this.filteredCountriesForPhoneField.forEach((element) => {
			this.countryCodeForPhoneField[element.COUNTRY_DIAL_CODE] = new Array();

			this.countryNameForPhoneField.push(
				element.COUNTRY_NAME + '|' + element.COUNTRY_DIAL_CODE
			);
			this.countryCodeForPhoneField[element.COUNTRY_DIAL_CODE].push(
				element.COUNTRY_NAME
			);
			this.countryCodeForPhoneField[element.COUNTRY_DIAL_CODE].push(
				element.COUNTRY_CODE
			);
			this.countryCodeForPhoneField[element.COUNTRY_DIAL_CODE].push(
				element.COUNTRY_FLAG
			);
		});
	}

	public searchCountriesForPhoneField(value: string) {
		if (value && value !== null && value !== '') {
			const searchString = value.toLowerCase();
			this.filteredCountriesForPhoneField =
				this.renderLayoutService.countries.filter(
					(country) =>
						country.COUNTRY_NAME.toLowerCase().indexOf(searchString) > -1
				);
		} else {
			this.filteredCountriesForPhoneField = this.renderLayoutService.countries;
		}
	}

	public getFieldPermissionValues(moduleId, layoutType, dataId) {
		let query = '';
		query = `{
			getFieldPermissions(
			  moduleId: "${moduleId}"
			  layout: "${layoutType}"
			  dataId: "${dataId}"
			) {
			  fieldId
			  notEditable
			}
		  }`;

		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
