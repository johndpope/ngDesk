import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AppGlobals } from '@src/app/app.globals';
import { CacheService } from '@src/app/cache.service';
import { forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
	providedIn: 'root',
})
export class NormalizationRulesService {
	public allNormalizationRules = [];

	constructor(
		private translateService: TranslateService,
		private http: HttpClient,
		private globals: AppGlobals,
		private cacheService: CacheService
	) {}

	public getOperators() {
		return [
			{
				DISPLAY: this.translateService.instant('CONTAINS'),
				BACKEND: 'Contains',
			},
			{
				DISPLAY: this.translateService.instant('ENDS_WITH'),
				BACKEND: 'Ends With',
			},
			{ DISPLAY: this.translateService.instant('IS'), BACKEND: 'Is' },
			{
				DISPLAY: this.translateService.instant('STARTS_WITH'),
				BACKEND: 'Starts With',
			},
		];
	}

	// Get all call in order to fetch all normalization rules.
	public getAllNormalizationRules(page, pageSize, sortBy, orderBy) {
		const query = `{
			normalizationRules: getNormalizationRules(pageNumber: ${page}, pageSize: ${pageSize}) {
				dateCreated
				name
                normalizationRuleId
			}
			totalCount: getNormalizationRulesCount
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Get in dividula entry based on id.
	public getNormalizationRule(ruleId) {
		const query = ` {
			normalizationRule: getNormalizationRule(ruleId: "${ruleId}") {
				name
				description
				publisher: publisher{
						 key
						 operator
						 value
					 }
				product: product{
						key
						operator
						value
					}
				version: version{
						key
						operator
						value
					}
				}
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getNormalizationDetails(
		moduleName,
		page,
		pageSize,
		sortBy,
		orderBy,
		isScrolling,
		searchString
	): Observable<any[]> {
		const modules: any[] = this.cacheService.companyData['MODULES'];
		const softwareProductModule = modules.find(
			(module) => module.NAME === 'Software Products'
		);
		const softwareInstallationModule = modules.find(
			(module) => module.NAME === 'Software Installation'
		);

		// Fetch all products.
		let productResponse;
		if (moduleName || moduleName === '' || moduleName === 'products') {
			const productField = softwareProductModule.FIELDS.find(
				(field) => field.NAME === 'NAME'
			);
			const productQuery = `{
				DATA: getDistinctValues(moduleId: "${softwareProductModule.MODULE_ID}",fieldId: "${productField.FIELD_ID}",pageNumber: ${page},
				pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}")
			 }`;
			productResponse = this.http
				.post(`${this.globals.graphqlUrl}`, productQuery)
				.pipe(
					map((softwareProduct) => {
						return softwareProduct['DATA'];
					})
				);
			if (isScrolling) {
				return productResponse;
			}
		}

		// Fetch all publishers.
		let publisherResponse;
		if (moduleName || moduleName === '' || moduleName === 'publishers') {
			const publisherField = softwareInstallationModule.FIELDS.find(
				(field) => field.NAME === 'PUBLISHER'
			);
			const publisherQuery = `{
				DATA: getDistinctValues(moduleId: "${softwareInstallationModule.MODULE_ID}",fieldId: "${publisherField.FIELD_ID}",pageNumber: ${page},
				pageSize: ${pageSize}, sortBy: "PUBLISHER", orderBy: "${orderBy}")
			 }`;
			publisherResponse = this.http
				.post(`${this.globals.graphqlUrl}`, publisherQuery)
				.pipe(
					map((publisher) => {
						return publisher['DATA'];
					})
				);
			if (isScrolling) {
				return publisherResponse;
			}
		}

		// Fetch all publishers.
		let versionResponse;
		if (moduleName || moduleName === '' || moduleName === 'publishers') {
			const versionField = softwareInstallationModule.FIELDS.find(
				(field) => field.NAME === 'VERSION'
			);
			const versionQuery = `{
				DATA: getDistinctValues(moduleId: "${softwareInstallationModule.MODULE_ID}",fieldId: "${versionField.FIELD_ID}",pageNumber: ${page},
				pageSize: ${pageSize}, sortBy: "VERSION", orderBy: "${orderBy}")
			 }`;
			versionResponse = this.http
				.post(`${this.globals.graphqlUrl}`, versionQuery)
				.pipe(
					map((version) => {
						return version['DATA'];
					})
				);
			if (isScrolling) {
				return versionResponse;
			}
		}

		return forkJoin([productResponse, publisherResponse, versionResponse]);
	}
}
