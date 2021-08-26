import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { RolesService } from '@src/app/company-settings/roles/roles-old.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { ConditionsComponent } from '@src/app/custom-components/conditions/conditions.component';
import { AdditionalFields } from '@src/app/models/additional-field';
import { Condition } from '@src/app/models/condition';
import { Field } from '@src/app/models/field';
import { Role } from '@src/app/models/role';
import { ModulesService } from '@src/app/modules/modules.service';
import { ChannelsService } from 'src/app/channels/channels.service';
import { ListLayoutApiService, ListLayout } from '@ngdesk/module-api';
import { CacheService } from '@src/app/cache.service';

@Component({
	selector: 'app-list-layout',
	templateUrl: './list-layout.component.html',
	styleUrls: ['./list-layout.component.scss'],
})
export class ListLayoutComponent implements OnInit {
	@ViewChild(ConditionsComponent)
	private conditionsComponent: ConditionsComponent;
	public conditions: Condition[] = [];
	public listLayout: ListLayout = {
		NAME: '',
		DESCRIPTION: '',
		ID: '',
		ROLE: '',
		IS_DEFAULT: false,
		CONDITIONS: [],
		ORDER_BY: {
			COLUMN: '',
			ORDER: '',
		},
		COLUMN_SHOW: {
			FIELDS: [],
		},
	};
	public roles: Role[] = [];
	public fields: Field[] = [];
	public shownColumns: any[] = [];
	public availableFields: any[] = [];
	public listLayoutLoaded = false;
	public errorMessage = '';
	public additionalFields = [];
	public additionalCheckboxFields = [];
	public channellist: any;
	public channels: any = [];
	public fieldlist: any;
	public fieldId: any;

	public listLayoutForm: FormGroup;
	public params = {
		orderBy: {},
		order: {},
		role: {},
	};

	constructor(
		private formBuilder: FormBuilder,
		private rolesService: RolesService,
		private bannerMessageService: BannerMessageService,
		public modulesService: ModulesService,
		private listLayoutApiService: ListLayoutApiService,
		private route: ActivatedRoute,
		private router: Router,
		private translateService: TranslateService,
		private channelsService: ChannelsService,
		private cacheService: CacheService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.
		this.translateService.get('ORDER_BY').subscribe((res: string) => {
			this.params['orderBy']['field'] = res;
		});

		this.translateService.get('ORDER').subscribe((res: string) => {
			this.params['order']['field'] = res;
		});

		this.translateService.get('ROLE').subscribe((res: string) => {
			this.params['role']['field'] = res;
		});
	}

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

		const moduleId = this.route.snapshot.params['moduleId'];
		const listLayoutId = this.route.snapshot.params['listLayoutId'];

		this.listLayoutForm = this.formBuilder.group({
			CONDITIONS: this.formBuilder.array([]),
			ROLE: ['', Validators.required],
			IS_DEFAULT: [false],
			ORDER_BY: this.formBuilder.group({
				COLUMN: ['', Validators.required],
				ORDER: ['', Validators.required],
			}),
		});

		// get list of roles for roles dropdown
		this.rolesService.getRoles().subscribe(
			(rolesResponse: any) => {
				rolesResponse['ROLES'].filter((role) => {
					if (role.NAME === 'Customers') {
						role['NAME'] = 'Customer';
					}
				});
				this.roles = rolesResponse.ROLES.filter(
					(role) =>
						role.NAME !== 'Public' &&
						role.NAME !== 'ExternalProbe' &&
						role.NAME !== 'LimitedUser'
				);
				this.roles = this.roles.sort((a, b) => a.NAME.localeCompare(b.NAME));
				this.additionalFields.push(
					new AdditionalFields(
						'ROLE',
						'ROLE',
						'list',
						'ROLE_ID',
						'ROLE',
						this.roles,
						'NAME',
						'role'
					)
				);
				this.additionalCheckboxFields.push(
					new AdditionalFields(
						'DEFAULT_LAYOUT',
						'IS_DEFAULT',
						'checkbox',
						null,
						'IS_DEFAULT',
						null,
						'DEFAULT',
						null
					)
				);
				this.modulesService.getModuleById(moduleId).subscribe(
					(moduleResponse: any) => {
						this.fields = moduleResponse.FIELDS;
						// Filtered fields to not show Many to Many or One to Many fields
						moduleResponse.FIELDS = moduleResponse.FIELDS.filter(
							(field) =>
								field.RELATIONSHIP_TYPE !== 'Many to Many' &&
								field.RELATIONSHIP_TYPE !== 'One to Many' &&
								field.DATA_TYPE.DISPLAY !== 'Discussion' &&
								field.DATA_TYPE.DISPLAY !== 'File Upload' &&
								field.DATA_TYPE.DISPLAY !== 'Zoom' &&
								field.DATA_TYPE.DISPLAY !== 'Button' &&
								field.DATA_TYPE.DISPLAY !== 'List Text' &&
								field.DATA_TYPE.DISPLAY !== 'Approval' &&
								field.DATA_TYPE.DISPLAY !== 'Image' &&
								field.DATA_TYPE.DISPLAY !== 'File Preview' &&
								field.DATA_TYPE.DISPLAY !== 'PDF' &&
								field.DATA_TYPE.DISPLAY !== 'Receipt Capture' &&
								field.DATA_TYPE.DISPLAY !== 'Password'
						);

						if (listLayoutId !== 'new') {
							// loops through all list layouts and matches on selected id
							const selectedListLayout = moduleResponse.LIST_LAYOUTS.find(
								(layout) => {
									return layout.LAYOUT_ID === listLayoutId;
								}
							);
							this.setValueToForm(selectedListLayout);
							const listLayout = this.convertListLayout(selectedListLayout);
							// add field objects in each column category based on the field id
							moduleResponse.FIELDS.forEach((field) => {
								if (
									listLayout.COLUMN_SHOW.FIELDS.indexOf(field.FIELD_ID) !== -1
								) {
									listLayout.COLUMN_SHOW.FIELDS[
										listLayout.COLUMN_SHOW.FIELDS.indexOf(field.FIELD_ID)
									] = field;
								} else {
									this.availableFields.push(field);
								}
							});
							this.shownColumns = listLayout.COLUMN_SHOW.FIELDS;
							this.listLayoutLoaded = true;
						} else {
							this.availableFields = moduleResponse.FIELDS;
							this.listLayoutLoaded = true;
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
	}

	// Set values from api response to list layout form
	private setValueToForm(listLayoutObj: any) {
		this.listLayoutForm.controls['NAME'].setValue(listLayoutObj.NAME);
		this.listLayoutForm.controls['DESCRIPTION'].setValue(
			listLayoutObj.DESCRIPTION
		);
		this.listLayoutForm.controls['ROLE'].setValue(listLayoutObj.ROLE);
		this.listLayoutForm.controls['IS_DEFAULT'].setValue(
			listLayoutObj.IS_DEFAULT
		);
		this.listLayoutForm.controls['ORDER_BY']['controls']['COLUMN'].setValue(
			listLayoutObj.ORDER_BY.COLUMN
		);
		this.listLayoutForm.controls['ORDER_BY']['controls']['ORDER'].setValue(
			listLayoutObj.ORDER_BY.ORDER
		);
	}

	// casts api response object as custom list layout data type
	private convertListLayout(listLayoutObj: any) {
		for (const condition of listLayoutObj.CONDITIONS) {
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

		let newListLayout = {
			LAYOUT_ID: listLayoutObj.LAYOUT_ID,
			NAME: listLayoutObj.NAME,
			DESCRIPTION: listLayoutObj.DESCRIPTION,
			ID: listLayoutObj.ID,
			ROLE: listLayoutObj.ROLE,
			IS_DEFAULT: listLayoutObj.IS_DEFAULT,
			ORDER_BY: {
				COLUMN: listLayoutObj.ORDER_BY.COLUMN,
				ORDER: listLayoutObj.ORDER_BY.ORDER,
			},
			COLUMN_SHOW: {
				FIELDS: listLayoutObj.COLUMN_SHOW.FIELDS,
			},
			CONDITIONS: this.conditions,
		};
		return newListLayout;
	}

	public save() {
		if (this.listLayoutForm.valid) {
			// transforms conditions to only store field id
			this.listLayout.CONDITIONS =
				this.conditionsComponent.transformConditions();

			this.listLayout.NAME = this.listLayoutForm.value['NAME'];
			this.listLayout.DESCRIPTION = this.listLayoutForm.value['DESCRIPTION'];
			this.listLayout.ROLE = this.listLayoutForm.value['ROLE'];
			this.listLayout.IS_DEFAULT = this.listLayoutForm.value['IS_DEFAULT'];
			this.listLayout.ORDER_BY = this.listLayoutForm.value['ORDER_BY'];
			// pass over only field id for columns
			this.listLayout.COLUMN_SHOW.FIELDS = this.shownColumns.map(
				(column) => column.FIELD_ID
			);
			const listLayoutId = this.route.snapshot.params['listLayoutId'];
			const moduleId = this.route.snapshot.params['moduleId'];
			if (listLayoutId !== 'new') {
				this.listLayout.LAYOUT_ID = listLayoutId;
				// call put list layouts to update selected list layout
				this.listLayoutApiService
					.putListLayout(moduleId, this.listLayout)
					.subscribe(
						(listLayoutResponse: any) => {
							this.cacheService.updateModule(moduleId);
							this.router.navigate([`modules/${moduleId}/list_layouts`]);
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			} else {
				// call post list layouts to create new list layout
				this.listLayoutApiService
					.postListLayout(moduleId, this.listLayout)
					.subscribe(
						(listLayoutResponse: any) => {
							this.cacheService.updateModule(moduleId);
							this.router.navigate([`modules/${moduleId}/list_layouts`]);
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			}
		}
	}
}
