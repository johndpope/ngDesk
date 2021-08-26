import { Condition } from './condition';

export class RoleLayout {

  constructor(
		private ROLE: string,
    private NAME: string,
    private DESCRIPTION: string,
    private IS_DEFAULT: boolean,
    private MODULES: LayoutModule[]
  ) { }

  public get role() {
    return this.ROLE;
  }

  public set role(role: string) {
    this.ROLE = role;
  }

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
	
	public get isDefault() {
    return this.IS_DEFAULT;
  }

  public set isDefault(isDefault: boolean) {
    this.IS_DEFAULT = isDefault;
	}
	
	public get modules() {
    return this.MODULES;
  }

  public set modules(modules: LayoutModule[]) {
    this.MODULES = modules;
  }
}

export class LayoutModule {
	constructor(
		private MODULE: string,
		private LIST_LAYOUT: Layout
	) { }

  public get module() {
    return this.MODULE;
  }

  public set module(module: string) {
    this.MODULE = module;
  }

  public get listLayout() {
    return this.LIST_LAYOUT;
  }

  public set listLayout(listLayout: Layout) {
    this.LIST_LAYOUT = listLayout;
  }
}

export class Layout {
	constructor(
		private ORDER_BY: OrderBy,
    private COLUMN_SHOW: ColumnShow,
		private CONDITIONS: Condition[],
	) {}

	public get orderBy() {
    return this.ORDER_BY;
  }

  public set orderBy(orderBy: OrderBy) {
    this.ORDER_BY = orderBy;
  }

  public get columnShow() {
    return this.COLUMN_SHOW;
  }

  public set columnShow(columnShow: ColumnShow) {
    this.COLUMN_SHOW = columnShow;
  }

  public get conditions() {
    return this.CONDITIONS;
  }

  public set conditions(conditions: Condition[]) {
    this.CONDITIONS = conditions;
  }
}

export class OrderBy {
  constructor(private COLUMN: string, private ORDER: string) { }

  public get column() {
    return this.COLUMN;
  }

  public set column(column: string) {
    this.COLUMN = column;
  }

  public get order() {
    return this.ORDER;
  }

  public set order(order: string) {
    this.ORDER = order;
  }
}

// TODO: keep a column type and remove the others
export class ColumnShow {
  constructor(private FIELDS: string[]) { }

  public get fields() {
    return this.FIELDS;
  }

  public set fields(fields: string[]) {
    this.FIELDS = fields;
  }
}
