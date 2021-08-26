package com.ngdesk.channels.facebook;

import com.fasterxml.jackson.annotation.JsonProperty;

public class From {

	@JsonProperty("name")
	private String name;

	@JsonProperty("id")
	private String id;

	public From() {
	}

	public From(String name, String id) {
		super();
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
