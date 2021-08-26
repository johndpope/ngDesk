import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import { AppGlobals } from '../../app.globals';
import { Field } from '../../models/field';
import { Role } from '../../models/role';
import { ModulesService } from '../../modules/modules.service';
import { RenderLayoutService } from '../../render-layout/render-layout.service';
import { FieldCreatorComponent } from 'src/app/modules/modules-detail/fields/field-creator/field-creator.component';
import { UsersService } from 'src/app/users/users.service';
import { RolesService } from '@src/app/roles/roles.service';

@Component({
	selector: 'app-api-builder',
	templateUrl: './api-builder.component.html',
	styleUrls: ['./api-builder.component.scss'],
})
export class ApiBuilderComponent implements OnInit {
	public selectedModule = {
		NAME: null,
		FIELDS: [],
	};
	public country: any = {
		COUNTRY_CODE: 'us',
		DIAL_CODE: '+1',
		COUNTRY_FLAG: 'us.svg',
		PHONE_NUMBER: '',
	};
	public order = '';
	public pagesize = '';
	public sort = '';
	public page = '';
	public search = '';
	public sortValues: any[] = [];
	public roles: Role[];
	public entry: any;
	public errorMessage: string;
	public modules: any[] = [];
	public checkBoxValues = [true, false];
	public relationFieldEntries = {};
	public curlCommand = 'curl -X POST'; // TODO: hard coded will need to be removed
	public httpEndpoint: string;
	public httpEndpointGet: string;
	public copyTooltip: string;
	public displayedFields: Field[];
	public additionalFields: Field[];
	public attachments = [];
	public requestType = 'POST'; // TODO: hard coded to POST for now, will need to remove
	public requestTypes = [{ DISPLAY: 'GET' }, { DISPLAY: 'POST' }];
	public isFirefox;
	public apiKey = '';
	public params;
	public isGetByFilter = false;
	public allListLayouts = [];
	public selectedListLayout;
	public currentRole;
	public moduleId;

	constructor(
		private globals: AppGlobals,
		private modulesService: ModulesService,
		private translateService: TranslateService,
		public renderLayoutService: RenderLayoutService,
		private rolesService: RolesService,
		private cd: ChangeDetectorRef,
		private usersService: UsersService
	) {}

	public ngOnInit() {
		this.isFirefox =
			window.navigator.userAgent.indexOf('Firefox') !== -1 ? true : false;
		// gets list of modules to select from
		this.modulesService.getModules().subscribe(
			(modulesResponse: any) => {
				this.modules = modulesResponse.MODULES.sort((a, b) =>
					a.NAME.localeCompare(b.NAME)
				);

				// convert names to upper case strings and replace spaces with underscores
				this.modules.forEach((item) => {
					switch (item.NAME) {
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
							'COMPANY_SETTINGS':
							const translationKey = item.NAME.toUpperCase().replace(/ /g, '_');
							item.NAME = this.translateService.instant(translationKey);
					}
				});
			},
			(modulesError: any) => {
				this.errorMessage = modulesError.error.ERROR;
			}
		);

		this.params = {
			MODULE: { field: this.translateService.instant('MODULE') },
			API_KEY: { field: this.translateService.instant('API_KEY') },
		};

		this.rolesService
			.getRole(this.usersService.user.ROLE)
			.subscribe((role: any) => {
				this.currentRole = role.ROLE_ID;
			});

		this.rolesService.getRoles().subscribe(
			(rolesResponse: any) => {
				this.roles = rolesResponse.ROLES;
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	public loadModuleFields(selectedModule) {
		const baseUrl = `https://${this.usersService.getSubdomain()}.ngdesk.com/api/ngdesk-data-service-v1`;
		this.httpEndpoint = `'${baseUrl}/modules/${selectedModule.MODULE_ID}/data' -H 'authentication_token:`;
		this.httpEndpointGet = `${baseUrl}/modules/${selectedModule.MODULE_ID}/data`;
		this.moduleId = selectedModule.MODULE_ID;
		this.entry = {};
		const tempLayouts = selectedModule.LIST_LAYOUTS;
		this.setAllListLayouts(tempLayouts);
		this.additionalFields = selectedModule.FIELDS.filter(
			(field) =>
				!field.NOT_EDITABLE &&
				field.RELATIONSHIP_TYPE !== 'One to Many' &&
				!field.REQUIRED
		);
		this.displayedFields = selectedModule.FIELDS.filter(
			(field) =>
				!field.NOT_EDITABLE &&
				field.RELATIONSHIP_TYPE !== 'One to Many' &&
				field.REQUIRED
		);
		for (const field of this.displayedFields) {
			this.params[field.NAME] = {
				field: this.translateService.instant(field.NAME),
			};
			if (!field.NOT_EDITABLE) {
				this.formatPayload(field);
			}
		}
		this.entry = JSON.parse(JSON.stringify(this.entry));

		this.sortValues = [];
		this.modules.forEach((element) => {
			if (this.selectedModule.NAME === element.NAME) {
				element.FIELDS.forEach((label) => {
					this.sortValues.push({
						value: label.DISPLAY_LABEL,
						viewValue: label.DISPLAY_LABEL,
					});
				});
			}
		});
	}

	// copy curl command
	public copyText() {
		let val = '';
		if (this.requestType === 'GET') {
			val = `${this.curlCommand} ${this.httpEndpointGet}
			sort=${this.sort}&order=${this.order}&page=${this.page}&page_size=${this.pagesize}&search=${this.search}
			&authentication_token=${this.apiKey}`;
		} else {
			val = `${this.curlCommand} ${this.httpEndpoint}${
				this.apiKey
			}' -H 'Content-Type: application/json' -d '${JSON.stringify(
				this.entry
			)}'`;
		}
		const selBox = document.createElement('textarea');
		selBox.style.position = 'fixed';
		selBox.style.left = '0';
		selBox.style.top = '0';
		selBox.style.opacity = '0';
		selBox.value = val;
		document.body.appendChild(selBox);
		selBox.focus();
		selBox.select();
		document.execCommand('copy');
		document.body.removeChild(selBox);
		this.copyTooltip = this.translateService.instant('COPIED');
	}

	// date change for firefox
	public changeDate(event, type, fieldName) {
		let dateString = '';
		let timeString = '';
		if (type === 'date') {
			dateString = event.target.value;
			if (this.entry[fieldName].split('T')[1] !== undefined) {
				timeString = this.entry[fieldName].split('T')[1];
			}
		} else {
			dateString = this.entry[fieldName].split('T')[0];
			timeString = event.target.value;
		}
		this.entry[fieldName] = dateString + 'T' + timeString;
	}

	public requestTypeChanged(requestType) {
		// TODO: add in get and put methods
		switch (requestType) {
			case 'POST':
				this.curlCommand = `$ curl -X ${requestType} `;
				break;
			case 'GET':
				this.curlCommand = `$ curl -X ${requestType} `;
				break;
		}
	}

	private formatPayload(field) {
		switch (field.DATA_TYPE.DISPLAY) {
			case 'Discussion': {
				this.entry[field.NAME] = [
					{
						MESSAGE: '',
						ATTACHMENTS: [],
					},
				];
				break;
			}
			case 'Relationship': {
				this.entry[field.NAME] = '';
				this.relationFieldEntries[field.PRIMARY_DISPLAY_FIELD] = [];
				this.modulesService.getModuleById(field.MODULE).subscribe(
					(relationModule: any) => {
						const primaryDisplayField = relationModule.FIELDS.find(
							(tempField) => tempField.FIELD_ID === field.PRIMARY_DISPLAY_FIELD
						);
						field.PRIMARY_DISPLAY_FIELD_NAME = primaryDisplayField.NAME;
						this.modulesService.getEntries(relationModule.MODULE_ID).subscribe(
							(entriesResponse: any) => {
								this.relationFieldEntries[field.PRIMARY_DISPLAY_FIELD] =
									entriesResponse.DATA;
							},
							(error) => {
								this.errorMessage = error.error.ERROR;
							}
						);
					},
					(error) => {
						this.errorMessage = error.error.ERROR;
					}
				);
				break;
			}
			case 'List Text': {
				this.entry[field.NAME] = [];
				break;
			}
			case 'File Upload': {
				break;
			}

			case 'Phone': {
				this.entry[field.NAME] = {
					COUNTRY_CODE: this.country['COUNTRY_CODE'],
					DIAL_CODE: this.country['DIAL_CODE'],
					COUNTRY_FLAG: this.country['COUNTRY_FLAG'],
					PHONE_NUMBER: '',
				};

				break;
			}

			default: {
				this.entry[field.NAME] = '';
			}
		}
	}

	public addField(field) {
		this.additionalFields.splice(this.additionalFields.indexOf(field), 1);
		this.formatPayload(field);
		this.displayedFields.push(field);
	}

	public removeField(field, index) {
		this.displayedFields.splice(index, 1);
		delete this.entry[field.NAME];
		this.additionalFields.push(field);
		if (field.DATA_TYPE.DISPLAY === 'File Upload') {
			this.attachments = [];
			this.entry['MESSAGES'][0]['ATTACHMENTS'] = [];
		}
	}

	// adds attachment with message
	public onFileChange(event) {
		const reader = new FileReader();

		if (
			event.target.files &&
			event.target.files.length &&
			this.attachments.length <= 5
		) {
			const [file] = event.target.files;
			reader.readAsDataURL(file);

			// (file.size <= 1024000) ? this.fileSizeError = false : this.fileSizeError = true;
			reader.onload = () => {
				const data: any = reader.result;
				this.attachments.push({
					FILE_NAME: file.name,
					FILE: data.split('base64,')[1],
					FILE_EXTENSION: file.type,
					ATTACHMENT_UUID: this.globals.guid(),
				});
				// need to run CD since file load runs outside of zone
				this.cd.markForCheck();
				this.entry['MESSAGES'][0]['ATTACHMENTS'] = this.attachments;
			};
		}
	}

	public updatePhoneInfo(country, fieldName) {
		this.country['COUNTRY_CODE'] = country.COUNTRY_CODE;
		this.country['DIAL_CODE'] = country.COUNTRY_DIAL_CODE;
		this.country['COUNTRY_FLAG'] = country.COUNTRY_FLAG;
		this.country['PHONE_NUMBER'] = '';
	}

	public setAllListLayouts(listLayouts) {
		this.allListLayouts = [];
		listLayouts.forEach((layout) => {
			if (layout.ROLE === this.currentRole) {
				this.allListLayouts.push(layout);
			}
		});
		this.selectedListLayout = this.allListLayouts[0].LAYOUT_ID;
	}

	public updateAPI() {
		if (this.isGetByFilter) {
			this.httpEndpointGet = `${this.globals.baseRestUrl}/modules/${this.moduleId}/layouts/${this.selectedListLayout}?`;
		} else {
			this.httpEndpointGet = `${this.globals.baseRestUrl}/modules${this.moduleId}/data?`;
		}
	}

	public OnChange() {
		this.updateAPI();
	}
}
