package com.ngdesk.repositories.chat.channel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.chat.channel.dao.ChatChannel;

public interface CustomChatChannelRepository {

	public Optional<List<ChatChannel>> findAllChannels(Pageable pageable, String collectionName);

	public Optional<ChatChannel> findByChannelName(String name, String collectionName);

}
