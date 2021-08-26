import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';

import { CompaniesService } from '../../companies/companies.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { LoadingDialogComponent } from '../../dialogs/loading-dialog/loading-dialog.component';

import { ConfirmDialogComponent } from '../../dialogs/confirm-dialog/confirm-dialog.component';
import { UsersService } from '../../users/users.service';

@Component({
	selector: 'app-cname',
	templateUrl: './cname.component.html',
	styleUrls: ['./cname.component.scss']
})
export class CnameComponent implements OnInit {
	public isLinear = false;
	public errors;
	public param;
	public cnameFormGroup: FormGroup;
	public showLoader = false;
	public isDisabled = true;

	constructor(
		private _formBuilder: FormBuilder,
		private companiesService: CompaniesService,
		private usersService: UsersService,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private dialog: MatDialog
	) {}

	public ngOnInit() {
		this.cnameFormGroup = this._formBuilder.group({
			cname: ['', Validators.required]
		});

		this.errors = { cname: { field: 'CNAME' } };

		this.param = { subdomain: this.usersService.getSubdomain() };

		// get CNAME
		this.companiesService.getCname().subscribe(
			(data: any) => {
				this.cnameFormGroup.get('cname').setValue(data.CNAME);
				if (data.CNAME !== '') {
					this.isDisabled = false;
				}
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR
				});
			}
		);
	}

	public saveCNAME() {
		// save new CNAME record
		if (this.cnameFormGroup.valid) {
			const dialogRef = this.dialog.open(LoadingDialogComponent, {
				data: {
					message: this.translateService.instant('SAVING_CNAME_RECORD'),
					disclaimer: this.translateService.instant(
						'THIS_MIGHT_TAKE_UP_TO_MINUTE'
					),
					loadingTimer: 20
				}
			});

			// EVENT AFTER MODAL DIALOG IS OPENED
			dialogRef.afterOpened().subscribe(() => {
				this.companiesService
					.postCname(this.cnameFormGroup.get('cname').value)
					.subscribe(
						(cnameResponse: any) => {
							this.companiesService.trackEvent(
								`Added a new CNAME ${this.cnameFormGroup.get('cname').value}`
							);
							dialogRef.close('cancel');
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('CNAME_SAVE_SUCCESS')
							});
						},
						(cnameError: any) => {
							dialogRef.close('cancel');
							this.bannerMessageService.errorNotifications.push({
								message: cnameError.error.ERROR
							});
						}
					);
			});
		}
	}

	public deleteCNAME() {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message: this.translateService.instant('DELETING_CNAME_RECORD'),
				closeDialog: this.translateService.instant('NO'),
				buttonText: this.translateService.instant('YES'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn'
			}
		});
		dialogRef.afterClosed().subscribe(result => {
			if (result === this.translateService.instant('DELETE')) {
				this.companiesService.deleteCname().subscribe(
					(data: any) => {
						this.cnameFormGroup.get('cname').setValue('');
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('CNAME_DELETE_SUCCESS')
						});
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR
						});
					}
				);
			}
		});
	}
}
