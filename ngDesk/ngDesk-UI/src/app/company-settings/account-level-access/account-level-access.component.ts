import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '../../app.globals';
import { UsersService } from '../../users/users.service';
import { CompanySettingsApiService } from '@ngdesk/company-api';
import { CompanySettings } from '@ngdesk/company-api';
import { Router } from '@angular/router';


@Component({
  selector: 'app-account-level-access',
  templateUrl: './account-level-access.component.html',
  styleUrls: ['./account-level-access.component.scss']
})
export class AccountLevelAccessComponent implements OnInit {
  public accountLevelAccess = false;
  public subdomain: string;
  public companySettings: CompanySettings = {};

  constructor(
    private translateService: TranslateService,
    private formBuilder: FormBuilder,
    private companiesService: CompaniesService,
    private bannerMessageService: BannerMessageService,
    private httpClient: HttpClient,
    private appGlobals: AppGlobals,
    private usersService: UsersService,
    private companyApiService: CompanySettingsApiService,
    private router: Router
  ) {}

  public ngOnInit() {
    const query = ` {

      COMPANY: getCompanyDetails{
        ACCOUNT_LEVEL_ACCESS: accountLevelAccess
      }
           }`
    this.makeGraphQLCall(query).subscribe(
      (response: any) => {
        if(response.COMPANY.ACCOUNT_LEVEL_ACCESS === null ||  response.COMPANY.ACCOUNT_LEVEL_ACCESS === false){
          this.accountLevelAccess = false;
        } else {
          this.accountLevelAccess = true;
        }
      },
      (error: any) => {
        this.bannerMessageService.errorNotifications.push({
          message: error.error.ERROR
        });
      }
    );
  }

  public save() {
    this.companySettings.COMPANY_SUBDOMAIN = this.usersService.getSubdomain();
    this.companySettings.ACCOUNT_LEVEL_ACCESS  = this.accountLevelAccess;
    this.companyApiService.putAccountLevelAccess(this.companySettings).subscribe(
        (putSettingsResponse: any) => {
          this.router.navigate(['/company-settings']);
          this.bannerMessageService.successNotifications.push({
            message: this.translateService.instant(
              'Settings has been updated successfully!'
            )
          });
        },
        (error: any) => {
          this.bannerMessageService.errorNotifications.push({
            message: error.error.ERROR
          });
        }
      );
  }

  public makeGraphQLCall(query: string) {
		return this.httpClient.post(`${this.appGlobals.graphqlUrl}`, query);
	}

}
