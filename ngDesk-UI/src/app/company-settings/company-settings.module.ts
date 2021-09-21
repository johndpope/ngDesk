import { ClipboardModule } from '@angular/cdk/clipboard';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { OverlayModule } from '@angular/cdk/overlay';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '@src/app/api/api.service';
import { AgentDetailComponent } from '@src/app/company-settings/agents/agent-detail/agent-detail.component';
import { AgentService } from '@src/app/company-settings/agents/agent.service';
import { AgentsMasterComponent } from '@src/app/company-settings/agents/agents-master/agents-master.component';
import { DownloadAgentComponent } from '@src/app/company-settings/agents/download-agent/download-agent.component';
import { ApiBuilderComponent } from '@src/app/company-settings/api-builder/api-builder.component';
import { ApiKeysComponent } from '@src/app/company-settings/api-keys/api-keys.component';
import { BlacklistComponent } from '@src/app/company-settings/blacklist/blacklist.component';
import { ChatFaqDetailComponent } from '@src/app/company-settings/chat-settings/chat-faqs/chat-faq-detail/chat-faq-detail.component';
import { ChatFaqsComponent } from '@src/app/company-settings/chat-settings/chat-faqs/chat-faqs.component';
import { ChatGeneralSettingsComponent } from '@src/app/company-settings/chat-settings/chat-general-settings/chat-general-settings.component';
import { CnameComponent } from '@src/app/company-settings/cname/cname.component';
import { CompanySettingsRoutingModule } from '@src/app/company-settings/company-settings-routing.module';
import { CompanySettingsComponent } from '@src/app/company-settings/company-settings.component';
import { CompanySettingService } from '@src/app/company-settings/company-settings.service';
import { CurrenciesDetailComponent } from '@src/app/company-settings/currencies/currencies-detail/currencies-detail.component';
import { CurrenciesMasterComponent } from '@src/app/company-settings/currencies/currencies-master/currencies-master.component';
import { EmailTemplateComponent } from '@src/app/company-settings/email-template/email-template.component';
import { FileRuleDetailComponent } from '@src/app/company-settings/file-rule/file-rule-detail/file-rule-detail.component';
import { FileRuleMasterComponent } from '@src/app/company-settings/file-rule/file-rule-master/file-rule-master.component';
import { CsvLogsDetailComponent } from '@src/app/company-settings/file-upload/csv-logs/csv-logs-detail/csv-logs-detail.component';
import { CsvLogsComponent } from '@src/app/company-settings/file-upload/csv-logs/csv-logs.component';
import { FileUploadComponent } from '@src/app/company-settings/file-upload/file-upload.component';
import { GeneralSettingsComponent } from '@src/app/company-settings/general-settings/general-settings.component';
import { ImportService } from '@src/app/company-settings/import/import.service';
import { ZendeskImportComponent } from '@src/app/company-settings/import/zendesk-import/zendesk-import.component';
import { InviteEmailComponent } from '@src/app/company-settings/invite-email/invite-email.component';
import { KnowledgeBaseGeneralSettingsComponent } from '@src/app/company-settings/knowledge-base-general-settings/knowledge-base-general-settings.component';
import { CamgaignCategoryComponent } from '@src/app/company-settings/marketing/campaigns/camgaign-category/camgaign-category.component';
import { CampaignsDetailComponent } from '@src/app/company-settings/marketing/campaigns/campaigns-detail/campaigns-detail.component';
import { CampaignsMasterComponent } from '@src/app/company-settings/marketing/campaigns/campaigns-master/campaigns-master.component';
import { CampaignsService } from '@src/app/company-settings/marketing/campaigns/campaigns.service';
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
import { RoleMasterComponent } from '@src/app/company-settings/roles/role-master/role-master.component';
import { RolesService } from '@src/app/company-settings/roles/roles-old.service';
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
import { ApiKeyDialogComponent } from '@src/app/dialogs/api-key-dialog/api-key-dialog.component';
import { CsvImportDialogComponent } from '@src/app/dialogs/csv-import-dialog/csv-import-dialog.component';
import { DeleteCompanyDialogComponent } from '@src/app/dialogs/delete-company-dialog/delete-company-dialog.component';
import { SidebarMenuCustomizeDialogComponent } from '@src/app/dialogs/sidebar-menu-customize-dialog/sidebar-menu-customize-dialog.component';
import { SpfRecordDialogComponent } from '@src/app/dialogs/spf-record-dialog/spf-record-dialog.component';
import { SharedModule } from '@src/app/shared-module/shared.module';
import { ObjectToArrayPipe } from '@src/app/shared/object-to-array/object-to-array.pipe';
import { AwsIntegrationComponent } from './aws-integration/aws-integration.component';
import { MicrosoftTeamsIntegrationComponent } from './team-integration/microsoft-teams-integration/microsoft-teams-integration.component';
import { MicrosoftTeamsUnsubscribeComponent } from './team-integration/microsoft-teams-unsubscribe/microsoft-teams-unsubscribe.component';
import { DocusignIntegrationComponent } from './docusign-integration/docusign-integration.component';
import { FieldPermissionComponent } from './roles/field-permission/field-permission.component';
import { CatalogueDetailComponent } from './catalogues/catalogue-detail/catalogue-detail.component';
import { CatalogueMasterComponent } from './catalogues/catalogue-master/catalogue-master.component';
import { AccountLevelAccessComponent } from './account-level-access/account-level-access.component';
import { CsvLogsService } from './file-upload/csv-logs/csv-logs-detail/csv-logs-detail.service';

@NgModule({
	imports: [
		SharedModule,
		ScrollingModule,
		CommonModule,
		FlexLayoutModule,
		FormsModule,
		ReactiveFormsModule,
		MatButtonModule,
		MatCardModule,
		MatCheckboxModule,
		MatChipsModule,
		MatIconModule,
		MatInputModule,
		MatDialogModule,
		MatDividerModule,
		MatPaginatorModule,
		MatProgressSpinnerModule,
		MatProgressBarModule,
		MatRadioModule,
		MatSelectModule,
		MatTableModule,
		MatTabsModule,
		MatTooltipModule,
		CompanySettingsRoutingModule,
		MatButtonToggleModule,
		MatSlideToggleModule,
		MatExpansionModule,
		// MatFileUploadModule,
		DragDropModule,
		MatMenuModule,
		OverlayModule,
		MatSidenavModule,
		MatSliderModule,
		MatListModule,
		ClipboardModule,
	],
	declarations: [
		ApiKeyDialogComponent,
		ApiKeysComponent,
		CompanySettingsComponent,
		ObjectToArrayPipe,
		PageCustomizationComponent,
		CompanySecurityComponent,
		SidebarCustomizationDetailComponent,
		SidebarCustomizationMasterComponent,
		SidebarMenuCustomizeDialogComponent,
		RoleMasterComponent,
		ApiBuilderComponent,
		CnameComponent,
		ZendeskImportComponent,
		GeneralSettingsComponent,
		BlacklistComponent,
		PremadeResponseDetailComponent,
		PremadeResponsesMasterComponent,
		KnowledgeBaseGeneralSettingsComponent,
		SocialSignInComponent,
		ChatGeneralSettingsComponent,
		ChatFaqsComponent,
		ChatFaqDetailComponent,
		EmailTemplateComponent,
		InviteEmailComponent,
		SpfRecordsComponent,
		SpfRecordDialogComponent,
		FileUploadComponent,
		RoleDetailComponent,
		FieldPermissionComponent,
		CsvImportDialogComponent,
		CurrenciesMasterComponent,
		CurrenciesDetailComponent,
		CampaignsMasterComponent,
		CampaignsDetailComponent,
		EmailListsMasterComponent,
		EmailListsDetailComponent,
		CsvLogsComponent,
		CsvLogsDetailComponent,
		ReferralsComponent,
		RoleCreateComponent,
		AgentsMasterComponent,
		AgentDetailComponent,
		CamgaignCategoryComponent,
		DownloadAgentComponent,
		PluginsComponent,
		PaymentComponent,
		DeleteCompanyDialogComponent,
		RoleLayoutDetailComponent,
		RoleLayoutMasterComponent,
		FileRuleMasterComponent,
		FileRuleDetailComponent,
		ZoomIntegrationComponent,
		NormalizationRuleMasterComponent,
		NormalizationRuleDetailComponent,
		DiscoveryMapsDetailComponent,
		DiscoveryMapsMasterComponent,
		EnterpriseSearchMasterComponent,
		EnterpriseSearchDetailComponent,
		AwsIntegrationComponent,
		MicrosoftTeamsIntegrationComponent,
		MicrosoftTeamsUnsubscribeComponent,
		DocusignIntegrationComponent,
		CatalogueDetailComponent,
		CatalogueMasterComponent,
		AccountLevelAccessComponent,
	],
	providers: [
		RolesService,
		DatePipe,
		ApiService,
		ImportService,
		AgentService,
		CsvLogsService,
		CurrenciesDetailComponent,
		CampaignsService,
		CompanySettingService,
	],
	// entryComponents: [
	// 	SidebarMenuCustomizeDialogComponent,
	// 	ApiKeyDialogComponent,
	// 	SpfRecordDialogComponent,
	// 	CsvImportDialogComponent,
	// ],
})
export class CompanySettingsModule {}
