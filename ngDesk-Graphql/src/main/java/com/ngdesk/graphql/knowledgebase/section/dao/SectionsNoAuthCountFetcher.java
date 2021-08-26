package com.ngdesk.graphql.knowledgebase.section.dao;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.repositories.company.CompanyRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.section.SectionRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SectionsNoAuthCountFetcher implements DataFetcher<Integer> {

	@Autowired
	SectionRepository sectionRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	CompanyRepository companyRepository;
	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String categoryId = environment.getArgument("category");
		String companyId = (String) sessionManager.getSessionInfo().get("companyId");

		Optional<Map<String, Object>> optionalTeamId = moduleEntryRepository.getPublicTeams("Teams_" + companyId);
		String publicTeamId = "";
		if (optionalTeamId.isEmpty()) {
			throw new BadRequestException("INVALID_TEAM", null);
		} else {
			publicTeamId = optionalTeamId.get().get("_id").toString();
		}
		return sectionRepository.sectionsCountByPublicTeamId(categoryId, publicTeamId, "sections_" + companyId);
	}
}
