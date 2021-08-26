import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { RoleLayoutApiService } from '@ngdesk/role-api';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { CacheService } from '@src/app/cache.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';

@Component({
	selector: 'app-role-layout-master',
	templateUrl: './role-layout-master.component.html',
	styleUrls: ['./role-layout-master.component.scss'],
})
export class RoleLayoutMasterComponent implements OnInit {
	public isLoading = true;
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public roleLayoutsActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	public allRoles = [];
	public specialDataTypes = ['Relationship', 'Date/Time', 'Date', 'Time'];
	public roleLayouts=[];

	constructor(
		private router: Router,
		public customTableService: CustomTableService,
		private translateService: TranslateService,
		private roleLayoutApiService: RoleLayoutApiService,
		private dialog: MatDialog,
		private bannerMessageService: BannerMessageService,
		private cacheService: CacheService,
		private http: HttpClient,
		private globals: AppGlobals,
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.

		this.translateService.get('DELETE').subscribe((value: string) => {
			// create a function on this.escalationsActions with the name of the translated word
			this.roleLayoutsActions[value] = (response) => {
				this.deleteRoleLayout(response);
			};
			this.roleLayoutsActions.actions[0].NAME = value;
		});
	}

	public ngOnInit() {
		this.customTableService.isLoading = true;
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: {
			DISPLAY: string;
			NAME: string;
		}[] = [];
		columnsHeadersObj.push(
			{
				DISPLAY: this.translateService.instant('NAME'),
				NAME: 'name',
			},
			{
				DISPLAY: this.translateService.instant('ROLE'),
				NAME: 'role',
			},
			{
				DISPLAY: this.translateService.instant('ACTION'),
				NAME: 'ACTION',
			}
		);
		columnsHeaders.push(
			this.translateService.instant('NAME'),
			this.translateService.instant('ROLE'),
			this.translateService.instant('ACTION')
		);

		// Fetch all roles to filter the role entry.
		this.cacheService.getRoles().subscribe((roles) => {
			this.allRoles = roles;
		});

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.sortBy = 'name';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: this.translateService.instant('NAME'),
			NAME: 'name',
		};
		this.getRoleLayout();
	}

	public getRoleLayout() {
		const sort = [
			this.customTableService.sortBy + ',' + this.customTableService.sortOrder,
		];

		this.getAllRoleLayoutsUsingGraphql().subscribe((graphqlData:any)=>{
			this.roleLayouts=this.setRoleLayout(graphqlData);
			this.isLoading = false;
			this.customTableService.setTableDataSource(
			this.roleLayouts,
			this.roleLayouts.length
			);		
			
		},
		(error: any) => {
			this.bannerMessageService.errorNotifications.push({
				message: error.error.ERROR,
			});
		});
	}

	public setRoleLayout(graphqlData:any){
		let layouts=graphqlData['getRoleLayouts'];
			layouts.forEach((layout) => {
				let tabs=[];
			layout.tabs.forEach((tabData) => {
				let columns=[];
				tabData.columnsShow.forEach((column) => {
				columns.push(column.fieldId);
			});
			let conditions=[];
			tabData.conditions.forEach((conditionData) => {
			let condition={
				condition:conditionData.condition.fieldId,
				conditionValue:conditionData.conditionValue,
				operator:conditionData.operator,
				requirementType:conditionData.requirementType,
			};
			conditions.push(condition);
			});

			let tab={
				columnsShow:columns,
				conditions:conditions,
				module:tabData.module.moduleId,
        		orderBy:{
					order:tabData.orderBy.order,
					column:tabData.orderBy.column.fieldId
				},
			}
			tabs.push(tab);
		});
		layout.role=layout.role.name;
		layout.tabs=tabs;
	});
	return layouts;
			
	}

	public sortData() {
		this.getRoleLayout();
	}

	public newRoleLayout(): void {
		this.router.navigate([`company-settings/role-layouts/new`]);
	}

	public rowClicked(rowData): void {
		this.router.navigate([
			`company-settings/role-layouts/${rowData.layoutId}`,
		]);
	}

	public pageChangeEmit(event) {
		this.getRoleLayout();
	}

	private deleteRoleLayout(roleLayout) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_ACTION_ITEM'
					) +
					roleLayout.name +
					' ?',
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn',
			},
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe((result) => {
			if (result === this.translateService.instant('DELETE')) {
				this.roleLayoutApiService
					.deleteRoleLayout(roleLayout.layoutId)
					.subscribe(
						(response: any) => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('DELETED_SUCCESSFULLY'),
							});
							this.getRoleLayout();
						//	this.router.navigate([`company-settings`]);

						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			}
		});
	}

public getAllRoleLayoutsUsingGraphql(){

	const query = `{
		getRoleLayouts(pageNumber: 0, pageSize: 20){
			layoutId
			name
			companyId
			defaultLayout
			description
			role{
				name
			}
			tabs
				{
					module{
						moduleId
						name
					}
					columnsShow{
						fieldId
					}
					 orderBy{
						  order
							   column{
								   fieldId
							   }
					   }
					   conditions{
						requirementType
						operator
						condition{
							fieldId
						}
						conditionValue
					   }
				}			   
		}
	  
	}`;
	return this.http.post(`${this.globals.graphqlUrl}`, query);


}

public getCountUsingGraphql(){
	const query=`{
		count:getRoleLayoutCount
		  }`;
		  return this.http.post(`${this.globals.graphqlUrl}`, query);
}

}
