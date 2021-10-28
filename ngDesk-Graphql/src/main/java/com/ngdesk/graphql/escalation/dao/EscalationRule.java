package com.ngdesk.graphql.escalation.dao;

public class EscalationRule {

	private Integer minsAfter;

	private Integer order;
	
	private EscalateTo escalateTo;

	public EscalationRule() {

	}

	public EscalationRule(Integer minsAfter, Integer order, EscalateTo escalateTo) {
		super();
		this.minsAfter = minsAfter;
		this.order = order;
		this.escalateTo = escalateTo;
	}

	public Integer getMinsAfter() {
		return minsAfter;
	}

	public void setMinsAfter(Integer minsAfter) {
		this.minsAfter = minsAfter;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public EscalateTo getEscalateTo() {
		return escalateTo;
	}

	public void setEscalateTo(EscalateTo escalateTo) {
		this.escalateTo = escalateTo;
	}

}
