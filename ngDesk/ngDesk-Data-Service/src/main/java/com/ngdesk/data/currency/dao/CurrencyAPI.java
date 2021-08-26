package com.ngdesk.data.currency.dao;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.currency.CurrencyRepository;

@RestController
public class CurrencyAPI {
	@Autowired
	AuthManager authManager;

	@Autowired
	CurrencyRepository currencyRepository;

	@Autowired
	CurrencyService currencyService;

	@PostMapping("/currency")
	public Currency postCurrency(@Valid @RequestBody Currency currency) {

		currency.setStatus("Active");
		currencyService.isDuplicateCurrencyName(currency);
		if (currency.getTeams().isEmpty()) {
			currency.setTeams(currencyService.getDefaultTeam(currency));
		} else if (currency.getTeams() != null) {

			currencyService.isValidTeamId(currency);

		}
		currency.setDateCreated(new Date());
		currency.setDateUpdated(new Date());
		currency.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		currency.setCreatedBy(authManager.getUserDetails().getUserId());

		currencyRepository.save(currency, "currencies_" + authManager.getUserDetails().getCompanyId());

		return currency;
	}

	@PutMapping("/currency")
	public Currency putCurrency(@Valid @RequestBody Currency currency) {

		Optional<Currency> optionalCurrency = currencyRepository.findById(currency.getCurrencyId(),
				"currencies_" + authManager.getUserDetails().getCompanyId());

		if (!optionalCurrency.isEmpty()) {

			if (currency.getTeams().isEmpty()) {
				currency.setTeams(currencyService.getDefaultTeam(currency));
			} else if (currency.getTeams() != null) {

				currencyService.isValidTeamId(currency);

			}
			currencyService.isCurrencyNameChanged(optionalCurrency, currency);
			currencyRepository.removeCurrencyById(currency.getCurrencyId(),
					"currencies_" + authManager.getUserDetails().getCompanyId());
			currency.setDateUpdated(new Date());
			currency.setLastUpdatedBy(authManager.getUserDetails().getUserId());
			currencyRepository.save(currency, "currencies_" + authManager.getUserDetails().getCompanyId());

		} else {
			throw new NotFoundException("CURRENCY_NOT_FOUND", null);
		}

		return currency;

	}

	@DeleteMapping("/currency")
	public Currency deleteCurrency(@RequestParam("CURRENCY_ID") String currencyId) {

		Optional<Currency> currency = currencyRepository.findById(currencyId,
				"currencies_" + authManager.getUserDetails().getCompanyId());

		if (currency.isEmpty()) {

			throw new NotFoundException("CURRENCY_NOT_FOUND", null);
		}
		Currency currencyToDelete = currency.get();

		currencyRepository.removeCurrencyById(currencyId, "currencies_" + authManager.getUserDetails().getCompanyId());

		return currencyToDelete;
	}

}
