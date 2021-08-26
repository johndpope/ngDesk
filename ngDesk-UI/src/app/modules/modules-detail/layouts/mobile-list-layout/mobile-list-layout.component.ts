import {
	CdkDragDrop,
	moveItemInArray,
	transferArrayItem,
} from '@angular/cdk/drag-drop';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { RolesService } from '../../../../company-settings/roles/roles-old.service';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { ConditionsComponent } from '../../../../custom-components/conditions/conditions.component';

import { Condition } from '../../../../models/condition';
import { Field } from '../../../../models/field';
import {
	MobileListLayout,
	OrderBy,
} from '../../../../models/mobile-list-layout';
import { Role } from '../../../../models/role';
import { AdditionalFields } from '../../../../models/additional-field';
import { ModulesService } from '../../../modules.service';
import { ChannelsService } from 'src/app/channels/channels.service';

@Component({
	selector: 'app-mobile-list-layout',
	templateUrl: './mobile-list-layout.component.html',
	styleUrls: ['./mobile-list-layout.component.scss'],
})
export class MobileListLayoutComponent implements OnInit {
	@ViewChild(ConditionsComponent)
	private conditionsComponent: ConditionsComponent;
	public listLayout: MobileListLayout = new MobileListLayout(
		'',
		'',
		'',
		'',
		new OrderBy('', ''),
		[],
		[],
		false
	);
	public roles: Role[] = [];
	public fields: Field[] = [];
	public shownColumns: any[] = [];
	public availableFields: any[] = [];
	public orderByFields: any = [];
	public channellist: any;
	public channels: any = [];
	public fieldlist: any;
	public fieldId: any;
	public listLayoutLoaded = false;
	public errorMessage = '';
	public additionalFields = [];
	public additionalCheckboxFields = [];
	public listLayoutForm: FormGroup;
	public params = {
		orderBy: {},
		order: {},
		role: {},
	};

	constructor(
		private bannerMessageService: BannerMessageService,
		private formBuilder: FormBuilder,
		private rolesService: RolesService,
		public modulesService: ModulesService,
		private route: ActivatedRoute,
		private router: Router,
		private translateService: TranslateService,
		private channelsService: ChannelsService
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
		const listLayoutId = this.route.snapshot.params['listMobileLayoutId'];

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
				this.roles = rolesResponse.ROLES.sort((a, b) =>
					a.NAME.localeCompare(b.NAME)
				);
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
								field.DATA_TYPE.DISPLAY !== 'Approval' &&
								field.DATA_TYPE.DISPLAY !== 'Receipt Capture' &&
								field.DATA_TYPE.DISPLAY !== 'Password'
						);
						this.orderByFields = moduleResponse.FIELDS.filter(
							(field) =>
								field.DATA_TYPE.DISPLAY !== 'Relationship' &&
								field.DATA_TYPE.DISPLAY !== 'Discussion' &&
								field.DATA_TYPE.DISPLAY !== 'File Upload' &&
								field.DATA_TYPE.DISPLAY !== 'File Preview' &&
								field.DATA_TYPE.DISPLAY !== 'Image' &&
								field.DATA_TYPE.DISPLAY !== 'Zoom' &&
								field.DATA_TYPE.DISPLAY !== 'Approval' &&
								field.DATA_TYPE.DISPLAY !== 'Receipt Capture' &&
								field.DATA_TYPE.DISPLAY !== 'Password'
						);
						if (listLayoutId !== 'new') {
							// loops through all list layouts and matches on selected id
							const selectedListLayout =
								moduleResponse.LIST_MOBILE_LAYOUTS.find((layout) => {
									return layout.LAYOUT_ID === listLayoutId;
								});
							this.listLayout = this.convertListLayout(selectedListLayout);
							this.setValueToForm(selectedListLayout);
							// add field objects in each column category based on the field id
							moduleResponse.FIELDS.forEach((field) => {
								if (this.listLayout.fields.indexOf(field.FIELD_ID) !== -1) {
									this.listLayout.fields[
										this.listLayout.fields.indexOf(field.FIELD_ID)
									] = field;
								} else {
									this.availableFields.push(field);
								}
							});
							this.shownColumns = this.listLayout.fields;
							this.listLayoutLoaded = true;
						} else {
							this.availableFields = moduleResponse.FIELDS;
							this.listLayoutLoaded = true;
						}
					},
					(modulesError: any) => {
						return this.bannerMessageService.errorNotifications.push({
							message: modulesError,
						});
					}
				);
			},
			(rolesError: any) => {
				return this.bannerMessageService.errorNotifications.push({
					message: rolesError,
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
	private convertListLayout(listLayoutObj: any): MobileListLayout {
		const conditions: Condition[] = [];
		for (const condition of listLayoutObj.CONDITIONS) {
			if (condition.CONDITION === this.fieldId) {
				this.channellist.forEach((element) => {
					if (element.ID === condition.CONDITION_VALUE) {
						conditions.push(
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
				conditions.push(
					new Condition(
						condition.CONDITION,
						condition.CONDITION_VALUE,
						condition.OPERATOR,
						condition.REQUIREMENT_TYPE
					)
				);
			}
		}
		const newListLayout = new MobileListLayout(
			listLayoutObj.NAME,
			listLayoutObj.DESCRIPTION,
			listLayoutObj.ID,
			listLayoutObj.ROLE,
			new OrderBy(listLayoutObj.ORDER_BY.COLUMN, listLayoutObj.ORDER_BY.ORDER),
			listLayoutObj.FIELDS,
			conditions,
			listLayoutObj.IS_DEFAULT,
			listLayoutObj.LAYOUT_ID,
			listLayoutObj.DATE_CREATED,
			listLayoutObj.DATE_UPDATED,
			listLayoutObj.LAST_UPDATED_BY,
			listLayoutObj.CREATED_BY
		);
		return newListLayout;
	}

	// Used for drag and drop of columns in each category
	public drop(event: CdkDragDrop<any[]>) {
		if (event.previousContainer === event.container) {
			moveItemInArray(
				event.container.data,
				event.previousIndex,
				event.currentIndex
			);
		} else {
			if (event.container.data.length < 2 && event.container.id === 'shown') {
				transferArrayItem(
					event.previousContainer.data,
					event.container.data,
					event.previousIndex,
					event.currentIndex
				);
			} else if (event.container.id === 'available') {
				transferArrayItem(
					event.previousContainer.data,
					event.container.data,
					event.previousIndex,
					event.currentIndex
				);
			} else {
				return this.bannerMessageService.errorNotifications.push({
					message: 'Please select only two fields that you want to display',
				});
			}
		}
	}

	public save() {
		if (this.listLayoutForm.valid) {
			// transforms conditions to only store field id
			this.listLayout.conditions =
				this.conditionsComponent.transformConditions();

			this.listLayout.name = this.listLayoutForm.value['NAME'];
			this.listLayout.description = this.listLayoutForm.value['DESCRIPTION'];
			this.listLayout.role = this.listLayoutForm.value['ROLE'];
			this.listLayout.isDefault = this.listLayoutForm.value['IS_DEFAULT'];
			this.listLayout.orderBy = this.listLayoutForm.value['ORDER_BY'];
			// pass over only field id for columns
			this.listLayout.fields = this.shownColumns.map(
				(column) => column.FIELD_ID
			);
			const listLayoutId = this.route.snapshot.params['listMobileLayoutId'];
			const moduleId = this.route.snapshot.params['moduleId'];
			if (this.listLayout.fields.length < 2) {
				return this.bannerMessageService.errorNotifications.push({
					message: 'Select at least two fields that you want to be shown',
				});
			}
			if (listLayoutId !== 'new') {
				// call put list layouts to update selected list layout
				this.modulesService
					.putMobileListLayouts(moduleId, this.listLayout)
					.subscribe(
						(listLayoutResponse: any) => {
							this.router.navigate([`modules/${moduleId}/list_mobile_layouts`]);
						},
						(listLayoutError: any) => {
							return this.bannerMessageService.errorNotifications.push({
								message: listLayoutError.error.ERROR,
							});
						}
					);
			} else {
				// call post list layouts to create new list layout
				this.modulesService
					.postMobileListLayouts(moduleId, this.listLayout)
					.subscribe(
						(listLayoutResponse: any) => {
							this.router.navigate([`modules/${moduleId}/list_mobile_layouts`]);
						},
						(listLayoutError: any) => {
							return this.bannerMessageService.errorNotifications.push({
								message: listLayoutError.error.ERROR,
							});
						}
					);
			}
		}
	}
}
