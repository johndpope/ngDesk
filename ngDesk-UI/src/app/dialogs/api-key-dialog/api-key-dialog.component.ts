import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import {
	AbstractControl,
	FormBuilder,
	FormGroup,
	Validators,
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatStepper } from '@angular/material/stepper';
import { TranslateService } from '@ngx-translate/core';

import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { ApiService } from '../../api/api.service';
import { User } from '../../models/user';
import { ModulesService } from '../../modules/modules.service';

@Component({
	selector: 'app-api-key-dialog',
	templateUrl: './api-key-dialog.component.html',
	styleUrls: ['./api-key-dialog.component.scss'],
})
export class ApiKeyDialogComponent implements OnInit {
	public apiKey = '';
	public generateKeyForm: FormGroup;
	public users: User[];
	public errorMessage = '';
	public copyTooltip = '';
	public filteredOptions: Observable<any>;
	@ViewChild('stepper', { static: true }) private stepper: MatStepper;
	constructor(
		@Inject(MAT_DIALOG_DATA) public data: any,
		public dialogRef: MatDialogRef<ApiKeyDialogComponent>,
		private formBuilder: FormBuilder,
		private modulesService: ModulesService,
		private apiService: ApiService,
		public translateService: TranslateService
	) {}

	public ngOnInit() {
		this.generateKeyForm = this.formBuilder.group({
			KEY_NAME: ['', Validators.required],
			USER: ['', [Validators.required, this.checkUserExist]],
		});

		this.modulesService.getModuleByName('Users').subscribe(
			(response: any) => {
				this.modulesService.getEntries(response.MODULE_ID).subscribe(
					(entries: any) => {
						this.users = entries.DATA;

						this.filteredOptions = this.generateKeyForm.controls.USER.valueChanges.pipe(
							startWith(''),
							map((value) => this.filterUsers(value))
						);
					},
					(entriesError: any) => {
						this.errorMessage = entriesError.error.error;
					}
				);
			},
			(moduleError: any) => {
				this.errorMessage = moduleError.error.error;
			}
		);
	}

	public checkUserExist(control: AbstractControl) {
		if (!control.value.hasOwnProperty('DATA_ID')) {
			return { userError: true };
		} else {
			return null;
		}
	}

	// authentication token is the one we display to user so they have to save it before closing the modal
	public generateKey() {
		this.apiService
			.postKey(
				this.generateKeyForm.value.KEY_NAME,
				this.generateKeyForm.value.USER.DATA_ID
			)
			.subscribe(
				(postKeyResponse: any) => {
					this.apiKey = postKeyResponse.AUTHENTICATION_TOKEN;
					// this.dialogRef.updateSize('500px', '340px').updatePosition();
					this.stepper.selectedIndex = 2;
				},
				(postKeyError: any) => {
					this.stepper.selectedIndex = 0;
					this.errorMessage = postKeyError.error.ERROR;
				}
			);
	}

	public onNoClick(): void {
		this.dialogRef.close('cancel');
	}

	private filterUsers(value) {
		const filteredUsers = [];
		let filterValue = value;
		if (typeof value === 'string') {
			filterValue = value.toLowerCase();
		}
		this.users.forEach((user) => {
			if (user.EMAIL_ADDRESS.toLowerCase().includes(filterValue)) {
				filteredUsers.push(user);
			}
		});
		return filteredUsers;
	}

	// copy generated key
	public copyKey() {
		const selBox = document.createElement('textarea');
		selBox.style.position = 'fixed';
		selBox.style.left = '0';
		selBox.style.top = '0';
		selBox.style.opacity = '0';
		selBox.value = this.apiKey;
		document.body.appendChild(selBox);
		selBox.focus();
		selBox.select();
		document.execCommand('copy');
		document.body.removeChild(selBox);
		this.copyTooltip = this.translateService.instant('COPIED');
	}
}
