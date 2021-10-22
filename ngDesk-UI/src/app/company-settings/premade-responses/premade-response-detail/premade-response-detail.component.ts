import { Component, OnInit } from '@angular/core';
import {
	FormBuilder,
	FormControl,
	FormGroup,
	Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import tinymce from 'tinymce/tinymce';
import { BannerMessageService } from '../../../custom-components/banner-message/banner-message.service';
import { LoaderService } from '../../../custom-components/loader/loader.service';
import { ModulesService } from '../../../modules/modules.service';
import { config } from '../../../tiny-mce/tiny-mce-config';

@Component({
	selector: 'app-premade-response-detail',
	templateUrl: './premade-response-detail.component.html',
	styleUrls: ['./premade-response-detail.component.scss'],
})
export class PremadeResponseDetailComponent implements OnInit {
	public modules: any;
	public fields: any;
	public relatedFields = {};
	public message = '';
	public errorParams = {
		name: {},
		body: {},
		teams: {},
		module: {},
	};
	public config = config;
	public dicussionExists = false;
	public responseForm: FormGroup;
	public teams: any[];
	private allModules: any = [];
	private moduleId: string;
	public module: any = {};

	constructor(
		private formBuilder: FormBuilder,
		private modulesService: ModulesService,
		private translateService: TranslateService,
		private bannerMessageService: BannerMessageService,
		private route: ActivatedRoute,
		private router: Router,
		private loaderService: LoaderService
	) {
		this.translateService.get('NAME').subscribe((res: string) => {
			this.errorParams['name']['field'] = res;
		});

		this.translateService.get('TEAMS').subscribe((res: string) => {
			this.errorParams['teams']['field'] = res;
		});

		this.translateService.get('MESSAGE').subscribe((res: string) => {
			this.errorParams['body']['field'] = res;
		});

		this.translateService.get('MODULE').subscribe((res: string) => {
			this.errorParams['module']['field'] = res;
		});
		this.config['height'] = '100%';
	}

	public ngOnInit() {
		this.responseForm = this.formBuilder.group({
			NAME: ['', Validators.required],
			DESCRIPTION: '',
			MODULE: '',
			MESSAGE: ['', Validators.required],
			TEAMS: ['', Validators.required],
		});

		const premadeResponseId = this.route.snapshot.params['responseId'];
		this.modulesService.getModuleByName('Teams').subscribe(
			(response: any) => {
				this.modulesService.getEntries(response.MODULE_ID).subscribe(
					(entriesResponse: any) => {
						this.teams = entriesResponse.DATA;
						if (premadeResponseId !== 'new') {
							this.responseForm.addControl(
								'PREMADE_RESPONSE_ID',
								new FormControl('')
							);
							this.modulesService
								.getPremadeResponse(premadeResponseId, null, null)
								.subscribe(
									(getResponseSuccess: any) => {
										this.responseForm.setValue({
											NAME: getResponseSuccess.NAME,
											DESCRIPTION: getResponseSuccess.DESCRIPTION,
											MODULE: getResponseSuccess.MODULE,
											MESSAGE: getResponseSuccess.MESSAGE,
											TEAMS: getResponseSuccess.TEAMS,
											PREMADE_RESPONSE_ID:
												getResponseSuccess.PREMADE_RESPONSE_ID,
										});
										this.modulesService
											.getFields(getResponseSuccess.MODULE)
											.subscribe(
												(fieldsReponse: any) => {
													this.fields = fieldsReponse.FIELDS;
													this.setRelatedFields(fieldsReponse.FIELDS);
												},
												(error: any) => {
													console.log(error);
												}
											);
									},
									(getResponseError: any) => {
										this.bannerMessageService.errorNotifications.push({
											message: getResponseError.error.ERROR,
										});
									}
								);
						}
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);

		this.modulesService.getModules().subscribe(
			(modulesResponse: any) => {
				this.modules = modulesResponse.MODULES.sort((a, b) =>
					a.NAME.localeCompare(b.NAME)
				);
				this.modules = this.modules.filter((module) =>
					module.FIELDS.some(
						(field) => field.DATA_TYPE.DISPLAY === 'Discussion'
					)
				);
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	public moduleSelected(moduleId) {
		this.modulesService.getFields(moduleId).subscribe(
			(fieldsReponse: any) => {
				this.fields = fieldsReponse.FIELDS;
				this.setRelatedFields(fieldsReponse.FIELDS);
				tinymce.activeEditor.setContent('');
				this.responseForm.get('MESSAGE').setValue('');
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	private setRelatedFields(fields) {
		const relationshipFields = fields.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY === 'Relationship' &&
				field.RELATIONSHIP_TYPE !== 'Many to Many' &&
				field.NAME !== 'LAST_UPDATED_BY' &&
				field.NAME !== 'CREATED_BY'
		);
		for (const relationshipField of relationshipFields) {
			if (!this.relatedFields.hasOwnProperty(relationshipField.MODULE)) {
				this.modulesService.getFields(relationshipField.MODULE).subscribe(
					(relatedFieldsResponse: any) => {
						this.relatedFields[relationshipField.MODULE] =
							relatedFieldsResponse.FIELDS;
						this.setRelatedFields(relatedFieldsResponse.FIELDS);
					},
					(relatedFieldsError: any) => {
						console.log(relatedFieldsError);
					}
				);
			}
		}
	}

	public isDefaultField(field): boolean {
		const defaultFields = [
			'ASSIGNEE',
			'REQUESTOR',
			'CREATED_BY',
			'LAST_UPDATED_BY',
			'ACCOUNT',
		];
		if (defaultFields.indexOf(field.NAME) !== -1) {
			return true;
		}
		return false;
	}

	public concatenateVariables(field, mainItem?, subItem?, subSubItem?) {
		let concatVariable = mainItem.NAME;
		if (subItem) {
			concatVariable += `.${subItem.NAME}`;
			if (subSubItem) {
				concatVariable += `.${subSubItem.NAME}`;
			}
		}
		this.insertBodyVariable(field.NAME, concatVariable);
	}

	public insertBodyVariable(fieldName, relatedVars?) {
		const bodyEnd = tinymce.activeEditor.getContent().split('</body>');
		let fieldVar = fieldName;
		if (relatedVars) {
			fieldVar += `.${relatedVars}`;
		}
		const valueToCompare =
			fieldVar.indexOf('.') !== -1
				? fieldVar.split('.')[fieldVar.split('.').length - 1]
				: fieldVar;
		switch (valueToCompare) {
			case 'REQUESTOR': {
				const newBody = `${bodyEnd[0]} {{inputMessage.${fieldVar}.EMAIL_ADDRESS}}</body>`;
				tinymce.activeEditor.setContent(newBody);
				this.responseForm.get('MESSAGE').setValue(newBody);
				break;
			}
			case 'ASSIGNEE': {
				const newBody = `${bodyEnd[0]} {{inputMessage.${fieldVar}.EMAIL_ADDRESS}}</body>`;
				tinymce.activeEditor.setContent(newBody);
				this.responseForm.get('MESSAGE').setValue(newBody);
				break;
			}
			case 'CREATED_BY': {
				const newBody = `${bodyEnd[0]} {{inputMessage.${fieldVar}.EMAIL_ADDRESS}}</body>`;
				tinymce.activeEditor.setContent(newBody);
				this.responseForm.get('MESSAGE').setValue(newBody);
				break;
			}
			case 'LAST_UPDATED_BY': {
				const newBody = `${bodyEnd[0]} {{inputMessage.${fieldVar}.EMAIL_ADDRESS}}</body>`;
				tinymce.activeEditor.setContent(newBody);
				this.responseForm.get('MESSAGE').setValue(newBody);
				break;
			}
			case 'ACCOUNT': {
				const newBody = `${bodyEnd[0]} {{inputMessage.${fieldVar}.ACCOUNT_NAME}}</body>`;
				tinymce.activeEditor.setContent(newBody);
				this.responseForm.get('MESSAGE').setValue(newBody);
				break;
			}
			default: {
				const newBody = `${bodyEnd[0].replace(
					/<\/p>$/,
					''
				)} {{inputMessage.${fieldVar}}}</body>`;
				tinymce.activeEditor.setContent(newBody);
				this.responseForm.get('MESSAGE').setValue(newBody);
				break;
			}
		}
	}

	public savePremadeResponse() {
		if (this.responseForm.valid) {
			const premadeResponseId = this.route.snapshot.params['responseId'];
			if (premadeResponseId !== 'new') {
				this.modulesService
					.putPremadeResponse(this.responseForm.value)
					.subscribe(
						(putResponseSuccess: any) => {
							this.router.navigate([`company-settings/premade-responses`]);
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant(
									'PREMADE_RESPONSE_UPDATE_SUCCESS'
								),
							});
						},
						(putResponseError: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: putResponseError.error.ERROR,
							});
						}
					);
			} else {
				this.modulesService
					.postPremadeResponse(this.responseForm.value)
					.subscribe(
						(postResponseSuccess: any) => {
							this.router.navigate([`company-settings/premade-responses`]);
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SETTINGS_SAVE_SUCCESS'),
							});
						},
						(postResponseError: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: postResponseError.error.ERROR,
							});
						}
					);
			}
		} else {
			this.loaderService.isLoading = false;
			if (this.responseForm.value.MESSAGE === '') {
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('MESSAGE_FIELD_EMPTY'),
				});
			} else {
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('FILL_REQUIRED_FIELDS'),
				});
			}
		}
	}
}
