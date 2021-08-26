package com.ngdesk.company.settings.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.company.dao.Company;
import com.ngdesk.company.role.dao.Role;
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
	public Company putChatSettings(@RequestBody CompanySettings chatSettings) {

		String companySubdomain = (String) chatSettings.getCompanySubdomain();
		Optional<Company> optionalCompany = companyRepository.findByCompanySubdomain(companySubdomain);
		Company company = optionalCompany.get();
		company.setMaxChatPerAgent(chatSettings.getMaxChatsPerAgent());
		if (chatSettings.getRolesWithChat() == null || chatSettings.getRolesWithChat().size() == 0) {
			throw new BadRequestException("ROLE_REQUIRED_CHAT_SETTINGS", null);
		}
		if (chatSettings.getMaxChatsPerAgent() <= 0 || chatSettings.getMaxChatsPerAgent() > 5) {
			throw new BadRequestException("MAX_NUMBER_OF_AGENTS_PER_CHAT", null);
		}
		List<Role> roles = rolesRepository.findAllRolesByCollectionName("roles_" + company.getCompanyId()).get();
		List<String> roleIds = new ArrayList<String>();

		for (Role role : roles) {
			if (!role.getName().equals("Customers") && !role.getName().equals("SystemAdmin")) {
				roleIds.add(role.getId());
			}
		}
		for (String role : chatSettings.getRolesWithChat()) {
			if (!roleIds.contains(role)) {
				throw new BadRequestException("INVALID_ROLE_SELECTED", null);
			}
		}
		company.setRolesWithChat(chatSettings.getRolesWithChat());
		companyRepository.updateEntry(company, "companies");
		return company;
	}
}
