import { DragDropModule } from '@angular/cdk/drag-drop';
import {
	HttpClient,
	HttpClientModule,
	HTTP_INTERCEPTORS,
} from '@angular/common/http';
import {
	APP_INITIALIZER,
	CompilerFactory,
	ErrorHandler,
	NgModule,
} from '@angular/core';
import { AngularFireModule } from '@angular/fire';
import { AngularFireAuthModule } from '@angular/fire/auth';
import { AngularFireMessagingModule } from '@angular/fire/messaging';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BrowserModule } from '@angular/platform-browser';
import { JitCompilerFactory } from '@angular/platform-browser-dynamic';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BASE_PATH as DATASERVICE_BASE_PATH } from '@ngdesk/data-api';
import { BASE_PATH as ESCALATION_BASE_PATH } from '@ngdesk/escalation-api';
import { BASE_PATH as MODULE_BASE_PATH } from '@ngdesk/module-api';
import { BASE_PATH as PAYMENT_BASE_PATH } from '@ngdesk/payment-api';
import { BASE_PATH as ROLE_BASE_PATH } from '@ngdesk/role-api';
import { BASE_PATH as SAM_BASE_PATH } from '@ngdesk/sam-api';
import { BASE_PATH as SIDEBAR_BASE_PATH } from '@ngdesk/sidebar-api';
import { BASE_PATH as WORKFLOW_BASE_PATH } from '@ngdesk/workflow-api';
import { BASE_PATH as AUTH_BASE_PATH } from '@ngdesk/auth-api';
import { BASE_PATH as ZOOM_BASE_PATH } from '@ngdesk/integration-api';
import { BASE_PATH as REPORT_BASE_PATH } from '@ngdesk/report-api'
import { BASE_PATH as NOTIFICATION_BASE_PATH } from '@ngdesk/notification-api'
import { BASE_PATH as COMPANY_BASE_PATH } from '@ngdesk/company-api'
import {
	MissingTranslationHandler,
	TranslateLoader,
	TranslateModule,
} from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { AppRoutingModule } from '@src/app/app-routing.module';
import { AppUpdateService } from '@src/app/app-update.service';
import { AppComponent } from '@src/app/app.component';
import { CustomErrorHandler } from '@src/app/app.custom-error-handler';
import { AppGlobals } from '@src/app/app.globals';
import { AttachmentsService } from '@src/app/attachments/attachments.service';
import { CacheService } from '@src/app/cache.service';
import { GraphqlListLayoutService } from '@src/app/graphql-list-layout.service';
import { CompaniesService } from '@src/app/companies/companies.service';
import { ConfigService } from '@src/app/config.service';
import { BannerMessageComponent } from '@src/app/custom-components/banner-message/banner-message.component';
import { CustomMissingTranslationHandler } from '@src/app/custom-missing-translation.handler';
import { ChangePasswordDialogComponent } from '@src/app/dialogs/change-password-dialog/change-password-dialog.component';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { CloneDialogComponent } from '@src/app/dialogs/clone-dialog/clone-dialog.component';
import { UnsubscribeDialogComponent } from '@src/app/dialogs/unsubscribe-dialog/unsubscribe-dialog.component';
import { EditModuleDialogComponent } from '@src/app/dialogs/edit-module-dialog/edit-module-dialog.component';
import { InviteUsersDialogComponent } from '@src/app/dialogs/invite-users-dialog/invite-users-dialog.component';
import { LoadingDialogComponent } from '@src/app/dialogs/loading-dialog/loading-dialog.component';
import { PanelSettingsDialogComponent } from '@src/app/dialogs/panel-settings-dialog/panel-settings-dialog.component';
import { ReferralEmailDialogComponent } from '@src/app/dialogs/referral-email-dialog/referral-email-dialog.component';
import { ScheduleReportsDialogComponent } from '@src/app/dialogs/schedule-reports-dialog/schedule-reports-dialog.component';
import { SignupQuestionsDialogComponent } from '@src/app/dialogs/signup-questions-dialog/signup-questions-dialog.component';
import { WalkthroughDialogComponent } from '@src/app/dialogs/walkthrough-dialog/walkthrough-dialog.component';
import { DownloadInstallerComponent } from '@src/app/download-installer/download-installer.component';
import { EmailVerifyComponent } from '@src/app/email-verify/email-verify.component';
import { EscalationsDetailComponent } from '@src/app/escalations/escalations-detail/escalations-detail.component';
import { EscalationsMasterComponent } from '@src/app/escalations/escalations-master/escalations-master.component';
// import { DashboardsMasterComponent } from '@src/app/dashboards-new/dashboards-master/dashboards-master.component';
import { MessagingService } from '@src/app/firebase/messaging.service';
import { ForgotPasswordComponent } from '@src/app/forgot-password/forgot-password.component';
import { GuideService } from '@src/app/guide/guide.service';
// import { ListLayoutsDetailComponent } from './list-layouts/list-layouts-detail/list-layouts-detail.component';
// import { ListLayoutsMasterComponent } from './list-layouts/list-layouts-master/list-layouts-master.component';
import { LoginComponent } from '@src/app/login/login.component';
import { ManageInvitesComponent } from '@src/app/manage-invites/manage-invites.component';
import { MobileUserLogoutComponent } from '@src/app/mobile-user-logout/mobile-user-logout.component';
import { ModuleSidebarComponent } from '@src/app/module-sidebar/module-sidebar.component';
import { SlaBusinessRulesComponent } from '@src/app/modules/modules-detail/slas/sla-detail/sla-business-rules/sla-business-rules.component';
import { ModulesService } from '@src/app/modules/modules.service';
import { MyActionItemsComponent } from '@src/app/my-action-items/my-action-items.component';
import { NotificationsService } from '@src/app/notifications/notifications.service';
import { AngularFireMessagingHelper } from '@src/app/ns-local-storage/angular-fire-messaging-helper';
import { AutocompleteService } from '@src/app/render-layout/autocomplete/autocomplete.service';
import { RenderListLayoutService } from '@src/app/render-layout/render-list-layout-new/render-list-layout.service';
import { ResetPasswordComponent } from '@src/app/reset-password/reset-password.component';
import { SharedModule } from '@src/app/shared-module/shared.module';
import { FilePreviewOverlayService } from '@src/app/shared/file-preview-overlay/file-preview-overlay.service';
import { FILE_PREVIEW_DIALOG_DATA } from '@src/app/shared/file-preview-overlay/file-preview-overlay.tokens';
import { SafePipe } from '@src/app/shared/safe-pipe/safe-pipe.pipe';
import { SignupComponent } from '@src/app/signup/signup.component';
import { BarChartComponent } from '@src/app/storyboard/bar-chart/bar-chart.component';
import { ScoreCardComponent } from '@src/app/storyboard/score-card/score-card.component';
import { StoryboardItemDirective } from '@src/app/storyboard/storyboard-item.directive';
import { StoryboardComponent } from '@src/app/storyboard/storyboard.component';
import { SubdomainComponent } from '@src/app/subdomain/subdomain.component';
import { ThemeService } from '@src/app/theme.service';
import { UnsubscribeToMarketingEmailComponent } from '@src/app/unsubscribe-to-marketing-email/unsubscribe-to-marketing-email.component';
import { UsersSidebarComponent } from '@src/app/users-sidebar/users-sidebar.component';
import { ApiInterceptor } from '@src/app/users/api.interceptor';
import { UsersService } from '@src/app/users/users.service';
import { WalkthroughService } from '@src/app/walkthrough/walkthrough.service';

import { NgxChartsModule } from '@swimlane/ngx-charts';
import { GridsterModule } from 'angular-gridster2';
// import { MatFileUploadModule } from 'angular-material-fileupload';
import { ChartsModule } from 'ng2-charts';
import { CookieService } from 'ngx-cookie-service';
import { LoggerModule, NgxLoggerLevel } from 'ngx-logger';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import { BASE_PATH as ATTACHMENT_BASE_PATH } from '@ngdesk/data-api';
import { RenderListHelper } from '@src/app/render-layout/render-list-helper/render-list-helper';
import { MatDialogHelper } from '@src/app/render-layout/dialog-snackbar-helper/matdialog-helper';
import { RenderDetailHelper } from '@src/app/render-layout/render-detail-helper/render-detail-helper';
import { ZoomIntegrationDialogComponent } from '@src/app/dialogs/zoom-integration-dialog/zoom-integration-dialog.component';
import { ZoomIntegrationFailedDialogComponent } from '@src/app/dialogs/zoom-integration-failed-dialog/zoom-integration-failed-dialog.component';
import { FeatureRolledOutDialogComponent } from '@src/app/dialogs/feature-rolled-out-dialog/feature-rolled-out-dialog.component';
import { DashboardsRenderComponent } from '@src/app/dashboards-new/dashboards-render/dashboards-render.component';
import { NbThemeService, NbCardModule } from '@nebular/theme';
import { DashboardsDetailComponent } from '@src/app/dashboards-new/dashboards-detail/dashboards-detail.component';
import { ConditionsDialogComponent } from './dialogs/conditions-dialog/conditions-dialog.component';
import { DocumentViewerComponent } from './document-viewer/document-viewer.component';
import { SignaturePadComponent } from './dialogs/signature-pad/signature-pad.component';
import { DashboardEntriesComponent } from './dashboard-entries/dashboard-entries.component';
import { ApprovalRejectDialogComponent  } from './dialogs/approval-reject-dialog/approval-reject-dialog.component';
import { ApprovalRejectInformationDialogComponent } from './dialogs/approval-reject-information-dialog/approval-reject-information-dialog.component';
import { OneToManyDialogComponent } from './dialogs/one-to-many-dialog/one-to-many-dialog.component';
import { ChatBusinessRulesComponent } from './company-settings/chat-settings/chat-general-settings/chat-business-rules/chat-business-rules.component';
import { ToolbarService } from './toolbar/toolbar.service';

export function createJitCompiler() {
	return new JitCompilerFactory().createCompiler();
}

// AoT requires an exported function for factories (translations)
export function HttpLoaderFactory(httpClient: HttpClient) {
	return new TranslateHttpLoader(httpClient);
}

export function I18nHttpLoaderFactory(httpClient: HttpClient) {
	return new TranslateHttpLoader(httpClient, `./assets/i18n/`, '.json');
}

const appConfig = (config: ConfigService) => {
	return () => {
		return config.loadConfig();
	};
};

@NgModule({
	declarations: [
		AppComponent,
		LoginComponent,
		ModuleSidebarComponent,
		EmailVerifyComponent,
		EscalationsDetailComponent,
		SignupComponent,
		ScheduleReportsDialogComponent,
		EscalationsMasterComponent,
		// DashboardsMasterComponent,
		ForgotPasswordComponent,
		ResetPasswordComponent,
		ConfirmDialogComponent,
		UnsubscribeDialogComponent,
		CloneDialogComponent,
		EditModuleDialogComponent,
		SubdomainComponent,
		UsersSidebarComponent,
		SignupQuestionsDialogComponent,
		ManageInvitesComponent,
		BannerMessageComponent,
		InviteUsersDialogComponent,
		LoadingDialogComponent,
		WalkthroughDialogComponent,
		ScheduleReportsDialogComponent,
		SlaBusinessRulesComponent,
		ChangePasswordDialogComponent,
		UnsubscribeToMarketingEmailComponent,
		PanelSettingsDialogComponent,
		ReferralEmailDialogComponent,
		StoryboardComponent,
		SafePipe,
		ScoreCardComponent,
		BarChartComponent,
		StoryboardItemDirective,
		DownloadInstallerComponent,
		MobileUserLogoutComponent,
		MyActionItemsComponent,
		ZoomIntegrationDialogComponent,
		ZoomIntegrationFailedDialogComponent,
		FeatureRolledOutDialogComponent,
		DashboardsRenderComponent,
		DashboardsDetailComponent,
		ConditionsDialogComponent,
		DocumentViewerComponent,
		SignaturePadComponent,
		DashboardEntriesComponent,
		ApprovalRejectDialogComponent,
		ApprovalRejectInformationDialogComponent,
  		OneToManyDialogComponent,
		ChatBusinessRulesComponent
	],
	imports: [
		NbCardModule,
		GridsterModule,
		NgxChartsModule,
		BrowserAnimationsModule,
		MatProgressBarModule,
		MatExpansionModule,
		MatProgressSpinnerModule,
		MatFormFieldModule,
		MatStepperModule,
		DragDropModule,
		MatAutocompleteModule,
		SharedModule,
		BrowserModule,
		AppRoutingModule,
		FormsModule,
		ReactiveFormsModule,
		MatButtonModule,
		MatButtonToggleModule,
		MatCheckboxModule,
		MatChipsModule,
		MatIconModule,
		MatInputModule,
		MatCardModule,
		MatDialogModule,
		MatListModule,
		MatSlideToggleModule,
		MatNativeDateModule,
		MatRadioModule,
		MatRippleModule,
		MatSelectModule,
		MatSidenavModule,
		MatTooltipModule,
		AngularFireAuthModule,
		AngularFireModule,
		AngularFireMessagingModule,
		FlexLayoutModule,
		HttpClientModule,
		TranslateModule.forRoot({
			missingTranslationHandler: {
				provide: MissingTranslationHandler,
				useClass: CustomMissingTranslationHandler,
			},
			loader: {
				provide: TranslateLoader,
				useFactory: HttpLoaderFactory,
				deps: [HttpClient],
			},
		}),
		LoggerModule.forRoot({
			// serverLoggingUrl: '/ngdesk-rest/ngdesk/log',
			level: NgxLoggerLevel.DEBUG,
			// serverLogLevel: NgxLoggerLevel.TRACE
		}),
		MatToolbarModule,
		MatMenuModule,
		SharedModule,
		MatTabsModule,
		// MatFileUploadModule,
		ChartsModule,
		NgxMatSelectSearchModule,
		AngularFireModule.initializeApp({
			apiKey: 'AIzaSyCnd-iqFUtF72-dsY022NpUHNy6xjKmEJ8',
			authDomain: 'ngdesk2.firebaseapp.com',
			databaseURL: 'https://ngdesk2.firebaseio.com',
			projectId: 'ngdesk2',
			storageBucket: 'ngdesk2.appspot.com',
			messagingSenderId: '200186710529',
			appId: '1:200186710529:web:e0f3c75605981e49',
		}),
	],
	providers: [
		NbThemeService,
		ConfigService,
		{
			provide: APP_INITIALIZER,
			useFactory: appConfig,
			multi: true,
			deps: [ConfigService],
		},
		UsersService,
		AppGlobals,
		CookieService,
		CacheService,
		ToolbarService,
		GraphqlListLayoutService,
		RenderListLayoutService,
		CompaniesService,
		AttachmentsService,
		MessagingService,
		MatDialogHelper,
		RenderListHelper,
		RenderDetailHelper,
		// AsyncPipe,
		ModulesService,
		{
			provide: HTTP_INTERCEPTORS,
			useClass: ApiInterceptor,
			multi: true,
		},
		// { provide: HAMMER_GESTURE_CONFIG, useClass: GestureConfig },
		// RxStompService,
		AutocompleteService,
		FilePreviewOverlayService,
		WalkthroughService,
		GuideService,
		NotificationsService,
		ThemeService,
		AngularFireMessagingHelper,
		AppUpdateService,
		{ provide: ErrorHandler, useClass: CustomErrorHandler },
		{ provide: FILE_PREVIEW_DIALOG_DATA, useValue: FILE_PREVIEW_DIALOG_DATA },
		{
			provide: ESCALATION_BASE_PATH,
			useValue: '/api/ngdesk-escalation-service-v1',
		},
		{
			provide: SIDEBAR_BASE_PATH,
			useValue: '/api/ngdesk-sidebar-service-v1',
		},
		{
			provide: SAM_BASE_PATH,
			useValue: '/api/ngdesk-sam-service-v1',
		},
		{ provide: CompilerFactory, useFactory: createJitCompiler },
		{
			provide: DATASERVICE_BASE_PATH,
			useValue: '/api/ngdesk-data-service-v1',
		},
		{
			provide: PAYMENT_BASE_PATH,
			useValue: '/api/ngdesk-payment-service-v1',
		},
		{
			provide: WORKFLOW_BASE_PATH,
			useValue: '/api/ngdesk-workflow-service-v1',
		},
		{
			provide: ROLE_BASE_PATH,
			useValue: '/api/ngdesk-role-service-v1',
		},
		{
			provide: ATTACHMENT_BASE_PATH,
			useValue: '/api/ngdesk-data-service-v1',
		},
		{
			provide: MODULE_BASE_PATH,
			useValue: '/api/ngdesk-module-service-v1',
		},
		{
			provide: AUTH_BASE_PATH,
			useValue: '/api/ngdesk-auth-service-v1',
		},
		{
			provide: ZOOM_BASE_PATH,
			useValue: '/api/ngdesk-integration-service-v1',
		},
		{
			provide: REPORT_BASE_PATH,
			useValue: '/api/ngdesk-report-service-v1',
		},
		{
			provide: NOTIFICATION_BASE_PATH,
			useValue: '/api/ngdesk-notification-service-v1',
		},
		{
			provide: COMPANY_BASE_PATH,
			useValue: '/api/ngdesk-company-service-v1',
		}
	],
	bootstrap: [AppComponent],
	// entryComponents: [
	// 	InviteUsersDialogComponent,
	// 	ConfirmDialogComponent,
	// 	SignupQuestionsDialogComponent,
	// 	LoadingDialogComponent,
	// 	WalkthroughDialogComponent,
	// 	ScheduleReportsDialogComponent,
	// 	SlaBusinessRulesComponent,
	// 	EditModuleDialogComponent,
	// 	ChangePasswordDialogComponent,
	// 	PanelSettingsDialogComponent,
	// 	ScoreCardComponent,
	// 	BarChartComponent,
	// 	ReferralEmailDialogComponent,
	// ],
})
export class AppModule {}
