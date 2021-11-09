import { COMMA, ENTER } from '@angular/cdk/keycodes';
import {
	ChangeDetectorRef,
	Component,
	Inject,
	OnDestroy,
	OnInit,
	Optional,
	ViewChild,
	ElementRef,
} from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { AttachmentApiService } from '@ngdesk/data-api';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { ModulesService } from '@src/app/modules/modules.service';
import { FilePreviewService } from '@src/app/render-layout/data-types/file-preview.service';
import { RenderDetailHelper } from '@src/app/render-layout/render-detail-helper/render-detail-helper';
import { CustomModulesService } from './custom-modules.service';
import { PasswordEncryptionDecryptionService } from '@src/app/render-layout/render-detail-new/password-encryption-decryption.service';
import { GridLayoutService } from '@src/app/render-layout/render-detail-new/grid-layout.service';
import { PredefinedTemplateService } from '@src/app/render-layout/render-detail-new/predefined-template.service';
import { RenderDetailDataService } from './../render-detail-new/render-detail-data.service';
import { RenderLayoutService } from '@src/app/render-layout/render-layout.service';
import { RolesService } from '@src/app/roles/roles.service';
import { UsersService } from '@src/app/users/users.service';
import { Condition } from '../../models/condition';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';

import { AppGlobals } from '@src/app/app.globals';

import { Observable, of, Subject, Subscription } from 'rxjs';

import { concatMap, mergeMap, takeUntil } from 'rxjs/operators';

import { DataApiService } from '@ngdesk/data-api';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
// import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { WebsocketService } from '@src/app/websocket.service';
import { CreateUserComponent } from '@src/app/dialogs/create-user/create-user.component';
// import { InviteUsersDialogComponent } from '@src/app/dialogs/invite-users-dialog/invite-users-dialog.component';
import { MatDialogHelper } from '../dialog-snackbar-helper/matdialog-helper';
import { FormControl } from '@angular/forms';
import { ListPicker } from '@nativescript/core';
import {
	LoadOnDemandListViewEventData,
	RadListView,
} from 'nativescript-ui-listview';
import { InviteUsersDialogComponent } from '@src/app/dialogs/invite-users-dialog/invite-users-dialog.component';
import {
	ZoomIntegrationApiService,
	MeetingRequest,
} from '@ngdesk/integration-api';
import { ImageviewerService } from '../data-types/imageviewer.service';

import { WorkflowStagesService } from '@src/app/render-layout/data-types/workflowStages.service';
import { ConditionService } from '../data-types/condition.service';
import { config } from '../../tiny-mce/tiny-mce-config';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { ApprovalRejectDialogComponent } from './../../dialogs/approval-reject-dialog/approval-reject-dialog.component';
import { ApprovalRejectInformationDialogComponent } from './../../dialogs/approval-reject-information-dialog/approval-reject-information-dialog.component';
import { OneToManyDialogComponent } from './../../dialogs/one-to-many-dialog/one-to-many-dialog.component';
import { filter, indexOf } from 'lodash';
import * as _moment from 'moment';
import * as _momentTimeZone from 'moment-timezone';
import { MatListOptionCheckboxPosition } from '@angular/material/list';
import { ChatDataService } from './chat-data.service';
import { ConditionsService } from '@src/app/custom-components/conditions/conditions.service';

@Component({
	selector: 'app-render-detail-new',
	templateUrl: './render-detail-new.component.html',
	styleUrls: ['./render-detail-new.component.css'],
	providers: [MatDialogHelper, RenderDetailHelper],
})
export class RenderDetailNewComponent implements OnInit, OnDestroy {
	@ViewChild(MatSort, { static: true }) private sort: MatSort;
	@ViewChild(MatPaginator, { static: true }) private paginator: MatPaginator;
	private module;
	public entry: any = {};
	public layoutMissingForRole: boolean;
	public disableTheEntry: boolean;
	public viewAccess: boolean;
	public editAccess: boolean;
	newAttachmentResponse = [];
	public deleteAccess: boolean;
	public panels = [];
	public showTabs = false;
	public gridLayout = false;
	public template;
	public dataMaterialModule: any = {};
	public component = 'render';
	public tinyMceConfig;
	public attachmentLoading = false;
	public attachments = [];
	public generalAttachments = [];
	public imageAttachments = [];
	public ImageAttachmentResponse = [];
	public premadeResponses: any = [];
	public createLayout = false;
	private layout;
	public fieldsMap = {};
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	public roles;
	public zoomIntegrated: boolean;
	public startZoomMeeting: boolean;
	public meetingUrl: any;
	public showSaveOnTitleBar = false;
	public fieldSettingsMap: Map<String, boolean> = new Map<String, any>();
	public helpTextMap: Map<String, boolean> = new Map<String, boolean>();
	public entryApproval = 'NOT_REQUIRED';

	// CODE COPIED OVER NEEDS TO BE REVIEWED
	public formControls = {};
	public relationFieldFilteredEntries = {};
	public chronometerValues = {};
	public manyToManyMap: Map<any, any> = new Map<any, any>();
	private _destroyed$ = new Subject();
	public metaDataArray: any = [];
	public userTemplate = {
		RIGHT_SIDEBAR: {
			OPEN: true,
		},
	};
	public isComponentLoaded = true;
	public titleBarTemplate = '';
	private updateEntrySubscription: Subscription;
	public saving = false;
	imageURL;
	public pdfSrc = '';
	public attachmentsList = [];
	public zoom = 1;
	public hint: boolean;
	public reloadPage: boolean;
	public page = 1;
	public rotate = 0;
	public pdf;

	public isAndroidDevice = false;
	public isIosDevice = false;
	public previousUrl: string;
	public isFileadded: boolean = false;
	public isImageAdded: boolean = false;
	public conditionFieldData: any = {
		CONDITION: '',
		OPERATOR: '',
		VALUE: '',
	};

	valuesArray: any = [];
	filteredValuesArray: any = [];

	selectedFieldName: any = '';
	selectedConditionField: any;
	conditionTypeFieldName: any = '';
	fieldsListForCondition: any[] = [];
	softwareInstalationModule: any;
	public config = config;
	public workflowTemplate = '';
	public workflows: Object = Object();
	public workflowIds = [];
	public workflowInstances: Object = Object();
	public isWorkflowsLoading = true;
	fieldMappingObject = new Map<String, Object>();
	private allModules = [];
	formulaFields = [];
	public currencyExchangeFields: any[] = [];
	private loadDateTime: Date;
	public receiptAttachments: any[] = [];
	public fileUloadCount = 0;
	public updatedFileCount = 0;
	public passwordFieldMap: Map<String, boolean> = new Map<String, any>();
	public passwordField = [];
	public isRenderedFromOneToMany = null;
	public checkboxPosition: MatListOptionCheckboxPosition = 'before';

	public isFilterActive: boolean = false;
	public chatChannel: any = {};
	public customersForAgent: any = [];
	public currentUserStatus = '';
	public customerDetail: any = {
		FIRST_NAME: '',
		LAST_NAME: '',
		EMAIL_ADDRESS: '',
	};
	public chatboxDisabled = false;
	public closeChatMessage = '';
	public themeWrapper = document.querySelector('body');
	public fieldsForChatFilter: any = [];
	public filterField = {
		FIELD: '',
		OPERATOR: 'equals to',
		VALUE: '',
		REQUIREMENT_TYPE: 'All',
	};
	public operators: any = [];
	public chatFilters = [];

	constructor(
		@Optional() @Inject(MAT_DIALOG_DATA) public modalData: any,
		@Optional()
		private renderDetailDialogRef: MatDialogRef<RenderDetailNewComponent>, // public computeAggregateFieldsFromOneToMany(fieldData) {
		// 	if (this.customModulesService.layoutType === 'edit') {
		// 		this.entry[fieldData.NAME] = [];
		// 	}
		// 	this.cacheService.getModule(fieldData.MODULE).subscribe((response: any) => {
		// 		this.module.FIELDS.forEach((field) => {
		// 			if (
		// 				field.DATA_TYPE.DISPLAY === 'Aggregate' &&
		// 				this.entry.hasOwnProperty(fieldData.NAME) &&
		// 				field.AGGREGATION_FIELD === fieldData.FIELD_ID
		// 			) {
		// 				const aggregationField = response['FIELDS'].find(
		// 					(fieldInRelated) =>
		// 						fieldInRelated.FIELD_ID === field.AGGREGATION_RELATED_FIELD
		// 				);
		// 				this.entry[field.NAME] = 0;
		// 				this.customModulesService.oneToManyFields[
		// 					fieldData.FIELD_ID
		// 				].DATA.forEach((element) => {
		// 					if (field.AGGREGATION_TYPE === 'sum') {
		// 						let currentStatus;
		// 						let overAllStatus;
		// 						let hasAll = false;
		// 						if (field.CONDITIONS && field.CONDITIONS.length > 0) {
		// 							field.CONDITIONS.forEach((condition) => {

		// 								const requirementType = condition.REQUIREMENT_TYPE;
		// 								const fieldRelated = response['FIELDS'].find(
		// 									(fieldInRelated) =>
		// 										fieldInRelated.FIELD_ID === condition.CONDITION
		// 								);
		// 								currentStatus =
		// 									this.customModulesService.fieldConditionEvaluation(
		// 										condition,
		// 										fieldRelated,
		// 										element
		// 									);
		// 								if (requirementType === 'All') {
		// 									hasAll = true;
		// 								}
		// 								if (overAllStatus === undefined) {
		// 									overAllStatus = currentStatus;
		// 								} else if (currentStatus === false && hasAll) {
		// 									overAllStatus = false;
		// 								} else if (currentStatus === true && !hasAll) {
		// 									overAllStatus = true;
		// 								} else if (currentStatus === true && hasAll) {
		// 									overAllStatus = true;
		// 								} else if (currentStatus === false && !hasAll) {
		// 									overAllStatus = overAllStatus;
		// 								}

		// 							});
		// 						} else {
		// 							overAllStatus = true;
		// 						}
		// 						if (overAllStatus) {
		// 							if (
		// 								element[aggregationField.NAME] === undefined ||
		// 								element[aggregationField.NAME] === null
		// 							) {
		// 								element[aggregationField.NAME] = 0;
		// 							}

		// 							this.entry[field.NAME] =
		// 								parseFloat(this.entry[field.NAME]) +
		// 								parseFloat(element[aggregationField.NAME]);

		// 							this.entry[field.NAME] = (Math.round(this.entry[field.NAME] * 100) / 100);

		// 						}
		// 						//  else {
		// 						// 	element[aggregationField.NAME] = 0;
		// 						// }

		// 					} else {
		// 						if (
		// 							element[aggregationField.NAME] === undefined ||
		// 							element[aggregationField.NAME] === null
		// 						) {
		// 							element[aggregationField.NAME] = 0;
		// 						}

		// 						this.entry[field.NAME] = parseFloat(
		// 							element[aggregationField.NAME]
		// 						);

		// 						this.entry[field.NAME] = (Math.round(this.entry[field.NAME] * 100) / 100);

		// 					}
		// 				});
		// 			}
		// 		});
		// 	});
		// }
		private bannerMessageService: BannerMessageService,
		private cacheService: CacheService,
		private cd: ChangeDetectorRef,
		private dataService: DataApiService,
		private usersService: UsersService,
		private customModulesService: CustomModulesService,
		private globals: AppGlobals,
		private gridLayoutService: GridLayoutService,
		private modulesService: ModulesService,
		public predefinedTemplateService: PredefinedTemplateService,
		private renderDetailDataSerice: RenderDetailDataService,
		public renderDetailHelper: RenderDetailHelper,
		public rolesService: RolesService,
		private route: ActivatedRoute,
		private router: Router,
		private translateService: TranslateService,
		private userService: UsersService,
		private renderLayoutService: RenderLayoutService,
		private websocketService: WebsocketService,
		private filePreviewService: FilePreviewService,
		public dialogHelper: MatDialogHelper,
		private zoomIntegrationApiService: ZoomIntegrationApiService,
		private imageviewerService: ImageviewerService,
		private workflowStagesService: WorkflowStagesService,
		private conditionService: ConditionService,
		private _snackBar: MatSnackBar,
		private loaderService: LoaderService,
		public passwordEncryptionDecryptionService: PasswordEncryptionDecryptionService,
		private chatDataService: ChatDataService,
		private conditionsService: ConditionsService
	) {
		this.dataMaterialModule = this.renderDetailHelper.dataMaterialModule;
		this.tinyMceConfig = this.renderDetailHelper.config;
		// this config is used for Discussion text editor
		this.config['height'] = 400;
		_momentTimeZone.tz.setDefault('UTC');
	}

	public ngOnInit() {
		// Inherit value in one to many create layout
		this.customModulesService.customModuleServiceObservable.subscribe((res) => {
			this.entry[res.fieldName] = res.value;
		});

		this.isRenderedFromOneToMany = false;
		window.localStorage.removeItem('previousUrl');

		this.route.params.pipe(takeUntil(this._destroyed$)).subscribe((params) => {
			this.resetVariables();
			this.initializeComponent();
		});
		this.reloadEntryOnUpdate();
		this.isZoomCreated();
	}

	private resetVariables() {
		this.layoutMissingForRole = false;
		this.viewAccess = false;
		this.editAccess = false;
		this.deleteAccess = false;
		this.template = undefined;
		this.component = 'render';
		this.layout = undefined;
		this.fieldsMap = {};
		this.module = undefined;
		this.entry = {};
		this.panels = [];
		this.showTabs = false;
		this.gridLayout = false;
		this.saving = false;
		this.showSaveOnTitleBar = false;

		this.workflowTemplate = '';
		this.workflows = {};
		this.workflowIds = [];
		this.workflowInstances = {};
		this.isWorkflowsLoading = true;

		// TODO: TO BE MOVED
		this.attachmentLoading = false;
		this.attachments = [];
		this.generalAttachments = [];
		this.imageAttachments = [];
		this.premadeResponses = [];
		this.allModules = [];
		this.imageviewerService.getImageURL((this.imageURL = ''));
		this.formulaFields = [];
		this.newAttachmentResponse = [];
		this.receiptAttachments = [];
		this.fileUloadCount = 0;
		this.updatedFileCount = 0;
	}

	public ngOnDestroy() {
		this._destroyed$.next();
		this._destroyed$.complete();
		if (this.updateEntrySubscription && this.updateEntrySubscription !== null) {
			this.updateEntrySubscription.unsubscribe();
		}
		if (this._snackBar) {
			this._snackBar.dismiss();
		}
		this.loaderService.isLoading2 = false;
	}

	private initializeComponent() {
		this.isRenderedFromOneToMany = false;
		if (window.localStorage.getItem('previousUrl') !== null) {
			if (
				this.router.url.includes(window.localStorage.getItem('previousUrl'))
			) {
				window.localStorage.removeItem('previousUrl');
			} else {
				this.isRenderedFromOneToMany = true;
			}
		}
		let moduleId;
		let dataId;
		this.customModulesService.fieldsDisableMap = new Map();
		if (this.modalData) {
			if (this.modalData.DATA_ID === undefined) {
				moduleId = this.modalData.MODULE_ID;
				dataId = 'new';
				this.customModulesService.layoutType = 'create';
			} else {
				moduleId = this.modalData.MODULE_ID;
				dataId = this.modalData.DATA_ID;
				this.customModulesService.layoutType = 'edit';
			}
		} else {
			moduleId = this.route.snapshot.params['moduleId'];
			dataId = this.route.snapshot.params['dataId'];
			this.customModulesService.layoutType = this.route.snapshot.params['type'];
		}
		this.isAndroidDevice = this.renderDetailHelper.isAndroid();
		this.isIosDevice = this.renderDetailHelper.isIOS();
		this.previousUrl = this.route.snapshot.queryParamMap.get('previousUrl');
		this.cacheService.getPrerequisiteForDetaiLayout(moduleId, dataId).subscribe(
			(responseList) => {
				this.rolesService
					.getRole(this.userService.user.ROLE)
					.subscribe((roleResponse) => {
						this.renderDetailDataSerice
							.getFieldPermissionValues(
								moduleId,
								this.customModulesService.layoutType,
								dataId
							)
							.subscribe((permissions) => {
								this.customModulesService.disableFieldBasedOnFieldPermission(
									permissions
								);
								if (!this.rolesService.role) {
									this.rolesService.role = roleResponse;
								}
								this.module = responseList[0];
								this.fetchPasswordField(this.module);
								if (responseList[1].hasOwnProperty('entry')) {
									this.entry = responseList[1].entry;
								} else {
									this.entry = responseList[1];
								}
								if (this.module['NAME'] == 'Chats') {
									this.getChatChannelDetails();
									this.getCustomerForAgent();
									if (
										this.entry['REQUESTOR'] &&
										this.entry['REQUESTOR'] !== ''
									) {
										this.loadUserDetailsByRequestorId(
											this.entry['REQUESTOR']['DATA_ID']
										);
										this.getChatModuleFields();
									}
								}
								this.formulaFields = this.module.FIELDS.filter((field) => {
									return (
										(field.DATA_TYPE.DISPLAY === 'Formula' && field.FORMULA) ||
										(field.DATA_TYPE.DISPLAY === 'List Formula' &&
											field.LIST_FORMULA)
									);
								});

								this.currencyExchangeFields = this.module.FIELDS.filter(
									(field) => {
										return field.DATA_TYPE.DISPLAY === 'Currency Exchange';
									}
								);

								if (dataId === 'new') {
									this.entry = {};
								}
								if (dataId !== 'new') {
									this.entry['DATA_ID'] = dataId;
									this.condensePayload();
								}
								const attachmentField = this.module.FIELDS.find(
									(moduleField) =>
										'File Upload' === moduleField.DATA_TYPE.DISPLAY
								);
								if (
									attachmentField &&
									this.entry.hasOwnProperty(attachmentField.NAME) &&
									this.entry[attachmentField.NAME].length !== 0
								) {
									this.generalAttachments = this.entry[attachmentField.NAME];
								}

								const imageAttachmentField = this.module.FIELDS.find(
									(moduleField) => 'Image' === moduleField.DATA_TYPE.DISPLAY
								);
								if (
									imageAttachmentField &&
									this.entry.hasOwnProperty(imageAttachmentField.NAME) &&
									this.entry[imageAttachmentField.NAME].length !== 0
								) {
									this.imageAttachments = this.entry[imageAttachmentField.NAME];
									this.createURLForImagePreview(
										this.imageAttachments[0].ATTACHMENT_UUID,
										imageAttachmentField
									);
								}

								const receiptAttachmentField = this.module.FIELDS.find(
									(moduleField) =>
										'Receipt Capture' === moduleField.DATA_TYPE.DISPLAY
								);
								if (
									receiptAttachmentField &&
									this.entry.hasOwnProperty(receiptAttachmentField.NAME) &&
									this.entry[receiptAttachmentField.NAME]
								) {
									this.receiptAttachments.push(
										this.entry[receiptAttachmentField.NAME]
									);
								}
								let conditionFields = [];
								this.module.FIELDS.map((moduleField) => {
									if (moduleField.DATA_TYPE.DISPLAY == 'Condition') {
										conditionFields.push(moduleField);
									}
								});
								if (conditionFields && conditionFields.length > 0) {
									this.getFieldsForConditionDataType();
									conditionFields.map((item) => {
										if (
											this.entry[item.NAME] &&
											this.entry.hasOwnProperty('EDITION_CONDITION')
										) {
											this.conditionFieldData = this.entry[item.NAME];
										} else if (
											this.entry[item.NAME] &&
											this.entry.hasOwnProperty('VERSION_CONDITION')
										) {
											this.conditionFieldData = this.entry[item.NAME];
										}
									});
								}
								this.viewAccess = this.rolesService.getViewAccess(moduleId);
								this.editAccess = this.rolesService.getEditAccess(moduleId);
								this.deleteAccess = this.rolesService.getDeleteAccess(moduleId);
								if (dataId === 'new') {
									this.createLayout = true;
								}
								this.cacheService
									.getModulePremadeResponses(moduleId)
									.subscribe((response) => {
										this.premadeResponses = response;
									});

								this.computeAggregationFields();
								this.toggleSaveOnTitleBar();
								if (!this.editAccess) {
									if (this.viewAccess) {
										this.layout = this.predefinedTemplateService.getLayout(
											'detail',
											this.module
										);
									}
								} else {
									this.layout = this.predefinedTemplateService.getLayout(
										this.customModulesService.layoutType,
										this.module
									);
								}

								this.customModulesService.clearVariables();
								this.customModulesService.loadVariablesForModule(
									this.module,
									this.entry,
									this.createLayout
								);

								if (this.modalData) {
									if (this.modalData.ENTRY) {
										this.customModulesService.loadVariablesForModule(
											this.module,
											this.modalData.ENTRY,
											this.createLayout
										);
									}
								}
								this.entry =
									this.renderDetailDataSerice.formatChronometerFieldsOnGet(
										this.entry,
										this.module
									);

								for (let i = 0; i < this.module['FIELDS'].length; i++) {
									this.fieldsMap[this.module['FIELDS'][i].FIELD_ID] =
										this.module['FIELDS'][i];
								}
								this.fieldsMapping();

								this.module['FIELDS'].forEach((field) => {
									this.evaluateConditions(field.FIELD_ID);
									if (field.HELP_TEXT !== null && field.HELP_TEXT !== '') {
										this.hint = true;
										this.helpTextMap.set(field.FIELD_ID, this.hint);
									}
								});

								if (!this.layout) {
									this.layoutMissingForRole = true;
								} else {
									this.initializeLayout(this.customModulesService.layoutType);
								}
								// TODO: REMOVE, HARDCODED FOR HALOOCOM QUICK FIX
								if (this.module.NAME === 'Users' && dataId !== 'new') {
									const oneToManyFields = this.module.FIELDS.filter(
										(oneToManyField) =>
											oneToManyField.NAME === 'TICKETS_REQUESTED' ||
											oneToManyField.NAME === 'TICKETS_ASSIGNED'
									);
									oneToManyFields.forEach((field) => {
										this.cacheService
											.getModule(field.MODULE)
											.subscribe((relatedModule: any) => {
												const relatedField = relatedModule.FIELDS.find(
													(moduleField) =>
														moduleField.FIELD_ID === field.RELATIONSHIP_FIELD
												);
												const search = `${relatedField.NAME}=${dataId}`;
												this.dataService
													.getAllData(field.MODULE, search)
													.subscribe(
														(response: any) => {
															this.entry[field.NAME] = response.content;
														},
														(error) => {
															console.log(error);
														}
													);
											});
									});
								}

								this.route.queryParams.subscribe((params) => {
									if (
										params['phone_number'] &&
										params['phone_number'] != null
									) {
										this.customModulesService.loadUserDetails(
											params['phone_number']
										);
									}
								});
							});
					});
			},
			(error) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
				this.router.navigate([`render/${moduleId}`]);
			}
		);
	}

	public fetchPasswordField(module) {
		this.passwordField = module.FIELDS.filter(
			(moduleField) => moduleField.DATA_TYPE.DISPLAY === 'Password'
		);
		if (this.passwordField.length > 0) {
			this.passwordField.forEach((element) => {
				this.passwordFieldMap[element.FIELD_ID] = false;
			});
		}
	}

	public encryptPasswordOnSave() {
		if (this.passwordField.length > 0) {
			this.passwordField.forEach((element) => {
				if (this.entry[element.NAME]) {
					if (!this.entry[element.NAME].includes('ENCRYPTED:')) {
						if (!this.passwordFieldMap[element.FIELD_ID]) {
							this.passwordFieldMap[element.FIELD_ID] = true;
							this.showHide(element.FIELD_ID, element.NAME);
						} else {
							this.showHide(element.FIELD_ID, element.NAME);
						}
					}
				}
			});
		}
	}

	public showHide(fieldId, fieldName) {
		if (this.passwordFieldMap[fieldId]) {
			const fieldValue = this.entry[fieldName];
			const encryptedPassword =
				this.passwordEncryptionDecryptionService.encryptData(fieldValue);
			this.entry[fieldName] = `ENCRYPTED:${encryptedPassword}`;
			this.passwordFieldMap[fieldId] = false;
		} else {
			let encryptedValue;
			const passwordEncryptedValue = this.entry[fieldName];
			if (passwordEncryptedValue) {
				if (passwordEncryptedValue.includes('ENCRYPTED:')) {
					encryptedValue = passwordEncryptedValue.split(':')[1];
				} else {
					encryptedValue = passwordEncryptedValue;
				}
				const decryptedPassword =
					this.passwordEncryptionDecryptionService.decryptData(encryptedValue);
				if (
					decryptedPassword.length > 40 &&
					!decryptedPassword.includes(' ') &&
					decryptedPassword.includes('=')
				) {
					this.entry[fieldName] = '';
				} else {
					this.entry[fieldName] = decryptedPassword;
				}
			}
			this.passwordFieldMap[fieldId] = true;
		}
	}

	public copyPassword(field) {
		let encryptedValue;
		const passwordEncryptedValue = this.entry[field];
		let decryptedPassword = '';
		if (passwordEncryptedValue) {
			if (passwordEncryptedValue.includes('ENCRYPTED:')) {
				encryptedValue = passwordEncryptedValue.split(':')[1];
				decryptedPassword =
					this.passwordEncryptionDecryptionService.decryptData(encryptedValue);
			}
			const selBox = document.createElement('textarea');
			selBox.style.position = 'fixed';
			selBox.style.left = '0';
			selBox.style.top = '0';
			selBox.style.opacity = '0';
			if (decryptedPassword === 'undefined' || decryptedPassword === '') {
				selBox.value = this.entry[field];
			} else {
				selBox.value = decryptedPassword;
			}
			document.body.appendChild(selBox);
			selBox.focus();
			selBox.select();
			document.execCommand('copy');
			document.body.removeChild(selBox);
			this.bannerMessageService.successNotifications.push({
				message: this.translateService.instant('COPIED'),
			});
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: 'Type password to copy',
			});
		}
	}

	private toggleSaveOnTitleBar() {
		switch (this.module['NAME']) {
			case 'Tickets':
			case 'Chats':
			case 'Teams':
				this.showSaveOnTitleBar = false;
				break;
			default:
				this.showSaveOnTitleBar = true;
				break;
		}
	}

	private initializeLayout(layoutType) {
		// In order to fetch the attachments for the preview
		if (layoutType === 'edit' && module && this.module['FIELDS'].length > 0) {
			const filePreviewField = this.module['FIELDS'].find(
				(field) => field.DATA_TYPE.DISPLAY === 'File Upload'
			);
			if (
				filePreviewField &&
				this.entry &&
				this.entry[filePreviewField.NAME] &&
				this.entry[filePreviewField.NAME].length > 0
			) {
				this.getpdfsAndImagesForPreview(this.entry, filePreviewField);
				if (this.attachmentsList[0]) {
					this.pdfSrc = this.attachmentsList[0];
					if (this.pdfSrc) {
						this.pdf = this.pdfSrc['FILE'];
					}
				}
			}
		}

		if (this.layout['PANELS'] === null) {
			if (
				this.layout['CUSTOM_LAYOUT'] &&
				this.layout['CUSTOM_LAYOUT'] !== null
			) {
				this.template = this.layout['CUSTOM_LAYOUT'];
			} else if (
				this.layout['PREDEFINED_TEMPLATE'] &&
				this.layout['PREDEFINED_TEMPLATE'] !== null
			) {
				// PREDEFINED TEMPLATES

				let predefinedTemplate;
				this.cacheService
					.getPredefinedTemplate(
						this.module['MODULE_ID'],
						layoutType,
						this.module['NAME']
					)
					.subscribe((templateResponse) => {
						predefinedTemplate = templateResponse;
						predefinedTemplate =
							this.predefinedTemplateService.formatPredefinedTemplate(
								this.layout,
								predefinedTemplate,
								this.module,
								layoutType
							);
						this.entry = this.renderDetailDataSerice.loadMissingFields(
							this.layout,
							this.module,
							this.entry,
							layoutType
						);
						console.log(this.entry['CHAT']);
						this.template = predefinedTemplate;
					});
			}
		} else {
			// GRID LAYOUT
			if (layoutType === 'create') {
				this.entry =
					this.renderDetailDataSerice.getDefaultValuesForCreateLayout(
						this.module,
						this.layout,
						this.entry,
						'GRID'
					);
			}
			this.gridLayout = true;
			this.showTabs = this.gridLayoutService.showTabs(this.layout);
			this.panels = this.gridLayoutService.getCustomPanelsForGridLayout(
				this.layout
			);
			this.panels = this.gridLayoutService.buildTemplates(
				this.panels,
				this.module,
				layoutType,
				this.layout
			);
			this.customModulesService.fieldConditions(this.panels);
			if (this.modalData && this.modalData.ENTRY) {
				this.entry = this.modalData.ENTRY;
			}
			this.titleBarTemplate = this.gridLayoutService.getTitleBarTemplate(
				this.module,
				this.layout,
				layoutType
			);
			this.cd.detectChanges();

			// INITIALIZE FORMULA FIELDS
			this.renderDetailDataSerice.initializeFormulaFields(
				this.module,
				this.entry,
				this.formulaFields
			);
			if (this.module.NAME === 'Users') {
				this.cacheService.getRoles().subscribe((roles) => {
					roles.filter((role) => {
						if (role.NAME === 'Customers') {
							role['NAME'] = 'Customer';
						}
					});
					this.roles = roles.sort((a, b) => a.NAME.localeCompare(b.NAME));
				});
			}
			this.getcalculatedValuesForFormula();
		}
		// DISPLAY APPROVAL BUTTONS On TOOLBAR

		const approvalField = this.customModulesService.getApprovalField(
			this.module
		);
		if (approvalField) {
			const fieldName = approvalField['NAME'];
			if (this.entry[fieldName] && this.entry[fieldName] !== null) {
				this.customModulesService.setToolBarButtons(
					this.entry,
					fieldName,
					this.route.snapshot.params['moduleId']
				);
			}
		}

		if (
			layoutType === 'edit' &&
			!this.customModulesService.displayApprovalButton &&
			approvalField
		) {
			const fieldName = approvalField['NAME'];
			if (this.entry[fieldName] && this.entry[fieldName] !== null) {
				const approvalStatus = this.entry[fieldName]['STATUS'];
				if (
					approvalStatus === 'APPROVED' &&
					this.entry[fieldName]['DISABLE_ENTRY']
				) {
					this.showSaveOnTitleBar = false;
					this.disableTheEntry = true;
					this.customModulesService.customModuleVariables['SHOW_NEXT_BUTTON'] =
						false;
					this.customModulesService.customModuleVariables['SHOW_BACK_BUTTON'] =
						false;
				}
			}
		}

		// DISCOVERY MAP FIELD
		const discoveryMapField = this.customModulesService.getDiscoveryMapField(
			this.module
		);
		if (discoveryMapField) {
			if (this.entry['DISCOVERY_MAP']) {
				this.customModulesService.getDiscoveryMaps();
			}
		}

		//ADD DEFAULT PHONE FIELD
		const phoneField = this.module['FIELDS'].find(
			(field) => field.DATA_TYPE.DISPLAY === 'Phone'
		);
		if (
			phoneField &&
			layoutType === 'edit' &&
			module &&
			this.module['FIELDS'].length > 0
		) {
			this.customModulesService.setDefaultPhoneEntry(this.entry, phoneField);
		}
		// INITIALIZE FORMULA FIELDS
		this.renderDetailDataSerice.initializeFormulaFields(
			this.module,
			this.entry,
			this.formulaFields
		);
		if (this.module.NAME === 'Users') {
			this.cacheService.getRoles().subscribe((roles) => {
				roles.filter((role) => {
					if (role.NAME === 'Customers') {
						role['NAME'] = 'Customer';
					}
				});
				this.roles = roles.sort((a, b) => a.NAME.localeCompare(b.NAME));
			});
		}
		this.getcalculatedValuesForFormula();
		let defaultWorkflowId;

		if (layoutType === 'edit') {
			this.workflowTemplate = this.workflowStagesService.getWorkflow();
			this.workflowStagesService
				.getWorkflows(this.entry, this.module)
				.subscribe((response) => {
					if (
						response.WORKFLOW_INSTANCE !== null &&
						response.WORKFLOW_INSTANCE !== undefined &&
						response.WORKFLOW_INSTANCE.length > 0
					) {
						let workflowInstancePayload = response.WORKFLOW_INSTANCE;
						workflowInstancePayload.forEach((workflow) => {
							this.workflows[workflow.WORKFLOW.WORKFLOW_ID] = workflow.WORKFLOW;
							this.workflowInstances[workflow.WORKFLOW.WORKFLOW_ID] = workflow;
							if (this.workflows !== null) {
								this.workflowIds = Object.keys(this.workflows);
								defaultWorkflowId = this.workflowIds[0];
								this.isWorkflowsLoading = false;
							}
						});
						this.workflowStagesService.workflowSwitch(
							this.entry,
							this.customModulesService,
							defaultWorkflowId,
							this.module.MODULE_ID,
							this.workflows,
							this.workflowInstances
						);
					}
				});
		}
	}

	public getAddress(place, fieldName) {
		const field = fieldName.toUpperCase().split(' ')[0];
		this.getStreet1(place, field);
		this.getStreet2(place, field);
		this.getCity(place, field);
		this.getState(place, field);
		this.getZipCode(place, field);
		this.getCountry(place, field);
		// this.addCoordinates(place, field);
	}

	// public addCoordinates(place, fieldName) {
	// 	const longitude = place.geometry.viewport.Qa.j;
	// 	const latitude = place.geometry.viewport.Va.j;
	// 	this.entry[fieldName + '_ADDRESS_LATITUDE'] = latitude;
	// 	this.entry[fieldName + '_ADDRESS_LONGITUDE'] = longitude;
	// }

	public getAddrComponent(place, componentTemplate) {
		let result;
		const addressType = place.address_components.find((component) =>
			component.types.some((type) => type === componentTemplate)
		);
		if (addressType) {
			result = addressType.long_name;
			return result;
		}
		return;
	}

	public getStreet1(place, fieldName) {
		const COMPONENT_TEMPLATE = 'street_number';
		const streetNumber = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		if (streetNumber) {
			this.entry[fieldName + '_STREET_1'] = streetNumber;
		} else {
			this.entry[fieldName + '_STREET_1'] = '';
		}
	}

	public getStreet2(place, fieldName) {
		const COMPONENT_TEMPLATE = 'route';
		const street = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		if (street) {
			this.entry[fieldName + '_STREET_2'] = street;
		} else {
			this.entry[fieldName + '_STREET_2'] = '';
		}
	}

	public getCity(place, fieldName) {
		const COMPONENT_TEMPLATE = 'locality';
		const city = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		if (city === undefined) {
			this.entry[fieldName + '_CITY'] = this.entry[fieldName + '_STATE'];
		} else {
			this.entry[fieldName + '_CITY'] = city;
		}
	}

	public getZipCode(place, fieldName) {
		const COMPONENT_TEMPLATE = 'postal_code';
		const postCode = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		if (postCode) {
			this.entry[fieldName + '_ZIPCODE'] = postCode;
		} else {
			this.entry[fieldName + '_ZIPCODE'] = '';
		}
	}

	public getState(place, fieldName) {
		// tslint:disable-next-line: one-variable-per-declaration
		const COMPONENT_TEMPLATE = 'administrative_area_level_1';
		const state = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		if (state) {
			this.entry[fieldName + '_STATE'] = state;
		} else {
			this.entry[fieldName + '_STATE'] = '';
		}
	}

	public getCountry(place, fieldName) {
		// tslint:disable-next-line: one-variable-per-declaration
		const TEMPLATE = 'country';
		const country = this.getAddrComponent(place, TEMPLATE);
		const countryField = this.module.FIELDS.find(
			(moduleField) => moduleField.NAME === fieldName + '_COUNTRY'
		);
		if (countryField) {
			const picklistValues = countryField.PICKLIST_VALUES;
			picklistValues.forEach((value) => {
				if (value.includes(country)) {
					this.entry[fieldName + '_COUNTRY'] = value;
				}
			});
		}
	}

	public addDataForDiscoveryField(field, event) {
		if (this.entry[field.NAME] === undefined) {
			this.entry[field.NAME] = [];
		}
		this.entry[field.NAME].push(event.option.value.id);
	}

	public addPicklistMultiselectValue(field, event) {
		if (this.entry[field.NAME] === undefined) {
			this.entry[field.NAME] = [];
		}
		this.entry[field.NAME].push(event.option.value);
	}

	public disableOption(fieldName, option) {
		if (this.entry[fieldName]) {
			const item = this.entry[fieldName].find((val) => val === option);
			if (item) {
				return true;
			}
		}
		return false;
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
			if (
				field.RELATIONSHIP_TYPE === 'Many to One' &&
				field.INHERITANCE_MAPPING
			) {
				this.inheritValues(field.FIELD_ID, event.option.value.DATA_ID);
			}
		} else if (field.RELATIONSHIP_TYPE === 'Many to Many') {
			if (this.entry[field.NAME] === undefined) {
				this.entry[field.NAME] = [];
			}
			this.entry[field.NAME].push(event.option.value);
			this.customModulesService.formControls[formControlFieldName].setValue('');
			if (field.NAME === 'USERS' && this.module.NAME === 'Teams') {
				this.customModulesService.teamsAdded.push(event.option.value.DATA_ID);
			}
		}
	}

	public remove(element, arrayName, trigger): void {
		const index = this.entry[arrayName].indexOf(element);
		if (index >= 0) {
			const array = this.entry[arrayName];
			array.splice(index, 1);

			// to set disabled and enabled content
			trigger.openPanel();
			trigger.closePanel();
		}
	}

	// END RELATIONSHIP FUNCTIONS

	// START LIST TEST
	public addItem(event: MatChipInputEvent, fieldName): void {
		const input = event.input;
		this.entry = this.customModulesService.addItemToListText(
			this.entry,
			event.value,
			fieldName,
			this.module
		);
		if (input) {
			event.input.value = '';
		}
	}

	public removeItem(value: string, fieldName): void {
		this.entry = this.customModulesService.removeItemFromListText(
			this.entry,
			value,
			fieldName
		);
	}
	// END LIST TEXT

	public reloadEntryOnUpdate() {
		this.updateEntrySubscription = this.cacheService.entryUpdated.subscribe(
			(updated) => {
				if (
					updated.STATUS &&
					updated.DATA_ID === this.entry.DATA_ID &&
					updated.TYPE !== 'DISCUSSION'
				) {
					this.cacheService.entryUpdated.next({ STATUS: false, DATA_ID: null });
					this._snackBar
						.open(this.translateService.instant('DATA_UPDATED'), 'OK', {
							horizontalPosition: 'center',
						})
						.afterDismissed()
						.subscribe((data) => {
							this.onNotificationReload();
						});
				} else if (
					updated.STATUS &&
					updated.DATA_ID === this.entry.DATA_ID &&
					updated.TYPE === 'DISCUSSION'
				) {
					const discussionField = this.module.FIELDS.find(
						(field) => field.DATA_TYPE.DISPLAY === 'Discussion'
					);
					if (discussionField) {
						let query = `{
							entry: get${this.module.NAME.replace(/ /g, '_')}Entry(id: "${
							this.entry.DATA_ID
						}") {'${discussionField.NAME}'}}`;
						query = this.cacheService.buildDiscussionQuery(
							query,
							discussionField.NAME
						);
						this.cacheService
							.executeGraphqlQuery(query)
							.subscribe((discussionMessages: any) => {
								if (discussionMessages) {
									this.entry[discussionField.NAME] =
										discussionMessages.entry[discussionField.NAME];
									// console.log(this.entry['CHAT']);
								}
							});
					}
				}
			}
		);
	}

	public onNotificationReload() {
		const disucssionControls = this.customModulesService.discussionControls;
		this.resetVariables();
		this.initializeComponent();
		this.customModulesService.discussionControls = disucssionControls;
	}

	public newPremadeResponse() {
		this.router.navigate([`company-settings/premade-responses/new`]);
	}

	// adds attachment with message
	public onFileChange(event) {
		const reader = new FileReader();
		if (
			event.target.files &&
			event.target.files.length &&
			this.attachments.length <= 5
		) {
			const [file] = event.target.files;
			reader.readAsDataURL(file);

			// (file.size <= 1024000) ? this.fileSizeError = false : this.fileSizeError = true;
			reader.onload = () => {
				const data: any = reader.result;
				this.attachments.push({
					FILE_NAME: file.name,
					FILE: data.split('base64,')[1],
					FILE_EXTENSION: file.type,
				});
				// need to run CD since file load runs outside of zone
				this.cd.markForCheck();
			};
		}
	}

	// For general purose attachments
	public onFileChangeForGeneral(event) {
		const reader = new FileReader();
		if (event.target.files && event.target.files.length > 0) {
			const [file] = event.target.files;
			reader.readAsDataURL(file);

			// (file.size <= 1024000) ? this.fileSizeError = false : this.fileSizeError = true;
			reader.onload = () => {
				const data: any = reader.result;
				this.generalAttachments.push({
					FILE_NAME: file.name,
					FILE: data.split('base64,')[1],
					FILE_EXTENSION: file.type,
				});

				// need to run CD since file load runs outside of zone
				this.cd.markForCheck();
			};
		}
	}

	public onImageUpload(event) {
		const reader = new FileReader();
		if (
			event.target.files &&
			event.target.files.length &&
			this.imageAttachments.length <= 5
		) {
			const [file] = event.target.files;
			reader.readAsDataURL(file);
			reader.onload = () => {
				const data: any = reader.result;
				this.imageAttachments = [];
				this.imageAttachments.push({
					FILE_NAME: file.name,
					FILE: data.split('base64,')[1],
					FILE_EXTENSION: file.type,
				});

				// need to run CD since file load runs outside of zone
				this.cd.markForCheck();
			};
		}
	}

	public onReceiptUpload(event) {
		const reader = new FileReader();
		if (event.target.files && event.target.files.length) {
			this.receiptAttachments = [];
			const [file] = event.target.files;
			reader.readAsDataURL(file);
			reader.onload = () => {
				const data: any = reader.result;
				this.receiptAttachments.push({
					FILE_NAME: file.name,
					FILE: data,
					FILE_EXTENSION: file.type,
				});
				// need to run CD since file load runs outside of zone
				this.cd.markForCheck();
			};
		}
	}

	// Check zoom is integrated or not
	public isZoomCreated() {
		this.zoomIntegrationApiService
			.getZoomStatus()
			.subscribe((response: any) => {
				if (response.ZOOM_AUTHENTICATED) {
					this.zoomIntegrated = true;
				}
			});
	}

	// when create Zoom Meeting button clicked
	public createZoomMeeting() {
		let meetingRequest: MeetingRequest = {
			ENTRY_ID: this.entry.DATA_ID,
			MODULE_ID: this.module.MODULE_ID,
			TOPIC: 'ngDesk-Zoom Meeting',
		};
		this.zoomIntegrationApiService
			.generateZoomMeetingLink(meetingRequest)
			.subscribe(
				(response: any) => {
					this.startZoomMeeting = true;
					this.meetingUrl = response.MEETING_START_URL;
				},
				(error) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	// when zoom Meeting Button is clicked
	public startMeetingButton() {
		window.open(this.meetingUrl, '_blank');
	}

	public downloadAttachment(uuid, messageId) {
		return `${this.globals.baseRestUrl}/attachments?attachment_uuid=${uuid}
		&message_id=${messageId}&entry_id=${this.entry.DATA_ID}&module_id=${this.module.MODULE_ID}`;
	}

	public downloadAttachmentForPdfs(uuid, fieldId, dataType, messageId?) {
		// TODO: will need to replace rest with data service call
		let url =
			window.location.protocol +
			'//' +
			window.location.host +
			'/api/ngdesk-data-service-v1';
		url += `/attachments?attachment_uuid=${uuid}&data_id=${this.entry.DATA_ID}&module_id=${this.module.MODULE_ID}&field_id=${fieldId}`;
		if (dataType === 'Discussion' && messageId) {
			url += `&message_id=${messageId}`;
		}
		return url;
	}

	public OnChangeInternalCommentCheckbox($event) {
		if ($event.checked === true) {
			this.customModulesService.discussionControls['MESSAGE_TYPE'] =
				'INTERNAL_COMMENT';
		} else {
			this.customModulesService.discussionControls['MESSAGE_TYPE'] = 'MESSAGE';
		}
	}

	public addPremadeResponse(premadeResponse) {
		const regex = new RegExp(`{{inputMessage.`);
		if (regex.test(premadeResponse.MESSAGE)) {
			this.modulesService
				.getPremadeResponse(
					premadeResponse.PREMADE_RESPONSE_ID,
					this.module['MODULE_ID'],
					this.getEntryId()
				)
				.subscribe(
					(response: any) => {
						this.customModulesService.discussionControls['MESSAGE'] +=
							response.MESSAGE;

						if (this.module['NAME'] == 'Chats') {
							this.convertHTMLToPlainText();
						}
					},
					(error: any) => {
						console.log(error);
					}
				);
		} else {
			this.customModulesService.discussionControls['MESSAGE'] +=
				premadeResponse.MESSAGE;
		}
		if (this.module['NAME'] == 'Chats') {
			this.convertHTMLToPlainText();
		}
	}

	// Evaluates conditions in order to show and hide fields.
	public evaluateConditions(fieldId) {
		// Load the panels first time when load the page
		if (this.customModulesService.fieldsInfluenceMap.size === 0) {
			let panels = [];
			if (this.layout !== undefined && this.layout['PANELS'] != null) {
				panels = this.gridLayoutService.getCustomPanelsForGridLayout(
					this.layout
				);
			}
			this.customModulesService.fieldConditions(panels);
		}

		// Evaluate the conditions when the layout is loaded.
		if (this.customModulesService.fieldsInfluenceMap.has(fieldId)) {
			this.customModulesService.fieldsInfluenceMap
				.get(fieldId)
				.forEach((currentFieldId) => {
					const restrictionValues = {
						All: [],
						Any: [],
					};
					this.evaluateShowAndHideBasedOnCondition(
						currentFieldId,
						restrictionValues
					);
					this.resetHiddenValues(currentFieldId);
				});
		}

		// Evaluate the conditions when the field is selected from the layout.
		if (
			!this.fieldSettingsMap.has(fieldId) ||
			this.fieldSettingsMap.get(fieldId) === undefined
		) {
			if (this.customModulesService.fieldsConditionsMap.has(fieldId)) {
				const restrictionValues = {
					All: [],
					Any: [],
				};
				this.evaluateShowAndHideBasedOnCondition(fieldId, restrictionValues);
			} else {
				this.fieldSettingsMap.set(fieldId, true);
			}
		}
	}

	// Evaluate Hide and Show functionality based on condition value.
	public evaluateShowAndHideBasedOnCondition(
		currentFieldId,
		restrictionValues
	) {
		const conditions =
			this.customModulesService.fieldsConditionsMap.get(
				currentFieldId
			).CONDITIONS;

		conditions.forEach((condition) => {
			// Based on entry value evaluate the condition value
			const conditionValue = this.customModulesService.fieldConditionEvaluation(
				condition,
				this.fieldsMap[condition.CONDITION],
				this.entry
			);
			if (condition.REQUIREMENT_TYPE === 'All') {
				restrictionValues.All.push(conditionValue);
			} else if (condition.REQUIREMENT_TYPE === 'Any') {
				restrictionValues.Any.push(conditionValue);
			}
		});
		// Based on all and Any condition evaluate conditions.
		const result =
			this.customModulesService.evaluateConditionResult(restrictionValues);

		// Evaluate Show and hide and set boolean values to fieldSettingsMap.
		// The map contains field id as key and true or false value as value.
		// Based on the map value the field will be shown.
		if (
			this.customModulesService.fieldsConditionsMap.get(currentFieldId)
				.ACTION === 'HIDE'
		) {
			this.fieldSettingsMap.set(currentFieldId, !result);
		} else if (
			this.customModulesService.fieldsConditionsMap.get(currentFieldId)
				.ACTION === 'SHOW'
		) {
			this.fieldSettingsMap.set(currentFieldId, result);
		}
	}

	private getEntryId() {
		if (this.createLayout) {
			return 'new';
		} else {
			return this.entry['DATA_ID'];
		}
	}

	// Reset hidden Field Values
	public resetHiddenValues(fieldId) {
		if (!this.fieldSettingsMap.get(fieldId)) {
			const resetHiddenField = this.fieldsMap[fieldId];
			if (resetHiddenField.DATA_TYPE.BACKEND === 'String') {
				this.entry[resetHiddenField.NAME] = '';
			} else {
				this.entry[resetHiddenField.NAME] = 0;
			}
			this.getcalculatedValuesForFormula();
		}
		this.getcalculatedValuesForFormula();
	}

	public save(OnclickSaveButton) {
		Object.keys(this.entry).forEach((key) => {
			if (key !== 'DATA_ID') {
				const currentField = this.module['FIELDS'].find(
					(field) => field.NAME === key
				);
				if (typeof this.entry[key] === 'number') {
					if (
						currentField &&
						currentField.DATA_TYPE.BACKEND === 'Date' &&
						typeof this.entry[key] === 'number'
					) {
						this.entry[key] = null;
					}
				} else if (
					currentField &&
					currentField.DATA_TYPE.DISPLAY === 'Currency'
				) {
					const numStr = String(this.entry[key]);
					if (numStr.includes('.')) {
						const decimalPlace = numStr.split('.')[1].length;
						if (decimalPlace > 3) {
							this.entry[key] = this.entry[key].toFixed(3);
						}
					}
					this.entry[key] = null;
				}
			}
		});
		this.saving = true;
		let isAPICalled: boolean = false;

		this.encryptPasswordOnSave();

		// POST ATTACHMENTS
		// BUILD DISCUSSION PAYLOAD
		// ADD DISCUSSION TO ENTRY
		// Add one to many

		if (this.createLayout) {
			delete this.entry['DATA_ID'];
		}
		// to update entry with codition type data
		if (this.conditionTypeFieldName && this.conditionTypeFieldName != '') {
			this.entry[this.conditionTypeFieldName] = this.conditionFieldData;
		}
		//to update entry if image deleted

		const imageAttachmentField = this.module.FIELDS.find(
			(moduleField) => 'Image' === moduleField.DATA_TYPE.DISPLAY
		);

		if (
			imageAttachmentField &&
			this.imageAttachments.length == 0 &&
			this.entry.hasOwnProperty(imageAttachmentField.NAME)
		) {
			this.entry[imageAttachmentField.NAME] = [];
		}

		const fileUploadField = this.module['FIELDS'].find(
			(field) => field.DATA_TYPE.DISPLAY === 'File Upload'
		);

		// This filters out the attachments saved before
		this.generalAttachments = this.generalAttachments.filter(
			(attachment) =>
				attachment['ATTACHMENT_UUID'] === undefined ||
				attachment['ATTACHMENT_UUID'] === '' ||
				attachment['ATTACHMENT_UUID'] === null
		);

		// This filters out the attachments saved before
		if (this.imageAttachments.length > 0) {
			this.imageAttachments = this.imageAttachments.filter(
				(attachment) =>
					attachment['ATTACHMENT_UUID'] === undefined ||
					attachment['ATTACHMENT_UUID'] === '' ||
					attachment['ATTACHMENT_UUID'] === null
			);
		}
		if (this.receiptAttachments.length > 0) {
			this.receiptAttachments = this.receiptAttachments.filter(
				(attachment) =>
					attachment['ATTACHMENT_UUID'] === undefined ||
					attachment['ATTACHMENT_UUID'] === '' ||
					attachment['ATTACHMENT_UUID'] === null
			);
		}
		const receiptUploadField = this.module['FIELDS'].find(
			(field) => field.DATA_TYPE.DISPLAY === 'Receipt Capture'
		);
		if (this.receiptAttachments.length > 0 && receiptUploadField) {
			this.entry[receiptUploadField.NAME] = {};
			this.entry[receiptUploadField.NAME] = this.receiptAttachments[0];
		}
		if (this.generalAttachments.length > 0) {
			isAPICalled = true;
			this.postAttachments(
				OnclickSaveButton,
				this.generalAttachments,
				fileUploadField,
				'File Upload'
			);
		} else {
			if (this.imageAttachments.length == 0) {
				isAPICalled = true;
				if (
					this.modalData &&
					this.customModulesService.oneToManyRelationshipData &&
					this.customModulesService.oneToManyRelationshipData.PARRENT_ENTRY &&
					this.customModulesService.oneToManyRelationshipData.FIELD_NAME &&
					this.customModulesService.oneToManyRelationshipData.PARRENT_ENTRY
						.DATA_ID
				) {
					this.entry[
						this.customModulesService.oneToManyRelationshipData.FIELD_NAME
					] =
						this.customModulesService.oneToManyRelationshipData.PARRENT_ENTRY.DATA_ID;
				}
				let payload = JSON.parse(JSON.stringify(this.entry));
				payload = this.renderDetailDataSerice.formatDiscussion(
					payload,
					this.module,
					this.attachments
				);

				this.doPostOrPutCall(payload, OnclickSaveButton);
			}
		}
		if (this.imageAttachments.length > 0) {
			isAPICalled = true;
			this.postAttachments(
				OnclickSaveButton,
				this.imageAttachments,
				imageAttachmentField,
				'Image'
			);
		}
		if (
			!isAPICalled &&
			this.generalAttachments.length == 0 &&
			this.imageAttachments.length == 0
		) {
			if (
				this.modalData &&
				this.customModulesService.oneToManyRelationshipData &&
				this.customModulesService.oneToManyRelationshipData.PARRENT_ENTRY &&
				this.customModulesService.oneToManyRelationshipData.FIELD_NAME &&
				this.customModulesService.oneToManyRelationshipData.PARRENT_ENTRY
					.DATA_ID
			) {
				this.entry[
					this.customModulesService.oneToManyRelationshipData.FIELD_NAME
				] =
					this.customModulesService.oneToManyRelationshipData.PARRENT_ENTRY.DATA_ID;
			}

			let payload = JSON.parse(JSON.stringify(this.entry));
			payload = this.renderDetailDataSerice.formatDiscussion(
				payload,
				this.module,
				this.attachments
			);

			this.doPostOrPutCall(payload, OnclickSaveButton);
		}
	}

	public events() {
		this.router.navigate([
			`render/${this.route.snapshot.params.moduleId}/edit/${this.route.snapshot.params.dataId}/events`,
		]);
		this.loaderService.isLoading3 = false;
	}

	public doPostOrPutCall(payload, saveButtonValue) {
		if (this.createLayout || this.modalData) {
			this.dataService
				.postModuleEntry(this.module['MODULE_ID'], payload, false)
				.subscribe(
					(response: any) => {
						if (saveButtonValue === 'return') {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SAVED_SUCCESSFULLY'),
							});
							this.saving = false;
							this.router.navigate([
								`render/${this.route.snapshot.params.moduleId}`,
							]);
						} else if (saveButtonValue === 'saveFromDialog') {
							this.closeCreateOneToManyDialog();
							this.saving = true;
							this.onNotificationReload();
							this.loaderService.isLoading2 = false;
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SAVED_SUCCESSFULLY'),
							});
						}
					},
					(error) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		} else {
			this.dataService
				.putModuleEntry(this.module['MODULE_ID'], payload, false)
				.subscribe(
					(response) => {
						if (saveButtonValue === 'return') {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
							});
							this.saving = true;
							this.router.navigate([
								`render/${this.route.snapshot.params.moduleId}`,
							]);
						} else if (saveButtonValue === 'continue') {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
							});
							this.saving = true;
							this.onNotificationReload();
							this.loaderService.isLoading2 = false;
						} else if (saveButtonValue === 'saveFromDialog') {
							this.saving = true;
							this.onNotificationReload();
							this.loaderService.isLoading2 = false;
						}
					},
					(error) => {
						this.loaderService.isLoading2 = false;
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			this.loaderService.isLoading2 = false;
		}
	}

	public publishDiscussion() {
		const publishMessage = this.renderDetailDataSerice.canPublish(
			this.attachments
		);
		if (publishMessage) {
			this.attachments = this.renderDetailDataSerice.removeInlineBaseImages(
				this.attachments
			);
			this.renderDetailDataSerice.postAttachments(this.attachments).subscribe(
				(attachmentResponse: any) => {
					this.attachments = attachmentResponse;
					const messageId = this.globals.guid();
					this.renderDetailDataSerice.replaceInlineMessageWithUrl(
						this.attachments,
						this.entry['DATA_ID'],
						this.module['MODULE_ID'],
						messageId
					);
					const messagePayload =
						this.renderDetailDataSerice.buildDiscussionPayload(
							messageId,
							this.attachments,
							this.module['MODULE_ID'],
							this.entry['DATA_ID'],
							true
						);
					this.websocketService.publishMessage(messagePayload);
					this.attachments = [];
					this.customModulesService.discussionControls['MESSAGE'] = '';
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push(error.error.ERROR);
				}
			);
		}
	}

	public deleteEntry() {
		let dialogMessage = '';
		this.translateService
			.get('ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS', {
				value: this.translateService.instant('TICKET').toLowerCase(),
			})
			.subscribe((res) => {
				dialogMessage = res;
			});

		const dialogRef = this.dialogHelper.deleteEntries(dialogMessage);
		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DELETE')) {
				const ids = [this.entry['DATA_ID']];

				this.dataService
					.deleteData(this.module['MODULE_ID'], ids, false)
					.subscribe(
						(deleteResponse: any) => {
							this.router.navigate([`render/${this.module['MODULE_ID']}`]);
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			}
		});
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
				this.customModulesService.discussionControls['MESSAGE'].trim().length >
					0
			) {
				const messagePayload =
					this.renderDetailDataSerice.buildDiscussionPayload(
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

	public calculatedValuesForFormula(event, field) {
		this.getcalculatedValuesForFormula();
	}

	public concatenatedValuesForFormula(event, field) {
		this.getcalculatedValuesForFormula();
	}

	private computeAggregationFields() {
		// CHECK AGGREGATION FIELD AND PARSE IN PROPER FORMAT
		this.module.FIELDS.forEach((field) => {
			if (
				field.DATA_TYPE.DISPLAY === 'Aggregate' &&
				this.entry.hasOwnProperty(field.NAME)
			) {
				this.customModulesService
					.convertAggregateFields(field, this.module, this.entry[field.NAME])
					.subscribe((value: any) => {
						this.entry[field.NAME] = value;
					});
			}
		});
	}

	private removeTeamsEntry(userId, fieldName) {
		const index = this.entry[fieldName].indexOf(userId);
		const indexOfTeamsArray = this.customModulesService.teamsAdded.indexOf(
			userId.DATA_ID
		);
		if (index !== -1) {
			this.entry[fieldName].splice(index, 1);
		}
		if (indexOfTeamsArray !== -1) {
			this.customModulesService.teamsAdded.splice(indexOfTeamsArray, 1);
		}
	}

	public updatePhoneInfo(country, fieldName) {
		this.entry[fieldName]['COUNTRY_CODE'] = country.COUNTRY_CODE;
		this.entry[fieldName]['DIAL_CODE'] = country.COUNTRY_DIAL_CODE;
		this.entry[fieldName]['COUNTRY_FLAG'] = country.COUNTRY_FLAG;
	}

	public createUser() {
		const dialogRef = this.renderDetailHelper.dialog.open(CreateUserComponent, {
			data: this.customModulesService.newUserDetails,
			width: '700px',
			disableClose: true,
			maxHeight: '90vh',
		});
	}

	public openOneToOneCreateLayoutDialog(moduleId, field) {
		if (!this.customModulesService.fieldsDisableMap[field.FIELD_ID]) {
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
						this.loadPermissionsForFields();
						if (entry) {
							this.inheritValues(field.FIELD_ID, entry.DATA_ID);
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
										this.entry,
										this.createLayout
									);
								});
						}
					});
				} else {
					const inviteUser = this.dialogHelper.inviteUsers();
					inviteUser.afterClosed().subscribe((result) => {
						this.loadPermissionsForFields();
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
									this.entry,
									this.createLayout
								);
							});
					});
				}
			});
		}
	}

	// public computeAggregateFieldsFromOneToMany(fieldData) {
	// 	if (this.customModulesService.layoutType === 'edit') {
	// 		this.entry[fieldData.NAME] = [];
	// 	}
	// 	this.cacheService.getModule(fieldData.MODULE).subscribe((response: any) => {
	// 		this.module.FIELDS.forEach((field) => {
	// 			if (
	// 				field.DATA_TYPE.DISPLAY === 'Aggregate' &&
	// 				this.entry.hasOwnProperty(fieldData.NAME) &&
	// 				field.AGGREGATION_FIELD === fieldData.FIELD_ID
	// 			) {
	// 				const aggregationField = response['FIELDS'].find(
	// 					(fieldInRelated) =>
	// 						fieldInRelated.FIELD_ID === field.AGGREGATION_RELATED_FIELD
	// 				);
	// 				this.entry[field.NAME] = 0;
	// 				this.customModulesService.oneToManyFields[
	// 					fieldData.FIELD_ID
	// 				].DATA.forEach((element) => {
	// 					if (field.AGGREGATION_TYPE === 'sum') {
	// 						let currentStatus;
	// 						let overAllStatus;
	// 						let hasAll = false;
	// 						if (field.CONDITIONS && field.CONDITIONS.length > 0) {
	// 							field.CONDITIONS.forEach((condition) => {

	// 								const requirementType = condition.REQUIREMENT_TYPE;
	// 								const fieldRelated = response['FIELDS'].find(
	// 									(fieldInRelated) =>
	// 										fieldInRelated.FIELD_ID === condition.CONDITION
	// 								);
	// 								currentStatus =
	// 									this.customModulesService.fieldConditionEvaluation(
	// 										condition,
	// 										fieldRelated,
	// 										element
	// 									);
	// 								if (requirementType === 'All') {
	// 									hasAll = true;
	// 								}
	// 								if (overAllStatus === undefined) {
	// 									overAllStatus = currentStatus;
	// 								} else if (currentStatus === false && hasAll) {
	// 									overAllStatus = false;
	// 								} else if (currentStatus === true && !hasAll) {
	// 									overAllStatus = true;
	// 								} else if (currentStatus === true && hasAll) {
	// 									overAllStatus = true;
	// 								} else if (currentStatus === false && !hasAll) {
	// 									overAllStatus = overAllStatus;
	// 								}

	// 							});
	// 						} else {
	// 							overAllStatus = true;
	// 						}
	// 						if (overAllStatus) {
	// 							if (
	// 								element[aggregationField.NAME] === undefined ||
	// 								element[aggregationField.NAME] === null
	// 							) {
	// 								element[aggregationField.NAME] = 0;
	// 							}

	// 							this.entry[field.NAME] =
	// 								parseFloat(this.entry[field.NAME]) +
	// 								parseFloat(element[aggregationField.NAME]);

	// 							this.entry[field.NAME] = (Math.round(this.entry[field.NAME] * 100) / 100);

	// 						}
	// 						//  else {
	// 						// 	element[aggregationField.NAME] = 0;
	// 						// }

	// 					} else {
	// 						if (
	// 							element[aggregationField.NAME] === undefined ||
	// 							element[aggregationField.NAME] === null
	// 						) {
	// 							element[aggregationField.NAME] = 0;
	// 						}

	// 						this.entry[field.NAME] = parseFloat(
	// 							element[aggregationField.NAME]
	// 						);

	// 						this.entry[field.NAME] = (Math.round(this.entry[field.NAME] * 100) / 100);

	// 					}
	// 				});
	// 			}
	// 		});
	// 	});
	// }

	public setApproval(approved) {
		const payload = {
			dataId: this.route.snapshot.params['dataId'],
			moduleId: this.module.MODULE_ID,
			approved: approved,
			comments: '',
		};

		if (approved) {
			this.websocketService.publishApproval(payload);
		} else {
			const setComment = this.renderDetailHelper.dialog.open(
				ApprovalRejectDialogComponent,
				{
					width: '480px',
					height: '320px',
					disableClose: true,
				}
			);

			setComment.afterClosed().subscribe((comment) => {
				payload.comments = comment;
				this.websocketService.publishApproval(payload);
			});
		}

		// this.save();
	}

	// This function is used to get the attachments based on their types.
	public getpdfsAndImagesForPreview(entry, filePreviewField) {
		const dataId = this.route.snapshot.params['dataId'];
		const attachmentLists = entry[filePreviewField.NAME];
		this.attachmentsList = [];
		attachmentLists.forEach((attachment) => {
			this.attachmentsList.push({
				FILE: {
					url: `https://${this.userService.getSubdomain()}.ngdesk.com/api/ngdesk-data-service-v1/attachments?message_id&module_id=${
						this.module.MODULE_ID
					}&data_id=${dataId}&attachment_uuid=${
						attachment['ATTACHMENT_UUID']
					}&field_id=${filePreviewField.FIELD_ID}`,
					httpHeaders: {
						authentication_token: this.userService.getAuthenticationToken(),
					},
					withCredentials: true,
				},
				TITLE: attachment['FILE_NAME'],
				ATTACHMENT_UUID: attachment['ATTACHMENT_UUID'],
				FILE_EXTENSION: attachment['FILE_EXTENSION'],
			});
		});
	}

	// This function will be called when select the file.
	public onFileSelected() {
		const $img: any = document.querySelector('#file');
		if (typeof FileReader !== 'undefined') {
			const reader = new FileReader();
			reader.onload = (e: any) => {
				this.pdfSrc = e.target.result;
			};
			reader.readAsArrayBuffer($img.files[0]);
		}
	}

	// This method will to see the next document in the pdf viewer.
	public nextDocument() {
		this.zoom = 1;
		const currentIndex = this.attachmentsList.indexOf(this.pdfSrc);
		this.pdfSrc = this.attachmentsList[currentIndex + 1];
		this.pdf = this.pdfSrc['FILE'];
	}

	// This method will to see the previous document in the pdf viewer.
	public previousDocument() {
		this.zoom = 1;
		const currentIndex = this.attachmentsList.indexOf(this.pdfSrc);
		this.pdfSrc = this.attachmentsList[currentIndex - 1];
		this.pdf = this.pdfSrc['FILE'];
	}

	// This function is used to download the file which is rendering in the file preview.
	public downloadPdf(pdfSrc) {
		const filePreviewField = this.module['FIELDS'].find(
			(field) => field.DATA_TYPE.DISPLAY === 'File Upload'
		);
		const filePreviewFieldId = filePreviewField.FIELD_ID;
		const dataId = this.route.snapshot.params['dataId'];
		const subDomain = this.userService.getSubdomain();
		const moduleId = this.module.MODULE_ID;
		const attachmentUuid = pdfSrc['ATTACHMENT_UUID'];
		// This call will fetch the file data in blob format.
		this.filePreviewService
			.getPdf(subDomain, attachmentUuid, moduleId, dataId, filePreviewFieldId)
			.subscribe((response) => {
				// TODO: Need to work for the image file download.
				if (pdfSrc['FILE_EXTENSION'] === 'application/pdf') {
					const blob = new Blob([response], { type: 'application/pdf' });
					const downloadURL = window.URL.createObjectURL(blob);
					const link = document.createElement('a');
					link.href = downloadURL;
					link.download = pdfSrc['TITLE'];
					link.click();
				}
			});
	}

	public triggerWorkflow(fieldId) {
		const field = this.module.FIELDS.find(
			(moduleField) => moduleField.FIELD_ID === fieldId
		);
		if (field) {
			const workflowPayload = {
				WORKFLOW_ID: field.WORKFLOW,
				MODULE_ID: this.module.MODULE_ID,
				DATA_ID: this.entry.DATA_ID,
				USER_ID: this.usersService.user.DATA_ID,
			};
			this.websocketService.publishMessage(workflowPayload);
		}
	}

	// Inherit the properties from the parent modules for one to one and many to one fields.
	public inheritValues(fieldId, entryId) {
		const parentFieldsMap = new Map<any, any>();
		const resultMap = new Map<any, any>();
		const map = new Map<any, any>();
		const field = this.fieldsMap[fieldId];
		const childModuleId = field.MODULE;
		// Get the child module.
		const childModule = this.cacheService.companyData['MODULES'].find(
			(module) => module.MODULE_ID === childModuleId
		);
		childModule.FIELDS.forEach((field) => {
			parentFieldsMap.set(field.FIELD_ID, field);
		});
		// Get the ineritance map
		if (
			field.INHERITANCE_MAPPING !== undefined &&
			field.INHERITANCE_MAPPING !== null
		) {
			const inheritanceMap: Map<string, any> = new Map(
				Object.entries(field.INHERITANCE_MAPPING)
			);
			// Fetch the child module entry using entryId
			this.cacheService
				.getPrerequisiteForDetaiLayout(childModule.MODULE_ID, entryId)
				.subscribe((responseList) => {
					const entry = responseList[1].entry;
					const keysList = Array.from(inheritanceMap.keys());
					// Assign the parent field's inerited values
					keysList.forEach((key) => {
						const parentField = parentFieldsMap.get(key);
						if (parentField) {
							map.set(
								parentFieldsMap.get(key).FIELD_ID,
								entry[parentField.NAME]
							);
						}

						resultMap.set(inheritanceMap.get(key), map.get(key));
						const field = this.fieldsMap[inheritanceMap.get(key)];
						this.entry[field.NAME] = resultMap.get(inheritanceMap.get(key));

						// if inherited values are relationship
						// needs to set data for form control
						if (
							field.DATA_TYPE.DISPLAY === 'Relationship' &&
							(field.RELATIONSHIP_TYPE === 'One to One' ||
								field.RELATIONSHIP_TYPE === 'Many to One')
						) {
							const event = {
								option: {
									value: resultMap.get(inheritanceMap.get(key)),
								},
							};

							const fieldControlName =
								field.FIELD_ID.replace(/-/g, '_') + 'Ctrl';

							this.addDataForRelationshipField(field, event, fieldControlName);
						}
					});
				});
		}
	}

	// To create URL For Image Preview

	public createURLForImagePreview(uuid, field) {
		let subDomain = this.userService.getSubdomain();
		const url = `https://${subDomain}.ngdesk.com/api/ngdesk-data-service-v1/attachments?
		message_id&module_id=${this.module.MODULE_ID}&data_id=${this.entry.DATA_ID}&attachment_uuid=${uuid}&field_id=${field.FIELD_ID}`;
		this.imageURL = url;
		this.imageviewerService.getImageURL(this.imageURL);
	}

	public deleteImage() {
		this.imageAttachments = [];
		this.imageURL = '';
		this.imageviewerService.getImageURL(this.imageURL);
		this.gridLayoutService.buildTemplates(
			this.panels,
			this.module,
			this.customModulesService.layoutType,
			this.layout
		);
	}

	// to post attachments on click of save
	// called only if attachements are there

	public postAttachments(
		OnclickSaveButton,
		attachmentsArray,
		field,
		fieldName
	) {
		this.renderDetailDataSerice.postAttachments(attachmentsArray).subscribe(
			(attachmentResponse: any) => {
				this.newAttachmentResponse = [];
				//adding FILE_EXTENSION key to attachmentResponse
				attachmentResponse.forEach((element) => {
					attachmentsArray.forEach((fileExtensons) => {
						if (element.FILE_NAME === fileExtensons.FILE_NAME) {
							this.newAttachmentResponse.push({
								ATTACHMENT_UUID: element.ATTACHMENT_UUID,
								FILE_NAME: element.FILE_NAME,
								HASH: element.HASH,
								FILE_EXTENSION: fileExtensons.FILE_EXTENSION,
							});
						}
					});
				});
				if (fieldName == 'File Upload') {
					this.generalAttachments = [];
					// to filter unuploaded docs
					if (this.entry[field.NAME] && this.entry[field.NAME].length > 0) {
						this.entry[field.NAME].map((item) => {
							if (item.ATTACHMENT_UUID) {
								this.generalAttachments.push(item);
							}
						});
					}

					// to attach new docs
					this.newAttachmentResponse.forEach((doc) => {
						this.generalAttachments.push(doc);
					});
					this.isFileadded = true;
					this.entry[field.NAME] = JSON.parse(
						JSON.stringify(this.generalAttachments)
					);
				}

				if (fieldName == 'Image') {
					this.imageAttachments = this.newAttachmentResponse;
					this.isImageAdded = true;
					this.entry[field.NAME] = JSON.parse(
						JSON.stringify(this.imageAttachments)
					);
				}
				let payload = JSON.parse(JSON.stringify(this.entry));
				payload = this.renderDetailDataSerice.formatDiscussion(
					payload,
					this.module,
					this.attachments
				);

				// below code is to avoid duplicate entry
				if (
					(this.generalAttachments.length > 0 &&
						this.imageAttachments.length == 0) ||
					(this.generalAttachments.length == 0 &&
						this.imageAttachments.length > 0)
				) {
					this.doPostOrPutCall(payload, OnclickSaveButton);
				}

				//to make api call once  only after file and image docs uploaded
				if (
					this.generalAttachments.length > 0 &&
					this.imageAttachments.length > 0 &&
					this.isImageAdded &&
					this.isFileadded
				) {
					this.doPostOrPutCall(payload, OnclickSaveButton);
				}
			},
			(error) => {
				this.loaderService.isLoading2 = false;
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public condensePayload() {
		let entryUpdated = this.entry;
		const properties = Object.keys(entryUpdated);
		properties.forEach((property) => {
			if (entryUpdated[property] === null) {
				delete entryUpdated[property];
			}
		});
		this.entry = entryUpdated;
	}
	// to attach selectedoptions for condition object
	// api call to get edition and version details for values field
	//passing Software Installation Module id
	public addFieldDataForConditionField(event, field) {
		this.conditionTypeFieldName = field.NAME;
		this.fieldsListForCondition.map((item) => {
			if (item.DISPLAY_LABEL == event.option.value) {
				this.selectedConditionField = item;
				this.selectedFieldName = item.DISPLAY_LABEL;
			}
		});
		this.conditionFieldData.CONDITION = this.selectedConditionField.FIELD_ID;
		this.conditionService
			.getValuesForCondition(
				this.softwareInstalationModule.MODULE_ID,
				this.selectedConditionField.FIELD_ID
			)
			.subscribe((results: any) => {
				if (results.DATA.length > 0) {
					this.valuesArray = [];
					this.filteredValuesArray = [];

					this.valuesArray = results.DATA;
					this.filteredValuesArray = results.DATA;
				}
			});
	}

	public addOptionDataForConditionField(event) {
		this.conditionFieldData.OPERATOR = event.option.value;
	}

	public addValueDataForConditionField(event) {
		this.conditionFieldData.VALUE = event.option.value;
	}

	//passing Software Installation Module id
	getFieldsForConditionDataType() {
		this.modulesService
			.getAllModules()
			.pipe(
				concatMap((allModules) => {
					this.softwareInstalationModule = allModules['MODULES'].find(
						(module) => module.NAME == 'Software Installation'
					);
					return this.cacheService.getModule(
						this.softwareInstalationModule.MODULE_ID
					);
				})
			)
			.subscribe((data) => {
				this.fieldsListForCondition = data.FIELDS;
				this.fieldsListForCondition.map((item) => {
					if (item.FIELD_ID == this.conditionFieldData.CONDITION) {
						this.selectedFieldName = item.DISPLAY_LABEL;
					}
				});
			});
	}

	onValueChange(search) {
		if (this.valuesArray.length == 0 && this.conditionFieldData.CONDITION) {
			this.conditionService
				.getValuesForCondition(
					this.softwareInstalationModule.MODULE_ID,
					this.conditionFieldData.CONDITION
				)
				.subscribe((results: any) => {
					if (results.DATA.length > 0) {
						this.valuesArray = [];
						this.filteredValuesArray = [];

						this.valuesArray = results.DATA;
						this.filteredValuesArray = results.DATA;
					}
				});
		}

		if (search && search != '') {
			this.filteredValuesArray = [];
			search = search.toLocaleLowerCase();
			this.filteredValuesArray = this.valuesArray.filter((value: string) => {
				return value.toLocaleLowerCase().includes(search);
			});
		} else {
			this.filteredValuesArray = this.valuesArray;
		}
	}

	public fieldsMapping(fieldName?) {
		if (fieldName) {
			const checkboxField = this.module.FIELDS.find(
				(field) => field.NAME === fieldName
			);
			if (
				checkboxField !== undefined &&
				checkboxField.DATA_TYPE.DISPLAY === 'Checkbox'
			) {
				if (checkboxField.FIELDS_MAPPING) {
					const fieldsMapping: Map<string, any> = new Map(
						Object.entries(checkboxField.FIELDS_MAPPING)
					);
					const keysList = Array.from(fieldsMapping.keys());

					keysList.forEach((key) => {
						const keyField = this.fieldsMap[key];
						const valueField = this.fieldsMap[fieldsMapping.get(key)];
						if (this.entry[fieldName]) {
							this.entry[valueField.NAME] = this.entry[keyField.NAME];
						} else {
							this.entry[valueField.NAME] = '';
						}
					});
				}
			}
		} else {
			// TODO: make the texts dynamic
			// this.module.FIELDS.forEach((field) => {
			// 	if (field.DATA_TYPE.DISPLAY === 'Checkbox' && field.FIELDS_MAPPING) {
			// 		let tempMap = new Map<String, String>();
			// 		tempMap =  field.FIELDS_MAPPING;
			// 		const fieldKeys = Array.from(tempMap.keys());
			// 		let fieldsMapObject =  new Map<String, Object>();
			// 		fieldKeys.forEach(fieldKey => {
			// 			const mapObject = {
			// 				checkbox:  field.FIELD_ID,
			// 				fieldMapped: tempMap.get(fieldKey)
			// 			}
			// 			fieldsMapObject.set(fieldKey, mapObject);
			// 		});
			// 			this.fieldMappingObject = fieldsMapObject;
			// 	}
			// });
		}
	}

	public showRejectedInformation() {
		const setComment = this.renderDetailHelper.dialog.open(
			ApprovalRejectInformationDialogComponent,
			{
				width: '640px',
				height: '480px',
				data: this.customModulesService.approvalStatusObject,
			}
		);

		setComment.afterClosed().subscribe((comment) => {});
	}

	public getcalculatedValuesForFormula() {
		this.customModulesService
			.calculateFormula(this.module, this.entry)
			.subscribe((results: any) => {
				if (results) {
					this.formulaFields.forEach((field) => {
						if (
							field.DATA_TYPE.DISPLAY === 'List Formula' &&
							results[field.NAME]
						) {
							this.entry[field.NAME] = results[field.NAME];
						}
						if (
							field.DATA_TYPE.DISPLAY !== 'List Formula' &&
							results[field.NAME] &&
							Number(results[field.NAME])
						) {
							const value = +(Math.round(results[field.NAME] * 100) / 100);
							this.entry[field.NAME] =
								this.customModulesService.transformNumbersField(
									value,
									field.NUMERIC_FORMAT,
									field.PREFIX,
									field.SUFFIX
								);
						} else if (
							field.DATA_TYPE.DISPLAY !== 'List Formula' &&
							results[field.NAME] &&
							!Number(results[field.NAME])
						) {
							const value = results[field.NAME];
							this.entry[field.NAME] =
								this.customModulesService.transformNumbersField(
									value,
									field.NUMERIC_FORMAT,
									field.PREFIX,
									field.SUFFIX
								);
						} else if (
							field.DATA_TYPE.DISPLAY === 'List Formula' &&
							results[field.NAME] &&
							results[field.NAME] !== null &&
							results[field.NAME].length > 0
						) {
							results[field.NAME].forEach((element) => {
								if (Number(element['VALUE'])) {
									const value = +(Math.round(element['VALUE'] * 100) / 100);
									element['VALUE'] =
										this.customModulesService.transformNumbersField(
											value,
											field.NUMERIC_FORMAT,
											field.PREFIX,
											field.SUFFIX
										);
								}
							});
							this.entry[field.NAME] = results[field.NAME];
						}
					});
				}
				return this.entry;
			});
	}

	public getCalculatedExchangeRate() {
		this.currencyExchangeFields.forEach((field) => {
			let toCurrencyField = this.fieldsMap[field.TO_CURRENCY];
			let fromCurrencyField = this.fieldsMap[field.FROM_CURRENCY];
			let dateIncurredField = this.fieldsMap[field.DATE_INCURRED];
			if (
				this.entry[dateIncurredField.NAME] !== undefined &&
				this.entry[dateIncurredField.NAME] !== null &&
				this.entry[fromCurrencyField.NAME] !== undefined &&
				this.entry[fromCurrencyField.NAME] !== null &&
				this.entry[toCurrencyField.NAME] !== undefined &&
				this.entry[toCurrencyField.NAME] !== null
			) {
				this.customModulesService
					.calculateExchangeRate(
						this.module.MODULE_ID,
						field.FIELD_ID,
						this.entry
					)
					.subscribe((response: any) => {
						if (response) {
							this.entry[field.NAME] = response;
							this.getcalculatedValuesForFormula();
						}
					});
			}
		});

		return this.entry;
	}

	public createURLForReceiptPreview(uuid, field) {
		let subDomain = this.userService.getSubdomain();
		if (uuid && subDomain) {
			const url = `https://${subDomain}.ngdesk.com/api/ngdesk-data-service-v1/attachments?message_id&module_id=${this.module.MODULE_ID}&data_id=${this.entry.DATA_ID}&attachment_uuid=${uuid}&field_id=${field}`;
			return url;
		}
	}

	SaveAttachnent(uuid, field) {
		let subDomain = this.userService.getSubdomain();
		if (uuid && subDomain) {
			const url = `https://${subDomain}.ngdesk.com/api/ngdesk-data-service-v1/attachments?message_id&module_id=${this.module.MODULE_ID}&data_id=${this.entry.DATA_ID}&attachment_uuid=${uuid}&field_id=${field}`;
			window.open(url, '_blank');
		}
	}

	public removeReceiptCaptured(fieldName) {
		this.receiptAttachments = [];
		this.entry[fieldName] = null;
	}

	public openOneToManyMapDialog(field) {
		this.customModulesService.oneToManyControls['FIELD_IN_FOCUS'] = '';

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
		const that = this;
		dialogRef.afterClosed().subscribe((result) => {
			that.loadPermissionsForFields();
			if (result.length > 0) {
				this.customModulesService.isSavingOneToMany = true;
				let payload = {};
				const entryId = this.entry['DATA_ID'];
				payload['DATA_ID'] = entryId;
				payload[field.NAME] = result;
				this.dataService
					.putModuleEntry(this.module['MODULE_ID'], payload, false)
					.subscribe(
						(response) => {
							this.computeAggregationFields();
							this.customModulesService.isSavingOneToMany = false;
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
							});
						},
						(error) => {
							this.customModulesService.isSavingOneToMany = false;
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			}
		});
	}

	private openOneToManyCreateLayoutDialog(moduleId, field) {
		this.customModulesService.loadVariablesForModule(
			this.module,
			this.entry,
			this.createLayout
		);

		this.cacheService.getModule(field.MODULE).subscribe((response: any) => {
			if (response.NAME !== 'Users') {
				this.customModulesService.oneToManyControls['FIELD_IN_FOCUS'] = '';
				// generate unique id based on index for each dialog
				let dialogId = `render-detail-dialog_0`;
				if (this.renderDetailHelper.dialog.openDialogs.length > 0) {
					dialogId = `render-detail-dialog_${this.renderDetailHelper.dialog.openDialogs.length}`;
				}

				if (
					(this.modalData && this.modalData.DATA_ID !== undefined) ||
					this.entry?.DATA_ID
				) {
					this.customModulesService.oneToManyRelationshipData.PARRENT_ENTRY =
						this.entry;
					this.customModulesService.oneToManyRelationshipData.PARRENT_MADULE_ID =
						this.module['MODULE_ID'];
					this.customModulesService.oneToManyRelationshipData.CURRENT_FIELD =
						field;
				}

				const dialogs = this.renderDetailHelper.dialog.openDialogs.length;
				const renderDetail = this.renderDetailHelper.dialog.open(
					RenderDetailNewComponent,
					{
						width: '1024px',
						height: '768px',
						id: dialogId,
						disableClose: true,
						data: {
							MODULE_ID: moduleId,
							PARENT_MODULE_ID: this.module['MODULE_ID'],
							FIELD: field,
							IS_EDIT: false,
							FORM_CONTROLS: this.customModulesService.formControls,
						},
					}
				);
				renderDetail.afterClosed().subscribe((modalData) => {
					this.customModulesService.oneToManyRelationshipData = {
						VALUE: {},
						FIELD_NAME: '',
						PARRENT_MADULE_ID: '',
						PARRENT_ENTRY: {},
						CURRENT_FIELD: {},
					};

					this.loadPermissionsForFields();
					this.customModulesService.layoutType = 'edit';
					this.modalData = null;
					this.module['MODULE_ID'] = this.route.snapshot.params['moduleId'];
					this.entry['DATA_ID'] = this.route.snapshot.params['dataId'];
					if (!modalData.hasOwnProperty('cancel')) {
						this.save('saveFromDialog');
					}
					//this.computeAggregationFields();
				});
			} else {
				const inviteUser = this.dialogHelper.inviteUsers();
				inviteUser.afterClosed().subscribe((result) => {
					this.loadPermissionsForFields();
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
						});
					this.customModulesService.oneToManyRelationshipData = {
						VALUE: {},
						FIELD_NAME: '',
						PARRENT_MADULE_ID: '',
						PARRENT_ENTRY: {},
						CURRENT_FIELD: {},
					};
				});
			}
		});
	}

	public closeCreateOneToManyDialog(cancel?) {
		let modalData;
		if (this.modalData.FIELD) {
			modalData = {
				dialogFieldId: this.modalData.FIELD.FIELD_ID,
				formControls: this.modalData.FORM_CONTROLS,
				relationFieldFilteredEntries:
					this.modalData.RELATION_FIELD_FILTERED_ENTRIES,
			};
		} else {
			modalData = {
				dataId: this.modalData.DATA_ID,
				moduleId: this.modalData.MODULE_ID,
			};
		}
		if (cancel) {
			modalData['cancel'] = true;
		}
		this.renderDetailDialogRef.close(modalData);
	}

	public removeOneToManyEntry(element, field) {
		const newData = [];
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
	loadPermissionsForFields() {
		let moduleID;
		let dataID;
		const that = this;
		if (that.modalData) {
			if (that.modalData.DATA_ID === undefined) {
				moduleID = that.modalData.MODULE_ID;
				dataID = 'new';
			} else {
				moduleID = that.modalData.MODULE_ID;
				dataID = that.modalData.DATA_ID;
			}
		} else {
			moduleID = that.route.snapshot.params['moduleId'];
			dataID = that.route.snapshot.params['dataId'];
		}
		that.renderDetailDataSerice
			.getFieldPermissionValues(
				moduleID,
				that.customModulesService.layoutType,
				dataID
			)
			.subscribe((permissions) => {
				that.customModulesService.disableFieldBasedOnFieldPermission(
					permissions
				);
			});
	}

	public navigateTorelationEntry(relatedModuleId: any, currentField) {
		const relatedModule = this.cacheService.companyData['MODULES'].find(
			(module) => module.MODULE_ID === relatedModuleId
		);
		let relationentry = this.entry[currentField.NAME];
		if (relationentry && relationentry.DATA_ID) {
			this.router.navigate([
				`render/${relatedModule.MODULE_ID}/edit/${relationentry.DATA_ID}`,
			]);
		}
	}

	public navigateToManyToOneEntry(entry, field) {
		window.localStorage.setItem('previousUrl', this.router.url);
		this.router.navigate([`render/${field.MODULE}/edit/${entry.DATA_ID}`]);
	}

	public navigateBack() {
		this.router.navigate([window.localStorage.getItem('previousUrl')]);
		window.localStorage.removeItem('previousUrl');
	}

	public onChangeSelectionList(event, field) {
		this.entry[field.NAME] = event.option.selectionList._value;
		this.getcalculatedValuesForFormula();
	}

	public compareFn(op1, op2) {
		return op1.FORMULA_NAME === op2.FORMULA_NAME;
	}

	public getFormulaListValue(fieldName, formulaName) {
		if (this.entry[fieldName]) {
			const formula = this.entry[fieldName].find(
				(field) => field.FORMULA_NAME === formulaName
			);
			if (formula && formula.VALUE) {
				return formula.VALUE;
			}
		}

		return '';
	}

	// chat related code

	public triggerFunction(event, chatmessage) {
		// enterToSend is checkbox in chat for if "press enter to submit" is preferred
		// this.fileSizeLimit = true;
		if (event.keyCode === 16 && event.keyCode === 13) {
			chatmessage += '\n';
		} else if (event.keyCode === 13) {
			this.publishMessages(chatmessage);
		}
	}

	public publishMessages(chatmessage) {
		let messageBody = chatmessage;
		const linkifyStr = require('linkifyjs/string');
		messageBody = linkifyStr(messageBody, {});
		const htmlMsg = `<html> <head></head> <body>${messageBody}</body> </html>`;

		this.renderDetailDataSerice
			.postAttachments(this.attachments)
			.subscribe((attachmentResponse: any) => {
				this.attachments = [];
				const msgObj = {
					agentDataID: this.userService.user.DATA_ID,
					customerDataID: this.entry.REQUESTOR.DATA_ID,
					sessionUUID: this.entry.SESSION_UUID,
					discussionMessage: {
						ATTACHMENTS: JSON.parse(JSON.stringify(attachmentResponse)),
						COMPANY_SUBDOMAIN: this.userService.getSubdomain(),
						ENTRY_ID: this.route.snapshot.params['dataId'],
						MESSAGE: htmlMsg,
						MESSAGE_ID: '',
						MESSAGE_TYPE: 'MESSAGE',
						MODULE_ID: this.module['MODULE_ID'],
						SENDER: {
							FIRST_NAME: this.userService.user.FIRST_NAME,
							LAST_NAME: this.userService.user.LAST_NAME,
							ROLE: this.userService.user.ROLE,
							USER_UUID: this.userService.user.USER_UUID,
						},
					},
				};
				this.websocketService.publishMessage(msgObj);
			});

		this.customModulesService.discussionControls['MESSAGE'] = '';
	}

	public getChatChannelDetails() {
		this.chatDataService.getChatChannel().subscribe((chatChannel: any) => {
			this.chatChannel = chatChannel.CHAT_CHANNEL;
			this.setSenderColors(
				this.chatChannel.senderBubbleColor,
				this.chatChannel.senderTextColor
			);
			this.setReceiverColors(
				this.chatChannel.receiverBubbleColor,
				this.chatChannel.receiverTextColor
			);
		});
	}
	// to get Plain text from premade responces
	// using for chats
	public convertHTMLToPlainText() {
		let tempHtml = document.createElement('div');
		tempHtml.innerHTML =
			this.customModulesService.discussionControls['MESSAGE'];
		this.customModulesService.discussionControls['MESSAGE'] =
			tempHtml.innerText || tempHtml.textContent;
	}

	public getCustomerForAgent() {
		this.chatDataService
			.getChatsByUserId(this.module['MODULE_ID'], this.chatFilters)
			.subscribe((users: any) => {
				this.customersForAgent = [];
				users.DATA.forEach((user) => {
					if (user.REQUESTOR) {
						this.customersForAgent.push(user);
						this.currentUserStatus = user.STATUS;
					}
				});
				console.log(this.customersForAgent);
			});
	}

	public loadUserChatDetails(user) {
		this.router.navigate([
			`render/${this.module['MODULE_ID']}/edit/${user.DATA_ID}`,
		]);
	}

	public loadUserDetailsByRequestorId(userID) {
		this.modulesService.getAllModules().subscribe((allModules: any) => {
			this.allModules = allModules['MODULES'];
			const contactsModule = this.allModules.find(
				(module) => module.NAME === 'Contacts'
			);

			this.cacheService
				.getPrerequisiteForDetaiLayout(contactsModule['MODULE_ID'], userID)
				.subscribe((customerDetails: any) => {
					// console.log(customerDetails);

					if (customerDetails[1].hasOwnProperty('entry')) {
						this.customerDetail = customerDetails[1].entry;
					} else {
						this.customerDetail = customerDetails[1];
					}

					// console.log(this.entry);
				});
		});
	}

	public closeSession() {
		this.chatboxDisabled = true;

		const msgObj = {
			agentDataID: this.userService.user.DATA_ID,
			customerDataID: this.entry.REQUESTOR.DATA_ID,
			sessionUUID: this.entry.SESSION_UUID,
			discussionMessage: {
				ATTACHMENTS: [],
				COMPANY_SUBDOMAIN: this.userService.getSubdomain(),
				ENTRY_ID: this.route.snapshot.params['dataId'],
				MESSAGE:
					' Session has been closed by ' +
					this.userService.user.FIRST_NAME +
					' ' +
					this.userService.user.LAST_NAME,
				MESSAGE_ID: '',
				MESSAGE_TYPE: 'MESSAGE',
				MODULE_ID: this.module['MODULE_ID'],
				SENDER: {
					FIRST_NAME: this.userService.user.FIRST_NAME,
					LAST_NAME: this.userService.user.LAST_NAME,
					ROLE: this.userService.user.ROLE,
					USER_UUID: this.userService.user.USER_UUID,
				},
			},
		};

		this.websocketService.publishMessage(msgObj);

		const endChatObj = {
			sessionUUID: this.entry['SESSION_UUID'],
			subdomain: this.userService.getSubdomain(),
			isSendChatTranscript: false,
			isAgentCloseChat: true,
		};

		this.websocketService.publishMessage(endChatObj);
	}

	public vlidateCloseSessionMessage(message) {
		const ClosedMessage =
			' Session has been closed by ' +
			this.userService.user.FIRST_NAME +
			' ' +
			this.userService.user.LAST_NAME;
		if (message == ClosedMessage) {
			return true;
		} else {
			return false;
		}
	}

	public setSenderColors(bgColor, textColor) {
		this.themeWrapper.style.setProperty('--chatSenderBubbleColor', bgColor);
		this.themeWrapper.style.setProperty('--chatSenderTextColor', textColor);
	}

	public setReceiverColors(bgColor, textColor) {
		this.themeWrapper.style.setProperty('--chatReceiverBubbleColor', bgColor);
		this.themeWrapper.style.setProperty('--chatReceiverTextColor', textColor);
	}

	public getChatModuleFields() {
		this.fieldsForChatFilter = [];
		this.module['FIELDS'].filter((field) => {
			if (
				field.DATA_TYPE.DISPLAY == 'Date/Time' ||
				(field.DATA_TYPE.DISPLAY == 'Picklist' && field.NAME == 'STATUS') ||
				(field.RELATIONSHIP_TYPE == 'Many to One' && field.NAME == 'REQUESTOR')
			) {
				this.fieldsForChatFilter.push(field);
			}
		});
	}

	public filterFieldSelection(field) {
		this.filterField = {
			FIELD: field,
			OPERATOR: 'equals to',
			VALUE: '',
			REQUIREMENT_TYPE: 'All',
		};

		if (field.RELATIONSHIP_TYPE === 'Many to One') {
			this.filterField.FIELD['RELATION_FIELD_VALUE'] = this.customersForAgent;
		}
		this.operators = this.conditionsService.setOperators(field);
	}

	public applyFilter() {
		const filterField = {
			condition: this.filterField.FIELD['FIELD_ID'],
			operator: this.filterField.OPERATOR,
			conditionValue: this.filterField.VALUE,
			requirementType: this.filterField.REQUIREMENT_TYPE,
		};
		this.chatFilters.push(filterField);
		this.getCustomerForAgent();
		this.isFilterActive = false;
		this.filterField = {
			FIELD: '',
			OPERATOR: 'equals to',
			VALUE: '',
			REQUIREMENT_TYPE: 'All',
		};
	}

	public removeFilter(index) {
		this.chatFilters.splice(index, 1);
		this.getCustomerForAgent();
	}

	public getRequestorById(requestorId) {
		let requestorName;
		if (this.customersForAgent && this.customersForAgent.length > 0) {
			this.customersForAgent.find((user) => {
				if (user.REQUESTOR.DATA_ID === requestorId) {
					requestorName =
						user.REQUESTOR.FIRST_NAME + ' ' + user.REQUESTOR.LAST_NAME;
				}
			});
		}

		return requestorName;
	}
}
