package com.ngdesk.sam.swidtag;

import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Swidtags {

	@JsonProperty("SWIDTAGS")
	@Valid
	List<Swidtag> swidtags;

	public Swidtags() {

	}

	public Swidtags(List<Swidtag> swidtags) {
		super();
		this.swidtags = swidtags;
	}

	public List<Swidtag> getSwidtags() {
		return swidtags;
	}

	public void setSwidtags(List<Swidtag> swidtags) {
		this.swidtags = swidtags;
	}

}
