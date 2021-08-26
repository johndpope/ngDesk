import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
	MicrosoftTeams,
	MicrosoftTeamsApiService,
} from '@ngdesk/integration-api';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';

@Component({
	selector: 'app-microsoft-teams-authentication',
	templateUrl: './microsoft-teams-authentication.component.html',
	styleUrls: ['./microsoft-teams-authentication.component.scss'],
})
export class MicrosoftTeamsAuthenticationComponent implements OnInit {
	public channelId;
	public teamsData;
	public authenticated = true;

	public subdomain: String;
	public companyId: String;
	public emailAddress: string;

	constructor(
		private microsoftTeamsApiService: MicrosoftTeamsApiService,
		private route: ActivatedRoute,
		private router: Router,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService
	) {}

	ngOnInit() {
		this.route.queryParams.subscribe((res) => {
			this.channelId = res['channelId'];
			this.microsoftTeamsApiService
				.getMicrosoftTeam(this.channelId)
				.subscribe((response: any) => {
					this.saveUpdateChannel(response);
				});
		});
	}

	public saveUpdateChannel(content) {
		let teamsChannel: MicrosoftTeams = content;
		teamsChannel.AUTHENTICATED = true;
		this.microsoftTeamsApiService.putMicrosoftTeams(teamsChannel).subscribe(
			(dataResponse: any) => {
				if (dataResponse) {
					this.router.navigate([`company-settings/teams-integration`]);
					this.bannerMessageService.successNotifications.push({
						message: this.translateService.instant(
							'TEAMS_CHANNEL_AUTHENTICATED',
							{
								channelName: dataResponse.CHANNEL_NAME,
							}
						),
					});
				}
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}
}
