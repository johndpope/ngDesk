import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

@Component({
	selector: 'app-camgaign-category',
	templateUrl: './camgaign-category.component.html',
	styleUrls: ['./camgaign-category.component.scss']
})
export class CamgaignCategoryComponent implements OnInit {
	public availableItems = [];

	constructor(
		private router: Router,
		private translateService: TranslateService
	) {
		this.translateService.get('PLAIN').subscribe((plainValue: string) => {
			this.translateService.get('SIMPLE').subscribe((simpleValue: string) => {
				this.translateService
					.get('WELCOME')
					.subscribe((welcomeValue: string) => {
						this.translateService
							.get('PLAIN_EMAIL_DESCRIPTION')
							.subscribe((plainEmailDescription: string) => {
								this.translateService
									.get('SIMPLE_EMAIL_DESCRIPTION')
									.subscribe((simpleEmailDescription: string) => {
										this.translateService
											.get('WELCOME_EMAIL_DESCRIPTION')
											.subscribe((welcomeEmailDescription: string) => {
												this.availableItems = [
													{
														DISPLAY: welcomeValue,
														TYPE: 'Welcome',
														DESCRIPTION: welcomeEmailDescription,
														IMAGE_SRC:
															'../../../../../assets/images/campaign-previews/campaign_welcome.png'
													},
													{
														DISPLAY: plainValue,
														TYPE: 'Plain',
														DESCRIPTION: plainEmailDescription,
														IMAGE_SRC:
															'../../../../../assets/images/campaign-previews/campaign_plain.png'
													},
													{
														DISPLAY: simpleValue,
														TYPE: 'Simple',
														DESCRIPTION: simpleEmailDescription,
														IMAGE_SRC:
															'../../../../../assets/images/campaign-previews/campaign_simple.png'
													}
												];
											});
									});
							});
					});
			});
		});
	}

	public ngOnInit() {}

	public newCampaign(campaignType: String): void {
		this.router.navigate([
			`company-settings/marketing/campaigns/${campaignType}/new`
		]);
	}
}
