import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { CompaniesService } from '../../../companies/companies.service';
import { Category } from '../../../models/category';
import { Section } from '../../../models/section';
import { UsersService } from '../../../users/users.service';
import { GuideService } from '../../guide.service';
import { RolesService } from '@src/app/roles/roles.service';
import { map, mergeMap } from 'rxjs/operators';

@Component({
	selector: 'app-render-section',
	templateUrl: './render-section.component.html',
	styleUrls: ['./render-section.component.scss'],
})
export class RenderSectionComponent implements OnInit {
	public section: Section = new Section('', '', '', '', 0);
	public category: Category = new Category('', '', false, 0);
	public articles = [];
	public isLoading = true;
	public roleName: string;
	public authToken = this.usersService.getAuthenticationToken();

	constructor(
		private guideService: GuideService,
		private route: ActivatedRoute,
		private router: Router,
		private rolesService: RolesService,
		private usersService: UsersService,
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
		if (
			this.usersService.getAuthenticationToken() !== '' &&
			this.usersService.getAuthenticationToken() !== null
		) {
			this.rolesService.getRole(this.usersService.user.ROLE).subscribe(
				(roleResponse: any) => {
					this.roleName = roleResponse.NAME;
				},
				(roleError: any) => {
					console.log(roleError);
				}
			);
		}

		const sectionId = this.route.snapshot.params['sectionId'];
		this.guideService
			.getkbSections(sectionId)
			.pipe(
				mergeMap((sectionResponse: any) => {
					this.section = this.convertSection(sectionResponse);
					console.log('	this.section=====', this.section);
					console.log('	this.section[CATEGORY]=====', this.section['CATEGORY']);
					return this.guideService
						.getKbCategoryById(this.section['CATEGORY'])
						.pipe(
							mergeMap((categoryResponse: any) => {
								this.category = this.convertCategory(categoryResponse);
								return this.guideService
									.getArticlesBySectionId(this.section['sectionId'])
									.pipe(
										map((articlesResponse: any) => {
											this.articles = this.sortByOrder(
												articlesResponse.getArticlesBySectionId.filter(
													(article) =>
														article.publish === true &&
														article.section === this.section['sectionId']
												)
											);
											this.isLoading = false;

											(articlesError: any) => {
												console.log(articlesError);

												(categoryError: any) => {
													console.log(categoryError);
												};

												(sectionError: any) => {
													console.log(sectionError);
												};
											};
										})
									);
							})
						);
				})
			)
			.subscribe();
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

	public convertSection(sectionData): Section {
		const section = new Section(
			sectionData['DATA'].name,
			sectionData['DATA'].sourceLanguage,
			sectionData['DATA'].category['categoryId'],
			sectionData['DATA'].sortBy,
			sectionData['DATA'].order,
			sectionData['DATA'].description,
			sectionData['DATA'].sectionId
		);

		return section;
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

	public navigate(path) {
		// if (this.authToken) {
		this.router.navigate(path);
		// }
	}

	public newArticle() {
		this.guideService.sectionId = this.section['sectionId'];
		this.navigate(['guide', 'articles', 'detail', 'new']);
	}
}
