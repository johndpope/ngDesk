import { transition } from '@angular/animations';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { CdkTableModule } from '@angular/cdk/table';
import {
	ChangeDetectorRef,
	Component,
	ElementRef,
	EventEmitter,
	Inject,
	Input,
	OnDestroy,
	OnInit,
	Output,
	TemplateRef,
	ViewChild,
	Optional,
} from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import {
	MatAutocompleteModule,
	MatAutocompleteSelectedEvent,
} from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipInputEvent, MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { TranslateService } from '@ngx-translate/core';
import { CheckBox } from '@nstudio/nativescript-checkbox';
import { ChannelsService } from '@src/app/channels/channels.service';
import { ChangePasswordDialogComponent } from '@src/app/dialogs/change-password-dialog/change-password-dialog.component';
import * as _moment from 'moment';
import {
	OWL_DATE_TIME_FORMATS,
	OwlDateTimeModule,
	OwlNativeDateTimeModule,
} from 'ng-pick-datetime';
import { OwlMomentDateTimeModule } from 'ng-pick-datetime/date-time/adapter/moment-adapter/moment-date-time.module';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import { FileDetector, Key } from 'protractor';
import { combineLatest, of, Subject } from 'rxjs';
import {
	debounceTime,
	delay,
	distinctUntilChanged,
	finalize,
	map,
	shareReplay,
	startWith,
	switchMap,
	takeUntil,
} from 'rxjs/operators';
import { Subscription } from 'rxjs/Subscription';
import tinymce from 'tinymce/tinymce';
import { ListPicker } from 'tns-core-modules/ui/list-picker/list-picker';
import { AppGlobals } from '../../app.globals';
import { AttachmentsService } from '../../attachments/attachments.service';
import { CompaniesService } from '../../companies/companies.service';
import { RolesService } from '../../company-settings/roles/roles-old.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { LoaderService } from '../../custom-components/loader/loader.service';
import { CustomTableService } from '../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../dialogs/confirm-dialog/confirm-dialog.component';
import { LearnMoreDialogComponent } from '../../dialogs/learn-more-dialog/learn-more-dialog.component';
import { Condition } from '../../models/condition';
import { DiscussionMessage } from '../../models/discussion-message';
import { ColumnShow, ListLayout, OrderBy } from '../../models/list-layout';
import { ModulesService } from '../../modules/modules.service';
import { NotificationsService } from '../../notifications/notifications.service';
import { SharedModule } from '../../shared-module/shared.module';
import { FilePreviewOverlayRef } from '../../shared/file-preview-overlay/file-preview-overlay-ref';
import { FilePreviewOverlayService } from '../../shared/file-preview-overlay/file-preview-overlay.service';
// import { config } from '../../tiny-mce/tiny-mce-config';
import { UsersService } from '../../users/users.service';
import { WalkthroughService } from '../../walkthrough/walkthrough.service';
import { RenderDetailHelper } from '../render-detail-helper/render-detail-helper';
import { RenderLayoutService } from '../render-layout.service';
import { Stomp } from '@stomp/stompjs/esm5';
import { Page } from 'tns-core-modules/ui/page/page';
import { Street1Service } from '../data-types/street1.service';
import { Street2Service } from '../data-types/street2.service';
import { CityService } from '../data-types/city.service';
import { CountryService } from '../data-types/country.service';
import { AutoNumberService } from '../data-types/auto-number.service';
import { ButtonService } from '../data-types/button.service';
import { CheckboxService } from '../data-types/checkbox.service';
import { CurrencyService } from '../data-types/currency.service';
import { ChronometerService } from '../data-types/chronometer.service';
import { DateTimeService } from '../data-types/date-time.service';
import { DateService } from '../data-types/date.service';
import { FileUploadService } from '../data-types/file-upload.service';
import { FormulaService } from '../data-types/formula.service';
import { ListTextService } from '../data-types/list-text.service';
import { NumberService } from '../data-types/number.service';
import { PhoneService } from '../data-types/phone.service';
import { PicklistService } from '../data-types/picklist.service';
import { RelationshipService } from '../data-types/relationship.service';
import { TextAreaService } from '../data-types/text-area.service';
import { TimeService } from '../data-types/time.service';
import { TextService } from '../data-types/text.service';
import { WebsocketService } from '@src/app/websocket.service';
import { functions } from 'firebase';
import { CurrencyExchangeService } from '../data-types/currency-exchange.service';
// import { Label} from 'tns-core-modules/ui/label';
// import { TouchGestureEventData, TouchAction } from '@nativescript/core/ui/gestures/gestures';
// import { StackLayout } from 'tns-core-modules/ui/layouts/stack-layout/stack-layout';

export interface CurrentWorkflow {
	name: string;
	startDate: string;
	currentStep: string;
}

const currentWorkflowsData: CurrentWorkflow[] = [
	{ name: 'Hydrogen', startDate: '2020-03-30T17:04', currentStep: 'Edit' },
	{ name: 'Helium', startDate: '2020-03-30T17:04', currentStep: 'Edit' },
	{ name: 'Lithium', startDate: '2020-03-30T17:04', currentStep: 'Edit' },
	{ name: 'Beryllium', startDate: '2020-03-30T17:04', currentStep: 'Edit' },
	{ name: 'Boron', startDate: '2020-03-30T17:04', currentStep: 'Edit' },
];

export interface PO {
	name: string;
	birthDate: string;
	actions: string;
}

const poData: PO[] = [
	{ name: 'Hydrogen', birthDate: '2020-03-30T17:04', actions: 'Edit' },
	{ name: 'Helium', birthDate: '2020-03-30T17:04', actions: 'Edit' },
	{ name: 'Lithium', birthDate: '2020-03-30T17:04', actions: 'Edit' },
	{ name: 'Beryllium', birthDate: '2020-03-30T17:04', actions: 'Edit' },
	{ name: 'Boron', birthDate: '2020-03-30T17:04', actions: 'Edit' },
];

export const MY_CUSTOM_FORMATS = {
	parseInput: 'LL LT',
	fullPickerInput: 'LL LT',
	datePickerInput: 'L',
	timePickerInput: 'LT',
	monthYearLabel: 'MMM YYYY',
	dateA11yLabel: 'LL',
	monthYearA11yLabel: 'MMM YYYY',
};

@Component({
	selector: 'app-render-detail-layout',
	templateUrl: './render-detail-layout.component.html',
	styleUrls: ['./render-detail-layout.component.scss'],
	// providers: [
	// 	// {
	// 	//   provide: DateTimeAdapter,
	// 	//   useClass: OwlMomentDateTimeModule,
	// 	//   deps: [OWL_DATE_TIME_LOCALE]
	// 	// },
	// 	{ provide: OWL_DATE_TIME_FORMATS, useValue: MY_CUSTOM_FORMATS }
	// ],
})
export class RenderDetailLayoutComponent implements OnInit, OnDestroy {
	@ViewChild(MatSort, { static: true }) private sort: MatSort;
	@ViewChild(MatPaginator, { static: true }) private paginator: MatPaginator;
	@Output() public sortData = new EventEmitter<any>();
	@Output() public pageChangeEmit = new EventEmitter<any>();
	@Input() public templateRef: TemplateRef<any>;
	public filteredLayouts: ListLayout[] = [];
	public currentWorkflowDisplayedColumns: string[] = [
		'name',
		'startDate',
		'currentStep',
	];
	public currentWorkflowDataSource = new MatTableDataSource<CurrentWorkflow>(
		currentWorkflowsData
	);
	public poDisplayedColumns: string[] = ['name', 'birthDate', 'actions'];
	public poDataSource = new MatTableDataSource<PO>(poData);
	@ViewChild(MatPaginator, { static: true }) public poPaginator: MatPaginator;
	@ViewChild(MatPaginator, { static: true })
	public currentWorkflowPaginator: MatPaginator;
	public premadeResponses: any = [];
	public manyToManyMap: Map<any, any> = new Map<any, any>();
	public currentRole: any = {};
	public loading = true;
	public isModuleAllowed = false;
	public layoutExists = false;
	public layoutType: string;
	public config: any;
	private navigationSubscription: Subscription;
	public module: any = {};
	public entry: any = {};
	public layout: any = {};
	public disabled = false;
	private moduleId: string;
	private entryId: string;
	public message = '';
	public messageType = 'MESSAGE';
	public showType = 'MESSAGES';
	public discussionType = 'Messages';
	public oldChronometerValue: string;
	public tableModuleId = '';
	public tableFieldId = '';
	public chronometerValues = {};
	public lhs: any;
	public rhs: any;
	public addValue: any;
	public subValue: any;
	private tableFields;
	public mulValue: any;
	public divValue: any;
	public textLhs: any;
	public textRhs: any;
	public textSeparator: any;
	public moduleName: string;
	public concatenateValue: any;
	// private chatId: string;
	private discussionFieldName: string;
	private _destroyed$ = new Subject();
	public relationFieldEntries = {};
	public isCreateLayout = false;
	public roles;
	public rolesMap = {};
	public attachments = [];
	public dateTimeFormat: string;
	public requiredFields: string[] = [];
	public maxDiscussionRows = '3';
	public customLayout = true;
	public isChatTransfered = false;
	public putEntry = false;
	public isAssignedChat = false;
	public dialogRef: FilePreviewOverlayRef;
	private previewRef;
	private adminSignupSubscription: Subscription;
	public sections = [];
	public component = 'render';
	private chatChannel: any;
	public senderBubbleColor: string;
	public receiverBubbleColor: string;
	public senderTextColor: string;
	public receiverTextColor: string;
	public template = '';
	public moduleFields = {};
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	private allModules: any = [];
	public emptyListMessage = '';
	public relationFieldFilteredEntries = {};
	public formControls = {};
	public showVistedLinks = 2;
	public attachmentLoading = false;
	public showMessage = false;
	public allEntries: any[] = [];
	private chatSub;
	private notificationSub;
	private showNextButton = false;
	private showSaveButton = false;
	public showListLayouts: boolean;
	private saveDisabled = false;
	public clicked = false;
	public editAccess = false;
	public deleteAccess = false;
	public showRouteToEditButton = false;
	public unreadNotifications = {};
	public listData = this.renderLayoutService.getListLayoutData();
	public gridLayout = false;
	public discussionSubscribe;
	public updateSubscription;
	public tableLoaded = false;
	public showChangeButton = false;
	public filteredCountries: any = [];
	public dataSort;
	public pageTitle = '';
	public currentCreateLayout;
	public tableModuleName = '';
	public hideMatTable = false;
	public numberOfListLayout: number;
	public matTabGroupExists = false;
	public matTabGroup = [];
	public totalRecords = 0;
	public currentTable = '';
	public oneToManyCountMap = {};
	private currentListLayout: ListLayout = new ListLayout(
		'',
		'',
		'',
		'',
		new OrderBy('', ''),
		new ColumnShow([]),
		[new Condition('', '', '', '')],
		false
	);
	public entriesCount = 0;
	public titleBarTemplate = null;
	public userTemplate = {
		RIGHT_SIDEBAR: {
			OPEN: true,
		},
	};
	public oneToManyModulesToListLayoutFields = {};
	public dataMaterialModule: any = {};

	// 	imports: [
	// 		FormsModule,
	// 		ReactiveFormsModule,
	// 		FlexLayoutModule,
	// 		CdkTableModule,
	// 		MatAutocompleteModule,
	// 		MatButtonModule,
	// 		MatCardModule,
	// 		MatCheckboxModule,
	// 		MatChipsModule,
	// 		MatDatepickerModule,
	// 		MatDialogModule,
	// 		MatExpansionModule,
	// 		MatIconModule,
	// 		MatInputModule,
	// 		MatListModule,
	// 		MatMenuModule,
	// 		MatNativeDateModule,
	// 		MatRadioModule,
	// 		MatRippleModule,
	// 		MatSelectModule,
	// 		MatSlideToggleModule,
	// 		MatSidenavModule,
	// 		MatTableModule,
	// 		MatToolbarModule,
	// 		MatTooltipModule,
	// 		TranslateModule,
	// 		SharedModule,
	// 		OwlDateTimeModule,
	// 		OwlNativeDateTimeModule,
	// 		OwlMomentDateTimeModule,
	// 		MatTabsModule,
	// 		NgxMatSelectSearchModule,
	// 		MatPaginatorModule,
	// 		MatSortModule
	// 	],
	// 	exports: [],
	// };
	public grids: any[][] = [[]];
	public customLayoutTemplate = `<div [ngStyle]="{'height': '100vh'}"
   fxFlex fxLayoutAlign="center center"><mat-spinner></mat-spinner></div>`;

	public fieldsMap: any = [];
	public customLayouts = [];
	public globalIndex = 0;
	private conditionFields = [];
	public layoutStyle: any;
	public enterToSend = false;
	private listLayoutData = [];
	public layoutButtonShow: boolean;
	public recordName: string;
	public layoutName: String;
	private titleBar = [];
	public fieldArray: any = [];
	public EditfieldArray: any = [];
	public data: any;
	public metaDataArray: any = [];
	public objectKeys = Object.keys;
	public detailLayout = true;
	public textFieldValue: string;
	public showPicklistPicker = false;
	public moduleResponse: any;
	public listRelationshipData = new Map();
	public relationshipPicklistValues: any = {};
	public relationshipFieldValue: any = {};
	public showRelationshipPicker = false;
	public formControlsMobile = {};
	public relationFieldEntriesOneToMany: any = {};
	public relationshipDetailValues = {};
	public layoutFieldIds: any;
	public responseArray: any = [];
	public entriesArray: any = [];
	public dialogOpen = false;
	public countryCode: any = [];
	public countryName: any = [];
	public picklistShow = true;
	@Input() public tap: any;
	public picklistValues: any = {};
	public isloadingData = true;
	public mobileTitle: String = ' ';
	private _snackBar: any;
	private scrollSubject = new Subject<any>();
	public onSubmitting = false;
	public layoutTypeMobile: string;
	public previousUrl: string;
	public isAndroidDevice = false;
	public isIosDevice = false;
	private oneToManyCreateData = new Map<String, Object>();
	public chats = [];
	public moduleNameMobile: string;
	public stackPanelHeight: number = 0;
	public messageShowType = 'MESSAGE';
	public changeMessageType = false;
	public messageTypeOption = ['Messages', 'Internal Comments', 'Events', 'All'];
	public showMessages = false;
	public selectedOptionIndex = 0;
	public showDiscussion = false;
	public discussionMessage = '';
	public sendButtonEnabled = false;
	public createLayoutTableData = new Map<String, any>();
	// private stompClient: StompClient;
	// public aDelegate: StompClientCallback;

	constructor(
		private cd: ChangeDetectorRef,
		@Optional()
		private renderDetailDialogRef: MatDialogRef<RenderDetailLayoutComponent>,
		@Optional() @Inject(MAT_DIALOG_DATA) public modalData: any,
		private globals: AppGlobals,
		private modulesService: ModulesService,
		private route: ActivatedRoute,
		public userService: UsersService,
		private router: Router,
		private roleservice: RolesService,
		private translateService: TranslateService,
		private dialog: MatDialog,
		private bannerMessageService: BannerMessageService,
		public renderLayoutService: RenderLayoutService,
		private attachmentsService: AttachmentsService,
		private notificationsService: NotificationsService,
		// private _snackBar: MatSnackBar,
		public customTableService: CustomTableService,
		private walkthroughService: WalkthroughService,
		public fpos: FilePreviewOverlayService,
		public elRef: ElementRef,
		private companiesService: CompaniesService,
		private channelsService: ChannelsService,
		private loaderService: LoaderService,
		// private sanitizer: DomSanitizer,
		private renderDetailHelper: RenderDetailHelper,
		private street1Service: Street1Service,
		private street2Service: Street2Service,
		private cityService: CityService,
		private countryService: CountryService,
		private autoNumberService: AutoNumberService,
		private buttonService: ButtonService,
		private checkboxService: CheckboxService,
		private chronometerService: ChronometerService,
		private currencyService: CurrencyService,
		private dateTimeService: DateTimeService,
		private dateService: DateService,
		private fileUploadService: FileUploadService,
		private formulaService: FormulaService,
		private listTextService: ListTextService,
		private numberService: NumberService,
		private phoneService: PhoneService,
		private picklistService: PicklistService,
		private relationshipService: RelationshipService,
		private textAreaService: TextAreaService,
		private textService: TextService,
		private timeService: TimeService,
		private currencyExchangeService: CurrencyExchangeService
	) {
		this.dataMaterialModule = this.renderDetailHelper.dataMaterialModule;
		this.config = this.renderDetailHelper.config;
		this._snackBar = this.renderDetailHelper._snackBar;
		// this.config['height'] = '100%';
	}

	public OnChangeInternalCommentCheckbox($event) {
		if ($event.checked === true) {
			this.messageType = 'INTERNAL_COMMENT';
		} else {
			this.messageType = 'MESSAGE';
		}
	}
	private initialiseComponent() {
		this.saveDisabled = false;
		this.discussionFieldName = undefined;
		this.entry = {};
		// this.route.params.subscribe(params => {
		this.dateTimeFormat =
			window.navigator.userAgent.indexOf('Firefox') !== -1
				? 'MMM d, y h:mm a'
				: 'yyyy-MM-ddThh:mm';
		if (this.modalData) {
			this.moduleId = this.modalData.MODULE_ID;
		} else {
			this.moduleId = this.route.snapshot.params['moduleId'];
		}
		if (this.modalData) {
			this.entryId = 'new';
		} else {
			this.entryId = this.route.snapshot.params['dataId'];
		}

		this.layoutType = this.route.snapshot.params['type'];
		if (this.moduleId != null && this.entryId != null) {
			// Gets module by moduleId

			this.modulesService.getModules().subscribe(
				(response: any) => {
					this.allModules = response.MODULES;
					this.module = this.allModules.find(
						(module) => module.MODULE_ID === this.moduleId
					);

					if (!this.module) {
						this.redirectToHome();
					}
					if (this.module.NAME !== 'Tickets' || this.module.NAME !== 'Chats') {
						this.gridLayout = true;
					}
					const aggregateFields = [];
					this.module.FIELDS.forEach((field) => {
						this.moduleFields[field.NAME] = field;
						if (field.DATA_TYPE.DISPLAY === 'Aggregate') {
							aggregateFields.push(field);
						}
					});
					aggregateFields.forEach((field) => {
						const oneToManyField = this.module.FIELDS.find(
							(mField) => mField.FIELD_ID === field.AGGREGATION_FIELD
						);
						const relatedModule = this.allModules.find(
							(module) => module.MODULE_ID === oneToManyField.MODULE
						);
						const fieldToRender = relatedModule.FIELDS.find(
							(mField) => mField.FIELD_ID === field.AGGREGATION_RELATED_FIELD
						);
						this.moduleFields[field.NAME].DATA_TYPE = fieldToRender.DATA_TYPE;
					});

					this.modulesService
						.getAllPremadeResponsesByModuleId(this.moduleId)
						.subscribe(
							(premadeResponse: any) => {
								this.premadeResponses = premadeResponse.DATA;
								if (this.entryId === 'new') {
									this.isCreateLayout = true;
									this.entry.SOURCE_TYPE = 'web';
									this.disabled = false;
									this.emptyListMessage = 'EMPTY_USERS_LIST';
									this.roleservice.getRoles().subscribe(
										(rolesResponse: any) => {
											this.roles = rolesResponse.ROLES;
											this.checkModulePermission();
											for (let i = 0; i < rolesResponse.TOTAL_RECORDS; i++) {
												if (rolesResponse.ROLES[i].NAME === 'Customers') {
													this.rolesMap[rolesResponse.ROLES[i].ROLE_ID] =
														'Customer';
												} else if (
													rolesResponse.ROLES[i].NAME === 'SystemAdmin'
												) {
													this.rolesMap[rolesResponse.ROLES[i].ROLE_ID] =
														'System Admin';
												} else {
													this.rolesMap[rolesResponse.ROLES[i].ROLE_ID] =
														rolesResponse.ROLES[i].NAME;
												}
											}
											let manyToManyFlag = false;
											const moduleIdsForGet = {};

											for (const field of this.module.FIELDS) {
												const relationModule = this.allModules.find(
													(module) => module.MODULE_ID === field.MODULE
												);
												if (field.DATA_TYPE.DISPLAY === 'Relationship') {
													if (
														field.RELATIONSHIP_TYPE === 'Many to Many' &&
														field.DEFAULT_VALUE &&
														field.DEFAULT_VALUE.search(',') === -1
													) {
														manyToManyFlag = true;

														if (
															!moduleIdsForGet.hasOwnProperty(
																field.DEFAULT_VALUE
															)
														) {
															moduleIdsForGet[field.DEFAULT_VALUE] =
																this.modulesService.getEntry(
																	relationModule.MODULE_ID,
																	field.DEFAULT_VALUE
																);
														}
													} else if (
														field.RELATIONSHIP_TYPE === 'Many to Many' &&
														field.DEFAULT_VALUE &&
														field.DEFAULT_VALUE.search(',') !== -1
													) {
														manyToManyFlag = true;
														const defaultValues =
															field.DEFAULT_VALUE.split(',');
														let valuesProcessed = 0;
														defaultValues.forEach((element) => {
															this.modulesService
																.getEntry(relationModule.MODULE_ID, element)
																.subscribe((result: any) => {
																	valuesProcessed++;
																	this.manyToManyMap.set(
																		result.DATA_ID,
																		result
																	);
																	if (
																		valuesProcessed === defaultValues.length
																	) {
																		this.initializeLayout('CREATE_LAYOUTS');
																	}
																});
														});
													}
												}
											}
											this.module.FIELDS.forEach((field) => {
												this.fieldsMap[field.FIELD_ID] = field;
											});
											combineLatest(Object.values(moduleIdsForGet)).subscribe(
												(value: any) => {
													for (const relationshipEntries of value) {
														this.manyToManyMap.set(
															relationshipEntries.DATA_ID,
															relationshipEntries
														);
													}
													this.initializeLayout('CREATE_LAYOUTS');
												}
											);

											if (!manyToManyFlag) {
												this.initializeLayout('CREATE_LAYOUTS');
											}
										},
										(error) => {
											this.bannerMessageService.errorNotifications.push({
												message: error.error.ERROR,
											});
										}
									);
								} else {
									// Gets entry by entryId
									this.modulesService
										.getEntry(this.moduleId, this.entryId)
										.subscribe(
											(entryResponse: any) => {
												this.entry = entryResponse;
												if (
													this.module.SINGULAR_NAME === 'User' &&
													this.entry['USER_UUID'] ===
														this.userService.user['USER_UUID']
												) {
													this.showChangeButton = true;
												}
												this.maxDiscussionRows = '7';
												if (
													this.entry.USERS !== undefined &&
													this.entry.USERS.length > 0
												) {
												} else {
													this.emptyListMessage = 'EMPTY_USERS_LIST';
												}
												this.roleservice.getRoles().subscribe(
													(rolesResponse: any) => {
														this.roles = rolesResponse.ROLES;
														this.checkModulePermission();
														for (
															let i = 0;
															i < rolesResponse.TOTAL_RECORDS;
															i++
														) {
															if (rolesResponse.ROLES[i].NAME === 'Customers') {
																this.rolesMap[rolesResponse.ROLES[i].ROLE_ID] =
																	'Customer';
															} else if (
																rolesResponse.ROLES[i].NAME === 'SystemAdmin'
															) {
																this.rolesMap[rolesResponse.ROLES[i].ROLE_ID] =
																	'System Admin';
															} else {
																this.rolesMap[rolesResponse.ROLES[i].ROLE_ID] =
																	rolesResponse.ROLES[i].NAME;
															}
														}

														let manyToManyFlag = false;
														let moduleIdsForGet = {};
														for (let field of this.module.FIELDS) {
															if (field.DATA_TYPE.DISPLAY === 'Relationship') {
																if (
																	field.RELATIONSHIP_TYPE === 'Many to Many' &&
																	this.entry[field.NAME] &&
																	this.entry[field.NAME].length >= 1
																) {
																	manyToManyFlag = true;
																	const relationModule = this.allModules.find(
																		(module) =>
																			module.MODULE_ID === field.MODULE
																	);
																	moduleIdsForGet[field.NAME] =
																		this.modulesService.getSelectedEntries(
																			relationModule.MODULE_ID,
																			this.entry[field.NAME]
																		);
																}
															}
														}
														this.module.FIELDS.forEach((field) => {
															this.fieldsMap[field.FIELD_ID] = field;
														});
														combineLatest(
															Object.values(moduleIdsForGet)
														).subscribe((value: any) => {
															for (const relationshipEntries of value) {
																relationshipEntries.DATA.forEach((element) => {
																	this.manyToManyMap.set(
																		element.DATA_ID,
																		element
																	);
																});
															}
															if (this.layoutType === 'edit') {
																this.initializeLayout('EDIT_LAYOUTS');
															} else if (this.layoutType === 'detail') {
																this.initializeLayout('DETAIL_LAYOUTS');
															}
														});

														if (!manyToManyFlag) {
															if (this.layoutType === 'edit') {
																this.initializeLayout('EDIT_LAYOUTS');
															} else if (this.layoutType === 'detail') {
																this.initializeLayout('DETAIL_LAYOUTS');
															}
														}
														if (this.module.NAME === 'Chats') {
															this.getAllEntries();
															this.subscribeChat();
															this.notificationSubscription();
															this.getUnreadNotifications();
															this.checkAssignee();
															this.checkPressToEnter();
														} else {
															if (
																this.discussionFieldName !== undefined ||
																(this.discussionFieldName !== '' &&
																	this.route.snapshot.params['dataId'] !==
																		'new')
															) {
																this.subscribeDiscussion();
															}
														}
														this.subscribeUpdates();
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
												this.router.navigate([`render/${this.moduleId}`]);
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
		}
		// });
	}

	public ngOnInit() {
		if (this.modalData) {
			this.moduleId = this.modalData.MODULE_ID;
		} else {
			this.moduleId = this.route.snapshot.params['moduleId'];
		}
		this.entryId = this.route.snapshot.params.dataId;

		if (
			this.renderDetailHelper.isAndroid() ||
			this.renderDetailHelper.isIOS()
		) {
			this.layoutTypeMobile = 'detail';
			let values = {};
			values = this.renderDetailHelper.getEntryValues();
			this.moduleId = values['MODULE_ID'];
			this.entryId = values['DATA_ID'];
		}

		this.modulesService.getAllModules().subscribe((modules: any) => {
			this.allModules = modules.MODULES;
		});
		this.modulesService
			.getModuleById(this.moduleId)
			.subscribe((moduleResponse: any) => {
				this.mobileTitle = moduleResponse.SINGULAR_NAME;
			});

		this.formControlsMobile = new FormControl();

		this.scrollSubject
			.pipe(
				switchMap((moduleField: any) => {
					const fieldControlName =
						moduleField.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
					const fieldSearch =
						moduleField.PRIMARY_DISPLAY_FIELD_NAME +
						'=' +
						(this.formControls[fieldControlName] &&
						this.formControls[fieldControlName].value
							? this.formControls[fieldControlName].value
							: '*');
					return this.modulesService
						.getFieldFilteredPaginatedSearchEntries(
							moduleField.MODULE,
							fieldSearch,
							moduleField.PRIMARY_DISPLAY_FIELD_NAME,
							'asc',
							Math.ceil(
								this.relationFieldFilteredEntries[moduleField.NAME].length / 10
							) + 1,
							10,
							moduleField.RELATIONSHIP_FIELD
						)
						.pipe(
							map((results: any) => {
								if (results.DATA.length > 0) {
									this.relationFieldFilteredEntries[moduleField.NAME] =
										this.relationFieldFilteredEntries[moduleField.NAME].concat(
											results.DATA
										);
								}
								return results.DATA;
							})
						);
				})
			)
			.subscribe();
		this.poDataSource.paginator = this.poPaginator;
		this.currentWorkflowDataSource.paginator = this.currentWorkflowPaginator;
		// SET SENDER AND RECEIVER BUBBLE COLOR
		this.filteredCountries = this.renderLayoutService.countries;

		this.filteredCountries.forEach((element) => {
			this.countryCode[element.COUNTRY_DIAL_CODE] = new Array();

			this.countryName.push(
				element.COUNTRY_NAME + '|' + element.COUNTRY_DIAL_CODE
			);
			this.countryCode[element.COUNTRY_DIAL_CODE].push(element.COUNTRY_NAME);
			this.countryCode[element.COUNTRY_DIAL_CODE].push(element.COUNTRY_CODE);
			this.countryCode[element.COUNTRY_DIAL_CODE].push(element.COUNTRY_FLAG);
		});

		this.channelsService.getChatChannel('Chats').subscribe(
			(response: any) => {
				this.senderBubbleColor = response.SENDER_BUBBLE_COLOR;
				this.receiverBubbleColor = response.RECEIVER_BUBBLE_COLOR;
				this.senderTextColor = response.SENDER_TEXT_COLOR;
				this.receiverTextColor = response.RECEIVER_TEXT_COLOR;
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
		// START FIX FOR TSTLINT
		if (false) {
			let test = this.showNextButton;
			test = this.showSaveButton;
			this.getField('test');
			test = this.saveDisabled;
		}
		// END FIX FOR TSTLINT

		this.route.params.pipe(takeUntil(this._destroyed$)).subscribe(() => {
			this.customLayouts.length = 0;
			this.initialiseComponent();
			this.customLayout = true;
		});
		this.roleservice.getRoleById(this.userService.user.ROLE).subscribe(
			(response: any) => {
				this.currentRole = response;
				this.modulesService.getModuleById(this.moduleId).subscribe(
					(data: any) => {
						this.module = data;
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
		if (
			this.renderDetailHelper.isAndroid() ||
			this.renderDetailHelper.isIOS()
		) {
			// console.log(args.object);
			// this.scrollToBottom(args);
			this.getEntryData();
		}
	}

	public getEntryData() {
		if (this.layoutTypeMobile === 'detail') {
			this.roleservice
				.getRoleById(this.userService.user.ROLE)
				.subscribe((response: any) => {
					this.currentRole = response;
				});

			this.isAndroidDevice = this.renderDetailHelper.isAndroid();
			this.isIosDevice = this.renderDetailHelper.isIOS();

			this.previousUrl = this.route.snapshot.queryParamMap.get('previousUrl');

			this.isloadingData = true;
			this.detailLayout = false;
			let moduleData: any;
			let layout: any;
			this.fieldArray = [];
			this.metaDataArray = [];
			this.relationshipDetailValues = {};

			this.modulesService
				.getEntry(this.moduleId, this.entryId)
				.subscribe((response: any) => {
					this.data = response;
					let moduleObj = this.allModules.find(
						(module) => module.MODULE_ID === this.moduleId
					);
					this.moduleNameMobile = moduleObj.NAME;

					if (this.data.CHAT) {
						this.chats = this.data.CHAT;
						// this.message='';
						this.formControlsMobile['CHAT'] = new FormControl();
						this.chats.forEach((chat) => {
							let index = this.chats.indexOf(chat);
							if (chat.MESSAGE_TYPE === 'META_DATA') {
								this.chats[index].MESSAGE = chat.MESSAGE.replace(
									/<\/?[^>]+(>|$)/g,
									''
								).replace(/\s\s+/g, ' ');
							} else {
								this.chats[index].MESSAGE = chat.MESSAGE.replace(
									/<[^>]*>/g,
									''
								);
							}
						});
						// this.isloadingData=false;
					}

					if (this.data.MESSAGES) {
						this.data.MESSAGES.forEach((message) => {
							let index = this.data.MESSAGES.indexOf(message);
							if (message.MESSAGE_TYPE === 'META_DATA') {
								this.data.MESSAGES[index].MESSAGE = message.MESSAGE.replace(
									/<\/?[^>]+(>|$)/g,
									''
								).replace(/\s\s+/g, ' ');

								this.metaDataArray.push(
									message.MESSAGE.replace(/<\/?[^>]+(>|$)/g, '').replace(
										/\s\s+/g,
										' '
									)
								);
							} else {
								this.data.MESSAGES[index].MESSAGE = message.MESSAGE.replace(
									/<[^>]*>/g,
									''
								);
								this.metaDataArray.push(
									message.MESSAGE.replace(/<[^>]*>/g, '')
								);
							}
						});
					}

					if (this.moduleNameMobile !== 'Chats') {
						this.modulesService
							.getModuleById(this.moduleId)
							.subscribe((moduleResponse: any) => {
								this.moduleResponse = moduleResponse;

								layout = moduleResponse['DETAIL_MOBILE_LAYOUTS'].find(
									(L) => L.ROLE === this.userService.user.ROLE
								);

								for (const fieldId of layout.FIELDS) {
									let index = layout.FIELDS.indexOf(fieldId);
									let fieldData = moduleResponse.FIELDS.find(
										(F) => F.FIELD_ID === layout.FIELDS[index]
									);
									this.fieldArray.push(fieldData);

									if (fieldData.DATA_TYPE.DISPLAY === 'Relationship') {
										const relationModule = this.allModules.find(
											(module) => module.MODULE_ID === fieldData.MODULE
										);

										this.modulesService
											.getModuleById(relationModule.MODULE_ID)
											.subscribe((relationModuleResponse: any) => {
												this.modulesService
													.getEntries(relationModule.MODULE_ID)
													.subscribe((entries: any) => {
														const primaryDisplayField =
															relationModuleResponse.FIELDS.find(
																(tempField) =>
																	tempField.FIELD_ID ===
																	fieldData.PRIMARY_DISPLAY_FIELD
															);
														this.relationshipDetailValues[fieldData.NAME] =
															new Array();

														if (fieldData.DATA_TYPE.BACKEND !== 'String') {
															this.data[fieldData.NAME].forEach((element) => {
																let entry = entries.DATA.filter(
																	(E) => E.DATA_ID === element
																);
																// entry[0][primaryDisplayField.NAME]

																this.relationshipDetailValues[
																	fieldData.NAME
																].push(entry[0][primaryDisplayField.NAME]);
															});
														} else {
															let element = this.data[fieldData.NAME];
															let entry = entries.DATA.filter(
																(E) => E.DATA_ID === element
															);
															this.relationshipDetailValues[
																fieldData.NAME
															].push(entry[0][primaryDisplayField.NAME]);
														}
													});
											});
									}
								}

								this.onSubmitting = false;
								this.detailLayout = true;
								this.isloadingData = false;
							});
					}
				});
		}
	}

	public toEditLayout(entryId) {
		this.relationshipPicklistValues = {};

		this.metaDataArray = [];
		this.moduleResponse = [];
		this.EditfieldArray = [];
		this.relationshipFieldValue = {};
		this.detailLayout = false;

		this.modulesService
			.getEntry(this.moduleId, entryId)
			.subscribe((response: any) => {
				this.data = response;

				// this.data.MESSAGES[0].MESSAGE_TYPE === 'META_DATA' ?"<TextView style={{color: '#757575', fontWeight: '200'}} text="{{this.data.MESSAGES[0].MESSAGE.message.replace(/<\/?[^>]+(>|$)/g, "").replace(/\s\s+/g, ' ')}}"></TextView>"  : "<HtmlView html={{this.data.MESSAGES[0].MESSAGE}} ></HtmlView>"
				if (this.data.MESSAGES) {
					this.data.MESSAGES.forEach((message) => {
						if (message.MESSAGE_TYPE === 'META_DATA') {
							let obj: any = {};
							let dataIndex = this.data.MESSAGES.indexOf(message);
							this.metaDataArray.push(
								message.MESSAGE.replace(/<\/?[^>]+(>|$)/g, '').replace(
									/\s\s+/g,
									' '
								)
							);
							let length1 = message.MESSAGE.replace(
								/<\/?[^>]+(>|$)/g,
								''
							).replace(/\s\s+/g, ' ').length;
						} else {
							this.metaDataArray.push('');
						}
					});
				}

				this.setFormControllerForMobile();
			});
	}

	public setFormControllerForMobile() {
		let items: any = [];
		let listFieldNames: any = [];

		this.modulesService
			.getModuleById(this.moduleId)
			.subscribe((moduleResponse: any) => {
				this.moduleResponse = moduleResponse;

				this.layoutFieldIds = moduleResponse['EDIT_MOBILE_LAYOUTS'].find(
					(L) => L.ROLE === this.userService.user.ROLE
				);
				let obj: any = [];

				for (const fieldId of this.layoutFieldIds.FIELDS) {
					let index = this.layoutFieldIds.FIELDS.indexOf(fieldId);
					let fieldData = moduleResponse.FIELDS.find(
						(F) => F.FIELD_ID === this.layoutFieldIds.FIELDS[index]
					);
					if (fieldData.DATA_TYPE.DISPLAY === 'Relationship') {
						this.formControlsMobile[fieldData.NAME] = new FormControl([]);
					} else if (fieldData.DATA_TYPE.DISPLAY === 'Phone') {
						this.formControlsMobile['PHONE_NUMBER'] = new FormControl();
						this.formControlsMobile['COUNTRY_CODE'] = new FormControl();
						this.formControlsMobile['DIAL_CODE'] = new FormControl();
						this.formControlsMobile['COUNTRY_FLAG'] = new FormControl();
					} else {
						this.formControlsMobile[fieldData.NAME] = new FormControl();
					}

					if (fieldData.NOT_EDITABLE) {
						this.formControlsMobile[fieldData.NAME].disable();
					}

					this.EditfieldArray.push(fieldData);

					let listItems: any = [];
					let fieldName: any;
					this.picklistValues[fieldData.NAME] = new Array();
					if (fieldData.DATA_TYPE.DISPLAY === 'Picklist') {
						fieldData.PICKLIST_VALUES.forEach((value) => {
							this.picklistValues[fieldData.NAME].push(value);
						});
					}

					if (fieldData.DATA_TYPE.DISPLAY === 'Relationship') {
						if (this.data[fieldData.NAME] !== -1) {
							const relationModule = this.allModules.find(
								(module) => module.MODULE_ID === fieldData.MODULE
							);

							this.modulesService
								.getModuleById(relationModule.MODULE_ID)
								.subscribe((relationModuleResponse: any) => {
									this.responseArray[fieldData.NAME] = relationModuleResponse;
									this.modulesService
										.getEntries(relationModule.MODULE_ID)
										.subscribe((entries: any) => {
											this.entriesArray[fieldData.NAME] = entries;
											const primaryDisplayField =
												relationModuleResponse.FIELDS.find(
													(tempField) =>
														tempField.FIELD_ID ===
														fieldData.PRIMARY_DISPLAY_FIELD
												);
											this.relationshipPicklistValues[fieldData.NAME] =
												new Array();
											entries.DATA.forEach((value) => {
												if (
													!this.relationshipPicklistValues[
														fieldData.NAME
													].includes(value[primaryDisplayField.NAME])
												) {
													let list = [];

													this.relationshipPicklistValues[fieldData.NAME].push(
														value[primaryDisplayField.NAME]
													);
												}
											});

											this.relationshipFieldValue[fieldData.NAME] = new Array();
											if (fieldData.DATA_TYPE.BACKEND !== 'String') {
												this.data[fieldData.NAME].forEach((element) => {
													let entry = entries.DATA.filter(
														(E) => E.DATA_ID === element
													);

													listItems.push(entry[0].NAME);

													fieldName = fieldData.NAME;

													this.relationshipFieldValue[fieldData.NAME].push(
														entry[0][primaryDisplayField.NAME]
													);
												});

												items.push(listItems);
												listFieldNames.push(fieldName);
												this.listRelationshipData.set(fieldName, listItems);
											} else {
												let field = this.data[fieldData.NAME];
												let entry = entries.DATA.filter(
													(E) => E.DATA_ID === field
												);

												this.relationshipFieldValue[fieldData.NAME] =
													new Array();
												this.relationshipFieldValue[fieldData.NAME].push(
													entry[0][primaryDisplayField.NAME]
												);
											}
										});
								});
						}
					}
				}
			});
		this.isloadingData = false;
	}

	public selectedRelationshipValue(args, field) {
		const picker = <ListPicker>args.object;

		if (
			!this.relationshipFieldValue[field.NAME].includes(
				this.relationshipPicklistValues[field.NAME][picker.selectedIndex]
			)
		) {
			if (field.DATA_TYPE.BACKEND !== 'String') {
				this.relationshipFieldValue[field.NAME].push(
					this.relationshipPicklistValues[field.NAME][picker.selectedIndex]
				);
			} else {
				this.relationshipFieldValue[field.NAME] =
					this.relationshipPicklistValues[field.NAME][picker.selectedIndex];
			}
		} else {
			alert(this.translateService.instant('VALUE_EXISTS'));
		}

		this.relationshipFieldValue[field.NAME] =
			this.relationshipFieldValue[field.NAME].slice();

		this.showRelationshipPicker = false;

		this.formControlsMobile[field.NAME].touched = false;
	}

	public showPicker(field, args) {
		if (
			field.DATA_TYPE.DISPLAY === 'Relationship' &&
			field.DATA_TYPE.BACKEND === 'String'
		) {
			this.renderDetailHelper.disableKeyboard(args);
		}
		this.formControlsMobile[field.NAME].valueChanges.subscribe((value) => {
			let val = [];
			val = value.split(',');

			this.relationshipFieldValue[field.NAME].forEach((element) => {
				if (!val.includes(element)) {
					this.relationshipFieldValue[field.NAME].pop(element);
				}
			});
		});

		this.showRelationshipPicker = true;
		this.formControlsMobile[field.NAME].markAsTouched();
	}

	public showHideField(field, args) {
		this.showPicklistPicker = true;
		this.picklistShow = true;
		this.formControlsMobile[field.NAME].markAsTouched();
		this.renderDetailHelper.disableKeyboard(args);
		// UIApplication.sharedApplication
		// .keyWindow
		// .endEditing(true);

		// utils.ad.dismissSoftInput();
	}

	public onBack() {
		this.isloadingData = true;
		if (this.previousUrl != undefined) {
			this.renderDetailHelper.navigateToSidebar(this.previousUrl);
		}
	}
	public changMessageDisplay(option, index) {
		this.selectedOptionIndex = index;
		if (option === 'Messages') {
			this.messageShowType = 'MESSAGE';
		} else if (option === 'All') {
			this.messageShowType = 'ALL';
		} else if (option === 'Internal Comments') {
			this.messageShowType = 'INTERNAL_COMMENT';
		} else if (option === 'Events') {
			this.messageShowType = 'META_DATA';
		}

		this.changeMessageType = false;
	}

	public onChangeMessageType(args) {
		this.changeMessageType = !this.changeMessageType;
	}

	public selectedChanged(args, field) {
		this.picklistShow = true;
		const picker = <ListPicker>args.object;

		let value = this.moduleResponse.FIELDS.filter((F) => F.NAME === field.NAME);

		this.formControlsMobile[field.NAME].setValue(
			value[0].PICKLIST_VALUES[picker.selectedIndex]
		);

		this.showPicklistPicker = false;
		this.picklistShow = false;

		this.formControlsMobile[field.NAME].touched = false;
	}

	public selectCountryDialog(field) {
		this.renderDetailHelper.countryDialCodeDialogHelper(
			this.countryName,
			this.formControlsMobile
		);
	}

	public setDiscussion() {
		this.showDiscussion = true;
	}

	public onTextViewFocus() {
		this.sendButtonEnabled = true;
	}

	public hideDiscussion() {
		this.showDiscussion = false;
	}

	public sendMessage() {
		if (this.discussionMessage) {
			const msgObject = {
				MESSAGE: this.discussionMessage,
				ATTACHMENTS: [],
				SENDER: {
					FIRST_NAME: this.userService.user.FIRST_NAME,
					LAST_NAME: this.userService.user.LAST_NAME,
					USER_UUID: this.userService.user.USER_UUID,
					ROLE: this.userService.user.ROLE,
				},
				MESSAGE_TYPE: 'MESSAGE',
			};
			if (this.data['TIME_SPENT'] === 0) {
				this.data['TIME_SPENT'] = '0m';
			}

			this.data.MESSAGES.push(msgObject);

			this.modulesService
				.putEntry(this.moduleId, this.entryId, this.data)
				.subscribe(
					(response: any) => {
						this.discussionMessage = '';
						this.showDiscussion = false;
						this.modulesService
							.getEntry(this.moduleId, this.entryId)
							.subscribe((response: any) => {
								this.data = response;
								if (this.data.MESSAGES) {
									this.data.MESSAGES.forEach((message) => {
										let index = this.data.MESSAGES.indexOf(message);
										if (message.MESSAGE_TYPE === 'META_DATA') {
											this.data.MESSAGES[index].MESSAGE =
												message.MESSAGE.replace(/<\/?[^>]+(>|$)/g, '').replace(
													/\s\s+/g,
													' '
												);

											this.metaDataArray.push(
												message.MESSAGE.replace(/<\/?[^>]+(>|$)/g, '').replace(
													/\s\s+/g,
													' '
												)
											);
										} else {
											this.data.MESSAGES[index].MESSAGE =
												message.MESSAGE.replace(/<[^>]*>/g, '');
											this.metaDataArray.push(
												message.MESSAGE.replace(/<[^>]*>/g, '')
											);
										}
									});
								}
							});
						this.renderDetailHelper.bannerNotification(
							'MESSAGE_SENT_SUCCESSFULLY'
						);
					},
					(error: any) => {
						alert(error.error.ERROR);
					}
				);
		}
	}

	public onSave() {
		let entryToPut = {};
		this.isloadingData = true;
		this.detailLayout = false;
		this.onSubmitting = true;
		entryToPut = this.data;
		if (
			entryToPut['TIME_SPENT'] !== undefined ||
			entryToPut['TIME_SPENT'] === 0
		) {
			entryToPut['TIME_SPENT'] = '0m';
		}

		if (this.entryId !== 'new') {
			for (const fieldId of this.layoutFieldIds.FIELDS) {
				let index = this.layoutFieldIds.FIELDS.indexOf(fieldId);
				let fieldData = this.moduleResponse.FIELDS.find(
					(F) => F.FIELD_ID === this.layoutFieldIds.FIELDS[index]
				);

				if (
					fieldData.DATA_TYPE.DISPLAY !== 'Discussion' &&
					fieldData.DATA_TYPE.DISPLAY !== 'Relationship' &&
					fieldData.DATA_TYPE.DISPLAY !== 'Chronometer' &&
					fieldData.DATA_TYPE.DISPLAY !== 'Phone'
				) {
					entryToPut[fieldData.NAME] =
						this.formControlsMobile[fieldData.NAME].value;
				} else if (fieldData.DATA_TYPE.DISPLAY === 'Relationship') {
					const relationModule = this.allModules.find(
						(module) => module.MODULE_ID === fieldData.MODULE
					);

					this.responseArray[fieldData.NAME];
					this.entriesArray[fieldData.NAME];

					const primaryDisplayField = this.responseArray[
						fieldData.NAME
					].FIELDS.find(
						(tempField) =>
							tempField.FIELD_ID === fieldData.PRIMARY_DISPLAY_FIELD
					);

					if (fieldData.DATA_TYPE.BACKEND !== 'String') {
						let list = [];

						if (this.relationshipFieldValue[fieldData.NAME] !== undefined) {
							this.relationshipFieldValue[fieldData.NAME].forEach((val) => {
								list.push(val);
							});

							if (!fieldData.NOT_EDITABLE) {
								entryToPut[fieldData.NAME] = [];
								if (list.length > 0) {
									list.forEach((element) => {
										let value = this.entriesArray[fieldData.NAME].DATA.find(
											(D) => D[primaryDisplayField.NAME] === element
										);

										if (!entryToPut[fieldData.NAME].includes(value.DATA_ID)) {
											entryToPut[fieldData.NAME].push(value.DATA_ID);
										}
									});
								}
							}
						}
					} else {
						if (!fieldData.NOT_EDITABLE) {
							if (this.formControlsMobile[fieldData.NAME].value) {
								let value = this.entriesArray[fieldData.NAME].DATA.find(
									(D) =>
										D[primaryDisplayField.NAME] ===
										this.formControlsMobile[fieldData.NAME].value
								);

								entryToPut[fieldData.NAME] = value.DATA_ID;
							} else {
								entryToPut[fieldData.NAME] = '';
							}
						}
					}
				} else if (fieldData.DATA_TYPE.DISPLAY === 'Discussion') {
					let attachments = [];

					if (this.formControlsMobile[fieldData.NAME].value === null) {
						this.formControlsMobile[fieldData.NAME].setValue(' ');
					}
					const msgBody = this.formControlsMobile[fieldData.NAME].value;

					const msgObject = {
						MESSAGE: msgBody,
						ATTACHMENTS: JSON.parse(JSON.stringify(attachments)),
						SENDER: {
							FIRST_NAME: this.userService.user.FIRST_NAME,
							LAST_NAME: this.userService.user.LAST_NAME,
							USER_UUID: this.userService.user.USER_UUID,
							ROLE: this.userService.user.ROLE,
						},
						MESSAGE_TYPE: 'MESSAGE',
					};
					// entryToPut[fieldData.NAME] = [msgObject];
					entryToPut[fieldData.NAME].push(msgObject);
					// this._stompService._stompManagerService.publish({
					// 	destination: `ngdesk/discussion`,
					// 	body: JSON.stringify(msgObject),
					// });
				}
				if (fieldData.DATA_TYPE.DISPLAY === 'Chronometer') {
					entryToPut[fieldData.NAME] = '0m';
				}
				if (fieldData.DATA_TYPE.DISPLAY === 'Phone') {
					entryToPut[fieldData.NAME].PHONE_NUMBER =
						this.formControlsMobile['PHONE_NUMBER'].value;
					entryToPut[fieldData.NAME].DIAL_CODE =
						this.formControlsMobile['DIAL_CODE'].value;
					entryToPut[fieldData.NAME].COUNTRY_FLAG =
						this.countryCode[this.formControlsMobile['DIAL_CODE'].value][2];
					entryToPut[fieldData.NAME].COUNTRY_CODE =
						this.countryCode[this.formControlsMobile['DIAL_CODE'].value][1];
				}
			}

			this.modulesService
				.putEntry(this.moduleId, this.entryId, entryToPut)
				.subscribe(
					(response: any) => {
						this.message = '';

						this.renderDetailHelper.bannerNotification('UPDATED_SUCCESSFULLY');
						this.renderDetailHelper.setshowBackButton();
						this.renderDetailHelper.navigateToListLayout(this.moduleId);
						// this.router.navigate([`render/${this.moduleId}`]);

						// this.detailLayout = true;
						// this.isloadingData = true;
						// this.getEntryData();
					},
					(error: any) => {
						this.isloadingData = false;
						this.detailLayout = false;
						this.onSubmitting = false;
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
						alert(error.error.ERROR);
					}
				);
		}
	}

	private showLearnMore(module: string) {
		const showMoreData = this.fpos.getShowMoreData(module);
		const learnMoreDialogRef = this.dialog.open(LearnMoreDialogComponent, {
			data: {
				title: this.translateService.instant(showMoreData.DIALOG_TITLE),
				description: this.translateService.instant(showMoreData.DIALOG_DESC),
				buttonText: this.translateService.instant('LEARN_MORE'),
				linkText: this.translateService.instant('DISMISS'),
			},
		});
		// EVENT AFTER MODAL DIALOG IS CLOSED
		learnMoreDialogRef.afterClosed().subscribe((result) => {
			let walkthroughComplete = false;
			// if user selected the learn more button and the layouts button is available
			if (result === this.translateService.instant('LEARN_MORE')) {
				const element = this.elRef.nativeElement.querySelector(
					`#${showMoreData.FIRST_STEP_ELEMENT}`
				);
				const els = this.fpos.getAllWalkthroughData(module);
				this.showPreview(
					element,
					this.fpos.getWalkthroughData(
						this.module.NAME,
						`${showMoreData.FIRST_STEP_ELEMENT}`
					)
				);
				walkthroughComplete = true;
			}
			// post walkthrough key with true value indicating walkthrough complete
			this.walkthroughService
				.postWalkthrough(showMoreData.API_KEY, walkthroughComplete)
				.subscribe((walkthroughSuccess: any) => {});
		});
	}

	// will show walkthrough popup depending on host element and popup data
	public showPreview(hostElement, data) {
		// open overlay for WalkthroughDialogComponent
		this.previewRef = this.fpos.open(data, hostElement);
		this.dialogRef = this.previewRef.dialogRef;
		this.previewRef.overlayComponent.closeButton.subscribe((value) => {
			this.dialogRef.close();
			switch (this.module.NAME) {
				case 'Chats': {
					switch (value) {
						// third step is new tickets button
						case this.translateService.instant('WALKTHROUGH_PROGRESS_1_OF_5'): {
							const element =
								this.elRef.nativeElement.querySelector('#chat-text-area');
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(this.module.NAME, 'chat-text-area')
							);
							break;
						}
						// fourth step is emailing in ticket
						case this.translateService.instant('WALKTHROUGH_PROGRESS_2_OF_5'): {
							const element =
								this.elRef.nativeElement.querySelector('#chat-details-area');
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'chat-details-area'
								)
							);
							break;
						}
						// fourth step is emailing in ticket
						case this.translateService.instant('WALKTHROUGH_PROGRESS_3_OF_5'): {
							const element = this.elRef.nativeElement.querySelector(
								'#chat-transfer-button'
							);
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'chat-transfer-button'
								)
							);
							break;
						}
						// fourth step is emailing in ticket
						case this.translateService.instant('WALKTHROUGH_PROGRESS_4_OF_5'): {
							const element = this.elRef.nativeElement.querySelector(
								'#pre-chat-survey-detail'
							);
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'pre-chat-survey-detail'
								)
							);
							break;
						}
						case this.translateService.instant('WALKTHROUGH_PROGRESS_5_OF_5'): {
							this.fpos.hostElement = '';
							break;
						}
					}
				}
				case 'Tickets': {
					switch (value) {
						// first step is status dropdown
						case this.translateService.instant('WALKTHROUGH_PROGRESS_1_OF_8'): {
							const element =
								this.elRef.nativeElement.querySelector('#assignee-dropdown');
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'assignee-dropdown'
								)
							);
							break;
						}
						// second step is assignee dropdown
						case this.translateService.instant('WALKTHROUGH_PROGRESS_2_OF_8'): {
							const element =
								this.elRef.nativeElement.querySelector('#subject-heading');
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'subject-heading'
								)
							);
							break;
						}
						// third step is subject heading
						case this.translateService.instant('WALKTHROUGH_PROGRESS_3_OF_8'): {
							const element = this.elRef.nativeElement.querySelector(
								'#ticket-conversation-list'
							);
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'ticket-conversation-list'
								)
							);
							break;
						}
						// fourth step is the ticket conversation list
						case this.translateService.instant('WALKTHROUGH_PROGRESS_4_OF_8'): {
							const element = this.elRef.nativeElement.querySelector(
								'#ticket-submit-message-area'
							);
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'ticket-submit-message-area'
								)
							);
							break;
						}
						// fifth step is the ticket submit message area
						case this.translateService.instant('WALKTHROUGH_PROGRESS_5_OF_8'): {
							const element = this.elRef.nativeElement.querySelector(
								'#private-message-switch'
							);
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'private-message-switch'
								)
							);
							break;
						}
						// sixth step is private message toggle
						case this.translateService.instant('WALKTHROUGH_PROGRESS_6_OF_8'): {
							const element =
								this.elRef.nativeElement.querySelector('#time-spent-input');
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'time-spent-input'
								)
							);
							break;
						}
						// seventh step is time spent input
						case this.translateService.instant('WALKTHROUGH_PROGRESS_7_OF_8'): {
							const element = this.elRef.nativeElement.querySelector(
								'#save-ticket-action'
							);
							this.showPreview(
								element,
								this.fpos.getWalkthroughData(
									this.module.NAME,
									'save-ticket-action'
								)
							);
							break;
						}
						// sixth step is save button; conclude walkthrough on close
						case this.translateService.instant('WALKTHROUGH_PROGRESS_8_OF_8'): {
							this.fpos.hostElement = '';
							break;
						}
					}
				}
			}
		});
	}

	private checkPressToEnter() {
		this.enterToSend = true;
		// let  keyCode: number;
		// keyCode  =this.renderDetailHelper.checkPressToEnterhelper(this.enterToSend);
		// if(keyCode === 13){
		// 	this.publishChat();
		// }

		//commented by Chaithra
		this.config['setup'] = (ed) => {
			ed.on('SetContent', function (ed1) {
				ed1.target.editorCommands.execCommand('fontName', false, 'Arial');
			});
			ed.on('KeyUp', (e) => {
				if (this.enterToSend && e.keyCode === 13) {
					this.publishChat();
				}
			});
			ed.on('KeyDown', (e) => {
				if (this.enterToSend && e.keyCode === 13) {
					e.preventDefault();
				}
			});
		};
	}

	private getAvailableWalkthroughElements(moduleName: string) {
		const availableWalkthroughElements =
			this.fpos.getAllWalkthroughData(moduleName);
		const elementsFound = [];
		for (let i = 0; i < availableWalkthroughElements.length; i++) {
			const element = this.elRef.nativeElement.querySelector(
				`#${availableWalkthroughElements[i].element}`
			);
			if (element) {
				elementsFound.push(availableWalkthroughElements[i]);
			}
		}
		return elementsFound;
	}

	public checkModulePermission() {
		this.roles.filter((element) => {
			if (element.ROLE_ID === this.userService.user.ROLE) {
				if (element.NAME === 'SystemAdmin') {
					this.isModuleAllowed = true;
					this.editAccess = true;
					this.deleteAccess = true;
				} else {
					const permissions = element.PERMISSIONS;
					permissions.find((modulePermissions) => {
						if (modulePermissions.MODULE === this.moduleId) {
							if (modulePermissions.MODULE_PERMISSIONS.ACCESS === 'Enabled') {
								this.isModuleAllowed = true;
								if (
									modulePermissions.MODULE_PERMISSIONS.EDIT === 'All' ||
									modulePermissions.MODULE_PERMISSIONS.EDIT === 'Not Set'
								) {
									this.editAccess = true;
								}
								if (
									modulePermissions.MODULE_PERMISSIONS.DELETE === 'All' ||
									modulePermissions.MODULE_PERMISSIONS.DELETE === 'Not Set'
								) {
									this.deleteAccess = true;
								}
							} else {
								this.isModuleAllowed = false;
							}
						}
					});
				}
			}
		});
	}

	public checkAssignee() {
		if (this.entry.AGENTS) {
			this.entry.AGENTS.forEach((agent) => {
				if (agent !== this.userService.user.DATA_ID) {
					this.isAssignedChat = false;
				} else {
					this.isAssignedChat = true;
				}
			});
		}
	}

	public addPremadeResponse(premadeResponse) {
		const regex = new RegExp(`{{inputMessage.`);
		if (regex.test(premadeResponse.MESSAGE)) {
			this.modulesService
				.getPremadeResponse(
					premadeResponse.PREMADE_RESPONSE_ID,
					this.moduleId,
					this.entryId
				)
				.subscribe(
					(response: any) => {
						this.message += response.MESSAGE;
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		} else {
			this.message += premadeResponse.MESSAGE;
		}
	}

	public newPremadeResponse() {
		this.router.navigate([`company-settings/premade-responses/new`]);
	}

	public transferChat() {
		if (this.fpos.hostElement === '') {
			const moduleId = this.moduleId;
			const dataId = this.entry.DATA_ID;
			const body = {
				MODULE_ID: moduleId,
				DATA_ID: dataId,
				SUBDOMAIN: this.userService.getSubdomain(),
			};
			// this._stompService._stompManagerService.publish({
			// 	destination: `ngdesk/transfer/chat`,
			// 	body: JSON.stringify(body),
			// });
		}

		// const chatTransferSubscription$ = this._stompService._stompManagerService
		// 	.watch(`topic/chat-transfer-status`)
		// 	.pipe(shareReplay());
		// this.chatSub = chatTransferSubscription$.subscribe((message: any) => {
		// 	if (message['body'] !== 'chat-transfer') {
		// 		this.bannerMessageService.errorNotifications.push({
		// 			message: message['body'],
		// 		});
		// 	} else {
		// 		this.isChatTransfered = true;
		// 		this.router.navigate([`render/${this.moduleId}`]);
		// 	}
		// });
	}

	public displayFn(
		fieldName,
		field: any,
		selectedValueId: any
	): string | undefined {
		this.entry[fieldName] = selectedValueId.DATA_ID;
		return selectedValueId[field.PRIMARY_DISPLAY_FIELD_NAME]
			? selectedValueId[field.PRIMARY_DISPLAY_FIELD_NAME]
			: undefined;
	}
	private filterEntries(value, primaryDisplayField, array) {
		let filterValue = '';
		if (typeof value !== 'object') {
			filterValue = value.toLowerCase();
		}
		return array.filter(
			(entry) =>
				entry[primaryDisplayField].toLowerCase().indexOf(filterValue) === 0
		);
	}
	private redirectToHome() {
		const ticketsModule = this.allModules.find(
			(module) => module.NAME === 'Tickets'
		);
		this.router.navigate([`render/${ticketsModule.MODULE_ID}`]);
	}

	public checkRolePermissions() {
		if (
			this.module.NAME === 'Tickets' ||
			this.module.NAME === 'Chats' ||
			this.module.NAME === 'Teams'
		) {
			return true;
		}
		if (this.rolesMap[this.userService.user.ROLE] === 'System Admin') {
			return true;
		} else if (this.module.NAME === 'Users') {
			if (this.userService.user.DATA_ID === this.entry.DATA_ID) {
				return true;
			}
		}
		this.currentRole = this.roles.find((role) => {
			return role.ROLE_ID === this.userService.user.ROLE;
		});
		const permissions = this.currentRole.PERMISSIONS;
		const modulePermission = permissions.find((modulePermissions) => {
			return modulePermissions.MODULE === this.module.MODULE_ID;
		});
		if (modulePermission.MODULE_PERMISSIONS.EDIT === 'None') {
			return false;
		} else {
			return true;
		}
	}

	private initializeLayout(type) {
		this.showRouteToEditButton = false;
		this.clicked = false;
		if (type === 'EDIT_LAYOUTS' && !this.editAccess) {
			this.routeToDetailLayout();
		} else {
			let createLayout;
			if (this.checkRolePermissions()) {
				this.showSaveButton = true;
				createLayout = this.module[type].find(
					(layout) => layout.ROLE === this.userService.user.ROLE
				);
			} else {
				createLayout = this.module['DETAIL_LAYOUTS'].find(
					(layout) => layout.ROLE === this.userService.user.ROLE
				);
			}
			this.currentCreateLayout = createLayout;
			if (
				type === 'EDIT_LAYOUTS' &&
				!createLayout &&
				this.currentRole['NAME'] === 'System Admin'
			) {
				this.layoutExists = false;
				this.loading = false;
			} else if (
				type === 'EDIT_LAYOUTS' &&
				!createLayout &&
				this.editAccess &&
				this.currentRole['NAME'] !== 'System Admin'
			) {
				this.layoutExists = false;
				this.loading = false;
			}

			if (!createLayout) {
				this.layoutExists = false;
				this.loading = false;
			} else {
				this.layoutExists = true;
				this.loading = false;
				this.layoutStyle = createLayout.LAYOUT_STYLE;
			}
			if (type === 'DETAIL_LAYOUTS') {
				this.showSaveButton = false;
				if (this.editAccess) {
					this.showRouteToEditButton = true;
				}
			}
			if (createLayout !== undefined) {
				if (createLayout.PANELS === null) {
					if (
						createLayout.PREDEFINED_TEMPLATE === null ||
						createLayout.PREDEFINED_TEMPLATE === undefined
					) {
						let customLayout = createLayout.CUSTOM_LAYOUT;
						// this.template = createLayout.CUSTOM_LAYOUT;
						this.template = this.module.FIELDS.forEach(
							(moduleField, fieldIndex) => {
								const fieldId = moduleField.FIELD_ID;
								const fieldName = moduleField.NAME;
								const displayLabel = moduleField.DISPLAY_LABEL;
								const regexName = new RegExp(`${fieldId}_NAME`, 'g');
								const regexDisplayLabel = new RegExp(
									`${fieldId}_DISPLAY_LABEL`,
									'g'
								);
								// Setting default values
								if (
									customLayout.match(regexName) !== null &&
									type === 'CREATE_LAYOUTS'
								) {
									this.loadDefaultValueForField(moduleField);
								}
								if (moduleField.DATA_TYPE.DISPLAY === 'Discussion') {
									this.loadEntryForEachFieldType(moduleField, type);
								} else if (customLayout.match(regexName) !== null) {
									this.loadEntryForEachFieldType(moduleField, type);
								}

								customLayout = customLayout
									.replace(regexName, fieldName)
									.replace(regexDisplayLabel, displayLabel);
							}
						);
						this.template = customLayout;
					} else {
						this.modulesService
							.getPreDefinedLayout(this.module.NAME, type)
							.subscribe((predefinedTemplate: any) => {
								let initialTemplate = predefinedTemplate.HTML_TEMPLATE;
								if (
									createLayout.hasOwnProperty('LAYOUT_STYLE') &&
									createLayout.LAYOUT_STYLE !== null
								) {
									this.layoutStyle = createLayout.LAYOUT_STYLE;
								} else {
									this.layoutStyle = 'standard';
								}
								this.sections = createLayout.PREDEFINED_TEMPLATE;
								this.sections.forEach((section) => {
									let sectionTemplate = '';
									const styleType = section.FIELD_STYLE;
									section.FIELDS.forEach((element) => {
										if (type === 'CREATE_LAYOUTS') {
											this.loadDefaultValueForField(
												this.fieldsMap[element.FIELD_ID]
											);
										}
										this.loadEntryForEachFieldType(
											this.fieldsMap[element.FIELD_ID],
											type
										);
										if (styleType === 'EDITABLE') {
											sectionTemplate =
												sectionTemplate +
												this.getTemplateForField(
													this.fieldsMap[element.FIELD_ID]
												);
										} else if (styleType === 'ONE_TO_MANY_SECTIONS') {
											sectionTemplate =
												sectionTemplate +
												this.getTemplateForField(
													this.fieldsMap[element.FIELD_ID]
												);
										} else if (styleType === 'LABELS_WITH_TITLE') {
											sectionTemplate =
												sectionTemplate +
												this.getLabels(this.fieldsMap[element.FIELD_ID]);
										} else {
											sectionTemplate =
												sectionTemplate +
												'<mat-divider [vertical]=true></mat-divider>' +
												this.getPillsLayout(this.fieldsMap[element.FIELD_ID]);
										}
									});

									const sectionTemplateRegex = new RegExp(
										`<ng-container id="${section.ID}"(.*?)<\/ng-container>`
									);

									initialTemplate = initialTemplate.replace(
										sectionTemplateRegex,
										sectionTemplate
									);
								});
								this.module.FIELDS.forEach((moduleField) => {
									if (
										moduleField.DATA_TYPE.DISPLAY === 'Discussion' &&
										(this.module.NAME === 'Tickets' ||
											this.module.NAME === 'Users')
									) {
										this.loadEntryForEachFieldType(moduleField, type);
									}
								});

								this.template = initialTemplate;
							});
					}
					this.toggleNextButton();

					// only admin goes through walkthrough
					this.adminSignupSubscription = this.companiesService
						.getAdminSignup()
						.subscribe((signup) => {
							const userRole = this.rolesMap[this.userService.user.ROLE];
							if (signup.status === true && userRole === 'System Admin') {
								// check if company has completed walkthrough yet
								this.walkthroughService
									.getWalkthrough()
									.subscribe((walkthroughSuccess: any) => {
										if (
											!walkthroughSuccess.hasOwnProperty('TICKETS_DETAIL') &&
											this.module.NAME === 'Tickets' &&
											type === 'EDIT_LAYOUTS'
										) {
											if (
												this.getAvailableWalkthroughElements(this.module.NAME)
													.length === 8
											) {
												this.showLearnMore('Tickets');
											} else {
												// post walkthrough key with true value indicating walkthrough complete
												// this.walkthroughService
												// 	.postWalkthrough('TICKETS_DETAIL', false)
												// 	.subscribe((walkthroughPostSuccess: any) => {});
											}
										} else if (
											!walkthroughSuccess.hasOwnProperty('CHAT_DETAIL') &&
											this.module.NAME === 'Chats'
										) {
											this.showLearnMore('Chats');
										}
									});
							}
						});
				} else if (
					createLayout.PANELS !== null &&
					createLayout.PANELS !== undefined
				) {
					// GRID LAYOUT TEMPLATE
					this.customLayout = false;
					this.gridLayout = true;
					createLayout.PANELS.forEach((value) => {
						this.customLayouts.push({
							displayType: value.DISPLAY_TYPE,
							name: value.ID,
							displayName: value.PANEL_NAME,
							customLayout: `<div [ngStyle]="{'height': '100vh'}"
            				fxFlex fxLayoutAlign="center center"><mat-spinner></mat-spinner></div>`,
							collapse: value.SETTINGS.COLLAPSABLE,
							grids: JSON.parse(JSON.stringify(value.GRIDS)),
							fields: [],
							discussionDropped: false,
							discussionPosition: {
								xPos: null,
								yPos: null,
								size: 0,
							},
							settings: {
								action: value.SETTINGS.ACTION,
								conditions: value.SETTINGS.CONDITIONS,
							},
							display: true,
						});
						this.globalIndex++;
						value.SETTINGS.CONDITIONS.forEach((condition) => {
							if (this.conditionFields.indexOf(condition.CONDITION) === -1) {
								this.conditionFields.push(condition.CONDITION);
							}
						});

						value.GRIDS.forEach((grids) => {
							grids.forEach((grid) => {
								if (grid.SETTINGS && grid.SETTINGS.CONDITIONS.length > 0) {
									grid.SETTINGS.CONDITIONS.forEach((condition) => {
										if (
											this.conditionFields.indexOf(condition.CONDITION) === -1
										) {
											this.conditionFields.push(condition.CONDITION);
										}
									});
								}
							});
						});
					});
					this.groupTabs();
					if (createLayout.TITLE_BAR) {
						this.titleBar = createLayout.TITLE_BAR;
					}

					// LOAD FIELDS MAP
					this.module.FIELDS.forEach((field) => {
						this.fieldsMap[field.FIELD_ID] = field;
					});

					this.customLayouts.forEach((custom) => {
						// GET GRIDS AND SET IT TO GRIDS
						// DEFAULT GRID LAYOUT
						let discussionEncountered = false;
						let layout = `<div fxLayout="column" fxFlex>`;
						for (let i = 0; i < custom.grids.length; i++) {
							layout =
								layout +
								`<div class='ROW_${i}' fxFlex fxLayout="row" fxLayoutGap=10px>`;
							for (let j = 0; j < 4; j++) {
								if (custom.grids[i][j] && custom.grids[i][j].IS_EMPTY) {
									layout = layout + this.initialTemplate(custom.name, i, j);
								} else if (custom.grids[i][j]) {
									if (type === 'CREATE_LAYOUTS') {
										// Setting default values
										this.loadDefaultValueForField(
											this.fieldsMap[custom.grids[i][j].FIELD_ID]
										);
									}
									custom.fields.push(custom.grids[i][j].FIELD_ID);
									if (!discussionEncountered) {
										if (
											this.fieldsMap[custom.grids[i][j].FIELD_ID].DATA_TYPE
												.DISPLAY === 'Discussion'
										) {
											discussionEncountered = true;
											custom.discussionPosition.xPos = i;
											custom.discussionPosition.yPos = j;
										}
									}
									layout = layout + this.initialTemplate(custom.name, i, j);
								}
							}
							layout = layout + `</div> <!--END_ROW_${i}-->`;
						}
						layout = layout + `</div>`;
						custom.customLayoutTemplate = `<!--CUSTOM_LAYOUT_START-->
        <!--START_REPLACABLE_LAYOUT-->
        ${layout}
        <!--END_REPLACABLE_LAYOUT-->`;
						let size = 0;
						if (discussionEncountered) {
							for (let y = custom.discussionPosition.yPos; y < 4; y++) {
								if (
									!custom.grids[custom.discussionPosition.xPos][y].IS_EMPTY &&
									this.fieldsMap[
										custom.grids[custom.discussionPosition.xPos][y].FIELD_ID
									].DATA_TYPE.DISPLAY === 'Discussion'
								) {
									size = size + 1;
								}
							}
							custom.discussionPosition.size = size;
							this.loadLayoutWithDiscussionSection(
								custom,
								custom.discussionPosition,
								size
							);
						}
						this.loadRenderLayout(custom, type);
					});
					this.layoutName = createLayout.NAME;
					if (createLayout.TITLE_BAR) {
						this.titleBarTemplate = this.buildTitleBarTemplate(
							createLayout.TITLE_BAR
						);
					}
					this.evaluateConditions('', 'INITIALISE');
				}
			}
		}
	}

	// CUSTOM LAYOUT FUNCTIONS

	public loadDefaultValueForField(moduleField) {
		if (
			moduleField &&
			moduleField.DEFAULT_VALUE != null &&
			moduleField.DEFAULT_VALUE !== '' &&
			moduleField.DEFAULT_VALUE.search(',') === -1
		) {
			if (
				(moduleField.DATA_TYPE.DISPLAY === 'Relationship' &&
					moduleField.RELATIONSHIP_TYPE === 'Many to Many') ||
				moduleField.DATA_TYPE.DISPLAY === 'Picklist (Multi-Select)'
			) {
				this.entry[moduleField.NAME] = [];
				this.entry[moduleField.NAME].push(moduleField.DEFAULT_VALUE);
			} else if (
				moduleField.DATA_TYPE.DISPLAY === 'Relationship' &&
				moduleField.RELATIONSHIP_TYPE === 'Many to One'
			) {
				// Current user replace

				if (
					moduleField.DEFAULT_VALUE.match(new RegExp('{{(.*?)}}'))[1] ===
					'CURRENT_USER'
				) {
					this.entry[moduleField.NAME] = this.userService.user.DATA_ID;
				} else if (
					moduleField.DATA_TYPE.DISPLAY === 'Relationship' &&
					moduleField.RELATIONSHIP_TYPE === 'Many to One'
				) {
					// Current user replace
					if (
						moduleField.DEFAULT_VALUE.match(new RegExp('{{(.*?)}}'))[1] ===
						'CURRENT_USER'
					) {
						this.entry[moduleField.NAME] = this.userService.user.DATA_ID;
					} else {
						this.entry[moduleField.NAME] = moduleField.DEFAULT_VALUE;
					}
				} else if (moduleField.DATA_TYPE.DISPLAY === 'Phone') {
					this.entry[moduleField.NAME]['PHONE_NUMBER'] =
						moduleField.DEFAULT_VALUE;
				} else if (moduleField.DATA_TYPE.DISPLAY === 'Checkbox') {
					this.entry[moduleField.NAME] =
						moduleField.DEFAULT_VALUE === 'true' ? true : false;
				} else {
					this.entry[moduleField.NAME] = moduleField.DEFAULT_VALUE;
				}
			} else if (moduleField.DATA_TYPE.DISPLAY === 'Phone') {
				let defaultValue = JSON.parse(moduleField.DEFAULT_VALUE);
				this.entry[moduleField.NAME] = {};
				if (defaultValue.COUNTRY_CODE === '') {
					this.entry[moduleField.NAME]['COUNTRY_CODE'] = 'us';
				} else {
					this.entry[moduleField.NAME]['COUNTRY_CODE'] =
						defaultValue.COUNTRY_CODE;
				}
				if (defaultValue.DIAL_CODE === '') {
					this.entry[moduleField.NAME]['DIAL_CODE'] = '+1';
				} else {
					this.entry[moduleField.NAME]['DIAL_CODE'] = defaultValue.DIAL_CODE;
				}
				this.entry[moduleField.NAME]['PHONE_NUMBER'] =
					defaultValue.PHONE_NUMBER;
				if (defaultValue.COUNTRY_FLAG === '') {
					this.entry[moduleField.NAME]['COUNTRY_FLAG'] = 'us.svg';
				} else {
					this.entry[moduleField.NAME]['COUNTRY_FLAG'] =
						defaultValue.COUNTRY_FLAG;
				}
			} else if (moduleField.DATA_TYPE.DISPLAY === 'Checkbox') {
				this.entry[moduleField.NAME] =
					moduleField.DEFAULT_VALUE === 'true' ? true : false;
			} else {
				this.entry[moduleField.NAME] = moduleField.DEFAULT_VALUE;
			}
		} else if (
			moduleField &&
			moduleField.DEFAULT_VALUE != null &&
			moduleField.DEFAULT_VALUE !== '' &&
			moduleField.DEFAULT_VALUE.search(',') !== -1
		) {
			if (
				moduleField.DATA_TYPE.DISPLAY === 'Relationship' &&
				moduleField.RELATIONSHIP_TYPE === 'Many to Many'
			) {
				const defaultValues = moduleField.DEFAULT_VALUE.split(',');
				this.entry[moduleField.NAME] = defaultValues;
			}
		}
	}

	public evaluateConditions(event, fieldId) {
		if (
			fieldId === 'INITIALISE' ||
			this.conditionFields.indexOf(fieldId) !== -1
		) {
			this.customLayouts.forEach((value) => {
				const { action, conditions } = value.settings;
				if (
					conditions.length > 0 &&
					(fieldId === 'INITIALISE' ||
						conditions.map((v) => v.CONDITION).indexOf(fieldId) !== -1)
				) {
					value.display =
						action === 'SHOW'
							? this.validateConditions(conditions, fieldId)
							: !this.validateConditions(conditions, fieldId);
				}
				if (!value.display) {
					value.fields.forEach((field) => {
						delete this.entry[this.fieldsMap[field].NAME];
					});
				}
				const pannelgrids = value.grids;
				pannelgrids.forEach((grids) => {
					grids.forEach((grid) => {
						if (!grid.IS_EMPTY && grid.SETTINGS) {
							const gridField = grid.FIELD_ID;
							const gridConditions = grid.SETTINGS.CONDITIONS;
							if (
								grid.SETTINGS.ACTION !== '' &&
								(fieldId === 'INITIALISE' ||
									gridConditions.map((v) => v.CONDITION).indexOf(fieldId) !==
										-1)
							) {
								const gridAction = grid.SETTINGS.ACTION;
								this.fieldsMap[gridField].VISIBILITY =
									gridAction === 'SHOW'
										? !this.validateConditions(gridConditions, fieldId)
										: this.validateConditions(gridConditions, fieldId);
							}
						}
					});
				});
			});

			this.titleBar.forEach((field) => {
				if (field.SETTINGS) {
					const fieldConditions = field.SETTINGS.CONDITIONS;
					if (
						field.SETTINGS.ACTION !== '' &&
						(fieldId === 'INITIALISE' ||
							fieldConditions.map((v) => v.CONDITION).indexOf(fieldId) !== -1)
					) {
						const fieldAction = field.SETTINGS.ACTION;
						this.fieldsMap[field.FIELD_ID].VISIBILITY =
							fieldAction === 'SHOW'
								? !this.validateConditions(fieldConditions, fieldId)
								: this.validateConditions(fieldConditions, fieldId);
					}
				}
			});
		}
	}

	public validateConditions(conditions, fieldId) {
		if (conditions.length === 0) {
			return true;
		}
		const fields = JSON.parse(JSON.stringify(this.module.FIELDS));
		const discussionField = fields.find(
			(f) => f.DATA_TYPE.DISPLAY === 'Discussion'
		);
		const dateFields = fields.filter(
			(f) =>
				f.DATA_TYPE.DISPLAY === 'Date/Time' ||
				f.DATA_TYPE.DISPLAY === 'Date' ||
				f.DATA_TYPE.DISPLAY === 'Time'
		);
		const allArray = [];
		const anyArray = [];

		conditions.forEach((condition) => {
			const { CONDITION, CONDITION_VALUE, OPERATOR, REQUIREMENT_TYPE } =
				condition;
			const fieldName = fields.find((f) => f.FIELD_ID === CONDITION).NAME;

			switch (OPERATOR.toLocaleUpperCase()) {
				case 'DOES_NOT_EXIST':
					if (!this.entry[fieldName]) {
						if (REQUIREMENT_TYPE === 'All') {
							allArray.push(true);
						} else if (REQUIREMENT_TYPE === 'Any') {
							anyArray.push(true);
						}
					} else {
						if (REQUIREMENT_TYPE === 'All') {
							allArray.push(false);
						} else if (REQUIREMENT_TYPE === 'Any') {
							anyArray.push(false);
						}
					}
					break;
				case 'EXISTS':
					if (this.entry[fieldName]) {
						if (REQUIREMENT_TYPE === 'All') {
							allArray.push(true);
						} else if (REQUIREMENT_TYPE === 'Any') {
							anyArray.push(true);
						}
					} else {
						if (REQUIREMENT_TYPE === 'All') {
							allArray.push(false);
						} else if (REQUIREMENT_TYPE === 'Any') {
							anyArray.push(false);
						}
					}
					break;
				case 'CHANGED':
					if (this.entry[fieldName] && fieldId === CONDITION) {
						allArray.push(true);
						anyArray.push(true);
					} else {
						allArray.push(false);
						anyArray.push(false);
					}
					break;
				case 'EQUALS_TO' || 'IS':
					if (discussionField && CONDITION === discussionField.FIELD_ID) {
						if (this.entry[fieldName]) {
							if (REQUIREMENT_TYPE === 'All') {
								let isValid = true;
								for (const message of this.entry[fieldName]) {
									if (message.MESSAGE !== CONDITION_VALUE) {
										isValid = false;
										break;
									}
								}
								allArray.push(isValid);
							} else if (REQUIREMENT_TYPE === 'Any') {
								let isValid = false;
								for (const message of this.entry[fieldName]) {
									if (message.MESSAGE === CONDITION_VALUE) {
										isValid = true;
										break;
									}
								}
								anyArray.push(isValid);
							}
						}
					} else {
						if (this.entry[fieldName] !== CONDITION_VALUE) {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(false);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(false);
							}
						} else {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(true);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(true);
							}
						}
					}
					break;
				case 'NOT_EQUALS_TO':
					if (discussionField && CONDITION === discussionField.FIELD_ID) {
						if (this.entry[fieldName]) {
							if (REQUIREMENT_TYPE === 'All') {
								let isValid = true;
								for (const message of this.entry[fieldName]) {
									if (message.MESSAGE === CONDITION_VALUE) {
										isValid = false;
										break;
									}
								}
								allArray.push(isValid);
							} else if (REQUIREMENT_TYPE === 'Any') {
								let isValid = false;
								for (const message of this.entry[fieldName]) {
									if (message.MESSAGE !== CONDITION_VALUE) {
										isValid = true;
										break;
									}
								}
								anyArray.push(isValid);
							}
						}
					} else {
						if (this.entry[fieldName] === CONDITION_VALUE) {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(false);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(false);
							}
						} else {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(true);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(true);
							}
						}
					}
					break;
				case 'CONTAINS':
					if (discussionField && CONDITION === discussionField.FIELD_ID) {
						if (this.entry[fieldName]) {
							if (REQUIREMENT_TYPE === 'All') {
								let isValid = true;
								for (const message of this.entry[fieldName]) {
									const textMessage: string = message.MESSAGE.replace(
										/<\/?[^>]+(>|$)/g,
										''
									).replace(/\s\s+/g, ' ');
									if (textMessage.indexOf(CONDITION_VALUE) === -1) {
										isValid = false;
										break;
									}
								}
								allArray.push(isValid);
							} else if (REQUIREMENT_TYPE === 'Any') {
								let isValid = false;
								for (const message of this.entry[fieldName]) {
									const textMessage: string = message.MESSAGE.replace(
										/<\/?[^>]+(>|$)/g,
										''
									).replace(/\s\s+/g, ' ');
									if (textMessage.indexOf(CONDITION_VALUE) !== -1) {
										isValid = true;
										break;
									}
								}
								anyArray.push(isValid);
							}
						}
					} else {
						if (
							this.entry[fieldName] &&
							this.entry[fieldName].indexOf(CONDITION_VALUE) !== -1
						) {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(true);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(true);
							}
						} else {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(false);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(false);
							}
						}
					}
					break;
				case 'DOES_NOT_CONTAIN':
					if (discussionField && CONDITION === discussionField.FIELD_ID) {
						if (this.entry[fieldName]) {
							if (REQUIREMENT_TYPE === 'All') {
								let isValid = true;
								for (const message of this.entry[fieldName]) {
									if (message.MESSAGE.indexOf(CONDITION_VALUE) !== -1) {
										isValid = false;
										break;
									}
								}
								allArray.push(isValid);
							} else if (REQUIREMENT_TYPE === 'Any') {
								let isValid = false;
								for (const message of this.entry[fieldName]) {
									if (message.MESSAGE.indexOf(CONDITION_VALUE) === -1) {
										isValid = true;
										break;
									}
								}
								anyArray.push(isValid);
							}
						}
					} else {
						if (
							this.entry[fieldName] &&
							this.entry[fieldName].indexOf(CONDITION_VALUE) !== -1
						) {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(false);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(false);
							}
						} else {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(true);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(true);
							}
						}
					}
					break;
				case 'REGEX':
					const regex = new RegExp(CONDITION_VALUE);
					if (regex.test(this.entry[fieldName])) {
						if (REQUIREMENT_TYPE === 'All') {
							allArray.push(true);
						} else if (REQUIREMENT_TYPE === 'Any') {
							anyArray.push(true);
						}
					} else {
						if (REQUIREMENT_TYPE === 'All') {
							allArray.push(false);
						} else if (REQUIREMENT_TYPE === 'Any') {
							anyArray.push(false);
						}
					}
					break;
				case 'LESS_THAN':
					if (dateFields.map((v) => v.FIELD_ID).indexOf(CONDITION) === -1) {
						if (
							parseInt(this.entry[fieldName], 10) >=
							parseInt(CONDITION_VALUE, 10)
						) {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(false);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(false);
							}
						} else {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(true);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(true);
							}
						}
					} else {
						if (this.entry[fieldName]) {
							if (!isNaN(Date.parse(CONDITION_VALUE))) {
								if (
									new Date(CONDITION_VALUE) < this.entry[fieldName].toDate()
								) {
									if (REQUIREMENT_TYPE === 'All') {
										allArray.push(false);
									} else if (REQUIREMENT_TYPE === 'Any') {
										anyArray.push(false);
									}
								} else {
									if (REQUIREMENT_TYPE === 'All') {
										allArray.push(true);
									} else if (REQUIREMENT_TYPE === 'Any') {
										anyArray.push(true);
									}
								}
							} else {
								if (REQUIREMENT_TYPE === 'All') {
									allArray.push(false);
								} else if (REQUIREMENT_TYPE === 'Any') {
									anyArray.push(false);
								}
							}
						} else {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(false);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(false);
							}
						}
					}
					break;
				case 'GREATER_THAN':
					if (dateFields.map((v) => v.FIELD_ID).indexOf(CONDITION) === -1) {
						if (
							parseInt(this.entry[fieldName], 10) <
							parseInt(CONDITION_VALUE, 10)
						) {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(false);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(false);
							}
						} else {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(true);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(true);
							}
						}
					} else {
						if (this.entry[fieldName]) {
							if (!isNaN(Date.parse(CONDITION_VALUE))) {
								if (
									new Date(CONDITION_VALUE) > this.entry[fieldName].toDate()
								) {
									if (REQUIREMENT_TYPE === 'All') {
										allArray.push(false);
									} else if (REQUIREMENT_TYPE === 'Any') {
										anyArray.push(false);
									}
								} else {
									if (REQUIREMENT_TYPE === 'All') {
										allArray.push(true);
									} else if (REQUIREMENT_TYPE === 'Any') {
										anyArray.push(true);
									}
								}
							} else {
								if (REQUIREMENT_TYPE === 'All') {
									allArray.push(false);
								} else if (REQUIREMENT_TYPE === 'Any') {
									anyArray.push(false);
								}
							}
						} else {
							if (REQUIREMENT_TYPE === 'All') {
								allArray.push(false);
							} else if (REQUIREMENT_TYPE === 'Any') {
								anyArray.push(false);
							}
						}
					}
					break;
				case 'IS_UNIQUE':
					if (fieldName) {
						if (REQUIREMENT_TYPE === 'All') {
							allArray.push(true);
						} else if (REQUIREMENT_TYPE === 'Any') {
							anyArray.push(true);
						}
					}
					break;
				default:
					if (REQUIREMENT_TYPE === 'All') {
						allArray.push(false);
					} else if (REQUIREMENT_TYPE === 'Any') {
						anyArray.push(false);
					}
					break;
			}
		});

		let allValue = true;
		let anyValue = true;
		for (const bool of allArray) {
			if (!bool) {
				allValue = false;
				break;
			}
		}
		for (const bool of anyArray) {
			if (!bool) {
				anyValue = false;
			} else {
				anyValue = true;
				break;
			}
		}
		return allValue && anyValue;
	}

	// THIS FUNCTION LOADS THE VALUES FOR THE SPECIFIC DATA TYPES - IT IS GENERIC USED BY TICKETS AND GRIDS
	public loadEntryForEachFieldType(moduleField, type) {
		if (moduleField.DATA_TYPE.DISPLAY === 'Phone') {
			if (!this.entry[moduleField.NAME]) {
				this.entry[moduleField.NAME] = {};
				this.entry[moduleField.NAME]['COUNTRY_CODE'] = 'us';
				this.entry[moduleField.NAME]['DIAL_CODE'] = '+1';
				this.entry[moduleField.NAME]['PHONE_NUMBER'] = '';
				this.entry[moduleField.NAME]['COUNTRY_FLAG'] = 'us.svg';
			}
		}
		if (moduleField.REQUIRED) {
			this.requiredFields.push(moduleField.NAME);
		}
		if (
			// tslint:disable-next-line: prefer-switch
			moduleField.DATA_TYPE.DISPLAY === 'Text' ||
			moduleField.DATA_TYPE.DISPLAY === 'Text Area' ||
			moduleField.DATA_TYPE.DISPLAY === 'Text Area Rich' ||
			moduleField.DATA_TYPE.DISPLAY === 'Text Area Long' ||
			moduleField.DATA_TYPE.DISPLAY === 'URL' ||
			moduleField.DATA_TYPE.DISPLAY === 'Email' ||
			moduleField.DATA_TYPE.DISPLAY === 'Street 1' ||
			moduleField.DATA_TYPE.DISPLAY === 'Street 2' ||
			moduleField.DATA_TYPE.DISPLAY === 'City' ||
			moduleField.DATA_TYPE.DISPLAY === 'State' ||
			moduleField.DATA_TYPE.DISPLAY === 'Country' ||
			moduleField.DATA_TYPE.DISPLAY === 'Zipcode' ||
			moduleField.DATA_TYPE.DISPLAY === 'Id' ||
			moduleField.DATA_TYPE.DISPLAY === 'Currency'
		) {
			if (
				moduleField.NAME !== 'CHANNEL' &&
				(this.entry[moduleField.NAME] === undefined ||
					this.entry[moduleField.NAME] === null)
			) {
				this.entry[moduleField.NAME] = '';
			}
		}
		if (moduleField.DATA_TYPE.DISPLAY === 'Discussion') {
			this.discussionFieldName = moduleField.NAME;
			if (type === 'CREATE_LAYOUTS') {
				this.entry[moduleField.NAME] = [];
			}
		} else if (moduleField.DATA_TYPE.DISPLAY === 'Relationship') {
			if (moduleField.RELATIONSHIP_TYPE === 'One to Many') {
				const relationModule = this.allModules.find(
					(module) => module.MODULE_ID === moduleField.MODULE
				);

				this.oneToManyModulesToListLayoutFields[relationModule.MODULE_ID] = [];

				if (
					relationModule.LIST_LAYOUTS &&
					relationModule.LIST_LAYOUTS.length > 0
				) {
					const listLayout = relationModule.LIST_LAYOUTS.find(
						(layout) =>
							layout.IS_DEFAULT && layout.ROLE === this.userService.user.ROLE
					);
					if (listLayout) {
						listLayout.COLUMN_SHOW.FIELDS.forEach((fieldId) => {
							const field = relationModule.FIELDS.find(
								(relationField) => relationField.FIELD_ID === fieldId
							);
							if (
								field.DATA_TYPE.DISPLAY === 'Text' ||
								field.DATA_TYPE.DISPLAY === 'Picklist'
							) {
								if (
									this.oneToManyModulesToListLayoutFields[
										relationModule.MODULE_ID
									].length <= 3
								) {
									this.oneToManyModulesToListLayoutFields[
										relationModule.MODULE_ID
									].push({
										NAME: field.NAME,
										DISPLAY_LABEL: field.DISPLAY_LABEL,
									});
								}
							}
						});
					}
				}
				this.modulesService
					.getOneToManyData(
						this.moduleId,
						relationModule.MODULE_ID,
						moduleField.FIELD_ID,
						this.entryId,
						'5',
						'1',
						'DATE_UPDATED',
						'desc'
					)
					.subscribe(
						(response: any) => {
							const field = moduleField.NAME;
							this.relationFieldEntries[moduleField.NAME] = response.DATA;
							this.oneToManyCountMap[field] =
								this.relationFieldEntries[moduleField.NAME].length;
						},
						(error: any) => {
							console.log(error);
						}
					);
			} else {
				// Relationship field controls
				const fieldControlName =
					moduleField.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
				this.formControls[fieldControlName] = new FormControl();
				if (moduleField.NOT_EDITABLE) {
					this.formControls[fieldControlName].disable();
				}
				const relationModule = this.allModules.find(
					(module) => module.MODULE_ID === moduleField.MODULE
				);

				const primaryDisplayField = relationModule.FIELDS.find(
					(tempField) =>
						tempField.FIELD_ID === moduleField.PRIMARY_DISPLAY_FIELD
				);

				if (moduleField.PRIMARY_DISPLAY_FIELD !== null) {
					this.moduleFields[moduleField.NAME].PRIMARY_DISPLAY_FIELD_NAME =
						primaryDisplayField.NAME;
				}

				// Get relationship entries
				if (
					moduleField.RELATIONSHIP_TYPE === 'One to One' ||
					moduleField.RELATIONSHIP_TYPE === 'Many to One'
				) {
					if (this.entry[moduleField.NAME]) {
						this.modulesService
							.getEntry(relationModule.MODULE_ID, this.entry[moduleField.NAME])
							.subscribe((response: any) => {
								if (this.relationFieldEntries[moduleField.NAME]) {
									this.relationFieldEntries[moduleField.NAME].push(response);
								} else {
									this.relationFieldEntries[moduleField.NAME] = [response];
								}

								this.formControls[fieldControlName].setValue(
									this.getRelationshipData(
										this.entry[moduleField.NAME],
										moduleField.NAME
									)[
										this.moduleFields[moduleField.NAME]
											.PRIMARY_DISPLAY_FIELD_NAME
									]
								);
							});
					}
				}
				if (moduleField.RELATIONSHIP_TYPE === 'Many to Many') {
					if (moduleField.NOT_EDITABLE && !this.showSaveButton) {
						this.formControls[fieldControlName].disable();
					}
				}
				this.formControls[fieldControlName].valueChanges
					.pipe(
						startWith(''),
						debounceTime(400),
						distinctUntilChanged(),
						switchMap((value: any) => {
							const fieldSearch =
								primaryDisplayField.NAME +
								'=' +
								(value && value !== '' ? value : '*');
							return this.modulesService
								.getFieldFilteredPaginatedSearchEntries(
									moduleField.MODULE,
									fieldSearch,
									primaryDisplayField.NAME,
									'asc',
									1,
									10,
									moduleField.RELATIONSHIP_FIELD
								)
								.pipe(
									map(
										(results: any) =>
											(this.relationFieldFilteredEntries[moduleField.NAME] =
												results.DATA)
									)
								);
						})
					)
					.subscribe();
			}
		} else if (
			// Tranformation of Backend value in chronometer format
			moduleField.DATA_TYPE.DISPLAY === 'Chronometer' &&
			this.entry[moduleField.NAME] !== undefined
		) {
			this.entry[moduleField.NAME] =
				this.renderLayoutService.chronometerFormatTransform(
					this.entry[moduleField.NAME],
					''
				);
			this.oldChronometerValue = this.entry[moduleField.NAME];
		}
	}

	// TEMPLATE FOR EMPTY SPACE
	public initialTemplate(name, i, j) {
		let custom = this.customLayouts.find((f) => f.name === name);
		let index = this.customLayouts.findIndex((f) => f.name === name);
		if (custom.grids[i][j].WIDTH !== 0) {
			return `<div class='CELL_${name}_${i}_${j}'
        fxFlex="{{context.customLayouts[${index}].grids[${i}][${j}].WIDTH}}">
      </div><!--END_CELL_${name}_${i}_${j}-->`;
		} else {
			return `<div class='CELL_${name}_${i}_${j}' 
				*ngIf="context.customLayouts[${index}].grids[${i}][${j}].WIDTH !== 0">
			</div><!--END_CELL_${name}_${i}_${j}-->`;
		}
	}

	public loadLayoutWithDiscussionSection(custom, discussionPosition, size) {
		const removeDivFrom = discussionPosition.xPos + 1;
		const removeDivTo = custom.grids.length - 1;

		custom.customLayoutTemplate = custom.customLayoutTemplate.replace(
			new RegExp(
				`<div class='ROW_${removeDivFrom}([\\s\\S]*?)<!--END_ROW_${removeDivTo}-->`
			),
			''
		);
		const rowRegex = new RegExp(
			`<div class='ROW_${discussionPosition.xPos}([\\s\\S]*?)<!--END_ROW_${discussionPosition.xPos}-->`
		);
		custom.customLayoutTemplate = custom.customLayoutTemplate.replace(
			rowRegex,
			this.buildTemplate(custom, size)
		);
		custom.discussionDropped = true;
	}

	// TEMPLATE ONLY FOR DISCUSSION
	public buildTemplate(custom, size) {
		let flex = 0;
		const columnTemplate = `<div fxLayout=column fxLayoutGap=5px fxFlex='COLUMN_FLEX'>ADD_ROWS_FOR_THIS_COLUMN</div>`;
		let rows = ``;
		const discussionInitialSize = custom.discussionPosition.size;
		const xPos = custom.discussionPosition.xPos;
		const yPos = custom.discussionPosition.yPos;
		const field = custom.grids[xPos][yPos].FIELD_ID;

		if (discussionInitialSize === 3) {
			for (let x = xPos; x < custom.grids.length; x++) {
				for (let y = yPos; y < yPos + 3; y++) {
					custom.grids[x][y] = {
						IS_EMPTY: true,
						HEIGHT: custom.grids.length,
						WIDTH: 100,
						FIELD_ID: '',
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
		}

		for (let x = xPos; x < custom.grids.length; x++) {
			for (let y = yPos; y < yPos + size; y++) {
				custom.grids[x][y] = {
					IS_EMPTY: false,
					HEIGHT: custom.grids.length,
					WIDTH: 100,
					FIELD_ID: field,
				};
			}
		}

		let row1 = '';
		let row2 = '';
		for (let x = xPos; x < custom.grids.length; x++) {
			if (yPos === 1 && size === 2) {
				row1 = row1 + this.buildRowForDiscussion(custom, 3, x, yPos);
				row2 = row2 + this.buildRowForDiscussion(custom, 3, x, 0);
			} else {
				rows = rows + this.buildRowForDiscussion(custom, size, x, yPos);
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
			this.initialTemplate(custom.name, xPos, yPos)
		);
		let mainTemplate = `<div class='DISCUSSION_SECTION' fxLayoutGap=5px fxFlex fxLayout="row">`;
		if (yPos === 0) {
			// FIRST COLUMN DISCUSSION
			mainTemplate = mainTemplate + columnWithDiscussion + columnWithRows;
		} else if (yPos === 1 && size === 2) {
			// DISCUSSION IN MIDDLE
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
			// DISCUSSION AT END
			mainTemplate = mainTemplate + columnWithRows + columnWithDiscussion;
		}
		mainTemplate = mainTemplate + `</div><!--END_DISCUSSION_SECTION-->`;
		return mainTemplate;
	}

	// ROWS FOR DISCUSSION
	public buildRowForDiscussion(custom, size, i, j) {
		let row = `<div class='ROW_${i}' fxLayoutGap=5px fxLayout=row fxFlex>`;
		for (let y = 0; y < 4; y++) {
			if (size === 2 && j !== 1) {
				if (y !== j && y !== j + 1) {
					custom.grids[i][y].WIDTH = 50;
					row = row + this.initialTemplate(custom.name, i, y);
				}
			} else if (size === 3) {
				if (y === (j - 1 < 0 ? 3 : j - 1)) {
					custom.grids[i][y].WIDTH = 100;
					row = row + this.initialTemplate(custom.name, i, y);
				}
			}
		}
		row = row + `</div> <!--END_ROW_${i}-->`;
		return row;
	}

	public loadRenderLayout(custom, type) {
		// FIRST REPLACE DISCUSSION SECTION IF PRESENT
		let index = this.customLayouts.findIndex((f) => f.name === custom.name);
		if (custom.discussionDropped) {
			const xPos = custom.discussionPosition.xPos;
			const yPos = custom.discussionPosition.yPos;
			const cellRegex = new RegExp(
				`<div class='CELL_${custom.name}_${xPos}_${yPos}([\\s\\S]*?)<!--END_CELL_${custom.name}_${xPos}_${yPos}-->`,
				'g'
			);
			const field = this.fieldsMap[custom.grids[xPos][yPos].FIELD_ID];
			custom.customLayoutTemplate = custom.customLayoutTemplate.replace(
				cellRegex,
				`<div class='CELL_${custom.name}_${xPos}_${yPos}'
        fxLayout="column" fxFlex="{{context.customLayouts[${index}].grids[${xPos}][${yPos}].WIDTH}}"
      [ngStyle]="{'border-radius': '5px'}">
            <div fxLayout= "row" fxLayoutGap= "20px" [ngStyle]= "{'padding': '15px', 'min-height': '45vh'}">
                    <div fxLayout= "row" fxLayoutAlign= "center center"
                    [ngStyle]= "{'border-radius': '50%', 'border': '1px solid #68737D','width': '40px', 'height': '40px'}">
                        <label class= "mat-body-2">
                        {{context.userService.user.FIRST_NAME | firstLetter}}{{context.userService.user.LAST_NAME | firstLetter}}
                        </label>
                    </div>
                    <div fxLayout= "column" fxFlex>
                        <div fxFill fxLayout= "row">
                            <div fxFill class= "text-no-bottom-padding no-hover-effect" fxLayout= "column" fxFlex= "100">
                                <div fxFlex>
                                    <tinymce #editor [config]= "context.config" [(ngModel)]= "context.message"
									(ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}')"></tinymce>
                                </div>
                                <div fxLayout= "row" fxLayoutAlign= "space-between center"
                                [ngStyle]= "{'border': '1px solid #ccc','border-top':'0px'}">
                                    <div fxLayout= "row" fxLayoutAlign= "start center" fxLayoutGap= "10px"
                                    [ngStyle]= "{'font-size': '20px', 'padding': '10px'}">
                                        <mat-icon inline class= "pointer" (click)= "fileInput.click()">attach_file</mat-icon>
                                        <input hidden type= "file" #fileInput (change)= "context.onFileChange($event)">
                                        <ng-container *ngFor= "let attachment of context.attachments; index as i">
                                            <label class= "mat-body-strong">{{attachment.FILE_NAME}}</label>
                                            <mat-icon class= "pointer" (click)= "context.attachments.splice(i,1)">close</mat-icon>
                                        </ng-container>
                                        <div fxLayout= "row" *ngIf="!context.isCreateLayout" >
                                            <div [ngStyle]= "{'height': '22px', 'color': '#2F3941'}"
                                            class= "pointer" matRipple [matMenuTriggerFor]= "premadeResponses" fxLayout= "row">
                                                <mat-icon>chat_bubble_outline</mat-icon>
                                                <div class= "mat-small" [ngStyle]= "{'margin-left': '2px'}">Pre-made responses</div>
                                            </div>

                                            <mat-menu #premadeResponses= "matMenu">
                                            <div *ngIf="context.premadeResponses.length > 0">
                                                <div mat-menu-item class= "pointer"
                                                *ngFor= "let premadeResponse of context.premadeResponses"
                                                fxLayout= "row"
                                                (click)= "context.addPremadeResponse(premadeResponse)">
												<span>{{premadeResponse.NAME}}</span> </div></div>
												<div *ngIf="context.premadeResponses.length===0 && context.currentRole.NAME === 'SystemAdmin'">
												<button mat-menu-item (click) ="context.newPremadeResponse()">
												<mat-icon>add</mat-icon>{{'NEW_PREMADE_RESPONSE' | translate}}</button></div>
												<div *ngIf="context.premadeResponses.length===0 && context.currentRole.NAME !== 'SystemAdmin'">
												<span mat-menu-item>{{'NO_PREMADE_RESPONSES_FOUND' | translate}}</span></div>
                                            </mat-menu>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <mat-divider></mat-divider>
<div class= "DISCUSSION_SECTION_REPLACE" style= "word-break: break-word" fxFlex>
                    <div fxLayout= "column" *ngFor= "let message of context.entry['${field.NAME}'] | reverse">
                        <div fxLayout= "row" fxLayoutGap= "20px" [ngStyle]= "{'padding': '15px'}">
                            <div *ngIf= "message.MESSAGE_TYPE=='MESSAGE'" fxLayout= "row"
                            fxLayoutAlign= "center center"
                            [ngStyle]= "{'border-radius': '50%', 'border': '1px solid #68737D', 'width': '40px', 'height': '40px'}">
                                <label class= "mat-body-2">
                                {{message.SENDER.FIRST_NAME | firstLetter}}{{message.SENDER.LAST_NAME | firstLetter}}</label>
                            </div>
                            <div *ngIf= "message.MESSAGE_TYPE==='MESSAGE';" fxLayout= "column" fxFlex= "100" fxLayoutGap= "10px">
                                <div fxLayout= "row" fxLayoutAlign= " center" fxLayoutGap= "10px">
                                    <div fxLayout= "column">
                                        <label class= "mat-body-strong"
                                        [ngStyle]= "{'margin-bottom': '0px'}">
                                         {{message.SENDER.FIRST_NAME}} {{message.SENDER.LAST_NAME}} </label>
                                    </div>
                                    <div fxLayout= "column" [ngStyle]= "{'color': '#68737D'}">
                                        <label class= "mat-caption">{{message.DATE_CREATED | date: 'MMM d, y h:mm a'}}</label>
                                    </div>
                                </div>
                                <div>
                                    <p class= "mat-body" [ngStyle]= "{'margin-bottom': '0px'}"
                                    [innerHtml]= "message.MESSAGE | allowStyles"></p>
                                </div>
                                <div fxLayout= "row" fxLayoutGap= "10px" class= "pointer">
                                    <div *ngFor= "let attachment of message.ATTACHMENTS"
                                    fxLayoutAlign= "start center" [ngStyle]= "{'color': '#1f73b7', 'border-radius': '5px'}">
                                        <a [ngStyle]= "{'color': '#1f73b7', 'text-decoration': 'none'}"
                                        class= "mat-body" fxLayout= "row" fxLayoutAlign= "center center"
                                        [attr.href]= "context.downloadAttachment(attachment.ATTACHMENT_UUID, message.MESSAGE_ID)"
                                        target= "blank" download= "attachment.FILE_NAME">
                                            <mat-icon>attach_file</mat-icon>{{attachment.FILE_NAME}} </a>
                                    </div>
								</div>
                            </div>
                            <div *ngIf= "message.MESSAGE_TYPE==='META_DATA';" fxLayout= "column" fxFlex= "100">
                                <div fxLayout= "row" fxLayoutAlign= " center" [ngStyle]= "{'color': '#68737D'}">
                                    <label class= "mat-caption">Event occurred at {{message.DATE_CREATED | date: 'MMM d, y h:mm a'}}</label>
                                </div>
                                <p class= "mat-body" [ngStyle]= "{'margin-bottom': '0px'}" [innerHtml]= "message.MESSAGE | allowStyles"></p>
                            </div>
                        </div>
                        <mat-divider></mat-divider>
                    </div>
                </div>
        </div>
      <!--END_CELL_${custom.name}_${xPos}_${yPos}-->
 `
			);
			this.loadEntryForEachFieldType(field, type);
		}
		// LOOP AND REPLACE ALL THE OTHER FIELDS
		for (let i = 0; i < custom.grids.length; i++) {
			for (let j = 0; j < 4; j++) {
				if (custom.grids[i][j]) {
					const field = this.fieldsMap[custom.grids[i][j].FIELD_ID];
					const cellRegex = new RegExp(
						`<div class='CELL_${custom.name}_${i}_${j}([\\s\\S]*?)<!--END_CELL_${custom.name}_${i}_${j}-->`,
						'g'
					);
					if (
						!custom.grids[i][j].IS_EMPTY &&
						field.DATA_TYPE.DISPLAY !== 'Discussion'
					) {
						// TO LOAD THE RELATIONSHIP AND OTHER VALUES FOR AUTOCOMPLETE
						this.loadEntryForEachFieldType(field, type);
						custom.customLayoutTemplate = custom.customLayoutTemplate.replace(
							cellRegex,
							`<div class='CELL_${custom.name}_${i}_${j}'
              fxLayout="row" fxFlex="calc({{context.customLayouts[${index}].grids[${i}][${j}].WIDTH}}% - 10px)" fxLayoutAlign='center center'
            [ngStyle]="{'border-radius': '5px','margin':'10px'}" fxLayoutGap="15px">
              ${this.getTemplateForField(field)}</div><!--END_CELL_${
								custom.name
							}_${i}_${j}-->`
						);
					}
				}
			}
		}
	}

	// FIELD SPECIFIC TEMPLATE
	public getTemplateForField(field) {
		switch (field.DATA_TYPE.DISPLAY) {
			case 'Street 1':
				return this.street1Service.getStreet1(field, this.layoutStyle);
			case 'Street 2':
				return this.street2Service.getStreet2(field, this.layoutStyle);
			case 'City':
			case 'State':
				return this.cityService.getCity(field, this.layoutStyle);
			case 'Country':
			case 'Zipcode':
				return this.countryService.getCountry(field, this.layoutStyle);
			case 'Text':
			case 'Email':
			case 'URL':
			case 'Id':
				return this.textService.getText(field, this.layoutStyle);
			case 'Picklist':
				return this.picklistService.getPicklist(field, this.layoutStyle);
			case 'Phone':
				return this.phoneService.getPhone(field, this.layoutStyle);
			case 'Relationship':
				return this.relationshipService.getRelationship(
					field,
					this.layoutStyle
				);
				break;
			case 'List Text':
				return this.listTextService.getListText(field, this.layoutStyle);
			case 'Number':
				return this.numberService.getNumber(
					field,
					this.layoutStyle,
					this.layoutType
				);
			case 'Currency':
				return this.currencyService.getCurrency(field, this.layoutStyle);
			case 'Auto Number':
				return this.autoNumberService.getAutoNumber(field, this.layoutType);
				break;
			case 'Formula':
				return this.formulaService.getFormula(
					field,
					this.layoutStyle,
					this.layoutType
				);
			case 'Currency Exchange':
				return this.currencyExchangeService.getCurrencyExchange(
					field,
					this.layoutStyle
				);
			case 'Chronometer':
				return this.chronometerService.getChronometer(field, this.layoutStyle);
			case 'Checkbox':
				return this.checkboxService.getCheckbox(field);
			case 'Text Area':
			case 'Text Area Rich':
			case 'Text Area Long':
				return this.textAreaService.getTextArea(field, this.layoutStyle);
			case 'Date/Time':
				return this.dateTimeService.getDateTime(
					field,
					this.layoutStyle,
					this.layoutType
				);
			case 'Time':
				return this.timeService.getTime(field, this.layoutStyle);
			case 'Date':
				return this.dateService.getDate(
					field,
					this.layoutStyle,
					this.layoutType
				);
			case 'Button':
				return this.buttonService.getButton(field);
			case 'File Upload':
				return this.fileUploadService.getFileUpload();
		}
		if (this.module.NAME === 'Accounts') {
			return '';
		} else {
			return `
		<mat-form-field appearance="${this.layoutStyle}"
		[style.visibility]="context.fieldsMap['${field.FIELD_ID}'].VISIBILITY  ? 'hidden' : 'visible' " fxFlex floatLabel="always">
		<mat-label>${field.DISPLAY_LABEL}</mat-label>
		  <input matInput type="text" autocomplete="off"
			  [(ngModel)]="context.entry.${field.NAME}"
			  (ngModelChange)="context.evaluateConditions($event, '${field.FIELD_ID}')"
			  [required]="context.moduleFields['${field.NAME}'].REQUIRED"
			  [disabled]="!context.showSaveButton || context.moduleFields['${field.NAME}'].NOT_EDITABLE">
			  <mat-error>${field.DISPLAY_LABEL} {{ "IS_REQUIRED" | translate }}.</mat-error>
			  <mat-hint>${field.HELP_TEXT}</mat-hint>
		</mat-form-field>
	  `;
		}
	}

	// CUSTOM LAYOUT FUNCTIONS

	public customButtonClick(fieldId: String) {
		const customButtonPayload = {
			COMPANY_UUID: this.userService.companyUuid,
			MODULE_ID: this.moduleId,
			ENTRY_ID: this.entryId,
			FIELD_ID: fieldId,
			USER_UUID: this.userService.user.USER_UUID,
		};
		// this._stompService._stompManagerService.publish({
		// 	destination: `ngdesk/button/event`,
		// 	body: JSON.stringify(customButtonPayload),
		// });
	}

	private getField(fieldId) {
		return this.module.FIELDS.find((field) => field.FIELD_ID === fieldId);
	}

	public getModuleFields(moduleId) {
		const moduleFound = this.allModules.find(
			(module) => module.MODULE_ID === moduleId
		);
		return moduleFound['FIELDS'];
	}

	public getAddress(place, fieldName) {
		const field = fieldName.toUpperCase().split(' ')[0];
		this.getStreet1(place, field);
		this.getStreet2(place, field);
		this.getCity(place, field);
		this.getState(place, field);
		this.getZipCode(place, field);
	}

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
		// tslint:disable-next-line: one-variable-per-declaration
		const COMPONENT_TEMPLATE = 'street_number';
		const streetNumber = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		this.entry[fieldName + '_STREET_1'] = streetNumber;
	}

	public getStreet2(place, fieldName) {
		// tslint:disable-next-line: one-variable-per-declaration
		const COMPONENT_TEMPLATE = 'route';
		const street = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		this.entry[fieldName + '_STREET_2'] = street;
	}

	public getCity(place, fieldName) {
		// tslint:disable-next-line: one-variable-per-declaration
		const COMPONENT_TEMPLATE = 'locality';
		const city = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		if (city === undefined) {
			this.entry[fieldName + '_CITY'] = this.entry[fieldName + '_STATE'];
		} else {
			this.entry[fieldName + '_CITY'] = city;
		}
	}

	public getZipCode(place, fieldName) {
		// tslint:disable-next-line: one-variable-per-declaration
		const COMPONENT_TEMPLATE = 'postal_code';
		const postCode = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		this.entry[fieldName + '_ZIPCODE'] = postCode;
	}

	public getState(place, fieldName) {
		// tslint:disable-next-line: one-variable-per-declaration
		const COMPONENT_TEMPLATE = 'administrative_area_level_1';
		const state = this.getAddrComponent(place, COMPONENT_TEMPLATE);
		// tslint:disable-next-line: one-variable-per-declaration
		const TEMPLATE = 'country';
		const country = this.getAddrComponent(place, TEMPLATE);
		this.entry[fieldName + '_STATE'] = state + ', ' + country;
	}

	public concatenatedValuesForFormula(event, fieldId) {
		const formulaField = this.module.FIELDS.filter(
			(field) => field.DATA_TYPE.DISPLAY === 'Formula'
		);
		const textField = this.module.FIELDS.filter(
			(field) => field.DATA_TYPE.DISPLAY === 'Text'
		);
		formulaField.forEach((form) => {
			if (form.FORMULA.FIELD_LHS !== null && form.FORMULA.FIELD_RHS !== null) {
				textField.forEach((text1) => {
					if (form.FORMULA.FIELD_LHS === text1.FIELD_ID) {
						this.textLhs = this.entry[text1.NAME];
						textField.forEach((text2) => {
							if (form.FORMULA.FIELD_RHS === text2.FIELD_ID) {
								this.textRhs = this.entry[text2.NAME];
								this.textSeparator = form.FORMULA.SEPARATOR;
								if (
									this.textLhs !== undefined &&
									this.textRhs !== undefined &&
									this.textSeparator === null &&
									form.FORMULA.OPERATOR === 'Concatenate'
								) {
									this.concatenateValue = this.textLhs + this.textRhs;
									this.entry[form.NAME] = this.concatenateValue;
								} else if (
									this.textLhs !== undefined &&
									this.textRhs !== undefined &&
									this.textSeparator !== null &&
									form.FORMULA.OPERATOR === 'Concatenate'
								) {
									this.concatenateValue =
										this.textLhs + this.textSeparator + this.textRhs;
									this.entry[form.NAME] = this.concatenateValue;
								}
							}
						});
					}
				});
			}
		});
	}

	public calculatedValuesForFormula(event, fieldId) {
		console.log('hit');
		const formulaField = this.module.FIELDS.filter(
			(field) => field.DATA_TYPE.DISPLAY === 'Formula'
		);
		const numberField = this.module.FIELDS.filter(
			(field) =>
				field.DATA_TYPE.DISPLAY === 'Number' ||
				field.DATA_TYPE.DISPLAY === 'Currency' ||
				field.DATA_TYPE.DISPLAY === 'Currency Exchange' ||
				field.DATA_TYPE.DISPLAY === 'Formula'
		);
		formulaField.forEach((form) => {
			if (form.FORMULA.FIELD_LHS !== null && form.FORMULA.FIELD_RHS !== null) {
				numberField.forEach((num1) => {
					if (form.FORMULA.FIELD_LHS === num1.FIELD_ID) {
						this.lhs = this.entry[num1.NAME];
						numberField.forEach((num2) => {
							if (form.FORMULA.FIELD_RHS === num2.FIELD_ID) {
								this.rhs = this.entry[num2.NAME];
								if (
									this.lhs !== undefined &&
									this.rhs !== undefined &&
									form.FORMULA.OPERATOR === 'Add'
								) {
									// tslint:disable-next-line: radix
									this.addValue = parseInt(this.lhs) + parseInt(this.rhs);
									this.entry[form.NAME] = this.addValue;
								} else if (
									this.lhs !== undefined &&
									this.rhs !== undefined &&
									form.FORMULA.OPERATOR === 'Subtract'
								) {
									// tslint:disable-next-line: radix
									this.subValue = parseInt(this.lhs) - parseInt(this.rhs);
									this.entry[form.NAME] = this.subValue;
								} else if (
									this.lhs !== undefined &&
									this.rhs !== undefined &&
									form.FORMULA.OPERATOR === 'Multiply'
								) {
									// tslint:disable-next-line: radix
									this.mulValue = parseInt(this.lhs) * parseInt(this.rhs);
									this.entry[form.NAME] = this.mulValue;
								} else if (
									this.lhs !== undefined &&
									this.rhs !== undefined &&
									form.FORMULA.OPERATOR === 'Division'
								) {
									// tslint:disable-next-line: radix
									this.divValue = parseInt(this.lhs) / parseInt(this.rhs);
									this.entry[form.NAME] = this.divValue;
								}
							}
						});
					}
				});
			}
		});
	}

	// transform Ids -> Objects
	// private transformObjects(arr, initialArray, key) {
	//   const arrWithObjects = [];
	//   for (const id of arr) {
	//     for (const obj of initialArray) {
	//       if (obj[key] === id) {
	//         arrWithObjects.push(obj);
	//       }
	//     }
	//   }
	//   return arrWithObjects;
	// }

	// transform Objects -> Ids
	// private transformIds(arr, key) {
	//   const arrWithIds = [];
	//   for (const obj of arr) {
	//     if (typeof obj === 'object') {
	//       arrWithIds.push(obj[key]);
	//     } else {
	//       arrWithIds.push(obj);
	//     }
	//   }
	//   return arrWithIds;
	// }
	// private validateFields() {
	//   let valid = true;
	//   this.requiredFields.forEach((field) => {
	//     if (this.entry[field] === undefined || this.entry[field] === '') {
	//       valid = false;
	//     }
	//   });
	//   return valid;
	// }

	public getLabels(field: any) {
		switch (field.DATA_TYPE.DISPLAY) {
			case 'Relationship':
				if (
					field.RELATIONSHIP_TYPE === 'Many to One' ||
					field.RELATIONSHIP_TYPE === 'One to One'
				) {
					return `<div><span class="mat-body-strong">${field.DISPLAY_LABEL} :  </span><span class="mat-body">
				 {{
					context.getRelationshipData(context.entry['${field.NAME}'], '${field.NAME}')[
						context.moduleFields['${field.NAME}'].PRIMARY_DISPLAY_FIELD_NAME
					]
				}}
				</span></div>`;
				} else if (field.RELATIONSHIP_TYPE === 'Many to Many') {
					return `<div><span class="mat-body-strong">${field.DISPLAY_LABEL} :
					 </span><span class="mat-body" *ngFor="let entry of context.entry['${field.NAME}']">
				 {{context.manyToManyMap.get(entry)[context.moduleFields["${field.NAME}"].PRIMARY_DISPLAY_FIELD_NAME]}}
				</span></div>`;
				}
			case 'Date/Time':
				return `<div><span class="mat-body-strong">${field.DISPLAY_LABEL} : </span><span class="mat-body">
					{{context.entry.${field.NAME} | dateFormat:'medium'}}
					</span></div>`;
			default:
				return `<div><span class="mat-body-strong">${
					field.DISPLAY_LABEL
				} :  </span><span class="mat-body"> ${
					this.entry[field.NAME] === undefined ? '--' : this.entry[field.NAME]
				}</span></div>`;
		}
	}

	public getRelationshipData(value, fieldName) {
		if (value === undefined) {
			return {};
		}
		if (this.relationFieldEntries[fieldName] !== undefined) {
			const user = this.relationFieldEntries[fieldName].find(
				(entry) => entry.DATA_ID === value
			);
			if (user === undefined) {
				return {};
			} else {
				return user;
			}
		} else {
			return {};
		}
	}

	// This function updates the entry or enables saving when disabled
	public toggleDisable() {
		this.companiesService.trackEvent(`Saved a ${this.module.NAME}`, {
			MODULE_ID: this.moduleId,
			LAYOUT_ID: this.entryId || 'new',
		});
		this.module.FIELDS.forEach((moduleField, fieldIndex) => {
			if (
				moduleField.DATA_TYPE.DISPLAY === 'Phone' &&
				this.entry[moduleField.NAME] !== undefined
			) {
				if (this.entry[moduleField.NAME].hasOwnProperty('PHONE_NUMBER')) {
					if (
						!this.entry[moduleField.NAME].PHONE_NUMBER ||
						this.entry[moduleField.NAME].PHONE_NUMBER.length === 0
					) {
						this.entry[moduleField.NAME]['COUNTRY_CODE'] =
							this.entry[moduleField.NAME]['COUNTRY_CODE'];
						this.entry[moduleField.NAME]['DIAL_CODE'] =
							this.entry[moduleField.NAME]['DIAL_CODE'];
						this.entry[moduleField.NAME]['COUNTRY_FLAG'] =
							this.entry[moduleField.NAME]['COUNTRY_FLAG'];
					} else {
						this.entry[moduleField.NAME]['PHONE_NUMBER'] =
							this.entry[moduleField.NAME]['PHONE_NUMBER'].toString();
					}
				}
			}
			// Incoming value in chronometer during put/post call
			if (
				moduleField.DATA_TYPE.DISPLAY === 'Chronometer'
				// &&
				// this.entry[moduleField.NAME] !== undefined
			) {
				if (
					this.chronometerValues[moduleField.NAME] === undefined ||
					this.chronometerValues[moduleField.NAME].length === 0
				) {
					this.entry[moduleField.NAME] = '0m';
				} else {
					this.entry[moduleField.NAME] =
						this.chronometerValues[moduleField.NAME];
				}
			}
			// if (moduleField.DATA_TYPE.DISPLAY === 'Date/Time') {
			//   if (this.entry.hasOwnProperty(moduleField.NAME) && !moduleField.NOT_EDITABLE && this.entry[moduleField.NAME] != null) {
			//     this.entry[moduleField.NAME] = this.entry[moduleField.NAME].toISOString();
			//   }
			// }

			// Convertion of Object -> Ids
			// if (
			//   moduleField.DATA_TYPE.DISPLAY === 'Relationship' &&
			//   this.entry[moduleField.NAME] !== undefined &&
			//   (this.customLayout || this.gridLayout)
			// ) {
			//   if (moduleField.RELATIONSHIP_TYPE === 'Many to Many') {
			//     this.entry[moduleField.NAME] = this.transformIds(
			//       this.entry[moduleField.NAME],
			//       'DATA_ID'
			//     );
			//   }
			// }

			// if (
			//   moduleField.DATA_TYPE.DISPLAY === 'Auto Number' &&
			//   this.customLayout
			// ) {
			//   this.entry[moduleField.NAME] = null;
			// }
		});
		if (this.entryId === 'new') {
			if (
				this.discussionFieldName !== undefined &&
				this.entry.hasOwnProperty(this.discussionFieldName)
			) {
				if (this.entry[this.discussionFieldName].length > 1) {
					this.entry[this.discussionFieldName].pop();
				}
				this.entry[this.discussionFieldName].push({
					MESSAGE: this.message,
					ATTACHMENTS: JSON.parse(JSON.stringify(this.attachments)),
					SENDER: {
						FIRST_NAME: this.userService.user.FIRST_NAME,
						LAST_NAME: this.userService.user.LAST_NAME,
						USER_UUID: this.userService.user.USER_UUID,
						ROLE: this.userService.user.ROLE,
					},
					MESSAGE_TYPE: this.messageType,
				});
			}
			this.modulesService.postEntry(this.moduleId, this.entry).subscribe(
				(response: any) => {
					const keysList = Array.from(this.oneToManyCreateData.keys());
					if (keysList.length === 0) {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SAVED_SUCCESSFULLY'),
						});
						this.router.navigate([
							`render/${this.route.snapshot.params.moduleId}`,
						]);
					} else {
						keysList.forEach((key) => {
							this.oneToManyCreateData.get(key)['MANY_SIDE'].VALUE =
								response.DATA_ID;
							this.modulesService
								.putEntryOneToMany(
									this.moduleId,
									response.DATA_ID,
									this.oneToManyCreateData.get(key)
								)
								.subscribe((ressponse) => {
									this.bannerMessageService.successNotifications.push({
										message:
											this.translateService.instant('SAVED_SUCCESSFULLY'),
									});
									// .customTableService.dataIds.clear();
									this.router.navigate([
										`render/${this.route.snapshot.params.moduleId}`,
									]);
								});
						});
					}
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
					// If error revert Ids -> Objects
					// this.module.FIELDS.forEach((moduleField, fieldIndex) => {
					//   if (
					//     moduleField.DATA_TYPE.DISPLAY === 'Relationship' &&
					//     this.entry[moduleField.NAME] !== undefined &&
					//     this.customLayout
					//   ) {
					//     if (moduleField.RELATIONSHIP_TYPE === 'Many to Many') {
					//       this.entry[moduleField.NAME] = this.transformObjects(
					//         this.entry[moduleField.NAME],
					//         this.relationFieldEntries[moduleField.NAME],
					//         'DATA_ID'
					//       );
					//     }
					//   }
					// });
				}
			);
		} else {
			this.updateSubscription.unsubscribe();
			const entryToPut = JSON.parse(JSON.stringify(this.entry));

			// ADD ONLY SINGLE MESSAGE TO THE DISCUSSION PAYLOAD
			if (this.message !== '' && this.discussionFieldName !== undefined) {
				const msgBody = this.message;

				const msgObject = {
					MESSAGE: msgBody,
					ATTACHMENTS: JSON.parse(JSON.stringify(this.attachments)),
					SENDER: {
						FIRST_NAME: this.userService.user.FIRST_NAME,
						LAST_NAME: this.userService.user.LAST_NAME,
						USER_UUID: this.userService.user.USER_UUID,
						ROLE: this.userService.user.ROLE,
					},
					MESSAGE_TYPE: this.messageType,
				};
				entryToPut[this.discussionFieldName] = [msgObject];
			} else if (this.discussionFieldName !== undefined) {
				entryToPut[this.discussionFieldName] = [];
			}
			this.modulesService
				.putEntry(this.moduleId, this.entryId, entryToPut)
				.subscribe(
					(response: any) => {
						this.message = '';
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
						});
						if (this.module.NAME === 'Users') {
							const customer = this.roles.find(
								(role) => role.NAME === 'Customers'
							);
							if (this.userService.user.ROLE !== customer.ROLE_ID) {
								this.router.navigate([`render/${this.moduleId}`]);
								// this.customTableService.dataIds.clear();
							}
						} else {
							this.router.navigate([`render/${this.moduleId}`]);
							// this.customTableService.dataIds.clear();
						}
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		}
	}

	public changePasswordDialog() {
		this.loaderService.isLoading2 = false;
		this.dialog.open(ChangePasswordDialogComponent, {
			width: '500px',
			disableClose: true,
		});
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

	// Remove tab for teams layout
	public removeTab(userId, fieldName) {
		const index = this.entry[fieldName].indexOf(userId);
		if (index !== -1) {
			this.entry[fieldName].splice(index, 1);
		}
		if (
			this.entry[fieldName] === undefined ||
			this.entry[fieldName].length <= 0
		) {
			this.emptyListMessage = 'EMPTY_USERS_LIST';
		}
	}

	// Adding the data to relationship
	public addDataForRelationshipField(field, event, formControlFieldName) {
		this.emptyListMessage = '';
		if (field.RELATIONSHIP_TYPE !== 'Many to Many') {
			this.entry[field.NAME] = event.option.value.DATA_ID;
			this.formControls[formControlFieldName].setValue(
				event.option.value[field.PRIMARY_DISPLAY_FIELD_NAME]
			);
			this.evaluateConditions('', field.FIELD_ID);
		} else {
			if (this.entry[field.NAME] === undefined) {
				this.manyToManyMap.set(event.option.value.DATA_ID, event.option.value);
				this.entry[field.NAME] = [];
				this.entry[field.NAME].push(event.option.value.DATA_ID);
				if (this.module.NAME === 'Teams') {
					this.formControls[formControlFieldName].setValue('');
				}
			} else {
				this.manyToManyMap.set(event.option.value.DATA_ID, event.option.value);
				this.entry[field.NAME].push(event.option.value.DATA_ID);
				if (this.module.NAME === 'Teams') {
					this.formControls[formControlFieldName].setValue('');
				} else {
					// this.formControls[formControlFieldName].setValue(this.entry[field.NAME]);
				}
			}
			this.formControls[formControlFieldName].setValue('');
		}
		if (field.RELATIONSHIP_TYPE === 'Many to One') {
			this.inheritValues(field.FIELD_ID, event.option.value);
		}
	}
	public remove(element, arrayName): void {
		const index = this.entry[arrayName].indexOf(element);
		if (index >= 0) {
			const array = this.entry[arrayName];
			array.splice(index, 1);
		}
	}

	public downloadAttachment(uuid, messageId) {
		return `${this.globals.baseRestUrl}/attachments?attachment_uuid=${uuid}&message_id=${messageId}&entry_id=${this.entryId}&module_id=${this.moduleId}`;
	}

	public getFieldByName(fieldName) {
		return this.module.FIELDS.find((field) => field.NAME === fieldName);
	}

	public selected(
		event: MatAutocompleteSelectedEvent,
		inputType,
		control: FormControl
	): void {
		if (inputType === 'teamsInput') {
			this.entry['TEAMS'].push(event.option.value);
		}
		control.setValue('');
	}

	// If input doesn't match in dropdown, reseting the input field
	public resetInput(event: MatChipInputEvent): void {
		// if (!this.matAutocomplete.isOpen) {
		const input = event.input;
		// Reset the input value
		if (input) {
			input.value = '';
		}
		// }
	}

	public clearInput(event: any, fieldName) {
		if (event.value.length <= 0) {
			delete this.entry[fieldName];
		}
	}

	// adding list item of email
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

	// removing cc'd items
	public removeItem(object: string, fieldName): void {
		const index = this.entry[fieldName].indexOf(object);
		this.entry[fieldName].splice(index, 1);
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

		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message: dialogMessage,
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DELETE')) {
				const body = { IDS: [this.entryId] };

				this.modulesService.deleteEntries(this.moduleId, body).subscribe(
					(entriesResponse: any) => {
						this.router.navigate([`render/${this.moduleId}`]);
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

	public updatePhoneInfo(country, fieldName) {
		this.entry[fieldName]['COUNTRY_CODE'] = country.COUNTRY_CODE;
		this.entry[fieldName]['DIAL_CODE'] = country.COUNTRY_DIAL_CODE;
		this.entry[fieldName]['COUNTRY_FLAG'] = country.COUNTRY_FLAG;
	}

	private subscribeChat() {
		// const chatSubscription$ = this._stompService._stompManagerService
		// 	.watch(`topic/chat/${this.entry.SESSION_UUID}`)
		// 	.pipe(shareReplay());
		// this.chatSub = chatSubscription$.subscribe((message: any) => {
		// 	if (!this.entry.hasOwnProperty('CHAT')) {
		// 		this.entry.CHAT = [];
		// 		this.checkAssignee();
		// 	}
		// 	if (JSON.parse(message.body).MESSAGE_TYPE === 'META_DATA') {
		// 		this.modulesService.getEntry(this.moduleId, this.entryId).subscribe(
		// 			(entryResponse: any) => {
		// 				this.entry.STATUS = entryResponse.STATUS;
		// 			},
		// 			(error: any) => {
		// 				this.bannerMessageService.errorNotifications.push(
		// 					error.error.ERROR
		// 				);
		// 			}
		// 		);
		// 	}
		// 	this.entry.CHAT.push(JSON.parse(message.body));
		// });
	}

	public subscribeUpdates() {
		// SUBSCRIBE TO CHANGES IN THE ENTRY AND RELOAD THE ENTRY
		// const updateSubscription$ = this._stompService._stompRestService
		// 	.watch(`rest/dataupdated/${this.entry.DATA_ID}`)
		// 	.pipe(shareReplay());
		// this.updateSubscription = updateSubscription$.subscribe(
		// 	(message: any) => {
		// 		this.saveDisabled = true;
		// 		// let data =this.renderDetailHelper.snackbarHelper();
		// 		// if (data.dismissedByAction) {
		// 		// 	this.layoutExists = false;
		// 		// 	this.loading = true;
		// 		// 	this.ngOnDestroy();
		// 		// 	this.initialiseComponent();
		// 		// }
		//
		// 		this._snackBar
		// 			.open(this.translateService.instant('DATA_UPDATED'), 'OK', {
		// 				horizontalPosition: 'center',
		// 			})
		// 			.afterDismissed()
		// 			.subscribe((data) => {
		// 				if (data.dismissedByAction) {
		// 					this.layoutExists = false;
		// 					this.loading = true;
		// 					this.ngOnDestroy();
		// 					this.initialiseComponent();
		// 				}
		// 			});
		// 	},
		// 	(error) => {
		// 		this.bannerMessageService.errorNotifications.push(error.error.ERROR);
		// 	}
		// );
	}

	public subscribeDiscussion() {
		// SUBSCRIBE TO THE DISCUSSION
		// const discussionSubscription$ = this._stompService._stompManagerService
		// 	.watch(`topic/update/${this.entry.DATA_ID}`)
		// 	.pipe(shareReplay());
		// this.discussionSubscribe = discussionSubscription$.subscribe(
		// 	(message: any) => {
		// 		this.entry[this.discussionFieldName].push(JSON.parse(message.body));
		// 	},
		// 	(error) => {
		// 		this.bannerMessageService.errorNotifications.push(error.error.ERROR);
		// 	}
		// );
	}
	public publishDiscussion() {
		this.companiesService.trackEvent(`Send new discussion`, {
			MODULE_ID: this.moduleId,
			LAYOUT_ID: this.entryId,
		});
		let hasAttachment = false;
		if (this.message === '') {
			return;
		}
		var imageFromClipboard = false;
		if (this.message.includes('base64')) {
			imageFromClipboard = true;
			var imageTag = this.message.match(/<\s*img[^>]*(.*?)\s*\s*>/g);
			var imageType = [];
			imageTag.forEach((image) => {
				if (image.includes('base64')) {
					image.match(/(image.*?)(.*)(?=;)/g).forEach((tag) => {
						imageType.push(tag);
					});
				}
			});
			var imageData = [];
			imageTag.forEach((image) => {
				if (image.includes('base64')) {
					image.match(/(,.*?)\"(.*?)/g).forEach((tag) => {
						imageData.push(tag.slice(1, -1));
					});
				}
			});

			var fileExtName = [];
			var fileName = [];
			imageType.forEach((image) => {
				fileExtName.push(image.replace('image/', ''));
				fileName.push(this.globals.guid());
			});
			for (let i = 0; i < imageTag.length; i++) {
				this.message = this.message.replace(
					/src="data(.*?)base64/,
					'src="' + fileName[i] + '"' + ' alt'
				);
			}

			for (let i = 0; i < imageType.length; i++) {
				this.attachments.push({
					FILE_NAME: fileName[i] + '.' + fileExtName[i],
					FILE: imageData[i],
					FILE_EXTENSION: imageType[i],
				});
			}
		}
		if (this.attachments.length > 0) {
			hasAttachment = true;
			this.postAttachmentAndPublish(fileName, imageTag, imageFromClipboard);
		}
		if (!hasAttachment) {
			const msgBody = this.message;
			const msgObject = {
				MESSAGE: msgBody,
				ATTACHMENTS: JSON.parse(JSON.stringify(this.attachments)),
				COMPANY_SUBDOMAIN: this.userService.getSubdomain(),
				SENDER: {
					FIRST_NAME: this.userService.user.FIRST_NAME,
					LAST_NAME: this.userService.user.LAST_NAME,
					USER_UUID: this.userService.user.USER_UUID,
					ROLE: this.userService.user.ROLE,
				},
				MODULE: this.module.MODULE_ID,
				ENTRY_ID: this.entry.DATA_ID,
				MESSAGE_TYPE: this.messageType,
				TRIGGER_WORKFLOW: true,
			};

			if (this.message.trim().length > 0) {
				this.message = '';
				this.attachments = [];
				this.attachments.length = 0;
				// this._stompService._stompManagerService.publish({
				// 	destination: `ngdesk/discussion`,
				// 	body: JSON.stringify(msgObject),
				// });
			}
		}
	}

	public postAttachmentAndPublish(fileName, imageTag, imageFromclipboard) {
		// SHOW LOADING ATTACHMENT AND POST THE ATTACHMENT AND DO A STOMP PUBLISH
		this.attachmentLoading = true;
		this.attachmentsService
			.postAttachments({
				ATTACHMENTS: this.attachments,
			})
			.subscribe(
				(val: any) => {
					this.attachmentLoading = false;
					this.attachments = val.ATTACHMENTS;
					var attachmentUuid = [];
					this.attachments.forEach((element) => {
						attachmentUuid.push(element.ATTACHMENT_UUID);
					});
					const messageId = this.globals.guid();
					if (imageFromclipboard) {
						let i;
						let j = 0;
						for (i = 0; i < imageTag.length; i++) {
							for (j = 0; j < attachmentUuid.length; j++) {
								if (imageTag[i].includes('base64')) {
									var url =
										this.globals.baseRestUrl +
										'/attachments?attachment_uuid=' +
										attachmentUuid[j] +
										'&message_id=' +
										messageId +
										'&entry_id=' +
										this.entryId +
										'&module_id=' +
										this.moduleId;

									if (val.ATTACHMENTS[j].FILE_NAME.includes(fileName[i])) {
										this.message = this.message.replace(
											'src="' + fileName[i] + '" alt',
											'src="' + url + '"' + ' alt'
										);
									}
								}
							}
						}
					}
					const msgBody = this.message;
					const msgObject = {
						MESSAGE_ID: messageId,
						MESSAGE: msgBody,
						ATTACHMENTS: JSON.parse(JSON.stringify(this.attachments)),
						COMPANY_SUBDOMAIN: this.userService.getSubdomain(),
						SENDER: {
							FIRST_NAME: this.userService.user.FIRST_NAME,
							LAST_NAME: this.userService.user.LAST_NAME,
							USER_UUID: this.userService.user.USER_UUID,
							ROLE: this.userService.user.ROLE,
						},
						MODULE: this.module.MODULE_ID,
						ENTRY_ID: this.entry.DATA_ID,
						MESSAGE_TYPE: this.messageType,
						TRIGGER_WORKFLOW: true,
					};

					if (this.message.trim().length > 0) {
						this.message = '';
						this.attachments = [];
						this.attachments.length = 0;
						// this._stompService._stompManagerService.publish({
						// 	destination: `ngdesk/discussion`,
						// 	body: JSON.stringify(msgObject),
						// });
					}
				},
				(error: any) => {
					this.attachmentLoading = false;
					this.bannerMessageService.errorNotifications.push(error.error.ERROR);
				}
			);
	}

	public publishChat() {
		const msgObj = {
			MESSAGE: this.message,
			ATTACHMENTS: JSON.parse(JSON.stringify(this.attachments)),
			SESSION_UUID: this.entry.SESSION_UUID,
			WIDGET_ID: this.entry.CHANNEL,
			COMPANY_SUBDOMAIN: this.userService.getSubdomain(),
			SENDER: {
				FIRST_NAME: this.userService.user.FIRST_NAME,
				LAST_NAME: this.userService.user.LAST_NAME,
				UUID: this.userService.user.USER_UUID,
				ROLE: this.userService.user.ROLE,
			},
			EMAIL_ADDRESS: this.userService.user.EMAIL_ADDRESS,
		};
		if (this.message.trim().length > 0 || this.attachments.length > 0) {
			this.message = '';
			// tinymce.activeEditor.setContent('');
			this.attachments.length = 0;
			// this._stompService._stompManagerService.publish({
			// 	destination: `ngdesk/discussion`,
			// 	body: JSON.stringify(msgObj),
			// });
		}
	}

	public postAttachment(event) {
		// adds attachment
		const reader = new FileReader();
		if (
			event.target.files &&
			event.target.files.length &&
			this.attachments.length <= 5
		) {
			this.attachmentLoading = true;
			const [file] = event.target.files;
			reader.readAsDataURL(file);

			// (file.size <= 1024000) ? this.fileSizeError = false : this.fileSizeError = true;
			reader.onload = () => {
				const data: any = reader.result;
				const attachment = {
					FILE_NAME: file.name,
					FILE: data.split('base64,')[1],
				};
				const attachmentsObj = { ATTACHMENTS: [attachment] };

				// post attachments
				this.attachmentsService
					.postAttachments(attachmentsObj)
					.pipe(
						delay(2000),
						finalize(() => {
							this.attachmentLoading = false;
						})
					)
					.subscribe(
						(val: any) => {
							// add attachment
							this.attachments.push(val.ATTACHMENTS[0]);
						},
						(error: any) => {
							this.attachmentLoading = false;
							this.bannerMessageService.errorNotifications.push(
								error.error.ERROR
							);
						}
					);
			};
			// need to run CD since file load runs outside of zone
			this.cd.markForCheck();
		}
	}

	//  getting all the entries(chats) for chats module
	private getAllEntries() {
		this.modulesService.getEntries(this.moduleId).subscribe(
			(response: any) => {
				this.allEntries = response.DATA;
				if (this.module.NAME === 'Chats') {
					this.allEntries = this.allEntries.filter(
						(entry) =>
							entry.hasOwnProperty('REQUESTOR') &&
							entry.STATUS === 'Chatting' &&
							entry.AGENTS &&
							entry.AGENTS.includes(this.userService.user.DATA_ID)
					);
				}
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push(error.error.ERROR);
			}
		);
	}

	private updateRequestor(moduleId) {
		// Get relationship entries
		this.modulesService.getEntries(moduleId).subscribe(
			(entriesResponse: any) => {
				this.relationFieldEntries['REQUESTOR'] = entriesResponse.DATA;
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push(error.error.ERROR);
			}
		);
	}

	public endChat(entry) {
		const chatObj = {
			SESSION_UUID: entry.SESSION_UUID,
			WIDGET_ID: entry.CHANNEL,
			COMPANY_SUBDOMAIN: this.userService.getSubdomain(),
			USER_UUID: this.userService.user.USER_UUID,
		};
		// this._stompService._stompManagerService.publish({
		// 	destination: `ngdesk/chat/end`,
		// 	body: JSON.stringify(chatObj),
		// });
	}

	private notificationSubscription() {
		// const notifySubscription$ = this._stompService._stompManagerService
		// 	.watch(`/topic/notify/${this.userService.user.DATA_ID}`)
		// 	.pipe(shareReplay());
		// this.notificationSub = notifySubscription$.subscribe(
		// 	(notification: any) => {
		// 		notification = JSON.parse(notification.body);
		// 		if (this.entry.DATA_ID !== notification.DATA_ID) {
		// 			this.getAllEntries();
		// 		}
		// 		// if new user has created update users array for getting new requestor
		// 		if (
		// 			!this.allEntries.find(
		// 				(entry) => entry.DATA_ID === notification.DATA_ID
		// 			)
		// 		) {
		// 			this.updateRequestor(
		// 				this.module.FIELDS.find((field) => field.NAME === 'REQUESTOR')
		// 					.MODULE
		// 			);
		// 		}
		// 		if (this.unreadNotifications.hasOwnProperty(notification.DATA_ID)) {
		// 			this.unreadNotifications[notification.DATA_ID].push(notification);
		// 		} else {
		// 			this.unreadNotifications[notification.DATA_ID] = [notification];
		// 		}
		// 		if (notification.DATA_ID === this.entry.DATA_ID) {
		// 			this.readNotification();
		// 		}
		// 	}
		// );
	}

	private getUnreadNotifications() {
		this.notificationsService.getNotifications().subscribe(
			(response: any) => {
				response.NOTIFICATIONS.forEach((element) => {
					if (this.unreadNotifications.hasOwnProperty(element.DATA_ID)) {
						this.unreadNotifications[element.DATA_ID].push(element);
					} else {
						this.unreadNotifications[element.DATA_ID] = [element];
					}
				});

				// read notifications of opened chat
				if (this.unreadNotifications.hasOwnProperty(this.entry.DATA_ID)) {
					this.readNotification();
				}

				// updating tollbar notifications
				this.notificationsService.unreadNotifications =
					response.NOTIFICATIONS.filter(
						(notification) => notification.DATA_ID !== this.entry.DATA_ID
					);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push(error.error.ERROR);
			}
		);
	}

	private readNotification() {
		this.unreadNotifications[this.entry.DATA_ID].forEach((notification) => {
			const notificationObj = {
				USER_ID: this.userService.user.DATA_ID,
				COMPANY_UUID: this.userService.companyUuid,
				NOTIFICATION_UUID: notification.NOTIFICATION_UUID,
			};
			// this._stompService._stompManagerService.publish({
			// 	destination: `ngdesk/notification/update`,
			// 	body: JSON.stringify(notificationObj),
			// });
		});
		delete this.unreadNotifications[this.entry.DATA_ID];
	}

	public cleanHtml(message): string {
		return message.replace(/<[^>]+>/g, '');
	}

	public navigateTo(moduleId, entryId) {
		if (this.updateSubscription && this.updateSubscription != null) {
			this.updateSubscription.unsubscribe();
		}
		if (this.module.NAME === 'Chats') {
			this.chatSub.unsubscribe();
			this.notificationSub.unsubscribe();
		} else {
			if (this.route.snapshot.params['dataId'] !== 'new') {
				this.discussionSubscribe.unsubscribe();
			}
		}
		this.router.navigate([`render/${moduleId}/edit/${entryId}`]);
	}

	public ngOnDestroy() {
		// avoid memory leaks here by cleaning up after ourselves. If we
		// don't then we will continue to run our initialiseInvites()
		// method on every navigationEnd event.
		if (this.navigationSubscription) {
			this.navigationSubscription.unsubscribe();
		}
		if (this.updateSubscription && this.updateSubscription != null) {
			this.updateSubscription.unsubscribe();
		}
		if (this.module.NAME === 'Chats') {
			if (this.chatSub !== undefined) {
				this.chatSub.unsubscribe();
			}
			if (this.notificationSub !== undefined) {
				this.notificationSub.unsubscribe();
			}
		} else {
			if (this.entryId !== 'new') {
				if (this.discussionSubscribe && this.discussionSubscribe != null) {
					this.discussionSubscribe.unsubscribe();
				}
			}
		}

		this.renderDetailHelper.configSetup();

		// this.config['setup'] = (ed) => {
		// 	ed.on('SetContent', function (ed1) {
		// 		ed1.target.editorCommands.execCommand('fontName', false, 'Arial');
		// 	});
		// };

		this._destroyed$.next();
		this._destroyed$.complete();
	}

	public onClickRelationshipField(entryId, moduleId) {
		this.router.navigate([`render/${moduleId}/detail/${entryId}`]);
	}

	public toggleNextButton() {
		if (this.listData && this.listData.length > 1) {
			this.showNextButton = true;
		}
	}

	public next(navigate) {
		const index = this.listData.findIndex(
			(data) => data.DATA_ID === this.entry.DATA_ID
		);
		if (navigate && this.listData.length - 1 > index) {
			this.router.navigate([
				`render/${this.moduleId}/edit/${this.listData[index + 1].DATA_ID}`,
			]);
		} else {
			this.router.navigate([`render/${this.moduleId}`]);
		}
	}

	public onSelectDiscussionTabs($event) {
		const tabValue = $event.tab.textLabel;
		if (tabValue === 'All') {
			this.showType = 'ALL';
			this.discussionType = 'Messages';
		} else {
			this.showType = 'MESSAGES';
		}
	}

	public addCreatelayout() {
		this.router.navigate([`modules/${this.moduleId}/create_layouts/new`]);
	}

	public addEditlayout() {
		this.router.navigate([`modules/${this.moduleId}/edit_layouts/new`]);
	}

	public addDetaillayout() {
		this.router.navigate([`modules/${this.moduleId}/detail_layouts/new`]);
	}

	public routeToEditLayout() {
		this.clicked = true;
		this.router.navigate([`render/${this.moduleId}/edit/${this.entryId}`]);
	}

	public routeToDetailLayout() {
		this.router.navigate([`render/${this.moduleId}/detail/${this.entryId}`]);
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
			if (type === 'CREATE_LAYOUTS') {
				fields.forEach((field) => {
					this.loadDefaultValueForField(this.fieldsMap[field.FIELD_ID]);
				});
				return fields[0].FIELDS;
			} else {
				return fields[0].FIELDS;
			}
		}
	}

	public getPillsLayout(field) {
		switch (field.DATA_TYPE.DISPLAY) {
			case 'Relationship':
				if (this.currentRole.NAME !== 'Customers') {
					return `<div fxLayout="row" fxLayoutAlign="center center"
					[ngStyle]="{'background': '#E9EBED', 'padding': '5px', 'min-width':'150px'}" >
					<label class="pointer mat-body"
					 (click)="context.onClickRelationshipField(context.entry.${field.NAME}, context.moduleFields['${field.NAME}'].MODULE)">
					  {{context.getRelationshipData(context.entry.${field.NAME}, '${field.NAME}')[context.moduleFields['${field.NAME}']
					  .PRIMARY_DISPLAY_FIELD_NAME]}}
					</label>
				  </div>`;
				} else {
					return `<div fxLayout="row" fxLayoutAlign="center center"
					[ngStyle]="{'background': '#E9EBED', 'padding': '5px', 'min-width':'150px'}" >
					<label class="mat-body">
					  {{context.getRelationshipData(context.entry.${field.NAME}, '${field.NAME}')[context.moduleFields['${field.NAME}']
					  .PRIMARY_DISPLAY_FIELD_NAME]}}
					</label>
				  </div>`;
				}
			default:
				return `<div fxLayout="row" fxLayoutAlign="center center"
					  [ngStyle]="{'background': '#E9EBED', 'padding': '5px','min-width':'150px'}">
						<label class="mat-body">{{context.entry.${field.NAME}}}</label>
					</div>`;
		}
	}

	public zipValidator(event): boolean {
		const charCode = event.which ? event.which : event.keyCode;
		if (charCode > 31 && (charCode < 48 || charCode > 57) && charCode !== 45) {
			return false;
		}
		return true;
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

	public setupOneToManyTable(moduleId, fieldId) {
		//	this.currentTable = fieldId;
		this.customTableService.paginator = this.paginator;
		this.customTableService.sort = this.sort;
		this.tableModuleId = moduleId;
		this.tableFieldId = fieldId;
		this.customTableService.isLoading = true;
		const roleId = this.userService.user.ROLE;
		this.modulesService.getModuleById(this.tableModuleId).subscribe(
			(moduleResponse: any) => {
				this.customTableService.totalRecords = undefined;
				this.currentListLayout = new ListLayout(
					'',
					'',
					'',
					'',
					new OrderBy('', ''),
					new ColumnShow([]),
					[new Condition('', '', '', '')],
					false
				);

				this.pageTitle = '';
				this.moduleName = moduleResponse.SINGULAR_NAME;
				this.tableFields = moduleResponse.FIELDS;
				this.numberOfListLayout = moduleResponse.LIST_LAYOUTS.length;
				if (this.numberOfListLayout === 0) {
					this.customTableService.isLoading = false;
				}
				// set custom empty table message for when there are 0 records on the table
				this.recordName = moduleResponse.PLURAL_NAME.toLowerCase();

				const listLayouts = moduleResponse.LIST_LAYOUTS;
				const tempfilteredLayouts: ListLayout[] = [];
				let layoutInLocalStorage = false;

				// get the sidebar state from localStorage
				if (
					localStorage.getItem(`${moduleId}_LIST_LAYOUT`) &&
					JSON.parse(localStorage.getItem(`${moduleId}_LIST_LAYOUT`))
						.SHOW_LIST_LAYOUTS
				) {
					this.showListLayouts = JSON.parse(
						localStorage.getItem(`${moduleId}_LIST_LAYOUT`)
					).SHOW_LIST_LAYOUTS;
				} else {
					this.showListLayouts = false;
				}

				// get the current listLayout base on roleId  and if the listLayout is default
				for (const listLayout of listLayouts) {
					if (listLayout.ROLE === roleId) {
						this.customTableService.isLoading = true;
						tempfilteredLayouts.push(listLayout);
						// Display default list layout unless there is one saved in localStorage
						if (
							(listLayout.IS_DEFAULT &&
								!localStorage.getItem(`${moduleId}_LIST_LAYOUT`)) ||
							(localStorage.getItem(`${moduleId}_LIST_LAYOUT`) !== null &&
								JSON.parse(localStorage.getItem(`${moduleId}_LIST_LAYOUT`))[
									'LAYOUT_ID'
								] === listLayout.LAYOUT_ID)
						) {
							layoutInLocalStorage = true;
							this.setListLayout(listLayout);
						}
					}
				}
				// if not in local storage load the default layout
				if (!layoutInLocalStorage) {
					const defaultListLayout = listLayouts.find(
						(listLayout) => listLayout.ROLE === roleId && listLayout.IS_DEFAULT
					);
					if (listLayouts.length > 1 && defaultListLayout) {
						this.setListLayout(defaultListLayout);
					} else if (this.isModuleAllowed && !defaultListLayout) {
						this.customTableService.isLoading = false;
						this.numberOfListLayout = 0;
						this.hideMatTable = true;
						this.loading = false;
						this.showMessage = true;
					}
				}
				// layouts to show in the layouts side table
				this.filteredLayouts = tempfilteredLayouts;
				// show/hide button depending if the user has access to more than one layouts
				if (this.filteredLayouts.length <= 1) {
					this.layoutButtonShow = false;
				}
			},
			(error: any) => {
				// If module is allowed, and there is some error, then only display that error.
				// If it is not allowed, then no point of showing any error message
				if (this.isModuleAllowed) {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			}
		);
	}

	public setListLayout(layout) {
		// this.currentListLayout.layoutId = layout.LAYOUT_ID;
		if (layout) {
			this.pageTitle = layout.NAME;
		}
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: {
			DISPLAY: string;
			NAME: string;
			DATA_TYPE?: string;
			FIELD_ID?: string;
		}[] = [];
		// columnsHeaders.push('Add Entry');
		this.currentListLayout = new ListLayout(
			layout.NAME,
			layout.DESCRIPTION,
			layout.ID,
			layout.ROLE,
			new OrderBy(layout.ORDER_BY.COLUMN, layout.ORDER_BY.ORDER),
			new ColumnShow(layout.COLUMN_SHOW.FIELDS),
			[new Condition('', '', '', '')],
			layout.IS_DEFAULT,
			layout.LAYOUT_ID
		);
		this.currentListLayout.columnShow.fields.forEach((layoutField) => {
			const field = this.tableFields.find(
				(fieldIn) => fieldIn.FIELD_ID === layoutField
			);
			// column headers
			columnsHeadersObj.push({
				DISPLAY: field.DISPLAY_LABEL,
				NAME: field.NAME,
				DATA_TYPE: field.DATA_TYPE.DISPLAY,
				FIELD_ID: field.FIELD_ID,
			});
			columnsHeaders.push(field.DISPLAY_LABEL);

			// set the initial sort by and order by pulled from the current list layout
			if (field.FIELD_ID === this.currentListLayout.orderBy.column) {
				this.customTableService.activeSort = {
					ORDER_BY: this.currentListLayout.orderBy.order.toLowerCase(),
					SORT_BY: field.DISPLAY_LABEL,
					NAME: field.NAME,
				};
			}
		});
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		// if (this.listLayoutActions.actions.length > 0) {
		// 	columnsHeadersObj.push({
		// 		DISPLAY: this.translateService.instant('ACTION'),
		// 		NAME: 'ACTION'
		// 	});
		// 	columnsHeaders.push(this.translateService.instant('ACTION'));
		// }

		let savedLayout = null;
		if (
			localStorage.getItem(`${this.tableModuleId}_LIST_LAYOUT`) !== null &&
			JSON.parse(localStorage.getItem(`${this.tableModuleId}_LIST_LAYOUT`))[
				'LAYOUT_ID'
			] === layout.LAYOUT_ID
		) {
			savedLayout = JSON.parse(
				localStorage.getItem(`${this.tableModuleId}_LIST_LAYOUT`)
			);

			this.customTableService.activeSort = {
				ORDER_BY: savedLayout['ORDER_BY'].toLowerCase(),
				SORT_BY: this.translateService.instant(savedLayout['SORT_BY']),
				NAME: savedLayout['SORT_BY'],
			};
			this.customTableService.pageIndex = savedLayout['PAGE'];
			this.customTableService.pageSize = savedLayout['PAGE_SIZE'];
		}

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.sortBy = this.customTableService.activeSort.NAME;
		this.customTableService.sortOrder =
			this.customTableService.activeSort.ORDER_BY;
		this.customTableService.isLoading = true;

		// passes search to get entries call if its defined in local storage
		this.entriesCount = 0;

		let fieldName = '';
		this.modulesService
			.getModuleById(this.tableModuleId)
			.subscribe((module: any) => {
				module.FIELDS.forEach((field) => {
					if (field.RELATIONSHIP_FIELD === this.tableFieldId) {
						fieldName = field.NAME;
					}
				});
				this.entriesCount = 0;
				this.modulesService
					.getEntries(this.tableModuleId)
					.subscribe((entriesResponse: any) => {
						this.totalRecords = entriesResponse.TOTAL_RECORDS;
						entriesResponse.DATA.forEach((data) => {
							if (data.hasOwnProperty(fieldName)) {
								if (data[fieldName] === this.entryId) {
									this.entriesCount++;
								}
							}
						});
						this.getEntries();
						// this.fieldMap = this.fields
						// 	.filter(
						// 		f =>
						// 			this.currentListLayout.columnShow.fields.indexOf(f.FIELD_ID) !==
						// 			-1
						// 	)
						// 	.map(v => [v.NAME, v.DISPLAY_LABEL]);
					});
			});
	}

	public getEntries() {
		this.listLayoutData = [];
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		const layoutId = this.currentListLayout.layoutId;
		this.dataSort = {
			sortBy,
			orderBy,
		};

		// save layout to localstorage
		const layoutObj = {
			MODULE_NAME: this.tableModuleName,
			LAYOUT_ID: layoutId,
			SORT_BY: sortBy,
			ORDER_BY: orderBy,
			PAGE: page,
			PAGE_SIZE: pageSize,
			SHOW_LIST_LAYOUTS: this.showListLayouts,
		};
		localStorage.setItem(
			`${this.tableModuleId}_LIST_LAYOUT`,
			JSON.stringify(layoutObj)
		);
		let fieldName = '';
		let fieldId = '';
		this.modulesService
			.getModuleById(this.tableModuleId)
			.subscribe((module: any) => {
				module.FIELDS.forEach((field) => {
					if (field.RELATIONSHIP_FIELD === this.tableFieldId) {
						fieldName = field.NAME;
						fieldId = field.RELATIONSHIP_FIELD;
					}
				});
				this.modulesService
					.getEntriesByLayoutIdOneToMany(
						this.tableModuleId,
						layoutId,
						sortBy,
						orderBy,
						page + 1,
						pageSize,
						fieldName,
						this.entryId,
						'MAPPED'
					)
					.subscribe(
						(entriesResponse: any) => {
							this.listLayoutData = entriesResponse.DATA;
							let fieldArray = this.tableFields.filter(
								(field) => field.DATA_TYPE.DISPLAY === 'Chronometer'
							);
							fieldArray.forEach((key) => {
								const chronoFieldName = key.NAME;
								this.listLayoutData.forEach((data) => {
									if (data.hasOwnProperty(chronoFieldName)) {
										data[chronoFieldName] =
											this.renderLayoutService.chronometerFormatTransform(
												data[chronoFieldName],
												''
											);
									}
								});
							});
							fieldArray = this.tableFields.filter(
								(field) => field.DATA_TYPE.DISPLAY === 'Phone'
							);
							fieldArray.forEach((key) => {
								const phoneFieldName = key.NAME;
								this.listLayoutData.forEach((data) => {
									if (data.hasOwnProperty(phoneFieldName)) {
										data[phoneFieldName] =
											data[phoneFieldName].DIAL_CODE +
											data[phoneFieldName].PHONE_NUMBER;
									}
								});
							});
							const totalEntries = this.entriesCount;
							if (totalEntries > 0 && this.customTableService.pageIndex !== 0) {
								if (
									totalEntries <=
									pageSize * this.customTableService.pageIndex
								) {
									this.customTableService.pageIndex =
										this.customTableService.pageIndex - 1;
									this.getEntries();
								}
							}
							if (this.layoutType !== 'create') {
								this.customTableService.setTableDataSource(
									this.listLayoutData,
									this.entriesCount
								);
							}
							// this.modulesService
							// 	.getOneToManyData(
							// 		this.moduleId,
							// 		this.tableModuleId,
							// 		fieldId,
							// 		this.entryId,
							// 		pageSize,
							// 		page + 1,
							// 		sortBy,
							// 		orderBy
							// 	)
							// 	.subscribe((result: any) => {
							// 	});
							// this.customTableService.setTable(this.customTableService);
							if (
								this.layoutType === 'create' &&
								this.createLayoutTableData.has(fieldId)
							) {
								if (
									this.customTableService.columnsHeaders.includes('Add Entry')
								) {
									this.customTableService.columnsHeaders.splice(-1, 1);
									this.customTableService.columnsHeadersObj.splice(-1, 1);
								}
								this.customTableService.paginator = this.paginator;
								this.customTableService.sort = this.sort;
								this.customTableService.setTableDataSource(
									this.createLayoutTableData.get(fieldId),
									this.createLayoutTableData.get(fieldId).length
								);
							}

							this.customTableService.isLoading = false;
							this.tableLoaded = true;
							this.customTableService.isLoading = false;
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			});
	}

	public rowHovered(element, action) {
		if (action === 'enter') {
			element.parentElement.style.background = '#f4f4f4';
		} else {
			element.parentElement.style.background = 'white';
		}
	}

	public sortTableData(event) {
		this.customTableService.isLoading = true;
		this.customTableService.columnsHeadersObj.forEach((item) => {
			if (item.DISPLAY === event.active) {
				this.customTableService.sortBy = item.NAME;
			}
		});
		this.customTableService.sortOrder = event.direction;
		this.sortData.emit(event);
		this.getEntries();
		this.customTableService.isLoading = false;
	}

	public rowClicked(row) {
		this.router.navigate([`render/${this.tableModuleId}/edit/${row.DATA_ID}`]);
	}

	public pageChanged(event) {
		this.customTableService.isLoading = true;
		this.customTableService.pageIndex = event.pageIndex;
		this.customTableService.pageSize = event.pageSize;
		this.pageChangeEmit.emit(event);
		this.getEntries();
		this.customTableService.isLoading = false;
	}

	private buildTitleBarTemplate(titleBar: any): String {
		let titleBarTemplate = '<div fxLayoutGap=10px>';
		this.titleBar = titleBar;
		titleBar.forEach((grid) => {
			titleBarTemplate =
				titleBarTemplate +
				this.getTemplateForField(this.fieldsMap[grid.FIELD_ID]);
			if (grid.SETTINGS && grid.SETTINGS.CONDITIONS.length > 0) {
				if (
					this.conditionFields.indexOf(grid.SETTINGS.CONDITIONS.CONDITION) ===
					-1
				) {
					this.conditionFields.push(grid.SETTINGS.CONDITIONS.CONDITION);
				}
			}
		});
		return titleBarTemplate + '</div>';
	}

	public onScroll(moduleField) {
		this.scrollSubject.next(moduleField);
	}

	public closeAutoComplete(moduleField) {
		const fieldControlName = moduleField.FIELD_ID.replace(/-/g, '_') + 'Ctrl';
		of(moduleField)
			.pipe(
				startWith(''),
				debounceTime(400),
				distinctUntilChanged(),
				switchMap((value: any) => {
					const fieldSearch =
						moduleField.PRIMARY_DISPLAY_FIELD_NAME +
						'=' +
						(this.formControls[fieldControlName] &&
						this.formControls[fieldControlName].value
							? this.formControls[fieldControlName].value
							: '*');
					return this.modulesService
						.getFieldFilteredPaginatedSearchEntries(
							moduleField.MODULE,
							fieldSearch,
							moduleField.PRIMARY_DISPLAY_FIELD_NAME,
							'asc',
							1,
							10,
							moduleField.RELATIONSHIP_FIELD
						)
						.pipe(
							map((results: any) => {
								if (results.DATA.length > 0) {
									this.relationFieldFilteredEntries[moduleField.NAME] =
										results.DATA;
								}
								return results.DATA;
							})
						);
				})
			)
			.subscribe();
	}

	private groupTabs() {
		// for (let i = 0; i < this.customLayouts.length; i++) {
		// 	if (this.customLayouts[i].displayType === 'Tab') {
		// 		this.matTabGroup.push(this.customLayouts[i]);
		// 	}
		// }

		this.customLayouts.forEach((layout) => {
			if (layout.displayType === 'Tab') {
				this.matTabGroupExists = true;
				this.matTabGroup.push(layout);
			}
		});
	}

	private oneToManyCreateLayout(moduleId, fieldId) {
		this.currentTable = '';
		const renderDetail = this.dialog.open(RenderDetailLayoutComponent, {
			width: '1024px',
			height: '768px',
			id: 'render-detail-dialog',
			data: {
				MODULE_ID: moduleId,
				PARENT_MODULE_ID: this.moduleId,
				FIELD_ID: fieldId,
				EXISTING_CREATE_DATA: this.oneToManyCreateData,
				DATA_IDS: this.customTableService.dataIds,
			},
		});
		renderDetail.afterClosed().subscribe((result) => {
			this.moduleId = this.route.snapshot.params['moduleId'];
			this.entryId = this.route.snapshot.params['dataId'];
			if (result.ONE_TO_MANY_DATA !== null && this.entryId === 'new') {
				this.oneToManyCreateData = result.ONE_TO_MANY_DATA;
				if (this.createLayoutTableData.has(fieldId)) {
					const data = this.createLayoutTableData.get(fieldId);
					data.push(result.CREATE_LAYOUT_TABLE_DATA);
					this.createLayoutTableData.set(fieldId, data);
				} else {
					this.createLayoutTableData.set(fieldId, [
						result.CREATE_LAYOUT_TABLE_DATA,
					]);
				}
				this.customTableService.customTableDataSource.data.push(
					result.CREATE_LAYOUT_TABLE_DATA
				);

				this.customTableService.totalRecords =
					this.customTableService.customTableDataSource.data.length;
			} else if (result.ONE_TO_MANY_DATA !== null && this.entryId !== 'new') {
				const keysList = Array.from(result.ONE_TO_MANY_DATA.keys());

				const parentModule = this.allModules.find(
					(module) => module.MODULE_ID === this.moduleId
				);
				parentModule.FIELDS.forEach((field) => {
					this.fieldsMap[field.FIELD_ID] = field;
				});
				keysList.forEach((key) => {
					const payload = result.ONE_TO_MANY_DATA.get(key);
					payload.MANY_SIDE.VALUE = this.entryId;
					const parentField = parentModule.FIELDS.find(
						(field) => field.NAME === payload.ONE_SIDE.FIELD_NAME
					);
					// this.entry[this.fieldsMap[relFieldId].NAME] = [];
					// this.entry[this.fieldsMap[relFieldId].NAME] = result.DATA_IDS;
					this.modulesService
						.putEntryOneToMany(this.moduleId, this.entryId, payload)
						.subscribe((ressponse) => {
							this.modulesService
								.getEntry(this.moduleId, this.entryId)
								.subscribe(
									(entryResponse: any) => {
										this.entry[this.fieldsMap[parentField.FIELD_ID].NAME] =
											entryResponse[this.fieldsMap[parentField.FIELD_ID].NAME];
										this.modulesService
											.getOneToManyData(
												this.moduleId,
												this.fieldsMap[parentField.FIELD_ID].MODULE,
												parentField.FIELD_ID,
												this.entryId,
												'5',
												'1',
												'DATE_UPDATED',
												'desc'
											)
											.subscribe(
												(response: any) => {
													this.oneToManyCountMap[
														this.fieldsMap[parentField.FIELD_ID].NAME
													] = response.DATA.length;
												},
												(error: any) => {
													console.log(error);
												}
											);
									},
									(error: any) => {
										this.bannerMessageService.errorNotifications.push(
											error.error.ERROR
										);
									}
								);
						});
				});
			}
		});
	}

	private saveFromDialog(action) {
		this.oneToManyCreateData = this.modalData.EXISTING_CREATE_DATA;
		if (action === 'cancel') {
			const result = {
				ONE_TO_MANY_DATA: this.oneToManyCreateData,
				DATA_IDS: this.modalData.DATA_IDS,
			};
			this.renderDetailDialogRef.close(result);
		} else if (action === 'done') {
			this.modulesService.postEntry(this.moduleId, this.entry).subscribe(
				(response: any) => {
					const module = this.allModules.find(
						(module) => module.MODULE_ID === this.moduleId
					);
					const field = module.FIELDS.find(
						(field) => field.RELATIONSHIP_FIELD === this.modalData.FIELD_ID
					);
					const parentModule = this.allModules.find(
						(module) => module.MODULE_ID === this.modalData.PARENT_MODULE_ID
					);
					const parentField = parentModule.FIELDS.find(
						(field) => field.FIELD_ID === this.modalData.FIELD_ID
					);

					parentModule.FIELDS.forEach((field) => {
						this.fieldsMap[field.FIELD_ID] = field;
					});
					const payload = {};
					let dataId = [];
					// if (this.customTableService.dataIds.has(module.SINGULAR_NAME)) {
					// 	dataId = this.customTableService.dataIds.get(module.SINGULAR_NAME);
					// }
					dataId.push(response.DATA_ID);
					payload['ONE_SIDE'] = {
						FIELD_NAME: parentField.NAME,
						VALUE: dataId,
					};
					payload['MANY_SIDE'] = {
						FIELD_NAME: field.NAME,
						VALUE: this.entryId,
						MODULE_ID: parentField.MODULE,
					};
					// this.customTableService.dataIds.set(module.SINGULAR_NAME, dataId);

					this.oneToManyCreateData.set(
						this.fieldsMap[parentField.FIELD_ID].NAME,
						payload
					);
					this.bannerMessageService.successNotifications.push({
						message: this.translateService.instant('SAVED_SUCCESSFULLY'),
					});
					module.FIELDS.forEach((currentField) => {
						if (
							currentField.RELATIONSHIP_TYPE === 'Many to Many' ||
							currentField.RELATIONSHIP_TYPE === 'Many to One'
						) {
							if (response.hasOwnProperty(currentField.NAME)) {
								this.modulesService
									.getEntry(currentField.MODULE, response[currentField.NAME])
									.subscribe((entry: any) => {
										const moduleRelated = this.allModules.find(
											(module) => module.MODULE_ID === currentField.MODULE
										);

										const fieldRelated = moduleRelated.FIELDS.find(
											(field) =>
												field.FIELD_ID === currentField.PRIMARY_DISPLAY_FIELD
										);
										const fieldName = fieldRelated.NAME;
										response[currentField.NAME] = entry[fieldName];
									});
							}
						}
					});
					const result = {
						ONE_TO_MANY_DATA: this.oneToManyCreateData,
						CREATE_LAYOUT_TABLE_DATA: response,
						DATA_IDS: this.modalData.DATA_IDS,
					};
					this.renderDetailDialogRef.close(result);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		}
	}

	public inheritValues(fieldId, entry) {
		const parentFieldsMap = new Map<any, any>();
		const resultMap = new Map<any, any>();
		const map = new Map<any, any>();
		const field = this.fieldsMap[fieldId];
		const childModuleId = field.MODULE;
		const childModule = this.allModules.find(
			(module) => module.MODULE_ID === childModuleId
		);
		childModule.FIELDS.forEach((field) => {
			parentFieldsMap.set(field.FIELD_ID, field);
		});
		const inheritanceMap: Map<string, any> = new Map(
			Object.entries(field.INHERITANCE_MAPPING)
		);
		const keysList = Array.from(inheritanceMap.keys());
		keysList.forEach((key) => {
			const parentFieldName = parentFieldsMap.get(key).NAME;
			map.set(parentFieldsMap.get(key).FIELD_ID, entry[parentFieldName]);
			resultMap.set(inheritanceMap.get(key), map.get(key));
			const fieldName = this.fieldsMap[inheritanceMap.get(key)].NAME;
			this.entry[fieldName] = resultMap.get(inheritanceMap.get(key));
		});
	}
}
