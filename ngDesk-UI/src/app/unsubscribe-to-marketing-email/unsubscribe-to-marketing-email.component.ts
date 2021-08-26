import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BannerMessageService } from '../custom-components/banner-message/banner-message.service';
import { UsersService } from '../users/users.service';

@Component({
  selector: 'app-unsubscribe-to-marketing-email',
  templateUrl: './unsubscribe-to-marketing-email.component.html',
  styleUrls: ['./unsubscribe-to-marketing-email.component.scss']
})
export class UnsubscribeToMarketingEmailComponent implements OnInit {
  public displayPage: boolean;
  public errorMessage: string;
  public email: any;
  public uuid: any;
  constructor(
    private usersService: UsersService,
    private bannerMessageService: BannerMessageService,
    private route: ActivatedRoute
  ) {}

  public ngOnInit() {
    this.email = this.route.snapshot.queryParams['email'];
    this.uuid = this.route.snapshot.queryParams['uuid'];
    this.usersService
      .postUnsubscriptionToMarketingEmail(this.email, this.uuid)
      .subscribe(
        (response: any) => {
          this.displayPage = true;
        },
        (error: any) => {
          this.displayPage = false;
          this.errorMessage = error.error.ERROR;
        }
      );
  }
}
