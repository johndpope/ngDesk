import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SidebarApiService } from '@ngdesk/sidebar-api';
import { TranslateService } from '@ngx-translate/core';
import { CustomTableService } from '../../../custom-table/custom-table.service';
import { RolesService } from '../../roles/roles-old.service';

@Component({
	selector: 'app-sidebar-customization-master',
	templateUrl: './sidebar-customization-master.component.html',
	styleUrls: ['./sidebar-customization-master.component.scss']
})
export class SidebarCustomizationMasterComponent implements OnInit {
	constructor(
		private customTableService: CustomTableService,
		private sidebarApiService: SidebarApiService,
		private router: Router,
		private translateService: TranslateService,
		private rolesService: RolesService
	) {}

	public ngOnInit() {
		this.rolesService.getRoles().subscribe(
			(rolesResponse: any) => {
				rolesResponse['ROLES'].filter(
					(role) => {
						if (role.NAME === 'Customers')
						{
						  role['NAME'] = 'Customer'; 
						} 
					});
				// TODO: Need Role Name from get Sidebar call
				this.customTableService.columnsHeadersObj = [
					{ DISPLAY: this.translateService.instant('ROLE'), NAME: 'ROLE_NAME' }
				];
				this.customTableService.columnsHeaders = [
					this.translateService.instant('ROLE')
				];
				this.sidebarApiService.getSidebar().subscribe(
					(response: any) => {
						// Get role name from id
						response.SIDEBAR_MENU.forEach(menu => {
							rolesResponse.ROLES.forEach(role => {
								if (role.ROLE_ID === menu.ROLE) {
									menu.ROLE_NAME = role.NAME;
								}
							});
						});

						// TODO: replace response.SIDEBAR_MENU.length with TOTLA_RECORDS
						this.customTableService.setTableDataSource(
							response.SIDEBAR_MENU,
							response.SIDEBAR_MENU.length
						);
					},
					(error: any) => {
						console.log(error);
					}
				);
			},
			(rolesError: any) => {
				console.log(rolesError);
			}
		);
	}

	// directing to detail sidebar page ot clicked role
	public rowClicked(rowData): void {
		this.router.navigate([
			`company-settings/sidebar-customization/detail/${rowData.ROLE}`
		]);
	}
}
