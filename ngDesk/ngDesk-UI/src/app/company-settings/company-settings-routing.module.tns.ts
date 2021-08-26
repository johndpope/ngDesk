import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ApiBuilderComponent } from './api-builder/api-builder.component';
// import { ApiKeysComponent } from './api-keys/api-keys.component';
import { BlacklistComponent } from './blacklist/blacklist.component';
import { ChatFaqDetailComponent } from './chat-settings/chat-faqs/chat-faq-detail/chat-faq-detail.component';
import { ChatFaqsComponent } from './chat-settings/chat-faqs/chat-faqs.component';
import { ChatGeneralSettingsComponent } from './chat-settings/chat-general-settings/chat-general-settings.component';
import { CnameComponent } from './cname/cname.component';
import { CompanySettingsComponent } from './company-settings.component';
import { AgentDetailComponent } from './agents/agent-detail/agent-detail.component';
import { AgentsMasterComponent } from './agents/agents-master/agents-master.component';
import { DownloadAgentComponent } from './agents/download-agent/download-agent.component';
import { CurrenciesDetailComponent } from './currencies/currencies-detail/currencies-detail.component';
import { CurrenciesMasterComponent } from './currencies/currencies-master/currencies-master.component';
import { CustomizationComponent } from './customization/customization.component';
import { EmailTemplateComponent } from './email-template/email-template.component';
import { CsvLogsDetailComponent } from './file-upload/csv-logs/csv-logs-detail/csv-logs-detail.component';
import { CsvLogsComponent } from './file-upload/csv-logs/csv-logs.component';
import { FileUploadComponent } from './file-upload/file-upload.component';
import { GeneralSettingsComponent } from './general-settings/general-settings.component';
import { ZendeskImportComponent } from './import/zendesk-import/zendesk-import.component';
import { InviteEmailComponent } from './invite-email/invite-email.component';
import { KnowledgeBaseGeneralSettingsComponent } from './knowledge-base-general-settings/knowledge-base-general-settings.component';
import { CamgaignCategoryComponent } from './marketing/campaigns/camgaign-category/camgaign-category.component';
import { CampaignsDetailComponent } from './marketing/campaigns/campaigns-detail/campaigns-detail.component';
import { CampaignsMasterComponent } from './marketing/campaigns/campaigns-master/campaigns-master.component';
import { EmailListsDetailComponent } from './marketing/email-lists/email-lists-detail/email-lists-detail.component';
import { EmailListsMasterComponent } from './marketing/email-lists/email-lists-master/email-lists-master.component';
import { PageCustomizationComponent } from './page-customization/page-customization.component';
import { PluginsComponent } from './plugins/plugins.component';
import { PremadeResponseDetailComponent } from './premade-responses/premade-response-detail/premade-response-detail.component';
import { PremadeResponsesMasterComponent } from './premade-responses/premade-responses-master/premade-responses-master.component';
import { ReferralsComponent } from './referrals/referrals.component';
import { RoleCreateComponent } from './roles/role-create/role-create.component';
import { RoleDetailComponent } from './roles/role-detail/role-detail.component';
import { RoleMasterComponent } from './roles/role-master/role-master.component';
import { CompanySecurityComponent } from './security/security.component';
import { SidebarCustomizationDetailComponent } from './sidebar-customization/detail/sidebar-customization-detail.component';
import { SidebarCustomizationMasterComponent } from './sidebar-customization/master/sidebar-customization-master.component';
import { SocialSignInComponent } from './social-sign-in/social-sign-in.component';
import { AccountLevelAccessComponent } from './account-level-access/account-level-access.component';
import { SpfRecordsComponent } from './spf-records/spf-records.component';
import { FieldPermissionComponent } from './roles/field-permission/field-permission.component';

const routes: Routes = [
	{ path: '', component: CompanySettingsComponent },
	{ path: 'cname', component: CnameComponent },
	{ path: 'chat-settings', component: ChatGeneralSettingsComponent },
	{ path: 'roles', component: RoleMasterComponent },
	{ path: 'roles/:roleId/:moduleId/field-permisssion', component: FieldPermissionComponent },
	{ path: 'roles/:type/:dataId', component: RoleCreateComponent },
	{ path: 'roles/:roleId', component: RoleDetailComponent },
	{ path: 'page-customization', component: PageCustomizationComponent },
	{ path: 'security', component: CompanySecurityComponent },
	{ path: 'chat-faqs', component: ChatFaqsComponent },
	{ path: 'chat-faqs/:faqId', component: ChatFaqDetailComponent },
	{ path: 'currencies', component: CurrenciesMasterComponent },
	{
		path: 'referrals',
		component: ReferralsComponent
	},
	{
		path: 'sidebar-customization/detail/:roleId',
		component: SidebarCustomizationDetailComponent
	},
	{
		path: 'sidebar-customization/master',
		component: SidebarCustomizationMasterComponent
	},
	{ path: 'customization', component: CustomizationComponent },
	{ path: 'api-builder', component: ApiBuilderComponent },
	// { path: 'api-keys', component: ApiKeysComponent },
	{ path: 'import/zendesk', component: ZendeskImportComponent },
	{ path: 'blacklist', component: BlacklistComponent },
	{ path: 'spf-records', component: SpfRecordsComponent },
	{ path: 'general-settings', component: GeneralSettingsComponent },
	{ path: 'premade-responses', component: PremadeResponsesMasterComponent },
	{
		path: 'premade-responses/:responseId',
		component: PremadeResponseDetailComponent
	},
	{
		path: 'knowledge-base-general-settings',
		component: KnowledgeBaseGeneralSettingsComponent
	},
	{ path: 'social-sign-in', component: SocialSignInComponent },
	{ path: 'account-level-access', component: AccountLevelAccessComponent },
	{ path: 'signup-email', component: EmailTemplateComponent },
	{ path: 'invite-email', component: InviteEmailComponent },
	{
		path: 'currencies/:currencyId',
		component: CurrenciesDetailComponent
	},
	{ path: 'file-upload', component: FileUploadComponent },
	{ path: 'file-upload/csv-logs', component: CsvLogsComponent },
	{ path: 'file-upload/csv-logs/:dataId', component: CsvLogsDetailComponent },
	{ path: 'marketing/campaigns', component: CampaignsMasterComponent },
	{
		path: 'marketing/campaigns/type',
		component: CamgaignCategoryComponent
	},
	{
		path: 'marketing/campaigns/:campaignType/:campaignId',
		component: CampaignsDetailComponent
	},
	{ path: 'marketing/email-lists', component: EmailListsMasterComponent },
	{
		path: 'marketing/email-lists/:emailListId',
		component: EmailListsDetailComponent
	},
	// { path: 'getting-started:moduleId', component: GettingStartedComponent },
	{ path: 'controllers', component: AgentsMasterComponent },
	{
		path: 'controller/:controllerId',
		component: AgentDetailComponent
	},
	{
		path: 'controllers/download',
		component: DownloadAgentComponent
	},
	{
		path: 'plugins/wordpress',
		component: PluginsComponent
	},
	{
		path: 'plugins/joomla',
		component: PluginsComponent
	},
	{
		path: 'plugins/shopify',
		component: PluginsComponent
	}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	schemas: [NO_ERRORS_SCHEMA],
})
export class CompanySettingsRoutingModule {}
