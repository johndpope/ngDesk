import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Category } from '../../../models/category';
import { Section } from '../../../models/section';
import { GuideService } from '../../guide.service';

@Component({
	selector: 'app-arrange-articles',
	templateUrl: './arrange-articles.component.html',
	styleUrls: ['../../guide.component.scss'],
})
export class ArrangeArticlesComponent implements OnInit {
	public category: Category = new Category('', '', false, 0);
	public section: Section = new Section('', '', '', '', 0);
	public articles = [];
	public sectionId: string;
	public errorMessage: string;
	public isLoading = true;

	constructor(
		private route: ActivatedRoute,
		private guideService: GuideService
	) {
		this.sectionId = this.route.snapshot.params['sectionId'];
		this.guideService.getkbSections(this.sectionId).subscribe(
			(sectionsResponse: any) => {
				console.log('sectionsResponse', sectionsResponse);

				this.section = this.convertSection(sectionsResponse);
				this.guideService
					.getKbSectionByCategoryId(this.category.categoryId)
					.subscribe(
						(categoryResponse: any) => {
							console.log('categoryResponse', categoryResponse);
							this.category = this.convertCategory(categoryResponse);
							this.guideService
								.getArticlesBySectionId(this.section.sectionId)
								.subscribe(
									(articlesResponse: any) => {
										console.log('articlesResponse', articlesResponse);
										this.articles =
											articlesResponse.getArticlesBySectionId.filter(
												(article) => article.SECTION === this.section.sectionId
											);
										this.articles.sort((n1, n2) => {
											if (n1.ORDER > n2.ORDER) {
												return 1;
											}

											if (n1.ORDER < n2.ORDER) {
												return -1;
											}

											return 0;
										});
										this.isLoading = false;
									},
									(articlesError: any) => {
										console.log(articlesError);
									}
								);
						},
						(categoryError: any) => {
							console.log(categoryError);
						}
					);
			},
			(sectionsError: any) => {
				console.log(sectionsError);
			}
		);
	}

	public ngOnInit() {}

	private convertCategory(categoryObj): Category {
		const category: Category = new Category(
			categoryObj['DATA'].name,
			categoryObj['DATA'].sourceLanguage,
			categoryObj['DATA'].isDraft,
			categoryObj['DATA'].order,
			categoryObj['DATA'].description,
			categoryObj['DATA'].categoryId
		);
		return category;
	}

	private convertSection(sectionObj): Section {
		const section: Section = new Section(
			sectionObj['DATA'].name,
			sectionObj['DATA'].sourceLanguage,
			sectionObj['DATA'].category['categoryId'],
			sectionObj['DATA'].sortBy,
			sectionObj['DATA'].order,
			sectionObj['DATA'].description,
			sectionObj['DATA'].sectionId
		);
		return section;
	}

	public drop(event: CdkDragDrop<string[]>) {
		moveItemInArray(this.articles, event.previousIndex, event.currentIndex);
		this.reorderContent();
	}

	public sendToTop(index, array) {
		moveItemInArray(array, index, 0);
		this.reorderContent();
	}

	public reorderContent() {
		const reorderBody = [];
		this.articles.forEach((article, index) => {
			reorderBody.push({ ID: article.ARTICLE_ID, ORDER: index + 1 });
			if (index === this.articles.length - 1) {
				this.guideService
					.reorderContent('articles', reorderBody, this.section.sectionId)
					.subscribe(
						(reorderResponse: any) => {
							// do nothing
						},
						(reorderError: any) => {
							this.errorMessage = reorderError.error.ERROR;
						}
					);
			}
		});
	}
}
