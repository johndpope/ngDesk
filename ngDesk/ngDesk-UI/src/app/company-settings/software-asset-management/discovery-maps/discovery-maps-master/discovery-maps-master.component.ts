import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { DiscoveryMapApiService } from '@ngdesk/sam-api';
import { TranslateService } from '@ngx-translate/core';
import { DiscoveryMapsService } from '@src/app/company-settings/software-asset-management/discovery-maps/discovery-maps.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';

@Component({
	selector: 'app-discovery-maps-master',
	templateUrl: './discovery-maps-master.component.html',
	styleUrls: ['./discovery-maps-master.component.scss'],
})
export class DiscoveryMapsMasterComponent implements OnInit {
	public discoveryMapActions = {
		actions: [{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' }],
	};
	constructor(
		private translateService: TranslateService,
		public customTableService: CustomTableService,
		private discoveryMapService: DiscoveryMapsService,
		private router: Router,
		private bannerMessageService: BannerMessageService,
		private dialog: MatDialog,
		private discoveryMapApiService: DiscoveryMapApiService,
	) {
		this.translateService.get('DELETE').subscribe((value: string) => {
			this.discoveryMapActions[value] = (discoveryMap) => {
				this.deleteDiscoveryMap(discoveryMap);
			};
			this.discoveryMapActions.actions[0].NAME = value;
		});
	}

	ngOnInit() {
		this.initializeHeaders();
		this.getAllDataDiscoveryMaps();
	}

	public initializeHeaders() {
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('NAME'),
			NAME: 'name',
		});

		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('DESCRIPTION'),
			NAME: 'description',
		});

		columnsHeadersObj.push({ 
			DISPLAY: this.translateService.instant('ACTION'), 
			NAME: 'ACTION' 
		});

		columnsHeaders.push(this.translateService.instant('NAME'));
		columnsHeaders.push(this.translateService.instant('DESCRIPTION'));
		columnsHeaders.push(this.translateService.instant('ACTION'));
		this.customTableService.sortBy = 'name';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
	}

	// Get all discovery maps.
	public getAllDataDiscoveryMaps() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.discoveryMapService
			.getAllDiscoveryMaps(page, pageSize, sortBy, orderBy)
			.subscribe(
				(discoveryMapResponse: any) => {
					this.customTableService.setTableDataSource(
						discoveryMapResponse.discoveryMaps,
						discoveryMapResponse.discoveryMaps.length
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
	}

	private deleteDiscoveryMap(discoveryMap) {
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message:
					this.translateService.instant(
						'ARE_YOU_SURE_YOU_WANT_TO_DELETE_DISCOVERY_MAP'
					) +
					discoveryMap.name +
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
				this.discoveryMapApiService
					.deleteDiscoveryMap(discoveryMap.id)
					.subscribe(
						(discoveryMapResponse: any) => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('DELETED_SUCCESSFULLY'),
							});
							this.getAllDataDiscoveryMaps();
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

	// Navigate to detail page to show create  discovery-map.
	public createNewDiscoveryMap() {
		this.router.navigate([`company-settings/discovery-maps/new`]);
	}

	// Navigate to detail page to show edit discovery-maps.
	public rowClicked(event) {
		this.router.navigate([`company-settings/discovery-maps/${event.id}`]);
	}

	// On sort of data.
	public sortData() {
		this.getAllDataDiscoveryMaps();
	}

	// On use of pagination
	public pageChangeEmit(event) {
		this.getAllDataDiscoveryMaps();
	}
}
