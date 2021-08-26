import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';

import { CommonLayoutService } from '@src/app/render-layout/render-detail-new/common-layout.service';
import { RolesService } from '@src/app/roles/roles.service';

@Injectable({
	providedIn: 'root',
})
export class PredefinedTemplateService {
	constructor(
		private rolesService: RolesService,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private commonLayoutService: CommonLayoutService
	) {}

	public getLayout(type, module) {
		if (type === 'detail') {
			return module['DETAIL_LAYOUTS'].find(
				(layout) => layout.ROLE === this.rolesService.role.ROLE_ID
			);
		} else if (type === 'create') {
			return module['CREATE_LAYOUTS'].find(
				(layout) => layout.ROLE === this.rolesService.role.ROLE_ID
			);
		} else if (type === 'edit') {
			if (module['EDIT_LAYOUTS'].length === 0) {
				// 	this.bannerMessageService.errorNotifications.push({
				// 	message: this.translateService.instant('NO_EDIT_LAYOUT_CREATED'),
				//    });
			} else {
				return module['EDIT_LAYOUTS'].find(
					(layout) => layout.ROLE === this.rolesService.role.ROLE_ID
				);
			}
		}
	}

	public getTemplateForCustomLayout(layout, module) {
		let customLayout = layout['CUSTOM_LAYOUT'];
		module['FIELDS'].forEach((moduleField) => {
			const fieldId = moduleField.FIELD_ID;
			const fieldName = moduleField.NAME;
			const displayLabel = moduleField.DISPLAY_LABEL;
			const regexName = new RegExp(`${fieldId}_NAME`, 'g');
			const regexDisplayLabel = new RegExp(`${fieldId}_DISPLAY_LABEL`, 'g');

			customLayout = customLayout
				.replace(regexName, fieldName)
				.replace(regexDisplayLabel, displayLabel);
		});
		return customLayout;
	}

	public formatPredefinedTemplate(
		layout,
		predefinedTemplate,
		module,
		layoutType
	) {
		const layoutStyle = this.commonLayoutService.getLayoutStyle(layout);
		layout['PREDEFINED_TEMPLATE'].forEach((section) => {
			let sectionTemplate = '';
			const styleType = section.FIELD_STYLE;
			section['FIELDS'].forEach((sectionField) => {
				const moduleField = module['FIELDS'].find(
					(field) => field.FIELD_ID === sectionField.FIELD_ID
				);

				switch (styleType) {
					case 'EDITABLE':
					case 'ONE_TO_MANY_SECTIONS':
						sectionTemplate += this.commonLayoutService.getTemplateForField(
							moduleField,
							layoutStyle,
							layoutType
						);
						break;
					case 'LABELS_WITH_TITLE':
						sectionTemplate += this.getLabels(moduleField);
						break;
					default:
						sectionTemplate +=
							'<mat-divider [vertical]=true></mat-divider>' +
							this.getPillsLayout(moduleField);
				}
			});
			const sectionTemplateRegex = new RegExp(
				`<ng-container id="${section.ID}"(.*?)<\/ng-container>`
			);
			predefinedTemplate = predefinedTemplate.replace(
				sectionTemplateRegex,
				sectionTemplate
			);
		});
		return predefinedTemplate;
	}

	public getLabels(field: any) {
		switch (field.DATA_TYPE.DISPLAY) {
			case 'Relationship':
				if (
					field.RELATIONSHIP_TYPE === 'Many to One' ||
					field.RELATIONSHIP_TYPE === 'One to One'
				) {
					return `<div><span class="mat-body-strong">${field.DISPLAY_LABEL} :  </span><span class="mat-body">
				 		{{ context.entry['${field.NAME}']['PRIMARY_DISPLAY_FIELD'] }}
					</span></div>`;
				} else if (field.RELATIONSHIP_TYPE === 'Many to Many') {
					return `<div><span class="mat-body-strong">${field.DISPLAY_LABEL} :
					 </span><span class="mat-body" *ngFor="let entry of context.entry['${field.NAME}']">
				 {{context.manyToManyMap[context.fieldsMap["${field.FIELD_ID}"].PRIMARY_DISPLAY_FIELD]}}
				</span></div>`;
				}
				break;
			case 'Date/Time':
				return `<div><span class="mat-body-strong">${field.DISPLAY_LABEL} : </span><span class="mat-body">
					{{context.entry.${field.NAME} | dateFormat:'medium'}}
                    </span></div>`;
			default:
				return `<div><span class="mat-body-strong">${field.DISPLAY_LABEL}: </span><span class="mat-body">
                {{context.entry['${field.NAME}'] === undefined ? '--' : context.entry['${field.NAME}']}}</span></div>`;
		}
	}

	public getPillsLayout(field) {
		switch (field.DATA_TYPE.DISPLAY) {
			case 'Relationship':
				if (this.rolesService.role.NAME !== 'Customers') {
					return `<div fxLayout="row" fxLayoutAlign="center center"
					[ngStyle]="{'background': '#E9EBED', 'padding': '5px', 'min-width':'150px'}" >
					<label class="pointer mat-body"
					(click)="context.navigateToManyToOneEntry(context.entry.${field.NAME}, context.fieldsMap['${field.FIELD_ID}'])">
					  {{ context.entry['${field.NAME}']['PRIMARY_DISPLAY_FIELD'] }}
					</label>
				  </div>`;
				} else {
					return `<div fxLayout="row" fxLayoutAlign="center center"
					[ngStyle]="{'background': '#E9EBED', 'padding': '5px', 'min-width':'150px'}" >
					<label class="mat-body">
						{{ context.entry['${field.NAME}']['PRIMARY_DISPLAY_FIELD'] }}
					</label>
				  </div>`;
				}
			default:
				return `<div fxLayout="row" fxLayoutAlign="center center"
					  [ngStyle]="{'background': '#E9EBED', 'padding': '5px','min-width':'150px'}">
						<label class="mat-body">{{context.entry.${field.NAME}}}</label>
					</div>`;
		}
	}
}
