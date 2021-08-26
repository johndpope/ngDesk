import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { InviteEmailMessage } from '../../models/invite-email-message';

@Component({
  selector: 'app-invite-email',
  templateUrl: './invite-email.component.html',
  styleUrls: ['./invite-email.component.scss']
})
export class InviteEmailComponent implements OnInit {
  private inviteEmailMesssage: InviteEmailMessage = new InviteEmailMessage(
    '',
    '',
    ''
  );
  public inviteEmailForm: FormGroup;
  public successMessage: string;
  public errorMessage = '';
  public errors;
  public labels;
  public variables = [
    { display: 'First Name', backend: 'first_name' },
    { display: 'Last Name', backend: 'last_name' }
  ];

  constructor(
    private companiesService: CompaniesService,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private bannerMessageService: BannerMessageService
  ) {}

  public ngOnInit() {
    this.inviteEmailForm = this.formBuilder.group({
      SUBJECT: ['', Validators.required],
      MESSAGE_1: ['', Validators.required],
      MESSAGE_2: ['', Validators.required]
    });

    // for translating field labels with params
    this.labels = {
      greeting: { section: this.translateService.instant('GREETINGS_SECTION') },
      signature: { section: this.translateService.instant('SIGNATURE_SECTION') }
    };

    // for translating the mat errors with params
    this.errors = {
      subject: { field: this.translateService.instant('SUBJECT') },
      greeting: { field: this.translateService.instant('GREETINGS_SECTION') },
      signature: { field: this.translateService.instant('SIGNATURE_SECTION') }
    };

    this.companiesService.getCompanyInviteEmailMessage().subscribe(
      (inviteEmailMessageResponse: any) => {
        const regex = /<br\s*[\/]?>/gi;
        this.inviteEmailForm
          .get('SUBJECT')
          .setValue(inviteEmailMessageResponse.SUBJECT);
        this.inviteEmailForm
          .get('MESSAGE_1')
          .setValue(inviteEmailMessageResponse.MESSAGE_1.replace(regex, '\n'));
        this.inviteEmailForm
          .get('MESSAGE_2')
          .setValue(inviteEmailMessageResponse.MESSAGE_2.replace(regex, '\n'));
      },
      (error: any) => {
        this.errorMessage = error.ERROR;
      }
    );
  }

  public insertBodyVariable(variable, bodyText) {
    variable = ` ${variable} `;
    if (bodyText.selectionStart !== bodyText.selectionEnd) {
      bodyText.value = bodyText.value.replace(
        bodyText.value.substring(
          bodyText.selectionStart,
          bodyText.selectionEnd
        ),
        variable
      );
    } else {
      bodyText.value =
        bodyText.value.slice(0, bodyText.selectionStart) +
        variable +
        bodyText.value.slice(bodyText.selectionStart);
    }
  }

  public saveInviteEmailMessage() {
    if (this.inviteEmailForm.valid) {
      this.inviteEmailMesssage.subject = this.inviteEmailForm.get(
        'SUBJECT'
      ).value;
      this.inviteEmailMesssage.message1 = this.inviteEmailForm.get(
        'MESSAGE_1'
      ).value;
      this.inviteEmailMesssage.message2 = this.inviteEmailForm.get(
        'MESSAGE_2'
      ).value;
      this.companiesService
        .putCompanyInviteEmailMessage(this.inviteEmailMesssage)
        .subscribe(
          (inviteEmailMessageResponse: any) => {
            // this.errorMessage = '';
            // this.successMessage = this.translateService.instant(
            //   'SAVED_SUCCESSFULLY'
            // );
            this.bannerMessageService.successNotifications.push({
              message: this.translateService.instant('INVITE_EMAIL_SAVED')
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
