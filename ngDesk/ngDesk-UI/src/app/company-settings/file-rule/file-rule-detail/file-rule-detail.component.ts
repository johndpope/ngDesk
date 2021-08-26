import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RuleApiService, SamFileRule } from '@ngdesk/sam-api';
import { TranslateService } from '@ngx-translate/core';
import { CompanySettingService } from '@src/app/company-settings/company-settings.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';

@Component({
	selector: 'app-file-rule-detail',
	templateUrl: './file-rule-detail.component.html',
	styleUrls: ['./file-rule-detail.component.scss'],
})
export class FileRuleDetailComponent implements OnInit {
		public ruleId: string;
		public allSamRules = [];
		public samFileRuleForm: FormGroup;
		public params;
		public errorMessage = '';
		public samFileRule: SamFileRule = {} as SamFileRule;

		constructor(
			private formBuilder: FormBuilder,
			private ruleApiService: RuleApiService,
			private router: Router,
			private route: ActivatedRoute,
			private companySettingService: CompanySettingService,
			private bannerMessageService: BannerMessageService,
			private translateService: TranslateService,
			private loaderService: LoaderService,
			private http: HttpClient,
			private globals: AppGlobals
		) {}

	public ngOnInit() {
				this.initializeRuleForm();
				// Store all the rules to a variable.
				this.allSamRules = this.companySettingService.getSamRules();
				// Get the rule id from the url.
				this.ruleId = this.route.snapshot.params['ruleId'];
				// If id present then fetch the sam file rule.
				if (this.ruleId !== 'new') {
					this.getSamRuleBasedOnId(this.ruleId);
				}
	}

		// This form will initialize the rule form.
		public initializeRuleForm() {
			this.samFileRuleForm = this.formBuilder.group({
				FILE_NAME: [''],
				FILE_PATH: [''],
				VERSION: [''],
				PUBLISHER: [''],
				EDITION: [''],
				SOFTWARE_NAME: ['', [Validators.required]],
				RULE_CONDITION: ['', [Validators.required]],
			});
			this.params = {
				filePath: { field: this.translateService.instant('FILE_PATH') },
				fileName: { field: this.translateService.instant('FILE_NAME') },
				ruleCondition: { field: this.translateService.instant('RULE_CONDITION') },
			};
		}

		// Fetch the same file rule based on the id and set the properties of form.
		public getSamRuleBasedOnId(ruleId) {
			let query = `{
				getSamFileRule(id: "${ruleId}") {
					id
		        	fileName
        			filePath
        			ruleCondition
        			hash
        			companyId
        			version
        			publisher
        			edition
        			softwareName
        			dateCreated
        			dateUpdated
        			createdBy
        			lastUpdatedBy
				}
			}`;
			this.http.post(
				`${this.globals.graphqlUrl}`,
				query
			).subscribe((samFileResponse: any)=>{
				this.samFileRule = samFileResponse.getSamFileRule;
				this.samFileRuleForm.controls['FILE_NAME'].setValue(
					this.samFileRule.fileName
				);
				this.samFileRuleForm.controls['FILE_PATH'].setValue(
					this.samFileRule.filePath
				);
				this.samFileRuleForm.controls['SOFTWARE_NAME'].setValue(
					this.samFileRule.softwareName
				);
				this.samFileRuleForm.controls['VERSION'].setValue(
					this.samFileRule.version
				);
				this.samFileRuleForm.controls['PUBLISHER'].setValue(
					this.samFileRule.publisher
				);
				this.samFileRuleForm.controls['EDITION'].setValue(
					this.samFileRule.edition
				);
				const selectedRule = this.allSamRules.find(
					(rule) => rule['BACKEND'] === this.samFileRule.ruleCondition
				);
				this.samFileRuleForm.controls['RULE_CONDITION'].setValue(selectedRule);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
					});
				}
			);
		}

		// This will make an API call to save the file rule if id is new.
		// If the id is already present will make a put call.
		public save() {
			this.errorMessage = '';
			this.loaderService.isLoading = true;
			this.samFileRule.fileName = this.samFileRuleForm.value['FILE_NAME'];
			this.samFileRule.filePath = this.samFileRuleForm.value['FILE_PATH'];
			this.samFileRule.softwareName = this.samFileRuleForm.value['SOFTWARE_NAME']
			this.samFileRule.ruleCondition = this.samFileRuleForm.value['RULE_CONDITION'].BACKEND;
			this.samFileRule.version = this.samFileRuleForm.value['VERSION'];
			this.samFileRule.publisher = this.samFileRuleForm.value['PUBLISHER'];
			this.samFileRule.edition = this.samFileRuleForm.value['EDITION'];
			const samFileRuleObj: SamFileRule = JSON.parse(
				JSON.stringify(this.samFileRule)
			);
			this.errorMessage = this.validate();
			if (this.errorMessage === '') {
				if (this.ruleId === 'new') {
					// If id is new do a post call.
					this.ruleApiService.postSamFileRule(samFileRuleObj).subscribe(
						(samFileResponse: any) => {
							if (samFileResponse) {
								this.router.navigate([`company-settings/file-rules`]);
								this.bannerMessageService.successNotifications.push({
									message: this.translateService.instant('SAVED_SUCCESSFULLY'),
								});
							}
						},
						(error: any) => {
							this.errorMessage = error.error.ERROR;
						}
					);
				} else {
					// put call if the id already present or not new.
					this.ruleApiService.putSamFileRule(samFileRuleObj).subscribe(
						(samFileResponse: any) => {
							if (samFileResponse) {
								this.router.navigate([`company-settings/file-rules`]);
								this.bannerMessageService.successNotifications.push({
									message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
								});
							}
						},
						(error: any) => {
							this.errorMessage = error.error.ERROR;
						}
					);
				}
			} else {
				this.loaderService.isLoading = false;
			}
		}

		// validate the File name and path based on condition.
		public radioChange() {
			const condition = this.samFileRuleForm.value['RULE_CONDITION'];
			switch (condition.BACKEND) {
				case 'Filename, File Path and Hash':
					this.samFileRuleForm
						.get('FILE_NAME')
						.setValidators([Validators.required]);
					this.samFileRuleForm
						.get('FILE_PATH')
						.setValidators([Validators.required]);

					break;
				case 'Filename, Hash':
					this.samFileRuleForm
						.get('FILE_NAME')
						.setValidators([Validators.required]);

					break;
				case 'Filename':
					this.samFileRuleForm
						.get('FILE_NAME')
						.setValidators([Validators.required]);

					break;
				case 'Filename, File Path':
					this.samFileRuleForm
						.get('FILE_NAME')
						.setValidators([Validators.required]);
					this.samFileRuleForm
						.get('FILE_PATH')
						.setValidators([Validators.required]);

					break;
				case 'Hash':
					this.samFileRuleForm
						.get('FILE_NAME')
						.setValidators([Validators.required]);
					this.samFileRuleForm
						.get('FILE_PATH')
						.setValidators([Validators.required]);
					break;
			}
		}

		// Validate the form while saving based on different conditions chosen.
		public validate() {
			let message = '';
			const condition = this.samFileRuleForm.value['RULE_CONDITION'];
			switch (condition.BACKEND) {
				case 'Filename, File Path and Hash':
					if (
						!this.samFileRule.fileName ||
						this.samFileRule.fileName === '' ||
						!this.samFileRule.filePath ||
						this.samFileRule.filePath === ''
					) {
						message = this.getFieldErrorMessage(
							'FILE_NAME_AND_FILE_PATH',
							'SHOULD_MATCH'
						);
					}
					break;
				case 'Filename, Hash':
					if (!this.samFileRule.fileName || this.samFileRule.fileName === '') {
						message = this.getFieldErrorMessage('FILE_NAME', 'FIELD_IS_REQUIRED');
					}

					break;
				case 'Filename':
					if (!this.samFileRule.fileName || this.samFileRule.fileName === '') {
						message = this.getFieldErrorMessage('FILE_NAME', 'FIELD_IS_REQUIRED');
					}

					break;
				case 'Filename, File Path':
					if (
						!this.samFileRule.fileName ||
						this.samFileRule.fileName === '' ||
						!this.samFileRule.filePath ||
						this.samFileRule.filePath === ''
					) {
						message = this.getFieldErrorMessage(
							'FILE_NAME_AND_FILE_PATH',
							'SHOULD_MATCH'
						);
					}
					break;
				case 'Hash':
					if (
						!this.samFileRule.fileName ||
						this.samFileRule.fileName === '' ||
						!this.samFileRule.filePath ||
						this.samFileRule.filePath === ''
					) {
						message = this.getFieldErrorMessage(
							'FILE_NAME_AND_FILE_PATH',
							'SHOULD_MATCH'
						);
					}
					break;
			}
			return message;
		}

		// Get the formatted error messages
		public getFieldErrorMessage(field, error) {
			let message = '';
			const parameters = {
				field: this.translateService.instant(field),
			};
			this.translateService.get(error, parameters).subscribe((res: string) => {
				message += res;
			});
			return message;
		}

}
