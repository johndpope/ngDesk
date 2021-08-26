package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.workflow.channels.chat.ChatChannel;

public interface CustomChatChannelRepository {

	public Optional<ChatChannel> findChannelByName(String name, String collectionName);

}
