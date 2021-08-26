import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SchedulesDetailComponent } from './schedules-detail/schedules-detail.component';
import { SchedulesMasterComponent } from './schedules-master/schedules-master.component';

const routes: Routes = [
  { path: '', component: SchedulesMasterComponent },
  { path: ':scheduleName', component: SchedulesDetailComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  schemas: [NO_ERRORS_SCHEMA],
})
export class SchedulesRoutingModule { }
