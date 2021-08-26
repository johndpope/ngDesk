package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Gallery {

	@JsonProperty("LOGO")
	@Field("LOGO")
	private Image logo;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	private String companyId;

	@JsonProperty("IMAGE_ID")
	@Field("IMAGE_ID")
	private String image_id;

	public Gallery() {
		super();
	}

	public Gallery(Image logo, String companyId, String image_id) {
		super();
		this.logo = logo;
		this.companyId = companyId;
		this.image_id = image_id;
	}

	public Image getLogo() {
		return logo;
	}

	public void setLogo(Image logo) {
		this.logo = logo;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getImage_id() {
		return image_id;
	}

	public void setImage_id(String image_id) {
		this.image_id = image_id;
	}

}
