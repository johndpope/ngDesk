import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ArrangeArticlesComponent } from './arrange/arrange-articles/arrange-articles.component';
import { ArrangeCategoriesComponent } from './arrange/arrange-categories/arrange-categories.component';
import { ArrangeSectionsComponent } from './arrange/arrange-sections/arrange-sections.component';
import { GuideComponent } from './guide.component';

const routes: Routes = [
  { path: '', component: GuideComponent },
  { path: 'arrange', component: ArrangeCategoriesComponent },
  { path: 'arrange/categories/:categoryId', component: ArrangeSectionsComponent },
  { path: 'arrange/sections/:sectionId', component: ArrangeArticlesComponent },
  { path: 'categories', loadChildren: () => import('./categories/categories.module').then(m => m.CategoriesModule) },
  { path: 'sections', loadChildren: () => import('./sections/sections.module').then(m => m.SectionsModule) },
  { path: 'articles', loadChildren: () => import('./articles/articles.module').then(m => m.ArticlesModule) },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  schemas: [NO_ERRORS_SCHEMA],
})
export class GuideRoutingModule { }
