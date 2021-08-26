import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatStepper } from '@angular/material/stepper';
import { TranslateService } from '@ngx-translate/core';

import { CacheService } from '@src/app/cache.service';
import { CompaniesService } from '../../companies/companies.service';

@Component({
	selector: 'app-signup-questions-dialog',
	templateUrl: './signup-questions-dialog.component.html',
	styleUrls: ['./signup-questions-dialog.component.scss']
})
export class SignupQuestionsDialogComponent implements OnInit {
	@ViewChild('stepper', {
		static: true
	})
	private stepper: MatStepper;
	public englishTranslation: any;
	public isLinear = true;
	public questionsForm: FormGroup;
	public ticketsOrPagerForm: FormGroup;
	public companySizeForm: FormGroup;
	public industryTypeForm: FormGroup;
	public departmentForm: FormGroup;
	public interestSolutionForm: FormGroup;
	public gettignStartedForm: FormGroup;
	public isTicket: boolean;
	public platformForm: FormGroup;
	public errorMessage = '';
	public companySizes = [
		'1_TO_45',
		'46_TO_200',
		'201_TO_1000',
		'1001_TO_4500',
		'4501_TO_10000',
		'10000_PLUS'
	];
	public industryTypes = [
		'ACCOUNTING',
		'AGRICULTURE',
		'AIRLINES',
		'APPAREL_FASHION',
		'ARCHITECTURE',
		'ARTS_CRAFTS',
		'AUTOMOBILE',
		'BANKING',
		'CHEMICALS',
		'DAIRY',
		'EDUCATION',
		'ENGINEERING',
		'ENTERTAINMENT',
		'FOOD',
		'HEALTH_FITNESS',
		'MEDIA',
		'MEDICAL',
		'NETWORKING',
		'TECHNOLOGY',
		'OTHER'
	];
	constructor(
		@Inject(MAT_DIALOG_DATA) public data: any,
		public dialogRef: MatDialogRef<SignupQuestionsDialogComponent>,
		private formBuilder: FormBuilder,
		private companiesService: CompaniesService,
		public translateService: TranslateService,
		private cacheService: CacheService
	) {}

	public ngOnInit() {
		this.stepper.selectedIndex = this.getcurrentStep(this.data.questionCount);
		this.industryTypeForm = this.formBuilder.group({
			INDUSTRY: ['', Validators.required]
		});

		this.companySizeForm = this.formBuilder.group({
			COMPANY_SIZE: ['', Validators.required]
		});

		this.ticketsOrPagerForm = this.formBuilder.group(
			{
				TICKETS: [false],
				PAGER: [false],
				CHAT: [false]
			},
			{
				validator: this.validateTicketsOrPager
			}
		);
	}

	public setIndustryType() {
		const industryInEnglish = this.translateService.translations['en'][
			this.industryTypeForm.value.INDUSTRY
		];
		this.companiesService.postIndustryQuestion(industryInEnglish).subscribe(
			(response: any) => {
				this.errorMessage = '';
				this.cacheService.companyData['COMPANY_QUESTION_COUNT'] = response;
				this.stepper.next();
			},
			(errorResponse: any) => {
				this.errorMessage = errorResponse.error.ERROR;
			}
		);
	}

	public setCompanySize() {
		const sizeInEnglish = this.translateService.translations['en'][
			this.companySizeForm.value.COMPANY_SIZE
		];

		this.companiesService.postCompanySizeQuestion(sizeInEnglish).subscribe(
			(response: any) => {
				this.errorMessage = '';
				this.cacheService.companyData['COMPANY_QUESTION_COUNT'] = response;
				this.stepper.next();
			},
			(errorResponse: any) => {
				this.errorMessage = errorResponse.error.ERROR;
			}
		);
	}

	public setUsage() {
		this.companiesService.postUsage(this.ticketsOrPagerForm.value).subscribe(
			(response: any) => {
				this.errorMessage = '';
				this.cacheService.companyData['COMPANY_QUESTION_COUNT'] = response;
				this.dialogRef.close();
			},
			(errorResponse: any) => {
				this.errorMessage = errorResponse.error.ERROR;
			}
		);
	}

	private validateTicketsOrPager(group: FormGroup) {
		if (
			!group.controls.TICKETS.value &&
			!group.controls.PAGER.value &&
			!group.controls.CHAT.value
		) {
			return {
				notValid: true
			};
		} else {
			return null;
		}
	}

	public getcurrentStep(count) {
		switch (count) {
			case 1: {
				return 0;
				break;
			}
			case 2: {
				return 1;
				break;
			}
			default: {
				return 2;
				break;
			}
		}
	}
}
