package com.ngdesk.data.currency.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.currency.CurrencyRepository;

@Component
public class CurrencyService {

	@Autowired
	AuthManager authManager;

	@Autowired
	CurrencyRepository currencyRepository;

	public List<String> getDefaultTeam(Currency currency) {

		String systemAdminId = currencyRepository
				.findTeamByName("SystemAdmin", "Teams_" + authManager.getUserDetails().getCompanyId()).get().get("_id")
				.toString();

		List<String> teamId = new ArrayList<String>();
		teamId.add(systemAdminId);

		return teamId;

	}

	public void isDuplicateCurrencyName(Currency currencyObj) {
		Optional<List<Currency>> currencies = currencyRepository
				.findAllCurrencies("currencies_" + authManager.getUserDetails().getCompanyId());
		if (!currencies.isEmpty()) {
			for (Currency currency : currencies.get()) {
				if (currency.getCurrencyName().equals(currencyObj.getCurrencyName())) {
					String[] currencyName = { currencyObj.getCurrencyName() };
					throw new BadRequestException("DUPLICATE_CURRENCY", currencyName);
				}
			}
		}
	}

	public void isValidTeamId(Currency currency) {
		List<String> teamIds = currency.getTeams();
		for (String id : teamIds) {
			Optional<Currency> team = currencyRepository.findById(id,
					"Teams_" + authManager.getUserDetails().getCompanyId());
			if (team.isEmpty()) {
				throw new BadRequestException("TEAM_NOT_FOUND", null);
			}
		}

	}

	public void isCurrencyNameChanged(Optional<Currency> optionalCurrency, @Valid Currency currency) {
		String currencyName = optionalCurrency.get().getCurrencyName();
		if (!currencyName.equals(currency.getCurrencyName())) {
			throw new BadRequestException("CURRENCY_NAME_CHANGED", null);
		}

	}

}
