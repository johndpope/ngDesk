package com.ngdesk.company.settings.dao;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.company.dao.Company;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.RoleRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RefreshScope
public class CompanySettingsApi {

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	RoleRepository rolesRepository;

	@Autowired
	CompanySettingsService companySettingsService;

	@Autowired
	RedisTemplate<String, ChatSettingsMessage> redisTemplate;

	@PutMapping("company/settings/account/access")
	@Operation(summary = "Put company's account level access", description = "Put company's account level access settings for entries")
	public Company putAccountLevelAccess(@RequestBody CompanySettings accountLevelAccess) {
		try {
			String companySubdomain = (String) accountLevelAccess.getCompanySubdomain();
			Optional<Company> optionalCompany = companyRepository.findByCompanySubdomain(companySubdomain);
			Company company = optionalCompany.get();
			company.setAccountLevelAccess(accountLevelAccess.isAccountLevelAccess());
			return companyRepository.updateEntry(company, "companies");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@PutMapping("company/settings/chat-settings/")
	@Operation(summary = "Put company's chat settings", description = "Put company's chat settings max chat settings and role access")
	public Company putChatSettings(@Valid @RequestBody CompanySettings companySettings) {

		if (companySettings.getCompanySubdomain() != null) {
			Optional<Company> optionalCompany = companyRepository
					.findByCompanySubdomain(companySettings.getCompanySubdomain());
			if (optionalCompany.isPresent()) {
				Company company = optionalCompany.get();

				companySettingsService.validTimezone(companySettings.getTimezone());
				company.setTimezone(companySettings.getTimezone());
				if (companySettings.getChatSettings() != null) {
					ChatSettings chatSettings = companySettings.getChatSettings();
					if (chatSettings.getMaxChatsPerAgent() <= 0 || chatSettings.getMaxChatsPerAgent() > 5) {
						throw new BadRequestException("MAX_NUMBER_OF_CHATS_PER_AGENT", null);
					}
					companySettingsService.validateTeams(chatSettings, company);
					companySettingsService.validRestrictions(chatSettings);
					company.setChatSettings(chatSettings);
				}
				ChatSettingsMessage message = new ChatSettingsMessage(company.getCompanyId(), "CHAT_SETTINGS_UPDATED",
						company.getChatSettings());
				addToQueue(message);
				return companyRepository.updateEntry(company, "companies");
			}
			throw new BadRequestException("SUBDOMAIN_DOESNOT_EXISTS", null);

		}
		throw new BadRequestException("SUBDOMAIN_NOT_NULL", null);
	}

	public void addToQueue(ChatSettingsMessage message) {
		redisTemplate.convertAndSend("chat_settings_update", message);
	}
}
