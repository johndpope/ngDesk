import { Injectable } from '@angular/core';
import { FilePreviewOverlayService } from '../shared/file-preview-overlay/file-preview-overlay.service';

declare let window;
@Injectable({
	providedIn: 'root',
})
export class ApplicationSettings {
	constructor(public fpos: FilePreviewOverlayService) {}

	public setSubdomain(subdomain) {}
	public getSubdomain() {
		return null;
	}

	public clear() {}

	public setAuthentication(auth) {}

	public getAuthentication() {
		return null;
	}

	public getPageNumber(module) {
		return null;
	}

	public getPageSize(module) {
		return null;
	}

	public getListLayoutId(module) {
		return null;
	}

	public getSearchQuey(module) {
		return null;
	}

	public getSortBy(module) {
		return null;
	}

	public getOrderBy(module) {
		return null;
	}

	public postUserToken(authToken, roleService, userService, messagingService) {
		console.log('subscribe');
	}

	public disableBackButton(disable) {}

	public navigateToListLayout(moduleId) {}

	public enableBackButton() {}

	public setEntryValues(moduleId, dataId) {}

	public getshowBackButton() {
		return null;
	}

	public setshowBackButton() {}

	public setLocation(subdomain) {
		window.location = `https://${subdomain}.ngdesk.com`;
	}

	public isMobile() {
		return false;
	}
	// public highlight(index,event){
	//     return null;
	// }
}
