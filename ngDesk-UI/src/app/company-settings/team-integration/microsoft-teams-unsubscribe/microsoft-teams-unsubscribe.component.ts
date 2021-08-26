import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { MicrosoftTeamsApiService } from '@ngdesk/integration-api';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';

@Component({
  selector: 'app-microsoft-teams-unsubscribe',
  templateUrl: './microsoft-teams-unsubscribe.component.html',
  styleUrls: ['./microsoft-teams-unsubscribe.component.scss']
})
export class MicrosoftTeamsUnsubscribeComponent implements OnInit {

  constructor(
    private microsoftTeamsApiService:MicrosoftTeamsApiService,
	private bannerMessageService: BannerMessageService,
    private router: Router,
    private route: ActivatedRoute,
    public translateService: TranslateService,
  ) { }

  public ngOnInit() {
    const channelId = this.route.snapshot.queryParams.channelId;
    this.microsoftTeamsApiService
    .getMicrosoftTeam(channelId)
      .subscribe((team: any) => {
        this.microsoftTeamsApiService
        .deactivateChannel(team)
        .subscribe(
               (response: any) => {
                this.bannerMessageService.successNotifications.push({
                  message: this.translateService.instant(
                    'UNSUBSCRIBE_SUCCESSFULLY', {
                      channelName: team.CHANNEL_NAME
                    }
                  ),
                });
                this.router.navigate([`company-settings/teams-integration`]);
            },
            (error: any) => {
                this.bannerMessageService.errorNotifications.push({
                    message: error.error.ERROR,
                });
            }
        );
      }
    );
  } 
}
