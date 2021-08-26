package com.ngdesk.graphql.knowledgebase.section.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.company.CompanyRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.section.SectionRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SectionsNoAuthDataFetcher implements DataFetcher<List<Section>> {

	@Autowired
	SectionRepository sectionRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	RoleService roleService;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public List<Section> get(DataFetchingEnvironment environment) {
		String categoryId = environment.getArgument("category");
		String companyId = (String) sessionManager.getSessionInfo().get("companyId");
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
			sort = Sort.by("DATE_CREATED");
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
		Optional<Map<String, Object>> optionalTeamId = moduleEntryRepository.getPublicTeams("Teams_" + companyId);
		String publicTeamId = "";
		if (optionalTeamId.isEmpty()) {
			throw new BadRequestException("INVALID_TEAM", null);
		} else {
			publicTeamId = optionalTeamId.get().get("_id").toString();
		}
		Optional<List<Section>> optionalSections = sectionRepository.findSectionsByPublicTeamId(categoryId,
				publicTeamId, pageable, "sections_" + companyId);
		if (optionalSections.isPresent()) {
			return optionalSections.get();
		} else {
			return null;
		}
	}
}
