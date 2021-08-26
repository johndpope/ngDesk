import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';

import { LayerRestrictionComponent } from '@src/app/schedules/schedules-detail/layer-restriction/layer-restriction.component';
import { SchedulesDetailComponent } from '@src/app/schedules/schedules-detail/schedules-detail.component';
import { SchedulesMasterComponent } from '@src/app/schedules/schedules-master/schedules-master.component';
import { NativeScriptCommonModule } from '@nativescript/angular';

@NgModule({
  imports: [
    NativeScriptCommonModule
  ],
  declarations: [
  LayerRestrictionComponent,
  SchedulesDetailComponent,
  SchedulesMasterComponent],
  providers: [
  ],
  schemas: [
    NO_ERRORS_SCHEMA
  ]
})
export class SchedulesModule { }
