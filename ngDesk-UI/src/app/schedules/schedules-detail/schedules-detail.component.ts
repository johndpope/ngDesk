import {
	Component,
	ElementRef,
	OnInit,
	ViewChild,
	OnDestroy,
} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';

import * as moment from 'moment-timezone/builds/moment-timezone-with-data-2012-2022.min';

import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { UsersService } from 'src/app/users/users.service';
import { CompaniesService } from '../../companies/companies.service';
import { LayerRestriction } from '../../models/layer-restriction';
import { Schedule } from '../../models/schedule';
import { ScheduleLayer } from '../../models/schedule-layer';
import { ModulesService } from '../../modules/modules.service';
import { LayerRestrictionComponent } from './layer-restriction/layer-restriction.component';
import { SchedulesDetailService } from './schedules-detail.service';
import { RolesService } from '@src/app/roles/roles.service';
import { Subscription } from 'rxjs';
import { CacheService } from '@src/app/cache.service';
import { SchedulesService } from '../schedules.service';
import { ScheduleLayerRestriction } from '@src/app/models/schedule-layer-restriction';
import { MatChipInputEvent } from '@angular/material/chips';
import { Subject } from 'rxjs';
import { DataApiService } from '@ngdesk/data-api';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	switchMap,
} from 'rxjs/operators';
import { COMMA, ENTER } from '@angular/cdk/keycodes';

@Component({
	selector: 'app-schedules-detail',
	templateUrl: './schedules-detail.component.html',
	styleUrls: ['./schedules-detail.component.scss'],
})
export class SchedulesDetailComponent implements OnInit, OnDestroy {
	public schedule: Schedule = new Schedule('', '', moment.tz.guess(), [
		new ScheduleLayer([], 'Weekly', '00:00', false, new Date(), [], null),
	]);
	public timeZones: string[];
	public usersMap: any = {};
	public users: any = [];
	public usersModule;
	public rotationTypes: string[];
	public startTimes: string[];
	public dayInView: Date;
	public dayInViewEnd: Date;
	public currentView: string;
	public layerDivs: any;
	public layerUsers: any = { 0: { users: [] } };
	public finalLayer: any = {};
	public scheduleForm: FormGroup;
	public disableButtons = false;
	public _userFilter = '';
	public usersLength;
	public filteredUsers: any = [];
	public companyInfoSubscription: Subscription;
	public tempUserInput = '';
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	public escalationId: string;
	public paramValue: string;
	@ViewChild('search') public searchTextBox: ElementRef;
	public scheduleDataScrollSubject = new Subject<any>();
	get userFilter(): string {
		return this._userFilter;
	}
	set userFilter(value: string) {
		this._userFilter = value;
		this.filteredUsers = this.userFilter
			? this.performFilter(this.userFilter)
			: this.users;
		this.setSelectedValues();
	}

	constructor(
		private bannerMessageService: BannerMessageService,
		private schedulesDetailService: SchedulesDetailService,
		private route: ActivatedRoute,
		private modulesService: ModulesService,
		public dialog: MatDialog,
		private companiesService: CompaniesService,
		private router: Router,
		private formBuilder: FormBuilder,
		private usersService: UsersService,
		private rolesService: RolesService,
		private loaderService: LoaderService,
		private cacheService: CacheService,
		private schedulesService: SchedulesService,
		private dataService: DataApiService
	) {}

	public ngOnInit() {
		this.scheduleForm = this.formBuilder.group({});
		const scheduleId = this.route.snapshot.params['scheduleId'];
		if (this.route.snapshot.queryParams['value']) {
			this.paramValue = this.route.snapshot.queryParams['value'];
		}
		this.escalationId = this.route.snapshot.queryParams['id'];
		const scheduleName = this.route.snapshot.queryParams['scheduleName'];
		this.dayInView = new Date();
		this.dayInViewEnd = new Date();
		this.currentView = 'Day';
		this.finalLayer.users = [];
		this.setLayerDivs();
		this.timeZones = this.schedulesDetailService.timeZones;
		this.rotationTypes = this.schedulesDetailService.rotationTypes;
		this.startTimes = this.schedulesDetailService.startTimes;

		this.schedulesDetailService
			.getRequiredData(scheduleName)
			.subscribe((responseList: any) => {
				const roleResponse = responseList[0];
				this.usersModule = responseList[2];
				if (!this.rolesService.role) {
					this.rolesService.role = roleResponse;
				}
				if (!this.rolesService.getEditAccess('Schedules')) {
					this.disableButtons = true;
					this.scheduleForm.disable();
				}

				const entriesResponse = responseList[1];
				this.users = entriesResponse.DATA;
				this.usersLength = this.users.length;
				if (scheduleId !== 'new') {
					this.schedulesService.getSchedule(scheduleId).subscribe(
						(response: any) => {
							const scheduleResponse = response.schedule;
							const layers: ScheduleLayer[] = [];
							this.loadUsersFromLayers(scheduleResponse);
							this.initializeUsers();
							const oldScheduleResponse = responseList[3];
							const scheduleResponseCopy = scheduleResponse;
							for (let i = 0; i < scheduleResponseCopy.layers.length; i++) {
								scheduleResponseCopy.layers[i].users =
									oldScheduleResponse.layers[i].users;
							}
							scheduleResponseCopy.layers.forEach((layer, layerIndex) => {
								const layerRestrictions: ScheduleLayerRestriction[] = [];
								layer.restrictions.forEach((restriction) => {
									layerRestrictions.push(
										new ScheduleLayerRestriction(
											restriction.startTime,
											restriction.endTime,
											restriction.startDay,
											restriction.endDay
										)
									);
								});

								const startDate = this.getLocalDateFromUtc(
									layer.startDate,
									scheduleResponse.timezone
								);

								layers.push(
									new ScheduleLayer(
										layer.users,
										layer.rotationType,
										layer.startTime,
										layer.hasRestrictions,
										startDate,
										layerRestrictions,
										layer.restrictionType
									)
								);
							});

							this.scheduleForm.controls['NAME'].setValue(
								scheduleResponse.name
							);
							this.scheduleForm.controls['DESCRIPTION'].setValue(
								scheduleResponse.description
							);
							this.schedule = new Schedule(
								scheduleResponse.name,
								scheduleResponse.description,
								scheduleResponse.timezone,
								layers,
								scheduleResponse.scheduleId
							);
							this.initTable();
						},
						(error) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
				} else {
					this.filteredUsers = this.users;
					this.initializeScheduleDataScrollSubject();
					this.filteredUsers.forEach((user, userKey) => {
						this.usersMap[user.DATA_ID] = user;
						const index = userKey % 10;
						this.usersMap[user.DATA_ID].COLORS =
							this.schedulesDetailService.colors[index];
					});
				}
			});
	}

	private loadUsersFromLayers(scheduleResponse) {
		scheduleResponse.layers.forEach((layer) => {
			layer.users.forEach((user) => {
				const currentUser = this.users.find(
					(layerUser) => layerUser.DATA_ID === user.DATA_ID
				);
				if (!currentUser) {
					this.users.push(user);
				}
			});
		});
	}

	private initializeUsers() {
		this.filteredUsers = this.users;
		this.initializeScheduleDataScrollSubject();
		this.filteredUsers.forEach((user, userKey) => {
			this.usersMap[user.DATA_ID] = user;
			const index = userKey % 10;
			this.usersMap[user.DATA_ID].COLORS =
				this.schedulesDetailService.colors[index];
		});
	}

	private performFilter(filterBy: string) {
		filterBy = filterBy.toLocaleLowerCase();
		return this.users.filter(
			(user: any) =>
				user !== null &&
				user.CONTACT !== null &&
				user.CONTACT.PRIMARY_DISPLAY_FIELD.toLocaleLowerCase().indexOf(
					filterBy
				) !== -1
		);
	}

	public clearSearch(event) {
		event.stopPropagation();
		this.userFilter = '';
	}

	public openedChange(e) {
		// Set search textbox value as empty while opening selectbox
		this.userFilter = '';
		// Focus to search textbox while clicking on selectbox
		if (e === true) {
			this.searchTextBox.nativeElement.focus();
		}
	}

	private setSelectedValues() {
		if (
			this.schedule._layers[0]._users &&
			this.schedule._layers[0]._users.length > 0
		) {
			this.schedule._layers[0]._users.forEach((selectedUser) => {
				if (
					this.filteredUsers.map((e) => e.DATA_ID).indexOf(selectedUser) === -1
				) {
					this.filteredUsers.push(
						this.users.filter((user) => user.DATA_ID === selectedUser)[0]
					);
				}
			});
		}
	}

	public getLocalDateFromUtc(date, timezone) {
		return new Date(moment(date).tz(timezone).format('YYYY-MM-DD HH:mm:ss'));
	}

	public addLayer() {
		const scheduleLayer = new ScheduleLayer(
			[],
			'Weekly',
			'00:00',
			false,
			new Date(),
			[],
			null
		);
		this.searchUser();
		this.schedule._layers.push(scheduleLayer);
		this.layerUsers[this.schedule._layers.length - 1] = { users: [] };
	}

	public removeLayer(layerIndex) {
		if (this.schedule._layers.length > 1) {
			this.schedule._layers.splice(layerIndex, 1);
			this.initTable();
		}
	}

	public swap(layerIndex, userIndex, direction) {
		const tempUser = this.schedule._layers[layerIndex]._users[userIndex];
		if (direction === 'up') {
			this.schedule._layers[layerIndex]._users[userIndex] =
				this.schedule._layers[layerIndex]._users[userIndex - 1];
			this.schedule._layers[layerIndex]._users[userIndex - 1] = tempUser;
		} else if (direction === 'down') {
			this.schedule._layers[layerIndex]._users[userIndex] =
				this.schedule._layers[layerIndex]._users[userIndex + 1];
			this.schedule._layers[layerIndex]._users[userIndex + 1] = tempUser;
		}
		this.initTable();
	}

	public changeCurrentView(type) {
		if (this.currentView !== type) {
			if (type === 'Week') {
				this.dayInViewEnd = new Date(this.dayInView);
				this.dayInViewEnd = new Date(
					this.dayInViewEnd.setDate(this.dayInView.getDate() + 6)
				);
			} else if (type === 'Month') {
				this.dayInViewEnd = new Date(this.dayInView);
				this.dayInViewEnd = new Date(
					this.dayInViewEnd.setDate(this.dayInView.getDate() + 29)
				);
			}
			this.currentView = type;
			this.setLayerDivs();
			this.initTable();
		}
	}

	public setLayerDivs() {
		this.layerDivs = [];
		const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
		const monthNames = [
			'Jan',
			'Feb',
			'Mar',
			'Apr',
			'May',
			'Jun',
			'July',
			'Aug',
			'Sep',
			'Oct',
			'Nov',
			'Dec',
		];

		if (this.currentView === 'Day') {
			for (let i = 0; i < 24; i++) {
				if (i % 4 === 0) {
					if (i < 10) {
						this.layerDivs.push({ LABEL: '0' + i + ':00' });
					} else {
						this.layerDivs.push({ LABEL: i + ':00' });
					}
				}
			}
		} else if (this.currentView === 'Week') {
			const date = new Date(this.dayInView);
			for (let i = 0; i < 7; i++) {
				const label =
					days[date.getDay()] +
					' ' +
					(date.getMonth() + 1) +
					'/' +
					date.getDate();
				this.layerDivs.push({ LABEL: label });
				date.setDate(date.getDate() + 1);
			}
		} else if (this.currentView === 'Month') {
			const date = new Date(this.dayInView);
			for (let i = 0; i < 30; i++) {
				const label = monthNames[date.getMonth()] + ' ' + date.getDate();
				this.layerDivs.push({ LABEL: label });
				date.setDate(date.getDate() + 1);
			}
		}
	}

	public initTable() {
		this.layerUsers = {};
		this.finalLayer.users = [];
		this.schedule._layers.forEach((layer, layerIndex) => {
			this.layerUsers[layerIndex] = { users: [] };
			if (layer._users.length > 0) {
				layer._startDate.setHours(parseInt(layer._startTime.split(':')[0], 10));
				layer._startDate.setMinutes(
					parseInt(layer._startTime.split(':')[1], 10)
				);
				layer._startDate.setSeconds(0);
				layer._startDate.setMilliseconds(0);
				let currentUser;
				const tempDate = new Date(this.dayInView);
				let limit = 0;
				if (this.currentView === 'Day') {
					limit = 48;
				} else if (this.currentView === 'Week') {
					limit = 48 * 7;
				} else if (this.currentView === 'Month') {
					limit = 48 * 30;
				}

				tempDate.setMinutes(0);
				tempDate.setHours(0);
				tempDate.setSeconds(0);
				tempDate.setMilliseconds(0);
				let marginLeft = 0;
				let width = 0;
				for (let i = 0; i < limit; i++) {
					if (!layer._hasRestrictions) {
						if (tempDate >= layer._startDate) {
							const userRotation = this.getCurrentUser(layer, tempDate, false);
							const user: any = this.usersMap[layer._users[userRotation]];
							if (!currentUser) {
								// FIRST USER
								currentUser = Object.assign({}, user);
								width = width + 100 / limit;
							} else {
								if (user.USER_UUID !== currentUser.USER_UUID) {
									currentUser.SIZE = width;
									currentUser.OFFSET = marginLeft;
									this.layerUsers[layerIndex].users.push(currentUser);
									marginLeft = 0;
									width = 100 / limit;
									currentUser = Object.assign({}, user);
								} else {
									// SAME USER KEEP ADDING WIDTH
									width = width + 100 / limit;
									if (i === limit - 1) {
										currentUser.SIZE = width;
										currentUser.OFFSET = marginLeft;
										this.layerUsers[layerIndex].users.push(currentUser);
									}
								}
							}
							tempDate.setMinutes(tempDate.getMinutes() + 30);
						} else {
							tempDate.setMinutes(tempDate.getMinutes() + 30);
							marginLeft = marginLeft + 100 / limit;
						}
					} else {
						if (tempDate >= layer._startDate) {
							const user: any =
								this.usersMap[
									layer._users[this.getCurrentUser(layer, tempDate, false)]
								];
							if (user) {
								if (!currentUser) {
									// FIRST USER
									currentUser = Object.assign({}, user);
									width = width + 100 / limit;
									if (i === limit - 1) {
										currentUser.SIZE = width;
										currentUser.OFFSET = marginLeft;
										this.layerUsers[layerIndex].users.push(currentUser);
									}
								} else {
									if (user.USER_UUID !== currentUser.USER_UUID) {
										currentUser.SIZE = width;
										currentUser.OFFSET = marginLeft;
										this.layerUsers[layerIndex].users.push(currentUser);
										width = 0;
										marginLeft = 0;
										currentUser = Object.assign({}, user);
										width = width + 100 / limit;
									} else {
										width = width + 100 / limit; // same user keep adding width
										if (i === limit - 1) {
											currentUser.SIZE = width;
											currentUser.OFFSET = marginLeft;
											this.layerUsers[layerIndex].users.push(currentUser);
										}
									}
								}
							} else {
								// GAPS
								if (currentUser) {
									currentUser.SIZE = width;
									currentUser.OFFSET = marginLeft;
									this.layerUsers[layerIndex].users.push(currentUser);
									marginLeft = 0;
									width = 0;
									marginLeft += 100 / limit;
									currentUser = undefined;
								} else {
									currentUser = undefined;
									marginLeft += 100 / limit;
								}
							}
							tempDate.setMinutes(tempDate.getMinutes() + 30);
						} else {
							tempDate.setMinutes(tempDate.getMinutes() + 30);
							marginLeft += 100 / limit;
						}
					}
				}
			}
		});

		let keepGoing = true;
		let setFinalLayer = true;
		this.schedule._layers.forEach((layer, layerIndex) => {
			if (keepGoing) {
				if (layer._users.length === 0) {
					keepGoing = false;
					setFinalLayer = false;
				}
			}
		});

		// FINAL LAYER
		if (setFinalLayer) {
			let currentUser;
			const tempDate = new Date(this.dayInView);
			let marginLeft = 0;
			let width = 0;
			let limit = 0;

			if (this.currentView === 'Day') {
				limit = 48;
			} else if (this.currentView === 'Week') {
				limit = 48 * 7;
			} else if (this.currentView === 'Month') {
				limit = 48 * 30;
			}

			tempDate.setMinutes(0);
			tempDate.setHours(0);
			tempDate.setSeconds(0);
			tempDate.setMilliseconds(0);

			for (let i = 0; i < limit; i++) {
				for (let k = this.schedule._layers.length - 1; k >= 0; k--) {
					const layer = this.schedule._layers[k];
					layer._startDate.setHours(
						parseInt(layer._startTime.split(':')[0], 10)
					);
					layer._startDate.setMinutes(
						parseInt(layer._startTime.split(':')[1], 10)
					);
					layer._startDate.setSeconds(0);
					layer._startDate.setMilliseconds(0);

					if (tempDate >= layer._startDate) {
						const user: any =
							this.usersMap[
								layer._users[this.getCurrentUser(layer, tempDate, true)]
							];
						if (user) {
							if (!currentUser) {
								// First User Logged
								width = width + 100 / limit;
								currentUser = Object.assign({}, user);
								if (i === limit - 1) {
									currentUser.SIZE = width;
									currentUser.OFFSET = marginLeft;
									this.finalLayer.users.push(currentUser);
								}
							} else {
								if (user.USER_UUID !== currentUser.USER_UUID) {
									// Checking for change of user
									currentUser.SIZE = width;
									currentUser.OFFSET = marginLeft;
									this.finalLayer.users.push(currentUser);
									width = 0;
									marginLeft = 0;
									currentUser = Object.assign({}, user);
									width = width + 100 / limit;
									if (i === limit - 1) {
										currentUser.SIZE = width;
										currentUser.OFFSET = marginLeft;
										this.finalLayer.users.push(currentUser);
									}
								} else {
									// Same user
									width = width + 100 / limit;
									if (i === limit - 1) {
										currentUser.SIZE = width;
										currentUser.OFFSET = marginLeft;
										this.finalLayer.users.push(currentUser);
									}
								}
							}
							tempDate.setMinutes(tempDate.getMinutes() + 30);
							k = -1;
						} else {
							if (k === 0) {
								if (currentUser) {
									currentUser.SIZE = width;
									currentUser.OFFSET = marginLeft;
									this.finalLayer.users.push(currentUser);
									marginLeft = 0;
									width = 0;
									currentUser = undefined;
								}
								tempDate.setMinutes(tempDate.getMinutes() + 30);
								marginLeft = marginLeft + 100 / limit;
							}
						}
					} else {
						if (k === 0) {
							// ALL LAYERS
							tempDate.setMinutes(tempDate.getMinutes() + 30);
							marginLeft = marginLeft + 100 / limit;
						}
					}
				}
			}
		}
	}

	public getCurrentUser(layer, date, finalLayer) {
		let shiftLengthInHours = 24;
		if (layer.rotationType === 'Weekly') {
			shiftLengthInHours = 24 * 7;
		}
		const startDate = layer.startDate;
		const stOff = moment(startDate.getTime()).tz(
			this.schedule._timezone
		)._offset;
		const etOff = moment(date.getTime()).tz(this.schedule._timezone)._offset;
		const st = moment(startDate.getTime() + stOff * 60 * 1000);
		const et = moment(date.getTime() + etOff * 60 * 1000);
		const users = layer.users.length;
		const hoursSinceLayerStart = moment.duration(et.diff(st)).asHours();
		const numRotationsSinceStart = Math.floor(
			hoursSinceLayerStart / shiftLengthInHours
		);
		const currentRotation = numRotationsSinceStart % users;
		let foundMatch = false;

		if (!layer.hasRestrictions) {
			return currentRotation;
		} else {
			if (layer.restrictionType === 'Day') {
				layer.restrictions.forEach((day, dayKey) => {
					let start = parseInt(day.startTime.split(':')[0], 10);
					if (parseInt(day.startTime.split(':')[1], 10) === 30) {
						start += 0.5;
					}
					let end = parseInt(day.endTime.split(':')[0], 10);
					if (parseInt(day.endTime.split(':')[1], 10) === 30) {
						end += 0.5;
					}

					const currTime = date.getHours() + date.getMinutes() / 60;

					if (
						((end - start) * 60 >= 0 && currTime >= start && currTime < end) ||
						((start - end) * 60 >= 0 &&
							((currTime >= start && currTime < end + 24) ||
								(currTime >= start - 24 && currTime < end)))
					) {
						foundMatch = true;
					}
				});
			} else if (layer.restrictionType === 'Week') {
				layer.restrictions.forEach((week, weekKey) => {
					let currDay = date.getDay();
					const currTime = date.getHours() + date.getMinutes() / 60;

					let startTime = parseInt(week.startTime.split(':')[0], 10);
					if (parseInt(week.startTime.split(':')[1], 10) === 30) {
						startTime += 0.5;
					}

					let endTime = parseInt(week.endTime.split(':')[0], 10);
					if (parseInt(week.endTime.split(':')[1], 10) === 30) {
						endTime += 0.5;
					}

					const startDay = this.getDayOfWeek(week.startDay);
					let endDay = this.getDayOfWeek(week.endDay);

					if (
						startDay > endDay ||
						(startDay === endDay && startTime >= endTime)
					) {
						if (currDay <= endDay) {
							currDay += 7;
						}
						endDay += 7;
					}

					if (startDay === currDay && endDay === currDay) {
						if (startTime <= currTime && currTime < endTime) {
							foundMatch = true;
						}
					} else if (currDay >= startDay && currDay <= endDay) {
						if (
							currDay >= 7 &&
							currDay === endDay &&
							startDay + 7 === endDay &&
							(currTime < endTime || currTime >= startTime)
						) {
							foundMatch = true;
						} else if (currDay === startDay) {
							if (currTime >= startTime) {
								foundMatch = true;
							}
						} else if (currDay === endDay) {
							if (currTime < endTime) {
								foundMatch = true;
							}
						} else {
							foundMatch = true;
						}
					}
				});
			}
		}

		if (foundMatch) {
			return currentRotation;
		}
	}

	private getDayOfWeek(day) {
		switch (day) {
			case 'Sun':
				return 0;
			case 'Mon':
				return 1;
			case 'Tue':
				return 2;
			case 'Wed':
				return 3;
			case 'Thu':
				return 4;
			case 'Fri':
				return 5;
			case 'Sat':
				return 6;
			default:
				return null;
		}
	}

	public setDates(day) {
		let incrementBy = 0;
		if (this.currentView === 'Day') {
			incrementBy = 1;
		} else if (this.currentView === 'Week') {
			incrementBy = 7;
		} else if (this.currentView === 'Month') {
			incrementBy = 30;
		}

		if (day === 'today') {
			this.dayInView = new Date();
			this.dayInViewEnd = new Date();

			if (this.currentView === 'Week') {
				this.dayInViewEnd = new Date(
					this.dayInViewEnd.setDate(this.dayInView.getDate() + 6)
				);
			} else if (this.currentView === 'Month') {
				this.dayInViewEnd = new Date(
					this.dayInViewEnd.setDate(this.dayInView.getDate() + 29)
				);
			}
		} else if (day === 'next') {
			this.dayInView = new Date(
				this.dayInView.setDate(this.dayInView.getDate() + incrementBy)
			);
			this.dayInViewEnd = new Date(
				this.dayInViewEnd.setDate(this.dayInViewEnd.getDate() + incrementBy)
			);
		} else if (day === 'previous') {
			this.dayInView = new Date(
				this.dayInView.setDate(this.dayInView.getDate() - incrementBy)
			);
			this.dayInViewEnd = new Date(
				this.dayInViewEnd.setDate(this.dayInViewEnd.getDate() - incrementBy)
			);
		}

		this.setLayerDivs();
		this.initTable();
	}

	public toggleRestrictions(layerIndex) {
		if (this.schedule._layers[layerIndex]._hasRestrictions) {
			if (this.schedule._layers[layerIndex]._restrictionType !== 'Week') {
				this.schedule._layers[layerIndex]._restrictionType = 'Day';
			}

			const dialogRef = this.dialog.open(LayerRestrictionComponent, {
				width: '600px',
				data: { layer: this.schedule._layers[layerIndex] },
				disableClose: true,
				maxHeight: '90vh',
			});
			dialogRef.afterClosed().subscribe((result) => {
				if (result) {
					this.schedule._layers[layerIndex] = result.data;
				} else {
					this.schedule._layers[layerIndex]._hasRestrictions = false;

					this.schedule._layers[layerIndex]._restrictionType = null;
				}
				this.initTable();
			});
		} else {
			this.schedule._layers[layerIndex]._restrictions = [];
			this.schedule._layers[layerIndex]._restrictionType = null;
			this.initTable();
		}
	}

	public saveSchedule() {
		this.scheduleForm.get('NAME').markAsTouched();
		if (this.scheduleForm.valid) {
			this.schedule._name = this.scheduleForm.value['NAME'];
			this.schedule._description = this.scheduleForm.value['DESCRIPTION'];
			const scheduleId = this.route.snapshot.params['scheduleId'];
			if (scheduleId !== 'new') {
				this.companiesService.putSchedule(this.schedule).subscribe(
					(response: any) => {
						this.companiesService.trackEvent(`Updated Schedule`, {
							SCHEDULE_ID: response.SCHEDULE_ID,
						});
						this.router.navigate(['schedules']);
					},
					(error) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
						this.loaderService.isLoading = false;
					}
				);
			} else {
				this.companiesService.postSchedule(this.schedule).subscribe(
					(response: any) => {
						this.companiesService.trackEvent(`Created Schedule`, {
							SCHEDULE_ID: response.SCHEDULE_ID,
						});
						if (this.paramValue == 'escNew') {
							this.router.navigate(['escalations/new']);
						} else if (
							this.escalationId != '' &&
							this.escalationId != undefined
						) {
							this.router.navigate([`escalations` + '/' + this.escalationId]);
						} else {
							this.router.navigate(['schedules']);
						}
					},
					(error) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
						this.loaderService.isLoading = false;
					}
				);
			}
		} else {
			this.loaderService.isLoading = false;
		}
	}

	public ngOnDestroy() {
		if (this.companyInfoSubscription) {
			this.companyInfoSubscription.unsubscribe();
		}
	}

	// This function will be called when we select value inside drop-down.
	public addUserToLayer(event, layerIndex) {
		this.schedule._layers[layerIndex]._users.push(event.option.value);
		this.tempUserInput = '';
		this.searchUser();
		this.initTable();
	}

	// This function will be called when cross button will be called.
	public removeUserFromLayer(layerIndex, userIndex) {
		if (userIndex >= 0) {
			this.schedule._layers[layerIndex]._users.splice(userIndex, 1);
			this.searchUser();
			this.initTable();
		}
	}

	// This function will be called when we scroll.
	public onUsersScroll() {
		this.scheduleDataScrollSubject.next([this.tempUserInput, false]);
	}

	// This function will be called when we search inside drop-down.
	public searchUser() {
		this.scheduleDataScrollSubject.next([this.tempUserInput, true]);
	}

	// This function will be called when we cancel the search input.
	public resetInput(event: MatChipInputEvent): void {
		const input = event.input;
		if (input) {
			input.value = '';
		}
	}

	public filterNewLists(type, data) {
		const newArr = [];
		data.forEach((user) => {
			const existingUser = this.users.find(
				(currentUser) => currentUser.DATA_ID === user.DATA_ID
			);
			if (!existingUser) {
				newArr.push(user);
			}
		});
		return newArr;
	}

	public initializeScheduleDataScrollSubject() {
		this.scheduleDataScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let searchValue = '';
					if (value !== '') {
						searchValue = 'EMAIL_ADDRESS' + '=' + value;
					}
					let page = 0;
					let moduleId = '';
					moduleId = this.usersModule.MODULE_ID;
					if (this.filteredUsers && !search) {
						page = Math.ceil(this.filteredUsers.length / 10);
					}
					return this.schedulesDetailService
						.getUsersData(moduleId, page, searchValue)
						.pipe(
							map((results: any) => {
								if (search) {
									this.filteredUsers = results['DATA'];
									this.filteredUsers.forEach((user, userKey) => {
										this.usersMap[user.DATA_ID] = user;
										const index = userKey % 10;
										this.usersMap[user.DATA_ID].COLORS =
											this.schedulesDetailService.colors[index];
									});
								} else {
									const newlist = this.filterNewLists('Users', results['DATA']);
									if (newlist.length > 0) {
										this.filteredUsers = this.filteredUsers.concat(newlist);
										this.filteredUsers.forEach((user, userKey) => {
											this.usersMap[user.DATA_ID] = user;
											const index = userKey % 10;
											this.usersMap[user.DATA_ID].COLORS =
												this.schedulesDetailService.colors[index];
										});
									}
								}
								this.usersLength = this.filteredUsers.length;
								return results;
							})
						);
				})
			)
			.subscribe();
	}
	public disableSelectedUsers(item, layerIndex) {
		if (this.schedule._layers[layerIndex]._users.length > 0) {
			if (this.schedule._layers[layerIndex]._users.includes(item.DATA_ID)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
