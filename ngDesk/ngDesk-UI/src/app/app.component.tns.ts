
import { Component, Inject, OnInit, OnDestroy,AfterViewInit ,ViewChild, ChangeDetectorRef, NgZone } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import * as jwt_decode from 'jwt-decode';
import { CookieService } from 'ngx-cookie-service';
import { Observable, Subscription } from 'rxjs';
import { CompaniesService } from './companies/companies.service';
import { UsersService } from './users/users.service';
import { firebase } from '@nativescript/firebase'
// import * as firebase from 'nativescript-plugin-firebase';
import { AppGlobals } from './app.globals.tns';
import { ApplicationSettings } from './ns-local-storage/app-settings-helper';
import { setBoolean, setString, getString } from '@nativescript/core/application-settings';


// import { TNSFontIconService } from 'nativescript-ngx-fonticon';
@Component({
	selector: 'app-root',
	templateUrl: './app.component.tns.html',
	styleUrls: ['./app.component.scss'],
	providers:[ApplicationSettings]
})
export class AppComponent implements OnInit {

	  
	// public themeColorObserver: Observable<string>;
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
		'zh'
	];
	// private fulldomain: string;
	// private themeWrapper = document.querySelector('body');
	// private browserName = this.getBrowserVersion().browser;
	// private browserVersion = parseFloat(this.getBrowserVersion().version);
	// public screenHeight = window.screen.height + 'px';
	// public screenWidth = window.screen.width + 'px';
	// private os = this.getOS();
	private isAuthenticated = false;
  	private navigationSubscription: Subscription;
	public message:any={};
 	// public aDelegate: StompClientCallback ;
	// public stompClient = new StompClient(this.aDelegate);
	
	constructor(
		// overlayContainer: OverlayContainer,
		// @Inject(DOCUMENT) private _document: HTMLDocument,
		private router: Router,
		private companiesService: CompaniesService,
		private cookieService: CookieService,
		// private dialog: MatDialog,
		private userService: UsersService,
		private appGlobals: AppGlobals,
		// public fonticonService:TNSFontIconService
		private translateService: TranslateService,
		private applicationSettingsHelper:ApplicationSettings
	) {
		// add languages
		translateService.addLangs(this.languages);
		// this.themeService.getThemeColor().subscribe(
		// 	(theme: any) => {
		// 		const primaryColor = theme.PRIMARY_COLOR;
		// 		// tslint:disable-next-line: prefer-switch
		// 		if (primaryColor === '#3f51b5') {
		// 			overlayContainer.getContainerElement().classList.add('blue-theme');
		// 		} else if (primaryColor === '#43a047') {
		// 			overlayContainer.getContainerElement().classList.add('green-theme');
		// 			// tslint:disable-next-line: prefer-switch
		// 		} else if (primaryColor === '#f44336') {
		// 			overlayContainer.getContainerElement().classList.add('red-theme');
		// 		} else if (primaryColor === '#f90200') {
		// 			overlayContainer.getContainerElement().classList.add('red1-theme');
		// 		} else if (primaryColor === '#ffea00') {
		// 			overlayContainer.getContainerElement().classList.add('yellow-theme');
		// 		} else if (primaryColor === '#9c27b0') {
		// 			overlayContainer.getContainerElement().classList.add('purple-theme');
		// 		} else if (primaryColor === '#000000') {
		// 			overlayContainer.getContainerElement().classList.add('black-theme');
		// 		}
		// 	},
		// 	(error: any) => {
		// 		overlayContainer.getContainerElement().classList.add('blue-theme');
		// 	}
		// );
		// overlayContainer.getContainerElement().classList.add('blue-theme');
		// // set default language if no language is found
		translateService.setDefaultLang('en');
		// // Send google analytics page requested
	}

	public ngOnInit() {
		setBoolean("onMessageCallBack",false);
		firebase.init({
			showNotifications: true,
			showNotificationsWhenInForeground: true,

			onPushTokenReceivedCallback: (token) => {

				setString("onPushToken",token);
			},
	  
			onMessageReceivedCallback: (message: firebase.Message) => {
				setBoolean("onMessageCallBack",true);
				setString("dataId",message.data.dataId);
				setString("moduleId",message.data.moduleId);
				setBoolean("foreground",message.foreground);
				this.message=message;
			}
		  })
			.then(() => {
			  console.log('[Firebase] Initialized');
			})
			.catch(error => {
			  console.log('[Firebase] Initialize', { error });
			});

			
			

		
		// const userEvents = {
		// 	OS: this.os,
		// 	BROWSER: this.browserName,
		// 	BROWSER_VERSION: this.browserVersion,
		// 	SCREEN_HEIGHT: this.screenHeight,
		// 	SCREEN_WIDTH: this.screenWidth,
		// 	DEVICE_ID: null
		// };
		// this.companiesService.eventMetaData = userEvents;

		// this.themeColorObserver = this.themeService.primaryColor;
		// this.themeService.getThemeColor().subscribe(
		// 	(theme: any) => {
		// 		const primaryColor = theme.PRIMARY_COLOR;
		// 		this.themeService.setColorTheme(primaryColor);
		// 		const secondaryColor = theme.SECONDARY_COLOR;
		// 		this.setPrimaryColor(primaryColor);
		// 		// tslint:disable-next-line: prefer-switch
		// 		if (secondaryColor === '#e8eaf6') {
		// 			this.setSecondaryColors(secondaryColor, '#5c6bc0');
		// 		} else if (secondaryColor === '#e8f5e9') {
		// 			this.setSecondaryColors(secondaryColor, '#66bb6a');
		// 			// tslint:disable-next-line: prefer-switch
		// 		} else if (secondaryColor === '#ffebee') {
		// 			this.setSecondaryColors(secondaryColor, '#ef9a9a');
		// 		} else if (secondaryColor === '#fbfaff') {
		// 			this.setSecondaryColors(secondaryColor, '#ef9a9a');
		// 		} else if (secondaryColor === '#fffde7') {
		// 			this.setSecondaryColors(secondaryColor, '#fff59d');
		// 		} else if (secondaryColor === '#f3e5f5') {
		// 			this.setSecondaryColors(secondaryColor, '#ab47bc');
		// 		} else if (secondaryColor === '#cccccc') {
		// 			this.setSecondaryColors(secondaryColor, '#c0c0c0');
		// 		}
		// 	},
		// 	(error: any) => {
		// 		this.themeService.setColorTheme('#3f51b5');
		// 		this.setPrimaryColor('#3f51b5');
		// 		this.setSecondaryColors('#e8eaf6', '#5c6bc0');
		// 	}
		// );

		console.log(this.appGlobals.websocketUrl+"subdomain");
		
		console.log("get Auth"+this.cookieService.get('authentication_token'));
		if (this.cookieService.get('authentication_token') !== '') {
			this.userService.validate().subscribe(
				(data: any) => {
					this.isAuthenticated = true;
					const jwtToken = data.AUTHENTICATION_TOKEN;
					const decoded = jwt_decode(jwtToken);
					this.userService.setUserDetails(JSON.parse(decoded.USER));
					this.userService.setCompanyUuid(decoded.COMPANY_UUID);
					this.userService.setSubdomain(decoded.SUBDOMAIN);

				
					// if (window.location.hostname === 'localhost') {
					// 	this.fulldomain = 'dev1';
					// } else {
					// 	this.fulldomain = window.location.hostname;
					// }
					// // loading saved language
					this.companiesService.getLanguage(getString("subdomain")).subscribe(
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
				},
				(error: any) => {
					this.cookieService.set('authentication_token', '');
					this.isAuthenticated = false;
				}
			);
		} else {
			this.isAuthenticated = false;
		}
		// setting logos and titles
		// this.companiesService.getTheme().subscribe(
		// 	(response: any) => {
		// 		this._document
		// 			.getElementById('app-favicon')
		// 			.setAttribute('href', response.FAVICON.FILE);
		// 		getDOM().setTitle(this._document, response.FAVICON.HEADER);
		// 		this.userService.sidebarTitle = response.SIDEBAR.HEADER;
		// 		this.userService.sidebarLogo = response.SIDEBAR.FILE;
		// 	},
		// 	(error: any) => {
		// 		console.log(error);
		// 	}
		// );
	}
 
	// private getOS() {
	// 	const userAgent = window.navigator.userAgent;
	// 	const platform = window.navigator.platform;
	// 	const macosPlatforms = ['Macintosh', 'MacIntel', 'MacPPC', 'Mac68K'];
	// 	const windowsPlatforms = ['Win32', 'Win64', 'Windows', 'WinCE'];
	// 	const iosPlatforms = ['iPhone', 'iPad', 'iPod'];
	// 	let os = null;

	// 	if (macosPlatforms.indexOf(platform) !== -1) {
	// 		os = 'Mac OS';
	// 	} else if (iosPlatforms.indexOf(platform) !== -1) {
	// 		os = 'iOS';
	// 	} else if (windowsPlatforms.indexOf(platform) !== -1) {
	// 		os = 'Windows';
	// 	} else if (/Android/.test(userAgent)) {
	// 		os = 'Android';
	// 	} else if (!os && /Linux/.test(platform)) {
	// 		os = 'Linux';
	// 	}
	// 	return os;
	// }

	// private getBrowserVersion() {
	// 	const nAgt = navigator.userAgent;
	// 	let browser = navigator.appName;
	// 	let version = '' + parseFloat(navigator.appVersion);
	// 	let verOffset;

	// 	// Opera
	// 	if ((verOffset = nAgt.indexOf('Opera')) !== -1) {
	// 		browser = 'Opera';
	// 		version = nAgt.substring(verOffset + 6);
	// 		if ((verOffset = nAgt.indexOf('Version')) !== -1) {
	// 			version = nAgt.substring(verOffset + 8);
	// 		}
	// 	} else if ((verOffset = nAgt.indexOf('Chrome')) !== -1) {
	// 		// Chrome
	// 		browser = 'Chrome';
	// 		version = nAgt.substring(verOffset + 7);
	// 	} else if ((verOffset = nAgt.indexOf('Safari')) !== -1) {
	// 		// Safari
	// 		browser = 'Safari';
	// 		version = nAgt.substring(verOffset + 7);
	// 		if ((verOffset = nAgt.indexOf('Version')) !== -1) {
	// 			version = nAgt.substring(verOffset + 8);
	// 		}

	// 		if (nAgt.indexOf('CriOS') !== -1) {
	// 			browser = 'Chrome';
	// 		}
	// 	} else if ((verOffset = nAgt.indexOf('Firefox')) !== -1) {
	// 		// Firefox
	// 		browser = 'Firefox';
	// 		version = nAgt.substring(verOffset + 8);
	// 	} else if (
	// 		navigator.userAgent.indexOf('MSIE') !== -1 ||
	// 		!!document.DOCUMENT_NODE === true
	// 	) {
	// 		// IF IE > 10
	// 		browser = 'Microsoft Internet Explorer';
	// 		version = nAgt.substring(verOffset + 5);
	// 	} else {
	// 		browser = 'unknown';
	// 		version = nAgt.substring(verOffset + 1);
	// 		if (browser.toLowerCase() === browser.toUpperCase()) {
	// 			browser = navigator.appName;
	// 		}
	// 	}
	// 	return { browser, version };
	// }

	// public setSecondaryColors(secondaryColor, hoverColor) {
	// 	this.themeWrapper.style.setProperty('--secondaryColor', secondaryColor);
	// 	this.themeWrapper.style.setProperty('--hoverColor', hoverColor);
	// }
	// public setPrimaryColor(primaryColor) {
	// 	this.themeWrapper.style.setProperty('--primaryColor', primaryColor);
	// }

	public setAuthStatus() {
		if (this.cookieService.get('authentication_token') !== '') {
			this.isAuthenticated = true;
		} else {
			this.isAuthenticated = false;
		}
  }

//   public  base64ToHex(str) {
// 	const raw = this.decodeBase64(str);
// 	let result = '';
// 	for (let i = 0; i < raw.length; i++) {
// 	  const hex = raw.charCodeAt(i).toString(16);
// 	  result += (hex.length === 2 ? hex : '0' + hex);
// 	}
// 	return result.toUpperCase();
//   }

//   public decodeBase64(s) {
//     var e={},i,b=0,c,x,l=0,a,r='',w=String.fromCharCode,L=s.length;
//     var A="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
//     for(i=0;i<64;i++){e[A.charAt(i)]=i;}
//     for(x=0;x<L;x++){
//         c=e[s.charAt(x)];b=(b<<6)+c;l+=6;
//         while(l>=8){((a=(b>>>(l-=8))&0xff)||(x<(L-2)))&&(r+=w(a));}
//     }
//     return r;
// };
  
}
