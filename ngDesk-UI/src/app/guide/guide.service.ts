import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import { AppGlobals } from '../app.globals';
import { CacheService } from '../cache.service';
import { UsersService } from '../users/users.service';

@Injectable({
	providedIn: 'root',
})
export class GuideService {
	public sectionId: string;
	public categoryId: string;

	// TODO: need to finalize where to get languages
	public languageObject = [];

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private translateService: TranslateService,
		private cacheService: CacheService,
		private usersService: UsersService
	) {
		this.languageObject = [
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

	public getDataForArticles() {
		const modules: any[] = this.cacheService.companyData['MODULES'];
		const userModule = modules.find((module) => module.NAME === 'Users');

		const query = `{
				DATA: getUsers(moduleId: "${userModule.MODULE_ID}", pageNumber: 0, pageSize: 5000) {
					DATA_ID: _id
					CONTACT {
						DATA_ID: _id
						PRIMARY_DISPLAY_FIELD: FULL_NAME
					}
					USER_UUID
				}
			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
	public getCategories() {
		return this.http.get(`${this.globals.baseRestUrl}/categories`);
	}

	public getCategoryById(categoryId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/categories/${categoryId}`
		);
	}

	public postCategory(category) {
		return this.http.post(`${this.globals.baseRestUrl}/categories`, category);
	}

	public putCategory(category) {
		return this.http.put(`${this.globals.baseRestUrl}/categories`, category);
	}

	public deleteCategory(categoryId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/categories/${categoryId}`
		);
	}

	public getSections(categoryId) {
		const httpParams = new HttpParams().set('category', categoryId);
		return this.http.get(`${this.globals.baseRestUrl}/sections`, {
			params: httpParams,
		});
	}

	public getSectionById(sectionId) {
		return this.http.get(`${this.globals.baseRestUrl}/sections/${sectionId}`);
	}

	public postSection(section) {
		return this.http.post(`${this.globals.baseRestUrl}/sections`, section);
	}

	public putSection(section) {
		return this.http.put(`${this.globals.baseRestUrl}/sections`, section);
	}

	public deleteSection(sectionId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/sections/${sectionId}`
		);
	}

	public reorderContent(docType, reorderBody, parentId?) {
		let httpParams;
		switch (docType) {
			case 'sections': {
				httpParams = new HttpParams().set('category_id', parentId);
				break;
			}

			case 'articles': {
				httpParams = new HttpParams().set('section_id', parentId);
				break;
			}
		}
		return this.http.post(
			`${this.globals.baseRestUrl}/re-order/${docType}`,
			reorderBody,
			{ params: httpParams }
		);
	}

	public postArticle(article) {
		return this.http.post(`${this.globals.baseRestUrl}/articles`, article);
	}

	public getArticle(articleId) {
		return this.http.get(`${this.globals.baseRestUrl}/articles/${articleId}`);
	}

	public putArticle(article) {
		return this.http.put(`${this.globals.baseRestUrl}/articles`, article);
	}

	public getSortedArticles(sortBy, orderBy, page, pageSize, sectionId?) {
		let httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize)
			.set('published_only', 'false');
		if (sectionId) {
			httpParams = new HttpParams()
				.set('sort', sortBy)
				.set('order', orderBy)
				.set('page', page)
				.set('page_size', pageSize)
				.set('section', sectionId)
				.set('published_only', 'false');
		}
		return this.http.get(`${this.globals.baseRestUrl}/articles`, {
			params: httpParams,
		});
	}

	public getArticlesBySection(sectionId, isPublic) {
		const httpParams = new HttpParams()
			.set('section', sectionId)
			.set('published_only', isPublic);
		return this.http.get(`${this.globals.baseRestUrl}/articles`, {
			params: httpParams,
		});
	}

	public postComment(articleId, comment) {
		return this.http.post(
			`${this.globals.baseRestUrl}/articles/${articleId}/comments`,
			comment
		);
	}

	public deleteArticle(articleId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/articles/${articleId}`
		);
	}

	public getSearchedArticle(searchString) {
		const httpParams = new HttpParams()
			.set('search', searchString)
			.set('published_only', 'true');
		return this.http.get(`${this.globals.baseRestUrl}/articles`, {
			params: httpParams,
		});
	}
}
