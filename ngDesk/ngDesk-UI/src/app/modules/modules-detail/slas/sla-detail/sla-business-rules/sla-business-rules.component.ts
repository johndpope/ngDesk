import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BannerMessageService } from '../../../../../custom-components/banner-message/banner-message.service';
import { SchedulesDetailService } from '../../../../../schedules/schedules-detail/schedules-detail.service';
import { SLARestriction } from '@ngdesk/module-api';
@Component({
	selector: 'app-sla-business-rules',
	templateUrl: './sla-business-rules.component.html',
	styleUrls: ['./sla-business-rules.component.scss'],
})
export class SlaBusinessRulesComponent implements OnInit {
	public times: any = [];
	public weekDays: any = [];
	public restrictionsDaily: any = [];
	public restrictionsSpecific: any = [];
	public errorMessage: String = '';
	public restriction: SLARestriction = {
		startTime: '',
		endTime: '',
		startDay: '',
		endDay: '',
	};
	constructor(
		public dialogRef: MatDialogRef<SlaBusinessRulesComponent>,
		public scheduleDetailService: SchedulesDetailService,
		public modalBannerMessageService: BannerMessageService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {}

	public ngOnInit() {
		this.times = this.scheduleDetailService.startTimes;
		this.weekDays = this.scheduleDetailService.weekDays;
		if (this.data.businessRuleValue.restrictionType === 'Day') {
			if (this.data.businessRuleValue.restrictions.length > 0) {
				this.restrictionsDaily = this.data.businessRuleValue.restrictions;
				this.restrictionsSpecific.push(
					(this.restriction = {
						startTime: '00:00',
						endTime: '01:00',
						startDay: 'Sun',
						endDay: 'Sun',
					})
				);
			} else {
				// EMPTY RESTRICTIONS
				this.restrictionsDaily.push(
					(this.restriction = {
						startTime: '00:00',
						endTime: '01:00',
						startDay: null,
						endDay: null,
					})
				);
				this.restrictionsSpecific.push(
					(this.restriction = {
						startTime: '00:00',
						endTime: '01:00',
						startDay: 'Sun',
						endDay: 'Sun',
					})
				);
			}
		} else {
			if (this.data.businessRuleValue.restrictions.length > 0) {
				this.restrictionsSpecific = this.data.businessRuleValue.restrictions;
				this.restrictionsDaily.push(
					(this.restriction = {
						startTime: '00:00',
						endTime: '01:00',
						startDay: null,
						endDay: null,
					})
				);
			} else {
				// EMPTY RESTRICTIONS
				this.restrictionsDaily.push(
					(this.restriction = {
						startTime: '00:00',
						endTime: '01:00',
						startDay: null,
						endDay: null,
					})
				);
				this.restrictionsSpecific.push(
					(this.restriction = {
						startTime: '00:00',
						endTime: '01:00',
						startDay: 'Sun',
						endDay: 'Sun',
					})
				);
			}
		}
	}

	public addRestriction(restrictionType): void {
		if (restrictionType === 'Day') {
			this.restrictionsDaily.push(
				(this.restriction = {
					startTime: '00:00',
					endTime: '01:00',
					startDay: null,
					endDay: null,
				})
			);
		} else if (restrictionType === 'Week') {
			this.restrictionsSpecific.push(
				(this.restriction = {
					startTime: '00:00',
					endTime: '01:00',
					startDay: 'Sun',
					endDay: 'Sun',
				})
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

	public onNoClick(): void {
		if (this.data.businessRuleValue.restrictions.length === 0) {
			this.data.isRestrictedValue = false;
		} else {
			this.data.isRestrictedValue = true;
		}
		const resultValue = {
			businessRuleValue: this.data.businessRuleValue,
			isRestrictedValue: this.data.isRestrictedValue,
		};
		this.dialogRef.close({ data: resultValue });
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
		if (this.data.businessRuleValue.restrictionType === 'Day') {
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
						(this.restriction = {
							startTime: restriction.startTime,
							endTime: '00:00',
							startDay: null,
							endDay: null,
						})
					);
					temp.push(
						(this.restriction = {
							startTime: '00:00',
							endTime: restriction.endTime,
							startDay: null,
							endDay: null,
						})
					);
				} else if (st >= end && end === 0) {
					temp.push(
						(this.restriction = {
							startTime: restriction.startTime,
							endTime: '00:00',
							startDay: null,
							endDay: null,
						})
					);
				} else {
					temp.push(restriction);
				}
			});
			return temp;
		} else if (this.data.businessRuleValue.restrictionType === 'Week') {
			const temp: any = [];
			this.restrictionsSpecific.forEach((restriction, restrictionIndex) => {
				const startDay = this.getDayValue(restriction.startDay);
				let endDay = this.getDayValue(restriction.endDay);

				let startTime = parseInt(restriction.startTime.split(':')[0], 10);
				if (parseInt(restriction.startTime.split(':')[1], 10) === 30) {
					startTime += 0.5;
				}

				let endTime = parseInt(restriction.endTime.split(':')[0], 10);
				if (parseInt(restriction.endTime.split(':')[1], 10) === 30) {
					endTime += 0.5;
				}

				if (startDay !== 0 && endDay === 0) {
					endDay = 7;
				}

				if (startDay === 0 && endDay === 0) {
					if (startTime < endTime) {
						temp.push(restriction);
					} else {
						temp.push(
							(this.restriction = {
								startTime: restriction.startTime,
								endTime: '00:00',
								startDay: restriction.startDay,
								endDay: 'Sun',
							})
						);
						temp.push(
							(this.restriction = {
								startTime: '00:00',
								endTime: restriction.endTime,
								startDay: 'Sun',
								endDay: restriction.endDay,
							})
						);
					}
				} else {
					if (startDay < endDay) {
						temp.push(restriction);
					} else if (startDay > endDay) {
						temp.push(
							(this.restriction = {
								startTime: restriction.startTime,
								endTime: '00:00',
								startDay: restriction.startDay,
								endDay: 'Sun',
							})
						);
						temp.push(
							(this.restriction = {
								startTime: '00:00',
								endTime: restriction.endTime,
								startDay: 'Sun',
								endDay: restriction.endDay,
							})
						);
					} else {
						// start and end days are same
						if (startTime < endTime) {
							temp.push(restriction);
						} else {
							temp.push(
								(this.restriction = {
									startTime: restriction.startTime,
									endTime: '00:00',
									startDay: restriction.startDay,
									endDay: 'Sun',
								})
							);
							temp.push(
								(this.restriction = {
									startTime: '00:00',
									endTime: restriction.endTime,
									startDay: 'Sun',
									endDay: restriction.endDay,
								})
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
					if (
						restriction.startTime === restriction.endTime &&
						restriction.startDay === restriction.endDay
					) {
						keepGoing = false;
						hasSameRestrictions = true;
					}
				}
			});
			return hasSameRestrictions;
		}
	}

	public saveRestrictions(): void {
		if (this.data.businessRuleValue.restrictionType === 'Day') {
			if (
				!this.checkForSameRestrictions(
					this.data.businessRuleValue.restrictionType
				)
			) {
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
					// this.data.isRestrictedValue = true;
					this.data.businessRuleValue.restrictions = this.restrictionsDaily;
					const resultValue = {
						businessRuleValue: this.data.businessRuleValue,
						isRestrictedValue: this.data.isRestrictedValue,
					};
					this.dialogRef.close({ data: resultValue });
				} else {
					// TODO: Get Error displaying on banner
					this.errorMessage =
						'You have one or more business Rules which are overlapping please review';
					console.log('Business Rules Exclusive');
				}
			} else {
				// HAS SAME RESTRICTIONS
				// TODO: Get Error Displaying on Banner
				this.errorMessage =
					'You have one or more similar business Rules please review';
				console.log('Business Rules Same');
			}
		} else if (this.data.businessRuleValue.restrictionType === 'Week') {
			if (
				!this.checkForSameRestrictions(
					this.data.businessRuleValue.restrictionType
				)
			) {
				// NO SAME RESTRICTIONS
				const temp: any = this.sortRestrictions();
				let validRestrictions = true;
				outerloop: for (let i = 0; i < temp.length; i++) {
					const startDay = this.getDayValue(temp[i].startDay);
					const endDay = this.getDayValue(temp[i].endDay);

					let startTime = parseInt(temp[i].startTime.split(':')[0], 10);
					if (parseInt(temp[i].startTime.split(':')[1], 10) === 30) {
						startTime += 0.5;
					}

					let endTime = parseInt(temp[i].endTime.split(':')[0], 10);
					if (parseInt(temp[i].endTime.split(':')[1], 10) === 30) {
						endTime += 0.5;
					}

					const startDate = this.getDate();
					const endDate = this.getDate();

					startDate.setHours(parseInt(temp[i].startTime.split(':')[0], 10));
					startDate.setMinutes(parseInt(temp[i].startTime.split(':')[1], 10));

					endDate.setHours(parseInt(temp[i].endTime.split(':')[0], 10));
					endDate.setMinutes(parseInt(temp[i].endTime.split(':')[1], 10));

					if (startDay === 0 && endDay === 0 && startTime >= endTime) {
						startDate.setDate(
							startDate.getDate() + startDay - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + endDay + 7 - endDate.getDay());
					} else if (startDay !== startDate.getDay() && startDay > endDay) {
						startDate.setDate(
							startDate.getDate() + startDay - 7 - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + endDay - endDate.getDay());
					} else if (startDay === startDate.getDay() && startDay > endDay) {
						startDate.setDate(
							startDate.getDate() + startDay - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + endDay + 7 - endDate.getDay());
					} else if (startDay === endDay && startTime >= endTime) {
						startDate.setDate(
							startDate.getDate() + startDay - 7 - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + endDay - endDate.getDay());
					} else {
						startDate.setDate(
							startDate.getDate() + startDay - startDate.getDay()
						);
						endDate.setDate(endDate.getDate() + endDay - endDate.getDay());
					}

					for (let j = i + 1; j < temp.length; j++) {
						const j_startDay = this.getDayValue(temp[j].startDay);
						const j_endDay = this.getDayValue(temp[j].endDay);

						let i_startTime = parseInt(temp[j].startTime.split(':')[0], 10);
						if (parseInt(temp[j].startTime.split(':')[j], 10) === 30) {
							i_startTime += 0.5;
						}

						let j_endTime = parseInt(temp[j].endTime.split(':')[0], 10);
						if (parseInt(temp[j].endTime.split(':')[j], 10) === 30) {
							j_endTime += 0.5;
						}

						const stDate = this.getDate();
						const etDate = this.getDate();

						stDate.setHours(parseInt(temp[j].startTime.split(':')[0], 10));
						stDate.setMinutes(parseInt(temp[j].startTime.split(':')[1], 10));

						etDate.setHours(parseInt(temp[j].endTime.split(':')[0], 10));
						etDate.setMinutes(parseInt(temp[j].endTime.split(':')[1], 10));

						if (
							j_startDay === 0 &&
							j_endDay === 0 &&
							i_startTime >= j_endTime
						) {
							stDate.setDate(stDate.getDate() + j_startDay - stDate.getDay());
							etDate.setDate(etDate.getDate() + j_endDay + 7 - etDate.getDay());
						} else if (
							j_startDay > j_endDay ||
							(j_startDay === j_endDay && i_startTime >= j_endTime)
						) {
							stDate.setDate(
								stDate.getDate() + j_startDay - 7 - stDate.getDay()
							);
							etDate.setDate(etDate.getDate() + j_endDay - etDate.getDay());
						} else {
							stDate.setDate(stDate.getDate() + j_startDay - stDate.getDay());
							etDate.setDate(etDate.getDate() + j_endDay - etDate.getDay());
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
					// this.data.isRestrictedValue = true;
					this.data.businessRuleValue.restrictions = this.restrictionsSpecific;
					const resultValue = {
						businessRuleValue: this.data.businessRuleValue,
						isRestrictedValue: this.data.isRestrictedValue,
					};
					this.dialogRef.close({ data: resultValue });
				} else {
					// TODO: Get Error displaying on banner
					// this.modalBannerMessageService.errors.push(this.global.translations.RESTRICTION_CANNOT_BE_EXCLUSIVE);
					this.errorMessage =
						'You have one or more business Rules which are overlapping please review';
					console.log('Restrictions Exclusive');
				}
			} else {
				// HAS SAME RESTRICTIONS
				// TODO: Get Error displaying on banner
				// this.modalBannerMessageService.errors.push(this.global.translations.RESTRICTION_CANNOT_BE_SAME);
				this.errorMessage =
					'You have one or more similar business Rules please review';
				console.log('Restrictions Same');
			}
		} else {
			this.errorMessage = 'Please select valid type';
		}
	}
}
