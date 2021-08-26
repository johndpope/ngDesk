import { Component, Inject, OnInit, Optional } from '@angular/core';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { MatChipInputEvent } from '@angular/material/chips';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { RenderLayoutService } from '../../render-layout/render-layout.service';
import { forkJoin, Observable, Subject } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomModulesService } from '../render-detail-new/custom-modules.service';
import { RenderDetailNewComponent } from '../render-detail-new/render-detail-new.component';
import { DataApiService } from '@ngdesk/data-api';
import { FormDataApiService } from '@ngdesk/data-api';
import { CacheService } from '@src/app/cache.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { RenderListHelper } from '../render-list-helper/render-list-helper';
import { RenderDetailHelper } from '../render-detail-helper/render-detail-helper';
import { MatDialogHelper } from '../dialog-snackbar-helper/matdialog-helper';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { ServiceCatalogueService } from '@src/app/modules/modules-detail/forms/service-catalogue-detail/service-catalogue.service';
import { OneToManyDialogComponent } from './../../dialogs/one-to-many-dialog/one-to-many-dialog.component';
import { ThemeService } from '@src/app/theme.service';
import { FormGridLayoutService } from './forms-grid-layout.service';
@Component({
	selector: 'app-render-forms',
	templateUrl: './render-forms.component.html',
	styleUrls: ['./render-forms.component.scss'],
	providers: [RenderListHelper, RenderDetailHelper, MatDialogHelper],
})
export class RenderFormsComponent implements OnInit {
	public themeColorObserver: Observable<string>;
	private formId;
	private moduleId;
	public scrollSubject = new Subject<any>();
	public layoutStyle = 'standard';
	public fieldsMap = {};
	public serviceCatalogue;
	public fields = [];
	public entry: any = {};
	public formEnabled = true;
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	public formName = '';
	public message = '';
	private module;
	public dataMaterialModule: any = {};
	public hint: boolean;
	public helpTextMap: Map<String, boolean> = new Map<String, boolean>();
	public filteredCountries: any = [];
	public panels = [];
	public gridLayout = false;

	constructor(
		@Optional() @Inject(MAT_DIALOG_DATA) public modalData: any,
		private router: Router,
		private route: ActivatedRoute,
		private themeService: ThemeService,
		public renderLayoutService: RenderLayoutService,
		private customModulesService: CustomModulesService,
		private cacheService: CacheService,
		private dataService: DataApiService,
		private modulesService: ModulesService,
		public dialogHelper: MatDialogHelper,
		public renderDetailHelper: RenderDetailHelper,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private serviceCatalogueService: ServiceCatalogueService,
		private formGridLayoutService: FormGridLayoutService,
		private formDataApiService: FormDataApiService
	) {
		this.dataMaterialModule = this.renderDetailHelper.dataMaterialModule;
	}

	public ngOnInit() {
		this.filteredCountries = this.renderLayoutService.countries;
		this.formId = this.route.snapshot.params['formId'];
		this.themeColorObserver = this.themeService.primaryColor;
		this.moduleId = this.route.snapshot.params['moduleId'];
		forkJoin([
			this.modulesService.getModuleById(this.moduleId),
			this.serviceCatalogueService.getForm(this.moduleId, this.formId),
		]).subscribe((response: any) => {
			this.module = response[0];
			this.customModulesService.clearVariables();
			this.customModulesService.loadVariablesForModule(this.module, this.entry);
			this.fields = this.module.FIELDS;
			this.module.FIELDS.forEach((field) => {
				this.fieldsMap[field.FIELD_ID] = field;
				if (field.HELP_TEXT !== null && field.HELP_TEXT !== '') {
					this.hint = true;
					this.helpTextMap.set(field.FIELD_ID, this.hint);
				}
			});
			this.serviceCatalogue = response[1].FORM;
			this.formName = this.serviceCatalogue.name;
			this.layoutStyle = this.serviceCatalogue.layoutStyle;

			//load the layout
			this.gridLayout = true;
			this.panels = this.formGridLayoutService.getCustomPanelsForGridLayout(
				this.serviceCatalogue
			);
			this.panels = this.formGridLayoutService.buildTemplates(
				this.panels,
				this.module,
				this.layoutStyle,
				this.entry
			);
		});
	}

	public save() {
		let discussionField = '';
		this.fields.forEach((field) => {
			if (
				field.DATA_TYPE.DISPLAY === 'Phone' &&
				this.entry[field.NAME] !== undefined
			) {
				if (this.entry[field.NAME].hasOwnProperty('PHONE_NUMBER')) {
					if (this.entry[field.NAME].PHONE_NUMBER.length === 0) {
						this.entry[field.NAME]['COUNTRY_CODE'] =
							this.entry[field.NAME]['COUNTRY_CODE'];
						this.entry[field.NAME]['DIAL_CODE'] =
							this.entry[field.NAME]['DIAL_CODE'];
						this.entry[field.NAME]['COUNTRY_FLAG'] =
							this.entry[field.NAME]['COUNTRY_FLAG'];
					} else {
						this.entry[field.NAME]['PHONE_NUMBER'] =
							this.entry[field.NAME]['PHONE_NUMBER'].toString();
					}
				}
			}
			if (field.DATA_TYPE.DISPLAY === 'Discussion') {
				discussionField = field.NAME;
			}
		});

		if (discussionField !== '' && this.message !== '') {
			this.entry[discussionField] = [];
			this.entry[discussionField].push({
				MESSAGE: this.message,
				ATTACHMENTS: [],
			});
		}

		this.formDataApiService
			.postFormEntry(this.formId, this.moduleId, this.entry)
			.subscribe(
				(response: any) => {
					this.bannerMessageService.successNotifications.push({
						message: this.translateService.instant('SAVED_SUCCESSFULLY'),
					});
					this.router.navigate([`render/catalogue`]);
				},
				(error) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	public addItem(event: MatChipInputEvent, fieldname): void {
		const input = event.input;
		const value = event.value;
		if (!this.entry[fieldname]) {
			this.entry[fieldname] = [];
		}

		// Add email if its valid
		if (fieldname === 'CC_EMAILS') {
			if ((value || '').trim() && /\S+@\S+\.\S+/.test((value || '').trim())) {
				this.entry[fieldname].push(value.trim());
			}
		} else {
			this.entry[fieldname].push(value.trim());
		}

		// Reset the input value
		if (input) {
			input.value = '';
		}
	}

	public openOneToOneCreateLayoutDialog(moduleId, field) {
		const currentEntry = Object.assign({}, this.entry);
		this.cacheService.getModule(field.MODULE).subscribe((response: any) => {
			if (response.NAME !== 'Users') {
				// generate unique id based on index for each dialog
				let dialogId = `render-detail-dialog_0`;
				if (this.renderDetailHelper.dialog.openDialogs.length > 0) {
					dialogId = `render-detail-dialog_${this.renderDetailHelper.dialog.openDialogs.length}`;
				}
				const dialogs = this.renderDetailHelper.dialog.openDialogs.length;
				const renderDetail = this.renderDetailHelper.dialog.open(
					RenderDetailNewComponent,
					{
						width: '1024px',
						height: '768px',
						id: dialogId,
						data: {
							MODULE_ID: moduleId,
							PARENT_MODULE_ID: this.module['MODULE_ID'],
							FIELD: field,
							IS_EDIT: false,
						},
					}
				);
				renderDetail.afterClosed().subscribe((entry) => {
					// this.inheritValues(field.FIELD_ID, entry.DATA_ID);
					const fieldVar = this.module['FIELDS'].find(
						(temp) => temp.NAME === field.NAME
					);
					this.cacheService
						.getModule(fieldVar.MODULE)
						.subscribe((response: any) => {
							this.entry = currentEntry;
							const relatedField = response.FIELDS.find(
								(temp) => temp.FIELD_ID === fieldVar.PRIMARY_DISPLAY_FIELD
							);
							this.entry[field.NAME] = {
								DATA_ID: entry.DATA_ID,
								PRIMARY_DISPLAY_FIELD: entry[relatedField.NAME],
							};
							this.customModulesService.loadVariablesForModule(
								this.module,
								this.entry
							);
						});
				});
			} else {
				const inviteUser = this.dialogHelper.inviteUsers();
				inviteUser.afterClosed().subscribe((result) => {
					this.dataService
						.getAllData(field.MODULE)
						.subscribe((usersResponse: any) => {
							let resultArray = [];
							const users = usersResponse.content;
							users.forEach((user) => {
								if (result.includes(user.EMAIL_ADDRESS)) {
									resultArray.push(user);
								}
							});
							// this.entry[field.NAME] = resultArray;
							this.customModulesService.oneToManyFields[field.FIELD_ID].DATA =
								resultArray;

							let userEntry;
							users.forEach((user) => {
								if (result.includes(user.EMAIL_ADDRESS)) {
									userEntry = user;
								}
							});
							this.entry[field.NAME] = {
								DATA_ID: userEntry.DATA_ID,
								PRIMARY_DISPLAY_FIELD: userEntry.EMAIL_ADDRESS,
							};
							this.customModulesService.loadVariablesForModule(
								this.module,
								this.entry
							);
						});
				});
			}
		});
	}

	public updatePhoneInfo(country, fieldName) {
		this.entry[fieldName]['COUNTRY_CODE'] = country.COUNTRY_CODE;
		this.entry[fieldName]['DIAL_CODE'] = country.COUNTRY_DIAL_CODE;
		this.entry[fieldName]['COUNTRY_FLAG'] = country.COUNTRY_FLAG;
	}

	public removeItem(object: string, fieldName): void {
		const index = this.entry[fieldName].indexOf(object);
		this.entry[fieldName].splice(index, 1);
	}

	// START RELATION FUNCTIONS
	public clearInput(event: any) {}

	public addDataForRelationshipField(field, event, formControlFieldName) {
		if (
			field.RELATIONSHIP_TYPE === 'Many to One' ||
			field.RELATIONSHIP_TYPE === 'One to One'
		) {
			this.entry[field.NAME] = event.option.value;
			this.customModulesService.formControls[formControlFieldName].setValue(
				event.option.value['PRIMARY_DISPLAY_FIELD']
			);
		} else if (field.RELATIONSHIP_TYPE === 'Many to Many') {
			if (this.entry[field.NAME] === undefined) {
				this.entry[field.NAME] = [];
			}
			this.entry[field.NAME].push(event.option.value);
			this.customModulesService.formControls[formControlFieldName].setValue('');
		}
	}

	public remove(element, arrayName): void {
		const index = this.entry[arrayName].indexOf(element);
		if (index >= 0) {
			const array = this.entry[arrayName];
			array.splice(index, 1);
		}
	}

	public openOneToManyMapDialog(relmoduleId, field): void {
		this.customModulesService.oneToManyControls['FIELD_IN_FOCUS'] = '';
		this.customModulesService.setupOneToManyTable(
			this.module,
			field.MODULE,
			field,
			this.entry
		);
		const that = this;
		setTimeout(function () {
			if (that.customModulesService.oneToManyFields[field.FIELD_ID]) {
				const dialogRef = this.renderDetailHelper.dialog.open(
					OneToManyDialogComponent,
					{
						panelClass: 'one-to-many-dialog',
						data: {
							relmoduleId: field.MODULE,
							moduleId: this.module.MODULE_ID,
							fieldId: field.FIELD_ID,
							relatedField: field.RELATIONSHIP_FIELD,
						},
						width: '700px',
						disableClose: true,
						minHeight: '600px',
						minWidth: '800px',
					}
				);
				dialogRef.afterClosed().subscribe((preOneToManyFields) => {
					let postToManyFieldsDataIds = [];
					that.customModulesService.oneToManyFields[
						field.FIELD_ID
					].DATA.forEach((oneToManyFieldsData) => {
						postToManyFieldsDataIds.push(oneToManyFieldsData.DATA_ID);
					});
					that.entry[field.NAME] =
						that.customModulesService.oneToManyFields[field.FIELD_ID].DATA;
					preOneToManyFields.forEach((currentDataId) => {
						if (!postToManyFieldsDataIds.includes(currentDataId)) {
							that.cacheService
								.getPrerequisiteForDetaiLayout(field.MODULE, currentDataId)
								.subscribe((responseList) => {
									const module = responseList[0];
									const relatedfieldDetails = module['FIELDS'].find(
										(relatedfield) =>
											relatedfield.FIELD_ID === field.RELATIONSHIP_FIELD
									);

									that.dataService
										.putManyToOneEntry(
											field.MODULE,
											currentDataId,
											relatedfieldDetails.FIELD_ID
										)
										.subscribe(
											(response) => {},
											(error) => {
												that.bannerMessageService.errorNotifications.push({
													message: error.error.ERROR,
												});
											}
										);
								});
						}
					});
					that.computeAggregateFieldsFromOneToMany(field);
				});
			}
		}, 1000);
	}

	public computeAggregateFieldsFromOneToMany(fieldData) {
		this.cacheService.getModule(fieldData.MODULE).subscribe((response: any) => {
			this.module.FIELDS.forEach((field) => {
				if (
					field.DATA_TYPE.DISPLAY === 'Aggregate' &&
					this.entry.hasOwnProperty(fieldData.NAME) &&
					field.AGGREGATION_FIELD === fieldData.FIELD_ID
				) {
					const aggregationField = response['FIELDS'].find(
						(fieldInRelated) =>
							fieldInRelated.FIELD_ID === field.AGGREGATION_RELATED_FIELD
					);
					this.entry[field.NAME] = 0;
					this.customModulesService.oneToManyFields[
						fieldData.FIELD_ID
					].DATA.forEach((element) => {
						if (this.entry[field.NAME] && field.AGGREGATION_TYPE === 'sum') {
							if (
								element[aggregationField.NAME] === undefined ||
								element[aggregationField.NAME] === null
							) {
								element[aggregationField.NAME] = 0;
							}

							this.entry[field.NAME] =
								parseFloat(this.entry[field.NAME]) +
								parseFloat(element[aggregationField.NAME]);
						} else {
							if (
								element[aggregationField.NAME] === undefined ||
								element[aggregationField.NAME] === null
							) {
								element[aggregationField.NAME] = 0;
							}

							this.entry[field.NAME] = parseFloat(
								element[aggregationField.NAME]
							);
						}
					});
				}
			});
		});
	}

	public searchCountries(value: string) {
		if (value && value !== null && value !== '') {
			const searchString = value.toLowerCase();
			this.filteredCountries = this.renderLayoutService.countries.filter(
				(country) =>
					country.COUNTRY_NAME.toLowerCase().indexOf(searchString) > -1
			);
		} else {
			this.filteredCountries = this.renderLayoutService.countries;
		}
	}

	private openOneToManyCreateLayoutDialog(moduleId, field) {
		this.customModulesService.setupOneToManyTable(
			this.module,
			field.MODULE,
			field,
			this.entry
		);
		const that = this;
		setTimeout(function () {
			if (that.customModulesService.oneToManyFields[field.FIELD_ID]) {
				that.cacheService.getModule(field.MODULE).subscribe((response: any) => {
					if (response.NAME !== 'Users') {
						that.customModulesService.oneToManyControls['FIELD_IN_FOCUS'] = '';
						// generate unique id based on index for each dialog
						let dialogId = `render-detail-dialog_0`;
						if (that.renderDetailHelper.dialog.openDialogs.length > 0) {
							dialogId = `render-detail-dialog_${that.renderDetailHelper.dialog.openDialogs.length}`;
						}
						const dialogs = that.renderDetailHelper.dialog.openDialogs.length;
						const renderDetail = that.renderDetailHelper.dialog.open(
							RenderDetailNewComponent,
							{
								width: '1024px',
								height: '768px',
								id: dialogId,
								disableClose: true,
								data: {
									MODULE_ID: moduleId,
									PARENT_MODULE_ID: that.module['MODULE_ID'],
									FIELD: field,
									ONE_TO_MANY_FIELDS: that.customModulesService.oneToManyFields,
									EXISTING_IDS:
										that.customModulesService.existingDataIdsOneToMany,
									IS_EDIT: false,
									FORM_CONTROLS: that.customModulesService.formControls,
									RELATION_FIELD_FILTERED_ENTRIES:
										that.customModulesService.relationFieldFilteredEntries,
								},
							}
						);
						renderDetail.afterClosed().subscribe((modalData) => {
							if (that.modalData) {
								if (that.modalData.DATA_ID === undefined) {
									that.loadDataForNewEntry(dialogs, modalData, field);
								} else if (that.modalData.DATA_ID == 'new') {
									that.loadDataForNewEntry(dialogs, modalData, field);
								} else if (that.modalData.DATA_ID != 'new') {
									that.loadDialogDataForOldEntry(modalData);
								}
							} else if (
								that.entry['DATA_ID'] == undefined ||
								that.entry['DATA_ID'] == 'new'
							) {
								that.loadDataForNewEntry(dialogs, modalData, field);
							} else if (that.entry['DATA_ID']) {
								that.loadDialogDataForOldEntry(modalData);
							} else if (that.route.snapshot.params) {
								if (that.route.snapshot.params['dataId'] == 'new') {
									that.loadDataForNewEntry(dialogs, modalData, field);
								} else {
									that.loadDialogDataForOldEntry(modalData);
								}
							}
						});
					} else {
						const inviteUser = that.dialogHelper.inviteUsers();
						inviteUser.afterClosed().subscribe((result) => {
							that.dataService
								.getAllData(field.MODULE)
								.subscribe((usersResponse: any) => {
									let resultArray = [];
									const users = usersResponse.content;
									users.forEach((user) => {
										if (result.includes(user.EMAIL_ADDRESS)) {
											resultArray.push(user);
										}
									});
									// this.entry[field.NAME] = resultArray;
									that.customModulesService.oneToManyFields[
										field.FIELD_ID
									].DATA = resultArray;
								});
						});
					}
				});
			}
		}, 1000);
	}

	public loadDialogDataForOldEntry(modalData) {
		const that = this;
		const dialogFied = that.module.FIELDS.find(
			(field) => field.FIELD_ID === modalData.dialogFieldId
		);

		let subEntry =
			that.customModulesService.oneToManyFields[modalData.dialogFieldId].DATA[
				that.customModulesService.oneToManyFields[modalData.dialogFieldId].DATA
					.length - 1
			];
		that.UpdateOneToManyEntryToParrent(subEntry, dialogFied);
	}

	// post call to update One to Many entry
	// triggers in afterClose() method

	UpdateOneToManyEntryToParrent(entry, dialogFied) {
		let payload = JSON.parse(JSON.stringify(entry));
		this.dataService
			.postModuleEntry(dialogFied['MODULE'], payload, false)
			.subscribe((res) => {
				let entries =
					this.customModulesService.oneToManyFields[dialogFied.FIELD_ID].DATA;
				let latestestEntry = entries[entries.length - 1];
				latestestEntry['DATA_ID'] = res.DATA_ID;
				this.customModulesService.oneToManyFields[
					dialogFied.FIELD_ID
				].DATA.splice(entries.length - 1, 1);
				this.customModulesService.oneToManyFields[
					dialogFied.FIELD_ID
				].DATA.push(latestestEntry);
			});
	}

	public loadDataForNewEntry(dialogs, modalData, field) {
		const that = this;
		if (dialogs === 0) {
			that.modalData = null;
		} else {
			const dialogFied = that.module.FIELDS.find(
				(field) => field.FIELD_ID === modalData.dialogFieldId
			);
			that.entry[dialogFied.NAME] =
				that.customModulesService.oneToManyFields[modalData.dialogFieldId].DATA;
		}
		if (that.route.snapshot?.params['moduleId']) {
			that.module['MODULE_ID'] = that.route.snapshot.params['moduleId'];
			that.entry['DATA_ID'] = that.route.snapshot.params['dataId'];
			that.customModulesService.layoutType = that.route.snapshot.params['type'];
		}

		that.customModulesService.formControls = modalData.formControls;
		that.customModulesService.relationFieldFilteredEntries =
			modalData.relationFieldFilteredEntries;
		that.computeAggregateFieldsFromOneToMany(field);
	}

	public openEditForOneToManyCreateLayout(entry, field) {
		this.customModulesService.oneToManyControls['FIELD_IN_FOCUS'] = '';
		if (!entry.hasOwnProperty('DATA_ID')) {
			this.customModulesService.oneToManyControls['FIELD_IN_FOCUS'] = '';
			// generate unique id based on index for each dialog
			let dialogId = `render-detail-dialog_0`;
			if (this.renderDetailHelper.dialog.openDialogs.length > 0) {
				dialogId = `render-detail-dialog_${this.renderDetailHelper.dialog.openDialogs.length}`;
			}
			const renderDetail = this.renderDetailHelper.dialog.open(
				RenderDetailNewComponent,
				{
					width: '1024px',
					height: '768px',
					id: dialogId,
					data: {
						MODULE_ID: field.MODULE,
						PARENT_MODULE_ID: this.module['MODULE_ID'],
						FIELD: field,
						ONE_TO_MANY_FIELDS: this.customModulesService.oneToManyFields,
						ENTRY: entry,
						IS_EDIT: true,
					},
				}
			);
			renderDetail.afterClosed().subscribe((result) => {
				this.modalData = null;
				this.module['MODULE_ID'] = this.route.snapshot.params['moduleId'];
				this.entry['DATA_ID'] = this.route.snapshot.params['dataId'];
				this.computeAggregateFieldsFromOneToMany(field);
			});
		} else {
			this.router.navigate([`render/${field.MODULE}/edit/${entry.DATA_ID}`]);
		}
	}

	public removeOneToManyEntry(element, field) {
		const newData = [];
		this.customModulesService.oneToManyFields[field.FIELD_ID].DATA.forEach(
			(entry) => {
				if (entry !== element) {
					newData.push(entry);
				}
			}
		);
		this.customModulesService.oneToManyFields[field.FIELD_ID].DATA = newData;
		if (element.DATA_ID === 'new' || element.DATA_ID === undefined) {
			this.customModulesService.setupOneToManyTable(
				this.module,
				field.MODULE,
				field,
				this.entry
			);
		} else {
			this.cacheService
				.getPrerequisiteForDetaiLayout(field.MODULE, element.DATA_ID)
				.subscribe((responseList) => {
					const module = responseList[0];
					const relatedfieldDetails = module['FIELDS'].find(
						(relatedfield) => relatedfield.FIELD_ID === field.RELATIONSHIP_FIELD
					);
					delete element[relatedfieldDetails.NAME];
					this.dataService
						.putManyToOneEntry(
							field.MODULE,
							element.DATA_ID,
							relatedfieldDetails.FIELD_ID
						)
						.subscribe(
							(response) => {
								this.customModulesService.setupOneToManyTable(
									this.module,
									field.MODULE,
									field,
									this.entry
								);
							},
							(error) => {
								this.bannerMessageService.errorNotifications.push({
									message: error.error.ERROR,
								});
							}
						);
				});
		}
		this.computeAggregateFieldsFromOneToMany(field);
	}
	// END RELATIONSHIP FUNCTIONS
}
