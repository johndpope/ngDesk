import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { AgentService } from '../agent.service';

@Component({
	selector: 'app-download-agent',
	templateUrl: './download-agent.component.html',
	styleUrls: ['./download-agent.component.scss'],
})
export class DownloadAgentComponent implements OnInit {
	constructor(
		private agentService: AgentService,
		private bannerMessage: BannerMessageService,
		private translate: TranslateService
	) {}

	public ngOnInit() {}

	public downloadApplication(filename: any) {
		const url =
			window.location.protocol +
			'//' +
			window.location.host +
			'/installers/' +
			filename;

		const link = document.createElement('a');
		link.setAttribute('target', '_blank');
		link.setAttribute('href', url);
		document.body.appendChild(link);
		link.click();
		link.remove();

		return url;
	}
}
