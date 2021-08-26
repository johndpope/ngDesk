import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AgentDetailComponent } from '@src/app/company-settings/agents/agent-detail/agent-detail.component';
import { AgentsMasterComponent } from '@src/app/company-settings/agents/agents-master/agents-master.component';
import { DownloadAgentComponent } from '@src/app/company-settings/agents/download-agent/download-agent.component';
import { ApiBuilderComponent } from '@src/app/company-settings/api-builder/api-builder.component';
import { ApiKeysComponent } from '@src/app/company-settings/api-keys/api-keys.component';
import { BlacklistComponent } from '@src/app/company-settings/blacklist/blacklist.component';
import { ChatFaqDetailComponent } from '@src/app/company-settings/chat-settings/chat-faqs/chat-faq-detail/chat-faq-detail.component';
import { ChatFaqsComponent } from '@src/app/company-settings/chat-settings/chat-faqs/chat-faqs.component';
import { ChatGeneralSettingsComponent } from '@src/app/company-settings/chat-settings/chat-general-settings/chat-general-settings.component';
import { CnameComponent } from '@src/app/company-settings/cname/cname.component';
import { CompanySettingsComponent } from '@src/app/company-settings/company-settings.component';
import { CurrenciesDetailComponent } from '@src/app/company-settings/currencies/currencies-detail/currencies-detail.component';
import { CurrenciesMasterComponent } from '@src/app/company-settings/currencies/currencies-master/currencies-master.component';
import { CustomizationComponent } from '@src/app/company-settings/customization/customization.component';
import { EmailTemplateComponent } from '@src/app/company-settings/email-template/email-template.component';
import { FileRuleDetailComponent } from '@src/app/company-settings/file-rule/file-rule-detail/file-rule-detail.component';
import { FileRuleMasterComponent } from '@src/app/company-settings/file-rule/file-rule-master/file-rule-master.component';
import { CsvLogsDetailComponent } from '@src/app/company-settings/file-upload/csv-logs/csv-logs-detail/csv-logs-detail.component';
import { CsvLogsComponent } from '@src/app/company-settings/file-upload/csv-logs/csv-logs.component';
import { FileUploadComponent } from '@src/app/company-settings/file-upload/file-upload.component';
import { GeneralSettingsComponent } from '@src/app/company-settings/general-settings/general-settings.component';
import { ZendeskImportComponent } from '@src/app/company-settings/import/zendesk-import/zendesk-import.component';
import { InviteEmailComponent } from '@src/app/company-settings/invite-email/invite-email.component';
import { KnowledgeBaseGeneralSettingsComponent } from '@src/app/company-settings/knowledge-base-general-settings/knowledge-base-general-settings.component';
import { CamgaignCategoryComponent } from '@src/app/company-settings/marketing/campaigns/camgaign-category/camgaign-category.component';
import { CampaignsDetailComponent } from '@src/app/company-settings/marketing/campaigns/campaigns-detail/campaigns-detail.component';
import { CampaignsMasterComponent } from '@src/app/company-settings/marketing/campaigns/campaigns-master/campaigns-master.component';
import { EmailListsDetailComponent } from '@src/app/company-settings/marketing/email-lists/email-lists-detail/email-lists-detail.component';
import { EmailListsMasterComponent } from '@src/app/company-settings/marketing/email-lists/email-lists-master/email-lists-master.component';
import { NormalizationRuleDetailComponent } from '@src/app/company-settings/normalization-rule/normalization-rule-detail/normalization-rule-detail.component';
import { NormalizationRuleMasterComponent } from '@src/app/company-settings/normalization-rule/normalization-rule-master/normalization-rule-master.component';
import { PageCustomizationComponent } from '@src/app/company-settings/page-customization/page-customization.component';
import { PaymentComponent } from '@src/app/company-settings/payment/payment.component';
import { PluginsComponent } from '@src/app/company-settings/plugins/plugins.component';
import { PremadeResponseDetailComponent } from '@src/app/company-settings/premade-responses/premade-response-detail/premade-response-detail.component';
import { PremadeResponsesMasterComponent } from '@src/app/company-settings/premade-responses/premade-responses-master/premade-responses-master.component';
import { ReferralsComponent } from '@src/app/company-settings/referrals/referrals.component';
import { RoleLayoutDetailComponent } from '@src/app/company-settings/role-layout/role-layout-detail/role-layout-detail.component';
import { RoleLayoutMasterComponent } from '@src/app/company-settings/role-layout/role-layout-master/role-layout-master.component';
import { RoleCreateComponent } from '@src/app/company-settings/roles/role-create/role-create.component';
import { RoleDetailComponent } from '@src/app/company-settings/roles/role-detail/role-detail.component';
import { FieldPermissionComponent } from '@src/app/company-settings/roles/field-permission/field-permission.component';
import { RoleMasterComponent } from '@src/app/company-settings/roles/role-master/role-master.component';
import { CompanySecurityComponent } from '@src/app/company-settings/security/security.component';
import { SidebarCustomizationDetailComponent } from '@src/app/company-settings/sidebar-customization/detail/sidebar-customization-detail.component';
import { SidebarCustomizationMasterComponent } from '@src/app/company-settings/sidebar-customization/master/sidebar-customization-master.component';
import { SocialSignInComponent } from '@src/app/company-settings/social-sign-in/social-sign-in.component';
import { DiscoveryMapsDetailComponent } from '@src/app/company-settings/software-asset-management/discovery-maps/discovery-maps-detail/discovery-maps-detail.component';
import { DiscoveryMapsMasterComponent } from '@src/app/company-settings/software-asset-management/discovery-maps/discovery-maps-master/discovery-maps-master.component';
import { EnterpriseSearchMasterComponent } from '@src/app/company-settings/software-asset-management/enterprise-search/enterprise-search-master/enterprise-search-master.component';
import { EnterpriseSearchDetailComponent } from '@src/app/company-settings/software-asset-management/enterprise-search/enterprise-search-detail/enterprise-search-detail.component';
import { SpfRecordsComponent } from '@src/app/company-settings/spf-records/spf-records.component';
import { ZoomIntegrationComponent } from '@src/app/company-settings/zoom-integration/zoom-integration.component';
import { GettingStartedComponent } from '@src/app/getting-started/getting-started.component';
import { AwsIntegrationComponent } from './aws-integration/aws-integration.component';
import { MicrosoftTeamsIntegrationComponent } from './team-integration/microsoft-teams-integration/microsoft-teams-integration.component';
import { MicrosoftTeamsUnsubscribeComponent } from './team-integration/microsoft-teams-unsubscribe/microsoft-teams-unsubscribe.component';
import { MicrosoftTeamsAuthenticationComponent } from './team-integration/microsoft-teams-authentication/microsoft-teams-authentication.component';
import { DocusignIntegrationComponent } from './docusign-integration/docusign-integration.component';
import { CatalogueMasterComponent } from './catalogues/catalogue-master/catalogue-master.component';
import { CatalogueDetailComponent } from './catalogues/catalogue-detail/catalogue-detail.component';
import { AccountLevelAccessComponent } from './account-level-access/account-level-access.component';


const routes: Routes = [
	{ path: '', component: CompanySettingsComponent },
	{ path: 'cname', component: CnameComponent },
	{ path: 'chat-settings', component: ChatGeneralSettingsComponent },
	{ path: 'roles', component: RoleMasterComponent },
	{ path: 'roles/:roleId/:moduleId/field-permisssion', component: FieldPermissionComponent },
	{ path: 'roles/:type/:dataId', component: RoleCreateComponent },
	{ path: 'roles/:roleId', component: RoleDetailComponent },
	{ path: 'role-layouts', component: RoleLayoutMasterComponent },
	{ path: 'role-layouts/:roleLayoutId', component: RoleLayoutDetailComponent },
	{ path: 'page-customization', component: PageCustomizationComponent },
	{ path: 'security', component: CompanySecurityComponent },
	{ path: 'chat-faqs', component: ChatFaqsComponent },
	{ path: 'chat-faqs/:faqId', component: ChatFaqDetailComponent },
	{ path: 'currencies', component: CurrenciesMasterComponent },
	{ path: 'zoom-integration', component: ZoomIntegrationComponent },
	{ path: 'catalogues', component: CatalogueMasterComponent },
	
	{
		path: 'referrals',
		component: ReferralsComponent,
	},
	{
		path: 'sidebar-customization/detail/:roleId',
		component: SidebarCustomizationDetailComponent,
	},
	{
		path: 'sidebar-customization/master',
		component: SidebarCustomizationMasterComponent,
	},
	{ path: 'customization', component: CustomizationComponent },
	{ path: 'api-builder', component: ApiBuilderComponent },
	{ path: 'api-keys', component: ApiKeysComponent },
	{ path: 'import/zendesk', component: ZendeskImportComponent },
	{ path: 'blacklist', component: BlacklistComponent },
	{ path: 'spf-records', component: SpfRecordsComponent },
	{ path: 'general-settings', component: GeneralSettingsComponent },
	{ path: 'premade-responses', component: PremadeResponsesMasterComponent },
	{
		path: 'premade-responses/:responseId',
		component: PremadeResponseDetailComponent,
	},
	{
		path: 'knowledge-base-general-settings',
		component: KnowledgeBaseGeneralSettingsComponent,
	},
	{ path: 'social-sign-in', component: SocialSignInComponent },
	{ path: 'account-level-access', component: AccountLevelAccessComponent },
	{ path: 'signup-email', component: EmailTemplateComponent },
	{ path: 'invite-email', component: InviteEmailComponent },
	{
		path: 'currencies/:currencyId',
		component: CurrenciesDetailComponent,
	},
	{
		path: 'catalogues/:catalogueId',
		component: CatalogueDetailComponent,
	},
	{ path: 'file-upload', component: FileUploadComponent },
	{ path: 'file-upload/csv-logs', component: CsvLogsComponent },
	{ path: 'file-upload/csv-logs/:dataId', component: CsvLogsDetailComponent },
	{ path: 'marketing/campaigns', component: CampaignsMasterComponent },
	{
		path: 'marketing/campaigns/type',
		component: CamgaignCategoryComponent,
	},
	{
		path: 'marketing/campaigns/:campaignType/:campaignId',
		component: CampaignsDetailComponent,
	},
	{ path: 'marketing/email-lists', component: EmailListsMasterComponent },
	{
		path: 'marketing/email-lists/:emailListId',
		component: EmailListsDetailComponent,
	},
	{ path: 'getting-started:moduleId', component: GettingStartedComponent },
	{ path: 'controllers', component: AgentsMasterComponent },
	{
		path: 'controller/:controllerId',
		component: AgentDetailComponent,
	},
	{
		path: 'controllers/download',
		component: DownloadAgentComponent,
	},
	{
		path: 'plugins/wordpress',
		component: PluginsComponent,
	},
	{
		path: 'plugins/joomla',
		component: PluginsComponent,
	},
	{
		path: 'plugins/shopify',
		component: PluginsComponent,
	},
	{
		path: 'subscription',
		component: PaymentComponent,
	},
	{ path: 'file-rules', component: FileRuleMasterComponent },
	{
		path: 'file-rules/:ruleId',
		component: FileRuleDetailComponent,
	},
	{ path: 'normalization-rules', component: NormalizationRuleMasterComponent },
	{
		path: 'normalization-rules/:id',
		component: NormalizationRuleDetailComponent,
	},
	{
		path: 'discovery-maps',
		component: DiscoveryMapsMasterComponent,
	},
	{
		path: 'discovery-maps/:id',
		component: DiscoveryMapsDetailComponent,
	},
	{
		path: 'enterprise-search',
		component: EnterpriseSearchMasterComponent,
	},
	{
		path: 'enterprise-search/:id',
		component: EnterpriseSearchDetailComponent,
	},
	{ path: 'aws-integration', component: AwsIntegrationComponent },
	{ path: 'teams-integration', component: MicrosoftTeamsIntegrationComponent },
	{ path: 'teams-integration/unsubscribe', component: MicrosoftTeamsUnsubscribeComponent },
	{ path: 'teams-integration/authenticate', component: MicrosoftTeamsAuthenticationComponent },
	{path:'docusign-integration', component:DocusignIntegrationComponent}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
})
export class CompanySettingsRoutingModule {}
