package com.ngdesk.tesseract.dao;

import java.util.List;

import com.ngdesk.tesseract.module.dao.Module;
import com.ngdesk.tesseract.module.dao.ModuleField;

public class OCRPayload {

	private String companyId;

	private String dataId;

	private String moduleId;

	private String fieldId;

	private MessageAttachment attachment;

	public OCRPayload() {

	}

	public OCRPayload(String companyId, String dataId, String moduleId, String fieldId, MessageAttachment attachment) {
		super();
		this.companyId = companyId;
		this.dataId = dataId;
		this.moduleId = moduleId;
		this.fieldId = fieldId;
		this.attachment = attachment;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public MessageAttachment getAttachment() {
		return attachment;
	}

	public void setAttachment(MessageAttachment attachment) {
		this.attachment = attachment;
	}

}
