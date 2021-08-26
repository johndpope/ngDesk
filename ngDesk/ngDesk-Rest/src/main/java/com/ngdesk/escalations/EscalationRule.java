package com.ngdesk.escalations;


import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class EscalationRule {
	@ApiModelProperty(notes = "Expects minutes-after rule and cannot be null")
	@JsonProperty("MINS_AFTER")
	@NotNull(message = "MINS_AFTER_NOT_NULL")
	@Min(value = 0, message = "MINS_AFTER_CANNOT_BE_NEGATIVE")
	private int minsAfter;

	@ApiModelProperty(notes = "Expects order rule and cannot be null")
	@JsonProperty("ORDER")
	@NotNull(message = "ORDER_NOT_NULL")
	@Min(value = 1, message = "ORDER_MINIMUM")
	private int order;

	@ApiModelProperty(notes = "Expects escalteTo object and cannot be null")
	@JsonProperty("ESCALATE_TO")
	@NotNull(message = "ESCALATE_TO_NOT_NULL")
	@Valid
	private EscalateTo escalateTo;

	public EscalationRule() {

	}

	public EscalationRule(@NotNull(message = "MINS_AFTER_NOT_NULL") int minsAfter,
			@NotNull(message = "ORDER_NOT_NULL") @Min(value = 1, message = "ORDER_MINIMUM") int order,
			@NotNull(message = "ESCALATE_TO_NOT_NULL") @Valid EscalateTo escalateTo) {
		super();
		this.minsAfter = minsAfter;
		this.order = order;
		this.escalateTo = escalateTo;
	}

	public int getMinsAfter() {
		return minsAfter;
	}

	public void setMinsAfter(int minsAfter) {
		this.minsAfter = minsAfter;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public EscalateTo getEscalateTo() {
		return escalateTo;
	}

	public void setEscalateTo(EscalateTo escalateTo) {
		this.escalateTo = escalateTo;
	}

}

