import {
	CdkDragDrop,
	DragDropModule,
	transferArrayItem,
} from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import {
	FormBuilder,
	FormGroup,
	FormsModule,
	ReactiveFormsModule,
	Validators,
} from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderChange, MatSliderModule } from '@angular/material/slider';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { PanelSettingsDialogComponent } from 'src/app/dialogs/panel-settings-dialog/panel-settings-dialog.component';
import { CompaniesService } from '../../../../companies/companies.service';
import { DceLayout } from '../../../../models/dce-layout';
import { AdditionalFields } from '../../../../models/additional-field';
import { RolesService } from '../../../../roles/roles.service';
import { SharedModule } from '../../../../shared-module/shared.module';
import { FilePreviewOverlayService } from '../../../../shared/file-preview-overlay/file-preview-overlay.service';
import { config } from '../../../../tiny-mce/tiny-mce-config';
import { UsersService } from '../../../../users/users.service';
import { ModulesService } from '../../../modules.service';
import { CommonModule } from '@angular/common';
import { CacheService } from '@src/app/cache.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';

@Component({
	selector: 'app-detail-layout',
	templateUrl: './detail-layout.component.html',
	styleUrls: ['./detail-layout.component.scss'],
})
export class DetailLayoutComponent implements OnInit {
	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private modulesService: ModulesService,
		private rolesService: RolesService,
		private userService: UsersService,
		private bannerMessageService: BannerMessageService,
		private formBuilder: FormBuilder,
		private translateService: TranslateService,
		private dialog: MatDialog,
		private fpos: FilePreviewOverlayService,
		private companiesService: CompaniesService,
		private cacheService: CacheService,
		private loaderService: LoaderService
	) {
		this.config['height'] = '100%';
		this.translateService.get('ROLE').subscribe((value: string) => {
			this.params = { field: value };
		});
	}
	public premadeResponses: any = [];
	public config = config;
	public roles = [];
	public today: Date = new Date();
	public layout: DceLayout = new DceLayout('', '', '');
	public additionalFields = [];
	private module: any = {};
	public layoutForm: FormGroup;
	private layoutType = '';
	private roleName = '';
	public showType = 'ALL';
	public discussionType = 'Messages';
	public gridLayout = false;
	public customLayouts = [];
	public layoutData;
	public params;
	public layoutStyle = 'standard';
	public allRequiredFields = [];
	public sections: any = [];
	public component = 'builder';
	public currentRole = {};
	public template = '';
	// CUSTOM LAYOUT VARIABLES

	// public grids: any[][] = [[]];
	public rowSize = 10;
	public fieldsMap: any = [];
	public fields;
	public dropList: any[] = [];
	public customLayout = `<div [ngStyle]="{'height': '100vh'}"
    fxFlex fxLayoutAlign="center center"><mat-spinner></mat-spinner></div>`;
	public showView = {};
	public globalIndex = 0;
	public cellFlexSize = 25;
	public sidebarCount = 0;
	public titleBarFields: any[] = [];
	public discussionEncountered = false;
	public discussionPosition = {
		xPos: null,
		yPos: null,
		size: 0,
	};
	public ImageEncountered = false;
	public imagePosition = {
		xPos: null,
		yPos: null,
		size: 0,
	};
	public filePreviewEncountered = false;
	public filePreviewPosition = {
		xPos: null,
		yPos: null,
		size: 0,
	};
	public conditionEncountered: boolean = false;
	public conditionPosition = {
		xPos: null,
		yPos: null,
		size: 0,
	};

	public receiptEncountered = false;
	public receiptPosition = {
		xPos: null,
		yPos: null,
		size: 0,
	};
	public listFormulaEncountered = false;
	public listFormulaPosition = {
		xPos: null,
		yPos: null,
		size: 0,
	};

	public dataMaterialModule: any = {
		imports: [
			FormsModule,
			ReactiveFormsModule,
			DragDropModule,
			FlexLayoutModule,
			MatAutocompleteModule,
			MatButtonModule,
			MatCardModule,
			MatCheckboxModule,
			MatChipsModule,
			MatDatepickerModule,
			MatIconModule,
			MatInputModule,
			MatListModule,
			MatMenuModule,
			MatNativeDateModule,
			MatRadioModule,
			MatRippleModule,
			MatSelectModule,
			MatSidenavModule,
			MatSlideToggleModule,
			MatToolbarModule,
			MatTooltipModule,
			MatTabsModule,
			SharedModule,
			TranslateModule,
			MatSliderModule,
			CommonModule,
		],
		exports: [],
	};

	// public role = new FormControl('', [Validators.required]);
	public ngOnInit() {
		// START FOR TS LINT
		// TODO: FIX TS LINT CHECK
		if (this.userService.user) {
			// DO NOTHING TS LINT FIX
		}
		this.displayFn('test');
		// END FOR TS LINT

		this.dropList.push('field1');

		const layoutId = this.route.snapshot.params['layoutId'];
		this.layoutType = this.route.snapshot.params['layoutType'];
		this.layoutForm = this.formBuilder.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
			ROLE: ['', [Validators.required]],
		});
		this.layoutForm.setValue({ NAME: '', DESCRIPTION: '', ROLE: '' });
		const moduleId = this.route.snapshot.params['moduleId'];
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
				this.modulesService.getModuleById(moduleId).subscribe(
					(response: any) => {
						this.module = response;
						// TODO: Remove
						if (response.NAME !== 'Tickets' && response.NAME !== 'Chat') {
							// this.redirectToHome();
							this.gridLayout = true;
						}

						// filtering the fields
						if (this.layoutType === 'create_layouts') {
							const fields = [];
							this.module.FIELDS.forEach((field) => {
								if (
									field.NAME !== 'SOURCE_TYPE' &&
									field.NAME !== 'LAST_UPDATED_BY' &&
									field.NAME !== 'CREATED_BY' &&
									field.NAME !== 'DATE_UPDATED' &&
									field.NAME !== 'DATE_CREATED' &&
									field.DATA_TYPE.DISPLAY !== 'Auto Number' &&
									field.NAME !== 'CHANNEL' &&
									field.DATA_TYPE.DISPLAY !== 'Button' &&
									field.DATA_TYPE.DISPLAY !== 'Zoom' &&
									field.NAME !== 'DATA_ID' &&
									field.NAME !== 'EFFECTIVE_FROM' &&
									field.NAME !== 'EFFECTIVE_TO' &&
									field.NAME !== 'FILE_PREVIEW' &&
									field.DATA_TYPE.DISPLAY !== 'Condition' &&
									field.NAME !== 'APPROVAL' &&
									field?.RELATIONSHIP_TYPE !== 'One to Many'
								) {
									fields.push(field);
								}
							});
							this.module.FIELDS = fields;
						} else {
							const fields = [];
							this.module.FIELDS.forEach((field) => {
								if (field.NAME !== 'DATA_ID' && field.NAME !== 'APPROVAL') {
									fields.push(field);
								}
							});
							this.module.FIELDS = fields;
						}
						// CUSTOM LAYOUT CODE
						this.module.FIELDS.forEach((field) => {
							this.fieldsMap[field.FIELD_ID] = field;
						});
						this.fields = JSON.parse(JSON.stringify(this.module.FIELDS));
						if (!this.gridLayout) {
							if (layoutId !== 'new') {
								let layout;
								// getting layout based on type
								this.modulesService
									.getLayoutById(moduleId, this.layoutType, layoutId)
									.subscribe(
										(layoutResponse: any) => {
											layout = layoutResponse;
											this.layoutForm.controls['NAME'].setValue(layout.NAME);
											this.layoutForm.controls['ROLE'].setValue(layout.ROLE);
											this.layoutForm.controls['DESCRIPTION'].setValue(
												layout.DESCRIPTION
											);
											let getTemplate = 'CREATE_LAYOUTS';
											if (this.layoutType === 'create_layouts') {
												getTemplate = 'CREATE_LAYOUTS';
											} else if (this.layoutType === 'edit_layouts') {
												getTemplate = 'EDIT_LAYOUTS';
											}
											this.modulesService
												.getPreDefinedLayout(this.module.NAME, getTemplate)
												.subscribe(
													(predefinedLayout: any) => {
														if (layout.PREDEFINED_TEMPLATE !== null) {
															this.sections = layout.PREDEFINED_TEMPLATE;
														}
														const template = predefinedLayout.HTML_TEMPLATE;
														const fieldDropRegex = new RegExp(
															'id="(fieldDrop.*?)"',
															'gm'
														);
														let resultArray = [];
														while (
															(resultArray = fieldDropRegex.exec(template)) !==
															null
														) {
															this.dropList.push(resultArray[1]);
														}
														this.layout = new DceLayout(
															layout.NAME,
															layout.DESCRIPTION,
															layout.ROLE,
															layout.DATE_CREATED,
															layout.DATE_UPDATED,
															layout.LAST_UPDATED_BY,
															layout.CREATED_BY,
															null,
															template,
															layout.LAYOUT_ID
														);
														this.layoutStyle = layout.LAYOUT_STYLE;
														this.currentRole = this.roles.find((role) => {
															return role.ROLE_ID === layout.ROLE;
														});
													},
													(error) => {
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
							} else {
								this.modulesService
									.getPreDefinedLayout(this.module.NAME, 'CREATE_LAYOUTS')
									.subscribe(
										(predefinedLayout: any) => {
											this.template = predefinedLayout.HTML_TEMPLATE;
											const fieldDropRegex = new RegExp(
												'id="(fieldDrop.*?)"',
												'gm'
											);
											let resultArray = [];
											while (
												(resultArray = fieldDropRegex.exec(this.template)) !==
												null
											) {
												this.dropList.push(resultArray[1]);
											}

											this.layout = new DceLayout(
												'',
												'',
												this.roles[0].ROLE_ID,
												null,
												null,
												null,
												null,
												null,
												this.template,
												null,
												null
											);
											this.currentRole = this.roles.find((role) => {
												return role.ROLE_ID === this.layout.role;
											});
										},
										(error) => {
											this.bannerMessageService.errorNotifications.push({
												message: error.error.ERROR,
											});
										}
									);
							}
						} else {
							if (layoutId !== 'new') {
								this.modulesService
									.getLayoutById(moduleId, this.layoutType, layoutId)
									.subscribe(
										(data: any) => {
											this.layoutData = data;
											// this.layout = data;
											this.layoutForm.setValue({
												NAME: data.NAME,
												DESCRIPTION: data.DESCRIPTION,
												ROLE: data.ROLE,
											});
											this.layoutStyle = data.LAYOUT_STYLE;
											this.layout.role = data.ROLE;
											if (data.PANELS) {
												data.PANELS.forEach((value) => {
													this.loadNewGridAndView(value);
													this.globalIndex++;
												});
											}
											if (data.TITLE_BAR) {
												const titleBar = data.TITLE_BAR.map((value) => {
													return {
														settings: {
															action: value.SETTINGS.ACTION
																? value.SETTINGS.ACTION
																: '',
															conditions: value.SETTINGS.CONDITIONS
																? value.SETTINGS.CONDITIONS
																: [],
														},
														FIELD_ID: value.FIELD_ID,
													};
												});
												this.titleBarFields = titleBar;
											}
										},
										(error) => {
											this.bannerMessageService.errorNotifications.push({
												message: error.error.ERROR,
											});
										}
									);
							} else {
								this.loadNewGridAndView(null);
							}
						}
					},
					(error) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
						this.redirectToHome();
					}
				);
			},
			(error) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public deleteEntry() {}
	public onFileChange() {}
	public toggleDisable() {}

	public removeDiv(fieldId: string, div) {
		this.sections.forEach((section) => {
			if (section.ID === div) {
				section.FIELDS = section.FIELDS.filter(
					(field) => field.FIELD_ID !== fieldId
				);
			}
		});
	}

	public dropTitleBarFields(event: CdkDragDrop<string[]>) {
		const fieldId = event.item.element.nativeElement.id;
		if (this.fieldsMap[fieldId].DATA_TYPE.DISPLAY !== 'Button') {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('ONLY_BUTTON_ALLOWED'),
			});
			return;
		}
		if (
			this.titleBarFields.length < 8 &&
			this.titleBarFields
				.map((v) => {
					return v.FIELD_ID;
				})
				.indexOf(fieldId) === -1
		) {
			this.titleBarFields.push({ FIELD_ID: fieldId });
		}
	}
	public drop(event: CdkDragDrop<string[]>, div: string) {
		let hasSection = false;
		const fieldId = event.item.element.nativeElement.id;
		if (this.fieldsMap[fieldId].NOT_EDITABLE && div === 'SIDE_BAR') {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('NOT_EDITABLE_FIELDS'),
			});
			return;
		} else if (
			this.fieldsMap[fieldId].DATA_TYPE.DISPLAY === 'Discussion' ||
			this.fieldsMap[fieldId].NAME === 'SUBJECT'
		) {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('FIELD_ALREADY AVAILABLE'),
			});
			return;
		} else if (
			this.fieldsMap[fieldId].DATA_TYPE.DISPLAY === 'Button' &&
			div === 'HEADER'
		) {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('CANNOT_BE_DROPPED_TO_HEADER'),
			});
			return;
		} else if (this.fieldsMap[fieldId].DATA_TYPE.DISPLAY === 'List Formula') {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant(
					'LIST_FORMULA_CANNOT_BE_DROPPED'
				),
			});
			return;
		}
		this.sections.forEach((section) => {
			if (section.ID === div) {
				hasSection = true;
				let fieldDropped = false;
				section.FIELDS.forEach((field) => {
					if (field.FIELD_ID === fieldId) {
						fieldDropped = true;
					}
				});
				if (!fieldDropped) {
					section.FIELDS.push({ FIELD_ID: fieldId });
				}
			}
		});
		if (!hasSection) {
			if (div === 'HEADER') {
				this.sections.push({
					ID: div,
					FIELDS: [{ FIELD_ID: fieldId }],
					FIELD_STYLE: 'NON_EDITABLE_PILLS',
				});
			} else {
				this.sections.push({
					ID: div,
					FIELDS: [{ FIELD_ID: fieldId }],
					FIELD_STYLE: 'EDITABLE',
				});
			}
		}
	}

	public newLayout() {
		if (this.customLayout.indexOf('<mat-spinner></mat-spinner>') === -1) {
			this.loadNewGridAndView(null);
		}
	}

	public removeLayout(index) {
		this.customLayouts[index].fields.forEach((v) => this.module.FIELDS.push(v));
		this.customLayouts.splice(index, 1);
	}

	public gridRow(name, index, num) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const i = this.customLayouts.findIndex((f) => f.name === name);
		if (i === 0) {
			this.sidebarCount = 0;
		}
		let groupId = null;
		if (num === -1) {
			this.fields.forEach((element) => {
				if (element.FIELD_ID === custom.grids[index][0].FIELD_ID) {
					groupId = element.GROUP_ID;
				}
			});
			if (groupId != null) {
				let loop = 0;
				custom.grids[index].forEach((v) => {
					for (let x = 0; x < custom.grids.length; x++) {
						let gId = null;

						this.fields.forEach((element) => {
							if (!custom.grids[x][0].IS_EMPTY) {
								if (element.FIELD_ID === custom.grids[x][0].FIELD_ID) {
									gId = element.GROUP_ID;
								}
							}
							if (element.GROUP_ID === groupId) {
								if (!this.module.FIELDS.includes(element)) {
									this.module.FIELDS.push(element);
								}
							}
							if (loop < 3) {
								if (gId === groupId) {
									custom.grids.splice(x, 1);
									loop++;
								}
							}
						});
					}
				});
			} else {
				if (custom.grids.length > 1) {
					custom.grids[index].forEach((v) => {
						if (!v.IS_EMPTY) {
							if (this.module.FIELDS.indexOf(v.FIELD_ID) === -1) {
								this.module.FIELDS.push(
									this.fields.find((f) => f.FIELD_ID === v.FIELD_ID)
								);
							}
						}
					});
					custom.grids.splice(index, 1);
				} else {
					this.bannerMessageService.errorNotifications.push({
						message: 'Cannot remove the only row',
					});
				}
			}
		} else {
			if (custom.grids.length < 20) {
				const grid = [];
				for (let j = 0; j < 4; j++) {
					grid[j] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: 25,
						FIELD_ID: '',
						settings: null,
					};
				}
				custom.grids.push(grid);
			} else {
				this.bannerMessageService.errorNotifications.push({
					message: 'Maximum 20 rows allowed',
				});
			}
		}
		const fields = this.loadCustomLayout(name, custom.grids);
		custom.customLayout = this.customLayout;
		custom.fields = fields;
		if (custom.discussionDropped) {
			custom.discussionDropped = false;
			custom.discussionPosition.xPos =
				num === -1 && custom.discussionPosition.xPos > index
					? custom.discussionPosition.xPos - 1
					: custom.discussionPosition.xPos;
			this.loadLayoutWithDiscussionSection(
				name,
				custom.discussionPosition,
				custom.discussionPosition.size
			);
		} else if (custom.imageDropped) {
			custom.imageDropped = false;
			custom.imagePosition.xPos =
				num === -1 && custom.imagePosition.xPos > index
					? custom.imagePosition.xPos - 1
					: custom.imagePosition.xPos;
			this.loadLayoutWithImageSection(
				name,
				custom.imagePosition,
				custom.imagePosition.size
			);
		} else if (custom.filePreviewDropped) {
			custom.filePreviewDropped = false;
			custom.filePreviewPosition.xPos =
				num === -1 && custom.filePreviewPosition.xPos > index
					? custom.filePreviewPosition.xPos - 1
					: custom.filePreviewPosition.xPos;
			this.loadLayoutWithfilePreviewSection(
				name,
				custom.filePreviewPosition,
				custom.filePreviewPosition.size
			);
		} else if (custom.conditionDropped) {
			custom.conditionDropped = false;
			custom.conditionPosition.xPos =
				num === -1 && custom.conditionPosition.xPos > index
					? custom.conditionPosition.xPos - 1
					: custom.conditionPosition.xPos;
			this.loadLayoutWithConditionRow(
				name,
				custom.conditionPosition,
				custom.conditionPosition.size
			);
		}
		if (custom.receiptDropped) {
			custom.receiptDropped = false;
			custom.receiptPosition.xPos =
				num === -1 && custom.receiptPosition.xPos > index
					? custom.receiptPosition.xPos - 1
					: custom.receiptPosition.xPos;
			this.loadLayoutWithReceiptSection(
				name,
				custom.receiptPosition,
				custom.receiptPosition.size
			);
		}
		if (custom.listFormulaDropped) {
			custom.listFormulaDropped = false;
			custom.listFormulaPosition.xPos =
				num === -1 && custom.listFormulaPosition.xPos > index
					? custom.listFormulaPosition.xPos - 1
					: custom.listFormulaPosition.xPos;

			this.loadLayoutWithListFormulaSection(
				name,
				custom.listFormulaPosition,
				custom.listFormulaPosition.size
			);
		}
	}

	private redirectToHome() {
		const moduleId = this.route.snapshot.params['moduleId'];
		this.router.navigate([`modules/${moduleId}/${this.layoutType}`]);
	}

	public save() {
		this.layoutForm.get('NAME').markAsTouched();
		this.layoutForm.get('ROLE').markAsTouched();

		if (this.layoutForm.valid) {
			if (this.layout.role && this.layout.role === '') {
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('SELECT_ROLE'),
				});
				return;
			}
			if (!this.gridLayout) {
				this.layout.name = this.layoutForm.value.NAME;
				this.layout.description = this.layoutForm.value.DESCRIPTION;
				this.layout.role = this.layoutForm.value.ROLE;
				const layoutToSave = JSON.parse(JSON.stringify(this.layout));
				layoutToSave.PREDEFINED_TEMPLATE = this.sections;
				layoutToSave.CUSTOM_LAYOUT = null;
				layoutToSave.LAYOUT_STYLE = this.layoutStyle;
				const layoutId = this.route.snapshot.params['layoutId'];
				if (layoutId !== 'new') {
					this.modulesService
						.putLayout(
							this.module.MODULE_ID,
							this.layoutType,
							layoutId,
							layoutToSave
						)
						.subscribe(
							(response: any) => {
								this.companiesService.trackEvent(`Updated layout`, {
									LAYOUT_TYPE: this.layoutType,
									LAYOUT_ID: response.LAYOUT_ID,
									MODULE_ID: this.module.MODULE_ID,
								});
								this.router.navigate([
									`modules/${this.module.MODULE_ID}/${this.layoutType}`,
								]);
							},
							(error) => {
								this.bannerMessageService.errorNotifications.push({
									message: error.error.ERROR,
								});
							}
						);
				} else {
					this.modulesService
						.postLayouts(this.module.MODULE_ID, this.layoutType, layoutToSave)
						.subscribe(
							(data: any) => {
								this.companiesService.trackEvent(`Created Layout`, {
									LAYOUT_TYPE: this.layoutType,
									LAYOUT_ID: data.LAYOUT_ID,
									MODULE_ID: this.module.MODULE_ID,
								});
								this.cacheService.updateModule(this.module.MODULE_ID);
								this.router.navigate([
									`modules/${this.route.snapshot.params.moduleId}/${this.layoutType}`,
								]);
							},
							(error) => {
								this.bannerMessageService.errorNotifications.push({
									message: error.error.ERROR,
								});
							}
						);
				}
			} else {
				const layoutId = this.route.snapshot.params['layoutId'];
				// this.layout.grids = this.grids;
				const { NAME, DESCRIPTION } = this.layoutForm.value;
				const panels = this.customLayouts.map((value) => {
					return {
						SETTINGS: {
							COLLAPSABLE: value.collapse,
							ACTION: value.settings ? value.settings.action : '',
							CONDITIONS: value.settings ? value.settings.conditions : [],
						},
						ID: value.name,
						PANEL_NAME: value.displayName,
						DISPLAY_TYPE: value.displayType,
						GRIDS: value.grids.map((grids) => {
							return grids.map((grid) => {
								return {
									IS_EMPTY: grid.IS_EMPTY,
									HEIGHT: grid.HEIGHT,
									WIDTH: grid.WIDTH,
									FIELD_ID: grid.FIELD_ID,
									SETTINGS: {
										ACTION: grid.settings ? grid.settings.action : '',
										CONDITIONS: grid.settings ? grid.settings.conditions : [],
									},
								};
							});
						}),
					};
				});
				if (this.layoutStyle === null || this.layoutStyle === undefined) {
					this.layoutStyle = 'standard';
				}
				const titleBar = this.titleBarFields.map((value) => {
					return {
						SETTINGS: {
							ACTION: value.settings ? value.settings.action : '',
							CONDITIONS: value.settings ? value.settings.conditions : [],
						},
						FIELD_ID: value.FIELD_ID,
					};
				});
				const newLayout = {
					NAME,
					DESCRIPTION,
					ROLE: this.layoutForm.get('ROLE').value,
					PANELS: panels,
					TITLE_BAR: titleBar,
					LAYOUT_STYLE: this.layoutStyle,
				};
				if (layoutId !== 'new') {
					this.modulesService
						.putLayout(this.module.MODULE_ID, this.layoutType, layoutId, {
							...this.layoutData,
							...newLayout,
						})
						.subscribe(
							(layout) => {
								this.companiesService.trackEvent(`Updated layout`, {
									LAYOUT_TYPE: this.layoutType,
									LAYOUT_ID: layoutId,
									MODULE_ID: this.module.MODULE_ID,
								});
								this.cacheService.updateModule(this.module.MODULE_ID);
								this.router.navigate([
									`modules/${this.module.MODULE_ID}/${this.layoutType}`,
								]);
							},
							(error) => {
								this.bannerMessageService.errorNotifications.push({
									message: error.error.ERROR,
								});
							}
						);
				} else {
					this.modulesService
						.postLayouts(this.module.MODULE_ID, this.layoutType, newLayout)
						.subscribe(
							(data: any) => {
								this.companiesService.trackEvent(`Created Layout`, {
									LAYOUT_TYPE: this.layoutType,
									LAYOUT_ID: data.LAYOUT_ID,
									MODULE_ID: this.module.MODULE_ID,
								});
								this.router.navigate([
									`modules/${this.route.snapshot.params.moduleId}/${this.layoutType}`,
								]);
							},
							(error) => {
								this.bannerMessageService.errorNotifications.push({
									message: error.error.ERROR,
								});
							}
						);
				}
			}
		} else {
			this.loaderService.isLoading = false;
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('FILL_REQUIRED_FIELDS'),
			});
		}
	}

	// Validate if all the required fields exist in the layout or not
	// private validateFields(layoutToSave) {
	// 	let valid = true;
	// 	if (this.roleName === 'SystemAdmin') {
	// 		for (const requiredField of this.allRequiredFields) {
	// 			const regxSidebar = new RegExp(
	// 				`<div class="SIDEBAR_${requiredField.FIELD_ID}"([\\s\\S]*?)`,
	// 				'g'
	// 			);
	// 			if (!requiredField.INTERNAL && !requiredField.NOT_EDITABLE) {
	// 				if (layoutToSave.match(regxSidebar)) {
	// 					valid = true;
	// 				} else {
	// 					valid = false;
	// 					this.bannerMessageService.errorNotifications.push({
	// 						message:
	// 							requiredField.DISPLAY_LABEL +
	// 							' ' +
	// 							this.translateService.instant('IS_REQUIRED')
	// 					});
	// 					break;
	// 				}
	// 			}
	// 		}
	// 	}
	// 	return valid;
	// }

	public onSelectDiscussionTabs($event) {
		const tabValue = $event.tab.textLabel;
		if (tabValue === 'All') {
			this.showType = 'ALL';
			this.discussionType = 'Messages';
		} else {
			this.showType = 'MESSAGES';
		}
	}
	// CUSTOM LAYOUT FUNCTIONS

	public openSettings(customLayout) {
		const dialogRef = this.dialog.open(PanelSettingsDialogComponent, {
			data: {
				buttonText: this.translateService.instant('SAVE'),
				closeDialog: this.translateService.instant('CANCEL'),
				fields: JSON.parse(JSON.stringify(this.fields)),
				moduleId: this.route.snapshot.params['moduleId'],
				customLayout,
			},
		});
		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result) {
				customLayout.settings = result;
			}
		});
	}

	public loadCustomLayout(name, grids) {
		let layout = `<div fxLayout="column" fxLayoutGap=5px fxFlex>`;
		let discussionEncountered = false;
		let ImageEncountered = false;
		let filePreviewEncountered = false;
		let conditionEncountered = false;
		let receiptEncountered = false;
		let listFormulaEncountered = false;

		const fields = [];
		const size = grids[0].length === 0 ? 10 : grids.length;
		for (let i = 0; i < size; i++) {
			layout = layout + `<div class='ROW_${i}' fxLayout="row" fxLayoutGap=5px>`;
			grids[i] = grids[i] ? grids[i] : [];
			for (let j = 0; j < 4; j++) {
				grids[i][j] = grids[i][j]
					? grids[i][j]
					: {
							IS_EMPTY: true,
							HEIGHT: 10,
							WIDTH: 25,
							FIELD_ID: '',
							SETTINGS: null,
					  };
				// USED FOR LINKING THE DRAG AND DROP
				this.dropList.push(`${name}_${i}_${j}`);
				// TO CONTROL THE VIEW (EDIT/LABEL)
				this.showView[`CELL_${name}_${i}_${j}`] = 'LABEL';
				if (grids[i][j].IS_EMPTY) {
					if (
						(j === 1 &&
							(grids[i][j - 1].WIDTH === 75 ||
								grids[i][j - 1].WIDTH === 100)) ||
						(j === 2 &&
							(grids[i][j - 2].WIDTH === 75 ||
								grids[i][j - 2].WIDTH === 100)) ||
						(j === 3 && grids[i][j - 3].WIDTH === 100)
					) {
						grids[i][j].WIDTH = 0;
					}
					layout = layout + this.initialTemplate(grids, name, i, j);
				} else {
					this.module.FIELDS = this.module.FIELDS.filter(
						(element) => element.FIELD_ID !== grids[i][j].FIELD_ID
					);
					if (
						fields.map((v) => v.FIELD_ID).indexOf(grids[i][j].FIELD_ID) === -1
					) {
						fields.push(
							this.fields.find((f) => f.FIELD_ID === grids[i][j].FIELD_ID)
						);

						this.fields.forEach((element) => {
							if (element.GROUP_ID === grids[i][j].GROUP_ID) {
								fields.push(element);
							}
						});
					}
					if (!discussionEncountered) {
						if (
							this.fieldsMap[grids[i][j].FIELD_ID] &&
							this.fieldsMap[grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY ===
								'Discussion'
						) {
							this.discussionEncountered = true;
							this.discussionPosition.xPos = i;
							this.discussionPosition.yPos = j;
							discussionEncountered = true;
						}
					}

					// to set X and y possitions if field display is Image
					if (!ImageEncountered) {
						if (
							this.fieldsMap[grids[i][j].FIELD_ID] &&
							this.fieldsMap[grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY === 'Image'
						) {
							this.ImageEncountered = true;
							this.imagePosition.xPos = i;
							this.imagePosition.yPos = j;
							ImageEncountered = true;
						}
					}

					// Set the x and y positions for file preview field.
					if (!filePreviewEncountered) {
						if (
							this.fieldsMap[grids[i][j].FIELD_ID] &&
							this.fieldsMap[grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY ===
								'File Preview'
						) {
							this.filePreviewEncountered = true;
							this.filePreviewPosition.xPos = i;
							this.filePreviewPosition.yPos = j;
							filePreviewEncountered = true;
						}
					}

					if (!conditionEncountered) {
						if (
							this.fieldsMap[grids[i][j].FIELD_ID] &&
							this.fieldsMap[grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY ===
								'Condition'
						) {
							this.conditionEncountered = true;
							this.conditionPosition.xPos = i;
							this.conditionPosition.yPos = j;
							conditionEncountered = true;
						}
					}
					// to set X and y possitions if field display is Receipt Capture
					if (!receiptEncountered) {
						if (
							this.fieldsMap[grids[i][j].FIELD_ID] &&
							this.fieldsMap[grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY ===
								'Receipt Capture'
						) {
							this.receiptEncountered = true;
							this.receiptPosition.xPos = i;
							this.receiptPosition.yPos = j;
							receiptEncountered = true;
						}
					}
					if (!listFormulaEncountered) {
						if (
							this.fieldsMap[grids[i][j].FIELD_ID] &&
							this.fieldsMap[grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY ===
								'List Formula'
						) {
							this.listFormulaEncountered = true;
							this.listFormulaPosition.xPos = i;
							this.listFormulaPosition.yPos = j;
							listFormulaEncountered = true;
						}
					}
					layout = layout + this.replaceCellTemplate(grids, name, i, j);
				}
			}
			layout =
				layout +
				`<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
				(click)="context.gridRow('${name}', ${i}, -1)">close</mat-icon></div></div><!--END_ROW_${i}-->`;
		}
		layout = layout + `</div>`;
		// FINAL LAYOUT USED BY P3X
		this.customLayout = `<!--CUSTOM_LAYOUT_START-->
            <!--START_REPLACABLE_LAYOUT-->
            ${layout}
			<!--END_REPLACABLE_LAYOUT-->`;

		this.sidebarCount++;
		return fields;
	}

	private toTitleCase(phrase: string) {
		return phrase
			.toLowerCase()
			.split('_')
			.map((word) => word.charAt(0).toUpperCase() + word.slice(1))
			.join(' ');
	}

	public loadNewGridAndView(value) {
		this.rowSize = 10;
		if (this.customLayouts.length === 0) {
			this.module.FIELDS = JSON.parse(JSON.stringify(this.fields));
		}
		let name = '';
		let displayName = '';
		let displayType = 'Panel';
		let grids = [[]];
		let settings = null;
		if (value) {
			this.rowSize = value.GRIDS.length;
			grids = value.GRIDS.map((grids: any[]) => {
				return grids.map((grid) => {
					return {
						IS_EMPTY: grid.IS_EMPTY,
						HEIGHT: grid.HEIGHT,
						WIDTH: grid.WIDTH,
						FIELD_ID: grid.FIELD_ID,
						settings: {
							action: grid.SETTINGS ? grid.SETTINGS.ACTION : '',
							conditions: grid.SETTINGS ? grid.SETTINGS.CONDITIONS : [],
						},
					};
				});
			});
			name = value.ID;
			displayName = value.PANEL_NAME;
			displayType = value.DISPLAY_TYPE;
			settings = {
				action: value.SETTINGS.ACTION,
				conditions: value.SETTINGS.CONDITIONS,
			};
		} else {
			name = `PANEL_${this.customLayouts.length + 1}`;
			displayName = this.toTitleCase(name);
			// DEFAULT GRID LAYOUT
			this.rowSize = this.rowSize < 1 || this.rowSize > 20 ? 20 : this.rowSize;
		}
		const fields = this.loadCustomLayout(name, grids);
		this.customLayouts.push({
			name,
			displayName,
			customLayout: this.customLayout,
			collapse: value ? value.SETTINGS.COLLAPSABLE : false,
			grids: JSON.parse(JSON.stringify(grids)),
			fields,
			displayType,
			discussionDropped: false,
			discussionPosition: this.discussionEncountered
				? this.discussionPosition
				: {
						xPos: null,
						yPos: null,
				  },
			filePreviewDropped: false,
			filePreviewPosition: this.filePreviewEncountered
				? this.filePreviewPosition
				: {
						xPos: null,
						yPos: null,
				  },
			settings,
			imageDropped: false,
			imagePosition: this.ImageEncountered
				? this.imagePosition
				: {
						xPos: null,
						yPos: null,
				  },
			conditionDropped: false,
			conditionPosition: this.conditionPosition
				? this.conditionPosition
				: {
						xPos: null,
						yPos: null,
				  },
			receiptDropped: false,
			receiptPosition: this.receiptEncountered
				? this.receiptPosition
				: {
						xPos: null,
						yPos: null,
				  },
			listFormulaDropped: false,
			listFormulaPosition: this.listFormulaEncountered
				? this.listFormulaPosition
				: {
						xPos: null,
						yPos: null,
				  },
		});
		if (this.discussionEncountered) {
			let size = 0;
			for (let y = this.discussionPosition.yPos; y < 4; y++) {
				if (
					!grids[this.discussionPosition.xPos][y].IS_EMPTY &&
					this.fieldsMap[grids[this.discussionPosition.xPos][y].FIELD_ID]
						.DATA_TYPE.DISPLAY === 'Discussion'
				) {
					size = size + 1;
				}
			}
			this.discussionPosition.size = size;
			this.loadLayoutWithDiscussionSection(name, this.discussionPosition, size);
		}
		this.discussionEncountered = false;

		// Incresing the field size and loading Image layout
		if (this.ImageEncountered) {
			let size = 0;
			for (let y = this.imagePosition.yPos; y < 4; y++) {
				if (
					!grids[this.imagePosition.xPos][y].IS_EMPTY &&
					this.fieldsMap[grids[this.imagePosition.xPos][y].FIELD_ID].DATA_TYPE
						.DISPLAY === 'Image'
				) {
					size = size + 1;
				}
			}
			this.imagePosition.size = size;
			this.loadLayoutWithImageSection(name, this.imagePosition, size);
		}
		this.ImageEncountered = false;

		// To get the drag and drp fields positions and layout for File Preview field.
		if (this.filePreviewEncountered) {
			let size = 0;
			for (let y = this.filePreviewPosition.yPos; y < 4; y++) {
				if (
					!grids[this.filePreviewPosition.xPos][y].IS_EMPTY &&
					this.fieldsMap[grids[this.filePreviewPosition.xPos][y].FIELD_ID]
						.DATA_TYPE.DISPLAY === 'File Preview'
				) {
					size = size + 1;
				}
			}
			this.filePreviewPosition.size = size;
			this.loadLayoutWithfilePreviewSection(
				name,
				this.filePreviewPosition,
				size
			);
		}
		this.filePreviewEncountered = false;

		if (this.conditionEncountered) {
			// let size = 0;
			// for (let y = this.conditionPosition.yPos; y < 4; y++) {
			// 	if (
			// 		!grids[this.conditionPosition.xPos][y].IS_EMPTY &&
			// 		this.fieldsMap[grids[this.conditionPosition.xPos][y].FIELD_ID]
			// 			.DATA_TYPE.DISPLAY === 'Condition'
			// 	) {
			// 		size = size + 3;
			// 	}
			// }
			this.conditionPosition.size = 4;
			this.loadLayoutWithConditionRow(name, this.conditionPosition, 4);
		}
		this.conditionEncountered = false;

		// to load Receipt Capture view and change size
		if (this.receiptEncountered) {
			let size = 0;
			for (let y = this.receiptPosition.yPos; y < 4; y++) {
				if (
					!grids[this.receiptPosition.xPos][y].IS_EMPTY &&
					this.fieldsMap[grids[this.receiptPosition.xPos][y].FIELD_ID].DATA_TYPE
						.DISPLAY === 'Receipt Capture'
				) {
					size = size + 1;
				}
			}
			this.receiptPosition.size = size;
			this.loadLayoutWithReceiptSection(name, this.receiptPosition, size);
		}
		this.receiptEncountered = false;

		if (this.listFormulaEncountered) {
			let size = 0;
			for (let y = this.listFormulaPosition.yPos; y < 4; y++) {
				if (
					!grids[this.listFormulaPosition.xPos][y].IS_EMPTY &&
					this.fieldsMap[grids[this.listFormulaPosition.xPos][y].FIELD_ID]
						.DATA_TYPE.DISPLAY === 'List Formula'
				) {
					size = size + 1;
				}
			}
			this.listFormulaPosition.size = size;
			this.loadLayoutWithListFormulaSection(
				name,
				this.listFormulaPosition,
				size
			);
		}
		this.listFormulaEncountered = false;
	}

	// DROP HERE TEMPLATE
	public initialTemplate(grids, name, i, j) {
		if (grids[i][j].WIDTH !== 0) {
			let fieldWidth: string = grids[i][j].WIDTH;
			return `<div class='CELL_${name}_${i}_${j}'
      style="height: 40px;"
         fxLayoutAlign="start center"
         cdkDropList (cdkDropListDropped)="context.dropField('${name}', $event)"
         id='${name}_${i}_${j}'
         [ngStyle]="{'border': '1px dashed #ccc','border-radius': '5px','min-width': (${fieldWidth} == 50) ? '47.5%' : (${fieldWidth} == 25) ? '24.06%' : (${fieldWidth} == 100) ? '92%' : (${fieldWidth} == 75) ? '73.08%':'0%'}">
        <div class="mat-caption" style="color:#888;padding:10px;">
          Drop Here
        </div>
      </div><!--END_CELL_${name}_${i}_${j}-->`;
		} else {
			return `<div class='CELL_${name}_${i}_${j}' *ngIf="${grids[i][j].WIDTH} !== 0"></div><!--END_CELL_${name}_${i}_${j}-->`;
		}
	}

	// TEMPLATE AFTER DROP
	public replaceCellTemplate(grids, name, i, j) {
		const index =
			this.customLayouts.findIndex((f) => f.name === name) === -1
				? this.globalIndex
				: this.customLayouts.findIndex((f) => f.name === name);
		this.cellFlexSize = 25;
		const fieldId = grids[i][j].FIELD_ID;
		const displayLabel = this.fieldsMap[fieldId].DISPLAY_LABEL;

		return `<div class='CELL_${name}_${i}_${j} mat-caption'
      fxLayout="row" fxLayoutAlign="center center"
	  [ngStyle]="{ 'border': '1px solid #ccc','border-radius': '5px', 'height': '40px','background-color':'rgb(235, 236, 236)', 'min-width': (context.customLayouts[${index}].grids[${i}][${j}].WIDTH == 75) ? '73.08%' : (context.customLayouts[${index}].grids[${i}][${j}].WIDTH == 50) ? '48.5%' : (context.customLayouts[${index}].grids[${i}][${j}].WIDTH == 25) ? '24.06%' : (context.customLayouts[${index}].grids[${i}][${j}].WIDTH == 100) ? '97.5%' : '0%'}">
	  <div fxFlex=90 *ngIf="context.showView.CELL_${name}_${i}_${j} ==='LABEL'"
      class="mat-body" style="padding: 10px;">
          <span>{{context.fieldsMap['${fieldId}'].DISPLAY_LABEL}}</span>
	  </div>

      <div fxFlex=90 *ngIf="context.showView.CELL_${name}_${i}_${j} ==='EDIT'">
      <mat-label style="padding-left:10px"> Width : </mat-label>
      <mat-slider (change)="context.resizeField('${name}',${i},${j},$event)"
      color='primary'
      thumbLabel
      [min]="context.determineMinSliderValue('${name}',${i},${j})"
      [max]="context.determineMaxSliderValue('${name}',${i},${j})"
      [step]="context.getStepValue('${name}',${i},${j})"
      [displayWith]="context.displayFn"
      [value]="context.determineSliderValue('${name}',${i},${j})">
	  </mat-slider>
	  </div>

	  <div class="pointer" fxFlex fxLayoutAlign="end center">
	  <!-- hide field Settings for file preview datatype -->
	  <span *ngIf="context.fieldsMap['${fieldId}'].DATA_TYPE.DISPLAY!=='Discussion' && context.fieldsMap['${fieldId}'].DATA_TYPE.DISPLAY!=='List Formula' && context.fieldsMap['${fieldId}'].DATA_TYPE.DISPLAY!=='File Preview' "
	  (click)="context.openSettings(context.customLayouts[${index}].grids[${i}][${j}])"
	  >
	  <mat-icon class="grey-black-color" matTooltip="{{'FIELD_SETTINGS'|translate}}" fontSet="material-icons-outlined">
	  <img src="../../assets/icons/gear_icon.svg" style="width:15px;height:15px;"></mat-icon>
      </span>
		  <span (click)="context.toggleView('${name}',${i},${j})"><mat-icon class="layout-icons mat-caption"
		   class="grey-black-color" fontSet="material-icons-outlined" matTooltip="{{'RESIZE'|translate}}">
		   <img src="../../assets/icons/pencil_icon.svg" style="width:15px;height:15px;"></mat-icon></span>
		  <span (click)="context.removeField('${name}',${i},${j})">
		  <mat-icon class="layout-icons" class="grey-black-color" fontSet="material-icons-outlined" matTooltip="{{'REMOVE'|translate}}">
		  <div style="padding-right:5px">
		  <img src="../../assets/icons/delete_icon.svg" style="width:15px;height:15px;"> </div></mat-icon> </span>
         </div>
      </div>
       <!--END_CELL_${name}_${i}_${j}-->`;
	}

	// TEMPLATE AFTER DROP FOR ADDRESS FIELD
	public replaceCellAddressTemplate(grids, name, i, j) {
		const index =
			this.customLayouts.findIndex((f) => f.name === name) === -1
				? this.globalIndex
				: this.customLayouts.findIndex((f) => f.name === name);
		this.cellFlexSize = 25;
		const fieldId = grids[i][j].FIELD_ID;
		const displayLabel = this.fieldsMap[fieldId].DISPLAY_LABEL;
		return `<div class='CELL_${name}_${i}_${j} mat-caption'
      fxLayout="row" fxLayoutAlign="center center" fxFlex="{{context.customLayouts[${index}].grids[${i}][${j}].WIDTH}}"
	  [ngStyle]="{ 'border': '1px solid #ccc','border-radius': '5px', 'height': '40px'}">
	  <div fxFlex=90 *ngIf="context.showView.CELL_${name}_${i}_${j} ==='LABEL'"
      class="mat-body" style="padding: 10px;">
          <span>{{context.fieldsMap['${fieldId}'].DISPLAY_LABEL}}</span>
	  </div>

      <div fxFlex=90 *ngIf="context.showView.CELL_${name}_${i}_${j} ==='EDIT'">
      <mat-label style="padding-left:10px"> Width : </mat-label>
      <mat-slider (change)="context.resizeField('${name}',${i},${j},$event)"
      color='primary'
      thumbLabel
      [min]="context.determineMinSliderValue('${name}',${i},${j})"
      [max]="context.determineMaxSliderValue('${name}',${i},${j})"
      [step]="context.getStepValue('${name}',${i},${j})"
      [displayWith]="context.displayFn"
      [value]="context.determineSliderValue('${name}',${i},${j})">
	  </mat-slider>
	  </div>

      <div class="pointer" fxFlex fxLayoutAlign="end center">
		  <span (click)="context.removeField('${name}',${i},${j})">
		  <mat-icon class="layout-icons" class="grey-balck-color" fontSet="material-icons-outlined" matTooltip="Remove">close</mat-icon></span>
      </div>
  </div>
  <!--END_CELL_${name}_${i}_${j}-->`;
	}

	public dropField(name: string, event: CdkDragDrop<string[]>) {
		// GETTING FIELD ID FROM THE ELEMENT
		const custom = this.customLayouts.find((f) => f.name === name);
		const fieldId = event.item.element.nativeElement.id;

		// POSITION FROM THE ELEMENT
		const droppedIndex = event.container.element.nativeElement.id.substr(
			event.container.element.nativeElement.id.indexOf('_', 6) + 1
		);
		let xPos = parseInt(droppedIndex.split('_')[0], 10);
		let yPos = parseInt(droppedIndex.split('_')[1], 10);

		// FIND THE FIELD
		const field = this.module.FIELDS.find(
			(element) => element.FIELD_ID === fieldId
		);

		const cellRegex = new RegExp(
			`<div class='CELL_${name}_${xPos}_${yPos}([\\s\\S]*?)<!--END_CELL_${name}_${xPos}_${yPos}-->`
		);

		if (
			field.DATA_TYPE.DISPLAY !== 'Discussion' &&
			field.DATA_TYPE.DISPLAY !== 'Image' &&
			field.DATA_TYPE.DISPLAY !== 'File Preview' &&
			field.DATA_TYPE.DISPLAY !== 'Condition' &&
			field.DATA_TYPE.DISPLAY !== 'Receipt Capture' &&
			field.DATA_TYPE.DISPLAY !== 'List Formula'
		) {
			// REMOVE THE FIELD FROM THE LIST

			const groupId = field.GROUP_ID;

			// SET THE GRID WITH THE FIELD

			if (custom.fields.map((v) => v.FIELD_ID).indexOf(fieldId) === -1) {
				if (
					field.GROUP_ID != null &&
					(field.DATA_TYPE.DISPLAY === 'Street 1' ||
						field.DATA_TYPE.DISPLAY === 'Street 2' ||
						field.DATA_TYPE.DISPLAY === 'City' ||
						field.DATA_TYPE.DISPLAY === 'State' ||
						field.DATA_TYPE.DISPLAY === 'Zipcode')
				) {
					const droppable = [];

					for (let row = xPos; row < xPos + 3; row++) {
						for (let col = 0; col < 4; col++) {
							if (custom.grids[row][col].IS_EMPTY) {
								droppable.push(true);
							} else {
								droppable.push(false);
							}
						}
						if (
							xPos === custom.grids.length - 2 ||
							xPos === custom.grids.length - 1
						) {
							break;
						}
					}

					if (droppable.includes(false)) {
						this.bannerMessageService.errorNotifications.push({
							message: this.translateService.instant('ADDRESSFIELD_ON_NEW_ROW'),
						});
					} else {
						this.module.FIELDS = this.module.FIELDS.filter(
							(element) => element.FIELD_ID !== fieldId
						);
						const xp = xPos;
						yPos = 0;

						const lastRow = custom.grids.length - 1;
						if (xPos === lastRow) {
							for (let row = lastRow; row < lastRow + 3; row++) {
								this.gridRow(name, row, 1);
							}
						} else if (xPos === lastRow - 1) {
							for (let row = lastRow - 1; row < lastRow + 1; row++) {
								this.gridRow(name, row, 1);
							}
						}

						custom.grids[xPos][yPos].IS_EMPTY = false;
						custom.grids[xPos][yPos].FIELD_ID = fieldId;
						this.fields.forEach((eachField) => {
							if (
								eachField.GROUP_ID !== null &&
								eachField.GROUP_ID === field.GROUP_ID
							) {
								if (xPos > xp + 2) {
									xPos = xp + 2;
									yPos = yPos + 1;
								}

								this.module.FIELDS = this.module.FIELDS.filter(
									(element) => element.FIELD_ID !== eachField.FIELD_ID
								);

								custom.grids[xPos][yPos].IS_EMPTY = false;
								custom.grids[xPos][yPos].FIELD_ID = eachField.FIELD_ID;
								const cellRegex = new RegExp(
									`<div class='CELL_${name}_${xPos}_${yPos}([\\s\\S]*?)<!--END_CELL_${name}_${xPos}_${yPos}-->`
								);
								custom.fields.push(eachField);
								custom.customLayout = custom.customLayout.replace(
									cellRegex,
									this.replaceCellAddressTemplate(
										custom.grids,
										name,
										xPos,
										yPos
									)
								);
								if (xPos === xp || xPos === xp + 1) {
									if (100 > custom.grids[xPos][yPos].WIDTH) {
										this.removeCells(name, xPos, yPos + 1, 100);
									} else {
										let maxGrid = yPos;
										for (let y = yPos + 1; y < 4; y++) {
											if (
												!custom.grids[xPos][y].IS_EMPTY ||
												custom.grids[xPos][y].WIDTH !== 0
											) {
												break;
											}
											maxGrid = y;
										}
										this.addCells(
											name,
											xPos,
											maxGrid,
											custom.grids[xPos][yPos].WIDTH - 100
										);
									}
									custom.grids[xPos][yPos].WIDTH = 100;
								}

								xPos = xPos + 1;
							}
						});
					}
				} else {
					this.module.FIELDS = this.module.FIELDS.filter(
						(element) => element.FIELD_ID !== fieldId
					);
					custom.grids[xPos][yPos].IS_EMPTY = false;
					custom.grids[xPos][yPos].FIELD_ID = fieldId;
					custom.fields.push(field);
					custom.customLayout = custom.customLayout.replace(
						cellRegex,
						this.replaceCellTemplate(custom.grids, name, xPos, yPos)
					);
				}
			}
		} else if (
			field.DATA_TYPE.DISPLAY === 'Discussion' &&
			this.validateDiscussionPosition(name, xPos, yPos, 2) &&
			!custom.filePreviewDropped
		) {
			this.module.FIELDS = this.module.FIELDS.filter(
				(element) => element.FIELD_ID !== fieldId
			);
			custom.discussionPosition.xPos = xPos;
			custom.discussionPosition.yPos = yPos;
			custom.grids[xPos][yPos] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: fieldId,
			};
			this.loadLayoutWithDiscussionSection(name, custom.discussionPosition, 2);
		} else if (
			field.DATA_TYPE.DISPLAY === 'Image' &&
			this.validateImagePosition(name, xPos, yPos, 2)
		) {
			this.module.FIELDS = this.module.FIELDS.filter(
				(element) => element.FIELD_ID !== fieldId
			);
			custom.imagePosition.xPos = xPos;
			custom.imagePosition.yPos = yPos;
			custom.grids[xPos][yPos] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: fieldId,
			};
			this.loadLayoutWithImageSection(name, custom.imagePosition, 2);
		} else if (
			field.DATA_TYPE.DISPLAY === 'File Preview' &&
			this.validateFilePreviewPosition(name, xPos, yPos, 2) &&
			!custom.discussionDropped
		) {
			this.module.FIELDS = this.module.FIELDS.filter(
				(element) => element.FIELD_ID !== fieldId
			);
			custom.filePreviewPosition.xPos = xPos;
			custom.filePreviewPosition.yPos = yPos;
			custom.grids[xPos][yPos] = {
				IS_EMPTY: false,
				HEIGHT: 10,
				WIDTH: 100,
				FIELD_ID: fieldId,
			};
			this.loadLayoutWithfilePreviewSection(
				name,
				custom.filePreviewPosition,
				2
			);
		} else if (
			field.DATA_TYPE.DISPLAY == 'Condition' &&
			this.validateConditionFieldPosition(name, xPos) &&
			!custom.conditionDropped
		) {
			this.module.FIELDS = this.module.FIELDS.filter(
				(element) => element.FIELD_ID !== fieldId
			);
			custom.conditionPosition.xPos = xPos;
			custom.conditionPosition.yPos = 0;
			custom.grids[xPos][0] = {
				IS_EMPTY: false,
				HEIGHT: 10,
				WIDTH: 100,
				FIELD_ID: fieldId,
			};
			this.loadLayoutWithConditionRow(name, custom.conditionPosition, 4);
		} else if (
			field.DATA_TYPE.DISPLAY === 'Receipt Capture' &&
			this.validateReceiptPosition(name, xPos, yPos, 2)
		) {
			this.module.FIELDS = this.module.FIELDS.filter(
				(element) => element.FIELD_ID !== fieldId
			);
			custom.receiptPosition.xPos = xPos;
			custom.receiptPosition.yPos = yPos;
			custom.grids[xPos][yPos] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: fieldId,
			};
			this.loadLayoutWithReceiptSection(name, custom.receiptPosition, 2);
		} else if (
			field.DATA_TYPE.DISPLAY === 'List Formula' &&
			this.validateListFormulaPosition(name, xPos, yPos, 2)
		) {
			this.module.FIELDS = this.module.FIELDS.filter(
				(element) => element.FIELD_ID !== fieldId
			);
			custom.listFormulaPosition.xPos = xPos;
			custom.listFormulaPosition.yPos = yPos;
			custom.grids[xPos][yPos] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: fieldId,
			};
			this.loadLayoutWithListFormulaSection(
				name,
				custom.listFormulaPosition,
				2
			);
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('FIELD_CANNOT_BE_DROPPED'),
			});
		}
	}

	public validateDiscussionPosition(name, i, j, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (!custom.discussionDropped) {
			if (i < 12 && j !== 3) {
				for (let x = i; x < custom.grids.length; x++) {
					for (let y = j; y < j + size; y++) {
						if (!custom.grids[x][y].IS_EMPTY) {
							return false;
						}
					}
				}
			} else {
				return false;
			}
		} else {
			for (let x = i; x < custom.grids.length; x++) {
				if (!custom.grids[x][j].IS_EMPTY) {
					return false;
				}
			}
		}
		return true;
	}

	// checking Image Drop possition

	public validateImagePosition(name, i, j, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (!custom.imageDropped) {
			if (i < 12 && j !== 3) {
				for (let x = i; x < custom.grids.length; x++) {
					for (let y = j; y < j + size; y++) {
						if (!custom.grids[x][y].IS_EMPTY) {
							return false;
						}
					}
				}
			} else {
				return false;
			}
		} else {
			for (let x = i; x < custom.grids.length; x++) {
				if (!custom.grids[x][j].IS_EMPTY) {
					return false;
				}
			}
		}
		return true;
	}
	// This function is to file preview drag and drop to proper position.
	public validateFilePreviewPosition(name, i, j, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (!custom.filePreviewDropped) {
			if (i < 12 && j !== 3) {
				for (let x = i; x < custom.grids.length; x++) {
					for (let y = j; y < j + size; y++) {
						if (!custom.grids[x][y].IS_EMPTY) {
							return false;
						}
					}
				}
			} else {
				return false;
			}
		} else {
			for (let x = i; x < custom.grids.length; x++) {
				if (!custom.grids[x][j].IS_EMPTY) {
					return false;
				}
			}
		}
		return true;
	}

	// TEMPLATE ONLY FOR DISCUSSION SECTION
	public buildTemplate(name, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const xPos = custom.discussionPosition.xPos;
		const yPos = custom.discussionPosition.yPos;
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const discussionInitialSize = custom.discussionPosition.size;
		const field = custom.grids[xPos][yPos].FIELD_ID;
		const settings = custom.grids[xPos][yPos].settings;

		if (discussionInitialSize === 3) {
			for (let x = xPos; x < custom.grids.length; x++) {
				for (let y = yPos; y < yPos + 3; y++) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: custom.grids.length,
						FIELD_ID: '',
						settings: null,
					};
				}
			}
		}

		if (size === 2) {
			custom.discussionPosition.size = 2;
			flex = 50;
		} else if (size === 3) {
			custom.discussionPosition.size = 3;
			flex = 75;
		} else if (size === 4) {
			custom.discussionPosition.size = 4;
			flex = 100;
			custom.grids[xPos][0] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: field,
				settings: settings,
			};
		}
		if (size !== 4) {
			for (let x = xPos; x < custom.grids.length; x++) {
				for (let y = yPos; y < yPos + size; y++) {
					custom.grids[x][y] = {
						IS_EMPTY: false,
						HEIGHT: custom.grids.length,
						WIDTH: 100,
						FIELD_ID: field,
						settings: settings,
					};
				}
			}
		}

		let row1 = '';
		let row2 = '';
		for (let x = xPos; x < custom.grids.length; x++) {
			if (yPos === 1 && size === 2) {
				row1 = row1 + this.buildRowForDiscussion(name, 3, x, yPos);
				row2 = row2 + this.buildRowForDiscussion(name, 3, x, 0);
			} else {
				rows = rows + this.buildRowForDiscussion(name, size, x, yPos);
			}
		}
		const flexRegex = new RegExp('COLUMN_FLEX');
		const rowsRegex = new RegExp('ADD_ROWS_FOR_THIS_COLUMN');
		let columnWithRows = columnTemplate.replace(rowsRegex, rows);
		columnWithRows = columnWithRows.replace(flexRegex, (100 - flex).toString());

		let columnWithDiscussion = columnTemplate.replace(
			flexRegex,
			flex.toString()
		);
		columnWithDiscussion = columnWithDiscussion.replace(
			rowsRegex,
			this.replaceCellTemplate(custom.grids, name, xPos, yPos)
		);
		let mainTemplate = `<div class='DISCUSSION_SECTION' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			// FIRST COLUMN
			mainTemplate = mainTemplate + columnWithDiscussion + columnWithRows;
		} else if (yPos === 1 && size === 2) {
			// MIDDLE
			let column1WithRows1 = columnTemplate.replace(rowsRegex, row1);
			column1WithRows1 = column1WithRows1.replace(
				flexRegex,
				(flex / 2).toString()
			);
			let column2WithRows2 = columnTemplate.replace(rowsRegex, row2);
			column2WithRows2 = column2WithRows2.replace(
				flexRegex,
				(flex / 2).toString()
			);
			mainTemplate =
				mainTemplate +
				column1WithRows1 +
				columnWithDiscussion +
				column2WithRows2;
		} else {
			// END
			mainTemplate = mainTemplate + columnWithRows + columnWithDiscussion;
		}
		mainTemplate = mainTemplate + `</div><!--END_DISCUSSION_SECTION-->`;
		return mainTemplate;
	}

	public buildTemplateForImage(name, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const xPos = custom.imagePosition.xPos;
		const yPos = custom.imagePosition.yPos;
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const ImageInitialSize = custom.imagePosition.size;
		const field = custom.grids[xPos][yPos].FIELD_ID;
		const settings = custom.grids[xPos][yPos].settings;

		if (ImageInitialSize === 3) {
			for (let x = xPos; x < custom.grids.length; x++) {
				for (let y = yPos; y < yPos + 3; y++) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: custom.grids.length,
						FIELD_ID: '',
						settings: null,
					};
				}
			}
		}

		if (size === 2) {
			custom.imagePosition.size = 2;
			flex = 50;
		} else if (size === 3) {
			custom.imagePosition.size = 3;
			flex = 75;
		} else if (size === 4) {
			custom.imagePosition.size = 4;
			flex = 100;
			custom.grids[xPos][0] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: field,
				settings: settings,
			};
		}
		if (size !== 4) {
			for (let x = xPos; x < custom.grids.length; x++) {
				for (let y = yPos; y < yPos + size; y++) {
					custom.grids[x][y] = {
						IS_EMPTY: false,
						HEIGHT: custom.grids.length,
						WIDTH: 100,
						FIELD_ID: field,
						settings: settings,
					};
				}
			}
		}

		let row1 = '';
		let row2 = '';
		for (let x = xPos; x < custom.grids.length; x++) {
			if (yPos === 1 && size === 2) {
				row1 = row1 + this.buildRowForDiscussion(name, 3, x, yPos);
				row2 = row2 + this.buildRowForDiscussion(name, 3, x, 0);
			} else {
				rows = rows + this.buildRowForDiscussion(name, size, x, yPos);
			}
		}
		const flexRegex = new RegExp('COLUMN_FLEX');
		const rowsRegex = new RegExp('ADD_ROWS_FOR_THIS_COLUMN');
		let columnWithRows = columnTemplate.replace(rowsRegex, rows);
		columnWithRows = columnWithRows.replace(flexRegex, (100 - flex).toString());

		let columnWithImage = columnTemplate.replace(flexRegex, flex.toString());
		columnWithImage = columnWithImage.replace(
			rowsRegex,
			this.replaceCellTemplate(custom.grids, name, xPos, yPos)
		);
		let mainTemplate = `<div class='IMAGE_SECTION' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			// FIRST COLUMN
			mainTemplate = mainTemplate + columnWithImage + columnWithRows;
		} else if (yPos === 1 && size === 2) {
			// MIDDLE
			let column1WithRows1 = columnTemplate.replace(rowsRegex, row1);
			column1WithRows1 = column1WithRows1.replace(
				flexRegex,
				(flex / 2).toString()
			);
			let column2WithRows2 = columnTemplate.replace(rowsRegex, row2);
			column2WithRows2 = column2WithRows2.replace(
				flexRegex,
				(flex / 2).toString()
			);
			mainTemplate =
				mainTemplate + column1WithRows1 + columnWithImage + column2WithRows2;
		} else {
			// END
			mainTemplate = mainTemplate + columnWithRows + columnWithImage;
		}
		mainTemplate = mainTemplate + `</div><!--END_IMAGE_SECTION-->`;
		return mainTemplate;
	}

	// GET THE ROWS FOR DISCUSSION SECTION
	public buildRowForDiscussion(name, size, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		let row = `<div class='ROW_${i}' fxLayoutGap=5px fxLayout=row fxFlex>`;
		for (let y = 0; y < 4; y++) {
			if (size === 2 && j !== 1) {
				if (y !== j && y !== j + 1) {
					custom.grids[i][y].WIDTH = 50;
					if (custom.grids[i][y].IS_EMPTY) {
						row = row + this.initialTemplate(custom.grids, name, i, y);
					} else {
						row = row + this.replaceCellTemplate(custom.grids, name, i, y);
					}
				}
			} else if (size === 3) {
				if (y === (j - 1 < 0 ? 3 : j - 1)) {
					custom.grids[i][y].WIDTH = 100;
					if (custom.grids[i][y].IS_EMPTY) {
						row = row + this.initialTemplate(custom.grids, name, i, y);
					} else {
						row = row + this.replaceCellTemplate(custom.grids, name, i, y);
					}
				}
			}
		}
		if (j >= 1) {
			row += `</div><!--END_ROW_${i}-->`;
		} else {
			row += `<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
			(click)="context.gridRow('${name}', ${i}, -1)">close</mat-icon></div></div><!--END_ROW_${i}-->`;
		}
		return row;
	}

	// GET THE ROWS FOR FILE PREVIEW SECTION
	public buildRowForFilePreview(name, size, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		let row = `<div class='ROW_${i}' fxLayoutGap=5px fxLayout=row fxFlex>`;
		for (let y = 0; y < 4; y++) {
			if (size === 2 && j !== 1) {
				if (y !== j && y !== j + 1) {
					custom.grids[i][y].WIDTH = 50;
					if (custom.grids[i][y].IS_EMPTY) {
						row = row + this.initialTemplate(custom.grids, name, i, y);
					} else {
						row = row + this.replaceCellTemplate(custom.grids, name, i, y);
					}
				}
			} else if (size === 3) {
				if (y === (j - 1 < 0 ? 3 : j - 1)) {
					custom.grids[i][y].WIDTH = 100;
					if (custom.grids[i][y].IS_EMPTY) {
						row = row + this.initialTemplate(custom.grids, name, i, y);
					} else {
						row = row + this.replaceCellTemplate(custom.grids, name, i, y);
					}
				}
			}
		}
		if (j >= 1) {
			row += `</div><!--END_ROW_${i}-->`;
		} else {
			row += `<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
			(click)="context.gridRow('${name}', ${i}, -1)">close</mat-icon></div></div><!--END_ROW_${i}-->`;
		}
		return row;
	}

	// LOAD DISCUSSION SECTION
	public loadLayoutWithDiscussionSection(name, discussionPosition, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const removeDivFrom = discussionPosition.xPos + 1;
		const removeDivTo = custom.grids.length - 1;

		custom.customLayout = custom.customLayout.replace(
			new RegExp(
				`<div class='ROW_${removeDivFrom}([\\s\\S]*?)<!--END_ROW_${removeDivTo}-->`
			),
			''
		);
		if (!custom.discussionDropped) {
			const rowRegex = new RegExp(
				`<div class='ROW_${discussionPosition.xPos}([\\s\\S]*?)<!--END_ROW_${discussionPosition.xPos}-->`
			);
			custom.customLayout = custom.customLayout.replace(
				rowRegex,
				this.buildTemplate(name, size)
			);
		} else {
			const discussionRegex = new RegExp(
				`<div class='DISCUSSION_SECTION([\\s\\S]*?)<!--END_DISCUSSION_SECTION-->`
			);
			custom.customLayout = custom.customLayout.replace(
				discussionRegex,
				this.buildTemplate(name, size)
			);
		}
		custom.discussionDropped = true;
	}

	//load Image section
	public loadLayoutWithImageSection(name, imagePosition, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const removeDivFrom = imagePosition.xPos + 1;
		const removeDivTo = custom.grids.length - 1;

		custom.customLayout = custom.customLayout.replace(
			new RegExp(
				`<div class='ROW_${removeDivFrom}([\\s\\S]*?)<!--END_ROW_${removeDivTo}-->`
			),
			''
		);
		if (!custom.imageDropped) {
			const rowRegex = new RegExp(
				`<div class='ROW_${imagePosition.xPos}([\\s\\S]*?)<!--END_ROW_${imagePosition.xPos}-->`
			);
			custom.customLayout = custom.customLayout.replace(
				rowRegex,
				this.buildTemplateForImage(name, size)
			);
		} else {
			const imageRegex = new RegExp(
				`<div class='IMAGE_SECTION([\\s\\S]*?)<!--END_IMAGE_SECTION-->`
			);
			custom.customLayout = custom.customLayout.replace(
				imageRegex,
				this.buildTemplateForImage(name, size)
			);
		}
		custom.imageDropped = true;
	}

	// REMOVES FIELDS AND ADD THE FIELD BACK TO MODULE FIELDS
	public removeField(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const arr: any = [];
		let dataType: any;
		const removedField = this.fieldsMap[custom.grids[i][j].FIELD_ID];
		this.fields.forEach((field) => {
			if (field.FIELD_ID === custom.grids[i][j].FIELD_ID) {
				dataType = field.DATA_TYPE;
			}
		});
		if (
			removedField.GROUP_ID != null &&
			(dataType.DISPLAY === 'Street 1' ||
				dataType.DISPLAY === 'Street 2' ||
				dataType.DISPLAY === 'City' ||
				dataType.DISPLAY === 'State' ||
				dataType.DISPLAY === 'Zipcode')
		) {
			let groupId: any;
			for (let row = 0; row < custom.grids.length; row++) {
				for (let col = 0; col < 4; col++) {
					this.fields.forEach((field) => {
						if (field.FIELD_ID === custom.grids[row][col].FIELD_ID) {
							groupId = field.GROUP_ID;
						}
					});
				}
				arr.push(row);
			}
			custom.fields.forEach((field) => {
				if (field.GROUP_ID === removedField.GROUP_ID) {
					this.module.FIELDS.unshift(field);
				}
			});
			const currentLoop = 1;
			let maxGrid = 3;
			let index = 0;
			for (let x = 0; x < custom.grids.length; x++) {
				for (let y = 0; y < 4; y++) {
					let displayType: any;
					if (!custom.grids[x][y].IS_EMPTY) {
						this.fields.forEach((field) => {
							if (field.FIELD_ID === custom.grids[x][y].FIELD_ID) {
								groupId = field.GROUP_ID;
								displayType = field.DATA_TYPE.DISPLAY;
							}
						});
						if (groupId === removedField.GROUP_ID) {
							custom.grids[x][y].IS_EMPTY = true;
							custom.fields = custom.fields.filter(
								(f) => f.FIELD_ID !== custom.grids[x][y].FIELD_ID
							);

							if (displayType === 'Street 1' || displayType === 'Street 2') {
								maxGrid = 3;
							} else if (displayType === 'City') {
								maxGrid = 0;
							} else if (displayType === 'State') {
								maxGrid = 1;
							} else if (displayType === 'Zipcode') {
								maxGrid = 2;
							}
							this.addCells(name, x, maxGrid, custom.grids[x][y].WIDTH);
						}
					}
				}
				index++;
			}
		} else {
			this.module.FIELDS.unshift(removedField);
			custom.fields = custom.fields.filter(
				(f) => f.FIELD_ID !== custom.grids[i][j].FIELD_ID
			);

			custom.grids[i][j].IS_EMPTY = true;
			if (removedField.DATA_TYPE.DISPLAY === 'Discussion') {
				this.onDeleteDiscussion(name, i, j);
			} else if (removedField.DATA_TYPE.DISPLAY === 'Image') {
				this.onDeleteImageField(name, i, j);
			} else if (removedField.DATA_TYPE.DISPLAY === 'File Preview') {
				this.onDeleteFilePreview(name, i, j);
			} else if (removedField.DATA_TYPE.DISPLAY === 'Condition') {
				this.onDeleteCondition(name, i, j);
			} else if (removedField.DATA_TYPE.DISPLAY === 'Receipt Capture') {
				this.onDeleteReceiptCaptureField(name, i, j);
			} else if (removedField.DATA_TYPE.DISPLAY === 'List Formula') {
				this.onDeleteListFormulaField(name, i, j);
			} else {
				let maxGrid = j;
				for (let y = j + 1; y < 4; y++) {
					if (!custom.grids[i][y].IS_EMPTY || custom.grids[i][y].WIDTH !== 0) {
						break;
					}
					maxGrid = y;
				}
				this.addCells(name, i, maxGrid, custom.grids[i][j].WIDTH);
			}
		}
	}

	public onDeleteDiscussion(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const discussionRegex = new RegExp(
			`<div class='DISCUSSION_SECTION([\\s\\S]*?)<!--END_DISCUSSION_SECTION-->`
		);
		let layout = '';
		for (let x = i; x < custom.grids.length; x++) {
			layout = layout + `<div class='ROW_${x}' fxLayout="row" fxLayoutGap=5px>`;
			for (let y = 0; y < 4; y++) {
				if (custom.grids[x][y].IS_EMPTY) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: 25,
						FIELD_ID: '',
						settings: null,
					};
					layout = layout + this.initialTemplate(custom.grids, name, x, y);
				} else {
					if (
						this.fieldsMap[custom.grids[x][y].FIELD_ID].DATA_TYPE.DISPLAY ===
						'Discussion'
					) {
						custom.grids[x][y] = {
							IS_EMPTY: true,
							HEIGHT: 10,
							WIDTH: 25,
							FIELD_ID: '',
							settings: null,
						};
						layout = layout + this.initialTemplate(custom.grids, name, x, y);
					} else {
						layout =
							layout + this.replaceCellTemplate(custom.grids, name, x, y);
					}
				}
			}
			layout =
				layout +
				`<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
			(click)="context.gridRow('${name}', ${x}, -1)">close</mat-icon></div></div><!--END_ROW_${x}-->`;
		}
		custom.customLayout = custom.customLayout.replace(discussionRegex, layout);
		custom.discussionDropped = false;
	}

	public onDeleteImageField(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const imageRegex = new RegExp(
			`<div class='IMAGE_SECTION([\\s\\S]*?)<!--END_IMAGE_SECTION-->`
		);
		let layout = '';
		for (let x = i; x < custom.grids.length; x++) {
			layout = layout + `<div class='ROW_${x}' fxLayout="row" fxLayoutGap=5px>`;
			for (let y = 0; y < 4; y++) {
				if (custom.grids[x][y].IS_EMPTY) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: 25,
						FIELD_ID: '',
						settings: null,
					};
					layout = layout + this.initialTemplate(custom.grids, name, x, y);
				} else {
					if (
						this.fieldsMap[custom.grids[x][y].FIELD_ID].DATA_TYPE.DISPLAY ===
						'Image'
					) {
						custom.grids[x][y] = {
							IS_EMPTY: true,
							HEIGHT: 10,
							WIDTH: 25,
							FIELD_ID: '',
							settings: null,
						};
						layout = layout + this.initialTemplate(custom.grids, name, x, y);
					} else {
						layout =
							layout + this.replaceCellTemplate(custom.grids, name, x, y);
					}
				}
			}
			layout =
				layout +
				`<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
			(click)="context.gridRow('${name}', ${x}, -1)">close</mat-icon></div></div><!--END_ROW_${x}-->`;
		}
		custom.customLayout = custom.customLayout.replace(imageRegex, layout);
		custom.imageDropped = false;
	}

	// DISPLAY FOR SLIDER
	private displayFn(value) {
		return value + '%';
	}

	// SLIDER MIN VALUE
	public determineMinSliderValue(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (
			custom.discussionDropped &&
			custom.discussionPosition.xPos === i &&
			custom.discussionPosition.yPos === j
		) {
			return 50;
		} else if (
			custom.discussionDropped &&
			i >= custom.discussionPosition.xPos &&
			(custom.discussionPosition.yPos === 2 ||
				custom.discussionPosition.yPos === 0)
		) {
			return 50;
		} else if (
			custom.discussionDropped &&
			custom.discussionPosition.yPos === 1 &&
			i >= custom.discussionPosition.xPos
		) {
			return 100;
		} else if (
			custom.filePreviewDropped &&
			custom.filePreviewPosition.xPos === i &&
			custom.filePreviewPosition.yPos === j
		) {
			return 50;
		} else if (
			custom.filePreviewDropped &&
			i >= custom.filePreviewPosition.xPos &&
			(custom.filePreviewPosition.yPos === 2 ||
				custom.filePreviewPosition.yPos === 0)
		) {
			return 50;
		} else if (
			custom.filePreviewDropped &&
			custom.filePreviewPosition.yPos === 1 &&
			i >= custom.filePreviewPosition.xPos
		) {
			return 100;
		} else if (
			custom.imageDropped &&
			custom.imagePosition.xPos === i &&
			custom.imagePosition.yPos === j
		) {
			return 50;
		} else if (
			custom.imageDropped &&
			i >= custom.imagePosition.xPos &&
			(custom.imagePosition.yPos === 2 || custom.imagePosition.yPos === 0)
		) {
			return 50;
		} else if (
			custom.imageDropped &&
			custom.imagePosition.yPos === 1 &&
			i >= custom.imagePosition.xPos
		) {
			return 100;
		} else if (custom.conditionDropped) {
			return 100;
		} else if (
			custom.receiptDropped &&
			custom.receiptPosition.xPos === i &&
			custom.receiptPosition.yPos === j
		) {
			return 50;
		} else if (
			custom.receiptDropped &&
			i >= custom.receiptPosition.xPos &&
			(custom.receiptPosition.yPos === 2 || custom.receiptPosition.yPos === 0)
		) {
			return 50;
		} else if (
			custom.receiptDropped &&
			custom.receiptPosition.yPos === 1 &&
			i >= custom.receiptPosition.xPos
		) {
			return 100;
		} else if (
			custom.listFormulaDropped &&
			i >= custom.listFormulaPosition.xPos &&
			(custom.listFormulaPosition.yPos === 2 ||
				custom.listFormulaPosition.yPos === 0)
		) {
			return 50;
		} else if (
			custom.listFormulaDropped &&
			custom.listFormulaPosition.yPos === 1 &&
			i >= custom.listFormulaPosition.xPos
		) {
			return 100;
		}

		return 25;
	}

	// SLIDER STEP
	public getStepValue(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (
			custom.discussionDropped &&
			i >= custom.discussionPosition.xPos &&
			(custom.discussionPosition.yPos === 2 ||
				custom.discussionPosition.yPos === 0) &&
			custom.discussionPosition.yPos !== j
		) {
			return 50;
		} else if (
			custom.filePreviewDropped &&
			i >= custom.filePreviewPosition.xPos &&
			(custom.filePreviewPosition.yPos === 2 ||
				custom.filePreviewPosition.yPos === 0) &&
			custom.filePreviewPosition.yPos !== j
		) {
			return 50;
		} else if (
			custom.imageDropped &&
			i >= custom.imagePosition.xPos &&
			(custom.imagePosition.yPos === 2 || custom.imagePosition.yPos === 0) &&
			custom.imagePosition.yPos !== j
		) {
			return 50;
		} else if (
			custom.receiptDropped &&
			i >= custom.receiptPosition.xPos &&
			(custom.receiptPosition.yPos === 2 ||
				custom.receiptPosition.yPos === 0) &&
			custom.receiptPosition.yPos !== j
		) {
			return 50;
		} else if (
			custom.listFormulaDropped &&
			i >= custom.listFormulaPosition.xPos &&
			(custom.listFormulaPosition.yPos === 2 ||
				custom.listFormulaPosition.yPos === 0) &&
			custom.listFormulaPosition.yPos !== j
		) {
			return 50;
		} else if (custom.conditionDropped) {
			return 100;
		} else if (custom.listFormulaDropped) {
			return 100;
		}

		return 25;
	}

	// SLIDER VALUE
	public determineSliderValue(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (
			custom.discussionDropped &&
			custom.discussionPosition.xPos === i &&
			custom.discussionPosition.yPos === j
		) {
			return custom.discussionPosition.size * 25;
		} else if (
			custom.filePreviewDropped &&
			custom.filePreviewPosition.xPos === i &&
			custom.filePreviewPosition.yPos === j
		) {
			return custom.filePreviewPosition.size * 25;
		} else if (
			custom.imageDropped &&
			custom.imagePosition.xPos === i &&
			custom.imagePosition.yPos === j
		) {
			return custom.imagePosition.size * 25;
		} else if (
			custom.receiptDropped &&
			custom.receiptPosition.xPos === i &&
			custom.receiptPosition.yPos === j
		) {
			return custom.receiptPosition.size * 25;
		} else if (
			custom.listFormulaDropped &&
			custom.listFormulaPosition.xPos === i &&
			custom.listFormulaPosition.yPos === j
		) {
			return custom.listFormulaPosition.size * 25;
		} else if (custom.conditionDropped) {
			return 100;
		}

		return custom.grids[i][j].WIDTH;
	}

	// SLIDER MAX VALUE TO WHAT EVER IT CAN BE RESIZED TO
	public determineMaxSliderValue(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const xPosDiscussion = custom.discussionPosition.xPos;
		const yPosDiscussion = custom.discussionPosition.yPos;
		const xPosFilePreview = custom.filePreviewPosition.xPos;
		const yPosFilePreview = custom.filePreviewPosition.yPos;
		const xPosImage = custom.imagePosition.xPos;
		const yPosImage = custom.imagePosition.yPos;
		const xPosReceipt = custom.receiptPosition.xPos;
		const yPosReceipt = custom.receiptPosition.yPos;
		if (
			custom.discussionDropped &&
			i === xPosDiscussion &&
			j === yPosDiscussion
		) {
			return 75;
		}
		if (custom.discussionDropped && i >= xPosDiscussion) {
			return custom.grids[i][j].WIDTH * 2 > 100
				? 100
				: custom.grids[i][j].WIDTH * 2;
		}
		if (
			custom.filePreviewDropped &&
			i === xPosFilePreview &&
			j === yPosFilePreview
		) {
			return 75;
		}
		if (custom.filePreviewDropped && i >= xPosFilePreview) {
			return custom.grids[i][j].WIDTH * 2 > 100
				? 100
				: custom.grids[i][j].WIDTH * 2;
		}
		if (custom.imageDropped && i === xPosImage && j === yPosImage) {
			return 75;
		}
		if (custom.imageDropped && i >= xPosImage) {
			return custom.grids[i][j].WIDTH * 2 > 100
				? 100
				: custom.grids[i][j].WIDTH * 2;
		}
		if (custom.receiptDropped && i === xPosReceipt && j === yPosReceipt) {
			return 75;
		}
		if (custom.receiptDropped && i >= xPosReceipt) {
			return custom.grids[i][j].WIDTH * 2 > 100
				? 100
				: custom.grids[i][j].WIDTH * 2;
		}

		let max = custom.grids[i][j].WIDTH;
		for (let f = j + 1; f < 4; f++) {
			if (custom.grids[i][f].IS_EMPTY) {
				max = max + custom.grids[i][f].WIDTH;
			} else {
				break;
			}
		}
		return max;
	}

	// TO PROVIDE MENU FOR THE CELLS
	public toggleView(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (this.showView[`CELL_${name}_${i}_${j}`] === 'LABEL') {
			this.showView[`CELL_${name}_${i}_${j}`] = 'EDIT';
		} else {
			this.showView[`CELL_${name}_${i}_${j}`] = 'LABEL';
		}
	}

	// HANDLE RESIZE OF DISCUSSION (OTHER FIELDS ARE ALSO HANDLED ONLY IF SLIDER FAILS TO SET MIN AND MAX)
	public canBeResized(name, i, j, width) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const currentWidth = custom.grids[i][j].WIDTH;
		const extraWidth = Math.floor(width / currentWidth);
		if (
			this.fieldsMap[custom.grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY !==
			'Discussion'
		) {
			if (currentWidth < width) {
				for (let y = j + 1; y < j + 1 + extraWidth; y++) {
					if (!custom.grids[i][y].IS_EMPTY) {
						return false;
					}
					return true;
				}
			} else if (currentWidth > width) {
				return true;
			}
		} else if (
			this.fieldsMap[custom.grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY !==
			'File Preview'
		) {
			if (currentWidth < width) {
				for (let y = j + 1; y < j + 1 + extraWidth; y++) {
					if (!custom.grids[i][y].IS_EMPTY) {
						return false;
					}
					return true;
				}
			} else if (currentWidth > width) {
				return true;
			}
		} else {
			if (width === 75) {
				const extendingPosition = j - 1 + 3;
				if (
					extendingPosition <= 3 &&
					this.validateDiscussionPosition(
						name,
						custom.discussionPosition.xPos,
						extendingPosition,
						3
					)
				) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	public resizeField(name, i, j, event: MatSliderChange) {
		this.toggleView(name, i, j);
		const custom = this.customLayouts.find((f) => f.name === name);

		if (
			this.fieldsMap[custom.grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY ===
			'Condition'
		) {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('CANNOT_BE_RESIZED'),
			});
		} else {
			if (!this.canBeResized(name, i, j, event.value)) {
				this.bannerMessageService.errorNotifications.push({
					message:
						this.translateService.instant('CANNOT_BE_RESIZED') +
						event.value +
						'%',
				});
				return;
			}
			if (
				custom.discussionDropped &&
				custom.discussionPosition.xPos === i &&
				custom.discussionPosition.yPos === j
			) {
				if (custom.discussionPosition.size === 3) {
					this.loadLayoutWithDiscussionSection(
						name,
						custom.discussionPosition,
						2
					);
				} else {
					this.loadLayoutWithDiscussionSection(
						name,
						custom.discussionPosition,
						3
					);
				}
			} else if (
				custom.filePreviewDropped &&
				custom.filePreviewPosition.xPos === i &&
				custom.filePreviewPosition.yPos === j
			) {
				if (custom.filePreviewPosition.size === 3) {
					this.loadLayoutWithfilePreviewSection(
						name,
						custom.filePreviewPosition,
						2
					);
				} else {
					this.loadLayoutWithfilePreviewSection(
						name,
						custom.filePreviewPosition,
						3
					);
				}
			} else if (
				custom.imageDropped &&
				custom.imagePosition.xPos === i &&
				custom.imagePosition.yPos === j
			) {
				if (custom.imagePosition.size === 3) {
					this.loadLayoutWithImageSection(name, custom.imagePosition, 2);
				} else {
					this.loadLayoutWithImageSection(name, custom.imagePosition, 3);
				}
			} else if (
				custom.receiptDropped &&
				custom.receiptPosition.xPos === i &&
				custom.receiptPosition.yPos === j
			) {
				if (custom.imagePosition.size === 3) {
					this.loadLayoutWithReceiptSection(name, custom.receiptPosition, 2);
				} else {
					this.loadLayoutWithReceiptSection(name, custom.receiptPosition, 3);
				}
			} else if (
				custom.listFormulaDropped &&
				custom.listFormulaPosition.xPos === i &&
				custom.listFormulaPosition.yPos === j
			) {
				if (custom.imagePosition.size === 3) {
					this.loadLayoutWithListFormulaSection(
						name,
						custom.receiptPosition,
						2
					);
				} else {
					this.loadLayoutWithListFormulaSection(
						name,
						custom.receiptPosition,
						3
					);
				}
			} else {
				if (event.value > custom.grids[i][j].WIDTH) {
					this.removeCells(name, i, j + 1, event.value);
				} else {
					let maxGrid = j;
					for (let y = j + 1; y < 4; y++) {
						if (
							!custom.grids[i][y].IS_EMPTY ||
							custom.grids[i][y].WIDTH !== 0
						) {
							break;
						}
						maxGrid = y;
					}
					this.addCells(
						name,
						i,
						maxGrid,
						custom.grids[i][j].WIDTH - event.value
					);
				}
				custom.grids[i][j].WIDTH = event.value;
			}
		}
	}

	// REMOVE CELLS IN CASE OF RESIZE
	public removeCells(name, i, j, width) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (width > 25 && custom.grids[i][j] !== undefined) {
			custom.grids[i][j].WIDTH = 0;
			if (
				!custom.grids[i][j].IS_EMPTY &&
				this.fieldsMap[custom.grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY ===
					'Discussion'
			) {
				return this.removeCells(name, i, j + 1, width - 25);
			}
			if (
				!custom.grids[i][j].IS_EMPTY &&
				this.fieldsMap[custom.grids[i][j].FIELD_ID].DATA_TYPE.DISPLAY ===
					'File Preview'
			) {
				return this.removeCells(name, i, j + 1, width - 25);
			}
			const cellRegex = new RegExp(
				`<div class='CELL_${name}_${i}_${j}([\\s\\S]*?)<!--END_CELL_${name}_${i}_${j}-->`
			);
			custom.customLayout = custom.customLayout.replace(
				cellRegex,
				`<div class='CELL_${name}_${i}_${j}' *ngIf="${custom.grids[i][j].WIDTH} !== 0"></div><!--END_CELL_${name}_${i}_${j}-->`
			);
			return this.removeCells(name, i, j + 1, width - 25);
		} else {
			return;
		}
	}

	// GENERIC FUNCTION TO ADD THE CELLS ON DELETE
	public addCells(name, i, j, width) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (width >= this.determineMinSliderValue(name, i, j)) {
			const cellRegex = new RegExp(
				`<div class='CELL_${name}_${i}_${j}([\\s\\S]*?)<!--END_CELL_${name}_${i}_${j}-->`
			);
			custom.grids[i][j].WIDTH = this.determineMinSliderValue(name, i, j);
			custom.customLayout = custom.customLayout.replace(
				cellRegex,
				this.initialTemplate(custom.grids, name, i, j)
			);

			return this.addCells(
				name,
				i,
				j - 1,
				width - this.determineMinSliderValue(name, i, j)
			);
		} else {
			return;
		}
	}

	public getLayoutFields(type: string) {
		let fields = [];
		if (this.sections === null || this.sections === undefined) {
			return fields;
		}
		fields = this.sections.filter((element) => element.ID === type);
		if (fields.length === 0) {
			return fields;
		} else {
			return fields[0].FIELDS;
		}
	}

	public getHtmlContent(field) {
		return `<mat-form-field fxFlex floatLabel="always" >
		<input matInput type="text" placeholder="${field.DISPLAY_LABEL}"
			[(ngModel)]="context.entry.${field.NAME}"
			(ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}')"
			[required]="context.moduleFields['${field.NAME}'].REQUIRED">
		<mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
	  </mat-form-field>`;
	}

	public setRole() {
		this.currentRole = this.roles.find((role) => {
			return role.ROLE_ID === this.layout.role;
		});
	}

	public removeTitleBarField(field) {
		this.titleBarFields = this.titleBarFields.filter((fieldObject) => {
			return fieldObject.FIELD_ID !== field.FIELD_ID;
		});
	}

	public setDisplayType(currentLayout) {
		const panelName = currentLayout.name;
		const currentIndex = this.customLayouts.findIndex(
			(customLayout) => customLayout.name === panelName
		);
		if (currentLayout.displayType === 'Tab') {
			for (let i = 0; i <= currentIndex; i++) {
				this.customLayouts[i].displayType = 'Tab';
			}
		} else if (currentLayout.displayType === 'Panel') {
			for (let i = currentIndex; i < this.customLayouts.length; i++) {
				this.customLayouts[i].displayType = 'Panel';
			}
		}
	}

	public loadLayoutWithfilePreviewSection(name, filePreviewPosition, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const removeDivFrom = filePreviewPosition.xPos + 1;
		const removeDivTo = custom.grids.length - 1;

		custom.customLayout = custom.customLayout.replace(
			new RegExp(
				`<div class='ROW_${removeDivFrom}([\\s\\S]*?)<!--END_ROW_${removeDivTo}-->`
			),
			''
		);
		if (!custom.filePreviewDropped) {
			const rowRegex = new RegExp(
				`<div class='ROW_${filePreviewPosition.xPos}([\\s\\S]*?)<!--END_ROW_${filePreviewPosition.xPos}-->`
			);
			custom.customLayout = custom.customLayout.replace(
				rowRegex,
				this.buildTemplateForFilePreview(name, size)
			);
		} else {
			const filePreviewRegex = new RegExp(
				`<div class='FILE_PREVIEW_SECTION([\\s\\S]*?)<!--END_FILE_PREVIEW_SECTION-->`
			);
			custom.customLayout = custom.customLayout.replace(
				filePreviewRegex,
				this.buildTemplateForFilePreview(name, size)
			);
		}
		custom.filePreviewDropped = true;
	}

	// Template only for file preview field
	public buildTemplateForFilePreview(name, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const xPos = custom.filePreviewPosition.xPos;
		const yPos = custom.filePreviewPosition.yPos;
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const fieldPreviewInitialSize = custom.filePreviewPosition.size;
		const field = custom.grids[xPos][yPos].FIELD_ID;
		const settings = custom.grids[xPos][yPos].settings;

		if (fieldPreviewInitialSize === 3) {
			for (let x = xPos; x < custom.grids.length; x++) {
				for (let y = yPos; y < yPos + 3; y++) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: custom.grids.length,
						FIELD_ID: '',
						settings: null,
					};
				}
			}
		}

		if (size === 2) {
			custom.filePreviewPosition.size = 2;
			flex = 50;
		} else if (size === 3) {
			custom.filePreviewPosition.size = 3;
			flex = 75;
		} else if (size === 4) {
			custom.filePreviewPosition.size = 4;
			flex = 100;
			custom.grids[xPos][0] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: field,
				settings: settings,
			};
		}
		if (size !== 4) {
			for (let x = xPos; x < custom.grids.length; x++) {
				for (let y = yPos; y < yPos + size; y++) {
					custom.grids[x][y] = {
						IS_EMPTY: false,
						HEIGHT: custom.grids.length,
						WIDTH: 100,
						FIELD_ID: field,
						settings: settings,
					};
				}
			}
		}

		let row1 = '';
		let row2 = '';
		for (let x = xPos; x < custom.grids.length; x++) {
			if (yPos === 1 && size === 2) {
				row1 = row1 + this.buildRowForFilePreview(name, 3, x, yPos);
				row2 = row2 + this.buildRowForFilePreview(name, 3, x, 0);
			} else {
				rows = rows + this.buildRowForFilePreview(name, size, x, yPos);
			}
		}
		const flexRegex = new RegExp('COLUMN_FLEX');
		const rowsRegex = new RegExp('ADD_ROWS_FOR_THIS_COLUMN');
		let columnWithRows = columnTemplate.replace(rowsRegex, rows);
		columnWithRows = columnWithRows.replace(flexRegex, (100 - flex).toString());

		let columnWithFilePreview = columnTemplate.replace(
			flexRegex,
			flex.toString()
		);
		columnWithFilePreview = columnWithFilePreview.replace(
			rowsRegex,
			this.replaceCellTemplate(custom.grids, name, xPos, yPos)
		);
		let mainTemplate = `<div class='FILE_PREVIEW_SECTION' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			// FIRST COLUMN
			mainTemplate = mainTemplate + columnWithFilePreview + columnWithRows;
		} else if (yPos === 1 && size === 2) {
			// MIDDLE
			let column1WithRows1 = columnTemplate.replace(rowsRegex, row1);
			column1WithRows1 = column1WithRows1.replace(
				flexRegex,
				(flex / 2).toString()
			);
			let column2WithRows2 = columnTemplate.replace(rowsRegex, row2);
			column2WithRows2 = column2WithRows2.replace(
				flexRegex,
				(flex / 2).toString()
			);
			mainTemplate =
				mainTemplate +
				column1WithRows1 +
				columnWithFilePreview +
				column2WithRows2;
		} else {
			// END
			mainTemplate = mainTemplate + columnWithRows + columnWithFilePreview;
		}
		mainTemplate = mainTemplate + `</div><!--END_FILE_PREVIEW_SECTION-->`;
		return mainTemplate;
	}

	public onDeleteFilePreview(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const filePreviewRegex = new RegExp(
			`<div class='FILE_PREVIEW_SECTION([\\s\\S]*?)<!--END_FILE_PREVIEW_SECTION-->`
		);
		let layout = '';
		for (let x = i; x < custom.grids.length; x++) {
			layout = layout + `<div class='ROW_${x}' fxLayout="row" fxLayoutGap=5px>`;
			for (let y = 0; y < 4; y++) {
				if (custom.grids[x][y].IS_EMPTY) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: 25,
						FIELD_ID: '',
						settings: null,
					};
					layout = layout + this.initialTemplate(custom.grids, name, x, y);
				} else {
					if (
						this.fieldsMap[custom.grids[x][y].FIELD_ID].DATA_TYPE.DISPLAY ===
						'File Preview'
					) {
						custom.grids[x][y] = {
							IS_EMPTY: true,
							HEIGHT: 10,
							WIDTH: 25,
							FIELD_ID: '',
							settings: null,
						};
						layout = layout + this.initialTemplate(custom.grids, name, x, y);
					} else {
						layout =
							layout + this.replaceCellTemplate(custom.grids, name, x, y);
					}
				}
			}
			layout =
				layout +
				`<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
			(click)="context.gridRow('${name}', ${x}, -1)">close</mat-icon></div></div><!--END_ROW_${x}-->`;
		}
		custom.customLayout = custom.customLayout.replace(filePreviewRegex, layout);
		custom.filePreviewDropped = false;
	}

	//  Code to rener Condition field
	// To validate condition field
	public validateConditionFieldPosition(name, i) {
		const custom = this.customLayouts.find((f) => f.name === name);
		for (let y = 0; y < 4; y++) {
			if (!custom.grids[i][y].IS_EMPTY) {
				return false;
			}
		}

		return true;
	}

	// to add condition row to template

	public loadLayoutWithConditionRow(name, condtionPosition, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const rowRegex = new RegExp(
			`<div class='ROW_${condtionPosition.xPos}([\\s\\S]*?)<!--END_ROW_${condtionPosition.xPos}-->`
		);
		custom.customLayout = custom.customLayout.replace(
			rowRegex,
			this.buildTemplateForCondition(name, size)
		);

		custom.conditionDropped = true;
	}

	// template for condition
	public buildTemplateForCondition(name, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const xPos = custom.conditionPosition.xPos;
		const yPos = custom.conditionPosition.yPos;
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const field = custom.grids[xPos][yPos].FIELD_ID;
		const settings = custom.grids[xPos][yPos].settings;
		if (size === 4) {
			custom.conditionPosition.size = 4;
			flex = 100;
			custom.grids[xPos][0] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: field,
				settings: settings,
			};
		}
		rows = rows + this.buildRowForDiscussion(name, size, xPos, yPos);
		const flexRegex = new RegExp('COLUMN_FLEX');
		const rowsRegex = new RegExp('ADD_ROWS_FOR_THIS_COLUMN');
		let columnWithRows = columnTemplate.replace(rowsRegex, rows);
		columnWithRows = columnWithRows.replace(flexRegex, (100 - flex).toString());

		let columnWithCondition = columnTemplate.replace(
			flexRegex,
			flex.toString()
		);
		columnWithCondition = columnWithCondition.replace(
			rowsRegex,
			this.replaceCellTemplate(custom.grids, name, xPos, yPos)
		);
		let mainTemplate = `<div class='CONDITION_SECTION' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			// FIRST COLUMN
			mainTemplate = mainTemplate + columnWithCondition + columnWithRows;
		}
		mainTemplate = mainTemplate + `</div><!--END_CONDITION_SECTION-->`;
		return mainTemplate;
	}
	//called on  condition  row delete
	public onDeleteCondition(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const conditionRegex = new RegExp(
			`<div class='CONDITION_SECTION([\\s\\S]*?)<!--END_CONDITION_SECTION-->`
		);
		let layout = '';
		layout = layout + `<div class='ROW_${i}' fxLayout="row" fxLayoutGap=5px>`;
		for (let y = 0; y < 4; y++) {
			if (custom.grids[i][y].IS_EMPTY) {
				custom.grids[i][y] = {
					IS_EMPTY: true,
					HEIGHT: 10,
					WIDTH: 25,
					FIELD_ID: '',
					settings: null,
				};
				layout = layout + this.initialTemplate(custom.grids, name, i, y);
			} else {
				if (
					this.fieldsMap[custom.grids[i][y].FIELD_ID].DATA_TYPE.DISPLAY ===
					'Condition'
				) {
					custom.grids[i][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: 25,
						FIELD_ID: '',
						settings: null,
					};
					layout = layout + this.initialTemplate(custom.grids, name, i, y);
				} else {
					layout = layout + this.replaceCellTemplate(custom.grids, name, i, y);
				}
			}
		}
		layout =
			layout +
			`<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
			(click)="context.gridRow('${name}', ${i}, -1)">close</mat-icon></div></div><!--END_ROW_${i}-->`;
		custom.customLayout = custom.customLayout.replace(conditionRegex, layout);
		custom.conditionDropped = false;
	}

	// RECEIPT CAPTURE CODE
	public loadLayoutWithReceiptSection(name, receiptPosition, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const removeDivFrom = receiptPosition.xPos + 1;
		const removeDivTo = custom.grids.length - 1; //4 + receiptPosition.xPos; //custom.grids.length - 1;
		custom.customLayout = custom.customLayout.replace(
			new RegExp(
				`<div class='ROW_${removeDivFrom}([\\s\\S]*?)<!--END_ROW_${removeDivTo}-->`
			),
			''
		);
		if (!custom.receiptDropped) {
			const rowRegex = new RegExp(
				`<div class='ROW_${receiptPosition.xPos}([\\s\\S]*?)<!--END_ROW_${receiptPosition.xPos}-->`
			);
			custom.customLayout = custom.customLayout.replace(
				rowRegex,
				this.buildTemplateForReceiptField(name, size)
			);
		} else {
			const receiptRegex = new RegExp(
				`<div class='RECEIPT_CAPTURE_SECTION([\\s\\S]*?)<!--END_RECEIPT_CAPTURE_SECTION-->`
			);
			custom.customLayout = custom.customLayout.replace(
				receiptRegex,
				this.buildTemplateForReceiptField(name, size)
			);
		}
		custom.receiptDropped = true;
	}

	public buildTemplateForReceiptField(name, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const xPos = custom.receiptPosition.xPos;
		const yPos = custom.receiptPosition.yPos;
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const receiptFieldInitialSize = custom.receiptPosition.size;
		const field = custom.grids[xPos][yPos].FIELD_ID;
		const settings = custom.grids[xPos][yPos].settings;

		if (receiptFieldInitialSize === 3) {
			for (let x = xPos; x < custom.grids.length; x++) {
				if (x < custom.grids.length) {
					for (let y = yPos; y < yPos + 3; y++) {
						custom.grids[x][y] = {
							IS_EMPTY: true,
							HEIGHT: 10,
							WIDTH: custom.grids.length,
							FIELD_ID: '',
							settings: null,
						};
					}
				}
			}
		}

		if (size === 2) {
			custom.receiptPosition.size = 2;
			flex = 50;
		} else if (size === 3) {
			custom.receiptPosition.size = 3;
			flex = 75;
		} else if (size === 4) {
			custom.receiptPosition.size = 4;
			flex = 100;
			custom.grids[xPos][0] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: field,
				settings: settings,
			};
		}
		if (size !== 4) {
			for (let x = xPos; x < custom.grids.length; x++) {
				if (x < custom.grids.length) {
					for (let y = yPos; y < yPos + size; y++) {
						custom.grids[x][y] = {
							IS_EMPTY: false,
							HEIGHT: custom.grids.length,
							WIDTH: 100,
							FIELD_ID: field,
							settings: settings,
						};
					}
				}
			}
		}

		let row1 = '';
		let row2 = '';
		for (let x = xPos; x < custom.grids.length; x++) {
			if (yPos === 1 && size === 2) {
				row1 = row1 + this.buildRowForDiscussion(name, 3, x, yPos);
				row2 = row2 + this.buildRowForDiscussion(name, 3, x, 0);
			} else {
				rows = rows + this.buildRowForDiscussion(name, size, x, yPos);
			}
		}
		const flexRegex = new RegExp('COLUMN_FLEX');
		const rowsRegex = new RegExp('ADD_ROWS_FOR_THIS_COLUMN');
		let columnWithRows = columnTemplate.replace(rowsRegex, rows);
		columnWithRows = columnWithRows.replace(flexRegex, (100 - flex).toString());

		let columnWithReceipt = columnTemplate.replace(flexRegex, flex.toString());
		columnWithReceipt = columnWithReceipt.replace(
			rowsRegex,
			this.replaceCellTemplate(custom.grids, name, xPos, yPos)
		);
		let mainTemplate = `<div class='RECEIPT_CAPTURE_SECTION' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			// FIRST COLUMN
			mainTemplate = mainTemplate + columnWithReceipt + columnWithRows;
		} else if (yPos === 1 && size === 2) {
			// MIDDLE
			let column1WithRows1 = columnTemplate.replace(rowsRegex, row1);
			column1WithRows1 = column1WithRows1.replace(
				flexRegex,
				(flex / 2).toString()
			);
			let column2WithRows2 = columnTemplate.replace(rowsRegex, row2);
			column2WithRows2 = column2WithRows2.replace(
				flexRegex,
				(flex / 2).toString()
			);
			mainTemplate =
				mainTemplate + column1WithRows1 + columnWithReceipt + column2WithRows2;
		} else {
			// END
			mainTemplate = mainTemplate + columnWithRows + columnWithReceipt;
		}
		mainTemplate = mainTemplate + `</div><!--END_RECEIPT_CAPTURE_SECTION-->`;
		return mainTemplate;
	}

	public validateReceiptPosition(name, i, j, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (!custom.receiptDropped) {
			if (i < 12 && j !== 3) {
				for (let x = i; x < custom.grids.length; x++) {
					for (let y = j; y < j + size; y++) {
						if (!custom.grids[x][y].IS_EMPTY) {
							return false;
						}
					}
				}
			} else {
				return false;
			}
		} else {
			for (let x = i; x < custom.grids.length; x++) {
				if (!custom.grids[x][j].IS_EMPTY) {
					return false;
				}
			}
		}
		return true;
	}
	public onDeleteReceiptCaptureField(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const receiptRegex = new RegExp(
			`<div class='RECEIPT_CAPTURE_SECTION([\\s\\S]*?)<!--END_RECEIPT_CAPTURE_SECTION-->`
		);
		let layout = '';
		for (let x = i; x < custom.grids.length; x++) {
			layout = layout + `<div class='ROW_${x}' fxLayout="row" fxLayoutGap=5px>`;
			for (let y = 0; y < 4; y++) {
				if (custom.grids[x][y].IS_EMPTY) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: 25,
						FIELD_ID: '',
						settings: null,
					};
					layout = layout + this.initialTemplate(custom.grids, name, x, y);
				} else {
					if (
						this.fieldsMap[custom.grids[x][y].FIELD_ID].DATA_TYPE.DISPLAY ===
						'Receipt Capture'
					) {
						custom.grids[x][y] = {
							IS_EMPTY: true,
							HEIGHT: 10,
							WIDTH: 25,
							FIELD_ID: '',
							settings: null,
						};
						layout = layout + this.initialTemplate(custom.grids, name, x, y);
					} else {
						if (custom.grids[y + 1] && !custom.grids[y + 1][y].IS_EMPTY) {
							custom.grids[x][y].WIDTH = custom.grids[x][y].WIDTH / 2;
						}
						layout =
							layout + this.replaceCellTemplate(custom.grids, name, x, y);
					}
				}
			}
			layout =
				layout +
				`<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
			(click)="context.gridRow('${name}', ${x}, -1)">close</mat-icon></div></div><!--END_ROW_${x}-->`;
		}
		custom.customLayout = custom.customLayout.replace(receiptRegex, layout);
		custom.receiptDropped = false;
	}

	public buildTemplateForListFormula(name, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const xPos = custom.listFormulaPosition.xPos;
		const yPos = custom.listFormulaPosition.yPos;
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const ImageInitialSize = custom.listFormulaPosition.size;
		const field = custom.grids[xPos][yPos].FIELD_ID;
		const settings = custom.grids[xPos][yPos].settings;
		const endPos =
			xPos + 3 > custom.grids.length ? custom.grids.length : xPos + 3;

		if (custom.listFormulaPosition.size === 3) {
			for (let x = xPos; x <= endPos; x++) {
				for (let y = yPos; y < yPos + 3; y++) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: 25,
						FIELD_ID: '',
						settings: null,
					};
				}
			}
		}

		if (size === 2) {
			custom.listFormulaPosition.size = 2;
			flex = 50;
		} else if (size === 3) {
			custom.listFormulaPosition.size = 3;
			flex = 75;
		} else if (size === 4) {
			custom.listFormulaPosition.size = 4;
			flex = 100;
			custom.grids[xPos][0] = {
				IS_EMPTY: false,
				HEIGHT: custom.grids.length,
				WIDTH: 100,
				FIELD_ID: field,
				settings: settings,
			};
		}
		if (size !== 4) {
			for (let x = xPos; x <= endPos; x++) {
				for (let y = yPos; y < yPos + size; y++) {
					custom.grids[x][y] = {
						IS_EMPTY: false,
						HEIGHT: 4,
						WIDTH: 100,
						FIELD_ID: field,
						settings: settings,
					};
				}
			}
		}

		let row1 = '';
		let row2 = '';
		for (let x = xPos; x <= endPos; x++) {
			if (yPos === 1 && size === 2) {
				row1 = row1 + this.buildRowForDiscussion(name, 3, x, yPos);
				row2 = row2 + this.buildRowForDiscussion(name, 3, x, 0);
			} else {
				rows = rows + this.buildRowForDiscussion(name, size, x, yPos);
			}
		}
		const flexRegex = new RegExp('COLUMN_FLEX');
		const rowsRegex = new RegExp('ADD_ROWS_FOR_THIS_COLUMN');
		let columnWithRows = columnTemplate.replace(rowsRegex, rows);
		columnWithRows = columnWithRows.replace(flexRegex, (100 - flex).toString());

		let columnWithImage = columnTemplate.replace(flexRegex, flex.toString());
		columnWithImage = columnWithImage.replace(
			rowsRegex,
			this.replaceCellTemplate(custom.grids, name, xPos, yPos)
		);
		let mainTemplate = `<div class='LIST_FORMULA_SECTION' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			// FIRST COLUMN
			mainTemplate = mainTemplate + columnWithImage + columnWithRows;
		} else if (yPos === 1 && size === 2) {
			// MIDDLE
			let column1WithRows1 = columnTemplate.replace(rowsRegex, row1);
			column1WithRows1 = column1WithRows1.replace(
				flexRegex,
				(flex / 2).toString()
			);
			let column2WithRows2 = columnTemplate.replace(rowsRegex, row2);
			column2WithRows2 = column2WithRows2.replace(
				flexRegex,
				(flex / 2).toString()
			);
			mainTemplate =
				mainTemplate + column1WithRows1 + columnWithImage + column2WithRows2;
		} else {
			// END
			mainTemplate = mainTemplate + columnWithRows + columnWithImage;
		}
		mainTemplate = mainTemplate + `</div><!--END_LIST_FORMULA_SECTION-->`;
		return mainTemplate;
	}

	public loadLayoutWithListFormulaSection(name, listFormulaPosition, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const removeDivFrom = listFormulaPosition.xPos + 1;
		const removeDivTo =
			listFormulaPosition.xPos + 3 > custom.grids.length - 1
				? custom.grids.length - 1
				: listFormulaPosition.xPos + 3;
		custom.customLayout = custom.customLayout.replace(
			new RegExp(
				`<div class='ROW_${removeDivFrom}([\\s\\S]*?)<!--END_ROW_${removeDivTo}-->`
			),
			''
		);

		if (!custom.listFormulaDropped) {
			const rowRegex = new RegExp(
				`<div class='ROW_${listFormulaPosition.xPos}([\\s\\S]*?)<!--END_ROW_${listFormulaPosition.xPos}-->`
			);
			custom.customLayout = custom.customLayout.replace(
				rowRegex,
				this.buildTemplateForListFormula(name, size)
			);
		} else {
			const listFormulaRegex = new RegExp(
				`<div class='LIST_FORMULA_SECTION([\\s\\S]*?)<!--END_LIST_FORMULA_SECTION-->`
			);
			custom.customLayout = custom.customLayout.replace(
				listFormulaRegex,
				this.buildTemplateForListFormula(name, size)
			);
		}
		custom.listFormulaDropped = true;
	}

	public onDeleteListFormulaField(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const removeDivTo =
			i + 3 > custom.grids.length ? custom.grids.length - 1 : i + 3;
		const listFormulaRegex = new RegExp(
			`<div class='LIST_FORMULA_SECTION([\\s\\S]*?)<!--END_LIST_FORMULA_SECTION-->`
		);
		let layout = '';
		for (let x = i; x <= removeDivTo; x++) {
			layout = layout + `<div class='ROW_${x}' fxLayout="row" fxLayoutGap=5px>`;
			for (let y = 0; y < 4; y++) {
				if (custom.grids[x][y].IS_EMPTY) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: 25,
						FIELD_ID: '',
						settings: null,
					};
					layout = layout + this.initialTemplate(custom.grids, name, x, y);
				} else {
					if (
						this.fieldsMap[custom.grids[x][y].FIELD_ID].DATA_TYPE.DISPLAY ===
						'List Formula'
					) {
						custom.grids[x][y] = {
							IS_EMPTY: true,
							HEIGHT: 10,
							WIDTH: 25,
							FIELD_ID: '',
							settings: null,
						};
						layout = layout + this.initialTemplate(custom.grids, name, x, y);
					} else {
						if (custom.grids[y + 1] && !custom.grids[y + 1][y].IS_EMPTY) {
							custom.grids[x][y].WIDTH = custom.grids[x][y].WIDTH / 2;
						}
						layout =
							layout + this.replaceCellTemplate(custom.grids, name, x, y);
					}
				}
			}
			layout =
				layout +
				`<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
			(click)="context.gridRow('${name}', ${x}, -1)">close</mat-icon></div></div><!--END_ROW_${x}-->`;
		}
		custom.customLayout = custom.customLayout.replace(listFormulaRegex, layout);
		custom.listFormulaDropped = false;
	}

	public validateListFormulaPosition(name, i, j, size) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const removeDivTo =
			i + 4 > custom.grids.length ? custom.grids.length - 1 : i + 4;
		if (!custom.listFormulaDropped) {
			if (i < 12 && j !== 3) {
				for (let x = i; x < removeDivTo; x++) {
					for (let y = j; y < j + size; y++) {
						if (!custom.grids[x][y].IS_EMPTY) {
							return false;
						}
					}
				}
			} else {
				return false;
			}
		} else {
			for (let x = i; x < removeDivTo; x++) {
				if (!custom.grids[x][j].IS_EMPTY) {
					return false;
				}
			}
		}

		for (let x = 0; x < custom.grids.length; x++) {
			for (let y = 0; y < 4; y++) {
				if (!custom.grids[x][y].IS_EMPTY) {
					if (
						this.fieldsMap[custom.grids[x][y].FIELD_ID].DATA_TYPE.DISPLAY ===
						'List Formula'
					) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
