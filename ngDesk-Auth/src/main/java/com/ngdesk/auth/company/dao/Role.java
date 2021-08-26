package com.ngdesk.auth.company.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {

	@Id
	@JsonProperty("ROLE_ID")
	private String id;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	public Role() {

	}

	public Role(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
