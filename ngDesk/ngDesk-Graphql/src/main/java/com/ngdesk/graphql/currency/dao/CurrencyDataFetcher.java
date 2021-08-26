package com.ngdesk.graphql.currency.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.currency.CurrencyRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CurrencyDataFetcher implements DataFetcher<Currency> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CurrencyRepository currencyRepository;

	@Override
	public Currency get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String currencyId = environment.getArgument("currencyId");

		Optional<Currency> optionalCurrency = currencyRepository.findCurrencyById(currencyId,
				"currencies_" + companyId);
		
		if (optionalCurrency.isPresent()) {
			return optionalCurrency.get();
		}
		return null;
	}
}
