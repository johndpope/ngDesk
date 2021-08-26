import { DragDropModule } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';

import { SharedModule } from '../shared-module/shared.module';
import { ArrangeArticlesComponent } from './arrange/arrange-articles/arrange-articles.component';
import { ArrangeCategoriesComponent } from './arrange/arrange-categories/arrange-categories.component';
import { ArrangeSectionsComponent } from './arrange/arrange-sections/arrange-sections.component';
import { GuideRoutingModule } from './guide-routing.module';
import { GuideComponent } from './guide.component';

@NgModule({
  declarations: [
    GuideComponent,
    ArrangeArticlesComponent,
    ArrangeCategoriesComponent,
    ArrangeSectionsComponent
  ],
  imports: [
    GuideRoutingModule,
    CommonModule,
    SharedModule,
    FlexLayoutModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatListModule,
    MatMenuModule,
    MatIconModule,
    MatInputModule,
    MatToolbarModule,
    MatDividerModule,
    MatSelectModule,
    MatChipsModule,
    DragDropModule
  ]
})
export class GuideModule { }
