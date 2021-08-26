import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CreateArticlesComponent } from './create-articles/create-articles.component';
import { ManageArticlesComponent } from './manage-articles/manage-articles.component';
import { RenderArticlesComponent } from './render-articles/render-articles.component';

const routes: Routes = [
  { path: 'detail/:articleId', component: CreateArticlesComponent },
  { path: 'manage/:sectionId', component: ManageArticlesComponent },
  { path: 'manage', component: ManageArticlesComponent },
  { path: ':sectionId/:articleName', component: RenderArticlesComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ArticlesRoutingModule { }
