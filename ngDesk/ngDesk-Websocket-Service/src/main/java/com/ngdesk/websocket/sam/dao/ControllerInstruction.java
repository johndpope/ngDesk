package com.ngdesk.websocket.sam.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ControllerInstruction {

	@JsonProperty("CONTROLLER_ID")
	private String controllerId;

	@JsonProperty("INSTRUCTION")
	private Instruction instruction;

	public ControllerInstruction() {

	}

	public ControllerInstruction(String controllerId, Instruction instruction) {
		this.controllerId = controllerId;
		this.instruction = instruction;
	}

	public String getControllerId() {
		return controllerId;
	}

	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}

}
