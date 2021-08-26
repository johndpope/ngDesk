import { ScheduleLayer } from '@src/app/models/schedule-layer';

export class Schedule {
	constructor(
		private name: string,
		private description: string,
		public timezone: string,
		public layers: ScheduleLayer[],
		private scheduleId?: string
	) {}

	get _name() {
		return this.name;
	}

	set _name(value) {
		this.name = value;
	}

	get _description() {
		return this.description;
	}

	set _description(value) {
		this.description = value;
	}

	get _timezone() {
		return this.timezone;
	}

	set _timezone(value) {
		this.timezone = value;
	}

	get _layers() {
		return this.layers;
	}

	set _layers(value) {
		this.layers = value;
	}

	get _scheduleId() {
		return this.scheduleId;
	}

	set _scheduleId(value) {
		this.scheduleId = value;
	}
}
