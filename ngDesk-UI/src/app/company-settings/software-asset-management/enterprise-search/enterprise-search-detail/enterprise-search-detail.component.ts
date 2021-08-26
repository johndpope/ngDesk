import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MatChipInputEvent } from '@angular/material/chips';
import { WebsocketService } from '@src/app/websocket.service';
import { EnterpriseSearchApiService, EnterpriseSearch } from '@ngdesk/sam-api';
import { EnterpriseSearchService } from '@src/app/company-settings/software-asset-management/enterprise-search/enterprise-search.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';

@Component({
	selector: 'app-enterprise-search-detail',
	templateUrl: './enterprise-search-detail.component.html',
	styleUrls: ['./enterprise-search-detail.component.scss'],
})
export class EnterpriseSearchDetailComponent implements OnInit {
    public tags = ["Credit Card",
    "Healthcare",
    "Ssn",
    "Address",
    "Name",
    "Postal Code"
    ];
	public enterpriseSearchForm: FormGroup;
	public enterpriseSearchId;
	public params;
	public enterpriseSearch: EnterpriseSearch = {
		name: '',
		description: '',
		tags: [],
		filePath: '',
		regex: '',
	} as EnterpriseSearch;

	constructor(
		private formBuilder: FormBuilder,
		private translateService: TranslateService,
        private route: ActivatedRoute,
		private websocketService: WebsocketService,
		private enterpriseSearchService: EnterpriseSearchService,
		private enterpriseSearchApiService: EnterpriseSearchApiService,
		private router: Router,
		private bannerMessageService: BannerMessageService,
	) {}

	public ngOnInit() {
        this.enterpriseSearchId = this.route.snapshot.params['id'];
		this.initializeForm();
		if (this.enterpriseSearchId !== 'new') {
			this.getEnterpriseSearch();
		}
	}

	// This function initializes the form.
	public initializeForm() {
		this.enterpriseSearchForm = this.formBuilder.group({
			TAGS: ['', [Validators.required]],
			FILE_PATH: ['', [Validators.required]],
			REGEX: ['', [Validators.required]],
		});
		this.params = {
            TAGS: { field: this.translateService.instant('TAGS') },
            FILE_PATH: { field: this.translateService.instant('FILE_PATH') },
            REGEX: { field: this.translateService.instant('REGEX') },
		};
	}

	public getEnterpriseSearch(): void {
		this.enterpriseSearchService
		.getEnterpriseSearch(this.enterpriseSearchId)
		.subscribe((enterpriseSearchpResponse) => {
			this.enterpriseSearch = enterpriseSearchpResponse['enterpriseSearch'];
			this.initializeTags();
			this.enterpriseSearchForm.controls['NAME'].setValue(
				enterpriseSearchpResponse['enterpriseSearch'].name
			);
			this.enterpriseSearchForm.controls['DESCRIPTION'].setValue(
				enterpriseSearchpResponse['enterpriseSearch'].description
			);
			this.enterpriseSearchForm.controls['FILE_PATH'].setValue(
				enterpriseSearchpResponse['enterpriseSearch'].filePath
			);
			this.enterpriseSearchForm.controls['REGEX'].setValue(
				enterpriseSearchpResponse['enterpriseSearch'].regex
			);
		});
	}
	
	public initializeTags() {
		const enterpriseSearchTags = this.enterpriseSearch.tags;
		const newTags = [];
		this.tags.forEach(dropDownTag => {
			const data = enterpriseSearchTags.find((tag) => tag === dropDownTag);
			if(!data) {
				newTags.push(dropDownTag);
			}
		});
		this.tags = newTags;
	}
    
    public removeTags(element): void {
		const index = this.enterpriseSearch.tags.indexOf(element);
		if (index >= 0) {
			const array = this.enterpriseSearch.tags;
			array.splice(index, 1);
		}
		this.tags.push(element);
	}

	public resetInput(event: MatChipInputEvent): void {
		const input = event.input;
		if (input) {
			input.value = '';
		}
	}

	public addTags(event) {
		this.enterpriseSearch.tags.push(event.option.value);
		const newTags = []; 
		this.tags.forEach(tag => {
			if(tag !== event.option.value) {
				newTags.push(tag);
			}
		});
		this.tags = newTags;
    }

	// This method save and update the discovery map entry.
	public save() {
		this.enterpriseSearch.name = this.enterpriseSearchForm.value['NAME'];
		this.enterpriseSearch.description = this.enterpriseSearchForm.value['DESCRIPTION'];
		const tags = [];
		this.enterpriseSearch.tags.forEach(tag => {
			tags.push(tag);
		});
		this.enterpriseSearch.tags = tags;
		this.enterpriseSearch.filePath = this.enterpriseSearchForm.value['FILE_PATH'];
		this.enterpriseSearch.regex = this.enterpriseSearchForm.value['REGEX'];
		if (this.enterpriseSearchId === 'new') {
			// If id is new do a post call.
			this.enterpriseSearchApiService.postEnterpriseSearch(this.enterpriseSearch).subscribe(
				(enterpriseSearchResponse: any) => {
					if (enterpriseSearchResponse) {
						this.router.navigate([`company-settings/enterprise-search`]);
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('SAVED_SUCCESSFULLY'),
						});
					}
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		} else {
			// If id is not new do a put call.
			this.enterpriseSearchApiService.putEnterpriseSearch(this.enterpriseSearch).subscribe(
				(enterpriseSearchResponse: any) => {
					if (enterpriseSearchResponse) {
						this.router.navigate([`company-settings/enterprise-search`]);
						this.bannerMessageService.successNotifications.push({
							message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
						});
					}
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		}
	}
}
