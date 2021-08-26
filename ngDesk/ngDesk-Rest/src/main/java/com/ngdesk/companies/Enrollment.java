package com.ngdesk.companies;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Enrollment {

	@JsonProperty("ENABLE_SIGNUPS")
	public Boolean enabled;

	public Enrollment() {

	}

	public Enrollment(Boolean enabled) {
		super();
		this.enabled = enabled;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
