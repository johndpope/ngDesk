package com.ngdesk.graphql.campaigns.dao;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

public class Footer {
	@Field("ADDRESS")
	private Address address;
	@Field("ALIGNMENT")
	private String alignment;

	public Footer() {

	}

	public Footer(@NotNull(message = "ADDRESS_NOT_NULL") Address address,
			@NotNull(message = "FOOTER_ALIGNMENT_NOT_NULL") @Pattern(regexp = "flex-start|center|flex-end", message = "FOOTER_ALIGNMENT_INVALID") String alignment) {
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
