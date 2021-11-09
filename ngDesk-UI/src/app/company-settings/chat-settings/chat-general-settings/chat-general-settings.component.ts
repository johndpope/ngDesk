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
import { ChatBusinessRulesComponent } from './chat-business-rules/chat-business-rules.component';
import { MatChipInputEvent } from '@angular/material/chips';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, map, mergeMap } from 'rxjs/operators';
import { SchedulesDetailService } from '@src/app/schedules/schedules-detail/schedules-detail.service';

@Component({
	selector: 'app-chat-general-settings',
	templateUrl: './chat-general-settings.component.html',
	styleUrls: ['./chat-general-settings.component.scss'],
})
export class ChatGeneralSettingsComponent implements OnInit {
	public maxChatOptions = [1, 2, 3, 4, 5];
	public teams = [];
	public subdomain: string;
	public params = {
		max_chats_per_agent: {},
	};
	public companySettings: CompanySettings = {
		COMPANY_SUBDOMAIN: '',
		TIMEZONE: '',
		CHAT_SETTINGS: {
			MAX_CHATS_PER_AGENT: 0
		},

	};
	public teamsModule;
	public isRestricted = false;
	public timezone = '';
	public chatBusinessRules = {};
	public timezones;
	public maxChatsPerAgent = 0;
	public selectedTeamIds = [];
	public selectedTeams = [];
	public hasRestrictions = false;
	public tempTeamInput = '';
	public chatDataScrollSubject = new Subject<any>();


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
		public schedulesDetailService: SchedulesDetailService,

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
		this.timezones = this.schedulesDetailService.timeZones;
		this.modulesService.getModuleByName('Teams').subscribe((response: any) => {
			this.teamsModule = response;
			this.getTeamsData(0, '', this.teamsModule)
				.subscribe((teamResponse) => {
					let response = [];
					response = teamResponse['DATA'];
					let filteredTeams = [];
					if (response.length > 0) {
						response.forEach(team => {
							if (team.name !== 'Ghost Team' && team.name !== 'Public') {
								filteredTeams.push(team);
							}
						});
					}

					this.teams = filteredTeams;
					this.initializeChatDataScrollSubject();
					const query = `{
						COMPANY: getCompanyDetails {
						  CHAT_SETTINGS: chatSettings {
							MAX_CHATS_PER_AGENT: maxChatsPerAgent
							TEAMS_WHO_CAN_CHAT: teamsWhoCanChat{
								id: _id
								name: NAME
							}
							HAS_RESTRICTIONS: hasRestrictions
							CHAT_BUSINESS_RULES: chatBusinessRules {
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
							this.timezone = response.COMPANY.TIMEZONE;

							if (response.COMPANY.CHAT_SETTINGS !== undefined && response.COMPANY.CHAT_SETTINGS !== null) {
								this.maxChatsPerAgent
									= response.COMPANY.CHAT_SETTINGS.MAX_CHATS_PER_AGENT;
								if (response.COMPANY.CHAT_SETTINGS.TEAMS_WHO_CAN_CHAT !== null) {
									let currentTeams = [];
									this.selectedTeams = response.COMPANY.CHAT_SETTINGS.TEAMS_WHO_CAN_CHAT;

									response.COMPANY.CHAT_SETTINGS.TEAMS_WHO_CAN_CHAT.forEach((teamsWhoCanChat) => {

										currentTeams.push(teamsWhoCanChat.id);

									});
									this.selectedTeamIds = currentTeams;

								}
								this.hasRestrictions = response.COMPANY.CHAT_SETTINGS.HAS_RESTRICTIONS;
								if (this.hasRestrictions) {
									let businessRules = response.COMPANY.CHAT_SETTINGS.CHAT_BUSINESS_RULES;
									this.chatBusinessRules = businessRules;
								}
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
		this.companySettings.COMPANY_SUBDOMAIN = this.usersService.getSubdomain();
		this.companySettings.TIMEZONE = this.timezone;
		this.companySettings.CHAT_SETTINGS.MAX_CHATS_PER_AGENT = this.maxChatsPerAgent;
		let teams = [];
		this.selectedTeams.forEach((team) => {
			teams.push(team.id);
		});
		this.companySettings.CHAT_SETTINGS.TEAMS_WHO_CAN_CHAT = teams;
		this.companySettings.CHAT_SETTINGS.HAS_RESTRICTIONS = this.hasRestrictions;
		this.companySettings.CHAT_SETTINGS.CHAT_BUSINESS_RULES = this.chatBusinessRules;
		if (this.companySettings.CHAT_SETTINGS.TEAMS_WHO_CAN_CHAT.length !== 0 && this.companySettings.CHAT_SETTINGS.MAX_CHATS_PER_AGENT !== 0) {
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
			if (this.companySettings.CHAT_SETTINGS.TEAMS_WHO_CAN_CHAT.length === 0) {
				this.loaderService.isLoading = false;
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant(
						'ATLEAST_ONE_TEAM'
					),
				});
			} else if (this.companySettings.CHAT_SETTINGS.MAX_CHATS_PER_AGENT === 0) {
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

	public getTeamsData(pageNumber, searchValue, teamsModule) {
		let query = '';
		query = `{
					DATA: getTeams(moduleId: "${teamsModule.MODULE_ID}", pageNumber: ${pageNumber}, pageSize: 10, sortBy: "NAME", orderBy: "Asc", search: "${searchValue}") {
						id: _id
						name: NAME
					}
				}`;
		return this.httpClient.post(`${this.appGlobals.graphqlUrl}`, query);
	}
	public toggleRestrictions(event, editModal: boolean) {
		if (this.hasRestrictions) {
			if (this.chatBusinessRules['RESTRICTION_TYPE'] !== 'Week') {
				this.chatBusinessRules['RESTRICTION_TYPE'] = 'Day';
			}
			const dialogRef = this.dialog.open(ChatBusinessRulesComponent, {
				width: '600px',
				data: {
					businessRuleValue: this.chatBusinessRules,
					isRestrictedValue: this.hasRestrictions,
				},
				disableClose: true,
			});

			dialogRef.afterClosed().subscribe((result) => {
				if (result) {
					this.chatBusinessRules = result.data.businessRuleValue;
					this.hasRestrictions = result.data.isRestrictedValue;
				} else {
					this.hasRestrictions = false;
					this.chatBusinessRules['RESTRICTION_TYPE'] = null;
				}
			});
		} else {
			this.chatBusinessRules['CHAT_RESTRICTIONS'] = [];
			this.chatBusinessRules['RESTRICTION_TYPE'] = null;
		}
	}


	public initializeChatDataScrollSubject() {
		this.chatDataScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let searchValue = '';
					if (value !== '') {
						searchValue = 'NAME' + '=' + value;
					}
					let page = 0;

					if (this.teams && !search) {
						page = Math.ceil(this.teams.length / 10);
					}
					return this
						.getTeamsData(page, searchValue, this.teamsModule)
						.pipe(
							mergeMap((results: any) => {
								let response = [];
								response = results['DATA'];
								let filteredTeams = [];
								if (response.length > 0) {
									response.forEach(teamToAdd => {
										if (teamToAdd.name !== 'Ghost Team' && teamToAdd.name !== 'Public') {
											filteredTeams.push(teamToAdd);
										}
									});
									if (search) {
										this.teams = filteredTeams;
									} else {
										this.teams = this.teams.concat(filteredTeams);
									}
									this.teams = this.teams.filter((team, index) => this.teams.findIndex(item => item.id == team.id) === index);
									return this.teams;
								}

							})
						);
				})
			)
			.subscribe();
	}

	public removeTeam(index) {
		this.selectedTeams.splice(index, 1);
		let teamIds = [];
		this.selectedTeams.forEach((selectedTeam: any) => {
			teamIds.push(selectedTeam.id);
		});
		this.selectedTeamIds = teamIds;
		this.searchTeam();
	}

	public resetInput(event: MatChipInputEvent): void {
		const input = event.input;
		if (input) {
			input.value = '';
		}
	}

	public onTeamsScroll() {
		this.chatDataScrollSubject.next([this.tempTeamInput, false]);
	}
	public searchTeam() {
		this.chatDataScrollSubject.next([this.tempTeamInput, true]);
	}

	public addTeam(event) {
		this.selectedTeams.push(event.option.value);
		this.selectedTeamIds.push(event.option.value.id);
		this.tempTeamInput = '';
		this.searchTeam();
	}

	public disableSelectedTeams(teamId) {
		if (this.selectedTeamIds.length > 0) {
			if (this.selectedTeamIds.includes(teamId)) {
				return true;
			}
			return false;
		}
		return false;
	}

}
