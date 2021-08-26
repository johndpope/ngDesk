import { DceColumn } from './dce-column';

export class DceLayout {
	constructor(
		private NAME: string,
		private DESCRIPTION: string,
		private ROLE: string,
		private DATE_CREATED?: Date,
		private DATE_UPDATED?: Date,
		private LAST_UPDATED_BY?: string,
		private CREATED_BY?: string,
		private COLUMNS?: DceColumn[],
		private CUSTOM_LAYOUT?: string,
		private LAYOUT_ID?: string,
		private GRIDS?: any[][],
		private PREDEFINED_TEMPLATE?: any[]
	) {}

	get predefinedTemplate() {
		return this.PREDEFINED_TEMPLATE;
	}

	set predefinedTemplate(predefinedTemplate) {
		this.PREDEFINED_TEMPLATE = predefinedTemplate;
	}
	get grids() {
		return this.GRIDS;
	}

	set grids(grids) {
		this.GRIDS = grids;
	}

	get customLayout() {
		return this.CUSTOM_LAYOUT;
	}

	set customLayout(customLayout) {
		this.CUSTOM_LAYOUT = customLayout;
	}

	get layoutId() {
		return this.LAYOUT_ID;
	}

	set layoutId(value) {
		this.LAYOUT_ID = value;
	}

	get name() {
		return this.NAME;
	}

	set name(value) {
		this.NAME = value;
	}

	get description() {
		return this.DESCRIPTION;
	}

	set description(value) {
		this.DESCRIPTION = value;
	}

	get columns() {
		return this.COLUMNS;
	}

	set columns(value) {
		this.COLUMNS = value;
	}

	get role() {
		return this.ROLE;
	}

	set role(value) {
		this.ROLE = value;
	}

	get dateCreated() {
		return this.DATE_CREATED;
	}

	set dateCreated(value) {
		this.DATE_CREATED = value;
	}

	get dateUpdated() {
		return this.DATE_UPDATED;
	}

	set dateUpdated(value) {
		this.DATE_UPDATED = value;
	}

	get lastUpdatedBy() {
		return this.LAST_UPDATED_BY;
	}

	set lastUpdatedBy(value) {
		this.LAST_UPDATED_BY = value;
	}

	get createdBy() {
		return this.CREATED_BY;
	}

	set createdBy(value) {
		this.CREATED_BY = value;
	}
}
