import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '../../../companies/companies.service';
import { BannerMessageService } from '../../../custom-components/banner-message/banner-message.service';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '../../../app.globals';
import { Role } from './../../../models/role';
import { RolesService } from './../../roles/roles-old.service';
import {
	CompanySettings,
	CompanySettingsApiService,
} from '@ngdesk/company-api';
import { UsersService } from './../../../users/users.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { Router } from '@angular/router';

@Component({
	selector: 'app-chat-general-settings',
	templateUrl: './chat-general-settings.component.html',
	styleUrls: ['./chat-general-settings.component.scss'],
})
export class ChatGeneralSettingsComponent implements OnInit {
	public maxChatOptions = [1, 2, 3, 4, 5];
	public roles: Role[] = [];
	public generalSettingsForm: FormGroup;
	public subdomain: string;
	public params = {
		max_chats_per_agent: {},
	};
	public companySettings: CompanySettings = {};

	constructor(
		private translateService: TranslateService,
		private formBuilder: FormBuilder,
		private companiesService: CompaniesService,
		private bannerMessageService: BannerMessageService,
		private appGlobals: AppGlobals,
		private httpClient: HttpClient,
		private rolesService: RolesService,
		private companySettingsApiService: CompanySettingsApiService,
		private usersService: UsersService,
		private router: Router,
		private loaderService: LoaderService,
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.
		this.translateService
			.get('MAX_CHATS_PER_AGENT')
			.subscribe((value: string) => {
				this.params['max_chats_per_agent']['field'] = value;
			});
	}

	public ngOnInit() {
		this.generalSettingsForm = this.formBuilder.group({
			MAX_CHATS_PER_AGENT: [''],
			ROLES_WITH_CHAT: [''],
		});
		this.rolesService.getRoles().subscribe((rolesResponse: any) => {
			this.roles = rolesResponse.ROLES.filter(
				(role) => role.NAME !== 'SystemAdmin' && role.NAME !== 'Customers'
			);
			const query = ` {

      COMPANY: getCompanyDetails{
        ROLES_WITH_CHAT: rolesWithChat
        MAX_CHATS_PER_AGENT: maxChatsPerAgent
      }
           }`;
			this.makeGraphQLCall(query).subscribe(
				(response: any) => {
					this.generalSettingsForm.controls['MAX_CHATS_PER_AGENT'].setValue(
						response.COMPANY.MAX_CHATS_PER_AGENT
					);
					let roles = [];
					if (response.COMPANY.ROLES_WITH_CHAT !== null) {
						response.COMPANY.ROLES_WITH_CHAT.forEach((rolesWithChat) => {
							let currentRole = this.roles.find(
								(role) => role.ROLE_ID === rolesWithChat
							);
							roles.push(currentRole);
						});
					} 
					this.generalSettingsForm.controls['ROLES_WITH_CHAT'].setValue(roles);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		});
	}

	public saveGeneralSettings() {
		this.companySettings.COMPANY_SUBDOMAIN = this.usersService.getSubdomain();
		this.companySettings.MAX_CHATS_PER_AGENT =
			this.generalSettingsForm.value.MAX_CHATS_PER_AGENT;
		let roles = [];
		this.generalSettingsForm.value.ROLES_WITH_CHAT.forEach((role) => {
			roles.push(role.ROLE_ID);
		});
		this.companySettings.ROLES_WITH_CHAT = roles;
		console.log('@here', this.companySettings.MAX_CHATS_PER_AGENT);
		if(this.companySettings.ROLES_WITH_CHAT.length !==0 && this.companySettings.MAX_CHATS_PER_AGENT !== 0){
		this.companySettingsApiService
			.putChatSettings(this.companySettings)
			.subscribe(
				(putSettingsResponse: any) => {
						this.router.navigate(['/company-settings']);
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant(
								'Settings has been updated successfully!'
							),
						});
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	} else {
		if(this.companySettings.ROLES_WITH_CHAT.length === 0){
		this.loaderService.isLoading = false;
		this.bannerMessageService.errorNotifications.push({
			message: this.translateService.instant(
				'ATLEAST_ONE_ROLE'
			),
		});
	  } else if(this.companySettings.MAX_CHATS_PER_AGENT === 0){
		this.loaderService.isLoading = false;
		this.bannerMessageService.errorNotifications.push({
			message: this.translateService.instant(
				'SELECT_MAX_CHATS'
			),
		});
	  } 
	}
}

	public makeGraphQLCall(query: string) {
		return this.httpClient.post(`${this.appGlobals.graphqlUrl}`, query);
	}
}
