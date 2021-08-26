import {
	Component,
	EventEmitter,
	Input,
	OnChanges,
	OnInit,
	Output,
} from '@angular/core';

import { DataApiService, MergeApiService } from '@ngdesk/data-api';
import { TranslateService } from '@ngx-translate/core';

import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';

@Component({
	selector: 'app-merge-entries-sidenav',
	templateUrl: './merge-entries-sidenav.component.html',
	styleUrls: ['./merge-entries-sidenav.component.scss'],
})
export class MergeEntriesSidenavComponent implements OnInit, OnChanges {
	@Input() public module;
	@Input() public listLayout: any;
	@Input() public data: any;
	@Output() public cancel = new EventEmitter<any>();
	@Output() public save = new EventEmitter<any>();
	public mergeData;
	public mergeSave = false;
	public fieldMap = [];
	public mergeSearchData = [];
	public params = {
		moduleName: {},
	};

	constructor(
		private dataService: DataApiService,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private mergeApi: MergeApiService
	) {}

	public ngOnInit() {
		// sets the params for translation
		this.params.moduleName = { value: this.module['NAME'] };
		// gets fieldMap of only the columns to show in layout
		this.fieldMap = this.module['FIELDS']
			.filter(
				(f) =>
					this.listLayout['COLUMN_SHOW']['FIELDS'].indexOf(f.FIELD_ID) !== -1
			)
			.map((v) => [v.NAME, v.DISPLAY_LABEL]);
	}

	public ngOnChanges() {
		//sets the data for merge
		const listLayoutData = JSON.parse(JSON.stringify(this.data));
		let relationshipFields = [];
		let prefixAndSuffixFields = [];
		this.module.FIELDS.map((moduleField) => {
			if (moduleField.DATA_TYPE.DISPLAY == 'Relationship') {
				relationshipFields.push(moduleField);
			}
			if (
				moduleField.DATA_TYPE.DISPLAY !== 'Auto Number' &&
				(moduleField.DATA_TYPE.BACKEND === 'Integer' ||
					moduleField.DATA_TYPE.BACKEND === 'Float' ||
					moduleField.DATA_TYPE.BACKEND === 'Double' ||
					moduleField.DATA_TYPE.DISPLAY === 'Formula')
			) {
				if (
					(moduleField.PREFIX !== null && moduleField.PREFIX !== '') ||
					(moduleField.SUFFIX !== null && moduleField.SUFFIX !== '')
				) {
					prefixAndSuffixFields.push(moduleField);
				}
			}
		});

		const phoneField = this.module['FIELDS'].find(
			(field) => field.DATA_TYPE.DISPLAY === 'Phone'
		);

		let phoneFieldName = null;
		if (phoneField !== null && phoneField !== undefined) {
			phoneFieldName = phoneField.NAME;
		}

		listLayoutData.forEach(function (currentData) {
			for (const field of relationshipFields) {
				const key = field['NAME'];
				if (currentData[key] !== null && currentData[key] !== undefined) {
					if (
						currentData[key]['PRIMARY_DISPLAY_FIELD'] !== null &&
						currentData[key]['PRIMARY_DISPLAY_FIELD'] !== undefined
					) {
						const value = currentData[key]['PRIMARY_DISPLAY_FIELD'];
						currentData[key] = value;
					}
				}
			}

			for (const field of prefixAndSuffixFields) {
				const key = field['NAME'];
				if (
					field['PREFIX'] !== null &&
					field['PREFIX'] !== '' &&
					field['PREFIX'] !== undefined
				) {
					const prefix = field['PREFIX'];
					if (currentData[key] !== null && currentData[key] !== undefined) {
						const value = prefix + currentData[key];
						currentData[key] = value;
					}
				} else if (
					field['SUFFIX'] !== null &&
					field['SUFFIX'] !== '' &&
					field['SUFFIX'] !== undefined
				) {
					const suffix = field['SUFFIX'];
					if (currentData[key] !== null && currentData[key] !== undefined) {
						const value = currentData[key] + suffix;
						currentData[key] = value;
					}
				}
			}

			if (
				phoneFieldName !== null &&
				currentData[phoneFieldName] !== null &&
				currentData[phoneFieldName] !== undefined
			) {
				if (
					currentData[phoneFieldName]['PHONE_NUMBER'] !== null &&
					currentData[phoneFieldName]['PHONE_NUMBER'] !== undefined
				) {
					const phoneNumber = currentData[phoneFieldName]['PHONE_NUMBER'];
					const dialCode = currentData[phoneFieldName]['DIAL_CODE'];
					currentData[phoneFieldName] = dialCode + ' ' + phoneNumber;
				} else if (currentData[phoneFieldName]['PHONE_NUMBER'] === null) {
					currentData[phoneFieldName] = '+1';
				}
			}
		});

		this.mergeData = listLayoutData[0];

		if (listLayoutData.length > 1) {
			this.mergeSearchData = listLayoutData.filter((f, i) => i > 0);
			for (const currentData of this.mergeSearchData) {
				currentData.merge = true;
			}
			this.mergeSave = true;
		} else {
			this.mergeSearchData = [];
			this.mergeSave = false;
		}
	}

	public mergeChanged(data) {
		this.data.merge = !this.data.merge;
		for (const currentData of this.mergeSearchData) {
			if (currentData.merge) {
				this.mergeSave = true;
				break;
			} else {
				this.mergeSave = false;
			}
		}
	}

	public onMergeEntries(): void {
		// makes api call and sends back to render list layout new
		if (this.mergeSave) {
			const merge = this.mergeSearchData
				.filter((f) => f.merge)
				.map((v) => v.DATA_ID);
			const payload = {
				ENTRY_ID: this.mergeData.DATA_ID,
				MERGE_ENTRY_IDS: merge,
			};
			// TODO: replace with data service for merge entries
			this.mergeApi
				.mergeModuleEntries(this.module['MODULE_ID'], payload)
				.subscribe(
					(response) => {
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('MERGED_SUCCESSFULLY'),
						});
						this.save.emit();
					},
					(error) =>
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						})
				);
		}
	}

	public validateDate(value) {
		const date_regex =
			/^(0[1-9]|1[0-2])\/(0[1-9]|1\d|2\d|3[01])\/(19|20)\d{2}$/;

		if (date_regex.test(value)) {
			return true;
		} else {
			return false;
		}
	}
}
