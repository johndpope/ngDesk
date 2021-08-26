package com.ngdesk.faqs;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Faq {
	@JsonProperty("FAQ_ID")
	private String faqId;

	@JsonProperty("NAME")
	@NotEmpty(message = "FAQ_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("DESCRIPTION")
	private String description;

	@JsonProperty("QUESTIONS")
	@NotEmpty(message = "QUESTIONS_NOT_EMPTY")
	@Size(min = 5, message = "QUESTIONS_MIN")
	private List<String> questions;

	@JsonProperty("ANSWERS")
	@NotEmpty(message = "ANSWERS_NOT_EMPTY")
	@Size(min = 1, message = "ANSWERS_MIN")
	private List<String> answers;

	@JsonProperty("MODULES")
	@NotEmpty(message = "MODULES_NOT_EMPTY")
	private List<String> modules;

	@JsonProperty("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	public Faq() {
	}

	public Faq(String faqId, @NotEmpty(message = "FAQ_NAME_NOT_EMPTY") String name, String description,
			@NotEmpty(message = "QUESTIONS_NOT_EMPTY") @Size(min = 5, message = "QUESTIONS_MIN") List<String> questions,
			@NotEmpty(message = "ANSWERS_NOT_EMPTY") @Size(min = 1, message = "ANSWERS_MIN") List<String> answers,
			@NotEmpty(message = "MODULES_NOT_EMPTY") List<String> modules, String companySubdomain,
			Timestamp dateCreated, Timestamp dateUpdated, String lastUpdatedBy, String createdBy) {
		super();
		this.faqId = faqId;
		this.name = name;
		this.description = description;
		this.questions = questions;
		this.answers = answers;
		this.modules = modules;
		this.companySubdomain = companySubdomain;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
	}

	public String getFaqId() {
		return faqId;
	}

	public void setFaqId(String faqId) {
		this.faqId = faqId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getQuestions() {
		return questions;
	}

	public void setQuestions(List<String> questions) {
		this.questions = questions;
	}

	public List<String> getAnswers() {
		return answers;
	}

	public void setAnswers(List<String> answers) {
		this.answers = answers;
	}

	public List<String> getModules() {
		return modules;
	}

	public void setModules(List<String> modules) {
		this.modules = modules;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
