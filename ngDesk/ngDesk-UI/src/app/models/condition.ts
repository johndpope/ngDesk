export class Condition {
  constructor(
	private CONDITION: any, // TODO: remove any type and figure out type with string or Field
	private CONDITION_VALUE: string,
	private OPERATOR: string,
	private REQUIREMENT_TYPE: string,
  ) { }

  public get condition() {
	return this.CONDITION;
  }

  public set condition(condition) {
	this.CONDITION = condition;
  }

  public get conditionValue() {
	return this.CONDITION_VALUE;
  }

  public set conditionValue(conditionValue) {
	this.CONDITION_VALUE = conditionValue;
  }

  public get operator() {
	return this.OPERATOR;
  }

  public set operator(operator) {
	this.OPERATOR = operator;
  }

  public get requirementType() {
	return this.REQUIREMENT_TYPE;
  }

  public set requirementType(requirementType) {
	this.REQUIREMENT_TYPE = requirementType;
  }

}
