import { OverlayModule } from '@angular/cdk/overlay';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatRippleModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSortModule } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { ModulesService } from '@src/app/modules/modules.service';
import { SharedModule } from '@src/app/shared-module/shared.module';
import { RenderLayoutRoutingModule } from '@src/app/render-layout/render-layout-routing.module';
import { RenderListLayoutNewComponent } from '@src/app/render-layout/render-list-layout-new/render-list-layout-new.component';
import { EventsComponent } from '@src/app/render-layout/events/events.component';
import { CommonLayoutService } from '@src/app/render-layout/render-detail-new/common-layout.service';
import { GridLayoutService } from '@src/app/render-layout/render-detail-new/grid-layout.service';
import { PredefinedTemplateService } from '@src/app/render-layout/render-detail-new/predefined-template.service';
import { RenderDetailDataService } from '@src/app/render-layout/render-detail-new/render-detail-data.service';
import { RenderLayoutService } from '@src/app/render-layout/render-layout.service';
import { MobileRenderListLayoutComponent } from '@src/app/render-layout/mobile-render-list-layout/mobile-render-list-layout.component';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { MatDialogHelper } from '@src/app/render-layout/dialog-snackbar-helper/matdialog-helper';
import { MatSnackBarHelper } from '@src/app/render-layout/dialog-snackbar-helper/matsnackbar-helper';
import {
	OwlMomentDateTimeModule,
	OWL_DATE_TIME_FORMATS,
} from '@danielmoncada/angular-datetime-picker';
import {
	OwlDateTimeModule,
	OwlNativeDateTimeModule,
} from '@danielmoncada/angular-datetime-picker';
import { AutoNumberService } from '@src/app/render-layout/data-types/auto-number.service';
import { ButtonService } from '@src/app/render-layout/data-types/button.service';
import { PasswordService } from '@src/app/render-layout/data-types/password.service';
import { CheckboxService } from '@src/app/render-layout/data-types/checkbox.service';
import { ChronometerService } from '@src/app/render-layout/data-types/chronometer.service';
import { CityService } from '@src/app/render-layout/data-types/city.service';
import { CountryService } from '@src/app/render-layout/data-types/country.service';
import { CurrencyService } from '@src/app/render-layout/data-types/currency.service';
import {
	DateTimeService,
	OWL_DATE_FORMATS,
} from '@src/app/render-layout/data-types/date-time.service';
import { DateService } from '@src/app/render-layout/data-types/date.service';
import { DiscussionService } from '@src/app/render-layout/data-types/discussion.service';
import { FileUploadService } from '@src/app/render-layout/data-types/file-upload.service';
import { FormulaService } from '@src/app/render-layout/data-types/formula.service';
import { ListTextService } from '@src/app/render-layout/data-types/list-text.service';
import { NumberService } from '@src/app/render-layout/data-types/number.service';
import { PhoneService } from '@src/app/render-layout/data-types/phone.service';
import { PicklistService } from '@src/app/render-layout/data-types/picklist.service';
import { PicklistMultiselectService } from '@src/app/render-layout/data-types/picklistMultiselect.service';
import { RelationshipService } from '@src/app/render-layout/data-types/relationship.service';
import { Street1Service } from '@src/app/render-layout/data-types/street1.service';
import { Street2Service } from '@src/app/render-layout/data-types/street2.service';
import { TextAreaService } from '@src/app/render-layout/data-types/text-area.service';
import { TextService } from '@src/app/render-layout/data-types/text.service';
import { TimeService } from '@src/app/render-layout/data-types/time.service';
import { MergeEntriesSidenavComponent } from '@src/app/render-layout/merge-entries-sidenav/merge-entries-sidenav.component';
import { RenderDetailNewComponent } from '@src/app/render-layout/render-detail-new/render-detail-new.component';
import { CreateUserComponent } from '../dialogs/create-user/create-user.component';
import { NumericFormatPipe } from '../render-layout/render-list-layout-new/numeric-format.pipe';
import { RenderFormsComponent } from './render-forms/render-forms.component';
import { RenderFormsService } from './render-forms/render-forms.service';
import { FormsPhoneService } from './forms-datatypes/forms-phone.service';
import { FormsLayoutService } from './render-forms/forms-layout.service';
import { FormsNumberService } from './forms-datatypes/forms-number.service';
import { FormsDateTimeService } from './forms-datatypes/forms-date-time.service';
import { FormsDateService } from './forms-datatypes/forms-date.service';
import { FormsDiscussionService } from './forms-datatypes/forms-discussion.service';
import { FormsListTextService } from './forms-datatypes/forms-list-text.service';
import { FormsPicklistService } from './forms-datatypes/forms-picklist.service';
import { FormsRelationshipService } from './forms-datatypes/forms-relationship.service';
import { FormsTextAreaService } from './forms-datatypes/forms-text-area.service';
import { FormsTextService } from './forms-datatypes/forms-text.service';
import { FormsTimeService } from './forms-datatypes/forms-time.service';
import { CatalogueListComponent } from './catalogue-list/catalogue-list.component';
import { CatalogueDetailComponent } from './catalogue-detail/catalogue-detail.component';
import { FormGridLayoutService } from './render-forms/forms-grid-layout.service';

@NgModule({
	imports: [
		MatTabsModule,
		MatAutocompleteModule,
		MatIconModule,
		MatDialogModule,
		SharedModule,
		CommonModule,
		MatRippleModule,
		RenderLayoutRoutingModule,
		MatSortModule,
		FlexLayoutModule,
		MatButtonModule,
		MatDividerModule,
		MatListModule,
		MatCardModule,
		MatChipsModule,
		MatInputModule,
		MatSelectModule,
		FormsModule,
		ReactiveFormsModule,
		OverlayModule,
		ScrollingModule,
		MatMenuModule,
		MatTooltipModule,
		MatExpansionModule,
		MatSidenavModule,
		OwlDateTimeModule,
		OwlNativeDateTimeModule,
		OwlMomentDateTimeModule,
		MatSlideToggleModule,
	],
	declarations: [
		MobileRenderListLayoutComponent,
		RenderListLayoutNewComponent,
		EventsComponent,
		RenderDetailNewComponent,
		MergeEntriesSidenavComponent,
		CreateUserComponent,
		NumericFormatPipe,
		RenderFormsComponent,
		CatalogueListComponent,
		CatalogueDetailComponent,
	],
	providers: [
		CustomTableService,
		ModulesService,
		RenderLayoutService,
		RenderDetailDataService,
		PredefinedTemplateService,
		GridLayoutService,
		FormGridLayoutService,
		CommonLayoutService,
		MatDialogHelper,
		MatSnackBarHelper,
		{ provide: OWL_DATE_TIME_FORMATS, useValue: OWL_DATE_FORMATS },
		Street1Service,
		Street2Service,
		CityService,
		CountryService,
		AutoNumberService,
		ButtonService,
		PasswordService,
		CheckboxService,
		ChronometerService,
		CurrencyService,
		DateTimeService,
		DateService,
		FileUploadService,
		FormulaService,
		ListTextService,
		NumberService,
		PhoneService,
		PicklistService,
		PicklistMultiselectService,
		RelationshipService,
		TextAreaService,
		TextService,
		TimeService,
		DiscussionService,
		NumericFormatPipe,
		RenderFormsService,
		FormsLayoutService,
		FormsTextService,
		FormsPicklistService,
		FormsPhoneService,
		FormsRelationshipService,
		FormsListTextService,
		FormsNumberService,
		FormsDateTimeService,
		FormsTimeService,
		FormsDiscussionService,
		FormsTextAreaService,
		FormsDateService,
	],
	// entryComponents: [],
})
export class RenderLayoutModule {}
