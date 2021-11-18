package com.ngdesk.graphql.campaigns.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class Footer {

	@Field("ADDRESS")
	private Address address;

	@Field("ALIGNMENT")
	private String alignment;

	public Footer() {

	}

	public Footer(Address address, String alignment) {
		super();
		this.address = address;
		this.alignment = alignment;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

}
