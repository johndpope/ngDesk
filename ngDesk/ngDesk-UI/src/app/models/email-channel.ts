import { ModuleMapping } from './moduleMapping';

export class EmailChannel {
	constructor(
		public NAME: string,
		public SOURCE_TYPE: string,
		public MODULE: string,
		public DESCRIPTION: string,
		public EMAIL_ADDRESS: string,
		public TYPE: string,
		public CHANNEL_ID?: string,
		public IS_VERIFIED?: boolean,
		public WORKFLOW?: any,
		public DATE_CREATED?: Date,
		public LAST_UPDATED_BY?: any,
		public DATE_UPDATED?: Date,
		public CREATE_MAPPING?: ModuleMapping,
		public UPDATE_MAPPING?: ModuleMapping
	) {}

	public get emailAddress() {
		return this.EMAIL_ADDRESS;
	}

	public set emailAddress(emailAddress: string) {
		this.EMAIL_ADDRESS = emailAddress;
	}

	public get description() {
		return this.DESCRIPTION;
	}

	public set description(description: string) {
		this.DESCRIPTION = description;
	}

	public get sourceType() {
		return this.SOURCE_TYPE;
	}

	public set sourceType(sourceType: string) {
		this.SOURCE_TYPE = sourceType;
	}

	public get module() {
		return this.MODULE;
	}

	public set module(module: string) {
		this.MODULE = module;
	}

	public get isVerified() {
		return this.IS_VERIFIED;
	}

	public set isVerified(isVerified: boolean) {
		this.IS_VERIFIED = isVerified;
	}

	public get type() {
		return this.TYPE;
	}

	public set type(type: string) {
		this.TYPE = type;
	}

	public get workflow() {
		return this.WORKFLOW;
	}

	public set workflow(workflow: any) {
		this.WORKFLOW = workflow;
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

	public get dateUpdated() {
		return this.DATE_UPDATED;
	}

	public set dateUpdated(dateUpdated: Date) {
		this.DATE_UPDATED = dateUpdated;
	}
	public get createMapping() {
		return this.CREATE_MAPPING;
	}

	public set createMapping(createMapping: ModuleMapping) {
		this.CREATE_MAPPING = createMapping;
	}

	public get updateMapping() {
		return this.CREATE_MAPPING;
	}

	public set updateMapping(updateMapping: ModuleMapping) {
		this.UPDATE_MAPPING = updateMapping;
	}
}
