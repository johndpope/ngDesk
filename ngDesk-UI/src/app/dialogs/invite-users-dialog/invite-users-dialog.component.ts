import { Component, Inject, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CompaniesService } from '../../companies/companies.service';
import { RolesService } from '../../company-settings/roles/roles-old.service';
import { Role } from '../../models/role';
import { UsersService } from '../../users/users.service';

@Component({
	selector: 'app-invite-users-dialog',
	templateUrl: './invite-users-dialog.component.html',
	styleUrls: ['./invite-users-dialog.component.scss'],
})
export class InviteUsersDialogComponent implements OnInit {
	public errorMessage = '';
	public inviteUsersForm: FormGroup;
	public roles: Role[];
	public params;
	constructor(
		private translateService: TranslateService,
		private companiesService: CompaniesService,
		private rolesService: RolesService,
		private router: Router,
		private usersService: UsersService,
		private formBuilder: FormBuilder,
		public dialogRef: MatDialogRef<InviteUsersDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {}

	public ngOnInit() {
		// intialize translation params
		this.params = {
			firstName: { field: this.translateService.instant('FIRST_NAME') },
			lastName: { field: this.translateService.instant('LAST_NAME') },
			emailAddress: { field: this.translateService.instant('EMAIL_ADDRESS') },
			role: { field: this.translateService.instant('ROLE') },
		};

		this.rolesService.getRoles().subscribe(
			(rolesResponse: any) => {
				rolesResponse['ROLES'].filter(
					(role) => {
						if (role.NAME === 'Customers')
						{
						  role['NAME'] = 'Customer'; 
						} 
					});
				this.roles = rolesResponse.ROLES.filter(
					(role) =>
						role.NAME !== 'Public' &&
						role.NAME !== 'ExternalProbe' &&
						role.NAME !== 'LimitedUser'
				);
				this.roles = this.roles.sort((a, b) =>
					a.NAME.localeCompare(b.NAME)
				);
			},
			(error: any) => {
				console.log(error);
			}
		);

		this.inviteUsersForm = this.formBuilder.group({
			users: this.formBuilder.array([this.createFormItem()]),
		});
	}

	public getValue(val) {
		return this.translateService.instant(val);
	}

	public createFormItem(): FormGroup {
		return this.formBuilder.group({
			FIRST_NAME: ['', Validators.required],
			LAST_NAME: [''],
			EMAIL_ADDRESS: ['', [Validators.required, Validators.email]],
			ROLE: ['', Validators.required],
		});
	}

	public addInvite() {
		const users = this.inviteUsersForm.get('users') as FormArray;
		users.push(this.createFormItem());
	}
	public removeInvite(userIndex) {
		const users = this.inviteUsersForm.get('users') as FormArray;
		if (users.value.length > 1) {
			users.removeAt(userIndex);
		}
	}

	public sendInvite() {
		this.errorMessage = '';
		this.companiesService
			.postUserInvite({ USERS: this.inviteUsersForm.value.users })
			.subscribe(
				(inviteResponse: any) => {
					this.companiesService.trackEvent('Invited users', {
						USERS: this.inviteUsersForm.value.users,
					});
					let result = [];
					this.inviteUsersForm.value.users.forEach(user => {
						result.push(user.EMAIL_ADDRESS);
					});
					this.dialogRef.close(result);
					this.companiesService
						.getUsageType(this.usersService.getSubdomain())
						.subscribe(
							(usageType: any) => {
								if (
									(usageType.USAGE_TYPE.TICKETS &&
										!usageType.USAGE_TYPE.CHAT) ||
									usageType.USAGE_TYPE.PAGER
								) {
									this.companiesService
										.getAllGettingStarted()
										.subscribe((getAll: any) => {
											if (!getAll.GETTING_STARTED[3].COMPLETED) {
												this.companiesService
													.putGettingStarted(getAll.GETTING_STARTED[3])
													.subscribe(
														(put: any) => {},
														(errorResponse: any) => {
															console.log(errorResponse);
														}
													);
											}
										});
								}
								// location.reload();
							},
							(error: any) => {
								console.log(error);
							}
						);
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
	}

	public goToInvites() {
		this.dialogRef.close('cancel');
		this.router.navigate([`manage-invites`]);
	}

	// FormArray class contains the controls property.
	get formData() {
		return <FormArray>this.inviteUsersForm.get('users');
	}
}
