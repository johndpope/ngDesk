package com.ngdesk.knowledgebase.role.dao;

import org.springframework.data.annotation.Id;

public class Role {

	@Id
	public String id;

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
