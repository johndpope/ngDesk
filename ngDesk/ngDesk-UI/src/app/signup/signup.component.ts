import { Component, OnInit } from '@angular/core';
import {
	FormBuilder,
	FormControl,
	FormGroup,
	FormGroupDirective,
	NgForm,
	Validators,
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../companies/companies.service';
import { Customer } from '../models/customer';

import { RenderLayoutService } from '../render-layout/render-layout.service';

export class PasswordErrorMatcher implements ErrorStateMatcher {
	public isErrorState(
		control: FormControl | null,
		form: FormGroupDirective | NgForm | null
	): boolean {
		const invalidCtrl = !!(control && control.invalid && control.dirty);
		const invalidParent = !!(
			control &&
			control.parent &&
			control.dirty &&
			control.parent.errors &&
			control.parent.errors.hasOwnProperty('notSame')
		);

		return invalidCtrl || invalidParent;
	}
}

@Component({
	selector: 'app-signup',
	styleUrls: ['./signup.component.scss'],
	templateUrl: './signup.component.html',
})
export class SignupComponent implements OnInit {
	public customer: Customer;
	public confirmPassword: string = null;
	public captchaError = false;
	public signupForm: FormGroup;
	public matcher = new PasswordErrorMatcher();
	public errorMessage: string = null;
	public signupIcon;
	public header = 'Signup for ngDesk!';
	constructor(
		public translateService: TranslateService,
		private companiesService: CompaniesService,
		private router: Router,
		private formBuilder: FormBuilder,
		public renderLayoutService: RenderLayoutService
	) {
		this.signupForm = this.formBuilder.group(
			{
				signupEmailAddress: ['', [Validators.required, Validators.email]],
				signupPassword: ['', [Validators.required, this.validatePassword]],
				signupConfirmPassword: ['', [Validators.required]],
				signupFirstName: ['', [Validators.required]],
				signupLastName: ['', [Validators.required]],
				signupPhoneNumber: [
					{
						COUNTRY_CODE: 'us',
						DIAL_CODE: '+1',
						COUNTRY_FLAG: 'us.svg',
						PHONE_NUMBER: '',
					},
					[Validators.required],
				],
				phoneNumber: ['', [Validators.required]],
			},
			{ validator: this.checkPasswords }
		);
	}

	public ngOnInit() {
		console.log('Entered on init');
		console.log(this.signupForm.value.signupPhoneNumber);
		(<any>window).onloadCallback = function () {
			(<any>window).grecaptcha.render('ngdesk_gcaptcah', {
				sitekey: '6LcMIWgUAAAAAJ2vlypSg5uu9NyjDZm_qnSu6LlT',
			});
		};

		const body = <HTMLDivElement>document.body;
		const script = document.createElement('script');
		script.innerHTML = '';
		script.src =
			'https://www.google.com/recaptcha/api.js?onload=onloadCallback&render=explicit';
		script.async = true;
		script.defer = true;
		body.appendChild(script);

		this.companiesService.getTheme().subscribe(
			(response: any) => {
				this.header = response.SIGNUP_PAGE.HEADER;
				this.signupIcon = response.SIGNUP_PAGE.FILE;
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);
	}

	private validatePassword(control: FormControl) {
		if (control.value.length < 8) {
			// if there is not at least 8 characters
			return { minlength: true };
		} else if (!/(?=.*?[A-Z])/.test(control.value)) {
			// if there is no capital letter
			return { uppercaseReq: true };
		} else if (!/(?=.*?[0-9])/.test(control.value)) {
			// if there is no number
			return { numberReq: true };
		} else if (!/(?=.*?[_#?!@$%^&*-])/.test(control.value)) {
			// if there is no special character
			return { specialCharReq: true };
		}
		return null;
	}

	private checkPasswords(group: FormGroup) {
		const pass = group.controls.signupPassword.value;
		const confirmPass = group.controls.signupConfirmPassword.value;
		return pass === confirmPass ? null : { notSame: true };
	}

	public getEmailErrorMessage() {
		if (this.signupForm.controls.signupEmailAddress.errors.required) {
			let message = '';
			const parameters = {
				field: this.translateService.instant('EMAIL_ADDRESS'),
			};
			this.translateService
				.get('FIELD_REQUIRED', parameters)
				.subscribe((res: string) => {
					message += res;
				});
			return message;
		} else if (this.signupForm.controls.signupEmailAddress.errors.email) {
			return this.translateService.instant('EMAIL_MUST_BE_VALID');
		} else {
			return this.translateService.instant('UNKOWN_ERROR_OCCURED');
		}
	}

	public signUp() {
		if (this.signupForm.valid) {
			const captcha = (<any>window).grecaptcha.getResponse();
			if (captcha === '') {
				this.captchaError = true;
			} else {
				this.captchaError = false;
				this.signupForm.value.signupPhoneNumber.PHONE_NUMBER = this.signupForm.value.phoneNumber;
				console.log(this.signupForm.value.signupPhoneNumber);
				// create customer object using form element values
				this.customer = new Customer(
					this.signupForm.value.signupEmailAddress,
					this.signupForm.value.signupPassword,
					this.signupForm.value.signupFirstName,
					this.signupForm.value.signupLastName,
					this.signupForm.value.signupPhoneNumber
				);

				this.companiesService.postSignup(this.customer, captcha).subscribe(
					(data: any) => {
						this.router.navigate(['/login']);
					},
					(error: any) => {
						this.errorMessage = error.error.ERROR;
						(<any>window).grecaptcha.reset();
					}
				);
			}
		}
	}

	public updatePhoneInfo(country) {
		this.signupForm.value.signupPhoneNumber.COUNTRY_CODE = country.COUNTRY_CODE;
		this.signupForm.value.signupPhoneNumber.DIAL_CODE =
			country.COUNTRY_DIAL_CODE;
		this.signupForm.value.signupPhoneNumber.COUNTRY_FLAG = country.COUNTRY_FLAG;
	}
}
