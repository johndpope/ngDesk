package com.ngdesk.knowledgebase;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.Attachment;

public class Article {

	@NotEmpty(message = "TITLE_REQUIRED")
	@JsonProperty("TITLE")
	@Size(max = 100, message = "INVALID_ARTICLE_TITLE_SIZE")
	private String title;

	@NotEmpty(message = "BODY_REQUIRED")
	@JsonProperty("BODY")
	private String body;

	@NotEmpty(message = "AUTHOR_REQUIRED")
	@JsonProperty("AUTHOR")
	private String author;

	@NotNull(message = "VISIBLE_TO_REQUIRED")
	@JsonProperty("VISIBLE_TO")
	private List<String> visibleTo;

	@JsonProperty("OPEN_FOR_COMMENTS")
	private boolean cancomment;

	@JsonProperty("SOURCE_LANGUAGE")
	@Size(min = 2, max = 2, message = "LANGUAGE_MUST_BE_2_CHAR")
	private String sourceLanguage;

	@JsonProperty("LABELS")
	private List<String> labels;

	@JsonProperty("ORDER")
	int order;

	@JsonProperty("SECTION")
	@NotEmpty(message = "SECTION_EMPTY")
	private String section;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	@JsonProperty("CREATED_BY")
	private String created;

	@JsonProperty("ARTICLE_ID")
	private String articleId;

	@JsonProperty("PUBLISH")
	private boolean publish;

	@JsonProperty("COMMENTS")
	@Valid
	private List<Comment> comments;

	@JsonProperty("ATTACHMENTS")
	@Valid
	private List<Attachment> attachments;

	private Article() {

	}

	public Article(
			@NotEmpty(message = "TITLE_REQUIRED") @Size(max = 100, message = "INVALID_ARTICLE_TITLE_SIZE") String title,
			@NotEmpty(message = "BODY_REQUIRED") String body, @NotEmpty(message = "AUTHOR_REQUIRED") String author,
			@NotNull(message = "VISIBLE_TO_REQUIRED") List<String> visibleTo, boolean cancomment,
			@Size(min = 2, max = 2, message = "LANGUAGE_MUST_BE_2_CHAR") String sourceLanguage, List<String> labels,
			int order, @NotEmpty(message = "SECTION_EMPTY") String section, Date dateCreated, Date dateUpdated,
			String lastUpdated, String created, String articleId, boolean publish, @Valid List<Comment> comments,
			@Valid List<Attachment> attachments) {
		super();
		this.title = title;
		this.body = body;
		this.author = author;
		this.visibleTo = visibleTo;
		this.cancomment = cancomment;
		this.sourceLanguage = sourceLanguage;
		this.labels = labels;
		this.order = order;
		this.section = section;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.created = created;
		this.articleId = articleId;
		this.publish = publish;
		this.comments = comments;
		this.attachments = attachments;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<String> getVisibleTo() {
		return visibleTo;
	}

	public void setVisibleTo(List<String> visibleTo) {
		this.visibleTo = visibleTo;
	}

	public boolean isCancomment() {
		return cancomment;
	}

	public void setCancomment(boolean cancomment) {
		this.cancomment = cancomment;
	}

	public String getSourceLanguage() {
		return sourceLanguage;
	}

	public void setSourceLanguage(String sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

}
