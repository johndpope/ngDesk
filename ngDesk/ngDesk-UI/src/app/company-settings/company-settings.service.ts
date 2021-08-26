import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
	providedIn: 'root',
})
export class CompanySettingService {
	public companySettingsList = [];
	constructor(private translateService: TranslateService) {}

	public getCompanySettings() {
		return (this.companySettingsList = [
			{
				header: this.translateService.instant('COMPANY_SETTINGS'),
				options: [
					{
						ICON: 'settings',
						NAME: 'GENERAL_SETTINGS',
						PATH: 'general-settings',
					},
					{
						ICON: 'view_quilt',
						NAME: 'SIDEBAR_CUSTOMIZATION',
						PATH: 'sidebar-customization/master',
					},
					{
						ICON: 'question_answer',
						NAME: 'PREMADE_RESPONSES',
						PATH: 'premade-responses',
					},
					{
						ICON: 'attach_money',
						NAME: 'CURRENCIES',
						PATH: 'currencies',
					},
					{
						ICON: 'group_add',
						NAME: 'REFERRALS',
						PATH: 'referrals',
					},
					{
						ICON: 'payment',
						NAME: 'SUBSCRIPTION',
						PATH: 'subscription',
					},
					{
						ICON: 'summarize',
						NAME: 'CATALOGUES',
						PATH: 'catalogues',
					},
				],
			},
			{
				header: this.translateService.instant('USER_MANAGEMENT'),
				options: [
					{ ICON: 'people', NAME: 'Users', PATH: 'render/Users' },
					{ ICON: 'supervisor_account', NAME: 'ROLES', PATH: 'roles' },
					{ ICON: 'view_quilt', NAME: 'ACTION_ITEMS', PATH: 'role-layouts' },
				],
			},
			{
				header: this.translateService.instant('SECURITY'),
				options: [
					{ ICON: 'settings', NAME: 'GENERAL_SETTINGS', PATH: 'security' },
					{ ICON: 'person', NAME: 'SOCIAL_SIGN_IN', PATH: 'social-sign-in' },
				],
			},
			{
				header: this.translateService.instant('CHAT_SETTINGS'),
				options: [
					{ ICON: 'chat', NAME: 'CHAT_SETTINGS', PATH: 'chat-settings' },
				],
			},
			{
				header: this.translateService.instant('KNOWLEDGE_BASE'),
				options: [
					{
						ICON: 'settings',
						NAME: 'GENERAL_SETTINGS',
						PATH: 'knowledge-base-general-settings',
					},
				],
			},
			{
				header: this.translateService.instant('MARKETING'),
				options: [
					{
						ICON: 'markunread_mailbox',
						NAME: 'CAMPAIGNS',
						PATH: 'marketing/campaigns',
					},
					{
						ICON: 'contact_mail',
						NAME: 'EMAIL_LISTS',
						PATH: 'marketing/email-lists',
					},
				],
			},
			{
				header: this.translateService.instant('EMAIL_SETTINGS'),
				options: [
					{
						ICON: 'import_contacts',
						NAME: 'ALLOWED_BLOCKED',
						PATH: 'blacklist',
					},
					{ ICON: 'email', NAME: 'SPF_RECORDS', PATH: 'spf-records' },
				],
			},
			{
				header: this.translateService.instant('BRANDING'),
				options: [
					{ ICON: 'color_lens', NAME: 'CUSTOMIZATION', PATH: 'customization' },
					{ ICON: 'dns', NAME: 'DOMAIN', PATH: 'cname' },
					{
						ICON: 'account_box',
						NAME: 'USER_SIGNUP_EMAIL',
						PATH: 'signup-email',
					},
					{
						ICON: 'person_add',
						NAME: 'USER_INVITE_EMAIL',
						PATH: 'invite-email',
					},
				],
			},
			{
				header: this.translateService.instant('API'),
				options: [
					{ ICON: 'vpn_key', NAME: 'API_KEYS', PATH: 'api-keys' },
					{ ICON: 'build', NAME: 'API_BUILDER', PATH: 'api-builder' },
				],
			},
			{
				header: this.translateService.instant('ACCOUNT_SETTINGS'),
				options: [
					{
						ICON: 'cloud_download',
						NAME: 'IMPORT_DATA_FROM_ZENDESK',
						PATH: 'import/zendesk',
					},
					{
						ICON: 'check_circle_outline',
						NAME: 'ACCOUNT_LEVEL_ACCESS',
						PATH: 'account-level-access',
					},
				],
			},
			{
				header: this.translateService.instant('MODULE_SETTINGS'),
				options: [
					{
						ICON: 'save_alt',
						NAME: 'IMPORT_DATA',
						PATH: 'file-upload/csv-logs',
					},
				],
			},
			{
				header: this.translateService.instant('SOFTWARE_ASSET_MANAGEMENT'),
				options: [
					{
						ICON: 'router',
						NAME: 'AGENTS',
						PATH: 'controllers',
					},
					{
						ICON: 'download',
						NAME: 'DOWNLOAD_INSTALLERS',
						PATH: 'controllers/download',
					},
					{
						ICON: 'rule',
						NAME: 'FILE_RULES',
						PATH: 'file-rules',
					},
					{
						ICON: 'toc',
						NAME: 'NORMALIZATION_RULES',
						PATH: 'normalization-rules',
					},
					{
						ICON: 'link',
						NAME: 'DISCOVERY_MAPS',
						PATH: 'discovery-maps',
					},
					{
						ICON: 'perm_identity',
						NAME: 'ENTERPRISE_SEARCH',
						PATH: 'enterprise-search',
					},
				],
			},
			// {
			// 	header: this.translateService.instant('PLUGINS'),
			// 	options: [
			// 		{ ICON: 'menu_book', NAME: 'WORDPRESS', PATH: 'plugins/wordpress' },
			// 		{ ICON: 'control_camera', NAME: 'JOOMLA', PATH: 'plugins/joomla' },
			// 		{ ICON: 'shop', NAME: 'SHOPIFY', PATH: 'plugins/shopify' },
			// 	],
			// },
			{
				header: this.translateService.instant('INTEGRATIONS'),
				options: [
					{ PATH: 'zoom-integration' },
					{ NAME: 'AWS Cloud Watch', PATH: 'aws-integration' },
					{ NAME: 'Microsoft Teams', PATH: 'teams-integration' },
					// { PATH:'docusign-integration'}
				],
			},
		]);
	}

	public getSamRules() {
		return [
			{
				BACKEND: 'Filename, File Path and Hash',
				DISPLAY: 'FILE_NAME_HASH_RULE',
			},
			{ BACKEND: 'Filename, Hash', DISPLAY: 'FILE_HASH_RULE' },
			{ BACKEND: 'Filename', DISPLAY: 'FILE_NAME_RULE' },
			{ BACKEND: 'Filename, File Path', DISPLAY: 'FILE_NAME_PATH_RULE' },
			{ BACKEND: 'Hash', DISPLAY: 'HASH_RULE' },
		];
	}
}
