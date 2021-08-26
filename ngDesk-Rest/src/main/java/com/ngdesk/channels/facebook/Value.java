package com.ngdesk.channels.facebook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Value {

	@JsonProperty("created_time")
	private int createdTime;

	@JsonProperty("item")
	private String item;

	@JsonProperty("link")
	private String link;

	@JsonProperty("photos")
	private List<String> photos;

	@JsonProperty("photo")
	private String photo;

	@JsonProperty("post_id")
	private String postId;

	@JsonProperty("comment_id")
	private String commentId;

	@JsonProperty("verb")
	private String verb;

	@JsonProperty("from")
	private From from;

	@JsonProperty("published")
	private int published;

	@JsonProperty("message")
	private String message;

	public Value() {
	}

	public Value(int createdTime, String item, String link, List<String> photos, String photo, String postId,
			String commentId, String verb, From from, int published, String message) {
		super();
		this.createdTime = createdTime;
		this.item = item;
		this.link = link;
		this.photos = photos;
		this.photo = photo;
		this.postId = postId;
		this.commentId = commentId;
		this.verb = verb;
		this.from = from;
		this.published = published;
		this.message = message;
	}

	public int getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(int createdTime) {
		this.createdTime = createdTime;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public List<String> getPhotos() {
		return photos;
	}

	public void setPhotos(List<String> photos) {
		this.photos = photos;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public From getFrom() {
		return from;
	}

	public void setFrom(From from) {
		this.from = from;
	}

	public int getPublished() {
		return published;
	}

	public void setPublished(int published) {
		this.published = published;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
