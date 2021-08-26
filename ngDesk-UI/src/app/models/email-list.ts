import { Condition } from './condition';

export class EmailList {
	constructor(
		private NAME: string,
		private DESCRIPTION: string,
		private CONDITIONS: Condition[],
		private EMAIL_LIST_ID?: string,
		private DATE_CREATED?: string,
		private DATE_UPDATED?: string,
		private LAST_UPDATED_BY?: string,
		private CREATED_BY?: string
	) {}

	public get name() {
		return this.NAME;
	}

	public set name(name: string) {
		this.NAME = name;
	}

	public get description() {
		return this.DESCRIPTION;
	}

	public set description(description: string) {
		this.DESCRIPTION = description;
	}

	public get emailListId() {
		return this.EMAIL_LIST_ID;
	}

	public set emailListId(emailListId: string) {
		this.EMAIL_LIST_ID = emailListId;
	}

	public get conditions() {
		return this.CONDITIONS;
	}

	public set conditions(conditions: Condition[]) {
		this.CONDITIONS = conditions;
	}

	public get dateCreated() {
		return this.DATE_CREATED;
	}

	public set dateCreated(dateCreated: string) {
		this.DATE_CREATED = dateCreated;
	}

	public get dateUpdated() {
		return this.DATE_UPDATED;
	}

	public set dateUpdated(dateUpdated: string) {
		this.DATE_UPDATED = dateUpdated;
	}

	public get lastUpdatedBy() {
		return this.LAST_UPDATED_BY;
	}

	public set lastUpdatedBy(lastUpdatedBy: string) {
		this.LAST_UPDATED_BY = lastUpdatedBy;
	}

	public get createdBy() {
		return this.CREATED_BY;
	}

	public set createdBy(createdBy: string) {
		this.CREATED_BY = createdBy;
	}
}
