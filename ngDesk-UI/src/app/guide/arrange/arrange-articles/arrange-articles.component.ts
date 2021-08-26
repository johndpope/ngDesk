import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Category } from '../../../models/category';
import { Section } from '../../../models/section';
import { GuideService } from '../../guide.service';

@Component({
  selector: 'app-arrange-articles',
  templateUrl: './arrange-articles.component.html',
  styleUrls: ['../../guide.component.scss']
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
    private guideService: GuideService,
  ) {
    this.sectionId = this.route.snapshot.params['sectionId'];
    this.guideService.getSectionById(this.sectionId).subscribe(
      (sectionsResponse: any) => {
        this.section = this.convertSection(sectionsResponse);
        this.guideService.getCategoryById(this.section.category).subscribe(
          (categoryResponse: any) => {
            this.category = this.convertCategory(categoryResponse);
            this.guideService.getArticlesBySection(this.section.sectionId, false).subscribe(
              (articlesResponse: any) => {
                this.articles = articlesResponse.DATA.filter(article => article.SECTION === this.section.sectionId);
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
              }, (articlesError: any) => {
                console.log(articlesError);
              }
            );
          }, (categoryError: any) => {
            console.log(categoryError);
          }
        );
      }, (sectionsError: any) => {
        console.log(sectionsError);
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

  private convertSection(sectionObj): Section {
    const section: Section = new Section(
      sectionObj.NAME,
      sectionObj.SOURCE_LANGUAGE,
      sectionObj.CATEGORY,
      sectionObj.SORT_BY,
      sectionObj.ORDER,
      sectionObj.DESCRIPTION,
      sectionObj.SECTION_ID
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
        this.guideService.reorderContent('articles', reorderBody, this.sectionId).subscribe(
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
