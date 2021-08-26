import { Component, OnDestroy, OnInit } from '@angular/core';
import { ZoomIntegrationApiService } from '@ngdesk/integration-api';
import { MatDialog } from '@angular/material/dialog';
import { ZoomIntegrationDialogComponent } from '../../dialogs/zoom-integration-dialog/zoom-integration-dialog.component';
import { ZoomIntegrationFailedDialogComponent } from '../../dialogs/zoom-integration-failed-dialog/zoom-integration-failed-dialog.component';
import { UsersService } from '@src/app/users/users.service';

@Component({
	selector: 'app-zoom-integration',
	templateUrl: './zoom-integration.component.html',
	styleUrls: ['./zoom-integration.component.scss'],
})
export class ZoomIntegrationComponent implements OnInit, OnDestroy {
	public zoomIntegrated = false;
	public disableTheButton: boolean;
	public setIntervalForAPICall;
	public companyName: String;
	public firstName: String;
	public lastName: String;
	public emailAddress: String;
	public roleNAme: String;
	public panelOpenState = false;

	constructor(
		private dialog: MatDialog,
		private zoomIntegrationApiService: ZoomIntegrationApiService,
		private userService: UsersService
	) {}

	public ngOnDestroy(): void {
		if (this.setIntervalForAPICall && this.setIntervalForAPICall != null) {
			// clearInterval(this.setIntervalForAPICall);
		}
	}

	public ngOnInit() {
		this.zoomIntegrationApiService
			.getZoomStatus()
			.subscribe((response: any) => {
				if (response.ZOOM_AUTHENTICATED) {
					this.disableTheButton = true;
					if (response.ZOOM_USER_INFORMATION !== null) {
						this.zoomIntegrated = true;
						this.companyName = response.ZOOM_USER_INFORMATION.COMPANY_NAME;
						this.firstName = response.ZOOM_USER_INFORMATION.FIRST_NAME;
						this.lastName = response.ZOOM_USER_INFORMATION.LAST_NAME;
						this.emailAddress = response.ZOOM_USER_INFORMATION.EMAIL_ADDRESS;
						this.roleNAme = response.ZOOM_USER_INFORMATION.ROLE_NAME;
					}
				}
			});
	}

	public addToZoom() {
		const onSuccessCloseWindow = window.open(
			`https://zoom.us/oauth/authorize?response_type=code&client_id=85PnSMMrS0u2b8o1TIludA&state=${this.userService.getSubdomain()}&redirect_uri=https://${this.userService.getSubdomain()}.ngdesk.com/api/ngdesk-integration-service-v1/zoom/authorized`,
			'_blank'
		);

		// dialog when zoom integration is being created
		const dialogRef = this.dialog.open(ZoomIntegrationDialogComponent, {
			width: '500px',
		});

		setTimeout(() => {
			this.dialog.closeAll();
			clearInterval(this.setIntervalForAPICall);
		}, 90000);

		clearInterval(this.setIntervalForAPICall);

		// zoom api call is called every 10s
		this.setIntervalForAPICall = setInterval(() => {
			this.zoomIntegrationApiService
				.getZoomStatus()
				.subscribe((response: any) => {
					if (response.ZOOM_AUTHENTICATED) {
						this.disableTheButton = true;
						if (response.ZOOM_USER_INFORMATION !== null) {
							this.zoomIntegrated = true;
							this.companyName = response.ZOOM_USER_INFORMATION.COMPANY_NAME;
							this.firstName = response.ZOOM_USER_INFORMATION.FIRST_NAME;
							this.lastName = response.ZOOM_USER_INFORMATION.LAST_NAME;
							this.emailAddress = response.ZOOM_USER_INFORMATION.EMAIL_ADDRESS;
							this.roleNAme = response.ZOOM_USER_INFORMATION.ROLE_NAME;
						}
						onSuccessCloseWindow.window.close();
						this.dialog.closeAll();
						clearInterval(this.setIntervalForAPICall);
					} else {
						setTimeout(() => {
							this.zoomIntegrationFailed();
						}, 80000);
					}
				});
		}, 5000);
	}

	// when Zoom integration failed
	public zoomIntegrationFailed() {
		const dialogRef = this.dialog.open(ZoomIntegrationFailedDialogComponent, {
			width: '500px',
		});
	}
}
