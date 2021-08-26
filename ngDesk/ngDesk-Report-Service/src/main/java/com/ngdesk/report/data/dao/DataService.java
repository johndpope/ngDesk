package com.ngdesk.report.data.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ModuleEntryRepository;

@Service
public class DataService {

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	public Set<String> getAllTeamIds() {
		Optional<List<Map<String, Object>>> optionalTeams = moduleEntryRepository
				.findAllTeamsOfCurrentUser(authManager.getUserDetails().getCompanyId());
		List<Map<String, Object>> teams = optionalTeams.get();
		Set<String> teamIds = new HashSet<String>();
		teams.forEach(team -> {
			String teamId = team.get("_id").toString();
			teamIds.add(teamId);
		});
		return teamIds;
	}

	public Set<String> getAllTeamIdsForGivenUserId(String userId, String companyId) {
		Optional<List<Map<String, Object>>> optionalTeams = moduleEntryRepository.findAllTeamsOfGivenUser(userId,
				companyId);
		List<Map<String, Object>> teams = optionalTeams.get();
		Set<String> teamIds = new HashSet<String>();
		teams.forEach(team -> {
			String teamId = team.get("_id").toString();
			teamIds.add(teamId);
		});
		return teamIds;
	}

}
