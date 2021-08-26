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
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';

import { SharedModule } from '../../shared-module/shared.module';
import { CreateSectionComponent } from './create-section/create-section.component';
import { RenderSectionComponent } from './render-section/render-section.component';
import { SectionsRoutingModule } from './sections-routing.module';

@NgModule({
  declarations: [CreateSectionComponent, RenderSectionComponent],
  imports: [
    CommonModule,
    SectionsRoutingModule,
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
    MatRadioModule
  ]
})
export class SectionsModule { }
