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
import { ModulesService } from '@src/app/modules/modules.service';
import { MatDialog } from '@angular/material/dialog';

@Component({
	selector: 'app-chat-general-settings',
	templateUrl: './chat-general-settings.component.html',
	styleUrls: ['./chat-general-settings.component.scss'],
})
export class ChatGeneralSettingsComponent implements OnInit {
	public maxChatOptions = [1, 2, 3, 4, 5];
	public teams = [];
	public generalSettingsForm: FormGroup;
	public subdomain: string;
	public params = {
		max_chats_per_agent: {},
	};
	public companySettings: CompanySettings = {};
	public teamsModule;
	public checked = false;
	public timezone = '';
	public chatBusinessRules = {};
	public hasRestriction = false;
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
		private modulesService: ModulesService,
		public dialog: MatDialog,

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
			TEAMS_WHO_CAN_CHAT: ['']
		});
		this.modulesService.getModuleByName('Teams').subscribe((response: any) => {
			this.teamsModule = response;
			this.getTeamsData(0, '', this.teamsModule)
				.subscribe((teamResponse) => {
					this.teams = teamResponse['DATA'];

					const query = `{
						COMPANY: getCompanyDetails {
						  CHAT_SETTINGS: chatSettings {
							TEAMS_WHO_CAN_CHAT: teamsWhoCanChat
							MAX_CHATS_PER_AGENT: maxChatsPerAgent
							CHAT_BUSINESS_RULES: chatBusinessRules {
							  HAS_RESTRICTIONS: hasRestrictions
							  RESTRICTION_TYPE: restrictionType
							  CHAT_RESTRICTIONS: chatRestrictions {
								START_TIME: startTime
								END_TIME: endTime
								START_DAY: startDay
								END_DAY: endDay
							  }
							}
						  }
					  
						  TIMEZONE: timezone
						}
					  }
					  `;
					this.makeGraphQLCall(query).subscribe(
						(response: any) => {
							this.generalSettingsForm.controls['MAX_CHATS_PER_AGENT'].setValue(
								response.COMPANY.CHAT_SETTINGS.MAX_CHATS_PER_AGENT
							);
							let teams = [];
							if (response.COMPANY.CHAT_SETTINGS.TEAMS_WHO_CAN_CHAT !== null) {
								response.COMPANY.CHAT_SETTINGS.TEAMS_WHO_CAN_CHAT.forEach((teamsWhoCanChat) => {
									let currentTeam = this.teams.find(
										(team) => team.ID === teamsWhoCanChat
									);
									teams.push(currentTeam);
								});
							}
							this.generalSettingsForm.controls['TEAMS_WHO_CAN_CHAT'].setValue(teams);
							this.timezone = response.COMPANY.TIMEZONE;
							this.hasRestriction = response.COMPANY.CHAT_SETTINGS.CHAT_BUSINESS_RULES.HAS_RESTRICTIONS;
							this.checked = response.COMPANY.CHAT_SETTINGS.CHAT_BUSINESS_RULES.HAS_RESTRICTIONS;
							if (response.COMPANY.CHAT_SETTINGS.CHAT_BUSINESS_RULES.HAS_RESTRICTIONS) {
								this.chatBusinessRules = response.COMPANY.CHAT_SETTINGS.CHAT_BUSINESS_RULES;
							}

						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
				});
		});
	}

	public saveGeneralSettings() {
		// this.companySettings.COMPANY_SUBDOMAIN = this.usersService.getSubdomain();
		// this.companySettings.MAX_CHATS_PER_AGENT =
		// 	this.generalSettingsForm.value.MAX_CHATS_PER_AGENT;
		// let roles = [];
		// this.generalSettingsForm.value.ROLES_WITH_CHAT.forEach((role) => {
		// 	roles.push(role.ROLE_ID);
		// });
		// this.companySettings.ROLES_WITH_CHAT = roles;
		// if (this.companySettings.ROLES_WITH_CHAT.length !== 0 && this.companySettings.MAX_CHATS_PER_AGENT !== 0) {
		// 	this.companySettingsApiService
		// 		.putChatSettings(this.companySettings)
		// 		.subscribe(
		// 			(putSettingsResponse: any) => {
		// 				this.router.navigate(['/company-settings']);
		// 				this.bannerMessageService.successNotifications.push({
		// 					message: this.translateService.instant(
		// 						'Settings has been updated successfully!'
		// 					),
		// 				});
		// 			},
		// 			(error: any) => {
		// 				this.bannerMessageService.errorNotifications.push({
		// 					message: error.error.ERROR,
		// 				});
		// 			}
		// 		);
		// } else {
		// 	if (this.companySettings.ROLES_WITH_CHAT.length === 0) {
		// 		this.loaderService.isLoading = false;
		// 		this.bannerMessageService.errorNotifications.push({
		// 			message: this.translateService.instant(
		// 				'ATLEAST_ONE_ROLE'
		// 			),
		// 		});
		// 	} else if (this.companySettings.MAX_CHATS_PER_AGENT === 0) {
		// 		this.loaderService.isLoading = false;
		// 		this.bannerMessageService.errorNotifications.push({
		// 			message: this.translateService.instant(
		// 				'SELECT_MAX_CHATS'
		// 			),
		// 		});
		// 	}
		// }
	}

	public makeGraphQLCall(query: string) {
		return this.httpClient.post(`${this.appGlobals.graphqlUrl}`, query);
	}

	public getTeamsData(pageNumber, searchValue, teamsModule) {
		let query = '';
		query = `{
					DATA: getTeams(moduleId: "${teamsModule.MODULE_ID}", pageNumber: ${pageNumber}, pageSize: 10, sortBy: "NAME", orderBy: "Asc", search: "${searchValue}") {
						id: ID
						name: NAME
					}
				}`;
		return this.httpClient.post(`${this.appGlobals.graphqlUrl}`, query);
	}
	public toggleRestrictions(event, editModal: boolean) {
		if (this.chatBusinessRules['RESTRICTION_TYPE'] !== 'Week') {
			this.chatBusinessRules['RESTRICTION_TYPE'] = 'Day'
		}

	}
}
