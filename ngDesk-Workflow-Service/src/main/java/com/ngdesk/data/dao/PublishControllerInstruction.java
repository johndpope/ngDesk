package com.ngdesk.data.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.workflow.sam.dao.ControllerInstruction;

public class PublishControllerInstruction {

	@JsonProperty("CONTROLLER_INSTRUCTION")
	private ControllerInstruction controllerInstruction;

	@JsonProperty("SUBDOMAIN")
	private String subdomain;

	public PublishControllerInstruction() {

	}

	public PublishControllerInstruction(ControllerInstruction controllerInstruction, String subdomain) {
		super();
		this.controllerInstruction = controllerInstruction;
		this.subdomain = subdomain;
	}

	public ControllerInstruction getControllerInstruction() {
		return controllerInstruction;
	}

	public void setControllerInstruction(ControllerInstruction controllerInstruction) {
		this.controllerInstruction = controllerInstruction;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

}
