export class AdditionalFields {
	constructor(
		private PLACEHOLDER: string,
		private CONTROL: string,
		private TYPE: string,
		private VALUE?: string,
		private BIND_VALUE?: string,
		private LIST?: any,
		private DISPLAY?: string,
		private ERROR?: string
	) {}

	public get placeHolder() {
		return this.PLACEHOLDER;
	}

	public set placeHolder(placeHolder) {
		this.PLACEHOLDER = placeHolder;
	}

	public get control() {
		return this.CONTROL;
	}

	public set control(control) {
		this.CONTROL = control;
	}

	public get list() {
		return this.LIST;
	}

	public set list(list) {
		this.LIST = list;
	}

	public get value() {
		return this.VALUE;
	}

	public set value(value) {
		this.VALUE = value;
	}

	public get bindValue() {
		return this.BIND_VALUE;
	}

	public set bindValue(bindValue) {
		this.BIND_VALUE = bindValue;
	}

	public get display() {
		return this.DISPLAY;
	}

	public set display(display) {
		this.DISPLAY = display;
	}

	public get error() {
		return this.ERROR;
	}

	public set error(error) {
		this.ERROR = error;
	}

	public get type() {
		return this.TYPE;
	}

	public set type(type) {
		this.TYPE = type;
	}
}
