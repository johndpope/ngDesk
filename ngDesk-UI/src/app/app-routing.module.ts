import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MyActionItemsComponent } from '@src/app/my-action-items/my-action-items.component';
import { ConditionsComponent } from './custom-components/conditions/conditions.component';
// import { DashboardsMasterComponent } from './dashboards-new/dashboards-master/dashboards-master.component';
import { DownloadInstallerComponent } from './download-installer/download-installer.component';
import { EmailVerifyComponent } from './email-verify/email-verify.component';
import { EscalationsDetailComponent } from './escalations/escalations-detail/escalations-detail.component';
import { EscalationsMasterComponent } from './escalations/escalations-master/escalations-master.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { GettingStartedComponent } from './getting-started/getting-started.component';
import { GuideGuard } from './guide.guard';
import { LoginComponent } from './login/login.component';
import { ManageInvitesComponent } from './manage-invites/manage-invites.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { SignupComponent } from './signup/signup.component';
// import { StoryboardComponent } from './storyboard/storyboard.component';
import { SubdomainComponent } from './subdomain/subdomain.component';
import { UnsubscribeToMarketingEmailComponent } from './unsubscribe-to-marketing-email/unsubscribe-to-marketing-email.component';
import { DashboardsRenderComponent } from '@src/app/dashboards-new/dashboards-render/dashboards-render.component';
import { DashboardsDetailComponent } from '@src/app/dashboards-new/dashboards-detail/dashboards-detail.component';
import { DocumentViewerComponent } from './document-viewer/document-viewer.component';
import { DashboardEntriesComponent } from './dashboard-entries/dashboard-entries.component';
import { EventsComponent } from './render-layout/events/events.component';

const routes: Routes = [
	{
		path: '',
		loadChildren: () =>
			import('./guide/guide.module').then((m) => m.GuideModule),
		canActivate: [GuideGuard],
	},
	{ path: 'login', component: LoginComponent },
	{ path: 'login-support', component: SubdomainComponent },
	{ path: 'reset-password', component: ResetPasswordComponent },
	{ path: 'create-password', component: ResetPasswordComponent },
	{ path: 'forgot-password', component: ForgotPasswordComponent },
	{ path: 'signup', component: SignupComponent },
	// { path: 'dashboards', component: StoryboardComponent },
	{ path: 'dashboards', component: DashboardsRenderComponent },
	{ path: 'dashboards/entries/:id/:widgetId/:value', component: DashboardEntriesComponent },
	{ path: 'dashboards/:id', component: DashboardsDetailComponent },
	{ path: 'my-action-items', component: MyActionItemsComponent },
	{ path: 'document-viewer/:templateId', component: DocumentViewerComponent },
	{
		path: 'guide',
		loadChildren: () =>
			import('./guide/guide.module').then((m) => m.GuideModule),
	},
	{ path: 'conditions', component: ConditionsComponent },
	{
		path: 'getting-started/:moduleId',
		component: GettingStartedComponent,
	},
	{
		path: 'escalations',
		component: EscalationsMasterComponent,
	},
	{
		path: 'escalations/:escalationId',
		component: EscalationsDetailComponent,
	},
	{
		path: 'company-settings',
		loadChildren: () =>
			import('./company-settings/company-settings.module').then(
				(m) => m.CompanySettingsModule
			),
	},
	{
		path: 'render',
		loadChildren: () =>
			import('./render-layout/render-layout.module').then(
				(m) => m.RenderLayoutModule
			),
	},
	{
		path: 'schedules',
		loadChildren: () =>
			import('./schedules/schedules.module').then((m) => m.SchedulesModule),
	},
	{ path: 'email/verify', component: EmailVerifyComponent },
	{ path: 'manage-invites', component: ManageInvitesComponent },
	{
		path: 'reports',
		loadChildren: () =>
			import('./reports/reports.module').then((m) => m.ReportsModule),
	},
	{
		path: 'modules',
		loadChildren: () =>
			import('./modules/modules.module').then((m) => m.ModulesModule),
	},
	{
		path: 'guide',
		loadChildren: () =>
			import('./guide/guide.module').then((m) => m.GuideModule),
	},
	{
		path: 'unsubscribe-to-marketing-email',
		component: UnsubscribeToMarketingEmailComponent,
	},
	{
		path: 'sam/installer/download',
		component: DownloadInstallerComponent,
	},
	{ path: 'render/:moduleId/:type/:dataId/events', component: EventsComponent },
	// {
	// 	path: 'dashboards-new',
	// 	component: DashboardsMasterComponent,
	// },
	// {
	// 	path: 'dashboards-new/:dashboardId',
	// 	component: DashboardsRenderComponent,
	// },
];

@NgModule({
	imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
	exports: [RouterModule],
})
export class AppRoutingModule {}
