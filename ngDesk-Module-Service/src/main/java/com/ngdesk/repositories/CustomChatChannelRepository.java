package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.module.channels.chat.ChatChannel;
import com.ngdesk.module.channels.chat.ChatPrompt;

public interface CustomChatChannelRepository {

	public Optional<ChatChannel> findChannelByName(String name, String collectionName);

	public ChatChannel deleteByName(String name, String collectionName);

	public void updateChatPrompt(String name, ChatPrompt chatPrompt, String collectionName);

	public void updateChatPrompts(String name, List<ChatPrompt> chatPrompts, String collectionName);

	public void deleteChatPrompts(String name, String promptId, String collectionName);

}
