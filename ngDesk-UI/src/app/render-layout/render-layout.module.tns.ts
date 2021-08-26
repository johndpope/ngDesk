import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';

import { ModulesService } from '@src/app/modules/modules.service';
import { RenderLayoutService } from '@src/app/render-layout/render-layout.service';
import { SharedModule } from '@src/app/shared-module/shared-module.tns';
import { RenderLayoutRoutingModule } from '@src/app/render-layout/render-layout-routing.module.tns';

import { MatSnackBarHelper } from '@src/app/render-layout/dialog-snackbar-helper/matsnackbar-helper';
import { MatDialogHelper } from '@src/app/render-layout/dialog-snackbar-helper/matdialog-helper';
//import { NativeScriptDateTimePickerModule } from "nativescript-datetimepicker/angular";

import { ReactiveFormsModule } from '@angular/forms';

import { Feedback } from 'nativescript-feedback';

// import { RenderListLayoutNewComponent } from '@src/app/render-layout/render-list-layout-new/render-list-layout-new.component';
import { NativeScriptCommonModule } from '@nativescript/angular';
import { NativeScriptFormsModule } from '@nativescript/angular';
import { MobileRenderListLayoutComponent } from './mobile-render-list-layout/mobile-render-list-layout.component';
// import { RenderDetailNewComponent } from '@src/app/render-layout/render-detail-new/render-detail-new.component';
// import { registerElement } from "nativescript-angular";
// registerElement("PreviousNextView", () => require("nativescript-iqkeyboardmanager").PreviousNextView);
@NgModule({
	schemas: [
        NO_ERRORS_SCHEMA
	],
	imports: [
		NativeScriptCommonModule,
		// NativeScriptDateTimePickerModule,
		SharedModule,
		RenderLayoutRoutingModule,
		NativeScriptFormsModule,
		ReactiveFormsModule
	],
	declarations: [
		MobileRenderListLayoutComponent,
		//  RenderListLayoutNewComponent,
		// RenderDetailNewComponent,
		// MergeEntriesSidenavComponent,
	],
	providers: [
		ModulesService,
		RenderLayoutService,
		MatDialogHelper,
		MatSnackBarHelper,
		Feedback,
	]
	
})
export class RenderLayoutModule {}
