import {
	CdkDragDrop,
	moveItemInArray,
	transferArrayItem,
} from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { RolesService } from '../../../../company-settings/roles/roles-old.service';
import { Field } from '../../../../models/field';
import { MobileDetailLayout } from '../../../../models/mobile-list-layout';
import { Role } from '../../../../models/role';
import { AdditionalFields } from '../../../../models/additional-field';
import { ModulesService } from '../../../modules.service';

@Component({
	selector: 'app-mobile-detail-layout',
	templateUrl: './mobile-detail-layout.component.html',
	styleUrls: ['./mobile-detail-layout.component.scss'],
})
export class MobileDetailLayoutComponent implements OnInit {
	public listLayout: MobileDetailLayout = new MobileDetailLayout(
		'',
		'',
		'',
		'',
		[]
	);

	public layoutType = '';
	public roles: Role[] = [];
	public fields: Field[] = [];
	public shownColumns: any[] = [];
	public availableFields: any[] = [];
	public additionalFields = [];
	public listLayoutLoaded = false;
	public errorMessage = '';
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
		private translateService: TranslateService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('ROLE').subscribe((res: string) => {
			this.params['role']['field'] = res;
		});
	}

	public ngOnInit() {
		const moduleId = this.route.snapshot.params['moduleId'];
		const listLayoutId = this.route.snapshot.params['mobileLayoutId'];
		this.layoutType = this.route.snapshot.url[1].path;
		this.listLayoutForm = this.formBuilder.group({
			ROLE: ['', Validators.required],
		});

		// get list of roles for roles

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
				this.modulesService.getModuleById(moduleId).subscribe(
					(moduleResponse: any) => {
						this.fields = moduleResponse.FIELDS;
						moduleResponse.FIELDS = moduleResponse.FIELDS.filter(
							(field) =>
								field.DATA_TYPE.DISPLAY !== 'Approval' &&
								field.DATA_TYPE.DISPLAY !== 'Password'
						);

						if (listLayoutId !== 'new') {
							// loops through all list layouts and matches on selected id
							const layoutName = this.route.snapshot.url[1].path.toUpperCase();
							if (layoutName === 'CREATE_MOBILE_LAYOUTS') {
								const selectedListLayout =
									moduleResponse.CREATE_MOBILE_LAYOUTS.find((layout) => {
										return layout.LAYOUT_ID === listLayoutId;
									});

								this.listLayout = this.convertListLayout(selectedListLayout);
								this.setValueToForm(selectedListLayout);
							} else if (layoutName === 'EDIT_MOBILE_LAYOUTS') {
								const selectedListLayout =
									moduleResponse.EDIT_MOBILE_LAYOUTS.find((layout) => {
										return layout.LAYOUT_ID === listLayoutId;
									});

								this.listLayout = this.convertListLayout(selectedListLayout);
								this.setValueToForm(selectedListLayout);
							} else if (layoutName === 'DETAIL_MOBILE_LAYOUTS') {
								const selectedListLayout =
									moduleResponse.DETAIL_MOBILE_LAYOUTS.find((layout) => {
										return layout.LAYOUT_ID === listLayoutId;
									});

								this.listLayout = this.convertListLayout(selectedListLayout);
								this.setValueToForm(selectedListLayout);
							}
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
							message: modulesError.error.ERROR,
						});
					}
				);
			},
			(rolesError: any) => {
				return this.bannerMessageService.errorNotifications.push({
					message: rolesError.error.ERROR,
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
	}

	// casts api response object as custom list layout data type
	private convertListLayout(listLayoutObj: any): MobileDetailLayout {
		const newListLayout = new MobileDetailLayout(
			listLayoutObj.NAME,
			listLayoutObj.DESCRIPTION,
			listLayoutObj.ID,
			listLayoutObj.ROLE,
			listLayoutObj.FIELDS,
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
			transferArrayItem(
				event.previousContainer.data,
				event.container.data,
				event.previousIndex,
				event.currentIndex
			);
		}
	}

	public save() {
		if (this.listLayoutForm.valid) {
			// transforms conditions to only store field id

			this.listLayout.name = this.listLayoutForm.value['NAME'];
			this.listLayout.description = this.listLayoutForm.value['DESCRIPTION'];
			this.listLayout.role = this.listLayoutForm.value['ROLE'];

			// pass over only field id for columns
			this.listLayout.fields = this.shownColumns.map(
				(column) => column.FIELD_ID
			);
			const listLayoutId = this.route.snapshot.params['mobileLayoutId'];
			const moduleId = this.route.snapshot.params['moduleId'];
			if (this.listLayout.fields.length < 1) {
				return this.bannerMessageService.errorNotifications.push({
					message: 'Select at least one field that you want to be shown',
				});
			}
			if (listLayoutId !== 'new') {
				// call put list layouts to update selected list layout
				const layoutName = this.route.snapshot.url[1].path;
				this.modulesService
					.putMobileLayout(moduleId, this.listLayout, layoutName)
					.subscribe(
						(listLayoutResponse: any) => {
							this.router.navigate([
								`modules/${moduleId}/${this.route.snapshot.url[1].path}`,
							]);
						},
						(listLayoutError: any) => {
							return this.bannerMessageService.errorNotifications.push({
								message: listLayoutError.error.ERROR,
							});
						}
					);
			} else {
				const layoutName = this.route.snapshot.url[1].path;
				this.modulesService
					.postMobileLayout(moduleId, this.listLayout, layoutName)
					.subscribe(
						(listLayoutResponse: any) => {
							this.router.navigate([
								`modules/${moduleId}/${this.route.snapshot.url[1].path}`,
							]);
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
