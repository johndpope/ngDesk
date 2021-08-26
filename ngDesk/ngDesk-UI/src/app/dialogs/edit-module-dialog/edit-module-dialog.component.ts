import { Component, Inject, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DataApiService } from '@ngdesk/data-api';
import { TranslateService } from '@ngx-translate/core';
import * as _moment from 'moment';
import { OWL_DATE_TIME_FORMATS } from '@danielmoncada/angular-datetime-picker';
import { RolesService } from '../../company-settings/roles/roles-old.service';
import { BannerMessageService } from '../../custom-components/banner-message/banner-message.service';
import { Role } from '../../models/role';
import { ModulesService } from '../../modules/modules.service';
import { RenderLayoutService } from '../../render-layout/render-layout.service';
import { OWL_DATE_FORMATS } from '@src/app/render-layout/data-types/date-time.service';

@Component({
	selector: 'app-edit-module-dialog',
	templateUrl: './edit-module-dialog.component.html',
	styleUrls: ['./edit-module-dialog.component.scss'],
	providers: [
		// {
		//   provide: DateTimeAdapter,
		//   useClass: OwlMomentDateTimeModule,
		//   deps: [OWL_DATE_TIME_LOCALE]
		// },
		{ provide: OWL_DATE_TIME_FORMATS, useValue: OWL_DATE_FORMATS },
	],
})
export class EditModuleDialogComponent implements OnInit {
	private moduleId: string;
	public fields;
	private fieldName: string;
	public chronometerValues = {};
	public entry: any = {};
	public country: any = {
		COUNTRY_CODE: 'us',
		DIAL_CODE: '+1',
		COUNTRY_FLAG: 'us.svg',
		PHONE_NUMBER: '',
	};
	public roles: Role[];
	public editModuleForm: FormGroup;

	constructor(
		private bannerMessageService: BannerMessageService,
		private modulesService: ModulesService,
		private translateService: TranslateService,
		private rolesService: RolesService,
		public renderLayoutService: RenderLayoutService,
		@Inject(MAT_DIALOG_DATA) public data: any,
		public dialogRef: MatDialogRef<EditModuleDialogComponent>,
		private dataService: DataApiService
	) {}

	public ngOnInit() {
		this.fields = this.data.fields;
		this.moduleId = this.data.moduleId;
		this.rolesService.getRoles().subscribe(
			(rolesResponse: any) => {
				this.roles = rolesResponse.ROLES;
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	public onNoClick(): void {
		this.dialogRef.close('cancel');
	}

	public onUpdateEntries() {
		for (const field of this.fields) {
			this.fieldName = field['NAME'];

			if (field['DATA_TYPE']['DISPLAY'] === 'Phone') {
				this.country['PHONE_NUMBER'] = this.entry[this.fieldName];

				this.entry[this.fieldName] = this.country;
			}
		}

		const body = { ENTRY_IDS: [], UPDATE: {} };
		body.ENTRY_IDS = this.data.body.ENTRY_IDS;
		body.UPDATE = this.entry;

		this.dataService.bulkUpdate(this.moduleId, body).subscribe(
			(entriesResponse: any) => {
				this.dialogRef.close('cancel');
				this.bannerMessageService.successNotifications.push({
					message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
				});
			},
			(error: any) => {
				this.dialogRef.close('cancel');
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public updatePhoneInfo(country, fieldName) {
		this.country['COUNTRY_CODE'] = country.COUNTRY_CODE;
		this.country['DIAL_CODE'] = country.COUNTRY_DIAL_CODE;
		this.country['COUNTRY_FLAG'] = country.COUNTRY_FLAG;
		this.country['PHONE_NUMBER'] = '';
	}

	public helpText(option) {
		if (option.HELP_TEXT !== null && option.HELP_TEXT !== undefined) {
			return true;
		} else {
			return false;
		}
	}
}
