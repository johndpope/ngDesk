import { Injectable } from '@angular/core';
import {
	setString,
	remove,
	clear,
	getString,
	getBoolean,
	setBoolean,
} from '@nativescript/core/application-settings';
import { AppGlobals } from '../app.globals.tns';
// import { device } from '@nativescript/angular/common';
import { SidebarApiService } from '@ngdesk/sidebar-api';
import { RouterExtensions } from '@nativescript/angular';
import { isAndroid, AndroidApplication, isIOS } from '@nativescript/core';
// import {  AndroidActivityBackPressedEventData } from '@nativescript/core';
import { android } from '@nativescript/core/application';
import { DataApiService } from '@ngdesk/data-api';
import { UsersService } from '../users/users.service';
require('nativescript-websockets');

@Injectable({
	providedIn: 'root',
})
export class ApplicationSettings {
	public fpos: string = '';
	constructor(
		private appGlobals: AppGlobals,
		private sidebarApiService: SidebarApiService,
		private dataApiService: DataApiService,
		private router: RouterExtensions,
		private usersService:UsersService
	) {}
	public setSubdomain(subdomain) {
		console.log('set');
		console.log(subdomain);
		setString('subdomain', subdomain);
		this.sidebarApiService.configuration.basePath =
			'https://' + subdomain + '.ngdesk.com/api/ngdesk-sidebar-service-v1';
		this.dataApiService.configuration.basePath =
			'https://' + subdomain + '.ngdesk.com/api/ngdesk-data-service-v1';
		this.appGlobals.baseRestUrl =
			'https://' + subdomain + '.ngdesk.com/ngdesk-rest/ngdesk';
		this.appGlobals.websocketUrl =
			'wss://' + subdomain + '.ngdesk.com/ngdesk-manager/ngdesk';
		this.appGlobals.restWebsocketUrl =
			'wss://' + subdomain + '.ngdesk.com/ngdesk-rest/ngdesk';
	}

	public getSubdomain() {
		return getString('subdomain');
	}

	public clear() {
		clear();
	}

	public setAuthentication(auth) {
		setString('authentication_token', auth);
	}

	public getAuthentication() {
		return getString('authentication_token');
	}

	public getPageNumber(module) {
		return 0;
	}

	public getPageSize(module) {
		return 20;
	}

	public getListLayoutId(module) {
		const listLayout = module.LIST_MOBILE_LAYOUTS.find(
			(layout) => layout.IS_DEFAULT && layout.ROLE === this.usersService.user.ROLE
		);

		if(listLayout){
            return listLayout.LAYOUT_ID;
        }else{
           const listLayout = module.LIST_MOBILE_LAYOUTS.find(
                (layout) =>
                    layout.ROLE === this.usersService.user.ROLE
            );
            if(!listLayout){
                return false;
            }
            return listLayout.LAYOUT_ID;
        }
	}

	public getSearchQuey(module) {
		return '';
	}

	public getSortBy(module) {
		const listLayout = module.LIST_MOBILE_LAYOUTS.find(
			(layout) => layout.IS_DEFAULT
		);
		if (listLayout && listLayout !== null) {
			const field = module['FIELDS'].find(
				(field) => field.FIELD_ID === listLayout['ORDER_BY']['COLUMN']
			);
			if (field) {
				return field['NAME'];
			}
		}
	}

	public getOrderBy(module) {
		const listLayout = module.LIST_MOBILE_LAYOUTS.find(
			(layout) => layout.IS_DEFAULT
		);
		if (listLayout && listLayout !== null) {
			return listLayout['ORDER_BY']['ORDER'].toLowerCase();
		}
	}

	public postUserToken(authToken, rolesService, userService, messagingService) {
		console.log('subscribe');
		if (authToken) {
			rolesService
				.getRole(userService.user.ROLE)
				.subscribe((roleResponse: any) => {
					if (roleResponse.NAME !== 'Customers') {
						console.log(roleResponse.NAME);
						console.log('userToken set');
						console.log(getString('onPushToken'));
						// FIREBASE REQUEST AND RECEIVE
						// messagingService.updateTokenMobile(
						// 	getString('onPushToken'),
						// 	device.uuid,
						// 	device.os
						// );
						messagingService.receiveMessage();
					}
				});
		}
	}

	// public disableBackButton(disable) {
	// 	if (isAndroid) {
	// 		android.once(
	// 			AndroidApplication.activityBackPressedEvent,
	// 			(data: AndroidActivityBackPressedEventData) => {
	// 				data.cancel = disable; // prevents default back button behavior
	// 			}
	// 		);
	// 	}
	// }

	public navigateToListLayout(moduleId) {
		console.log('go to list layout');
		console.log(`render/${moduleId}`);
		this.router.navigate([`/render/${moduleId}`]);
	}

	// public enableBackButton() {
	// 	if (isAndroid) {
	// 		android.on(
	// 			AndroidApplication.activityBackPressedEvent,
	// 			(args: AndroidActivityBackPressedEventData) => {
	// 				console.log('enableBackButton');
	// 				console.log(args.cancel);
	// 				args.cancel = false;
	// 			}
	// 		);
	// 	}
	// }

	public setEntryValues(moduleId, dataId) {
		setString('moduleId', moduleId);
		setString('dataId', dataId);
	}

	public getshowBackButton() {
		return getBoolean('showBackButton');
	}

	public setshowBackButton() {
		setBoolean('showBackButton', false);
	}

	public setLocation(subdomain) {}

	public isMobile() {
		return true;
	}
	// public highlight(index,$event:TouchGestureEventData){
	//     console.log("highlight")
	//     if ($event.action === TouchAction.down) {
	// 		return index;
	// 	}
	// 	if ($event.action === TouchAction.cancel || $event.action === TouchAction.up) {
	// 		return -1;
	// 	}
	// }
}
