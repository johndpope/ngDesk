import {
	Component,
	EventEmitter,
	Input,
	OnDestroy,
	OnInit,
	Output,
	TemplateRef,
	ViewChild,
} from '@angular/core';
import { MatMenuTrigger } from '@angular/material/menu';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import {
	ActivatedRoute,
	NavigationEnd,
	NavigationStart,
	Router,
} from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CacheService } from '@src/app/cache.service';
import { Subscription, interval } from 'rxjs';
import { ModulesService } from '../modules/modules.service';
import { CustomModulesService } from '../render-layout/render-detail-new/custom-modules.service';
// import { interval } from 'rxjs';
import { CustomTableService } from './custom-table.service';
@Component({
	selector: 'app-custom-table',
	templateUrl: './custom-table.component.html',
	styleUrls: ['./custom-table.component.scss'],
})
export class CustomTableComponent implements OnInit, OnDestroy {
	@ViewChild(MatSort, { static: true }) private sort: MatSort;
	@ViewChild(MatPaginator, { static: true }) private paginator: MatPaginator;
	@ViewChild(MatMenuTrigger)
	private menuItem: MatMenuTrigger;
	@Input() public templateRef: TemplateRef<any>;
	@Input() public noRecordsMessage: string;
	@Input() public actions = [];
	@Output() public timer = new EventEmitter<any>();
	@Output() public rowClickedEvent = new EventEmitter<any>();
	@Output() public sortData = new EventEmitter<any>();
	@Output() public pageChangeEmit = new EventEmitter<any>();
	@Output() public menuItemTrigger = new EventEmitter<any>();
	@Input() public slaActions = [];
	@Input() public hideAction = false;

	public param;
	public notFoundMessage = '';
	public urls = [];
	public moduleId = '';
	public moduleMap = new Map<String, String>();
	public firstLoad = false;
	private navigationSubscription: Subscription;
	private companyInfoSubscription: Subscription;
	private isModalTable = false;

	public arrayTest = [];
	constructor(
		public customTableService: CustomTableService,
		private translateService: TranslateService,
		private route: ActivatedRoute,
		private router: Router,
		private modulesService: ModulesService,
		public customModulesService: CustomModulesService,
		private cacheService: CacheService
	) {
		this.translateService
			.getTranslation(this.translateService.store.currentLang)
			.subscribe((key) => {
				const rangeLabel = (page: number, pageSize: number, length: number) => {
					if (length === 0 || pageSize === 0) {
						return `0 ${key.TABLE_RANGE_LABEL} ${length}`;
					}

					length = Math.max(length, 0);
					const startIndex = page * pageSize;
					// If the start index exceeds the list length, do not try and fix the end index to the end.
					const endIndex =
						startIndex < length
							? Math.min(startIndex + pageSize, length)
							: startIndex + pageSize;
					return `${startIndex + 1} - ${endIndex} ${
						key.TABLE_RANGE_LABEL
					} ${length}`;
				};

				this.paginator._intl.itemsPerPageLabel = key.ITEMS_PER_PAGE;
				this.paginator._intl.lastPageLabel = key.LAST_PAGE;
				this.paginator._intl.nextPageLabel = key.NEXT_PAGE;
				this.paginator._intl.previousPageLabel = key.PREVIOUS_PAGE;
				this.paginator._intl.firstPageLabel = key.FIRST_PAGE_LABEL;
				this.paginator._intl.firstPageLabel = key.FIRST_PAGE_LABEL;
				this.paginator._intl.getRangeLabel = rangeLabel;
			});
		this.navigationSubscription = this.router.events.subscribe((e: any) => {
			// If it is a NavigationEnd event re-initalise the component
			if (e instanceof NavigationEnd) {
				this.notFoundMessageSet();
			}
			if (e instanceof NavigationStart && this.menuItem) {
				this.menuItem.closeMenu();
			}
		});
	}

	public ngOnInit() {
		const url = this.router.url;
		if (url.includes('/edit/') || url.includes('/create/')) {
			this.isModalTable = true;
		}
		interval(60000).subscribe((x) => {
			this.timer.emit();
		});
		this.customTableService.paginator = this.paginator;
		this.customTableService.sort = this.sort;
		this.notFoundMessageSet();
	}

	public ngOnDestroy() {
		this.customTableService.isLoading = true;

		if (this.navigationSubscription) {
			this.navigationSubscription.unsubscribe();
		}

		if (this.companyInfoSubscription) {
			this.companyInfoSubscription.unsubscribe();
		}
	}

	public sortTableData(event) {
		this.customTableService.columnsHeadersObj.forEach((item) => {
			if (item.DISPLAY === event.active) {
				this.customTableService.sortBy = item.NAME;
			}
		});
		this.customTableService.sortOrder = event.direction;

		this.sortData.emit(event);
	}

	public pageChanged(event) {
		this.customTableService.pageIndex = event.pageIndex;
		this.customTableService.pageSize = event.pageSize;
		this.pageChangeEmit.emit(event);
	}

	public rowHovered(element, action) {
		if (action === 'enter') {
			element.parentElement.style.background = '#f4f4f4';
		} else {
			element.parentElement.style.background = 'white';
		}
	}

	public menuItemClicked(action, row) {
		this.menuItemTrigger.emit({ ACTION: action, ROW_DATA: row });
	}

	public rowClicked(row): void {
		this.rowClickedEvent.emit(row);
	}

	public setSlaActions(row) {
		if (row.hasOwnProperty('slaId')) {
			if (row.deleted) {
				this.actions = [this.slaActions[0]];
			} else {
				this.actions = [this.slaActions[1]];
			}
		}
	}

	public notFoundMessageSet() {
		this.urls = [];
		this.route.snapshot.url.forEach((element) => {
			this.urls.push(element.path);
		});
		if (this.urls.includes('controllers') && this.urls.length <= 2) {
			this.notFoundMessage = 'NO_AGENTS_FOUND_MESSAGE';
		} else if (this.urls[0] === undefined) {
			if (this.router.url.includes('schedules')) {
				this.notFoundMessage = 'NO_SCHEDULES_FOUND_MESSAGE';
			} else if (this.router.url.includes('reports')) {
				this.notFoundMessage = 'NO_REPORTS_FOUND_MESSAGE';
			}
		} else if (this.urls.includes('escalations') && this.urls.length <= 2) {
			this.notFoundMessage = 'NO_ESCALATIONS_FOUND_MESSAGE';
		} else if (this.urls.includes('role-layouts') && this.urls.length <= 2) {
			this.notFoundMessage = 'NO_ACTION_ITEMS_FOUND_MESSAGE';
		} else {
			if (!this.firstLoad) {
				// use modules saved in cache
				this.companyInfoSubscription =
					this.cacheService.companyInfoSubject.subscribe((dataStored) => {
						if (dataStored) {
							const modules: any[] = this.cacheService.companyData['MODULES'];
							this.firstLoad = true;
							modules.forEach((element) => {
								this.moduleMap.set(element.MODULE_ID, element.NAME);
							});
							this.moduleId = this.urls[0];
							this.urls.push(this.moduleMap.get(this.moduleId));
							if (this.urls.includes('Chat')) {
								if (this.urls.length <= 2) {
									this.notFoundMessage = 'NO_CHATS_FOUND_MESSAGE';
								} else {
									this.notFoundMessage = 'NO_ENTRIES_FOUND';
								}
							} else if (this.urls.includes('Tickets')) {
								if (this.urls.length <= 2) {
									this.notFoundMessage = 'NO_TICKETS_FOUND_MESSAGE';
								} else {
									this.notFoundMessage = 'NO_ENTRIES_FOUND';
								}
							} else {
								if (this.urls.length <= 2 && !this.urls.includes('Users')) {
									this.notFoundMessage = 'NO_CUSTOM_MODULE_FOUND_MESSAGE';
								} else if (
									this.urls.length <= 2 &&
									this.urls.includes('Users')
								) {
									this.notFoundMessage = 'NO_USER_FOUND';
								} else if (this.urls.includes('email-lists')) {
									this.notFoundMessage = 'NO_EMAIL_LISTS';
								} else {
									this.notFoundMessage = 'NO_ENTRIES_FOUND';
								}
							}
						}
					});
			} else {
				this.moduleId = this.urls[0];
				this.urls.push(this.moduleMap.get(this.moduleId));
				if (this.urls.includes('Chat')) {
					if (this.urls.length <= 2) {
						this.notFoundMessage = 'NO_CHATS_FOUND_MESSAGE';
					} else {
						this.notFoundMessage = 'NO_ENTRIES_FOUND';
					}
				} else if (this.urls.includes('Tickets')) {
					if (this.urls.length <= 2) {
						this.notFoundMessage = 'NO_TICKETS_FOUND_MESSAGE';
					} else {
						this.notFoundMessage = 'NO_ENTRIES_FOUND';
					}
				} else {
					if (this.urls.length <= 2) {
						this.notFoundMessage = 'NO_CUSTOM_MODULE_FOUND_MESSAGE';
					} else {
						this.notFoundMessage = 'NO_ENTRIES_FOUND';
					}
				}
			}
		}
	}

	public navigate() {
		switch (this.notFoundMessage) {
			case 'NO_CHATS_FOUND_MESSAGE': {
				this.router.navigate([
					`modules/${this.moduleId}/channels/chat-widgets/Chat`,
				]);
				break;
			}
			case 'NO_AGENTS_FOUND_MESSAGE': {
				this.router.navigate([`company-settings/controllers/download`]);
				break;
			}
		}
	}

	public clicked(element) {
		if (!this.customTableService.dataIds.includes(element.DATA_ID)) {
			this.customTableService.dataIds.push(element.DATA_ID);
		} else {
			const index = this.customTableService.dataIds.indexOf(element.DATA_ID);
			this.customTableService.dataIds.splice(index, 1);
		}
	}
}
