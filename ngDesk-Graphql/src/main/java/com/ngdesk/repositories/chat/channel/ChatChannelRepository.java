package com.ngdesk.repositories.chat.channel;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.chat.channel.dao.ChatChannel;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface ChatChannelRepository
		extends CustomChatChannelRepository, CustomNgdeskRepository<ChatChannel, String> {

}
