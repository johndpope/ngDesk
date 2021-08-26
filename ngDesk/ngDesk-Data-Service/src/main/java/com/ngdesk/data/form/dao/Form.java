package com.ngdesk.data.form.dao;

import org.springframework.data.annotation.Id;

public class Form {

	@Id
	private String formId;

	private String workflow;

	public Form() {
		super();
	}

	public Form(String formId, String workflow) {
		super();
		this.formId = formId;
		this.workflow = workflow;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

}
