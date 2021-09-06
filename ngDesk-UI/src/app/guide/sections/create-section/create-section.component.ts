import { COMMA, ENTER } from '@angular/cdk/keycodes';
import {
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
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { FilterRuleOptionPipe } from '@src/app/custom-components/conditions/filter-rule-option/filter-rule-option.pipe';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { GuideService } from '@src/app/guide/guide.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { Subscription } from 'rxjs';
import { CompaniesService } from 'src/app/companies/companies.service';
import { LoaderService } from 'src/app/custom-components/loader/loader.service';
import { SectionApiService } from '@ngdesk/knowledgebase-api';

@Component({
	selector: 'app-create-section',
	templateUrl: './create-section.component.html',
	styleUrls: ['./create-section.component.scss'],
})
export class CreateSectionComponent implements OnInit, OnDestroy {
	@ViewChild('autoVisibleTo')
	public visibleToMatAutocomplete: MatAutocomplete;
	@ViewChild('visibleToInput')
	public visibleToInput: ElementRef<HTMLInputElement>;
	@ViewChild('visibleToChipList') public visibleToChipList;
	@ViewChild('autoManagedBy')
	public managedByMatAutocomplete: MatAutocomplete;
	@ViewChild('managedByInput')
	public managedByInput: ElementRef<HTMLInputElement>;
	@ViewChild('managedByChipList') public managedByChipList;
	public categories = [];
	public fields = [];
	public sectionForm: FormGroup;
	public sectionId: string;
	public section;
	public languages = [];
	public errorMessage: string;
	public errorParams = {
		name: {},
		sourceLanguage: {},
		category: {},
		sortBy: {},
		orderBy: {},
		visibleTo: {},
		managedBy: {},
	};
	public title = '';
	public visibleTo = [];
	public visibleToInitial = [];
	public managedBy = [];
	public managedByInitial = [];
	private allTeams = [];
	public visibleToCtrl = new FormControl();
	public managedByCtrl = new FormControl();
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	public isLoading = true;
	private companyInfoSubscription: Subscription;

	constructor(
		private formBuilder: FormBuilder,
		private route: ActivatedRoute,
		private router: Router,
		private translateService: TranslateService,
		private guideService: GuideService,
		private dialog: MatDialog,
		private cacheService: CacheService,
		private modulesService: ModulesService,
		private companiesService: CompaniesService,
		private loaderService: LoaderService,
		private sectionApiService: SectionApiService
	) {
		this.translateService.get('NAME').subscribe((val) => {
			this.errorParams.name = { field: val };
		});
		this.translateService.get('SOURCE_LANGUAGE').subscribe((val) => {
			this.errorParams.sourceLanguage = { field: val };
		});
		this.translateService.get('CATEGORY').subscribe((val) => {
			this.errorParams.category = { field: val };
		});
		this.translateService.get('SORT_BY').subscribe((val) => {
			this.errorParams.sortBy = { field: val };
		});
		this.translateService.get('ORDER_BY').subscribe((val) => {
			this.errorParams.orderBy = { field: val };
		});
		this.translateService.get('MANUALLY').subscribe((val) => {
			this.fields.push(val);
		});
		this.translateService.get('VISIBLE_TO').subscribe((val) => {
			this.errorParams.visibleTo = { field: val };
		});
		this.translateService.get('MANAGED_BY').subscribe((val) => {
			this.errorParams.managedBy = { field: val };
		});

		this.sectionId = this.route.snapshot.params['sectionId'];
		if (this.sectionId !== 'new') {
			this.title = 'EDIT_SECTION';
		} else {
			this.title = 'CREATE_SECTION';
		}

		this.languages = this.guideService.languageObject;

		// get list of categories
		this.guideService.getKbCategories().subscribe(
			(categoriesResponse: any) => {
				this.categories = categoriesResponse.DATA;
			},
			(categoriesError: any) => {
				this.errorMessage = categoriesError.error.ERROR;
			}
		);
	}

	public ngOnInit() {
		// get teams
		this.companyInfoSubscription =
			this.cacheService.companyInfoSubject.subscribe((dataStored) => {
				if (dataStored) {
					const teamsModule = this.cacheService.companyData['MODULES'].find(
						(module) => module['NAME'] === 'Teams'
					);
					this.modulesService.getEntries(teamsModule.MODULE_ID).subscribe(
						(teams: any) => {
							const sortedTeams = teams.DATA.sort((a, b) =>
								a.NAME.localeCompare(b.NAME)
							);
							this.visibleTo = sortedTeams;
							this.visibleToInitial = sortedTeams;
							this.managedBy = sortedTeams;
							this.managedByInitial = sortedTeams;
							this.allTeams = sortedTeams;

							this.sectionForm = this.formBuilder.group({
								name: ['', Validators.required],
								language: ['', Validators.required],
								category: ['', Validators.required],
								description: '',
								sortBy: ['', Validators.required],
								visibleTo: [[], Validators.required],
								managedBy: [[], Validators.required],
								isDraft: [false, Validators.required],
								sectionId: null,
							});
							// make get call if not new
							if (this.sectionId !== 'new') {
								this.guideService.getkbSections(this.sectionId).subscribe(
									(sectionResponse: any) => {
										// get changed category
										const category = this.categories.find(
											(categoryMatched) =>
												categoryMatched.categoryId ===
												sectionResponse.DATA['category']['categoryId']
										);

										this.visibleTo = this.transformObjects(
											this.convertToArr(category['visibleTo'], '_id'),
											this.allTeams,
											'DATA_ID'
										);
										this.visibleToInitial = this.transformObjects(
											this.convertToArr(category['visibleTo'], '_id'),
											this.allTeams,
											'DATA_ID'
										);
										this.section = sectionResponse;
										this.sectionForm
											.get('name')
											.setValue(sectionResponse['DATA'].name);
										this.sectionForm
											.get('language')
											.setValue(sectionResponse['DATA'].language);
										this.sectionForm
											.get('category')
											.setValue(sectionResponse['DATA'].category['categoryId']);
										this.sectionForm
											.get('description')
											.setValue(sectionResponse['DATA'].description);
										this.sectionForm
											.get('sortBy')
											.setValue(sectionResponse['DATA'].sortBy);
										this.sectionForm
											.get('sectionId')
											.setValue(sectionResponse['DATA'].sectionId);
										this.sectionForm
											.get('isDraft')
											.setValue(sectionResponse['DATA'].isDraft);
										this.sectionForm.get('visibleTo').setValue(
											this.transformObjects(
												this.convertToArr(
													sectionResponse['DATA'].visibleTo,
													'_id'
												),
												// sectionResponse['DATA'].visibleTo,
												this.allTeams,
												'DATA_ID'
											)
										);
										this.sectionForm.get('managedBy').setValue(
											this.transformObjects(
												this.convertToArr(
													sectionResponse['DATA'].managedBy,
													'_id'
												),
												//	sectionResponse['DATA'].managedBy,
												this.allTeams,
												'DATA_ID'
											)
										);

										this.onCategoryChange();
										this.onPermissionsChange();
										this.isLoading = false;
									},
									(sectionError: any) => {
										console.log(sectionError);
									}
								);
							} else {
								this.isLoading = false;
								this.onCategoryChange();
								this.onPermissionsChange();
								if (this.guideService.categoryId) {
									// pre filling the category
									this.sectionForm
										.get('category')
										.setValue(this.guideService.categoryId);
								}
							}
						},
						(error: any) => {
							this.errorMessage = error.error.ERROR;
							this.isLoading = false;
						}
					);
				}
			});
	}

	private onCategoryChange() {
		this.sectionForm.get('category').valueChanges.subscribe((val) => {
			// clear visible to
			this.sectionForm.get('visibleTo').setValue([]);
			// get changed category

			const category = this.categories.find(
				(categoryMatched) => categoryMatched.categoryId === val
			);

			this.visibleTo = this.transformObjects(
				this.convertToArr(category['visibleTo'], '_id'),
				this.allTeams,
				'DATA_ID'
			);
			this.visibleToInitial = this.transformObjects(
				this.convertToArr(category['visibleTo'], '_id'),
				this.allTeams,
				'DATA_ID'
			);
			// pre filling teams
			this.sectionForm.get('visibleTo').setValue(this.visibleTo);
		});
	}
	private convertToArr(input, key) {
		let output = [];
		for (var i = 0; i < input.length; ++i) {
			output.push(input[i][key]);
		}
		return output;
	}

	private onPermissionsChange() {
		this.sectionForm.get('visibleTo').valueChanges.subscribe((val) => {
			if (
				this.sectionForm.get('visibleTo').value.length === 0 &&
				this.sectionForm.get('visibleTo').dirty
			) {
				this.visibleToChipList.errorState = true;
			}
		});

		this.sectionForm.get('managedBy').valueChanges.subscribe((val) => {
			this.managedByChipList.errorState = false;
			if (
				this.sectionForm.get('managedBy').value.length === 0 &&
				this.sectionForm.get('managedBy').dirty
			) {
				this.managedByChipList.errorState = true;
			}
		});
	}

	// filtering array based on input event
	public filterInputValues(event, filteredArray, initialArray) {
		let input = event;
		// if has field ID it means the user slected from the dropdown and didnt
		// type it manually
		if (event.hasOwnProperty('NAME')) {
			input = event.NAME;
		}
		this[filteredArray] = new FilterRuleOptionPipe().transform(
			initialArray,
			input.toLowerCase(),
			'teams'
		);
	}

	// removing teams from array
	public remove(element, arrayName): void {
		const index = this.sectionForm.value[arrayName].indexOf(element);
		if (index >= 0) {
			const array = this.sectionForm.value[arrayName];
			array.splice(index, 1);
			this.sectionForm.get(arrayName).setValue(array);
		}
	}

	// If input doesn't match in dropdown, reseting the input field
	public resetInput(event: MatChipInputEvent): void {
		if (
			!this.visibleToMatAutocomplete.isOpen ||
			!this.managedByMatAutocomplete.isOpen
		) {
			const input = event.input;
			// Reset the input value
			if (input) {
				input.value = '';
			}
		}
	}

	public selected(event: MatAutocompleteSelectedEvent, type: string): void {
		switch (type) {
			case 'visibleTo': {
				const teams = this.sectionForm.value.visibleTo;
				teams.push(event.option.value);
				this.sectionForm.get('visibleTo').setValue(teams);
				this.visibleToInput.nativeElement.value = '';
				break;
			}
			case 'managedBy': {
				const teams = this.sectionForm.value.managedBy;
				teams.push(event.option.value);
				this.sectionForm.get('managedBy').setValue(teams);
				this.managedByInput.nativeElement.value = '';
				break;
			}
		}
	}

	// replace Ids -> Objects
	private transformObjects(arr, initialArray, key): any[] {
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

	public deleteSection() {
		let dialogMessage = '';
		this.translateService
			.get('ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS', {
				value: this.translateService.instant('SECTION').toLowerCase(),
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
				this.sectionApiService.deleteSection(this.sectionId).subscribe(
					(deleteResponse: any) => {
						this.router.navigate([
							`guide/categories/${this.section['DATA'].category['categoryId']}/detail`,
						]);
					},
					(error: any) => {
						console.log(error);
					}
				);
			}
		});
	}

	public save() {
		this.visibleToChipList.errorState = false;
		this.managedByChipList.errorState = false;
		if (this.sectionForm.valid) {
			const section = JSON.parse(JSON.stringify(this.sectionForm.value));

			section.visibleTo = this.transformIds(section.visibleTo, 'DATA_ID');
			section.managedBy = this.transformIds(section.managedBy, 'DATA_ID');

			if (this.sectionId !== 'new') {
				//	call put

				this.sectionApiService.updateSection(section).subscribe(
					(putSectionResponse: any) => {
						// need success message
						this.router.navigate([
							`guide/sections/${putSectionResponse.sectionId}/detail`,
						]);
					},
					(putSectionError: any) => {
						this.errorMessage = putSectionError.error.ERROR;
					}
				);
			} else {
				// delete section['sectionId'];

				this.sectionApiService.postSection(section).subscribe(
					(postSectionResponse: any) => {
						console.log(
							'postSectionResponse---------------->',
							postSectionResponse
						);
						this.companiesService.trackEvent(
							`Added a new Section ${section.name}`
						);
						// need success message
						console.log(postSectionResponse);
						this.router.navigate([
							`guide/sections/${postSectionResponse.sectionId}/detail`,
						]);
					},
					(postSectionError: any) => {
						console.log(postSectionError);
						this.errorMessage = postSectionError.error.ERROR;
					}
				);
			}
		} else {
			this.loaderService.isLoading = false;
			if (this.sectionForm.get('visibleTo').value.length === 0) {
				this.visibleToChipList.errorState = true;
			}

			if (this.sectionForm.get('managedBy').value.length === 0) {
				this.managedByChipList.errorState = true;
			}
		}
	}

	public ngOnDestroy() {
		this.guideService.categoryId = undefined;

		if (this.companyInfoSubscription) {
			this.companyInfoSubscription.unsubscribe();
		}
	}
}
