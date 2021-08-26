package com.ngdesk.companies;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KnowledgeBaseGeneralSettings {

	@JsonProperty("ENABLE_DOCS")
	@NotNull(message = "ENABLE_DOCS_NOT_NULL")
	private boolean enableDocs;

	KnowledgeBaseGeneralSettings() {
	}

	public KnowledgeBaseGeneralSettings(@NotNull(message = "ENABLE_DOCS_NOT_NULL") boolean enableDocs) {
		super();
		this.enableDocs = enableDocs;
	}

	public boolean isEnableDocs() {
		return enableDocs;
	}

	public void setEnableDocs(boolean enableDocs) {
		this.enableDocs = enableDocs;
	}

}
