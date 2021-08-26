package com.ngdesk.module.channels.chat;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.company.Company;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.ChatChannelRepository;
import com.ngdesk.repositories.CompanyRepository;

@RestController
@RefreshScope
public class ChatChannelApi {

	@Autowired
	ChatService chatService;

	@Autowired
	AuthManager authManager;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	RoleService roleService;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	Global global;

	@Autowired
	SendMail sendMail;

	@GetMapping("/channels/chat_channel")
	public ChatChannel getChatChannel(@RequestParam("name") String channelName,
			@RequestParam("subdomain") String subdomain) {
		Company company = companyRepository.findCompanyBySubdomain(subdomain).orElse(null);

		if (company == null) {
			throw new BadRequestException("INVALID_COMPANY", null);
		}
		ChatChannel channel = chatChannelRepository
				.findChannelByName(channelName, "channels_chat_" + company.getCompanyId()).orElse(null);
		if (channel != null) {
			return channel;
		}
		throw new BadRequestException("CHANNEL_DOES_NOT_EXIST", null);
	}

	@PostMapping("/channels/chat")
	public ChatChannel createChatchannel(@Valid @RequestBody ChatChannel chatChannel) {

		chatChannel.setDateCreated(new Date());
		chatChannel.setDateUpdated(new Date());

		if (!chatService.isValidColor(chatChannel.getColor())) {

			throw new BadRequestException("INVALID_HEADER_COLOR", null);
		}

		if (!chatService.isValidColor(chatChannel.getTextColor())) {
			throw new BadRequestException("INVALID_TEXT_COLOR", null);
		}

		Company company = companyRepository.findCompanyBySubdomain(authManager.getUserDetails().getCompanySubdomain())
				.orElse(null);

		if (company == null) {
			throw new BadRequestException("INVALID_COMPANY", null);
		}

		if (chatChannel.getSettings() != null) {
			if (!Global.timezones.contains(chatChannel.getSettings().getBusinessRules().getTimezone())) {
				throw new BadRequestException("TIMEZONE_INVALID", null);
			}
		} else {
			String timezone = company.getTimezone();
			chatChannel = chatService.setDefaultSettings(chatChannel, timezone);
		}

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");

		}

		if (!chatChannel.getSourceType().equals("chat")) {
			throw new BadRequestException("CHANNEL_TYPE_MISMATCH", null);

		}
		ChatChannel existingChannel = chatChannelRepository.findChannelByName(chatChannel.getName(),
				"channels_chat_" + authManager.getUserDetails().getCompanyId()).orElse(null);

		if (existingChannel != null) {
			throw new BadRequestException("CHANNEL_NOT_UNIQUE", null);

		}

		return chatChannelRepository.save(chatChannel, "channels_chat_" + authManager.getUserDetails().getCompanyId());

	}

	@PostMapping("channels/chat/{name}/email")
	public List<String> emailToDevelopers(@PathVariable("name") String channelName,
			@RequestBody List<String> emailIds) {

		if (emailIds.isEmpty()) {
			throw new BadRequestException("EMAIL_ADDRESS_NOT_NULL", null);
		}

		for (String emailId : emailIds) {
			if (!EmailValidator.getInstance().isValid(emailId)) {
				throw new BadRequestException("EMAIL_INVALID", null);
			}
		}

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		ChatChannel existingChannel = chatChannelRepository
				.findChannelByName(channelName, "channels_chat_" + authManager.getUserDetails().getCompanyId())
				.orElse(null);
		if (existingChannel == null) {
			throw new BadRequestException("CHANNEL_DOES_NOT_EXIST", null);

		}
		String channelId = existingChannel.getChannelId();
		String body = global.getFile("ChatSetupInstructions.html");
		body = body.replace("SUBDOMAIN", authManager.getUserDetails().getCompanySubdomain());
		body = body.replace("CHAT_CHANNEL_ID", channelId);

		String emailFrom = "support@" + authManager.getUserDetails().getCompanySubdomain() + ".ngdesk.com";
		String emailSubject = "Instructions for setting up chat widget";

		for (String emailTo : emailIds) {
			sendMail.send(emailTo, emailFrom, emailSubject, body);
		}
		return emailIds;

	}

	@PutMapping("channels/chat/{name}")
	public ChatChannel updateChatChannel(@PathVariable("name") String channelName,
			@Valid @RequestBody ChatChannel chatChannel) {

		if (!chatService.isValidColor(chatChannel.getTextColor())) {
			throw new BadRequestException("INVALID_TEXT_COLOR", null);
		}

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		if ((chatChannel.getSettings().getBusinessRules().isActive() == true
				&& chatChannel.getSettings().getBusinessRules().getRestrictionType() != null)
				|| (chatChannel.getSettings().getBusinessRules().isActive() == false
						&& chatChannel.getSettings().getBusinessRules().getRestrictionType() == null)) {
			if (chatChannel.getSettings().getBusinessRules().isActive() == true) {
				if (!Global.timezones.contains(chatChannel.getSettings().getBusinessRules().getTimezone())) {
					throw new BadRequestException("TIMEZONE_INVALID", null);
				}

			} else {
				chatChannel.getSettings().getBusinessRules().setTimezone(null);
			}

			if (chatChannel.getSettings().getBotSettings() != null
					&& chatChannel.getSettings().getBotSettings().isEnabled()) {

				String botSelected = chatChannel.getSettings().getBotSettings().getChatBot();
				if (botSelected == null || botSelected.trim().isEmpty()) {
					throw new BadRequestException("CHAT_BOT_REQUIRED", null);
				}
			}
		} else {
			throw new BadRequestException("RESTRICTION_TYPE_NULL", null);
		}

		chatChannel.setDateUpdated(new Date());
		chatChannel.setLastUpdated(authManager.getUserDetails().getUserId());
		ChatChannel oldChannel = chatChannelRepository
				.findChannelByName(channelName, "channels_chat_" + authManager.getUserDetails().getCompanyId())
				.orElse(null);
		if (oldChannel != null) {
			if (chatChannel.getFile() != null) {
				if (chatChannel.getTitle() != null) {
					if (chatChannel.getSubTitle() != null) {
						if (chatChannel.getColor() != null) {
							if (chatChannel.getSourceType().equals("chat")) {
								if (!channelName.equalsIgnoreCase(chatChannel.getName())) {
									ChatChannel existingChannel = chatChannelRepository
											.findChannelByName(channelName,
													"channels_chat_" + authManager.getUserDetails().getCompanyId())
											.orElse(null);
									if (existingChannel != null) {
										throw new BadRequestException("CHANNEL_NOT_UNIQUE", null);
									}
								}
								return chatChannelRepository.save(chatChannel,
										"channels_chat_" + authManager.getUserDetails().getCompanyId());
							} else {
								throw new BadRequestException("CHANNEL_TYPE_MISMATCH", null);
							}
						} else {
							throw new BadRequestException("COLOR_NULL", null);
						}
					} else {
						throw new BadRequestException("SUBTITLE_NULL", null);
					}
				} else {
					throw new BadRequestException("TITLE_NULL", null);
				}
			} else {
				throw new BadRequestException("FILE_NULL", null);
			}
		} else {
			throw new BadRequestException("CHANNEL_DOES_NOT_EXIST", null);
		}

	}

	@DeleteMapping("/channels/chat/{name}")
	public ChatChannel deleteChatChannel(@PathVariable("name") String channelName) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		ChatChannel chatChannel = chatChannelRepository
				.findChannelByName(channelName, "channels_chat_" + authManager.getUserDetails().getCompanyId())
				.orElse(null);
		if (chatChannel != null) {
			return chatChannelRepository.deleteByName(channelName,
					"channels_chat_" + authManager.getUserDetails().getCompanyId());
		} else {
			throw new BadRequestException("CHANNEL_DOES_NOT_EXIST", null);
		}

	}

}
