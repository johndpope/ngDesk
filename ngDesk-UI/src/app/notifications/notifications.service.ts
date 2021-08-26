import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { AppGlobals } from '../app.globals';

@Injectable({
	providedIn: 'root',
})
export class NotificationsService {
	constructor(private http: HttpClient, private globals: AppGlobals) {}

	public getNotifications() {
		return this.http.get(`${this.globals.baseRestUrl}/notifications`);
	}

	public getUnreadNotifications(pageNumber) {
		let query = '';
		query = `{
				 getUnreadNotifications(
          		read: false
          		pageNumber: ${pageNumber}
          		pageSize: 5
          		sortBy: "dateCreated"
          		orderBy: "Desc"
        		) {
					read
          			recipientId
		  			notificationId
          			companyId
          			message
		  			moduleId
		  			dataId
				}
				unreadNotificationLength: getUnReadNotificationsDataFetcherCount
			}`;

		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public filterNewLists(unreadNotifications, data) {
		const newArr = [];
		data.forEach((notification) => {
			const existingNotification = unreadNotifications.find(
				(currentNotification) =>
					currentNotification.notificationId === notification.notificationId
			);
			if (!existingNotification) {
				newArr.push(notification);
			}
		});
		return newArr;
	}
}
