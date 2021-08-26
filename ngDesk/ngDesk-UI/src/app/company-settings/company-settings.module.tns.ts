import { DragDropModule } from '@angular/cdk/drag-drop';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { CommonModule, DatePipe } from '@angular/common';
import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
// import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
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
import { ApiKeyDialogComponent } from '@src/app/dialogs/api-key-dialog/api-key-dialog.component';
import { CsvImportDialogComponent } from '@src/app/dialogs/csv-import-dialog/csv-import-dialog.component';
import { SidebarMenuCustomizeDialogComponent } from '@src/app/dialogs/sidebar-menu-customize-dialog/sidebar-menu-customize-dialog.component';
import { SpfRecordDialogComponent } from '@src/app/dialogs/spf-record-dialog/spf-record-dialog.component';
import { SharedModule } from '@src/app/shared-module/shared.module';
import { ObjectToArrayPipe } from '@src/app/shared/object-to-array/object-to-array.pipe';
import { ApiBuilderComponent } from '@src/app/company-settings/api-builder/api-builder.component';
import { ApiKeysComponent } from '@src/app/company-settings/api-keys/api-keys.component';
import { BlacklistComponent } from '@src/app/company-settings/blacklist/blacklist.component';
import { ChatFaqDetailComponent } from '@src/app/company-settings/chat-settings/chat-faqs/chat-faq-detail/chat-faq-detail.component';
import { ChatFaqsComponent } from '@src/app/company-settings/chat-settings/chat-faqs/chat-faqs.component';
import { ChatGeneralSettingsComponent } from '@src/app/company-settings/chat-settings/chat-general-settings/chat-general-settings.component';
import { CnameComponent } from '@src/app/company-settings/cname/cname.component';
import { CompanySettingsRoutingModule } from '@src/app/company-settings/company-settings-routing.module';
import { CompanySettingsComponent } from '@src/app/company-settings/company-settings.component';
import { AgentService } from '@src/app/company-settings/agents/agent.service';
import { AgentDetailComponent } from '@src/app/company-settings/agents/agent-detail/agent-detail.component';
import { AgentsMasterComponent } from '@src/app/company-settings/agents/agents-master/agents-master.component';
import { DownloadAgentComponent } from '@src/app/company-settings/agents/download-agent/download-agent.component';
import { CurrenciesDetailComponent } from '@src/app/company-settings/currencies/currencies-detail/currencies-detail.component';
import { CurrenciesMasterComponent } from '@src/app/company-settings/currencies/currencies-master/currencies-master.component';
import { CustomizationComponent } from '@src/app/company-settings/customization/customization.component';
import { EmailTemplateComponent } from '@src/app/company-settings/email-template/email-template.component';
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
import { PageCustomizationComponent } from '@src/app/company-settings/page-customization/page-customization.component';
import { PluginsComponent } from '@src/app/company-settings/plugins/plugins.component';
import { PremadeResponseDetailComponent } from '@src/app/company-settings/premade-responses/premade-response-detail/premade-response-detail.component';
import { PremadeResponsesMasterComponent } from '@src/app/company-settings/premade-responses/premade-responses-master/premade-responses-master.component';
import { ReferralsComponent } from '@src/app/company-settings/referrals/referrals.component';
import { RoleCreateComponent } from '@src/app/company-settings/roles/role-create/role-create.component';
import { RoleDetailComponent } from '@src/app/company-settings/roles/role-detail/role-detail.component';
import { RoleMasterComponent } from '@src/app/company-settings/roles/role-master/role-master.component';

import { CompanySecurityComponent } from '@src/app/company-settings/security/security.component';
import { SidebarCustomizationDetailComponent } from '@src/app/company-settings/sidebar-customization/detail/sidebar-customization-detail.component';
import { SidebarCustomizationMasterComponent } from '@src/app/company-settings/sidebar-customization/master/sidebar-customization-master.component';
import { SocialSignInComponent } from '@src/app/company-settings/social-sign-in/social-sign-in.component';
import { SpfRecordsComponent } from '@src/app/company-settings/spf-records/spf-records.component';
import { PaymentComponent } from '@src/app/company-settings/payment/payment.component';
import { NativeScriptCommonModule } from '@nativescript/angular';
import { NormalizationRuleMasterComponent } from '@src/app/company-settings/normalization-rule/normalization-rule-master/normalization-rule-master.component';
import { NormalizationRuleDetailComponent } from '@src/app/company-settings/normalization-rule/normalization-rule-detail/normalization-rule-detail.component';
import { FieldPermissionComponent } from '@src/app/company-settings/roles/field-permission/field-permission.component';
import { AccountLevelAccessComponent } from './account-level-access/account-level-access.component';

@NgModule({
	imports: [
        NativeScriptCommonModule,
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
		// MatDialogModule,
		MatDividerModule,
		MatPaginatorModule,
		MatProgressSpinnerModule,
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
		MatSidenavModule,
		MatSliderModule
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
		NormalizationRuleMasterComponent,
		NormalizationRuleDetailComponent,
		AccountLevelAccessComponent
	],
	providers: [
		//RolesService,
		DatePipe,
		ApiService,
		ImportService,
		AgentService,
		CurrenciesDetailComponent,
		CampaignsService
	],
	schemas: [NO_ERRORS_SCHEMA],
	// entryComponents: [
	// 	SidebarMenuCustomizeDialogComponent,
	// 	ApiKeyDialogComponent,
	// 	SpfRecordDialogComponent,
	// 	CsvImportDialogComponent
	// ]
})
export class CompanySettingsModule {}
