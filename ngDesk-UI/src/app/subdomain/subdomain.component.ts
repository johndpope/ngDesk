import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '../companies/companies.service';
import { Router } from '@angular/router';
import { ApplicationSettings } from '../ns-local-storage/app-settings-helper';

@Component({
  selector: 'app-subdomain',
  templateUrl: './subdomain.component.html',
  styleUrls: ['./subdomain.component.scss'],
  providers:[ApplicationSettings]
})
export class SubdomainComponent implements OnInit {

  public subdomainForm: FormGroup;
  public subdomain : string;
  public baseRestUrl : string;
  public loading=false;

  constructor(
    private translateService: TranslateService,
    private formBuilder: FormBuilder,
    private translate:TranslateService,
    private companiesService: CompaniesService,
    private router: Router,
    private applicationSettings:ApplicationSettings
    ) {
     
    this.subdomainForm = this.formBuilder.group({
      subdomain: ['', [Validators.required]],
    });
   
  }

  public ngOnInit(){
    this.loading = false;
  }

  public checkSubdomain() {
    this.loading = true;
    this.baseRestUrl = 'https://' + this.subdomainForm.value.subdomain+'.ngdesk.com/ngdesk-rest/ngdesk'
    this.subdomain =this.subdomainForm.value.subdomain;
    this.applicationSettings.setSubdomain(this.subdomain);
  
    // this.appGlobals.baseRestUrl=this.baseRestUrl;
    // console.log(getString("onPushToken"));
    this.companiesService.checkSubdomain(this.subdomainForm.value.subdomain).subscribe(
      (response: any) => {
        this.loading = false;
        this.router.navigate(['/login']);

        this.applicationSettings.setLocation(this.subdomain);
        // window.location = `https://${this.subdomainForm.value.subdomain}.ngdesk.com`;
      },
      (error: any) => {
        alert(this.translateService.instant('SUBDOMAIN_NOT_FOUND'));
        this.loading = false;
        console.log(error);
      }
    );

  }
}
