export class SMSChannel {
	constructor(
		private NAME: string,
		private DESCRIPTION: string,
		private WHATSAPP_ENABLED: boolean,
		public WHATSAPP_REQUESTED: boolean,
		private PHONE_NUMBER?: string,
		private VERIFIED?: boolean,
		private CHANNEL_ID?: string,
		private MODULE?: string,
		private DATE_CREATED?: Date,
		private LAST_UPDATED_BY?: string,
		private CREATED_BY?: string,
		private DATE_UPDATED?: Date
	) {}

	public get description() {
		return this.DESCRIPTION;
	}

	public set description(description: string) {
		this.DESCRIPTION = description;
	}

	public get phoneNumber() {
		return this.PHONE_NUMBER;
	}

	public set phoneNumber(phoneNumber: string) {
		this.PHONE_NUMBER = phoneNumber;
	}

	public get verified() {
		return this.VERIFIED;
	}

	public set verified(verified: boolean) {
		this.VERIFIED = verified;
	}

	public get name() {
		return this.NAME;
	}

	public set name(name: string) {
		this.NAME = name;
	}

	public get channelId() {
		return this.CHANNEL_ID;
	}

	public set channelId(channelId: string) {
		this.CHANNEL_ID = channelId;
	}

	public get dateCreated() {
		return this.DATE_CREATED;
	}

	public set dateCreated(dateCreated: Date) {
		this.DATE_CREATED = dateCreated;
	}

	public get lastUpdatedBy() {
		return this.LAST_UPDATED_BY;
	}

	public set lastUpdatedBy(lastUpdatedBy: any) {
		this.LAST_UPDATED_BY = lastUpdatedBy;
	}

	public get createdBy() {
		return this.CREATED_BY;
	}

	public set createdBy(createdBy: any) {
		this.CREATED_BY = createdBy;
	}

	public get dateUpdated() {
		return this.DATE_UPDATED;
	}

	public set dateUpdated(dateUpdated: Date) {
		this.DATE_UPDATED = dateUpdated;
	}

	public get module() {
		return this.MODULE;
	}

	public set module(module: string) {
		this.MODULE = module;
	}

	public get whatsappRequested() {
		return this.WHATSAPP_REQUESTED;
	}

	public set whatsappRequested(whatsappRequested: boolean) {
		this.WHATSAPP_REQUESTED = whatsappRequested;
	}

	public get whatsappEnabled() {
		return this.WHATSAPP_ENABLED;
	}

	public set whatsappEnabled(whatsappEnabled: boolean) {
		this.WHATSAPP_ENABLED = whatsappEnabled;
	}
}
