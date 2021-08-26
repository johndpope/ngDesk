package com.ngdesk.graphql.knowledgebase.section.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.section.SectionRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SectionsDataFetcher implements DataFetcher<List<Section>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	SectionRepository sectionRepository;

	@Autowired
	RoleService roleService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public List<Section> get(DataFetchingEnvironment environment) throws Exception {

		String categoryId = environment.getArgument("category");
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");

		if (page == null || page < 0) {
			page = 0;
		}

		if (pageSize == null || pageSize < 0) {
			pageSize = 20;
		}
		Sort sort = null;
		if (sortBy == null) {
			sort = Sort.by("dateCreated");
		} else {
			sort = Sort.by(sortBy);
		}
		if (orderBy == null) {
			sort = sort.descending();
		} else {
			if (orderBy.equalsIgnoreCase("asc")) {
				sort = sort.ascending();
			} else {
				sort = sort.descending();
			}
		}
		Pageable pageable = PageRequest.of(page, pageSize, sort);

		String userId = authManager.getUserDetails().getUserId();

		Optional<Map<String, Object>> user = moduleEntryRepository.findById(userId,
				"Users_" + authManager.getUserDetails().getCompanyId());
		List<String> teams = (List<String>) user.get().get("TEAMS");

		if (roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			return sectionRepository.findAllWithCategoryId(categoryId, pageable,
					"sections_" + authManager.getUserDetails().getCompanyId()).get();
		} else {
			return sectionRepository.findAllWithCategoryIdAndTeam(categoryId, teams, pageable,
					"sections_" + authManager.getUserDetails().getCompanyId()).get();
		}

	}

}
