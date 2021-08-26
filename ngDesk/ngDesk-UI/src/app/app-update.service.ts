import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { interval } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ConfigService } from '@src/app/config.service';
import { DOCUMENT } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class AppUpdateService {
	constructor(
		private http: HttpClient,
		private configService: ConfigService,
		private _snackBar: MatSnackBar,
		@Inject(DOCUMENT) private _document: HTMLDocument
	) {}

	// this will be replaced by actual timestamp post-build.js
	private currentTimestamp = '{{POST_BUILD_ENTERS_TIMESTAMP_HERE}}';
	private appJsonFile = 'software.json';
	
	public initApplicationUpdating() {
		console.debug('SoftwareUpdateService.initApplicationUpdating()');
		let frequency = this.configService.getConfig()
			.applicationVersionCheckDelay;
		console.debug('SoftwareUpdateService.initApplicationUpdating()', frequency);
		let frequencyNumber = Number(frequency);
		if (isNaN(frequencyNumber) || 0 >= frequencyNumber) {
			console.warn(
				'SoftwareUpdateService.initApplicationUpdating() no frequency : runApplicationUpdating is skipped'
			);
		} else {
			// on application init, force reload if necessary
			this.runApplicationCheck(true);
			// check will be done to reload on user validation
			interval(frequencyNumber).subscribe((val) =>
				this.runApplicationCheck(true)
			);
		}
	}

    public setAppFavicon(image: string){ 
		this._document.getElementById('app-favicon').setAttribute('href',image); 
	}

	/**
	 * Will get new application timestamp and launch application update if needed
	 * @param forceReload
	 */
	private runApplicationCheck(forceReload: boolean = false): void {
		console.debug('SoftwareUpdateService.runApplicationCheck()', forceReload);
		let url = 'https://' + window.location.hostname + '/' + this.appJsonFile;
		// get file plus timestamp in request to invalidate caches
		this.http.get(url + '?t=' + new Date().getTime()).subscribe(
			(response: any) => {
				// get timestamp in json
				const newTimestamp = response.timestamp;
				const applicationNeedsToBeUpdated = this.isApplicationHasBeenUpdatedOnServer(
					newTimestamp
				);
				// If application needs to be updated
				if (applicationNeedsToBeUpdated) {
					this.updateApplication(forceReload);
				} else {
					console.debug(
						'SoftwareUpdateService.runApplicationCheck() application does not need to be updated'
					);
				}
			},
			(err) => {
				console.error(
					err,
					`SoftwareUpdateService.runApplicationCheck() : could not get ${this.appJsonFile}`
				);
			}
		);
	}

	/**
	 * Reload application, user may be asked for validation
	 * if forceReload is false then user will be asked for application refresh
	 * if foceTeload is true then application will be refreshed
	 * @param forceReload
	 */
	private updateApplication(forceReload: boolean): void {
		console.debug(
			'SoftwareUpdateService.updateApplication() application has to be updated'
		);
		if (forceReload) {
			// Force reload
			console.debug('SoftwareUpdateService.updateApplication() refresh forced');
			window.location.reload(true);
		} else {
			// ask for user's validation to reload
			let snackBarRef = this._snackBar.open(
				'Application has been updated!',
				'Update application',
				{}
			);
			snackBarRef.onAction().subscribe(() => {
				// Refresh application when user asks for...
				console.debug(
					'SoftwareUpdateService.updateApplication() refresh asked'
				);
				window.location.reload(true);
			});
		}
	}

	/**
	 * Checks if application timestamp has changed
	 * @param newTimestamp
	 * @returns {boolean}
	 */
	private isApplicationHasBeenUpdatedOnServer(newTimestamp: string): boolean {
		console.debug(
			'SoftwareUpdateService.isApplicationHasBeenUpdatedOnServer()',
			this.currentTimestamp,
			newTimestamp
		);
		if (!this.currentTimestamp || !newTimestamp) {
			console.debug(
				'SoftwareUpdateService.isApplicationHasBeenUpdatedOnServer() : missing input : false'
			);
			return false;
		}
		let result = this.currentTimestamp !== newTimestamp;
		console.debug(
			`SoftwareUpdateService.isApplicationHasBeenUpdatedOnServer() : ${result}`
		);
		return result;
	}
}
