import { NgModule, NO_ERRORS_SCHEMA, ErrorHandler, APP_INITIALIZER } from '@angular/core';
import { NativeScriptModule ,NativeScriptCommonModule , NativeScriptAnimationsModule} from '@nativescript/angular';
import { NativeScriptFormsModule } from '@nativescript/angular';
import { AppRoutingModule } from '@src/app/app-routing.module.tns';
import { AppComponent } from '@src/app/app.component.tns';
import { SubdomainComponent } from '@src/app/subdomain/subdomain.component';

import { AppGlobals } from '@src/app/app.globals';
import { BASE_PATH as DATASERVICE_BASE_PATH } from '@ngdesk/data-api';
import { CompaniesService } from '@src/app/companies/companies.service';
import { CookieService } from 'ngx-cookie-service';
import {
	HTTP_INTERCEPTORS,
	HttpClient,
	HttpClientModule,
} from '@angular/common/http';
import { ApiInterceptor } from '@src/app/users/api.interceptor';
import { ThemeService } from '@src/app/theme.service';
import { NativeScriptHttpClientModule } from '@nativescript/angular';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import {
	TranslateModule,
	MissingTranslationHandler,
	TranslateLoader,
} from '@ngx-translate/core';
import { CustomMissingTranslationHandler } from '@src/app/custom-missing-translation.handler';
import {
	NGXLogger,
	NGXLoggerHttpService,
	LoggerConfig,
	LoggerModule,
	NgxLoggerLevel,
} from 'ngx-logger';
import { UsersService } from '@src/app/users/users.service';
import { DomSanitizerHelper } from '@src/app/dom-sanitizer/domsanitizer-helper-service';
import { BASE_PATH as SIDEBAR_BASE_PATH } from '@ngdesk/sidebar-api';

import { ApplicationSettings } from '@src/app/ns-local-storage/app-settings-helper';
import { OVERLAY_PROVIDERS } from '@angular/cdk/overlay';
import { RouterNavigationHelper } from '@src/app/router-navigation/router-navigation-helper';
import { AngularFireMessagingHelper } from '@src/app/ns-local-storage/angular-fire-messaging-helper';
import { Feedback } from 'nativescript-feedback';
// import { CreateUserComponent } from '@src/app/dialogs/create-user/create-user.component';
import { getString } from '@nativescript/core/application-settings';
import { ConfigService } from '@src/app/config.service';
import { ModuleSidebarComponent } from '@src/app/module-sidebar/module-sidebar.component';
import { MobileUserLogoutComponent } from '@src/app/mobile-user-logout/mobile-user-logout.component';
import { HideActionBarDirective } from '@src/app/router-navigation/HideAction';
 import { RenderListLayoutNewComponent } from '@src/app/render-layout/render-list-layout-new/render-list-layout-new.component';
import { MatDialogHelper } from '@src/app/render-layout/dialog-snackbar-helper/matdialog-helper';
import { RenderListHelper } from '@src/app/render-layout/render-list-helper/render-list-helper';
import { RenderDetailHelper } from '@src/app/render-layout/render-detail-helper/render-detail-helper';
import { LoginComponent } from '@src/app/login/login.component';
import { NativeScriptUIListViewModule } from "nativescript-ui-listview/angular";
// import { MobileRenderListLayoutComponent } from '@src/app/render-layout/mobile-render-list-layout/mobile-render-list-layout.component';
import { RenderDetailNewComponent } from '@src/app/render-layout/render-detail-new/render-detail-new.component';
// import { NativeScriptUIListViewModule } from "nativescript-ui-listview/angular";
import { DateFormatPipe } from './shared/date-format/date-format.pipe.tns';
// import { TNSFontIconModule, TNSFontIconService, USE_STORE, } from 'nativescript-ngx-fonticon';
// import { TranslateModule, TranslateLoader, TranslateStaticLoader } from "ng2-translate";

// export function HttpLoaderFactory(http: Http) {
// 	return new TranslateStaticLoader(http);
// }
//import { MatDialogModule } from '@angular/material/dialog';
export function HttpLoaderFactory(http: HttpClient) {
	return new TranslateHttpLoader(http, `/assets/i18n/`, '.json');
}

// Uncomment and add to NgModule imports if you need to use two-way binding
// import { NativeScriptFormsModule } from 'nativescript-angular/forms';

// Uncomment and add to NgModule imports  if you need to use the HTTP wrapper
// import { NativeScriptHttpClientModule } from 'nativescript-angular/http-client';

const appConfig = (config: ConfigService) => {
	return () => {
		return config.loadConfig();
	};
};


@NgModule({
	declarations: [
		DateFormatPipe,
		AppComponent,
		SubdomainComponent,
		LoginComponent,
		ModuleSidebarComponent,
		HideActionBarDirective,
		MobileUserLogoutComponent,
		//MobileRenderListLayoutComponent,
		RenderListLayoutNewComponent,
		RenderDetailNewComponent
	],
	imports: [
	//	MatDialogModule,
		NativeScriptModule,
		NativeScriptUIListViewModule,
		AppRoutingModule,
		NativeScriptFormsModule,
		NativeScriptHttpClientModule,
		NativeScriptCommonModule,
		NativeScriptAnimationsModule,
		NativeScriptUIListViewModule,
		ReactiveFormsModule,
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
	],
	providers: [
		CompaniesService,
		AppGlobals,
		UsersService,
		ThemeService,
		RenderListHelper,
		RenderDetailHelper,
		DateFormatPipe,
		MatDialogHelper,
		HttpClientModule,
		{
			provide: HTTP_INTERCEPTORS,
			useClass: ApiInterceptor,
			multi: true,
		},
		ConfigService,
		{
			provide: APP_INITIALIZER,
			useFactory: appConfig,
			multi: true,
			deps: [ConfigService],
		},
		CookieService,
		// NativeScriptSanitizer,
		DomSanitizerHelper,
		ApplicationSettings,
		RouterNavigationHelper,
		AngularFireMessagingHelper,
		{
			provide: SIDEBAR_BASE_PATH,
			useValue:
				'https://' +
				getString('subdomain') +
				'.ngdesk.com/api/ngdesk-sidebar-service-v1',
		},
		{
			provide: DATASERVICE_BASE_PATH,
			useValue: 'https://' +
			getString('subdomain') +
			'.ngdesk.com/api/ngdesk-data-service-v1',
		},
		[OVERLAY_PROVIDERS],
		Feedback,
	],
	bootstrap: [AppComponent],
	schemas: [
        NO_ERRORS_SCHEMA
    ]
})
/*
Pass your application module to the bootstrapModule function located in main.ts to start your app
*/
export class AppModule {}
