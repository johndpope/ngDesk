export class ScheduleLayerRestriction {
	constructor(
		private startTime: string,
		private endTime: string,
		private startDay?: string,
		private endDay?: string
	) {}

	get _startTime() {
		return this.startTime;
	}

	set _startTime(value) {
		this.startTime = value;
	}

	get _endTime() {
		return this.endTime;
	}

	set _endTime(value) {
		this.endTime = value;
	}

	get _startDay() {
		return this.startDay;
	}

	set _startDay(value) {
		this.startDay = value;
	}

	get _endDay() {
		return this.endDay;
	}

	set _endDay(value) {
		this.endDay = value;
	}
}
