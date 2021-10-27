import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '../../companies/companies.service';
import { ModuleApiService } from '@ngdesk/module-api';
import { ModulesService } from '../modules.service';

@Component({
	selector: 'app-modules-detail',
	templateUrl: './modules-detail.component.html',
	styleUrls: ['./modules-detail.component.scss'],
})
export class ModulesDetailComponent implements OnInit {
	public moduleForm: FormGroup;
	public moduleId: string;
	public moduleName: string;
	public module: any;
	public successMessage: string;
	public errorMessage: string;
	public value: boolean;
	public showSideNav = true;
	public navigations: any = [];
	public sidebarTitle: any;
	public usersService: any;
	public sidebarLogo: any;
	public sidebarForAllRoles: any;
	public navigationList1: any = [];
	public navigationList2: any = [];
	public navigationList3: any = [];
	constructor(
		private companiesService: CompaniesService,
		private router: Router,
		private route: ActivatedRoute,
		private _formBuilder: FormBuilder,
		private translateService: TranslateService,
		private moduleApiService: ModuleApiService,
		private modulesService: ModulesService
	) {}

	public ngOnInit() {
		this.route.params.subscribe((params) => {
			this.moduleId = this.route.snapshot.params['moduleId'];
			if (this.moduleId !== 'new') {
				this.modulesService.getModuleById(this.moduleId).subscribe(
					(response: any) => {
						this.module = response;
						this.moduleName = response.NAME;
						if (response.NAME === 'Tickets') {
							this.navigations = [
								{
									NAME: 'MODULE_DETAIL',
									PATH: ['', 'modules', this.moduleId],
									SRC: '',
									DESCRIPTION: '',
								},
								{
									NAME: 'FIELDS',
									PATH: ['', 'modules', this.moduleId, 'fields'],
									SRC: 'ballot',
									DESCRIPTION: 'CUSTOM_FIELDS',
								},
								{
									NAME: 'LAYOUTS',
									PATH: ['', 'modules', this.moduleId, 'layouts'],
									SRC: 'view_quilt',
									DESCRIPTION: 'MANAGE_DATA',
								},
								{
									NAME: 'VALIDATIONS',
									PATH: ['', 'modules', this.moduleId, 'validations'],
									SRC: 'verified_user',
									DESCRIPTION: 'VALIDATE_DATA',
								},
								{
									NAME: 'WORKFLOWS',
									PATH: ['', 'modules', this.moduleId, 'workflows'],
									SRC: 'touch_app',
									DESCRIPTION: 'AUTOMATE_WORKFLOW',
								},
								{
									NAME: 'SLAS',
									PATH: ['', 'modules', this.moduleId, 'slas'],
									SRC: 'collections_bookmark',
									DESCRIPTION: 'MANAGE_SLAS',
								},
								{
									NAME: 'CHANNELS',
									PATH: ['', 'modules', this.moduleId, 'channels'],
									SRC: 'email',
									DESCRIPTION: 'MODES_TO_REACH_SUPPORT_TEAM',
								},
								{
									NAME: 'FORMS',
									PATH: ['', 'modules', this.moduleId, 'forms'],
									SRC: 'description',
									DESCRIPTION: 'COLLECT_INFORMATION',
								},
								{
									NAME: 'PDFs',
									PATH: ['', 'modules', this.moduleId, 'pdf'],
									SRC: 'picture_as_pdf',
									DESCRIPTION: 'GENERATE_PDF',
								},
								{
									NAME: 'TASK',
									PATH: ['', 'modules', this.moduleId, 'task'],
									SRC: 'list_alt',
									DESCRIPTION: 'MANAGE_TASK',
								},
							];
						} else if (response.NAME === 'Chats') {
							this.navigations = [
								{
									NAME: 'MODULE_DETAIL',
									PATH: ['', 'modules', this.moduleId],
									SRC: '',
									DESCRIPTION: '',
								},
								{
									NAME: 'FIELDS',
									PATH: ['', 'modules', this.moduleId, 'fields'],
									SRC: 'ballot',
									DESCRIPTION: 'CUSTOM_FIELDS',
								},
								{
									NAME: 'LAYOUTS',
									PATH: ['', 'modules', this.moduleId, 'layouts'],
									SRC: 'view_quilt',
									DESCRIPTION: 'MANAGE_DATA',
								},
								{
									NAME: 'WORKFLOWS',
									PATH: ['', 'modules', this.moduleId, 'workflows'],
									SRC: 'touch_app',
									DESCRIPTION: 'AUTOMATE_WORKFLOW',
								},
								{
									NAME: 'CHANNELS',
									PATH: ['', 'modules', this.moduleId, 'channels'],
									SRC: 'email',
									DESCRIPTION: 'MODES_TO_REACH_SUPPORT_TEAM',
								},
							];
						} else {
							this.navigations = [
								{
									NAME: 'MODULE_DETAIL',
									PATH: ['', 'modules', this.moduleId],
									SRC: '',
									DESCRIPTION: '',
								},
								{
									NAME: 'FIELDS',
									PATH: ['', 'modules', this.moduleId, 'fields'],
									SRC: 'ballot',
									DESCRIPTION: 'CUSTOM_FIELDS',
								},
								{
									NAME: 'LAYOUTS',
									PATH: ['', 'modules', this.moduleId, 'layouts'],
									SRC: 'view_quilt',
									DESCRIPTION: 'MANAGE_DATA',
								},
								{
									NAME: 'VALIDATIONS',
									PATH: ['', 'modules', this.moduleId, 'validations'],
									SRC: 'verified_user',
									DESCRIPTION: 'VALIDATE_DATA',
								},
								{
									NAME: 'WORKFLOWS',
									PATH: ['', 'modules', this.moduleId, 'workflows'],
									SRC: 'touch_app',
									DESCRIPTION: 'AUTOMATE_WORKFLOW',
								},
								{
									NAME: 'SLAS',
									PATH: ['', 'modules', this.moduleId, 'slas'],
									SRC: 'collections_bookmark',
									DESCRIPTION: 'MANAGE_SLAS',
								},
								{
									NAME: 'FORMS',
									PATH: ['', 'modules', this.moduleId, 'forms'],
									SRC: 'description',
									DESCRIPTION: 'COLLECT_INFORMATION',
								},
								{
									NAME: 'CHANNELS',
									PATH: ['', 'modules', this.moduleId, 'channels'],
									SRC: 'email',
									DESCRIPTION: 'MODES_TO_REACH_SUPPORT_TEAM',
								},
								{
									NAME: 'PDFs',
									PATH: ['', 'modules', this.moduleId, 'pdf'],
									SRC: 'picture_as_pdf',
									DESCRIPTION: 'GENERATE_PDF',
								},
								{
									NAME: 'TASK',
									PATH: ['', 'modules', this.moduleId, 'task'],
									SRC: 'list_alt',
									DESCRIPTION: 'MANAGE_TASK',
								},
							];
						}
						let i = 0;
						this.navigations.forEach((element) => {
							if (i < 3 && element.NAME !== 'MODULE_DETAIL') {
								this.navigationList1.push(element);
								i++;
							} else if (i < 6 && element.NAME !== 'MODULE_DETAIL') {
								this.navigationList2.push(element);
								i++;
							} else if (element.NAME !== 'MODULE_DETAIL') {
								this.navigationList3.push(element);
								i++;
							}
						});
					},
					(error: any) => {
						this.errorMessage = error.error.ERROR;
					}
				);
			} else {
				this.moduleForm = this._formBuilder.group({
					NAME: ['', Validators.required],
					SINGULAR_NAME: '',
					PLURAL_NAME: '',
					DESCRIPTION: '',
				});
			}
		});
	}

	public getRequiredFieldErrorMessage(field: any) {
		let message = '';
		const parameters = {
			field: this.translateService.instant(field),
		};
		this.translateService
			.get('FIELD_REQUIRED', parameters)
			.subscribe((res: string) => {
				message += res;
			});
		return message;
	}

	public save() {
		if (this.moduleId === 'new') {
			this.moduleApiService.postModule(this.moduleForm.value).subscribe(
				(response: any) => {
					this.companiesService.trackEvent(`Created Module`, {
						MODULE_ID: response.MODULE_ID,
					});
					// this.mixpanelService.track(`Created New Module`);
					const moduleId = response.MODULE_ID;
					this.successMessage =
						this.translateService.instant('SAVED_SUCCESSFULLY');

					this.router.navigate(['modules', moduleId, 'channels', 'email']);

					// updates sidebar upon successful save
					this.companiesService.getModuleSidebar().subscribe(
						(data: any) => {
							// convert names to upper case strings and replace spaces with underscores
							data.MENU_ITEMS.forEach((menuItem) => {
								switch (menuItem.NAME) {
									case 'TICKETS' ||
										'CHATS' ||
										'TEAMS' ||
										'MANAGE_USERS' ||
										'ACCOUNTS' ||
										'KNOWLEDGE_BASE' ||
										'MODULES' ||
										'SCHEDULES' ||
										'ESCALATIONS' ||
										'REPORTS' ||
										'COMPANY_SETTINGS':
										const translationKey = menuItem.NAME.toUpperCase().replace(
											/ /g,
											'_'
										);
										menuItem.NAME =
											this.translateService.instant(translationKey);
								}
							});
							this.companiesService.userSidebar = data;
						},
						(error) => {
							console.log(error);
						}
					);
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
		} else {
			this.moduleApiService.updateModule(this.moduleForm.value).subscribe(
				(response: any) => {
					this.successMessage = this.translateService.instant(
						'UPDATED_SUCCESSFULLY'
					);
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
		}
	}

	public clicked(nav) {
		this.router.navigate([
			`${nav.PATH[1]}`,
			`${nav.PATH[2]}`,
			`${nav.PATH[3]}`,
		]);
	}
}
