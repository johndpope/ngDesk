import { ScheduleLayerRestriction } from '@src/app/models/schedule-layer-restriction';

export class ScheduleLayer {
	constructor(
		private users: string[],
		private rotationType: string,
		private startTime: string,
		private hasRestrictions: boolean,
		private startDate: Date,
		private restrictions: ScheduleLayerRestriction[],
		private restrictionType?: string
	) {}

	get _users() {
		return this.users;
	}

	set _users(value) {
		this.users = value;
	}

	get _rotationType() {
		return this.rotationType;
	}

	set _rotationType(value) {
		this.rotationType = value;
	}

	get _startTime() {
		return this.startTime;
	}

	set _startTime(value) {
		this.startTime = value;
	}

	get _hasRestrictions() {
		return this.hasRestrictions;
	}

	set _hasRestrictions(value) {
		this.hasRestrictions = value;
	}

	get _restrictionType() {
		return this.restrictionType;
	}

	set _restrictionType(value) {
		this.restrictionType = value;
	}

	get _startDate() {
		return this.startDate;
	}

	set _startDate(value) {
		this.startDate = value;
	}

	get _restrictions() {
		return this.restrictions;
	}

	set _restrictions(value) {
		this.restrictions = value;
	}
}
