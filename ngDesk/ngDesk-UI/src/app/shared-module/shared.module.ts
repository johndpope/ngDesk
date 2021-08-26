import { DragDropModule } from '@angular/cdk/drag-drop';
import { CdkTableModule } from '@angular/cdk/table';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
	OwlDateTimeModule,
	OwlNativeDateTimeModule,
} from '@danielmoncada/angular-datetime-picker';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { DragDropFieldsComponent } from '@src/app/custom-components/drag-drop-fields/drag-drop-fields.component';
import { FeatureDescriptionCardComponent } from '@src/app/custom-components/feature-description-card/feature-description-card.component';
import { NgxTinymceModule } from 'ngx-tinymce';
import { CompanySettingService } from '../company-settings/company-settings.service';
import { CustomizationComponent } from '../company-settings/customization/customization.component';
import { ConditionsComponent } from '../custom-components/conditions/conditions.component';
import { ConditionsService } from '../custom-components/conditions/conditions.service';
import { FilterRuleOptionPipe } from '../custom-components/conditions/filter-rule-option/filter-rule-option.pipe';
import { LoaderComponent } from '../custom-components/loader/loader.component';
import { NewNameDescriptionComponent } from '../custom-components/new-name-description/new-name-description.component';
import { SearchBarComponent } from '../custom-components/search-bar/search-bar.component';
import { SlidingViewPanelComponent } from '../custom-components/sliding-view-panel/sliding-view-panel.component';
import { TitleBarComponent } from '../custom-components/title-bar/title-bar.component';
import { UserProfileIconComponent } from '../custom-components/user-profile-icon/user-profile-icon.component';
import { CustomTableComponent } from '../custom-table/custom-table.component';
import { CustomTableService } from '../custom-table/custom-table.service';
import { ChatBusinessRuleDialogComponent } from '../dialogs/chat-business-rule-dialog/chat-business-rule-dialog.component';
import { LearnMoreDialogComponent } from '../dialogs/learn-more-dialog/learn-more-dialog.component';
import { AutocompleteScrollDirective } from '../directives/autocomplete-scroll.directive';
import { HighlightDirective } from '../directives/highlight/highlight';
import { DynamicComponent } from '../dynamic/dynamic.component';
import { GettingStartedComponent } from '../getting-started/getting-started.component';
import { ChatGettingStartedComponent } from '../modules/modules-detail/channels/chat-widgets/chat-getting-started/chat-getting-started.component';
import { GooglePlaceComponent } from '../render-layout/google-places/google-places.component';
import { AllowStylesPipe } from '../shared/allow-styles/allow-styles.pipe';
import { BooleanToYesNoPipe } from '../shared/boolean-to-yes-no/boolean-to-yes-no.pipe';
import { DateFormatPipe } from '../shared/date-format/date-format.pipe';
import { DisableManyToManyPipe } from '../shared/disable-many-to-many-options/disable-many-to-many-options.pipe';
import { DisableDiscoveryMapPipe } from '../shared/disable-discovery-map-options/disable-discovery-map-options.pipe';
import { DisableToEnablePipe } from '../shared/disable-to-enable/disable-to-enable.pipe';
import { FilePreviewOverlayService } from '../shared/file-preview-overlay/file-preview-overlay.service';
import { FilterPipe } from '../shared/filter/filter.pipe';
import { FirstLetterPipe } from '../shared/first-letter/first-letter.pipe';
import { ModuleidToNamePipe } from '../shared/moduleid-to-name/moduleid-to-name.pipe';
import { Nl2brPipe } from '../shared/nl2br/nl2br.pipe';
import { LocalNumberPipe } from '../shared/number-format/number-format.pipe';
import { ReportFilterFieldsPipe } from '../shared/report-filter-fields/report-filter-fields.pipe';
import { ReversePipe } from '../shared/reverse-array/reverse-array.pipe';
import { TruncatePipe } from '../shared/truncate/truncate.pipe';
import { ToolbarComponent } from '../toolbar/toolbar.component';
import {ClipboardModule} from '@angular/cdk/clipboard';
import { CustomMatcardComponent } from '../custom-components/custom-matcard/custom-matcard.component';

// AoT requires an exported function for factories (translations)
export function HttpLoaderFactory(httpClient: HttpClient) {
	return new TranslateHttpLoader(httpClient);
}

@NgModule({
	declarations: [
		GooglePlaceComponent,
		GettingStartedComponent,
		ChatBusinessRuleDialogComponent,
		ChatGettingStartedComponent,
		CustomizationComponent,
		DynamicComponent,
		TitleBarComponent,
		NewNameDescriptionComponent,
		CustomTableComponent,
		ConditionsComponent,
		FeatureDescriptionCardComponent,
		FilterRuleOptionPipe,
		DisableManyToManyPipe,
		DisableDiscoveryMapPipe,
		SlidingViewPanelComponent,
		DateFormatPipe,
		LocalNumberPipe,
		UserProfileIconComponent,
		FirstLetterPipe,
		ReversePipe,
		FilterPipe,
		Nl2brPipe,
		TruncatePipe,
		AllowStylesPipe,
		ModuleidToNamePipe,
		HighlightDirective,
		LearnMoreDialogComponent,
		ReportFilterFieldsPipe,
		HighlightDirective,
		BooleanToYesNoPipe,
		ToolbarComponent,
		SearchBarComponent,
		DisableToEnablePipe,
		LoaderComponent,
		AutocompleteScrollDirective,
		DragDropFieldsComponent,
		CustomMatcardComponent
	],
	imports: [
		MatSelectModule,
		MatExpansionModule,
		MatSlideToggleModule,
		MatCardModule,
		MatButtonToggleModule,
		MatTabsModule,
		MatFormFieldModule,
		MatRadioModule,
		MatStepperModule,
		TranslateModule,
		MatCheckboxModule,
		MatChipsModule,
		MatDividerModule,
		MatProgressBarModule,
		MatDialogModule,
		MatAutocompleteModule,
		MatProgressSpinnerModule,
		MatSortModule,
		MatButtonModule,
		MatIconModule,
		MatInputModule,
		MatListModule,
		DragDropModule,
		FormsModule,
		ReactiveFormsModule,
		FlexLayoutModule,
		CommonModule,
		MatTableModule,
		CdkTableModule,
		MatPaginatorModule,
		MatMenuModule,
		NgxTinymceModule.forRoot({
			baseURL: '/assets/tinymce/',
		}),
		TranslateModule.forChild({
			loader: {
				provide: TranslateLoader,
				useFactory: HttpLoaderFactory,
				deps: [HttpClient],
			},
		}),
		OwlDateTimeModule,
		OwlNativeDateTimeModule,
		MatDividerModule,
		MatTooltipModule,
		MatBadgeModule,
		MatSliderModule,
		MatSnackBarModule,
		ClipboardModule
	],
	exports: [
		GooglePlaceComponent,
		GettingStartedComponent,
		ChatBusinessRuleDialogComponent,
		ChatGettingStartedComponent,
		CustomizationComponent,
		DynamicComponent,
		MatPaginatorModule,
		MatSortModule,
		MatAutocompleteModule,
		MatStepperModule,
		TranslateModule,
		MatCheckboxModule,
		MatProgressSpinnerModule,
		MatTableModule,
		TitleBarComponent,
		NewNameDescriptionComponent,
		CustomTableComponent,
		ConditionsComponent,
		FeatureDescriptionCardComponent,
		FilterRuleOptionPipe,
		DisableManyToManyPipe,
		DisableDiscoveryMapPipe,
		SlidingViewPanelComponent,
		DateFormatPipe,
		LocalNumberPipe,
		UserProfileIconComponent,
		FirstLetterPipe,
		ReversePipe,
		FilterPipe,
		Nl2brPipe,
		TruncatePipe,
		AllowStylesPipe,
		ModuleidToNamePipe,
		ReportFilterFieldsPipe,
		OwlDateTimeModule,
		OwlNativeDateTimeModule,
		HighlightDirective,
		NgxTinymceModule,
		BooleanToYesNoPipe,
		ToolbarComponent,
		SearchBarComponent,
		DisableToEnablePipe,
		LoaderComponent,
		AutocompleteScrollDirective,
		DragDropFieldsComponent,
		ClipboardModule,
		CustomMatcardComponent
	],
	providers: [
		DatePipe,
		CustomTableService,
		ConditionsService,
		DateFormatPipe,
		LocalNumberPipe,
		FilePreviewOverlayService,
		CompanySettingService,
	],
	schemas: [CUSTOM_ELEMENTS_SCHEMA],
	// entryComponents: [LearnMoreDialogComponent, ChatBusinessRuleDialogComponent],
})
export class SharedModule {}
