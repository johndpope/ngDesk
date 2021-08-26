import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
	NormalizationRule,
	NormalizationRuleApiService,
	Rule,
} from '@ngdesk/sam-api';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { NormalizationRulesService } from '@src/app/company-settings/normalization-rule/normalization-rules.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { Subject, Subscription } from 'rxjs';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	switchMap,
} from 'rxjs/operators';

@Component({
	selector: 'app-normalization-rule-detail',
	templateUrl: './normalization-rule-detail.component.html',
	styleUrls: ['./normalization-rule-detail.component.scss'],
})
export class NormalizationRuleDetailComponent implements OnInit {
	public normalizationRuleForm: FormGroup;
	public productForm: FormGroup;
	public publisherForm: FormGroup;
	public versionForm: FormGroup;
	public allOperators = [];
	public allProducts = [];
	public allPublishers = [];
	public allVersions = [];
	public normalizationRuleId;
	public params;
	public companyInfoSubscription: Subscription;
	public formControls = {};
	public allRuleKeys = ['products', 'publisher', 'version'];
	public normalizationRuleScrollSubject = new Subject<any>();
	public publisher: Rule = {
		key: '',
		operator: '',
		value: '',
	} as Rule;
	public product: Rule = {
		key: '',
		operator: '',
		value: '',
	} as Rule;
	public version: Rule = {
		key: '',
		operator: '',
		value: '',
	} as Rule;
	public normalizationRule: NormalizationRule = {
		name: '',
		description: '',
		publisher: '',
		product: '',
		version: '',
	} as NormalizationRule;

	constructor(
		private formBuilder: FormBuilder,
		private normalizationRulesService: NormalizationRulesService,
		private route: ActivatedRoute,
		private router: Router,
		private translateService: TranslateService,
		private normalizationApiService: NormalizationRuleApiService,
		private bannerMessageService: BannerMessageService,
		private cacheService: CacheService,
		private loaderService: LoaderService
	) {}

	public ngOnInit() {
		// Get the id from the url.
		this.normalizationRuleId = this.route.snapshot.params['id'];

		// It will initialize the formgroup.
		this.initializeForms();

		this.allOperators = this.normalizationRulesService.getOperators();

		this.companyInfoSubscription = this.cacheService.companyInfoSubject.subscribe(
			(dataStored) => {
				if (dataStored) {
					this.normalizationRulesService
						.getNormalizationDetails('', 0, 10, 'NAME', 'asc', false, '')
						.subscribe(
							(normalizationDataResponse: any) => {
								this.initializeNormalizationRuleScrollSubject();
								this.allProducts = normalizationDataResponse[0];
								this.allPublishers = normalizationDataResponse[1];
								this.allVersions = normalizationDataResponse[2];
								if (this.normalizationRuleId !== 'new') {
									this.getNormalizationRule(this.normalizationRuleId);
								}
							},
							(error: any) => {
								this.bannerMessageService.errorNotifications.push({
									message: this.translateService.instant(error.error.ERROR),
								});
							}
						);
				}
			}
		);
	}

	// This form will initialize the normalization rule form.
	public initializeForms() {
		this.normalizationRuleForm = this.formBuilder.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
		});
		this.productForm = this.formBuilder.group({
			key: ['', [Validators.required]],
			value: ['', [Validators.required]],
			operator: ['Is', [Validators.required]],
		});
		this.publisherForm = this.formBuilder.group({
			key: ['', [Validators.required]],
			value: ['', [Validators.required]],
			operator: ['Is'],
		});
		this.versionForm = this.formBuilder.group({
			key: ['', [Validators.required]],
			value: ['', [Validators.required]],
			operator: ['Is'],
		});
		this.params = {
			name: { field: this.translateService.instant('NAME') },
		};
	}

	// Fetch the same normalization rule based on the id and set the properties of form.
	public getNormalizationRule(id) {
		this.normalizationRulesService.getNormalizationRule(id).subscribe(
			(normalizedRuleResponse: any) => {
				if (normalizedRuleResponse) {
					const normalizationRule = normalizedRuleResponse['normalizationRule'];
					this.normalizationRuleForm.controls['NAME'].setValue(
						normalizationRule.name
					);
					this.normalizationRuleForm.controls['DESCRIPTION'].setValue(
						normalizationRule.description
					);
					if (normalizationRule.publisher) {
						this.loadForms('publisherForm', 'publisher', normalizationRule);
					}
					if (normalizationRule.product) {
						this.loadForms('productForm', 'product', normalizationRule);
					}
					if (normalizationRule.version) {
						this.loadForms('versionForm', 'version', normalizationRule);
					}
				}
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant(error.error.ERROR),
				});
			}
		);
	}

	public loadForms(formType, ruleType, normalizationRule) {
		this[formType].controls['key'].setValue(normalizationRule[ruleType].key);
		this[formType].controls['operator'].setValue(
			normalizationRule[ruleType].operator
		);
		this[formType].controls['value'].setValue(
			normalizationRule[ruleType].value
		);
	}

	public initializeNormalizeRuleObjects() {
		this.product = {
			key: this.productForm.value['key'],
			operator: this.productForm.value['operator'],
			value: this.productForm.value['value'],
		};
		this.publisher = {
			key: this.publisherForm.value['key'],
			operator: this.publisherForm.value['operator'],
			value: this.publisherForm.value['value'],
		};
		this.version = {
			key: this.versionForm.value['key'],
			operator: this.versionForm.value['operator'],
			value: this.versionForm.value['value'],
		};
		this.normalizationRule = {
			publisher: this.publisher,
			product: this.product,
			version: this.version,
		};
	}

	// Calls on click of save button
	public save() {
		this.initializeNormalizeRuleObjects();
		if (
			this.publisherForm.valid ||
			this.productForm.valid ||
			this.versionForm.valid
		) {
			this.normalizationRule.name = this.normalizationRuleForm.value['NAME'];
			this.normalizationRule.description = this.normalizationRuleForm.value[
				'DESCRIPTION'
			];
			this.publisherForm.valid
				? (this.normalizationRule.publisher = this.publisher)
				: (this.normalizationRule.publisher = null);
			this.productForm.valid
				? (this.normalizationRule.product = this.product)
				: (this.normalizationRule.product = null);
			this.versionForm.valid
				? (this.normalizationRule.version = this.version)
				: (this.normalizationRule.version = null);

			if (this.normalizationRuleId === 'new') {
				this.normalizationApiService
					.postNormalizationRule(this.normalizationRule)
					.subscribe(
						(normalizedRuleResponse: any) => {
							if (normalizedRuleResponse) {
								this.router.navigate([`company-settings/normalization-rules`]);
								this.bannerMessageService.successNotifications.push({
									message: this.translateService.instant('SAVED_SUCCESSFULLY'),
								});
							}
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: this.translateService.instant(error.error.ERROR),
							});
						}
					);
			} else {
				this.normalizationRule.normalizationRuleId = this.normalizationRuleId;
				this.normalizationApiService
					.putNormalizationRule(this.normalizationRule)
					.subscribe(
						(normalizedRuleResponse: any) => {
							if (normalizedRuleResponse) {
								this.router.navigate([`company-settings/normalization-rules`]);
								this.bannerMessageService.successNotifications.push({
									message: this.translateService.instant(
										'UPDATED_SUCCESSFULLY'
									),
								});
							}
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: this.translateService.instant(error.error.ERROR),
							});
						}
					);
			}
		} else {
			this.loaderService.isLoading = false;
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('SET_ANY_NORMALIZATION_RULE'),
			});
		}
	}

	public initializeNormalizationRuleScrollSubject() {
		this.normalizationRuleScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search, type]) => {
					let searchValue = null;
					if (value !== '') {
						searchValue = 'NAME' + '=' + value;
					}
					let page = 0;
					let list = [];
					let sortBy = '';
					if (type === 'products') {
						list = this.allProducts;
						sortBy = 'NAME';
					} else if (type === 'publishers') {
						list = this.allPublishers;
						sortBy = 'PUBLISHER';
					} else {
						list = this.allVersions;
						sortBy = 'VERSION';
					}
					if (list && !search) {
						page = Math.ceil(list.length / 10);
					}
					return this.normalizationRulesService
						.getNormalizationDetails(type, page, 10, sortBy, 'asc', true, '')
						.pipe(
							map((results: any) => {
								if (type === 'products') {
									if (results && results.length > 0) {
										this.allProducts = this.allProducts.concat(results);
									}
								} else if (type === 'publishers') {
									if (results && results.length > 0) {
										this.allPublishers = this.allPublishers.concat(results);
									}
								} else {
									if (results && results.length > 0) {
										this.allVersions = this.allVersions.concat(results);
									}
								}
								return results;
							})
						);
				})
			)
			.subscribe();
	}

	public onScroll(type) {
		this.normalizationRuleScrollSubject.next(['', '', type]);
	}
}
