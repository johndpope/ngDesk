import { Component, Inject, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { UsersService } from '@src/app/users/users.service';

@Component({
	selector: 'app-delete-company-dialog',
	templateUrl: './delete-company-dialog.component.html',
	styleUrls: ['./delete-company-dialog.component.scss'],
})
export class DeleteCompanyDialogComponent implements OnInit {
	public deleteCompanyForm: FormGroup;
	public feedbacks: any = [
		{ NAME: 'CREATED_SECOND_ACCOUNT' },
		{ NAME: 'PRIVACY_CONCERNS' },
		{ NAME: 'TOO_BUSY_TOO_DISTRACTING' },
		{ NAME: 'TOO_DIFFICULT_TO_USE' },
		{ NAME: 'TROUBLE_GETTING_STARTED' },
		{ NAME: 'WANT_TO_REMOVE_SOMETHING' },
		{ NAME: 'SOMETHING_ELSE' },
	];

	public params = {
		typedSubdomain: {},
		reason: {},
		feedback: {},
		subdomain: '',
	};

	constructor(
		@Inject(MAT_DIALOG_DATA) public data: any,
		private translateService: TranslateService,
		private usersService: UsersService
	) {
		this.translateService.get('SUBDOMAIN').subscribe((res: string) => {
			this.params['typedSubdomain']['field'] = res;
			console.log(res);
		});

		this.translateService.get('REASON').subscribe((res: string) => {
			this.params['reason']['field'] = res;
		});

		this.translateService.get('FEEDBACK').subscribe((res: string) => {
			this.params['feedback']['field'] = res;
		});

		this.params['subdomain'] = this.usersService.getSubdomain();
	}

	public ngOnInit() {
		this.deleteCompanyForm = this.data.deleteCompanyForm;
	}
}
