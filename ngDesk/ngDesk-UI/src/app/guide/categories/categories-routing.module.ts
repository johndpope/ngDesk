import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CreateCategoryComponent } from './create-category/create-category.component';
import { RenderCategoryComponent } from './render-category/render-category.component';

const routes: Routes = [
  { path: ':categoryId/detail', component: RenderCategoryComponent },
  { path: ':categoryId', component: CreateCategoryComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CategoriesRoutingModule { }
