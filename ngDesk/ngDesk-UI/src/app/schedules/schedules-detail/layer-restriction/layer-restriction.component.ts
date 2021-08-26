import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { ScheduleLayerRestriction } from '@src/app/models/schedule-layer-restriction';
import { SchedulesDetailService } from '@src/app/schedules/schedules-detail/schedules-detail.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';

@Component({
	selector: 'app-layer-restriction',
	templateUrl: './layer-restriction.component.html',
	styleUrls: ['./layer-restriction.component.scss'],
})
export class LayerRestrictionComponent implements OnInit {
	public times: any = [];
	public weekDays: any = [];
	public restrictionsDaily: any = [];
	public restrictionsSpecific: any = [];
	public errorMessage: String;
	public successMessage: string;
	constructor(
		public dialogRef: MatDialogRef<LayerRestrictionComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private scheduleDetailService: SchedulesDetailService,
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService
	) {}

	public ngOnInit() {
		this.times = this.scheduleDetailService.startTimes;
		this.weekDays = this.scheduleDetailService.weekDays;

		if (this.data.layer.restrictionType === 'Day') {
			if (this.data.layer.restrictions.length > 0) {
				this.restrictionsDaily = this.data.layer.restrictions;
				this.restrictionsSpecific.push(
					new ScheduleLayerRestriction('00:00', '01:00', 'Sun', 'Sun')
				);
			} else {
				// EMPTY RESTRICTIONS
				this.restrictionsDaily.push(
					new ScheduleLayerRestriction('00:00', '01:00', null, null)
				);
				this.restrictionsSpecific.push(
					new ScheduleLayerRestriction('00:00', '01:00', 'Sun', 'Sun')
				);
			}
		} else {
			if (this.data.layer.restrictions.length > 0) {
				this.restrictionsSpecific = this.data.layer.restrictions;
				this.restrictionsDaily.push(
					new ScheduleLayerRestriction('00:00', '01:00', null, null)
				);
			} else {
				// EMPTY RESTRICTIONS
				this.restrictionsDaily.push(
					new ScheduleLayerRestriction('00:00', '01:00', null, null)
				);
				this.restrictionsSpecific.push(
					new ScheduleLayerRestriction('00:00', '01:00', 'Sun', 'Sun')
				);
			}
		}
	}

	public onNoClick(): void {
		if (this.data.layer.restrictions.length === 0) {
			this.data.layer.hasRestrictions = false;
		} else {
			this.data.layer.hasRestrictions = true;
		}
		this.dialogRef.close({ data: this.data.layer });
	}

	public addRestriction(restrictionType): void {
		if (restrictionType === 'Day') {
			this.restrictionsDaily.push(
				new ScheduleLayerRestriction('00:00', '01:00', null, null)
			);
		} else if (restrictionType === 'Week') {
			this.restrictionsSpecific.push(
				new ScheduleLayerRestriction('00:00', '01:00', 'Sun', 'Sun')
			);
		}
	}

	public removeRestriction(restrictionType, index): void {
		if (restrictionType === 'Day') {
			this.restrictionsDaily.splice(index, 1);
		} else if (restrictionType === 'Week') {
			this.restrictionsSpecific.splice(index, 1);
		}
	}

	private getDate(): Date {
		const tempDate = new Date();
		tempDate.setHours(0);
		tempDate.setMinutes(0);
		tempDate.setSeconds(0);
		tempDate.setMilliseconds(0);

		return tempDate;
	}

	private getDayValue(day): number {
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

	// This function returns an array of restrictions in ascending order, helps in determining crazy overlapping conditions
	// ex: for daily if 09:00 - 03:00 is given then it returns 09:00 - 00:00 and 00:00 - 03:00
	// incase of weekly if Tue 09:00 - Mon 03:00 is given then it returns Tue 09:00 - Sun 00:00 and Sun 00:00 - Mon 03:00
	private sortRestrictions(): any {
		if (this.data.layer.restrictionType === 'Day') {
			console.log('hit for day');
			const temp: any = [];
			this.restrictionsDaily.forEach((restriction, restrictionIndex) => {
				let st = parseInt(restriction.startTime.split(':')[0], 10);
				if (parseInt(restriction.startTime.split(':')[1], 10) === 30) {
					st += 0.5;
				}

				let end = parseInt(restriction.endTime.split(':')[0], 10);
				if (parseInt(restriction.endTime.split(':')[1], 10) === 30) {
					end += 0.5;
				}

				if (st >= end && end !== 0) {
					temp.push(
						new ScheduleLayerRestriction(
							restriction.START_TIME,
							'00:00',
							null,
							null
						)
					);
					temp.push(
						new ScheduleLayerRestriction(
							'00:00',
							restriction.END_TIME,
							null,
							null
						)
					);
				} else if (st >= end && end === 0) {
					temp.push(
						new ScheduleLayerRestriction(
							restriction.START_TIME,
							'00:00',
							null,
							null
						)
					);
				} else {
					temp.push(restriction);
				}
			});
			return temp;
		} else if (this.data.layer.restrictionType === 'Week') {
			console.log('hit for week');
			const temp: any = [];
			this.restrictionsSpecific.forEach((restriction, restrictionIndex) => {
				const START_DAY = this.getDayValue(restriction.startDay);
				let END_DAY = this.getDayValue(restriction.endDay);

				let START_TIME = parseInt(restriction.startTime.split(':')[0], 10);
				if (parseInt(restriction.startTime.split(':')[1], 10) === 30) {
					START_TIME += 0.5;
				}

				let END_TIME = parseInt(restriction.endTime.split(':')[0], 10);
				if (parseInt(restriction.endTime.split(':')[1], 10) === 30) {
					END_TIME += 0.5;
				}

				if (START_DAY !== 0 && END_DAY === 0) {
					END_DAY = 7;
				}

				if (START_DAY === 0 && END_DAY === 0) {
					if (START_TIME < END_TIME) {
						temp.push(restriction);
					} else {
						temp.push(
							new ScheduleLayerRestriction(
								restriction.startTime,
								'00:00',
								restriction.startDay,
								'Sun'
							)
						);
						temp.push(
							new ScheduleLayerRestriction(
								'00:00',
								restriction.endTime,
								'Sun',
								restriction.endDay
							)
						);
					}
				} else {
					if (START_DAY < END_DAY) {
						temp.push(restriction);
					} else if (START_DAY > END_DAY) {
						temp.push(
							new ScheduleLayerRestriction(
								restriction.startTime,
								'00:00',
								restriction.startDay,
								'Sun'
							)
						);
						temp.push(
							new ScheduleLayerRestriction(
								'00:00',
								restriction.endTime,
								'Sun',
								restriction.endDay
							)
						);
					} else {
						// start and end days are same
						if (START_TIME < END_TIME) {
							temp.push(restriction);
						} else {
							temp.push(
								new ScheduleLayerRestriction(
									restriction.startTime,
									'00:00',
									restriction.startDay,
									'Sun'
								)
							);
							temp.push(
								new ScheduleLayerRestriction(
									'00:00',
									restriction.endTime,
									'Sun',
									restriction.endDay
								)
							);
						}
					}
				}
			});
			return temp;
		}
	}

	public checkForSameRestrictions(type): boolean {
		if (type === 'Day') {
			let keepGoing = true;
			let hasSameRestrictions = false;
			this.restrictionsDaily.forEach((restriction, restrictionIndex) => {
				if (keepGoing) {
					if (restriction.startTime === restriction.endTime) {
						keepGoing = false;
						hasSameRestrictions = true;
					}
				}
			});
			return hasSameRestrictions;
		} else if (type === 'Week') {
			let keepGoing = true;
			let hasSameRestrictions = false;
			this.restrictionsSpecific.forEach((restriction, restrictionIndex) => {
				if (keepGoing) {
					console.log(
						restriction.startTime,
						restriction.endTime,
						restriction.startDay,
						restriction.endDay
					);
					console.log(
						'if',
						restriction.startTime === restriction.endTime &&
							restriction.startDay === restriction.endDay
					);
					if (
						restriction.startTime === restriction.endTime &&
						restriction.startDay === restriction.endDay
					) {
						keepGoing = false;
						hasSameRestrictions = true;
						console.log('inside if condition', hasSameRestrictions);
					}
				}
			});
			console.log('outside', hasSameRestrictions);
			return hasSameRestrictions;
		}
	}

	public saveRestrictions(): void {
		if (this.data.layer.restrictionType === 'Day') {
			if (!this.checkForSameRestrictions(this.data.layer.restrictionType)) {
				// NO SAME RESTRICTIONS
				const temp: any = this.sortRestrictions();
				let validRestrictions = true;

				outerloop: for (let i = 0; i < temp.length - 1; i++) {
					let st = parseInt(temp[i].startTime.split(':')[0], 10);
					if (parseInt(temp[i].startTime.split(':')[1], 10) === 30) {
						st += 0.5;
					}

					let end = parseInt(temp[i].endTime.split(':')[0], 10);
					if (parseInt(temp[i].endTime.split(':')[1], 10) === 30) {
						end += 0.5;
					}

					if (
						temp[i].endTime.split(':')[0] === '00' &&
						temp[i].endTime.split(':')[1] === '00'
					) {
						end = 24;
					}

					for (let j = i + 1; j < temp.length; j++) {
						let daySt = parseInt(temp[j].startTime.split(':')[0], 10);
						if (parseInt(temp[j].startTime.split(':')[1], 10) === 30) {
							daySt += 0.5;
						}

						let dayEnd = parseInt(temp[j].endTime.split(':')[0], 10);
						if (parseInt(temp[j].endTime.split(':')[1], 10) === 30) {
							dayEnd += 0.5;
						}

						if (st >= daySt && st < dayEnd) {
							validRestrictions = false;
							break outerloop;
						} else if (daySt >= st && daySt < end) {
							validRestrictions = false;
							break outerloop;
						}
					}
				}

				if (validRestrictions) {
					this.data.layer.hasRestrictions = true;
					this.data.layer.restrictions = this.restrictionsDaily;
					this.dialogRef.close({ data: this.data.layer });
				} else {
					this.bannerMessageService.errorNotifications.push({
						message: this.translateService.instant(
							'RESTRICTIONS_MORE_THAN_ONE'
						),
					});
				}
			} else {
				// HAS SAME RESTRICTIONS
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('RESTRICTIONS_SIMILAR'),
				});
			}
		} else if (this.data.layer.restrictionType === 'Week') {
			console.log('week type nn', this.checkForSameRestrictions);
			console.log('data layer instruction', this.data.layer.restrictionType);
			console.log();
			console.log(
				!this.checkForSameRestrictions(this.data.layer.restrictionType)
			);
			if (!this.checkForSameRestrictions(this.data.layer.restrictionType)) {
				console.log('hit week calculation');
				// NO SAME RESTRICTIONS
				const temp: any = this.sortRestrictions();
				let validRestrictions = true;
				outerloop: for (let i = 0; i < temp.length; i++) {
					const START_DAY = this.getDayValue(temp[i].startDay);
					const END_DAY = this.getDayValue(temp[i].endDay);

					let START_TIME = parseInt(temp[i].startTime.split(':')[0], 10);
					if (parseInt(temp[i].startTime.split(':')[1], 10) === 30) {
						START_TIME += 0.5;
					}

					let END_TIME = parseInt(temp[i].endTime.split(':')[0], 10);
					if (parseInt(temp[i].endTime.split(':')[1], 10) === 30) {
						END_TIME += 0.5;
					}

					const startDate = this.getDate();
					const endDate = this.getDate();

					startDate.setHours(parseInt(temp[i].startTime.split(':')[0], 10));
					startDate.setMinutes(parseInt(temp[i].startTime.split(':')[1], 10));

					endDate.setHours(parseInt(temp[i].endTime.split(':')[0], 10));
					endDate.setMinutes(parseInt(temp[i].endTime.split(':')[1], 10));

					if (START_DAY === 0 && END_DAY === 0 && START_TIME >= END_TIME) {
						startDate.setDate(
							startDate.getDate() + START_DAY - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + END_DAY + 7 - endDate.getDay());
					} else if (START_DAY !== startDate.getDay() && START_DAY > END_DAY) {
						startDate.setDate(
							startDate.getDate() + START_DAY - 7 - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + END_DAY - endDate.getDay());
					} else if (START_DAY === startDate.getDay() && START_DAY > END_DAY) {
						startDate.setDate(
							startDate.getDate() + START_DAY - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + END_DAY + 7 - endDate.getDay());
					} else if (START_DAY === END_DAY && START_TIME >= END_TIME) {
						startDate.setDate(
							startDate.getDate() + START_DAY - 7 - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + END_DAY - endDate.getDay());
					} else {
						startDate.setDate(
							startDate.getDate() + START_DAY - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + END_DAY - endDate.getDay());
					}

					for (let j = i + 1; j < temp.length; j++) {
						const J_START_DAY = this.getDayValue(temp[j].startDay);
						const J_END_DAY = this.getDayValue(temp[j].endDay);

						let J_START_TIME = parseInt(temp[j].startTime.split(':')[0], 10);
						if (parseInt(temp[j].startTime.split(':')[j], 10) === 30) {
							J_START_TIME += 0.5;
						}

						let J_END_TIME = parseInt(temp[j].endTime.split(':')[0], 10);
						if (parseInt(temp[j].endTime.split(':')[j], 10) === 30) {
							J_END_TIME += 0.5;
						}

						const stDate = this.getDate();
						const etDate = this.getDate();

						stDate.setHours(parseInt(temp[j].startTime.split(':')[0], 10));
						stDate.setMinutes(parseInt(temp[j].startTime.split(':')[1], 10));

						etDate.setHours(parseInt(temp[j].endTime.split(':')[0], 10));
						etDate.setMinutes(parseInt(temp[j].endTime.split(':')[1], 10));

						if (
							J_START_DAY === 0 &&
							J_END_DAY === 0 &&
							J_START_TIME >= J_END_TIME
						) {
							stDate.setDate(stDate.getDate() + J_START_DAY - stDate.getDay());
							etDate.setDate(
								etDate.getDate() + J_END_DAY + 7 - etDate.getDay()
							);
						} else if (
							J_START_DAY > J_END_DAY ||
							(J_START_DAY === J_END_DAY && J_START_TIME >= J_END_TIME)
						) {
							stDate.setDate(
								stDate.getDate() + J_START_DAY - 7 - stDate.getDay()
							);
							etDate.setDate(etDate.getDate() + J_END_DAY - etDate.getDay());
						} else {
							stDate.setDate(stDate.getDate() + J_START_DAY - stDate.getDay());
							etDate.setDate(etDate.getDate() + J_END_DAY - etDate.getDay());
						}

						if (startDate > stDate && startDate < etDate) {
							validRestrictions = false;
							break outerloop;
						} else if (stDate > startDate && stDate < endDate) {
							validRestrictions = false;
							break outerloop;
						}
					}
				}
				if (validRestrictions) {
					this.data.layer.hasRestrictions = true;
					this.data.layer.restrictions = this.restrictionsSpecific;
					this.dialogRef.close({ data: this.data.layer });
				} else {
					// RESTRICTION ARE EXCLUSIVE
					console.log('hit');
					this.bannerMessageService.errorNotifications.push({
						message: this.translateService.instant(
							'RESTRICTIONS_MORE_THAN_ONE'
						),
					});
					console.log('Restrictions Exclusive');
				}
			} else {
				// HAS SAME RESTRICTIONS
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('RESTRICTIONS_SIMILAR'),
				});
			}
		}
	}
}
