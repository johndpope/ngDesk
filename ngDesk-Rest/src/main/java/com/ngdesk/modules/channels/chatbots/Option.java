package com.ngdesk.modules.channels.chatbots;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Option {

	@JsonProperty("CONNECTS_TO")
	private String connectsTo;

	@JsonProperty("OPTION")
	private String option;

	@JsonProperty("OPTION_ID")
	private String optionId;

	public Option() {

	}

	public Option(String connectsTo, String option, String optionId) {
		super();
		this.connectsTo = connectsTo;
		this.option = option;
		this.optionId = optionId;
	}

	public String getConnectsTo() {
		return connectsTo;
	}

	public void setConnectsTo(String connectsTo) {
		this.connectsTo = connectsTo;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String getOptionId() {
		return optionId;
	}

	public void setOptionId(String optionId) {
		this.optionId = optionId;
	}

}
