import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MicrosoftTeamsApiService } from '@ngdesk/integration-api';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { UnsubscribeDialogComponent } from '@src/app/dialogs/unsubscribe-dialog/unsubscribe-dialog.component';

@Component({
  selector: 'app-microsoft-teams-integration',
  templateUrl: './microsoft-teams-integration.component.html',
  styleUrls: ['./microsoft-teams-integration.component.scss']
})
export class MicrosoftTeamsIntegrationComponent implements OnInit {

  public navigations: any = [];
  public remainValues = 10;
  public length:any=[];
  public microsoftTeams = {};
  constructor(
    private microsoftTeamsApiService:MicrosoftTeamsApiService,
    private dialog: MatDialog,
    private translateService: TranslateService,
	  private bannerMessageService: BannerMessageService
  ) { }

  public ngOnInit() {
    this.getAllMicrosoftTeams();
  }

  public getAllMicrosoftTeams(){
	this.microsoftTeamsApiService
      .getMicrosoftTeams()
    	.subscribe((response: any) => {
			this.navigations=response.content;
			this.navigations.forEach(element => {
				element.TEAMS_CONTEXT_ACTIVITY= JSON.parse(element.TEAMS_CONTEXT_ACTIVITY);
			});
			this.length=response;
        }
    );
  }

  public getMoreData() {
     this.remainValues = this.length.numberOfElements;
  }

  public unsubscribe(nav): void {
    this.microsoftTeamsApiService
      .getMicrosoftTeam(nav.CHANNEL_ID)
      .subscribe(
        (team: any) => {
          const dialogRef = this.dialog.open(UnsubscribeDialogComponent, {
            width: '500px',
            data: {
              message:
                this.translateService.instant(
                  'WARNING_MESSAGE'
                ) +
                '.' ,
              buttonText: this.translateService.instant('UNSUBSCRIBE'),
              closeDialog: this.translateService.instant('CANCEL'),
              action: this.translateService.instant('UNSUBSCRIBE'),
              executebuttonColor: 'warn',
            },
          });
          // EVENT AFTER MODAL DIALOG IS CLOSED
          dialogRef.afterClosed().subscribe((result) => {
            if (result === this.translateService.instant('UNSUBSCRIBE')) {
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
                    this.getAllMicrosoftTeams();
                  },
                  (error: any) => {
                    this.bannerMessageService.errorNotifications.push({
                      message: error.error.ERROR,
                    });
                  }
                );
            }
          });
        });
	}
}
