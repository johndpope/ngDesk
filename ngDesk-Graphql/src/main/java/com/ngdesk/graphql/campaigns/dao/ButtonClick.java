package com.ngdesk.graphql.campaigns.dao;

import java.util.List;

public class ButtonClick {

	private String buttonId;

	private String link;

	private int totalClicks;

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
