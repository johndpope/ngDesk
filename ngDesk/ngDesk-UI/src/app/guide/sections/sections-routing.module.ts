import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CreateSectionComponent } from './create-section/create-section.component';
import { RenderSectionComponent } from './render-section/render-section.component';

const routes: Routes = [
  { path: ':sectionId/detail', component: RenderSectionComponent },
  { path: ':sectionId', component: CreateSectionComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SectionsRoutingModule { }
