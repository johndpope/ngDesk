import {
	animate,
	state,
	style,
	transition,
	trigger,
} from '@angular/animations';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Menu, SidebarApiService } from '@ngdesk/sidebar-api';
import { CompaniesService } from '../companies/companies.service';
import { MessagingService } from '../firebase/messaging.service';
import { MenuItem } from '../models/menu-item';
import { ModulesService } from '../modules/modules.service';
import { ApplicationSettings } from '../ns-local-storage/app-settings-helper';
import { AngularFireMessagingHelper } from '../ns-local-storage/angular-fire-messaging-helper';

import { RolesService } from '../roles/roles.service';
// import { FilePreviewOverlayService } from '../shared/file-preview-overlay/file-preview-overlay.service';
import { UsersService } from '../users/users.service';

export const setSubdomain = '';

@Component({
	selector: 'app-module-sidebar',
	templateUrl: './module-sidebar.component.html',
	styleUrls: ['./module-sidebar.component.scss'],
	animations: [
		trigger('openClose', [
			state(
				'open',
				style({
					width: '225px',
				})
			),
			state(
				'closed',
				style({
					width: '56px',
				})
			),
			transition('open => closed', [animate('0.1s')]),
			transition('closed => open', [animate('0.1s')]),
		]),
		trigger('indicatorRotate', [
			state('collapsed', style({ transform: 'rotate(-90deg)' })),
			state('expanded', style({ transform: 'rotate(0deg)' })),
			transition(
				'expanded <=> collapsed',
				animate('225ms cubic-bezier(0.4,0.0,0.2,1)')
			),
		]),
	],
	providers: [
		MessagingService,
		ApplicationSettings,
		AngularFireMessagingHelper,
		// 	{
		// 		provide: SIDEBAR_BASE_PATH,
		// 		useValue: 'https://'+ this.setSubdomain +'.ngdesk.com/api/ngdesk-sidebar-service-v1'
		// }
	],
})
export class ModuleSidebarComponent implements OnInit {
	public isOpen = false;
	public menuItems: MenuItem[] = [];
	public sidebarTitle = 'ngDesk';
	public sidebarLogo;
	public mobileDevice = false;
	public iconList: any = {};
	public modules: any;
	public expandedIndex = -1;
	public authToken;
	public sidebarItem: any = {};
	public setSubdomain = 'prod';
	public isloadingData = false;
	public selectedOptionIndex = -1;
	public fpos: any;
	public temporaryTicketsMenuForMobile;
	constructor(
		public companiesService: CompaniesService,
		private sidebarApiService: SidebarApiService,
		public router: Router,
		public modulesService: ModulesService,
		public userService: UsersService,
		// public fpos: FilePreviewOverlayService,
		private rolesService: RolesService,
		private messagingService: MessagingService,
		private applicationSettingsHelper: ApplicationSettings
	) {
		this.setSubdomain = this.applicationSettingsHelper.getSubdomain();
		this.fpos = this.applicationSettingsHelper.fpos;
		this.authToken = this.userService.getAuthenticationToken();
	}

	public ngOnInit() {
		this.applicationSettingsHelper.setshowBackButton();
		this.applicationSettingsHelper.postUserToken(
			this.authToken,
			this.rolesService,
			this.userService,
			this.messagingService
		);
		this.loadSidebar();
		this.checkDeviceType();
	}

	public loadSidebar() {
		this.isloadingData = true;
		this.sidebarApiService.getSidebarByRole().subscribe(
			(data: Menu) => {
				// convert names to upper case strings and replace spaces with underscores
				data.MENU_ITEMS.forEach((menuItem) => {
					// Temporary fix
					if (menuItem.NAME === 'Tickets') {
						this.temporaryTicketsMenuForMobile = menuItem;
					}
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
							'COMPANY_SETTINGS' ||
							'DASHBOARDS' ||
							'MY_ACTION_ITEMS':
							const translationKey = menuItem.NAME.toUpperCase().replace(
								/ /g,
								'_'
							);
							menuItem.NAME = translationKey;
					}
				});

				this.sidebarTitle = this.userService.sidebarTitle;
				this.sidebarLogo = this.userService.sidebarLogo;
				this.companiesService.userSidebar = data;
				this.isloadingData = false;
			},
			(error) => {
				this.isloadingData = false;
				console.log(error);
			}
		);
	}

	public expandCollapseMenu(menuItemIndex: number) {
		if (this.expandedIndex === -1 || this.expandedIndex !== menuItemIndex) {
			this.expandedIndex = menuItemIndex;
		} else if (this.expandedIndex === menuItemIndex) {
			this.expandedIndex = -1;
		}
	}

	private checkDeviceType() {
		const ua = navigator.userAgent;

		if (
			/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini|Mobile|mobile|CriOS/i.test(
				ua
			)
		) {
			this.mobileDevice = true;
		}
	}

	public generateRouterLink(menuItem) {
		if (menuItem.IS_MODULE) {
			return ['render', menuItem.MODULE];
		} else if (!menuItem.IS_MODULE && menuItem.MODULE !== '') {
			return [menuItem.MODULE];
		} else if (
			menuItem.NAME === 'TRIGGERS' ||
			menuItem.NAME === 'LIST_LAYOUTS' ||
			menuItem.NAME === 'DETAIL_LAYOUTS'
		) {
			const params = menuItem.ROUTE.split('/');
			return [params[0], params[1], params[2]];
		} else if (menuItem.ROUTE !== '') {
			return [menuItem.ROUTE];
		} else {
			return null;
		}
	}

	public trackViaMixpanel(menuItem) {
		this.companiesService.trackEvent(`Sidebar Go to ${menuItem.NAME}`, {
			MODULE_ID: menuItem.MODULE,
		});
	}
	public onSidebarClick(menuItem, index) {
		this.selectedOptionIndex = index;

		setTimeout(() => {
			if (menuItem.IS_MODULE) {
				// return ['render', menuItem.MODULE];
				// this.router.navigate([`render/${menuItem.MODULE}`])
				this.selectedOptionIndex = -1;
				this.applicationSettingsHelper.navigateToListLayout(menuItem.MODULE);
			} else if (
				menuItem.NAME === 'TRIGGERS' ||
				menuItem.NAME === 'LIST_LAYOUTS' ||
				menuItem.NAME === 'DETAIL_LAYOUTS'
			) {
				const params = menuItem.ROUTE.split('/');
				// return [params[0], params[1], params[2]];
			} else if (menuItem.ROUTE !== '') {
				// return [menuItem.ROUTE];
				this.router.navigate([`${menuItem.ROUTE}`]);
			} else {
				return null;
			}
		}, 0);
	}

	public temporaryMobileNavigation() {
		this.applicationSettingsHelper.navigateToListLayout(
			this.temporaryTicketsMenuForMobile.MODULE
		);
	}

	public userDetails() {
		this.router.navigate(['/logout']);
	}
}
