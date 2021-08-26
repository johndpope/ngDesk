import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { MicrosoftTeamsApiService } from '@ngdesk/integration-api';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '@src/app/companies/companies.service';
import { CompanySettingService } from '@src/app/company-settings/company-settings.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { DeleteCompanyDialogComponent } from '@src/app/dialogs/delete-company-dialog/delete-company-dialog.component';
import { UsersService } from '@src/app/users/users.service';
import { CookieService } from 'ngx-cookie-service';
import { ModulesService } from '../modules/modules.service';
import { FeatureRolledOutDialogComponent } from '@src/app/dialogs/feature-rolled-out-dialog/feature-rolled-out-dialog.component';

@Component({
	selector: 'app-company-settings',
	templateUrl: './company-settings.component.html',
	styleUrls: ['./company-settings.component.scss'],
})
export class CompanySettingsComponent implements OnInit {
	public deleteCompanyForm: FormGroup;
	public moduleId;
	public searchCtrl = new FormControl();
	public settingLists = [];

	constructor(
		private companiesService: CompaniesService,
		private dialog: MatDialog,
		private router: Router,
		public usersService: UsersService,
		private translateService: TranslateService,
		private cookieService: CookieService,
		private modulesService: ModulesService,
		private fb: FormBuilder,
		private loaderService: LoaderService,
		private companySettingService: CompanySettingService,
		private microsoftTeamsApiService: MicrosoftTeamsApiService,
		
	) { }

	public ngOnInit() {
		this.deleteCompanyForm = this.fb.group({
			TYPED_SUBDOMAIN: [
				'',
				[
					Validators.required,
					Validators.pattern(this.usersService.getSubdomain()),
				],
			],
			DELETION_FEEDBACK: ['', Validators.required],
			DELETION_REASON: ['', Validators.required],
		});

		// Get all the settings as a list.
		this.settingLists = this.companySettingService.getCompanySettings();

		// On type of any character on search control input call search function.
		this.searchCtrl.valueChanges.subscribe((value) => {
			this.settingLists = this.searchCompanySettings(value);
		});
	}

	// TODO: full list of setting add back as needed
	// public companySettings: { ICON: string; NAME: string; PATH: string }[] = [
	//   { ICON: 'security', NAME: 'Security', PATH: 'security' },
	//   { ICON: 'email', NAME: 'Email Templates', PATH: 'page-customization' },
	//   { ICON: 'perm_data_setting', NAME: 'Roles', PATH: 'roles' },
	//   { ICON: 'list', NAME: 'Sidebar', PATH: 'sidebar-customization/master' },
	//   { ICON: 'playlist_add', NAME: 'Email Settings', PATH: 'page-customization' },
	//   { ICON: 'dashboard', NAME: 'Dashboard', PATH: 'page-customization' },
	//   { ICON: 'view_list', NAME: 'Global Picklist', PATH: 'page-customization' },
	//   { ICON: 'color_lens', NAME: 'Customization', PATH: 'page-customization' }
	// ];

	public goToSeletctedSetting(selectedSetting) {
		this.companiesService.trackEvent(
			`Clicked company-setting: ${selectedSetting}`
		);
		if (selectedSetting.includes('render')) {
			const moduleName = selectedSetting.split('/')[1];
			this.modulesService.getAllModules().subscribe(
				(response: any) => {
					const allModules = response.MODULES;
					const module = allModules.find((temp) => temp.NAME === moduleName);
					this.router.navigate([`render/${module.MODULE_ID}`]);
				});
		}
		else if (selectedSetting.search('getting-started') !== -1) {
			this.router.navigate([`${selectedSetting}`]);
		} 
		else{
			this.router.navigate([`company-settings/${selectedSetting}`]);
		}
	}

	// This function will open the confirmation popup on click of Delete Company button.
	public openDialog() {
		const dialogRef = this.dialog.open(DeleteCompanyDialogComponent, {
			width: '550px',
			data: {
				// Passing the deleteCompanyForm to Dialog component.
				deleteCompanyForm: this.deleteCompanyForm,
			},
		});

		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DELETE')) {
				const deleteCompanyObj = this.deleteCompanyForm.value;
				delete deleteCompanyObj.TYPED_SUBDOMAIN;
				this.companiesService.deleteCompany(deleteCompanyObj).subscribe(
					(deleteResponse: any) => {
						this.cookieService.delete('authentication_token', '/', window.location.host);
						window.location.href = 'https://www.ngdesk.com/';
					},
					(error: any) => {
						console.log(error);
					}
				);
			}
			this.loaderService.isLoading = false;
		});
	}

	// dialog to display message
	public featureRolledOutMessageDialog() {
		const dialogRef = this.dialog.open(FeatureRolledOutDialogComponent, {
			width: '500px',
		});
	}

	// search the settings when any text is typed in the search input.
	public searchCompanySettings(searchString) {
		const settingList = this.companySettingService.getCompanySettings();
		const searchedList = [];
		if (searchString != null && searchString !== '') {
			settingList.forEach((companySetting) => {
				const setting = companySetting;
				const filteredOptions = companySetting['options'].filter(
					(option) =>
						this.translateService
							.instant(option['PATH'])
							.toLowerCase()
							.indexOf(searchString.toLowerCase()) >= 0
				);
				if (filteredOptions.length > 0) {
					setting['options'] = filteredOptions;
				}

				if (
					companySetting['header']
						.toLowerCase()
						.indexOf(searchString.toLowerCase()) >= 0 ||
					filteredOptions.length > 0
				) {
					searchedList.push(setting);
				}
			});
			return searchedList;
		} else {
			// If user eraise the search input then the whole list should be returned.
			return this.companySettingService.getCompanySettings();
		}
	}

	// when clicking on clear button in search
	// reset the entered text
	public removeAll() {
		this.searchCtrl.reset();
	}
}
