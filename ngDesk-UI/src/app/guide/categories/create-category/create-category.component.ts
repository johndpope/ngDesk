import { COMMA, ENTER } from '@angular/cdk/keycodes';
import {
	Component,
	ElementRef,
	OnInit,
	ViewChild,
	OnDestroy,
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

import { Subscription } from 'rxjs';
import { CompaniesService } from 'src/app/companies/companies.service';
import { LoaderService } from 'src/app/custom-components/loader/loader.service';
import { FilterRuleOptionPipe } from '../../../custom-components/conditions/filter-rule-option/filter-rule-option.pipe';
import { ConfirmDialogComponent } from '../../../dialogs/confirm-dialog/confirm-dialog.component';
import { ModulesService } from '../../../modules/modules.service';
import { GuideService } from '../../guide.service';
import { CacheService } from '@src/app/cache.service';
import { CategoryApiService } from '@ngdesk/knowledgebase-api';
@Component({
	selector: 'app-create-category',
	templateUrl: './create-category.component.html',
	styleUrls: ['./create-category.component.scss'],
})
export class CreateCategoryComponent implements OnInit, OnDestroy {
	@ViewChild('autoTeam')
	public matAutocomplete: MatAutocomplete;
	@ViewChild('teamInput') public teamInput: ElementRef<HTMLInputElement>;
	@ViewChild('teamChipList') public teamChipList;

	public categoryForm: FormGroup;
	public categoryId: string;
	public languages = [];
	public errorMessage: string;
	public errorParams = {
		name: {},
		sourceLanguage: {},
		visibleTo: {},
	};
	public title = '';
	public teams = [];

	public teamsInitial = [];
	public isLoading = true;
	public teamCtrl = new FormControl();
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	public companyInfoSubscription: Subscription;

	constructor(
		private formBuilder: FormBuilder,
		private router: Router,
		private route: ActivatedRoute,
		private translateService: TranslateService,
		private guideService: GuideService,
		private dialog: MatDialog,
		private loaderService: LoaderService,
		private modulesService: ModulesService,
		private companiesService: CompaniesService,
		private cacheService: CacheService,
		private categoryApiService: CategoryApiService
	) {
		this.translateService.get('NAME').subscribe((val) => {
			this.errorParams.name = { field: val };
		});
		this.translateService.get('SOURCE_LANGUAGE').subscribe((val) => {
			this.errorParams.sourceLanguage = { field: val };
		});
		this.translateService.get('VISIBLE_TO').subscribe((val: string) => {
			this.errorParams.visibleTo = { field: val };
		});

		this.categoryId = this.route.snapshot.params['categoryId'];
		if (this.categoryId !== 'new') {
			this.title = 'EDIT_CATEGORY';
		} else {
			this.title = 'CREATE_CATEGORY';
		}

		this.languages = this.guideService.languageObject;
	}

	public ngOnInit() {
		// get teams
		this.companyInfoSubscription =
			this.cacheService.companyInfoSubject.subscribe(
				(dataStored) => {
					if (dataStored) {
						const modules: any[] = this.cacheService.companyData['MODULES'];

						// Get teams module from the list of modules.
						const teamsModule = modules.find(
							(module) => module.NAME === 'Teams'
						);
						this.modulesService.getEntries(teamsModule.MODULE_ID).subscribe(
							(teams: any) => {
								this.teams = teams.DATA.sort((a, b) =>
									a.NAME.localeCompare(b.NAME)
								);
								this.teamsInitial = teams.DATA.sort((a, b) =>
									a.NAME.localeCompare(b.NAME)
								);

								this.categoryForm = this.formBuilder.group({
									name: ['', Validators.required],
									sourceLanguage: ['', Validators.required],
									isDraft: [false, Validators.required],
									visibleTo: [[], Validators.required],
									description: '',
									categoryId: null,
								});

								// make get call if not new
								if (this.categoryId !== 'new') {
									this.guideService
										.getKbCategoryById(this.categoryId)
										.subscribe(
											(categoryResponse: any) => {
												this.categoryForm
													.get('name')
													.setValue(categoryResponse['DATA'].name);
												this.categoryForm
													.get('sourceLanguage')
													.setValue(categoryResponse['DATA'].sourceLanguage);
												this.categoryForm
													.get('isDraft')
													.setValue(categoryResponse['DATA'].isDraft);
												this.categoryForm
													.get('description')
													.setValue(categoryResponse['DATA'].description);
												this.categoryForm
													.get('categoryId')
													.setValue(categoryResponse['DATA'].categoryId);
												this.categoryForm
													.get('visibleTo')
													.setValue(
														this.transformObjects(
															this.convertToArr(
																categoryResponse['DATA'].visibleTo,
																'_id'
															),
															this.teamsInitial,
															'DATA_ID'
														)
													);
												this.onVisibleToChange();
												this.isLoading = false;
											},
											(categoryError: any) => {
												console.log(categoryError);
											}
										);
								} else {
									this.onVisibleToChange();
									this.isLoading = false;
								}
							},
							(error: any) => {
								this.errorMessage = error.error.ERROR;
								this.isLoading = false;
							}
						);
					}
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
					this.isLoading = false;
				}
			);
	}

	private convertToArr(input, key) {
		let output = [];
		for (var i = 0; i < input.length; ++i) {
			output.push(input[i][key]);
		}
		return output;
	}

	private onVisibleToChange() {
		this.categoryForm.get('visibleTo').valueChanges.subscribe((val) => {
			this.teamChipList.errorState = false;
			if (this.categoryForm.get('visibleTo').value.length === 0) {
				this.teamChipList.errorState = true;
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
			filteredArray
		);
	}

	// removing teams from array
	public remove(element, arrayName): void {
		const index = this.categoryForm.value[arrayName].indexOf(element);

		if (index >= 0) {
			const array = this.categoryForm.value[arrayName];
			array.splice(index, 1);
			this.categoryForm.get(arrayName).setValue(array);
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
		const teams = this.categoryForm.value.visibleTo;
		teams.push(event.option.value);
		this.categoryForm.get('visibleTo').setValue(teams);
		this.teamInput.nativeElement.value = '';
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

	public deleteCategory() {
		let dialogMessage = '';
		this.translateService
			.get('ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS', {
				value: this.translateService.instant('CATEGORY').toLowerCase(),
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
				this.categoryApiService.deleteCategory(this.categoryId).subscribe(
					(deleteResponse: any) => {
						this.router.navigate([`guide`]);
					},
					(error: any) => {
						console.log(error);
					}
				);
			}
		});
	}

	public save() {
		this.teamChipList.errorState = false;
		if (this.categoryForm.valid) {
			const category = JSON.parse(JSON.stringify(this.categoryForm.value));
			category.visibleTo = this.transformIds(category.visibleTo, 'DATA_ID');
			if (this.categoryId !== 'new') {
				// call put
				this.categoryApiService.putCategory(category).subscribe(
					(putCategoryResponse: any) => {
						this.router.navigate([
							`guide/categories/${putCategoryResponse.categoryId}/detail`,
						]);
					},
					(putCategoryError: any) => {
						this.errorMessage = putCategoryError.error.ERROR;
					}
				);
			} else {
				// call post
				this.categoryApiService.postCategory(category).subscribe(
					(postCategoryResponse: any) => {
						this.companiesService.trackEvent(
							`Added a new category ${category.name}`
						);
						this.router.navigate([
							`guide/categories/${postCategoryResponse.categoryId}/detail`,
						]);
					},
					(postCategoryError: any) => {
						this.errorMessage = postCategoryError.error.ERROR;
					}
				);
			}
		} else {
			this.loaderService.isLoading = false;
			if (this.categoryForm.get('visibleTo').value.length === 0) {
				this.teamChipList.errorState = true;
			}
		}
	}

	public ngOnDestroy() {
		if (this.companyInfoSubscription) {
			this.companyInfoSubscription.unsubscribe();
		}
	}
}
