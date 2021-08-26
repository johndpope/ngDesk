import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Menu, Sidebar, SidebarApiService } from '@ngdesk/sidebar-api';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CompaniesService } from '../../../companies/companies.service';
import { SidebarMenuCustomizeDialogComponent } from '../../../dialogs/sidebar-menu-customize-dialog/sidebar-menu-customize-dialog.component';
import { ModulesService } from '../../../modules/modules.service';

@Component({
	selector: 'app-sidebar-customization-detail',
	templateUrl: './sidebar-customization-detail.component.html',
	styleUrls: ['./sidebar-customization-detail.component.scss'],
})
export class SidebarCustomizationDetailComponent implements OnInit {
	constructor(
		private companiesService: CompaniesService,
		private modulesService: ModulesService,
		private dialog: MatDialog,
		public route: ActivatedRoute,
		private router: Router,
		private translateService: TranslateService,
		private sidebarApiService: SidebarApiService,
		private bannerMessageService: BannerMessageService
	) {}
	public dialogRef: MatDialogRef<SidebarMenuCustomizeDialogComponent>;
	public sidebar: any = {};
	public modules: any = [];
	public sidebarForAllRoles: any = {};
	public sidebarLoaded = false;
	public schedule = { MODULE_ID: 'schedules', NAME: 'Schedules' };
	public escalations = { MODULE_ID: 'escalations', NAME: 'Escalations' };
	public knowledgeBase = { MODULE_ID: 'guide', NAME: 'Knowledge Base' };
	public internalMenus = [
		'DASHBOARD',
		'MY_ACTION_ITEMS',
		'KNOWLEDGE_BASE',
		'COMPANY_SETTINGS',
		'REPORTS',
		'PAGER',
		'MODULES',
		'SCHEDULES',
		'ESCALATIONS',
	];
	public ngOnInit() {
		// getting sidebar from GET sidebar call
		this.sidebarApiService.getSidebar().subscribe(
			(response: Sidebar) => {
				// TODO: Need API call for sidebar according to role
				const roleId = this.route.snapshot.params['roleId'];
				this.sidebarForAllRoles = response;
				this.sidebar = this.sidebarForAllRoles.SIDEBAR_MENU.find(
					(menu) => menu.ROLE === roleId
				);
				console.log(this.sidebar);

				this.modulesService.getAllModules().subscribe(
					(moduleResponse: any) => {
						this.modules = moduleResponse;
						this.sidebar.MENU_ITEMS.forEach((menu, menuIndex) => {
							moduleResponse.MODULES.forEach((module) => {
								if (module.MODULE_ID === menu.MODULE) {
									this.sidebar.MENU_ITEMS[menuIndex].MODULE = module.NAME;
								}
							});

							menu.SUB_MENU_ITEMS.forEach((subMenu, subMenuIndex) => {
								moduleResponse.MODULES.forEach((module) => {
									if (module.MODULE_ID === subMenu.MODULE) {
										this.sidebar.MENU_ITEMS[menuIndex].SUB_MENU_ITEMS[
											subMenuIndex
										].MODULE = module.NAME;
									}
								});
							});
							this.sidebarLoaded = true;
						});
						this.modules.MODULES.push(this.schedule);
						this.modules.MODULES.push(this.escalations);
						this.modules.MODULES.push(this.knowledgeBase);
						// Alphabetize the list of modules
						this.modules.MODULES = this.modules.MODULES.sort((a, b) =>
							a.NAME.localeCompare(b.NAME)
						);
					},
					(error: any) => {
						console.log(error);
					}
				);
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	public dropMenu(event: CdkDragDrop<string[]>) {
		moveItemInArray(
			this.sidebar.MENU_ITEMS,
			event.previousIndex,
			event.currentIndex
		);
		this.reorganizeMenuItems(this.sidebar.MENU_ITEMS);
	}

	public dropSubMenu(event: CdkDragDrop<string[]>, menuIndex: number) {
		const menu = this.sidebar.MENU_ITEMS[menuIndex];
		moveItemInArray(
			menu.SUB_MENU_ITEMS,
			event.previousIndex,
			event.currentIndex
		);
		this.reorganizeMenuItems(menu.SUB_MENU_ITEMS);
	}

	private reorganizeMenuItems(items: any[]) {
		for (let i = 0; i < items.length; i++) {
			items[i].ORDER = i;
		}
	}

	// Will open a dialog to edit the sidebar menu
	public editMenu(menu, menuIndex) {
		const selectedManu = Object.assign({}, menu);
		this.dialogRef = this.dialog.open(SidebarMenuCustomizeDialogComponent, {
			data: { MENU: selectedManu, MODULES: this.modules },
		});

		this.dialogRef.afterClosed().subscribe((result) => {
			if (result !== 'cancel' && result !== undefined) {
				const moduleFound = this.modules.MODULES.find(
					(module) => module.NAME === result.MODULE
				);
				if (
					result.MODULE &&
					result.MODULE !== '' &&
					result.MODULE !== 'Schedules' &&
					result.MODULE !== 'Escalations' &&
					result.MODULE !== 'Knowledge Base' &&
					moduleFound
				) {
					result.IS_MODULE = true;
				}
				this.sidebar.MENU_ITEMS[menuIndex] = result;
			}
		});
	}

	// Will open a dialog to edit the sidebar menu
	public editSubMenu(subMenu, menuIndex, subMenuIndex) {
		const selectedSubMenu = Object.assign({}, subMenu);
		this.dialogRef = this.dialog.open(SidebarMenuCustomizeDialogComponent, {
			data: { MENU: selectedSubMenu, MODULES: this.modules },
		});

		this.dialogRef.afterClosed().subscribe((result) => {
			if (result !== 'cancel' && result !== undefined) {
				const moduleFound = this.modules.MODULES.find(
					(module) => module.NAME === result.MODULE
				);
				if (
					result.MODULE &&
					result.MODULE !== '' &&
					result.MODULE !== 'Schedules' &&
					result.MODULE !== 'Escalations' &&
					result.MODULE !== 'Knowledge Base' &&
					moduleFound
				) {
					result.IS_MODULE = true;
				}
				this.sidebar.MENU_ITEMS[menuIndex].SUB_MENU_ITEMS[
					subMenuIndex
				] = result;
			}
		});
	}

	public addMenu(menu) {
		const selectedManu = Object.assign({}, menu);
		this.dialogRef = this.dialog.open(SidebarMenuCustomizeDialogComponent, {
			data: { MENU: selectedManu, MODULES: this.modules },
		});

		this.dialogRef.afterClosed().subscribe((result) => {
			if (result !== 'cancel' && result !== undefined) {
				const obj = {
					EDITABLE: true,
					IS_MODULE: true,
					ROUTE: '',
					ORDER: 0,
					MODULE: '',
					NAME: '',
					ICON: '',
					PATH_PARAMETER: '',
					SUB_MENU_ITEMS: [],
				};
				obj.NAME = result.NAME;
				obj.ICON = result.ICON;
				obj.MODULE = result.MODULE ? result.MODULE : '';
				obj.ORDER = this.sidebar.MENU_ITEMS.length + 1;
				if (
					!obj.MODULE ||
					obj.MODULE === 'Schedules' ||
					obj.MODULE === 'Escalations' ||
					obj.MODULE === 'Knowledge Base'
				) {
					obj.IS_MODULE = false;
				}
				this.sidebar.MENU_ITEMS.push(obj);
			}
		});
	}

	public addSubMenu(menuIndex) {
		const menu = {};
		const selectedManu = Object.assign({}, menu);
		this.dialogRef = this.dialog.open(SidebarMenuCustomizeDialogComponent, {
			data: { MENU: selectedManu, MODULES: this.modules },
		});

		this.dialogRef.afterClosed().subscribe((result) => {
			if (result !== 'cancel' && result !== undefined) {
				const obj = {
					EDITABLE: true,
					IS_MODULE: true,
					ROUTE: '',
					ORDER: 0,
					MODULE: '',
					NAME: '',
					ICON: '',
					PATH_PARAMETER: '',
				};
				obj.NAME = result.NAME;
				obj.ICON = result.ICON;
				obj.MODULE = result.MODULE ? result.MODULE : '';
				obj.ORDER = this.sidebar.MENU_ITEMS.length + 1;
				if (
					!obj.MODULE ||
					obj.MODULE === 'Schedules' ||
					obj.MODULE === 'Escalations' ||
					obj.MODULE === 'Knowledge Base'
				) {
					obj.IS_MODULE = false;
				}
				this.sidebar.MENU_ITEMS[menuIndex].SUB_MENU_ITEMS.push(obj);
			}
		});
	}

	// Change order of items
	public changeOrder(oldIndex, newIndex) {
		if (newIndex >= this.sidebar.MENU_ITEMS.length) {
			let k = newIndex - this.sidebar.MENU_ITEMS.length + 1;
			while (k--) {
				this.sidebar.MENU_ITEMS.push(undefined);
			}
		}
		this.sidebar.MENU_ITEMS.splice(
			newIndex,
			0,
			this.sidebar.MENU_ITEMS.splice(oldIndex, 1)[0]
		);
	}

	// To delete Menu
	public deleteMenu(menuIndex) {
		this.sidebar.MENU_ITEMS.splice(menuIndex, 1);
	}

	// To delete sub menu
	public deleteSubMenu(menuIndex, subMenuIndex) {
		const subMenuItems = this.sidebar.MENU_ITEMS[menuIndex].SUB_MENU_ITEMS;
		subMenuItems.splice(subMenuIndex, 1);
	}

	// replacing module names with module ID's and making saving sidebar (put call)
	public save() {
		// this is done so that data bound to UI does not change, like module name to module ID
		// for display purposes
		const sidebarObj = JSON.parse(JSON.stringify(this.sidebar));

		sidebarObj.MENU_ITEMS.forEach((menu, menuIndex) => {
			this.modules.MODULES.forEach((module) => {
				if (module.NAME === menu.MODULE) {
					sidebarObj.MENU_ITEMS[menuIndex].MODULE = module.MODULE_ID;
				}
			});
			if (menu.EDITABLE) {
				sidebarObj.MENU_ITEMS[menuIndex].ROUTE = menu.MODULE;
			}

			menu.SUB_MENU_ITEMS.forEach((subMenu, subMenuIndex) => {
				this.modules.MODULES.forEach((module) => {
					if (module.NAME === subMenu.MODULE) {
						sidebarObj.MENU_ITEMS[menuIndex].SUB_MENU_ITEMS[
							subMenuIndex
						].MODULE = module.MODULE_ID;
					}
				});
				if (menu.EDITABLE) {
					sidebarObj.MENU_ITEMS[menuIndex].SUB_MENU_ITEMS[subMenuIndex].ROUTE =
						menu.MODULE;
				}
			});
		});

		// replace sidebar with newly updated sidebar for role
		this.sidebarForAllRoles.SIDEBAR_MENU.forEach(
			(menuElement, menuElementIndex) => {
				if (sidebarObj.ROLE === menuElement.ROLE) {
					this.sidebarForAllRoles.SIDEBAR_MENU[menuElementIndex] = sidebarObj;
				}
			}
		);

		this.sidebarApiService.putSidebar(this.sidebarForAllRoles).subscribe(
			(response: Sidebar) => {
				// updates sidebar upon successful save
				this.sidebarApiService.getSidebarByRole().subscribe(
					(data: Menu) => {
						// convert names to upper case strings and replace spaces with underscores
						data.MENU_ITEMS.forEach((menuItem) => {
							const translationKey = menuItem.NAME.toUpperCase().replace(
								/ /g,
								'_'
							);
							// if translation does not exist (was custom inputted by user), then do not attempt to translate
							if (
								this.translateService.instant(translationKey) !== translationKey
							) {
								// if not a module, then translate
								if (!menuItem.IS_MODULE) {
									menuItem.NAME = this.translateService.instant(translationKey);
								}
							}
						});
						this.companiesService.userSidebar = data;
					},
					(error) => {
						console.log(error);
					}
				);
				this.router.navigate([`company-settings/sidebar-customization/master`]);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}
}
