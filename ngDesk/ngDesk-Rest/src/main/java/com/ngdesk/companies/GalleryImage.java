package com.ngdesk.companies;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.Base64Validator;

public class GalleryImage {
	
	@JsonProperty("IMAGE_ID")
	private String imageId;
	
	@JsonProperty("LOGO")
	@NotNull(message = "LOGO_REQUIRED")
	LogoTemplate logo;
	
	@JsonProperty("COMPANY_ID")
	private String companyId;
	
	public GalleryImage() {
		
	}

	public GalleryImage(String imageId, @NotNull(message = "LOGO_REQUIRED") LogoTemplate logo, String companyId) {
		super();
		this.imageId = imageId;
		this.logo = logo;
		this.companyId = companyId;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public LogoTemplate getLogo() {
		return logo;
	}

	public void setLogo(LogoTemplate logo) {
		this.logo = logo;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	
	
	
}
