import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DataApiService } from '@ngdesk/data-api';
import { AppGlobals } from '@src/app/app.globals';
import { CacheService } from '@src/app/cache.service';
import { SchedulesDetailService } from '@src/app/schedules/schedules-detail/schedules-detail.service';
import { Subject } from 'rxjs';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	switchMap,
} from 'rxjs/operators';

@Injectable({
	providedIn: 'root',
})
export class SchedulesService {
	public usersScrollSubject = new Subject<any>();
	public storedUsers = [];
	public allSelectedUsers = [];
	public usersMap = {};
	public userModule;
	public newUserArr = [];
	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private cacheService: CacheService,
		private dataService: DataApiService,
		private schedulesDetailService: SchedulesDetailService
	) {}

	// This function will initailize the users on load of page.
	public initializeUsers() {
		const modules: any[] = this.cacheService.companyData['MODULES'];
		this.userModule = modules.find((module) => module.NAME === 'Users');
		return this.dataService.getAllData(this.userModule.MODULE_ID, '', 0, 10, [
			'EMAIL_ADDRESS',
			'Asc',
		]);
	}

	// This function will initialize the scrollsubject.
	public initializeScrollSubject() {
		this.usersScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					const moduleId = this.cacheService.moduleNamesToIds['Users'];
					let searchValue = '';

					if (value !== '') {
						searchValue = 'FULL_NAME' + '=' + value;
					}
					let page = 0;
					if (this.storedUsers && !search) {
						page = Math.ceil(this.storedUsers.length / 10);
					}
					return this.dataService
						.getAllData(moduleId, searchValue, page, 10, [
							'EMAIL_ADDRESS',
							'Asc',
						])
						.pipe(
							map((results: any) => {
								if (search) {
									this.allSelectedUsers = results.content;
								} else if (results.content.length > 0) {
									this.allSelectedUsers = this.storedUsers.concat(
										results.content
									);
								}
								this.initializeUserMap(this.allSelectedUsers);
								return results.content;
							})
						);
				})
			)
			.subscribe();
	}

	// This will add the users to the list in edit layout
	public addSelectedUsersToList(layerUser) {
		const userData =
			this.cacheService.moduleInfo[this.userModule.MODULE_ID]?.DATA;
		if (userData) {
			const newUser = this.newUserArr.find(
				(user) => user.DATA_ID === layerUser.DATA_ID
			);
			if (newUser) {
				this.allSelectedUsers.push(newUser);
				this.initializeUserMap(this.allSelectedUsers);
			} else {
				this.dataService
					.getModuleEntry(this.userModule.MODULE_ID, layerUser.DATA_ID)
					.subscribe((entryResponse: any) => {
						this.allSelectedUsers.push(entryResponse);
						this.initializeUserMap(this.allSelectedUsers);
					});
			}
		}
	}

	// It removes the duplicates from the userlists
	public removeDuplicates(users) {
		if (this.newUserArr.length > 0) {
			for (let i = 0; i < users.length; i++) {
				const duplicateUser = this.newUserArr.find(
					(user) => user.DATA_ID === users[i].DATA_ID
				);
				if (!duplicateUser) {
					this.newUserArr.push(users[i]);
				}
			}
		} else {
			this.newUserArr = users;
		}
		console.log(this.newUserArr);
		return this.newUserArr;
	}

	// This function will initialize the userMap.
	public initializeUserMap(users) {
		this.storedUsers = this.removeDuplicates(users);
		this.storedUsers = this.storedUsers.sort((user1, user2) =>
			user1.CONTACT?.PRIMARY_DISPLAY_FIELD?.localeCompare(
				user2.CONTACT?.PRIMARY_DISPLAY_FIELD
			)
		);
		this.storedUsers.forEach((user, userKey) => {
			this.usersMap[user.DATA_ID] = user;
			const index = userKey % 10;
			this.usersMap[user.DATA_ID].COLORS =
				this.schedulesDetailService.colors[index];
		});
	}

	// Calling graphql call to fetch list of schedules
	public getAllSchedules(query) {
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Calling graphql call to fetch a schedule
	public getSchedule(scheduleId) {
		const query = `{
			schedule: getSchedule(scheduleId: "${scheduleId}") {
				name
				scheduleId: id
				description
				timezone
				dateCreated
				dateUpdated
				createdBy {
					DATA_ID: _id
					CONTACT {
						PRIMARY_DISPLAY_FIELD: FULL_NAME
						DATA_ID: _id
					}
				}
				lastUpdatedBy {
					DATA_ID: _id
					CONTACT {
						FULL_NAME
					}
				}
				layers {
					users {
						DATA_ID: _id
						EMAIL_ADDRESS:EMAIL_ADDRESS
						CONTACT {
							PRIMARY_DISPLAY_FIELD: FULL_NAME
							DATA_ID: _id
						} 
						USER_UUID
					}
					rotationType
					startTime
					endTime
					startDate
					hasRestrictions
					restrictionType
					restrictions {
						startTime
						endTime
						startDay
						endDay
					}
				}
			}
		}`;

		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	public getUsers(userModuleId) {
		const query = `{
			DATA: getUsers(moduleId: "${userModuleId}", pageNumber: 0, pageSize: 5000) {
				DATA_ID: _id
				PRIMARY_DISPLAY_FIELD: CONTACT {
					FULL_NAME
				}
				USER_UUID
			}
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
