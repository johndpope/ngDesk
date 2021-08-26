import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { SignupEmail } from '../../models/signup-email';

@Component({
  selector: 'app-email-template',
  templateUrl: './email-template.component.html',
  styleUrls: ['./email-template.component.scss']
})
export class EmailTemplateComponent implements OnInit {
  public errorMessage = '';
  public errors;
  public emailTemplateForm: FormGroup;
  private signupMessage: SignupEmail = new SignupEmail('', '');

  constructor(
    private companiesService: CompaniesService,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private bannerMessageService: BannerMessageService
  ) {}

  public ngOnInit() {
    this.errors = {
      subject: { field: this.translateService.instant('SUBJECT') },
      body: { field: this.translateService.instant('BODY') }
    };

    this.emailTemplateForm = this.formBuilder.group({
      SUBJECT: ['', Validators.required],
      BODY: ['', Validators.required]
    });

    this.companiesService.getCompanySignupMessage().subscribe(
      (signupMessageResponse: any) => {
        this.emailTemplateForm
          .get('SUBJECT')
          .setValue(signupMessageResponse.SUBJECT);
        this.emailTemplateForm
          .get('BODY')
          .setValue(signupMessageResponse.MESSAGE);
      },
      (error: any) => {
        this.errorMessage = error.ERROR;
      }
    );
  }

  public saveSignupMessage() {
    if (this.emailTemplateForm.valid) {
      this.signupMessage.subject = this.emailTemplateForm.get('SUBJECT').value;
      this.signupMessage.message = this.emailTemplateForm.get('BODY').value;
      this.companiesService
        .putCompanySignupMessage(this.signupMessage)
        .subscribe(
          (response: any) => {
            this.bannerMessageService.successNotifications.push({
              message: this.translateService.instant('SIGNUP_EMAIL_SAVED')
            });
          },
          (error: any) => {
            this.bannerMessageService.errorNotifications.push({
              message: error.ERROR
            });
          }
        );
    }
  }
}
