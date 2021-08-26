import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';

import { SharedModule } from '../shared-module/shared.module';
import { LayerRestrictionComponent } from './schedules-detail/layer-restriction/layer-restriction.component';
import { SchedulesDetailComponent } from './schedules-detail/schedules-detail.component';
import { SchedulesMasterComponent } from './schedules-master/schedules-master.component';
import { SchedulesRoutingModule } from './schedules-routing.module';
import { SchedulesService } from './schedules.service';

@NgModule({
	declarations: [
		LayerRestrictionComponent,
		SchedulesDetailComponent,
		SchedulesMasterComponent,
	],
	imports: [
		CommonModule,
		SchedulesRoutingModule,
		MatDividerModule,
		MatSelectModule,
		MatRadioModule,
		FormsModule,
		ReactiveFormsModule,
		MatIconModule,
		SharedModule,
		MatDatepickerModule,
		FlexLayoutModule,
		MatInputModule,
		MatButtonModule,
		MatChipsModule,
	],
	// entryComponents: [
	//   LayerRestrictionComponent
	// ]
	providers: [SchedulesService],
})
export class SchedulesModule {}
