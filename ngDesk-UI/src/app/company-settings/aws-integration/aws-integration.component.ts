import { Component, OnInit } from '@angular/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { UsersService } from '@src/app/users/users.service';

@Component({
  selector: 'app-aws-integration',
  templateUrl: './aws-integration.component.html',
  styleUrls: ['./aws-integration.component.scss']
})
export class AwsIntegrationComponent implements OnInit {
AWSURL:any = "";
subdomain:any="";
public panelOpenState: boolean;
  constructor(
    private userService: UsersService,
    private bannerMessageService:BannerMessageService
  ) { }

  public ngOnInit() {

    this.subdomain = this.userService.getSubdomain();
    this.AWSURL = `https://${this.subdomain}.ngdesk.com/api/ngdesk-integration-service-v1/amazon/aws`;

  }

  public copyAWSURL(){
    this.bannerMessageService.successNotifications.push({
      message: "URL Copied"
    });
    return;
  }

}
