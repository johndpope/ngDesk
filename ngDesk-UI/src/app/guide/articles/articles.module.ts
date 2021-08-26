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
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatToolbarModule } from '@angular/material/toolbar';

import { SharedModule } from '../../shared-module/shared.module';
import { ArticlesRoutingModule } from './articles-routing.module';
import { CreateArticlesComponent } from './create-articles/create-articles.component';
import { ManageArticlesComponent } from './manage-articles/manage-articles.component';
import { RenderArticlesComponent } from './render-articles/render-articles.component';

@NgModule({
  declarations: [CreateArticlesComponent, ManageArticlesComponent, RenderArticlesComponent],
  imports: [
    CommonModule,
    ArticlesRoutingModule,
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
    DragDropModule,
    MatSlideToggleModule
  ]
})
export class ArticlesModule { }
