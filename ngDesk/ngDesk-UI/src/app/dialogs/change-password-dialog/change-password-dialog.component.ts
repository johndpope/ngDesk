import { Component, OnInit } from '@angular/core';

import { MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { UsersService } from '@src/app/users/users.service';

@Component({
	selector: 'app-change-password-dialog',
	templateUrl: './change-password-dialog.component.html',
	styleUrls: ['./change-password-dialog.component.scss'],
})
export class ChangePasswordDialogComponent implements OnInit {
	public newPassword: string;
	public oldPassword: string;
	public confirmPassword: string;
	public errorMessage: string;
	constructor(
		public dialogRef: MatDialogRef<ChangePasswordDialogComponent>,
		private bannerMessageService: BannerMessageService,
		private userService: UsersService,
		private translateService: TranslateService,
		public loaderService: LoaderService
	) {}

	public ngOnInit() {}
	public savePassword() {
		console.log(this.confirmPassword, this.oldPassword, this.newPassword);
		if (
			this.confirmPassword !== undefined &&
			this.oldPassword !== undefined &&
			this.newPassword !== undefined
		) {
			if (this.oldPassword !== this.newPassword) {
				const newPasswordObj: { NEW_PASSWORD: string; OLD_PASSWORD: string } = {
					NEW_PASSWORD: '',
					OLD_PASSWORD: '',
				};
				newPasswordObj.NEW_PASSWORD = this.newPassword;
				newPasswordObj.OLD_PASSWORD = this.oldPassword;
				if (this.newPassword === this.confirmPassword) {
					if (this.newPassword) {
						this.userService.postChangePassword(newPasswordObj).subscribe(
							(response: any) => {
								this.bannerMessageService.successNotifications.push({
									message: this.translateService.instant(
										'PASSWORD_SAVED_SUCCESSFULLY'
									),
								});
								this.dialogRef.close();
								this.loaderService.isLoading2 = false;
							},
							(error: any) => {
								this.bannerMessageService.errorNotifications.push({
									message: error.error.ERROR,
								});
							}
						);
					}
				} else {
					this.errorMessage = this.translateService.instant(
						'PASSWORD_NOT_MATCH'
					);
				}
			} else {
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('OLD_PASSWORD_NEW_PASSWORD'),
				});
			}
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('FILL_ALL_FIELDS'),
			});
		}
	}
}
