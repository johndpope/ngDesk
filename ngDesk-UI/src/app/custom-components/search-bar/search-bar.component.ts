import { COMMA, ENTER } from '@angular/cdk/keycodes';
import {
	Component,
	ElementRef,
	EventEmitter,
	Input,
	OnChanges,
	Output,
	ViewChild,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import {
	MatAutocomplete,
	MatAutocompleteTrigger,
} from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { ActivatedRoute } from '@angular/router';
import { DataApiService } from '@ngdesk/data-api';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { RenderListLayoutService } from '@src/app/render-layout/render-list-layout-new/render-list-layout.service';
import { Subscription } from 'rxjs';
import { ModulesService } from '../../modules/modules.service';
import { BannerMessageService } from '../banner-message/banner-message.service';

// import { AutocompleteService } from '../../render-layout/autocomplete/autocomplete.service';

@Component({
	selector: 'app-search-bar',
	templateUrl: './search-bar.component.html',
	styleUrls: ['./search-bar.component.scss'],
})
export class SearchBarComponent implements OnChanges {
	@Output() public searchFieldsEvent = new EventEmitter<any[]>();
	@Input() public moduleId: string;
	@Input() public isController: boolean;
	// @Input() public savedFields: any[];
	@ViewChild('auto', { static: true }) public matAutocomplete: MatAutocomplete;
	@ViewChild('searchInput', { read: MatAutocompleteTrigger, static: true })
	public matAutocompleteTrigger: MatAutocompleteTrigger;
	@ViewChild('searchInput', { static: true })
	private searchInput: ElementRef<HTMLInputElement>;
	public fields: any[];
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	public searchCtrl = new FormControl();
	public savedFields = [];
	public filteredFields = [];
	private fieldEntries: any = [];
	private allModules: any[] = [];
	private fieldOptions = [];
	public isLoading = false;
	public datePicker = true;
	public formattedDate: any;
	public dateTimeRange: any;
	private searchSubscription: Subscription;
	private companyInfoSubscription: Subscription;
	public controllerFields = ['HOST_NAME', 'STATUS', 'UPDATER_STATUS'];

	public relationshipSort: string[] = [];
	public relationshipField: any;
	public pageNuber = 0;
	public searchBy: string;
	public searchString = '';

	constructor(
		private modulesService: ModulesService,
		private route: ActivatedRoute,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private cacheService: CacheService,
		private dataService: DataApiService,
		private renderListLayoutService: RenderListLayoutService
	) {}
	// constructor(private modulesService: ModulesService, private route: ActivatedRoute,
	// private autocompleteService: AutocompleteService) { }

	public ngOnChanges() {
		this.relationshipField = null;
		// for each time route changes but component reused, make new api calls
		this.route.params.subscribe((params) => {
			// release memory of search subscription to avoid duplicate autocomplete api calls from being made
			if (this.searchSubscription) {
				this.searchSubscription.unsubscribe();
			}

			// unsubscribes company info subject subscription on reload
			if (this.companyInfoSubscription) {
				this.companyInfoSubscription.unsubscribe();
			}

			// This condition for the controller master search.
			if (this.isController) {
				this.controllerFields.forEach((field) => {
					this.fieldOptions.push({
						VALUE: this.translateService.instant(field),
						TYPE: 'field',
						NAME: field,
						COMPONENT: 'controller',
					});
				});
				this.filteredFields = this.fieldOptions;
				this.searchSubscription = this.searchCtrl.valueChanges.subscribe(
					(value) => {
						if (!value || value === '') {
							this.searchString = '';
							this.pageNuber = 0;
							this.searchFieldsEvent.emit([]);
						} else {
							this.onSearchOfRelationshipField(value);
						}
					}
				);
			} else {
				const searchParams =
					localStorage.getItem(`${this.moduleId}_LIST_LAYOUT`) &&
					JSON.parse(localStorage.getItem(`${this.moduleId}_LIST_LAYOUT`))[
						'SEARCH'
					]
						? JSON.parse(localStorage.getItem(`${this.moduleId}_LIST_LAYOUT`))[
								'SEARCH'
						  ]
						: [];
				if (
					searchParams.length > 0 &&
					searchParams[searchParams.length - 1]['TYPE'] !== 'field'
				) {
					this.savedFields = searchParams;
				} else {
					// clear input when navigating to new render list
					this.searchInput.nativeElement.value = '';
					this.savedFields = [];
					this.searchCtrl.reset();
				}

				// use modules saved in cache
				this.companyInfoSubscription =
					this.cacheService.companyInfoSubject.subscribe((dataStored) => {
						if (dataStored) {
							this.allModules = this.cacheService.companyData['MODULES'];
							let moduleId;
							if (!this.moduleId) {
								moduleId = this.route.snapshot.params.moduleId;
							} else {
								moduleId = this.moduleId;
							}

							const allModuleFields = this.allModules.find(
								(module) => module.MODULE_ID === moduleId
							)['FIELDS'];
							// only display certain types of fields in search
							this.fields = allModuleFields.filter(this.isValidSearchField);
							this.detectSearchValueChanges('fields');
						}
					});

				// checks when search control value has changed
				this.searchSubscription = this.searchCtrl.valueChanges.subscribe(
					(value) => {
						if (!value || value === '') {
							this.searchFieldsEvent.emit([]);
							this.searchString = '';
							this.pageNuber = 0;
						} else {
							this.onSearchOfRelationshipField(value);
						}
						let fieldIncluded = false;
						this.savedFields.forEach((field) => {
							if (field['TYPE'] === 'field') {
								fieldIncluded = true;
							}
						});

						//To filter the dropdown list

						if (value && value.length > 2) {
							this.filteredFields = [];
							this.fieldOptions.forEach((item) => {
								if (
									item.VALUE.toLocaleLowerCase().includes(
										value.toLocaleLowerCase()
									)
								) {
									this.filteredFields.push(item);
								}
							});
						}
						// for autocomplete
						// do not allow autocomplete api to be made if other fields are being searched
						if (!fieldIncluded) {
							// this.filteredFields = [];
							if (value && value.length > 2) {
								// TODO: uncomment when autocomplete is ready
								// display loading icon in search when making autocomplete api call
								// loading icon will stop once api returns a response
								// this.isLoading = true;
								// this.autocompleteService.getAutocomplete(this.moduleId, value).subscribe(
								//   (response1: any) => {
								//     response1['SUGGESTIONS'].forEach((suggestion) => {
								//       this.filteredFields.push(
								//         { VALUE: suggestion, TYPE: 'global', NAME: suggestion }
								//       );
								//     });
								//     this.isLoading = false;
								//   }, (error: any) => {
								//     this.isLoading = false;
								//   }
								// );
							} else {
								this.filteredFields = this.fieldOptions;
							}
						}
					}
				);
			}
		});
	}

	private isValidSearchField(field): boolean {
		if (
			field.DATA_TYPE.DISPLAY === 'File Upload' ||
			field.DATA_TYPE.DISPLAY === 'Password'
		) {
			return false;
		} else if (field.DATA_TYPE.DISPLAY === 'Relationship') {
			if (
				field.RELATIONSHIP_TYPE === 'One to Many' ||
				field.RELATIONSHIP_TYPE === 'Many to Many'
			) {
				return false;
			}
		}
		return true;
	}

	// removing field from array
	public removeItem(object: string, index): void {
		if (this.savedFields.length > 0) {
			// if global search param, just remove that one param
			if (index === 0 || object['TYPE'] === 'global') {
				this.savedFields.splice(index, 1);
			} else {
				// for field param with value, remove two items
				this.savedFields.splice(index - 1, 2);
			}
			if (object['COMPONENT'] === 'controller') {
				this.detectSearchValueChanges('controller');
			} else {
				this.detectSearchValueChanges('fields');
			}

			// if last search param has been removed, call removeall function
			if (this.savedFields.length === 0) {
				this.removeAll();
			}
			// emit changes to parent component
			this.searchFieldsEvent.emit(this.savedFields);
		} else {
			this.searchFieldsEvent.emit([]);
		}
	}

	public dateRange(value) {
		if (value.includes(null) || value.length === 0) {
			this.removeAll();
			this.bannerMessageService.errorNotifications.push({
				message: 'Select proper date/time range value',
			});
		} else {
			// tslint:disable-next-line: prefer-const
			const formatDate =
				value[0]._d.getFullYear() +
				'-' +
				('0' + (value[0]._d.getMonth() + 1)).slice(-2) +
				'-' +
				('0' + value[0]._d.getDate()).slice(-2) +
				'T' +
				('0' + value[0]._d.getHours()).slice(-2) +
				':' +
				('0' + value[0]._d.getMinutes()).slice(-2) +
				':' +
				('0' + value[0]._d.getSeconds()).slice(-2) +
				'~' +
				value[1]._d.getFullYear() +
				'-' +
				('0' + (value[1]._d.getMonth() + 1)).slice(-2) +
				'-' +
				('0' + value[1]._d.getDate()).slice(-2) +
				'T' +
				('0' + value[1]._d.getHours()).slice(-2) +
				':' +
				('0' + value[1]._d.getMinutes()).slice(-2) +
				':' +
				('0' + value[1]._d.getSeconds()).slice(-2);
			this.formattedDate =
				new Date(value[0]).toISOString() +
				'~' +
				new Date(value[1]).toISOString();
			const previousField = this.fields.find(
				(field) =>
					field.FIELD_ID === this.savedFields[this.savedFields.length - 1]['ID']
			);
			this.fieldOptions.push({
				VALUE: formatDate,
				TYPE: 'entry',
				NAME: previousField['NAME'],
			});
			this.resetInput(value);
			this.datePicker = true;
		}
	}
	public closePanel() {
		this.matAutocompleteTrigger.closePanel();
		this.dateTimeRange = null;
	}

	public formatDate(value) {
		let abbreviation;
		const monthNamesShort = [
			'Jan',
			'Feb',
			'Mar',
			'Apr',
			'May',
			'Jun',
			'Jul',
			'Aug',
			'Sep',
			'Oct',
			'Nov',
			'Dec',
		];
		let hour;
		if (value.getHours() >= 12) {
			hour = value.getHours() - 12;
			hour = ('0' + hour).slice(-2);
			if (hour === '00') {
				hour = 12;
			}
			abbreviation = 'PM';
		} else {
			hour = value.getHours();
			hour = ('0' + hour).slice(-2);
			abbreviation = 'AM';
		}
		const formatDate =
			monthNamesShort[value.getMonth()] +
			' ' +
			('0' + value.getDate()).slice(-2) +
			', ' +
			value.getFullYear() +
			', ' +
			hour +
			':' +
			('0' + value.getMinutes()).slice(-2) +
			':' +
			('0' + value.getSeconds()).slice(-2) +
			' ' +
			abbreviation;
		return formatDate;
	}

	// re-populates the dropdown data depending on previous selected param
	private detectSearchValueChanges(type, onScroll?: boolean) {
		if (type !== '' && onScroll !== true) {
			this.fieldOptions = [];
		}
		// cast data using one model for both fields and picklist values
		switch (type) {
			case 'fields': {
				this.fields.forEach((field) => {
					if (
						field['DATA_TYPE']['DISPLAY'] !== 'Discussion' &&
						field['DATA_TYPE']['DISPLAY'] !== 'File Upload'
					) {
						this.fieldOptions.push({
							VALUE: field['DISPLAY_LABEL'],
							TYPE: 'field',
							NAME: field['NAME'],
							ID: field['FIELD_ID'],
						});
					}
				});
				break;
			}
			case 'entries': {
				this.fieldEntries.forEach((entry) => {
					const previousField = this.fields.find(
						(field) =>
							field.FIELD_ID ===
							this.savedFields[this.savedFields.length - 1]['ID']
					);
					if (previousField.DATA_TYPE.DISPLAY === 'Relationship') {
						const relatedModuleFields = this.allModules.find(
							(module) => module.MODULE_ID === previousField.MODULE
						)['FIELDS'];

						const relatedField = relatedModuleFields.find(
							(field) => field.FIELD_ID === previousField.PRIMARY_DISPLAY_FIELD
						);
						const relatedFieldName = relatedField['NAME'];
						if (entry['PRIMARY_DISPLAY_FIELD']) {
							this.fieldOptions.push({
								VALUE: entry['PRIMARY_DISPLAY_FIELD'],
								TYPE: 'entry',
								NAME: previousField['NAME'],
								DATA_ID: entry['DATA_ID'],
							});
						}
					} else {
						this.fieldOptions.push({
							VALUE: entry,
							TYPE: 'entry',
							NAME: previousField['NAME'],
						});
					}
				});
				break;
			}
			case 'controller': {
				this.controllerFields.forEach((field) => {
					this.fieldOptions.push({
						VALUE: this.translateService.instant(field),
						TYPE: 'field',
						NAME: field,
						COMPONENT: 'controller',
					});
				});
				break;
			}
		}
		this.filteredFields = this.fieldOptions;
		if (
			this.savedFields.length > 0 &&
			this.savedFields[this.savedFields.length - 1]['TYPE'] === 'field'
		) {
			setTimeout(() => {
				this.matAutocompleteTrigger.openPanel();
			}, 300);
		}
	}

	public openPopup() {
		if (
			(this.savedFields.length > 0 &&
				this.savedFields[this.savedFields.length - 1]['TYPE'] !== 'field') ||
			this.savedFields.length === 0
		) {
			this.matAutocompleteTrigger.openPanel();
		}
	}

	// when clicking on clear button in search
	// clears all params and chips from input
	public removeAll() {
		this.datePicker = false;
		this.savedFields = [];
		this.searchInput.nativeElement.value = '';
		this.searchCtrl.reset();
		this.relationshipField = null;
		this.searchString = '';
		this.pageNuber = 0;
		if (this.isController) {
			this.detectSearchValueChanges('controller');
		} else {
			this.detectSearchValueChanges('fields');
		}
		this.searchFieldsEvent.emit(this.savedFields);
	}

	// Add option only when MatAutocomplete is not open
	// To make sure this does not conflict with OptionSelected Event
	public resetInput(event: MatChipInputEvent): void {
		if (!this.matAutocomplete.isOpen && event.value !== '') {
			// add item to list
			const previousField =
				this.savedFields.length > 0
					? this.savedFields[this.savedFields.length - 1]
					: {};

			if (this.isController) {
				this.fieldSelected({
					VALUE: event.value,
					TYPE: 'entry',
					NAME: previousField['NAME'],
					COMPONENT: 'controller',
				});
			} else {
				if (
					previousField['TYPE'] === 'field' &&
					!(
						previousField['NAME'] === 'DATE_CREATED' ||
						previousField['NAME'] === 'DATE_UPDATED' ||
						previousField['NAME'] === 'DUE_DATE'
					)
				) {
					this.fieldSelected({
						VALUE: event.value,
						TYPE: 'entry',
						NAME: previousField['NAME'],
					});
				} else if (
					previousField['NAME'] === 'DATE_CREATED' ||
					previousField['NAME'] === 'DATE_UPDATED' ||
					previousField['NAME'] === 'DUE_DATE'
				) {
					this.fieldSelected({
						VALUE: this.formattedDate,
						TYPE: 'entry',
						NAME: previousField['NAME'],
					});
				} else {
					this.searchFieldsEvent.emit([
						{ VALUE: event.value, TYPE: 'global', NAME: event.value },
					]);
				}
			}
		}
	}

	public displayFn(field?: any): string | undefined {
		return field
			? field.hasOwnProperty('FIELD_ID')
				? field.DISPLAY_LABEL
				: field.DATA_ID
			: undefined;
	}

	public isDisabled(field): boolean {
		if (this.isController) {
			if (field['TYPE'] === 'field') {
				return field['NAME']
					? this.savedFields.find(
							(matchedField) => matchedField['NAME'] === field['NAME']
					  )
						? true
						: false
					: false;
			} else {
				return false;
			}
		} else {
			return field['ID']
				? this.savedFields.find(
						(matchedField) => matchedField['ID'] === field['ID']
				  )
					? true
					: false
				: false;
		}
	}

	public fieldSelected(option) {
		if (
			option.NAME === 'DATE_CREATED' ||
			option.NAME === 'DATE_UPDATED' ||
			option.NAME === 'DUE_DATE'
		) {
			this.datePicker = false;
		} else {
			this.datePicker = true;
		}
		this.savedFields.push(option);
		this.searchInput.nativeElement.value = '';
		this.searchFieldsEvent.emit(this.savedFields);
		this.filteredFields = [];

		if (this.isController) {
			if (option['TYPE'] === 'field') {
				if (
					option['NAME'] === 'STATUS' ||
					option['NAME'] === 'UPDATER_STATUS'
				) {
					this.fieldOptions = [];
					this.fieldOptions.push({
						VALUE: this.translateService.instant('OFFLINE'),
						TYPE: 'entry',
						NAME: 'OFFLINE',
						COMPONENT: 'controller',
					});
					this.fieldOptions.push({
						VALUE: this.translateService.instant('ONLINE'),
						TYPE: 'entry',
						NAME: 'ONLINE',
						COMPONENT: 'controller',
					});
					this.detectSearchValueChanges('');
				} else {
					this.fieldEntries = [];
				}
			} else {
				this.fieldEntries = [];
				this.detectSearchValueChanges('controller');
				this.matAutocompleteTrigger.closePanel();
			}
		} else {
			if (option['TYPE'] === 'field') {
				const selectedField = this.fields.find(
					(field) => field['FIELD_ID'] === option['ID']
				);
				// only display list of picklist values in dropdwon when field is selected previously
				// else, dont show dropdown to allow user to enter values
				if (selectedField['DATA_TYPE']['DISPLAY'] === 'Picklist') {
					this.fieldEntries = selectedField['PICKLIST_VALUES'];
					this.detectSearchValueChanges('entries');
				} else if (selectedField['DATA_TYPE']['DISPLAY'] === 'Relationship') {
					const primaryDisplayField = this.fields.find(
						(field) => field.FIELD_ID === selectedField['PRIMARY_DISPLAY_FIELD']
					);
					//To get related module related fields
					const relatedModuleFields = this.allModules.find(
						(module) => module.MODULE_ID === selectedField.MODULE
					)['FIELDS'];

					// TO get related fields's primary display field
					const relatedField = relatedModuleFields.find(
						(field) => field.FIELD_ID === selectedField['PRIMARY_DISPLAY_FIELD']
					);
					if (relatedField) {
						this.searchBy = relatedField['NAME'];
					} else {
						this.searchBy = null;
					}
					this.relationshipField = selectedField;
					this.pageNuber = 0;
					let primaryDisplayFieldName = 'DATE_CREATED';
					if (primaryDisplayField) {
						primaryDisplayFieldName = primaryDisplayField['NAME'];
					}
					this.relationshipSort = [primaryDisplayFieldName, 'asc'];
					this.getRelationshipData(
						selectedField,
						this.relationshipSort,
						false,
						this.searchString
					);
				} else {
					this.fieldEntries = [];
				}
			} else {
				this.fieldEntries = [];
				this.detectSearchValueChanges('fields');
				this.matAutocompleteTrigger.closePanel();
				this.relationshipField
					? (this.relationshipField = null)
					: (this.relationshipField = null);
			}
		}
	}

	public getRelationshipData(
		selectedField: any,
		sort: any,
		isOnScroll?: boolean,
		search?: string
	) {
		let searchString = '';
		if (search && search !== '' && this.searchBy) {
			searchString = this.searchBy + '=' + search;
		}

		const relatedModule = this.allModules.find(
			(module) => module.MODULE_ID === selectedField.MODULE
		);

		const primaryDisplayField = relatedModule.FIELDS.find(
			(field) => field.FIELD_ID === selectedField.PRIMARY_DISPLAY_FIELD
		);

		if (primaryDisplayField) {
			this.searchBy = primaryDisplayField['NAME'];
		}
		let query = `{
			DATA: get${relatedModule.NAME} (moduleId: "${relatedModule.MODULE_ID}", pageNumber: ${this.pageNuber}, pageSize: 10,layoutId: null,sortBy: "${sort[0]}",orderBy:  "${sort[1]}") {
					DATA_ID: _id
					PRIMARY_DISPLAY_FIELD: ${primaryDisplayField.NAME}
			}
		}`;
		if (searchString && searchString !== '') {
			let searchString = this.searchBy + '=' + search;
			query = `{
				DATA: get${relatedModule.NAME} (moduleId: "${relatedModule.MODULE_ID}", pageNumber: ${this.pageNuber}, pageSize: 10,layoutId: null, search: "${searchString}",sortBy: "${sort[0]}",orderBy:  "${sort[1]}") {
						DATA_ID: _id
						PRIMARY_DISPLAY_FIELD: ${primaryDisplayField.NAME}
				}
			}`;
		}

		this.renderListLayoutService.getListLayoutEntries(query).subscribe(
			(entriesResponse: any) => {
				this.fieldEntries = entriesResponse.DATA;
				this.detectSearchValueChanges('entries', isOnScroll);
			},
			(error: any) => {
				console.log(error);
			}
		);
	}

	onScrollOfSearch() {
		if (
			this.relationshipField &&
			this.relationshipField['DATA_TYPE']['DISPLAY'] === 'Relationship'
		) {
			this.pageNuber = this.pageNuber + 1;
			this.getRelationshipData(
				this.relationshipField,
				this.relationshipSort,
				true,
				this.searchString
			);
		}
	}

	onSearchOfRelationshipField(value) {
		if (
			this.relationshipField &&
			this.relationshipField['DATA_TYPE']['DISPLAY'] === 'Relationship' &&
			(value.length > 2 || value == '')
		) {
			this.searchString = value;
			this.pageNuber = 0;
			this.getRelationshipData(
				this.relationshipField,
				this.relationshipSort,
				false,
				this.searchString
			);
		}
	}
}
