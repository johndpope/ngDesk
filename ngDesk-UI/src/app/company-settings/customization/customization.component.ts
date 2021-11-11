import { OverlayContainer } from '@angular/cdk/overlay';
import {
	ChangeDetectorRef,
	Component,
	EventEmitter,
	OnInit,
	Output,
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ThemeService } from '../../theme.service';
import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { UsersService } from '../../users/users.service';
import { Title } from '@angular/platform-browser';
import { AppUpdateService } from '@src/app/app-update.service';

@Component({
	selector: 'app-customization',
	templateUrl: './customization.component.html',
	styleUrls: ['./customization.component.scss'],
})
export class CustomizationComponent implements OnInit {
	private themeWrapper = document.querySelector('body');
	public primaryColor = [
		'#3f51b5',
		'#43a047',
		'#f44336',
		'#f90200',
		'#ffea00',
		'#9c27b0',
		'#000000',
	];
	public secondaryColor = [
		'#e8eaf6',
		'#e8f5e9',
		'#ffebee',
		'#fbfaff',
		'#fffde7',
		'#f3e5f5',
		'#cccccc',
	];
	public customizationForm: FormGroup = this.fb.group({
		SIDEBAR: this.fb.group({
			FILENAME: ['', Validators.required],
			FILE: ['', Validators.required],
			HEADER: ['', Validators.required],
		}),
		LOGIN_PAGE: this.fb.group({
			FILENAME: ['', Validators.required],
			FILE: ['', Validators.required],
			HEADER: ['', Validators.required],
		}),
		FAVICON: this.fb.group({
			FILENAME: ['', Validators.required],
			FILE: ['', Validators.required],
			HEADER: ['', Validators.required],
		}),
		SIGNUP_PAGE: this.fb.group({
			FILENAME: ['', Validators.required],
			FILE: ['', Validators.required],
			HEADER: ['', Validators.required],
		}),
		PRIMARY_COLOR: [''],
		SECONDARY_COLOR: [''],
	});
	public successMessage: string;
	public checkGettingStarted;
	public errorMessage: string;
	public fileSizeError = {
		SIDEBAR: true,
		LOGIN_PAGE: true,
		FAVICON: true,
		SIGNUP_PAGE: true,
	};
	public fileTypeError = {
		SIDEBAR: true,
		LOGIN_PAGE: true,
		FAVICON: true,
		SIGNUP_PAGE: true,
	};
	public primaryColorValue;
	public secondaryColorValue;
	@Output() public saveEvent: EventEmitter<any> = new EventEmitter();

	constructor(
		private router: Router,
		private cd: ChangeDetectorRef,
		private companiesService: CompaniesService,
		private fb: FormBuilder,
		private usersService: UsersService,
		private translateService: TranslateService,
		private themeService: ThemeService,
		private bannerMessageService: BannerMessageService,
		private overlayContainer: OverlayContainer,
		private appUpdateService: AppUpdateService,
		private titleService: Title
	) {}
	public ngOnInit() {
		this.checkGettingStarted = this.router.url.search('getting-started');
		this.setApplicationFavicon();
	}

	public setApplicationFavicon() {
		this.companiesService.getTheme().subscribe(
			(response: any) => {
				if (response.FAVICON.FILE) {
					this.appUpdateService.setAppFavicon(response.FAVICON.FILE);
					this.titleService.setTitle(response.FAVICON.HEADER);
				}
				this.customizationForm.setValue({
					SIDEBAR: {
						FILENAME: response.SIDEBAR.FILENAME,
						FILE: response.SIDEBAR.FILE,
						HEADER: response.SIDEBAR.HEADER,
					},
					LOGIN_PAGE: {
						FILENAME: response.LOGIN_PAGE.FILENAME,
						FILE: response.LOGIN_PAGE.FILE,
						HEADER: response.LOGIN_PAGE.HEADER,
					},
					FAVICON: {
						FILENAME: response.FAVICON.FILENAME,
						FILE: response.FAVICON.FILE,
						HEADER: response.FAVICON.HEADER,
					},
					SIGNUP_PAGE: {
						FILENAME: response.SIGNUP_PAGE.FILENAME,
						FILE: response.SIGNUP_PAGE.FILE,
						HEADER: response.SIGNUP_PAGE.HEADER,
					},
					PRIMARY_COLOR: response.PRIMARY_COLOR,
					SECONDARY_COLOR: response.SECONDARY_COLOR,
				});
				this.primaryColorValue = response.PRIMARY_COLOR;
				this.secondaryColorValue = response.SECONDARY_COLOR;
			},
			(error: any) => {
				this.errorMessage = error.ERROR;
			}
		);
	}

	public onFileChange(event, fgName) {
		const reader = new FileReader();
		if (event.target.files && event.target.files.length) {
			const [file] = event.target.files;
			if (
				file.type === 'image/jpeg' ||
				file.type === 'image/png' ||
				file.type === 'image/x-png'
			) {
				this.fileTypeError[fgName] = true;
				file.size <= 1024000
					? (this.fileSizeError[fgName] = true)
					: (this.fileSizeError[fgName] = false);
			} else {
				this.fileTypeError[fgName] = false;
			}
			reader.readAsDataURL(file);
			reader.onload = () => {
				this.customizationForm.patchValue({
					[fgName]: { FILENAME: file.name, FILE: reader.result },
				});
				// need to run CD since file load runs outside of zone
				this.cd.markForCheck();
			};
		}
	}

	public getError(typeError, sizeError) {
		if (typeError) {
			return this.translateService.instant('CHOOSE_PNG_JPEG_FILE');
		} else if (sizeError) {
			return this.translateService.instant('FILE_SIZE_EXCEEDS') + ' 100Kb';
		}
	}

	private validateImages() {
		if (
			Object.values(this.fileSizeError).indexOf(false) === -1 &&
			Object.values(this.fileTypeError).indexOf(false) === -1 &&
			this.customizationForm.valid
		) {
			return true;
		} else {
			return false;
		}
	}

	public save() {
		this.saveEvent.emit();
		const overlayContainerClasses =
			this.overlayContainer.getContainerElement().classList;
		const themeClassesToRemove = Array.from(overlayContainerClasses).filter(
			(item: string) => item.includes('-theme')
		);
		if (themeClassesToRemove.length) {
			overlayContainerClasses.remove(...themeClassesToRemove);
		}
		switch (this.primaryColorValue) {
			case '#3f51b5':
				overlayContainerClasses.add('blue-theme');
				this.setPrimaryColor(this.primaryColorValue, '#060959');
				break;
			case '#43a047':
				overlayContainerClasses.add('green-theme');
				this.setPrimaryColor(this.primaryColorValue, '#2E7D32');
				break;
			case '#f44336':
				overlayContainerClasses.add('red-theme');
				this.setPrimaryColor(this.primaryColorValue, '#C62828');
				break;
			case '#f90200':
				overlayContainerClasses.add('red1-theme');
				this.setPrimaryColor(this.primaryColorValue, '#b71C1C');
				break;
			case '#ffea00':
				overlayContainerClasses.add('yellow-theme');
				this.setPrimaryColor(this.primaryColorValue, '#FFD600');
				break;
			case '#9c27b0':
				overlayContainerClasses.add('purple-theme');
				this.setPrimaryColor(this.primaryColorValue, '#6A1B9A');
				break;
			case '#000000':
				overlayContainerClasses.add('black-theme');
				this.setPrimaryColor(this.primaryColorValue, '#808080');
				break;
			default:
				overlayContainerClasses.add('blue-theme');
				this.setPrimaryColor(this.primaryColorValue, '#060959');
				break;
		}
		switch (this.secondaryColorValue) {
			case '#e8eaf6':
				this.setSecondaryColors(this.secondaryColorValue, '#5c6bc0');
				break;
			case '#e8f5e9':
				this.setSecondaryColors(this.secondaryColorValue, '#66bb6a');
				break;
			case '#ffebee':
				this.setSecondaryColors(this.secondaryColorValue, '#ef9a9a');
				break;
			case '#fffde7':
				this.setSecondaryColors(this.secondaryColorValue, '#fff59d');
				break;
			case '#f3e5f5':
				this.setSecondaryColors(this.secondaryColorValue, '#ab47bc');
				break;
			case '#cccccc':
				this.setSecondaryColors(this.secondaryColorValue, '#c0c0c0');
				break;
			case '#fbfaff':
				this.setSecondaryColors(this.secondaryColorValue, '#f0f0f0');
				break;
			default:
				this.setSecondaryColors(this.secondaryColorValue, '#5c6bc0');
				break;
		}

		this.customizationForm.value.PRIMARY_COLOR = this.primaryColorValue;
		this.customizationForm.value.SECONDARY_COLOR = this.secondaryColorValue;
		// material
		this.themeService.setColorTheme(this.primaryColorValue);

		if (this.validateImages()) {
			this.companiesService
				.putTheme(this.customizationForm.value)
				.subscribe((response: any) => {
					this.companiesService
						.getUsageType(this.usersService.getSubdomain())
						.subscribe(
							(usageType: any) => {
								if (
									(usageType.USAGE_TYPE.TICKETS &&
										!usageType.USAGE_TYPE.CHAT) ||
									usageType.USAGE_TYPE.PAGER
								) {
									this.bannerMessageService.successNotifications.push({
										message:
											this.translateService.instant('SAVED_SUCCESSFULLY'),
									});
									this.setApplicationFavicon();
									this.companiesService
										.getAllGettingStarted()
										.subscribe((getAll: any) => {
											if (!getAll.GETTING_STARTED[0].COMPLETED) {
												this.companiesService
													.putGettingStarted(getAll.GETTING_STARTED[0])
													.subscribe(
														(put: any) => {},
														(errorResponse: any) => {
															console.log(errorResponse);
														}
													);
											}
										});
								} else {
									this.bannerMessageService.successNotifications.push({
										message:
											this.translateService.instant('SAVED_SUCCESSFULLY'),
									});
								}
							},
							(error: any) => {
								this.errorMessage = error.error.ERROR;
							}
						);
				});
		}
	}
	public setPrimaryColor(primaryColor, blendColor) {
		this.themeWrapper.style.setProperty('--primaryColor', primaryColor);
		this.themeWrapper.style.setProperty('--blendColor', blendColor);
	}
	public setSecondaryColors(secondaryColor, hoverColor) {
		this.themeWrapper.style.setProperty('--secondaryColor', secondaryColor);
		this.themeWrapper.style.setProperty('--hoverColor', hoverColor);
	}
	// Function to set the theme for application
	public setColor(type, value) {
		if (type === 'primary') {
			this.primaryColorValue = value;
		} else if (type === 'secondary') {
			this.secondaryColorValue = value;
		}
		this.customizationForm.value.PRIMARY_COLOR = this.primaryColorValue;
		this.customizationForm.value.SECONDARY_COLOR = this.secondaryColorValue;
		switch (this.primaryColorValue) {
			case '#3f51b5':
				this.setPrimaryColor(this.primaryColorValue, '#060959');
				break;
			case '#43a047':
				this.setPrimaryColor(this.primaryColorValue, '#2E7D32');
				break;
			case '#f44336':
				this.setPrimaryColor(this.primaryColorValue, '#C62828');
				break;
			case '#f90200':
				this.setPrimaryColor(this.primaryColorValue, '#b71C1C');
				break;
			case '#ffea00':
				this.setPrimaryColor(this.primaryColorValue, '#FFD600');
				break;
			case '#9c27b0':
				this.setPrimaryColor(this.primaryColorValue, '#6A1B9A');
				break;
			case '#000000':
				this.setPrimaryColor(this.primaryColorValue, '#808080');
				break;
			default:
				this.setPrimaryColor(this.primaryColorValue, '#060959');
				break;
		}
		this.themeService.setColorTheme(this.primaryColorValue);
		switch (this.secondaryColorValue) {
			case '#e8eaf6':
				this.setSecondaryColors(this.secondaryColorValue, '#5c6bc0');
				break;
			case '#e8f5e9':
				this.setSecondaryColors(this.secondaryColorValue, '#66bb6a');
				break;
			case '#ffebee':
				this.setSecondaryColors(this.secondaryColorValue, '#ef9a9a');
				break;
			case '#fffde7':
				this.setSecondaryColors(this.secondaryColorValue, '#fff59d');
				break;
			case '#f3e5f5':
				this.setSecondaryColors(this.secondaryColorValue, '#ab47bc');
				break;
			case '#cccccc':
				this.setSecondaryColors(this.secondaryColorValue, '#c0c0c0');
				break;
			case '#fbfaff':
				this.setSecondaryColors(this.secondaryColorValue, '#f0f0f0');
				break;
			default:
				this.setSecondaryColors(this.secondaryColorValue, '#5c6bc0');
				break;
		}
	}
}
