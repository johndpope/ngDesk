package com.ngdesk.graphql.channels.email.dao;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.channels.EmailChannelRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ChannelDataFetcher implements DataFetcher<Channel>{
	
	@Autowired
	AuthManager authManager;
	
	@Autowired
	EmailChannelRepository emailChannelRepository;
	
	@Override
	public Channel get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		Map<String, Object> entry = environment.getSource();
		String fieldName = environment.getField().getName();
		if (entry.get(fieldName) != null) {
			String channelId = entry.get(fieldName).toString();
			if (!channelId.isBlank()) {
				Optional<Channel> optionalChannel = emailChannelRepository.findById(channelId, "channels_email_"+ companyId);
				if (optionalChannel.isPresent()) {
					return optionalChannel.get();
				}
			}
		}
		return null;
	}

}
