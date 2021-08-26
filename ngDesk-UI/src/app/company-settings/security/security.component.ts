import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { CompaniesService } from '../../companies/companies.service';

@Component({
  providers: [],
  selector: 'app-security',
  templateUrl: './security.component.html',
  styleUrls: ['./security.component.scss']
})
export class CompanySecurityComponent implements OnInit {

  public securityForm: FormGroup;
  public companyId: string;
  public successMssage: string;
  public errorMssage: string;

  constructor(
    private companiesService: CompaniesService,
    private formBuilder: FormBuilder,
    private bannerMessageService: BannerMessageService,
    private translateService: TranslateService) { }

  public ngOnInit() {

    this.securityForm = this.formBuilder.group({
      ENABLE_SIGNUPS: false,
      MAX_LOGIN_RETRIES: [1, [Validators.required, Validators.max(100), Validators.min(1)]]
    });

    // Getting company security from get security call
    this.companiesService.getSecurity().subscribe(
      (response: any) => {
        this.companyId = response.COMPANY_ID;
        this.securityForm.controls['ENABLE_SIGNUPS'].setValue(response.ENABLE_SIGNUPS);
        this.securityForm.controls['MAX_LOGIN_RETRIES'].setValue(response.MAX_LOGIN_RETRIES);
      }, (error: any) => {
        this.errorMssage = error.ERROR;
      }
    );
  }

  // Save the company security
  public save() {
    if (this.securityForm.valid) {
      const security = this.securityForm.value;
      security['COMPANY_ID'] = this.companyId;
      this.companiesService.putSecurity(security).subscribe(
        (response: any) => {
          this.bannerMessageService.successNotifications.push({
            message: this.translateService.instant(
              'Security has been updated successfully!'
            )
          });
        }, (error: any) => {
          this.bannerMessageService.errorNotifications.push({
            message: error.error.ERROR
          });
        }
      );
    }
  }
}
