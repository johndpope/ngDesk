import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Category } from '../../../models/category';
import { GuideService } from '../../guide.service';

@Component({
  selector: 'app-arrange-sections',
  templateUrl: './arrange-sections.component.html',
  styleUrls: ['../../guide.component.scss']
})
export class ArrangeSectionsComponent implements OnInit {

  public category: Category = new Category('', '', false, 0);
  public sections = [];
  public categoryId: string;
  public errorMessage: string;
  public isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private guideService: GuideService,
  ) {
    this.categoryId = this.route.snapshot.params['categoryId'];
    // get category
    this.guideService.getCategoryById(this.categoryId).subscribe(
      (categoryResponse: any) => {
        this.category = this.convertCategory(categoryResponse);
        // get all sections in this category
        this.guideService.getSections(this.categoryId).subscribe(
          (sectionsResponse: any) => {
            sectionsResponse.DATA.forEach((section, index) => {
              this.guideService.getArticlesBySection(section.SECTION_ID, false).subscribe(
                (articlesResponse: any) => {
                  // for each section, add its articles
                  section['ARTICLES'] = articlesResponse.DATA.filter(article => article.SECTION === section.SECTION_ID);
                  this.sections.push(section);
                  // waits until all sections with articles are loaded before sorting
                  if (sectionsResponse.DATA.length === this.sections.length) {
                    this.sections.sort((n1, n2) => {
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
                }, (articlesError: any) => {
                  console.log(articlesError);
                  this.isLoading = false;
                }
              );
            });
            if (sectionsResponse.DATA.length === 0) {
              this.isLoading = false;
            }
          }, (sectionsError: any) => {
            console.log(sectionsError);
            this.isLoading = false;
          }
        );
      }, (categoryError: any) => {
        console.log(categoryError);
        this.isLoading = false;
      }
    );
  }

  public ngOnInit() {

  }

  private convertCategory(categoryObj): Category {
    const category: Category = new Category(
      categoryObj.NAME,
      categoryObj.SOURCE_LANGUAGE,
      categoryObj.IS_DRAFT,
      categoryObj.ORDER,
      categoryObj.DESCRIPTION,
      categoryObj.CATEGORY_ID,
    );
    return category;
  }

  public drop(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.sections, event.previousIndex, event.currentIndex);
    this.reorderContent();
  }

  public sendToTop(index, array) {
    moveItemInArray(array, index, 0);
    this.reorderContent();
  }

  public reorderContent() {
    const reorderBody = [];
    this.sections.forEach((section, index) => {
      reorderBody.push({ ID: section.SECTION_ID, ORDER: index + 1 });
      if (index === this.sections.length - 1) {
        this.guideService.reorderContent('sections', reorderBody, this.categoryId).subscribe(
          (reorderResponse: any) => {
            // do nothing
          }, (reorderError: any) => {
            this.errorMessage = reorderError.error.ERROR;
          }
        );
      }
    });
  }

}
