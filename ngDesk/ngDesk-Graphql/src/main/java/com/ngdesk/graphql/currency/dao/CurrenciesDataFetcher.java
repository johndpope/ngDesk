package com.ngdesk.graphql.currency.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.repositories.currency.CurrencyRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CurrenciesDataFetcher implements DataFetcher<List<Currency>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CurrencyRepository currencyRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;
	
	@Autowired
	RolesRepository rolesRepository;

	@Override
	public List<Currency> get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
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

		Optional<List<Currency>> optionalCurrency = currencyRepository.findAllCurrencies(pageable,
				"currencies_" + companyId);

		String roleId = authManager.getUserDetails().getRole();
		Optional<Role> role = rolesRepository.findById(roleId, "roles_"+companyId);
		if (optionalCurrency.isPresent()) {

			if (role.get().getName().equals("SystemAdmin")) {
				return optionalCurrency.get();
			} else {
				Optional<Map<String, Object>> userEntry = moduleEntryRepository
						.findEntryById(authManager.getUserDetails().getUserId(), "Users_" + companyId);

				List<String> teamIds = (List<String>) userEntry.get().get("TEAMS");
				List<Currency> updatedCurrency = new ArrayList<Currency>();

				for (Currency currency : optionalCurrency.get()) {
					for (String currencyTeamId : currency.getTeams()) {
						if (teamIds.contains(currencyTeamId)) {
							updatedCurrency.add(currency);
							break;
						}
					}
				}
				return updatedCurrency;
			}
		}
		return null;
	}
}
