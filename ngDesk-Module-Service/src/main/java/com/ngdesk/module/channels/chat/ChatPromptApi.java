package com.ngdesk.module.channels.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.ChatChannelRepository;

@RestController
@RefreshScope
public class ChatPromptApi {

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	ChatService chatService;

	@PostMapping("/channels/chat/{name}/prompt")
	public ChatPrompt postChatPrompt(@PathVariable("name") String chatChannelName,
			@Valid @RequestBody ChatPrompt chatPrompt) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		if (!chatService.isChannelExist(chatChannelName,
				"channels_chat_" + authManager.getUserDetails().getCompanyId())) {
			throw new BadRequestException("CHANNEL_DOES_NOT_EXIST", null);

		}
		if (chatService.isChatPromptExist(chatChannelName, chatPrompt.getPromptName(),
				"channels_chat_" + authManager.getUserDetails().getCompanyId())) {
			throw new BadRequestException("CHAT_PROMPT_ALREADY_EXIST", null);
		}
		chatPrompt.setPromptId(UUID.randomUUID().toString());

		List<Conditions> conditions = chatPrompt.getConditions();
		if (conditions != null) {
			for (Conditions condition : conditions) {
				if (condition.getCondition().equals("STILL_ON_PAGE")
						|| condition.getCondition().equals("STILL_ON_SITE")) {
					if (condition.getOpearator() != null) {
						throw new BadRequestException("INVALID_OPERATOR", null);
					}
				}
			}
		}

		chatPrompt.setDateUpdated(new Date());
		chatPrompt.setLastUpdatedBy(authManager.getUserDetails().getUserId());
<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes
		chatChannelRepository.updateChatPrompt(chatChannelName, chatPrompt,
				"channels_chat_" + authManager.getUserDetails().getCompanyId());
		return chatPrompt;

	}

	@PutMapping("/channels/chat/{name}/prompt/{promptId}")
	public @Valid ChatPrompt putChatPrompt(@PathVariable("name") String chatChannelName,
			@PathVariable("promptId") String promptId, @Valid @RequestBody ChatPrompt chatPrompt) {

		Boolean update = false;

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		ChatChannel chatchannel = chatChannelRepository
				.findChannelByName(chatChannelName, "channels_chat_" + authManager.getUserDetails().getCompanyId())
				.orElse(null);
		if (chatchannel != null) {
			List<ChatPrompt> existingChatPrompts = chatchannel.getChatPrompt();
			if (existingChatPrompts != null) {
				List<ChatPrompt> updatedPrompts = new ArrayList<ChatPrompt>();
				for (ChatPrompt existingChatPrompt : existingChatPrompts) {
					if (existingChatPrompt.getPromptId().equals(promptId)) {
						if (!existingChatPrompt.getPromptName().equals(chatPrompt.getPromptName())
								&& chatService.isChatPromptExist(chatChannelName, chatPrompt.getPromptName(),
										"channels_chat_" + authManager.getUserDetails().getCompanyId())) {

							throw new BadRequestException("CHAT_PROMPT_ALREADY_EXIST", null);

						}
						update = true;
						List<Conditions> conditions = chatPrompt.getConditions();
						if (conditions != null) {
							for (Conditions condition : conditions) {
								if (condition.getCondition().equals("STILL_ON_PAGE")
										|| condition.getCondition().equals("STILL_ON_SITE")) {
									if (condition.getOpearator() != null) {
										throw new BadRequestException("INVALID_OPERATOR", null);
									}
								}
							}
						}

						chatPrompt.setDateUpdated(new Date());
						chatPrompt.setLastUpdatedBy(authManager.getUserDetails().getUserId());
						chatPrompt.setPromptId(existingChatPrompt.getPromptId());
						updatedPrompts.add(chatPrompt);

					} else {
						updatedPrompts.add(existingChatPrompt);
					}
				}
				if (update) {
					chatChannelRepository.updateChatPrompts(chatChannelName, updatedPrompts,
							"channels_chat_" + authManager.getUserDetails().getCompanyId());
					return chatPrompt;
				}

			}
			throw new BadRequestException("PROMPTS_DOES_NOT_EXIST", null);
		}
		throw new BadRequestException("CHANNEL_DOES_NOT_EXIST", null);

	}

	@DeleteMapping("/channels/chat/{name}/prompt/{promptId}")
	public ChatPrompt deleteChatPrompt(@PathVariable("promptId") String promptId,
			@PathVariable("name") String chatChannelName) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		if (!chatService.isChannelExist(chatChannelName,
				"channels_chat_" + authManager.getUserDetails().getCompanyId())) {
			throw new BadRequestException("CHANNEL_DOES_NOT_EXIST", null);
		}
		ChatChannel chatChannel = chatChannelRepository
				.findChannelByName(chatChannelName, "channels_chat_" + authManager.getUserDetails().getCompanyId())
				.orElse(null);

		if (chatChannel.getChatPrompt() == null || chatChannel.getChatPrompt().isEmpty()) {
			throw new BadRequestException("PROMPTS_DOES_NOT_EXIST", null);
		}

		ChatPrompt chatPrompt = chatChannel.getChatPrompt().stream()
				.filter(existingChatPrompt -> existingChatPrompt.getPromptId().equals(promptId)).findFirst()
				.orElse(null);
		if (chatPrompt == null) {
			throw new ForbiddenException("PROMPT_DOES_NOT_EXIST");

		}
		chatChannelRepository.deleteChatPrompts(chatChannelName, promptId,
				"channels_chat_" + authManager.getUserDetails().getCompanyId());
		return chatPrompt;

	}

}
