import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AppGlobals } from '@src/app/app.globals';
import { CacheService } from '@src/app/cache.service';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class DiscoveryMapsService {
	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private cacheService: CacheService,
		private translateService: TranslateService
	) {}

	public getAllLanguages() {
		return [
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
	}

	public getAllPlatforms() {
		return ['Linux', 'Mac', 'Windows'];
	}

	// Get all call in order to fetch all discovery maps.
	public getAllDiscoveryMaps(page, pageSize, sortBy, orderBy): Observable<any> {
		const query = `{
		discoveryMaps: getDiscoveryMaps(pageNumber: ${page}, pageSize: ${pageSize}) {
			name
			description
			id
			}
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public searchDiscoveryMaps(page, pageSize, sortBy, orderBy, searchString): Observable<any> {
		const query = `{
			DATA: getDiscoveryMaps(pageNumber: ${page}, pageSize: ${pageSize} ,sortBy: "${sortBy}", 
			orderBy: "${orderBy}",search:"${searchString}") {
				name
				id
			}
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Get individual entry based on id.
	public getDiscoveryMap(id): Observable<any> {
		const query = ` {
			discoveryMap: getDiscoveryMap(id: "${id}") {
				name
				description
				products
				platform
				language
				id
				}
   			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Get all the software products.
	public getSoftwareProducts(page, pageSize, sortBy, orderBy, searchString) {
		const modules: any[] = this.cacheService.companyData['MODULES'];
		const softwareProductModule = modules.find(
			(module) => module.NAME === 'Software Products'
		);
		const query = `{
				DATA: getSoftware_Products (moduleId: "${softwareProductModule.MODULE_ID}",pageNumber: ${page},
				pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}",search:"${searchString}") {
					name:NAME
					id:_id
				}
			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
