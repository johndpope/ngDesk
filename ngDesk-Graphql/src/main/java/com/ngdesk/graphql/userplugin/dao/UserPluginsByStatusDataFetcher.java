package com.ngdesk.graphql.userplugin.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.repositories.userplugin.UserPluginRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class UserPluginsByStatusDataFetcher implements DataFetcher<List<UserPlugin>> {

	@Autowired
	UserPluginRepository userPluginRepository;

	@Autowired
	AuthManager authManager;

	@Override
	public List<UserPlugin> get(DataFetchingEnvironment environment) throws Exception {
		String status = environment.getArgument("status");
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");

		String companyId = authManager.getUserDetails().getCompanyId();

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
		if (status != null) {
			if (status.equals("Published") || status.equals("Pending Approval") || status.equals("Draft")) {
				Optional<List<UserPlugin>> optional = userPluginRepository.findAllUserPluginsByStatus(companyId, status,
						pageable, "user_plugins");
				if (optional.isPresent()) {
					return optional.get();
				}
				return null;
			}
			throw new CustomGraphqlException(400, "INVALID_STATUS", null);

		}
		Optional<List<UserPlugin>> optional = userPluginRepository.findAllUserPluginsByCompanyId(companyId, pageable,
				"user_plugins");
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

}
