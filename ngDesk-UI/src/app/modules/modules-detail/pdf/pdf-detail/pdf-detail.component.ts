import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import {
	FormBuilder,
	FormGroup,
	Validators,
	FormControl,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HtmlTemplateApiService } from '@ngdesk/module-api';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { Subscription } from 'rxjs';
import tinymce from 'tinymce/tinymce';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { config } from '../../../../tiny-mce/tiny-mce-config';
@Component({
	selector: 'app-pdf-detail',
	templateUrl: './pdf-detail.component.html',
	styleUrls: ['./pdf-detail.component.scss'],
})
export class PdfDetailComponent implements OnInit, OnDestroy {
	public pdfForm: FormGroup;
	private pdfId: string;
	public isSubmitting = false;
	public clickedSave = false;
	public modules: any[];
	public module;
	public fields;
	private companyInfoSubscription: Subscription;
	private moduleId: string;
	public errorParams = {
		body: {},
		title: {},
	};
	public config = config;
	public isLoading = true;
	constructor(
		private formBuilder: FormBuilder,
		private translateService: TranslateService,
		private cd: ChangeDetectorRef,
		private route: ActivatedRoute,
		private router: Router,

		private bannerMessageService: BannerMessageService,
		private htmlTemplatenApiService: HtmlTemplateApiService,
		private cacheService: CacheService
	) {
		this.config['height'] = 550;
		this.translateService.get('BODY').subscribe((res: string) => {
			this.errorParams['body']['field'] = res;
		});

		this.translateService.get('TITLE').subscribe((res: string) => {
			this.errorParams['title']['field'] = res;
		});
	}
	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.pdfId = this.route.snapshot.params['pdfId'];
		const signature = {
			DISPLAY_LABEL: 'Signature',
			NAME: 'SIGNATURE',
		};
		this.companyInfoSubscription =
			this.cacheService.companyInfoSubject.subscribe((dataStored) => {
				if (dataStored) {
					this.modules = this.cacheService.companyData['MODULES'];
					this.module = this.modules.find(
						(data) => data.MODULE_ID === this.moduleId
					);
					this.fields = JSON.parse(JSON.stringify(this.module.FIELDS));
					this.fields.push(signature);
					this.isLoading = false;
				}
			});
		this.pdfForm = this.formBuilder.group({
			TITLE: ['', Validators.required],
			HTML_TEMPLATE: ['', Validators.required],
			MODULE: [this.moduleId],
		});
		if (this.pdfId !== 'new') {
			this.isLoading = true;
			this.pdfForm.addControl('TEMPLATE_ID', new FormControl(''));
			this.htmlTemplatenApiService
				.getTemplateById(this.moduleId, this.pdfId)
				.subscribe(
					(response: any) => {
						this.isLoading = false;
						this.pdfForm.setValue({
							TITLE: response.TITLE,
							HTML_TEMPLATE: response.HTML_TEMPLATE,
							MODULE: response.MODULE,
							TEMPLATE_ID: response.TEMPLATE_ID,
						});
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		}
	}

	public ngOnDestroy() {
		if (this.companyInfoSubscription) {
			this.companyInfoSubscription.unsubscribe();
		}
	}

	// Function to insert dynamic variable to PDF body
	public insertBodyVariable(field) {
		const bodyEnd = tinymce.activeEditor.getContent().split('</body></html>');
		let fieldVar = field.NAME;
		if (field.DISPLAY_LABEL !== 'Signature') {
			if (field.DATA_TYPE.DISPLAY === 'Relationship') {
				const module = this.modules.find(
					(data) => data.MODULE_ID === field.MODULE
				);
				const fields = module.FIELDS;
				let response;
				if (field.PRIMARY_DISPLAY_FIELD) {
					response = fields.find(
						(data) => data.FIELD_ID === field.PRIMARY_DISPLAY_FIELD
					);
					fieldVar = `${fieldVar}.${response.NAME}`;
				} else {
					fieldVar = `${fieldVar}`;
				}
				const newBody = `${bodyEnd[0]} {{inputMessage.${fieldVar}}}</body></html>`;
				tinymce.activeEditor.setContent(newBody);
				this.pdfForm.get('HTML_TEMPLATE').setValue(newBody);
			} else {
				const newBody = `${bodyEnd[0]} {{inputMessage.${fieldVar}}}</body></html>`;
				tinymce.activeEditor.setContent(newBody);
				this.pdfForm.get('HTML_TEMPLATE').setValue(newBody);
			}
		} else {
			const newBody = `${bodyEnd[0]} {{${fieldVar}_REPLACE}}</body></html>`;
			tinymce.activeEditor.setContent(newBody);
			this.pdfForm.get('HTML_TEMPLATE').setValue(newBody);
		}
	}

	public save() {
		this.clickedSave = true;
		if (this.pdfForm.valid) {
			this.isSubmitting = true;
			const pdfObj = JSON.parse(JSON.stringify(this.pdfForm.value));
			if (this.pdfId === 'new') {
				pdfObj.HTML_TEMPLATE = '<html><body>' + pdfObj.HTML_TEMPLATE;
				if (!pdfObj.HTML_TEMPLATE.includes('</body></html>')) {
					pdfObj.HTML_TEMPLATE = pdfObj.HTML_TEMPLATE + '</body></html>';
				}
				this.htmlTemplatenApiService
					.postTemplate(this.moduleId, pdfObj)
					.subscribe(
						(pdfResponse: any) => {
							this.isSubmitting = false;
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SAVED_SUCCESSFULLY'),
							});
							this.router.navigate([`modules/${this.moduleId}/pdf`]);
						},
						(error: any) => {
							this.isSubmitting = false;
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			} else {
				if (!pdfObj.HTML_TEMPLATE.includes('</body></html>')) {
					pdfObj.HTML_TEMPLATE = pdfObj.HTML_TEMPLATE + '</body></html>';
				}
				if (!pdfObj.HTML_TEMPLATE.includes('<html><body>')) {
					pdfObj.HTML_TEMPLATE = '<html><body>' + pdfObj.HTML_TEMPLATE;
				}
				this.htmlTemplatenApiService
					.putTemplate(this.moduleId, pdfObj)
					.subscribe(
						(pdf: any) => {
							this.isSubmitting = false;
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
							});
							this.router.navigate([`modules/${this.moduleId}/pdf`]);
						},
						(error: any) => {
							this.isSubmitting = false;
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			}
		}
	}
}
