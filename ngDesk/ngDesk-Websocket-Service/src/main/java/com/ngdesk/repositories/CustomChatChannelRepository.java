package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.websocket.channels.chat.ChatChannel;

public interface CustomChatChannelRepository {

	public Optional<ChatChannel> findChannelById(String id, String collectionName);

}
