export class LayerRestriction {
	constructor(
		private START_TIME: string,
		private END_TIME: string,
		private START_DAY?: string,
		private END_DAY?: string
	) {}

	get startTime() {
		return this.START_TIME;
	}

	set startTime(value) {
		this.START_TIME = value;
	}

	get endTime() {
		return this.END_TIME;
	}

	set endTime(value) {
		this.END_TIME = value;
	}

	get startDay() {
		return this.START_DAY;
	}

	set startDay(value) {
		this.START_DAY = value;
	}

	get endDay() {
		return this.END_DAY;
	}

	set endDay(value) {
		this.END_DAY = value;
	}
}
