import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { ReportMasterComponent } from '@src/app/reports/report-master/report-master.component';
import { ReportDetailComponent } from '@src/app/reports/report-detail/report-detail.component';
import { ReportsDialogComponent } from '@src/app/dialogs/reports-dialog/reports-dialog.component';
import { ReportService } from '@src/app/reports/report.service';
import { NativeScriptCommonModule } from '@nativescript/angular';

@NgModule({
  imports: [
    NativeScriptCommonModule
  ],
  declarations: [
  ReportMasterComponent,
  ReportDetailComponent,
  ReportsDialogComponent],
  providers: [
  ReportService],
  schemas: [
    NO_ERRORS_SCHEMA
  ]
})
export class ReportsModule { }
