package com.ngdesk.workflow;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Plug {

	@JsonProperty("NAME")
	@NotNull(message = "PLUG_NAME_NULL")
	@Size(min = 1, message = "PLUG_NAME_EMPTY")
	private String name;

	@JsonProperty("ID")
	@NotNull(message = "PLUG_ID_NULL")
	@Size(min = 1, message = "PLUG_ID_EMPTY")
	private String plugId;

	@JsonProperty("ORDER")
	@NotNull(message = "PLUG_ORDER_NULL")
	private Integer order;

	public Plug() {

	}

	public Plug(@NotNull(message = "PLUG_NAME_NULL") @Size(min = 1, message = "PLUG_NAME_EMPTY") String name,
			@NotNull(message = "PLUG_ID_NULL") @Size(min = 1, message = "PLUG_ID_EMPTY") String plugId,
			@NotNull(message = "PLUG_ORDER_NULL") Integer order) {
		super();
		this.name = name;
		this.plugId = plugId;
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlugId() {
		return plugId;
	}

	public void setPlugId(String plugId) {
		this.plugId = plugId;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

}
