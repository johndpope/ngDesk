import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ForgotPasswordApiService } from '@ngdesk/auth-api';
import { ForgotPassword } from '@ngdesk/auth-api';
import { CompaniesService } from '@src/app/companies/companies.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent implements OnInit {
  public forgotPassword: ForgotPassword = {
	EMAIL_ADDRESS: '',
    COMPANY_SUBDOMAIN: '',
  };
  public forgotPasswordSubmitted = false;
  public captchaError = false;
  public grecaptcha: any;
  public errorMessage: '';
  public forgotPasswordForm: FormGroup;

  constructor(
	private router: Router,
	private formBuilder: FormBuilder,
	private companiesService: CompaniesService,
	private forgotPasswordApiService:ForgotPasswordApiService,
	public translateService: TranslateService
  ) {}

  public ngOnInit() {
	// for captcha
	(<any>window).onloadCallback = function() {
		(<any>window).grecaptcha.render('ngdesk_gcaptcah', {
		sitekey: '6LcMIWgUAAAAAJ2vlypSg5uu9NyjDZm_qnSu6LlT'
		});
	};

	this.forgotPasswordForm = this.formBuilder.group({
		EMAIL_ADDRESS: ['', [Validators.required, Validators.email]]
	});

	const body = <HTMLDivElement>document.body;
	const script = document.createElement('script');
	script.innerHTML = '';
	script.src =
		'https://www.google.com/recaptcha/api.js?onload=onloadCallback&render=explicit';
	script.async = true;
	script.defer = true;
	body.appendChild(script);
  }

  public submitForgotPassword() {
	this.errorMessage = '';
	// checks if captcha has been completed correctly
	const captcha = (<any>window).grecaptcha.getResponse();
	if (captcha !== '' && this.forgotPasswordForm.valid) {
		this.captchaError = false;
		this.forgotPassword.EMAIL_ADDRESS = this.forgotPasswordForm.value.EMAIL_ADDRESS;
		this.forgotPassword.COMPANY_SUBDOMAIN = window.location.hostname.split('.')[0];		
		this.forgotPasswordApiService
		.postForgotPassword(captcha,this.forgotPassword)
		.subscribe(
			(data: any) => {
			this.forgotPasswordSubmitted = true;
			},
			(error: any) => {
			this.errorMessage = error.error.ERROR;
			}
		);
	} else if (captcha === '') {
		this.captchaError = true;
	}
  }

  public goToLogin() {
	// redirect to login page
	this.router.navigate(['/login']);
  }
}
