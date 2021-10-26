package com.ngdesk.escalation.dao;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import com.ngdesk.commons.annotations.CustomNotNull;
import io.swagger.v3.oas.annotations.media.Schema;

public class EscalationRule {

	@Schema(description = "Minutes after escalation triggered", required = true)
	@CustomNotNull(message = "NOT_NULL", values = { "MINUTES_AFTER" })
	private Integer minsAfter;

	@Schema(description = "Order of the rules", required = true)
	@CustomNotNull(message = "NOT_NULL", values = { "ESCALATION_ORDER" })
	@Min(value = 1, message = "ORDER_MINIMUM")
	private Integer order;

	@Schema(description = "Escalate to users, schedules or teams", required = true)
	@CustomNotNull(message = "NOT_NULL", values = { "ESCALATE_TO" })
	@Valid
	private EscalateTo escalateTo;

	public EscalationRule() {

	}

	public EscalationRule(Integer minsAfter, @Min(value = 1, message = "ORDER_MINIMUM") Integer order,
			@Valid EscalateTo escalateTo) {
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
