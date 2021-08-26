package com.ngdesk.graphql.knowledgebase.article.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class Article {

	@Id
	private String articleId;

	private String title;

	private String body;

	private String author;

	private List<String> visibleTo;

	private boolean openForComments;

	private String sourceLanguage;// :String(max and min 2 characters),

	private List<String> labels;

	private int order;

	private String section;

	private Date dateCreated;

	private Date dateUpdated;

	private String lastUpdatedBy;

	private String createdBy;

	private boolean publish;

	private List<CommentMessage> comments;

	private List<Attachment> attachments;

	public Article() {
	}

	public Article(String articleId, String title, String body, String author, List<String> visibleTo,
			boolean openForComments, String sourceLanguage, List<String> labels, int order, String section,
			Date dateCreated, Date dateUpdated, String lastUpdatedBy, String createdBy, boolean publish,
			List<CommentMessage> comments, List<Attachment> attachments) {
		super();
		this.articleId = articleId;
		this.title = title;
		this.body = body;
		this.author = author;
		this.visibleTo = visibleTo;
		this.openForComments = openForComments;
		this.sourceLanguage = sourceLanguage;
		this.labels = labels;
		this.order = order;
		this.section = section;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.publish = publish;
		this.comments = comments;
		this.attachments = attachments;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
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

	public boolean isOpenForComments() {
		return openForComments;
	}

	public void setOpenForComments(boolean openForComments) {
		this.openForComments = openForComments;
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

	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	public List<CommentMessage> getComments() {
		return comments;
	}

	public void setComments(List<CommentMessage> comments) {
		this.comments = comments;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
}
