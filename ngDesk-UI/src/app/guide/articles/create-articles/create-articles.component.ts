import { COMMA, ENTER } from '@angular/cdk/keycodes';
import {
	ChangeDetectorRef,
	Component,
	ElementRef,
	OnDestroy,
	OnInit,
	ViewChild,
} from '@angular/core';
import {
	FormBuilder,
	FormControl,
	FormGroup,
	Validators,
} from '@angular/forms';
import {
	MatAutocomplete,
	MatAutocompleteSelectedEvent,
} from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { RolesService } from '../../../company-settings/roles/roles-old.service';
import { FilterRuleOptionPipe } from '../../../custom-components/conditions/filter-rule-option/filter-rule-option.pipe';
import { ConfirmDialogComponent } from '../../../dialogs/confirm-dialog/confirm-dialog.component';
import { ModulesService } from '../../../modules/modules.service';
import { config } from '../../../tiny-mce/tiny-mce-config';
import { UsersService } from '../../../users/users.service';
import { GuideService } from '../../guide.service';
import { CompaniesService } from 'src/app/companies/companies.service';
import { CacheService } from '@src/app/cache.service';
import { Subscription } from 'rxjs';
import { ArticlesDataService } from '../articles-data.service';

@Component({
	selector: 'app-create-articles',
	templateUrl: './create-articles.component.html',
	styleUrls: ['./create-articles.component.scss'],
})
export class CreateArticlesComponent implements OnInit, OnDestroy {
	@ViewChild('auto') public matAutocomplete: MatAutocomplete;
	@ViewChild('teamInput') public teamInput: ElementRef<HTMLInputElement>;
	@ViewChild('teamChipList') public teamChipList;
	public articleForm: FormGroup;
	public roles = [];
	public teams = [];
	public teamsInitial = [];
	private allTeams = [];
	public categories = [];
	public errorParams = {
		sourceLanguage: {},
		teams: {},
		category: {},
		author: {},
		section: {},
		visibleTo: {},
		managedBy: {},
		body: {},
		title: {},
	};
	public teamCtrl = new FormControl();
	public config = config;
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	public languages = [
		{ NAME: this.translateService.instant('ARABIC'), CODE: 'ar' },
		{ NAME: this.translateService.instant('CHINESE'), CODE: 'zh' },
		{ NAME: this.translateService.instant('GERMAN'), CODE: 'de' },
		{ NAME: this.translateService.instant('ENGLISH'), CODE: 'en' },
		{ NAME: this.translateService.instant('FRENCH'), CODE: 'fr' },
		{ NAME: this.translateService.instant('GREEK'), CODE: 'el' },
		{ NAME: this.translateService.instant('HINDI'), CODE: 'hi' },
		{ NAME: this.translateService.instant('ITALIAN'), CODE: 'it' },
		{ NAME: this.translateService.instant('MALAY'), CODE: 'ms' },
		{ NAME: this.translateService.instant('PORTUGUESE'), CODE: 'pt' },
		{ NAME: this.translateService.instant('RUSSION'), CODE: 'ru' },
		{ NAME: this.translateService.instant('SPANISH'), CODE: 'es' },
	];
	public pageSize :number = 10;
	private articleId: string;
	public errorMessage: string;
	public successMessage: string;
	public isFormCreate = true;
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	private fileSize = 0;
	private filesArray = [];
	private companyInfoSubscription: Subscription;
	public tempAuthorInput
	public tempTeamInput = ""

	constructor(
		private route: ActivatedRoute,
		private formBuilder: FormBuilder,
		private modulesService: ModulesService,
		private rolesService: RolesService,
		private translateService: TranslateService,
		private usersService: UsersService,
		private guideService: GuideService,
		private router: Router,
		private dialog: MatDialog,
		private cd: ChangeDetectorRef,
		private companiesService: CompaniesService,
		private cacheService: CacheService,
		private articlesData:ArticlesDataService,
	) {
		this.config['height'] = 550;

		this.translateService.get('SOURCE_LANGUAGE').subscribe((res: string) => {
			this.errorParams['sourceLanguage']['field'] = res;
		});

		this.translateService.get('TEAMS').subscribe((res: string) => {
			this.errorParams['teams']['field'] = res;
		});

		this.translateService.get('CATEGORY').subscribe((res: string) => {
			this.errorParams['category']['field'] = res;
		});

		this.translateService.get('AUTHOR').subscribe((res: string) => {
			this.errorParams['author']['field'] = res;
		});

		this.translateService.get('SECTION').subscribe((res: string) => {
			this.errorParams['section']['field'] = res;
		});

		this.translateService.get('VISIBLE_TO').subscribe((res: string) => {
			this.errorParams['visibleTo']['field'] = res;
		});

		this.translateService.get('BODY').subscribe((res: string) => {
			this.errorParams['body']['field'] = res;
		});

		this.translateService.get('TITLE').subscribe((res: string) => {
			this.errorParams['title']['field'] = res;
		});
	}

	public  ngOnInit() {
		this.route.params.subscribe((params) => {
			this.isFormCreate = false;
			this.articleId = this.route.snapshot.params['articleId'];
			// get users
			this.companyInfoSubscription = this.cacheService.companyInfoSubject.subscribe(
				(dataStored) => {
					if (dataStored) {
							// get teams
							const modules: any[] = this.cacheService.companyData['MODULES'];
							const teamsModule = modules.find(
								(module) => module.NAME === 'Teams'
							);
							this.modulesService.getEntries(teamsModule.MODULE_ID).subscribe(
								(teams: any) => {
									this.teams = teams.DATA;
									this.teamsInitial = teams.DATA.slice();
									this.allTeams = teams.DATA.slice();	
									if (this.articleId !== 'new') {
										this.guideService.getArticle(this.articleId).subscribe(
											(articleResponse: any) => {
												this.guideService
													.getSectionById(articleResponse.SECTION)
													.subscribe(
														(sectionResponse: any) => {
															this.teams = this.transformObjects(
																sectionResponse.VISIBLE_TO,
																this.allTeams,
																'DATA_ID'
															);
															this.teamsInitial = this.transformObjects(
																sectionResponse.VISIBLE_TO,
																this.allTeams,
																'DATA_ID'
															);
															this.articleForm = this.formBuilder.group({
																ARTICLE_ID: [articleResponse.ARTICLE_ID],
																TITLE: [
																	articleResponse.TITLE,
																	Validators.required,
																],
																BODY: [
																	articleResponse.BODY,
																	Validators.required,
																],
																PUBLISH: [
																	articleResponse.PUBLISH,
																	Validators.required,
																],
																SOURCE_LANGUAGE: [
																	articleResponse.SOURCE_LANGUAGE,
																	Validators.required,
																],
																VISIBLE_TO: [
																	this.transformObjects(
																		articleResponse.VISIBLE_TO,
																		this.teamsInitial,
																		'DATA_ID'
																	),
																	Validators.required,
																],
																SECTION: [
																	articleResponse.SECTION,
																	Validators.required,
																],
																OPEN_FOR_COMMENTS: [
																	articleResponse.OPEN_FOR_COMMENTS,
																	Validators.required,
																],
																AUTHOR: [
																	JSON.parse(articleResponse.AUTHOR),
																	Validators.required,
																],
																LABELS: [articleResponse.LABELS],
																COMMENTS: [articleResponse.COMMENTS],
																CREATED_BY: [articleResponse.CREATED_BY],
																DATE_CREATED: [articleResponse.DATE_CREATED],
																DATE_UPDATED: [articleResponse.DATE_UPDATED],
																LAST_UPDATED_BY: [
																	articleResponse.LAST_UPDATED_BY,
																],
																ORDER: [articleResponse.ORDER],
																ATTACHMENTS: [
																	articleResponse.ATTACHMENTS || [],
																],
															});
															this.onChangeOfSection();
															this.isFormCreate = true;
														},
														(sectionError: any) => {
															this.errorMessage = sectionError.error.ERROR;
														}
													);
											},
											(error: any) => {
												this.errorMessage = error.error.ERROR;
											}
										);
									} else {
										this.articleForm = this.formBuilder.group({
											TITLE: ['', Validators.required],
											BODY: ['', Validators.required],
											PUBLISH: [true, Validators.required],
											SOURCE_LANGUAGE: ['en', Validators.required],
											VISIBLE_TO: [[], Validators.required],
											SECTION: ['', Validators.required],
											OPEN_FOR_COMMENTS: [false, Validators.required],
											AUTHOR: [this.usersService.user, Validators.required],
											LABELS: [[]],
											COMMENTS: [[]],
											ATTACHMENTS: [[]],
										});
										this.isFormCreate = true;
										this.onChangeOfSection();
										if (this.guideService.sectionId) {
											// pre filling the section
											this.articleForm
												.get('SECTION')
												.setValue(this.guideService.sectionId);
										}
									}
								},
								(error: any) => {
									this.errorMessage = error.error.ERROR;
								}
							);
					}
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);

			// get sections with categoreis
			this.guideService.getCategories().subscribe(
				(categoriesResponse: any) => {
					this.categories = categoriesResponse.DATA;
					this.categories.forEach((category) => {
						this.guideService.getSections(category.CATEGORY_ID).subscribe(
							(sectionsResponse: any) => {
								category.SECTIONS = sectionsResponse.DATA;
							},
							(error: any) => {
								this.errorMessage = error.error.ERROR;
							}
						);
					});
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
		});

			//to intialize subscptions for authors and teams 
			this.articlesData.initializeAuthors();
			this.articlesData.intitalizeVisibleTo();
	}

	private onChangeOfSection() {
		// re-populate visible to team options
		// only display list of teams that have visibility in section
		this.articleForm.get('SECTION').valueChanges.subscribe((val) => {
			// must clear list of visible to teams selected if section changes
			// in case team was available in the previous section, but not new section
			this.articleForm.get('VISIBLE_TO').setValue([]);
			// get section
			this.guideService.getSectionById(val).subscribe(
				(sectionResponse: any) => {
					this.teams = this.transformObjects(
						sectionResponse.VISIBLE_TO,
						this.allTeams,
						'DATA_ID'
					);
					this.teamsInitial = this.transformObjects(
						sectionResponse.VISIBLE_TO,
						this.allTeams,
						'DATA_ID'
					);

					// pre filling the first team
					this.articleForm.get('VISIBLE_TO').setValue(this.teams);
				},
				(sectionError: any) => {
					this.errorMessage = sectionError.error.ERROR;
				}
			);
		});
	}

	// filtering array based on ngModelChange
	public filterInputValues(event) {
		this.tempAuthorInput = event;

		this.articlesData.authorSubjects.next([
			this.tempAuthorInput,
			true,
		]);
	}

	public filterTeams() {
		this.articlesData.teamsSubject.next([
			this.tempTeamInput,
			true,
		]);
	}

	public displayConditionFn(field: any): string | undefined {
		if(field && field.FIRST_NAME){
			return field ? field.FIRST_NAME + ' ' + field.LAST_NAME : undefined;
		}else if(field && field?.CONTACT?.PRIMARY_DISPLAY_FIELD){
			return field.CONTACT.PRIMARY_DISPLAY_FIELD
		}
	}

	public add(event: MatChipInputEvent): void {
		const input = event.input;
		const value = event.value;

		// Add our fruit
		if ((value || '').trim()) {
			const labels = this.articleForm.value.LABELS;
			labels.push(value.trim());
			this.articleForm.get('LABELS').setValue(labels);
		}

		// Reset the input value
		if (input) {
			input.value = '';
		}
	}

	// removing teams from array
	public remove(element, arrayName): void {
		const index = this.articleForm.value[arrayName].indexOf(element);

		if (index >= 0) {
			const array = this.articleForm.value[arrayName];
			array.splice(index, 1);
			this.articleForm.get(arrayName).setValue(array);
		}
	}

	// If input doesn't match in dropdown, reseting the input field
	public resetInput(event: MatChipInputEvent): void {
		if (!this.matAutocomplete.isOpen) {
			const input = event.input;
			// Reset the input value
			if (input) {
				input.value = '';
			}
		}
	}

	public selected(event: MatAutocompleteSelectedEvent): void {
		let isContains:boolean = false
		const teams = this.articleForm.value.VISIBLE_TO;
		if(teams && teams.length>0){
			teams.map((item)=>{
				if(item.NAME ==  event.option.value.NAME){
				isContains = true;
				}
			})
		}
		if(isContains ==false){
			teams.push(event.option.value);
		}
		this.articleForm.get('VISIBLE_TO').setValue(teams);
		this.teamInput.nativeElement.value = '';
		this.tempTeamInput = "";
	}

	// replace Ids -> Objects
	private transformObjects(arr, initialArray, key) {
		const arrWithObjects = [];
		for (const id of arr) {
			for (const obj of initialArray) {
				if (obj[key] === id) {
					arrWithObjects.push(obj);
				}
			}
		}
		return arrWithObjects;
	}

	// transform Objects -> Ids
	private transformIds(arr, key) {
		const arrWithIds = [];
		for (const obj of arr) {
			arrWithIds.push(obj[key]);
		}
		return arrWithIds;
	}

	// adds attachment
	public onFileChange(event) {
		if (event.target.files && event.target.files.length) {
			const reader = new FileReader();
			const [file] = event.target.files;
			reader.readAsDataURL(file);
			this.fileSize += file.size;
			if (this.fileSize > 9500000) {
				// No need to choke complete 10mb, left some breathing space. Hence, 9.5mb
				this.fileSize -= file.size;
				this.errorMessage = 'File size cannot be greater than 10mb';
			} else {
				this.errorMessage = '';
				reader.onload = () => {
					const data: any = reader.result;
					this.filesArray.push({ fileSize: file.size });
					this.articleForm.get('ATTACHMENTS').value.push({
						FILE_NAME: file.name,
						FILE: data.split('base64,')[1],
					});
					// need to run CD since file load runs outside of zone
					this.cd.markForCheck();
				};
			}
		}
	}

	public removeFile(index: number): void {
		this.articleForm.get('ATTACHMENTS').value.splice(index, 1);
		this.fileSize -= this.filesArray[index].fileSize;
	}

	public save() {
		this.teamChipList.errorState = false;
		if (this.articleForm.valid) {
			const articleObj = JSON.parse(JSON.stringify(this.articleForm.value));
			articleObj['AUTHOR'] = articleObj['AUTHOR'].DATA_ID;
			articleObj['VISIBLE_TO'] = this.transformIds(
				articleObj['VISIBLE_TO'],
				'DATA_ID'
			);
			if (this.articleId === 'new') {
				this.guideService.postArticle(articleObj).subscribe(
					(article: any) => {
						this.companiesService.trackEvent('Added a new article');
						this.navigateTo(article, 'post');
					},
					(error: any) => {
						this.errorMessage = error.error.ERROR;
					}
				);
			} else {
				this.guideService.putArticle(articleObj).subscribe(
					(article: any) => {
						this.navigateTo(article, 'put');
					},
					(error: any) => {
						this.errorMessage = error.error.ERROR;
					}
				);
			}
		} else if (this.articleForm.get('VISIBLE_TO').value.length === 0) {
			this.teamChipList.errorState = true;
		}
	}

	public deleteArticle() {
		let dialogMessage = '';
		this.translateService
			.get('ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS', {
				value: this.translateService.instant('ARTICLE').toLowerCase(),
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
				this.guideService.deleteArticle(this.articleId).subscribe(
					(response: any) => {
						this.router.navigate([`guide`]);
					},
					(error: any) => {
						this.errorMessage = error.error.ERROR;
					}
				);
			}
		});
	}

	private navigateTo(article, requestType) {
		if (article.PUBLISH) {
			this.router.navigate([
				'guide',
				'articles',
				article.SECTION,
				article.TITLE,
			]);
		} else {
			if (requestType === 'post') {
				this.router.navigate([`guide/articles/detail/${article.ARTICLE_ID}`]);
			}
			this.successMessage = this.translateService.instant('SAVED_SUCCESSFULLY');
		}
	}

	public ngOnDestroy() {
		this.guideService.sectionId = undefined;

		if (this.companyInfoSubscription) {
			this.companyInfoSubscription.unsubscribe();
		}
	}

		// to call API  on scroll of Author list 
	public onUsersScroll() {
		this.articlesData.authorSubjects.next([
			this.tempAuthorInput,
			false,
		])	
	}
		// to call API  on scroll of Team list 
	public onTeamScroll() {
		this.articlesData.teamsSubject.next([
			this.tempTeamInput,
			false,
		])	
	
	}
	
}
