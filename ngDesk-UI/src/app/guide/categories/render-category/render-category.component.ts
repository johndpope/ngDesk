import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { CompaniesService } from '../../../companies/companies.service';
import { Category } from '../../../models/category';
import { UsersService } from '../../../users/users.service';
import { GuideService } from '../../guide.service';
import { RolesService } from '@src/app/roles/roles.service';

@Component({
	selector: 'app-render-category',
	templateUrl: './render-category.component.html',
	styleUrls: ['./render-category.component.scss'],
})
export class RenderCategoryComponent implements OnInit {
	public category: Category = new Category('', '', false, 0);
	public sections = [];
	public isLoading = true;
	public roleName;
	public isAuthenticated = true;
	protected authToken = this.usersService.getAuthenticationToken();
	constructor(
		private guideService: GuideService,
		private route: ActivatedRoute,
		private router: Router,
		private rolesService: RolesService,
		private usersService: UsersService,
		private companiesService: CompaniesService
	) {
		if (!this.authToken) {
			this.isAuthenticated = false;
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
		if (
			this.usersService.getAuthenticationToken() !== '' &&
			this.usersService.getAuthenticationToken() !== null
		) {
			this.rolesService.getRole(this.usersService.user.ROLE).subscribe(
				(roleResponse: any) => {
					this.roleName = roleResponse.NAME;
				},
				(roleError: any) => {
					this.isLoading = false;
				}
			);
		}

		const categoryId = this.route.snapshot.params['categoryId'];
		this.guideService.getKbCategoryById(categoryId).subscribe(
			(categoryResponse: any) => {
				console.log('categoryResponse', categoryResponse);
				this.category = this.convertCategory(categoryResponse);
				console.log('this.category', this.category);

				this.guideService
					.getKbSectionByCategoryId(this.category.categoryId)
					.subscribe(
						(sectionsResponse: any) => {
							console.log('sectionsResponse', sectionsResponse);
							this.sections = this.sortByOrder(sectionsResponse.DATA);
							this.sections.forEach((section, index) => {
								section['ARTICLES'] = [];
								//	debugger;
								this.guideService
									.getArticlesBySectionId(
										sectionsResponse.DATA[index].sectionId
									)
									.subscribe(
										(articlesResponse: any) => {
											console.log('articlesResponse', articlesResponse);
											section['ARTICLES'] = this.sortByOrder(
												articlesResponse.getArticlesBySectionId.filter(
													(article) =>
														article.publish === true &&
														article.section === section.sectionId
												)
											);
											if (index === this.sections.length - 1) {
												this.isLoading = false;
											}
										},
										(articlesError: any) => {
											this.isLoading = false;
										}
									);
							});
							if (this.sections.length === 0) {
								this.isLoading = false;
							}
						},
						(sectionsError: any) => {
							this.isLoading = false;
						}
					);
			},
			(categoryError: any) => {
				this.isLoading = false;
			}
		);
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

	public convertCategory(categoryData): Category {
		const category = new Category(
			categoryData['DATA'].name,
			categoryData['DATA'].sourceLanguage,
			categoryData['DATA'].isDraft,
			categoryData['DATA'].order,
			categoryData['DATA'].description,
			categoryData['DATA'].categoryId
		);
		return category;
	}

	public viewDetail(obj, type) {
		switch (type) {
			case 'Sections': {
				this.navigate(['guide', 'sections', obj.sectionId, 'detail']);
				break;
			}

			case 'Articles': {
				this.navigate([`guide`, `articles`, obj.section, obj.title]);
				break;
			}
		}
	}

	public navigate(path) {
		// if (this.authToken) {
		this.router.navigate(path);
		// }
	}

	public newSection() {
		this.guideService.categoryId = this.category.categoryId;
		this.navigate(['guide', 'sections', 'new']);
	}
}
