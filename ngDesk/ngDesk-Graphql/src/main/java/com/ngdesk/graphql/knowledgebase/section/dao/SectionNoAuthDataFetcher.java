package com.ngdesk.graphql.knowledgebase.section.dao;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.section.SectionRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SectionNoAuthDataFetcher implements DataFetcher<Section> {

	@Autowired
	SectionRepository sectionRepository;

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	RoleService roleService;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public Section get(DataFetchingEnvironment environment) throws Exception {
		String sectionId = environment.getArgument("sectionId");

		String companyId = (String) sessionManager.getSessionInfo().get("companyId");

		Optional<Map<String, Object>> optionalTeamId = moduleEntryRepository.getPublicTeams("Teams_" + companyId);
		String publicTeamId = "";
		if (optionalTeamId.isEmpty()) {
			throw new BadRequestException("INVALID_TEAM", null);
		} else {
			publicTeamId = optionalTeamId.get().get("_id").toString();
		}

		Optional<Section> optinalSection = sectionRepository.findByIdWithPublicTeam(sectionId, publicTeamId,
				"sections_" + companyId);
		if (optinalSection.isPresent()) {

			return optinalSection.get();
		}
		return null;
	}

}
