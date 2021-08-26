import { Component, OnInit, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { ChannelsService } from 'src/app/channels/channels.service';
import { LoaderService } from 'src/app/custom-components/loader/loader.service';
import { RolesService } from 'src/app/roles/roles.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { ConditionsComponent } from '../../../../custom-components/conditions/conditions.component';
import { Condition } from '../../../../models/condition';
import { Field } from '../../../../models/field';
import { AdditionalFields } from '../../../../models/additional-field';
import { ModulesService } from '../../../modules.service';

@Component({
	selector: 'app-validations-detail',
	templateUrl: './validations-detail.component.html',
	styleUrls: ['./validations-detail.component.scss'],
})
export class ValidationsDetailComponent implements OnInit {
	@ViewChild(ConditionsComponent)
	public conditionsComponent: ConditionsComponent;
	public validationForm: FormGroup;
	private moduleId: string;
	private validationId: string;
	public validationLoaded = false;
	public fields: Field[] = [];
	public errorMessage: string;
	public additionalFields = [];
	public successMessage: string;
	public conditions: Condition[] = [];
	public roles: any;
	public navigations = [];
	public channellist: any;
	public channels: any = [];
	public fieldlist: any;
	public fieldId: any;
	public showSideNav = true;
	public conditionMessage = '';
	public params: any;
	public triggerTypes = [
		{ DISPLAY: this.translateService.instant('CREATE'), BACKEND: 'CREATE' },
		{ DISPLAY: this.translateService.instant('UPDATE'), BACKEND: 'UPDATE' },
		{
			DISPLAY: this.translateService.instant('CREATE_OR_UPDATE'),
			BACKEND: 'CREATE_OR_UPDATE',
		},
	];

	constructor(
		private formBuilder: FormBuilder,
		public modulesService: ModulesService,
		private route: ActivatedRoute,
		private translateService: TranslateService,
		private rolesService: RolesService,
		private bannerMessageService: BannerMessageService,
		private router: Router,
		private loaderService: LoaderService,
		public channelsService: ChannelsService
	) {}

	public ngOnInit() {
		this.modulesService
			.getFields(this.route.snapshot.params['moduleId'])
			.subscribe((response: any) => {
				this.fieldlist = response.FIELDS;
				this.fieldlist.forEach((element) => {
					if (element.NAME === 'CHANNEL') {
						this.fieldId = element.FIELD_ID;
					}
				});
			});

		this.channelsService
			.getAllChannels(this.route.snapshot.params['moduleId'])
			.subscribe(
				(response: any) => {
					this.channellist = response.CHANNELS;
					response.CHANNELS.forEach((element) => {
						this.channels.push({
							value: element.NAME,
							viewValue: element.NAME,
						});
					});
				},
				(error) => {
					console.log(error);
				}
			);

		this.params = {
			name: { field: this.translateService.instant('NAME') },
			triggerType: { field: this.translateService.instant('TRIGGER_TYPE') },
			roles: { field: this.translateService.instant('ROLES') },
		};

		this.moduleId = this.route.snapshot.params['moduleId'];
		this.validationId = this.route.snapshot.params['validationId'];
		this.validationForm = this.formBuilder.group({
			CONDITIONS: this.formBuilder.array([]),
		});
		this.rolesService.getRoles().subscribe(
			(roleResponse: any) => {
				roleResponse['ROLES'].filter(
					(role) => {
						if (role.NAME === 'Customers')
						{
						  role['NAME'] = 'Customer'; 
						} 
					});
					this.roles = roleResponse.ROLES.sort((a, b) =>
						a.NAME.localeCompare(b.NAME)
			  		);
				this.additionalFields.push(
					new AdditionalFields(
						'TRIGGER_TYPE',
						'TYPE',
						'list',
						'BACKEND',
						'TYPE',
						this.triggerTypes,
						'DISPLAY',
						'triggerType'
					),
					new AdditionalFields(
						'ROLE',
						'ROLE',
						'multipleList',
						'ROLE_ID',
						null,
						this.roles,
						'NAME',
						'role'
					)
				);
				if (this.validationId !== 'new') {
					this.modulesService
						.getModuleValidation(this.moduleId, this.validationId)
						.subscribe(
							(response: any) => {
								this.validationForm.controls.NAME.setValue(response.NAME);
								this.validationForm.controls.DESCRIPTION.setValue(
									response.DESCRIPTION
								);
								this.validationForm.controls.TYPE.setValue(response.TYPE);
								this.validationForm.controls.ROLE.setValue(
									this.roles
										.map((role) => role.ROLE_ID)
										.filter((type) => response.ROLES.indexOf(type) !== -1)
								);
								// this.validationForm.setControl('ROLES', this.formBuilder.array(this.roles.filter(type => type.ROLE_ID === response.ROLE_ID)
								//   || []));

								for (const condition of response.VALIDATIONS) {
									if (condition.CONDITION === this.fieldId) {
										this.channellist.forEach((element) => {
											if (element.ID === condition.CONDITION_VALUE) {
												this.conditions.push(
													new Condition(
														condition.CONDITION,
														element.NAME,
														condition.OPERATOR,
														condition.REQUIREMENT_TYPE
													)
												);
											}
										});
									} else {
										this.conditions.push(
											new Condition(
												condition.CONDITION,
												condition.CONDITION_VALUE,
												condition.OPERATOR,
												condition.REQUIREMENT_TYPE
											)
										);
									}
								}
								if (this.conditions.length > 0) {
									this.validationLoaded = true;
								}
							},
							(error: any) => {
								this.bannerMessageService.errorNotifications.push({
									message: error.error.ERROR,
								});
							}
						);
				} else {
					this.validationLoaded = true;
				}
				this.onChanges();			
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public onChanges(): void {
		this.validationForm.valueChanges.subscribe((value) => {
			this.conditionMessage = '<strong>If </strong>( ';
			const allArray = value.CONDITIONS.filter(
				(a) => a.REQUIREMENT_TYPE === 'All'
			);
			const anyArray = value.CONDITIONS.filter(
				(a) => a.REQUIREMENT_TYPE === 'Any'
			);

			if (allArray.length > 0) {
				for (let i = 0; i < allArray.length; i++) {
					if (i > 0) {
						this.conditionMessage += '<br><strong> AND </strong>';
					}
					this.conditionMessage +=
						(allArray[i].CONDITION.DISPLAY_LABEL
							? allArray[i].CONDITION.DISPLAY_LABEL
							: '') +
						' ' +
						(allArray[i].OPERATOR.DISPLAY ? allArray[i].OPERATOR.DISPLAY : '') +
						' ';

					if (Object.keys(allArray[i].CONDITION).includes('DATA_TYPE')) {
						if (allArray[i].CONDITION.DATA_TYPE.DISPLAY !== 'Relationship') {
							let conditionValue: any;
							if (allArray[i].CONDITION_VALUE._isAMomentObject) {
								conditionValue = this.convertDate(
									allArray[i].CONDITION_VALUE.toDate()
								);
							} else {
								conditionValue = allArray[i].CONDITION_VALUE;
							}
							this.conditionMessage += conditionValue ? conditionValue : '';
						} else {
							let conditionValue: any;
							if (
								typeof allArray[i].CONDITION_VALUE !== 'string' &&
								allArray[i].CONDITION_VALUE[
									allArray[i].CONDITION.RELATION_FIELD_NAME
								]._isAMomentObject
							) {
								conditionValue = this.convertDate(
									allArray[i].CONDITION_VALUE[
										allArray[i].CONDITION.RELATION_FIELD_NAME
									].toDate()
								);
							} else {
								conditionValue =
									allArray[i].CONDITION_VALUE[
										allArray[i].CONDITION.RELATION_FIELD_NAME
									];
							}
							this.conditionMessage += conditionValue ? conditionValue : '';
						}
					}

					if (i === allArray.length - 1) {
						this.conditionMessage += ' ) ';
					}
				}
			}

			if (allArray.length > 0 && anyArray.length > 0) {
				this.conditionMessage += '<br><strong> AND </strong><br>(';
			}

			if (anyArray.length > 0) {
				for (let i = 0; i < anyArray.length; i++) {
					if (i > 0) {
						this.conditionMessage += '<br><strong> OR </strong>';
					}
					this.conditionMessage +=
						(anyArray[i].CONDITION.DISPLAY_LABEL
							? anyArray[i].CONDITION.DISPLAY_LABEL
							: '') +
						' ' +
						(anyArray[i].OPERATOR.DISPLAY ? anyArray[i].OPERATOR.DISPLAY : '') +
						' ';

					if (Object.keys(anyArray[i].CONDITION).includes('DATA_TYPE')) {
						if (anyArray[i].CONDITION.DATA_TYPE.DISPLAY !== 'Relationship') {
							let conditionValue: any;
							if (anyArray[i].CONDITION_VALUE._isAMomentObject) {
								conditionValue = this.convertDate(
									anyArray[i].CONDITION_VALUE.toDate()
								);
							} else {
								conditionValue = anyArray[i].CONDITION_VALUE;
							}
							this.conditionMessage += conditionValue ? conditionValue : '';
						} else {
							let conditionValue: any;
							if (
								typeof anyArray[i].CONDITION_VALUE !== 'string' &&
								anyArray[i].CONDITION_VALUE[
									anyArray[i].CONDITION.RELATION_FIELD_NAME
								]._isAMomentObject
							) {
								conditionValue = this.convertDate(
									anyArray[i].CONDITION_VALUE[
										anyArray[i].CONDITION.RELATION_FIELD_NAME
									].toDate()
								);
							} else {
								conditionValue =
									anyArray[i].CONDITION_VALUE[
										anyArray[i].CONDITION.RELATION_FIELD_NAME
									];
							}
							this.conditionMessage += conditionValue ? conditionValue : '';
						}
					}

					if (i === anyArray.length - 1) {
						this.conditionMessage += ' ) ';
					}
				}
			}
		});
	}

	private convertDate(date: Date): string {
		return (
			date.getMonth() +
			1 +
			'/' +
			date.getDate() +
			'/' +
			date.getFullYear() +
			' ' +
			date.getHours() +
			':' +
			date.getMinutes()
		);
	}

	public save() {
		if (this.validationForm.valid) {
			const VALIDATION = {
				TYPE: this.validationForm.value.TYPE,
				NAME: this.validationForm.value.NAME,
				DESCRIPTION: this.validationForm.value.DESCRIPTION,
				ROLES: this.validationForm.value.ROLE,
				VALIDATIONS: this.conditionsComponent.transformConditions(),
			};
			if (this.validationId === 'new') {
				this.modulesService
					.postModuleValidations(this.moduleId, VALIDATION)
					.subscribe(
						(response: any) => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SAVED_SUCCESSFULLY'),
							});
							this.router.navigate([`modules/${this.moduleId}/validations`]);
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			} else {
				VALIDATION['VALIDATION_ID'] = this.validationId;
				this.modulesService
					.putModuleValidations(this.moduleId, VALIDATION)
					.subscribe(
						(response: any) => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SAVED_SUCCESSFULLY'),
							});
							this.router.navigate([`modules/${this.moduleId}/validations`]);
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			}
		} else {
			this.loaderService.isLoading = false;
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('ENTER_THE_REQUIRED_FIELDS'),
			});
		}
	}
}
