import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { NativeScriptRouterModule } from '@nativescript/angular';
import { GuideGuard } from './guide.guard';
import { SubdomainComponent } from './subdomain/subdomain.component';
import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { ModuleSidebarComponent } from './module-sidebar/module-sidebar.component';
import { MobileUserLogoutComponent } from './mobile-user-logout/mobile-user-logout.component';
import { RenderListLayoutNewComponent } from './render-layout/render-list-layout-new/render-list-layout-new.component';
import { RenderDetailNewComponent } from '@src/app/render-layout/render-detail-new/render-detail-new.component';
import { EventsComponent } from './render-layout/events/events.component';

export const routes: Routes = [
	 { path: '', component: SubdomainComponent, canActivate: [GuideGuard] },
	{ path: 'login', component: LoginComponent },
	{ path: 'sidebar', component: ModuleSidebarComponent },
	{ path: 'render/:moduleId', component: RenderListLayoutNewComponent },
	{ path: 'render/:moduleId/:type/:dataId/events', component: EventsComponent },
	// {
	// 	path: 'render',
	// 	loadChildren: () =>
	// 		import('./render-layout/render-layout.module').then(
	// 			(m) => m.RenderLayoutModule
	// 		),
	// },
	{ path: 'render/:moduleId/:type/:dataId', component: RenderDetailNewComponent },
	{
		path: 'logout',
		component: MobileUserLogoutComponent,
	},
];
@NgModule({
	imports: [NativeScriptRouterModule.forRoot(routes)],
	exports: [NativeScriptRouterModule],
	schemas: [
        NO_ERRORS_SCHEMA
    ]
})
export class AppRoutingModule {}
