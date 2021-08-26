package com.ngdesk.module.channels.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.repositories.ChatChannelRepository;

@Component
public class ChatService {

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	Global global;

	public boolean isValidColor(String color) {
		String regex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(color);
		return matcher.matches();
	}

	public boolean isChannelExist(String name, String collectionName) {
		ChatChannel chatChannel = chatChannelRepository.findChannelByName(name, collectionName).orElse(null);
		if (chatChannel != null) {
			return true;
		}
		return false;
	}

	public boolean isChatPromptExist(String chatName, String promptName, String collectionName) {
		ChatChannel chatChannel = chatChannelRepository.findChannelByName(chatName, collectionName).orElse(null);
		if (chatChannel != null) {
			ChatPrompt existingChatPrompt = null;
			if (chatChannel.getChatPrompt() != null && !chatChannel.getChatPrompt().isEmpty()) {
				existingChatPrompt = chatChannel.getChatPrompt().stream()
						.filter(chatPrompt -> chatPrompt.getPromptName().equals(promptName)).findFirst().orElse(null);
			}
			if (existingChatPrompt != null) {
				return true;
			}
			return false;
		}
		throw new BadRequestException("CHANNEL_DOES_NOT_EXIST", null);

	}

	public ChatChannel setDefaultSettings(ChatChannel chatChannel, String timezone) {
		String settingJson = global.getFile("DefaultChatChannelSettings.json");
		settingJson = settingJson.replaceAll("DEFAULT_TIMEZONE", timezone);
		try {
			ChatChannelSettings settings = new ObjectMapper()
					.readValue(settingJson, ChatChannelSettings.class);
			chatChannel.setSettings(settings);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return chatChannel;
	}

}
