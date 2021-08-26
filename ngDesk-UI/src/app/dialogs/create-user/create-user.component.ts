import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { CompaniesService } from '@src/app/companies/companies.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { RenderLayoutService } from '@src/app/render-layout/render-layout.service';
import { RolesService } from '@src/app/roles/roles.service';

@Component({
	selector: 'app-create-user',
	templateUrl: './create-user.component.html',
	styleUrls: ['./create-user.component.scss'],
})
export class CreateUserComponent implements OnInit {
	public user: any = {};
	public roles: any = [];
	public showLayout = false;

	constructor(
		public dialogRef: MatDialogRef<CreateUserComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		public renderLayoutService: RenderLayoutService,
		private rolesService: RolesService,
		private companiesService: CompaniesService,
		private bannerMessageService: BannerMessageService
	) {}

	public ngOnInit() {
		this.rolesService.getRoles().subscribe((rolesResponse: any) => {
			this.roles = rolesResponse.ROLES.filter(
				(role) =>
					role.NAME !== 'Public' &&
					role.NAME !== 'ExternalProbe' &&
					role.NAME !== 'LimitedUser'
			);
		});
		this.user = this.data;
		this.showLayout = true;
	}

	public onNoClick(): void {
		this.dialogRef.close({ data: this.data.layer });
	}

	public createUser(): void {
		this.companiesService.postUserInvite({ USERS: [this.user] }).subscribe(
			(response: any) => {
				this.dialogRef.close({ data: this.data.layer });
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public updatePhoneInfo(country) {
		this.user.COUNTRY_CODE = country.COUNTRY_CODE;
		this.user.DIAL_CODE = country.COUNTRY_DIAL_CODE;
		this.user.COUNTRY_FLAG = country.COUNTRY_FLAG;
	}
}
