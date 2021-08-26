import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ChannelsService } from 'src/app/channels/channels.service';
import { CompaniesService } from 'src/app/companies/companies.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { UsersService } from 'src/app/users/users.service';

@Component({
	selector: 'app-plugins',
	templateUrl: './plugins.component.html',
	styleUrls: ['./plugins.component.scss']
})
export class PluginsComponent implements OnInit {
	public subdomain: string;
	public widgetId: string;
	public script;
	public path: string;

	constructor(
		public companiesService: CompaniesService,
		private channelsService: ChannelsService,
		public translateService: TranslateService,
		private usersService: UsersService,
		private bannerMessageService: BannerMessageService,
		private router: Router,
		private route: ActivatedRoute
	) {
		this.subdomain = this.usersService.getSubdomain();
		this.channelsService.getChatChannels().subscribe(
			(response: any) => {
				this.widgetId = response.CHANNELS[0].CHANNEL_ID;
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR
				});
			}
		);
	}

	public ngOnInit() {
		this.path = this.route.snapshot.url[1].path;
	}
}
