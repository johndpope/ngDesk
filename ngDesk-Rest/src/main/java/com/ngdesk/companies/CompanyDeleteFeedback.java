package com.ngdesk.companies;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanyDeleteFeedback {

	@JsonProperty("DELETION_FEEDBACK")
	@NotNull(message = "DELETION_FEEDBACK_NOT_NULL")
	@Size(min = 1, message = "DELETION_FEEDBACK_NOT_EMPTY")
	private String deletionFeedback;

	@JsonProperty("DELETION_REASON")
	@NotNull(message = "DELETION_REASON_NOT_NULL")
	@Size(min = 1, message = "DELETION_REASON_NOT_EMPTY")
	private String deletionReason;

	public CompanyDeleteFeedback() {

	}

	public CompanyDeleteFeedback(
			@NotNull(message = "DELETION_FEEDBACK_NOT_NULL") @Size(min = 1, message = "DELETION_FEEDBACK_NOT_EMPTY") String deletionFeedback,
			@NotNull(message = "DELETION_REASON_NOT_NULL") @Size(min = 1, message = "DELETION_REASON_NOT_EMPTY") String deletionReason) {
		super();
		this.deletionFeedback = deletionFeedback;
		this.deletionReason = deletionReason;
	}

	public String getDeletionFeedback() {
		return deletionFeedback;
	}

	public void setDeletionFeedback(String deletionFeedback) {
		this.deletionFeedback = deletionFeedback;
	}

	public String getDeletionReason() {
		return deletionReason;
	}

	public void setDeletionReason(String deletionReason) {
		this.deletionReason = deletionReason;
	}

}
