package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.websocket.channels.chat.ChatChannel;

@Repository
public interface ChatChannelRepository
		extends CustomChatChannelRepository, CustomNgdeskRepository<ChatChannel, String> {

}
