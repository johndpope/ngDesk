package com.ngdesk.graphql.channels.email.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

public class Channel {

	@Field("NAME")
	private String name;
	
	@Id
	private String channelId;

	public Channel() {

	}

	public Channel(String name, String channelId) {
		super();
		this.name = name;
		this.channelId = channelId;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

}
