import { DataSource, SelectionModel } from '@angular/cdk/collections';
import { Injectable } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { NavigationEnd, Router } from '@angular/router';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';

export class MyDataSource extends DataSource<any[]> {
	constructor(
		private subject: BehaviorSubject<any[]>,
		private paginator: MatPaginator,
		public sort: MatSort,
		public data: any
	) {
		super();
	}
	public connect(): Observable<any[]> {
		return this.subject.asObservable();
	}
	public disconnect(): void {}

	public getPaginator() {
		return this.paginator;
	}

	public setPaginator(paginator) {
		this.paginator = paginator;
	}

	public getData() {
		return this.data;
	}

	public setData(data) {
		this.data = data;
	}
}

@Injectable({
	providedIn: 'root',
})
export class CustomTableService implements OnDestroy {
	private navigationSubscription: Subscription;
	public currentOneToManyDialog = '';
	public dataIds = [];
	constructor(private router: Router) {
		this.navigationSubscription = this.router.events.subscribe((e: any) => {
			// If it is a NavigationEnd event re-initalise the component
			if (e instanceof NavigationEnd) {
				this.showPaginator = true;
			}
		});
	}

	public columnsHeaders: string[] = [];
	public columnsHeadersObj: {
		DISPLAY: string;
		NAME: string;
		DATA_TYPE?: string;
	}[] = [];
	private dataSubject = new BehaviorSubject<any[]>([]);
	public activeSort = { ORDER_BY: '', NAME: '', SORT_BY: '' };
	public rowCheked = false;
	public sort: any = {};
	public paginator: any = {};
	public isLoading = true;
	public totalRecords: number;
	public customTableDataSource: MyDataSource = new MyDataSource(
		this.dataSubject,
		this.paginator,
		this.sort,
		[]
	);
	public selection = new SelectionModel<any>(true, []);

	// Vars to be used by other component for pageination and sorting
	public pageIndex = 0;
	public pageSize = 10;
	public sortBy: string;
	public sortOrder: string;
	public showPaginator = true;

	public setTableDataSource(tableData, totalRecords) {
		this.dataSubject.next(tableData);
		this.totalRecords = totalRecords;

		this.customTableDataSource = new MyDataSource(
			this.dataSubject,
			this.paginator,
			this.sort,
			tableData
		);
		this.selection = new SelectionModel(true, []);
		this.customTableDataSource.sort = this.sort;
		this.isLoading = false;
	}

	public checkPermissionsForActions(
		role,
		componentActions,
		moduleType,
		reports?
	) {
		let rolePermissions: {
			ACCESS: String;
			ACCESS_TYPE: String;
			EDIT: string;
			VIEW: String;
		};
		let enabledActions = componentActions.actions;

		if (
			role.NAME !== 'SystemAdmin' &&
			(reports === undefined || reports === null)
		) {
			// reset enabledActions
			enabledActions = [];
			role.PERMISSIONS.forEach((permission) => {
				// check role permission for module id or if it's schedules or escalations
				if (moduleType === permission.MODULE) {
					rolePermissions = permission.MODULE_PERMISSIONS;
					// loop trhough the component actions and push to enableActions based on permission set on role
					componentActions.actions.forEach((action) => {
						if (rolePermissions.hasOwnProperty(action.PERMISSION_NAME)) {
							if (rolePermissions[action.PERMISSION_NAME] !== 'None') {
								enabledActions.push(action);
							}
						}
					});
				}
			});
		}
		return enabledActions;
	}

	/** Whether the number of selected elements matches the total number of rows. */
	public isAllSelected() {
		const numSelected = this.selection.selected.length;
		const numRows = this.customTableDataSource.getData().length;
		return numSelected === numRows;
	}

	/** Selects all rows if they are not all selected; otherwise clear selection. */
	public masterToggle() {
		if (this.isAllSelected()) {
			this.selection.clear();
		} else {
			this.customTableDataSource.getData().forEach((row) => {
				this.selection.select(row);
			});
		}
	}

	public ngOnDestroy() {
		// avoid memory leaks here by cleaning up after ourselves. If we
		// don't then we will continue to run our initialiseInvites()
		// method on every navigationEnd event.

		if (this.navigationSubscription) {
			this.navigationSubscription.unsubscribe();
		}
	}

	public showTooltip(element, col) {
		if (
			col.DATA_TYPE === 'Relationship' &&
			this.validateStringLength(element[col.NAME]?.PRIMARY_DISPLAY_FIELD)
		) {
			return element[col.NAME]?.PRIMARY_DISPLAY_FIELD;
		} else {
			if (this.validateStringLength(element[col.NAME])) {
				return element[col.NAME];
			}
		}
	}

	public validateStringLength(value: string) {
		if (value && value.length > 32) {
			return true;
		}
		return false;
	}
}
