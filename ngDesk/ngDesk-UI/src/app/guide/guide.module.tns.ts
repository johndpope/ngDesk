import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';

import { GuideComponent } from '@src/app/guide/guide.component';
import { ArrangeArticlesComponent } from '@src/app/guide/arrange/arrange-articles/arrange-articles.component';
import { ArrangeCategoriesComponent } from '@src/app/guide/arrange/arrange-categories/arrange-categories.component';
import { ArrangeSectionsComponent } from '@src/app/guide/arrange/arrange-sections/arrange-sections.component';
import { NativeScriptCommonModule } from '@nativescript/angular';

@NgModule({
  imports: [
    NativeScriptCommonModule
  ],
  declarations: [
  GuideComponent,
  ArrangeArticlesComponent,
  ArrangeCategoriesComponent,
  ArrangeSectionsComponent],
  providers: [
  ],
  schemas: [
    NO_ERRORS_SCHEMA
  ]
})
export class GuideModule { }
