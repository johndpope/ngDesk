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
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CookieService } from 'ngx-cookie-service';
import { UsersService } from '../users/users.service';
import { ResetPasswordApiService ,ResetPassword } from '@ngdesk/auth-api';

export class PasswordErrorMatcher implements ErrorStateMatcher {
	public isErrorState(
		control: FormControl | null,
		form: FormGroupDirective | NgForm | null
	): boolean {
		const invalidCtrl = !!(control && control.invalid && control.parent.dirty);
		const invalidParent = !!(
			control &&
			control.parent &&
			control.parent.invalid &&
			control.parent.dirty
		);

		return invalidCtrl || invalidParent;
	}
}

@Component({
	selector: 'app-reset-password',
	templateUrl: './reset-password.component.html',
	styleUrls: ['./reset-password.component.scss'],
})
export class ResetPasswordComponent implements OnInit {
	public hidePassword = true;
	public resetPasswordForm: FormGroup;
	public matcher = new PasswordErrorMatcher();
	public errorMessage: string;
	public title: string;
	public errorParams = {
		captitalLetter: {},
		number: {},
		specialCharacter: {},
	};
	public resetPasswordObj :ResetPassword = {
		PASSWORD: '',
    	COMPANY_SUBDOMAIN : '',
    	USER_UUID: '',
    	TEMP_UUID: '',
	}

	constructor(
		private cookieService: CookieService,
		private route: ActivatedRoute,
		private router: Router,
		private formBuilder: FormBuilder,
		private userService: UsersService,
		public translateService: TranslateService,
		public resetPasswordApiService: ResetPasswordApiService
	) {}

	public ngOnInit() {
		this.resetPasswordForm = this.formBuilder.group(
			{
				password: ['', [Validators.required, this.validatePassword]],
				confirmPassword: [''],
			},
			{ validator: this.checkPasswords }
		);

		this.title =
			this.route.snapshot.routeConfig.path === 'create-password'
				? 'CREATE_PASSWORD'
				: 'RESET_PASSWORD';

		const isIE = /msie\s|trident\//i.test(window.navigator.userAgent);

		if (isIE) {
			alert(`ngDesk does not support Internet Explorer. Please use Edge, Chrome or Firefox

        ngDesk لا يدعم متصفح Internet Explorer. يُرجى استخدام Edge، أو Chrome، أو Firefox

        ngDesk unterstützt Internet Explorer nicht. Bitte verwenden Sie Edge, Chrome oder Firefox.

        Το ngDesk δεν υποστηρίζει Internet Explorer. Χρησιμοποιήστε Edge, Chrome ή Firefox

        ngDesk no es compatible con Internet Explorer. Usa Edge, Chrome o Firefox

        Internet Explorer ne prend pas en charge ngDesk. Veuillez utiliser Edge, Chrome ou Firefox

        ngDesk इंटरनेट एक्सप्लोरर का समर्थन नहीं करता है । कृपया एज, क्रोम या फायरफॉक्स का उपयोग करें

        ngDesk non supporta Internet Explorer. Si prega di utilizzare Edge, Chrome o Firefox

        ngDesk tidak menyokong Internet Explorer. Sila gunakan Edge, Chrome atau Firefox

        a ngDesk não suporta o Internet Explorer. Deve utilizar Edge, Chrome ou Firefox

        ngDesk не поддерживает Internet Explorer. Пожалуйста, используйте Edge, Chrome или Firefox

        ngDesk 不支持 IE 浏览器。请使用 Edge、Chrome 或火狐浏览器`);
		}
	}

	private checkPasswords(group: FormGroup) {
		const pass = group.controls.password.value;
		const confirmPass = group.controls.confirmPassword.value;
		return pass === confirmPass ? null : { notSame: true };
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

	private loadDetails(token) {
		this.cookieService.delete('authentication_token', '/', '.ngdesk.com');
		this.userService.setAuthenticationToken(token);
		const expiredDate = new Date();
		expiredDate.setDate(expiredDate.getDate() + 45);
		this.cookieService.delete('authentication_token', '/', window.location.host);
		if (window.location.host === 'dev1.ngdesk.com') {
			this.cookieService.set(
				'authentication_token',
				token,
				expiredDate,
				'/',
				window.location.host,
				false,
				'Lax'
			);
		} else {
			this.cookieService.set(
				'authentication_token',
				token,
				expiredDate,
				'/',
				window.location.host,
				true,
				'None'
			);
		}
	}

	public resetPassword() {
		if (this.resetPasswordForm.valid) {
			this.resetPasswordObj.PASSWORD = this.resetPasswordForm.value.password;
			this.resetPasswordObj.COMPANY_SUBDOMAIN = window.location.hostname.split('.')[0];	
			this.resetPasswordObj.TEMP_UUID = this.route.snapshot.queryParams.temp_uuid;
			this.resetPasswordObj.USER_UUID = this.route.snapshot.queryParams.uuid;

			this.resetPasswordApiService.postResetPassword(this.resetPasswordObj).subscribe(
				(resetPasswordResponse: any) => {
					this.errorMessage = null;

					if (this.route.snapshot.routeConfig.path === 'create-password') {
						let subdomain = (<any>window).location.hostname.split(
							'.ngdesk.com'
						)[0];
						if ((<any>window).location.hostname === 'localhost') {
							subdomain = 'dev1';
						}

						const creds = {
							EMAIL_ADDRESS: this.route.snapshot.queryParams.email_address,
							PASSWORD: this.resetPasswordForm.value.password,
							SUBDOMAIN: subdomain,
						};
						this.userService.login(creds).subscribe(
							(data: any) => {
								this.errorMessage = null;
								this.loadDetails(data.AUTHENTICATION_TOKEN);
								this.router.navigate(['login']);
							},
							(error: any) => {
								this.errorMessage = error.error.ERROR;
							}
						);
					} else {
						this.router.navigate(['login']);
					}
				},
				(errorResponse: any) => {
					this.errorMessage = errorResponse.error.ERROR;
				}
			);
		}
	}
}
