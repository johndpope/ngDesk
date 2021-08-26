import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SchedulesDetailComponent } from './schedules-detail/schedules-detail.component';
import { SchedulesMasterComponent } from './schedules-master/schedules-master.component';

const routes: Routes = [
  { path: '', component: SchedulesMasterComponent },
  { path: ':scheduleId', component: SchedulesDetailComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SchedulesRoutingModule { }
