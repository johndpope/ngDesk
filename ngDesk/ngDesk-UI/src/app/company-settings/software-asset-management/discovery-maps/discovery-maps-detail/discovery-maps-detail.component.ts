import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DataApiService } from '@ngdesk/data-api';
import { DiscoveryMap, DiscoveryMapApiService } from '@ngdesk/sam-api';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { DiscoveryMapsService } from '@src/app/company-settings/software-asset-management/discovery-maps/discovery-maps.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { Subject, Subscription } from 'rxjs';
import { MatChipInputEvent } from '@angular/material/chips';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	switchMap,
} from 'rxjs/operators';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
@Component({
	selector: 'app-discovery-maps-detail',
	templateUrl: './discovery-maps-detail.component.html',
	styleUrls: ['./discovery-maps-detail.component.scss'],
})
export class DiscoveryMapsDetailComponent implements OnInit {
	public discoveryMapForm: FormGroup;
	public discoveryMapId;
	public companyInfoSubscription: Subscription;
	public softwareProducts = [];

	public params;
	public softwareProductsScrollSubject = new Subject<any>();
	public platforms = [];
	public languages = [];
	public discoveryMap: DiscoveryMap = {
		products: [],
		platform: '',
		language: '',
	} as DiscoveryMap;

	constructor(
		private discoveryMapService: DiscoveryMapsService,
		private formBuilder: FormBuilder,
		private cacheService: CacheService,
		private translateService: TranslateService,
		private discoveryMapApiService: DiscoveryMapApiService,
		private router: Router,
		private route: ActivatedRoute,
		private bannerMessageService: BannerMessageService,
		private dataService: DataApiService,
		private loaderService: LoaderService,
	) {}

	public ngOnInit() {
		this.platforms = this.discoveryMapService.getAllPlatforms();
		this.languages = this.discoveryMapService.getAllLanguages();
		this.initializeForm();
		// Get the discovery map id from the url.
		this.discoveryMapId = this.route.snapshot.params['id'];
		this.companyInfoSubscription = this.cacheService.companyInfoSubject.subscribe(
			(dataStored) => {
				if (dataStored) {
					this.getAllSoftwareProducts();
				}
			}
		);
	}

	// This function initializes the form.
	public initializeForm() {
		this.discoveryMapForm = this.formBuilder.group({
			PRODUCTS: ['', [Validators.required]],
			PLATFORM: [''],
			LANGUAGE: [''],
		});
		this.params = {
			PRODUCTS: { field: this.translateService.instant('PRODUCTS') },
		};
	}

	// Get all the software products in order to show in the dropdown
	public getAllSoftwareProducts() {
		this.discoveryMapService
			.getSoftwareProducts(0, 10, 'NAME', 'asc', '')
			.subscribe((softwareProductResponse) => {
				this.softwareProducts = softwareProductResponse['DATA'];
				this.initializeNormalizationRuleScrollSubject();
				if (this.discoveryMapId !== 'new') {
					this.getDiscoveryMap();
				}
			});
	}

	// Fetch the Discovery map entry for the detail page.
	public getDiscoveryMap(): void {
		this.discoveryMapService
			.getDiscoveryMap(this.discoveryMapId)
			.subscribe((discoveryMapResponse) => {
				this.discoveryMap = discoveryMapResponse['discoveryMap'];
				this.initializeSoftwareProduct();
				this.discoveryMapForm.controls['NAME'].setValue(
					discoveryMapResponse['discoveryMap'].name
				);
				this.discoveryMapForm.controls['DESCRIPTION'].setValue(
					discoveryMapResponse['discoveryMap'].description
				);
				this.discoveryMapForm.controls['PLATFORM'].setValue(
					discoveryMapResponse['discoveryMap'].platform
				);
				this.discoveryMapForm.controls['LANGUAGE'].setValue(
					discoveryMapResponse['discoveryMap'].language
				);
			});
	}

	// To initialize software product to form while editing.
	public initializeSoftwareProduct() {
		const productIds = this.discoveryMap.products;
		let product;
		const productObj = [];
		productIds.forEach(productId => {
			if (productId) {
				const softwareProduct = this.softwareProducts.find(
					(prod) => prod.id === productId
				);
				if (softwareProduct) {
					product = {
						name: softwareProduct.name,
						id: softwareProduct.id,
					};
					productObj.push(product);
					
				} else {
					const modules: any[] = this.cacheService.companyData['MODULES'];
					const softwareProductModule = modules.find(
						(module) => module.NAME === 'Software Products'
					);
					this.dataService
						.getModuleEntry(softwareProductModule['MODULE_ID'], productId)
						.subscribe((softwareProductResponse) => {
							product = {
								name: softwareProductResponse['NAME'],
								id: softwareProductResponse['DATA_ID'],
							};
							productObj.push(product);
						});
				}
			}
		});
		this.discoveryMap.products = productObj;
		const newSoftwareProducts = [];
		this.softwareProducts.forEach(product => {
			const data = productObj.find((prod) => prod.id === product.id);
			if (!data) {
				newSoftwareProducts.push(product);
			}
		});
		this.softwareProducts = newSoftwareProducts;
	}

	public removeProduct(element): void {
		const index = this.discoveryMap.products.indexOf(element);
		if (index >= 0) {
			const array = this.discoveryMap.products;
			array.splice(index, 1);
		}
		this.softwareProducts.push(element);
	}

	public resetInput(event: MatChipInputEvent): void {
		const input = event.input;
		if (input) {
			input.value = '';
		}
	}

	public addProduct(event) {
		this.discoveryMap.products.push(event.option.value);
		const newProduct = []; 
		this.softwareProducts.forEach(product => {
			if(product.id !== event.option.value.id) {
				newProduct.push(product);
			}
		});
		this.softwareProducts = newProduct;
	}

	// This method save and update the discovery map entry.
	public save() {
		this.discoveryMap.name = this.discoveryMapForm.value['NAME'];
		this.discoveryMap.description = this.discoveryMapForm.value['DESCRIPTION'];
		const products = [];
		if(this.discoveryMap.products.length===0){
			this.loaderService.isLoading = false;
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('PRODDUCTS_CANT_BE_EMPTY'),
			});
		} else{
		this.discoveryMap.products.forEach(product => {
			products.push(product['id']);
		});
		this.discoveryMap.products = products;
		this.discoveryMap.platform = this.discoveryMapForm.value['PLATFORM'];
		this.discoveryMap.language = this.discoveryMapForm.value['LANGUAGE'];
		if (this.discoveryMapId === 'new') {
			// If id is new do a post call.
			this.discoveryMapApiService.postDiscoveryMap(this.discoveryMap).subscribe(
				(discoveryMapResponse: any) => {
					if (discoveryMapResponse) {
						this.router.navigate([`company-settings/discovery-maps`]);
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SAVED_SUCCESSFULLY'),
						});
					}
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		} else {
			// If id is not new do a put call.
			this.discoveryMapApiService.putDiscoveryMap(this.discoveryMap).subscribe(
				(discoveryMapResponse: any) => {
					if (discoveryMapResponse) {
						this.router.navigate([`company-settings/discovery-maps`]);
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
						});
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
	}

	// Initialize scroll subject for scrolling.
	public initializeNormalizationRuleScrollSubject() {
		this.softwareProductsScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let searchValue = '';
					if (value !== '') {
						searchValue = 'NAME' + '=' + value;
					}
					let page = 0;
					if (this.softwareProducts && !search) {
						page = Math.ceil(this.softwareProducts.length / 10);
					}
					return this.discoveryMapService
						.getSoftwareProducts(page, 10, 'NAME', 'asc', searchValue)
						.pipe(
							map((results: any) => {
								if (search) {
									this.softwareProducts = results['DATA'];
								} else if (results['DATA'].length > 0) {
									this.softwareProducts = this.softwareProducts.concat(
										results['DATA']
									);
								}
								return results['DATA'];
							})
						);
				})
			)
			.subscribe();
	}

	// When scrolling the dropdown.
	public onScrollSoftwareProducts() {
		this.softwareProductsScrollSubject.next(['', false]);
	}

	// While entering any text to the input start searching.
	public onSearch() {		
		const product = this.discoveryMapForm.value['PRODUCTS'];
		if (typeof product !== 'object') {
			const searchText = product;
			this.softwareProductsScrollSubject.next([searchText, true]);
		}
	}
}
