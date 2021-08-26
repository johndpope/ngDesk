import { OverlayContainer } from '@angular/cdk/overlay';
import { DOCUMENT } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { NavigationEnd, Router, ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import * as jwt_decode from 'jwt-decode';
import { CookieService } from 'ngx-cookie-service';
import { Observable, Subscription } from 'rxjs';
import { AppUpdateService } from './app-update.service';
import { CompaniesService } from './companies/companies.service';
import { RolesService } from './company-settings/roles/roles-old.service';
import { MessagingService } from './firebase/messaging.service';
import { ThemeService } from './theme.service';
import { UsersService } from './users/users.service';
import { WebsocketService } from './websocket.service';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
	public themeColorObserver: Observable<string>;
	private languages = [
		'ar',
		'de',
		'el',
		'en',
		'es',
		'fr',
		'hi',
		'it',
		'ms',
		'no',
		'pt',
		'ru',
		'zh',
	];
	private fulldomain: string;
	private themeWrapper = document.querySelector('body');
	private browserName = this.getBrowserVersion().browser;
	private browserVersion = parseFloat(this.getBrowserVersion().version);
	public screenHeight = window.screen.height + 'px';
	public screenWidth = window.screen.width + 'px';
	private os = this.getOS();
	public isAuthenticated = false;
	private navigationSubscription: Subscription;
	public showSidebar = true;
	public isConfirmationPage = false;

	constructor(
		overlayContainer: OverlayContainer,
		@Inject(DOCUMENT) private _document: HTMLDocument,
		private router: Router,
		private route: ActivatedRoute,
		private companiesService: CompaniesService,
		private cookieService: CookieService,
		private userService: UsersService,
		private translateService: TranslateService,
		private themeService: ThemeService,
		private rolesService: RolesService,
		private messagingService: MessagingService,
		private translate: TranslateService,
		private cacheService: CacheService,
		private websocketService: WebsocketService,
		private appUpdateService: AppUpdateService,
		public bannerMessageService: BannerMessageService,
		private titleService: Title
	) {
		appUpdateService.initApplicationUpdating();

		// add languages
		translateService.addLangs(this.languages);
		this.themeService.getThemeColor().subscribe(
			(theme: any) => {
				const primaryColor = theme.PRIMARY_COLOR;
				// tslint:disable-next-line: prefer-switch
				if (primaryColor === '#3f51b5') {
					overlayContainer.getContainerElement().classList.add('blue-theme');
				} else if (primaryColor === '#43a047') {
					overlayContainer.getContainerElement().classList.add('green-theme');
					// tslint:disable-next-line: prefer-switch
				} else if (primaryColor === '#f44336') {
					overlayContainer.getContainerElement().classList.add('red-theme');
				} else if (primaryColor === '#f90200') {
					overlayContainer.getContainerElement().classList.add('red1-theme');
				} else if (primaryColor === '#ffea00') {
					overlayContainer.getContainerElement().classList.add('yellow-theme');
				} else if (primaryColor === '#9c27b0') {
					overlayContainer.getContainerElement().classList.add('purple-theme');
				} else if (primaryColor === '#000000') {
					overlayContainer.getContainerElement().classList.add('black-theme');
				}
			},
			(error: any) => {
				overlayContainer.getContainerElement().classList.add('blue-theme');
			}
		);
		overlayContainer.getContainerElement().classList.add('blue-theme');
		// set default language if no language is found
		translateService.setDefaultLang('en');
		// Send google analytics page requested
		this.navigationSubscription = this.router.events.subscribe((event) => {
			if (event instanceof NavigationEnd) {
				(<any>window).ga('set', 'page', event.urlAfterRedirects);
				(<any>window).ga('send', 'pageview');
				this.setAuthStatus();
			}
		});

		translate.setDefaultLang('en');
		translate.use('en');
	}

	public ngOnInit() {
		if (window.location.href.includes('email/verify')) {
			this.isConfirmationPage = true;
		}
		this.companiesService.getTheme().subscribe(
			(response: any) => {
				if(response.FAVICON.FILE) {
					this.appUpdateService.setAppFavicon(response.FAVICON.FILE);
					this.titleService.setTitle(response.FAVICON.HEADER);
				}
			});
		this.cookieService.set('subdomain', '');
		const userEvents = {
			OS: this.os,
			BROWSER: this.browserName,
			BROWSER_VERSION: this.browserVersion,
			SCREEN_HEIGHT: this.screenHeight,
			SCREEN_WIDTH: this.screenWidth,
			DEVICE_ID: null,
		};
		this.companiesService.eventMetaData = userEvents;

		this.themeColorObserver = this.themeService.primaryColor;
		this.themeService.getThemeColor().subscribe(
			(theme: any) => {
				const primaryColor = theme.PRIMARY_COLOR;
				this.themeService.setColorTheme(primaryColor);
				const secondaryColor = theme.SECONDARY_COLOR;
				switch (primaryColor) {
					case '#3f51b5':
						this.setPrimaryColor(primaryColor, '#060959');
						break;
					case '#43a047':
						this.setPrimaryColor(primaryColor, '#2E7D32');
						break;
					case '#f44336':
						this.setPrimaryColor(primaryColor, '#C62828');
						break;
					case '#f90200':
						this.setPrimaryColor(primaryColor, '#b71C1C');
						break;
					case '#ffea00':
						this.setPrimaryColor(primaryColor, '#FFD600');
						break;
					case '#9c27b0':
						this.setPrimaryColor(primaryColor, '#6A1B9A');
						break;
					case '#000000':
						this.setPrimaryColor(primaryColor, '#808080');
						break;
					default:
						this.setPrimaryColor(primaryColor, '#060959');
						break;
				}
				switch (secondaryColor) {
					case '#e8eaf6':
						this.setSecondaryColors(secondaryColor, '#5c6bc0');
						break;
					case '#e8f5e9':
						this.setSecondaryColors(secondaryColor, '#66bb6a');
						break;
					case '#ffebee':
						this.setSecondaryColors(secondaryColor, '#ef9a9a');
						break;
					case '#fffde7':
						this.setSecondaryColors(secondaryColor, '#fff59d');
						break;
					case '#f3e5f5':
						this.setSecondaryColors(secondaryColor, '#ab47bc');
						break;
					case '#cccccc':
						this.setSecondaryColors(secondaryColor, '#c0c0c0');
						break;
					case '#fbfaff':
						this.setSecondaryColors(secondaryColor, '#f0f0f0');
						break;
					default:
						this.setSecondaryColors(secondaryColor, '#5c6bc0');
						break;
				}
			},
			(error: any) => {
				this.themeService.setColorTheme('#3f51b5');
				this.setPrimaryColor('#3f51b5', '#060959');
				this.setSecondaryColors('#e8eaf6', '#5c6bc0');
			}
		);
		if (
			this.cookieService.get('authentication_token') !== undefined &&
			this.cookieService.get('authentication_token') !== 'undefined' &&
			this.cookieService.get('authentication_token') !== ''
		) {
			const authToken = this.cookieService.get('authentication_token');
			const decode = jwt_decode(authToken);
			this.userService.setUserDetails(JSON.parse(decode.USER));
			this.userService.setCompanyUuid(decode.COMPANY_UUID);
			this.userService.setSubdomain(decode.SUBDOMAIN);
			this.userService.validate().subscribe(
				(data: any) => {
					this.isAuthenticated = true;
					const jwtToken = data.AUTHENTICATION_TOKEN;
					const decoded = jwt_decode(jwtToken);
					this.userService.setUserDetails(JSON.parse(decoded.USER));
					this.userService.setCompanyUuid(decoded.COMPANY_UUID);
					this.userService.setSubdomain(decoded.SUBDOMAIN);
					this.websocketService.initialize();
					// this.myStompService.initStomp();
					this.cacheService.loadAllData();
					if (window.location.hostname === 'localhost') {
						this.fulldomain = 'dev1';
					} else {
						this.fulldomain = window.location.hostname;
					}
					// loading saved language
					this.companiesService.getLanguage(this.fulldomain).subscribe(
						(language: any) => {
							if (this.languages.indexOf(language.LANGUAGE) !== -1) {
								this.translateService.use(language.LANGUAGE);
							} else {
								this.translateService.use('en');
							}
						},
						(error: any) => {
							this.translateService.use('en');
						}
					);
					this.showSidebar = this.isComponentAuthenticated();
				},
				(error: any) => {
					this.cookieService.delete(
						'authentication_token',
						'/',
						window.location.host
					);
					this.isAuthenticated = false;
				}
			);
		} else {
			this.isAuthenticated = false;
			const expiredDate = new Date();
			expiredDate.setDate(expiredDate.getDate() + 45);
			const currentUrl = window.location.pathname;
			this.cookieService.set(
				'login_redirect',
				currentUrl,
				expiredDate,
				'/',
				'.ngdesk.com',
				true,
				'None'
			);
			if (window.location.href.includes('authentication_token')) {
				const url = window.location.href;
				const queryParams = url.split('?')[1].split('&');
				const authTokenParam = queryParams.find((param) =>
					param.includes('authentication_token')
				);
				const authToken = authTokenParam.split('=')[1];
				this.cookieService.delete(
					'authentication_token',
					'/',
					window.location.host
				);
				if (window.location.host === 'dev1.ngdesk.com') {
					this.cookieService.set(
						'authentication_token',
						authToken,
						expiredDate,
						'/',
						window.location.host,
						false,
						'Lax'
					);
				} else {
					this.cookieService.set(
						'authentication_token',
						authToken,
						expiredDate,
						'/',
						window.location.host,
						true,
						'None'
					);
				}
				this.userService.setAuthenticationToken(authToken);
				this.router.navigate(['/login']);
			}
		}
		// setting logos and titles
		this.companiesService.getTheme().subscribe(
			(response: any) => {
				// this._document
				// 	.getElementById('app-favicon')
				// 	.setAttribute('href', response.FAVICON.FILE);
				// getDOM().setTitle(this._document, response.FAVICON.HEADER);
				this.userService.sidebarTitle = response.SIDEBAR.HEADER;
				this.userService.sidebarLogo = response.SIDEBAR.FILE;
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	private getOS() {
		const userAgent = window.navigator.userAgent;
		const platform = window.navigator.platform;
		const macosPlatforms = ['Macintosh', 'MacIntel', 'MacPPC', 'Mac68K'];
		const windowsPlatforms = ['Win32', 'Win64', 'Windows', 'WinCE'];
		const iosPlatforms = ['iPhone', 'iPad', 'iPod'];
		let os = null;

		if (macosPlatforms.indexOf(platform) !== -1) {
			os = 'Mac OS';
		} else if (iosPlatforms.indexOf(platform) !== -1) {
			os = 'iOS';
		} else if (windowsPlatforms.indexOf(platform) !== -1) {
			os = 'Windows';
		} else if (/Android/.test(userAgent)) {
			os = 'Android';
		} else if (!os && /Linux/.test(platform)) {
			os = 'Linux';
		}
		return os;
	}

	private getBrowserVersion() {
		const nAgt = navigator.userAgent;
		let browser = navigator.appName;
		let version = '' + parseFloat(navigator.appVersion);
		let verOffset;

		// Opera
		if ((verOffset = nAgt.indexOf('Opera')) !== -1) {
			browser = 'Opera';
			version = nAgt.substring(verOffset + 6);
			if ((verOffset = nAgt.indexOf('Version')) !== -1) {
				version = nAgt.substring(verOffset + 8);
			}
		} else if ((verOffset = nAgt.indexOf('Chrome')) !== -1) {
			// Chrome
			browser = 'Chrome';
			version = nAgt.substring(verOffset + 7);
		} else if ((verOffset = nAgt.indexOf('Safari')) !== -1) {
			// Safari
			browser = 'Safari';
			version = nAgt.substring(verOffset + 7);
			if ((verOffset = nAgt.indexOf('Version')) !== -1) {
				version = nAgt.substring(verOffset + 8);
			}

			if (nAgt.indexOf('CriOS') !== -1) {
				browser = 'Chrome';
			}
		} else if ((verOffset = nAgt.indexOf('Firefox')) !== -1) {
			// Firefox
			browser = 'Firefox';
			version = nAgt.substring(verOffset + 8);
		} else if (
			navigator.userAgent.indexOf('MSIE') !== -1 ||
			!!document.DOCUMENT_NODE === true
		) {
			// IF IE > 10
			browser = 'Microsoft Internet Explorer';
			version = nAgt.substring(verOffset + 5);
		} else {
			browser = 'unknown';
			version = nAgt.substring(verOffset + 1);
			if (browser.toLowerCase() === browser.toUpperCase()) {
				browser = navigator.appName;
			}
		}
		return { browser, version };
	}

	public setSecondaryColors(secondaryColor, hoverColor) {
		this.themeWrapper.style.setProperty('--secondaryColor', secondaryColor);
		this.themeWrapper.style.setProperty('--hoverColor', hoverColor);
	}
	public setPrimaryColor(primaryColor, blendColor) {
		this.themeWrapper.style.setProperty('--primaryColor', primaryColor);
		this.themeWrapper.style.setProperty('--blendColor', blendColor);
	}

	public setAuthStatus() {
		if (this.cookieService.get('authentication_token') !== '') {
			this.isAuthenticated = true;
		} else {
			this.isAuthenticated = false;
		}
		this.showSidebar = this.isComponentAuthenticated();
	}

	private isComponentAuthenticated(): boolean {
		let isValid = true;
		const regex = 'signup$';

		if (
			this.router.url.indexOf('unsubscribe-to-marketing-email') !== -1 ||
			this.router.url.indexOf('login') !== -1 ||
			this.router.url.indexOf('login-support') !== -1 ||
			this.router.url.indexOf('reset-password') !== -1 ||
			this.router.url.indexOf('create-password') !== -1 ||
			this.router.url.indexOf('forgot-password') !== -1 ||
			this.router.url.indexOf('document-viewer') !== -1 ||
			this.router.url.indexOf(regex) !== -1
		) {
			isValid = false;
		}
		return isValid;
	}
}
