package com.ngdesk.knowledgebase;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArrangeContent {

	@JsonProperty("ID")
	@NotEmpty(message = "ID_REQUIRED")
	String id;

	@JsonProperty("ORDER")
	@NotNull(message = "ORDER_REQUIRED")
	int order;

	public ArrangeContent() {

	}

	public ArrangeContent(@NotEmpty(message = "ID_REQUIRED") String id,
			@NotNull(message = "ORDER_REQUIRED") int order) {
		super();
		this.id = id;
		this.order = order;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
