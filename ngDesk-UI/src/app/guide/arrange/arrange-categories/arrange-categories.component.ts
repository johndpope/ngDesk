import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';

import { GuideService } from '../../guide.service';

@Component({
	selector: 'app-arrange-categories',
	templateUrl: './arrange-categories.component.html',
	styleUrls: ['../../guide.component.scss'],
})
export class ArrangeCategoriesComponent implements OnInit {
	public categories = [];
	public sections = [];
	public articles = [];
	public errorMessage: string;
	public isLoading = true;

	constructor(private guideService: GuideService) {
		// get all CATEGORIES
		this.guideService.getKbCategories().subscribe(
			(categoriesResponse: any) => {
				console.log('categoriesResponse', categoriesResponse);
				categoriesResponse.DATA.forEach((category) => {
					this.guideService
						.getKbSectionByCategoryId(category.categoryId)
						.subscribe(
							(sectionsResponse: any) => {
								console.log('sectionsResponse', sectionsResponse);
								// for each category, add its sections
								category['SECTIONS'] = sectionsResponse.DATA;
								this.categories.push(category);
								// waits until all categories are loaded to sort by order
								if (this.categories.length === categoriesResponse.DATA.length) {
									this.categories.sort((n1, n2) => {
										if (n1.ORDER > n2.ORDER) {
											return 1;
										}

										if (n1.ORDER < n2.ORDER) {
											return -1;
										}

										return 0;
									});
									this.isLoading = false;
								}
							},
							(sectionsError: any) => {
								console.log(sectionsError);
								this.isLoading = false;
							}
						);
				});
				if (categoriesResponse.DATA.length === 0) {
					this.isLoading = false;
				}
			},
			(categoriesError: any) => {
				console.log(categoriesError);
				this.isLoading = false;
			}
		);
	}

	public ngOnInit() {}

	public drop(event: CdkDragDrop<string[]>) {
		moveItemInArray(this.categories, event.previousIndex, event.currentIndex);
		this.reorderContent();
	}

	public sendToTop(index, array) {
		moveItemInArray(array, index, 0);
		this.reorderContent();
	}

	public reorderContent() {
		const reorderBody = [];
		this.categories.forEach((category, index) => {
			reorderBody.push({ ID: category.categoryId, ORDER: index + 1 });
			if (index === this.categories.length - 1) {
				this.guideService.reorderContent('categories', reorderBody).subscribe(
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
