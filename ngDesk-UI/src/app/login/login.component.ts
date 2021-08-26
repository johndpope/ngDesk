import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import * as jwt_decode from 'jwt-decode';
import { CookieService } from 'ngx-cookie-service';
import { NGXLogger } from 'ngx-logger';
// import { NativeScriptSanitizer } from 'nativescript-angular/platform-common';
import { AppGlobals } from '../app.globals';
import { CompaniesService } from '../companies/companies.service';
import { ModulesService } from '../modules/modules.service';
import { UsersService } from '../users/users.service';

import { CacheService } from '@src/app/cache.service';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer, BrowserModule } from '@angular/platform-browser';
import { BannerMessageService } from '../custom-components/banner-message/banner-message.service';
// import { RouterExtensions } from 'nativescript-angular/router';
import { DomSanitizerHelper } from '../dom-sanitizer/domsanitizer-helper-service';
import { RouterNavigationHelper } from '../router-navigation/router-navigation-helper';
import { WebsocketService } from '../websocket.service';
// import { ApplicationSettings } from '../ns-local-storage/app-settings-helper';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.scss'],
	moduleId: module.id,
	providers: [
		DomSanitizerHelper,
		RouterNavigationHelper,
		// ,ApplicationSettings
	],
})
export class LoginComponent implements OnInit {
	public loginForm: FormGroup;
	public isIE = false;
	public enableFacebook = false;
	public enableGoogle = false;
	public enableMicrosoft = false;
	public enableTwitter = false;
	public errorMessage: string;
	public isSubmitting = false;
	public loginIcon;
	public header = 'Welcome to ngDesk';
	public subdomain = '';
	public enableSignup = false;
	private fulldomain: string;
	public loginRedirectUrl: string;
	public htmlString: string;

	constructor(
		private companiesService: CompaniesService,
		private userService: UsersService,
		private router: Router,
		private route: ActivatedRoute,
		private cookieService: CookieService,
		private logger: NGXLogger,
		private formBuilder: FormBuilder,
		private modulesService: ModulesService,
		private translateService: TranslateService,
		// private ngRouter: RouterExtensions,
		// private matIconRegistry: MatIconRegistry,
		// private domSanitizer: DomSanitizer,
		private globals: AppGlobals,
		private bannerMessageService: BannerMessageService,
		private domSanitizerHelper: DomSanitizerHelper,
		private routerNavigationHelper: RouterNavigationHelper,
		// private applicationSettingsHelper:ApplicationSettings,
		private appGlobals: AppGlobals,
		private cacheService: CacheService,
		private websocketService: WebsocketService
	) {
		domSanitizerHelper.sanitizer();
		// this.ngSanitizer.sanitize(
		// 	"facebook",'../../../assets/images/facebook.svg'
		// )

		// this.matIconRegistry.addSvgIcon(
		// 	'facebook',
		// 	this.domSanitizer.bypassSecurityTrustResourceUrl(
		// 		'../../../assets/images/facebook.svg'
		// 	)
		// );
		// this.matIconRegistry.addSvgIcon(
		// 	'google',
		// 	this.domSanitizer.bypassSecurityTrustResourceUrl(
		// 		'../../../assets/images/google.svg'
		// 	)
		// );
		// this.matIconRegistry.addSvgIcon(
		// 	'twitter',
		// 	this.domSanitizer.bypassSecurityTrustResourceUrl(
		// 		'../../../assets/images/twitter.svg'
		// 	)
		// );
		// this.matIconRegistry.addSvgIcon(
		// 	'microsoft',
		// 	this.domSanitizer.bypassSecurityTrustResourceUrl(
		// 		'../../../assets/images/microsoft.svg'
		// 	)
		// );
	}

	public ngOnInit() {
		this.htmlString = `<span style='color:blue'>NativeScript
	 </span>`;

		this.loginForm = this.formBuilder.group({
			emailAddress: ['', [Validators.required, Validators.email]],
			password: ['', [Validators.required]],
		});

		this.loginRedirectUrl = this.cookieService.get('login_redirect');
		this.companiesService.getSocialSignInSettings().subscribe(
			(data: any) => {
				if (data.ENABLE_FACEBOOK) {
					this.enableFacebook = true;
				}
				if (data.ENABLE_GOOGLE) {
					this.enableGoogle = true;
				}
				if (data.ENABLE_MICROSOFT) {
					this.enableMicrosoft = true;
				}
				if (data.ENABLE_TWITTER) {
					this.enableTwitter = true;
				}
			},
			(error: any) => {
				console.log(error);
			}
		);
		if (window.location.href.includes('?')) {
			if (
				window.location.href.split('?')[1].split('=')[0] ===
					'authentication_token' &&
				window.location.href.split('=')[1]
			) {
				this.route.queryParams.subscribe((params) => {
					const authenticationToken = params['authentication_token'];
					const decoded = jwt_decode(authenticationToken);
					this.loadDetails(
						decoded.USER,
						authenticationToken,
						decoded.COMPANY_UUID,
						decoded.SUBDOMAIN
					);
					this.cacheService.loadAllData();
					if (
						this.loginRedirectUrl &&
						this.loginRedirectUrl !== '/login' &&
						this.loginRedirectUrl !== '/reset-password' &&
						this.loginRedirectUrl !== '/create-password'
					) {
						this.router.navigateByUrl(this.loginRedirectUrl);
					} else {
						this.companiesService.setLocaleData();
						this.modulesService.getModuleByName('Tickets').subscribe(
							(response: any) => {
								this.routerNavigationHelper.navigate(response);
							},
							(error: any) => {
								console.log(error);
							}
						);
					}
				});
			}
		} else {
			if (this.cookieService.get('authentication_token') !== '') {
				this.userService.validate().subscribe(
					(data: any) => {
						const jwtToken = data.AUTHENTICATION_TOKEN;
						const decoded = jwt_decode(jwtToken);

						// Jwt time is always in ms hence dividing by 1000
						if (decoded.exp > new Date().getTime() / 1000) {
							this.loadDetails(
								decoded.USER,
								jwtToken,
								decoded.COMPANY_UUID,
								decoded.SUBDOMAIN
							);
							this.cacheService.loadAllData();
							if (
								this.loginRedirectUrl &&
								this.loginRedirectUrl !== '/login' &&
								this.loginRedirectUrl !== '/reset-password' &&
								this.loginRedirectUrl !== '/create-password'
							) {
								this.router.navigateByUrl(this.loginRedirectUrl);
							} else {
								// CALL CACHE SERVICE INIT

								this.companiesService.setLocaleData();
								this.modulesService.getModuleByName('Tickets').subscribe(
									(response: any) => {
										this.routerNavigationHelper.navigate(response);
										// this.router.navigate([`render/${response.MODULE_ID}`]);
										// this.router.navigate([`/sidebar`]);
									},
									(error: any) => {
										console.log(error);
									}
								);
							}
						}
					},
					(error: any) => {
						console.log(error);
					}
				);
			}
		}

		// Setting language
		if (window.location.hostname === 'localhost') {
			this.subdomain = 'dev1';
			this.fulldomain = 'dev1';
		} else {
			this.subdomain = window.location.hostname.split('.')[0];
			this.fulldomain = window.location.hostname;
		}
		this.companiesService.getLanguage(this.fulldomain).subscribe(
			(language: any) => {
				this.translateService.use(language.LANGUAGE);
			},
			(error: any) => {
				this.translateService.use('en');
			}
		);

		// get open enrollment
		this.companiesService.getEnrollment().subscribe(
			(response: any) => {
				this.enableSignup = response.ENABLE_SIGNUPS;
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);

		this.companiesService.getTheme().subscribe(
			(response: any) => {
				this.header = response.LOGIN_PAGE.HEADER;
				this.loginIcon = response.LOGIN_PAGE.FILE;
			},
			(error: any) => {
				this.errorMessage = error.error.ERROR;
			}
		);

		this.isIE = /msie\s|trident\//i.test(window.navigator.userAgent);

		if (this.isIE) {
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

	private loadDetails(user, token, companyUuid, subdomain) {
		this.cookieService.delete('authentication_token', '/', '.ngdesk.com');
		const expiredDate = new Date();
		expiredDate.setDate(expiredDate.getDate() + 45);
		this.userService.setAuthenticationToken(token);
		this.routerNavigationHelper.setAuthentication(token,expiredDate,subdomain);
		this.userService.setUserDetails(JSON.parse(user)); 
		this.userService.setSubdomain(subdomain);
		
		// if => web app | else => mobile app
		if (typeof window !== "undefined") {
			this.cookieService.delete('authentication_token', '/', window.location.host);
			if (window.location.host === 'dev1.ngdesk.com') {
				this.cookieService.set(
					'authentication_token',
					token,
					expiredDate,
					'/',
					window.location.host,
					true, 
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
		} else {
				this.routerNavigationHelper.setAuthentication(token,expiredDate,subdomain);
		}
		
		this.userService.setCompanyUuid(companyUuid);
		this.websocketService.initialize();
		// this.myStompService.initStomp();
	}

	public getErrorMessageEmail() {
		// const errorMessage =this.getEmailErrorMessage();
		// this.htmlString =`<span><font color="red">`+errorMessage+`</font></span>`;
		if (this.loginForm.controls.emailAddress.errors.required) {
			let message = '';
			const parameters = {
				field: this.translateService.instant('EMAIL_ADDRESS'),
			};
			this.translateService
				.get('FIELD_REQUIRED_NS', parameters)
				.subscribe((res: string) => {
					message += res;
				});
			return message;
		} else if (this.loginForm.controls.emailAddress.errors.email) {
			return this.translateService.instant('EMAIL_MUST_BE_VALID');
		} else {
			return this.translateService.instant('UNKNOWN_ERROR');
		}

		return this.htmlString;
	}

	public getErrorMessagePassword() {
		// const errorMessage =this.getPasswordErrorMessage();
		// this.htmlString =`<span><font color="red">`+errorMessage+`</font></span>`;
		// return this.htmlString;

		let message = '';
		const parameters = {
			field: this.translateService.instant('PASSWORD'),
		};
		this.translateService
			.get('FIELD_REQUIRED_NS', parameters)
			.subscribe((res: string) => {
				message += res;
			});

		return message;
	}

	public getEmailErrorMessage() {
		if (this.loginForm.controls.emailAddress.errors.required) {
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
		} else if (this.loginForm.controls.emailAddress.errors.email) {
			return this.translateService.instant('EMAIL_MUST_BE_VALID');
		} else {
			return this.translateService.instant('UNKNOWN_ERROR');
		}
	}

	public getPasswordErrorMessage() {
		let message = '';
		const parameters = {
			field: this.translateService.instant('PASSWORD'),
		};
		this.translateService
			.get('FIELD_REQUIRED', parameters)
			.subscribe((res: string) => {
				message += res;
			});

		return message;
	}

	public loadUserDetailsForTesting(creds) {
		this.userService.login(creds).subscribe(
			(data: any) => {
				const jwtToken = data.AUTHENTICATION_TOKEN;
				const decoded = jwt_decode(jwtToken);
				this.loadDetails(
					decoded.USER,
					jwtToken,
					decoded.COMPANY_UUID,
					decoded.SUBDOMAIN
				);
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	public login() {
		this.logger.trace('enter LoginComponent.login()');
		if (this.loginForm.valid) {
			this.isSubmitting = true;
			const creds = {
				EMAIL_ADDRESS: this.loginForm.value.emailAddress,
				PASSWORD: this.loginForm.value.password,
				SUBDOMAIN: null,
			};
			this.userService.login(creds).subscribe(
				(data: any) => {
					this.bannerMessageService.errorNotifications = [];
					this.bannerMessageService.successNotifications = [];
					this.cookieService.delete('login_redirect');
					const jwtToken = data.AUTHENTICATION_TOKEN;
					const decoded = jwt_decode(jwtToken);
					this.loadDetails(
						decoded.USER,
						jwtToken,
						decoded.COMPANY_UUID,
						decoded.SUBDOMAIN
					);

					this.cacheService.loadAllData();
					if (
						this.loginRedirectUrl &&
						this.loginRedirectUrl !== '/login' &&
						this.loginRedirectUrl !== '/reset-password' &&
						this.loginRedirectUrl !== '/create-password'
					) {
						this.router.navigateByUrl(this.loginRedirectUrl);
					} else {
						// TODO: figure way to use cache saved modules instead of api call
						this.modulesService.getModuleByName('Tickets').subscribe(
							(response: any) => {
								// TODO: change the route on login
								this.isSubmitting = false;
								this.logger.trace('exit LoginComponent.login()');

								this.routerNavigationHelper.navigate(response);
								// // this.router.navigate([`render/${response.MODULE_ID}`]);
								// this.router.navigate([`/sidebar`]);
							},
							(error: any) => {
								this.isSubmitting = false;
								console.log(error);
							}
						);
					}
					this.companiesService.setLocaleData();
				},
				(error: any) => {
					this.isSubmitting = false;
					// TODO: set a default value for errors coming from the server
					this.errorMessage = error.error.ERROR;
				}
			);
		}
	}

	public loginWithFacebook() {
		window.open(`${this.globals.baseRestUrl}/users/login/facebook`, '_blank');
	}

	public loginWithGoogle() {
		window.open(`${this.globals.baseRestUrl}/users/login/google`, '_blank');
	}

	public loginWithTwitter() {
		window.open(`${this.globals.baseRestUrl}/users/login/twitter`, '_blank');
	}

	public loginWithMicrosoft() {
		window.open(`${this.globals.baseRestUrl}/users/login/microsoft`, '_blank');
	}

	public signup() {
		// redirect to signup
		this.router.navigate(['/signup']);
	}

	public forgotPassword() {
		// redirect to forgot password page
		this.router.navigate(['/forgot-password']);
	}

	public onBackTap() {
		this.routerNavigationHelper.onBack();
	}
}
