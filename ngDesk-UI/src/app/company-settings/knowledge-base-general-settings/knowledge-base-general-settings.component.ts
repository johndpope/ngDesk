import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';

@Component({
  selector: 'app-knowledge-base-general-settings',
  templateUrl: './knowledge-base-general-settings.component.html',
  styleUrls: ['./knowledge-base-general-settings.component.scss']
})
export class KnowledgeBaseGeneralSettingsComponent implements OnInit {

  public generalSettingsForm: FormGroup;
  public subdomain: string;
  public params = {
    enableDocs: {}
  };

  constructor(private translateService: TranslateService, private formBuilder: FormBuilder, private companiesService: CompaniesService,
    private bannerMessageService: BannerMessageService) {
      this.translateService.get('ENABLE_KNOWLEDGE_BASE').subscribe((value: string) => {
        this.params['enableDocs']['field'] = value;
      });
    }

  public ngOnInit() {
    this.generalSettingsForm = this.formBuilder.group({
      ENABLE_DOCS: ['', [Validators.required]]
    });

    this.companiesService.getKnowledgeBaseGeneralSettings().subscribe(
      (settings: any) => {
        this.generalSettingsForm.controls['ENABLE_DOCS'].setValue(settings.ENABLE_DOCS);
      },
      (error: any) => {
        this.bannerMessageService.errorNotifications.push({ message: error.error.ERROR });
      }
    );
  }

  public saveGeneralSettings() {
    this.companiesService.putKnowledgeBaseGeneralSettings(this.generalSettingsForm.value).subscribe(
      (putSettingsResponse: any) => {
        this.bannerMessageService.successNotifications.push({ message: this.translateService.instant('SETTINGS_SAVE_SUCCESS') });
      },
      (error: any) => {
        this.bannerMessageService.errorNotifications.push({ message: error.error.ERROR });
      }
    );
  }

}
