import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';

@Component({
  selector: 'app-social-sign-in',
  templateUrl: './social-sign-in.component.html',
  styleUrls: ['./social-sign-in.component.scss']
})
export class SocialSignInComponent implements OnInit {
  public socialSignInForm: FormGroup;
  public subdomain: string;
  public params = {
    enableFacebook: {},
    enableGoogle: {},
    enableMicrosoft: {},
    enableTwitter: {}
  };

  constructor(
    private translateService: TranslateService,
    private formBuilder: FormBuilder,
    private companiesService: CompaniesService,
    private bannerMessageService: BannerMessageService
  ) {
    this.translateService.get('ENABLE_FACEBOOK').subscribe((value: string) => {
      this.params['enableFacebook']['field'] = value;
    });
    this.translateService.get('ENABLE_GOOGLE').subscribe((value: string) => {
      this.params['enableGoogle']['field'] = value;
    });
    this.translateService.get('ENABLE_MICROSOFT').subscribe((value: string) => {
      this.params['enableMicrosoft']['field'] = value;
    });
    this.translateService.get('ENABLE_TWITTER').subscribe((value: string) => {
      this.params['enableTwitter']['field'] = value;
    });
  }

  public ngOnInit() {
    this.socialSignInForm = this.formBuilder.group({
      ENABLE_FACEBOOK: [false, [Validators.required]],
      ENABLE_GOOGLE: [false, [Validators.required]],
      ENABLE_MICROSOFT: [false, [Validators.required]],
      ENABLE_TWITTER: [false, [Validators.required]]
    });

    this.companiesService.getSocialSignInSettings().subscribe(
      (settings: any) => {
        this.socialSignInForm.controls['ENABLE_FACEBOOK'].setValue(
          settings.ENABLE_FACEBOOK
        );
        this.socialSignInForm.controls['ENABLE_GOOGLE'].setValue(
          settings.ENABLE_GOOGLE
        );
        this.socialSignInForm.controls['ENABLE_MICROSOFT'].setValue(
          settings.ENABLE_MICROSOFT
        );
        this.socialSignInForm.controls['ENABLE_TWITTER'].setValue(
          settings.ENABLE_TWITTER
        );
      },
      (error: any) => {
        this.bannerMessageService.errorNotifications.push({
          message: error.error.ERROR
        });
      }
    );
  }

  public saveSocialSignInSettings() {
    this.companiesService
      .putSocialSignInSettings(this.socialSignInForm.value)
      .subscribe(
        (putSettingsResponse: any) => {
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
}
