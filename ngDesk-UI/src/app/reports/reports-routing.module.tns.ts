import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ReportDetailComponent } from './report-detail/report-detail.component';
import { ReportMasterComponent } from './report-master/report-master.component';

const routes: Routes = [
  { path: '', component: ReportMasterComponent },
  { path: ':reportId', component: ReportDetailComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  schemas: [NO_ERRORS_SCHEMA],
})
export class ReportsRoutingModule { }
