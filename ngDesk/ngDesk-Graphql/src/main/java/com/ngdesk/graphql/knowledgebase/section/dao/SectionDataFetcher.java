package com.ngdesk.graphql.knowledgebase.section.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.section.SectionRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SectionDataFetcher implements DataFetcher<Section> {

	@Autowired
	SectionRepository sectionRepository;

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public Section get(DataFetchingEnvironment environment) throws Exception {
		String sectionId = environment.getArgument("sectionId");
		String userId = authManager.getUserDetails().getUserId();

		Optional<Map<String, Object>> user = moduleEntryRepository.findById(userId,
				"Users_" + authManager.getUserDetails().getCompanyId());
		List<String> teams = (List<String>) user.get().get("TEAMS");

		if (roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {

			Optional<Section> optinalSection = sectionRepository.findById(sectionId,
					"sections_" + authManager.getUserDetails().getCompanyId());
			if (optinalSection.isPresent()) {
				return optinalSection.get();
			}
		} else {
			Optional<Section> optinalSectionWithTeam = sectionRepository.findByIdWithTeam(sectionId, teams,
					"sections_" + authManager.getUserDetails().getCompanyId());
			if (optinalSectionWithTeam.isPresent()) {
				return optinalSectionWithTeam.get();
			}

		}
		return null;
	}

}
