package com.ngdesk.company.settings.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.company.dao.Company;
import com.ngdesk.repositories.ModuleEntryRepository;

@Component
public class CompanySettingsService {

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	public void validateTeams(ChatSettings chatSettings, Company company) {
		if (chatSettings.getTeamsWhoCanChat() == null || chatSettings.getTeamsWhoCanChat().size() == 0) {
			throw new BadRequestException("TEAM_REQUIRED_CHAT_SETTINGS", null);
		}

		Optional<List<Map<String, Object>>> optionalTeams = moduleEntryRepository
				.findAllTeams("Teams_" + company.getCompanyId());
		if (optionalTeams.isPresent()) {
			List<Map<String, Object>> teams = optionalTeams.get();
			List<String> teamIds = new ArrayList<String>();
			for (Map<String, Object> team : teams) {
				teamIds.add(team.get("_id").toString());
			}
			for (String teamId : chatSettings.getTeamsWhoCanChat()) {
				if (!teamIds.contains(teamId)) {
					throw new BadRequestException("INVALID_TEAM", null);
				}
			}
		}
	}

	public void validTimezone(String timezone) {
		if (!Global.timezones.contains(timezone)) {
			throw new BadRequestException("INVALID_TIMEZONE", null);
		}
	}
}
