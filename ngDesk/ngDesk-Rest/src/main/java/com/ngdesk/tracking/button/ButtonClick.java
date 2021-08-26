package com.ngdesk.tracking.button;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ButtonClick {
	
	@JsonProperty("BUTTON_ID")
	private String buttonId;
	
	@JsonProperty("LINK")
	private String link;
	
	@JsonProperty("TOTAL_CLICKS")
	private int totalClicks;
	
	@JsonProperty("CLICKED_BY")
	private List<ClickedBy> clickedBy;
	
	public ButtonClick() {
		
	}

	public ButtonClick(String buttonId, String link, int totalClicks, List<ClickedBy> clickedBy) {
		super();
		this.buttonId = buttonId;
		this.link = link;
		this.totalClicks = totalClicks;
		this.clickedBy = clickedBy;
	}

	public String getButtonId() {
		return buttonId;
	}

	public void setButtonId(String buttonId) {
		this.buttonId = buttonId;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getTotalClicks() {
		return totalClicks;
	}

	public void setTotalClicks(int totalClicks) {
		this.totalClicks = totalClicks;
	}

	public List<ClickedBy> getClickedBy() {
		return clickedBy;
	}

	public void setClickedBy(List<ClickedBy> clickedBy) {
		this.clickedBy = clickedBy;
	}
}
