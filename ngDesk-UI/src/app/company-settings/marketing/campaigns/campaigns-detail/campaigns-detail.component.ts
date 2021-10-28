import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import {
	AfterViewInit,
	ChangeDetectorRef,
	Component,
	ElementRef,
	OnDestroy,
	OnInit,
	QueryList,
	TemplateRef,
	ViewChild,
	ViewChildren,
} from '@angular/core';
import {
	AbstractControl,
	FormArray,
	FormBuilder,
	FormControl,
	FormGroup,
	Validators,
} from '@angular/forms';
import {
	MatAutocomplete,
	MatAutocompleteSelectedEvent,
} from '@angular/material/autocomplete';
import { MatChipInputEvent, MatChipList } from '@angular/material/chips';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSidenav } from '@angular/material/sidenav';
import { ActivatedRoute, Router } from '@angular/router';
import { OWL_DATE_TIME_FORMATS } from '@danielmoncada/angular-datetime-picker';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { CompaniesService } from '@src/app/companies/companies.service';
import { CampaignsService } from '@src/app/company-settings/marketing/campaigns/campaigns.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { OWL_DATE_FORMATS } from '@src/app/render-layout/data-types/date-time.service';
import { config } from '@src/app/tiny-mce/tiny-mce-config';
import { UsersService } from '@src/app/users/users.service';
import { Observable, Subject, Subscription } from 'rxjs';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	startWith,
	switchMap,
} from 'rxjs/operators';
import tinymce from 'tinymce/tinymce';
import { v4 as uuid } from 'uuid';
import { CampaignsDetailService } from './campaigns-detail.service';
import { EmailListService } from '../../email-lists/email-lists.service';

export interface IMAGE {
	FILE: any;
	FILENAME: any;
}

@Component({
	selector: 'app-campaigns-detail',
	templateUrl: './campaigns-detail.component.html',
	styleUrls: ['./campaigns-detail.component.scss'],
	providers: [{ provide: OWL_DATE_TIME_FORMATS, useValue: OWL_DATE_FORMATS }],
})
export class CampaignsDetailComponent
	implements AfterViewInit, OnInit, OnDestroy
{
	public campaignForm: FormGroup;
	public availableItems = [];
	public availableLayouts = [];
	public linkToOptions = [];
	public usersFields: any;
	public users: any[] = [];
	public usersDisable: any[] = [];
	public testUsersDisable: any[] = [];
	public testUsers: any[] = [];
	public visible = true;
	public hover;
	public selectable = true;
	public removable = true;
	public addOnBlur = true;
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	public usersCtrl = new FormControl('', [Validators.required]);
	public testUsersCtrl = new FormControl('', [Validators.required]);
	public previewAsUserCtrl = new FormControl('');
	public filteredUsers: Observable<any[]>;
	public filteredTestUsers: Observable<any[]>;
	public filteredUsersToPreview: Observable<any[]>;
	public allUsers: any[] = [];
	public sendUsers: any[] = [];
	public requestUsers: any[] = [];
	public campaignId: string;
	public campaignType: string;
	public emailLists: any[] = [];
	public allEmailLists: any[] = [];
	public initialAllEmailLists: any[] = [];
	public emailListsCtrl = new FormControl('', [Validators.required]);
	public minDate = new Date();
	public params;
	public campaignLoaded = false;
	public sidepaneView: any = 'CONTENT';
	public imageGalleryData = [];
	public footerForm: FormGroup;
	public selectedElement: FormGroup = null;
	public selectedElementIndex: number = null;
	public sidenavView = 'SEND_TEST_EMAIL';
	public galleryLoading = false;
	public positionYChange;
	public footerSelected = false;
	public alignmentOptions = [];
	public fullAddressString;
	public fontFamilies = [
		'Roboto',
		'Arial',
		'Times New Roman',
		'Georgia',
		'Comic Sans MS',
		'Courier New',
	];
	private buttonClicks = [];
	public config = config;
	public tabIndex: number | undefined = 0;
	public fileTypeError = {
		LOGO: true,
	};
	public fileSizeError = {
		LOGO: true,
	};
	public defaultLogo = {
		LOGO: {
			FILE: '',
			FILENAME: '',
		},
		IMAGE_ID: '',
	};
	public elementsLoaded = false;
	private newRowDialogRef: MatDialogRef<any>;
	public selectedLayoutIndex: number;
	public dropList = [];
	public selectedRow: FormGroup | null = null;
	public selectedRowIndex: number | null = null;
	private companyInfoSubscription: Subscription;
	// public teamsScrollSubject = new Subject<any>();
	public usersScrollSubject = new Subject<any>();
	public emailListScrollSubject = new Subject<any>();
	public tempUserInput = '';
	public usersLength;
	@ViewChildren('editors') public editors: QueryList<any>;
	@ViewChild('tabGroup') public tabGroup: any;
	@ViewChild('usersInput') public usersInput: ElementRef<HTMLInputElement>;
	@ViewChild('testUsersInput')
	public testUsersInput: ElementRef<HTMLInputElement>;
	@ViewChild('emailListsInput')
	public emailListsInput: ElementRef<HTMLInputElement>;
	@ViewChild('usersAuto') public usersMaAutocomplete: MatAutocomplete;
	@ViewChild('testUsersAuto') public testUsersMaAutocomplete: MatAutocomplete;
	@ViewChild('emailListsAuto') public emailListsMaAutocomplete: MatAutocomplete;
	@ViewChild('testUsersChipList') public testUsersChipList: MatChipList;
	@ViewChild('usersChipList') public usersChipList: MatChipList;
	@ViewChild('emailListChipList') public emailListChipList: MatChipList;
	@ViewChild('sidenav') public sidenav: MatSidenav;
	@ViewChildren('images', { read: ElementRef })
	public images: QueryList<ElementRef>;
	@ViewChild('newRowDialog', { static: true })
	public newRowDialog: TemplateRef<any>;

	constructor(
		private formBuilder: FormBuilder,
		private modulesService: ModulesService,
		private companiesService: CompaniesService,
		private router: Router,
		private route: ActivatedRoute,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private usersService: UsersService,
		private cd: ChangeDetectorRef,
		private campaignsService: CampaignsService,
		private cacheService: CacheService,
		private dialog: MatDialog,
		private campaignsDetailService: CampaignsDetailService,
		private emailListService: EmailListService
	) {
		this.params = {
			noReplyEmail: `no-reply@${this.usersService.getSubdomain()}.ngdesk.com`,
		};

		this.translateService.get('TEXT').subscribe((textValue: string) => {
			this.translateService.get('IMAGE').subscribe((imageValue: string) => {
				this.translateService.get('BUTTON').subscribe((buttonValue: string) => {
					this.availableItems = [
						{ DISPLAY: textValue, TYPE: 'TEXT', ICON: 'subject' },
						{ DISPLAY: imageValue, TYPE: 'IMAGE', ICON: 'image' },
						{ DISPLAY: buttonValue, TYPE: 'BUTTON', ICON: 'add_box' },
					];
				});
			});
		});

		this.availableLayouts = this.campaignsService.getLayouts();

		this.translateService.get('LEFT_UPPER').subscribe((leftVal: string) => {
			this.translateService
				.get('CENTER_UPPER')
				.subscribe((centerVal: string) => {
					this.translateService
						.get('RIGHT_UPPER')
						.subscribe((rightVal: string) => {
							this.alignmentOptions = [
								{ DISPLAY: leftVal, ALIGN: 'flex-start' },
								{ DISPLAY: centerVal, ALIGN: 'center' },
								{ DISPLAY: rightVal, ALIGN: 'flex-end' },
							];
						});
				});
		});

		this.translateService
			.get('EMAIL_ADDRESS')
			.subscribe((emailAddressVal: string) => {
				this.translateService
					.get('FILE_DOWNLOAD')
					.subscribe((fileDownloadVal: string) => {
						this.translateService.get('URL').subscribe((urlVal: string) => {
							this.linkToOptions = [
								// {DISPLAY: emailAddressVal, OPTION: 'EMAIL_ADDRESS'},
								// {DISPLAY: fileDownloadVal, OPTION: 'FILE_DOWNLOAD'},
								{ DISPLAY: urlVal, OPTION: 'URL' },
							];
						});
					});
			});

		this.config['forced_root_block'] = '';
		this.config['force_br_newlines'] = true;
		this.config['plugins'] =
			'print preview searchreplace autolink directionality visualblocks visualchars link ' +
			'codesample table charmap hr pagebreak nonbreaking anchor insertdatetime advlist lists wordcount textpattern paste autoresize';
		this.config['toolbar'] =
			'formatselect | bold italic underline forecolor backcolor | link | ' +
			'alignleft aligncenter alignright alignjustify | numlist bullist outdent indent | removeformat tableprops';
	}

	public ngOnInit() {
		this.campaignId = this.route.snapshot.params['campaignId'];
		this.campaignType = this.route.snapshot.params['campaignType'];
		const template = this.campaignsService.getTemplate(this.campaignType);
		const footerAlignment =
			this.campaignType === 'Plain' ? 'flex-start' : 'center';
		// adding default text area with template for new campaigns as first row
		const defaultData = { TEXT: template ? template : '' };
		const defaultTextElement = this.addElement(
			'TEXT',
			defaultData
		) as FormGroup;
		defaultTextElement.addControl('WIDTH', new FormControl(3));
		const defaultRow = this.createRow('1') as FormGroup;
		(defaultRow.get('COLUMNS') as FormArray).push(defaultTextElement);
		// initialize the first row and its column as droppable areas
		this.dropList = ['0_0'];
		this.campaignForm = this.formBuilder.group({
			CAMPAIGN_ID: '',
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
			CAMPAIGN_TYPE: this.campaignType,
			SUBJECT: ['', [Validators.required]],
			BODY: '',
			ROWS: this.formBuilder.array([defaultRow]),
			SEND_OPTION: ['Send now', [Validators.required]],
			SEND_DATE: '',
			RECIPIENT_USERS: [],
			RECIPIENT_LISTS: [],
			STATUS: 'Draft',
			FOOTER: this.formBuilder.group({
				ADDRESS: this.formBuilder.group({
					COMPANY_NAME: ['', Validators.required],
					ADDRESS_1: ['', Validators.required],
					ADDRESS_2: '',
					CITY: ['', Validators.required],
					STATE: ['', Validators.required],
					ZIP_CODE: ['', Validators.required],
					COUNTRY: ['', Validators.required],
					PHONE: '',
				}),
				FULL_ADDRESS: { value: '', disabled: true },
				ALIGNMENT: footerAlignment,
			}),
		});
		this.footerForm = this.campaignForm.get('FOOTER') as FormGroup;
		this.getGalleryData();
		this.companyInfoSubscription =
			this.cacheService.companyInfoSubject.subscribe((dataStored) => {
				if (dataStored) {
					const userModule = this.cacheService.companyData['MODULES'].find(
						(module) => module['NAME'] === 'Users'
					);
					this.usersFields = userModule.FIELDS.filter(
						(field) => field.DATA_TYPE.DISPLAY !== 'Relationship'
					);
					let searchValue = '';
					let page = 0;
					this.campaignsDetailService
						.getUsersData(userModule.MODULE_ID, page, searchValue)
						.subscribe(
							(usersResponse: any) => {
								this.allUsers = usersResponse.DATA;
								this.initializeUsers();
								this.loadFieldData();
								this.emailListService
									.getAllEmailLists(0, 10, 'DATE_CREATED', 'Asc')
									.subscribe(
										(emailListsData: any) => {
											this.initialAllEmailLists = emailListsData.EMAIL_LISTS;
											this.allEmailLists = emailListsData.EMAIL_LISTS;
											this.initializeEmailList();
											if (this.campaignId !== 'new') {
												this.getCampaign();
											} else {
												this.elementsLoaded = true;
												this.campaignLoaded = true;
											}
										},
										(emailListsError: any) => {
											this.bannerMessageService.errorNotifications.push({
												message: emailListsError.error.ERROR,
											});
										}
									);
							},
							(usersError: any) => {
								this.bannerMessageService.errorNotifications.push({
									message: usersError.error.ERROR,
								});
							}
						);
				}
			});
	}

	public ngAfterViewInit() {
		this.tabIndex = this.tabGroup.selectedIndex;
	}

	public ngOnDestroy() {
		if (this.companyInfoSubscription) {
			this.companyInfoSubscription.unsubscribe();
		}
		(this.config['plugins'] =
			'print preview searchreplace autolink directionality visualblocks visualchars image imagetools\
  		link codesample table charmap hr pagebreak nonbreaking anchor insertdatetime advlist lists wordcount textpattern paste'),
			(this.config['toolbar'] =
				'formatselect | bold italic underline forecolor backcolor | link image | alignleft aligncenter alignright alignjustify |\
  	numlist bullist outdent indent | removeformat tableprops'),
			delete this.config['forced_root_block'];
		delete this.config['force_br_newlines'];
	}

	private getCampaign() {
		this.companiesService.getCampaignById(this.campaignId).subscribe(
			(campaignData: any) => {
				this.campaignForm.get('CAMPAIGN_ID').setValue(campaignData.CAMPAIGN_ID);
				this.campaignForm.get('NAME').setValue(campaignData.NAME);
				this.campaignForm.get('DESCRIPTION').setValue(campaignData.DESCRIPTION);
				this.campaignForm.get('BODY').setValue(campaignData.BODY);
				this.campaignForm.get('SUBJECT').setValue(campaignData.SUBJECT);
				this.campaignForm.get('STATUS').setValue(campaignData.STATUS);
				this.campaignForm.get('SEND_OPTION').setValue(campaignData.SEND_OPTION);
				this.campaignForm.get('SEND_DATE').setValue(campaignData.SEND_DATE);
				const campaignFormRows = this.campaignForm.get('ROWS') as FormArray;
				campaignFormRows.clear();
				const rows = this.setRows(campaignData.ROWS);
				rows.controls.forEach((row) => {
					campaignFormRows.push(row);
				});
				this.setFooter(campaignData.FOOTER);
				this.footerForm = this.campaignForm.get('FOOTER') as FormGroup;
				this.users = this.transformIdsToObj(
					campaignData.RECIPIENT_USERS,
					'users'
				);
				this.emailLists = this.transformIdsToObj(
					campaignData.RECIPIENT_LISTS,
					'emailLists'
				);
				console.log(this.emailLists);
				this.buttonClicks = campaignData.BUTTON_CLICKS;
				this.campaignLoaded = true;
				this.elementsLoaded = true;
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	private setRows(rowData): FormArray {
		const rows = this.formBuilder.array([]);
		rowData.forEach((row) => {
			const columnsToAdd = this.formBuilder.array([]);
			row.COLUMNS.forEach((column) => {
				if (column.SETTINGS.FONT_STYLING) {
					column.SETTINGS.FONT_STYLING = this.formBuilder.control(
						column.SETTINGS.FONT_STYLING
					);
				}
				const newColumn = this.formBuilder.group({
					TYPE: column.TYPE,
					WIDTH: column.WIDTH,
					SETTINGS: this.formBuilder.group(column.SETTINGS),
				});
				columnsToAdd.push(newColumn);
			});
			const newRow = this.formBuilder.group({
				SETTINGS: this.formBuilder.group(row.SETTINGS),
				COLUMNS: columnsToAdd,
			});
			rows.push(newRow);
		});
		return rows;
	}

	private loadFieldData() {
		this.campaignForm
			.get('SEND_OPTION')
			.valueChanges.subscribe((sendOption) => {
				this.minDate = new Date();
				if (sendOption === 'Send later') {
					this.campaignForm
						.get('SEND_DATE')
						.setValidators([Validators.required]);
				} else {
					this.campaignForm.get('SEND_DATE').setValidators(null);
					this.campaignForm.get('SEND_DATE').setValue('');
				}
				this.campaignForm.get('SEND_DATE').updateValueAndValidity();
			});
	}

	public displayNameFn(user?: any): string | undefined {
		return user ? user['EMAIL_ADDRESS'] : undefined;
	}

	public addEmailList(event: MatChipInputEvent): void {
		if (!this.usersMaAutocomplete.isOpen) {
			const input = event.input;
			const value = event.value;

			if ((value || '').trim() && value['NAME']) {
				this.emailLists.push(value.trim());
			}

			if (input) {
				input.value = '';
			}

			this.emailListsCtrl.setValue(null);
		}
	}

	public tabChanged() {
		this.elementsLoaded = false;
		if (this.tabIndex === 0) {
			this.elementsLoaded = true;
		} else {
			tinymce.DOM.remove('');
		}
	}

	private _filterEmailLists(value): any[] {
		if (value['NAME']) {
			value = value['NAME'];
		}
		const filterValue = value.toLowerCase();
		return this.allEmailLists.filter(
			(emailList) => emailList['NAME'].toLowerCase().indexOf(filterValue) === 0
		);
	}
	public initializeUsers() {
		this.usersScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let searchValue = '';
					if (value !== '') {
						searchValue = 'EMAIL_ADDRESS' + '=' + value;
					}
					let page = 0;
					const moduleId = this.cacheService.moduleNamesToIds['Users'];
					if (this.allUsers && !search) {
						page = Math.ceil(this.allUsers.length / 10);
					}
					return this.campaignsDetailService
						.getUsersData(moduleId, page, searchValue)
						.pipe(
							map((results: any) => {
								if (search) {
									this.allUsers = results.DATA;
								} else {
									const newlist = this.filterNewLists('Users', results['DATA']);
									if (newlist.length > 0) {
										this.allUsers = this.allUsers.concat(results.DATA);
									}
								}
								this.usersLength = this.allUsers.length;
								return results.DATA;
							})
						);
				})
			)
			.subscribe();
	}
	public filterNewLists(type, data) {
		const newArr = [];
		data.forEach((user) => {
			const existingUser = this.allUsers.find(
				(currentUser) => currentUser.DATA_ID === user.DATA_ID
			);
			if (!existingUser) {
				newArr.push(user);
			}
		});
		return newArr;
	}

	public onUsersScroll() {
		this.usersScrollSubject.next([this.tempUserInput, false]);
	}
	public searchUser() {
		this.usersScrollSubject.next([this.tempUserInput, true]);
	}
	public selected(event: MatAutocompleteSelectedEvent, type: string): void {
		if (type === 'users') {
			this.users.push(event.option.value);
			this.usersDisable.push(event.option.value.DATA_ID);
			this.tempUserInput = '';
		} else if (type === 'testUsers') {
			this.testUsers.push(event.option.value);
			this.testUsersDisable.push(event.option.value.DATA_ID);
			this.tempUserInput = '';
		} else {
			if (event.option.viewValue === 'New email list') {
				this.router.navigate([`company-settings/marketing/email-lists/new`]);
			}
			this.emailLists.push(event.option.value);
			this.emailListsInput.nativeElement.value = '';
			this.emailListsCtrl.setValue(null);
		}
	}

	public disabledCheck(entry, users, type: string) {
		if (type === 'users') {
			return users.indexOf(entry.DATA_ID) !== -1;
		} else {
			return users.indexOf(entry.DATA_ID) !== -1;
		}
	}

	public removeUsers(element, type: string): void {
		const index = this.usersDisable.indexOf(element.DATA_ID);
		if (index >= 0 && type === 'testUsers') {
			console.log(this.users);
			this.testUsersDisable.splice(index, 1);
			this.testUsers.splice(index, 1);
		} else {
			this.usersDisable.splice(index, 1);
			this.users.splice(index, 1);
		}
	}
	public resetInput(event: MatChipInputEvent): void {
		const input = event.input;
		if (input) {
			input.value = '';
		}
	}
	public remove(item: any, dataArray: any[]): void {
		const index = dataArray.indexOf(item);

		if (index >= 0) {
			dataArray.splice(index, 1);
		}
	}
	public userAutocompleteClosed() {
		this.usersScrollSubject.next(['', true]);
	}

	public insertBodyVariable(rowIndex, elementIndex, field, elementType) {
		let elementControl = null;
		if (elementType === 'subject') {
			elementControl = this.campaignForm.get('SUBJECT') as FormControl;
		} else {
			const bodyElements = this.campaignForm.get('ROWS') as FormArray;
			const element = (
				bodyElements.controls[rowIndex].get('COLUMNS') as FormArray
			).controls[elementIndex];
			elementControl = element.get('SETTINGS').get('TEXT') as FormControl;
		}
		const bodyText = elementControl.value;
		const fieldVar = field.NAME;
		switch (fieldVar) {
			case 'REQUESTOR': {
				const newBody = `${bodyText} {{inputMessage.${fieldVar}.EMAIL_ADDRESS}}`;
				elementControl.setValue(newBody);
				break;
			}
			case 'ASSIGNEE': {
				const newBody = `${bodyText} {{inputMessage.${fieldVar}.EMAIL_ADDRESS}}`;
				elementControl.setValue(newBody);
				break;
			}
			case 'CREATED_BY': {
				const newBody = `${bodyText} {{inputMessage.${fieldVar}.EMAIL_ADDRESS}}`;
				elementControl.setValue(newBody);
				break;
			}
			case 'LAST_UPDATED_BY': {
				const newBody = `${bodyText} {{inputMessage.${fieldVar}.EMAIL_ADDRESS}}`;
				elementControl.setValue(newBody);
				break;
			}
			case 'ACCOUNT': {
				const newBody = `${bodyText} {{inputMessage.${fieldVar}.ACCOUNT_NAME}}`;
				elementControl.setValue(newBody);
				break;
			}
			default: {
				const newBody = `${bodyText} {{inputMessage.${fieldVar}}}`;
				elementControl.setValue(newBody);
				break;
			}
		}
	}

	public onFileChange(event, fgName) {
		this.galleryLoading = true;
		const reader = new FileReader();
		if (event.target.files && event.target.files.length) {
			const [file] = event.target.files;
			if (
				file.type === 'image/jpeg' ||
				file.type === 'image/png' ||
				file.type === 'image/x-png'
			) {
				this.fileTypeError[fgName] = true;
				file.size <= 1024000
					? (this.fileSizeError[fgName] = true)
					: (this.fileSizeError[fgName] = false);
			} else {
				this.fileTypeError[fgName] = false;
			}
			reader.readAsDataURL(file);
			reader.onload = () => {
				const imageObj = {
					LOGO: { FILENAME: file.name, FILE: reader.result },
				};
				this.companiesService.postGalleryImage(imageObj).subscribe(
					(success: any) => {
						this.getGalleryData();
					},
					(error: any) => {
						console.log(error);
					}
				);

				// need to run CD since file load runs outside of zone
				this.cd.markForCheck();
			};
		}
	}

	public getGalleryData() {
		this.galleryLoading = true;
		this.companiesService.getGallery().subscribe(
			(gallerySuccess: any) => {
				this.galleryLoading = false;
				this.imageGalleryData = gallerySuccess.GALLERY;
				const ngDeskLogo = this.imageGalleryData.find(
					(image) => image.LOGO.FILENAME === 'ngdesk.png'
				);
				this.defaultLogo.LOGO = ngDeskLogo.LOGO;
				this.defaultLogo.IMAGE_ID = ngDeskLogo.IMAGE_ID;
			},
			(error: any) => {
				this.galleryLoading = false;
				console.log(error);
			}
		);
	}

	// build campaign body html
	public getCampaignObj(): any {
		this.setFooter(this.footerForm.value);
		this.campaignForm.get('RECIPIENT_USERS').setValue(this.users);
		this.campaignForm.get('RECIPIENT_LISTS').setValue(this.emailLists);
		const campaignObj = this.campaignForm.value;
		campaignObj.RECIPIENT_LISTS = this.transformObjsToId(
			campaignObj.RECIPIENT_LISTS,
			'EMAIL_LIST_ID'
		);
		campaignObj.RECIPIENT_USERS = this.transformObjsToId(
			campaignObj.RECIPIENT_USERS,
			'DATA_ID'
		);
		campaignObj.BUTTON_CLICKS = this.buttonClicks;
		return campaignObj;
	}

	public sendTest() {
		if (this.campaignForm.valid) {
			if (this.footerForm.valid) {
				const testEmailObj = this.getCampaignObj();
				testEmailObj['RECIPIENT_USERS'] = this.transformObjsToId(
					this.testUsers,
					'DATA_ID'
				);
				testEmailObj['RECIPIENT_LISTS'] = [];
				testEmailObj['PREVIEW_USER'] = this.previewAsUserCtrl.value
					? this.previewAsUserCtrl.value['DATA_ID']
					: null;
				this.companiesService.sendTestEmail(testEmailObj).subscribe(
					(sendSuccess: any) => {
						this.sidenav.close();
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SEND_CAMPAIGN_SUCCESS'),
						});
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			} else {
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('COMPANY_ADDRESS_MISSING'),
				});
				this.footerSelected = true;
				this.sidepaneView = 'EDIT_FOOTER';
			}
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant(
					'CAMPAIGN_REQUIRED_FIELDS_MISSING'
				),
			});
		}
	}

	public save(action: string) {
		if (this.campaignForm.valid) {
			const campaignObj = this.getCampaignObj();
			if (this.campaignId !== 'new') {
				campaignObj['CAMPAIGN_ID'] = this.campaignId;
				this.companiesService.putCampaign(campaignObj).subscribe(
					(putCampaignSuccess: any) => {
						this.afterSaveActions(action, putCampaignSuccess);
					},
					(putCampaignError: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: putCampaignError.error.ERROR,
						});
					}
				);
			} else {
				this.companiesService.postCampaign(campaignObj).subscribe(
					(postCampaignSuccess: any) => {
						this.afterSaveActions(action, postCampaignSuccess);
					},
					(postCampaignError: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: postCampaignError.error.ERROR,
						});
					}
				);
			}
		} else if (!this.campaignForm.get('FOOTER').valid) {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('COMPANY_ADDRESS_MISSING'),
			});
			this.footerSelected = true;
			this.sidepaneView = 'EDIT_FOOTER';
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant(
					'CAMPAIGN_REQUIRED_FIELDS_MISSING'
				),
			});
		}
	}

	private afterSaveActions(action: string, campaignObj?: any) {
		switch (action) {
			case 'send': {
				this.companiesService.sendCampaign(campaignObj).subscribe(
					(sendSuccess: any) => {
						this.campaignLoaded = false;
						this.elementsLoaded = false;
						this.campaignId = campaignObj.CAMPAIGN_ID;
						this.router.navigate([
							`company-settings/marketing/campaigns/${campaignObj.CAMPAIGN_TYPE}/${campaignObj.CAMPAIGN_ID}`,
						]);
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SEND_CAMPAIGN_SUCCESS'),
						});
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
				break;
			}
			case 'close': {
				this.router.navigate([`company-settings/marketing/campaigns`]);
				break;
			}
		}
	}

	private transformObjsToId(data: any[], key: any): any[] {
		const arrayOfIds = [];
		for (const item of data) {
			arrayOfIds.push(item[key]);
		}
		return arrayOfIds;
	}

	private transformIdsToObj(arrayOfIds: string[], type: string): any[] {
		const newList = [];
		switch (type) {
			case 'users': {
				for (const id of arrayOfIds) {
					const matchedUser = this.allUsers.find((user) => user.DATA_ID === id);
					newList.push(matchedUser);
				}
				break;
			}
			case 'emailLists': {
				console.log(this.allEmailLists);
				for (const id of arrayOfIds) {
					const matchedEmailList = this.allEmailLists.find(
						(list) => list.EMAIL_LIST_ID === id
					);
					newList.push(matchedEmailList);
				}
				break;
			}
		}
		return newList;
	}

	public elementSelected(item, index, rowIndex) {
		this.footerSelected = false;
		this.selectedElement = item as FormGroup;
		this.selectedElementIndex = index;
		this.selectedRowIndex = rowIndex;
		this.onElementChange();
		switch (item.value.TYPE) {
			case 'IMAGE': {
				this.sidepaneView = 'EDIT_IMAGE';
				break;
			}
			case 'BUTTON': {
				this.sidepaneView = 'EDIT_BUTTON';
				break;
			}
			default: {
				this.sidepaneView = 'CONTENT';
				break;
			}
		}
	}

	public rowSelected(index) {
		if (this.campaignType !== 'Plain') {
			this.selectedRow = (this.campaignForm.get('ROWS') as FormArray).at(
				index
			) as FormGroup;
			this.selectedRowIndex = index;
			if (this.selectedElement === null && this.selectedElementIndex === null) {
				this.sidepaneView = 'ROW_SETTINGS';
			}
		}
	}

	public columnLayoutChanged(event) {
		const newColumnLayout = event.value;
		const newColumns = this.formBuilder.array([]);
		const columnsToAdd =
			this.campaignsService.getLayoutByType(newColumnLayout).COLUMNS;
		columnsToAdd.forEach((column, columnIndex) => {
			newColumns.push(this.formBuilder.group(column));
		});
		const rowsToAdd = [];
		const rowColumns = this.selectedRow.get('COLUMNS') as FormArray;
		rowColumns.controls.forEach((column, columnIndex) => {
			if (column.value.SETTINGS) {
				if (newColumns.at(columnIndex)) {
					const columnToReplace = newColumns.at(columnIndex) as FormGroup;
					columnToReplace.get('TYPE').setValue(column.value.TYPE);
					columnToReplace.addControl(
						'SETTINGS',
						column.get('SETTINGS') as FormGroup
					);
					if (column.value.TYPE === 'IMAGE') {
						columnToReplace
							.get('SETTINGS')
							.get('WIDTH')
							.setValidators(
								Validators.max(this.getMaxImageWidth(columnToReplace))
							);
						columnToReplace
							.get('SETTINGS')
							.get('WIDTH')
							.setValue(this.getMaxImageWidth(columnToReplace));
						columnToReplace
							.get('SETTINGS')
							.get('HEIGHT')
							.setValue(
								this.calculateImageHeight(
									columnToReplace,
									this.getMaxImageWidth(columnToReplace)
								)
							);
					}
				} else {
					const newColumn = this.formBuilder.group({
						WIDTH: 3,
						TYPE: column.value.TYPE,
						SETTINGS: this.formBuilder.group(column.value.SETTINGS),
					});
					if (column.value.TYPE === 'IMAGE') {
						newColumn
							.get('SETTINGS')
							.get('WIDTH')
							.setValidators(Validators.max(this.getMaxImageWidth(newColumn)));
						newColumn
							.get('SETTINGS')
							.get('WIDTH')
							.setValue(this.getMaxImageWidth(newColumn));
						newColumn
							.get('SETTINGS')
							.get('HEIGHT')
							.setValue(
								this.calculateImageHeight(
									newColumn,
									this.getMaxImageWidth(newColumn)
								)
							);
					}
					const newRow = {
						SETTINGS: this.formBuilder.group({
							COLUMN_LAYOUT: '1',
						}),
						COLUMNS: this.formBuilder.array([newColumn]),
					};
					rowsToAdd.push(newRow);
				}
			}
		});
		(this.selectedRow.get('COLUMNS') as FormArray).clear();
		newColumns.controls.forEach((control: FormGroup) => {
			(this.selectedRow.get('COLUMNS') as FormArray).push(control);
		});
		rowsToAdd.forEach((row) => {
			(this.campaignForm.get('ROWS') as FormArray).controls.splice(
				this.selectedRowIndex + 1,
				0,
				this.formBuilder.group(row)
			);
		});
		this.cd.detectChanges();
		this.reorganizeDroplist();
	}

	private calculateImageHeight(column, width) {
		const i = new Image();
		i.src = column.get('SETTINGS').get('FILE').value;
		i.onload = () => {
			const aspectRatio = i.width / i.height;
			const w = width;
			const newH = w / aspectRatio;
			if (column.get('SETTINGS').get('HEIGHT').value !== newH) {
				column
					.get('SETTINGS')
					.get('HEIGHT')
					.setValue(Math.round(newH), { emitEvent: false });
			}
		};
	}

	private onElementChange() {
		if (this.selectedElement.value.TYPE === 'IMAGE') {
			this.selectedElement
				.get('SETTINGS')
				.get('HEIGHT')
				.valueChanges.subscribe((value) => {
					const i = new Image();
					i.src = this.selectedElement.get('SETTINGS').get('FILE').value;
					i.onload = () => {
						const aspectRatio = i.width / i.height;
						const h = value;
						const newW = h * aspectRatio;
						if (
							this.selectedElement.get('SETTINGS').get('WIDTH').value !== newW
						) {
							this.selectedElement
								.get('SETTINGS')
								.get('WIDTH')
								.setValue(Math.round(newW), { emitEvent: false });
						}
					};
				});

			this.selectedElement
				.get('SETTINGS')
				.get('WIDTH')
				.valueChanges.subscribe((value) => {
					this.calculateImageHeight(this.selectedElement, value);
					const maxValue = this.getMaxImageWidth(this.selectedElement);
					if (value > maxValue) {
						this.selectedElement
							.get('SETTINGS')
							.get('WIDTH')
							.setValue(maxValue);
					}
				});
		} else if (this.selectedElement.value.TYPE === 'BUTTON') {
			this.selectedElement
				.get('SETTINGS')
				.get('LINK_TO')
				.valueChanges.subscribe((value) => {
					if (value === 'EMAIL_ADDRESS') {
						this.selectedElement
							.get('SETTINGS')
							.get('LINK_VALUE')
							.setValidators([Validators.required, Validators.email]);
					} else if (value === 'URL') {
						this.selectedElement
							.get('SETTINGS')
							.get('LINK_VALUE')
							.setValidators(Validators.required);
					} else {
						this.selectedElement
							.get('SETTINGS')
							.get('LINK_VALUE')
							.clearValidators();
					}
					this.selectedElement
						.get('SETTINGS')
						.get('LINK_VALUE')
						.updateValueAndValidity();
				});

			this.selectedElement
				.get('SETTINGS')
				.get('BORDER_WIDTH')
				.valueChanges.subscribe((value) => {
					if (value > 10) {
						this.selectedElement
							.get('SETTINGS')
							.get('BORDER_WIDTH')
							.setValue(10);
					} else if (value < 1) {
						this.selectedElement
							.get('SETTINGS')
							.get('BORDER_WIDTH')
							.setValue(1);
					}
				});

			this.selectedElement
				.get('SETTINGS')
				.get('HAS_BORDER')
				.valueChanges.subscribe((value) => {
					if (value === false) {
						this.selectedElement
							.get('SETTINGS')
							.get('BORDER_WIDTH')
							.setValidators(Validators.min(0));
						this.selectedElement
							.get('SETTINGS')
							.get('BORDER_WIDTH')
							.setValue(0);
					} else if (value === true) {
						this.selectedElement
							.get('SETTINGS')
							.get('BORDER_WIDTH')
							.setValidators(Validators.min(1));
						this.selectedElement
							.get('SETTINGS')
							.get('BORDER_WIDTH')
							.setValue(1);
					}
					this.selectedElement
						.get('SETTINGS')
						.get('BORDER_WIDTH')
						.updateValueAndValidity();
				});

			this.selectedElement
				.get('SETTINGS')
				.get('FONT_SIZE')
				.valueChanges.subscribe((value) => {
					if (value > 60) {
						this.selectedElement.get('SETTINGS').get('FONT_SIZE').setValue(60);
					} else if (value < 0) {
						this.selectedElement.get('SETTINGS').get('FONT_SIZE').setValue(0);
					}
				});
		}
	}

	public dropElements(event: CdkDragDrop<any[]>) {
		if (event.previousContainer === event.container) {
			moveItemInArray(
				event.container.data,
				event.previousIndex,
				event.currentIndex
			);
		} else {
			const data = event.container.data;
			const elementsArray = this.campaignForm.get('ROWS') as FormArray;
			const rowFound = elementsArray.at(data['ROW']) as FormGroup;
			const columnFound = (rowFound.get('COLUMNS') as FormArray).at(
				data['COLUMN']
			) as FormGroup;
			if (columnFound.value.TYPE === '') {
				// to prevent items dragging on top of other items
				const newElement = this.addElement(event.item.data['TYPE']);
				const columnSettings = this.formBuilder.group(
					newElement.value['SETTINGS']
				);
				columnFound.addControl('SETTINGS', columnSettings);
				columnFound.patchValue({ TYPE: newElement.value['TYPE'] });
				if (event.item.data['TYPE'] === 'IMAGE') {
					columnFound
						.get('SETTINGS')
						.get('WIDTH')
						.setValidators(Validators.max(this.getMaxImageWidth(columnFound)));
				}
			}
		}
	}

	public getMaxImageWidth(column: FormGroup | AbstractControl): number {
		if (column.get('TYPE').value === 'IMAGE') {
			const imageWidthControl = column.get('SETTINGS').get('WIDTH');
			let maxWidth = 160;
			switch (column.get('WIDTH').value) {
				case 3.0: {
					maxWidth = 560;
					break;
				}
				case 0.6667: {
					maxWidth = 360;
					break;
				}
				case 1.5: {
					maxWidth = 260;
					break;
				}
			}
			return maxWidth;
		}
	}

	public removeRow(rowIndex): void {
		const rows = this.campaignForm.get('ROWS') as FormArray;
		const columns = rows.at(rowIndex).get('COLUMNS') as FormArray;
		columns.controls.forEach((column, columnIndex) => {
			this.dropList.splice(
				this.dropList.indexOf(`${rowIndex}_${columnIndex}`),
				1
			);
		});
		rows.removeAt(rowIndex);
		this.reorganizeDroplist();
		this.sidepaneView = 'CONTENT';
	}

	public removeElement(rowIndex, columnIndex) {
		this.selectedElement = null;
		this.selectedElementIndex = null;
		const columnsAtRow = (this.campaignForm.get('ROWS') as FormArray)
			.at(rowIndex)
			.get('COLUMNS') as FormArray;
		const columnElement = columnsAtRow.at(columnIndex) as FormGroup;
		columnElement.get('TYPE').setValue('');
		this.sidepaneView = 'CONTENT';
		columnElement.removeControl('SETTINGS');
	}

	public onRadiusChange(event) {
		this.selectedElement
			.get('SETTINGS')
			.get('CORNER_RADIUS')
			.setValue(event.value);
	}

	public onTextColorChange(event) {
		this.selectedElement
			.get('SETTINGS')
			.get('TEXT_COLOR')
			.setValue(event.target.value);
	}

	public onBorderColorChange(event) {
		this.selectedElement
			.get('SETTINGS')
			.get('BORDER_COLOR')
			.setValue(event.target.value);
	}

	public onBackgroundColorChange(event) {
		this.selectedElement
			.get('SETTINGS')
			.get('BACKGROUND_COLOR')
			.setValue(event.target.value);
	}

	public buttonTextStyleChange(event) {
		if (event.value.indexOf('bold') !== -1) {
			this.selectedElement.get('SETTINGS').get('FONT_WEIGHT').setValue('bold');
		} else {
			this.selectedElement
				.get('SETTINGS')
				.get('FONT_WEIGHT')
				.setValue('initial');
		}

		if (event.value.indexOf('italic') !== -1) {
			this.selectedElement
				.get('SETTINGS')
				.get('FONT_ITALICS')
				.setValue('italic');
		} else {
			this.selectedElement
				.get('SETTINGS')
				.get('FONT_ITALICS')
				.setValue('normal');
		}

		if (event.value.indexOf('underline') !== -1) {
			this.selectedElement
				.get('SETTINGS')
				.get('FONT_UNDERLINE')
				.setValue('underline');
		} else {
			this.selectedElement
				.get('SETTINGS')
				.get('FONT_UNDERLINE')
				.setValue('none');
		}
	}

	private createRow(columnLayout: string): FormGroup {
		return this.formBuilder.group({
			SETTINGS: this.formBuilder.group({
				COLUMN_LAYOUT: columnLayout,
			}),
			COLUMNS: this.formBuilder.array([]),
		});
	}

	public addElement(type: string, defaultData?: any): FormGroup {
		switch (type) {
			case 'BUTTON': {
				return this.formBuilder.group({
					TYPE: 'BUTTON',
					SETTINGS: this.formBuilder.group({
						TEXT: [
							defaultData
								? defaultData['TEXT']
								: this.translateService.instant('ADD_BUTTON_LINK_HERE'),
							Validators.required,
						],
						TEXT_COLOR: [
							defaultData ? defaultData['TEXT_COLOR'] : '#ffffff',
							Validators.required,
						],
						FONT_FAMILY: [
							defaultData ? defaultData['FONT_FAMILY'] : 'Roboto',
							[Validators.required, Validators.max(60), Validators.min(0)],
						],
						FONT_SIZE: [
							defaultData ? defaultData['FONT_SIZE'] : 14,
							Validators.required,
						],
						FONT_WEIGHT: defaultData ? defaultData['FONT_WEIGHT'] : 'initial',
						FONT_UNDERLINE: defaultData ? defaultData['FONT_UNDERLINE'] : false,
						FONT_ITALICS: defaultData ? defaultData['FONT_ITALICS'] : false,
						FONT_STYLING: this.formBuilder.control(
							defaultData ? defaultData['FONT_STYLING'] : []
						),
						BACKGROUND_COLOR: [
							defaultData ? defaultData['BACKGROUND_COLOR'] : '#3f51b5',
							Validators.required,
						],
						ID: defaultData ? defaultData['ID'] : uuid(),
						ALIGNMENT: [
							defaultData ? defaultData['ALIGNMENT'] : 'center',
							Validators.required,
						],
						HAS_FULL_WIDTH: defaultData ? defaultData['HAS_FULL_WIDTH'] : false,
						LINK_TO: [
							defaultData ? defaultData['LINK_TO'] : 'URL',
							Validators.required,
						],
						LINK_VALUE: [
							defaultData ? defaultData['LINK_VALUE'] : '',
							Validators.required,
						],
						CORNER_RADIUS: defaultData ? defaultData['CORNER_RADIUS'] : 4,
						HAS_BORDER: defaultData ? defaultData['HAS_BORDER'] : false,
						BORDER_WIDTH: [
							defaultData && defaultData['BORDER_WIDTH']
								? defaultData['BORDER_WIDTH']
								: 0,
							[Validators.min(0), Validators.max(10)],
						],
						BORDER_COLOR:
							defaultData && defaultData['BORDER_COLOR']
								? defaultData['BORDER_COLOR']
								: '#2d3e50',
					}),
				});
			}
			case 'IMAGE': {
				return this.formBuilder.group({
					TYPE: 'IMAGE',
					SETTINGS: this.formBuilder.group({
						FILE: defaultData ? defaultData['LOGO']['FILE'] : '',
						FILENAME: defaultData ? defaultData['LOGO']['FILENAME'] : '',
						WIDTH: [
							defaultData ? defaultData['WIDTH'] : '',
							Validators.required,
						],
						HEIGHT: [
							defaultData ? defaultData['HEIGHT'] : '',
							Validators.required,
						],
						ID: defaultData ? defaultData['ID'] : '',
						ALIGNMENT: defaultData ? defaultData['ALIGNMENT'] : 'flex-start',
						LINK_VALUE: defaultData ? defaultData['LINK_VALUE'] : '',
						ALTERNATE_TEXT: defaultData ? defaultData['ALTERNATE_TEXT'] : '',
					}),
				});
			}
			case 'TEXT': {
				return this.formBuilder.group({
					TYPE: 'TEXT',
					SETTINGS: this.formBuilder.group({
						TEXT: defaultData ? defaultData.TEXT : '',
					}),
				});
			}
		}
	}

	public openSidenav(view: string) {
		this.sidenavView = view;
		this.sidenav.open();
	}

	public setImage(image) {
		const i = new Image();
		i.src = image.LOGO.FILE;
		i.onload = () => {
			const imageFormGroup = this.selectedElement.get('SETTINGS');
			imageFormGroup.get('FILE').setValue(image.LOGO.FILE);
			imageFormGroup.get('FILENAME').setValue(image.LOGO.FILENAME);
			imageFormGroup.get('ID').setValue(image.IMAGE_ID);
			imageFormGroup
				.get('ALTERNATE_TEXT')
				.setValue(image.LOGO.FILENAME.split('.')[0]);
			const maxWidth = this.getMaxImageWidth(this.selectedElement);
			if (i.width > maxWidth) {
				imageFormGroup.get('WIDTH').setValue(maxWidth);
			} else {
				imageFormGroup.get('WIDTH').setValue(i.width);
				imageFormGroup.get('HEIGHT').setValue(i.height);
			}
			this.sidenav.close();
		};
	}

	public editFooter() {
		this.footerSelected = true;
		this.selectedElement = null;
		this.selectedElementIndex = -1;
		this.selectedRowIndex = null;
		this.sidepaneView = 'EDIT_FOOTER';
	}

	public saveAddress() {
		if (this.footerForm.get('ADDRESS').valid) {
			this.footerForm.get('FULL_ADDRESS').setValue(this.getFullAddressString());
			this.sidenav.close();
		}
	}

	private getFullAddressString(): String {
		const addressObj = this.footerForm.get('ADDRESS').value;
		let fullAddressString = `${addressObj.COMPANY_NAME}, ${addressObj.ADDRESS_1}`;
		if (addressObj.ADDRESS_2 !== '') {
			fullAddressString += `, ${addressObj.ADDRESS_2}`;
		}
		fullAddressString += `, ${addressObj.CITY}, ${addressObj.STATE}`;

		if (addressObj.COUNTRY !== '') {
			fullAddressString += `, ${addressObj.COUNTRY}`;
		}
		if (addressObj.ZIP_CODE !== '') {
			fullAddressString += `-${addressObj.ZIP_CODE}`;
		}
		if (addressObj.PHONE !== '') {
			fullAddressString += `, ${addressObj.PHONE}`;
		}
		return fullAddressString;
	}

	private setFooter(footerObj) {
		const campaignFooterGroup = this.campaignForm.get('FOOTER') as FormGroup;
		const campaignFooterAddressGroup = campaignFooterGroup.get(
			'ADDRESS'
		) as FormGroup;
		const footerAddressGroup = footerObj['ADDRESS'];
		campaignFooterAddressGroup
			.get('COMPANY_NAME')
			.setValue(footerAddressGroup['COMPANY_NAME']);
		campaignFooterAddressGroup
			.get('ADDRESS_1')
			.setValue(footerAddressGroup['ADDRESS_1']);
		campaignFooterAddressGroup
			.get('ADDRESS_2')
			.setValue(footerAddressGroup['ADDRESS_2']);
		campaignFooterAddressGroup.get('CITY').setValue(footerAddressGroup['CITY']);
		campaignFooterAddressGroup
			.get('STATE')
			.setValue(footerAddressGroup['STATE']);
		campaignFooterAddressGroup
			.get('ZIP_CODE')
			.setValue(footerAddressGroup['ZIP_CODE']);
		campaignFooterAddressGroup
			.get('COUNTRY')
			.setValue(footerAddressGroup['COUNTRY']);
		campaignFooterAddressGroup
			.get('PHONE')
			.setValue(footerAddressGroup['PHONE']);
		if (footerAddressGroup['ADDRESS_1'] !== '') {
			campaignFooterGroup
				.get('FULL_ADDRESS')
				.setValue(this.getFullAddressString());
		}
		campaignFooterGroup.get('ALIGNMENT').setValue(footerObj['ALIGNMENT']);
	}

	public openRowDialog(rowIndex) {
		this.selectedLayoutIndex = null;
		const layout = this.availableLayouts.find(
			(layoutFound) => layoutFound.DISPLAY === '1'
		);
		if (this.campaignType === 'Plain') {
			this.addRow(layout, rowIndex);
		} else {
			this.newRowDialogRef = this.dialog.open(this.newRowDialog);
			this.newRowDialogRef.afterClosed().subscribe((val) => {
				if (val !== 'cancel') {
					this.addRow(val, rowIndex);
				}
			});
		}
	}

	private addRow(columnLayout, rowIndex): void {
		const rows = this.campaignForm.get('ROWS') as FormArray;
		const newRow = this.createRow(columnLayout.DISPLAY);
		const newRowColumns = newRow.get('COLUMNS') as FormArray;
		columnLayout.COLUMNS.forEach((column) => {
			newRowColumns.push(
				this.formBuilder.group({
					WIDTH: column.WIDTH,
					TYPE: column.TYPE,
				})
			);
		});
		rows.insert(rowIndex + 1, newRow);
		this.selectedRow = this.campaignType !== 'Plain' ? newRow : null;
		this.selectedRowIndex = this.campaignType !== 'Plain' ? rowIndex + 1 : null;
		this.reorganizeDroplist();
	}

	public moveRow(rowIndex: number, newIndex: number) {
		const rows = this.campaignForm.get('ROWS') as FormArray;
		if (newIndex >= rows.controls.length) {
			let k = newIndex - rows.controls.length + 1;
			while (k--) {
				rows.push(undefined);
			}
		}
		rows.controls.splice(newIndex, 0, rows.controls.splice(rowIndex, 1)[0]);
		this.reorganizeDroplist();
	}

	private reorganizeDroplist() {
		this.dropList = [];
		const rows = this.campaignForm.get('ROWS') as FormArray;
		rows.controls.forEach((columns: FormGroup, rowIndex) => {
			columns.value.COLUMNS.forEach((column, columnIndex) => {
				this.dropList.push(`${rowIndex}_${columnIndex}`);
			});
		});
	}

	public initializeEmailList() {
		this.emailListScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let page = 0;
					if (this.allEmailLists) {
						page = Math.ceil(this.allEmailLists.length / 10);
					}
					return this.emailListService
						.getAllEmailLists(page, 10, 'DATE_CREATED', 'Asc')
						.pipe(
							map((results: any) => {
								const newlist = this.filterNewEmailLists(
									results['EMAIL_LISTS']
								);
								if (newlist.length > 0) {
									this.allEmailLists = this.allEmailLists.concat(
										results.EMAIL_LISTS
									);
								}
								return results.EMAIL_LISTS;
							})
						);
				})
			)
			.subscribe();
	}

	public filterNewEmailLists(data) {
		const newArr = [];
		data.forEach((emailList) => {
			const existingEmailList = this.allEmailLists.find(
				(currentEmailList) =>
					currentEmailList.EMAIL_LIST_ID === emailList.EMAIL_LIST_ID
			);
			if (!existingEmailList) {
				newArr.push(emailList);
			}
		});
		return newArr;
	}

	public onEmailListScroll() {
		this.emailListScrollSubject.next(['', false]);
	}
}
