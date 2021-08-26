import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { CompaniesService } from '../companies/companies.service';
import { UsersService } from '../users/users.service';
import { GuideService } from './guide.service';
import { RolesService } from '../roles/roles.service';

@Component({
	selector: 'app-guide',
	templateUrl: './guide.component.html',
	styleUrls: ['./guide.component.scss'],
})
export class GuideComponent implements OnInit {
	public categories = [];
	public filteredCategories = [];
	public sections = [];
	public articles = [];
	public categoriesLoaded = false;
	public roleName: string;
	public searchCategoriesForm: FormGroup;
	public isLoading = true;
	public errorMessage: string;
	public authToken = this.usersService.getAuthenticationToken();
	public logo;
	public filteredArticles = [];
	public showArticles = false;
	public searchString: string;
	constructor(
		private router: Router,
		private rolesService: RolesService,
		private usersService: UsersService,
		private formBuilder: FormBuilder,
		private guideService: GuideService,
		private route: ActivatedRoute,
		private companiesService: CompaniesService
	) {
		if (!this.authToken) {
			this.companiesService
				.getKnowledgeBaseGeneralSettings()
				.subscribe((response: any) => {
					if (!response.ENABLE_DOCS) {
						this.router.navigate(['login']);
					}
				});
		}
	}

	public ngOnInit() {
		if (!this.route.snapshot.routeConfig.path && this.authToken) {
			this.router.navigate([`guide`]);
		}
		if (
			this.usersService.getAuthenticationToken() !== '' &&
			this.usersService.getAuthenticationToken() !== null
		) {
			this.rolesService.getRole(this.usersService.user.ROLE).subscribe(
				(roleResponse: any) => {
					this.roleName = roleResponse.NAME;
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
		}

		this.guideService.getCategories().subscribe(
			(categoriesResponse: any) => {
				if (
					this.roleName &&
					this.roleName !== 'SystemAdmin' &&
					this.roleName !== 'Agent'
				) {
					this.categories = this.sortByOrder(
						categoriesResponse.DATA.filter(
							(category) => category.IS_DRAFT === false
						)
					);
					this.filteredCategories = this.sortByOrder(
						categoriesResponse.DATA.filter(
							(category) => category.IS_DRAFT === false
						)
					);
				} else {
					this.categories = this.sortByOrder(categoriesResponse.DATA);
					this.filteredCategories = this.sortByOrder(categoriesResponse.DATA);
				}
				this.isLoading = false;
			},
			(categoryError: any) => {
				this.errorMessage = categoryError.error.ERROR;
			}
		);

		this.searchCategoriesForm = this.formBuilder.group({
			SEARCH: '',
		});
		const searchValue = localStorage.getItem('GUIDE_SEARCH_VALUE');
		if (searchValue && searchValue !== '') {
			this.searchCategoriesForm.get('SEARCH').setValue(searchValue);
			this.searchArticle();
		}

		this.onChanges();
	}

	private onChanges(): void {
		this.searchCategoriesForm.get('SEARCH').valueChanges.subscribe((val) => {
			if (!val) {
				this.showArticles = false;
				localStorage.removeItem('GUIDE_SEARCH_VALUE');
			}
		});
	}

	public searchArticle(): void {
		this.searchString = this.searchCategoriesForm.get('SEARCH').value;
		localStorage.setItem('GUIDE_SEARCH_VALUE', this.searchString);
		if (this.searchCategoriesForm.get('SEARCH').value) {
			this.isLoading = true;
			this.showArticles = true;
			this.guideService.getSearchedArticle(this.searchString).subscribe(
				(response: any) => {
					this.filteredArticles = response.DATA;
					this.filteredArticles.forEach((article) => {
						article.BODY = article.BODY.replace(/<[^>]+>/g, '');
					});
					this.isLoading = false;
				},
				(error: any) => {
					this.isLoading = false;
					this.errorMessage = error.error.ERROR;
				}
			);
		}
	}

	private sortByOrder(array): any[] {
		array.sort((n1, n2) => {
			if (n1.ORDER > n2.ORDER) {
				return 1;
			}

			if (n1.ORDER < n2.ORDER) {
				return -1;
			}

			return 0;
		});
		return array;
	}

	public redirect(object, type) {
		if (this.authToken) {
			switch (type) {
				case 'Categories': {
					this.router.navigate([
						'guide',
						'categories',
						object.CATEGORY_ID,
						'detail',
					]);
					break;
				}
				case 'Sections': {
					this.router.navigate([
						'guide',
						'sections',
						object.SECTION_ID,
						'detail',
					]);
					break;
				}
				case 'Articles': {
					this.router.navigate([
						'guide',
						'articles',
						object.SECTION,
						object.TITLE,
					]);
					break;
				}
			}
		} else {
			switch (type) {
				case 'Categories': {
					this.router.navigate([
						'guide',
						'categories',
						object.CATEGORY_ID,
						'detail',
					]);
					break;
				}
				case 'Sections': {
					this.router.navigate([
						'guide',
						'sections',
						object.SECTION_ID,
						'detail',
					]);
					break;
				}
				case 'Articles': {
					this.router.navigate([
						'guide',
						'articles',
						object.SECTION,
						object.TITLE,
					]);
					break;
				}
			}
		}
	}

	public new(type) {
		switch (type) {
			case 'category': {
				this.router.navigate([`guide/categories/new`]);
				break;
			}
			case 'section': {
				this.router.navigate([`guide/sections/new`]);
				break;
			}
			case 'article': {
				this.router.navigate([`guide/articles/detail/new`]);
				break;
			}
		}
	}
}
