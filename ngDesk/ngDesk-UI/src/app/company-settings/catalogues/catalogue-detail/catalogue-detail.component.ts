import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject, Subscriber } from 'rxjs';
import { ServicecatalogueApiService } from '@ngdesk/module-api';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { CatalogueDetailService } from './catalogue-detail.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { CompaniesService } from '@src/app/companies/companies.service';
import { MatChipInputEvent } from '@angular/material/chips';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	switchMap,
} from 'rxjs/operators';

@Component({
	selector: 'app-catalogue-detail',
	templateUrl: './catalogue-detail.component.html',
	styleUrls: ['./catalogue-detail.component.scss'],
})
export class CatalogueDetailComponent implements OnInit {
	public catalogueForm: FormGroup;
	public modules = [];
	public forms: any = [];
	public catalogueId: string;
	public moduleId: string;
	public params;
	public catalogue;
	public myImage;
	public name;
	public description;
	public formData;
	public form;
	public length;
	public companyId: string;
	public visibleTo = [];
	public teamNames = [];
	public teamsModule;
	public teamScrollSubject = new Subject<any>();
	public selectedValues: any[] = [];

	constructor(
		private fb: FormBuilder,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private route: ActivatedRoute,
		private catalogueApiService: ServicecatalogueApiService,
		private router: Router,
		private modulesService: ModulesService,
		public catalogueDetailService: CatalogueDetailService,
		private loaderService: LoaderService,
		public customTableService: CustomTableService,
		private companiesService: CompaniesService
	) {}

	public ngOnInit() {
		this.catalogueId = this.route.snapshot.params['catalogueId'];
		this.catalogueForm = this.fb.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
			visibleTo: [''],
			displayImage: [''],
			catalogueForms: this.fb.array([]),
		});

		this.modulesService.getModuleByName('Teams').subscribe((response: any) => {
			this.teamsModule = response;
			this.catalogueDetailService
				.getTeamsData(0, '', this.teamsModule)
				.subscribe((teamResponse) => {
					this.teamNames = teamResponse['DATA'];
					this.teamsDataScroll();
				});
		});

		this.modulesService.getModules().subscribe((moduleResponse: any) => {
			this.modules = moduleResponse.MODULES.sort((a, b) =>
				a.NAME.localeCompare(b.NAME)
			);
		});

		this.companiesService.getSecurity().subscribe((response: any) => {
			this.companyId = response.COMPANY_ID;
		});

		if (this.catalogueId !== 'new') {
			this.catalogueDetailService.getCatalogue(this.catalogueId).subscribe(
				(catalogueResponse: any) => {
					this.catalogue = catalogueResponse.DATA;
					this.name = this.catalogue.name;
					this.description = this.catalogue.description;
					this.myImage = this.catalogue.displayImage;
					this.catalogue['visibleTo'].forEach((element) => {
						this.visibleTo.push(element._id);
					});
					this.initializeTeam();
					this.catalogueForm.get('visibleTo').setValue(this.visibleTo);
					this.setValueToForm(this.catalogue);
					this.setSelectedValues(this.catalogue);
					this.catalogue.catalogueForms.forEach((element, index) => {
						const sortBy = this.customTableService.sortBy;
						const orderBy = this.customTableService.sortOrder;
						const page = this.customTableService.pageIndex;
						const pageSize = this.customTableService.pageSize;
						this.catalogueDetailService
							.getAllForms(element.moduleId, page, pageSize, sortBy, orderBy)
							.subscribe((formsResponse: any) => {
								this.formData = formsResponse.DATA;
								this.forms[index] = this.formData;
							});
						this.catalogueDetailService
							.getForm(element.moduleId, element.formId.formId)
							.subscribe((response: any) => {
								this.form = response.DATA;
								const catarr = this.catalogueForm.get(
									'catalogueForms'
								) as FormArray;
								catarr.push(
									this.fb.group({
										moduleId: this.form.moduleId,
										formId: this.form.formId,
									})
								);
							});
					});
				},
				(error) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		} else {
			this.addCatalogueForms();
		}
	}
	public initializeTeam() {
		const teamIds = this.visibleTo;
		let teamData;
		const teamObj = [];
		teamIds.forEach((teamId) => {
			if (teamId) {
				this.catalogueDetailService
					.getCatalogue(this.catalogueId)
					.subscribe((res) => {
						const data = res.DATA;
						data['visibleTo'].forEach((element) => {
							if (element._id === teamId) {
								teamData = {
									name: element['NAME'],
									id: element['_id'],
								};
								teamObj.push(teamData);
							}
						});
					});
			}
		});
		this.visibleTo = teamObj;
		const newTeams = [];
		this.teamNames.forEach((team) => {
			const data = teamObj.find((teamData) => teamData.id === team.id);
			if (!data) {
				newTeams.push(team);
			}
		});
		this.teamNames = newTeams;
	}
	public removeTeam(element): void {
		const index = this.visibleTo.indexOf(element);
		if (index >= 0) {
			const data = this.visibleTo;
			data.splice(index, 1);
			this.selectedValues.splice(index, 1);
		}
	}
	public resetInput(event: MatChipInputEvent): void {
		const input = event.input;
		if (input) {
			input.value = '';
		}
	}
	public addTeam(event) {
		this.visibleTo.push(event.option.value);
		const newTeam = [];
		this.teamNames.forEach((team) => {
			if (team.id !== event.option.value.id) {
				newTeam.push(team);
			}
		});
		this.selectedValues.push(event.option.value.id);
		this.teamNames = newTeam;
	}
	private setValueToForm(catalogueObj: any) {
		this.catalogueForm.controls['NAME'].setValue(catalogueObj.name);
		this.catalogueForm.controls['DESCRIPTION'].setValue(
			catalogueObj.description
		);
	}

	public redirectToForms(moduleId, formId) {
		this.router.navigate([`modules/${moduleId}/service-catalogue/${formId}`]);
	}

	get catalogueFormData() {
		return <FormArray>this.catalogueForm.get('catalogueForms');
	}

	public createCatalogueForms(): FormGroup {
		return this.fb.group({
			moduleId: [''],
			formId: [''],
		});
	}
	public addCatalogueForms() {
		const catalogueFromAdding = this.catalogueForm.get(
			'catalogueForms'
		) as FormArray;
		catalogueFromAdding.push(this.createCatalogueForms());
	}

	public removeCatalogueForms(index) {
		const catalogueFormRemove = this.catalogueForm.get(
			'catalogueForms'
		) as FormArray;
		if (catalogueFormRemove.value.length >= 1) {
			catalogueFormRemove.removeAt(index);
			this.forms.splice(index, 1);
		}
	}
	public filterModuleForms(index, field, forms) {
		forms['controls']['formId'].setValue('');
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.catalogueDetailService
			.getAllForms(field.value, page, pageSize, sortBy, orderBy)
			.subscribe((formsResponse: any) => {
				this.formData = formsResponse.DATA;
				this.forms[index] = this.formData;
				this.length = formsResponse.TOTAL_RECORDS;
			});
	}
	createForms(moduleId) {
		this.router.navigate([`modules/${moduleId}/service-catalogue/new`]);
	}
	getName(event) {
		this.name = event;
	}
	getDesc(event) {
		this.description = event;
	}

	onChange($event: Event) {
		const file = ($event.target as HTMLInputElement).files[0];
		this.convertBase(file);
	}

	convertBase(file: File) {
		const imageConvert = new Observable((subscriber: Subscriber<any>) => {
			this.readFile(file, subscriber);
		});
		imageConvert.subscribe((response) => {
			this.myImage = response;
		});
	}

	readFile(file: File, subscriber: Subscriber<any>) {
		const filereader = new FileReader();
		filereader.readAsDataURL(file);
		filereader.onload = () => {
			subscriber.next(filereader.result);
			subscriber.complete();
		};
		filereader.onerror = (error) => {
			subscriber.error(error);
			subscriber.complete();
		};
	}

	save() {
		this.catalogueForm.get('NAME').markAsTouched();
		this.loaderService.isLoading = false;
		if (this.catalogueForm.valid) {
			let catalogueFormArry;
			catalogueFormArry = this.catalogueForm.value.catalogueForms;
			catalogueFormArry.forEach((element) => {
				if (element.catalogueForms) {
					element.catalogueForms.forEach((data) => {
						data.moduleId = data.moduleId.MODULE_ID;
						data.formId = data.formId.FORM_ID;
					});
				}
			});
			const teams = [];
			if (this.visibleTo.length === 0) {
				this.loaderService.isLoading = false;
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('VISIBLE_FIELD_REQUIRED'),
				});
			} else {
				this.visibleTo.forEach((team) => {
					teams.push(team['id']);
				});
				this.visibleTo = teams;

				const cataloguePayload = {
					name: this.catalogueForm.value.NAME,
					description: this.catalogueForm.value.DESCRIPTION,
					displayImage: this.myImage,
					visibleTo: this.visibleTo,
					catalogueForms: catalogueFormArry,
				};

				if (this.catalogueId === 'new') {
					this.catalogueApiService.postCatalogue(cataloguePayload).subscribe(
						(response) => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SAVED_SUCCESSFULLY'),
							});
							this.router.navigate(['company-settings', 'catalogues']);
						},
						(error) => {
							this.loaderService.isLoading = false;
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
				} else {
					cataloguePayload['catalogueId'] = this.catalogueId;
					cataloguePayload['companyId'] = this.companyId;
					this.catalogueApiService.putCatalogue(cataloguePayload).subscribe(
						(response) => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('UPDATED_SUCCESSFULLY'),
							});
							this.router.navigate(['company-settings', 'catalogues']);
						},
						(error) => {
							this.loaderService.isLoading = false;
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
				}
			}
		}
	}

	public teamsDataScroll() {
		this.teamScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let searchValue = '';
					if (value !== '') {
						searchValue = 'NAME' + '=' + value;
					}
					let page = 0;
					if (this.teamNames && !search) {
						page = Math.ceil(this.teamNames.length / 10);
					}
					return this.catalogueDetailService
						.getTeamsData(page, searchValue, this.teamsModule)
						.pipe(
							map((results: any) => {
								if (search) {
									this.teamNames = results['DATA'];
								} else if (results['DATA'].length > 0) {
									this.teamNames = this.teamNames.concat(results['DATA']);
								}
								return results['DATA'];
							})
						);
				})
			)
			.subscribe();
	}
	// When scrolling the dropdown.
	public onScrollTeams() {
		this.teamScrollSubject.next(['', false]);
	}
	// While entering any text to the input start searching.
	public onSearch() {
		const teams = this.catalogueForm.value['visibleTo'];
		if (typeof teams !== 'object') {
			const searchText = teams;
			this.teamScrollSubject.next([searchText, true]);
		}
	}

	public autocompleteClosed() {
		this.teamScrollSubject.next(['', true]);
	}

	public disableSelectedValues(item) {
		if (this.selectedValues.length > 0) {
			if (this.selectedValues.includes(item.id)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public setSelectedValues(catalogue) {
		if (catalogue) {
			catalogue.visibleTo.forEach((data) => {
				this.selectedValues.push(data._id);
			});
		}
	}
}
