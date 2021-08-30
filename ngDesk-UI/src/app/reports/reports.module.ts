import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';

import { ReportsDialogComponent } from '../dialogs/reports-dialog/reports-dialog.component';
import { SharedModule } from '../shared-module/shared.module';
import { ReportDetailComponent } from './report-detail/report-detail.component';
import { ReportMasterComponent } from './report-master/report-master.component';
import { ReportService } from './report.service';
import { ReportsRoutingModule } from './reports-routing.module';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { ResizableModule } from 'angular-resizable-element';

@NgModule({
	declarations: [
		ReportMasterComponent,
		ReportDetailComponent,
		ReportsDialogComponent,
	],
	imports: [
		CommonModule,
		ReportsRoutingModule,
		SharedModule,
		FlexLayoutModule,
		FormsModule,
		ReactiveFormsModule,
		MatButtonModule,
		MatDialogModule,
		MatIconModule,
		MatInputModule,
		MatMenuModule,
		MatSelectModule,
		MatTabsModule,
		MatRadioModule,
		MatTooltipModule,
		MatProgressSpinnerModule,
		MatExpansionModule,
		DragDropModule,
		ResizableModule,
	],
	// entryComponents: [ReportsDialogComponent],
	providers: [ReportService],
})
export class ReportsModule {}
