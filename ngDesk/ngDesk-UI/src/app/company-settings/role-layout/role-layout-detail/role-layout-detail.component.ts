import {
	ChangeDetectorRef,
	Component,
	OnDestroy,
	OnInit,
	ViewChild,
} from '@angular/core';
import {
	FormArray,
	FormBuilder,
	FormControl,
	FormGroup,
	FormGroupDirective,
	Validators,
} from '@angular/forms';
import { MatTabGroup } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { RoleLayoutApiService } from '@ngdesk/role-api';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { RolesService } from '@src/app/company-settings/roles/roles-old.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { ConditionsComponent } from '@src/app/custom-components/conditions/conditions.component';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { AdditionalFields } from '@src/app/models/additional-field';
import { Condition } from '@src/app/models/condition';
import { Role } from '@src/app/models/role';
import { ColumnShow, Layout, OrderBy } from '@src/app/models/role-layout';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
import { ModulesService } from '@src/app/modules/modules.service';
import { Subscription } from 'rxjs';
import { Console } from 'console';

@Component({
	selector: 'app-role-layout-detail',
	templateUrl: './role-layout-detail.component.html',
	styleUrls: ['./role-layout-detail.component.scss'],
})
export class RoleLayoutDetailComponent implements OnInit, OnDestroy {
	@ViewChild(ConditionsComponent)
	private conditionsComponent: ConditionsComponent;
	@ViewChild('moduleTabsGroup', { static: false })
	private tabGroup: MatTabGroup;
	public roleLayout = {
		role: '',
		name: '',
		description: '',
		defaultLayout: false,
		tabs: [],
	};
	public roles: Role[] = [];
	public roleLayoutId: string;
	public modules = [];
	public modulesAvaialable = [];
	public fields: any[] = [];
	public shownColumns: any = {};
	public availableFields: any = {};
	public roleLayoutLoaded = false;
	public additionalFields = [];
	public additionalCheckboxFields = [];
	public tabs = [];
	public selectedTabIndex = 0;
	private companyInfoSubscription: Subscription;
	public roleLayoutForm: FormGroup;
	public params = {
		module: {},
		orderBy: {},
		order: {},
	};
	public conditionsMap = {};
	public roleLayoutModuleCount = 0;
	public selected = new FormControl(0);

	constructor(
		private formBuilder: FormBuilder,
		private rolesService: RolesService,
		private bannerMessageService: BannerMessageService,
		private route: ActivatedRoute,
		private roleLayoutApiService: RoleLayoutApiService,
		private router: Router,
		private translateService: TranslateService,
		private cacheService: CacheService,
		private loaderService: LoaderService,
		private cdr: ChangeDetectorRef,
		private http: HttpClient,
		private globals: AppGlobals,
		private modulesService: ModulesService
	) {
		this.translateService.get('MODULE').subscribe((res: string) => {
			this.params['module']['field'] = res;
		});

		this.translateService.get('ORDER_BY').subscribe((res: string) => {
			this.params['orderBy']['field'] = res;
		});

		this.translateService.get('ORDER').subscribe((res: string) => {
			this.params['order']['field'] = res;
		});
	}

	public ngOnInit() {
		this.roleLayoutForm = this.formBuilder.group({
			NAME: ['', Validators.required],
			ROLE: ['', Validators.required],
			DESCRIPTION: [''],
			IS_DEFAULT: [false],
			TABS: this.formBuilder.array([]),
		});
		this.modulesService.getModules().subscribe((moduleResponse: any) => {
			this.modules = moduleResponse['MODULES'];
			this.roleLayoutId = this.route.snapshot.params['roleLayoutId'];

			this.rolesService.getRoles().subscribe((rolesResponse: any) => {
				this.roles = rolesResponse.ROLES.filter(
					(role) =>
						role.NAME !== 'Public' &&
						role.NAME !== 'ExternalProbe' &&
						role.NAME !== 'LimitedUser'
				);
				if (this.additionalFields.length === 0) {
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
				}
				if (this.additionalCheckboxFields.length === 0) {
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
				}

				// if existing
				if (this.roleLayoutId !== 'new') {
					this.getRoleLayout(this.roleLayoutId).subscribe(
						(roleResponse: any) => {
							const tabs = [];
							roleResponse.getRoleLayout.tabs.forEach((tab) => {
								const columnsShow = [];
								tab.columnsShow.forEach((column) => {
									columnsShow.push(column.fieldId);
								});

								const conditions = [];
								tab.conditions.forEach((condition) => {
									conditions.push({
										condition: condition.condition.fieldId,
										conditionValue: condition.conditionValue,
										operator: condition.operator,
										requirementType: condition.requirementType,
									});
								});
								tabs.push({
									tabId: tab.tabId,
									module: tab.module.moduleId,
									orderBy: {
										order: tab.orderBy.order,
										column: tab.orderBy.column.fieldId,
									},
									columnsShow: columnsShow,
									conditions: conditions,
								});
							});
							roleResponse.getRoleLayout.role =
								roleResponse.getRoleLayout.role.roleId;
							roleResponse.getRoleLayout.tabs = tabs;
							this.roleLayout = roleResponse.getRoleLayout;
							this.roleLayoutForm.controls.NAME.setValue(
								this.roleLayout['name']
							);
							this.roleLayoutForm.controls.DESCRIPTION.setValue(
								this.roleLayout['description']
							);
							this.roleLayoutForm.controls.ROLE.setValue(
								this.roleLayout['role']
							);
							this.roleLayoutForm.controls.IS_DEFAULT.setValue(
								this.roleLayout['defaultLayout']
							);

							this.roleLayoutModuleCount =
								roleResponse.getRoleLayout['tabs'].length;

							roleResponse.getRoleLayout['tabs'].forEach((tabs, tabIndex) => {
								this.addTab(false);
								const moduleId = tabs.module;
								const moduleTabs = this.roleLayoutForm.get('TABS') as FormArray;

								moduleTabs.at(tabIndex).get('MODULE').setValue(moduleId);

								const listLayout = tabs;
								const module = this.modules.find(
									(moduleFound) => moduleFound['MODULE_ID'] === moduleId
								);
								this.fields = module.FIELDS.filter(
									(field) =>
										field.DATA_TYPE.DISPLAY !== 'Discussion' &&
										field.DATA_TYPE.DISPLAY !== 'Workflow Stages' &&
										field.DATA_TYPE.DISPLAY !== 'PDF' &&
										field.DATA_TYPE.DISPLAY !== 'File Upload' &&
										field.NAME !== 'TIME_WINDOW'
								);

								// loops through all list layouts and matches on selected id
								this.availableFields[moduleId] = [];
								this.setValueToForm(tabs.orderBy, tabIndex);
								this.fields.forEach((field) => {
									if (listLayout.columnsShow.indexOf(field.FIELD_ID) !== -1) {
										listLayout.columnsShow[
											listLayout.columnsShow.indexOf(field.FIELD_ID)
										] = field;
									} else {
										this.availableFields[moduleId].push(field);
									}
								});
								this.shownColumns[moduleId] = listLayout.columnsShow;
								this.conditionsMap[moduleId] = listLayout.conditions;
								if (
									tabIndex ===
									roleResponse.getRoleLayout['tabs'].length - 1
								) {
									this.tabGroup.selectedIndex = 0;
									this.roleLayoutLoaded = true;
								}
								if (this.tabGroup) {
									this.tabGroup.selectedIndex = 0;
								}
							});
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
				} else {
					this.addLayout();
					this.addTab(false);
				}
			});
		});
	}

	public ngOnDestroy() {}

	// adds new module form group to form
	private addModules() {
		const tabs = this.roleLayoutForm.get('TABS') as FormArray;
		tabs.push(
			this.formBuilder.group({
				MODULE: ['', Validators.required],
				ORDER_BY: this.formBuilder.group({
					COLUMN: ['', Validators.required],
					ORDER: ['', Validators.required],
				}),
				CONDITIONS: this.formBuilder.array([]),
				COLUMN_SHOW: this.formBuilder.array([]),
			})
		);
	}

	// adds new layout to rolelayout module
	public addLayout() {
		const layoutModule = {
			module: '',
			columnsShow: [],
			orderBy: {},
			conditions: [],
		};
		this.roleLayout.tabs.push(layoutModule);
	}

	// to save the role layout form
	public save() {
		// creating roleLayout object
		this.loaderService.isLoading = false;
		this.roleLayoutForm.markAllAsTouched();
		if (this.roleLayoutForm.valid) {
			const tabs = this.roleLayoutForm.get('TABS') as FormArray;
			this.roleLayout.name = this.roleLayoutForm.value['NAME'];
			this.roleLayout.description = this.roleLayoutForm.value['DESCRIPTION'];
			this.roleLayout.role = this.roleLayoutForm.value['ROLE'];
			this.roleLayout.defaultLayout = this.roleLayoutForm.value['IS_DEFAULT'];
			tabs.value.forEach((moduleTab, moduleTabIndex) => {
				if (
					moduleTab.CONDITIONS.length > 0 &&
					moduleTab.CONDITIONS[0].CONDITION === null
				) {
					moduleTab.CONDITIONS = [];
				}
				this.roleLayout.tabs[moduleTabIndex].conditions =
					this.convertConditionsToLowercase(
						this.conditionsComponent.transformConditions(moduleTab.CONDITIONS)
					);
				this.roleLayout.tabs[moduleTabIndex].columnsShow = this.shownColumns[
					moduleTab.MODULE
				].map((column) => column.FIELD_ID);
				this.roleLayout.tabs[moduleTabIndex].orderBy.order =
					moduleTab.ORDER_BY.ORDER;
				this.roleLayout.tabs[moduleTabIndex].orderBy.column =
					moduleTab.ORDER_BY.COLUMN;
				this.roleLayout.tabs[moduleTabIndex].module = moduleTab.MODULE;
			});
			if (this.roleLayoutId === 'new') {
				this.roleLayoutApiService.postRoleLayout(this.roleLayout).subscribe(
					(roleLayoutResponse: any) => {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SAVED_SUCCESSFULLY'),
						});
						this.router.navigate(['company-settings', 'role-layouts']);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
						this.loaderService.isLoading = false;
					}
				);
			} else {
				this.roleLayoutApiService.putRoleLayout(this.roleLayout).subscribe(
					(roleLayoutResponse: any) => {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
						});
						this.router.navigate(['company-settings', 'role-layouts']);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
						this.loaderService.isLoading = false;
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

	// Add the tab
	public addTab(selectAfterAdding: boolean) {
		this.disableUsedModules();
		this.addModules();
		if (selectAfterAdding == false) {
			setTimeout(() => {
				this.tabGroup.selectedIndex = 0;
			});
		} else {
			setTimeout(() => {
				this.tabGroup.selectedIndex = this.roleLayout.tabs.length - 1;
			});
		}
	}

	// remove the tab
	public removeTab(index) {
		const tabs = this.roleLayoutForm.get('TABS') as FormArray;
		tabs.removeAt(index);
		this.roleLayout.tabs.splice(index, 1);
		this.selectedTabIndex = this.roleLayout.tabs.length - 1;
		this.cdr.detectChanges();
		this.disableUsedModules();
	}

	// On selection of module
	public selectModule(event, index) {
		const tabs = this.roleLayoutForm.get('TABS') as FormArray;
		const tab = tabs.at(index);
		tab.get('ORDER_BY').reset();
		tab.get('CONDITIONS').reset();
		tab.get('COLUMN_SHOW').reset();

		let selectedModule = this.modules.find(
			(module) => module['MODULE_ID'] === event.value
		);
		let fields = selectedModule['FIELDS'].filter(
			(field) =>
				field.DATA_TYPE.DISPLAY !== 'Approval' &&
				field.DATA_TYPE.DISPLAY !== 'Button' &&
				field.DATA_TYPE.DISPLAY !== 'Discussion' &&
				field.DATA_TYPE.DISPLAY !== 'File Preview' &&
				field.DATA_TYPE.DISPLAY !== 'File Upload' &&
				field.DATA_TYPE.DISPLAY !== 'PDF' &&
				field.DATA_TYPE.DISPLAY !== 'Phone' &&
				field.NAME !== 'TIME_WINDOW' &&
				field.DATA_TYPE.DISPLAY !== 'Workflow Stages' &&
				field.DATA_TYPE.DISPLAY !== 'Zoom'
		);
		this.fields = fields;
		this.roleLayout.tabs[index].module = event.value;
		this.availableFields[event.value] = fields;
		this.shownColumns[event.value] = [];
		this.roleLayoutLoaded = true;
		this.disableUsedModules();
	}

	public getForm(index): FormGroup {
		const tabs = this.roleLayoutForm.get('TABS') as FormArray;
		const listLayoutForm = tabs.at(index) as FormGroup;
		return listLayoutForm;
	}

	public getModuleName(moduleId: String): String {
		return this.modules.find((module) => module.MODULE_ID === moduleId).NAME;
	}

	private convertListLayout(listLayoutObj: any) {
		const conditions: Condition[] = [];
		for (const condition of listLayoutObj.CONDITIONS) {
			conditions.push(
				new Condition(
					condition.condition,
					condition.conditionValue,
					condition.operator,
					condition.requirementType
				)
			);
		}
		const newListLayout = new Layout(
			new OrderBy(listLayoutObj.ORDER_BY.column, listLayoutObj.ORDER_BY.order),
			new ColumnShow(listLayoutObj.COLUMN_SHOW),
			conditions
		);
		return newListLayout;
	}

	private setValueToForm(listLayoutObj: any, index: number) {
		const tabs = this.roleLayoutForm.get('TABS') as FormArray;
		const orderBy = tabs.at(index).get('ORDER_BY') as FormGroup;
		orderBy.controls['COLUMN'].setValue(listLayoutObj.column);
		orderBy.controls['ORDER'].setValue(listLayoutObj.order);
		this.disableUsedModules();
	}

	public getRoleLayout(layoutId: String) {
		const query = `{
			getRoleLayout(id: "${layoutId}"){
				layoutId
				name
				companyId
				defaultLayout
				description
				role{
					roleId
				}
				tabs
					{
						tabId
						module{
							moduleId
						}
						columnsShow{
							fieldId
							displayLabel
						}
						 orderBy{
							   order
							   column{
								   fieldId
							   }
						   }
						   conditions{
							   requirementType
							   operator
							   condition{
								   fieldId
							   }
							   conditionValue
						   }
					}			   
			}
		  
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public convertConditionsToLowercase(conditions: any) {
		const conditionsToreturn = [];
		conditions.forEach((condition) => {
			conditionsToreturn.push({
				requirementType: condition.REQUIREMENT_TYPE,
				operator: condition.OPERATOR,
				condition: condition.CONDITION,
				conditionValue: condition.CONDITION_VALUE,
			});
		});
		return conditionsToreturn;
	}

	public disableUsedModules() {
		let tabs = this.roleLayoutForm.get('TABS') as FormArray;
		this.modulesAvaialable = [];
		tabs.value.forEach((element) => {
			const module = this.modules.find(
				(moduleFound) => moduleFound['MODULE_ID'] === element.MODULE
			);
			this.modulesAvaialable.push(module);
		});
	}
}
