package com.ngdesk.company.settings.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.company.dao.Company;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
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
	public Company putChatSettings(@RequestBody CompanySettings companySettings) {

		if (companySettings.getCompanySubdomain() != null) {
			Optional<Company> optionalCompany = companyRepository
					.findByCompanySubdomain(companySettings.getCompanySubdomain());
			if (optionalCompany.isPresent()) {
				Company company = optionalCompany.get();
				if (companySettings.getMaxChatsPerAgent() <= 0 || companySettings.getMaxChatsPerAgent() > 5) {
					throw new BadRequestException("MAX_NUMBER_OF_AGENTS_PER_CHAT", null);
				}
				company.setMaxChatsPerAgent(companySettings.getMaxChatsPerAgent());
				companySettingsService.validTimezone(companySettings.getTimezone());
				company.setTimezone(companySettings.getTimezone());
				if (companySettings.getChatSettings() != null) {
					ChatSettings chatSettings = companySettings.getChatSettings();
					companySettingsService.validateTeams(chatSettings, company);
					company.setChatSettings(chatSettings);
				}
				return companyRepository.updateEntry(company, "companies");
			}
			throw new BadRequestException("SUBDOMAIN_DOESNOT_EXISTS", null);

		}
		throw new BadRequestException("SUBDOMAIN_NOT_NULL", null);
	}
}
