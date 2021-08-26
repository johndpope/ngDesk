package com.ngdesk.graphql.knowledgebase.section.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.section.SectionRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SectionsCountFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	SectionRepository sectionRepository;

	@Autowired
	RoleService roleService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) {

		String categoryId = environment.getArgument("category");
		;
		String roleId = authManager.getUserDetails().getRole();
		String userId = authManager.getUserDetails().getUserId();

		Optional<Map<String, Object>> user = moduleEntryRepository.findById(userId,
				"Users_" + authManager.getUserDetails().getCompanyId());
		List<String> teams = (List<String>) user.get().get("TEAMS");
		if (roleService.isSystemAdmin(roleId)) {

			return sectionRepository.count(categoryId, "sections_" + authManager.getUserDetails().getCompanyId());
		} else {
			return sectionRepository.sectionsCountByVisibleTo(categoryId, teams,
					"sections_" + authManager.getUserDetails().getCompanyId());

		}
	}
}
